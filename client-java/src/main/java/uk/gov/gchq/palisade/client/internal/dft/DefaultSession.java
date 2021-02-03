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

import uk.gov.gchq.palisade.client.Download;
import uk.gov.gchq.palisade.client.Resource;
import uk.gov.gchq.palisade.client.Session;
import uk.gov.gchq.palisade.client.internal.download.Downloader;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;

import java.util.Map;

/**
 * A session for the "dft" subname
 *
 * @since 0.5.0
 */
public class DefaultSession implements Session {

    private final Configuration configuration;

    /**
     * Returns a new instance of {@code DefaultSession} with the provided
     * {@code configuration}
     *
     * @param configuration The client configuration
     */
    public DefaultSession(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public DefaultQuery createQuery(final String queryString, final Map<String, String> properties) {
        return new DefaultQuery(this, queryString, properties);
    }

    /**
     * Returns the configuration for this session
     *
     * @return the configuration for this session
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Download fetch(final Resource resource) {
        return new Downloader(configuration.getObjectMapper(), configuration.getDataPath()).fetch(resource);
    }


}
