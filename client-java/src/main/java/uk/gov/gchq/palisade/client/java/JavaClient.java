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
import uk.gov.gchq.palisade.client.java.request.Context;
import uk.gov.gchq.palisade.client.java.request.PalisadeClient;
import uk.gov.gchq.palisade.client.java.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.java.request.RequestId;
import uk.gov.gchq.palisade.client.java.request.UserId;
import uk.gov.gchq.palisade.client.java.resource.ResourceClient;
import uk.gov.gchq.palisade.client.java.resource.ResourceReadyEvent;
import uk.gov.gchq.palisade.client.java.resource.ResourcesExhaustedEvent;
import uk.gov.gchq.palisade.client.java.util.Bus;

import javax.inject.Singleton;
import javax.websocket.ContainerProvider;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.java.job.IJobContext.createJobContext;

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
 * @since 0.5.0
 */
@Singleton
public class JavaClient implements Client {

    private static final Logger LOG = LoggerFactory.getLogger(JavaClient.class);
    private static final String EVENT_CAUGHT = "Caught event: {}";

    private final ClientContext clientContext;
    private final Map<String, JobContext> jobs = new HashMap<>();

    /**
     * Create a new JavaClient injected with required services. The main injection
     * is the actual ApplicationContext that is performing the injection. Same as
     * Guice, Micronaut can inject itself, which is cool.
     *
     * @param clientCcontext  The {@code ApplicationContext} doing the injecting
     */
    public JavaClient(final ClientContext clientCcontext) {
        this.clientContext = Objects.requireNonNull(clientCcontext);
    }

    @Override
    public Result submit(final UnaryOperator<JobConfig.Builder> func) {
        return submit(func.apply(JobConfig.builder()).build());
    }

    @Override
    public Result submit(final JobConfig jobConfig) {

        LOG.debug("Job configuration submitted: {}", jobConfig);

        final var palisadeRequest = createRequest(jobConfig); // actual instance sent to palisade Service
        final var palisadeService = clientContext.get(PalisadeClient.class); // the actual http client is wrapped

        LOG.debug("Submitting request to Palisade....");

        final var palisadeResponse = palisadeService.submit(palisadeRequest);

        assert palisadeResponse != null : "No response back from palisade service";

        LOG.debug("Got response from Palisade: {}", palisadeResponse);

        final var token = palisadeResponse.getToken();

        final var jobContext = createJobContext(b -> b
            .jobConfig(jobConfig)
            .response(palisadeResponse));

        jobs.put(token, jobContext);

        final var resourceClient = new ResourceClient(
            token,
            clientContext.get(Bus.class),
            clientContext.get(ObjectMapper.class),
            clientContext.get(DownloadTracker.class));

        try {
            final var url = palisadeResponse.getUrl();
            final var container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(resourceClient, new URI(url)); // this start communication
            LOG.debug("Job [{}] started", token);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        LOG.debug("Job created for token: {}", token);

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
    @EventListener
    public void onScheduleDownload(final ResourceReadyEvent event) {
        var resource = event.getResource();
        var jobContext = getJobContext(resource.getToken());
        var receiver = jobContext.getJobConfig().getReceiverSupplier().get();
        clientContext.get(DownloadManager.class).schedule(resource, receiver);
    }

    /**
     * Handles a download failed event
     *
     * @param event The event to be handled
     */
    @EventListener
    public void onFailedDownload(final DownloadFailedEvent event) {
        LOG.debug(EVENT_CAUGHT, event);
        LOG.debug("Download failed", event.getThrowble());
    }

    /**
     * Handles the {@code ResourcesExhaustedEvent} event.
     *
     * @param event to be handled
     */
    @EventListener
    public void onNoMoreDownloads(final ResourcesExhaustedEvent event) {
        LOG.debug(EVENT_CAUGHT, event);
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

    private PalisadeRequest createRequest(final JobConfig jobConfig) {

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

        LOG.debug("new palisade request crteated from job config: {}", req);

        return req;

    }

//    /**
//     * Handle the job complete event by shutting down the executor. This method will
//     * block for a small amount of time for the executors to complete before
//     * shutting down, or if the timeout occurs, it is forced to quit. Once the
//     * executor has terminated, an end of queue flag is added to the queue to
//     * signify that the stream should complete once all other queued downloads have
//     * been emitted.
//     *
//     * @param event The event to handle
//     */
//    @Subscribe
//    public void handleJobComplete(ResourcesExhaustedEvent event) {
//        // add a Download with no input stream to the queue. This will instruct the
//        // stream to terminate
//        executor.shutdown();
//        try {
//            executor.awaitTermination(2, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            // something musty be really stuck
//        } finally {
//            if (executor.isTerminating()) {
//                // we've given it enough time, so halt it
//                @SuppressWarnings("unused")
//                var tasks = executor.shutdownNow();
//                // TODO: we should do something with these tasks that were not executed.
//            }
//        }
//    }

}
