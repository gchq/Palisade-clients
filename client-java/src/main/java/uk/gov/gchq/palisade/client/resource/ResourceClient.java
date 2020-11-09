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

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
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
         * Returns the token
         *
         * @return the token
         */
        @Value.Derived
        default String getToken() {
            return getResourceClientListener().getToken();
        }

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
        ResourceClientListenr getResourceClientListener();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceClient.class);

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

    private final ResourceClientSetup setup;

    private WebSocket webSocket;

    /**
     * A {@code ResourceClient} manages the passing of messages to/from a websocket
     * server
     *
     * @param setup The setup instance
     */
    public ResourceClient(final ResourceClientSetup setup) {
        this.setup = Checks.checkArgument(setup);

    }

    /**
     * Connect to the server and start communications
     *
     * @return this for fluent usage
     */
    public ResourceClient connect() {

        var uri = URI.create(getBaseUri() + "/" + getToken());

        LOGGER.debug("Connecting to websocket at: {}", uri);

        if (webSocket != null && webSocket.isOutputClosed()) {
            throw new IllegalStateException(
                "open() has already been called and this client has been closed. Create a new instance.");
        }

        this.webSocket = HttpClient
            .newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(uri, getListener())
            .join();

        LOGGER.debug("Websocket created to handle token: {}", getToken());

        return this;
    }

    /**
     * Returns true if this resource client is still open
     *
     * @return true if this resource client is still open
     */
    public boolean isOpen() {
        return webSocket != null && !webSocket.isOutputClosed();
    }

    /**
     * Closes this resource client and the underlying socket connection
     */
    public void close() {
        // this will close the output side of the websocket
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "");
        // until the listener receives and onClose(), the input side of the websocket is
        // till open. this client should abort the websocket if no data arrives after
        // around 30 seconds. Not sure how to do this yet.
    }

    private String getToken() {
        return getSetup().getToken();
    }

    private ResourceClientListenr getListener() {
        return getSetup().getResourceClientListener();
    }

    private String getBaseUri() {
        return getSetup().getBaseUri();
    }

    private ResourceClientSetup getSetup() {
        return setup;
    }

}
