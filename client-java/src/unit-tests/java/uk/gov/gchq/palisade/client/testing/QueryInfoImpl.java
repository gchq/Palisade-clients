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
package uk.gov.gchq.palisade.client.testing;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.QueryInfo;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.util.function.UnaryOperator;

/**
 * Implementation of QueryInfo for testing
 *
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
public interface QueryInfoImpl extends QueryInfo {

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutableQueryInfoImpl.Builder { // empty
    }

    /**
     * Helper method to create a {@code PalisadeRequest} using a builder function
     *
     * @param func The builder function
     * @return a newly created data request instance
     */
    static QueryInfoImpl create(final UnaryOperator<Builder> func) {
        return func.apply(new Builder()).build();
    }

}
