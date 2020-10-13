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
package uk.gov.gchq.palisade.client.java.receiver;

import uk.gov.gchq.palisade.client.java.resource.Resource;

/**
 * <p>
 * Instances of this class are passed to a receiver.
 * </p>
 *
 * @since 0.5.0
 */
public interface ReceiverContext {

    /**
     * Returns the resource that is being received
     *
     * @return the resource that is being received
     */
    Resource getResource();

    /**
     * Returns an instance of {@code type} for this jobs registry. This could be
     * used to return an ObjectMapper for example.
     *
     * @param <T>  The type of object to return
     * @param type The class of the instance
     * @return an instance of {@code type} for this jobs registry
     */
    <T> T get(Class<T> type);

}