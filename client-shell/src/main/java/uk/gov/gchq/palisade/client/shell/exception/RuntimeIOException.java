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

package uk.gov.gchq.palisade.client.shell.exception;

import java.io.IOException;

/**
 * Wrap an {@link IOException} into a {@link RuntimeException} so it may be thrown at runtime.
 * This is usually done in order to throw inside a lambda.
 */
public class RuntimeIOException extends RuntimeException {
    /**
     * Constructor for a new expection
     * @param message a message describing what might have caused the exception
     * @param cause   wrap the original {@link IOException} thrown
     */
    public RuntimeIOException(final String message, final IOException cause) {
        super(message, cause);
    }
}
