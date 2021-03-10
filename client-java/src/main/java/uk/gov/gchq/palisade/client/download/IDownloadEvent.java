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

import uk.gov.gchq.palisade.client.resource.Resource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * A {@code DownloadEvent} is the super interface for all {@code Download*}
 * events.
 *
 * @since 0.5.0
 */
public interface IDownloadEvent {

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
    Resource getResource();

    /**
     * Returns any properties for this event
     *
     * @return any properties for this event
     */
    Map<String, String> getProperties();

}