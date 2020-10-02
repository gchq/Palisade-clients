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

package uk.gov.gchq.palisade.client.java.data;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.function.UnaryOperator;

@Value.Immutable
@ImmutableStyle
public interface IDataRequest {

    public static <E> DataRequest create(UnaryOperator<DataRequest.Builder> func) {
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

    String getToken();

    String getLeafResourceId();


}
