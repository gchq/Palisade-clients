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
package uk.gov.gchq.palisade.client.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.NoSubscriberEvent;
import org.greenrobot.eventbus.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.download.DownloadManager;
import uk.gov.gchq.palisade.client.download.DownloadManagerStatus;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Note that this class must be public for the subscriptions on the event bus to
 * work. SonarQube complains about this though.
 *
 * @since 0.5.0
 */
@MicronautTest
public class ResourceClientTest {

    private static final String TOKEN = "abcd-1";

    @Inject
    EmbeddedServer embeddedServer;

    private EventBus eventBus;
    private ObjectMapper objectMapper;
    private DownloadManagerStatus downloadTracker;

    private List<Object> events;
    private int port;

    @BeforeEach
    void setup() {
        this.port = embeddedServer.getPort();
        this.events = new ArrayList<>();
        this.eventBus = EventBus.builder().build();
        this.objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        this.downloadTracker = DownloadManager.createDownloadManager(b -> b
            .numThreads(1)
            .objectMapper(objectMapper));
        eventBus.register(this);
    }

    @Test
    void testMessageFlow() throws Exception {

        // as soon as the client is started, the client will start to receive resources

        var future = startClient();

        // There are two resources so we should have 3 events (2 resource and 1
        // complete)

        future.get(5, TimeUnit.HOURS);
        assertThat(events).hasSize(12);

        var event0 = getIfInstanceOf(events.get(0), ResourceReadyEvent.class);
        var event1 = getIfInstanceOf(events.get(1), ResourceReadyEvent.class);
        var event10 = getIfInstanceOf(events.get(10), ErrorEvent.class);
        var event11 = getIfInstanceOf(events.get(11), ResourcesExhaustedEvent.class);

        assertThat(event0.getResource())
            .extracting("leafResourceId", "token", "url")
            .containsExactly("resources/pi0.txt", TOKEN, "http://localhost:" + embeddedServer.getPort());

        assertThat(event1.getResource())
            .extracting("leafResourceId", "token", "url")
            .containsExactly("resources/pi1.txt", TOKEN, "http://localhost:" + embeddedServer.getPort());

        assertThat(event10.getError())
            .extracting("text")
            .isEqualTo("test error");

        assertThat(event11)
            .extracting("token")
            .isEqualTo(TOKEN);

    }

    /**
     * Receives a {@code ResourceReadyEvent} whenever the ResourceClientListener
     * gets a new resource from the Filtered Results Server
     *
     * @param resourceReadyEvent The event
     */
    @Subscribe
    public void onResourceReady(final ResourceReadyEvent resourceReadyEvent) {
        events.add(resourceReadyEvent);
    }

    /**
     * Receives a {@code ResourcesExhaustedEvent} whenever the
     * ResourceClientListener gets a complete message from the server
     *
     * @param resourcesExhaustedEvent The event
     */
    @Subscribe
    public void onResourcesExhausted(final ResourcesExhaustedEvent resourcesExhaustedEvent) {
        events.add(resourcesExhaustedEvent);
    }

    /**
     * Receives an {@code ErrorEvent} whenever the ResourceClientListener gets a
     * complete message from the server
     *
     * @param errorEvent The event
     */
    @Subscribe
    public void onError(final ErrorEvent errorEvent) {
        events.add(errorEvent);
    }

    /**
     * Receives a {@code noSubscriberEvent} when the event bus gets an event for
     * which there is no subscription. This should never happen, but if it does we
     * fail the test.
     *
     * @param noSubscriberEvent The event
     */
    @Subscribe
    public void onNoSubscriber(final NoSubscriberEvent noSubscriberEvent) {
        fail("Agggh!: " + noSubscriberEvent);
    }

    /*
     * =======================================================================
     * SUBSCRIPTIONS END
     * =======================================================================
     */

    @SuppressWarnings("unchecked")
    private <T> T getIfInstanceOf(final Object o, final Class<T> c) {
        assertThat(o).isInstanceOf(c);
        return (T) o;
    }

    private CompletableFuture<Void> startClient() {
        return ResourceClient
            .createResourceClient(b -> b
                .baseUri("ws://localhost:" + port + "/cluster/filteredResource/name/%t")
                .resourceClientListener(ResourceClientListener.createResourceClientListener(rcl -> rcl
                    .downloadManagerStatus(downloadTracker)
                    .eventBus(eventBus)
                    .objectMapper(objectMapper)
                    .token(TOKEN))))
            .connect();
    }

}
