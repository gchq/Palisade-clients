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

/**
 * The state manager maintains the state of each job via it's unique token that
 * was returned from the initial request to Palisade.
 *
 * @author dbell
 * @since 0.5.0
 */
public class StateManager {

    private final Map<String, State> states = new HashMap<>();

    /**
     * Create a new statemanager
     */
    public StateManager() { // noop
    }

    /**
     * Sets a new state. If the state is exists a new state will be created with the
     * new values mapped via the provided builder function or if no state is found,
     * a new default state is created and set.
     *
     * @param token The token
     * @param func  The builder function
     * @return this for method chaining
     */
    public StateManager set(String token, UnaryOperator<State.Builder> func) {
        var state = find(token)
            .map(s -> func.apply(builder().from(s)).build())
            .orElse(func.apply(builder().token(token)).build());
        return set(state);
    }

    /**
     * Sets the new state
     *
     * @param state The new state to set
     * @return this for fluent usage
     */
    public StateManager set(State state) {
        this.states.put(state.getToken(), state);
        return this;
    }

    /**
     * Returns the state for the provided token throwing a {@link StateException} if
     * none is found.
     *
     * @param token The token
     * @return the state for the provided token throwing a {@link StateException} if
     *         none is found.
     */
    public State get(String token) {
        return find(token).orElseThrow(() -> new StateException("Failed to find state for token: " + token));
    }

    /**
     * Returns an optional containing the state for the provided token or empty if
     * none found
     *
     * @param token The token
     * @return Optional containing the state for the provided token or enmpty if not
     *         found.
     */
    public Optional<State> find(String token) {
        return Optional.ofNullable(states.get(token));
    }

    /**
     * Returns true there are no states
     *
     * @return true there are no states
     */
    public boolean isEmpty() {
        return states.isEmpty();
    }

    /**
     * Handles the state changed event
     *
     * @param event The event
     */
    @Subscribe
    public void onStateChange(StateChangeEvent event) {
        var token = event.getToken();
        var state = event.getState();
        states.put(token, IState.create(b -> b.token(token).currentState(state)));
    }
}
