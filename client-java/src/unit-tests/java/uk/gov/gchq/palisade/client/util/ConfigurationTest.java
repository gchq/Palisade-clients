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
package uk.gov.gchq.palisade.client.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    private Configuration context;

    @BeforeEach
    void setUp() throws Exception {
        this.context = Configuration.from(Map.of());
    }

    @Test
    void testStatePath() {
        assertThat(context.getStatePath()).isEqualTo("/tmp");
    }

    @Test
    void testPalisadeUri() {
        assertThat(context.getPalisadeUri()).isEqualTo("http://localhost:8081/cluster/palisade/registerDataRequest");
    }

    @Test
    void testFilteredResourceUri() {
        assertThat(context.getFilteredResourceUri())
            .isEqualTo("ws://localhost:8081/cluster/filteredResource/name");
    }

    @Test
    void testDownloadThreads() {
        assertThat(context.getDownloadThreads()).isEqualTo(2);
    }

    @Test
    void testReceiverFilePath() {
        assertThat(context.getReceiverFilePath()).isEqualTo("/tmp");
    }

    @Test
    void testDataPath() {
        assertThat(context.getDataPath()).isEqualTo("data/read/chunked");
    }

}
