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

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.glassfish.tyrus.server.Server;
import org.junit.jupiter.api.Test;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.job.*;
import uk.gov.gchq.palisade.client.java.resource.ServerSocket;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@Property(name = "palisade.client.url", value = "http://localhost:8081")
@Property(name = "micronaut.server.port", value = "8081")
class FullTest {

    private static final Logger log = LoggerFactory.getLogger(FullTest.class);

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

        //
        // lets start the socket server
        var server = new Server(HOST, WS_PORT, "/", Map.of(), ServerSocket.class);
        server.start();

        // lets start the ball rolling

        var job = Client.create().submit(config);
        var publisher = job.start();

        var next = new AtomicInteger(0);

//        publisher.subscribe(new Subscriber<Download>() {
//
//            @Override public void onSubscribe(Subscription s) {
//            }
//            @Override public void onNext(Download t) {
//                next.incrementAndGet();
//                log.debug("next now called {} times", next.intValue());
//            }
//            @Override public void onError(Throwable t) {
//            }
//            @Override public void onComplete() {
//            }
//
//        });

        // this is not ideal, but
        Thread.sleep(2000);

        // TODO: Fix the streaming
        // assertThat(next).hasValue(1);

        var tracker = job.getDownLoadTracker();

        assertThat(tracker.getNumSuccessful()).isEqualTo(1);
        assertThat(tracker.getNumFailed()).isEqualTo(0);

    }


}
