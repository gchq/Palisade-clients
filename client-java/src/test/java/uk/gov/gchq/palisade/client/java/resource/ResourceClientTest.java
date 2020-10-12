/*
 * Copyright 2020 Crown Copyright
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
package uk.gov.gchq.palisade.client.java.resource;

import io.micronaut.context.ApplicationContext;
import org.glassfish.tyrus.server.Server;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.download.DownloadManager;
import uk.gov.gchq.palisade.client.java.receiver.LoggingReceiver;

import javax.websocket.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.eventbus.*;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceClientTest {

    private static final String TOKEN = "abcd-1";
    private static Server server;

    private int eventCount = 0;

    @Test
    void test() throws Exception {
        try {
            server = new Server("localhost", 8081, "/", Map.of(), ServerSocket.class);
            server.start();
            startClient();
            awaitEvents();
            assertThat(eventCount).isEqualTo(2);
        } finally {
            server.stop();
        }
    }

    @Subscribe
    public void handleResourceEvent(ResourceReadyEvent resourceReadyEvent) {
        eventCount++;
    }

    private ResourceClient startClient() throws Exception {

        var eb = new EventBus("bus-test");

        eb.register(this);

        var appCtx = ApplicationContext.run();

        var dm = DownloadManager
            .createDownloadManager(b -> b
                .id("dlm-test")
                .eventBus(eb)
                .receiverSupplier(() -> new LoggingReceiver())
                .clientContext(appCtx.getBean(ClientContext.class)));

        var rc = ResourceClient
            .createResourceClient(b -> b
                .token(TOKEN)
                .eventBus(eb)
                .mapper(new ObjectMapper().registerModule(new GuavaModule()).registerModule(new Jdk8Module()))
                .downloadTracker(dm.getDownloadTracker()));

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(rc, new URI("ws://localhost:8081/name"));

        return rc;

    }

    private void awaitEvents() throws Exception {
        var tries = 40;
        while (eventCount < 50 && tries-- > 0) {
            Thread.sleep(100);
        }
        if (tries == 0) {
            throw new TimeoutException("Timeout reached while waitin for all the resource events");
        }

    }

}
