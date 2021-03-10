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
package uk.gov.gchq.palisade.client.test.contract.servers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import uk.gov.gchq.palisade.client.internal.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.internal.request.PalisadeResponse;
import uk.gov.gchq.palisade.client.testing.ClientTestData;

/**
 * A controller containing our test endpoints
 */
@Controller("/cluster/palisade/api")
public class HttpEndpointPalisade {

    private static final Logger LOG = LoggerFactory.getLogger(HttpEndpointPalisade.class);

    /**
     * Returns a test response from the provide test request
     *
     * @param request The test request
     * @return a test response from the provide test request
     */
    @Post("/registerDataRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<PalisadeResponse> registerDataRequest(@Body final PalisadeRequest request) {
        try {
            MDC.put("server", "PL-SVC");
            LOG.debug("RCVD: {}", request);
            var palisadeResponse = PalisadeResponse.createPalisadeResponse(b -> b.token(ClientTestData.TOKEN));
            LOG.debug("RETN: {}", request);
            return HttpResponse
                .ok(palisadeResponse)
                .contentType(MediaType.APPLICATION_JSON_TYPE);
        } finally {
            MDC.remove("server");
        }
    }

}