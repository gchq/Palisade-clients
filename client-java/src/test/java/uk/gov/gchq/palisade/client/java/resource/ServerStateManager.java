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
package uk.gov.gchq.palisade.client.java.resource;

import org.slf4j.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerStateManager {

    private static final Logger log = LoggerFactory.getLogger(ServerStateManager.class);

    // Redis?
    Map<String, ServerState> states = new ConcurrentHashMap<>(0);

    public ServerStateManager() {
        // TODO Auto-generated constructor stub
    }

    public ServerStateManager set(ServerState state) {
        // TODO: test previous and current state
        this.states.put(state.getToken(), state);
        log.debug("New state: " + state);
        return this;
    }

    public ServerState get(String token) {
        return find(token).orElseThrow(() -> new RuntimeException("Failed to find state for token: " + token));
    }

    public Optional<ServerState> find(String token) {
        return Optional.ofNullable(states.get(token));
    }

    public void remove(String token) {
        states.remove(token);
    }

}
