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

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.java.state.StateType.*;

class StateTest {

    private static final String TOKEN = "abcd-1";
    private State state;

    @BeforeEach
    void setup() {
        state = IState.create(b -> b.currentState(COMPLETE).token(TOKEN));
    }

    @Test
    void testGetToken() {
        assertThat(state.getToken()).isEqualTo(TOKEN);
    }

    @Test
    void testGetCurrentState() {
        assertThat(state.getCurrentState()).isEqualTo(COMPLETE);
        assertThat(((IState) state).getCurrentState()).isEqualTo(COMPLETE);
    }

    @Test
    void testFrom() {
        assertThat(state.from(b -> b)).isEqualTo(state).isNotSameAs(state);
        assertThat(state.from(b -> b.currentState(SUBSCRIBED))).isNotEqualTo(state).isNotSameAs(state);
    }

    @Test
    void testIsAt() {
        assertThat(state.isAt(COMPLETE)).isTrue();
        assertThat(state.isAt(SUBSCRIBED)).isFalse();
    }


}
