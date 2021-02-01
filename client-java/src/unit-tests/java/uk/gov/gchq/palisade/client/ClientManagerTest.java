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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClientManagerTest {

    @Test
    void testGetClient() {
        var client = ClientManager.getClient("pal://mrblobby@localhost:1234/cluster");
        assertThat(client).isInstanceOf(DefaultClient.class);
        client = ClientManager.getClient("pal:dft://mrblobby@localhost:1234/cluster");
        assertThat(client).isInstanceOf(DefaultClient.class);
    }

    @Test
    void testOpenSessionUrlNoProperties() {

        var serviceUrl = "pal://mrblobby@localhost:1234/cluster";

        var session = ClientManager.openSession(serviceUrl);
        var dftSession = (DefaultSession) session;

        var configuration = dftSession.getConfiguration();

        assertThat(configuration.getServiceUrl()).isEqualTo(serviceUrl);
        assertThat(configuration.getPalisadeUrl())
            .isEqualTo("http://localhost:1234/cluster/palisade/registerDataRequest");
        assertThat(configuration.getFilteredResourceUrl())
            .isEqualTo("ws://localhost:1234/cluster/filteredResource/name/%t");
    }

    @Test
    void testOpenSessionWithUrlAndProperties() {

        var port = 1234;
        var serviceUrl = "pal://mrblobby@localhost/cluster";

        var properties = Map.of(
            "service.palisade.port", "" + port,
            "service.filteredResource.port", "" + port);

        var session = ClientManager.openSession(serviceUrl, properties);
        var dftSession = (DefaultSession) session;

        var configuration = dftSession.getConfiguration();

        assertThat(configuration.getServiceUrl()).isEqualTo(serviceUrl);
        assertThat(configuration.getPalisadeUrl())
            .isEqualTo("http://localhost:1234/cluster/palisade/registerDataRequest");
        assertThat(configuration.getFilteredResourceUrl())
            .isEqualTo("ws://localhost:1234/cluster/filteredResource/name/%t");

    }

}
