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
package uk.gov.gchq.palisade.client.request;

import java.util.concurrent.CompletableFuture;

/**
 * Instances of this class wrap the generated palisade service client
 *
 * @since 0.5.0
 */
public interface PalisadeClient {

    /**
     * Submit the provided request to Palisade
     *
     * @param request The request to submit
     * @return the response
     */
    PalisadeResponse submit(final PalisadeRequest request);

    /**
     * Submit the provided request to Palisade
     *
     * @param request The request to submit
     * @return the response
     */
    CompletableFuture<PalisadeResponse> submitAsync(final PalisadeRequest request);

}
