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
package uk.gov.gchq.palisade.client.internal.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.internal.request.PalisadeRequest.createPalisadeRequest;
import static uk.gov.gchq.palisade.client.internal.request.PalisadeResponse.createPalisadeResponse;

class RequestSerialisationTest {

    private static Map<String, String> map;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setupAll() {
        mapper = new ObjectMapper().registerModules(new Jdk8Module());
        map = Map.of("key", "value");
    }

    @AfterAll
    static void afterAll() {
        mapper = null;
        map = null;
    }

    static Object[] instances() {
        return new Object[] {
            createPalisadeResponse(b -> b.token("blah")),
            createPalisadeRequest(b -> b.resourceId("resourceId").userId("userId"))
        };
    }

    @ParameterizedTest
    @MethodSource("instances")
    void testSerialisation(final Object expected) throws Exception {
        var valueType = expected.getClass();
        var content = mapper.writeValueAsString(expected);
        var actual = mapper.readValue(content, valueType);
        assertThat(actual).as(valueType.getSimpleName() + " equals").isEqualTo(expected);
        assertThat(actual).as(valueType + " recursive equals").usingRecursiveComparison().isEqualTo(expected);
    }

}
