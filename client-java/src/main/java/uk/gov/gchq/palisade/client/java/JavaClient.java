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

import org.immutables.value.Value;
import org.slf4j.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.request.*;
import uk.gov.gchq.palisade.client.java.state.StateManager;
import uk.gov.gchq.palisade.client.java.util.*;

import java.io.IOException;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

import static uk.gov.gchq.palisade.client.java.JavaClient.ISystemConfig.createSystemConfig;

/**
 * The type Simple client.
 */
public class JavaClient implements Client {

    /**
     * This class is the configuration that is passed when creating the
     * {@code JavaClient}.
     */
    @Value.Immutable
    @ImmutableStyle
    public interface IClientConfig {

        /**
         * Returns the URL that is to pbe used when submitting a request to the palisade
         * service
         *
         * @return the URL that is to pbe used when submitting a request to the palisade
         *         service
         */
        String getUrl();

        /**
         * Returns the number of download threads. If this is not set, it defaults to
         * {@link JavaClient.DEFAULT_NUM_DOWNLOAD_THREADS}
         *
         * @return the number of download threads
         */
        @Value.Default()
        default int getDownloadThreads() {
            return JavaClient.DEFAULT_NUM_DOWNLOAD_THREADS;
        }
    }

    /**
     * This class holds information and services that all jobs created by the client
     * will need. All services contained by it are deemed thread safe.
     */
    @Value.Immutable
    @ImmutableStyle
    public interface ISystemConfig {

        /**
         * Creates and returns a new {@code SystemConfig} instance from the provided
         * function
         *
         * @param <E>
         * @param func A function that takes a SystemConfig.Builder as input parameter
         *             and return value
         * @return returns a new {@code SystemConfig} instance from the provided
         *         function
         */
        static <E> SystemConfig createSystemConfig(UnaryOperator<SystemConfig.Builder> func) {
            return func.apply(SystemConfig.builder()).build();
        }

        /**
         * Returns the client that will make HTTP calls to the palisade service
         *
         * @return the client that will make HTTP calls to the palisade service
         */
        PalisadeClient getPalisadeClient();

        /**
         * Returns the correctly configured object mapper used to transform Java pojos
         * from/to json strings
         *
         * @return the correctly configured object mapper used to transform Java pojos
         *         from/to json strings
         */
        ObjectMapper getObjectMapper();

        /**
         * Returns the state manager instance that manages the state of the submitted
         * jobs
         *
         * @return the state manager instance that manages the state of the submitted
         *         jobs
         */
        StateManager getStateManager();

        /**
         * Returns the initial configuration supplied when the client was created
         *
         * @return the initial configuration supplied when the client was created
         */
        ClientConfig getClientConfig();
    }

    public static final String DEFAULT_BASE_URL = "http://localhost:8081";

    private static final Logger log = LoggerFactory.getLogger(JavaClient.class);
    private static final int DEFAULT_NUM_DOWNLOAD_THREADS = 1;

    private final SystemConfig systemConfig;

    /**
     * Returns a newly created {@code JavaClient} using all configuration defaults
     *
     * @return a newly created using all configuration defaults
     */
    public static final Client create() {
        return createWith(null);
    }

    /**
     * Returns a newly created {@code JavaClient} using the provided function to
     * apply the configuration
     *
     * @param func The function used to configure the client
     * @return a newly created {@code JavaClient} using the provided function to
     *         apply the configuration
     */
    public static final Client create(UnaryOperator<ClientConfig.Builder> func) {
        return createWith(null, func);
    }

    /**
     * Returns a newly created {@code JavaClient} using all configuration defaults
     * but using the provided Palisade client which will make calls to the Palisade
     * service.
     *
     * @param pc The {@code PalisadeClient} implementation to be used when maing
     *           calls top the Palisade service
     * @return a newly created {@code JavaClient}
     */
    static final Client createWith(PalisadeClient pc) {
        // TODO: get url from a property file?
        return createWith(pc, b -> b.url(DEFAULT_BASE_URL));
    }

    /**
     * Returns a newly created {@code JavaClient} using the provided function to
     * apply the configuration and using the provided Palisade client which will
     * make calls to the Palisade service.
     *
     * @param pc   The {@code PalisadeClient} implementation to be used when maing
     *             calls top the Palisade service
     * @param func The function used to configure the client
     * @return a newly created {@code JavaClient}
     */
    static final Client createWith(PalisadeClient pc, UnaryOperator<ClientConfig.Builder> func) {

        var clientConfig = func.apply(ClientConfig.builder()).build();
        var palisadeClient = pc != null ? pc : createPalisadeClient(clientConfig);
        var objectMapper = ClientUtil.getObjectMapper();
        var stateManager = ClientUtil.getStateManager();

        return new JavaClient(
                createSystemConfig(b -> b
                    .clientConfig(clientConfig)
                    .palisadeClient(palisadeClient)
                    .objectMapper(objectMapper)
                    .stateManager(stateManager)));

    }

    private static PalisadeClient createPalisadeClient(ClientConfig palisadeConfig) {
        return new PalisadeClient() {
            PalisadeRetrofitClient prc = new Retrofit.Builder()
                    .addConverterFactory(JacksonConverterFactory.create())
                    .baseUrl(palisadeConfig.getUrl())
                    .build()
                    .create(PalisadeRetrofitClient.class);
            @Override
            public PalisadeResponse submit(PalisadeRequest request) {
                var call = prc.registerDataRequestSync(request);
                try {
                    var serviceResponse = call.execute();
                    if (!serviceResponse.isSuccessful()) {
                        var url = palisadeConfig.getUrl() + PalisadeRetrofitClient.REGISTER_DATA_REQUEST;
                        var code = serviceResponse.code();
                        throw new ClientException(String.format("Request to %s failed with status %s", url, code));
                    }
                    var response = serviceResponse.body();
                    return response;
                } catch (IOException e) {
                    String msg = "Request to palisade failed";
                    throw new ClientException(msg, e);
                }
            }
        };
    }

    private JavaClient(SystemConfig systemConfig) {
        assert systemConfig != null : "SystemConfig should never be null when creating a JavaClient instance";
        this.systemConfig = systemConfig;
    }

    @Override
    public <E> Job<E> submit(JobConfig<E> jobConfig) {

        var request = ClientUtil.createPalisadeRequest(jobConfig);
        var response = systemConfig.getPalisadeClient().submit(request);

        assert response != null : "No response back from palisade service";

        var token = response.getToken();
        var eventBus = new EventBus("bus:" + token);
        var stateManager = systemConfig.getStateManager();

        var jobContext = IJobContext.<E>createJobContext(b -> b
                .systemConfig(this.systemConfig)
                .jobConfig(jobConfig)
                .eventBus(eventBus) // create here as we could hook in if we wanted to
                .response(response));

        var job = new Job<>("job:" + token, jobContext);

        eventBus.register(stateManager);
        eventBus.register(job);

        return job;

    }

    @Override
    public <E> Job<E> submit(UnaryOperator<JobConfig.Builder<E>> func) {
        return submit(func.apply(JobConfig.builder()).build());
    }

    /**
     * Returns the system configuration active for this JavaClient instance
     *
     * @return the system configuration active for this JavaClient instance
     */
    SystemConfig getConfig() {
        return this.systemConfig;
    }

}
