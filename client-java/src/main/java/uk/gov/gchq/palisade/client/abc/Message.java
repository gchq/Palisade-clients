package uk.gov.gchq.palisade.client.abc;

public interface Message {

    public enum MessageType {
        ERROR,
        RESOURCE;
    }

    MessageType getType();

    String getToken();

}
