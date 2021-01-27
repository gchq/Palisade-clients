/*
 * Copyright 2020-2021 Crown Copyright
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
package uk.gov.gchq.palisade.client.abc.impl;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ConfigurationTest {

    private static final String FILENAME = "palisade-client2.yaml";


    private Configuration defaultConfig() {
        return Configuration
            .fromDefaults(Map.of("service.url", "pal://localhost:8081/cluster?wsport=8082"));
    }

    @Test
    void testServiceUrl() {
        assertThat(defaultConfig().getServiceUrl())
            .isEqualTo("pal://localhost:8081/cluster?wsport=8082");
    }

    @Test
    void testPalisadeUri() {
        assertThat(defaultConfig().getPalisadeUrl())
            .isEqualTo("http://localhost:8081/cluster/palisade/registerDataRequest");
    }

    @Test
    void testFilteredResourceUri() {
        assertThat(defaultConfig().getFilteredResourceUrl())
            .isEqualTo("ws://localhost:8082/cluster/filteredResource/name/%t");
    }

    @Test
    void testDataPath() {
        assertThat(defaultConfig().getDataPath()).isEqualTo("data/read/chunked");
    }

    @Test
    void testUserNone() {
        assertThat(defaultConfig().getUser()).as("check no user").isNull();
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
            "service.url", "pal://user_from_authority@localhost:8081/cluster?wsport=8082,user=user_from_param",
            "service.user", "user_from_property"));
        assertThat(config.getUser()).as("check user from authority").isEqualTo("user_from_authority");
    }

    @Test
    void testLoadFilenameNotFound() {
        assertThatExceptionOfType(ClientException.class).isThrownBy(() -> Configuration.from("doesnotexist"));
    }

    @Test
    void testLoadFilenameNull() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Configuration.from((String) null));
    }

    @Test
    void testInvalidServiceUrl() {
        assertThatExceptionOfType(ClientException.class).isThrownBy(
            () -> Configuration.fromDefaults(Map.of("service.url", "\\")));
        assertThatExceptionOfType(ClientException.class).isThrownBy(
            () -> Configuration.from(FILENAME, Map.of("service.url", "\\")));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void testMergeNullSame() {
        var expected = defaultConfig();
        var actual = expected.merge((Map) null);
        assertThat(actual).isSameAs(expected);
    }
}
