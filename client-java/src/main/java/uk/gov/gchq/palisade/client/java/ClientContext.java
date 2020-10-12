package uk.gov.gchq.palisade.client.java;

public interface ClientContext {

    <T> T get(Class<T> type);

    default ClientConfig getConfig() {
        return get(ClientConfig.class);
    }

}
