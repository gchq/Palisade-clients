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

import uk.gov.gchq.palisade.client.QueryItem;
import uk.gov.gchq.palisade.client.fuse.tree.ResourceTree;
import uk.gov.gchq.palisade.client.fuse.tree.impl.LeafResourceNode;
import uk.gov.gchq.palisade.client.internal.dft.DefaultQueryResponse;
import uk.gov.gchq.palisade.client.internal.dft.DefaultSession;
import uk.gov.gchq.palisade.client.internal.model.PalisadeResponse;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

@SuppressWarnings("unchecked")
public class ResourceTreeClient {
    public static class ResourceTreeWithContext extends ResourceTree {
        private final String token;

        public ResourceTreeWithContext(final String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

    private final DefaultSession session;

    public ResourceTreeClient(final DefaultSession session) {
        this.session = session;
    }

    static URI stripScheme(final URI uri) {
        return URI.create(uri.getSchemeSpecificPart());
    }

    static String stripScheme(final String uri) {
        return stripScheme(URI.create(uri)).toString();
    }

    Resource stripScheme(final Resource resource) {
        return resource.id(stripScheme(resource.getId()));
    }

    static String reapplyScheme(final String path) {
        return "file:" + URI.create(path).getSchemeSpecificPart();
    }

    Resource reapplyScheme(final Resource resource) {
        return resource.id(reapplyScheme(resource.getId()));
    }

    String substDataServiceAddress(final Map<String, String> env, final String serviceName) {
        return env.getOrDefault(serviceName, serviceName);
    }

    UnaryOperator<Resource> substDataServiceAddress(final Map<String, String> env) {
        return resource -> {
            if (resource instanceof LeafResource) {
                ((LeafResource) resource).connectionDetail(new SimpleConnectionDetail()
                        .serviceName(substDataServiceAddress(env, ((LeafResource) resource).getConnectionDetail().createConnection())));
            }
            return resource;
        };
    }

    <T extends Resource> T formatResource(final T resource, final UnaryOperator<Resource> formatter) {
        if (resource instanceof ChildResource) {
            ((ChildResource) resource).parent(formatResource(((ChildResource) resource).getParent(), formatter));
        }
        return (T) formatter.apply(resource);
    }

    public ResourceTreeWithContext register(final String resourceId) {
        CompletableFuture<DefaultQueryResponse> response = session
                .createQuery(resourceId)
                .execute()
                .thenApply(DefaultQueryResponse.class::cast);
        CompletableFuture<ResourceTreeWithContext> resourceTree = response
                .thenApply(DefaultQueryResponse::getPalisadeResponse)
                .thenApply(PalisadeResponse::getToken)
                .thenApply(ResourceTreeWithContext::new);
        CompletableFuture<ResourceTreeWithContext> populator = response
                .thenApply(DefaultQueryResponse::stream)
                .thenCombine(
                        resourceTree,
                        (stream, tree) -> {
                            stream.subscribe((OnNextStubSubscriber<QueryItem>) queryItem -> {
                                LeafResource leaf = queryItem.asResource();
                                UnaryOperator<Resource> formatter = this::stripScheme;
                                tree.add(formatResource(leaf, formatter));
                            });
                            return tree;
                        }
                );
        return populator.join();
    }

    public InputStream read(final String token, final LeafResourceNode node) {
        UnaryOperator<Resource> formatter = this::reapplyScheme;
        QueryItem queryItem = new LeafResourceQueryItem(formatResource(node.get(), formatter), token);
        return session
                .fetch(queryItem)
                .getInputStream();
    }
}
