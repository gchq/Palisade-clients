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
package uk.gov.gchq.palisade.client.java.download;

import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.*;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.reactivex.schedulers.Schedulers;
import org.immutables.value.Value;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.receiver.*;
import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.net.*;
import java.util.Objects;
import java.util.function.*;

import com.google.common.eventbus.EventBus;

/**
 * <p>
 * A runnable class that is responsible for initiating requests to a palisade
 * Data Service. Once the initial response is returned (as a stream), it is
 * published onto the event bus. The {@link DownloadManager} is listening for
 * these events and will queue them up to be emitted via a subscribed stream.
 * </p>
 * <p>
 * Events thrown by instances of this class:
 * <ul>
 * <li><b>DownloadReadyEvent</b> - When a HTTP request has been successfully
 * made and the inputstream is ready to be consumed</li>
 * <li><b>DownloadFailedEvent</b> - When an error occurs either during the
 * request, or posting to the event bus</li>
 * </ul>
 * </p>
 * <p>
 * This class does not listen to any events
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 * @see DownloadManager
 */
public class Downloader implements Runnable {

    /**
     * The configuration object used when configuring a {@code Downloader}
     *
     * @author dbell
     * @since 0.5.0
     *
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IDownloadConfig {

        /**
         * Helper method that uses a function to create a new instance
         *
         * @param func The builder function
         * @return a newly created {@code DownloadConfig}
         */
        public static DownloadConfig create(UnaryOperator<DownloadConfig.Builder> func) {
            return func.apply(DownloadConfig.builder()).build();
        }

        /**
         * Returns the event bus onto which the {@code Downloader} will publish download
         * events
         *
         * @return the event bus onto which the {@code Downloader} will publish download
         *         events
         */
        EventBus getEventBus();

        /**
         * Returns the details of the resource to be downloaded
         *
         * @return the details of the resource to be downloaded
         */
        Resource getResource();

        /**
         * Returns the factory that we will use to create the reciever instance
         *
         * @return the factory that we will use to create the reciever instance
         */
        Supplier<Receiver> getReceiverSupplier();

        /**
         * @return
         */
        ClientContext getClientContext();

    }

    /**
     * Returns a newly created {@code Downloader} instance configured via the
     * provided builder function
     *
     * @param func The function used to configure the returned instance
     * @return a newly created {@code Downloader} instance configured via the
     *         provided builder function
     */
    static Downloader create(UnaryOperator<DownloadConfig.Builder> func) {
        return new Downloader(func.apply(DownloadConfig.builder()).build());
    }

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);
    private final DownloadConfig config;

    /**
     * Creates a new {@code Downloader} from the provided configuration
     *
     * @param config The provided configuration for this {@code Downloader}
     */
    private Downloader(DownloadConfig config) {
        this.config = Objects.requireNonNull(config, "Must have a configuration");
    }

    @Override
    public void run() {

        log.debug("Downloader Started");

        var resource = config.getResource();
        var eventBus = config.getEventBus();
        var token = resource.getToken();

        URL url = null;
        try {
            url = new URL(resource.getUrl());
        } catch (MalformedURLException e) {
            config.getEventBus().post(DownloadFailedEvent.of(token, resource, e));
            return; // EARLY EXIT
        }

        try (var client = RxStreamingHttpClient.create(url)) {

            var body = IDataRequest.create(b -> b
                .token(token)
                .leafResourceId(resource.getLeafResourceId()));

            var request = HttpRequest
                .POST("/read/chunked", body)
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE);

            log.debug("Making request to: {}, " + url);

            var flowable = client
                .dataStream(request)
                .<java.nio.ByteBuffer>map(ByteBuffer::asNioBuffer)
                .observeOn(Schedulers.io());

            log.debug("Flowable returned from request");

            // instead of posting the flowable in an event, we should get hole of a
            // "Receiver" from the DownloadConfig

            var receiverContext = new ReceiverContext() {
                @Override public Resource getResource() {
                    return resource;
                }
                @Override public EventBus getEventBus() {
                    return eventBus;
                }
                @Override
                public <T> T get(Class<T> type) {
                    return config.getClientContext().get(type);
                }
            };

            var receiver = config.getReceiverSupplier().get();
            var bbis = new ByteBufferInputStream(flowable);

            try {

                receiver.process(receiverContext, bbis);

            } catch (Exception e) {
                // TODO: handle this (throw a DownloadFailedEvent maybe?)
            }

            // Note:
            // - we no longer need DownloadReadyEvent

            var event = DownloadCompleteEvent.of(token, resource);

            config.getEventBus().post(event);

            log.debug("posted event: {}, " + event);

        } finally {
        }

        log.debug("Downloader ended");

    }

}