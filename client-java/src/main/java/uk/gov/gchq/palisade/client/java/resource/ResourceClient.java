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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.download.DownloadTracker;
import uk.gov.gchq.palisade.client.java.util.Bus;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import java.io.IOException;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.java.resource.MessageType.SUBSCRIBE;

/**
 * The {@code ResourceClient} class represents a websocket endpoint. It handles
 * the communication to the Filtered Resource Service to negotiate the resources
 * that are available for download. This class uses the standard implementation
 * of websockets in Java
 *
 * @since 0.5.0
 */
@ClientEndpoint(
    encoders = {ResourceClient.MessageCode.class},
    decoders = {ResourceClient.MessageCode.class}
)
public class ResourceClient {

    /**
     * An implementation of a {@link JSONCoder} for type of {@link Message}
     *
     * @since 0.5.0
     */
    public static class MessageCode extends JSONCoder<Message> { // empty
    }

    private final Bus bus;
    private final String token;
    private final ObjectMapper objectMapper;
    private final DownloadTracker downloadTracker;

    private static final Logger LOG = LoggerFactory.getLogger(ResourceClient.class);

    private Session session = null;

    /**
     * A {@code ResourceClient} manages the passing of messages to/from a websocket
     * server
     *
     * @param token           The token being managed
     * @param bus             The event bus used to post application events
     * @param objectMapper    The json mapper
     * @param downloadTracker the download tracker providing insight into available
     *                        download slots
     */
    public ResourceClient(final String token, final Bus bus, final ObjectMapper objectMapper, final DownloadTracker downloadTracker) {
        this.token = token;
        this.bus = bus;
        this.objectMapper = objectMapper;
        this.downloadTracker = downloadTracker;
    }

    /**
     * Handle the WebSocket open event. As soon as the session is opened, a
     * SUBSCRIBE message is sent to the server.
     *
     * @param session        The websocket session
     * @param endpointConfig The endpoint configuration
     */
    @OnOpen
    public void onOpen(final Session session, final EndpointConfig endpointConfig) {
        LOG.debug("Session {} opened", session.getId());
        this.session = session;
        sendMessage(b -> b.type(SUBSCRIBE).token(token));
    }

    /**
     * Handle the WebSocket close event. Upon close the stored session is nulled.
     *
     * @param session     The websocket session
     * @param closeReason The reason for closing the session
     */
    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        LOG.debug("Session {} closed with code {} because {}",
            session.getId(), closeReason.getCloseCode(), closeReason.getReasonPhrase());
        this.session = null;
    }

    /**
     * Handle the WebSocket message event.
     *
     * @param message The message received
     */
    @OnMessage
    public void onMessage(final Message message) {

        LOG.debug("Recd: {}", message);

        var tkn = message.getToken();
        var type = message.getType();

        if (type == MessageType.SUBSCRIBED) {
            handleSubscribed(tkn);
        } else if (type == MessageType.ACK) {
            handleAck(tkn);
        } else if (type == MessageType.RTS) {
            handleReadyToSend(tkn);
        } else if (type == MessageType.RESOURCE) {
            handleResource(tkn, message);
        } else if (type == MessageType.COMPLETE) {
            handleComplete(tkn);
        } else {
            // TODO: handle unsupported message type
        }
    }

    private void handleSubscribed(final String token) { // empty (here for completeness)
    }

    private void handleAck(final String token) { // noop
    }

    private void handleReadyToSend(final String token) {
        LOG.debug("handle RTS for token {}", token);
        // This is a quite crude way of waiting for download slots to become available
        // Should implement a better way, but this will do for now.
        while (!downloadTracker.hasAvailableSlots()) {
            try {
                LOG.debug("no download slots available, waiting");
                Thread.sleep(1000);
            } catch (InterruptedException e) { // just swallow this
            }
        }
        sendMessage(b -> b.type(MessageType.CTS).token(token));
    }

    private void handleResource(final String token, final Message message) {
        LOG.debug("handle resource for token {}, message: {}", token, message);
        var body = message.getBody().orElseThrow(() -> new MissingResourceException(message));
        var resource = objectMapper.convertValue(body, Resource.class);
        post(ResourceReadyEvent.of(resource));
    }

    private void handleComplete(final String token) {
        LOG.debug("handle Complete for token {}", token);
        post(ResourcesExhaustedEvent.of(token));
    }

    private void sendMessage(final UnaryOperator<Message.Builder> func) {
        var message = func.apply(Message.builder()).build();
        try {
            this.session.getBasicRemote().sendObject(message);
            LOG.debug("Sent: " + message);
        } catch (IOException | EncodeException e) {
            // TODO need to handle this one
            e.printStackTrace();
        }
    }

    private void post(final Object event) {
        LOG.debug("Posting event: {}", event);
        bus.post(event);
    }

}