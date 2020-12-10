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
package uk.gov.gchq.palisade.client.job.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.job.JobDownloadStatus;
import uk.gov.gchq.palisade.client.job.state.ISavedJobState.IStateDownload;
import uk.gov.gchq.palisade.client.job.state.ISavedJobState.IStateExecution;
import uk.gov.gchq.palisade.client.job.state.ISavedJobState.IStateJobError;
import uk.gov.gchq.palisade.client.job.state.ISavedJobState.IStateJobRequest;
import uk.gov.gchq.palisade.client.job.state.ISavedJobState.IStatePalisadeResponse;
import uk.gov.gchq.palisade.client.request.IPalisadeResponse;
import uk.gov.gchq.palisade.client.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.util.Configuration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static uk.gov.gchq.palisade.client.job.state.IJobDownload.createDownload;
import static uk.gov.gchq.palisade.client.job.state.IJobExecution.createJobExecution;
import static uk.gov.gchq.palisade.client.request.IPalisadeResponse.createPalisadeResponse;
import static uk.gov.gchq.palisade.client.util.Checks.checkArgument;

/**
 * The state of a job
 *
 * @since 0.5.0
 */
public class JobState {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobState.class);
    private final JobStateService service;

    // this is the first ever run of this job. the sequence will be >0 if the job is
    // ever resumed
    private final int sequence;

    /*
     * This is the date/time when this job was first created
     */
    private final Instant created;
    private final IJobRequest jobRequest;
    private final Configuration configuration;
    private final Map<UUID, IJobDownload> downloads = new HashMap<>();
    private final Map<UUID, IJobExecution> executions = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    private IPalisadeResponse palisadeResponse;
    private IJobExecution currentExecution;
    private JobStatus status;

    /**
     * Returns a newly create {@code JobState} instance initialised at the beginning
     * of its pipeline
     *
     * @param service       The service to use when saving state.
     * @param jobRequest    The request for this job
     * @param configuration The client (global) properties
     */
    JobState(final JobStateService service, final IJobRequest jobRequest, final Configuration configuration) {

        this.service = service;
        this.jobRequest = jobRequest;
        this.configuration = configuration;

        this.sequence = 0;
        this.status = JobStatus.OPEN;
        this.created = Instant.now();

        // setup the first execution

        this.currentExecution = IJobExecution.createJobExecution();
        this.executions.put(this.currentExecution.getId(), this.currentExecution);

    }

    /**
     * Returns a newly create {@code JobState} instance initialised with the
     * provided state
     *
     * @param service       The service to use when saving state.
     * @param state         the state for this job
     * @param configuration The job configuration
     */
    JobState(final JobStateService service, final ISavedJobState state, final Map<String, Object> configuration) {

        this.service = service;

        // configure this state from a saved state

        this.sequence = state.getSequence();
        this.status = state.getStatus();
        this.created = state.getCreated();
        this.configuration = Configuration.from(state.getProperties()).merge(configuration);

        this.jobRequest = IJobRequest.createJobRequest((final JobRequest.Builder b) -> {
            var req = state.getRequest();
            return b
                .properties(req.getProperties())
                .purpose(req.getPurpose())
                .resourceId(req.getResourceId())
                .userId(req.getUserId());
        });

        // only do the following if the state is > 10 (IN_PROGRESS or later)

        if (this.status.getSequence() > JobStatus.REQUEST_SENT.getSequence()) {

            this.palisadeResponse = createPalisadeResponse(b -> b.token(state.getPalisadeResponse().getToken()));

            state.getExecutions()
                .stream()
                .map(ex -> createJobExecution(b -> b
                    .endTime(ex.getEnd())
                    .startTime(ex.getStart())
                    .id(UUID.fromString(ex.getId()))
                    .errors(ex.getErrors().stream()
                        .map(err -> IJobError.createJobError(c -> c.text(err.getText())))
                        .collect(Collectors.toList()))))
                .forEach(ex -> executions.put(ex.getId(), ex));

            state.getDownloads()
                .stream()
                .map(dl -> createDownload(b -> b
                    .id(UUID.fromString(dl.getId()))
                    .execution(executions.get(UUID.fromString(dl.getExecutionId())))
                    .duration(dl.getDuration())
                    .endTime(dl.getEnd())
                    .resourceId(dl.getResourceId())
                    .scheduledTime(dl.getScheduledTime())
                    .url(dl.getUrl())
                    .startTime(dl.getStart())
                    .status(dl.getStatus())
                    .statusCode(dl.getStatusCode())))
                .forEach(dl -> downloads.put(dl.getId(), dl));

        }

        // setup the next execution

        this.currentExecution = IJobExecution.createJobExecution();
        this.executions.put(this.currentExecution.getId(), this.currentExecution);

    }

    /**
     * Returns the job config
     *
     * @return the job config
     */
    public IJobRequest getJobConfig() {
        return this.jobRequest;
    }

    /**
     * Returns the configuration for the job state
     *
     * @return the configuration for the job state
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the palisade response
     *
     * @return the palisade response
     */
    public Optional<IPalisadeResponse> getPalisadeResponse() {
        return Optional.ofNullable(this.palisadeResponse);
    }

    /**
     * Indicates to this state that a request has been sent
     *
     * @param jobRequest The job request containing the details sent to the palisade
     *                   service
     */
    public void requestSent(final IJobRequest jobRequest) {
        lock.lock();
        try {
            this.status = JobStatus.REQUEST_SENT;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates to this state that the provided response has been received
     *
     * @param palisadeResponse The received response
     */
    public void responseReceived(final PalisadeResponse palisadeResponse) {
        lock.lock();
        try {
            this.palisadeResponse = palisadeResponse;
            this.status = JobStatus.RESPONSE_RECEIVED;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates to this state that downloads have commenced, but none have been
     * downloaded yet. A new execution is created and added to the state.
     */
    public void start() {
        lock.lock();
        try {
            this.status = JobStatus.DOWNLOADS_IN_PROGRESS;
            currentExecution = IJobExecution.createJobExecution(b -> b);
            executions.put(currentExecution.getId(), currentExecution);
            save();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates to this state that downloads have completed. The current execution
     * will be updated to reflect that it is complete and it's end time updated.
     */
    public void finish() {
        lock.lock();
        try {
            status = JobStatus.COMPLETE;
            currentExecution = this.currentExecution.change(b -> b.endTime(Instant.now()));
            executions.put(currentExecution.getId(), currentExecution);
            save();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates to this state that an error has been received from the Filtered
     * Resource Service. The error will be added to the current execution.
     *
     * @param text The error text
     */
    public void error(final String text) {
        lock.lock();
        try {
            var error = IJobError.createJobError(e -> e.text(text));
            currentExecution = this.currentExecution.change(b -> b.addError(error));
            executions.put(currentExecution.getId(), currentExecution);
            save();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates that a download has been scheduled. The state will be updated with
     * the download and associated with the current execution.
     *
     * @param id         The download id
     * @param resourceId The resource id
     * @param url        The URL to be downloaded from
     */
    public void downloadScheduled(final UUID id, final String resourceId, final String url) {
        checkArgument(id);
        checkArgument(resourceId);
        checkArgument(url);
        lock.lock();
        try {
            var dl = createDownload(b -> b
                .id(id)
                .resourceId(resourceId)
                .url(url)
                .execution(currentExecution));
            downloads.put(dl.getId(), dl);
            save();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates that a download has been started. The state will be updated to
     * reflect the download's status and start time.
     *
     * @param id    The download id
     * @param start The start time
     */
    public void downloadStarted(final UUID id, final Instant start) {
        checkArgument(id);
        checkArgument(start);
        lock.lock();
        try {
            var prev = downloads.get(id);
            var next = prev.change(b -> b
                .startTime(start)
                .status(JobDownloadStatus.IN_PROGRESS));
            downloads.put(next.getId(), next);
            save();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates that a download has been completed. This state will updated to
     * reflect the new state of the download.
     *
     * @param id         The download id
     * @param end        The end time
     * @param properties Any properties returned from the downloader
     */
    public void downloadCompleted(final UUID id, final Instant end, final Map<String, String> properties) {
        checkArgument(id);
        checkArgument(end);
        lock.lock();
        try {
            var prev = downloads.get(id);
            LOGGER.debug("Previous download state: {}", prev);
            var next = prev.change(b -> b
                .endTime(end)
                .duration(Duration.between(prev.getStartTime().get(), end).toMillis())
                .status(JobDownloadStatus.COMPLETE)
                .putAllProperties(properties));
            downloads.put(next.getId(), next);
            save();
        } finally {
            lock.unlock();
        }
    }

    Instant getCreated() {
        return this.created;
    }

    IJobExecution getCurrentExecution() {
        return this.currentExecution;
    }

    Map<UUID, IJobExecution> getExecutions() {
        return this.executions;
    }

    Map<UUID, IJobDownload> getDownloads() {
        return this.downloads;
    }

    int getSequence() {
        return this.sequence;
    }

    JobStatus getStatus() {
        return this.status;
    }

    /**
     * A download has failed
     *
     * @param id         The download id
     * @param end        The end time
     * @param statusCode The HTTP status code
     * @param cause      The cause of the failure
     */
    public void downloadFailed(final UUID id, final Instant end, final int statusCode, final Exception cause) {
        checkArgument(id);
        checkArgument(end);
        checkArgument(cause);
        lock.lock();
        try {
            var prev = downloads.get(id);
            var next = prev.change(b -> b
                .endTime(end)
                .status(JobDownloadStatus.FAILED)
                .statusCode(statusCode)
                .cause(cause));
            downloads.put(next.getId(), next);
            save();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a saved state instance from this state
     *
     * @return a saved state instance from this state
     */
    public ISavedJobState createSavedState() {
        return ISavedJobState.create(b -> b
            .sequence(sequence)
            .status(status)
            .created(created)
            .properties(configuration.getProperties())
            .request(map(jobRequest))
            .palisadeResponse(map(palisadeResponse))
            .executions(executions.values().stream().map(JobState::map).collect(Collectors.toList()))
            .downloads(downloads.values().stream().map(JobState::map).collect(Collectors.toList())));
    }

    public long getIncompleteDownloadCount() {
        return downloads.values().stream()
            .filter(IJobDownload::hasNotEnded)
            .count();
    }

    private void save() {
        service.save(createSavedState(), configuration.getStatePath());
    }

    private static IStateJobRequest map(final IJobRequest jc) {
        return IStateJobRequest.createStateJobConfig(b -> b
            .userId(jc.getUserId())
            .purpose(jc.getPurpose())
            .resourceId(jc.getResourceId())
            .receiverClass(jc.getReceiverClass().getName())
            .properties(jc.getProperties()));
    }

    private static IStatePalisadeResponse map(final IPalisadeResponse jc) {
        return IStatePalisadeResponse.createStatePalisadeResponse(b -> b
            .token(jc.getToken()));
    }

    private static IStateExecution map(final IJobExecution ex) {
        return IStateExecution.createStateExecution(b -> b
            .id(ex.getId().toString())
            .start(ex.getStartTime())
            .end(ex.getEndTime())
            .errors(ex.getErrors().stream().map(JobState::map).collect(Collectors.toList())));
    }

    private static IStateJobError map(final IJobError e) {
        return IStateJobError.createStateJobError(b -> b
            .text(e.getText()));
    }

    private static IStateDownload map(final IJobDownload dl) {
        return IStateDownload.createStateDownload(b -> b
            .id(dl.getId().toString())
            .status(dl.getStatus())
            .statusCode(dl.getStatusCode())
            .executionId(dl.getExecution().getId().toString())
            .resourceId(dl.getResourceId())
            .url(dl.getUrl())
            .scheduledTime(dl.getScheduledTime())
            .start(dl.getStartTime())
            .end(dl.getEndTime())
            .properties(dl.getProperties())
            .duration(dl.getDuration())
            .errorMessage(dl.getCause().map(Exception::getMessage))
            .stackTrace(dl.getCause().map(JobState::toString)));
    }

    private static String toString(final Exception e) {
        var sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
