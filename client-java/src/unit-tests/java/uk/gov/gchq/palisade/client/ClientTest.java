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

import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.util.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClientTest {

    private static final String HOST = "clusterhost";
    private static final String HTTP = "http";
    private static final int NUM_THREADS = 16;
    private static final String FILE_PATH = "/media";

    @Test
    void testCreateWithProperties() {

        var client = (JavaClient) Client.create(Map.<String, Object>of(
            Configuration.KEY_SERVICE_HOST, HOST,
            Configuration.KEY_SERVICE_PS_SCHEME, HTTP,
            Configuration.KEY_DOWNLOAD_THREADS, NUM_THREADS,
            Configuration.KEY_RECEIVER_FILE_PATH, FILE_PATH,
            "my.unkown", "boogie"));

        var properties = client.getConfiguration().getProperties();

        assertThat(properties).containsAllEntriesOf(Map.of(
            Configuration.KEY_SERVICE_HOST, HOST,
            Configuration.KEY_SERVICE_PS_SCHEME, HTTP,
            Configuration.KEY_DOWNLOAD_THREADS, NUM_THREADS,
            Configuration.KEY_RECEIVER_FILE_PATH, FILE_PATH,
            "my.unkown", "boogie"));

    }

    @Test
    void testCreateWithNoProperties() {

        var client = (JavaClient) Client.create();

        var properties = client.getConfiguration().getProperties();

        assertThat(properties).containsAllEntriesOf(Map.of(
            Configuration.KEY_SERVICE_HOST, "localhost",
            Configuration.KEY_SERVICE_PS_SCHEME, "http",
            Configuration.KEY_RECEIVER_FILE_PATH, "/tmp/palisade/%t/downloads/palisade-download_%t_%s_%r"));

    }

}
