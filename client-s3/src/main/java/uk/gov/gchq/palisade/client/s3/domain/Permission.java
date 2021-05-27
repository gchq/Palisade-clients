
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Permission.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Permission">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="READ"/>
 *     &lt;enumeration value="WRITE"/>
 *     &lt;enumeration value="READ_ACP"/>
 *     &lt;enumeration value="WRITE_ACP"/>
 *     &lt;enumeration value="FULL_CONTROL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Permission", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
@XmlEnum
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public enum Permission {

    READ,
    WRITE,
    READ_ACP,
    WRITE_ACP,
    FULL_CONTROL;

    public String value() {
        return name();
    }

    public static Permission fromValue(String v) {
        return valueOf(v);
    }

}
