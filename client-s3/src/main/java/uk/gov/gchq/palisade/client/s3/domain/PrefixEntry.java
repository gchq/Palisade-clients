
package uk.gov.gchq.palisade.client.s3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import uk.gov.gchq.palisade.Generated;

import java.util.StringJoiner;

public class PrefixEntry {

    @JsonProperty(value = "Prefix", required = true)
    protected String prefix;

    @Generated
    public String getPrefix() {
        return prefix;
    }

    @Generated
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", PrefixEntry.class.getSimpleName() + "[", "]")
                .add("prefix='" + prefix + "'")
                .toString();
    }
}
