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
package uk.gov.gchq.palisade.client.internal.resource;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * A websocket message is the actual instance sent/received to/from the
 * websocket
 *
 * @since 0.5.0
 */
@ImmutableStyle
public interface WebSocketMessage extends Serializable {

    /**
     * Helper method to create a {@link CompleteMessage} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242")
    static CompleteMessage createComplete(final UnaryOperator<CompleteMessage.Builder> func) {
        return func.apply(new CompleteMessage.Builder()).build();
    }

    /**
     * Helper method to create a {@link ErrorMessage} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242")
    static ErrorMessage createError(final UnaryOperator<ErrorMessage.Builder> func) {
        return func.apply(new ErrorMessage.Builder()).build();
    }

    /**
     * Helper method to create a {@link ResourceMessage} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    @SuppressWarnings("java:S3242")
    static ResourceMessage createResource(final UnaryOperator<ResourceMessage.Builder> func) {
        return func.apply(new ResourceMessage.Builder()).build();
    }

    /**
     * Returns the token to which this error belongs
     *
     * @return the token to which this error belongs
     */
    String getToken();

    /**
     * Returns any extra properties for this message or an empty map if there are
     * none
     *
     * @return any extra properties for this message or an empty map if there are
     *         none
     */
    Map<String, String> getProperties();

}
