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

package uk.gov.gchq.palisade.client.java.request;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.io.Serializable;
import java.util.function.UnaryOperator;

/**
 * <p>
 * This is the request id instance which is part of a {@code PalisadeRequest}
 * </p>
 * <p>
 * Note that the {@code RequestId} class is created at compile time. The way in
 * which the class is created is determined by the {@link ImmutableStyle}. This
 * class is also compatible with Jackson.
 * </p>
 *
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(builder = RequestId.Builder.class)
public interface IRequestId extends Serializable {

    /**
     * Helper method to create a {@code RequestId} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static RequestId create(final UnaryOperator<RequestId.Builder> func) {
        return func.apply(RequestId.builder()).build();
    }

    /**
     * Helper method to create a new {@code RequestId} using "tuple" style.
     *
     * @param requestId The request id
     * @return a newly created {@code RequestId}
     */
    static RequestId of(final String requestId) {
        return create(rid -> rid.id(requestId));
    }

    /**
     * Returns the request id
     *
     * @return the request id
     */
    String getId();

}
