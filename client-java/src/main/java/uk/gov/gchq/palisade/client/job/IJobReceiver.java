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

import uk.gov.gchq.palisade.client.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * An instance of {@link JobConfig} is passed during the creation of a new Job.
 * <p>
 * Note that the {@link JobConfig} class is created at compile time. The way in
 * which the class is created is determined by the {@code ImmutableStyle}.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
public interface IJobReceiver {

    /**
     * Helper method to create a {@code JobReceiver} using a builder function which
     * negates the need to use {@code builder()} and {@code build()} methods.
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static JobReceiver createJobReceiver(final UnaryOperator<JobReceiver.Builder> func) {
        return func.apply(JobReceiver.builder()).build();
    }

    /**
     * Helper method to create a {@code JobReceiver} using a builder function which
     * negates the need to use {@code builder()} and {@code build()} methods.
     *
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static JobReceiver createJobReceiver() {
        return JobReceiver.builder().build();
    }

    @Value.Default
    default Receiver getReciver() {
        return new FileReceiver();
    }

    Map<String, String> getProperties();

    default JobReceiver change(final UnaryOperator<JobReceiver.Builder> func) {
        return func.apply(JobReceiver.builder().from(this)).build();
    }

}
