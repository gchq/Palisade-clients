package uk.gov.gchq.palisade.client.java;

import org.immutables.value.Value;

import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import javax.websocket.Session;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Value.Immutable
@ImmutableStyle
public interface IServerState {

    public static <E> ServerState create(UnaryOperator<ServerState.Builder> func) {
        return func.apply(ServerState.builder()).build();
    }

    String getToken();

    ResourceGenerator getResourceGenerator();

    WeakReference<Session> getSessionReference();

    default Optional<Session> findSession() {
        return Optional.ofNullable(getSessionReference().get());
    }

    @Value.Default()
    default ServerStateType getCurrentState() {
        return ServerStateType.WAITING;
    }

    default ServerState change(UnaryOperator<ServerState.Builder> func) {
        return func.apply(ServerState.builder().from(this)).build();
    }

    default boolean isAt(ServerStateType expectedState) {
        return expectedState == getCurrentState();
    }

    default boolean isComplete() {
        return getCurrentState() == ServerStateType.COMPLETE;
    }

    default boolean isActive() {
        return !isComplete();
    }
}