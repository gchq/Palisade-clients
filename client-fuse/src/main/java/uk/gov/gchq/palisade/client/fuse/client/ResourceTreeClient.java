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

import akka.actor.ActorSystem;

import uk.gov.gchq.palisade.client.akka.AkkaClient;
import uk.gov.gchq.palisade.client.fuse.tree.ResourceTree;
import uk.gov.gchq.palisade.client.fuse.tree.impl.LeafResourceNode;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

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

    private ActorSystem actorSystem;
    private AkkaClient client;

    public ResourceTreeClient(final ActorSystem actorSystem, final AkkaClient client) {
        this.actorSystem = actorSystem;
        this.client = client;
    }

    URI stripScheme(final URI uri) {
        return URI.create(uri.getSchemeSpecificPart());
    }

    String stripScheme(final String uri) {
        return stripScheme(URI.create(uri)).toString();
    }

    Resource stripScheme(final Resource resource) {
        return resource.id(stripScheme(resource.getId()));
    }


    String reapplyScheme(final String path) {
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

    Resource formatResource(final Resource resource, final UnaryOperator<Resource> formatter) {
        if (resource instanceof ChildResource) {
            ((ChildResource) resource).parent((ParentResource) formatResource(((ChildResource) resource).getParent(), formatter));
        }
        return formatter.apply(resource);
    }

    public ResourceTreeWithContext register(final URI resourceId, final Map<String, String> env) {
        String userId = env.get("userId");
        Map<String, String> context = Map.of("purpose", env.get("purpose"));
        CompletableFuture<ResourceTreeWithContext> resourceTree = client.register(userId, resourceId.toString(), context)
                .thenApply(ResourceTreeWithContext::new)
                .toCompletableFuture();
        resourceTree.thenCompose(tree ->
                client.fetchSource(tree.getToken())
                        .runForeach(leaf -> tree.add(formatResource(formatResource(leaf, this::stripScheme), substDataServiceAddress(env))), actorSystem)
                        .toCompletableFuture())
                .join();
        return resourceTree.join();
    }

    public InputStream read(final String token, final LeafResourceNode node) {
        return client.read(token, (LeafResource) formatResource(node.get(), this::reapplyScheme));
    }
}
