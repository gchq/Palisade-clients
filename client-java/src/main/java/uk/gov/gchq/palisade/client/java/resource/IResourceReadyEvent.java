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

import uk.gov.gchq.palisade.client.java.util.TupleStyle;

/**
 * <p>
 * A {@code ResourceReadyEvent} is posted to the event bus when a resource
 * {@link Message} has been received signifying that it is ready for download.
 * </p>
 * <p>
 * Note that the {@code DownloadFailedEvent} class is generated at compile time.
 * The generated class does not use a builder but uses "Tuple Style". For
 * example, an instance can be generated in the following way.
 * </p>
 *
 * <pre>
 * {@code
 *     var event = ResourceReadyEvent.of("token", resource);
 * }
 * </pre>
 *
 * @author dbell
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@TupleStyle
public interface IResourceReadyEvent {

    /**
     * Returns the resource to be downloaded
     *
     * @return the resource to be downloaded
     */
    Resource getResource();

}