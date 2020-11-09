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

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.resource.Resource;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * An instance of {@link JobConfig} is passed during the submission of a new
 * request.
 * <p>
 * Note that the {@link JobConfig} class is created at compile time. The way in
 * which the class is created is determined by the {@code ImmutableStyle}.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
public interface IJobDownload {

    /**
     * Helper method to create a {@code JobConfig} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static JobDownload createDownload(final UnaryOperator<JobDownload.Builder> func) {
        return func.apply(JobDownload.builder()).build();
    }

    @Value.Default
    default UUID getId() {
        return UUID.randomUUID();
    }

    JobExecution getExecution();

    @Value.Default
    default Instant getStartedTime() {
        return Instant.now();
    }

    @Value.Default
    default JobDownloadStatus getStatus() {
        return JobDownloadStatus.IN_PROGRESS;
    }

    Optional<Instant> getEndTime();

    Resource getResource();

    Optional<Exception> getCause();

    @Value.Default
    default int getStatusCode() {
        return -1;
    }

    default JobDownload change(final UnaryOperator<JobDownload.Builder> func) {
        return func.apply(JobDownload.builder().from(this)).build();
    }

}
