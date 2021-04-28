/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.client.fuse.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.fuse.tree.ResourceTree;
import uk.gov.gchq.palisade.client.fuse.tree.impl.LeafResourceNode;
import uk.gov.gchq.palisade.client.java.QueryItem;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultQueryResponse;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultSession;
import uk.gov.gchq.palisade.client.java.internal.model.PalisadeResponse;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.function.UnaryOperator;

/**
 * Palisade client that stores returned resources into a {@link ResourceTree}.
 * This allows hierarchical querying of returned resources before requesting to read.
 * Additionally, the uri scheme is stripped from resourceIds, which allows for using
 * id's directly as the FUSE mount paths.
 */
@SuppressWarnings("unchecked")
public class ResourceTreeClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTreeClient.class);

    /**
     * Wraps a resource tree with some additional context, in this case the token of
     * the Palisade query.
     */
    public static class ResourceTreeWithContext extends ResourceTree {
        private final String token;

        /**
         * Construct a new  ResourceTree and keep track of the given token.
         *
         * @param token the response from the Palisade Service used to identify this
         *              request with the Data Service.
         */
        public ResourceTreeWithContext(final String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

    private final DefaultSession session;

    /**
     * Construct a new instance of the client using a connected session from e.g.
     * the {@link uk.gov.gchq.palisade.client.java.internal.dft.DefaultClient}.
     *
     * @param session a connected session pointing at an instance of Palisade
     */
    public ResourceTreeClient(final DefaultSession session) {
        this.session = session;
    }

    protected static URI stripScheme(final URI uri) {
        return URI.create(uri.getSchemeSpecificPart());
    }

    protected static String stripScheme(final String uri) {
        return stripScheme(URI.create(uri)).toString();
    }

    protected Resource stripScheme(final Resource resource) {
        return resource.id(stripScheme(resource.getId()));
    }

    protected static String reapplyScheme(final String path) {
        return "file:" + URI.create(path).getSchemeSpecificPart();
    }

    protected Resource reapplyScheme(final Resource resource) {
        return resource.id(reapplyScheme(resource.getId()));
    }

    protected String substDataServiceAddress(final Map<String, String> env, final String serviceName) {
        return env.getOrDefault(serviceName, serviceName);
    }

    protected UnaryOperator<Resource> substDataServiceAddress(final Map<String, String> env) {
        return (Resource resource) -> {
            if (resource instanceof LeafResource) {
                ((LeafResource) resource).connectionDetail(new SimpleConnectionDetail()
                    .serviceName(substDataServiceAddress(env, ((LeafResource) resource).getConnectionDetail().createConnection())));
            }
            return resource;
        };
    }

    protected <T extends Resource> T formatResource(final T resource, final UnaryOperator<Resource> formatter) {
        if (resource instanceof ChildResource) {
            ((ChildResource) resource).parent(formatResource(((ChildResource) resource).getParent(), formatter));
        }
        return (T) formatter.apply(resource);
    }

    /**
     * Register a request with Palisade.
     *
     * @param resourceId the resourceId to query, all returned resources will be this
     *                   resource or its children.
     * @param context    the additional context for the request, e.g. purpose.
     * @return a {@link ResourceTree} paired with the {@link PalisadeResponse#getToken()}
     * for this query.
     */
    public ResourceTreeWithContext register(final String resourceId, final Map<String, String> context) {
        LOGGER.debug("Registering request for resource {} with context {}", resourceId, context);

        // Execute the request to Palisade and receive a response
        CompletableFuture<DefaultQueryResponse> response = session
            .createQuery(resourceId, context)
            .execute()
            .thenApply(DefaultQueryResponse.class::cast);

        // Create a resource tree for the returned resources
        CompletableFuture<ResourceTreeWithContext> resourceTree = response
            .thenApply(DefaultQueryResponse::getPalisadeResponse)
            .thenApply((PalisadeResponse palisadeResponse) -> {
                LOGGER.debug("Registered request and received token {}", palisadeResponse.getToken());
                return palisadeResponse.getToken();
            })
            .thenApply(ResourceTreeWithContext::new);

        // Get the returned (stream of) resources and add them to the tree
        CompletableFuture<ResourceTreeWithContext> populator = response
            .thenApply(DefaultQueryResponse::stream)
            .thenCombine(resourceTree,
                (Publisher<QueryItem> stream, ResourceTreeWithContext tree) -> {
                    stream.subscribe((OnNextStubSubscriber<QueryItem>) queryItem -> {
                        UnaryOperator<Resource> formatter = this::stripScheme;
                        LeafResource leaf = queryItem.asResource();
                        LOGGER.debug("Adding resource {} to tree", leaf.getId());
                        // Strip the URI scheme from the resource
                        formatResource(leaf, formatter);
                        tree.add(leaf);
                    });
                    return tree;
                });

        // Join on the stream completion (closed websocket)
        return populator.join();
    }

    /**
     * Read a resource from the Data Service.
     *
     * @param token the token attached to the {@link ResourceTreeWithContext} that was
     *              returned by the Palisade Service on registering the query.
     * @param node  a leaf node (therefore {@link LeafResource}) from the {@link ResourceTree}
     * @return the {@link InputStream} to read this resource.
     */
    public InputStream read(final String token, final LeafResourceNode node) {
        UnaryOperator<Resource> formatter = this::reapplyScheme;
        QueryItem queryItem = new LeafResourceQueryItem(formatResource(node.get(), formatter), token);
        LOGGER.debug("Downloading resource {}", queryItem.asResource().getId());
        return session
            .fetch(queryItem)
            .getInputStream();
    }
}
