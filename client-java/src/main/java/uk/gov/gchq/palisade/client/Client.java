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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
     * Submits a request via the provided builder function to Palisade and returns a
     * result object through which a status object can be retrieved
     *
     * @param func The function applied to the {@code JobConfig.Builder} which
     *             provided the configuration
     * @return a result object through which a status object can be retrieved
     * @throws ClientException if any error is encountered
     */
    @SuppressWarnings("java:S3242")
    Result submit(UnaryOperator<JobRequest.Builder> func);

    /**
     * Submits the provided {@code jobRequest} to Palisade and returns a result
     * object through which a status object can be retrieved
     *
     * @param jobRequest The {@code IJobRequest} to be submitted
     * @return a result object through which a status object can be retrieved
     * @throws ClientException if any error is encountered
     */
    Result submit(IJobRequest jobRequest);

    /**
     * Loads a previous job state to construct a job which the resumes from the
     * point of its last known state.
     *
     * @param path to the job state to resume from
     * @return a result object through which a status object can be retrieved
     * @throws ClientException if any error is encountered
     */
    Result resume(Path path);

    /**
     * Loads a previous job state to construct a job which the resumes from the
     * point of its last known state. The provided map is used to override the
     * previous state.
     *
     * @param path          to the job state to resume from
     * @param configuration The configuration used to override the save state
     * @return a result object through which a status object can be retrieved
     * @throws ClientException if any error is encountered
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
            // comment out the 3 include directives below to tell jackson to output all
            // attributes, even if null, absent or empty (e.g. empty optional and
            // collection)
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_ABSENT)
            .setSerializationInclusion(Include.NON_EMPTY)
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
