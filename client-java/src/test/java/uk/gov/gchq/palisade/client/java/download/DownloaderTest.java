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

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;

import uk.gov.gchq.palisade.client.java.data.IDataRequest;
import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.resource.IResource;

import javax.inject.Inject;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.google.common.eventbus.*;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@Property(name = "palisade.client.url", value = "http://localhost:8081")
@Property(name = "micronaut.server.port", value = "8081")
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

    private EventBus eventBus;

    @Inject
    EmbeddedServer embeddedServer;

    @BeforeEach
    public void setup() {
        this.eventBus = new EventBus();
        this.eventBus.register(this);
        this.object = null;
    }

    @Test
    void test_download() throws Exception {

        var token = "abcd-1";

        var resource = IResource.create(b -> b
                .leafResourceId("leaf_resource_id")
                .token(token)
                .url(BASE_URL));

        var downloader = Downloader.create(b -> b
                .eventBus(eventBus)
                .resource(resource));

        var dataRequest = IDataRequest.create(b -> b
                .token(token)
                .leafResourceId(resource.getLeafResourceId()));

//        createExpectationForValidRequest(dataRequest);

        downloader.run();

        assertThat(object).isNotNull().isInstanceOf(String.class).isEqualTo("OneTwo");

    }

    private Object object;

    @Subscribe
    public void onDownloadReady(DownloadReadyEvent event) {
        var ds = getTempDS();
        var is = event.getInputStream();
        this.object = ds.deserialize(is);
        log.debug("Consumed from stream: {}", this.object);
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
