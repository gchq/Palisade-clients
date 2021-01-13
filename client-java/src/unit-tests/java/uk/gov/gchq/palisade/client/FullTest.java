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

import uk.gov.gchq.palisade.client.job.state.JobRequest;
import uk.gov.gchq.palisade.client.job.state.SavedJobState;
import uk.gov.gchq.palisade.client.util.Configuration;

import javax.inject.Inject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @since 0.5.0
 */
@MicronautTest
class FullTest {

    @Inject
    EmbeddedServer embeddedServer;

    private Client client;

    @BeforeEach
    void setup() {
        client = Client.create(Map.of(
            Configuration.KEY_SERVICE_PS_PORT, embeddedServer.getPort(),
            Configuration.KEY_SERVICE_FRS_PORT, embeddedServer.getPort(),
            Configuration.KEY_DOWNLOAD_THREADS, 1));
    }

    @Test
    void testFull() throws Exception {

        var jobRequest = JobRequest.createJobRequest(b -> b
            .userId("user_id")
            .resourceId("pi.txt")
            .purpose("purpose"));

        // lets start the ball rolling
        var result = client.submit(jobRequest);
        var state = result.future().join();

        assertDownloaded(state, "resources/pi0.txt");
        assertDownloaded(state, "resources/pi1.txt");
        assertDownloaded(state, "resources/pi2.txt");
        assertDownloaded(state, "resources/pi3.txt");
        assertDownloaded(state, "resources/pi4.txt");
        assertDownloaded(state, "resources/pi5.txt");
        assertDownloaded(state, "resources/pi6.txt");
        assertDownloaded(state, "resources/pi7.txt");
        assertDownloaded(state, "resources/pi8.txt");
        assertDownloaded(state, "resources/pi9.txt");

    }

    private void assertDownloaded(final SavedJobState state, final String resourceId) throws FileNotFoundException {

        var expected = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceId);
        var actual = new FileInputStream(findPath(state, resourceId));

        assertThat(actual).hasSameContentAs(expected);

    }

    /*
     * Finds the path of a download with the given resourceId by filtering the
     * downloads
     */
    private String findPath(final SavedJobState state, final String resourceId) {
        return state.getDownloads().stream()
            .filter(dl -> dl.getResourceId().equals(resourceId))
            .findFirst()
            .map(dl -> dl.getProperties().get("path"))
            .orElseThrow();
    }

}
