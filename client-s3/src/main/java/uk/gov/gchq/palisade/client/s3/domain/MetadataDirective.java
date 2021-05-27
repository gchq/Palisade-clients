
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MetadataDirective.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MetadataDirective">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="COPY"/>
 *     &lt;enumeration value="REPLACE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MetadataDirective", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
@XmlEnum
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public enum MetadataDirective {

    COPY,
    REPLACE;

    public String value() {
        return name();
    }

    public static MetadataDirective fromValue(String v) {
        return valueOf(v);
    }

}
