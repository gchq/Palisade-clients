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
package uk.gov.gchq.palisade.client.job;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.Client;
import uk.gov.gchq.palisade.client.JavaClient;

import javax.inject.Inject;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.job.IJobConfig.createJobConfig;
import static uk.gov.gchq.palisade.client.job.IJobReceiver.createJobReceiver;

@MicronautTest
class JobTest {

    @Inject
    EmbeddedServer embeddedServer;

    @BeforeEach
    void setup() throws Exception {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testNewJobCreation() {

        var client = (JavaClient) Client.create(Map.of(
            "service.url", "http://localhost:" + embeddedServer.getPort(),
            "download.threads", "1"));

        var config = createJobConfig(b -> b
            .purpose("purpose")
            .resourceId("resource_id")
            .userId("user_id"));

        var job = (ClientJob) client.createJob(config);
        var context = job.getContext();

        var palisadeResponse = job.getContext().getPalisadeResponse();

        assertThat(palisadeResponse).isNotNull();
        assertThat(palisadeResponse.getToken()).isEqualTo("abcd-1");
        assertThat(palisadeResponse.getUrl()).isEqualTo("ws://localhost:" + embeddedServer.getPort() + "/name");

        var jobConfig = context.getJobConfig();

        // the client adds the default properties in for the receiver

        JobConfig expected = createJobConfig(b -> b
            .purpose("purpose")
            .resourceId("resource_id")
            .userId("user_id")
            .receiver(createJobReceiver(r -> r
                .reciver(context.getReceiver())
                .putProperty("receiver.file.path", "/tmp"))));

        assertThat(jobConfig).isEqualTo(expected);

    }
}
