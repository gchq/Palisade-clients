package uk.gov.gchq.palisade.client.java;

import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.reactivex.Flowable;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.data.DataRequest;
import uk.gov.gchq.palisade.client.java.request.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A controller containing our test endpoints
 *
 * @author dbell
 */
@Controller()
public class HttpEndpoints {

    private static final Logger log = LoggerFactory.getLogger(HttpEndpoints.class);

    @Post("/registerDataRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public PalisadeResponse handleDataRequest(@Body final PalisadeRequest request) {
        return IPalisadeResponse
            .create(b -> b
                .url("ws://localhost:8082/name")
                .token("abcd-1"));
    }

    /**
     * Returns an http response containing an inputstream. so cool :)
     *
     * @return an http response containing an inputstream. so cool :)
     */
    @Post("/read/chunked")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public HttpResponse<Flowable<String>> getTest(@Body final DataRequest request) {
        log.debug("Test endpoint {} contected, with body: {}", "/read/chunked", request);
        var reply = "woohoo";
        var is = new ByteArrayInputStream(reply.getBytes(StandardCharsets.UTF_8));
        return HttpResponse
                .ok(Flowable.fromIterable(List.of("One", "Two")))
                .contentType("image/jpeg")
                .header("Content-Disposition", "inline");
    }

}