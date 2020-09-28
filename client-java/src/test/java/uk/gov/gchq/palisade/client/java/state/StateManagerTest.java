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

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class StateManagerTest {

    private static final String TOKEN = "abcd-1";
    private static final State originalState = IState.create(b -> b.currentState(StateType.WAITING).token(TOKEN));

    private StateManager sm;

    @BeforeEach
    void setup() {
        this.sm = new StateManager();
    }

    @Test
    void testGetFails() {
        assertThatExceptionOfType(StateException.class).isThrownBy(() -> sm.get("blah"));
    }

    @Test
    void testSetState() {
        var state = sm.set(originalState).get(TOKEN);
        assertThat(state).isSameAs(originalState);
    }

    @Test
    void testFindState() {
        sm.set(originalState);
        assertThat(sm.find(TOKEN)).isPresent();
        assertThat(sm.find("blah")).isNotPresent();
    }

    @Test
    void testIsEmpty() {
        assertThat(sm.isEmpty()).isTrue();
        assertThat(sm.set(originalState).isEmpty()).isFalse();
    }

}
