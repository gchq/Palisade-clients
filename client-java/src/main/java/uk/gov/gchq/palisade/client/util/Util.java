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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

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
                var newValue = input.get(key);
                if (newValue != null) {
                    result.put(entry.getKey(), newValue);
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
            .append("/")
            .append(trimSlashes(endpoint));

        for (String string : endpoints) {
            uri.append("/").append(trimSlashes(string));
        }

        return uri.toString();
    }

    /**
     * Returns a map of the query parameters in the provided url. Note that all
     * escape sequences contained in the properties are decoded.
     *
     * @param uri to extract query parameters from
     * @return a map of the query parameters in the provided url
     */
    public static Map<String, Object> extractQueryParams(final URI uri) {
        var query = Checks.checkNotNull(uri).getQuery();
        var queryParams = new HashMap<String, Object>();
        if (query != null) {
            for (String string : uri.getQuery().split("&")) {
                var a = string.split("=");
                queryParams.put(a[0], a[1]);
            }
        }
        return queryParams;
    }

    /**
     * Returns the string value in the provided properties associated with the
     * provided key. If the value associated with the key is not a String or the key
     * is not found then an {@code IllegalArgumentException} is thrown.
     *
     * @param <T>        The type of the property to return which is of type
     *                   {@code <O>} or a subclass.
     * @param <O>        The base type of the map
     * @param properties The properties to search
     * @param key        The key to find
     * @param clazz      The type of value
     * @return the value in the provided properties associated with the provided key
     * @throws IllegalArgumentException if properties, key or clazz is null
     * @throws NoSuchElementException   if there is no mapping for the provided key
     */
    @SuppressWarnings("java:S3655")
    public static <O extends Object, T extends O> T getProperty(
            final Map<String, O> properties,
            final String key,
            final Class<T> clazz) {
        return findProperty(properties, key, clazz).get();
    }

    /**
     * Returns a value in the provided properties associated with the provided key
     * or empty if not found.
     *
     * @param <T>        The type of the property to return which is of type
     *                   {@code <O>} or a subclass.
     * @param <O>        The base type of the map
     * @param properties The properties to search
     * @param key        The key to find
     * @param clazz      The type of value
     * @return the value in the provided properties associated with the provided key
     *         or empty of not found
     * @throws IllegalArgumentException if properties, key or clazz is null
     */
    @SuppressWarnings("unchecked")
    public static <O extends Object, T extends O> Optional<T> findProperty(
            final Map<String, O> properties,
            final String key,
            final Class<T> clazz) {
        checkNotNull(properties);
        checkNotNull(key);
        checkNotNull(clazz);
        var t = properties.get(key);
        if (t != null && !clazz.isAssignableFrom(t.getClass())) {
            throw new IllegalArgumentException(
                "Key " + key + " found but was of type " + t.getClass().getName() + " not the expected type "
                    + clazz.getName());
        }
        return (Optional<T>) Optional.ofNullable(properties.get(key));
    }

    /**
     * Returns a string containing he JSON representation of the provided
     * {@code object}. If a mapping error occurs, the exception is passed to the
     * provided function which wraps the exception and then the new exception is
     * thrown.
     *
     * @param mapper           The object mapper
     * @param object           The object being serialised
     * @param exceptionWrapper The function which wraps the thrown exception
     * @return a string containing he JSON representation of the provided
     *         {@code object}
     */
    @SuppressWarnings("java:S2221") // we want to catch Exception here
    public static String toJson(
            final ObjectMapper mapper,
            final Object object,
            final Function<Exception, RuntimeException> exceptionWrapper) {
        checkNotNull(mapper);
        checkNotNull(object);
        checkNotNull(exceptionWrapper);
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw exceptionWrapper.apply(e);
        }
    }

    /**
     * Returns a new object deserialised from the provided {@code json}. If a
     * mapping error occurs, the exception is passed to the provided function which
     * wraps the exception and then the new exception is thrown.
     *
     * @param <T>              The type of the object to return
     * @param mapper           The object mapper
     * @param jsonString       The JSON string to be deserialised
     * @param clazz            The type of the object being returned
     * @param exceptionWrapper The function which wraps the thrown exception
     * @return a string containing he JSON representation of the provided
     *         {@code object}
     */
    @SuppressWarnings("java:S2221") // really want to catch Exception
    public static <T> T toInstance(
            final ObjectMapper mapper,
            final String jsonString,
            final Class<T> clazz,
            final Function<Exception, RuntimeException> exceptionWrapper) {
        checkNotNull(mapper);
        checkNotNull(jsonString);
        checkNotNull(clazz);
        checkNotNull(exceptionWrapper);
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            throw exceptionWrapper.apply(e);
        }
    }

}
