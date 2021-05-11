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
package uk.gov.gchq.palisade.client.java.internal.resource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import uk.gov.gchq.palisade.client.java.internal.model.MessageType;
import uk.gov.gchq.palisade.client.java.internal.model.WebSocketMessage;
import uk.gov.gchq.palisade.client.java.testing.AbstractSerialisationTest;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.resource.impl.SystemResource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class ResourceSerialisationTest extends AbstractSerialisationTest {

    /**
     * Returns a stream of arguments, each having two elements. The first element is
     * the actual object to be tested, with the second element representing it's
     * serialised form.
     *
     * @return a stream of arguments, each having two elements (test object and JSON
     * string)
     */
    static Stream<Arguments> instances() {
        return Stream.of(
            arguments(
                WebSocketMessage.Builder.create()
                    .withType(MessageType.RESOURCE)
                    .withHeaders(Map.of("key", "value"))
                    .withSerialisedBody("body"),
                "{\"type\":\"RESOURCE\",\"headers\":{\"key\":\"value\"},\"body\":\"body\"}"),
            arguments(
                new FileResource()
                    .id("leaf-resource-id")
                    .connectionDetail(new SimpleConnectionDetail().serviceName("serviceName"))
                    .type("type")
                    .serialisedFormat("format")
                    .parent(new SystemResource().id("parent")),
                "{\"id\":\"leaf-resource-id\",\"serialisedFormat\":\"format\",\"type\":\"type\",\"connectionDetail\":{\"serviceName\":\"serviceName\"},\"attributes\":null,\"parent\":{\"id\":\"parent/\"}}"));
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
    void testSerialisation(final Object expectedInstance, final String expectedJson) throws Exception {
        testInstance(expectedInstance, expectedJson);
    }

}
