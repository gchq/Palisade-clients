/*
 * Copyright 2018-2021 Crown Copyright
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
package uk.gov.gchq.palisade.client.test.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.internal.download.Downloader;
import uk.gov.gchq.palisade.client.internal.download.DownloaderException;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;

import javax.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.FILE_NAME_0;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.TOKEN;

@MicronautTest
class DownloaderTest {

    private static final String BASE_URL = "http://localhost:%d/cluster/data/"; // needs port added before use
    private static final String ENDPOINT = "read/chunked";

    private static ObjectMapper objectMapper;

    private Downloader downloader;

    @Inject
    EmbeddedServer embeddedServer;

    private URI uri;

    @BeforeAll
    static void setupAll() {
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    }

    @BeforeEach
    void setup() {
        this.uri = URI.create(String.format(BASE_URL, embeddedServer.getPort()));
        this.downloader = Downloader.createDownloader(b -> b
            .httpClient(HttpClient.newHttpClient())
            .objectMapper(objectMapper)
            .path(ENDPOINT)
            .putServiceNameMap("data-service", uri));
    }

    @Test
    void testSuccessfulDownload() throws Exception {
        var resource = new FileResource()
            .id(FILE_NAME_0.asString())
            .connectionDetail(new SimpleConnectionDetail().serviceName("data-service"));

        var download = downloader.fetch(TOKEN, resource);

        // now load both the original file from the classpath (in resources folder) and
        // the one in /tmp. Both these files are compared byte by byte for equality.

        try (var actual = download.getInputStream();
             var expected = FILE_NAME_0.createStream();
        ) {
            assertThat(actual)
                .as("check downloaded input stream")
                .hasSameContentAs(expected);
        }
    }

    @Test
    void testFileNotFound() {
        var filename = "doesnotexist";

        var resource = new FileResource()
            .id(filename)
            .connectionDetail(new SimpleConnectionDetail().serviceName("data-service"));

        var expectedClass = DownloaderException.class;
        var expectedStatus = 500;

        assertThatExceptionOfType(expectedClass)
            .as("check correct exception when resource is not found")
            .isThrownBy(() -> downloader.fetch(TOKEN, resource))
            .withMessage("[" + expectedStatus + "] Request to DataService '" + uri + ENDPOINT + "' failed")
            .matches(ex -> ex.getStatusCode() == expectedStatus, "statuscode " + expectedStatus);
    }

}
