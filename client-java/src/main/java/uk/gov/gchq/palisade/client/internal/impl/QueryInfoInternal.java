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
package uk.gov.gchq.palisade.client.internal.impl;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.QueryInfo;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

/**
 * An instance of {@link QueryInfoInternal} is passed during the submission of a new
 * request.
 * <p>
 * Note that the {@link QueryInfoInternal} class is created at compile time. The way in
 * which the class is created is determined by the {@code ImmutableStyle}.
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@ImmutableStyle
public interface QueryInfoInternal extends QueryInfo {

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutableQueryInfoInternal.Builder { // empty
    }

}
