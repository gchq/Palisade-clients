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
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.download.DownloadManager;
import uk.gov.gchq.palisade.client.job.ClientJob;
import uk.gov.gchq.palisade.client.job.Result;
import uk.gov.gchq.palisade.client.job.state.IJobRequest;
import uk.gov.gchq.palisade.client.job.state.JobRequest;
import uk.gov.gchq.palisade.client.job.state.JobState;
import uk.gov.gchq.palisade.client.job.state.JobStateService;
import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.request.PalisadeService;
import uk.gov.gchq.palisade.client.util.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.UnaryOperator;

import static uk.gov.gchq.palisade.client.util.Checks.checkArgument;

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
public class JavaClient implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaClient.class);

    private final Configuration configuration;
    private final DownloadManager downloadManager;
    private final ObjectMapper objectMapper;

    /**
     * Create a new JavaClient injected with required services.
     *
     * @param configuration   The global configuration
     * @param downloadManager The download manager maintaining all downloads and
     *                        thread pools
     * @param objectMapper    The object mapper
     */
    public JavaClient(
            final Configuration configuration,
            final DownloadManager downloadManager,
            final ObjectMapper objectMapper) {
        this.configuration = checkArgument(configuration);
        this.objectMapper = checkArgument(objectMapper);
        this.downloadManager = checkArgument(downloadManager);
    }

    Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Result resume(final Path path) {
        return resume(path, Map.of());
    }

    @Override
    public Result resume(final Path path, final Map<String, Object> configuration) {
        var state = new JobStateService(objectMapper).createFrom(path, configuration);
        var receiver = createReceiver(state.getJobConfig().getReceiverClass());
        return submit(state, receiver);
    }

    @Override
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public Result submit(final UnaryOperator<JobRequest.Builder> func) {
        return submit(func.apply(JobRequest.builder()).build());
    }

    @Override
    public Result submit(final IJobRequest jobRequest) {

        LOGGER.debug("Job configuration submitted: {}", jobRequest);

        var receiver = createReceiver(jobRequest.getReceiverClass());
        var stateService = new JobStateService(objectMapper);
        var state = stateService.createNew(jobRequest, configuration);

        return submit(state, receiver);
    }

    private Result submit(final JobState state, final Receiver receiver) {

        var palisadeService = new PalisadeService(objectMapper, state.getConfiguration().getPalisadeUri());
        var eventBus = new EventBus();

        var job = ClientJob.createJob(b -> b
            .state(state)
            .palisadeClient(palisadeService)
            .receiver(receiver)
            .eventBus(eventBus)
            .downloadManager(downloadManager)
            .objectMapper(objectMapper)
        );
        return job.start();
    }

    private static final Receiver createReceiver(final Class<?> receiverClass) {
        try {
            return (Receiver) receiverClass.getConstructor().newInstance();
        } catch (IllegalArgumentException  |
                 IllegalAccessException    |
                 InstantiationException    |
                 InvocationTargetException |
                 NoSuchMethodException     |
                 SecurityException e) {
            throw new ClientException("Failed to create instance of " + receiverClass + ": " + e.getMessage(), e);
        }
    }

}
