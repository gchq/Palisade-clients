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
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.client.job.state.SavedJobState;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class StateTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUpClass() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void testStateSerialisation() throws Exception {

        var url = Thread.currentThread().getContextClassLoader().getResource("state.json");
        var file = new File(url.toURI());

        var expected = objectMapper.readValue(file, SavedJobState.class);
        var json = objectMapper.writeValueAsString(expected);
        var actual = objectMapper.readValue(json, SavedJobState.class);

        assertThat(actual).isNotNull().isEqualTo(expected);
        assertThat(actual).isNotNull().usingRecursiveComparison().isEqualTo(expected);

        actual = SavedJobState.builder().from(actual).sequence(2).build();

        assertThat(actual).isNotNull().isNotEqualTo(expected);
        assertThat(actual).isNotNull().usingRecursiveComparison().isNotEqualTo(expected);

    }

}
