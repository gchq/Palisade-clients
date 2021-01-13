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
package uk.gov.gchq.palisade.client.job;

import uk.gov.gchq.palisade.client.job.state.SavedJobState;
import uk.gov.gchq.palisade.client.resource.ResourceMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * A future result is returned from the client upon submitting a job
 *
 * @since 0.5.0
 */
public interface Result {

    /*
     * This class will in the future provide access to the actual configuration and
     * the resource requested. It will also contain a future that will signal the
     * completion of all processing for the job.
     *
     * Information provided will be information about each downloaded resource and
     * the path to the file if there is one.
     */

    /**
     * Returns a future that when successfully completed will return the final job
     * state
     *
     * @return a future that when successfully completed will return the final job
     *         state
     */
    CompletableFuture<SavedJobState> future();

    /**
     * Returns a producer that emits {@code RetrivedResource} objects as they
     * become available from the Filtered Resource Service. Resources will only
     * start being emitted once the publisher is subscribed to. This means that the
     * communication to the Filtered resource Service is deferred. Note that this
     * Publisher is "hot" in that if a second subscription is received later, it
     * will start at that point and will not emit previous resources.
     *
     * @return a producer that emits {@code RetrivedRTesource} objects as they
     *         become available from the Filtered Resource Service
     */
    Publisher<ResourceMessage> resources();

    Publisher<Error> errors();

}
