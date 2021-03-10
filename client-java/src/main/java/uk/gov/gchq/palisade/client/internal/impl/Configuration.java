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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.util.Checks;
import uk.gov.gchq.palisade.client.util.Util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static uk.gov.gchq.palisade.client.util.Util.URI_SEP;
import static uk.gov.gchq.palisade.client.util.Util.substituteVariables;
import static uk.gov.gchq.palisade.client.util.Util.trimSlashes;

/**
 * This is the main configuration object for a session. Create a new
 * configuration by call {@code Configuration#create(Map)}, passing in the
 * required parameters.
 * <p>
 * The only required parameter is "service.url". This is the URL to the palisade
 * cluster. Other values are set by parsing the URL, for example to create the
 * Palisade and Filtered Resource URIs. The service.url is stored as just a
 * string as it's never used to connect to a remote server.
 * <p>
 * Any port supplied as part of the service URL will be used for the palisade
 * and Filtered Service URI's, but these can be overridden by specifying
 * "service.palisade.port" and "service.filteredResource.port" respectively.
 * Alternatively the ports can be specified on the query part of the URL by
 * specifying the "psport" and "wsport" attributes. These attributes will
 * override those passed in the property map. Query parameters take precedence.
 *
 * @since 0.5.0
 */
public final class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    /**
     * The service URL simply represents the original URL supplied to the
     * ClientManager. e.g.
     * <ul>
     * <li><pre>pal://eve@localhost:8090/cluster?wsport=8091</pre></li>
     * <li><pre>{@code pal://localhost/cluster?port=8090&wsport=8091&user=alice}</pre></li>
     * </ul>
     */
    private static final String KEY_SERVICE_URL = "service.url";

    /**
     * The user credentials supplied as part of the authority or within the query
     * string. Note that the property supplied in the query string takes precedence
     * e.g. - "pal://eve@localhost/cluster" - "pal://localhost/cluster?user=alice"
     */
    private static final String KEY_SERVICE_USER_ID = "service.userid";

    /**
     * The generated Palisade URI. This URI is generated from "palisade.url". This
     * URI does not have the user portion of the authority or the query string, but
     * will include the port if provided. e.g. -
     * http://localhost/cluster/palisade/api/registerDataRequest
     */
    private static final String KEY_SERVICE_PS_URI = "service.palisade.uri";

    /**
     * The Palisade service port if provided
     */
    private static final String KEY_SERVICE_PS_PORT = "service.palisade.port";

    /**
     * Contains the port from "palisade.url" if provided.
     */
    private static final String KEY_SERVICE_PS_PATH = "service.palisade.path";

    /**
     * The generated Filtered Resource Service URI. This URI is generated from
     * "palisade.url". This URI does not have the user portion of the authority or
     * the query string, but will include the port if provided. As the value is
     * stored as a URI the "%t" is encoded to "%25t" e.g. -
     * ws://localhost/cluster/resource/%25t
     */
    private static final String KEY_SERVICE_FRS_URI = "service.filteredResource.uri";

    /**
     * The Filtered Resource Service port if provided
     */
    private static final String KEY_SERVICE_FRS_PORT = "service.filteredResource.port";

    /**
     * The path portion of the Filtered Resource Service URI which defaults to
     * "Resource/%t"
     */
    private static final String KEY_SERVICE_FRS_PATH = "service.filteredResource.path";

    /**
     * The path portion of the Data Service URI which defaults to "read/chunked"
     */
    private static final String KEY_SERVICE_DATA_PATH = "service.data.path";

    /**
     * The timeout in seconds to wait for a new message to become available before being
     * emitted into the resource stream before looping and trying again
     */
    private static final String KEY_QUERY_STREAM_POLL_TIMEOUT = "query.stream.poll.timeout";

    /**
     * If this property is set then the client will connect without forcing any
     * protocol. The decision is left up to the HttpClient and the server. But if
     * set to true, then the client will not ask to upgrade connections to HTTP/2.
     */
    private static final String KEY_SERVICE_HTTP2_ENABLED = "service.http2.enabled";

    /**
     * The user provided in the "service.url" property if provided
     */
    private static final String PARAM_USER_ID = "userid";

    /**
     * The port to the Filtered Resource Service if provided as a query parameter (wsport)
     * of the port of the main cluster if provided
     */
    private static final String PARAM_WS_PORT = "wsport";

    /**
     * The port to the Palisade Service if provided as a query parameter (psport)
     * of the port of the main cluster if provided
     */
    private static final String PARAM_PS_PORT = "psport";

    private final Map<String, Object> properties;

    private Configuration(final Map<String, Object> properties) {

        this.properties = properties;

        // see what version of HTTP is allowed and log accordingly
        if (isHttp2Enabled()) {
            LOGGER.debug("HTTP/2 support is eanbled");
        } else {
            LOGGER.debug("HTTP/2 support is disabled. Only using HTTP/1.1");
        }

    }

    /**
     * Returns a new configuration instance
     *
     * @param properties The property overrides
     * @return a new configuration instance loaded with all defaults
     * @throws ConfigurationException if the configuration could not be created
     *                                successfully
     */
    @SuppressWarnings("java:S2221")
    public static Configuration create(final Map<String, String> properties) {

        Checks.checkNotNull(properties, "If no properties then use an empty map or use the create() method");

        // set up map with system defaults
        // these can be overridden if needed.

        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SERVICE_PS_PATH, "palisade/api/registerDataRequest");
        map.put(KEY_SERVICE_FRS_PATH, "resource/%t");
        map.put(KEY_SERVICE_DATA_PATH, "read/chunked");
        map.put(KEY_SERVICE_HTTP2_ENABLED, false);
        map.putAll(properties); // add in user supplied properties which can override system defaults

        try {
            map = substituteVariables(map); // replace any substitution variables
            process(map); // generate the rest of the properties (e.g. palisade URL)
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("An unknown error occurred creating the configuration", e);
        }

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

    }

    @Override
    public String toString() {
        var sb = new StringBuilder("configuration: {\n");
        var set = new TreeMap<>(getProperties()).entrySet();
        for (Map.Entry<String, Object> entry : set) {
            sb.append("  ");
            sb.append(entry);
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public boolean isHttp2Enabled() {
        return getProperty(KEY_SERVICE_HTTP2_ENABLED, Boolean.class);
    }

    /**
     * Returns the user
     *
     * @return the user
     */
    public String getUser() {
        return findProperty(KEY_SERVICE_USER_ID).orElseThrow(() -> new ConfigurationException("No user configured"));
    }

    /**
     * Returns the number of seconds to wait before timing out when waiting for
     * another message to become available and then emitted from the stream
     *
     * @return the number of seconds to wait before timing out when waiting for
     *         another message
     */
    public int getQueryStreamPollTimeout() {
        return findProperty(KEY_QUERY_STREAM_POLL_TIMEOUT).map(Integer::valueOf).orElse(1);
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
    public URI getPalisadeUrl() {
        return getProperty(KEY_SERVICE_PS_URI, URI.class);
    }

    /**
     * Returns the full filtered resource service uri
     *
     * @return the full filtered resource service uri
     */
    public URI getFilteredResourceUrl() {
        return getProperty(KEY_SERVICE_FRS_URI, URI.class);
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
    private Map<String, Object> getProperties() {
        return this.properties;
    }

    private Optional<String> findProperty(final String key) {
        return findProperty(key, String.class);
    }

    private <T> Optional<T> findProperty(final String key, final Class<T> clazz) {
        return Util.findProperty(getProperties(), key, clazz);
    }

    private String getProperty(final String key) {
        return getProperty(key, String.class);
    }

    private <T> T getProperty(final String key, final Class<T> clazz) {
        return Util.getProperty(getProperties(), key, clazz);
    }

    private static Map<String, Object> process(final Map<String, Object> properties) {

        // process the URI

        String url = Util.findProperty(properties, KEY_SERVICE_URL, String.class)
            .orElseThrow(() -> new ConfigurationException("No url provided"));

        URI baseUri;
        try {
            baseUri = new URI(url);
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Invalid palisade url: " + url, e);
        }

        // convert any properties that should not be strings

        properties.computeIfPresent(KEY_SERVICE_PS_PORT, (k, v) -> Integer.valueOf(v.toString()));
        properties.computeIfPresent(KEY_SERVICE_FRS_PORT, (k, v) -> Integer.valueOf(v.toString()));
        properties.computeIfPresent(KEY_SERVICE_HTTP2_ENABLED, (k, v) -> Boolean.valueOf(v.toString()));

        // get the port from the url. if it's present then ports for all services.

        var port = baseUri.getPort();
        if (port > -1) {
            properties.put(KEY_SERVICE_PS_PORT, port);
            properties.put(KEY_SERVICE_FRS_PORT, port);
        }

        var queryParams = Util.extractQueryParams(baseUri);

        // override a property if a query parameter has been supplied

        Util.findProperty(queryParams, PARAM_PS_PORT, String.class)
            .map(Integer::valueOf)
            .ifPresent(v -> properties.put(KEY_SERVICE_PS_PORT, v));

        Util.findProperty(queryParams, PARAM_WS_PORT, String.class)
            .map(Integer::valueOf)
            .ifPresent(v -> properties.put(KEY_SERVICE_FRS_PORT, v));

        Util.findProperty(queryParams, PARAM_USER_ID, String.class)
            .ifPresent(v -> properties.put(KEY_SERVICE_USER_ID, v));

        if (!properties.containsKey(KEY_SERVICE_USER_ID)) {
            throw new ConfigurationException("User has not been set either via a property or url query string");
        }

        properties.put(KEY_SERVICE_PS_URI, createPalisadeUrl(baseUri, properties));
        properties.put(KEY_SERVICE_FRS_URI, createFilteredResourceUrl(baseUri, properties));

        return properties;

    }

    @SuppressWarnings("java:S1488")
    static URI createPalisadeUrl(final URI baseUri, final Map<String, Object> properties) {

        var port = Util.findProperty(properties, KEY_SERVICE_PS_PORT, Integer.class).orElse(baseUri.getPort());
        var path = baseUri.getPath() + URI_SEP
            + trimSlashes(Util.getProperty(properties, KEY_SERVICE_PS_PATH, String.class));
        var host = baseUri.getHost();
        var userInfo = baseUri.getUserInfo();

        try {
            return new URI("http", userInfo, host, port, path, null, null);
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Failed to create Palisade Service URI", e);
        }

    }

    static URI createFilteredResourceUrl(final URI baseUri, final Map<String, Object> properties) {

        var port = Util.findProperty(properties, KEY_SERVICE_FRS_PORT, Integer.class).orElse(baseUri.getPort());
        var path = baseUri.getPath() + URI_SEP
            + trimSlashes(Util.getProperty(properties, KEY_SERVICE_FRS_PATH, String.class));
        var host = baseUri.getHost();
        var userInfo = baseUri.getUserInfo();

        try {
            return new URI("ws", userInfo, host, port, path, null, null);
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Failed to create Filtered Resource Service URI", e);
        }

    }

}
