
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetBucketLoggingStatusResponse" type="{http://s3.amazonaws.com/doc/2006-03-01/}BucketLoggingStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getBucketLoggingStatusResponse"
})
@XmlRootElement(name = "GetBucketLoggingStatusResponse", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class GetBucketLoggingStatusResponse {

    @XmlElement(name = "GetBucketLoggingStatusResponse", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BucketLoggingStatus getBucketLoggingStatusResponse;

    /**
     * Gets the value of the getBucketLoggingStatusResponse property.
     * 
     * @return
     *     possible object is
     *     {@link BucketLoggingStatus }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BucketLoggingStatus getGetBucketLoggingStatusResponse() {
        return getBucketLoggingStatusResponse;
    }

    /**
     * Sets the value of the getBucketLoggingStatusResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link BucketLoggingStatus }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setGetBucketLoggingStatusResponse(BucketLoggingStatus value) {
        this.getBucketLoggingStatusResponse = value;
    }

}
