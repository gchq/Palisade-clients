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
package uk.gov.gchq.palisade.client.java.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract class providing a serialisation test for a class
 */
@SuppressWarnings("java:S2187") // no tests in this class by design
public class AbstractSerialisationTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setupAll() {
        mapper = new ObjectMapper().registerModules(new Jdk8Module());
    }

    @AfterAll
    static void afterAll() {
        mapper = null;
    }

    /**
     * Tests the serialisation and deserialisation of each object provided by the
     * method source
     * <p>
     * The tests asserted by this method:
     * <ul>
     * <li><em>Equals</em> - Test equality using the each instances equals
     * method</li>
     * <li><em>Recursive Equals</em> - Tests equality by testing field by field in
     * the instance</li>
     * <li><em>JSON Attributes</em> - uses JSONAssert to test whether the serialise
     * instance matches the provided JSON string</li>
     * </ul>
     *
     * @param expectedInstance The expected instance
     * @param expectedJson     The expected JSON of the provided instance
     * @throws Exception if an error occurs
     */
    protected void testInstance(final Object expectedInstance, final String expectedJson) throws Exception {
        var objectMapper = getObjectMapper();

        var valueType = expectedInstance.getClass();
        var actualJson = objectMapper.writeValueAsString(expectedInstance);
        var actualInstance = objectMapper.readValue(actualJson, valueType);

        assertThat(actualInstance)
            .as("check equality using recursive equals()",
                expectedInstance.getClass().getSimpleName())
            .usingRecursiveComparison()
            .isEqualTo(expectedInstance);
    }

    /**
     * Returns the object mapper. Override if needed.
     *
     * @return the object mapper
     */
    protected ObjectMapper getObjectMapper() {
        return mapper;
    }

}
