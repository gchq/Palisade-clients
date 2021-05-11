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
package uk.gov.gchq.palisade.client.java.util;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A style annotation that can be used on interfaces annotated via the Immutable
 * library to alter how the creation process is performed.
 * <p>
 * The overshadowImplementation = true style attribute makes sure that build()
 * will be declared to return abstract value type Person, not the implementation
 * ImmutablePerson, following metaphor: implementation type will be
 * "overshadowed" by abstract value type.
 * <p>
 * Essentially, the generated class becomes implementation detail without much
 * boilerplate which is needed to fully hide implementation behind user-written
 * code.
 *
 * @since 0.5.0
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Value.Style(
    visibility = ImplementationVisibility.PACKAGE,
    overshadowImplementation = true,
    depluralize = true,
    defaults = @Value.Immutable(copy = false)
)
public @interface ImmutableStyle { // empty
}
