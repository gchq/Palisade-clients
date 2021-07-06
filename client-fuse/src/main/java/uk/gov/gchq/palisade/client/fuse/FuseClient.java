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
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultClient;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Palisade Client to mount query response as a FUSE filesystem
 */
public class FuseClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuseClient.class);

    private static final int MIN_ARGS_LEN = 4;
    private static final int CLIENT_URI_INDEX = 1;
    private static final int RESOURCE_ID_INDEX = 2;
    private static final int MOUNT_DIR_INDEX = 3;
    private static final int CONTEXT_INDEX = 4;

    private static final String KEY_VALUE_SEP = "=";
    private static final int KEY_VALUE_LEN = 2;
    private static final int KEY_INDEX = 0;
    private static final int VALUE_INDEX = 1;

    private final ResourceTreeClient client;

    /**
     * Create a new instance of the client, specifying the {@link DefaultClient#connect(String)} uri config string.
     *
     * @param clientUri the client uri string, e.g. pal://cluster/?userid=Alice
     */
    public FuseClient(final String clientUri) {
        this.client = new ResourceTreeClient(new DefaultClient().connect(clientUri));
    }

    /**
     * Run a simple CLI application for this client, taking the clientUri, resourceId and mountDir from command-line args
     *
     * @param args Command-line arguments, expected to contain the application name and three-or-more passed arguments.
     *             The args list should be ordered [this-jar-name, client-uri-config, resource-id, local-mount-point, context-map...]
     *             e.g. [client-fuse.jar, pal://cluster/?userid=Alice, file:/data/local-data-store/, /mnt/palisade, purpose=SALARY]
     */
    public static void main(final String... args) {
        String jarName = args[0];

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
        }

        if (args.length >= MIN_ARGS_LEN) {
            // Parse command-line args
            Map<String, String> context = new HashMap<>();
            for (int i = CONTEXT_INDEX; i < args.length; i++) {
                String[] keyValue = args[i].split(KEY_VALUE_SEP, KEY_VALUE_LEN);
                if (keyValue.length == KEY_VALUE_LEN) {
                    context.put(keyValue[KEY_INDEX], keyValue[VALUE_INDEX]);
                } else {
                    throw new IllegalArgumentException("Expected additional args '<key>=<value>' to be parsed as ['key', 'value'], but was " + Arrays.toString(keyValue));
                }
            }

            String clientUri = args[CLIENT_URI_INDEX];
            String resourceId = args[RESOURCE_ID_INDEX];
            String mountDir = args[MOUNT_DIR_INDEX];

            // Mount and block for lifetime of the application
            new FuseClient(clientUri)
                .mount(resourceId, mountDir, context);
        } else {
            LOGGER.error("Usage: {} <clientUri> <resourceId> <mountDir> [<contextKey>=<value> ...]", jarName);
        }
    }

    /**
     * Register a request with Palisade using a configured client.
     * Mount the fuse directory and block until application exit.
     * Attempt to gracefully unmount on application exit.
     *
     * @param resourceId the requested resourceId
     * @param mountDir   the target mount directory
     * @param context    the context for the Palisade request
     */
    public void mount(final String resourceId, final String mountDir, final Map<String, String> context) {
        Path mountPath = Paths.get(mountDir).toAbsolutePath();

        ResourceTreeWithContext tree = client.register(resourceId, context);
        Function<LeafResourceNode, InputStream> reader = node -> client.read(tree.getToken(), node);
        ResourceTreeFS fuseFs = new ResourceTreeFS(tree, reader, URI.create(resourceId).getScheme());

        try {
            LOGGER.info("Mounted at {}, press <Ctrl-C> to unmount and exit", mountPath);
            fuseFs.mount(mountPath, true);
        } finally {
            fuseFs.umount();
            LOGGER.info("Unmounted {}", mountPath);
        }
    }
}
