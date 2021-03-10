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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.internal.dft.DefaultClient;
import uk.gov.gchq.palisade.client.util.Checks;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

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
@SuppressWarnings("java:S1774")
public final class ClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientManager.class);

    /**
     * The registered clients. A CopyOnWriteArrayList is used here as it implements
     * the semantics that we are after. We need to guarantee writes, but do
     * not need to lock on reads. As the clients are usually registered at startup
     * and read many times this fits the use case and avoids having to synchronise
     * on reads.
     */
    private static final CopyOnWriteArrayList<Client> REGISTERED_CLIENTS = new CopyOnWriteArrayList<>();
    private static final String PALISADE_CLIENTS_PROPERTY = "palisade.clients";
    private static final Object LOCK_FOR_INIT_CLIENTS = new Object();
    private static volatile boolean clientsInitialized;

    static {
        // add the default client to the palisade.clients system property, appending it
        // if it is already set
        Optional
            .ofNullable(System.getProperty(PALISADE_CLIENTS_PROPERTY, ""))
            .map(prop -> prop.isEmpty() ? "" : (prop + ":"))
            .map(prop -> prop + DefaultClient.class.getName())
            .ifPresent(prop -> System.setProperty(PALISADE_CLIENTS_PROPERTY, prop));
    }

    private ClientManager() { // prevent instantiation
    }

    /**
     * Returns a client for the given palisade URL. The ClientManager attempts to
     * select an appropriate client from the set of registered Clients.
     *
     * @param url a palisade URL of the form pal:subname://host:port/context
     * @return a client for the provided URL
     */
    public static Client getClient(final String url) {
        LOGGER.debug("ClientManager.getClient(\"{}\"", url);
        ensureClientsInitialized();
        return REGISTERED_CLIENTS.stream()
            .filter(c -> c.acceptsURL(url))
            .findFirst()
            .orElseThrow(() -> new ClientException("No suitable client found accepting url: " + url));
    }

    /**
     * Attempts to establish a session to the given Palisade cluster {@code url}.
     *
     * @param url a palisade URL of the form pal:subprotocol:subname
     * @return a session for the provided {@code url}
     * @throws ClientException if a Palisade access error occurs or the URL is
     *                         invalid
     */
    public static Session openSession(final String url) {
        return openSession(url, Map.of());
    }

    /**
     * Attempts to establish a session to the given Palisade cluster {@code url}.
     * The ClientManager attempts to select an appropriate client from the set of
     * registered Palisade clients.
     *
     * @param url  a palisade URL of the form pal:subprotocol:subname
     * @param info a list of arbitrary string tag/value pairs as connection
     *             arguments; normally at least a "user" property should be included
     * @return a session for the provided {@code url}
     * @throws ClientException if a Palisade access error occurs or the URL is
     *                         invalid
     */
    @SuppressWarnings("java:S1488")
    public static Session openSession(final String url, final Map<String, String> info) {

        Checks.checkNotNull(url, "The url cannot be null");

        LOGGER.debug("ClientManager.openSession(\"{}\"", url);

        ensureClientsInitialized();

        var client = getClient(url);
        var session = client.connect(url, info);
        return session;

    }

    /**
     * Registers the given client with the {@code ClientManager}. A newly-loaded
     * client class should call the method {@code registerClient} to make itself
     * known to the {@code ClientManager}. If the client is currently registered, no
     * action is taken.
     *
     * @param client the new Palisade Client that is to be registered with the
     *               {@code ClientManager}
     * @exception NullPointerException if {@code client} is null
     * @since 0.5.0
     */
    public static void registerClient(final Client client) {
        // Register the client if it has not already been added to our list */
        if (client != null) {
            REGISTERED_CLIENTS.addIfAbsent(client);
            LOGGER.debug("ClientManager.registerClient: client registered: {}", client.getClass().getName());
        } else {
            throw new IllegalArgumentException("Cannot register a null client");
        }
    }

    /**
     * Returns a Stream with all of the currently loaded Palisade clients
     *
     * @return the stream of Palisade clients
     */
    public static Stream<Client> getClients() {
        ensureClientsInitialized();
        return REGISTERED_CLIENTS.stream();
    }

    /*
     * Load the initial Palisade clients by checking the System property
     * palisade.clients
     */
    @SuppressWarnings({"java:S2221", "java:S2658" })
    private static void ensureClientsInitialized() {
        if (clientsInitialized) {
            return;
        }
        synchronized (LOCK_FOR_INIT_CLIENTS) {
            if (!clientsInitialized) { // again, in case something squeezed in.
                Optional
                    .ofNullable(System.getProperty(PALISADE_CLIENTS_PROPERTY))
                    .filter(clients -> !"".equals(clients))
                    .ifPresent(clients -> Arrays
                        .stream(clients.split(":"))
                        .filter(client -> !"".equals(client))
                        .forEach(ClientManager::loadClient));
                clientsInitialized = true;
                LOGGER.debug("Palisade ClientManager initialized");
            }
        }

    }

    @SuppressWarnings({"java:S2221", "java:S2658" })
    private static void loadClient(final String client) {
        try {
            LOGGER.debug("ClientManager.initialize: loading {}", client);
            Class.forName(client, true, ClassLoader.getSystemClassLoader());
        } catch (Exception ex) {
            LOGGER.debug("ClientManager.initialize: load failed", ex);
        }
    }
}
