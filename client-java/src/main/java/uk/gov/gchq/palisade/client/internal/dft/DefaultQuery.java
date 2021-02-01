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

import uk.gov.gchq.palisade.client.Query;
import uk.gov.gchq.palisade.client.QueryInfo;
import uk.gov.gchq.palisade.client.QueryResponse;
import uk.gov.gchq.palisade.client.internal.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.internal.request.PalisadeService;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static uk.gov.gchq.palisade.client.util.Checks.checkNotNull;

/**
 * The default {@code Query} implementation for subname {@code dft}
 *
 * @since 0.5.0
 */
public class DefaultQuery implements Query {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultQuery.class);

    private static final String PURPOSE_KEY = "purpose";
    private static final String NO_PURPOSE = "";

    private final QueryInfo info;
    private final DefaultSession session;

    /**
     * Creates a new query with the provided {@code session} and query {@code info}
     *
     * @param session The open session to the cluster
     * @param info    The query information
     */
    public DefaultQuery(final DefaultSession session, final QueryInfo info) {
        this.info = checkNotNull(info);
        this.session = checkNotNull(session);
    }

    @Override
    public CompletableFuture<QueryResponse> execute() {

        LOGGER.debug("Executing query: {}", info);

        var palisadeService = new PalisadeService(
            session.getConfiguration().getObjectMapper(),
            session.getConfiguration().getPalisadeUrl());

        var properties = new HashMap<>(info.getProperties());
        properties.put(PURPOSE_KEY, info.getPurpose().orElse(NO_PURPOSE));

        var palisadeRequest = PalisadeRequest.createPalisadeRequest(b -> b
            .resourceId(info.getResourceId())
            .userId(session.getConfiguration().getUser())
            .context(properties));

        return palisadeService
            .submitAsync(palisadeRequest)
            .thenApply(pr -> new DefaultQueryResponse(session, pr));

    }

}
