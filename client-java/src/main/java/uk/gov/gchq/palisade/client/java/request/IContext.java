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

import java.util.Map;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * <p>
 * This is the context instance which is part of a {@link PalisadeRequest}
 * </p>
 * <p>
 * Note that the {@link Context} class is created at compile time. The way in
 * which the class is created is determined by the {@link ImmutableStyle}. This
 * class is also compatible with Jackson.
 * </p>
 *
 * @author dbell
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(builder = Context.Builder.class)
public interface IContext {

    /**
     * Helper method to create a {@link Context} using a builder function
     *
     * @param func The builder function
     * @return a newly created {@code RequestId}
     */
    public static Context create(UnaryOperator<Context.Builder> func) {
        return func.apply(Context.builder()).build();
    }

    /**
     * Returns the purpose of this request
     *
     * @return the purpose of this request
     */
    public String getPurpose();

    /**
     * Returns the contents (properties)
     *
     * @return the contents (properties)
     */
    public Map<String, Object> getContents();

    /**
     * Returns a property for the provided key or null if not found
     *
     * @param key The key of the value to find
     * @return a property for the provided key or null if not found
     */
    @JsonIgnore
    default Object get(final String key) {
        return getContents().get(key);
    }

    /**
     * Returns the class name or the canonical classname of this instance if not set
     *
     * @return the class name or the canonical classname of this instance if not set
     */
    @Value.Default
    default String getClassName() {
        return this.getClass().getCanonicalName();
    }

}
