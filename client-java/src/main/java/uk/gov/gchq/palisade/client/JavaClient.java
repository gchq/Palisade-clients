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
import uk.gov.gchq.palisade.client.job.JobConfig;
import uk.gov.gchq.palisade.client.request.PalisadeClient;
import uk.gov.gchq.palisade.client.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static uk.gov.gchq.palisade.client.job.IJobContext.createJobContext;
import static uk.gov.gchq.palisade.client.request.IPalisadeRequest.createPalisadeRequest;
import static uk.gov.gchq.palisade.client.resource.ResourceClient.createResourceClient;
import static uk.gov.gchq.palisade.client.resource.ResourceClientListener.createResourceClientListenr;
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

    private final Map<String, String> properties;
    private final Map<String, String> receiverProperties;

    private final PalisadeClient palisadeService;
    private final DownloadManager downloadManager;
    private final ObjectMapper objectMapper;

    /**
     * Create a new JavaClient injected with required services.
     *
     * @param properties      The global configuration
     * @param palisadeService the service providing acces to Palisade
     * @param downloadManager The download manager maintaining all downloads and
     *                        thread pools
     * @param objectMapper    The object mapper
     */
    public JavaClient(
            final Map<String, String> properties,
            final PalisadeClient palisadeService,
            final DownloadManager downloadManager,
            final ObjectMapper objectMapper) {

        this.properties = checkArgument(properties);
        this.objectMapper = checkArgument(objectMapper);
        this.palisadeService = checkArgument(palisadeService);
        this.downloadManager = checkArgument(downloadManager);

        this.receiverProperties = properties.entrySet().stream()
            .filter(es -> es.getKey().startsWith("receiver."))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    Map<String, String> getProperties() {
        return this.properties;
    }

    Map<String, String> getReceiverProperties() {
        return this.receiverProperties;
    }

    @Override
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    public Job createJob(final UnaryOperator<JobConfig.Builder> func) {
        return createJob(func.apply(JobConfig.builder()).build());
    }

    @Override
    public Job createJob(final JobConfig jobConfig) {

        LOGGER.debug("Job configuration submitted: {}", jobConfig);

        var palisadeResponse = palisadeService.submit(createRequest(jobConfig));

        var token = palisadeResponse.getToken();
        var url = palisadeResponse.getUrl();
        var eventBus = new EventBus();

        var resourceClient = createResourceClient(rc -> rc
            .baseUri(url)
            .resourceClientListener(createResourceClientListenr(rcl -> rcl
                .token(token)
                .downloadManagerStatus(downloadManager)
                .eventBus(eventBus)
                .objectMapper(objectMapper))));

        // override receiver properties

        var moddedJobConfig = jobConfig.change(jc -> jc
            .receiver(jobConfig.getReceiver().change(b -> b
                .putAllProperties(
                    Utils.overrideProperties(jobConfig.getReceiver().getProperties(), receiverProperties)))));

        var receiver = jobConfig.getReceiver().getReciver();

        var jobContext = createJobContext(b -> b
            .eventBus(eventBus)
            .objectMapper(objectMapper)
            .receiver(receiver)
            .jobConfig(moddedJobConfig)
            .palisadeResponse(palisadeResponse));

        // create the job
        var job = ClientJob.createJob(b -> b
            .resourceClient(resourceClient)
            .downloadManager(downloadManager)
            .context(jobContext));

        LOGGER.debug("Job created for token: {}", token);

        return job;
    }

    private static PalisadeRequest createRequest(final JobConfig jobConfig) {

        var userId = jobConfig.getUserId();
        var purposeOpt = jobConfig.getPurpose();
        var resourceId = jobConfig.getResourceId();
        var properties = new HashMap<>(jobConfig.getProperties());

        purposeOpt.ifPresent(pp -> properties.put("PURPOSE", pp));

        var palisadeRequest = createPalisadeRequest(b -> b
            .resourceId(resourceId)
            .userId(userId)
            .context(properties));

        LOGGER.debug("new palisade request created from job config: {}", palisadeRequest);

        return palisadeRequest;

    }

}
