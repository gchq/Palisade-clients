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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility functions
 *
 * @since 0.5.0
 */
public abstract class Util {

    private Util() {
        // cannot instantiate
    }

    private static final DateTimeFormatter DATE_STAMP_FORMATTER = DateTimeFormatter
        .ofPattern("yyyyMMdd-HHmmss")
        .withZone(ZoneId.systemDefault());

    /**
     * Replaces tokens in the provide template by using the functions provided. This
     * is usefil when replacing path tokens in a template. For example
     * <pre>{@code /tmp/%t/%s/%r}</pre>
     *
     * @param template     The source template
     * @param replacements The replacement functions
     * @return the template with tokjens replaced
     */
    public static String replaceTokens(final String template, final Map<String, Supplier<String>> replacements) {
        String result = template;
        for (Map.Entry<String, Supplier<String>> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    /**
     * Formats the provided date, time, offset etc as a string to be used in a file
     * path
     *
     * @param accessor The accessor
     * @return the provided date, time, offset etc as a string to be used in a file
     *         path
     */
    public static String timeStampFormat(final TemporalAccessor accessor) {
        return DATE_STAMP_FORMATTER.format(accessor);
    }
}
