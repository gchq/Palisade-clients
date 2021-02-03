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
package uk.gov.gchq.palisade.client.internal.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.ClientException;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ConfigurationTest {

    private static final String FILENAME = "palisade-client2.yaml";

    private static Configuration configuration;

    @BeforeAll
    static void setupAll() {
        configuration = Configuration
            .fromDefaults(Map.of("service.url", "pal://localhost:8081/cluster?wsport=8082"));
    }

    private Configuration defaultConfig() {
        return Configuration
            .fromDefaults(Map.of("service.url", "pal://localhost:8081/cluster?wsport=8082"));
    }

    @Test
    void testServiceUrl() {
        assertThat(configuration.getServiceUrl())
            .isEqualTo("pal://localhost:8081/cluster?wsport=8082");
    }

    @Test
    void testPalisadeUri() {
        assertThat(configuration.getPalisadeUrl())
            .isEqualTo(URI.create("http://localhost:8081/cluster/palisade/registerDataRequest"));
    }

    @Test
    void testFilteredResourceUri() {
        assertThat(configuration.getFilteredResourceUrl())
            .isEqualTo(URI.create("ws://localhost:8082/cluster/filteredResource/name/%25t"));
    }

    @Test
    void testDataPath() {
        assertThat(configuration.getDataPath()).isEqualTo("data/read/chunked");
    }

    @Test
    void testUserNone() {
        assertThat(configuration.getUser()).as("check no user").isNull();
    }

    @Test
    void testUserFromProperty() {
        var config = Configuration.fromDefaults(Map.of(
            "service.url", "pal://localhost:8081/cluster?wsport=8082",
            "service.user", "user_from_property"));
        assertThat(config.getUser()).as("check user from property").isEqualTo("user_from_property");
    }

    @Test
    void testUserFromQueryParam() {
        var config = Configuration.from(FILENAME, Map.of(
            "service.url", "pal://localhost:8081/cluster?wsport=8082&user=user_from_param",
            "service.user", "user_from_property"));
        assertThat(config.getUser()).as("check user from query param").isEqualTo("user_from_param");
    }

    @Test
    void testUserFromAuthority() {
        var config = Configuration.fromDefaults(Map.of(
            "service.url", "pal://user_from_authority@localhost:8081/cluster?wsport=8082"));
        assertThat(config.getUser()).as("check user from authority").isEqualTo("user_from_authority");
    }

    @Test
    void testLoadFilenameNotFound() {
        assertThatExceptionOfType(ClientException.class).isThrownBy(() -> Configuration.from("doesnotexist"));
    }

    @Test
    void testLoadFilenameNull() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Configuration.from(null));
    }

    @Test
    void testInvalidServiceUrl() {
        assertThatExceptionOfType(ClientException.class).isThrownBy(
            () -> Configuration.fromDefaults(Map.of("service.url", "\\")));
        assertThatExceptionOfType(ClientException.class).isThrownBy(
            () -> Configuration.from(FILENAME, Map.of("service.url", "\\")));
    }

    @Test
    void testMergeNullSame() {
        var expected = configuration;
        var actual = expected.merge(null);
        assertThat(actual).isSameAs(expected);
    }
}
