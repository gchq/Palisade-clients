
package uk.gov.gchq.palisade.client.s3.domain;

public enum StorageClass {

    STANDARD,
    REDUCED_REDUNDANCY,
    GLACIER,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static StorageClass fromValue(String v) {
        return valueOf(v);
    }

}
