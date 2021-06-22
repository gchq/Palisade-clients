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

package uk.gov.gchq.palisade.client.akka;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.scaladsl.model.ws.TextMessage.Strict;
import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.BroadcastHub;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.MergeHub;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.FlowAdapters;

import uk.gov.gchq.palisade.client.akka.model.DataRequest;
import uk.gov.gchq.palisade.client.akka.model.MessageType;
import uk.gov.gchq.palisade.client.akka.model.PalisadeRequest;
import uk.gov.gchq.palisade.client.akka.model.PalisadeResponse;
import uk.gov.gchq.palisade.client.akka.model.WebSocketMessage;
import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Publisher;

/**
 * Implementation of the client interface that also exposes some akka-specific data-types such as {@link Source}s.
 */
public class AkkaClient implements Client {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public enum SSLMode {
        NONE("http", "ws"),
        SSL_TLS("https", "wss");

        private final String httpScheme;
        private final String wsScheme;

        SSLMode(final String httpScheme, final String wsScheme) {
            this.httpScheme = httpScheme;
            this.wsScheme = wsScheme;
        }

        public String getHttpScheme() {
            return httpScheme;
        }

        public String getWsScheme() {
            return wsScheme;
        }
    }

    private final String palisadeUrl;
    private final String filteredResourceUrl;
    private final Map<String, String> dataUrlMap;
    private final SSLMode sslMode;
    private final Materializer materializer;
    private final Http http;

    /**
     * Constructor used to create the AkkaClient
     *
     * @param palisadeUrl         the location of the Palisade Service
     * @param filteredResourceUrl the location of the Filtered Resource Service
     * @param dataUrlMap          lookup map from the names of Data Services to their locations
     * @param actorSystem         the akka Actor System bean
     * @param sslMode             whether the client should connect using SSL or not
     */
    public AkkaClient(final String palisadeUrl, final String filteredResourceUrl, final Map<String, String> dataUrlMap, final ActorSystem actorSystem, final SSLMode sslMode) {
        this.palisadeUrl = palisadeUrl;
        this.filteredResourceUrl = filteredResourceUrl;
        this.dataUrlMap = dataUrlMap;
        this.sslMode = sslMode;
        this.materializer = Materializer.createMaterializer(actorSystem);
        this.http = Http.get(actorSystem);
    }

    /**
     * Registers a request into the Palisade Service, taking a userId, resourceId and Map as a context, it then sends the request to the
     * Palisade registerDataRequest endpoint via rest and returns the token after the request has ben processed.
     *
     * @param userId     the userId of the user making the request.
     * @param resourceId the resourceId requested to read - note this is not necessarily the filename.
     * @param context    the context for this data access.
     * @return a String uuid token in a CompletionStage object
     */
    public CompletionStage<String> register(final String userId, final String resourceId, final Map<String, String> context) {
        return http
                .singleRequest(HttpRequest.POST(String.format("%s://%s/api/registerDataRequest", sslMode.getHttpScheme(), palisadeUrl))
                        .withEntity(ContentTypes.APPLICATION_JSON, serialize(
                                PalisadeRequest.Builder.create()
                                        .withUserId(userId)
                                        .withResourceId(resourceId)
                                        .withContext(context)
                        )))
                .thenApply(this::readHttpMessage)
                .thenApply(PalisadeResponse::getToken);
    }

    /**
     * By taking the uuid token, this method deserializes the message from the websocket, and if completed, returns the processed LeafResource to the user
     *
     * @param token uuid of the request
     * @return a processed LeafResource that has been processed by the Palisade Service
     */
    public Source<LeafResource, CompletionStage<NotUsed>> fetchSource(final String token) {
        // Send out CTS messages after each RESOURCE
        WebSocketMessage cts = WebSocketMessage.Builder.create().withType(MessageType.CTS).noHeaders().noBody();

        // Map inbound messages to outbound CTS until COMPLETE is seen
        var exposeSinkAndSource = Flow.<WebSocketMessage>create()
                // Merge ws upstream with our decoupled upstream - prefer our source of messages, complete when both ws and ours are complete
                .mergePreferredMat(MergeHub.of(WebSocketMessage.class), true, false, Keep.right())
                // Broadcast to both ws downstream and our downstream
                .alsoToMat(BroadcastHub.of(WebSocketMessage.class), Keep.both());

        // Ser/Des for messages to/from the websocket
        var clientFlow = Flow.<Message>create()
                .map(msg -> AkkaClient.readWsMessage(msg, materializer))
                // Expose source and sink to this stage in materialization
                .viaMat(exposeSinkAndSource, Keep.right())
                // Take until COMPLETE message is seen
                .takeWhile(wsMessage -> wsMessage.getType() != MessageType.COMPLETE)
                // Handle how to 'echo back' a message
                .map((WebSocketMessage wsMessage) -> {
                    switch (wsMessage.getType()) {
                        case RESOURCE:
                        case ERROR:
                            return cts;
                        default:
                            return wsMessage;
                    }
                })
                .map(AkkaClient::writeWsMessage);

        // Make the request using the ser/des flow linked to the oscillator
        var wsResponse = http.singleWebSocketRequest(
                WebSocketRequest.create(String.format("%s://%s/resource/%s", sslMode.getWsScheme(), filteredResourceUrl, token)),
                clientFlow,
                materializer);

        Sink<WebSocketMessage, NotUsed> upstreamSink = wsResponse.second().first();
        Source<WebSocketMessage, NotUsed> downstreamSource = wsResponse.second().second();

        // Once the wsUpgrade request completes
        return Source.completionStageSource(wsResponse.first()
                // Initialize connection with a single CTS message
                .thenRun(() -> Source.single(cts).runWith(upstreamSink, materializer))
                // Return the connected Source
                .thenApply(ignored -> downstreamSource))
                // Take until COMPLETE message is seen
                .takeWhile(wsMessage -> wsMessage.getType() != MessageType.COMPLETE)
                // Extract LeafResource from message object
                .filter(wsMessage -> wsMessage.getType() == MessageType.RESOURCE)
                .map(msg -> msg.getBodyObject(LeafResource.class));
    }

    /**
     * Converts the akka stream to a reactive stream publisher
     * for use in the {@link #fetchSource(String)} method
     *
     * @param token the token returned from the palisade-service by the {@link #register} method.
     * @return a Reactive streams publisher containing the LeafResource from the Palisade Service
     */
    public Publisher<LeafResource> fetch(final String token) {
        // Convert akka source to reactive-streams publisher
        org.reactivestreams.Publisher<LeafResource> rsPub = fetchSource(token)
                .runWith(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer);

        // Convert akka reactive-streams to java stdlib flow
        return FlowAdapters.toFlowPublisher(rsPub);
    }

    /**
     * This method connects to the data service to read the leafResource from the original request, linked by the uuid token
     *
     * @param token    the token returned from the palisade-service by the {@link #register} method.
     * @param resource that the user wants to read
     * @return a stream of bytes representing the contents of the resource
     */
    public Source<ByteString, CompletionStage<NotUsed>> readSource(final String token, final LeafResource resource) {
        String createConn = resource.getConnectionDetail().createConnection();
        String dataUrl = dataUrlMap.getOrDefault(createConn, createConn);
        return Source.completionStageSource(http.singleRequest(
                HttpRequest.POST(String.format("%s://%s/read/chunked", sslMode.getHttpScheme(), dataUrl))
                        .withEntity(ContentTypes.APPLICATION_JSON, serialize(DataRequest.Builder.create()
                                .withToken(token)
                                .withLeafResourceId(resource.getId()))
                        ))
                .thenApply(response -> response.entity().getDataBytes()
                        .mapMaterializedValue(ignored -> NotUsed.notUsed())));
    }

    /**
     * Converts an akka ByteString source to java a stdlib InputStream
     *
     * @param token    the token returned from the palisade-service by the {@link #register(String, String, Map)} method.
     * @param resource a resource returned by the filtered-resource-service that the client wishes to read.
     * @return a java stdlib InputStream
     */
    public InputStream read(final String token, final LeafResource resource) {
        // Convert akka ByteString source to java stdlib InputStream
        return readSource(token, resource)
                .runWith(StreamConverters.asInputStream(), materializer);
    }


    private PalisadeResponse readHttpMessage(final HttpResponse message) {
        // Akka will sometimes convert a StrictMessage to a StreamedMessage, so we have to handle both cases here
        StringBuilder builder = message.entity().getDataBytes()
                .map(ByteString::utf8String)
                .runFold(new StringBuilder(), StringBuilder::append, materializer)
                .toCompletableFuture().join();
        return deserialize(builder.toString(), PalisadeResponse.class);
    }

    private static WebSocketMessage readWsMessage(final Message message, final Materializer materializer) {
        // Akka will sometimes convert a StrictMessage to a StreamedMessage, so we have to handle both cases here
        StringBuilder builder = message.asTextMessage().getStreamedText()
                .runFold(new StringBuilder(), StringBuilder::append, materializer)
                .toCompletableFuture().join();
        return deserialize(builder.toString(), WebSocketMessage.class);
    }

    private static Message writeWsMessage(final WebSocketMessage message) {
        return new Strict(serialize(message));
    }

    private static <T> T deserialize(final String json, final Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to write message", e);
        }
    }

    private static String serialize(final Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to write message", e);
        }
    }
}
