/*
 * Copyright 2020-2021 Crown Copyright
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
package uk.gov.gchq.palisade.client.internal.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.Resource;
import uk.gov.gchq.palisade.client.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * A {@code Downloader} is responsible for initiating requests to a palisade
 * Data Service and processing its response.
 *
 * @since 0.5.0
 */
public final class Downloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_NOT_FOUND = 404;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

    private final ObjectMapper objectMapper;
    private final String endpoint;

    /**
     * Returns a new instance initialised with the provided object mapper and data
     * service endpoint.
     *
     * @param objectMapper to be used to serialise the request send to the data
     *                     service
     * @param endpoint     The endpoint (e.g. /data/read/chunked)
     */
    public Downloader(final ObjectMapper objectMapper, final String endpoint) {
        this.objectMapper = checkNotNull(objectMapper, "needs an object mapper");
        this.endpoint = checkNotNull(endpoint, "needs an endpoint to create uri");
    }

    /**
     * Start the download process
     *
     * @param resource The resource to fetch
     * @return a download result after successful completion
     * @throws DownloaderException if any error occurs
     */
    @SuppressWarnings("java:S2221")
    public DownloadImpl fetch(final Resource resource) {

        LOGGER.debug("Downloader Started");

        try {

            // using the create method here as the url is assumed correct as it is provided
            // by palisade

            // create the url which is made p of the base url which is provided as part of
            // the resource returned from the filtered resource service and the endpoint

            var uri = new URI(Util.createUrl(resource.getUrl(), endpoint));

            var requestBody = objectMapper
                .writeValueAsString(DataRequest.createDataRequest(b -> b
                    .token(resource.getToken())
                    .leafResourceId(resource.getLeafResourceId())));

            var httpResponse = sendRequest(requestBody, uri);

            var statusCode = httpResponse.statusCode();

            if (statusCode != HTTP_STATUS_OK) {
                String msg;
                if (statusCode == HTTP_STATUS_NOT_FOUND) {
                    msg = String.format("Resource \"%s\" not found", resource.getLeafResourceId());
                } else {
                    msg = "Request to DataService failed";
                }
                throw new DownloaderException(msg, statusCode);
            }

            return new DownloadImpl(httpResponse);

        } catch (DownloaderException e) {
            throw e;
        } catch (Exception e) {
            throw new DownloaderException("Caught unknown exception: " + e.getMessage(), e);
        } finally {
            LOGGER.debug("Downloader Ended");
        }

    }

    private static HttpResponse<InputStream> sendRequest(final String requestBody, final URI uri) {

        LOGGER.debug("Preparing to send request to {}", uri);

        var httpRequest = HttpRequest.newBuilder(uri)
            .setHeader("User-Agent", "Palisade Java Client")
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(requestBody))
            .build();

        try {
            LOGGER.debug("Sending...");
            var httpResponse = HTTP_CLIENT.send(httpRequest, BodyHandlers.ofInputStream());
            LOGGER.debug("Got http status: {}", httpResponse.statusCode());
            return httpResponse;
        } catch (IOException | InterruptedException e1) {
            Thread.currentThread().interrupt();
            throw new DownloaderException("Error occurred making request to data service", e1);
        }

    }

}
