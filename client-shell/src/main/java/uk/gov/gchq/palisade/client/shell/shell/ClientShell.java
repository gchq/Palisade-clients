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

package uk.gov.gchq.palisade.client.shell.shell;

import io.reactivex.rxjava3.core.Flowable;
import org.reactivestreams.FlowAdapters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import uk.gov.gchq.palisade.client.java.internal.dft.DefaultClient;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultQueryItem;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultQueryResponse;
import uk.gov.gchq.palisade.client.java.internal.dft.DefaultSession;
import uk.gov.gchq.palisade.client.shell.exception.RuntimeIOException;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring component for the Shell CLI.
 * All {@link ShellMethod} annotated functions are exposed as CLI commands.
 */
@ShellComponent
public class ClientShell {
    private static final String DESELECT = "..";
    private static final String KEY_VALUE_SEP = "=";

    protected final AtomicReference<DefaultSession> sessionState = new AtomicReference<>();
    protected final AtomicReference<String> tokenState = new AtomicReference<>();
    protected final Map<String, DefaultQueryResponse> registeredQueries = new HashMap<>();
    protected final Map<String, List<DefaultQueryItem>> filteredResources = new HashMap<>();

    private final DefaultClient client;

    /**
     * Create a new ClientShell and explicitly define the internal state from previous requests.
     *
     * @param client               the client implementation to use to connect to Palisade
     * @param initialQueryState    a map from tokens to previous Palisade Service responses
     * @param initialResourceState a map from tokens to previous Filtered-Resource Service responses
     */
    public ClientShell(final DefaultClient client, final Map<String, DefaultQueryResponse> initialQueryState, final Map<String, List<DefaultQueryItem>> initialResourceState) {
        this.client = client;
        this.registeredQueries.putAll(initialQueryState);
        this.filteredResources.putAll(initialResourceState);
    }

    /**
     * Create a new ClientShell with no previous session state.
     *
     * @param client the client implementation to use to connect to Palisade
     */
    @Autowired
    public ClientShell(final DefaultClient client) {
        this(client, Map.of(), Map.of());
    }

    // --- Internal State Accessors --- //

    /**
     * Get the selected session, if there is one, from a 'connect' command.
     *
     * @return the current selected session state of the client, or null
     */
    public DefaultSession getSessionState() {
        return sessionState.get();
    }

    /**
     * Get the selected token, if there is one, from a 'cd' or 'select' command.
     *
     * @return the current selected token state of the client, or null
     */
    public String getTokenState() {
        return tokenState.get();
    }


    // --- Availability Checks --- //

    /**
     * Declare the availability of some commands depending upon whether the user
     * has run a 'connect pal:some/url' command. If they have not, disallow the
     * 'register' command and others.
     *
     * @return {@link Availability#available()} if there is a session
     */
    public Availability openSession() {
        if (sessionState.get() == null) {
            return Availability.unavailable("Not connected, specify a client connect url with 'connect <url>'");
        } else {
            return Availability.available();
        }
    }


    // --- Shell Methods --- //

    /**
     * Connect to an instance of Palisade.
     * This doesn't actually make any network requests, just configures the client.
     *
     * @param url the client uri config string
     * @return some logging output for the user of the client
     */
    @ShellMethod(value = "Setup a client to connect to Palisade.", key = {"connect"})
    public String connect(final String url) {
        if (!client.acceptsURL(url)) {
            throw new IllegalArgumentException("Client does not accept url: " + url);
        }
        sessionState.set(client.connect(url));
        return String.format("Connected to %s", url);
    }

    /**
     * Send a register request to the configured Palisade Service.
     *
     * @param context    the user-defined context 'map' (comma-separated entries, equals-separated key/values)
     * @param resourceId the requested resource
     * @return some logging output for the user of the client
     */
    @ShellMethodAvailability("openSession")
    @ShellMethod(value = "Register a request for resources with Palisade and receive the request token.", key = {"register"})
    public String register(final String[] context, final String resourceId) {
        Map<String, String> contextMap = Arrays.stream(context)
            .map(entry -> entry.split(KEY_VALUE_SEP, 2))
            .map(entry -> new SimpleImmutableEntry<>(entry[0], entry[1]))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        DefaultQueryResponse response = (DefaultQueryResponse) sessionState.get()
            .createQuery(resourceId, contextMap)
            .execute()
            .join();
        String token = response.getPalisadeResponse()
            .getToken();

        registeredQueries.putIfAbsent(token, response);

        return token;
    }

    /**
     * List the internal state of either registered requests, or filtered resources.
     *
     * @param token if the user explicitly selected a specific token to ls for,
     *              otherwise the tokenState, otherwise all tokens.
     * @return some logging output for the user of the client
     */
    @ShellMethod(value = "List the resources returned for a request, or the tokens for each request.", key = {"list", "ls"})
    public String list(@Nullable @ShellOption(defaultValue = ShellOption.NULL) final String token) {
        String selectedToken = Optional.ofNullable(token)
            .orElse(this.tokenState.get());

        Stream<String> lines;

        if (selectedToken != null) {
            this.computeResourcesIfAbsent(selectedToken);
            lines = filteredResources.get(selectedToken)
                .stream()
                .map(item -> item.asResource().getId());
        } else {
            lines = registeredQueries.keySet()
                .stream();
        }

        return lines.collect(Collectors.joining("\n"));
    }

    /**
     * 'Change-directory' and selected a specific token for future requests.
     * Sets the tokenState.
     *
     * @param token the selected token, or '..' to deselect this token
     * @return some logging output for the user of the client
     */
    @ShellMethod(value = "Select a token for a given request, such that further commands are relative to this token ('..' for no token).", key = {"select", "cd"})
    public String select(@ShellOption(defaultValue = DESELECT) final String token) {
        if (token.equals(DESELECT)) {
            tokenState.set(null);
            return "Deselected token";
        } else {
            tokenState.set(registeredQueries.keySet()
                .stream()
                .filter(token::equals)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such registered token: " + token)));
            return "Selected " + token;
        }
    }

    /**
     * Read a resource from the Data Service.
     *
     * @param leafResourceId the requested leafResource
     * @param token          the token for this resource and its request
     * @return some logging output for the user of the client
     */
    @ShellMethod(value = "Read the data from a resource returned for a request.", key = {"read", "cat"})
    public String read(final String leafResourceId, @Nullable @ShellOption(defaultValue = ShellOption.NULL) final String token) {
        String selectedToken = Optional.ofNullable(token)
            .or(() -> Optional.ofNullable(this.tokenState.get()))
            .orElseThrow(() -> new IllegalArgumentException("No token selected"));

        this.computeResourcesIfAbsent(selectedToken);

        return filteredResources.get(selectedToken)
            .stream()
            .filter(response -> response.asResource().getId().equals(leafResourceId))
            .findAny()
            .map(response -> sessionState.get()
                .fetch(response)
                .getInputStream())
            .map(ClientShell::dumpInputStream)
            .orElseThrow(() -> new IllegalArgumentException("No such received resource: " + leafResourceId));
    }


    // --- Helper Methods --- //

    private void computeResourcesIfAbsent(final String token) {
        filteredResources.computeIfAbsent(token, (String tk) -> {
            LinkedList<DefaultQueryItem> resources = new LinkedList<>();
            DefaultQueryResponse query = Optional.ofNullable(registeredQueries.get(token))
                .orElseThrow(() -> new IllegalArgumentException("No such registered token: " + token));
            Flowable.fromPublisher(FlowAdapters.toPublisher(query.stream()))
                .blockingForEach(next -> resources.addLast((DefaultQueryItem) next));
            return resources;
        });
    }

    private static String dumpInputStream(final InputStream is) {
        try {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeIOException("Failed to read all bytes from input stream", e);
        }
    }
}
