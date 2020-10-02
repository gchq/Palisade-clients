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
package uk.gov.gchq.palisade.client.java.resource;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.Map;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.annotation.*;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = Resource.class)
@JsonSerialize(as = Resource.class)
public interface IResource {

    public static Resource create(UnaryOperator<Resource.Builder> func) {
        return func.apply(Resource.builder()).build();
    }

    String getToken();

    String getLeafResourceId();

    String getUrl();

    Map<String, String> getProperties();

}