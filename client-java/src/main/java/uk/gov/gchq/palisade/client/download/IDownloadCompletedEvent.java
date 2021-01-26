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
package uk.gov.gchq.palisade.client.download;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.TupleStyle;

/**
 * A {@link DownloadCompletedEvent} is posted to the event bus after a
 * successful download has been completed.
 * <p>
 * Note that the {@link DownloadCompletedEvent} class is generated at compile
 * time. The generated class does not use a builder but uses "Tuple Style". For
 * example, an instance can be generated in the following way.
 * <pre>
 * {@code
 *   var event = DownloadCompletedEvent.of("token", resource, result);
 * }
 * </pre>
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@Value.Immutable
@TupleStyle
public interface IDownloadCompletedEvent extends IDownloadEvent {

    /**
     * Returns the download result, which currently only contains the download id
     *
     * @return the download result
     */
    DownloadResult getResult();
}
