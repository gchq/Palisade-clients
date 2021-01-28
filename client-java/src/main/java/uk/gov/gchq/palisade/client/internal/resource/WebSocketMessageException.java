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
package uk.gov.gchq.palisade.client.internal.resource;

import uk.gov.gchq.palisade.client.ClientException;
import uk.gov.gchq.palisade.client.internal.resource.WebSocketListener.Item;

/**
 * An instance of this class is thrown if the body of a resource message is not
 * set by the server. This should be very rare.
 *
 * @since 0.5.0
 */
public class WebSocketMessageException extends ClientException {

    private static final long serialVersionUID = 6617992103641504671L;

    private final Item resourceMessage;

    /**
     * Creates a new {@code MissingResourceException} with the provided {@link Item}
     *
     * @param item The offending message
     */
    public WebSocketMessageException(final Item item) {
        super("Received a message with a missing resource. Message was: " + item);
        this.resourceMessage = item;
    }

    /**
     * Returns the message that caused this error
     *
     * @return the message that caused this error
     */
    public Item getResourceMessage() {
        return this.resourceMessage;
    }

}