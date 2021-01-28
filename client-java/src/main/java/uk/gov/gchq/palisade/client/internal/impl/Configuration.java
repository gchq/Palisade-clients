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
package uk.gov.gchq.palisade.client.internal.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;
import static uk.gov.gchq.palisade.client.util.Util.trimSlashes;

/**
 * A configuration object holds the job configuration
 *
 * @since 0.5.0
 */
public final class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final String KEY_SERVICE_URL = "service.url";
    private static final String KEY_SERVICE_PORT = "service.port";
    private static final String KEY_SERVICE_USER = "service.user";
    private static final String KEY_SERVICE_PS_URL = "service.palisade.url";
    private static final String KEY_SERVICE_PS_PORT = "service.palisade.port";
    private static final String KEY_SERVICE_PS_PATH = "service.palisade.path";
    private static final String KEY_SERVICE_FRS_URL = "service.filteredResource.url";
    private static final String KEY_SERVICE_FRS_PORT = "service.filteredResource.port";
    private static final String KEY_SERVICE_FRS_PATH = "service.filteredResource.path";
    private static final String KEY_SERVICE_DATA_PATH = "service.data.path";
    private static final String PARAM_PORT = "port";
    private static final String PARAM_USER = "user";
    private static final String PARAM_WS_PORT = "wsport";
    private static final String PARAM_PS_PORT = "psport";

    private final Map<String, Object> properties;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule())
        // comment out the 3 include directives below to tell jackson to output all
        // attributes, even if null, absent or empty (e.g. empty optional and
        // collection)
        .setSerializationInclusion(Include.NON_NULL)
        .setSerializationInclusion(Include.NON_ABSENT)
        .setSerializationInclusion(Include.NON_EMPTY)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private Configuration(final Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Creates a new configuration instance with the default overridden by values
     * from the provided map
     *
     * @param properties The property overrides
     * @return a new configuration instance with the default overridden by values
     *         from the provided map
     */
    public static Configuration fromDefaults(final Map<String, Object> properties) {
        return fromDefaults().merge(properties);
    }


    /**
     * Returns a new configuration instance loaded with all defaults
     *
     * @return a new configuration instance loaded with all defaults
     */
    public static Configuration fromDefaults() {
        return from("palisade-client2.yaml");
    }

    /**
     * Returns a new configuration instance loaded with all defaults
     *
     * @param filename   The name of the file containing the configuration
     * @param properties The property overrides
     * @return a new configuration instance loaded with all defaults
     */
    public static Configuration from(final String filename, final Map<String, Object> properties) {
        return from(filename).merge(properties);
    }

    /**
     * Returns a new configuration instance loaded with all defaults
     *
     * @param filename The name of the file containing the configuration
     * @return a new configuration instance loaded with all defaults
     */
    public static Configuration from(final String filename) {

        checkNotNull(filename);

        try {

            var url = Thread.currentThread().getContextClassLoader().getResource(filename);
            if (url == null) {
                throw new ClientException("Configuration file " + filename + " not found");
            }
            var file = new File(url.toURI());

            // this will actually read the file into a nested object graph of maps (maps as
            // values to keys etc)
            var object = new ObjectMapper(new YAMLFactory()).readValue(file, Object.class);

            // now we need to convert the map graph into json
            var json = new ObjectMapper().writeValueAsString(object);

            // ... so that the flattener can create a single flat map of dot delimited keys:
            // e.g. "file.receiver.path=/tmp"
            var map = JsonFlattener.flattenAsMap(json);
            map = Util.substituteVariables(map);
            map = process(map);

            if (LOGGER.isDebugEnabled()) {
                var sb = new StringBuilder("Default configuration: {\n");
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    sb.append("  ");
                    sb.append(entry);
                    sb.append("\n");
                }
                sb.append("}");
                LOGGER.debug(sb.toString());
            }

            return new Configuration(map);

        } catch (IOException e) {
            throw new ClientException("Failed to load initial properties", e);
        } catch (URISyntaxException e) {
            throw new ClientException("Could not load default properties (url not valid)", e);
        }

    }

    /**
     * Returns a new configuration with the map merged into this configuration. If
     * the provided map is null or empty, then this configuration is returned
     *
     * @param overrides The map to merge
     * @return a new configuration
     */
    public Configuration merge(final Map<String, ?> overrides) {

        if (overrides == null || overrides.isEmpty()) {
            return this;
        }

        Map<String, Object> allProperties = new HashMap<>(this.properties);
        allProperties.putAll(overrides);
        allProperties = Util.substituteVariables(allProperties);

        var conf = new Configuration(process(allProperties));

        LOGGER.debug("Merged {}", conf);

        return conf;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("configuration: {\n");
        var set = new TreeMap<>(properties).entrySet();
        for (Map.Entry<String, Object> entry : set) {
            sb.append("  ");
            sb.append(entry);
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns the user
     *
     * @return the user
     */
    public String getUser() {
        return getProperty(KEY_SERVICE_USER);
    }

    /**
     * Returns the service uri
     *
     * @return the service uri
     */
    public String getServiceUrl() {
        return getProperty(KEY_SERVICE_URL);
    }

    /**
     * Returns the full Palisade service uri
     *
     * @return the full Palisade service uri
     */
    public String getPalisadeUrl() {
        return getProperty(KEY_SERVICE_PS_URL);
    }

    /**
     * Returns the full filtered resource service uri
     *
     * @return the full filtered resource service uri
     */
    public String getFilteredResourceUrl() {
        return getProperty(KEY_SERVICE_FRS_URL);
    }

    /**
     * Returns the path that will be appended to url returned for a resource
     *
     * @return the path that will be appended to url returned for a resource
     */
    public String getDataPath() {
        return getProperty(KEY_SERVICE_DATA_PATH);
    }

    /**
     * Returns the properties for this configuration
     *
     * @return the properties for this configuration
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * Returns the object mapper
     *
     * @return the object mapper
     */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    private String getProperty(final String key) {
        return getProperty(key, String.class);
    }

    @SuppressWarnings({ "java:S1172" })
    private <T> T getProperty(final String key, final Class<T> clazz) {
        return getProperty(getProperties(), key, clazz);
    }

    private static String getProperty(final Map<String, Object> properties, final String key) {
        return getProperty(properties, key, String.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getProperty(final Map<String, Object> properties, final String key, final Class<T> clazz) {
        var t = properties.get(key);
        if (t != null && !clazz.isAssignableFrom(t.getClass())) {
            throw new IllegalArgumentException(
                "Key " + key + " found but was of type " + t.getClass().getName() + " not the expected type "
                    + clazz.getName());
        }
        return (T) properties.get(key);
    }

    private static Optional<String> findProperty(final Map<String, Object> properties, final String key) {
        return findProperty(properties, key, String.class);
    }

    private static <T> Optional<T> findProperty(final Map<String, Object> properties, final String key,
        final Class<T> clazz) {
        return Optional.ofNullable(getProperty(properties, key, clazz));
    }

    private static Map<String, Object> process(final Map<String, Object> properties) {

        // process the uri

        String url = getProperty(properties, KEY_SERVICE_URL);

        // if the url is not set, then we cannot propcess it
        if (url != null) {

            URI baseUri;
            try {
                baseUri = new URI(url);
            } catch (URISyntaxException e) {
                throw new ClientException("Invalid palisade url: " + url, e);
            }

            // convert any properties that should not be strings

            properties.computeIfPresent(KEY_SERVICE_PORT, (k, v) -> Integer.valueOf(v.toString()));
            properties.computeIfPresent(KEY_SERVICE_PS_PORT, (k, v) -> Integer.valueOf(v.toString()));
            properties.computeIfPresent(KEY_SERVICE_FRS_PORT, (k, v) -> Integer.valueOf(v.toString()));

            // get the port from the url. if it's present then ports for all services.

            var port = baseUri.getPort();
            if (port > -1) {
                properties.put(KEY_SERVICE_PORT, port);
                properties.put(KEY_SERVICE_PS_PORT, port);
                properties.put(KEY_SERVICE_FRS_PORT, port);
            }

            var queryParams = Util.extractQueryParams(baseUri);

            // override each port if found as a query parameter

            findProperty(queryParams, PARAM_PORT)
                .map(Integer::valueOf)
                .ifPresent(v -> properties.put(KEY_SERVICE_PORT, v));

            findProperty(queryParams, PARAM_PS_PORT)
                .map(Integer::valueOf)
                .ifPresent(v -> properties.put(KEY_SERVICE_PS_PORT, v));

            findProperty(queryParams, PARAM_WS_PORT)
                .map(Integer::valueOf)
                .ifPresent(v -> properties.put(KEY_SERVICE_FRS_PORT, v));

            // get the user from the url and then see if a query parameter
            // should override it

            extractUser(baseUri).ifPresent(u -> properties.put(KEY_SERVICE_USER, u));

            findProperty(queryParams, PARAM_USER)
                .ifPresent(v -> properties.put(KEY_SERVICE_USER, v));

            properties.put(KEY_SERVICE_PS_URL, createrPalisadeUrl(baseUri, properties));
            properties.put(KEY_SERVICE_FRS_URL, createFilteredResourceUrl(baseUri, properties));

        }

        return properties;

    }

    private static String createrPalisadeUrl(final URI baseUri, final Map<String, Object> properties) {

        final var bldr = new StringBuilder()
            .append("http://")
            .append(baseUri.getHost());

        findProperty(properties, KEY_SERVICE_PS_PORT, Integer.class)
            .ifPresent(p -> bldr.append(":").append(p));

        bldr.append(baseUri.getPath())
            .append("/")
            .append(trimSlashes(getProperty(properties, KEY_SERVICE_PS_PATH)));

        return bldr.toString();

    }

    private static String createFilteredResourceUrl(final URI baseUri, final Map<String, Object> properties) {

        var bldr = new StringBuilder()
            .append("ws://")
            .append(baseUri.getHost());

        findProperty(properties, KEY_SERVICE_FRS_PORT, Integer.class)
            .ifPresent(p -> bldr.append(":").append(p));

        bldr.append(baseUri.getPath())
            .append("/")
            .append(trimSlashes(getProperty(properties, KEY_SERVICE_FRS_PATH)));

        return bldr.toString();
    }

    static Optional<String> extractUser(final URI baseUri) {

        Object user = null;

        var authority = baseUri.getAuthority();
        if (authority != null && authority.contains("@")) {
            user = authority.split("@")[0];
        }

        return Optional.ofNullable((String) user);

    }
}
