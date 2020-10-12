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

import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.ClientConfig;
import uk.gov.gchq.palisade.client.java.resource.*;

import java.util.concurrent.*;
import java.util.function.UnaryOperator;

import com.google.common.eventbus.Subscribe;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * <p>
 * This class manages multiple downloads from the Palisade Data Service.
 * </p>
 * <p>
 * Events consumed by instances of this class:
 * </p>
 * <p>
 * <strong>None</strong>
 * <p>
 * Events consumed by instances of this class:
 * <ul>
 * <li><strong>DownloadReadyEvent</strong> - When a new download's inputstream
 * is ready to be consumed</li>
 * <li><strong>ResourcesExhaustedEvent</strong> - When there are no more
 * resources available to be downloaded</li>
 * </ul>
 * </p>
 * <p>
 * This class does not listen to any events
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 */
public class DownloadManager {

    /**
     * Creates and returns a new download manager using the provided configuration
     * builder function
     *
     * @param func The builder function providing the configuration
     * @return a new download manager using the provided configuration builder
     *         function
     */
    public static DownloadManager createDownloadManager(UnaryOperator<DownloadManagerConfig.Builder> func) {
        var config = func.apply(DownloadManagerConfig.builder()).build();
        return new DownloadManager(config);
    }

    private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);

    private final int numThreads;
    private final ThreadPoolExecutor executor;
    private final DownloadManagerConfig config;


    private final DownloadTracker downloadTracker = new DownloadTracker() {
        @Override
        public int getAvaliableSlots() {
            return numThreads - executor.getActiveCount();
        }
        @Override
        public boolean hasAvailableSlots() {
            return getAvaliableSlots() > 0;
        }
        @Override
        public ManagerStatus getStatus() {
            if (executor.isTerminating()) {
                return ManagerStatus.SHUTTING_DOWN;
            } else if (executor.isShutdown()) {
                return ManagerStatus.SHUT_DOWN;
            }
            return ManagerStatus.ACTIVE;
        }
    };

    private DownloadManager(DownloadManagerConfig config) {
        assert config != null : "Must provide a configuration";
        this.config = config;
        this.numThreads = config.getClientContext().get(ClientConfig.class).getDownload().getThreads();
        this.executor = new ThreadPoolExecutor(numThreads, numThreads, 2000L, MILLISECONDS, new LinkedBlockingQueue<>(2));
        log.debug("### Download manager created with thread pool size of {}", numThreads);
    }

    /**
     * Schedule the provided resource for download
     *
     * @param resource The resource describing the data to be downloaded.
     */
    public void schedule(Resource resource) {
        log.debug("### Scheduling resource: {}", resource);
        var downloader = Downloader.create(b -> b
            .clientContext(config.getClientContext())
            .resource(resource)
            .eventBus(config.getEventBus())
            .receiverSupplier(config.getReceiverSupplier()));
        executor.execute(downloader);
    }

    /**
     * Returns the id of this download manager
     *
     * @return the id of this download manager
     */
    public String getId() {
        return config.getId();
    }

    /**
     * Returns the download tracker for this download manager
     *
     * @return the download tracker for this download manager
     */
    public DownloadTracker getDownloadTracker() {
        return this.downloadTracker;
    }

    /**
     * Handle the job complete event by shutting down the executor. This method will
     * block for a small amount of time for the executors to complete before
     * shutting down, or if the timeout occurs, it is forced to quit. Once the
     * executor has terminated, an end of queue flag is added to the queue to
     * signify that the stream should complete once all other queued downloads have
     * been emitted.
     *
     * @param event The event to handle
     */
    @Subscribe
    public void handleJobComplete(ResourcesExhaustedEvent event) {
        // add a Download with no input stream to the queue. This will instruct the
        // stream to terminate
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // something musty be really stuck
        } finally {
            if (executor.isTerminating()) {
                // we've given it enough time, so halt it
                @SuppressWarnings("unused")
                var tasks = executor.shutdownNow();
                // TODO: we should do something with these tasks that were not executed.
            }
        }
    }

}