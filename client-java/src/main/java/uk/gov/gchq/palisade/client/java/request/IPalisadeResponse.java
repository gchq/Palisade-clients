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
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.annotation.*;

/**
 * <p>
 * An instance of {@code PalisadeResponse} is returned after a successful
 * request to the Palisade Service. This object contains the url and the token
 * which is to be used to contact the Filtered Resource Service and wait for
 * resources to become available.
 * </p>
 * <p>
 * Note that the {@code PalisadeResponse} class is created at compile time. The
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
@JsonDeserialize(as = PalisadeResponse.class)
@JsonSerialize(as = PalisadeResponse.class)
public interface IPalisadeResponse extends Serializable {

    /**
     * Helper method to create a {@code PalisadeResponse} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    public static PalisadeResponse create(UnaryOperator<PalisadeResponse.Builder> func) {
        return func.apply(PalisadeResponse.builder()).build();
    }

    /**
     * Returns the url of the websocket endpoint to contact when waiting for
     * resources
     *
     * @return the url of the websocket endpoint to contact when waiting for
     *         resources
     */
    public String getUrl();

    /**
     * Returns the unique token representing this request/response pair
     *
     * @return the unique token representing this request/response pair
     */
    public String getToken();

}
