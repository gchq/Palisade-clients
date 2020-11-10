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
package uk.gov.gchq.palisade.client.job;

import org.greenrobot.eventbus.Subscribe;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.Job;
import uk.gov.gchq.palisade.client.Result;
import uk.gov.gchq.palisade.client.download.DownloadCompletedEvent;
import uk.gov.gchq.palisade.client.download.DownloadFailedEvent;
import uk.gov.gchq.palisade.client.download.DownloadManager;
import uk.gov.gchq.palisade.client.download.DownloadStartedEvent;
import uk.gov.gchq.palisade.client.resource.ResourceClient;
import uk.gov.gchq.palisade.client.resource.ResourceReadyEvent;
import uk.gov.gchq.palisade.client.resource.ResourcesExhaustedEvent;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toMap;
import static uk.gov.gchq.palisade.client.job.IJobDownload.createDownload;
import static uk.gov.gchq.palisade.client.util.Checks.checkArgument;

/**
 * A job instance controls the downloading of resources
 *
 * @since 0.5.0
 */
public final class ClientJob implements Job {

    /**
     * The setup instance for a job
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IJobSetup extends Serializable {

        /**
         * Returns the job context for this job
         *
         * @return the job context
         */
        JobContext getContext();

        /**
         * Returns a reference to the download manager
         *
         * @return a reference to the download manager
         */
        DownloadManager getDownloadManager();

        /**
         * Returns a list of previous downloads that may have been run in a previous
         * execution
         *
         * @return a list of previous downloads
         */
        List<JobDownload> getDownloads();

        /**
         * Returns a list of previous executions
         *
         * @return a list of previous executions
         */
        List<JobExecution> getExecutions();

        /**
         * Returns the resource client instance that provides communication to the
         * Filtered Resource Service.
         *
         * @return the resource client
         */
        ResourceClient getResourceClient();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientJob.class);
    private static final String EVENT_CAUGHT = "Caught event: {}";

    private final JobSetup setup;
    private final Map<UUID, JobDownload> downloads;
    private final Map<UUID, JobExecution> executions;
    private final JobExecution currentExecution;

    private ClientJob(final JobSetup setup) {

        this.setup = checkArgument(setup);
        this.downloads = setup.getDownloads().stream().collect(toMap(JobDownload::getId, dl -> dl));
        this.executions = setup.getExecutions().stream().collect(toMap(JobExecution::getId, ex -> ex));

        this.currentExecution = IJobExecution.createJobExecution(e -> e
            .sequence(executions.size() + 1));

        this.executions.put(currentExecution.getId(), currentExecution);
    }

    /**
     * Helper method to create a {@code ClientJob} using a builder function
     *
     * @param func The builder function
     * @return a newly created ClientJob instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public static ClientJob createJob(final UnaryOperator<JobSetup.Builder> func) {
        return createJob(func.apply(JobSetup.builder()).build());
    }

    /**
     * Helper method to create a {@code ClientJob}
     *
     * @param setup The job setup
     * @return a newly created ClientJob
     */
    public static ClientJob createJob(final JobSetup setup) {
        return new ClientJob(setup);
    }

    /**
     * Returns this jobs context
     *
     * @return this jobs context
     */
    public JobContext getContext() {
        return this.setup.getContext();
    }

    /**
     * Returns the token
     *
     * @return the token
     */
    public String getToken() {
        return setup.getContext().getPalisadeResponse().getToken();
    }

    /**
     * Handles a download failed event
     *
     * @param event The event to be handled
     */
    @Subscribe
    public void onDownloadCompleted(final DownloadCompletedEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
        var prev = downloads.get(event.getId());
        var next = prev.change(b -> b
            .endTime(event.getTime())
            .status(JobDownloadStatus.FAILED));
        downloads.put(next.getId(), next);
    }

    /**
     * Handles a download failed event
     *
     * @param event The event to be handled
     */
    @Subscribe
    public void onDownloadFailed(final DownloadFailedEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event, event.getCause());
        var prev = downloads.get(event.getId());
        var next = prev.change(b -> b
            .endTime(event.getTime())
            .status(JobDownloadStatus.FAILED)
            .statusCode(event.getStatusCode()));
        downloads.put(next.getId(), next);
    }

    /**
     * Handles a download started
     *
     * @param event The event to be handled
     */
    @Subscribe
    public void onDownloadStarted(final DownloadStartedEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
        var dl = createDownload(b -> b
            .id(event.getId())
            .resource(event.getResource())
            .execution(currentExecution));
        downloads.put(dl.getId(), dl);
    }

    /**
     * Receives a {@code ResourcesExhaustedEvent} there are no more resources
     *
     * @param event The event to be handled
     */
    @SuppressWarnings("java:S2325") // make static
    @Subscribe
    public void onJobComplete(final ResourcesExhaustedEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
    }

    /**
     * Handles the {@code ResourceReadyEvent} by scheduling a download. This method
     * will return immediately as the download will be queued.
     *
     * @param event The event to be handled
     */
    @SuppressWarnings("java:S3242")
    @Subscribe
    public void onScheduleDownload(final ResourceReadyEvent event) {
        var resource = event.getResource();
        var jobContext = setup.getContext();
        setup.getDownloadManager().schedule(resource, jobContext);
    }

    @Override
    public Result start() {

        // we must register the event subscriptions in this job with the evnt bus
        // provided to us

        setup.getContext().getEventBus().register(this);

        // now we connect. This will connect to the server and start to receive
        // resources

        setup.getResourceClient().connect();

        LOGGER.debug("Job created for token: {}", getToken());

        // return an expty result for now

        return new Result() {
            // empty for the moment
        };

    }

}
