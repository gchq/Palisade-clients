package uk.gov.gchq.palisade.client.java;

import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.resource.*;
import uk.gov.gchq.palisade.client.java.util.ClientUtil;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static uk.gov.gchq.palisade.client.java.ServerStateType.WAITING;

@ServerEndpoint(
        value = "/name",
        encoders = { ResourceClient.MessageCode.class },
        decoders = { ResourceClient.MessageCode.class }
        )
public class ServerSocket {

    private static final Logger log = LoggerFactory.getLogger(ServerSocket.class);
    private static final ObjectMapper objectMapper = ClientUtil.getObjectMapper();
    private static final int NUM_RESOURCES = 50;

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

        var ack = IMessage.create(b -> b.type(MessageType.SUBSCRIBED).token(token)
                .putHeader("state", (oldState.isPresent() ? "reconnect" : "new"))
                .putHeader("test", "test response message"));

        sendMessage(session, ack);

        // create a weak reference for the session as we should not get in the way of it
        // being reclaimed
        var sessionRef = new WeakReference<Session>(session);

        var newState = oldState.isPresent()
                ? oldState.get().change(b -> b.sessionReference(sessionRef).currentState(WAITING))
                : IServerState.create(b -> b.token(token).sessionReference(sessionRef)
                        .resourceGenerator(new ResourceGenerator(NUM_RESOURCES)).currentState(WAITING));

        statemanager.set(newState);

        // now that we have done an ACK, we do have resources to send
        // we now send an RTS (ready to send message).
        // This will instruct the client that the server has a resource to send

        // The code below will later be in some other thread that will be reading a
        // queue

        if (newState.getResourceGenerator().hasNext()) {
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
        var resource = oldState.getResourceGenerator().next();

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

        if (newState.getResourceGenerator().hasNext()) {
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
            var text = ClientUtil.getObjectMapper().writeValueAsString(message);
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
