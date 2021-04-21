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

import uk.gov.gchq.palisade.client.internal.dft.DefaultClient;
import uk.gov.gchq.palisade.client.internal.dft.DefaultQueryItem;
import uk.gov.gchq.palisade.client.internal.dft.DefaultQueryResponse;
import uk.gov.gchq.palisade.client.internal.dft.DefaultSession;
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

@ShellComponent
public class ClientShell {
    private static final String DESELECT = "..";

    protected final AtomicReference<DefaultSession> sessionState = new AtomicReference<>();
    protected final AtomicReference<String> tokenState = new AtomicReference<>();
    protected final Map<String, DefaultQueryResponse> registeredQueries = new HashMap<>();
    protected final Map<String, List<DefaultQueryItem>> filteredResources = new HashMap<>();

    private final DefaultClient client;

    public ClientShell(final DefaultClient client, final Map<String, DefaultQueryResponse> initialQueryState, final Map<String, List<DefaultQueryItem>> initialResourceState) {
        this.client = client;
        this.registeredQueries.putAll(initialQueryState);
        this.filteredResources.putAll(initialResourceState);
    }

    @Autowired
    public ClientShell(final DefaultClient client) {
        this(client, Map.of(), Map.of());
    }

    // --- Internal State Accessors --- //

    public DefaultSession getSessionState() {
        return sessionState.get();
    }

    public String getTokenState() {
        return tokenState.get();
    }


    // --- Availability Checks --- //

    public Availability openSession() {
        return sessionState.get() == null
            ? Availability.unavailable("Not connected, specify a client connect url with 'connect <url>'")
            : Availability.available();
    }

    public Availability selectedToken() {
        return tokenState.get() == null
            ? Availability.unavailable("No selected token, specify a token with 'cd <token>'")
            : Availability.available();
    }


    // --- Shell Methods --- //

    @ShellMethod(value = "Setup a client to connect to Palisade.", key = {"connect"})
    public String connect(final String url) {
        if (!client.acceptsURL(url)) {
            throw new IllegalArgumentException("Client does not accept url: " + url);
        }
        sessionState.set(client.connect(url));
        return String.format("Connected to %s", url);
    }

    @ShellMethodAvailability("openSession")
    @ShellMethod(value = "Register a request for resources with Palisade and receive the request token.", key = {"register"})
    public String register(final String[] context, final String resourceId) {
        Map<String, String> contextMap = Arrays.stream(context)
            .map(entry -> entry.split("=", 2))
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

    @ShellMethod(value = "Select a token for a given request, such that further commands are relative to this token ('..' for no token).", key = {"select", "cd"})
    public String select(@ShellOption(defaultValue = "..") final String token) {
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
        filteredResources.computeIfAbsent(token, tk -> {
            LinkedList<DefaultQueryItem> resources = new LinkedList<>();
            Flowable.fromPublisher(FlowAdapters.toPublisher(registeredQueries.get(token).stream()))
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
