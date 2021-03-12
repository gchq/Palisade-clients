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

package uk.gov.gchq.palisade.client;

import uk.gov.gchq.palisade.resource.LeafResource;

/**
 * A QueryItem represents a single available resource as reported by the Filtered-Resource-Service, or an error.
 * This may be then downloaded using the {@link Session#fetch(QueryItem)}.
 *
 * @since 0.5.0
 */
public interface QueryItem {
    /**
     * The ItemType is the type of the QueryItem received.
     * This is a narrowing of the full range of responses from the Filtered-Resource-Service
     * to only the output types the {@link QueryResponse} flow will emit.
     */
    enum ItemType {
        RESOURCE,
        ERROR
    }

    /**
     * Get the type of this QueryItem, either a leaf resource or an error message
     *
     * @return the QueryItem's type
     */
    ItemType getType();

    /**
     * Get the token to pass to the Data-Service if fetching this item
     *
     * @return the token from the palisade-service that will be sent to the data-service
     */
    String getToken();

    /**
     * Get this item's content as an error message
     *
     * @return the error message if this item's {@link #getType()} was a {@link ItemType#ERROR}, null otherwise
     */
    String asError();

    /**
     * Get this item's content as a leaf resource
     *
     * @return the resource if this item's {@link #getType()} was a {@link ItemType#RESOURCE}, null otherwise
     */
    LeafResource asResource();
}
