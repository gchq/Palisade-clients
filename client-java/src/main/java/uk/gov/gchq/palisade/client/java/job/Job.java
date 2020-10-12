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

import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.download.*;
import uk.gov.gchq.palisade.client.java.resource.*;

import javax.websocket.ContainerProvider;

import java.net.URI;

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
 * @since 0.5.0
 */
public class Job {

    private static final Logger log = LoggerFactory.getLogger(Job.class);
    private static final String EVENT_CAUGHT = "Job [{}] caught event: {}";

    private final String id;
    private final JobContext jobContext;

    private DownloadManager downloadManager;

    /**
     * Returns a new Job configured with the provided job context
     *
     * @param id         The unique id (name) of this job
     * @param jobContext The job context containing relevant information about the
     *                   current job. For example, this would be the request and
     *                   response etc.
     */
    public Job(String id, JobContext jobContext) {
        this.id = id;
        this.jobContext = jobContext;
    }

    /**
     * <p>
     * This method will start the job. The following steps are performed:
     * <ul>
     * <li>Creates a new Download manager</li>
     * <li>Creates a new ResourceClient (web socket client)</li>
     * <li>Registers the download manager and the resource client with the provided
     * eventbus</li>
     * <li>Starts a new web socket container with the websocket client and the
     * provided url</li>
     * </ul>
     * </p>
     *
     * @return A {@code Publisher} instance that emits Downloads for consumption.
     *         Each emitted download instance will contain an InputStream
     */
    public void start() {

        var clientContext = jobContext.getClientContext();
        var token = jobContext.getResponse().getToken();
        var eventBus = jobContext.getEventBus();

        this.downloadManager = createDownloadManager(b -> b
            .clientContext(clientContext)
            .receiverSupplier(jobContext.getJobConfig().getReceiverSupplier())
            .id("dlm:" + token)
            .eventBus(eventBus));

        var resourceClient = createResourceClient(b -> b
            .eventBus(eventBus)
            .mapper(clientContext.get(ObjectMapper.class)).downloadTracker(downloadManager.getDownloadTracker())
            .token(token));

        eventBus.register(downloadManager);
        eventBus.register(resourceClient);

        try {
            var url = getContext().getResponse().getUrl();
            var container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(resourceClient, new URI(url)); // this start communication
            log.debug("Job [{}] started", id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Handles the {@code ResourceReadyEvent} by scheduling a download. This method
     * will return immediately as the download will be queued.
     *
     * @param event The event to be handled
     */
    @Subscribe
    public void schedule(ResourceReadyEvent event) {
        var resource = event.getResource();
        this.downloadManager.schedule(resource);
    }

//    /**
//     * Handles the {@code DownloadReadyEvent} event. This event signals that the
//     * Data Service has successfully responded.
//     *
//     * @param event
//     */
//    @Subscribe
//    public void onDownloadReady(DownloadReadyEvent event) {
//        log.debug(EVENT_CAUGHT, id, event.getToken());
//        var ds = getTempDS();
//        var is = event.getInputStream();
//        var object = ds.deserialize(is);
//
//        // now we need to do something with it.
//        // just log it out form now
//        log.debug("Downloaded: {}", object);
//    }

    /**
     * Handles the {@code ResourceReadyEvent} by scheduling a download. At the
     * moment we do not do anything, apart from log the error. Not sure what to do
     * with the errors though.
     *
     * @param event The event to be handled
     */
    @Subscribe
    public void onDownloadFailed(DownloadFailedEvent event) {
        log.debug(EVENT_CAUGHT, id, event);
        log.debug("Download failed", event.getThrowble());
    }

    /**
     * Handles the {@code ResourcesExhaustedEvent} event.
     *
     * @param event to be handled
     */
    @Subscribe
    public void onJobComplete(ResourcesExhaustedEvent event) {
        log.debug(EVENT_CAUGHT, id, event);
    }

    /**
     * Returns the id of this job
     *
     * @return the id of this job
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the Download tracker
     *
     * @return the Download tracker
     */
    public DownloadTracker getDownLoadTracker() {
        return this.downloadManager.getDownloadTracker();
    }

    /**
     * Returns this job's context
     *
     * @return this job's context
     */
    JobContext getContext() {
        return this.jobContext;
    }

//    private Deserializer<String> getTempDS() {
//
//        return stream -> {
//            var bufferSize = 1024;
//            var buffer = new char[bufferSize];
//            var out = new StringBuilder();
//            var in = new InputStreamReader(stream, StandardCharsets.UTF_8);
//            int charsRead;
//            try {
//                while ((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
//                    out.append(buffer, 0, charsRead);
//                }
//            } catch (IOException ioe) {
//                throw new DeserialiserException("Failed to read stream", ioe);
//            }
//            return out.toString();
//        };
//
//    }

}
