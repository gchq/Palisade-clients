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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.greenrobot.eventbus.EventBus;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.download.DownloadManagerStatus;
import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.IOException;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.UnaryOperator;

/**
 * Listens on the in-bound socket connection
 *
 * @since 0.5.0
 */
public class ResourceClientListener implements Listener {

    /**
     * An object that provides the setup for a {@code ResourceClientListener}
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IResourceClientListenrSetup {

        /**
         * Returns the download manager status
         *
         * @return the download manager status
         */
        DownloadManagerStatus getDownloadManagerStatus();

        /**
         * Returns the event bus
         *
         * @return the event bus
         */
        EventBus getEventBus();

        /**
         * Returns the object mapper
         *
         * @return the event bus
         */
        ObjectMapper getObjectMapper();

        /**
         * Returns the token
         *
         * @return the token
         */
        String getToken();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceClientListener.class);
    private static final long ONE_SECOND = 1000L;
    private static final int REASON_OK = 1001;



    private final IResourceClientListenrSetup setup;

    /**
     * A {@code ResourceClient} manages the passing of messages to/from a websocket
     * server
     *
     * @param setup The setup for this listener
     */
    public ResourceClientListener(final IResourceClientListenrSetup setup) {
        this.setup = Checks.checkArgument(setup);
    }

    /**
     * Helper method to create a {@link Message} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public static ResourceClientListener createResourceClientListenr(
        final UnaryOperator<ResourceClientListenrSetup.Builder> func) {
        return new ResourceClientListener(func.apply(ResourceClientListenrSetup.builder()).build());
    }

    DownloadManagerStatus getDownloadTracker() {
        return getSetup().getDownloadManagerStatus();
    }

    EventBus getEventBus() {
        return getSetup().getEventBus();
    }

    ObjectMapper getObjectMapper() {
        return getSetup().getObjectMapper();
    }

    IResourceClientListenrSetup getSetup() {
        return setup;
    }

    String getToken() {
        return getSetup().getToken();
    }

    private void handleError(final Message message) {
        LOGGER.debug("handle error for token {}, message: {}", getToken(), message);
        var body = message.getBody().orElseThrow(() -> new MissingResourceException(message));
        var error = getObjectMapper().convertValue(body, Error.class);
        post(ErrorEvent.of(error));
    }

    private void handleComplete(final WebSocket ws) {
        LOGGER.debug("handle Complete for token {}", getToken());
        ws.sendClose(REASON_OK, "complete (token=" + getToken() + ")");
        post(ResourcesExhaustedEvent.of(getToken()));
    }

    private void handleResource(final Message message) {
        LOGGER.debug("handle resource for token {}, message: {}", getToken(), message);
        var body = message.getBody().orElseThrow(() -> new MissingResourceException(message));
        var resource = getObjectMapper().convertValue(body, Resource.class);
        post(ResourceReadyEvent.of(resource));
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
        sendMessage(ws, b -> b.type(MessageType.CTS));
    }

    @Override
    public CompletionStage<?> onText(final WebSocket ws, final CharSequence data, final boolean last) {

        var text = data.toString();

        ws.request(1); // omit this and no methods are called listener

        Message message;
        try {
            message = getObjectMapper().readValue(text, Message.class);
        } catch (JsonProcessingException e) {
            onError(ws, e);
            return null;
        }

        LOGGER.debug("Recd: {}", message);

        var type = message.getType();

        if (type == MessageType.RESOURCE) {
            handleResource(message);
        } else if (type == MessageType.COMPLETE) {
            handleComplete(ws);
        } else if (type == MessageType.ERROR) {
            handleError(message);
        } else {
            // ignore the unsupported message
            LOGGER.warn("Ignoring unsupported {} message type", type);
        }

        LOGGER.debug("Testing if CTS can be sent for token {}", getToken());

        // This is a quite crude way of waiting for download slots to become available
        // Should implement a better way, but this will do for now.

        while (!getDownloadTracker().hasAvailableSlots()) {
            try {
                LOGGER.debug("No download slots available, waiting");
                Thread.sleep(ONE_SECOND);
            } catch (InterruptedException e) { // just swallow this
                Thread.currentThread().interrupt();
                LOGGER.warn("This thread was sleeping, when it was interrupted: {}", e.getMessage());
                // we'll loop round to see if there are any available slots
            }
        }

        sendMessage(ws, b -> b.type(MessageType.CTS));

        return null;

    }

    private void post(final Object event) {
        LOGGER.debug("Posting event: {}", event);
        getEventBus().post(event);
    }

    @SuppressWarnings({ "java:S3242", "java:S1135" }) // I REALLY want to use UnaryOperator here SonarQube!!!
    private CompletableFuture<WebSocket> sendMessage(final WebSocket ws, final UnaryOperator<Message.Builder> func) {
        var message = func.apply(Message.builder().putHeader("token", getToken())).build();
        try {
            var text = getObjectMapper().writeValueAsString(message);
            var future = ws.sendText(text, true);
            LOGGER.debug("Sent: {}", message);
            return future;
        } catch (IOException e) {
            // we should add this fail to a result object
            LOGGER.warn("Failed to send message: {}", message, e);
            return null;
        }
    }

}
