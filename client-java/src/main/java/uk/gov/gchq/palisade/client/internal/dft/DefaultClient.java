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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.Client;
import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.ClientManager;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * This client is the default implementation and responds to the subname of
 * "dft".
 *
 * @since 0.5.0
 */
public class DefaultClient implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClient.class);
    private static final DefaultClient INSTANCE = new DefaultClient();

    private static boolean registered;

    static {
        load();
    }

    /**
     * Returns a new instance of {@code DefaultClient}
     */
    public DefaultClient() { // noop
    }

    @Override
    public boolean acceptsURL(final String url) {
        checkNotNull(url, "url is null");
        boolean accepts = url.startsWith("pal://") || url.startsWith("pal:dft://");
        if (!accepts) {
            LOGGER.debug("Client {} does not accept url {}", this.getClass().getName(), url);
        }
        return accepts;
    }

    @Override
    public DefaultSession connect(final String url, final Map<String, String> info) {
        if (!acceptsURL(url)) {
            return null;
        }
        // copy incoming info
        var props = new HashMap<>(info);
        props.put("service.url", url);

        // load the default configuration and merge in overrides
        var configuration = Configuration.create(props);

        return new DefaultSession(configuration);
    }

    private static synchronized Client load() {
        try {
            if (!registered) {
                registered = true;
                ClientManager.registerClient(INSTANCE);
            }
        } catch (ClientException e) {
            LOGGER.error("Failed to register " + DefaultClient.class.getName(), e);
        }
        return INSTANCE;
    }

}
