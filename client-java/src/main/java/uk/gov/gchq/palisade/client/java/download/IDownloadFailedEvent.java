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
package uk.gov.gchq.palisade.client.java.download;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.resource.Resource;
import uk.gov.gchq.palisade.client.java.util.TupleStyle;

/**
 * <p>
 * A {@code DownloadFailedEvent} is posted to the event bus if a download has
 * failed. This can occur if the Data Service request fails.
 * </p>
 * <p>
 * Note that the {@code DownloadFailedEvent} class is generated at compile time.
 * The generated class does not use a builder but uses "Tuple Style". For
 * example, an instance can be generated in the following way.
 * </p>
 * <pre>
 * {@code
 *     var event = DownloadEventFailed.of("token", resource, throwable);
 * }
 * </pre>
 *
 * @author dbell
 * @since 0.5.0
 * @see "https://immutables.github.io/style.html"
 */
@Value.Immutable
@TupleStyle
public interface IDownloadFailedEvent {

    /**
     * Returns the failed download resource
     *
     * @return the failed download resource
     */
    Resource getResource();

    /**
     * Returns the throwable that caused this event
     *
     * @return the throwable that caused this event
     */
    Throwable getThrowble();

}
