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
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.receiver.Receiver;
import uk.gov.gchq.palisade.client.java.receiver.ReceiverContext;
import uk.gov.gchq.palisade.client.java.receiver.ReceiverException;
import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ByteBufferInputStream;

import java.net.MalformedURLException;
import java.net.URL;

import static io.micronaut.http.MediaType.APPLICATION_JSON_TYPE;
import static io.micronaut.http.MediaType.APPLICATION_OCTET_STREAM_TYPE;

/**
 * <p>
 * A runnable class that is responsible for initiating requests to a palisade
 * Data Service. Once the initial response is returned (as a stream), it is
 * published onto the event bus. The {@link DownloadManager} is listening for
 * these events and will queue them up to be emitted via a subscribed stream.
 * </p>
 * <p>
 * Events thrown by instances of this class:
 * </p>
 * <ul>
 * <li><b>DownloadReadyEvent</b> - When a HTTP request has been successfully
 * made and the inputstream is ready to be consumed</li>
 * <li><b>DownloadCompletedEvent</b> - When a download completes and is
 * processed successfully</li>
 * </ul>
 * <p>
 * This class does not listen to any events
 * </p>
 *
 * @see DownloadManager
 * @since 0.5.0
 */
public class Downloader implements Runnable {


    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    private final Resource resource;
    private final Receiver receiver;
    private final ClientContext clientContext;

    /**
     * Creates a new {@code Downloader} from the provided configuration
     *
     * @param clientContext The client context providing access to objects in its
     *                      registry (e.g. ObjectMapper)
     * @param resource      The resource to be downloaded
     * @param receiver      The receiver to which the data stream should be passed
     */
    public Downloader(final ClientContext clientContext, final Resource resource, final Receiver receiver) {
        this.clientContext = clientContext;
        this.resource = resource;
        this.receiver = receiver;
    }

    @Override
    public void run() {

        LOG.debug("Downloader Started");

        URL url = null;
        try {
            url = new URL(resource.getUrl());
        } catch (MalformedURLException e) {
            clientContext.post(DownloadFailedEvent.of(resource, e));
            return; // EARLY EXIT
        }

        try (var client = RxStreamingHttpClient.create(url)) {

            var token = resource.getToken();

            var body = IDataRequest.create(b -> b
                .token(token)
                .leafResourceId(resource.getLeafResourceId()));


            // instead of posting the flowable in an event, we should get hole of a
            // "Receiver" from the DownloadConfig

            var receiverContext = new ReceiverContext() {

                @Override public Resource getResource() {
                    return resource;
                }

                @Override
                public <T> T get(final Class<T> type) {
                    return clientContext.get(type);
                }
            };

            var request = HttpRequest
                .POST("/read/chunked", body)
                .contentType(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_OCTET_STREAM_TYPE);

            LOG.debug("Making request to: {}, ", url);

            var flowable = client
                .dataStream(request)
                .<java.nio.ByteBuffer>map(ByteBuffer::asNioBuffer)
                .observeOn(Schedulers.io());

            LOG.debug("Flowable returned from request");

            try {
                var bbis = new ByteBufferInputStream(flowable);
                receiver.process(receiverContext, bbis);
            } catch (ReceiverException e) {
                clientContext.post(DownloadFailedEvent.of(resource, e));
            }

            // Note:
            // - we no longer need DownloadReadyEvent

            var event = DownloadCompleteEvent.of(resource);

            clientContext.post(event);

            LOG.debug("posted event: {}, ", event);

        } finally { // empty
        }

        LOG.debug("Downloader ended");

    }

}
