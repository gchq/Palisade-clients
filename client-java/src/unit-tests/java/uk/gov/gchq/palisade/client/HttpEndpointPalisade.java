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
package uk.gov.gchq.palisade.client;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.request.PalisadeRequest;
import uk.gov.gchq.palisade.client.request.PalisadeResponse;

import static uk.gov.gchq.palisade.client.request.IPalisadeResponse.createPalisadeResponse;

/**
 * A controller containing our test endpoints
 */
@Controller("/cluster/palisade")
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
        LOG.debug("### Test endpoint {} received body: {}", "/registerDataRequest", request);
        return HttpResponse
            .ok(createPalisadeResponse(b -> b.token("abcd-1")))
            .contentType(MediaType.APPLICATION_JSON_TYPE);
    }

}