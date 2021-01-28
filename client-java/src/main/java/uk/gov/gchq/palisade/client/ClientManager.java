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
package uk.gov.gchq.palisade.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.internal.dft.DefaultClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The basic service for managing Palisade clients
 * <ul>
 * <li>pal://host:port - standard client (which using http for PS and FRS</li>
 * <li>pal:dft://host:port - standard client (which using http for PS and
 * FRS</li>
 * <li>pal:alt://host:port - alternative client</li>
 * </ul>
 *
 * @since 0.5.0
 */
public final class ClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientManager.class);
    private static final List<Client> CLIENTS = new ArrayList<>();

    static {
        // adds the default client which responds to "pal:dft:" urls
        // note that this client will also respond to "pal:" to keep things simple.
        CLIENTS.add(new DefaultClient());
    }

    private ClientManager() { // prevent instantiation
    }

    /**
     * Returns a client for the given palisade url. The ClientManager attempts to
     * select an appropriate client from the set of registered Clients.
     *
     * @param url a palisade url of the form pal:subname://host:port/context
     * @return a client for the provided URL
     */
    public static Client getClient(final String url) {
        for (Client client : CLIENTS) {
            if (client.acceptsURL(url)) {
                // Success!
                LOGGER.debug("getClient returning {}", client.getClass().getName());
                return (client);
            }
        }
        throw new ClientException("No suitable client");
    }

    /**
     * Attempts to establish a session to the given Palisade cluster {@code url}.
     *
     * @param url a palisade url of the form pal:subprotocol:subname
     * @return a session for the provided {@code url}
     * @throws ClientException if a Palisade access error occurs or the url is
     *                         invalid
     */
    public static Session openSession(final String url) {
        var client = getClient(url);
        return client.connect(url, Map.of());
    }

    /**
     * Attempts to establish a session to the given Palisade cluster {@code url}.
     * The ClientManager attempts to select an appropriate client from the set of
     * registered Palisade clients.
     *
     * @param url  a palisade url of the form pal:subprotocol:subname
     * @param info a list of arbitrary string tag/value pairs as connection
     *             arguments; normally at least a "user" property should be included
     * @return a session for the provided {@code url}
     * @throws ClientException if a Palisade access error occurs or the url is
     *                         invalid
     */
    public static Session openSession(final String url, final Map<String, String> info) {
        var client = getClient(url);
        return client.connect(url, info);
    }

}
