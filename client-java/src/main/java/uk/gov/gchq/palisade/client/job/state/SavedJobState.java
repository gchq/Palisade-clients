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
@JsonDeserialize(as = ImmutableSavedJobState.class)
@JsonSerialize(as = ImmutableSavedJobState.class)
public interface SavedJobState extends Serializable {

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutableSavedJobState.Builder { // empty
    }

    /**
     * Helper method to create a {@code State} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    static SavedJobState create(final UnaryOperator<Builder> func) {
        return func.apply(new Builder()).build();
    }

    /**
     * Returns a new instance with changes applied from the provided function
     *
     * @param func The changer function
     * @return a new instance with changes applied
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    default SavedJobState change(final UnaryOperator<Builder> func) {
        return func.apply(new Builder().from(this)).build();
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
    StateJobRequest getRequest();

    /**
     * Returns the palisade response
     *
     * @return the palisade response
     */
    StatePalisadeResponse getPalisadeResponse();

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
    List<StateExecution> getExecutions();

    /**
     * Returns the downloads for this job
     *
     * @return the downloads for this job
     */
    List<StateDownload> getDownloads();

    /**
     * The palisade response
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = ImmutableStatePalisadeResponse.class)
    @JsonSerialize(as = ImmutableStatePalisadeResponse.class)
    interface StatePalisadeResponse {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableStatePalisadeResponse.Builder { // empty
        }

        /**
         * Helper method to create a {@code StatePalisadeResponse} using a builder
         * function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        static StatePalisadeResponse createStatePalisadeResponse(final UnaryOperator<Builder> func) {
            return func.apply(new Builder()).build();
        }

        /**
         * Returns the unique token
         *
         * @return the unique token
         */
        String getToken();

    }

    /**
     * Represents an error from the Filtered Resource Service.
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = ImmutableStateJobError.class)
    @JsonSerialize(as = ImmutableStateJobError.class)
    interface StateJobError {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableStateJobError.Builder { // empty
        }

        /**
         * Helper method to create a {@code StatePalisadeResponse} using a builder
         * function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        static StateJobError createStateJobError(final UnaryOperator<Builder> func) {
            return func.apply(new Builder()).build();
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
    @JsonDeserialize(as = ImmutableStateJobRequest.class)
    @JsonSerialize(as = ImmutableStateJobRequest.class)
    interface StateJobRequest {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableStateJobRequest.Builder { // empty
        }

        /**
         * Helper method to create a {@code StateJobConfig} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        static StateJobRequest createStateJobConfig(final UnaryOperator<Builder> func) {
            return func.apply(new Builder()).build();
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
    @JsonDeserialize(as = ImmutableStateExecution.class)
    @JsonSerialize(as = ImmutableStateExecution.class)
    interface StateExecution {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableStateExecution.Builder { // empty
        }

        /**
         * Helper method to create a {@code StateExecution} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        static StateExecution createStateExecution(final UnaryOperator<Builder> func) {
            return func.apply(new Builder()).build();
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
        List<StateJobError> getErrors();

    }

    /**
     * A job download
     *
     * @since 0.5.0
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = ImmutableStateDownload.class)
    @JsonSerialize(as = ImmutableStateDownload.class)
    interface StateDownload {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableStateDownload.Builder { // empty
        }

        /**
         * Helper method to create a {@code StateDownload} using a builder function
         *
         * @param func The builder function
         * @return a newly created data request instance
         */
        static StateDownload createStateDownload(final UnaryOperator<Builder> func) {
            return func.apply(new Builder()).build();
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
