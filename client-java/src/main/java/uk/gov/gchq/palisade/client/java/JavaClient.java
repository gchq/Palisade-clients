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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.runtime.event.annotation.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.java.download.DownloadFailedEvent;
import uk.gov.gchq.palisade.client.java.download.DownloadManager;
import uk.gov.gchq.palisade.client.java.download.DownloadTracker;
import uk.gov.gchq.palisade.client.java.job.JobConfig;
import uk.gov.gchq.palisade.client.java.job.JobContext;
import uk.gov.gchq.palisade.client.java.receiver.Receiver;
import uk.gov.gchq.palisade.client.java.request.IPalisadeRequest;
import uk.gov.gchq.palisade.client.java.request.PalisadeClient;
import uk.gov.gchq.palisade.client.java.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.java.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.resource.ResourceClient;
import uk.gov.gchq.palisade.client.java.resource.ResourceReadyEvent;
import uk.gov.gchq.palisade.client.java.resource.ResourcesExhaustedEvent;
import uk.gov.gchq.palisade.client.java.util.Bus;

import javax.inject.Singleton;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.java.job.IJobContext.createJobContext;

/**
 * The main client implementation.
 * <p>
 * Note that this class should only be created via the static {@code create}
 * methods on the {@link Client} interface. For tests it can be injected into a
 * test class annotated with {@code MicronautTest} as it is configured for
 * dependency injection. This makes testing much easier.
 *
 * @since 0.5.0
 */
@Singleton
public class JavaClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaClient.class);
    private static final String EVENT_CAUGHT = "Caught event: {}";

    private final ClientContext clientContext;
    private final Map<String, JobContext> jobs = new HashMap<>();

    /**
     * Create a new JavaClient injected with required services. The main injection
     * is the actual ApplicationContext that is performing the injection. Same as
     * Guice, Micronaut can inject itself, which is cool.
     *
     * @param clientCcontext The {@code ApplicationContext} doing the injecting
     */
    public JavaClient(final ClientContext clientCcontext) {
        this.clientContext = Objects.requireNonNull(clientCcontext);
    }

    @Override
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public Result submit(final UnaryOperator<JobConfig.Builder> func) {
        return submit(func.apply(JobConfig.builder()).build());
    }

    @Override
    public Result submit(final JobConfig jobConfig) {

        LOGGER.debug("Job configuration submitted: {}", jobConfig);

        final PalisadeRequest palisadeRequest = createRequest(jobConfig); // actual instance sent to palisade Service
        final PalisadeClient palisadeService = clientContext.get(PalisadeClient.class); // the actual http client is wrapped

        LOGGER.debug("Submitting request to Palisade....");
        final PalisadeResponse palisadeResponse = palisadeService.submit(palisadeRequest);

        assert palisadeResponse != null : "No response back from palisade service";
        LOGGER.debug("Got response from Palisade: {}", palisadeResponse);

        final String token = palisadeResponse.getToken();

        final JobContext jobContext = createJobContext(b -> b
                .jobConfig(jobConfig)
                .response(palisadeResponse));

        jobs.put(token, jobContext);

        final ResourceClient resourceClient = new ResourceClient(
                token,
                clientContext.get(Bus.class),
                clientContext.get(ObjectMapper.class),
                clientContext.get(DownloadTracker.class));

        try {
            final String url = palisadeResponse.getUrl();
            final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(resourceClient, new URI(url)); // this start communication
            LOGGER.debug("Job [{}] started", token);
        } catch (final IOException | DeploymentException | URISyntaxException e) {
            throw new ClientException("Error occurred in websocket: " + e.getMessage(), e);
        }

        LOGGER.debug("Job created for token: {}", token);

        return new Result() {
            // empty for the moment
        };

    }

    /**
     * Handles the {@code ResourceReadyEvent} by scheduling a download. This method
     * will return immediately as the download will be queued.
     *
     * @param event The event to be handled
     */
    @SuppressWarnings("java:S3242")
    @EventListener
    public void onScheduleDownload(final ResourceReadyEvent event) {
        Resource resource = event.getResource();
        JobContext jobContext = getJobContext(resource.getToken());
        Receiver receiver = jobContext.getJobConfig().getReceiverSupplier().get();
        clientContext.get(DownloadManager.class).schedule(resource, receiver);
    }

    /**
     * Handles a download failed event
     *
     * @param event The event to be handled
     */
    @EventListener
    public void onFailedDownload(final DownloadFailedEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
        LOGGER.debug("Download failed", event.getThrowble());
    }

    /**
     * Handles the {@code ResourcesExhaustedEvent} event.
     *
     * @param event to be handled
     */
    @EventListener
    public void onNoMoreDownloads(final ResourcesExhaustedEvent event) {
        LOGGER.debug(EVENT_CAUGHT, event);
    }

    /**
     * Returns the job context for the provided token, or null if not found
     *
     * @param token The token whose job context is to be returned
     * @return the job context for the provided token, or null if not found
     */
    public JobContext getJobContext(final String token) {
        return jobs.get(token);
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

    private static PalisadeRequest createRequest(final JobConfig jobConfig) {

        String userId = jobConfig.getUserId();
        Optional<Object> purposeOpt = jobConfig.getPurpose();
        String resourceId = jobConfig.getResourceId();
        HashMap<String, Object> properties = new HashMap<>(jobConfig.getProperties());

        purposeOpt.ifPresent(pp -> properties.put("PURPOSE", pp));

        PalisadeRequest req = IPalisadeRequest.create(b -> b
                .resourceId(resourceId)
                .userId(userId)
                .context(properties));
        LOGGER.debug("new palisade request created from job config: {}", req);

        return req;

    }

}

