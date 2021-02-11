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
package uk.gov.gchq.palisade.client.internal.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.Resource;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketListener.Item;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;
import uk.gov.gchq.palisade.client.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * A {@code Downloader} is responsible for initiating requests to a palisade
 * Data Service and processing its response.
 *
 * @since 0.5.0
 */
@SuppressWarnings("java:S3242") // stop erroneous "use general type" message
public final class Downloader {

    /**
     * Provides the setup for the downloader
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface DownloaderSetup {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableDownloaderSetup.Builder { // empty
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
         * Returns the path portion of the URL
         *
         * @return the path portion of the URL that should be use when making calls to
         *         the Data Service
         */
        String getPath();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_NOT_FOUND = 404;

    private final DownloaderSetup setup;

    /**
     * Returns a new {@code Downloader}
     *
     * @param setup used to configure this instance
     */
    private Downloader(final DownloaderSetup setup) {
        this.setup = checkNotNull(setup, "missing setup");
    }

    /**
     * Helper method to create a {@link Item} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    public static Downloader createDownloader(final UnaryOperator<DownloaderSetup.Builder> func) {
        return new Downloader(func.apply(new DownloaderSetup.Builder()).build());
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

            // create the url which is made up of the base url which is provided as part of
            // the resource returned from the Filtered Resource Service and the endpoint

            var uri = Util.createUri(resource.getUrl(), getPath());

            var requestBody = getObjectMapper()
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

    private HttpResponse<InputStream> sendRequest(final String requestBody, final URI uri) {

        LOGGER.debug("Preparing to send request to {}", uri);

        var httpRequest = HttpRequest.newBuilder(uri)
            .setHeader("User-Agent", "Palisade Java Client")
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(requestBody))
            .build();

        try {
            LOGGER.debug("Sending...");
            var httpResponse = getHttpClient().send(httpRequest, BodyHandlers.ofInputStream());
            LOGGER.debug("Got http status: {}", httpResponse.statusCode());
            return httpResponse;
        } catch (IOException | InterruptedException e1) {
            Thread.currentThread().interrupt();
            throw new DownloaderException("Error occurred making request to data service", e1);
        }

    }

    private HttpClient getHttpClient() {
        return setup.getHttpClient();
    }

    private ObjectMapper getObjectMapper() {
        return setup.getObjectMapper();
    }

    private String getPath() {
        return setup.getPath();
    }

}
