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
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.receiver.*;
import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.ByteBufferInputStream;

import java.net.*;

import static io.micronaut.http.MediaType.*;

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
 * @author dbell
 * @since 0.5.0
 * @see DownloadManager
 */
public class Downloader implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(Downloader.class);

    private Resource resource;
    private Receiver receiver;
    private ClientContext clientContext;

    /**
     * Creates a new {@code Downloader} from the provided configuration
     *
     * @param clientContext
     * @param resource
     * @param receiver
     */
    public Downloader(ClientContext clientContext, Resource resource, Receiver receiver) {
        this.clientContext = clientContext;
        this.resource = resource;
        this.receiver = receiver;
    }

    @Override
    public void run() {

        log.debug("Downloader Started");

        var token = resource.getToken();

        URL url = null;
        try {
            url = new URL(resource.getUrl());
        } catch (MalformedURLException e) {
            clientContext.post(DownloadFailedEvent.of(resource, e));
            return; // EARLY EXIT
        }

        try (var client = RxStreamingHttpClient.create(url)) {

            var body = IDataRequest.create(b -> b
                .token(token)
                .leafResourceId(resource.getLeafResourceId()));

            var request = HttpRequest
                .POST("/read/chunked", body)
                .contentType(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_OCTET_STREAM_TYPE);

            log.debug("Making request to: {}, ", url);

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
                @Override
                public <T> T get(Class<T> type) {
                    return clientContext.get(type);
                }
            };

            try {
                var bbis = new ByteBufferInputStream(flowable);
                receiver.process(receiverContext, bbis);
            } catch (Exception e) {
                clientContext.post(DownloadFailedEvent.of(resource, e));
            }

            // Note:
            // - we no longer need DownloadReadyEvent

            var event = DownloadCompleteEvent.of(resource);

            clientContext.post(event);

            log.debug("posted event: {}, ", event);

        } finally { // empty
        }

        log.debug("Downloader ended");

    }

}