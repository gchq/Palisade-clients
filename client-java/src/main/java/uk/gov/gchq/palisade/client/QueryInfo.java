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

import java.util.Map;
import java.util.Optional;

/**
 * An instance of {@link QueryInfo} is passed during the submission of a new
 * query.
 *
 * @since 0.5.0
 */
public interface QueryInfo {

    /**
     * Returns the resource id
     *
     * @return the resource id
     */
    String getResourceId();

    /**
     * Returns the purpose
     *
     * @return the purpose
     */
    Optional<String> getPurpose();

    /**
     * Returns a map of properties that will be passed to the Palisade Service
     *
     * @return a map of properties that will be passed to the Palisade Service
     */
    Map<String, String> getProperties();


}
