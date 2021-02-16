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
package uk.gov.gchq.palisade.client.testing;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import uk.gov.gchq.palisade.client.internal.resource.WebSocketListener.Item;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketListener.WebSocketMessageType;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketMessage;

import javax.inject.Inject;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test websocket endpoint
 *
 * @since 0.5.0
 */
@ServerWebSocket("/cluster/resource/{token}")
public class WsEndpointFilteredResource {

    private static final String TOKEN_KEY = "token";

    /**
     * Generates test resources
     *
     * @since 0.5.0
     */
    public static class ResourceGenerator implements Iterable<Item> {

        private static final List<String> FILENAMES = List.of(
            "resources/pi0.txt", "resources/pi1.txt", "resources/pi2.txt", "resources/pi3.txt",
            "resources/pi4.txt", "resources/pi5.txt", "resources/pi6.txt", "resources/pi7.txt",
            "resources/pi8.txt", "resources/pi9.txt");
        private final List<Item> messages;
        private final String token;

        /**
         * Creates a new {@code ResourceGenerator} with the provided {@code token} and
         * {@code port}
         *
         * @param token the token
         * @param port  the port
         */
        public ResourceGenerator(final String token, final int port) {
            var url = "http://localhost:" + port;
            this.token = token;
            this.messages = FILENAMES.stream()
                .map(filename -> WebSocketMessage.createResourceMessage(b -> b
                    .token(token)
                    .id(filename)
                    .serialisedFormat("format")
                    .type("type")
                    .url(url)))
                .map(rsc -> message(rsc, WebSocketMessageType.RESOURCE))
                .collect(Collectors.toList());
            this.messages
                .add(message(WebSocketMessage.createErrorMessage(b -> b
                    .token(token)
                    .text("test error")),
                WebSocketMessageType.ERROR));

        }

        private Item message(final Object body, final WebSocketMessageType type) {
            return Item.createItem(builder -> builder
                .putHeader(TOKEN_KEY, token)
                .type(type)
                .body(body));
        }

        @Override
        public Iterator<Item> iterator() {
            return messages.iterator();
        }

    }

    @Inject
    EmbeddedServer embeddedServer;

    private static final Logger LOGGER = LoggerFactory.getLogger(WsEndpointFilteredResource.class);
    private Iterator<Item> messages;

    /**
     * Create a new {@code WsEndpointFilteredResource} with the provided
     * {@code broadcaster}
     *
     * @param broadcaster the web socket broadcaster
     */
    public WsEndpointFilteredResource(final WebSocketBroadcaster broadcaster) {
        // noop
    }

    /**
     * Called when the websocket is opened
     *
     * @param token   The token which is passed in as a query parameter on the HTTP
     *                request
     * @param session The web socket session
     */
    @OnOpen
    public void onOpen(final String token, final WebSocketSession session) {
        try {
            MDC.put("server", "FRS-SVC");
            assert token != null : "Should have the token as part of the path variable";
            LOGGER.debug("OPEN: Opening websocket for token {}", token);
            session.put(TOKEN_KEY, token);
            this.messages = new ResourceGenerator(token, embeddedServer.getPort()).iterator();
            if (!messages.hasNext()) {
                sendComplete(session);
            }
            LOGGER.debug("OPEN: WebSocket opened");
        } finally {
            MDC.remove("server");
        }
    }

    /**
     * Called when a new message arrives
     *
     * @param inmsg   The incoming message
     * @param session The web socket session
     */
    @OnMessage
    public void onMessage(final Item inmsg, final WebSocketSession session) {
        try {
            MDC.put("server", "FRS-SVC");
            LOGGER.debug("RCVD: {}", inmsg);
            var type = inmsg.getType();
            if (type == WebSocketMessageType.CTS) {
                if (messages.hasNext()) {
                    send(session, messages.next());
                } else {
                    sendComplete(session);
                }
            } else {
                LOGGER.warn("Unknown message type: {}", inmsg.getType());
            }
        } finally {
            MDC.remove("server");
        }
    }

    /**
     * Called when the websocket closes
     *
     * @param session The websocket session that is to close
     */
    @OnClose
    public void onClose(final WebSocketSession session) {
        try {
            MDC.put("server", "FRS-SVC");
            LOGGER.debug("RCVD: Close Request: {}", session.getId());
        } finally {
            MDC.remove("server");
        }
    }

    private static final void sendComplete(final WebSocketSession session) {
        send(session, Item.createItem(b -> b
            .putHeader("token",
                session.get(TOKEN_KEY, String.class)
                    .orElseThrow(() -> new IllegalStateException("Missing token key: " + TOKEN_KEY)))
            .type(WebSocketMessageType.COMPLETE)));
    }

    private static void send(final WebSocketSession session, final Item message) {
        try {
            MDC.put("server", "FRS-SVC");
            session.sendSync(message);
            LOGGER.debug("SEND: {}", message);
        } finally {
            MDC.remove("server");
        }
    }

}