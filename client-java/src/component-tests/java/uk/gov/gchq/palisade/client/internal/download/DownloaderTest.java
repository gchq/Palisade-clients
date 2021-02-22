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
package uk.gov.gchq.palisade.client.internal.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.internal.dft.DefaultQueryResponse.EmittedResource;

import javax.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.net.http.HttpClient;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.FILE_NAME_0;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.FILE_PATH_0;
import static uk.gov.gchq.palisade.client.testing.ClientTestData.TOKEN;

@MicronautTest
class DownloaderTest {

    private static final String BASE_URL = "http://localhost:%d"; // needs port added before use
    private static final String ENDPOINT = "/read/chunked";

    private static ObjectMapper objectMapper;

    private Downloader downloader;

    @Inject
    EmbeddedServer embeddedServer;

    private String url;

    @BeforeAll
    static void setupAll() {
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    }

    @BeforeEach
    void setup() {
        this.url = String.format(BASE_URL, embeddedServer.getPort());
        this.downloader = Downloader.createDownloader(b -> b
            .httpClient(HttpClient.newHttpClient())
            .objectMapper(objectMapper)
            .path(ENDPOINT));
    }

    @Test
    void testSuccessfulDownload() throws Exception {

        var resource = EmittedResource.createResource(b -> b
            .leafResourceId(FILE_PATH_0)
            .token(TOKEN)
            .url(url));

        var download = downloader.fetch(resource);
        var file = new File(Thread.currentThread().getContextClassLoader().getResource(FILE_PATH_0).toURI());

        assertThat(download.getFilename())
            .as("Download filename is %s", FILE_NAME_0)
            .isEqualTo(Optional.of(FILE_NAME_0));

        // now load both the original file from the classpath (in resources folder) and
        // the on in /tmp. Both these files are compared byte by byte for equality.

        try (var actual = download.getInputStream();
             var expected = new FileInputStream(file);
        ) {
            assertThat(actual)
                .as("Downloaded input stream same as file input stream")
                .hasSameContentAs(expected);
        }

    }

    @Test
    void testFileNotFound() {

        var filename = "doesnotexist";

        var resource = EmittedResource.createResource(b -> b
            .leafResourceId(filename)
            .token(TOKEN)
            .url(url));

        var expectedClass = DownloaderException.class;
        var expectedStatus = 404;

        assertThatExceptionOfType(expectedClass)
            .as("%s is thrown with status %s when resource is not found", expectedClass, expectedStatus)
            .isThrownBy(() -> downloader.fetch(resource))
            .withMessage("Resource \"" + filename + "\" not found")
            .matches(ex -> ex.getStatusCode() == expectedStatus, "statuscode " + expectedStatus);

    }

}
