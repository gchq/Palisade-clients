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
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.time.Instant;
import java.util.Map;
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
public interface IJobContext {

    /**
     * Helper method to create a {@code JobContext} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static JobContext createJobContext(final UnaryOperator<JobContext.Builder> func) {
        return func.apply(JobContext.builder()).build();
    }

    /**
     * Returns the token
     *
     * @return the token
     */
    default String getToken() {
        return getPalisadeResponse().getToken();
    }

    /**
     * Returns the creation time
     *
     * @return the creation time
     */
    @Value.Default
    default Instant getCreated() {
        return Instant.now();
    }

    /**
     * Returns the event bus specific to this context. Other jobs will have their
     * own eventbus.
     *
     * @return the event bus specific to this context
     */
    EventBus getEventBus();

    /**
     * Returns the supplied job configuration
     *
     * @return the supplied job configuration
     */
    JobConfig getJobConfig();

    /**
     * Returns the response from the palisade service
     *
     * @return the response from the palisade service
     */
    PalisadeResponse getPalisadeResponse();

    /**
     * Returns the receiver that will handle the downloads
     *
     * @return the receiver that will handle the downloads
     */
    Receiver getReceiver();

    /**
     * Returns the object mapper
     *
     * @return the object mapper
     */
    ObjectMapper getObjectMapper();

    /**
     * Returns a map of properties for this job
     *
     * @return a map of properties for this job
     */
    Map<String, String> getProperties();

}
