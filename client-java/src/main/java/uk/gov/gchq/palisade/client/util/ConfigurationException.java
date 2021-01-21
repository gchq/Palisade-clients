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
package uk.gov.gchq.palisade.client.util;

import clientException;

/**
 * An exception which is thrown when a configuration error occurs
 *
 * @since 0.5.0
 */
public class ConfigurationException extends ClientException {

    private static final long serialVersionUID = -1663859509675049796L;

    /**
     * Returns a newly created instance with the provided message
     *
     * @param message The message text explaining this exception
     */
    public ConfigurationException(final String message) {
        super(message);
    }

    /**
     * Returns a newly created instance with the provided message and cause
     *
     * @param message The message text explaining this exception
     * @param cause   The underlying cause
     */
    public ConfigurationException(final String message, final Throwable cause) {
        super(message, cause);

    }

}
