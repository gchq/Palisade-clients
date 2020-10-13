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
package uk.gov.gchq.palisade.client.java.request;

import org.junit.jupiter.api.*;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import static org.assertj.core.api.Assertions.assertThat;

class RequestSerialisationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    }

    @Test
    void test_PalisadeResponse_serialisation() throws Exception {
        var expected = IPalisadeResponse.create(b -> b
            .token("blah")
            .url("url"));
        var string = objectMapper.writeValueAsString(expected);
        var actual = objectMapper.readValue(string, PalisadeResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void test_PalisadeRequest_serialisation() throws Exception {
        var expected = PalisadeRequest.builder()
            .resourceId("resourceId")
            .userId(UserId.builder()
                .id("userId")
                .build())
            .requestId(RequestId.builder()
                .id("requestId")
                .build())
            .context(Context.builder()
                .className("className")
                .purpose("purpose")
                .contents(Map.of())
                .build())
            .build();
        var string = objectMapper.writeValueAsString(expected);
        var actual = objectMapper.readValue(string, PalisadeRequest.class);
        assertThat(actual).isEqualTo(expected);
    }

}
