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
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.types.files.StreamedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import uk.gov.gchq.palisade.client.internal.download.DataRequest;
import uk.gov.gchq.palisade.client.testing.ClientTestData.Name;

/**
 * A controller containing our test endpoints
 */
@Controller()
public class HttpEndpointData {

    private static final Logger LOG = LoggerFactory.getLogger(HttpEndpointData.class);

    /**
     * Returns an http response containing an inputstream
     *
     * @param request The request
     * @return an http response containing an inputstream
     */
    @SuppressWarnings("resource")
    @Post("/read/chunked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public HttpResponse<StreamedFile> getTest(@Body final DataRequest request) {

        try {

            // set up MDC
            MDC.put("server", "DT-SVC");

            LOG.debug("RCVD: body: {}", request);

            var octetStream = MediaType.APPLICATION_OCTET_STREAM_TYPE;

            Name nameTuple;
            try {
                var leafResourceId = request.getLeafResourceId();
                nameTuple = Name.from(leafResourceId);
            } catch (IllegalArgumentException e) {
                return HttpResponse.notFound();
            }

            var seed = nameTuple.getSeed();
            var bytes = nameTuple.getBytes();
            var name = nameTuple.getName();

            LOG.debug("LOAD: Created stream of {} bytes for {} from seed value {}", bytes, name, seed);

            var is = nameTuple.createStream();
            var sf = new StreamedFile(is, octetStream, System.currentTimeMillis(), bytes);

            LOG.debug("RETN: Stream");

            return HttpResponse
                .ok(sf)
                .contentType(octetStream);

        } finally {
            MDC.remove("server");
        }
    }

}