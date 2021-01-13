/*
 * Copyright 2020 Crown Copyright
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
package uk.gov.gchq.palisade.client.abc.impl;

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

import uk.gov.gchq.palisade.client.download.DataRequest;

import java.io.InputStream;

/**
 * A controller containing our test endpoints
 */
@Controller("data")
public class HttpEndpointData {

    private static final Logger LOG = LoggerFactory.getLogger(HttpEndpointData.class);

    /**
     * Returns an http response containing an inputstream
     *
     * @param request The request
     * @return an http response containing an inputstream
     */
    @Post("/read/chunked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public HttpResponse<StreamedFile> getTest(@Body final DataRequest request) {

        LOG.debug("### Test endpoint {} received body: {}", "/read/chunked", request);

        MediaType octetStream = MediaType.APPLICATION_OCTET_STREAM_TYPE;

        String filename = request.getLeafResourceId(); // using this as a file name to load a file for streaming

        LOG.debug("### Trying to load: {}", filename);

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        LOG.debug("### Loaded OK: {}", filename);

        if (is == null) {
            return HttpResponse.notFound();
        }

        StreamedFile sf = new StreamedFile(is, octetStream).attach("filename");

        LOG.debug("### Returning stream");

        return HttpResponse
                .ok(sf)
                .contentType(octetStream);
    }

}