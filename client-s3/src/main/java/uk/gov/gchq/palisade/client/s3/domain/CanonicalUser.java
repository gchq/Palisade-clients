
package uk.gov.gchq.palisade.client.s3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import uk.gov.gchq.palisade.Generated;

import java.util.StringJoiner;


public class CanonicalUser {

    @JsonProperty(value = "ID", required = true)
    @JacksonXmlProperty(namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    protected String id;
    @JsonProperty(value = "DisplayName")
    @JacksonXmlProperty(namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    protected String displayName;

    @Generated
    public String getId() {
        return id;
    }

    @Generated
    public void setId(final String id) {
        this.id = id;
    }

    @Generated
    public String getDisplayName() {
        return displayName;
    }

    @Generated
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", CanonicalUser.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("displayName='" + displayName + "'")
                .toString();
    }
}
