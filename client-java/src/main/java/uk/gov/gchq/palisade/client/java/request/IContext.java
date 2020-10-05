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

import java.util.*;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(builder = Context.Builder.class)
public interface IContext {

    public static Context create(UnaryOperator<Context.Builder> func) {
        return func.apply(Context.builder()).build();
    }

    public String getPurpose();

    public Map<String, Object> getContents();

    @JsonIgnore
    default Map<String, Object> getContentsCopy() {
        return Collections.unmodifiableMap(getContents());
    }

    @JsonIgnore
    default Object get(final String key) {
        return getContents().get(key);
    }

    @Value.Default
    default String getClassName() {
        return this.getClass().getCanonicalName();
    }

}
