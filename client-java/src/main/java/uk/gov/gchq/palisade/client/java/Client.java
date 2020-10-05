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
package uk.gov.gchq.palisade.client.java;

import uk.gov.gchq.palisade.client.java.job.*;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * <p>
 * A Client is the entry point to running jobs against Palisade. To create a
 * client do the following:
 * </p>
 * <pre>{@code
 * var client = (JavaClient) Client.create(Map.of(
 *     "palisade.client.download.threads", NUM_THREADS,
 *     "palisade.client.url", BASE_URL));
 * }</pre>
 * <p>
 * The number of threads is 1 by default. The default base url is
 * {@code http://localhost:8081}. In order to submit a request to Palisade,
 * use the following to create a {@code Job} instance:
 * </p>
 * <pre>{@code
 * var config = IJobConfig.<Troll>create(b -> b
 *     .classname("classname")
 *     .deserializer(ds)
 *     .objectFactory(of)
 *     .purpose("purpose")
 *     .requestId("request_id")
 *     .resourceId("resource_id")
 *     .userId("user_id"));
 * }</pre>
 * <p>
 * At the moment, the deserialiser and object factory is not used. Later the
 * idea is that the devloper would subscribe to the returned job and provide
 * a function to be run when an InputStream is emitted by the downloader/manager.
 * </p>
 * <p>
 * To start the job:
 * </p>
 * <pre>{@code
 * job.start() // this is where we need to provide a function
 * }</pre>
 *
 * At the momnent the client just outputs strings to the log
 *
 * @author dbell
 *
 */
public interface Client {

    /**
     * Returns a newly constructed {@code Job} using the provided configuration
     * function, which is used to submit a request to the Palisade service
     *
     * @param <E>  The type of instance that will eventually be downloaded
     * @param func The function applied to the {@code JobConfig.Builder} which
     *             provided the configuration
     * @return a newly constructed {@code Job} using the provided configuration
     *         function
     */
    <E> Job<E> submit(UnaryOperator<JobConfig.Builder<E>> func);

    /**
     * Returns a newly constructed {@code Job} using the provided configuration,
     * which is used to submit a request to the Palisade service
     *
     * @param <E>       The type of instance that will eventually be downloaded
     * @param jobConfig The configuration for the job
     * @return a newly constructed {@code Job} using the provided configuration
     */
    <E> Job<E> submit(JobConfig<E> jobConfig);

    /**
     * Returns a newly created {@code JavaClient} using all configuration defaults
     *
     * @return a newly created using all configuration defaults
     */
    public static Client create() {
        return JavaClient.createWith(null);
    }

    /**
     * Returns a newly created {@code JavaClient} using the provided function to
     * apply the configuration
     *
     * @param properties The properties to configure the client
     * @return a newly created {@code JavaClient} using the provided function to
     *         apply the configuration
     */
    public static Client create(Map<String, String> properties) {
        return JavaClient.createWith(null, properties);
    }

}