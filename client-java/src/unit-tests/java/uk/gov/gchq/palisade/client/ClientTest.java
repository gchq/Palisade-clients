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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClientTest {

    private static final int PORT = 8083;
    private static final String HOST = "localhost";
    private static final String BASE_URL = String.format("http://%s:%s", HOST, PORT);
    private static final String NUM_THREADS = "" + Runtime.getRuntime().availableProcessors();

    @Test
    void testCreateWithProperties() {

        var client = (JavaClient) Client.create(Map.of(
            "service.url", BASE_URL,
            "download.threads", "2",
            "receiver.file.path", "/media",
            "my.unkown", "boogie"));

        var properties = client.getProperties();

        assertThat(properties).containsAllEntriesOf(Map.of(
            "service.url", "http://localhost:8083",
            "download.threads", "2",
            "receiver.file.path", "/media",
            "my.unkown", "boogie"));

        properties = client.getReceiverProperties();

        assertThat(properties).containsAllEntriesOf(Map.of(
            "receiver.file.path", "/media"));

    }

    @Test
    void testCreateWithNoProperties() {

        var client = (JavaClient) Client.create();

        var properties = client.getProperties();

        assertThat(properties).containsAllEntriesOf(Map.of(
            "service.url", "http://localhost:8081",
            "download.threads", "" + NUM_THREADS,
            "receiver.file.path", "/tmp"));

        properties = client.getReceiverProperties();

        assertThat(properties).containsAllEntriesOf(Map.of(
            "receiver.file.path", "/tmp"));

    }

}
