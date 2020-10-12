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

import io.micronaut.websocket.RxWebSocketClient;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.request.*;
import uk.gov.gchq.palisade.client.java.state.StateManager;

import java.util.function.UnaryOperator;

import com.google.common.eventbus.EventBus;

/**
 * <p>
 * The main client implementation.
 * </p>
 * <p>
 * Note that this class should only be created via the static {@code create}
 * methods on the {@link Client} interface. For tests it can be injected into a
 * test class annotated with {@code @MicronautTest} as it is configured for
 * dependency injection. This makes testing much easier.
 *
 * @author dbell
 * @since 0.5.0
 */
public class JavaClient implements Client {

    private static final Logger log = LoggerFactory.getLogger(JavaClient.class);

    private final ClientContext clientContext;

    @SuppressWarnings("unused")
    private final RxWebSocketClient webSocketClient; // NOT used yet

    /**
     * Create a new JavaClient injected with required services. The main injection
     * is the actual ApplicationContext that is performing the injection. Same as
     * Guice, Micronaut can inject itself, which is cool.
     *
     * @param webSocketClient This is to be used later instead of Tyrus
     * @param clientCcontext  The {@code ApplicationContext} doing the injecting
     */
    public JavaClient(RxWebSocketClient webSocketClient, ClientContext clientCcontext) {
        this.clientContext = clientCcontext;
        this.webSocketClient = webSocketClient;
    }

    @Override
    public Job submit(JobConfig jobConfig) {

        log.debug("Job configuration submitted: {}", jobConfig);

        var palisadeRequest = createRequest(jobConfig); // actual instance sent to palisade Service
        var palisadeService = clientContext.get(PalisadeClient.class); // the actual http client is wrapped

        log.debug("Submitting request to Palisade....");

        var palisadeResponse = palisadeService.submit(palisadeRequest);

        log.debug("Got response from Palisade: {}", palisadeResponse);

        assert palisadeResponse != null : "No response back from palisade service";

        var token = palisadeResponse.getToken();
        var eventBus = new EventBus("bus:" + token); // each job gets its own event bus (name is good for debbugging)
        var stateManager = clientContext.get(StateManager.class);

        // we'll wrap Micronaut's ApplicationConfig as we do nor want to expose this to
        // the client later on.

        // The JobContext contains everything that is unique for this job plus the
        // application context used to look up extra services that may be need

        var jobContext = IJobContext.createJobContext(b -> b
            .clientContext(clientContext)
            .jobConfig(jobConfig)
            .eventBus(eventBus) // create here as we could hook in if we wanted to
            .response(palisadeResponse));

        var job = new Job("job:" + token, jobContext);

        // The services being registered below need to talk to each other as they are
        // loosely coupled.

        eventBus.register(stateManager);
        eventBus.register(job);

        log.debug("Job created for token: {}", token);

        return job;

    }

    @Override
    public Job submit(UnaryOperator<JobConfig.Builder> func) {
        return submit(func.apply(JobConfig.builder()).build());
    }

    /**
     * Returns the {@code ApplicationContext} that was used to create this client.
     * The context is used to retrieve other services that the client relies on,
     * e.g. the {@code StateManager}.
     *
     * @return the {@code ApplicationContext} that was used to create this client.
     * @see "https://docs.micronaut.io/latest/api/io/micronaut/context/ApplicationContext.html"
     */
    ClientContext getClientContext() {
        return this.clientContext;
    }

    private PalisadeRequest createRequest(JobConfig jobConfig) {

        var userId = jobConfig.getUserId();
        var purpose = jobConfig.getPurpose();
        var className = jobConfig.getClassname();
        var requestId = jobConfig.getRequestId();
        var resourceId = jobConfig.getResourceId();
        var properties = jobConfig.getProperties();

        var req = PalisadeRequest.builder()
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

        log.debug("new palisade request crteated from job config: {}", req);

        return req;

    }
}
