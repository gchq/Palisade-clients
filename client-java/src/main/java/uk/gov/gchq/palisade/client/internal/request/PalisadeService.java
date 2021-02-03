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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ClientException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.IntPredicate;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * This class represents the Palisade service and handles the communication
 * between the client and the server. A single instance of this class can be
 * used as it is thread safe.
 *
 * @since 0.5.0
 */
public class PalisadeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PalisadeService.class);
    private static final IntPredicate IS_HTTP_OK = sts -> sts == 200 || sts == 202;

    private final ObjectMapper objectMapper;
    private final URI uri;

    // Once created, an HttpClient instance is immutable, thus automatically
    // thread-safe, and multiple requests can be sent with it
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Creates a new Palisade service
     *
     * @param objectMapper The mapper used to {@code PalisadeRequest} and
     *                     {@code PalisadeResponse} objects to/from JSON.
     * @param uri          The uri to send requests to
     */
    public PalisadeService(final ObjectMapper objectMapper, final URI uri) {
        this.uri = checkNotNull(uri);
        this.objectMapper = checkNotNull(objectMapper);
    }

    /**
     * Returns a response from palisade for the given request
     *
     * @param palisadeRequest which will be passed to Palisade
     * @return the response from palisade
     */
    public PalisadeResponse submit(final PalisadeRequest palisadeRequest) {
        var future = submitAsync(palisadeRequest);
        var palisadeResponse = future.join();
        LOGGER.debug("Got response from Palisade: {}", palisadeResponse);
        return palisadeResponse;
    }

    /**
     * Returns a completable future which will provide the response from palisade
     * for the given request
     *
     * @param palisadeRequest which will be passed to Palisade
     * @return a completable future which will provide the response from palisade
     */
    public CompletableFuture<PalisadeResponse> submitAsync(final PalisadeRequest palisadeRequest) {

        checkNotNull(palisadeRequest);

        LOGGER.debug("Submitting request to Palisade: {}", palisadeRequest);

        var requestBody = toJson(palisadeRequest);
        var body = BodyPublishers.ofString(requestBody);

        LOGGER.debug("Submitting request to: {}", uri);

        var httpRequest = HttpRequest.newBuilder(uri)
            .setHeader("User-Agent", "Palisade Java Client")
            .setHeader("Content-Type", "application/json")
            .POST(body)
            .build();

        return httpClient
            .sendAsync(httpRequest, BodyHandlers.ofString())
            .thenApply(PalisadeService::checkStatusOK)
            .thenApply(HttpResponse::body)
            .thenApply(this::toResponse);

    }

    private static <T> HttpResponse<T> checkStatusOK(final HttpResponse<T> resp) {
        int status = resp.statusCode();
        if (!IS_HTTP_OK.test(status)) {
            throw new ClientException(
                "Request to palisade service failed (" + status + "), response\n" + resp.body().toString());
        }
        return resp;
    }

    private String toJson(final Object object) {
        try {
            return objectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e1) {
            throw new ClientException("Failed to parse request: " + object.toString(), e1);
        }
    }

    private PalisadeResponse toResponse(final String string) {
        try {
            return objectMapper().readValue(string, PalisadeResponse.class);
        } catch (IOException ioe) {
            throw new CompletionException(ioe);
        }
    }

    private ObjectMapper objectMapper() {
        return this.objectMapper;
    }

}
