
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Payer.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Payer">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BucketOwner"/>
 *     &lt;enumeration value="Requester"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Payer", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
@XmlEnum
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public enum Payer {

    @XmlEnumValue("BucketOwner")
    BUCKET_OWNER("BucketOwner"),
    @XmlEnumValue("Requester")
    REQUESTER("Requester");
    private final String value;

    Payer(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Payer fromValue(String v) {
        for (Payer c: Payer.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
