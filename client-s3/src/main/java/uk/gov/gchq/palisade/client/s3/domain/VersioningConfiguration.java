
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VersioningConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VersioningConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Status" type="{http://s3.amazonaws.com/doc/2006-03-01/}VersioningStatus" minOccurs="0"/>
 *         &lt;element name="MfaDelete" type="{http://s3.amazonaws.com/doc/2006-03-01/}MfaDeleteStatus" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VersioningConfiguration", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", propOrder = {
    "status",
    "mfaDelete"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class VersioningConfiguration {

    @XmlElement(name = "Status", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    @XmlSchemaType(name = "string")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected VersioningStatus status;
    @XmlElement(name = "MfaDelete", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    @XmlSchemaType(name = "string")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected MfaDeleteStatus mfaDelete;

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link VersioningStatus }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public VersioningStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link VersioningStatus }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setStatus(VersioningStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the mfaDelete property.
     * 
     * @return
     *     possible object is
     *     {@link MfaDeleteStatus }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public MfaDeleteStatus getMfaDelete() {
        return mfaDelete;
    }

    /**
     * Sets the value of the mfaDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link MfaDeleteStatus }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setMfaDelete(MfaDeleteStatus value) {
        this.mfaDelete = value;
    }

}
