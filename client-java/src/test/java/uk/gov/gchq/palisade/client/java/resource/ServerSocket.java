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

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@ServerEndpoint(
        value = "/name",
        encoders = { ResourceClient.MessageCode.class },
        decoders = { ResourceClient.MessageCode.class }
        )
public class ServerSocket {

    private static final Logger log = LoggerFactory.getLogger(ServerSocket.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    private static final int NUM_RESOURCES = 1;

    private final ServerStateManager statemanager = new ServerStateManager();

    @OnOpen
    public void onOpen(Session s) {
        System.out.println("onOpen::" + s.getId());
    }

    @OnClose
    public void onClose(Session s) {
        System.out.println("onClose::" + s.getId());
    }

    @OnMessage
    public void onMessage(Message inmsg, Session session) {
        log.debug("Recd: {}", inmsg);
        switch (inmsg.getType()) {
            case SUBSCRIBE: onSubscribe(session, inmsg); break;
            case CTS:       onCTS(session, inmsg);       break;
            default:
                log.warn("Unknown message type: {}", inmsg.getType());
                break;
        }
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        System.out.println("onError::" + t.getMessage());
    }

    private void onSubscribe(Session session, Message message) {

        var token = message.getToken();

        var oldState = statemanager.find(token).filter(ServerState::isActive);

        var ack = IMessage.create(b -> b
            .type(MessageType.SUBSCRIBED)
            .token(token)
            .putHeader("state", (oldState.isPresent() ? "reconnect" : "new"))
            .putHeader("test", "test response message"));

        sendMessage(session, ack);

        // create a weak reference for the session as we should not get in the way of it
        // being reclaimed
        var sessionRef = new WeakReference<Session>(session);

        var newState = oldState.isPresent()
            ? oldState.get().change(b -> b
                .sessionReference(sessionRef)
                .currentState(ServerStateType.WAITING))
            : IServerState.create(b -> b
                .token(token)
                .sessionReference(sessionRef)
                .resources(new ResourceGenerator(token).iterator())
                .currentState(ServerStateType.WAITING));

        statemanager.set(newState);

        // now that we have done an ACK, we do have resources to send
        // we now send an RTS (ready to send message).
        // This will instruct the client that the server has a resource to send

        // The code below will later be in some other thread that will be reading a
        // queue

        if (newState.getResources().hasNext()) {
            var rts = IMessage.create(b -> b.type(MessageType.RTS).token(token));
            sendMessage(session, rts);
            statemanager.set(statemanager.get(token).change(b -> b.currentState(ServerStateType.RTS)));
        }

    }

    private void onCTS(Session session, Message message) {

        var token = message.getToken();

        var oldState = statemanager.get(token);

        // we need to check the current state is RTS (the reason we are recieving a CTS)
        if (!oldState.isAt(ServerStateType.RTS)) {
            // TODO: we need to log the error (WARN) as something has gone wrong in the
            // protocol
            // should also return an error to the client
        }

        // get the next resource
        var resource = oldState.getResources().next();

        var msg = IMessage.create(b -> b.type(MessageType.RESOURCE).token(token)
                .putHeader("test", "test response message").body(resource));

        sendMessage(session, msg);

        // now create the state
        // for now we will just overwrite any existing state for this token.

        var newState = oldState.change(b -> b.currentState(ServerStateType.WAITING));
        statemanager.set(newState);

        // now that we have done an ACK, we do have resources to send
        // we now send an RTS (ready to send message).
        // This will instruct the client that the server has a resource to send

        // The code below will later be in some other thread that will be reading a
        // queue

        if (newState.getResources().hasNext()) {
            var rts = IMessage.create(b -> b.type(MessageType.RTS).token(token));
            sendMessage(session, rts);
            statemanager.set(statemanager.get(token).change(b -> b.currentState(ServerStateType.RTS)));
        } else {
            var comp = IMessage.create(b -> b.type(MessageType.COMPLETE).token(token));
            sendMessage(session, comp);
            statemanager.remove(token);
        }

    }

    private void sendMessage(Session session, Message message) {
        try {
            var text = objectMapper.writeValueAsString(message);
            session.getBasicRemote().sendText(text);
            log.debug("Sent: " + message);
        } catch (JsonProcessingException e) {
            // TODO: handle this
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: handle the send fail
            e.printStackTrace();
        }
    }

}