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
package uk.gov.gchq.palisade.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.gchq.palisade.client.util.Util.getProperty;

class UtilTest {

    @Test
    void testSubstituteWholeVariables() {

        var original = Map.<String, Object>of(
            "key1", "theValue",
            "key2", "${key1}",
            "key3", "${key1}");

        var substituted = Util.substituteVariables(original);

        assertThat(substituted)
            .as("Substituted values have been applied")
            .containsAllEntriesOf(Map.of(
                "key1", "theValue",
                "key2", "theValue",
                "key3", "theValue"));

    }

    @Test
    void testCreateUriFromBasePathAndEndpoint() {

        assertThat(Util.createUri("http://me", "endpoint").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint");

        assertThat(Util.createUri("http://me/", "endpoint").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint");

        assertThat(Util.createUri("http://me", "endpoint/").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint");

        assertThat(Util.createUri("http://me", "/endpoint/").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint");
    }

    @Test
    void testCreateUriFromBasePathAndEndpoints() {

        assertThat(Util.createUri("http://me", "endpoint1", "endpoint2").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint1/endpoint2");

        assertThat(Util.createUri("http://me/", "endpoint1", "/endpoint2").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint1/endpoint2");

        assertThat(Util.createUri("http://me", "endpoint1/", "endpoint2/").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint1/endpoint2");

        assertThat(Util.createUri("http://me", "/endpoint1/", "/endpoint2/").toString())
            .as("check URI created successfully")
            .isEqualTo("http://me/endpoint1/endpoint2");

    }

    @Test
    void testGetProperty() {

        var val = "pal://user_from_authority@localhost:8081/cluster?wsport=8082";
        var userKey = "user";
        var missingKey = "missing";

        var map = Map.<String, Object>of(userKey, val);

        final var dateType = Date.class;
        final var stringType = String.class;

        assertThat(getProperty(map, userKey, stringType))
            .as("check property \"%s\" (%s)", userKey, stringType)
            .isEqualTo(val);

        var nsee = NoSuchElementException.class;
        assertThatExceptionOfType(nsee)
            .as("check exception for missing key \"%s\"", missingKey)
            .isThrownBy(() -> getProperty(map, missingKey, stringType));

        var iae = IllegalArgumentException.class;
        assertThatExceptionOfType(iae)
            .as("check exception for invalid type \"%\"", dateType)
            .isThrownBy(() -> getProperty(map, userKey, dateType));

    }

    @Test
    void testExtractQueryParams() throws Exception {

        var uriWithQuery = new URI("http://localhost/cluster?a=one&b=two&c=three");
        var uriWithoutQuery = new URI("http://localhost/cluster");
        var map = Util.extractQueryParams(uriWithQuery);

        assertThat(map)
            .as("check query parameters from uri %s", uriWithQuery)
            .containsExactlyInAnyOrderEntriesOf(Map.of("a", "one", "b", "two", "c", "three"));

        assertThat(Util.extractQueryParams(uriWithoutQuery))
            .as("check no query parameters from uri %s", uriWithoutQuery)
            .isEmpty();

    }

    @Test
    void testToJson() throws Exception {

        var mapper = new ObjectMapper();
        var object = Map.of("k", "v");

        assertThat(Util.toJson(mapper, object, cause -> new RuntimeException())).isEqualTo("{\"k\":\"v\"}");

        var mockedMapper = mock(ObjectMapper.class);
        when(mockedMapper.writeValueAsString(Mockito.any())).thenThrow(IllegalArgumentException.class);

        assertThatExceptionOfType(RuntimeException.class)
            .as("when the object mapper encounters an error")
            .isThrownBy(() -> Util.toJson(mockedMapper, "alice", RuntimeException::new))
            .withCauseExactlyInstanceOf(IllegalArgumentException.class);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testToInstance() {

        var mapper = new ObjectMapper();
        var object = Map.of("k", "v");
        var instance = Util.toInstance(mapper, "{\"k\":\"v\"}", Map.class, RuntimeException::new);

        assertThat(instance)
            .as("instance deserialised from JSON string is equal to original object")
            .isEqualTo(object);

        assertThatExceptionOfType(RuntimeException.class)
            .as("an exception is thrown due to invalid JSON")
            .isThrownBy(() -> Util.toInstance(mapper, "\"k\":\"v\"}", Map.class, RuntimeException::new))
            .withCauseExactlyInstanceOf(MismatchedInputException.class);
    }
}
