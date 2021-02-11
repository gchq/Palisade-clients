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
package uk.gov.gchq.palisade.client.internal.dft;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import org.immutables.value.Value;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.Error;
import uk.gov.gchq.palisade.client.Message;
import uk.gov.gchq.palisade.client.MessageType;
import uk.gov.gchq.palisade.client.QueryResponse;
import uk.gov.gchq.palisade.client.Resource;
import uk.gov.gchq.palisade.client.internal.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.internal.resource.CompleteMessage;
import uk.gov.gchq.palisade.client.internal.resource.ErrorMessage;
import uk.gov.gchq.palisade.client.internal.resource.ResourceMessage;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketClient;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketMessage;

import java.net.http.HttpClient;
import java.util.Optional;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * Default implementation for the "dft" subname
 *
 * @since 0.5.0
 */
public class DefaultQueryResponse implements QueryResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultQueryResponse.class);

    private final DefaultSession session;
    private final PalisadeResponse palisadeResponse;

    /**
     * Returns a newly created {@code DefaultQueryResponse} with the provided
     * {@code session} and {@code palisadeResponse}
     *
     * @param session          The session providing connection to the cluster
     * @param palisadeResponse The response from the cluster
     */
    public DefaultQueryResponse(final DefaultSession session, final PalisadeResponse palisadeResponse) {
        this.session = session;
        this.palisadeResponse = palisadeResponse;
    }

    @SuppressWarnings("java:S3776")
    @Override
    public Publisher<Message> stream() {

        // our flowable must wrap the websocket client

        var flowable = Flowable.create((final FlowableEmitter<Message> emitter) -> {

            LOGGER.debug("Creating stream...");

            /*
             * Must use a new client here for the websocket connection. If we use the one
             * from the session, the websocket listener seems to fail and hang.
             */

            var httpClient = HttpClient.newHttpClient();

            var configuration = session.getConfiguration();

            var webSocketClient = WebSocketClient.createResourceClient(b -> b
                .httpClient(httpClient)
                .objectMapper(session.getObjectMapper())
                .token(palisadeResponse.getToken())
                .uri(configuration.getFilteredResourceUrl()));

            webSocketClient.connect();

            LOGGER.debug("Connected to websocket");

            var timeout = session.getConfiguration().getQueryStreamPollTimeout();
            var loop = true;

            do {
                var wsm = webSocketClient.poll(timeout, TimeUnit.SECONDS);
                if (wsm != null) {
                    if (wsm instanceof CompleteMessage) {
                        // we're done, so signal complete and set flag to get out
                        emitter.onComplete();
                        loop = false;
                        LOGGER.trace("emitter.complete");
                    } else {
                        createMessage(wsm).ifPresent((final Message msg) -> {
                            LOGGER.trace("emitter.onNext: {}", msg);
                            emitter.onNext(msg);
                        });
                    }
                }
                if (emitter.isCancelled()) {
                    // we're cancelled, so set flag to get out
                    loop = false;
                    LOGGER.trace("emitter.cancelled");
                }
            } while (loop);

        }, BackpressureStrategy.BUFFER);

        return FlowAdapters.toFlowPublisher(flowable); // return a Java Flow Publisher.

    }

    private static Optional<Message> createMessage(final WebSocketMessage wsm) {

        Message msg = null;

        if (wsm instanceof ResourceMessage) {

            var rm = (ResourceMessage) wsm;
            msg = EmittedResource.createResource(b -> b
                .token(rm.getToken())
                .url(rm.getUrl())
                .leafResourceId(rm.getId())
                .type(rm.getType())
                .serialisedFormat(rm.getSerialisedFormat()));

        } else if (wsm instanceof ErrorMessage) {

            var em = (ErrorMessage) wsm;
            msg = EmittedError.createError(b -> b
                .token(em.getToken())
                .text(em.getText()));

        } else {
            LOGGER.warn("Unknown message emitted from stream: {}", wsm.getClass().getName());
        }

        return Optional.ofNullable(msg);

    }

    /**
     * An emitted resource represents an resource emitted from the message stream
     *
     * @since 0.5.0
     */
    @SuppressWarnings("java:S3242") // stop erroneous "use general type" message
    @Value.Immutable
    public interface EmittedResource extends Resource {

        /**
         * Helper method to create a {@code EmittedResource} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        static EmittedResource createResource(final UnaryOperator<EmittedResource.Builder> func) {
            return func.apply(new EmittedResource.Builder()).build();
        }

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableEmittedResource.Builder { // empty
        }

        @Override
        @Value.Derived
        default MessageType getMessageType() {
            return MessageType.RESOURCE;
        }

    }

    /**
     * An emitted error represents an error emitted from the message stream
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @SuppressWarnings("java:S3242") // stop erroneous "use general type" message
    public interface EmittedError extends Error {

        /**
         * Helper method to create a {@code EmittedError} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */

        static EmittedError createError(final UnaryOperator<EmittedError.Builder> func) {
            return func.apply(new EmittedError.Builder()).build();
        }

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableEmittedError.Builder { // empty
        }

        @Override
        @Value.Derived
        default MessageType getMessageType() {
            return MessageType.ERROR;
        }

    }


}
