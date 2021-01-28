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

/**
 * An error representing some processing problem while processing the query.
 * This could range from simple no access error or a more serious problem
 * processing a resource.
 *
 * @since 0.5.0
 */
public interface Error extends Message {

    /**
     * Returns the text describing the error
     *
     * @return the text describing the error
     */
    String getText();

}
