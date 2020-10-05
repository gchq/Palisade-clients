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

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.java.Client;

import javax.inject.Inject;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@Property(name = "palisade.client.url", value = "http://localhost:8081")
@Property(name = "micronaut.server.port", value = "8081")
class JobTest {

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

    @Inject Client client;
    @Inject ObjectMapper objectMapper;
    @Inject EmbeddedServer embeddedServer;

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

        var config = IJobConfig.<Troll>create(b -> b
                .classname("classname")
                .deserializer(ds)
                .objectFactory(of)
                .purpose("purpose")
                .requestId("request_id")
                .resourceId("resource_id")
                .userId("user_id"));

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
}
