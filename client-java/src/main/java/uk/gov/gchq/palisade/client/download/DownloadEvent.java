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
package uk.gov.gchq.palisade.client.download;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.resource.ResourceMessage;
import uk.gov.gchq.palisade.client.util.ImmutableStyle;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * A {@code DownloadEvent} is the super interface for all {@code Download*}
 * events.
 *
 * @since 0.5.0
 */
@ImmutableStyle
public interface DownloadEvent {

    /**
     * A {@link CompletedEvent} is posted to the event bus after a
     * successful download has been completed.
     * <p>
     * Note that the {@link CompletedEvent} class is generated at compile
     * time. The generated class does not use a builder but uses "Tuple Style". For
     * example, an instance can be generated in the following way. <pre>
     * {@code
     *   var event = DownloadCompletedEvent.of("token", resource, result);
     * }
     * </pre>
     *
     * @see "https://immutables.github.io/style.html"
     * @since 0.5.0
     */
    @Value.Immutable
    public interface CompletedEvent extends DownloadEvent {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableCompletedEvent.Builder { // empty
        }

        /**
         * Returns the download result, which currently only contains the download id
         *
         * @return the download result
         */
        DownloadResult getResult();
    }

    /**
     * A {@link FailedEvent} is posted to the event bus if a download has
     * failed. This can occur if the Data Service request fails.
     * <p>
     * Note that the {@link FailedEvent} class is generated at compile time.
     * The generated class does not use a builder but uses "Tuple Style". For
     * example, an instance can be generated in the following way. <pre>
     * {@code
     *   var event = DownloadFailedEvent.of("token", resource, cause, code);
     * }
     * </pre>
     *
     * @see "https://immutables.github.io/style.html"
     * @since 0.5.0
     */
    @Value.Immutable
    public interface FailedEvent extends DownloadEvent {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableFailedEvent.Builder { // empty
        }

        /**
         * Returns the throwable that caused this event
         *
         * @return the throwable that caused this event
         */
        Exception getCause();

        /**
         * Returns the HTTP status code
         *
         * @return the HTTP status code
         */
        int getStatusCode();

    }

    /**
     * A {@link StartedEvent} is posted when a download has started
     * <p>
     * Note that the {@link StartedEvent} class is generated at compile
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
    public interface ScheduledEvent extends DownloadEvent {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableScheduledEvent.Builder { // empty
        }

    }

    /**
     * A {@link StartedEvent} is posted when a download has started
     * <p>
     * Note that the {@link StartedEvent} class is generated at compile
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
    public interface StartedEvent extends DownloadEvent {

        /**
         * Exposes the generated builder outside this package
         * <p>
         * While the generated implementation (and consequently its builder) is not
         * visible outside of this package. This builder inherits and exposes all public
         * methods defined on the generated implementation's Builder class.
         */
        class Builder extends ImmutableStartedEvent.Builder { // empty
        }

    }

    /**
     * Returns a new instance initialised via the provided {@code builderFunction}
     *
     * @param downloadId      The unique id of the download
     * @param resource        The resource being downloaded
     * @param builderFunction Function to set extra attributes
     * @return a new builder to assist in the creation of a {@code CompletedEvent}
     */
    static CompletedEvent completed(
            final UUID downloadId,
            final ResourceMessage resource,
            final DownloadResult result,
            final Map<String,String> properties) {
        return new CompletedEvent.Builder()
            .id(downloadId)
            .resource(resource)
            .result(result)
            .properties(properties)
            .build();
    }

    /**
     * Returns a new {@code FaileEvent}
     *
     * @param downloadId The unique id of the download
     * @param resource   The resource being downloaded
     * @param cause      The cause of the failure
     * @param statusCode The status code if available
     * @return a new {@code FailedEvent}
     */
    static FailedEvent failed(
            final UUID downloadId,
            final ResourceMessage resource,
            final Exception cause,
            final int statusCode) {
        return new FailedEvent.Builder()
            .id(downloadId)
            .resource(resource)
            .cause(cause)
            .statusCode(statusCode)
            .build();
    }

    /**
     * Returns a new instance initialised via the provided {@code builderFunction}
     *
     * @param downloadId      The unique id of the download
     * @param resource        The resource being downloaded
     * @return a new builder to assist in the creation of a {@code ScheduledEvent}
     */
    static ScheduledEvent scheduled(final UUID downloadId, final ResourceMessage resource) {
        return new ScheduledEvent.Builder().id(downloadId).resource(resource).build();
    }

    /**
     * Returns a new instance initialised via the provided {@code builderFunction}
     *
     * @param downloadId The unique id of the download
     * @param resource   The resource being downloaded
     * @return a new builder to assist in the creation of a {@code DownloadStartedEvent}
     */
    static StartedEvent started(final UUID downloadId, final ResourceMessage resource) {
        return new StartedEvent.Builder().id(downloadId).resource(resource).build();
    }

    /**
     * Returns the unique download id
     *
     * @return the unique download id
     */
    UUID getId();

    /**
     * Returns the time that this event was raised
     *
     * @return the time that this event was raised
     */
    @Value.Derived
    default Instant getTime() {
        return Instant.now();
    }

    /**
     * Returns the resource to be downloaded
     *
     * @return the resource to be downloaded
     */
    ResourceMessage getResource();

    /**
     * Returns any properties for this event
     *
     * @return any properties for this event
     */
    Map<String, String> getProperties();

}
