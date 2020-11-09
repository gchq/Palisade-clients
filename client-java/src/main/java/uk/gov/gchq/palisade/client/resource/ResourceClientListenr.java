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
import java.util.concurrent.CompletionStage;
import java.util.function.UnaryOperator;

/**
 * @since 0.5.0
 */
public class ResourceClientListenr implements Listener {

    /**
     * An object that provides the setup for a {@code ResourceClientListenr}
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IResourceClientListenrSetup {

        /**
         * Returns the token
         *
         * @return the token
         */
        String getToken();

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
         * Returns the download manager status
         *
         * @return the download manager status
         */
        DownloadManagerStatus getDownloadManagerStatus();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceClientListenr.class);
    private static final long ONE_SECOND = 1000L;

    private final ResourceClientListenrSetup setup;

    /**
     * Helper method to create a {@link Message} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public static ResourceClientListenr createResourceClientListenr(
        final UnaryOperator<ResourceClientListenrSetup.Builder> func) {
        return new ResourceClientListenr(func.apply(ResourceClientListenrSetup.builder()).build());
    }

    /**
     * A {@code ResourceClient} manages the passing of messages to/from a websocket
     * server
     *
     * @param setup The setup for this listener
     */
    public ResourceClientListenr(final ResourceClientListenrSetup setup) {
        this.setup = Checks.checkArgument(setup);
    }

    @Override
    public void onOpen(final WebSocket ws) {
        Listener.super.onOpen(ws);
        LOGGER.debug("WebSocket Listener has been opened for requests.");
    }

    @Override
    public void onError(final WebSocket ws, final Throwable error) {
        LOGGER.debug("A {} exception was thrown.", error.getCause().getClass().getSimpleName());
        LOGGER.debug("Message: {}", error.getLocalizedMessage());
    }

    @Override
    public CompletionStage<?> onText(final WebSocket ws, final CharSequence data, final boolean last) {

        var text = data.toString();

        Message message;
        try {

            message = getObjectMapper().readValue(text, Message.class);

            LOGGER.debug("Recd: {}", message);

            var type = message.getType();

            if (type == MessageType.RTS) {
                handleReadyToSend(ws);
            } else if (type == MessageType.RESOURCE) {
                handleResource(message);
            } else if (type == MessageType.COMPLETE) {
                handleComplete(ws);
            } else {
                // ignore the unsupported message
                LOGGER.warn("Ignoring unsupported {} message type", type);
            }

        } catch (JsonProcessingException e) {
            onError(ws, e);
        }

        return Listener.super.onText(ws, data, last);
    }

    private void handleResource(final Message message) {
        LOGGER.debug("handle resource for token {}, message: {}", getToken(), message);
        var body = message.getBody().orElseThrow(() -> new MissingResourceException(message));
        var resource = getObjectMapper().convertValue(body, Resource.class);
        post(ResourceReadyEvent.of(resource));
    }

    private void handleReadyToSend(final WebSocket ws) {
        LOGGER.debug("handle RTS for token {}", getToken());
        // This is a quite crude way of waiting for download slots to become available
        // Should implement a better way, but this will do for now.
        while (!getDownloadTracker().hasAvailableSlots()) {
            try {
                LOGGER.debug("no download slots available, waiting");
                Thread.sleep(ONE_SECOND);
            } catch (InterruptedException e) { // just swallow this
                Thread.currentThread().interrupt();
                LOGGER.warn("This thread was sleeping, when it was interrupted: {}", e.getMessage());
                // we'll loop round to see if there are any available slots
            }
        }
        sendMessage(ws, b -> b.type(MessageType.CTS));
    }

    private void handleComplete(final WebSocket ws) {
        LOGGER.debug("handle Complete for token {}", getToken());
        ws.sendClose(1001, "complete (token=" + getToken() + ")");
        post(ResourcesExhaustedEvent.of(getToken()));
    }

    @SuppressWarnings({ "java:S3242", "java:S1135" }) // I REALLY want to use UnaryOperator here SonarQube!!!
    private void sendMessage(final WebSocket ws, final UnaryOperator<Message.Builder> func) {
        var message = func.apply(Message.builder().putHeader("token", getToken())).build();
        try {
            var text = getObjectMapper().writeValueAsString(message);
            ws.sendText(text, true);
            LOGGER.debug("Sent: {}", message);
        } catch (IOException e) {
            // TODO: we should add this fail to a result object
            LOGGER.warn("Failed to send message: {}", message, e);
        }
    }

    private void post(final Object event) {
        LOGGER.debug("Posting event: {}", event);
        getEventBus().post(event);
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

    String getToken() {
        return getSetup().getToken();
    }

    ResourceClientListenrSetup getSetup() {
        return setup;
    }

}
