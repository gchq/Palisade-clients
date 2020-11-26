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

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    private Configuration defaultConfig;

    @BeforeEach
    void setUp() throws Exception {
        this.defaultConfig = Configuration.from(Map.of());
    }

    @Test
    void testPalisadePortWasSubstituted() {
        assertThat(defaultConfig.get("service.palisade.port", BigDecimal.class))
            .isEqualByComparingTo(new BigDecimal("8081"));
    }

    @Test
    void testFilteredResourcePortWasSubstituted() {
        assertThat(defaultConfig.get("service.filteredResource.port", BigDecimal.class))
            .isEqualByComparingTo(new BigDecimal("8081"));
    }

    @Test
    void testStatePath() {
        assertThat(defaultConfig.getStatePath()).isEqualTo("/tmp/palisade/%t/palisade-state_%t_%s.json");
    }

    @Test
    void testPalisadeUri() {
        assertThat(defaultConfig.getPalisadeUri())
            .isEqualTo("http://localhost:8081/cluster/palisade/registerDataRequest");
    }

    @Test
    void testFilteredResourceUri() {
        assertThat(defaultConfig.getFilteredResourceUri())
            .isEqualTo("ws://localhost:8081/cluster/filteredResource/name/%t");
    }

    @Test
    void testDownloadThreads() {
        assertThat(defaultConfig.getDownloadThreads()).isEqualTo(2);
    }

    @Test
    void testReceiverFilePath() {
        assertThat(defaultConfig.getReceiverFilePath())
            .isEqualTo("/tmp/palisade/%t/downloads/palisade-download_%t_%s_%r");
    }

    @Test
    void testDataPath() {
        assertThat(defaultConfig.getDataPath()).isEqualTo("data/read/chunked");
    }

}
