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
package uk.gov.gchq.palisade.client.java;

import java.util.concurrent.Flow.Publisher;

/**
 * A QueryResponse represents the response after executing a query to Palisade
 *
 * @since 0.5.0
 */
public interface QueryResponse {

    /**
     * Returns a publisher that, once subscribed to, will emit messages from
     * palisade. The stream will emit messages of either
     * {@code MessageType#RESOURCE} or {@code MessageType#ERROR}.
     *
     * @return a publisher that, once subscribed to, will emit messages from
     * palisade
     */
    Publisher<QueryItem> stream();

}
