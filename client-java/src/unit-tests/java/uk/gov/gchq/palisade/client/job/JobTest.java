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
import org.junit.jupiter.api.Test;

import client;
import uk.gov.gchq.palisade.client.job.state.ISavedJobState.IStateJobRequest;
import uk.gov.gchq.palisade.client.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.util.Configuration;

import javax.inject.Inject;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.job.state.IJobRequest.createJobRequest;

@MicronautTest
class JobTest {

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void testNewJobCreation() {

        var properties = Map.<String, Object>of(
            Configuration.KEY_SERVICE_PS_PORT, embeddedServer.getPort(),
            Configuration.KEY_SERVICE_FRS_PORT, embeddedServer.getPort(),
            Configuration.KEY_RECEIVER_FILE_PATH, "/WOAH",
            Configuration.KEY_DOWNLOAD_THREADS, 1);

        var client = Client.create(properties);

        var config = createJobRequest(b -> b
            .purpose("purpose")
            .resourceId("resource_id")
            .userId("user_id")
            .receiverClass(FileReceiver.class)
            .putProperty("request_key", "request_value"));

        var state = client.submit(config).future().join();

        var palisadeResponse = state.getPalisadeResponse();

        assertThat(palisadeResponse).isNotNull();
        assertThat(palisadeResponse.getToken()).isEqualTo("abcd-1");

        var actualRequest = state.getRequest();

        // the client adds the default properties in for the receiver

        var expectedRequest = IStateJobRequest.createStateJobConfig(b -> b
            .purpose("purpose")
            .resourceId("resource_id")
            .userId("user_id")
            .receiverClass(FileReceiver.class.getCanonicalName())
            .putProperty("request_key", "request_value"));

        assertThat(actualRequest).isEqualTo(expectedRequest);

    }
}
