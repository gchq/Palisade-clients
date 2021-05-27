
package uk.gov.gchq.palisade.client.s3.domain;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListAllMyBucketsResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListAllMyBucketsResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Owner" type="{http://s3.amazonaws.com/doc/2006-03-01/}CanonicalUser"/>
 *         &lt;element name="Buckets" type="{http://s3.amazonaws.com/doc/2006-03-01/}ListAllMyBucketsList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListAllMyBucketsResult", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", propOrder = {
    "owner",
    "buckets"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class ListAllMyBucketsResult {

    @XmlElement(name = "Owner", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected CanonicalUser owner;
    @XmlElement(name = "Buckets", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ListAllMyBucketsList buckets;

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link CanonicalUser }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public CanonicalUser getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link CanonicalUser }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setOwner(CanonicalUser value) {
        this.owner = value;
    }

    /**
     * Gets the value of the buckets property.
     * 
     * @return
     *     possible object is
     *     {@link ListAllMyBucketsList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ListAllMyBucketsList getBuckets() {
        return buckets;
    }

    /**
     * Sets the value of the buckets property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListAllMyBucketsList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setBuckets(ListAllMyBucketsList value) {
        this.buckets = value;
    }

}
