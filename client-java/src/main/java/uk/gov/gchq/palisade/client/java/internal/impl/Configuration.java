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
package uk.gov.gchq.palisade.client.java.internal.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.client.java.util.Util;

import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This is the main configuration object for a session. Create a new
 * configuration by call {@code Configuration#create(URI)}, passing in the
 * configuration spec.
 * <p>
 * The format of the spec is a URI of the form {@code pal://%cluster-addr%?configkey=%value%}, with the
 * only required key being {@link Configuration#USER_ID} for the userId.
 *
 * @since 0.5.0
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private interface StringProperty<T> {
        T valueOf(String string);
    }

    private static final String QUERY_STRING_SEP = "&";
    private static final String QUERY_STRING_ASSIGN = "=";
    private static final String TOKEN_PARAM = "%25t"; // % is percent-encoded to %25
    private static final int KV_PAIR_LENGTH = 2;

    // Keys for configurable properties
    /**
     * The userId for connecting to the palisade services, will be used for all requests.
     * <p>
     * Required
     */
    public static final String USER_ID = "userid";

    /**
     * Whether to use SSL (https/wss) connection, if supported by the services.
     * <p>
     * Optional, default 'false'
     */
    public static final String SSL_ENABLED = "ssl";

    /**
     * Whether to use HTTP/2 connection (instead of HTTP/1.1), if supported by the services.
     * <p>
     * Optional, default 'false'
     */
    public static final String HTTP2_ENABLED = "http2";

    /**
     * Polling timeout in seconds waiting for a websocket response.
     * <p>
     * Optional, default '3600'
     */
    public static final String POLL_SECONDS = "poll";

    // Allowed user-configurable properties and readers for them (from String to T)
    protected static final Map<String, StringProperty<?>> WHITELIST_PROPERTIES = Map.of(
            USER_ID, String::new,
            SSL_ENABLED, Boolean::valueOf,
            HTTP2_ENABLED, Boolean::valueOf,
            POLL_SECONDS, Long::valueOf
    );

    // Static keys which are not permitted to be configurable
    /**
     * Relative path from cluster root to palisade-service request endpoint.
     * <p>
     * Static, default '/palisade/api/registerDataRequest'
     */
    protected static final String PALISADE_PATH = "palisade.path";
    /**
     * Relative path from cluster root to filtered-resource-service response endpoint, including parameterised token {@link Configuration#TOKEN_PARAM}.
     * <p>
     * Static, default '/filteredResource/resource/%t'
     */
    protected static final String FILTERED_RESOURCE_PATH = "filtered-resource.path";
    /**
     * Relative path from data-service URI as returned by a resource's connection-detail to data-service read endpoint.
     * <p>
     * Static, default '/read/chunked'
     */
    public static final String DATA_PATH = "data.path";
    /**
     * Map from service-names to URIs
     * <p>
     * Static, default empty map
     */
    public static final String DATA_SERVICE_MAP = "data.service-map";

    // Defaults for above static and configurable keys
    protected static final Map<String, Object> DEFAULT_PROPERTIES = Map.of(
            // Static
            PALISADE_PATH, "/palisade/api/registerDataRequest",
            FILTERED_RESOURCE_PATH, "/filteredResource/resource/" + TOKEN_PARAM,
            DATA_PATH, "/read/chunked",
            // Configurable defaults
            SSL_ENABLED, Boolean.FALSE,
            HTTP2_ENABLED, Boolean.FALSE,
            POLL_SECONDS, 3600L
    );

    // Required and derived keys for connection properties
    /**
     * The original URI Spec string passed to the configuration class.
     * <p>
     * Required
     */
    public static final String SPEC_URI = "spec.uri";
    /**
     * Full path to palisade-service request endpoint.
     * <p>
     * Derived, example 'http://my.cluster:1234/ingress/palisade/api/registerDataRequest'
     */
    public static final String PALISADE_URI = "palisade.uri";
    /**
     * Full path to filtered-resource-service response endpoint, before substituting {@link Configuration#TOKEN_PARAM} for the token.
     * <p>
     * Derived, example 'ws://my.cluster:1234/ingress/filteredResource/resource/%t'
     */
    public static final String FILTERED_RESOURCE_URI = "filtered-resource.uri";


    private final Map<String, Object> properties = new TreeMap<>();

    protected Configuration(final Map<String, Object> properties) {
        this.properties.putAll(DEFAULT_PROPERTIES);
        // Override defaults with supplied properties
        this.properties.putAll(properties);
    }

    /**
     * Get a single value from the config map using the given key.
     * Available keys are declared as public static Strings by the {@link Configuration} class.
     *
     * @param key the key for a configmap object
     * @param <T> the expected type of the object
     * @return the value from the configmap
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String key) {
        return (T) Optional.ofNullable(this.properties.get(key))
                .orElseThrow(() -> new ConfigurationException(String.format("Missing value for key '%s'", key)));
    }

    /**
     * Create a config map from a String spec. This must be a URI-compliant string.
     * It is parsed as {@code pal://cluster.addr:port?additionalKey=value&otherKey=otherValue},
     * where cluster.addr:port points to the Palisade cluster's Traefik ingress and any other
     * configuration key/value pairs are passed in as query parameters.
     *
     * @param spec a URI-compliant string of the configuration spec
     * @return a populated config map
     */
    public static Configuration create(final String spec) {
        return create(URI.create(spec));
    }

    /**
     * Create a config map from a URI spec.
     * It is parsed as {@code pal://cluster.addr:port?additionalKey=value&otherKey=otherValue},
     * where cluster.addr:port points to the Palisade cluster's Traefik ingress and any other
     * configuration key/value pairs are passed in as query parameters.
     *
     * @param spec a URI of the configuration spec
     * @return a populated config map
     */
    public static Configuration create(final URI spec) {
        // Parse spec
        var clusterUri = spec.getAuthority() + spec.getPath();
        var queryParams = Optional.ofNullable(spec.getQuery())
                .map(Configuration::parseQueryParams)
                .orElse(Map.of());
        var config = new Configuration(queryParams);

        // Default scheme config
        var palisadeScheme = "http";
        var filteredResourceScheme = "ws";
        var dataScheme = "http";
        // Determine SSL config for URI schemes
        if (config.<Boolean>get(SSL_ENABLED).equals(Boolean.TRUE)) {
            palisadeScheme = "https";
            filteredResourceScheme = "wss";
            dataScheme = "https";
        }

        // Build service URLs
        var palisadeUri = Util.createUri(palisadeScheme + "://" + clusterUri, config.get(PALISADE_PATH));
        var filteredResourceUri = Util.createUri(filteredResourceScheme + "://" + clusterUri, config.get(FILTERED_RESOURCE_PATH));
        URI defaultDataUri = Util.createUri(dataScheme + "://" + clusterUri, "/data");
        // Update config
        config.properties.putAll(Map.of(
                SPEC_URI, spec,
                PALISADE_URI, palisadeUri,
                FILTERED_RESOURCE_URI, filteredResourceUri,
                DATA_SERVICE_MAP, Map.of("data-service", defaultDataUri)
        ));

        // Log properties map for debugging
        LOGGER.debug("Using spec {} built config:", spec);
        config.properties.forEach((key, value) -> LOGGER.debug("{} = {}", key, value));

        // Return config
        return config;
    }

    /**
     * Parse a URI QueryParam string into a Map.
     * e.g. {@code ?key=value&otherKey=otherValue -> (key: value, otherKey: otherValue)}
     *
     * @param queryParamString a {@link URI#getQuery()} string
     * @return a map built from the query string
     */
    private static Map<String, Object> parseQueryParams(final String queryParamString) {
        // Split ?queryParam string over separator - i.e. uri?propA&propB -> [propA, propB]
        return Arrays.stream(queryParamString.split(QUERY_STRING_SEP))
                // Assert each property pair is of the format propKey=propVal
                .map(kvPair -> Optional.of(kvPair.split(QUERY_STRING_ASSIGN))
                        .filter(kvArray -> kvArray.length == KV_PAIR_LENGTH)
                        .map(kvArray -> new SimpleEntry<>(kvArray[0], kvArray[1]))
                        .orElseThrow(() -> new ConfigurationException(String.format("Key-value pair '%s' was not of the format 'key%svalue'", kvPair, QUERY_STRING_ASSIGN))))
                // Assert the key is in the whitelist and the value can be converted
                .map(kvEntry -> Optional.ofNullable(WHITELIST_PROPERTIES.get(kvEntry.getKey()))
                        // Can the value be converted?
                        .map((StringProperty<?> valueClass) -> {
                            try {
                                return (Object) valueClass.valueOf(kvEntry.getValue());
                            } catch (RuntimeException ex) {
                                throw new ConfigurationException(
                                        String.format("Failed to convert value '%s' using converter '%s' from key '%s'", kvEntry.getValue(), valueClass, kvEntry.getKey()),
                                        ex);
                            }
                        })
                        .map(value -> new SimpleEntry<>(kvEntry.getKey(), value))
                        // Did we fail to find a value converter (illegal key)
                        .orElseThrow(() -> new ConfigurationException(String.format("Illegal configuration key '%s'", kvEntry.getKey()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Configuration)) {
            return false;
        }
        final Configuration that = (Configuration) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(properties);
    }

    @Override
    @Generated
    public String toString() {
        return properties.toString();
    }
}
