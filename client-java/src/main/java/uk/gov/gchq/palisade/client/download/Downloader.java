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
package uk.gov.gchq.palisade.client.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.receiver.ReceiverContext;
import uk.gov.gchq.palisade.client.receiver.ReceiverException;
import uk.gov.gchq.palisade.client.resource.Resource;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.download.IDataRequest.createDataRequest;
import static uk.gov.gchq.palisade.client.download.IDownloadResult.createDownloadResult;
import static uk.gov.gchq.palisade.client.util.Checks.checkArgument;

/**
 * A {@code Downloader} is responsible for initiating requests to a palisade
 * Data Service and processing its response.
 *
 * @see DownloadManager
 * @since 0.5.0
 */
public class Downloader implements ReceiverContext {

    /**
     * Data class containing the setup for a {@code Downloader}
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IDownloaderSetup {

        /**
         * Returns the id for this download session. If the id is omitted during
         * construction, a pre-created initializer method will be called to retrieve a
         * default id.
         *
         * @return the id or a default one if not specified on the builder
         */
        @Value.Default
        default UUID getId() {
            return UUID.randomUUID();
        }

        /**
         * Returns the {@code Receiver} instance which will be used to process the
         * download {@code InputStream}
         *
         * @return the {@code Receiver} instance which will be used to process the
         *         download {@code InputStream}
         */
        Receiver getReceiver();

        /**
         * Returns the resource to be downloaded. The resource contains the url of where
         * to download it from
         *
         * @return the resource to be downloaded
         */
        Resource getResource();

        /**
         * Returns the object mapper to be used when converting request bodies to json
         * strings
         *
         * @return the object mapper to be used when converting request bodies to json
         *         strings
         */
        ObjectMapper getObjectMapper();

        /**
         * Returns a map of properties which will be provided to the Receiver via a
         * {@code ReceiverContext} instance
         *
         * @return a map of properties which will be provided to the Receiver via a
         *         {@code ReceiverContext} instance
         */
        Map<String, Object> getProperties();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

    /**
     * Helper method to create a {@code Downloader} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static Downloader createDownloader(final UnaryOperator<DownloaderSetup.Builder> func) {
        checkArgument(func);
        return new Downloader(func.apply(DownloaderSetup.builder()).build());
    }

    private final DownloaderSetup setup;

    private Downloader(final DownloaderSetup setup) {
        assert setup != null : "Cannot create downloader without a setup!";
        this.setup = setup;
    }

    /**
     * Start the download process
     *
     * @return a download result after successful completion
     * @throws DownloaderException if any error occurs
     */
    public DownloadResult start() {

        LOGGER.debug("Downloader Started");

        var mapper = setup.getObjectMapper();
        var resource = setup.getResource();
        var receiver = setup.getReceiver();

        try {

            var uri = createUri(resource.getUrl(), "/read/chunked");
            var requestBody = createBody(resource, mapper);
            var httpResponse = sendRequest(requestBody, uri);

            var statusCode = httpResponse.statusCode();
            if (statusCode != 200) {
                String msg;
                if (statusCode == 404) {
                    msg = String.format("Resource \"%s\" not found", resource.getLeafResourceId());
                } else {
                    msg = "Request to DataService failed";
                }
                throw new DownloaderException(msg, httpResponse.statusCode());
            }

            processStream(httpResponse.body(), receiver, this);

            return createDownloadResult(b -> b.id(setup.getId()));

        } catch (ReceiverException e) {
            throw new DownloaderException("Caught exception from receiver", e);
        } catch (DownloaderException e) {
            throw e;
        } catch (Exception e) {
            throw new DownloaderException("Caught unknown exception", e);
        }

    }

    @Override
    public Resource getResource() {
        return setup.getResource();
    }

    @Override
    public Optional<Object> findProperty(final String key) {
        checkArgument(key);
        return Optional.ofNullable(setup.getProperties().get(key));
    }

    /**
     * Returns the downloadId associated with this downloader
     *
     * @return the downloadId associated with this downloader
     */
    UUID getDownloadId() {
        return setup.getId();
    }

    private static URI createUri(final String baseUri, final String endpoint) {

        assert baseUri != null : "Need the base uri";
        assert baseUri != null : "Need the uri endpoint to append to the base uri";

        var uri = new StringBuilder();
        if (baseUri.endsWith("/")) {
            uri.append(baseUri.substring(0, baseUri.length() - 2));
        } else {
            uri.append(baseUri);
        }
        if (!endpoint.startsWith("/")) {
            uri.append("/");
        }
        uri.append(endpoint);
        return URI.create(uri.toString());
    }

    private static HttpResponse<InputStream> sendRequest(final String requestBody, final URI uri) {

        assert requestBody != null : "Need a request body to send";
        assert uri != null : "Need a URI so we know where to send the request";

        var httpRequest = HttpRequest.newBuilder(uri)
            .setHeader("User-Agent", "Palisade Java Client")
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(requestBody))
            .build();

        LOGGER.debug("Sending request:\n{}", requestBody);

        try {
            var httpResponse = HTTP_CLIENT.send(httpRequest, BodyHandlers.ofInputStream());
            LOGGER.debug("Got http status: {}", httpResponse.statusCode());
            return httpResponse;
        } catch (IOException | InterruptedException e1) {
            Thread.currentThread().interrupt();
            throw new DownloaderException("Error occurred making request to data service", e1);
        }

    }

    private static String createBody(final Resource resource, final ObjectMapper objectMapper) {

        assert resource != null : "Need a resource to create a request from";
        assert resource != null : "Need an object mapper to convert request object to a json string";

        var dataRequest = createDataRequest(b -> b
            .token(resource.getToken())
            .leafResourceId(resource.getLeafResourceId()));

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataRequest);
        } catch (JsonProcessingException e1) {
            throw new DownloaderException("Failed to parse request body", e1);
        }

    }

    private static void processStream(final InputStream is, final Receiver rc, final ReceiverContext rcCtx)
        throws ReceiverException {

        assert is != null : "Need an inputstream for the reciver to process";
        assert rc != null : "Need a receiver to process the inmput stream";
        assert rcCtx != null : "Need a receiver context which provides access to other services";

        try (is) {
            rc.process(rcCtx, is);
        } catch (Exception e) {
            throw new ReceiverException("Caught unexpected error", e);
        }

    }

}