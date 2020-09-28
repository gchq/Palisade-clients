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

import org.slf4j.Logger;

import uk.gov.gchq.palisade.client.java.download.DownloadCompletedEvent;
import uk.gov.gchq.palisade.client.java.resource.ResourcesExhaustedEvent;

import javax.websocket.ContainerProvider;

import java.net.URI;

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

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Job.class);

    private final String id;
    private final JobContext<E> jobContext;

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

    public JobContext<E> getContext() {
        return this.jobContext;
    }

    /**
     * This method will actually start the job - open the web socket with the server
     * and negotiate connection - receive each resource - spawn a new task to connect
     * to data service and receive the data.
     *
     * Really not sure how we are going to test this.
     */
    public void start() {

        var token = jobContext.getResponse().getToken();
        var eventBus = jobContext.getEventBus();
        var numThreads = jobContext.getSystemConfig().getClientConfig().getDownloadThreads();
        var objectMapper = jobContext.getSystemConfig().getObjectMapper();

        /*
         * The DownloadManager orchestrates the download of 1 or more downloads from the data
         * service.
         * Raised events:
         * * DownloadStartedEvent - published when a thread is starting a download
         * * DownloadCompletedEvent - published when a download has completed successfully
         * * DownloadFailedEvent - published when a download has failed for some reason
         * * DownloadQueueEvent - published when the state of the queue changes (full or not)
         * Consumed events:
         * * ResourceReadyEvent
         */

        var downloadManager = createDownloadManager(b -> b
                .id("dlm:" + token)
                .eventBus(eventBus)
                .numThreads(numThreads));

        eventBus.register(downloadManager);

        /*
         * This tracker just exoposes from the download manager what clients need to
         * manage downloads. The download manager should not be needed by any other
         * object
         */

        var downloadTracker = downloadManager.getDownloadTracker();

        /*
         * The resource client.
         * Raised events:
         * * ResourceReadyEvent - published when a resource message is received and ready to
         *   be downloaded
         * * ResourcesComplete - published when there are no more resources to consume
         * The resource client does not do anything yet. It will be wired up to the
         * WebSocketContainer when Job.start() is called
         */

        var resourceClient = createResourceClient(b -> b
                .eventBus(eventBus)
                .mapper(objectMapper)
                .downloadTracker(downloadTracker)
                .token(token));

        eventBus.register(resourceClient);

        /*
         * Now we wire up the resource (websocket) client to the WebSocketContainer
         * As soon as this is done, the connection will be made and the onXXX handlers
         * will start to get fired in the resource client. The first is onOpen.
         */

        try {

            var url = getContext().getResponse().getUrl();
            var container = ContainerProvider.getWebSocketContainer();

            container.connectToServer(resourceClient, new URI(url));

            log.debug("Job [{}] started", id);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Subscribe
    public void onDownloadCompleted(DownloadCompletedEvent event) {
        log.debug("Job [{}] caught event: {}", id, event);
    }

    @Subscribe
    public void onJobComplete(ResourcesExhaustedEvent event) {
        log.debug("Job [{}] caught event: {}", id, event);
    }

    public String getId() {
        return this.id;
    }

}
