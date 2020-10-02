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

import org.glassfish.tyrus.server.Server;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;

import uk.gov.gchq.palisade.client.java.data.*;
import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.request.*;
import uk.gov.gchq.palisade.client.java.resource.ServerSocket;
import uk.gov.gchq.palisade.client.java.util.ClientUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class FullTest {

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

    private static final String TOKEN = "abcd-1";
    private static final String LEAF_RESOURCE_ID = "leaf_resource_id";

    private static final int WS_PORT = 8082;
    private static final int HTTP_PORT = 8081;

    private static final String HOST = "localhost";
    private static final String BASE_URL = String.format("http://%s:%s", HOST, HTTP_PORT);

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startServer() {
        mockServer = ClientAndServer.startClientAndServer(HTTP_PORT);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    void testFull() throws Exception {

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

        var config = IJobConfig.<Troll>create(b -> b
                .classname("classname")
                .deserializer(ds)
                .objectFactory(of)
                .purpose("purpose")
                .requestId("request_id")
                .resourceId("resource_id")
                .userId("user_id"));

        var request = ClientUtil.createPalisadeRequest(config);

        createExpectationForValidPalisadeRequest(request);

        var dataRequest = IDataRequest.create(b -> b
                .token(TOKEN)
                .leafResourceId(LEAF_RESOURCE_ID));

        createExpectationForValidDataRequest(dataRequest);

        //
        // lets start the socket server
        var server = new Server(HOST, WS_PORT, "/", Map.of(), ServerSocket.class);
        server.start();

        // lets start the ball rolling

        var job = JavaClient.create().submit(config);
        job.start();

        // this is not ideal, but
        Thread.sleep(2000);

        var tracker = job.getDownLoadTracker();

        assertThat(tracker.getNumSuccessful()).isEqualTo(1);
        assertThat(tracker.getNumFailed()).isEqualTo(0);

    }

    private void createExpectationForValidPalisadeRequest(PalisadeRequest request) throws Exception {
        new MockServerClient(HOST, HTTP_PORT)
            .when(
                request()
                    .withMethod("POST")
                    .withPath(PalisadeRetrofitClient.REGISTER_DATA_REQUEST)
                    .withHeader("Content-type", "application/json; charset=utf-8")
                    .withBody(json(request)),
                        Times.unlimited())
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

    private void createExpectationForValidDataRequest(DataRequest request) throws Exception {
        new MockServerClient(HOST, HTTP_PORT)
            .when(
                request()
                    .withMethod("POST")
                    .withPath(DataClient.ENDPOINT_READ_CHUNKED)
                                .withHeader("Content-type", "application/json; charset=utf-8"),
//                    .withBody(json(request)),
                        Times.unlimited())
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/octet-stream; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                    .withBody("woohoo")
//          .withDelay(TimeUnit.MILLISECONDS, 500)
                );
    }

    private String toJson(Object obj) throws Exception {
        var om = new ObjectMapper();
        return om.writeValueAsString(obj);
    }

}
