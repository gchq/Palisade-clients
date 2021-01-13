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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.resource.WebSocketListener.Item;
import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.resource.WebSocketListener.createResourceClientListener;

/**
 * An instance of this class manages the communications to the Filtered Resource
 * Server via WebSockets
 *
 * @since 0.5.0
 */
public class WebSocketClient {

    /**
     * Provides service and configuration for a resource client
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface ResourceClientSetup {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableResourceClientSetup.Builder { // empty
        }

        /**
         * Returns the token
         *
         * @return the token
         */
        String getToken();

        /**
         * Returns the base web socket uri
         *
         * @return the base web socket uri
         */
        String getBaseUri();

        /**
         * Returns the object mapper used for (de)serialisation of websocket messages
         *
         * @return the object mapper used for (de)serialisation of websocket messages
         */
        ObjectMapper getObjectMapper();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);

    private final String token;
    private final String baseUri;
    private final ObjectMapper objectMapper;
    private final BlockingQueue<WebSocketMessage> next = new LinkedBlockingQueue<>(1);

    private WebSocket webSocket;


    /**
     * A {@code ResourceClient} manages the passing of messages to/from a websocket
     * server
     *
     * @param setup The setup instance
     */
    public WebSocketClient(final ResourceClientSetup setup) {
        Checks.checkArgument(setup);
        this.token = setup.getToken();
        this.baseUri = setup.getBaseUri();
        this.objectMapper = setup.getObjectMapper();
    }

    /**
     * Helper method to create a {@link Item} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    public static WebSocketClient createResourceClient(final UnaryOperator<ResourceClientSetup.Builder> func) {
        return new WebSocketClient(func.apply(new ResourceClientSetup.Builder()).build());
    }

    /**
     * Retrieves and removes the next message, waiting if necessary until the next
     * message arrives.
     *
     * @return the next message
     */
    public WebSocketMessage take() {
        try {
            return next.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while taking next message from queue");
        }
    }

    /**
     * Retrieves and removes the next message, or returns {@code null} if there is
     * none.
     *
     * @return the next message, or {@code null} if there is none
     */
    public WebSocketMessage poll() {
        return next.poll();
    }

    /**
     * Retrieves and removes the next message, waiting up to the specified wait time
     * if necessary for a message to become available.
     *
     * @param timeout how long to wait before giving up, in units of {@code unit}
     * @param unit    a {@code TimeUnit} determining how to interpret the
     *                {@code timeout} parameter
     * @return the the next message, or {@code null} if the specified waiting time
     *         elapses before a message is available
     */
    public WebSocketMessage poll(final long timeout, final TimeUnit unit) {
        try {
            return next.poll(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while taking next message from queue");
        }
    }

    /**
     * Connect to the server and start communications
     *
     * @return this for fluent usage
     */
    public WebSocketClient connect() {

        // as soon as a client subscribes, the connection to the filtered resource
        // service should be instantiated and messages should start to be emitted

        var uri = URI.create(baseUri.replace("%t", token));

        LOGGER.debug("Connecting to websocket at: {}", uri);

        this.webSocket = HttpClient
            .newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(uri, createResourceClientListener(b -> b
                .eventsHandler((final WebSocketMessage msg) -> {
                    try {
                        LOGGER.debug("Emitted : {}", msg);
                        next.put(msg); // block if the last message has not been taken
                        if (msg instanceof CompleteMessage) {
                            close();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Thread interrupted while putting next message onto queue");
                    }
                })
                .objectMapper(objectMapper)
                .token(token)))
            .join();

        LOGGER.debug("WebSocket created to handle token: {}", token);

        return this;

    }

    private void put(final WebSocketMessage msg) {
        LOGGER.debug("Emitted : {}", msg);
        try {
            next.put(msg); // block if the last message has not been taken
            if (msg instanceof CompleteMessage) {
                close();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while putting next message onto queue");
        }
    }

    private void close() {
        if (webSocket != null) {
            this.webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").join();
            LOGGER.debug("--> CLOSE request sent");
            this.webSocket = null;
        }
    }
}
