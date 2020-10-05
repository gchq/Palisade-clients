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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(builder = UserId.Builder.class)
public interface IUserId extends Serializable {

    public static UserId create(UnaryOperator<UserId.Builder> func) {
        return func.apply(UserId.builder()).build();
    }

    public static UserId of(String id) {
        return create(rid -> rid.id(id));
    }

    public String getId();
}
