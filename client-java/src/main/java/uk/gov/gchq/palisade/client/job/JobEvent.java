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
package uk.gov.gchq.palisade.client.job;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.time.Instant;

/**
 * A {@code DownloadStartedEvent} is posted when a download has started
 * <p>
 * Note that the {@code DownloadStartedEvent} class is generated at compile
 * time. The generated class does not use a builder but uses "Tuple Style". For
 * example, an instance can be generated in the following way. <pre>
 * {@code
 *   var event = DownloadStartedEvent.of("token", resource);
 * }
 * </pre>
 *
 * @see "https://immutables.github.io/style.html"
 * @since 0.5.0
 */
@ImmutableStyle
public interface JobEvent {

    /**
     * A {@code DownloadStartedEvent} is posted when a download has started
     * <p>
     * Note that the {@code DownloadStartedEvent} class is generated at compile
     * time. The generated class does not use a builder but uses "Tuple Style". For
     * example, an instance can be generated in the following way. <pre>
     * {@code
     *   var event = DownloadStartedEvent.of("token", resource);
     * }
     * </pre>
     *
     * @see "https://immutables.github.io/style.html"
     * @since 0.5.0
     */
    @Value.Immutable
    public interface JobStartedEvent extends JobEvent {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableJobStartedEvent.Builder { // empty
        }

    }

    /**
     * A {@code DownloadStartedEvent} is posted when a download has started
     * <p>
     * Note that the {@code DownloadStartedEvent} class is generated at compile
     * time. The generated class does not use a builder but uses "Tuple Style". For
     * example, an instance can be generated in the following way. <pre>
     * {@code
     *   var event = DownloadStartedEvent.of("token", resource);
     * }
     * </pre>
     *
     * @see "https://immutables.github.io/style.html"
     * @since 0.5.0
     */
    @Value.Immutable
    public interface JobCompletedEvent extends JobEvent {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableJobCompletedEvent.Builder { // empty
        }

    }

    @Value.Derived
    default Instant getTime() {
        return Instant.now();
    }

}
