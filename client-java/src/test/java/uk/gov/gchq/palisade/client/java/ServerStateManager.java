package uk.gov.gchq.palisade.client.java;

import org.slf4j.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerStateManager {

    private static final Logger log = LoggerFactory.getLogger(ServerStateManager.class);

    // Redis?
    Map<String, ServerState> states = new ConcurrentHashMap<>(0);

    public ServerStateManager() {
        // TODO Auto-generated constructor stub
    }

    public ServerStateManager set(ServerState state) {
        // TODO: test previous and current state
        this.states.put(state.getToken(), state);
        log.debug("New state: " + state);
        return this;
    }

    public ServerState get(String token) {
        return find(token).orElseThrow(() -> new RuntimeException("Failed to find state for token: " + token));
    }

    public Optional<ServerState> find(String token) {
        return Optional.ofNullable(states.get(token));
    }

    public void remove(String token) {
        states.remove(token);
    }

}
