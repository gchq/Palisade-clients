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
import uk.gov.gchq.palisade.client.ClientManager;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * This client is the default implementation and responds to the subname of
 * "dft".
 *
 * @since 0.5.0
 */
public class DefaultClient implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClient.class);

    /**
     * There is only ever a single instance of the client. This is the instance that
     * is passed to the {@code ClientManager}.
     */
    private static final DefaultClient INSTANCE = new DefaultClient();

    private static boolean registered;

    /*
     * When this class is loaded by a class loader we need to register it with the
     * {@code ClientManager}.
     */
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
        boolean accepts = url.startsWith("pal://");
        if (!accepts) {
            LOGGER.debug("Client {} does not accept url {}", this.getClass().getName(), url);
        }
        return accepts;
    }

    @Override
    public DefaultSession connect(final String url) {
        if (!acceptsURL(url)) {
            return null;
        }

        // load the default configuration and merge in overrides
        var configuration = Configuration.create(url);

        return new DefaultSession(configuration);
    }

    /**
     * Loads the single instance of this client in to th {@code ClientManager}. This
     * method is synchronised as there could be multiple class loaders trying to
     * load the class.
     *
     * @return The single instance
     */
    @SuppressWarnings("java:S2221")
    private static synchronized Client load() {
        try {
            if (!registered) {
                registered = true;
                ClientManager.registerClient(INSTANCE);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register {}", DefaultClient.class.getName(), e);
        }
        return INSTANCE;
    }

}
