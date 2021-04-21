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
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.QueryItem;
import uk.gov.gchq.palisade.client.QueryResponse;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;
import uk.gov.gchq.palisade.client.internal.model.MessageType;
import uk.gov.gchq.palisade.client.internal.model.PalisadeResponse;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketClient;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.TimeUnit;

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

    @Override
    public Publisher<QueryItem> stream() {

        // our flowable must wrap the websocket client

        var flowable = Flowable.create((final FlowableEmitter<DefaultQueryItem> emitter) -> {

            LOGGER.debug("Creating stream...");

            /*
             * Must use a new client here for the websocket connection. If we use the one
             * from the session, the websocket listener hangs.
             */

            var httpClientBuilder = HttpClient.newBuilder();
            if (Boolean.FALSE.equals(session.getConfiguration().<Boolean>get(Configuration.HTTP2_ENABLED))) {
                httpClientBuilder.version(Version.HTTP_1_1);
            }
            var httpClient = httpClientBuilder.build();

            var configuration = session.getConfiguration();

            var webSocketClient = WebSocketClient.createResourceClient(b -> b
                .httpClient(httpClient)
                .objectMapper(session.getObjectMapper())
                .token(palisadeResponse.getToken())
                .uri(configuration.get(Configuration.FILTERED_RESOURCE_URI)));

            webSocketClient.connect();

            LOGGER.debug("Connected to websocket");

            var timeout = session.getConfiguration().<Long>get(Configuration.POLL_SECONDS);
            var loop = true;

            do {
                var wsm = webSocketClient.poll(timeout, TimeUnit.SECONDS);
                if (wsm != null) {
                    if (wsm.getType() == MessageType.COMPLETE) {
                        // we're done, so signal complete and set flag to get out
                        emitter.onComplete();
                        loop = false;
                        LOGGER.debug("emitter.complete");
                    } else {
                        emitter.onNext(new DefaultQueryItem(wsm));
                    }
                }
                if (emitter.isCancelled()) {
                    // we're cancelled, so set flag to get out
                    loop = false;
                    LOGGER.debug("emitter.cancelled");
                }
            } while (loop);

        }, BackpressureStrategy.BUFFER);

        return FlowAdapters.toFlowPublisher(flowable); // return a Java Flow Publisher.
    }

}
