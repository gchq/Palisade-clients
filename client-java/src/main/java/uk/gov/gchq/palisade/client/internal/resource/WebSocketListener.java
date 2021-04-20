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
package uk.gov.gchq.palisade.client.internal.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.internal.model.MessageType;
import uk.gov.gchq.palisade.client.internal.model.Token;
import uk.gov.gchq.palisade.client.internal.model.WebSocketMessage;
import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.IOException;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
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
         * listener
         */
        Consumer<WebSocketMessage> getEventsHandler();

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
     * Helper method to create a {@link WebSocketListener} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // Unary Operator vs Function
    public static WebSocketListener createResourceClientListener(final UnaryOperator<ResourceClientListenerSetup.Builder> func) {
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
        LOGGER.debug("OPEN: WebSocket Listener has been opened for requests.");
        send(ws, WebSocketMessage.Builder.create().withType(MessageType.CTS));
    }

    @Override
    public CompletionStage<?> onText(final WebSocket ws, final CharSequence data, final boolean last) {

        var text = data.toString();

        ws.request(1);

        WebSocketMessage wsMsg;
        try {
            wsMsg = objectMapper.readValue(text, WebSocketMessage.class);
        } catch (JsonProcessingException e) {
            onError(ws, e);
            return null;
        }

        LOGGER.debug("RCVD: {}", wsMsg);

        switch (wsMsg.getType()) {
            case RESOURCE:
            case ERROR:
                LOGGER.debug("EMIT: {}", wsMsg);
                handler.accept(wsMsg);
                send(ws, WebSocketMessage.Builder.create().withType(MessageType.CTS));
                break;
            case COMPLETE:
                LOGGER.debug("COMPLETE: {}", wsMsg);
                handler.accept(wsMsg);
                break;
            default:
                LOGGER.warn("Ignoring unsupported '{}' message type", wsMsg.getType());
                break;
        }

        return CompletableFuture.completedFuture(null);

    }

    @SuppressWarnings("java:S4276")
    private void send(final WebSocket ws, final WebSocketMessage.Builder.IHeaders messageBuilder) {
        var message = messageBuilder
                .withHeader(Token.HEADER, token).noHeaders()
                .noBody();
        try {
            var text = objectMapper.writeValueAsString(message);
            ws.sendText(text, true);
            LOGGER.debug("SEND: {}", message);
        } catch (IOException e) {
            // we should add this fail to a result object
            LOGGER.warn("Failed to send message: {}", message, e);
        }
    }

}
