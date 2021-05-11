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

import java.util.Map;

/**
 * A session represents a connection to palisade
 *
 * @since 0.5.0
 */
public interface Session {

    /**
     * Returns a new query
     *
     * @param queryString The query string
     * @return a new query
     */
    default Query createQuery(final String queryString) {
        return createQuery(queryString, Map.of());
    }

    /**
     * Returns a new query
     *
     * @param queryString The query string
     * @param properties  The properties for this query
     * @return a new query
     */
    Query createQuery(String queryString, Map<String, String> properties);

    /**
     * Returns a new download of the provided resource
     *
     * @param queryItem A {@link QueryItem} with type {@link QueryItem.ItemType#RESOURCE}, representing a resource to download
     * @return a new download of the provided resource
     */
    Download fetch(QueryItem queryItem);

}
