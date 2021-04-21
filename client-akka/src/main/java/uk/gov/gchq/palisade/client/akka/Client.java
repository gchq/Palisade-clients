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

package uk.gov.gchq.palisade.client.akka;

import uk.gov.gchq.palisade.resource.LeafResource;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Publisher;

/**
 * Interface for a palisade client that defines the connections to the three outward-facing services.
 * The expected interaction would be {@link #register} then {@link #fetch} then {@link #read}.
 */
public interface Client {

    /**
     * Register a request with the palisade-service entrypoint.
     *
     * @param userId     the userId of the user making the request.
     * @param resourceId the resourceId requested to read - note this is not necessarily the filename.
     * @param context    the context for this data access.
     * @return the token from the palisade-service which may be used in the following methods.
     */
    CompletionStage<String> register(final String userId, final String resourceId, final Map<String, String> context);

    /**
     * Fetch the returned {@link LeafResource}s from the filtered-resource-service.
     * These resources, coupled with the token, are authorised to be read by the data-service.
     *
     * @param token the token returned from the palisade-service by the {@link #register} method.
     * @return reactive streams {@link Publisher} that will request and return {@link LeafResource} results from the filtered-resource-service.
     */
    Publisher<LeafResource> fetch(final String token);

    /**
     * Read a single resource from the appropriate data-service specified by the resource's {@link uk.gov.gchq.palisade.resource.ConnectionDetail}.
     *
     * @param token    the token returned from the palisade-service by the {@link #register(String, String, Map)} method.
     * @param resource a resource returned by the filtered-resource-service that the client wishes to read.
     * @return an {@link InputStream} to that resource, with the data-service applying all appropriate rules.
     */
    InputStream read(final String token, final LeafResource resource);

}
