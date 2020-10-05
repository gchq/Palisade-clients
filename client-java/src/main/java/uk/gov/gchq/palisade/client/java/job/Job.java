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
package uk.gov.gchq.palisade.client.java.job;

import org.reactivestreams.Publisher;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.ClientConfig;
import uk.gov.gchq.palisade.client.java.download.*;
import uk.gov.gchq.palisade.client.java.resource.*;

import javax.websocket.ContainerProvider;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;

import static uk.gov.gchq.palisade.client.java.download.DownloadManager.createDownloadManager;
import static uk.gov.gchq.palisade.client.java.resource.ResourceClient.createResourceClient;

/**
 * A Job represents a submitted request to the Palisade service. A job is
 * responsible in orchestrating the retrieval of reources from the Filtered
 * Resource Service and the accessing and downloading the data from a Data
 * service.
 *
 * @author dbell
 *
 * @param <E> The type of deserialised instances
 */
public class Job<E> {

    private static final Logger log = LoggerFactory.getLogger(Job.class);
    private static final String EVENT_CAUGHT = "Job [{}] caught event: {}";

    private final String id;
    private final JobContext<E> jobContext;

    private DownloadManager downloadManager;

    /**
     * Returns a new Job configured with the provided job context
     *
     * @param jobContext The job context containing relevant information about the
     *                   current job. For example, this would be the request and
     *                   response etc.
     */
    public Job(String id, JobContext<E> jobContext) {
        this.id = id;
        this.jobContext = jobContext;
    }

    /**
     * This method will actually start the job - open the web socket with the server
     * and negotiate connection - receive each resource - spawn a new task to connect
     * to data service and receive the data.
     *
     * Really not sure how we are going to test this.
     */
    public Publisher<Download> start() {

        var appctx = jobContext.getApplicationContext();

        var token = jobContext.getResponse().getToken();
        var eventBus = jobContext.getEventBus();
        var numThreads = appctx.getBean(ClientConfig.class).getDownload().getThreads();
        var objectMapper = appctx.getBean(ObjectMapper.class);

        /*
         * The DownloadManager orchestrates the download of 1 or more downloads from the
         * data service.
         *
         * Raised events: * DownloadStartedEvent - published when a thread is starting a
         * download, but before any external connections are made *
         * DownloadCompletedEvent - published when a download has completed successfully
         * * DownloadFailedEvent - published when a download has failed for some reason
         * * DownloadReadyEvent - published the server has been called and an input
         * stream is ready to be consumed Consumed events: * ResourceReadyEvent -
         * published when the websocket server get a CTS and then sends a resource
         */

        this.downloadManager = createDownloadManager(b -> b
                .applicationContext(appctx)
                .id("dlm:" + token)
                .eventBus(eventBus));

        var publisher = downloadManager.subscribe();

        /*
         * This tracker just exposes from the download manager what clients need to
         * manage downloads. The download manager should not be needed by any other
         * object
         */

        var downloadTracker = downloadManager.getDownloadTracker();

        /*
         * The resource client.
         *
         * Raised events: * ResourceReadyEvent - published when a resource message is
         * received and ready to be downloaded * ResourcesComplete - published when
         * there are no more resources to consume
         *
         * The resource client does not do anything yet. It will be wired up to the
         * WebSocketContainer when Job.start() is called
         */

        var resourceClient = createResourceClient(b -> b
                .eventBus(eventBus)
                .mapper(objectMapper)
                .downloadTracker(downloadTracker)
                .token(token));

        /*
         * Subscribe some major parts to the eventbus
         *
         */

        eventBus.register(downloadManager);
        eventBus.register(resourceClient);

        /*
         * Now we wire up the resource (websocket) client to the WebSocketContainer As
         * soon as this is done, the connection will be made and the onXXX handlers will
         * start to get fired in the resource client. The first is onOpen.
         */

        try {


            var url = getContext().getResponse().getUrl();
            var container = ContainerProvider.getWebSocketContainer();

            container.connectToServer(resourceClient, new URI(url)); // this start communication

            log.debug("Job [{}] started", id);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return publisher;

    }

    @Subscribe
    public void schedule(ResourceReadyEvent event) {
        var resource = event.getResource();
        this.downloadManager.schedule(resource);
    }

    @Subscribe
    public void onDownloadStarted(DownloadStartedEvent event) {
        log.debug(EVENT_CAUGHT, id, event);
    }

    @Subscribe
    public void onDownloadReady(DownloadReadyEvent event) {
        log.debug(EVENT_CAUGHT, id, event.getToken());
        var ds = getTempDS();
        var is = event.getInputStream();
        var object = ds.deserialize(is);

        // now we need to do something with it.
        // just log it out form now
        log.debug("Downloaded: {}", object);
    }

    @Subscribe
    public void onDownloadCompleted(DownloadCompletedEvent event) {
        log.debug(EVENT_CAUGHT, id, event);
    }

    @Subscribe
    public void onDownloadFailed(DownloadFailedEvent event) {
        log.debug(EVENT_CAUGHT, id, event);
        log.debug("Download failed", event.getThrowble());
    }

    @Subscribe
    public void onJobComplete(ResourcesExhaustedEvent event) {
        log.debug(EVENT_CAUGHT, id, event);
    }

    public String getId() {
        return this.id;
    }

    JobContext<E> getContext() {
        return this.jobContext;
    }

    private Deserializer<String> getTempDS() {

        return stream -> {
            var bufferSize = 1024;
            var buffer = new char[bufferSize];
            var out = new StringBuilder();
            var in = new InputStreamReader(stream, StandardCharsets.UTF_8);
            int charsRead;
            try {
                while ((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                    out.append(buffer, 0, charsRead);
                }
            } catch (IOException ioe) {
                throw new DeserialiserException("Failed to read stream", ioe);
            }
            return out.toString();
        };

    }

    public DownloadTracker getDownLoadTracker() {
        return this.downloadManager.getDownloadTracker();
    }
}
