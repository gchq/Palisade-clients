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
package uk.gov.gchq.palisade.client.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * The {@code Message} object is used in the communication between the client
 * and the Filtered Resource Service over a WebSocket.
 * <p>
 * Note that the {@code UserId} class is created at compile time. The way in
 * which the class is created is determined by the {@code ImmutableStyle}. This
 * class is also compatible with Jackson.
 *
 * @see ResourceClient
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = Message.class)
@JsonSerialize(as = Message.class)
public interface IMessage extends Serializable {

    /**
     * Helper method to create a {@link Message} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    static Message createMessage(final UnaryOperator<Message.Builder> func) {
        return func.apply(Message.builder()).build();
    }

    /**
     * Returns the type of this message
     *
     * @return the {@link MessageType}
     */
    MessageType getType();

    /**
     * Returns the headers for this message an empty map if there are none
     *
     * @return the headers for this message an empty map if there are none
     */
    Map<String, String> getHeaders();

    /**
     * Returns the body of this message or empty if there is none. At this point
     * there should only be a body for {@link MessageType} of RESOURCE.
     *
     * @return the body of this message or empty if there is none
     */
    Optional<Object> getBody();

}