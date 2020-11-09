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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import uk.gov.gchq.palisade.client.job.JobConfig;
import uk.gov.gchq.palisade.client.request.PalisadeService;

import java.util.HashMap;
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
    Job createJob(UnaryOperator<JobConfig.Builder> func);

    /**
     * Returns a newly constructed {@code Job} using the provided configuration,
     * which is used to submit a request to the Palisade service
     *
     * @param jobConfig The configuration for the job
     * @return a newly constructed {@code Job} using the provided configuration
     */
    Job createJob(JobConfig jobConfig);

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
    static Client create(final Map<String, String> properties) {

        var objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

        var props = new HashMap<>(properties);

        props.computeIfAbsent("receiver.file.path", k -> "/tmp");
        var threads = props.computeIfAbsent("download.threads", k -> "" + Runtime.getRuntime().availableProcessors());
        var url = props.computeIfAbsent("service.url", k -> "http://localhost:8081");

        var downloadManager = createDownloadManager(b -> b
            .numThreads(Integer.parseInt(threads))
            .objectMapper(objectMapper));

        var palisadeService = new PalisadeService(objectMapper, url);

        // == Finally the Java Client itself

        return new JavaClient(
            props,
            palisadeService,
            downloadManager,
            objectMapper);

    }

}
