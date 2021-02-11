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
package uk.gov.gchq.palisade.client.testing;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;

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
    @SuppressWarnings("resource")
    @Post("/read/chunked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public HttpResponse<StreamedFile> getTest(@Body final DataRequest request) {

        try {

            // set up MDC
            MDC.put("server", "DTA-SVC");

            LOG.debug("RCVD: body: {}", request);

            MediaType octetStream = MediaType.APPLICATION_OCTET_STREAM_TYPE;

            String filename = request.getLeafResourceId(); // using this as a file name to load a file for streaming

            LOG.debug("LOAD: Loading: {}", filename);

            FileInputStream is;

            try {
                var url = Thread.currentThread().getContextClassLoader().getResource(filename);
                if (url == null) {
                    return HttpResponse.notFound();
                }
                var uri = url.toURI();
                var file = new File(uri);
                is = new FileInputStream(file);
            } catch (URISyntaxException | FileNotFoundException e1) {
                return HttpResponse.notFound();
            }

            LOG.debug("LOAD: Loaded OK: {}", filename);

            StreamedFile sf = new StreamedFile(
                is,
                octetStream,
                System.currentTimeMillis(),
                -1)
                    .attach(Path.of(filename).getFileName().toString());

            LOG.debug("RETN: Stream");

            return HttpResponse
                .ok(sf)
                .contentType(octetStream);
        } finally {
            MDC.remove("server");
        }
    }

}