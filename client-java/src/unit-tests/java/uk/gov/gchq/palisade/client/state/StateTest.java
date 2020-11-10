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
package uk.gov.gchq.palisade.client.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;

@SuppressWarnings("java:S2187") // no tests yet
class StateTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUpClass() throws Exception {
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

//    @Test
    void testStateSerialisation() throws Exception {

//        var is = Thread.currentThread().getContextClassLoader().getResourceAsStream("state.json");
//        var state1 = objectMapper.readValue(is, State.class);
//
//        assertThat(state1).isNotNull();
//
//        var json = objectMapper.writeValueAsString(state1);
//
//        assertThat(json).isNotNull();
//
//        var state2 = objectMapper.readValue(json, State.class);
//
//        assertThat(state2).isNotNull().isEqualTo(state1);

    }

}
