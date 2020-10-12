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
package uk.gov.gchq.palisade.client.java.state;

import uk.gov.gchq.palisade.client.java.ClientException;

/**
 * A {@code StateException} is thrown when any error occurs when retrieving or
 * updating states for a job (token). This could occur if a state is requested
 * for a token that does not exist.
 *
 * @author dbell
 * @since 0.5.0
 */
public class StateException extends ClientException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code StateException} with the provided message
     *
     * @param message The message
     */
    public StateException(String message) {
        super(message);
    }

}
