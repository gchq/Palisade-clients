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

import uk.gov.gchq.palisade.client.ClientException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for the client
 *
 * @since 0.5.0
 */
public abstract class Utils {

    private Utils() { // cannot instantiate
    }

    /**
     * Copies each property from source to target if they do not exist in target.
     * For example to copy receiver that may override those from global,
     *
     * @param overrides The overrides to apply
     * @param original  The original properties to be overriden
     * @return A new map containing all properties and overides
     */
    public static Map<String, String> overrideProperties(final Map<String, String> overrides,
        final Map<String, String> original) {
        var result = new HashMap<>(original);
        result.putAll(overrides);
        return result;
    }

    /**
     * Appends a suffix to a base path, removing or adding slashes as necessary
     *
     * @param basePath The base path
     * @param suffix   The suffix
     * @return The concatenated paths
     */
    public static String appendPath(final String basePath, final String suffix) {
        var path = basePath;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 2);
        }
        if (suffix.startsWith("/")) {
            return path + suffix;
        }
        return path + "/" + suffix;
    }

    /**
     * Returns a newly created instance of the provided class
     *
     * @param <T>       The type
     * @param className The class name
     * @return a newly created instance of the provided class
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(final String className) {
        Class<T> cls;
        try {
            return (T) Class.forName(className).getConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | NoSuchMethodException |
                 SecurityException e) {
            throw new ClientException("Failed to load class " + className);
        }
    }
}
