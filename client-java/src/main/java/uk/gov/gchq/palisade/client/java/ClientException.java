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

/**
 * The base {@code Throwable} type for Palisade Clients.
 *
 * @since 0.5.0
 */
public class ClientException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance with the provided {@code message}
     *
     * @param message a description of the exception
     * @see RuntimeException#RuntimeException(String)
     */
    public ClientException(final String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the provided {@code message} and {@code cause}
     *
     * @param message a description of the exception
     * @param cause   the underlying reason for this {@code ClientException} (which
     *                is saved for later retrieval by the getCause() method); may be
     *                null indicating the cause is non-existent or unknown.
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public ClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
