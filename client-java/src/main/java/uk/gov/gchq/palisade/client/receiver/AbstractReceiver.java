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
package uk.gov.gchq.palisade.client.receiver;

/**
 * Super class of receivers
 *
 * @since 0.5.0
 */
public abstract class AbstractReceiver implements Receiver {

    /**
     * Property key for the number of bytes downloaded
     */
    public static final String BYTES_KEY = "bytes";

    /**
     * Property key for the path where downloads are saved
     */
    public static final String PATH_KEY = "path";

    /**
     * Property key for the filename
     */
    public static final String FILENAME_KEY = "filename";

    /**
     * Create the instance
     */
    @SuppressWarnings("java:S1118")
    public AbstractReceiver() {
    }

}
