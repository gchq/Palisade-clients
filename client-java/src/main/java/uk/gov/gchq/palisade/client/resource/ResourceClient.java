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
package uk.gov.gchq.palisade.client.resource;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.UnaryOperator;

/**
 * An instance of this class manages the communications to the Filtered Resource
 * Server via webn sockets
 *
 * @since 0.5.0
 */
public class ResourceClient {

    /**
     * Provides service and configuration for a resource client
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IResourceClientSetup {

        /**
         * Returns the base web socket uri
         *
         * @return the base web socket uri
         */
        String getBaseUri();

        /**
         * Returns the listener that will handle communication to the server
         *
         * @return the listener
         */
        ResourceClientListener getResourceClientListener();

        /**
         * Returns the token
         *
         * @return the token
         */
        @Value.Derived
        default String getToken() {
            return getResourceClientListener().getToken();
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceClient.class);
    private static final String EVENT_CAUGHT = "---> Caught event: {}";

    private final ResourceClientSetup setup;
    private final CountDownLatch latch;


    /**
     * A {@code ResourceClient} manages the passing of messages to/from a websocket
     * server
     *
     * @param setup The setup instance
     */
    public ResourceClient(final ResourceClientSetup setup) {
        this.setup = Checks.checkArgument(setup);
        this.latch = new CountDownLatch(1);

    }

    /**
     * Helper method to create a {@link Message} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public static ResourceClient createResourceClient(final UnaryOperator<ResourceClientSetup.Builder> func) {
        return new ResourceClient(func.apply(ResourceClientSetup.builder()).build());
    }

    /**
     * Requests the closure of this resource client, return a completeable future,
     * that when complete will signify the successful closure.
     */
    public void close() {
        LOGGER.debug("closing resource client!");
        latch.countDown(); // release completeable future
    }

    /**
     * Receives a {@code ResourcesExhaustedEvent} there are no more resources
     *
     * @param event The event to be handled
     */
    @SuppressWarnings("java:S2325") // make static
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onNoMoreResources(final ResourcesExhaustedEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
        latch.countDown();
    }

    /**
     * Connect to the server and start communications
     *
     * @return this for fluent usage
     */
    public CompletableFuture<Void> connect() {

        var uri = getUri();

        LOGGER.debug("Connecting to websocket at: {}", uri);

        // register this resource client with the same eventbus that the listener uses
        // this is so we can get notified when there are no more resources

        setup.getResourceClientListener().getEventBus().register(this);

        var webSocket = HttpClient
            .newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(uri, getListener())
            .join();

        LOGGER.debug("Websocket created to handle token: {}", getToken());

        return CompletableFuture
            .runAsync(this::awaitNoMoreResources)
            .thenCompose(v -> webSocket.sendClose(WebSocket.NORMAL_CLOSURE, ""))
            .thenRun(() -> {
                LOGGER.debug("Websocket output is now closed");
                latch.countDown(); // release completeable future
            });
    }

    private void awaitNoMoreResources() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // swallow
        }
    }

    private URI getUri() {
        return URI.create(getSetup().getBaseUri().replace("%t", setup.getToken()));
    }

    private ResourceClientListener getListener() {
        return getSetup().getResourceClientListener();
    }

    private ResourceClientSetup getSetup() {
        return setup;
    }

    private String getToken() {
        return getSetup().getToken();
    }

}
