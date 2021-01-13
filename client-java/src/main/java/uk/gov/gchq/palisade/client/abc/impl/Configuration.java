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
package uk.gov.gchq.palisade.client.abc.impl;

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

import static uk.gov.gchq.palisade.client.util.Util.trimSlashes;

/**
 * A configuration object holds the job configuration
 *
 * @since 0.5.0
 */
public final class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    /**
     * The domain/host to connect to
     */
    public static final String KEY_SERVICE_URL = "service.url";

    /**
     * The domain/host to connect to
     */
    public static final String KEY_SERVICE_URI = "service.uri";

    /**
     * The domain/host to connect to
     */
    public static final String KEY_SERVICE_USER = "service.user";

    /**
     * The context
     */
    public static final String KEY_SERVICE_CONTEXT = "service.context";

    /**
     * path
     */
    public static final String KEY_STATE_PATH = "state.path";

    /**
     * palisade service context
     */
    public static final String KEY_SERVICE_PS_URL = "service.palisade.url";

    /**
     * palisade service context
     */
    public static final String KEY_SERVICE_PS_CONTEXT = "service.palisade.context";

    /**
     * palisade service endpoint name
     */
    public static final String KEY_SERVICE_PS_ENDPOINT = "service.palisade.endpoint";

    /**
     * palisade service context
     */
    public static final String KEY_SERVICE_FRS_URL = "service.filteredResource.url";

    /**
     * filtered resource service context
     */
    public static final String KEY_SERVICE_FRS_CONTEXT = "service.filteredResource.context";

    /**
     * filtered resource service endpoint
     */
    public static final String KEY_SERVICE_FRS_ENDPOINT = "service.filteredResource.endpoint";

    /**
     * data service context
     */
    public static final String KEY_SERVICE_DS_CONTEXT = "service.data.context";

    /**
     * data service endpoint
     */
    public static final String KEY_SERVICE_DS_ENDPOINT = "service.data.endpoint";

    /**
     * path for receiver to place files
     */
    public static final String KEY_RECEIVER_FILE_PATH = "receiver.file.path";

    /**
     * the class of the receiver
     */
    public static final String KEY_RECEIVER_FILE_CLASS = "receiver.file.class";

    /**
     * receiver file template
     */
    public static final String KEY_RECEIVER_FILE_TEMPLATE = "receiver.file.template";

    private final Map<String, Object> properties;

    ObjectMapper objectMapper = new ObjectMapper()
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
    public static Configuration from(final Map<String, Object> properties) {
        return fromDefaults().merge(properties);
    }

    /**
     * Returns a new configuration instance loaded with all defaults
     *
     * @return a new configuration instance loaded with all defaults
     */
    public static Configuration fromDefaults() {

        try {

            var url = Thread.currentThread().getContextClassLoader().getResource("palisade-client2.yaml");
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
     * Returns a new configuration with the provided configuration merged into this
     * one.
     *
     * @param configuration The configuration to merge
     * @return a new configuration
     */
    public Configuration merge(final Configuration configuration) {
        return merge(configuration.properties);
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
        Map<String, Object> allProperties = new HashMap<>();
        allProperties.putAll(this.properties);
        allProperties.putAll(overrides);
        allProperties = Util.substituteVariables(allProperties);

        if (LOGGER.isDebugEnabled()) {
            var sb = new StringBuilder("Merged configuration: {\n");
            for (Map.Entry<String, Object> entry : allProperties.entrySet()) {
                sb.append("  ");
                sb.append(entry);
                sb.append("\n");
            }
            sb.append("}");
            LOGGER.debug(sb.toString());
        }

        allProperties = process(allProperties);

        return new Configuration(allProperties);
    }

    @Override
    public String toString() {
        return "Configuration [properties=" + properties + "]";
    }

    public String getUser() {
        return getProperty(KEY_SERVICE_USER);
    }

    public URI getServiceUri() {
        return getProperty(KEY_SERVICE_URI, URI.class);
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
     * Returns the path where job states should be saved
     *
     * @return the path where job states should be saved
     */
    public String getStatePath() {
        return getProperty(KEY_STATE_PATH);
    }

    /**
     * Returns the path that will be appended to url returned for a resource
     *
     * @return the path that will be appended to url returned for a resource
     */
    public String getDataPath() {
        return getProperty("service.data.path");
    }

    /**
     * Returns where a file receiver should save files
     *
     * @return where a file receiver should save files
     */
    public String getReceiverFilePath() {
        var result = getProperty(KEY_RECEIVER_FILE_PATH);
        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    /**
     * Returns an optional containing the value for the provided key or empty if not
     * found
     *
     * @param key The key of the value to find
     * @return an optional containing the value for the provided key or empty if not
     *         found
     */
    public Optional<String> findProperty(final String key) {
        return Optional.ofNullable((String) properties.get(key));
    }

    /**
     * Returns an optional typed clazz, containing the value for the provided key or
     * empty if not found
     *
     * @param <T>   The type to return
     * @param key   The key of the value to find
     * @param clazz The class representing the type
     * @return an optional typed clazz, containing the value for the provided key or
     *         empty if not found
     */
    @SuppressWarnings({ "unchecked", "java:S1172" })
    public <T> Optional<T> findProperty(final String key, final Class<T> clazz) {
        return Optional.ofNullable((T) properties.get(key));
    }

    /**
     * Returns a value for the provided key. Throws {@code ConfigurationException}
     * if not found
     *
     * @param key The key of the value to find
     * @return a value for the provided key
     * @throws ConfigurationException if the key is not found
     */
    public String get(final String key) {
        return findProperty(key)
            .orElseThrow(() -> new ConfigurationException("Value for key \"" + key + "\" not found"));
    }

    /**
     * Returns a typed value for the provided key. Throws
     * {@code ConfigurationException} if not found
     *
     * @param <T>   The type of the value to be found
     * @param key   The key of the value to find
     * @param clazz The class representing the type
     * @return a value for the provided key
     * @throws ConfigurationException if the key is not found
     */
    @SuppressWarnings({ "unchecked", "java:S1172" })
    public <T> T get(final String key, final Class<T> clazz) {
        return (T) get(key);
    }

    /**
     * Returns the properties for this configuration
     *
     * @return the properties for this configuration
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    private String getProperty(final String key) {
        return getProperty(key, String.class);
    }

    @SuppressWarnings({ "java:S1172" })
    private <T> T getProperty(final String key, final Class<T> clazz) {
        return getProperty(properties, key, clazz);
    }

    private static String getProperty(final Map<String, Object> properties, final String key) {
        return getProperty(properties, key, String.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getProperty(final Map<String, Object> properties, final String key, final Class<T> clazz) {
        return (T) properties.get(key);
    }

    private static Optional<String> findProperty(final Map<String, Object> properties, final String key) {
        return findProperty(properties, key, String.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> findProperty(final Map<String, Object> properties, final String key,
        final Class<T> clazz) {
        return (Optional<T>) Optional.ofNullable(properties.get(key));
    }

    private static Map<String, Object> process(final Map<String, Object> properties) {

        // process the uri

        String url = getProperty(properties, KEY_SERVICE_URL);

        // if the url is not set, then we cannot propcess it
        if (url != null) {

            URI uri;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                throw new ClientException("Invalid palisade url: " + url);
            }

            // extract query params

            var queryParams = new HashMap<String, String>();
            if (uri.getQuery() != null) {
                for (String string : uri.getQuery().split("&")) {
                    var a = string.split("=");
                    queryParams.put(a[0], a[1]);
                }
            }

            // add user from authority

            var authority = uri.getAuthority();
            if (authority.contains("@")) {
                properties.put("service.user", authority.split("@")[0]);
            }

            // add user from query parameter

            var user = queryParams.get("user");
            if (user != null) {
                properties.put("service.user", user);
            }

            // process the palisade url

            final var bldr1 = new StringBuilder().append("http://").append(uri.getHost());
            if (uri.getPort() > -1) {
                bldr1.append(":").append(uri.getPort());
            } else {
                findProperty(properties, "service.palisade.port").ifPresent(p -> bldr1.append(":").append(p));
            }
            bldr1.append(uri.getPath())
                .append("/")
                .append(trimSlashes(getProperty(properties, "service.palisade.path")));
            properties.put(KEY_SERVICE_PS_URL, bldr1.toString());

            // process the websocket url

            var bldr2 = new StringBuilder().append("ws://").append(uri.getHost());
            var wsport = queryParams.get("wsport");
            if (wsport != null) {
                bldr2.append(":").append(wsport);
            } else {
                findProperty(properties, "service.filteredResource.port").ifPresent(p -> bldr2.append(":").append(p));
            }
            bldr2.append(uri.getPath())
                .append("/")
                .append(trimSlashes(getProperty(properties, "service.filteredResource.path")));
            properties.put(KEY_SERVICE_FRS_URL, bldr2.toString());

            if (LOGGER.isDebugEnabled()) {
                var sb = new StringBuilder("Processed configuration: {\n");
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    sb.append("  ");
                    sb.append(entry);
                    sb.append("\n");
                }
                sb.append("}");
                LOGGER.debug(sb.toString());
            }

        }

        return properties;

    }

}
