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
package uk.gov.gchq.palisade.client;

/**
 * The base type of all returned messages
 *
 * @since 0.5.0
 */
public interface Message {

    /**
     * Returns the type of message
     *
     * @return the type of message
     */
    MessageType getMessageType();

    /**
     * Returns the token. All messages will have a token associated with them.
     *
     * @return the token.
     */
    String getToken();

}
