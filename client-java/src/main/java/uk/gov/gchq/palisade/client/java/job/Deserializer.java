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

import java.io.InputStream;

/**
 * A deserialiser
 *
 * @author dbell
 * @since 0.5.0
 * @param <E> The type
 */
public interface Deserializer<E> {

    /**
     * Returns an instance of {@code E} from the provide {@code InputStream}
     *
     * @param inputStream The source
     * @return an instance of {@code E} from the provide {@code InputStream}
     */
    E deserialize(InputStream inputStream);

    /**
     * Returns an instance of {@code E} from the provide byte array
     *
     * @param bytea The source
     * @return an instance of {@code E} from the provide byte array
     */
    default E deserialize(byte[] bytea) {
        return deserialize(bytea);
    }

}
