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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.fuse.client.ResourceTreeClient;
import uk.gov.gchq.palisade.client.fuse.client.ResourceTreeClient.ResourceTreeWithContext;
import uk.gov.gchq.palisade.client.fuse.fs.ResourceTreeFS;
import uk.gov.gchq.palisade.client.fuse.tree.impl.LeafResourceNode;
import uk.gov.gchq.palisade.client.internal.dft.DefaultClient;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class FuseClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuseClient.class);
    private final ResourceTreeClient client;

    public FuseClient(String clientUri) {
        this.client = new ResourceTreeClient(new DefaultClient().connect(clientUri));
    }

    public static void main(final String... args) {
        if (args.length == 4) {
            new FuseClient(args[1])
                    .mount(args[2], args[3]);
        } else {
            LOGGER.error("Usage: {} <clientUri> <resourceId> <mountDir>", args[0]);
        }
    }

    public void mount(final String resourceId, final String mountDir) {
        Path mountPath = Paths.get(mountDir);

        ResourceTreeWithContext tree = client.register(resourceId);
        Function<LeafResourceNode, InputStream> reader = node -> client.read(tree.getToken(), node);
        ResourceTreeFS fuseFs = new ResourceTreeFS(tree, reader);

        try {
            fuseFs.mount(mountPath, true, false);
        } finally {
            fuseFs.umount();
        }
    }
}
