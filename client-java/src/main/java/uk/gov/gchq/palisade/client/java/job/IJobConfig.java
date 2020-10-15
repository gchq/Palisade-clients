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
package uk.gov.gchq.palisade.client.java.job;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.receiver.LoggingReceiver;
import uk.gov.gchq.palisade.client.java.receiver.Receiver;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * <p>
 * An instance of {@code JobConfig} is passed during the submission of a new
 * request.
 * </p>
 * <p>
 * Note that the {@code JobConfig} class is created at compile time. The way in
 * which the class is created is determined by the {@link ImmutableStyle}.
 * </p>
 *
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
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
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static JobConfig create(final UnaryOperator<JobConfig.Builder> func) {
        return func.apply(JobConfig.builder()).build();
    }

    /**
     * Returns the resource id
     *
     * @return the resource id
     */
    String getResourceId();

    /**
     * Returns the request id
     *
     * @return the request id
     */
    String getRequestId();

    /**
     * Returns the user id
     *
     * @return the user id
     */
    String getUserId();

    /**
     * Returns the purpose
     *
     * @return the purpose
     */
    String getPurpose();

    /**
     * Returns the class name
     *
     * @return the class name
     */
    String getClassname();

    /**
     * Returns a map of extra properties for this job
     *
     * @return a map of extra properties for this job
     */
    Map<String, String> getProperties();

    /**
     * Returns the receiver supplier. If it has not been set then a simple logging
     * receiver will be used. This just simply confirms that it was called.
     *
     * @return the receiver supplier
     */
    @Value.Default
    default Supplier<Receiver> getReceiverSupplier() {
        return LoggingReceiver::new;
    }

}
