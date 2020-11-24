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

import uk.gov.gchq.palisade.client.resource.MessageType;

import java.io.InputStream;
import java.util.Map;

/**
 * A {@link Receiver} is an object that will be provided with an input stream to
 * process. Implementations could write to a file or simply log the data.
 *
 * @since 0.5.0
 */
public interface Receiver {

    /**
     * A result returned from a receiver
     *
     * @since 0.5.0
     */
    public interface IReceiverResult {

        /**
         * Property key for the number of bytes downloaded
         */
        String BYTES_KEY = "bytes";

        /**
         * Property key for the path where downloads are saved
         */
        String PATH_KEY = "path";

        /**
         * Property key for the filename
         */
        String FILENAME_KEY = "filename";

        /**
         * Returns the type of this message
         *
         * @return the {@link MessageType}
         */
        Map<String, String> getProperties();

    }

    /**
     * Process the provided InputStream
     *
     * @param receiverContext The context
     * @param inputStream     the stream to process
     * @return the result
     * @throws ReceiverException if an erro occurs during processing
     */
    IReceiverResult process(ReceiverContext receiverContext, InputStream inputStream) throws ReceiverException;

}
