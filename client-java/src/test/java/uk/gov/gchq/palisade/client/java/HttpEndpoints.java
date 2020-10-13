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
package uk.gov.gchq.palisade.client.java;

import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.http.server.types.files.StreamedFile;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.download.DataRequest;
import uk.gov.gchq.palisade.client.java.request.*;

/**
 * A controller containing our test endpoints
 *
 * @author dbell
 */
@Controller()
public class HttpEndpoints {

    private static final Logger log = LoggerFactory.getLogger(HttpEndpoints.class);

    /**
     * Returns a test response from the provide test request
     *
     * @param request The test request
     * @return a test response from the provide test request
     */
    @Post("/registerDataRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<PalisadeResponse> handleDataRequest(@Body final PalisadeRequest request) {
        log.debug("### Test endpoint {} received body: {}", "/registerDataRequest", request);
        return HttpResponse
            .ok(IPalisadeResponse.create(b -> b
                .url("ws://localhost:8082/name")
                .token("abcd-1")))
            .contentType(MediaType.APPLICATION_JSON_TYPE);
    }

//    /**
//     * Returns an http response containing an inputstream. so cool :)
//     *
//     * @param request The request
//     * @return an http response containing an inputstream. so cool :)
//     */
//    @Post("/read/chunked")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    public HttpResponse<byte[]> getTest(@Body final DataRequest request) {
//        log.debug("### Test endpoint {} contected, with body: {}", "/read/chunked", request);
//        var reply = "OneTwo";
//         var is = new ByteArrayInputStream(reply.getBytes(StandardCharsets.UTF_8));
//         var octet_stream = MediaType.APPLICATION_OCTET_STREAM_TYPE;
////         var sf = new StreamedFile(is, octet_stream);
//         return HttpResponse
//             .ok(reply.getBytes())
//             .contentType(octet_stream)
//             .header("Content-Disposition", "inline");
//    }

    /**
     * Returns an http response containing an inputstream. so cool :)
     *
     * @param request The request
     * @return an http response containing an inputstream. so cool :)
     */
    @Post("/read/chunked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public HttpResponse<StreamedFile> getTest(@Body final DataRequest request) {

        log.debug("### Test endpoint {} received body: {}", "/read/chunked", request);

        var octet_stream = MediaType.APPLICATION_OCTET_STREAM_TYPE;

        var filename = request.getLeafResourceId(); // using this as a file name to load a file for streaming

        log.debug("### Trying to load: {}", filename);

        var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

//        if (is == null) {
//            return HttpResponse.badRequest();
//        }

        var sf = new StreamedFile(is, octet_stream);

        return HttpResponse
            .ok(sf)
            .contentType(octet_stream)
            .header("Content-Disposition", filename);
    }

}