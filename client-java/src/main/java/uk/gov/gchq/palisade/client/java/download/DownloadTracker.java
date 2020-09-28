package uk.gov.gchq.palisade.client.java.download;

public interface DownloadTracker {

    public enum ManagerStatus {
        ACTIVE, SHUTTING_DOWN, SHUT_DOWN
    }

    int getAvaliableSlots();

    boolean hasAvailableSlots();

    ManagerStatus getStatus();

}