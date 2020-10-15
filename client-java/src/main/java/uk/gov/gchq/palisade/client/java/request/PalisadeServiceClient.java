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
package uk.gov.gchq.palisade.client.java.request;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.annotation.Client;

/**
 * <p>
 * This class is a declarative HTTP Client which is generated at compile time.
 * It is responsible for making requests to the Palisade Service endpoint. The
 * url is configured at runtime via the provided property provided on the
 * {@code @Client} annotation.
 * </p>
 * <p>
 * This client can be either injected where needed (if the type being injected
 * into is itself injected) or looked up via the {@code ApplicationContext} via:
 *
 * <pre>
 * {
 *     &#64;code
 *     var client = appCtx.getBean(PalisadeServiceClient.class);
 * }
 * </pre>
 *
 * @since 0.5.0
 * @see "https://docs.micronaut.io/latest/guide/index.html#clientAnnotation"
 */
@Client("${palisade.client.url}")
public interface PalisadeServiceClient {

    /**
     * The endpoint
     */
    @SuppressWarnings("java:S1214")
    String REGISTER_DATA_REQUEST = "/registerDataRequest";

    /**
     * Submits a request to the palisade service and returns the response. The
     * reponse contains details of how to connect to the Filtered Resource Service
     * and wait for results.
     *
     * @param request The request
     * @return the response
     */
    @Post(REGISTER_DATA_REQUEST)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    HttpResponse<PalisadeResponse> registerDataRequestSync(@Body final PalisadeRequest request);

}
