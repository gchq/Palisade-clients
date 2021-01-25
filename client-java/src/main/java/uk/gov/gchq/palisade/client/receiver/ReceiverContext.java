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
package uk.gov.gchq.palisade.client.receiver;

import uk.gov.gchq.palisade.client.resource.Resource;

import java.util.Optional;

/**
 * Instances of this class are passed to a receiver.
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
     * Returns an optional containing the value for the provided key or empty if not
     * found
     *
     * @param key The key to find
     * @return an optional containing the value for the provided key or empty if not
     *         found
     */
    Optional<Object> findProperty(String key);

    /**
     * Returns the value for the provided key or throws
     * {@code IllegalArgumentException} if not found
     *
     * @param key The key to find
     * @return the value for the provided key or throws
     *         {@code IllegalArgumentException} if not found
     */
    default Object getProperty(final String key) {
        return findProperty(key).orElseThrow(
            () -> new IllegalArgumentException("Property " + key + " not found in client configuration"));
    }

}
