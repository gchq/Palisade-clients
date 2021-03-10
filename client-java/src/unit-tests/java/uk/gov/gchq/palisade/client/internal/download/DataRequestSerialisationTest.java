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
package uk.gov.gchq.palisade.client.internal.download;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import uk.gov.gchq.palisade.client.testing.AbstractSerialisationTest;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.gov.gchq.palisade.client.internal.download.DataRequest.createDataRequest;

class DataRequestSerialisationTest extends AbstractSerialisationTest {

    /**
     * Returns a stream of arguments, each having two elements. The first element is
     * the actual object to be tested, with the second element representing it's
     * serialised form.
     *
     * @return a stream of arguments, each having two elements (test object and JSON
     *         string)
     */
    static Stream<Arguments> instances() {
        return Stream.of(
            arguments(
                createDataRequest(b -> b
                    .token("test-request-token")
                    .leafResourceId("leaf-resource-id")),
                "{\"token\":\"test-request-token\",\"leafResourceId\":\"leaf-resource-id\"}"));
    }

    /**
     * Test the provided instance
     *
     * @param expectedInstance The expected instance
     * @param expectedJson     The expected JSON of the provided instance
     * @throws Exception if an error occurs
     * @see AbstractSerialisationTest#testInstance(Object, String)
     */
    @ParameterizedTest
    @MethodSource("instances")
    void testFullSerialisation(final Object expectedInstance, final String expectedJson) throws Exception {
        testInstance(expectedInstance, expectedJson);
    }

}
