/*
 * Copyright 2020-2021 Crown Copyright
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
package uk.gov.gchq.palisade.client.internal.download;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.util.function.UnaryOperator;

/**
 * An instance of a {@link DataRequest} represents a request to the data service
 * in order to initiate a download.
 * <p>
 * Note that the {@link DataRequest} class is created at compile time. The way
 * in which the class is created is determined by the {@code ImmutableStyle}.
 * The {@link JsonDeserialize} is so that Jackson can use the generated builder
 * upon deserialisation.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableDataRequest.class)
@JsonSerialize(as = ImmutableDataRequest.class)
public interface DataRequest {

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutableDataRequest.Builder { // empty
    }

    /**
     * Helper method to create a {@code DataRequest} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    static DataRequest createDataRequest(final UnaryOperator<Builder> func) {
        return func.apply(new Builder()).build();
    }

    /**
     * Returns the token
     *
     * @return the token
     */
    String getToken();

    /**
     * Returns the leaf resource to be downloaded
     *
     * @return the leaf resource to be downloaded
     */
    String getLeafResourceId();


}
