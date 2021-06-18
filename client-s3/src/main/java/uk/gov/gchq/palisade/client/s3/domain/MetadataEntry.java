
package uk.gov.gchq.palisade.client.s3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import uk.gov.gchq.palisade.Generated;

import java.util.StringJoiner;


public class MetadataEntry {

    @JsonProperty(value = "Name", required = true)
    protected String name;
    @JsonProperty(value = "Value", required = true)
    protected String value;

    @Generated
    public String getName() {
        return name;
    }

    @Generated
    public void setName(final String name) {
        this.name = name;
    }

    @Generated
    public String getValue() {
        return value;
    }

    @Generated
    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", MetadataEntry.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("value='" + value + "'")
                .toString();
    }
}
