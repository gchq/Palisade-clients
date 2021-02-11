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
package uk.gov.gchq.palisade.client.internal.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketListener.Item;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;
import uk.gov.gchq.palisade.client.util.Util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * This class represents the Palisade Service and handles the communication
 * between the client and the server. A single instance of this class can be
 * used as it is thread safe.
 *
 * @since 0.5.0
 */
@SuppressWarnings("java:S3242") // stop erroneous "use general type" message
public final class PalisadeService {

    /**
     * Provides the setup for the downloader
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface PalisadeServiceSetup {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutablePalisadeServiceSetup.Builder { // empty
        }

        /**
         * Returns the {@code HttpClient}
         *
         * @return the {@code HttpClient}
         */
        HttpClient getHttpClient();

        /**
         * Returns the object mapper used for (de)serialisation of websocket messages
         *
         * @return the object mapper used for (de)serialisation of websocket messages
         */
        ObjectMapper getObjectMapper();

        /**
         * Returns the full URI of the palisade service endpoint to call
         *
         * @return the full URI of the palisade service endpoint to call
         */
        URI getUri();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PalisadeService.class);
    private static final IntPredicate IS_HTTP_OK = sts -> sts == 200 || sts == 202;

    private final PalisadeServiceSetup setup;

    /**
     * Creates a new Palisade service
     *
     * @param setup The setup
     */
    private PalisadeService(final PalisadeServiceSetup setup) {
        this.setup = checkNotNull(setup);
    }

    /**
     * Helper method to create a {@link Item} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    public static PalisadeService createPalisadeService(final UnaryOperator<PalisadeServiceSetup.Builder> func) {
        return new PalisadeService(func.apply(new PalisadeServiceSetup.Builder()).build());
    }

    /**
     * Returns a response from Palisade for the given request
     *
     * @param palisadeRequest which will be passed to Palisade
     * @return the response from Palisade
     */
    public PalisadeResponse submit(final PalisadeRequest palisadeRequest) {
        var future = submitAsync(palisadeRequest);
        var palisadeResponse = future.join();
        LOGGER.debug("RCVD: {}", palisadeResponse);
        return palisadeResponse;
    }

    /**
     * Returns a completable future which will provide the response from palisade
     * for the given request
     *
     * @param palisadeRequest which will be passed to Palisade
     * @return a completable future which will provide the response from Palisade
     */
    public CompletableFuture<PalisadeResponse> submitAsync(final PalisadeRequest palisadeRequest) {

        checkNotNull(palisadeRequest);

        var uri = getUri();
        var requestBody = toJson(palisadeRequest);
        var body = BodyPublishers.ofString(requestBody);

        LOGGER.debug("SEND: To: [{}], Body: [{}]", uri, palisadeRequest);

        var httpRequest = HttpRequest.newBuilder(uri)
            .setHeader("User-Agent", "Palisade Java Client")
            .setHeader("Content-Type", "application/json")
            .POST(body)
            .build();

        return getHttpClient()
            .sendAsync(httpRequest, BodyHandlers.ofString())
            .thenApply(PalisadeService::checkStatusOK)
            .thenApply(HttpResponse::body)
            .thenApply(this::toResponse);

    }

    static <T> HttpResponse<T> checkStatusOK(final HttpResponse<T> resp) {
        int status = resp.statusCode();
        if (!IS_HTTP_OK.test(status)) {
            var body = resp.body();
            String msg = null;
            if (body != null) {
                msg = String.format("Request to palisade service failed (%s) with body:%n%s", status, body);
            } else {
                msg = String.format("Request to palisade service failed (%s) with no body", status);
            }
            throw new ClientException(msg);
        }
        return resp;
    }

    // placed in a method to be use fluently as a method reference
    private String toJson(final Object object) {
        return Util.toJson(objectMapper(), object,
            cause -> new ClientException("Failed to serialise request: " + object.toString(), cause));
    }

    // placed in a method to be use fluently as a method reference
    private PalisadeResponse toResponse(final String string) {
        return Util.toInstance(objectMapper(), string, PalisadeResponse.class,
            cause -> new ClientException("Failed to deserialise request: " + string, cause));
    }

    private PalisadeServiceSetup getSetup() {
        return this.setup;
    }

    private ObjectMapper objectMapper() {
        return getSetup().getObjectMapper();
    }

    private HttpClient getHttpClient() {
        return getSetup().getHttpClient();
    }

    private URI getUri() {
        return getSetup().getUri();
    }

}
