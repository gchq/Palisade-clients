package uk.gov.gchq.palisade.client.abc.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.abc.Query;
import uk.gov.gchq.palisade.client.abc.QueryResponse;
import uk.gov.gchq.palisade.client.job.state.JobRequest;
import uk.gov.gchq.palisade.client.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.request.PalisadeService;

import java.util.HashMap;
import java.util.Map;

public class DefaultQuery implements Query {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultQuery.class);

    private final JobRequest jobRequest;
    private final DefaultSession session;
    private final Map<String, String> properties;

    public DefaultQuery(final DefaultSession session, final JobRequest jobRequest,
        final Map<String, String> properties) {
        this.jobRequest = jobRequest;
        this.session = session;
        this.properties = properties;
    }

    @Override
    public QueryResponse execute() {

        var palisadeService = new PalisadeService(
            session.getConfiguration().getObjectMapper(),
            session.getConfiguration().getPalisadeUrl());

        var future = palisadeService.submitAsync(createRequest(jobRequest));
        var palisadeResponse = future.join();

        LOGGER.debug("Received palisade response: {}", palisadeResponse);

        return new DefaultQueryResponse(session, palisadeResponse);
    }

    private static PalisadeRequest createRequest(final JobRequest jobConfig) {

        var userId = jobConfig.getUserId();
        var purposeOpt = jobConfig.getPurpose();
        var resourceId = jobConfig.getResourceId();
        var properties = new HashMap<>(jobConfig.getProperties());
        properties.put("PURPOSE", purposeOpt.orElse("client_request"));

        var palisadeRequest = PalisadeRequest.createPalisadeRequest(b -> b
            .resourceId(resourceId)
            .userId(userId)
            .context(properties));

        LOGGER.debug("new palisade request created from job config: {}", palisadeRequest);

        return palisadeRequest;

    }

}
