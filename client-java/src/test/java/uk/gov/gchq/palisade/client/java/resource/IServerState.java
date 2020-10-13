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

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import javax.websocket.Session;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Value.Immutable
@ImmutableStyle
public interface IServerState {

    static <E> ServerState create(final UnaryOperator<ServerState.Builder> func) {
        return func.apply(ServerState.builder()).build();
    }

    String getToken();

    Iterator<Resource> getResources();

    WeakReference<Session> getSessionReference();

    default Optional<Session> findSession() {
        return Optional.ofNullable(getSessionReference().get());
    }

    @Value.Default()
    default ServerStateType getCurrentState() {
        return ServerStateType.WAITING;
    }

    default ServerState change(final UnaryOperator<ServerState.Builder> func) {
        return func.apply(ServerState.builder().from(this)).build();
    }

    default boolean isAt(final ServerStateType expectedState) {
        return expectedState == getCurrentState();
    }

    default boolean isComplete() {
        return getCurrentState() == ServerStateType.COMPLETE;
    }

    default boolean isActive() {
        return !isComplete();
    }
}