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

import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.download.DownloadTracker;
import uk.gov.gchq.palisade.client.java.util.Bus;

import javax.websocket.*;

import java.io.IOException;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.ObjectMapper;

import static uk.gov.gchq.palisade.client.java.resource.MessageType.SUBSCRIBE;

/**
 * The {@code ResourceClient} class represents a websocket endpoint. It handles
 * the communication to the Filtered Resource Service to negotiate the resources
 * that are available for download. This class uses the standard implementation
 * of websockets in Java
 *
 * @author dbell
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
     * @author dbell
     * @since 0.5.0
     */
    public static class MessageCode extends JSONCoder<Message> { // empty
    }

    private final Bus bus;
    private final String token;
    private final ObjectMapper objectMapper;
    private final DownloadTracker downloadTracker;

    private static final Logger log = LoggerFactory.getLogger(ResourceClient.class);

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
    public ResourceClient(String token, Bus bus, ObjectMapper objectMapper, DownloadTracker downloadTracker) {
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
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        log.debug("Session {} opened", session.getId());
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
    public void onClose(Session session, CloseReason closeReason) {
        log.debug("Session {} closed with code {} because {}",
            session.getId(), closeReason.getCloseCode(), closeReason.getReasonPhrase());
        this.session = null;
    }

    /**
     * Handle the WebSocket message event.
     *
     * @param message The message received
     */
    @OnMessage
    public void onMessage(Message message) {

        log.debug("Recd: {}", message);

        var token = message.getToken();
        var type = message.getType();

        if      (type == MessageType.SUBSCRIBED)   { handleSubscribed(token); }
        else if (type == MessageType.ACK)          { handleAck(token); }
        else if (type == MessageType.RTS)          { handleReadyToSend(token); }
        else if (type == MessageType.RESOURCE)     { handleResource(token, message); }
        else if (type == MessageType.COMPLETE)     { handleComplete(token); }
        else {
            // TODO: handle unsupported message type
        }
    }

    private void handleSubscribed(String token) { // empty
    }

    private void handleAck(String token) { // noop
    }

    private void handleReadyToSend(String token) {
        log.debug("handle RTS for token {}", token);
        // This is a quite crude way of waiting for download slots to become available
        // Should implement a better way, but this will do for now.
        while (!downloadTracker.hasAvailableSlots()) {
            try {
                log.debug("no download slots available, waiting");
                Thread.sleep(1000);
            } catch (InterruptedException e) { // just swallow this
            }
        }
        sendMessage(b -> b.type(MessageType.CTS).token(token));
    }

    private void handleResource(String token, Message message) {
        log.debug("handle resource for token {}, message: {}", token, message);
        var body = message.getBody().orElseThrow(() -> new MissingResourceException(message));
        var resource = objectMapper.convertValue(body, Resource.class);
        post(ResourceReadyEvent.of(resource));
    }

    private void handleComplete(String token) {
        log.debug("handle Complete for token {}", token);
        post(ResourcesExhaustedEvent.of(token));
    }

    private void sendMessage(UnaryOperator<Message.Builder> func) {
        var message = func.apply(Message.builder()).build();
        try {
            this.session.getBasicRemote().sendObject(message);
            log.debug("Sent: " + message);
        } catch (IOException | EncodeException e) {
            // TODO need to handle this one
            e.printStackTrace();
        }
    }

    private void post(Object event) {
        log.debug("Posting event: {}", event);
        bus.post(event);
    }

}