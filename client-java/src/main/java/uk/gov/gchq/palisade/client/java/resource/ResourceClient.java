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

import org.immutables.value.Value;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.download.DownloadTracker;
import uk.gov.gchq.palisade.client.java.state.*;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import javax.websocket.*;

import java.io.IOException;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

import static uk.gov.gchq.palisade.client.java.resource.MessageType.SUBSCRIBE;
import static uk.gov.gchq.palisade.client.java.state.StateType.*;

@ClientEndpoint(
    encoders = {ResourceClient.MessageCode.class},
    decoders = {ResourceClient.MessageCode.class}
)
public class ResourceClient {

    @Value.Immutable
    @ImmutableStyle
    public interface IResourceClientConfig {
        String getToken();
        EventBus getEventBus();
        ObjectMapper getMapper();
        DownloadTracker getDownloadTracker();
    }

    public static class MessageCode extends JSONCoder<Message> { // empty
    }

    public static ResourceClient createResourceClient(UnaryOperator<ResourceClientConfig.Builder> func) {
        return new ResourceClient(func.apply(ResourceClientConfig.builder()).build());
    }

    private static final Logger log = LoggerFactory.getLogger(ResourceClient.class);
    private final ResourceClientConfig config;
    private Session session = null;

    private ResourceClient(ResourceClientConfig config) {
        this.config = config;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        log.debug("Session {} opened", session.getId());
        this.session = session;
        var token = config().getToken();
        sendMessage(b -> b.type(SUBSCRIBE).token(token));
        post(StateChangeEvent.of(token, SUBSCRIBING));
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.debug("Session {} closed with code {} because {}",
                session.getId(), reason.getCloseCode(), reason.getReasonPhrase());
        this.session = null;
    }


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

    private void handleSubscribed(String token) {
        post(StateChangeEvent.of(token, StateType.SUBSCRIBED));
    }

    private void handleAck(String token) { // noop
    }

    private void handleReadyToSend(String token) {
        log.debug("handle RTS for token {}", token);
        // This is a quite crude way of waiting for download slots to become available
        // Should implement a better way, but this will do for now.
        while (!config.getDownloadTracker().hasAvailableSlots()) {
            try {
                log.debug("no download slots available, waiting");
                Thread.sleep(1000);
            } catch (InterruptedException e) { // just swallow this
            }
        }
        sendMessage(b -> b.type(MessageType.CTS).token(token));
        post(StateChangeEvent.of(token, CTS));
    }

    private void handleResource(String token, Message message) {
        log.debug("handle resource for token {}, message: {}", token, message);
        var body = message.getBody().orElseThrow(() -> new MissingResourceException(message));
        var resource = config().getMapper().convertValue(body, Resource.class);
        post(ResourceReadyEvent.of(token, resource));
        post(StateChangeEvent.of(token, RESOURCE));
    }

    private void handleComplete(String token) {
        log.debug("handle Complete for token {}", token);
        post(ResourcesExhaustedEvent.of(token));
        post(StateChangeEvent.of(token, COMPLETE));
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
        config.getEventBus().post(event);
    }

    private ResourceClientConfig config() {
        return this.config;
    }

}