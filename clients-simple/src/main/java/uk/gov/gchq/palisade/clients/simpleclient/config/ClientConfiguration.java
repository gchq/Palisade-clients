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
package uk.gov.gchq.palisade.clients.simpleclient.config;

import uk.gov.gchq.palisade.Generated;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * ClientConfiguration is used by the UrlDataClient to store a map of serviceName - urls used in the Feign {@link feign.Feign.Builder}, i.e data-service, localhost:8082
 */
public class ClientConfiguration {

    private Map<String, String> client;

    /**
     * Gets client map of String, String
     *
     * @return the client
     */
    @Generated
    public Map<String, String> getClient() {
        return client;
    }

    /**
     * Sets client map of String, String.
     *
     * @param client the client
     */
    @Generated
    public void setClient(final Map<String, String> client) {
        requireNonNull(client);
        this.client = client;
    }
}
