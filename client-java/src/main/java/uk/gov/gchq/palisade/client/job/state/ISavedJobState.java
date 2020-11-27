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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.job.JobDownloadStatus;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * An instance of {@code PalisadeRequest} is used to wrap all the information
 * that the user needs to supply to the palisade service to register the data
 * access request.
 * <p>
 * Note that the {@code PalisadeRequest} class is created at compile time. The
 * way in which the class is created is determined by the
 * {@code ImmutableStyle}. This class is also compatible with Jackson.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = SavedJobState.class)
@JsonSerialize(as = SavedJobState.class)
public interface ISavedJobState extends Serializable {

    /**
     * Helper method to create a {@code State} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static ISavedJobState create(final UnaryOperator<SavedJobState.Builder> func) {
        return func.apply(SavedJobState.builder()).build();
    }

    /**
     * Returns the sequence of this state. The sequence increments every time a job
     * is resumed.
     *
     * @return the sequence of this state
     */
    int getSequence();

    /**
     * Returns the job status
     *
     * @return the job status
     */
    JobStatus getStatus();

    /**
     * Returns the time this job was initially created. This will never change even
     * on subsequent resumes.
     *
     * @return the time this job was initially created
     */
    Instant getCreated();

    /**
     * Returns the job configuration
     *
     * @return the job configuration
     */
    IStateJobRequest getRequest();

    /**
     * Returns the palisade response
     *
     * @return the palisade response
     */
    IStatePalisadeResponse getPalisadeResponse();

    /**
     * Returns the properties for this job
     *
     * @return the properties for this job
     */
    Map<String, Object> getProperties();

    /**
     * Returns the executions. A new execution is added for each resume.
     *
     * @return the executions
     */
    List<IStateExecution> getExecutions();

    /**
     * Returns the downloads for this job
     *
     * @return the downloads for this job
     */
    List<IStateDownload> getDownloads();

    /**
     * The palisade response
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = StatePalisadeResponse.class)
    @JsonSerialize(as = StatePalisadeResponse.class)
    interface IStatePalisadeResponse {

        /**
         * Helper method to create a {@code StatePalisadeResponse} using a builder
         * function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        static StatePalisadeResponse createStatePalisadeResponse(
            final UnaryOperator<StatePalisadeResponse.Builder> func) {
            return func.apply(StatePalisadeResponse.builder()).build();
        }

        /**
         * Returns the unique token
         *
         * @return the unique token
         */
        String getToken();

    }

    /**
     * The palisade response
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = StateJobError.class)
    @JsonSerialize(as = StateJobError.class)
    public interface IStateJobError {

        /**
         * Helper method to create a {@code StatePalisadeResponse} using a builder
         * function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
        static IStateJobError createStateJobError(final UnaryOperator<StateJobError.Builder> func) {
            return func.apply(StateJobError.builder()).build();
        }

        /**
         * Returns the error text
         *
         * @return the error text
         */
        String getText();

    }

    /**
     * Job Configuration
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = StateJobRequest.class)
    @JsonSerialize(as = StateJobRequest.class)
    public interface IStateJobRequest {

        /**
         * Helper method to create a {@code StateJobConfig} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
        static IStateJobRequest createStateJobConfig(final UnaryOperator<StateJobRequest.Builder> func) {
            return func.apply(StateJobRequest.builder()).build();
        }

        /**
         * Returns the user id
         *
         * @return the user id
         */
        String getUserId();

        /**
         * Returns the resource id
         *
         * @return the resource id
         */
        String getResourceId();

        /**
         * Returns the purpose
         *
         * @return the purpose
         */
        Optional<String> getPurpose();

        /**
         * Returns the receiver class
         *
         * @return the receiver class
         */
        String getReceiverClass();

        /**
         * Returns the properties
         *
         * @return the properties
         */
        Map<String, String> getProperties();

    }

    /**
     * Job execution
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = StateExecution.class)
    @JsonSerialize(as = StateExecution.class)
    public interface IStateExecution {

        /**
         * Helper method to create a {@code StateExecution} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
        static StateExecution createStateExecution(final UnaryOperator<StateExecution.Builder> func) {
            return func.apply(StateExecution.builder()).build();
        }

        /**
         * Returns the id
         *
         * @return the id
         */
        String getId();

        /**
         * Returns the start time
         *
         * @return the start time
         */
        Instant getStart();

        /**
         * Returns the end time
         *
         * @return the end time
         */
        Optional<Instant> getEnd();

        /**
         * Returns the list of errors for this execution
         *
         * @return the list of errors for this execution
         */
        List<IStateJobError> getErrors();

    }

    /**
     * A job download
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = StateDownload.class)
    @JsonSerialize(as = StateDownload.class)
    public interface IStateDownload {

        /**
         * Helper method to create a {@code StateDownload} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
        static StateDownload createStateDownload(final UnaryOperator<StateDownload.Builder> func) {
            return func.apply(StateDownload.builder()).build();
        }

        /**
         * Returns the download status
         *
         * @return the download status
         */
        JobDownloadStatus getStatus();

        /**
         * Returns the unique id
         *
         * @return the unique id
         */
        String getId();

        /**
         * Returns the HTTP status code or -1 if there is none
         *
         * @return the HTTP status code
         */
        int getStatusCode();

        /**
         * Returns the execution id that this download is associated with
         *
         * @return the execution id
         */
        String getExecutionId();

        /**
         * Returns the resource id
         *
         * @return the resource id
         */
        String getResourceId();

        /**
         * Returns the download url
         *
         * @return the download url
         */
        String getUrl();

        /**
         * Returns the time this download was scheduled
         *
         * @return scheduled time
         */
        Instant getScheduledTime();

        /**
         * Returns the start time
         *
         * @return the start time
         */
        Optional<Instant> getStart();

        /**
         * Returns the end time or empty if not ended
         *
         * @return the end time
         */
        Optional<Instant> getEnd();

        /**
         * Returns the duration or empty if not ended
         *
         * @return the duration
         */
        Optional<Long> getDuration();

        /**
         * Returns the properties associated with this download
         *
         * @return the properties
         */
        Map<String, String> getProperties();

        /**
         * Returns the stack trace if this download ended in error
         *
         * @return the stack trace
         */
        Optional<String> getStackTrace();

        /**
         * Returns the error message if this download ended in error
         *
         * @return the error message
         */
        Optional<String> getErrorMessage();

    }

}
