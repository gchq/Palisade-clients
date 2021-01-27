/*
 * Copyright 2020-2021 Crown Copyright
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
package uk.gov.gchq.palisade.client;

import java.io.InputStream;
import java.util.Optional;

/**
 * Represents a download that has been fetched. No data is transferred until
 * {@code #getInputStream()} is called.
 *
 * @since 0.5.0
 */
public interface Download {

    /**
     * Returns an {@code InputStream} which contains the stream of bytes for the
     * file to be downloaded
     *
     * @return the inputstream of bytes
     */
    InputStream getInputStream();

    /**
     * Returns the size of the resource in bytes or -1 if not known. This can be
     * useful for progress.
     *
     * @return the size of the resource in bytes or -1 if not known
     */
    default int getLength() {
        return -1;
    }

    /**
     * Returns the name of the resource
     *
     * @return the name of the resource
     */
    default Optional<String> getName() {
        return Optional.empty();
    }

}
