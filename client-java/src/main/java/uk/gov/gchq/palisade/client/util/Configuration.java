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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.wnameless.json.flattener.JsonFlattener;

import uk.gov.gchq.palisade.client.ClientException;

import java.io.File;
import java.io.IOException;
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
public class Configuration {

    /**
     * The domain/host to connect to
     */
    public static final String KEY_SERVICE_HOST = "service.host";

    /**
     * The port
     */
    public static final String KEY_SERVICE_PORT = "service.port";

    /**
     * The context
     */
    public static final String KEY_SERVICE_CONTEXT = "service.context";

    /**
     * path
     */
    public static final String KEY_STATE_PATH = "state.path";

    /**
     * palisade service scheme
     */
    public static final String KEY_SERVICE_PS_SCHEME = "service.palisade.scheme";

    /**
     * pasliade service port
     */
    public static final String KEY_SERVICE_PS_PORT = "service.palisade.port";

    /**
     * palisade service context
     */
    public static final String KEY_SERVICE_PS_CONTEXT = "service.palisade.context";

    /**
     * palisade service endpoint name
     */
    public static final String KEY_SERVICE_PS_ENDPOINT = "service.palisade.endpoint";

    /**
     * filtered resource service scheme
     */
    public static final String KEY_SERVICE_FRS_SCHEME = "service.filteredResource.scheme";

    /**
     * filtered resource service port
     */
    public static final String KEY_SERVICE_FRS_PORT = "service.filteredResource.port";

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
     * number of download threads
     */
    public static final String KEY_DOWNLOAD_THREADS = "download.threads";

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

    private Configuration(final Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Creates a new configuration instance with the default overriden by values
     * from the provided map
     *
     * @param properties The property overrides
     * @return a new configuration instance with the default overriden by values
     *         from the provided map
     */
    public static final Configuration from(final Map<String, Object> properties) {
        return fromDefaults().merge(properties);
    }

    /**
     * Returns a new configuration instance loaded with all defaults
     *
     * @return a new configuration instance loaded with all defaults
     */
    public static final Configuration fromDefaults() {

        try {

            var url = Thread.currentThread().getContextClassLoader().getResource("palisade-client.yaml");
            var file = new File(url.toURI());

            // this will actually read the file into a nested object graph of maps (maps as
            // values to keys etc)
            var object = new ObjectMapper(new YAMLFactory()).readValue(file, Object.class);

            // now we need to convert the map graph into json
            var json = new ObjectMapper().writeValueAsString(object);

            // ... so that the flattener can create a single flat map of dot delimited keys:
            // e.g. "file.reciever.path=/tmp"
            var map = JsonFlattener.flattenAsMap(json);

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
    public Configuration merge(final Map<String, ? extends Object> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return this;
        }
        var p = new HashMap<String, Object>();
        p.putAll(this.properties);
        p.putAll(overrides);
        return new Configuration(p);
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
     * Returns the full Palisade service uri
     *
     * @return the full Palisade service uri
     */
    public String getPalisadeUri() {

        var scheme = getProperty(KEY_SERVICE_PS_SCHEME);
        var host = getProperty(KEY_SERVICE_HOST);
        var port = getProperty(KEY_SERVICE_PS_PORT, Number.class).intValue();
        var cluster = getProperty(KEY_SERVICE_CONTEXT);
        var context = getProperty(KEY_SERVICE_PS_CONTEXT);
        var endpoint = getProperty(KEY_SERVICE_PS_ENDPOINT);

        return String.format("%s://%s:%s/%s/%s/%s",
            scheme, host, port, trimSlashes(cluster), trimSlashes(context), trimSlashes(endpoint));

    }

    /**
     * Returns the full filtered resource service uri
     *
     * @return the full filtered resource service uri
     */
    public String getFilteredResourceUri() {

        var scheme = getProperty(KEY_SERVICE_FRS_SCHEME);
        var host = getProperty(KEY_SERVICE_HOST);
        var port = getProperty(KEY_SERVICE_FRS_PORT, Number.class).intValue();
        var cluster = getProperty(KEY_SERVICE_CONTEXT);
        var context = getProperty(KEY_SERVICE_FRS_CONTEXT);
        var endpoint = getProperty(KEY_SERVICE_FRS_ENDPOINT);

        return String.format("%s://%s:%s/%s/%s/%s",
            scheme, host, port, trimSlashes(cluster), trimSlashes(context), trimSlashes(endpoint));

    }

    /**
     * Returns the path that will be appended to url returned for a resource
     *
     * @return the path that will be appended to url returned for a resource
     */
    public String getDataPath() {
        var context = getProperty(KEY_SERVICE_DS_CONTEXT);
        var endpoint = getProperty(KEY_SERVICE_DS_ENDPOINT);
        return String.format("%s/%s", trimSlashes(context), trimSlashes(endpoint));
    }

    /**
     * Returns the number of download threads
     *
     * @return the number of download threads
     */
    public int getDownloadThreads() {
        var threads = getProperty(KEY_DOWNLOAD_THREADS, Number.class);
        return threads.intValue();
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
    public Optional<Object> find(final String key) {
        return Optional.ofNullable(properties.get(key));
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
    @SuppressWarnings("unchecked")
    public <T> Optional<T> find(final String key, final Class<T> clazz) {
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
    public Object get(final String key) {
        return find(key).orElseThrow(() -> new ConfigurationException("Value for key \"" + key + "\" not found"));
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
    @SuppressWarnings("unchecked")
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

    private String getProperty(final String key) {
        return getProperty(key, String.class);
    }

    @SuppressWarnings({ "unchecked", "java:S1172" })
    private <T> T getProperty(final String key, final Class<T> clazz) {
        return (T) properties.get(key);
    }

}
