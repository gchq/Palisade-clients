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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
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
@JsonDeserialize(as = Error.class)
@JsonSerialize(as = Error.class)
public interface IError extends Serializable {

    /**
     * Helper method to create a {@link Error} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static Error create(final UnaryOperator<Error.Builder> func) {
        return func.apply(Error.builder()).build();
    }

    /**
     * Returns the type of this message
     *
     * @return the {@link MessageType}
     */
    MessageType getText();

}
