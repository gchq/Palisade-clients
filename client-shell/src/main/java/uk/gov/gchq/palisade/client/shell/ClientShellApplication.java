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

package uk.gov.gchq.palisade.client.shell;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import uk.gov.gchq.palisade.client.internal.dft.DefaultClient;
import uk.gov.gchq.palisade.client.internal.dft.DefaultQueryItem;
import uk.gov.gchq.palisade.client.internal.dft.DefaultQueryResponse;
import uk.gov.gchq.palisade.client.internal.dft.DefaultSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@SpringBootApplication
@ShellComponent
public class ClientShellApplication {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(ClientShellApplication.class)
                .run(args);
    }

    DefaultClient client = new DefaultClient();
    DefaultSession session;

    Map<String, DefaultQueryResponse> registeredQueries;
    Map<String, List<DefaultQueryItem>> filteredResources;
    Map<String, AtomicBoolean> queryComplete;


    // --- Availability Checks --- //

    public Availability openSession() {
        return session == null
                ? Availability.unavailable("Not connected, specify a client connect url with 'connect <url>'")
                : Availability.available();
    }


    // --- Shell Methods --- //

    @ShellMethod(value = "Setup a client to connect to Palisade.", key = {"connect"})
    public String connect(final String url) {
        session = client.connect(url);
        return String.format("Connected to %s", url);
    }

    @ShellMethodAvailability("openSession")
    @ShellMethod(value = "Register a request for resources with Palisade and receive the request token.", key = {"register"})
    public String register(final String resourceId) {
        DefaultQueryResponse response = (DefaultQueryResponse) session.createQuery(resourceId)
                .execute().join();
        String token = response.getPalisadeResponse().getToken();
        registeredQueries.putIfAbsent(token, response);
        return token;
    }

    @ShellMethod(value = "List the tokens of all requests registered so far.", key = {"registered"})
    public List<String> registered() {
        return new ArrayList<>(filteredResources.keySet());
    }

    @ShellMethod(value = "List the resources returned for a request.", key = {"list", "ls"})
    public List<String> list(final String token) {
        if (!filteredResources.containsKey(token)) {
            LinkedList<DefaultQueryItem> filteredResourcesList = new LinkedList<>();
            AtomicBoolean complete = new AtomicBoolean(false);

            registeredQueries.get(token)
                    .stream()
                    .subscribe(new DelegatingSubscriber<>(
                            sub -> {
                                filteredResources.put(token, filteredResourcesList);
                                queryComplete.put(token, complete);
                            },
                            next -> filteredResourcesList.addLast((DefaultQueryItem) next),
                            error -> {
                                throw new RuntimeException(error.getMessage(), error);
                            },
                            () -> complete.set(true)));
        }

        return filteredResources.get(token)
                .stream()
                .map(item -> item.asResource().getId())
                .collect(Collectors.toList());
    }

    @ShellMethod(value = "Read the data from a resource returned for a request.", key = {"read", "cat"})
    public String read(final String token, final String leafResourceId) {
        return filteredResources.get(token)
                .stream()
                .filter(response -> response.asResource().getId().equals(leafResourceId))
                .findAny()
                .map(response -> session.fetch(response).getInputStream())
                .map(ClientShellApplication::dumpInputStream)
                .orElse(null);
    }


    // --- Helper Methods --- //

    private static String dumpInputStream(final InputStream is) {
        try {
            return new String(is.readAllBytes());
        } catch (IOException error) {
            throw new RuntimeException(error.getMessage(), error);
        }
    }
}
