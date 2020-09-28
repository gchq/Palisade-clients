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

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.request.*;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class ClientTest {

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
    void testCreate() {

        var client = (JavaClient) JavaClient.create();

        var syscfg = client.getConfig();

        assertThat(syscfg.getObjectMapper()).isNotNull();
        assertThat(syscfg.getPalisadeClient()).isNotNull();
        assertThat(syscfg.getClientConfig()).isNotNull();
        assertThat(syscfg.getStateManager()).isNotNull();

        var palisadeConfig = syscfg.getClientConfig();

        assertThat(palisadeConfig.getDownloadThreads()).isEqualTo(1);
        assertThat(palisadeConfig.getUrl()).isEqualTo(BASE_URL);

    }

    @Test
    void test_new_job_creation() throws Exception {

        Deserializer<Troll> ds = ba -> new Troll(new String(ba, StandardCharsets.UTF_8));
        ObjectFactory<Troll> of = Troll::new;

        var client = JavaClient.create();

        // TODO
        // The following mess is client facing. This really needs to be made into a MUCH
        // better fluent configuration pattern.
        //
        // Actually, this HAS to change as this exposes the actual REST request object
        // to the user of this library, which is VERY bad. This MUST change before
        // release to Production.

        var requestId = IRequestId.create(e -> e.id("request_id"));
        var userId = IUserId.create(e -> e.id("user_id"));
        var context = IContext.create(b -> b.className("class_name").purpose("purpose"));
        var request = IPalisadeRequest.create(b -> b
                .resourceId("resource_id")
                .context(context)
                .requestId(requestId)
                .userId(userId));

        var config = IJobConfig.<Troll>create(b -> b
                .deserialiser(ds)
                .objectFactory(of)
                .request(request));

        createExpectationForValidRequest(request);

        var job = client.createJob(config);

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

        var dlm = jobContext.getDownloadManager();

        assertThat(dlm).isNotNull();
        assertThat(dlm.getId()).isEqualTo("dlm:abcd-1");

        var jobConfig = jobContext.getJobConfig();

        assertThat(jobConfig).isNotNull();
        assertThat(jobConfig.getRequest()).isSameAs(request);
        assertThat(jobConfig.getDeserialiser()).isSameAs(ds);
        assertThat(jobConfig.getObjectFactory()).isSameAs(of);

    }

    private void createExpectationForValidRequest(PalisadeRequest request) throws Exception {
        new MockServerClient(HOST, PORT)
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/registerDataRequest")
                    .withHeader("Content-type", "application/json; charset=utf-8")
                    .withBody(json(request)),
                once())
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody(toJson(IPalisadeResponse
                                        .create(b -> b.url("ws://localhost:8082/name").token("abcd-1"))))
//          .withDelay(TimeUnit.MILLISECONDS, 500)
            );
    }

    private String toJson(Object obj) throws Exception {
        var om = new ObjectMapper();
        return om.writeValueAsString(obj);
    }

}
