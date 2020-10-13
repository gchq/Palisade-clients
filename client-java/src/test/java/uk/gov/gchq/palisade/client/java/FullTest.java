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
import org.junit.jupiter.api.*;

import uk.gov.gchq.palisade.client.java.job.IJobConfig;
import uk.gov.gchq.palisade.client.java.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.java.resource.ServerSocket;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@Property(name = ClientConfig.Client.URL_PROPERTY, value = "http://localhost:8081")
@Property(name = ClientConfig.Download.THREADS_PROPERTY, value = "2")
@Property(name = "micronaut.server.port", value = "8081")
class FullTest {

    private static final int WS_PORT = 8082;
    private static final String WS_HOST = "localhost";

    private Server server;

    @BeforeEach
    void setup() throws Exception {
        server = new Server(WS_HOST, WS_PORT, "/", Map.of(), ServerSocket.class);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testFull() throws Exception {

        var config = IJobConfig.create(b -> b
            .classname("classname")
            .purpose("purpose")
            .requestId("request_id")
            .resourceId("pi.txt")
            .userId("user_id")
            .receiverSupplier(() -> new FileReceiver()));

        // lets start the ball rolling

        var result = Client.create().submit(config);

        Thread.sleep(3000);

        /*
         * now we should read bothe the original and the new file The original is in
         * "src/test/resources" and the download file is in "/tmp"
         */

        var expected_is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pi.txt");
        var actual_is = new FileInputStream(new File("/tmp/pal-abcd-1-pi.txt"));

        assertThat(isEqual(actual_is, expected_is)).isTrue();

        expected_is = Thread.currentThread().getContextClassLoader().getResourceAsStream("Selection_032.png");
        actual_is = new FileInputStream(new File("/tmp/pal-abcd-1-Selection_032.png"));

        assertThat(isEqual(actual_is, expected_is)).isTrue();

    }

    private static boolean isEqual(InputStream i1, InputStream i2) throws IOException {

        ReadableByteChannel ch1 = Channels.newChannel(i1);
        ReadableByteChannel ch2 = Channels.newChannel(i2);

        ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
        ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);

        try {
            while (true) {

                int n1 = ch1.read(buf1);
                int n2 = ch2.read(buf2);

                if (n1 == -1 || n2 == -1)
                    return n1 == n2;

                buf1.flip();
                buf2.flip();

                for (int i = 0; i < Math.min(n1, n2); i++)
                    if (buf1.get() != buf2.get())
                        return false;

                buf1.compact();
                buf2.compact();
            }

        } finally {
            if (i1 != null)
                i1.close();
            if (i2 != null)
                i2.close();
        }
    }



}