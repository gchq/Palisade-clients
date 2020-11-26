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
import org.greenrobot.eventbus.EventBus;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.resource.Resource;
import uk.gov.gchq.palisade.client.util.Configuration;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.download.Downloader.createDownloader;
import static uk.gov.gchq.palisade.client.util.Checks.checkArgument;

/**
 * This class manages multiple downloads from the Palisade Data Service.
 *
 * @since 0.5.0
 */
public final class DownloadManager implements DownloadManagerStatus {

    /**
     * Setup for a {@code DownloadManager}
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IDownloadManagerSetup {

        /**
         * Returns the number of threads that the configured downloader will use
         *
         * @return the number of threads that the configured downloader will use
         */
        int getNumThreads();

        /**
         * Returns the object mapper to be passed on to downloader threads
         *
         * @return the object mapper to be passed on to downloader threads
         */
        ObjectMapper getObjectMapper();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadManager.class);

    private static final int CORE_POOL_SIZE = 0; // ensure that unreferenced pools are reclaimed
    private static final int KEEP_ALIVE_SECONDS = 10;

    private final ThreadPoolExecutor executor;
    private final LinkedBlockingQueue<Runnable> buffer;
    private final DownloadManagerSetup setup;

    /**
     * Create a new DownloadManager with the the provided
     * {@code DownloadManagerSetup}
     *
     * @param downloadManagerSetup The setup
     */
    private DownloadManager(final DownloadManagerSetup downloadManagerSetup) {
        this.setup = checkArgument(downloadManagerSetup);
        this.buffer = new LinkedBlockingQueue<>();
        this.executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            setup.getNumThreads(),
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            this.buffer);
        executor.allowCoreThreadTimeOut(true);
        LOGGER.debug("### Download manager created with thread pool size of {}", setup.getNumThreads());
    }

    /**
     * Helper method to create a {@code DownloadManager} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public static DownloadManager createDownloadManager(final UnaryOperator<DownloadManagerSetup.Builder> func) {
        return new DownloadManager(func.apply(DownloadManagerSetup.builder()).build());
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request, or
     * the timeout occurs, or the current thread is interrupted, whichever happens
     * first.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return true if this download manager terminated and false if the timeout
     *         elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        LOGGER.debug("Awaiting for download manager to terminate for {} {} ", timeout, unit);
        var terminated = executor.awaitTermination(timeout, unit);
        return terminated;
    }

    @Override
    public int getAvaliableSlots() {
        return buffer.remainingCapacity();
    }

    /**
     * Returns the download tracker for this download manager
     *
     * @return the download tracker for this download manager
     */
    public DownloadManagerStatus getDownloadTracker() {
        return this;
    }

    @Override
    public boolean hasAvailableSlots() {
        return getAvaliableSlots() > 0;
    }

    /**
     * Schedule the provided resource for download
     *
     * @param resource      The resource describing the data to be downloaded.
     * @param eventbus      The event bus to be use when posting download events
     * @param receiver      The receiver instance which will be passed the
     *                      dowbnloaded input stream for processing
     * @param configuration The job configuration
     * @return the download id
     */
    @SuppressWarnings("java:S2221")
    public UUID schedule(
            final Resource resource,
            final EventBus eventbus,
            final Receiver receiver,
            final Configuration configuration) {

        LOGGER.debug("### Scheduling resource: {}", resource);

        var downloader = createDownloader(b -> b
            .resource(resource)
            .receiver(receiver)
            .objectMapper(setup.getObjectMapper())
            .configuration(configuration));

        var downloadId = downloader.getDownloadId();

        eventbus.post(DownloadScheduledEvent.of(downloadId, resource, Map.of()));

        // The downloader is wrapped here so as to catch ALL exceptions that may be
        // thrown. They are handled within the runnable as no exceptions may be thrown
        // outside the executor.

        Runnable runner = () -> {
            eventbus.post(DownloadStartedEvent.of(downloadId, resource, Map.of()));
            try {
                var result = downloader.start();
                eventbus.post(DownloadCompletedEvent.of(downloadId, resource, result.getProperties(), result));
            } catch (DownloaderException e) {
                eventbus.post(DownloadFailedEvent.of(downloadId, resource, Map.of(), e, e.getStatusCode()));
            } catch (Exception e) {
                LOGGER.error("Unknown exception caught. Please check code as this should never happen", e);
                eventbus.post(DownloadFailedEvent.of(downloadId, resource, Map.of(), e, -1));
            }
        };

        executor.execute(runner);
        return downloadId;

    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted. Invocation has no additional
     * effect if already shut down.
     * <p>
     * This method does not wait for previously submitted tasks to complete
     * execution. Use awaitTermination to do that.
     */
    public void shutdown() {
        LOGGER.debug("Download manager requsted to shutdown");
        executor.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of
     * waiting tasks.
     * <p>
     * This method does not wait for actively executing tasks to terminate. Use
     * awaitTermination to do that.
     * <p>
     * There are no guarantees beyond best-effort attempts to stop processing
     * actively executing downloads. This implementation cancels tasks via
     * Thread.interrupt(), so any download that fails to respond to interrupts may
     * never terminate.
     */
    public void shutdownNow() {
        LOGGER.debug("Download manager told to shutdown now");
        executor.shutdownNow();
    }

}
