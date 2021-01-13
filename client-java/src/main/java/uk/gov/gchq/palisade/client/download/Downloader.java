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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.receiver.Receiver.IReceiverResult;
import uk.gov.gchq.palisade.client.receiver.ReceiverContext;
import uk.gov.gchq.palisade.client.receiver.ReceiverException;
import uk.gov.gchq.palisade.client.resource.ResourceMessage;
import uk.gov.gchq.palisade.client.util.Configuration;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.util.Checks.checkArgument;

/**
 * A {@code Downloader} is responsible for initiating requests to a palisade
 * Data Service and processing its response.
 *
 * @see DownloadManager
 * @since 0.5.0
 */
public final class Downloader implements ReceiverContext {

    /**
     * Data class containing the setup for a {@code Downloader}
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
        Configuration getConfiguration();

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
        ResourceMessage getResource();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();
    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_NOT_FOUND = 404;

    private final DownloaderSetup setup;

    private Downloader(final DownloaderSetup setup) {
        assert setup != null : "Cannot create downloader without a setup!";
        this.setup = setup;
    }

    /**
     * Helper method to create a {@code Downloader} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    static Downloader createDownloader(final UnaryOperator<DownloaderSetup.Builder> func) {
        checkArgument(func);
        return new Downloader(func.apply(new DownloaderSetup.Builder()).build());
    }

    @Override
    public Optional<Object> findProperty(final String key) {
        checkArgument(key);
        return setup.getConfiguration().find(key);
    }

    /**
     * Returns the downloadId associated with this downloader
     *
     * @return the downloadId associated with this downloader
     */
    UUID getDownloadId() {
        return setup.getId();
    }

    @Override
    public ResourceMessage getResource() {
        return setup.getResource();
    }

    /**
     * Start the download process
     *
     * @return a download result after successful completion
     * @throws DownloaderException if any error occurs
     */
    @SuppressWarnings("java:S2221")
    public DownloadResult start() {

        LOGGER.debug("Downloader Started");

        var mapper = setup.getObjectMapper();
        var resource = setup.getResource();
        var receiver = setup.getReceiver();

        try {

            var uri = Util.createUri(resource.getUrl(), setup.getConfiguration().getDataPath());

            var requestBody = mapper
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

            var receiverResult = processStream(httpResponse.body(), receiver, this);

            return DownloadResult.createDownloadResult(b -> b
                .id(setup.getId())
                .properties(receiverResult.getProperties()));

        } catch (ReceiverException e) {
            throw new DownloaderException("Caught exception from receiver: " + e.getMessage(), e);
        } catch (DownloaderException e) {
            throw e;
        } catch (Exception e) {
            throw new DownloaderException("Caught unknown exception: " + e.getMessage(), e);
        } finally {
            LOGGER.debug("Downloader Ended");
        }

    }

    private static IReceiverResult processStream(final InputStream is, final Receiver rc, final ReceiverContext rcCtx)
        throws ReceiverException {
        try (is) {
            return rc.process(rcCtx, is);
        } catch (Exception e) {
            throw new ReceiverException("Caught unexpected error: " + e.getMessage(), e);
        }

    }

    private static HttpResponse<InputStream> sendRequest(final String requestBody, final URI uri) {

        var httpRequest = HttpRequest.newBuilder(uri)
            .setHeader("User-Agent", "Palisade Java Client")
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(requestBody))
            .build();

        LOGGER.debug("Sending request to {} :\n{}", uri, requestBody);

        try {
            var httpResponse = HTTP_CLIENT.send(httpRequest, BodyHandlers.ofInputStream());
            LOGGER.debug("Got http status: {}", httpResponse.statusCode());
            return httpResponse;
        } catch (IOException | InterruptedException e1) {
            Thread.currentThread().interrupt();
            throw new DownloaderException("Error occurred making request to data service", e1);
        }

    }

}
