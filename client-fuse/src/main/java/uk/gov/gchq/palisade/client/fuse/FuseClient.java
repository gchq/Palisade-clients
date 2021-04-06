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

package uk.gov.gchq.palisade.client.fuse;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.akka.AkkaClient;
import uk.gov.gchq.palisade.client.fuse.client.ResourceTreeClient;
import uk.gov.gchq.palisade.client.fuse.client.ResourceTreeClient.ResourceTreeWithContext;
import uk.gov.gchq.palisade.client.fuse.fs.ResourceTreeFS;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FuseClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuseClient.class);
    private final ResourceTreeClient client;
    private final Map<String, String> dataServiceMap;

    public FuseClient(final String palisadeService, final String filteredResourceService, final String dataService) {
        ActorSystem actorSystem = ActorSystem.create();
        AkkaClient akkaClient = new AkkaClient(palisadeService, filteredResourceService, actorSystem);
        this.client = new ResourceTreeClient(actorSystem, akkaClient);
        this.dataServiceMap = Map.of("data-service", dataService);
    }

    public static void main(final String... args) {
        if (args.length == 8) {
            new FuseClient(args[1], args[2], args[3])
                    .mount(args[4], args[5], args[6], args[7]);
        } else {
            LOGGER.error("Usage: {} <palisadeService> <filteredResourceService> <dataService> <userId> <purpose> <resourceId> <mountDir>", args[0]);
        }
    }

    public void mount(final String userId, final String purpose, final String resourceId, final String mountDir) {
        Map<String, String> env = Stream.of(dataServiceMap, Map.of("userId", userId, "purpose", purpose))
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        URI resourceIdUri = URI.create(resourceId);
        Path mountPath = Paths.get(mountDir);

        ResourceTreeWithContext tree = client.register(resourceIdUri, env);
        ResourceTreeFS fuseFs = new ResourceTreeFS(tree, node -> client.read(tree.getToken(), node));

        try {
            fuseFs.mount(mountPath, true, false);
        } finally {
            fuseFs.umount();
        }
    }
}
