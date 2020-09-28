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
package uk.gov.gchq.palisade.client.java.resource;

import uk.gov.gchq.palisade.client.java.ClientException;

public class SendMessageException extends ClientException {

    private static final long serialVersionUID = 6617992103641504671L;

    private final Message resourceMessage;

    public SendMessageException(Message resourceMessage, Throwable t) {
        super("Recieved a message with a missing resource. Message was: " + resourceMessage, t);
        this.resourceMessage = resourceMessage;
    }

    public Message getResourceMessage() {
        return this.resourceMessage;
    }

}
