package uk.gov.gchq.palisade.client.abc.impl;

import uk.gov.gchq.palisade.client.abc.Client;
import uk.gov.gchq.palisade.client.util.Checks;

import java.util.HashMap;
import java.util.Map;

public class DefaultClient implements Client {

    public DefaultClient() { // noop
    }

    @Override
    public boolean acceptsURL(final String url) {
        return url.startsWith("pal://");
    }

    @Override
    public DefaultSession connect(final String url, final Map<String, String> info) {
        Checks.checkArgument(url, "url is null");
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
