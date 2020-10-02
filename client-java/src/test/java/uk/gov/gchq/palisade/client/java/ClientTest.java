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
package uk.gov.gchq.palisade.client.java;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientTest {

    private static final int PORT = 8081;
    private static final String HOST = "localhost";
    private static final String BASE_URL = String.format("http://%s:%s", HOST, PORT);

    @Test
    void testCreate() {

        var client = (JavaClient) Client.create();

        var syscfg = client.getConfig();

        assertThat(syscfg.getObjectMapper()).isNotNull();
        assertThat(syscfg.getPalisadeClient()).isNotNull();
        assertThat(syscfg.getClientConfig()).isNotNull();
        assertThat(syscfg.getStateManager()).isNotNull();

        var palisadeConfig = syscfg.getClientConfig();

        assertThat(palisadeConfig.getDownloadThreads()).isEqualTo(1);
        assertThat(palisadeConfig.getUrl()).isEqualTo(BASE_URL);

    }

}
