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
package uk.gov.gchq.palisade.client;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.internal.dft.DefaultClient;
import uk.gov.gchq.palisade.client.internal.dft.DefaultSession;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.ClientManager.getClient;
import static uk.gov.gchq.palisade.client.ClientManager.openSession;

class ClientManagerTest {

    @Test
    void testGetClient() {
        assertThat(getClient("pal://alice@localhost:1234/cluster"))
            .as("Client is of correct type")
            .isInstanceOf(DefaultClient.class);
        assertThat(getClient("pal:dft://alice@localhost:1234/cluster"))
            .as("Client is of correct type")
            .isInstanceOf(DefaultClient.class);
    }

    @Test
    void testOpenSessionUrlNoProperties() {

        var serviceUrl = "pal://localhost:1234/cluster?userid=alice";
        var dftSession = (DefaultSession) openSession(serviceUrl);
        var configuration = dftSession.getConfiguration();

        assertThat(configuration.getServiceUrl())
            .as("check configuration Service URL")
            .isEqualTo(serviceUrl);
        assertThat(configuration.getPalisadeUrl())
            .as("check generated Palisade URI")
            .isEqualTo(URI.create("http://localhost:1234/cluster/palisade/api/registerDataRequest"));
        assertThat(configuration.getFilteredResourceUrl())
            .as("check generated FilteredResource URI")
            .isEqualTo(URI.create("ws://localhost:1234/cluster/resource/%25t"));

    }

    @Test
    void testOpenSessionWithUrlAndProperties() {

        var port = 1234;
        var serviceUrl = "pal://localhost/cluster?userid=alice";
        var properties = Map.of(
            "service.palisade.port", "" + port,
            "service.filteredResource.port", "" + port);

        var dftSession = (DefaultSession) openSession(serviceUrl, properties);
        var configuration = dftSession.getConfiguration();

        assertThat(configuration.getServiceUrl())
            .as("Configuration Service URL")
            .isEqualTo(serviceUrl);
        assertThat(configuration.getPalisadeUrl())
            .as("Generated Palisade URI")
            .isEqualTo(URI.create("http://localhost:1234/cluster/palisade/api/registerDataRequest"));
        assertThat(configuration.getFilteredResourceUrl())
            .as("Generated FilteredResource URI")
            .isEqualTo(URI.create("ws://localhost:1234/cluster/resource/%25t"));

    }

}
