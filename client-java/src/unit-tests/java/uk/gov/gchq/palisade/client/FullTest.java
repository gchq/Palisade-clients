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
package uk.gov.gchq.palisade.client;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.job.state.IJobRequest;
import uk.gov.gchq.palisade.client.util.Configuration;

import javax.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dbell
 */
@MicronautTest
class FullTest {

    @Inject
    EmbeddedServer embeddedServer;

    private Client client;

    @BeforeEach
    void setup() throws Exception {
        client = Client.create(Map.of(
            Configuration.KEY_SERVICE_PS_PORT, embeddedServer.getPort(),
            Configuration.KEY_SERVICE_FRS_PORT, embeddedServer.getPort(),
            Configuration.KEY_DOWNLOAD_THREADS, 2));
    }

    @Test
    void testFull() throws Exception {

        IJobRequest jobRequest = IJobRequest.createJobRequest(b -> b
            .userId("user_id")
            .resourceId("pi.txt")
            .purpose("purpose"));

        // lets start the ball rolling
        var result = client.submit(jobRequest);
        var state = result.future().join();

        /*
         * now we should read both the original and the new file The original is in
         * "src/test/resources" and the download file is in "/tmp"
         */
        InputStream expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("pi.txt");
        FileInputStream actual = new FileInputStream(new File("/tmp/pal-abcd-1-pi.txt"));

        assertThat(isEqual(actual, expected)).isTrue();

        expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("Selection_032.png");
        actual = new FileInputStream(new File("/tmp/pal-abcd-1-Selection_032.png"));

        assertThat(isEqual(actual, expected)).isTrue();

    }

    private static boolean isEqual(final InputStream i1, final InputStream i2) throws IOException {
        ReadableByteChannel ch1 = Channels.newChannel(i1);
        ReadableByteChannel ch2 = Channels.newChannel(i2);

        ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
        ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);

        try (i1; i2) {
            while (true) {

                int n1 = ch1.read(buf1);
                int n2 = ch2.read(buf2);

                if (n1 == -1 || n2 == -1) {
                    return n1 == n2;
                }

                buf1.flip();
                buf2.flip();

                for (int i = 0; i < Math.min(n1, n2); i++) {
                    if (buf1.get() != buf2.get()) {
                        return false;
                    }
                }

                buf1.compact();
                buf2.compact();
            }
        }
    }

}
