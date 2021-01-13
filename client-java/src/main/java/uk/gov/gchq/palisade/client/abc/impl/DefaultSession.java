package uk.gov.gchq.palisade.client.abc.impl;

import uk.gov.gchq.palisade.client.abc.Query;
import uk.gov.gchq.palisade.client.abc.Session;
import uk.gov.gchq.palisade.client.job.state.JobRequest;

import java.util.Map;

public class DefaultSession implements Session {

    private final Configuration configuration;

    /**
     * @param configuration
     * @param objectMapper
     */
    public DefaultSession(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Query createQuery(final String resourceId) {
        return createQuery(resourceId, Map.of());
    }

    @Override
    public Query createQuery(final String resourceId, final Map<String, String> properties) {
        var request = JobRequest.createJobRequest(b -> b
            .userId(configuration.getUser())
            .resourceId(resourceId)
            .properties(properties));
        var query = new DefaultQuery(this, request, properties);
        return query;
    }

    Configuration getConfiguration() {
        return this.configuration;
    }

}
