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

package uk.gov.gchq.palisade.client.java.download;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.function.UnaryOperator;

/**
 * <p>
 * An instance of a {@code DataRequest} represents a request to the data service
 * in order to initiate a download.
 * </p>
 * <p>
 * Note that the {@code DataRequest} class is created at compile time. The way
 * in which the class is created is determined by the {@link ImmutableStyle}.
 * The {@code JsonDeserialize} so that Jackson can use the generated builder
 * upon deserialisation.
 * </p>
 *
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = DataRequest.class)
@JsonSerialize(as = DataRequest.class)
public interface IDataRequest {

    /**
     * Helper method to create a {@code DataRequest} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    static DataRequest create(final UnaryOperator<DataRequest.Builder> func) {
        return func.apply(DataRequest.builder()).build();
    }

//    /**
//     * this is a unique ID for each individual request made between the
//     * micro-services
//     *
//     * @return
//     */
//    @Value.Derived
//    default RequestId getId() {
//        return IRequestId.create(b -> b.id(UUID.randomUUID().toString()));
//    }

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
