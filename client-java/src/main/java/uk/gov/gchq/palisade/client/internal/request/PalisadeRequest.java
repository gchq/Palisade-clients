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
package uk.gov.gchq.palisade.client.internal.request;

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
 * Note that the {@link ImmutablePalisadeRequest} implementation class is
 * generated at compile time. The way in which the class is created is
 * determined by the {@code ImmutableStyle}. The generated class is package
 * private hence the nested {@code Builder}.
 * <p>
 * This class is also compatible with Jackson.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutablePalisadeRequest.class)
public interface PalisadeRequest extends Serializable {

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutablePalisadeRequest.Builder { // empty
    }

    /**
     * Helper method to create a {@code PalisadeRequest} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    @SuppressWarnings("java:S3242")
    static PalisadeRequest createPalisadeRequest(final UnaryOperator<Builder> func) {
        return func.apply(new Builder()).build();
    }

    /**
     * Returns the resource id. There will always be a value present as null will
     * never be returned.
     *
     * @return the resource id
     */
    String getResourceId();

    /**
     * Returns the user id. There will always be a value present as null will never
     * be returned.
     *
     * @return the user id
     */
    String getUserId();

    /**
     * Returns the context in support of this request. If there is no context
     * present, then an empty map is returned. This method will never return null.
     *
     * @return the context in support of this request
     */
    Map<String, String> getContext();

}
