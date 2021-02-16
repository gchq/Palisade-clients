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

    private static final String FILENAME = "palisade-client.yaml";

    private static Configuration configuration;

    @BeforeAll
    static void setupAll() {
        configuration = Configuration
            .create(Map.of("service.url", "pal://eve@localhost:8081/cluster?userid=alice&wsport=8082"));
    }

    @Test
    void testServiceUrl() {
        assertThat(configuration.getServiceUrl())
            .as("Generated main service URL is correct")
            .isEqualTo("pal://eve@localhost:8081/cluster?userid=alice&wsport=8082");
    }

    @Test
    void testGetPalisadeUri() {
        assertThat(configuration.getPalisadeUrl())
            .as("Generated Palisade Service URL is configured correctly")
            .isEqualTo(URI.create("http://eve@localhost:8081/cluster/palisade/registerDataRequest"));
    }

    @Test
    void testFilteredResourceUri() {
        assertThat(configuration.getFilteredResourceUrl())
            .as("Generated Filtered Resource Service is generated correctly")
            .isEqualTo(URI.create("ws://eve@localhost:8082/cluster/resource/%25t"));
    }

    @Test
    void testDataPath() {
        assertThat(configuration.getDataPath())
            .as("Check the Data Service URI path")
            .isEqualTo("read/chunked");
    }

    @Test
    void testUserNone() {
        assertThatExceptionOfType(ConfigurationException.class)
            .as("Check no user is configured")
            .isThrownBy(() -> Configuration.create(Map.of("service.url", "pal://localhost:8081/cluster?wsport=8082")));
    }

    @Test
    void testUserFromProperty() {
        var config = Configuration.create(Map.of(
            "service.userid", "user_from_property",
            "service.url", "pal://localhost:8081/cluster?wsport=8082",
            "service.user", "user_from_property"));
        assertThat(config.getUser())
            .as("check user from property")
            .isEqualTo("user_from_property");
    }

    @Test
    void testUserFromQueryParam() {
        var config = Configuration.create(Map.of(
            "service.url", "pal://localhost:8081/cluster?wsport=8082&userid=user_from_param",
            "service.user", "user_from_property"));
        assertThat(config.getUser())
            .as("check user from query param")
            .isEqualTo("user_from_param");
    }

    @Test
    void testInvalidServiceUrl() {
        assertThatExceptionOfType(ClientException.class)
            .as("Configuration has valid service URL")
            .isThrownBy(() -> Configuration.create(Map.of("service.url", "\\")));
    }

    @Test
    void testToString() {

        var expected =
            "configuration: {" + "\n" +
            "  service.data.path=read/chunked" + "\n" +
            "  service.filteredResource.path=resource/%t" + "\n" +
            "  service.filteredResource.port=8082" + "\n" +
            "  service.filteredResource.uri=ws://eve@localhost:8082/cluster/resource/%25t" + "\n" +
            "  service.palisade.path=palisade/registerDataRequest" + "\n" +
            "  service.palisade.port=8081" + "\n" +
            "  service.palisade.uri=http://eve@localhost:8081/cluster/palisade/registerDataRequest" + "\n" +
            "  service.url=pal://eve@localhost:8081/cluster?userid=alice&wsport=8082" + "\n" +
            "  service.userid=alice" + "\n" +
            "}";

        assertThat(configuration.toString())
            .as("Configuration toString() is correct")
            .isEqualTo(expected);

    }
}
