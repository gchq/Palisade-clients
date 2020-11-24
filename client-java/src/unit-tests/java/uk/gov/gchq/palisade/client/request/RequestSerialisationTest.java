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
package uk.gov.gchq.palisade.client.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.request.IPalisadeRequest.createPalisadeRequest;
import static uk.gov.gchq.palisade.client.request.IPalisadeResponse.createPalisadeResponse;

class RequestSerialisationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    }

    @Test
    void testPalisadeResponseSserialisation() throws Exception {
        PalisadeResponse expected = createPalisadeResponse(b -> b.token("blah"));
        String string = objectMapper.writeValueAsString(expected);
        PalisadeResponse actual = objectMapper.readValue(string, PalisadeResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testPalisadeRequestSerialisation() throws Exception {
        PalisadeRequest expected = createPalisadeRequest(b -> b.resourceId("resourceId").userId("userId"));
        String string = objectMapper.writeValueAsString(expected);
        PalisadeRequest actual = objectMapper.readValue(string, PalisadeRequest.class);
        assertThat(actual).isEqualTo(expected);
    }

}
