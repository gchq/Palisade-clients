
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LoggingSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoggingSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TargetBucket" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TargetPrefix" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TargetGrants" type="{http://s3.amazonaws.com/doc/2006-03-01/}AccessControlList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoggingSettings", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", propOrder = {
    "targetBucket",
    "targetPrefix",
    "targetGrants"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class LoggingSettings {

    @XmlElement(name = "TargetBucket", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String targetBucket;
    @XmlElement(name = "TargetPrefix", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String targetPrefix;
    @XmlElement(name = "TargetGrants", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected AccessControlList targetGrants;

    /**
     * Gets the value of the targetBucket property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getTargetBucket() {
        return targetBucket;
    }

    /**
     * Sets the value of the targetBucket property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setTargetBucket(String value) {
        this.targetBucket = value;
    }

    /**
     * Gets the value of the targetPrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getTargetPrefix() {
        return targetPrefix;
    }

    /**
     * Sets the value of the targetPrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setTargetPrefix(String value) {
        this.targetPrefix = value;
    }

    /**
     * Gets the value of the targetGrants property.
     * 
     * @return
     *     possible object is
     *     {@link AccessControlList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public AccessControlList getTargetGrants() {
        return targetGrants;
    }

    /**
     * Sets the value of the targetGrants property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessControlList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setTargetGrants(AccessControlList value) {
        this.targetGrants = value;
    }

}
