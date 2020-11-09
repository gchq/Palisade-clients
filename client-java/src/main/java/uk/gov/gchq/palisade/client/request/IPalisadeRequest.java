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

package uk.gov.gchq.palisade.client.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.io.Serializable;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * An instance of {@link PalisadeRequest} is used to wrap all the information
 * that the user needs to supply to the palisade service to register the data
 * access request.
 * <p>
 * Note that the {@link PalisadeRequest} class is created at compile time. The
 * way in which the class is created is determined by the
 * {@code ImmutableStyle}. This class is also compatible with Jackson.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
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
    @SuppressWarnings("java:S3242") // I REALLY want to use UnaryOperator here SonarQube!!!
    static PalisadeRequest createPalisadeRequest(final UnaryOperator<PalisadeRequest.Builder> func) {
        return func.apply(PalisadeRequest.builder()).build();
    }

    /**
     * Returns the resource id
     *
     * @return the resource id
     */
    String getResourceId();

    /**
     * Returns the user id
     *
     * @return the user id
     */
    String getUserId();

    /**
     * Returns the context in support of this request
     *
     * @return the context in support of this request
     */
    Map<String, Object> getContext();

}
