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
package uk.gov.gchq.palisade.client;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.job.state.ISavedJobState;
import uk.gov.gchq.palisade.client.util.Configuration;

import javax.inject.Inject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @since 0.5.0
 */
@MicronautTest
class ResumeTest {

    private static final String STATE_NO_DOWNLOADS = "resume/palisade-state_1_no-downloads.json";

    @Inject
    EmbeddedServer embeddedServer;

    private Client client;

    @BeforeEach
    void setup() {
        client = Client.create();
    }

    @Test
    void testFullResume() throws Exception {

        var url = Thread.currentThread().getContextClassLoader().getResource(STATE_NO_DOWNLOADS);
        var path = Paths.get(url.toURI());

        // when resuming in this unit test, there will be a different port for the
        // embedded server. we need to provide that to as part of the resume call so
        // that it can override those read in from the previous state.

        var result = client.resume(path, Map.of(
            Configuration.KEY_SERVICE_PS_PORT, embeddedServer.getPort(),
            Configuration.KEY_SERVICE_FRS_PORT, embeddedServer.getPort(),
            Configuration.KEY_DOWNLOAD_THREADS, 2));

        var state = result.future().join();

        /*
         * now we should read both the original and the new file The original is in
         * "src/test/resources" and the download file is in "/tmp"
         */
        InputStream expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/pi0.txt");
        FileInputStream actual = new FileInputStream(findPath(state, "resources/pi0.txt"));

        assertThat(actual).hasSameContentAs(expected);

        expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/pi1.txt");
        actual = new FileInputStream(findPath(state, "resources/pi1.txt"));

        assertThat(actual).hasSameContentAs(expected);

    }

    private String findPath(final ISavedJobState state, final String resourceId) {
        return state.getDownloads().stream()
            .filter(dl -> dl.getResourceId().equals(resourceId))
            .findFirst()
            .map(dl -> dl.getProperties().get("path"))
            .orElseThrow();
    }

}
