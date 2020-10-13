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
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;

import uk.gov.gchq.palisade.client.java.ClientContext;
import uk.gov.gchq.palisade.client.java.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.java.resource.IResource;

import javax.inject.Inject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dbell
 */
@MicronautTest
@Property(name = "palisade.client.url", value = DownloaderTest.BASE_URL)
@Property(name = "micronaut.server.port", value = DownloaderTest.PORT)
public class DownloaderTest implements ApplicationEventListener<DownloadCompleteEvent> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DownloaderTest.class);

    static final String PORT = "8083";
    static final String BASE_URL = "http://localhost:8083";

    @Inject
    EmbeddedServer embeddedServer;

    private static DownloadCompleteEvent event;

    @BeforeEach
    void setup() {
        embeddedServer.start();
    }

    @AfterEach
    void teardown() {
        embeddedServer.stop();
    }

    @Test
    void test_download() throws Exception {

        var token = "abcd-1";
        var filename = "Selection_032.png";
        var resource = IResource.create(b -> b.leafResourceId(filename).token(token).url(BASE_URL));

        var appctx = embeddedServer.getApplicationContext();
        ClientContext clientCtx = new ClientContext() {
            @Override public <T> T get(Class<T> type) {
                return appctx.getBean(type);
            }
        };

        var downloader = new Downloader(clientCtx, resource, new FileReceiver());

        downloader.run();

        log.debug("test says downloder.run() has finished");

        var actual_is = new FileInputStream(new File("/tmp/pal-" + token + "-" + filename));
        var expected_is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        assertThat(actual_is).isNotNull();
        assertThat(expected_is).isNotNull();

        // var o = getTempDS().deserialize(actual_is);
        // assertThat(o).isNotNull().isInstanceOf(String.class).isEqualTo(asString(filename));

        assertThat(isEqual(actual_is, expected_is)).isTrue();

        // now check that an event was thrown

        log.debug("test the event");

        assertThat(event).isNotNull();
        assertThat(event.getResource()).isEqualTo(resource);

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

    @Override
    public void onApplicationEvent(DownloadCompleteEvent event1) {
        this.event = event1;
        log.debug("caught: {}", event1);
    }

}
