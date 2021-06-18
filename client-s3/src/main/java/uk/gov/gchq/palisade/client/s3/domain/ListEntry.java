
package uk.gov.gchq.palisade.client.s3.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import uk.gov.gchq.palisade.Generated;

import javax.xml.datatype.XMLGregorianCalendar;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.StringJoiner;

public class ListEntry {

    @JsonProperty(value = "Key", required = true)
    protected String key;
    @JsonProperty(value = "LastModified", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    protected Date lastModified;
    @JsonProperty(value = "ETag", required = true)
    protected String eTag;
    @JsonProperty(value = "Size")
    protected long size;
    @JsonProperty(value = "Owner")
    protected CanonicalUser owner;
    @JsonProperty(value = "StorageClass", required = true)
    protected StorageClass storageClass;

    @Generated
    public String getKey() {
        return key;
    }

    @Generated
    public void setKey(final String key) {
        this.key = key;
    }

    @Generated
    public Date getLastModified() {
        return lastModified;
    }

    @Generated
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    @Generated
    public String geteTag() {
        return eTag;
    }

    @Generated
    public void seteTag(final String eTag) {
        this.eTag = eTag;
    }

    @Generated
    public long getSize() {
        return size;
    }

    @Generated
    public void setSize(final long size) {
        this.size = size;
    }

    @Generated
    public CanonicalUser getOwner() {
        return owner;
    }

    @Generated
    public void setOwner(final CanonicalUser owner) {
        this.owner = owner;
    }

    @Generated
    public StorageClass getStorageClass() {
        return storageClass;
    }

    @Generated
    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", ListEntry.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("lastModified=" + lastModified)
                .add("eTag='" + eTag + "'")
                .add("size=" + size)
                .add("owner=" + owner)
                .add("storageClass=" + storageClass)
                .toString();
    }
}
