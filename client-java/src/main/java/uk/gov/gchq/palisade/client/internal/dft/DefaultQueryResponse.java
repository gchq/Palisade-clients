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

    /**
     * Enum to reflect the state of reading from the websocket.
     *
     * @since 0.5.0
     */
    private enum Loop {
        // normal state, keep reading websocket buffer
        CONTINUE,
        // No more left and we are done
        COMPLETE,
        // someone cancelled, so we should get out
        CANCELLED
    }

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

            var configuration = session.getConfiguration();
            var webSocketClient = WebSocketClient.createResourceClient(b -> b
                .token(palisadeResponse.getToken())
                .objectMapper(configuration.getObjectMapper())
                .baseUri(configuration.getFilteredResourceUrl()));

            webSocketClient.connect();

            LOGGER.debug("Connected to websocket");

            var loop = Loop.CONTINUE;
            do {
                var wsm = webSocketClient.poll(1, TimeUnit.SECONDS);
                if (wsm != null) {
                    if (wsm instanceof CompleteMessage) {
                        loop = Loop.COMPLETE;
                    } else {
                        createMessage(wsm).ifPresent((final Message msg) -> {
                            LOGGER.debug("emitter.onNext: {}", msg);
                            emitter.onNext(msg);
                        });
                    }
                }
                if (emitter.isCancelled()) {
                    loop = Loop.CANCELLED;
                }
            } while (loop == Loop.CONTINUE);

            if (loop == Loop.COMPLETE) {
                LOGGER.debug("emitter.complete");
                emitter.onComplete();
            } else if (loop == Loop.CANCELLED) {
                LOGGER.debug("emitter.cancelled");
            } else {
                LOGGER.debug("emitter ended normally");
            }

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
                .leafResourceId(rm.getLeafResourceId()));

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
    @Value.Immutable
    public interface EmittedResource extends Resource {

        /**
         * Helper method to create a {@code EmittedResource} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        @SuppressWarnings("java:S3242")
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
        default MessageType getType() {
            return MessageType.RESOURCE;
        }

    }

    /**
     * An emitted error represents an error emitted from the message stream
     *
     * @since 0.5.0
     */
    @Value.Immutable
    public interface EmittedError extends Error {

        /**
         * Helper method to create a {@code EmittedError} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */

        @SuppressWarnings("java:S3242")
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
        default MessageType getType() {
            return MessageType.ERROR;
        }

    }


}
