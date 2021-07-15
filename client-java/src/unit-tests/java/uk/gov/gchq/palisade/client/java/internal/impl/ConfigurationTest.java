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
package uk.gov.gchq.palisade.client.java.internal.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ConfigurationTest {

    private static Configuration configuration;

    @BeforeAll
    static void setupAll() {
        configuration = Configuration.create("pal://eve@localhost:8081/cluster?userid=alice");
    }

    @Test
    void testGetPalisadeUri() {
        assertThat(configuration.<URI>get(Configuration.PALISADE_URI))
                .as("check generated Palisade Service URI")
                .isEqualTo(URI.create("http://eve@localhost:8081/cluster/palisade/api/registerDataRequest"));
    }

    @Test
    void testFilteredResourceUri() {
        assertThat(configuration.<URI>get(Configuration.FILTERED_RESOURCE_URI))
                .as("check generated Filtered Resource Service URI")
                .isEqualTo(URI.create("ws://eve@localhost:8081/cluster/filteredResource/resource/%25t"));
    }

    @Test
    void testDataPath() {
        assertThat(configuration.<String>get(Configuration.DATA_PATH))
                .as("check Data Service path")
                .isEqualTo("/read/chunked");
    }

    @Test
    void testUserNone() {
        var incompleteConfig = Configuration.create("pal://localhost:8081/cluster");
        assertThatExceptionOfType(ConfigurationException.class)
                .as("check no user is configured")
                .isThrownBy(() -> incompleteConfig.get(Configuration.USER_ID));
    }

    @Test
    void testUserId() {
        assertThat(configuration.<String>get(Configuration.USER_ID))
                .as("check user from query param")
                .isEqualTo("alice");
    }

    @Test
    void testInvalidServiceUrl() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("check Configuration validates service URL")
                .isThrownBy(() -> Configuration.create("\\"));
    }
}
