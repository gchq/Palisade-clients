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
package uk.gov.gchq.palisade.client.java.internal.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.ClientException;
import uk.gov.gchq.palisade.client.java.internal.model.PalisadeRequest;
import uk.gov.gchq.palisade.client.java.internal.model.PalisadeResponse;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.java.util.Checks.checkNotNull;

/**
 * This class represents the Palisade Service and handles the communication
 * between the client and the server. A single instance of this class can be
 * used as it is thread safe.
 *
 * @since 0.5.0
 */
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
     * Helper method to create a {@link PalisadeService} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // Unary Operator vs Function
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
        return submitAsync(palisadeRequest).join();
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
        var jsonBody = toJson(palisadeRequest);
        var bodyPublisher = BodyPublishers.ofString(jsonBody);

        LOGGER.debug("SEND: To: [{}], Body: [{}]", uri, palisadeRequest);

        var httpClient = getHttpClient();

        var httpRequest = HttpRequest.newBuilder(uri)
            .setHeader("Content-Type", "application/json")
            .POST(bodyPublisher)
            .build();

        return httpClient
            .sendAsync(httpRequest, BodyHandlers.ofString())
            .thenApply(PalisadeService::checkStatusOK)
            .thenApply(HttpResponse::body)
            .thenApply(this::toResponse)
            .thenApply((final PalisadeResponse pr) -> {
                LOGGER.debug("RCVD: {}", pr);
                return pr;
            });

    }

    /**
     * Check status of provided {@code HttpResponse} is OK.
     *
     * @param <T>      The type of response body
     * @param response the HTTP response
     * @return the provided response
     */
    public static <T> HttpResponse<T> checkStatusOK(final HttpResponse<T> response) {
        int status = response.statusCode();
        if (!IS_HTTP_OK.test(status)) {
            var body = response.body();
            String msg;
            if (body != null) {
                msg = String.format("Request to Palisade Service failed (%s) with body:%n%s", status, body);
            } else {
                msg = String.format("Request to Palisade Service failed (%s) with no body", status);
            }
            throw new ClientException(msg);
        }
        return response;
    }

    // placed in a method to be use fluently as a method reference
    private String toJson(final Object object) {
        try {
            return objectMapper().writeValueAsString(object);
        } catch (JsonProcessingException cause) {
            throw new ClientException("Failed to serialise request: " + object.toString(), cause);
        }
    }

    // placed in a method to be use fluently as a method reference
    private PalisadeResponse toResponse(final String string) {
        try {
            return objectMapper().readValue(string, PalisadeResponse.class);
        } catch (JsonProcessingException cause) {
            throw new ClientException("Failed to deserialise request: " + string, cause);
        }
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
