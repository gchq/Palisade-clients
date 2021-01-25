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
package uk.gov.gchq.palisade.client.download;

/**
 * Root class of client exceptions
 *
 * @since 0.5.0
 */
public class DownloaderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    /**
     * Creates a new instance with the provided {@code message}
     *
     * @param message The message
     * @see RuntimeException#RuntimeException(String)
     */
    public DownloaderException(final String message) {
        super(message);
        statusCode = -1;
    }

    /**
     * Creates a new instance with the provided {@code message} and {@code cause}
     *
     * @param message The message
     * @param cause   The cause
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public DownloaderException(final String message, final Throwable cause) {
        super(message, cause);
        statusCode = -1;
    }

    /**
     * Creates a new instance with the provided {@code message}
     *
     * @param message    The message
     * @param statusCode the HTTP status code or -1 if none
     * @see RuntimeException#RuntimeException(String)
     */
    public DownloaderException(final String message, final int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Creates a new instance with the provided {@code message} and {@code cause}
     *
     * @param message    The message
     * @param statusCode the HTTP status code or -1 if none
     * @param cause      The cause
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public DownloaderException(final String message, final int statusCode, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code or -1 if non present or not applicable
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return this.statusCode;
    }

}
