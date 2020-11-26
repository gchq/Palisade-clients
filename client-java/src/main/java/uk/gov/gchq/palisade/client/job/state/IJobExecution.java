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

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * An instance of {code JobExecution} is passed during the creation of a new
 * Job.
 * <p>
 * Note that the {@code JobExecution} class is created at compile time. The way
 * in which the class is created is determined by the {@code ImmutableStyle}.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
public interface IJobExecution {

    /**
     * Helper method to create a {@code JobExecution} using a builder function which
     * negates the need to use {@code builder()} and {@code build()} methods: <pre>
     * {@code
     * var jobEx =
     *     createJobExecution(b -> b
     *         .createdTime(time)
     *         .sequence(2));
     * }
     * </pre>
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static JobExecution createJobExecution(final UnaryOperator<JobExecution.Builder> func) {
        return func.apply(JobExecution.builder()).build();
    }

    /**
     * Helper method to create a {@code JobExecution} using a builder function which
     * negates the need to use {@code builder()} and {@code build()} methods: <pre>
     * {@code
     * var jobEx =
     *     createJobExecution(b -> b
     *         .createdTime(time)
     *         .sequence(2));
     * }
     * </pre>
     *
     * @return a newly created data request instance
     */
    static JobExecution createJobExecution() {
        return JobExecution.builder().build();
    }

    /**
     * Returns the unique id of this execution
     *
     * @return the unique id
     */
    @Value.Default
    default UUID getId() {
        return UUID.randomUUID();
    }

    /**
     * Returns the start date and time of this execution
     *
     * @return the start date and time of this execution
     */
    @Value.Default
    default Instant getStartTime() {
        return Instant.now();
    }

    /**
     * Returns the end date and time of this execution or empty if not complete
     *
     * @return the end date and time of this execution or empty if not complete
     */
    Optional<Instant> getEndTime();

    /**
     * Returns the sequence of this execution
     *
     * @return the sequence of this execution
     */
    @Value.Default
    default int getSequence() {
        return 1;
    }

    /**
     * Returns a list if errors that occurred during this execution
     *
     * @return a list if errors that occurred during this execution
     */
    List<IJobError> getErrors();

    /**
     * Returns a new instance with changes applied from the provided function
     *
     * @param func The changer function
     * @return a new instance with changes applied
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    default JobExecution change(final UnaryOperator<JobExecution.Builder> func) {
        return func.apply(JobExecution.builder().from(this)).build();
    }

}
