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
package uk.gov.gchq.palisade.client.internal.dft;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import uk.gov.gchq.palisade.client.Download;
import uk.gov.gchq.palisade.client.Resource;
import uk.gov.gchq.palisade.client.Session;
import uk.gov.gchq.palisade.client.internal.download.Downloader;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.util.Map;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * A session for the "dft" subname
 *
 * @since 0.5.0
 */
public class DefaultSession implements Session {

    private final Configuration configuration;

    /*
     * Once created, an HttpClient instance is immutable, thus automatically
     * thread-safe, and multiple requests can be sent with it
     */
    private final HttpClient httpClient;

    /*
     * Shared object mapper passed to downstream services
     */
    private final ObjectMapper objectMapper;

    /**
     * Returns a new instance of {@code DefaultSession} with the provided
     * {@code configuration}
     *
     * @param configuration The client configuration
     */
    public DefaultSession(final Configuration configuration) {

        this.configuration = configuration;

        var httpClientBuilder = HttpClient.newBuilder();
        if (!configuration.isHttp2Enabled()) {
            httpClientBuilder.version(Version.HTTP_1_1);
        }
        this.httpClient = httpClientBuilder.build();

        this.objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            // comment out the 3 include directives below to tell jackson to output all
            // attributes, even if null, absent or empty (e.g. empty optional and
            // collection)
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_ABSENT)
            .setSerializationInclusion(Include.NON_EMPTY)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @SuppressWarnings("java:S1774")
    @Override
    public DefaultQuery createQuery(final String queryString, final Map<String, String> properties) {
        checkNotNull(queryString, "Missing query");
        return new DefaultQuery(this, queryString, properties != null ? properties : Map.of());
    }

    @Override
    public Download fetch(final Resource resource) {
        checkNotNull(resource, "Missing resource");
        var downloader = Downloader.createDownloader(b -> b
            .httpClient(getHttpClient())
            .objectMapper(getObjectMapper())
            .path(configuration.getDataPath()));
        return downloader.fetch(resource);
    }

    /**
     * Returns the shared {@code HttpClient} for this session
     *
     * @return the shared {@code HttpClient} for this session
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Returns the configuration for this session
     *
     * @return the configuration for this session
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the shared object mapper
     *
     * @return the shared object mapper
     */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

}
