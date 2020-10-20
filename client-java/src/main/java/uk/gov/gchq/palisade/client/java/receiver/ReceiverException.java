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
 * Thrown when an inputstream receiver fails to complete
 *
 * @since 0.5.0
 */
public class ReceiverException extends Exception {
    private static final long serialVersionUID = 1L;

    private final Resource resource;

    /**
     * Creates a new instance with the provided {@code message} and {@code cause}
     *
     * @param resource The resource being received
     * @param message  The message
     * @param cause    The cause
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public ReceiverException(final Resource resource, final String message, final Throwable cause) {
        super(message, cause);
        this.resource = resource;
    }

    /**
     * Returns the resource that the receiver was handling at the time this
     * exception was thrown
     *
     * @return the resource that the receiver was handling
     */
    public Resource getResource() {
        return this.resource;
    }

}
