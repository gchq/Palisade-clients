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
package uk.gov.gchq.palisade.clients.simpleclient.exception;

import java.io.IOException;

/**
 * A {@link StreamingIOException} is a {@link RuntimeException} thrown by the
 * client when a {@link java.util.stream.Stream} encounters an {@link IOException}
 */
public class StreamingIOException extends RuntimeException {

    /**
     * Instantiates a new {@link StreamingIOException} request exception with a {@link Throwable} cause
     * which will then call super and throw a {@link RuntimeException} with an Exception
     *
     * @param cause The underlying cause of this exception
     */
    public StreamingIOException(final IOException cause) {
        super(cause);
    }
}
