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

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import uk.gov.gchq.palisade.client.java.Client;
import uk.gov.gchq.palisade.client.java.request.*;
import uk.gov.gchq.palisade.client.java.util.ClientUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class Jobtest {

    static class Troll {
        private String name;
        public Troll(String name) {
            this.name = name;
        }
        public Troll() {
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static final int PORT = 8081;
    private static final String HOST = "localhost";
    private static final String BASE_URL = String.format("http://%s:%s", HOST, PORT);

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startServer() {
        mockServer = ClientAndServer.startClientAndServer(PORT);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    void test_new_job_creation() throws Exception {

        Deserializer<Troll> ds = stream -> {
            var bufferSize = 1024;
            var buffer = new char[bufferSize];
            var out = new StringBuilder();
            var in = new InputStreamReader(stream, StandardCharsets.UTF_8);
            int charsRead;
            try {
                while ((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                    out.append(buffer, 0, charsRead);
                }
            } catch (IOException ioe) {
                throw new DeserialiserException("Failed to read stream", ioe);
            }
            return new Troll(out.toString());
        };

        ObjectFactory<Troll> of = Troll::new;

        var client = Client.create();

        var config = IJobConfig.<Troll>create(b -> b
                .classname("classname")
                .deserializer(ds)
                .objectFactory(of)
                .purpose("purpose")
                .requestId("request_id")
                .resourceId("resource_id")
                .userId("user_id"));

        var request = ClientUtil.createPalisadeRequest(config);

        createExpectationForValidRequest(request);

        var job = client.submit(config);

        assertThat(job).isNotNull();
        assertThat(job.getId()).isEqualTo("job:abcd-1");

        var jobContext = job.getContext();

        assertThat(jobContext).isNotNull();

        var response = jobContext.getResponse();

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("abcd-1");
        assertThat(response.getUrl()).isEqualTo("ws://localhost:8082/name");

        var bus = jobContext.getEventBus();

        assertThat(bus).isNotNull();
        assertThat(bus.identifier()).isEqualTo("bus:abcd-1");

        var jobConfig = jobContext.getJobConfig();

        assertThat(jobConfig).isEqualTo(config);

    }

    private void createExpectationForValidRequest(PalisadeRequest request) throws Exception {
        new MockServerClient(HOST, PORT)
            .when(
                request()
                    .withMethod("POST").withPath("/registerDataRequest")
                    .withHeader("Content-type", "application/json; charset=utf-8")
                    .withBody(json(request)),
                once())
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                    .withBody(toJson(IPalisadeResponse.create(b -> b.url("ws://localhost:8082/name").token("abcd-1"))))
//          .withDelay(TimeUnit.MILLISECONDS, 500)
                );
    }

    private String toJson(Object obj) throws Exception {
        var om = new ObjectMapper();
        return om.writeValueAsString(obj);
    }

}
