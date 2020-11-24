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
package uk.gov.gchq.palisade.client.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.receiver.Receiver;
import uk.gov.gchq.palisade.client.receiver.ReceiverContext;
import uk.gov.gchq.palisade.client.receiver.ReceiverException;
import uk.gov.gchq.palisade.client.resource.IResource;
import uk.gov.gchq.palisade.client.util.Configuration;

import javax.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.gchq.palisade.client.download.Downloader.createDownloader;

@MicronautTest
@Property(name = "palisade.client.url", value = DownloaderTest.BASE_URL)
@Property(name = "micronaut.server.port", value = DownloaderTest.PORT)
class DownloaderTest {

    static final String PORT = "8083";
    static final String BASE_URL = "http://localhost:8083";

    private static Receiver receiver;
    private static ObjectMapper objectMapper;
    private static Configuration configuration;

    @Inject
    EmbeddedServer embeddedServer;

    @BeforeAll
    static void setupAll() {
        receiver = new FileReceiver();
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        configuration = Configuration.fromDefaults();
    }

    @Test
    void testSuccessfulDownload() throws Exception {

        var token = "abcd-1";
        var filename = "pi.txt";
        var properties = Map.of("receiver.file.path", "/tmp");

        var downloader = Downloader.createDownloader(b -> b
            .objectMapper(objectMapper)
            .configuration(configuration)
            .receiver(receiver)
            .resource(IResource.createResource(r -> r
                .leafResourceId(filename)
                .token(token)
                .url(BASE_URL))));

        downloader.start();

        // now load both the original file from the classpath (in resources folder) and
        // the on in /tmp. Both these files are compared byte by byte for equality.

        var actual = new FileInputStream(new File("/tmp/pal-" + token + "-" + filename));
        var expected = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        assertThat(streamsAreEqual(actual, expected)).isTrue();

    }


    @Test
    void testInvalid() throws Exception {

        var token = "abcd-1";
        var filename = "pi.txt";
        var properties = Map.of("receiver.file.path", "/tmp");

        var downloader = createDownloader(b -> b
            .objectMapper(objectMapper)
            .configuration(configuration)
            .receiver(receiver)
            .resource(IResource.createResource(r -> r
                .leafResourceId(filename)
                .token(token)
                .url(BASE_URL))));

        downloader.start();

        // now load both the original file from the classpath (in resources folder) and
        // the on in /tmp. Both these files are compared byte by byte for equality.

        var actual = new FileInputStream(new File("/tmp/pal-" + token + "-" + filename));
        var expected = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        assertThat(streamsAreEqual(actual, expected)).isTrue();

    }

    @Test
    void testReceiverFail() {

        var token = "abcd-1";
        var filename = "Selection_032.png";
        var properties = Map.of("receiver.file.path", "/tmp");

        var downloader = createDownloader(b -> b
            .objectMapper(objectMapper)
            .configuration(configuration)
            .receiver(new Receiver() {
                @Override
                public IReceiverResult process(final ReceiverContext ctx, final InputStream is)
                    throws ReceiverException {
                    throw new IllegalArgumentException("test exception");
                }
            })
            .resource(IResource.createResource(r -> r
                .leafResourceId(filename)
                .token(token)
                .url(BASE_URL))));

        assertThatExceptionOfType(DownloaderException.class)
            .isThrownBy(() -> downloader.start())
            .havingCause()
            .isInstanceOf(ReceiverException.class)
            .havingCause()
            .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void testFileNotFound() {

        var token = "abcd-1";
        var filename = "doesnotexist";
        var properties = Map.of("receiver.file.path", "/tmp");

        var downloader = createDownloader(b -> b
            .objectMapper(objectMapper)
            .configuration(configuration)
            .receiver(receiver)
            .resource(IResource.createResource(r -> r
                .leafResourceId(filename)
                .token(token)
                .url(BASE_URL))));

        assertThatExceptionOfType(DownloaderException.class)
            .isThrownBy(() -> downloader.start())
            .withMessage("Resource \"" + filename + "\" not found")
            .matches(ex -> ex.getStatusCode() == 404, "statuscode 404");

    }

    private static boolean streamsAreEqual(final InputStream actual, final InputStream expected) throws IOException {

        assertThat(actual).isNotNull();
        assertThat(expected).isNotNull();

        var ch1 = Channels.newChannel(actual);
        var ch2 = Channels.newChannel(expected);

        var buf1 = ByteBuffer.allocateDirect(1024);
        var buf2 = ByteBuffer.allocateDirect(1024);

        try {
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

        } finally {
            if (actual != null) {
                actual.close();
            }
            if (expected != null) {
                expected.close();
            }
        }
    }

}
