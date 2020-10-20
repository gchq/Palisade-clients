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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.glassfish.tyrus.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.ClientConfig;
import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.download.DownloadTracker;
import uk.gov.gchq.palisade.client.java.util.Bus;

import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

import java.net.URI;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@MicronautTest
@Property(name = ClientConfig.Client.URL_PROPERTY, value = "http://localhost:8081")
@Property(name = ClientConfig.Download.THREADS_PROPERTY, value = "1")
@Property(name = "micronaut.server.port", value = "8081")
class ResourceClientTest implements ApplicationEventListener<ResourceReadyEvent> {

    private static final String TOKEN = "abcd-1";

    private static int eventCount = 0;

    @Inject
    ClientContext clientContext;

    @Inject
    EmbeddedServer embeddedServer;

    @Inject
    Bus bus;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    DownloadTracker downloadTracker;

    private Server server;

    @BeforeEach
    void setup() throws Exception {
        embeddedServer.start();
        server = new Server("localhost", 8082, "/", Map.of(), ServerSocket.class);
        server.start();
    }

    @AfterEach
    void tearDown() {
        embeddedServer.stop();
        server.stop();
    }

    @Disabled("skipped as this test fails intermittently")
    @Test
    void testMessageFlow() throws Exception {
        startClient();
        await().atMost(ofSeconds(5)).until(() -> eventCount != 2);
        assertThat(eventCount).isEqualTo(2);
    }

    private static final Logger LOG = LoggerFactory.getLogger(ResourceClientTest.class);

    @Override
    @EventListener
    public void onApplicationEvent(final ResourceReadyEvent resourceReadyEvent) {
        LOG.debug("EVENT");
        eventCount++;
    }

    private ResourceClient startClient() throws Exception {
        ApplicationContext.run();
        ResourceClient rc = new ResourceClient(TOKEN, bus, objectMapper, downloadTracker);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(rc, new URI("ws://localhost:8082/name"));

        return rc;
    }

}
