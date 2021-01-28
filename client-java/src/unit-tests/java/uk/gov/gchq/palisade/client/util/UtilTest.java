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

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {

    @Test
    void testReplaceTokens() {

        var token = "abcd-1";
        var now = Util.timeStampFormat(Instant.now());

        var replacementMap = Map.<String, Supplier<String>>of(
            "%t", () -> token,
            "%s", () -> now);

        var path = Util.replaceTokens("/my/path/t-%t/s-%s.json", replacementMap);

        assertThat(path).isEqualTo(("/my/path/t-" + token + "/s-" + now + ".json"));
    }

    @Test
    void testSubstituteWholeVariables() {

        var original = Map.<String, Object>of(
            "key1", "theValue",
            "key2", "${key1}",
            "key3", "${key1}");

        var substituted = Util.substituteVariables(original);

        assertThat(substituted).containsAllEntriesOf(Map.of(
            "key1", "theValue",
            "key2", "theValue",
            "key3", "theValue"));

    }

    @Test
    void testCreateUriFromBasePathAndEndpoint() {
        assertThat(Util.createUri("http://me", "endpoint").toString()).isEqualTo("http://me/endpoint");
        assertThat(Util.createUri("http://me/", "endpoint").toString()).isEqualTo("http://me/endpoint");
        assertThat(Util.createUri("http://me", "endpoint/").toString()).isEqualTo("http://me/endpoint");
        assertThat(Util.createUri("http://me", "/endpoint/").toString()).isEqualTo("http://me/endpoint");
    }

    @Test
    void testCreateUriFromBasePathAndEndpoints() {
        assertThat(Util.createUri("http://me", "endpoint1", "endpoint2").toString())
            .isEqualTo("http://me/endpoint1/endpoint2");
        assertThat(Util.createUri("http://me/", "endpoint1", "/endpoint2").toString())
            .isEqualTo("http://me/endpoint1/endpoint2");
        assertThat(Util.createUri("http://me", "endpoint1/", "endpoint2/").toString())
            .isEqualTo("http://me/endpoint1/endpoint2");
        assertThat(Util.createUri("http://me", "/endpoint1/", "/endpoint2/").toString())
            .isEqualTo("http://me/endpoint1/endpoint2");
    }
}
