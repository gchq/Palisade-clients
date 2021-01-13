package uk.gov.gchq.palisade.client.abc.impl;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import org.immutables.value.Value;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.abc.Error;
import uk.gov.gchq.palisade.client.abc.Message;
import uk.gov.gchq.palisade.client.abc.Message.MessageType;
import uk.gov.gchq.palisade.client.abc.QueryResponse;
import uk.gov.gchq.palisade.client.abc.Resource;
import uk.gov.gchq.palisade.client.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.resource.CompleteMessage;
import uk.gov.gchq.palisade.client.resource.ErrorMessage;
import uk.gov.gchq.palisade.client.resource.ResourceMessage;
import uk.gov.gchq.palisade.client.resource.WebSocketClient;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class DefaultQueryResponse implements QueryResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultQueryResponse.class);

    private final DefaultSession session;
    private final PalisadeResponse palisadeResponse;

    public DefaultQueryResponse(final DefaultSession session, final PalisadeResponse palisadeResponse) {
        this.session = session;
        this.palisadeResponse = palisadeResponse;
    }

    @Override
    public Publisher<Message> stream() {

        // our flowable must wrap the websocket client

        var flowable = Flowable.<Message>create(emitter -> {

            LOGGER.debug("Creating stream...");

            var configuration = session.getConfiguration();
            var webSocketClient = WebSocketClient.createResourceClient(b -> b
                .token(palisadeResponse.getToken())
                .objectMapper(configuration.getObjectMapper())
                .baseUri(configuration.getFilteredResourceUrl()));

            webSocketClient.connect();

            LOGGER.debug("Connected to websocket");

            boolean leave = false;

            do {

                var wsm = webSocketClient.poll(1, TimeUnit.SECONDS);

                if (wsm != null) {

                    if (wsm instanceof CompleteMessage) {
                        break;
                    }

                    Message msg;

                    if (wsm instanceof ResourceMessage) {

                        var rm = (ResourceMessage) wsm;
                        msg = EmittedResource.createResource(b -> b
                            .type(MessageType.RESOURCE)
                            .token(rm.getToken())
                            .resourceId(rm.getLeafResourceId())
                            .queryResourceId("ouch"));

                    } else if (wsm instanceof ErrorMessage) {

                        var em = (ErrorMessage) wsm;
                        msg = EmittedError.createError(b -> b
                            .type(MessageType.ERROR)
                            .token(em.getToken())
                            .text(em.getText()));

                    } else {
                        LOGGER.warn("Unknown message emitted from stream: {}", wsm.getClass().getName());
                        continue;
                    }

                    LOGGER.debug("emitter.onNext: {}", msg);

                    emitter.onNext(msg);
                }

                if (emitter.isCancelled()) {
                    LOGGER.debug("emitter.cancelled");
                    return;
                }

            } while (!leave);

            LOGGER.debug("emitter.complete");
            emitter.onComplete();

        }, BackpressureStrategy.BUFFER);

        return FlowAdapters.toFlowPublisher(flowable); // return a Java Flow Publisher.

    }

    @Value.Immutable
    public interface EmittedResource extends Resource {

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

    }

    @Value.Immutable
    public interface EmittedError extends Error {

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

    }


}
