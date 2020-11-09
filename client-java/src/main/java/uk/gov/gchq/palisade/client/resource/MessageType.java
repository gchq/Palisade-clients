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
package uk.gov.gchq.palisade.client.resource;

/**
 * The type of {@link Message}
 *
 * @since 0.5.0
 */
public enum MessageType {

    /**
     * Server is ready to send a resource
     * Required headers:
     * token: The request token to subscribe to
     */
    RTS,

    /**
     * Client reponds with CTS after it has recived a RTS when it is ready.
     */
    CTS,

    /**
     * An error has occurred on the server
     */
    ERROR,

    /**
     * A resource from the server Required headers: token: The request token to
     * subscribe to
     */
    RESOURCE,

    /**
     * No more resources available for token
     * Required headers:
     * token: The request token to subscribe to
     */
    COMPLETE

}