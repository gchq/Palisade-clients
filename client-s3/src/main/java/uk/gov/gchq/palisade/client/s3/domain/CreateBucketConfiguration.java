
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CreateBucketConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateBucketConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LocationConstraint" type="{http://s3.amazonaws.com/doc/2006-03-01/}LocationConstraint"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateBucketConfiguration", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", propOrder = {
    "locationConstraint"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class CreateBucketConfiguration {

    @XmlElement(name = "LocationConstraint", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected LocationConstraint locationConstraint;

    /**
     * Gets the value of the locationConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link LocationConstraint }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public LocationConstraint getLocationConstraint() {
        return locationConstraint;
    }

    /**
     * Sets the value of the locationConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationConstraint }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setLocationConstraint(LocationConstraint value) {
        this.locationConstraint = value;
    }

}
