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
package uk.gov.gchq.palisade.client;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.resource.IMessage;
import uk.gov.gchq.palisade.client.resource.IResource;
import uk.gov.gchq.palisade.client.resource.Message;
import uk.gov.gchq.palisade.client.resource.MessageType;
import uk.gov.gchq.palisade.client.resource.Resource;

import javax.inject.Inject;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.gchq.palisade.client.resource.MessageType.CTS;

@ServerWebSocket("/name/{token}")
public class WsEndpoint {

    public static class ResourceGenerator implements Iterable<Resource> {

        private static final List<String> FILENAMES = List.of("pi.txt", "Selection_032.png");
        private final List<Resource> resources;

        public ResourceGenerator(final String token, final int port) {
            var url = "http://localhost:" + port;
            resources = FILENAMES.stream()
                .map(fn -> IResource.createResource(b -> b
                    .token(token)
                    .leafResourceId(fn)
                    .url(url)))
                .collect(Collectors.toList());
        }

        @Override
        public Iterator<Resource> iterator() {
            return resources.iterator();
        }

    }

    @Inject
    EmbeddedServer embeddedServer;

    private static final Logger LOGGER = LoggerFactory.getLogger(WsEndpoint.class);
    private final WebSocketBroadcaster broadcaster;
    private Iterator<Resource> resources;

    public WsEndpoint(final WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public void onOpen(final String token, final WebSocketSession session) {
        session.put("token", token);
        this.resources = new ResourceGenerator(token, embeddedServer.getPort()).iterator();
        if (resources.hasNext()) {
            send(session, MessageType.RTS);
        } else {
            send(session, MessageType.COMPLETE);
        }
        System.out.println("onOpen::" + session.getId());
    }

    @OnMessage
    public void onMessage(final Message inmsg, final WebSocketSession session) {
        LOGGER.debug("Recd: {}", inmsg);
        var type = inmsg.getType();
        if (type == CTS) {
            sendResource(session, resources.next());
            send(session, resources.hasNext() ? MessageType.RTS : MessageType.COMPLETE);
        } else {
            LOGGER.warn("Unknown message type: {}", inmsg.getType());
        }
    }

    @OnClose
    public void onClose(final WebSocketSession session) {
        System.out.println("onClose::" + session.getId());
    }

    private static void sendResource(final WebSocketSession session, final Resource resource) {
        var token = session.get("token", String.class).get();
        var message = IMessage.create(b -> b.putHeader("token", token).type(MessageType.RESOURCE).body(resource));
        send(session, message);
    }

    private static void send(final WebSocketSession session, final MessageType messageType) {
        var token = session.get("token", String.class).get();
        var message = IMessage.create(b -> b.putHeader("token", token).type(messageType));
        send(session, message);
    }

    private static void send(final WebSocketSession session, final Message message) {
        session.sendSync(message);
        LOGGER.debug("Sent: " + message);
    }

}