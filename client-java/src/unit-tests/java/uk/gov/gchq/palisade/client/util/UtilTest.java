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

        String path = Util.replaceTokens("/my/path/t-%t/s-%s.json", replacementMap);

        assertThat(path).isEqualTo(("/my/path/t-" + token + "/s-" + now + ".json"));
    }

}
