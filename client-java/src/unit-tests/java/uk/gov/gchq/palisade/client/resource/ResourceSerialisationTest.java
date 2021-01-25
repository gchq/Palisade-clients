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
package uk.gov.gchq.palisade.client.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.palisade.client.resource.IResource.createResource;

class ResourceSerialisationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    }

    @Test
    void testMessageSerialisation() throws Exception {
        var expected = IMessage.createMessage(b -> b
            .type(MessageType.RESOURCE)
            .body("string")
            .headers(Map.of("key", "value")));
        var string = objectMapper.writeValueAsString(expected);
        var actual = objectMapper.readValue(string, Message.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testResourceSerialisation() throws Exception {
        var expected = createResource(b -> b
            .token("token")
            .leafResourceId("leaf-resource-id")
            .url("url")
            .properties(Map.of("key", "value")));
        var string = objectMapper.writeValueAsString(expected);
        var actual = objectMapper.readValue(string, Resource.class);
        assertThat(actual).isEqualTo(expected);
    }

}
