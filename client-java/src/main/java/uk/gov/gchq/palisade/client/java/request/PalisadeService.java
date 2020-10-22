/*
 * Copyright 2020 Crown Copyright
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
package uk.gov.gchq.palisade.client.java.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.gchq.palisade.client.java.ClientException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static uk.gov.gchq.palisade.client.java.util.Checks.checkArgument;

/**
 * This class represents the Palisade service and handles the communication
 * between the client and the server. A single instance of this class can be
 * used as it is thread safe.
 *
 * @since 0.5.0
 */
public class PalisadeService implements PalisadeClient {


    private final ObjectMapper objectMapper;
    private final String baseUri;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Creates a new Palisade service
     *
     * @param objectMapper The mapper used to {@code PalisadeRequest} and
     *                     {@code PalisadeResponse} objects to/from JSON.
     * @param baseUri      The uri to send requests to
     */
    public PalisadeService(final ObjectMapper objectMapper, final String baseUri) {
        this.baseUri = checkArgument(baseUri);
        this.objectMapper = checkArgument(objectMapper);
    }

    @Override
    public PalisadeResponse submit(final PalisadeRequest request) {
        var future = submitAsync(request);
        var r = future.join();
        return r;
    }

    @Override
    public CompletableFuture<PalisadeResponse> submitAsync(final PalisadeRequest palisadeRequest) {
        checkArgument(palisadeRequest != null);
        return httpClient
            .sendAsync(createHttpRequest(palisadeRequest), BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenApply(this::readValue);
    }

    private PalisadeResponse readValue(final String string) {
        try {
            return objectMapper().readValue(string, PalisadeResponse.class);
        } catch (IOException ioe) {
            throw new CompletionException(ioe);
        }
    }

    private HttpRequest createHttpRequest(final PalisadeRequest palisadeRequest) {
        String requestBody;
        try {
            requestBody = objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(palisadeRequest);
        } catch (JsonProcessingException e1) {
            throw new ClientException("Failed to parse request: " + palisadeRequest.toString(), e1);
        }
        var uri = URI.create(uri("/registerDataRequest"));
        return HttpRequest.newBuilder(uri)
            .setHeader("User-Agent", "Java 11 HttpClient Bot")
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(requestBody))
            .build();
    }

    private String uri(final String endpoint) {
        var baseUri = baseUri();
        if (baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length() - 2);
        }
        if (endpoint.startsWith("/")) {
            return baseUri + endpoint;
        }
        return baseUri + "/" + endpoint;
    }

    private String baseUri() {
        return this.baseUri;
    }

    private ObjectMapper objectMapper() {
        return this.objectMapper;
    }

}
