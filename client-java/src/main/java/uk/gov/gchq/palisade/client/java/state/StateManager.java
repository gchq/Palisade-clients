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
package uk.gov.gchq.palisade.client.java.state;

import java.util.*;
import java.util.function.UnaryOperator;

import com.google.common.eventbus.Subscribe;

import static uk.gov.gchq.palisade.client.java.state.State.builder;

public class StateManager {

    private final Map<String, State> states = new HashMap<>();

    public StateManager() { // noop
    }

    public StateManager set(String token, UnaryOperator<State.Builder> func) {
        var state = find(token)
            .map(s -> func.apply(builder().from(s)).build())
            .orElse(func.apply(builder().token(token)).build());
        return set(state);
    }

    public StateManager set(State state) {
        this.states.put(state.getToken(), state);
        return this;
    }

    public State get(String token) {
        return find(token).orElseThrow(() -> new StateException("Failed to find state for token: " + token));
    }

    public Optional<State> find(String token) {
        return Optional.ofNullable(states.get(token));
    }

    public boolean isEmpty() {
        return states.isEmpty();
    }

    @Subscribe
    public void onStateChange(StateChangeEvent event) {
        var token = event.getToken();
        var state = event.getState();
        states.put(token, IState.create(b -> b.token(token).currentState(state)));
    }
}
