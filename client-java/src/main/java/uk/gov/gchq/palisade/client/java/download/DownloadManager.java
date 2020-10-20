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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.ClientConfig;
import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.receiver.Receiver;
import uk.gov.gchq.palisade.client.java.resource.Resource;

import javax.inject.Singleton;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class manages multiple downloads from the Palisade Data Service.
 *
 * @since 0.5.0
 */
@Singleton
public class DownloadManager implements DownloadTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadManager.class);

    private final ThreadPoolExecutor executor;
    private final ClientContext clientContext;

    /**
     * Create a new DownloadManager with the the provided {@code ClientContext}
     *
     * @param clientContext The client context providing access to underlying
     *                      objects
     */
    public DownloadManager(final ClientContext clientContext) {
        this.clientContext = Objects.requireNonNull(clientContext);
        int numThreads = clientContext.get(ClientConfig.class).getDownload().getThreads();
        this.executor = new ThreadPoolExecutor(1, numThreads, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2));
        LOGGER.debug("### Download manager created with thread pool size of {}", numThreads);
    }

    /**
     * Schedule the provided resource for download
     *
     * @param resource The resource describing the data to be downloaded.
     * @param receiver The receiver to process the data stream
     */
    public void schedule(final Resource resource, final Receiver receiver) {
        LOGGER.debug("### Scheduling resource: {}", resource);
        Downloader downloader = new Downloader(clientContext, resource, receiver);
        executor.execute(downloader);
    }

    /**
     * Returns the download tracker for this download manager
     *
     * @return the download tracker for this download manager
     */
    public DownloadTracker getDownloadTracker() {
        return this;
    }

    @Override
    public int getAvaliableSlots() {
        return executor.getMaximumPoolSize() - executor.getActiveCount();
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
        } else {
            return ManagerStatus.ACTIVE;
        }
    }

}
