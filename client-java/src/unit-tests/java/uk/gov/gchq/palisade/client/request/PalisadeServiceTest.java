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
package uk.gov.gchq.palisade.client.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
class PalisadeServiceTest {

    @Inject ObjectMapper objectMapper;
    @Inject EmbeddedServer embeddedServer;

    @BeforeEach
    void setup() throws Exception {
        embeddedServer.start();
    }

    @AfterEach
    void tearDown() {
        embeddedServer.stop();
    }

    @Test
    void testSubmit() throws Exception {

        var port = embeddedServer.getPort();
        var palisadeRequest = IPalisadeRequest.createPalisadeRequest(b -> b
            .resourceId("resource_id")
            .userId("user_id")
            .putContext("key", "value"));

        var service = new PalisadeService(objectMapper, "http://localhost:" + port);

        var palisadeResponse = service.submit(palisadeRequest);

        assertThat(palisadeResponse).isNotNull();
        assertThat(palisadeResponse.getToken()).isEqualTo("abcd-1");
        assertThat(palisadeResponse.getUrl()).isEqualTo("ws://localhost:" + port + "/name");

    }
}
