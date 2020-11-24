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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.Job;
import uk.gov.gchq.palisade.client.download.DownloadCompletedEvent;
import uk.gov.gchq.palisade.client.download.DownloadFailedEvent;
import uk.gov.gchq.palisade.client.download.DownloadManager;
import uk.gov.gchq.palisade.client.download.DownloadScheduledEvent;
import uk.gov.gchq.palisade.client.download.DownloadStartedEvent;
import uk.gov.gchq.palisade.client.job.state.IJobRequest;
import uk.gov.gchq.palisade.client.job.state.ISavedJobState;
import uk.gov.gchq.palisade.client.job.state.JobState;
import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.request.PalisadeClient;
import uk.gov.gchq.palisade.client.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.resource.ErrorEvent;
import uk.gov.gchq.palisade.client.resource.ResourceClient;
import uk.gov.gchq.palisade.client.resource.ResourceReadyEvent;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.request.IPalisadeRequest.createPalisadeRequest;
import static uk.gov.gchq.palisade.client.resource.ResourceClientListener.createResourceClientListenr;
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
        JobState getState();

        /**
         * Returns the palisade client
         *
         * @return the palisade client
         */
        PalisadeClient getPalisadeClient();

        /**
         * Returns a reference to the download manager
         *
         * @return a reference to the download manager
         */
        DownloadManager getDownloadManager();

        /**
         * Returns the object mapper
         *
         * @return the object mapper
         */
        ObjectMapper getObjectMapper();

        /**
         * Returns the event bus specific to this context. Other jobs will have their
         * own eventbus.
         *
         * @return the event bus specific to this context
         */
        EventBus getEventBus();

        /**
         * Returns the receiver that will handle the downloads
         *
         * @return the receiver that will handle the downloads
         */
        Receiver getReceiver();

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientJob.class);
    private static final String EVENT_CAUGHT = "---> Caught event: {}";

    private final IJobSetup setup;
    private final JobState state;

    @SuppressWarnings("java:S1450")

    private ClientJob(final JobSetup setup) {
        this.setup = checkArgument(setup);
        this.state = setup.getState();
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
     * Handles a download failed event
     *
     * @param event The event to be handled
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    @SuppressWarnings("java:S3242")
    public void onDownloadCompleted(final DownloadCompletedEvent event) {
        state.downloadCompleted(event.getId(), event.getTime(), event.getProperties());
    }

    /**
     * Handles a download failed event
     *
     * @param event The event to be handled
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    @SuppressWarnings("java:S3242")
    public void onDownloadFailed(final DownloadFailedEvent event) {
        state.downloadFailed(event.getId(), event.getTime(), event.getStatusCode(), event.getCause());
    }

    /**
     * Handles a download started
     *
     * @param event The event to be handled
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    @SuppressWarnings("java:S3242")
    public void onDownloadStarted(final DownloadStartedEvent event) {
        state.downloadStarted(event.getId(), event.getTime());
    }

    /**
     * Handles a download scheduled event
     *
     * @param event The event to be handled
     */
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    @SuppressWarnings("java:S3242")
    public void onDownloadScheduled(final DownloadScheduledEvent event) {
        var resource = event.getResource();
        state.downloadScheduled(event.getId(), resource.getLeafResourceId(), resource.getUrl());
    }


    /**
     * Handles the {@code ResourceReadyEvent} by scheduling a download. This method
     * will return immediately as the download will be queued.
     *
     * @param event The event to be handled
     */
    @SuppressWarnings("java:S3242")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onScheduleDownload(final ResourceReadyEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
        setup.getDownloadManager().schedule(
            event.getResource(),
            setup.getEventBus(),
            setup.getReceiver(),
            setup.getState().getConfiguration());
    }

    /**
     * Handles the {@code ResourceReadyEvent} by scheduling a download. This method
     * will return immediately as the download will be queued.
     *
     * @param event The event to be handled
     */
    @SuppressWarnings("java:S2325")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onError(final ErrorEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
        var error = event.getError();
        LOGGER.debug("Job received an error: {}", error);
    }

    /**
     * Start this job
     *
     * @return this for chaining
     */
    public Result start() {

        // create request and call the palisade service

        IJobRequest jobConfig = state.getJobConfig();

        var future = setup.getPalisadeClient().submitAsync(createRequest(jobConfig));
        state.requestSent(jobConfig);
        var palisadeResponse = future.join();
        state.responseReceived(palisadeResponse);

        // create the resource client that will handle the websocket communication

        var token = palisadeResponse.getToken();
        var eventBus = setup.getEventBus();
        var downloadManager = setup.getDownloadManager();
        var objectMapper = setup.getObjectMapper();

        var wsUri = createUri(setup.getState().getConfiguration().getFilteredResourceUri());

        // we must register the event subscriptions in this job with the evnt bus
        // provided to us

        eventBus.register(this);

        CompletableFuture<ISavedJobState> future1 = ResourceClient.createResourceClient(rc -> rc
            .baseUri(wsUri)
            .resourceClientListener(createResourceClientListenr(rcl -> rcl
                .token(token)
                .downloadManagerStatus(downloadManager)
                .eventBus(eventBus)
                .objectMapper(objectMapper))))
            .connect()
            .thenApply(v -> state.createSavedState());

        state.downloadsStarted();

        LOGGER.debug("Job created for token: {}", token);

        // return an expty result for now

        return new Result() {
            @Override
            public CompletableFuture<ISavedJobState> future() {
                return future1;
            }
        };

    }

    IJobSetup getSetup() {
        return this.setup;
    }

    JobState getState() {
        return this.state;
    }

    private static PalisadeRequest createRequest(final IJobRequest jobConfig) {

        var userId = jobConfig.getUserId();
        var purposeOpt = jobConfig.getPurpose();
        var resourceId = jobConfig.getResourceId();
        var properties = new HashMap<>(jobConfig.getProperties());
        properties.put("PURPOSE", purposeOpt.orElse("client_request"));

        var palisadeRequest = createPalisadeRequest(b -> b
            .resourceId(resourceId)
            .userId(userId)
            .context(properties));

        LOGGER.debug("new palisade request created from job config: {}", palisadeRequest);

        return palisadeRequest;

    }

    private URI createUri(final String uriString) {
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new ClientException("URI \"" + uriString + "\" is invalid", e);
        }
    }


}

