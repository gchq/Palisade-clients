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
package uk.gov.gchq.palisade.client.java.job;

import uk.gov.gchq.palisade.client.java.ClientException;

/**
 * A deserialisation error has occured
 *
 * @author dbell
 * @since 0.5.0
 */
public class DeserialiserException extends ClientException {

    private static final long serialVersionUID = 1L;

    /**
     * @see ClientException#ClientException(String)
     */
    public DeserialiserException(String message) {
        super(message);
    }

    /**
     * @see ClientException#ClientException(String, Throwable)
     */
    public DeserialiserException(String message, Throwable cause) {
        super(message, cause);
    }


}
