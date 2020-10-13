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
package uk.gov.gchq.palisade.client.java.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.glassfish.tyrus.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.java.Client;
import uk.gov.gchq.palisade.client.java.ClientConfig;
import uk.gov.gchq.palisade.client.java.JavaClient;
import uk.gov.gchq.palisade.client.java.resource.ServerSocket;

import javax.inject.Inject;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@Property(name = ClientConfig.Client.URL_PROPERTY, value = "http://localhost:8081")
@Property(name = "micronaut.server.port", value = "8081")
class JobTest {

    private static final int WS_PORT = 8082;
    private static final String WS_HOST = "localhost";

    @Inject Client rawClient;
    @Inject ObjectMapper objectMapper;
    @Inject EmbeddedServer embeddedServer;

    private Server server;

    @BeforeEach
    void setup() throws Exception {
        server = new Server(WS_HOST, WS_PORT, "/", Map.of(), ServerSocket.class);
        server.start();
        embeddedServer.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
        embeddedServer.stop();
    }

    @Test
    void test_new_job_creation() throws Exception {

        var config = IJobConfig.create(b -> b
                .classname("classname")
                .purpose("purpose")
                .requestId("request_id")
                .resourceId("resource_id")
                .userId("user_id"));

        JavaClient client = (JavaClient) rawClient;

        var result = client.submit(config);

        var jobContext = client.getJobContext("abcd-1");

        assertThat(jobContext).isNotNull();

        var response = jobContext.getResponse();

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("abcd-1");
        assertThat(response.getUrl()).isEqualTo("ws://localhost:8082/name");

//        assertThat(response)
//            .hasToken("abcd-1")
//            .hasUrl("ws://localhost:8082/name");

        var jobConfig = jobContext.getJobConfig();

        assertThat(jobConfig).isEqualTo(config);

    }
}
