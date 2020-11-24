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
package uk.gov.gchq.palisade.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uk.gov.gchq.palisade.client.job.Result;
import uk.gov.gchq.palisade.client.job.state.IJobRequest;
import uk.gov.gchq.palisade.client.job.state.JobRequest;
import uk.gov.gchq.palisade.client.util.Configuration;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.download.DownloadManager.createDownloadManager;

/**
 * A Client is the entry point to running jobs against Palisade.
 *
 * @since 0.5.0
 */
public interface Client {

    /**
     * Returns a newly constructed {@code Job} using the provided configuration
     * function, which is used to submit a request to the Palisade service
     *
     * @param func The function applied to the {@code JobConfig.Builder} which
     *             provided the configuration
     * @return a newly constructed {@code Job} using the provided configuration
     * function
     */
    @SuppressWarnings("java:S3242")
    // I REALLY want to use UnaryOperator here SonarQube!!!
    Result submit(UnaryOperator<JobRequest.Builder> func);

    /**
     * Returns a newly constructed {@code Job} using the provided configuration,
     * which is used to submit a request to the Palisade service
     *
     * @param jobConfig The configuration for the job
     * @return a newly constructed {@code Job} using the provided configuration
     */
    Result submit(IJobRequest jobConfig);

    /**
     * Returns a newly constructed {@code Job} using the provided configuration,
     * which will resume from the point provided in the configuration
     *
     * @param path to the job state to resume from
     * @return a newly constructed {@code Job} using the provided configuration
     */
    Result resume(Path path);

    /**
     * Returns a newly constructed {@code Job} using the provided path to a saved
     * state and a map of configuration overrides to be applied.
     *
     * @param path          to the job state to resume from
     * @param configuration A map of attributes to be applied after the saved state
     *                      has been loaded
     * @return a newly constructed {@code Job} using the provided configuration
     */
    Result resume(Path path, Map<String, Object> configuration);

    /**
     * Returns a newly created {@code JavaClient} using all configuration defaults
     *
     * @return a newly created using all configuration defaults
     */
    static Client create() {
        return create(Map.of());
    }

    /**
     * Returns a newly created {@code JavaClient} using the provided property
     * overrides
     *
     * @param properties The properties to configure the client
     * @return a newly created {@code JavaClient} using the provided property
     * overrides
     */
    static Client create(final Map<String, Object> properties) {

        var configuration = Configuration.fromDefaults().merge(properties);

        var objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
//            .setSerializationInclusion(Include.NON_NULL)
//            .setSerializationInclusion(Include.NON_ABSENT)
//            .setSerializationInclusion(Include.NON_EMPTY)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        var downloadManager = createDownloadManager(b -> b
            .numThreads(configuration.getDownloadThreads())
            .objectMapper(objectMapper));

        return new JavaClient(
            configuration,
            downloadManager,
            objectMapper);

    }


}
