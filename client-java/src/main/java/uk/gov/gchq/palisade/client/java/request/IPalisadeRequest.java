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
 * This class is used to wrap all the information that the user needs to supply
 * to the palisade service to register the data access request.
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(builder = PalisadeRequest.Builder.class)
public interface IPalisadeRequest extends Serializable {

    public static PalisadeRequest create(UnaryOperator<PalisadeRequest.Builder> func) {
        return func.apply(PalisadeRequest.builder()).build();
    }

    public String getResourceId();

    public UserId getUserId();

    public Context getContext();

    public RequestId getRequestId();

    public Optional<RequestId> getOriginalRequestId();

}
