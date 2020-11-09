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

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.job.IJobReceiver.createJobReceiver;

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
public interface IJobConfig {

    /**
     * Helper method to create a {@code JobConfig} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
//    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static JobConfig createJobConfig(final UnaryOperator<JobConfig.Builder> func) {
        return func.apply(JobConfig.builder()).build();
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
     * Returns the receiver instance that will handle the input stream from the
     * server
     *
     * @return the receiver instance that will handle the input stream from the
     *         server
     */
    @Value.Default
    default JobReceiver getReceiver() {
        return createJobReceiver();
    }

    /**
     * Returns a map of properties that will be passed to the Palisade Service
     *
     * @return a map of properties that will be passed to the Palisade Service
     */
    Map<String, String> getProperties();

    /**
     * Returns a new instance which is created by copying this instance and then
     * applying the changes provided by the supplied function
     *
     * @param func The function to apply
     * @return a new instance
     */
    default JobConfig change(final UnaryOperator<JobConfig.Builder> func) {
        return func.apply(JobConfig.builder().from(this)).build();
    }

}
