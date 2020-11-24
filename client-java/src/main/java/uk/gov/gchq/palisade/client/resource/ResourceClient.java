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

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.UnaryOperator;

/**
 * An instance of this class manages the comunications to the Filtered Resource
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
        URI getBaseUri();

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

    private WebSocket webSocket;

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
     *
     * @return a completeable future
     */
    public CompletableFuture<Void> close() {
        LOGGER.debug("closing websocket!");
        // this will close the output side of the websocket
        // until the listener receives and onClose(), the input side of the websocket is
        // till open. this client should abort the websocket if no data arrives after
        // around 30 seconds. Not sure how to do this yet.
        return webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "")
            .thenRun(() -> {
                LOGGER.debug("Websocket output is now closed");
                latch.countDown(); // release completeable future
            });
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

        /*
         * Tasks: 1. Stop the resource client 2. Update any completable future instance
         * as completed and signal
         */

        latch.countDown();

    }

    /**
     * Connect to the server and start communications
     *
     * @return this for fluent usage
     */
    public CompletableFuture<Void> connect() {

        var uri = createUri(getBaseUri(), getToken());

        LOGGER.debug("Connecting to websocket at: {}", uri);

        if (webSocket != null && webSocket.isOutputClosed()) {
            throw new IllegalStateException(
                "open() has already been called and this client has been closed. Create a new instance.");
        }

        // register this resource client with the same eventbus that the listener uses
        // this is so we can get notified when there are no more resources

        setup.getResourceClientListener().getEventBus().register(this);

        this.webSocket = HttpClient
            .newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(uri, getListener())
            .join();

        LOGGER.debug("Websocket created to handle token: {}", getToken());

        return CompletableFuture.runAsync(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // swallow
            }
        });
    }

    private URI getBaseUri() {
        return getSetup().getBaseUri();
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

    /**
     * Returns true if this resource client is still open
     *
     * @return true if this resource client is still open
     */
    public boolean isOpen() {
        return webSocket != null && !webSocket.isOutputClosed();
    }

    private static URI createUri(final URI baseUri, final String endpoint) {

        assert baseUri != null : "Need the base uri";
        assert baseUri != null : "Need the uri endpoint to append to the base uri";

        var baseUriStr = baseUri.toString();
        var uri = new StringBuilder();
        if (baseUriStr.endsWith("/")) {
            uri.append(baseUriStr.substring(0, baseUriStr.length() - 2));
        } else {
            uri.append(baseUriStr);
        }
        if (!endpoint.startsWith("/")) {
            uri.append("/");
        }
        uri.append(endpoint);
        try {
            return new URI(uri.toString());
        } catch (URISyntaxException e) {
            throw new ClientException("Invalid websocket uri: " + uri, e);
        }
    }

}
