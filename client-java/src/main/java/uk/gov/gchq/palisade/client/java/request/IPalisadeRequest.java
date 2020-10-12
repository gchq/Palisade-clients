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

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * <p>
 * An instance of {@code PalisadeRequest} is used to wrap all the information
 * that the user needs to supply to the palisade service to register the data
 * access request.
 * </p>
 * <p>
 * Note that the {@code PalisadeRequest} class is created at compile time. The
 * way in which the class is created is determined by the
 * {@link ImmutableStyle}. This class is also compatible with Jackson.
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = PalisadeRequest.class)
public interface IPalisadeRequest extends Serializable {

    /**
     * Helper method to create a {@code PalisadeRequest} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    public static PalisadeRequest create(UnaryOperator<PalisadeRequest.Builder> func) {
        return func.apply(PalisadeRequest.builder()).build();
    }

    /**
     * Returns the resource id
     *
     * @return the resource id
     */
    public String getResourceId();

    /**
     * Returns the user id
     *
     * @return the user id
     */
    public UserId getUserId();

    /**
     * Returns the context
     *
     * @return the context
     */
    public Context getContext();

    /**
     * Returns the request id
     *
     * @return the request id
     */
    public RequestId getRequestId();

    /**
     * Returns the original request id
     *
     * @return the original request id
     */
    public Optional<RequestId> getOriginalRequestId();

}
