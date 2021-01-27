/*
 * Copyright 2020-2021 Crown Copyright
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

import uk.gov.gchq.palisade.client.Client;
import uk.gov.gchq.palisade.client.internal.impl.Configuration;
import uk.gov.gchq.palisade.client.util.Checks;

import java.util.HashMap;
import java.util.Map;

/**
 * This client is the default impleentation and responds to the subname of
 * "dft".
 *
 * @since 0.5.0
 */
public class DefaultClient implements Client {

    /**
     * Returns a new instance of {@code DefaultClient}
     */
    public DefaultClient() { // noop
    }

    @Override
    public boolean acceptsURL(final String url) {
        return url.startsWith("pal://");
    }

    @Override
    public DefaultSession connect(final String url, final Map<String, String> info) {
        Checks.checkNotNull(url, "url is null");
        if (!url.startsWith("pal:")) {
            return null;
        }
        // don't write to parameter
        var props = new HashMap<>(info);

        props.put("service.url", url);

        // load the default config and merge in overrides
        var configuration = loadDefaultConfiguration().merge(props);

        return new DefaultSession(configuration);

    }

    private static Configuration loadDefaultConfiguration() {
        return Configuration.fromDefaults();
    }

}
