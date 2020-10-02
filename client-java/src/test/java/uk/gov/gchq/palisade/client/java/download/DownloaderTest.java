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
package uk.gov.gchq.palisade.client.java.download;

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.slf4j.Logger;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import uk.gov.gchq.palisade.client.java.data.*;
import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.resource.IResource;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class DownloaderTest {

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

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DownloaderTest.class);
    private static final int PORT = 8083;
    private static final String HOST = "localhost";
    private static final String BASE_URL = String.format("http://%s:%s", HOST, PORT);

    private static ClientAndServer mockServer;
    private EventBus eventBus;

    @BeforeAll
    public static void startServer() {
        mockServer = ClientAndServer.startClientAndServer(PORT);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    @BeforeEach
    public void setup() {
        this.eventBus = new EventBus();
        this.eventBus.register(this);
        this.object = null;
    }

    @Test
    void test_download() throws Exception {

        var token = "abcd-1";

        // create a connection to data service
        var dataClient = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(DataClient.class);

        var resource = IResource.create(b -> b
                .leafResourceId("leaf_resource_id")
                .token(token)
                .url(BASE_URL));

        var downloader = Downloader.create(b -> b
                .eventBus(eventBus)
                .dataClient(dataClient)
                .resource(resource));

        var dataRequest = IDataRequest.create(b -> b
                .token(token)
                .leafResourceId(resource.getLeafResourceId()));

        createExpectationForValidRequest(dataRequest);

        downloader.run();

        assertThat(object).isNotNull().isInstanceOf(String.class).isEqualTo("woohoo");

    }

    private Object object;

    @Subscribe
    public void onDownloadReady(DownloadReadyEvent event) {
        var ds = getTempDS();
        var is = event.getInputStream();
        this.object = ds.deserialize(is);
        log.debug("Consumed from stream: {}", this.object);
    }

    private void createExpectationForValidRequest(DataRequest request) throws Exception {
        new MockServerClient(HOST, PORT)
            .when(
                request()
                    .withMethod("POST")
                    .withPath(DataClient.ENDPOINT_READ_CHUNKED)
                    .withHeader("Content-type", "application/json; charset=utf-8")
                    .withBody(json(request)),
                once())
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

    private Deserializer<String> getTempDS() {

        return stream -> {
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
            return out.toString();
        };

    }

}
