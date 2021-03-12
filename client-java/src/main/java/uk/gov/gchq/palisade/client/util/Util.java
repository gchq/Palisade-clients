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

import java.net.URI;

/**
 * Utility functions
 *
 * @since 0.5.0
 */
public final class Util {

    /**
     * URI separator
     */
    public static final String URI_SEP = "/";

    private Util() { // should not be instantiated
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
     * Returns a uri from the provide base path and endpoint(s)
     *
     * @param baseUri   the base uri
     * @param endpoint  the endpoint
     * @param endpoints further endpoints
     * @return a uri from the provide base path and endpoint(s)
     */
    public static URI createUri(final String baseUri, final String endpoint, final String... endpoints) {
        return URI.create(createUrl(baseUri, endpoint, endpoints));
    }

    /**
     * Returns a uri from the provide base path and endpoint(s)
     *
     * @param baseUri   the base uri
     * @param endpoint  the endpoint
     * @param endpoints further endpoints
     * @return a uri from the provide base path and endpoint(s)
     */
    public static String createUrl(final String baseUri, final String endpoint, final String... endpoints) {

        var uri = new StringBuilder(trimSlashes(baseUri))
            .append(URI_SEP)
            .append(trimSlashes(endpoint));

        for (String string : endpoints) {
            uri.append(URI_SEP).append(trimSlashes(string));
        }

        return uri.toString();
    }
}
