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

import io.micronaut.context.ApplicationContext;
import io.micronaut.websocket.RxWebSocketClient;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.request.*;
import uk.gov.gchq.palisade.client.java.state.StateManager;

import java.util.*;
import java.util.function.UnaryOperator;

import com.google.common.eventbus.EventBus;

/**
 * The type Simple client.
 */
public class JavaClient implements Client {

    /**
     * Returns a newly created {@code JavaClient} using all configuration defaults
     * but using the provided Palisade client which will make calls to the Palisade
     * service.
     *
     * @param pc The {@code PalisadeClient} implementation to be used when maing
     *           calls top the Palisade service
     * @return a newly created {@code JavaClient}
     */
    static Client createWith(PalisadeClient pc) {
        return createWith(pc, Map.of());
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
    static Client createWith(PalisadeClient pc, Map<String, String> properties) {
        var map = new LinkedHashMap<String, Object>(properties);
        return ApplicationContext.run(map).getBean(Client.class);
    }

    private static final Logger log = LoggerFactory.getLogger(JavaClient.class);
    private final ApplicationContext context;
    @SuppressWarnings("unused")
    private final RxWebSocketClient webSocketClient; // NOT used yet

    public JavaClient(RxWebSocketClient webSocketClient, ApplicationContext context) {
        this.context = context;
        this.webSocketClient = webSocketClient;
    }

    @Override
    public <E> Job<E> submit(JobConfig<E> jobConfig) {

        var request = createRequest(jobConfig);
        var palisadeService = context.getBean(PalisadeClient.class);

        var response = palisadeService.submit(request);

        assert response != null : "No response back from palisade service";

        var token = response.getToken();
        var eventBus = new EventBus("bus:" + token);
        var stateManager = context.getBean(StateManager.class);

        var jobContext = IJobContext.<E>createJobContext(b -> b
                .applicationContext(context)
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

    ApplicationContext getApplicationContext() {
        return this.context;
    }

    private <E> PalisadeRequest createRequest(JobConfig<E> jobConfig) {

        var userId = jobConfig.getUserId();
        var purpose = jobConfig.getPurpose();
        var className = jobConfig.getClassname();
        var requestId = jobConfig.getRequestId();
        var resourceId = jobConfig.getResourceId();
        var properties = jobConfig.getProperties();

        return PalisadeRequest.builder()
                .resourceId(resourceId)
                .userId(UserId.builder()
                        .id(userId)
                        .build())
                .requestId(RequestId.builder()
                        .id(requestId)
                        .build())
                .context(Context.builder()
                        .className(className)
                        .purpose(purpose)
                        .contents(properties)
                        .build())
                .build();

    }
}
