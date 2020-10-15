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
package uk.gov.gchq.palisade.client.java;

import io.micronaut.context.event.ApplicationEventPublisher;

import uk.gov.gchq.palisade.client.java.util.Bus;

/**
 * The main context for the client providing access to the internal registry,
 * the configuration and the event bus
 *
 * @since 0.5.0
 */
public interface ClientContext extends Bus {

    /**
     * Returns an instance of {@code type} from the client context
     *
     * @param <T>  The type
     * @param type The type class
     * @return an instance of {@code type} from the client context
     */
    <T> T get(Class<T> type);

    /**
     * Returns the client configuration
     *
     * @return the client configuration
     */
    default ClientConfig getConfig() {
        return get(ClientConfig.class);
    }

    /**
     * Posts the provided application {@code event}
     */
    @Override
    default void post(final Object event) {
        get(ApplicationEventPublisher.class).publishEvent(event);
    }

}
