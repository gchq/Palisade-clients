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
import uk.gov.gchq.palisade.client.job.state.ISavedJobState;
import uk.gov.gchq.palisade.client.util.Configuration;

import javax.inject.Inject;

import java.io.FileInputStream;
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
    void setup() {
        client = Client.create(Map.of(
            Configuration.KEY_SERVICE_PS_PORT, embeddedServer.getPort(),
            Configuration.KEY_SERVICE_FRS_PORT, embeddedServer.getPort(),
            Configuration.KEY_DOWNLOAD_THREADS, 1));
    }

    @Test
    void testFull() throws Exception {

        var jobRequest = IJobRequest.createJobRequest(b -> b
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
        var expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("pi.txt");
        var actual = new FileInputStream(findPath(state, "pi.txt"));

        assertThat(actual).hasSameContentAs(expected);

        expected = Thread.currentThread().getContextClassLoader().getResourceAsStream("Selection_032.png");
        actual = new FileInputStream(findPath(state, "Selection_032.png"));

        assertThat(actual).hasSameContentAs(expected);

    }

    /*
     * Finds the path of a download with the given resourceId by filtering the
     * downloads
     */
    private String findPath(final ISavedJobState state, final String resourceId) {
        return state.getDownloads().stream()
            .filter(dl -> dl.getResourceId().equals(resourceId))
            .findFirst()
            .map(dl -> dl.getProperties().get("path"))
            .orElseThrow();
    }

}
