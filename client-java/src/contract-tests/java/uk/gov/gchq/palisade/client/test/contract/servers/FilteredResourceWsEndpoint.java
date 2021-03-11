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
package uk.gov.gchq.palisade.client.test.contract.servers;

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

import uk.gov.gchq.palisade.client.internal.model.MessageType;
import uk.gov.gchq.palisade.client.internal.model.Token;
import uk.gov.gchq.palisade.client.internal.model.WebSocketMessage;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import javax.inject.Inject;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.gchq.palisade.client.testing.ClientTestData.FILE_NAMES;

/**
 * Test websocket endpoint
 *
 * @since 0.5.0
 */
@ServerWebSocket("/cluster/filteredResource/resource/{token}")
public class FilteredResourceWsEndpoint {

    private static final String TOKEN_KEY = "token";

    /**
     * Generates test resources
     *
     * @since 0.5.0
     */
    public static class ResourceGenerator implements Iterable<WebSocketMessage> {

        private final List<WebSocketMessage> messages;
        private final String token;

        /**
         * Creates a new {@code ResourceGenerator} with the provided {@code token} and
         * {@code port}
         *
         * @param token the token
         * @param port  the port
         */
        public ResourceGenerator(final String token, final int port) {
            this.token = token;
            this.messages = Stream.of(FILE_NAMES.stream()
                    .map(filename -> WebSocketMessage.Builder.create().withType(MessageType.RESOURCE)
                        .withHeader(Token.HEADER, token).noHeaders()
                        .withBody(new FileResource()
                            .id(filename)
                            .serialisedFormat("format")
                            .type("type")
                            .connectionDetail(new SimpleConnectionDetail().serviceName("data-service"))
                            .parent(new SystemResource().id("parent")))),
                Stream.of(WebSocketMessage.Builder.create()
                    .withType(MessageType.ERROR)
                    .withHeader(Token.HEADER, token).noHeaders()
                    .withBody("test error")))
                .flatMap(Function.identity())
                .collect(Collectors.toList());

        }

        @Override
        public Iterator<WebSocketMessage> iterator() {
            return messages.iterator();
        }

    }

    @Inject
    EmbeddedServer embeddedServer;

    private static final Logger LOGGER = LoggerFactory.getLogger(FilteredResourceWsEndpoint.class);
    private Iterator<WebSocketMessage> messages;

    /**
     * Create a new {@code FilteredResourceWsEndpoint} with the provided
     * {@code broadcaster}
     *
     * @param broadcaster the web socket broadcaster
     */
    public FilteredResourceWsEndpoint(final WebSocketBroadcaster broadcaster) {
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
            MDC.put("server", "FR-SVC");
            assert token != null : "Should have the token as part of the path variable";
            LOGGER.debug("OPEN: Opening websocket for token {}", token);
            session.put(TOKEN_KEY, token);
            this.messages = new ResourceGenerator(token, embeddedServer.getPort()).iterator();
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
    public void onMessage(final WebSocketMessage inmsg, final WebSocketSession session) {
        try {
            MDC.put("server", "FR-SVC");
            LOGGER.debug("RCVD: {}", inmsg);
            var type = inmsg.getType();
            if (type.equals(MessageType.CTS)) {
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
            MDC.put("server", "FR-SVC");
            LOGGER.debug("RCVD: Close Request: {}", session.getId());
        } finally {
            MDC.remove("server");
        }
    }

    private static void sendComplete(final WebSocketSession session) {
        send(session, WebSocketMessage.Builder.create()
            .withType(MessageType.COMPLETE)
            .noHeaders()
            .noBody());
    }

    private static void send(final WebSocketSession session, final WebSocketMessage message) {
        try {
            MDC.put("server", "FR-SVC");
            session.sendSync(message);
            LOGGER.debug("SEND: {}", message);
        } finally {
            MDC.remove("server");
        }
    }

}