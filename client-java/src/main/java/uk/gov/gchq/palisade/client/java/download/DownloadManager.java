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

import uk.gov.gchq.palisade.client.java.*;
import uk.gov.gchq.palisade.client.java.receiver.Receiver;
import uk.gov.gchq.palisade.client.java.resource.Resource;

import javax.inject.Singleton;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * <p>
 * This class manages multiple downloads from the Palisade Data Service.
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 */
@Singleton
public class DownloadManager {

    private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);

    private final ThreadPoolExecutor executor;
    private final ClientContext clientContext;


    private final DownloadTracker downloadTracker = new DownloadTracker() {
        @Override
        public int getAvaliableSlots() {
            return executor.getMaximumPoolSize() - executor.getActiveCount();
        }
        @Override
        public boolean hasAvailableSlots() {
            log.debug("free slots: {}", getAvaliableSlots());
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

    /**
     * @param clientContext
     */
    public DownloadManager(ClientContext clientContext) {
        assert clientContext != null : "Must provide the client context";
        this.clientContext = clientContext;
        var numThreads = clientContext.get(ClientConfig.class).getDownload().getThreads();
        this.executor = new ThreadPoolExecutor(1, numThreads, 2000L, MILLISECONDS, new LinkedBlockingQueue<>(2));
        log.debug("### Download manager created with thread pool size of {}", numThreads);
    }

    /**
     * Schedule the provided resource for download
     *
     * @param resource The resource describing the data to be downloaded.
     * @param receiver The receiver to process the data stream
     */
    public void schedule(Resource resource, Receiver receiver) {
        log.debug("### Scheduling resource: {}", resource);
        var downloader = new Downloader(clientContext, resource, receiver);
        executor.execute(downloader);
    }

    /**
     * Returns the download tracker for this download manager
     *
     * @return the download tracker for this download manager
     */
    public DownloadTracker getDownloadTracker() {
        return this.downloadTracker;
    }


}