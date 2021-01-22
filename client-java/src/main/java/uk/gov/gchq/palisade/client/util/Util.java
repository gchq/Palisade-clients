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

import java.net.URI;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * Utility functions
 *
 * @since 0.5.0
 */
public final class Util {

    private static final DateTimeFormatter DATE_STAMP_FORMATTER = DateTimeFormatter
        .ofPattern("yyyyMMdd-HHmmss")
        .withZone(ZoneId.systemDefault());

    private Util() {
        // cannot instantiate
    }

    /**
     * Replaces tokens in the provide template by using the functions provided. This
     * is useful when replacing path tokens in a template. For example
     * <pre>{@code /tmp/%t/%s/%r}</pre>
     *
     * @param template     The source template
     * @param replacements The replacement functions
     * @return the template with tokens replaced
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

    /**
     * Returns a new string with leading and trailing slashes removed
     *
     * @param path The path
     * @return a new string with leading and trailing slashes removed
     */
    public static String trimSlashes(final String path) {
        String result = path.trim();
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Returns a map containing a copy of the provided {@code input}, but with
     * expression values replaced.
     *
     * @param input The map to modify
     * @return a map containing a copy of the provided {@code input}, but with
     *         expression values replaced.
     */
    public static Map<String, Object> substituteVariables(final Map<String, Object> input) {
        var result = new HashMap<>(input);
        for (Entry<String, Object> entry : input.entrySet()) {
            var expr = entry.getValue().toString().trim();
            if (expr.startsWith("${") && expr.endsWith("}")) {
                var key = expr.substring(2, expr.length() - 1);
                var repl = input.get(key);
                if (repl != null) {
                    result.put(entry.getKey(), repl);
                }
            }
        }
        return result;
    }

    /**
     * Returns a uri from the provide base path and endpoint(s)
     *
     * @param baseUri   the base uri
     * @param endpoint  the endpoint
     * @param endpoints further endpoints
     * @return a uri from the provide base path and endpoint(s)
     */
    public static URI createUri(final String baseUri, final String endpoint, final String... endpoints) {

        var uri = new StringBuilder(trimSlashes(baseUri))
            .append("/")
            .append(trimSlashes(endpoint));

        for (String string : endpoints) {
            uri.append("/").append(trimSlashes(string));
        }

        return URI.create(uri.toString());
    }

}