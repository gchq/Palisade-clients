/*
 * Copyright 2020-2021 Crown Copyright
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
package uk.gov.gchq.palisade.client.internal.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.IOException;
import java.io.Serializable;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Listens on the in-bound socket connection
 *
 * @since 0.5.0
 */
public class WebSocketListener implements Listener {

    /**
     * An object that provides the setup for a {@code ResourceClientListener}
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface ResourceClientListenerSetup {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableResourceClientListenerSetup.Builder { // empty
        }

        /**
         * Returns the token
         *
         * @return the token
         */
        String getToken();

        /**
         * Returns the object mapper
         *
         * @return the event bus
         */
        ObjectMapper getObjectMapper();

        /**
         * Returns the consumer that will handle web socket events emitted from this
         * listener
         *
         * @return the consumer that will handle web socket events emitted from this
         *         listener
         */
        Consumer<WebSocketMessage> getEventsHandler();

    }

    /**
     * The type of {@link Item}
     *
     * @since 0.5.0
     */
    public enum MessageType {

        /**
         * Indicates that the client is ready to receive. This type of message is send
         * only.
         */
        CTS,

        /**
         * An error has occurred on the server. This type of message is receive only.
         */
        ERROR,

        /**
         * A resource from the server. This type of message is receive only.
         */
        RESOURCE,

        /**
         * No more resources available for token. This type of message is receive only.
         */
        COMPLETE

    }

    /**
     * An item is in object received from the websocket
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = ImmutableItem.class)
    @JsonSerialize(as = ImmutableItem.class)
    public interface Item extends Serializable {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableItem.Builder { // empty
        }

        /**
         * Helper method to create a {@link Item} using a builder function
         *
         * @param func The builder function
         * @return a newly created {@code RequestId}
         */
        @SuppressWarnings("java:S4276")
        static Item createMessage(final Function<Item.Builder, Item.Builder> func) {
            return func.apply(new Item.Builder()).build();
        }

        /**
         * Returns the type of this message
         *
         * @return the {@link MessageType}
         */
        MessageType getType();

        /**
         * Returns the headers for this message an empty map if there are none
         *
         * @return the headers for this message an empty map if there are none
         */
        Map<String, String> getHeaders();

        /**
         * Returns the body of this message or empty if there is none. At this point
         * there should only be a body for {@link MessageType} of RESOURCE.
         *
         * @return the body of this message or empty if there is none
         */
        Optional<Object> getBody();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketListener.class);

    private final ObjectMapper objectMapper;
    private final Consumer<WebSocketMessage> handler;
    private final String token;

    /**
     * A {@code ResourceClient} manages the passing of messages to/from a websocket
     * server
     *
     * @param setup The setup for this listener
     */
    public WebSocketListener(final ResourceClientListenerSetup setup) {
        Checks.checkNotNull(setup);
        this.token = setup.getToken();
        this.handler = setup.getEventsHandler();
        this.objectMapper = setup.getObjectMapper();
    }

    /**
     * Helper method to create a {@link Item} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242")
    public static WebSocketListener createResourceClientListener(
        final UnaryOperator<ResourceClientListenerSetup.Builder> func) {
        return new WebSocketListener(func.apply(new ResourceClientListenerSetup.Builder()).build());
    }

    @Override
    public void onError(final WebSocket ws, final Throwable error) {
        LOGGER.debug("A {} exception was thrown.", error.getCause().getClass().getSimpleName());
        LOGGER.debug("Message: {}", error.getLocalizedMessage());
    }

    @Override
    public void onOpen(final WebSocket ws) {
        Listener.super.onOpen(ws);
        LOGGER.debug("WebSocket Listener has been opened for requests.");
        send(ws, b -> b.type(MessageType.CTS));
    }

    @Override
    public CompletionStage<?> onText(final WebSocket ws, final CharSequence data, final boolean last) {

        var text = data.toString();

        ws.request(1); // omit this and no methods are called listener

        Item message;
        try {
            message = objectMapper.readValue(text, Item.class);
        } catch (JsonProcessingException e) {
            onError(ws, e);
            return null;
        }

        LOGGER.debug("<-- {}", message);

        var type = message.getType();

        if (type == MessageType.RESOURCE) {

            var body = message.getBody().orElseThrow(() -> new WebSocketMessageException(message));
            var item = objectMapper.convertValue(body, ResourceMessage.class);
            emit(item);

        } else if (type == MessageType.COMPLETE) {

            var item = WebSocketMessage.createComplete(b -> b.token(token));
            emit(item);

        } else if (type == MessageType.ERROR) {

            var body = message.getBody().orElseThrow(() -> new WebSocketMessageException(message));
            var item = objectMapper.convertValue(body, ErrorMessage.class);
            emit(item);

        } else {
            LOGGER.warn("Ignoring unsupported {} message type", type);
        }

        if (type != MessageType.COMPLETE) {
            send(ws, b -> b.type(MessageType.CTS));
        }

        return null;

    }

    private void emit(final WebSocketMessage event) {
        LOGGER.debug("emit {}", event);
        handler.accept(event);
    }

    @SuppressWarnings("java:S4276")
    private void send(final WebSocket ws, final UnaryOperator<Item.Builder> func) {
        var f1 = func.andThen(b -> b.putHeader("token", token));
        var message = Item.createMessage(f1);
        try {
            var text = objectMapper.writeValueAsString(message);
            ws.sendText(text, true);
            LOGGER.debug("--> {}", message);
        } catch (IOException e) {
            // we should add this fail to a result object
            LOGGER.warn("Failed to send message: {}", message, e);
        }
    }

}
