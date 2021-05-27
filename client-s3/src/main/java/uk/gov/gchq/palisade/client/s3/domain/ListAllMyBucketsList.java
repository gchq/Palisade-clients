
package uk.gov.gchq.palisade.client.s3.domain;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ListAllMyBucketsList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ListAllMyBucketsList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Bucket" type="{http://s3.amazonaws.com/doc/2006-03-01/}ListAllMyBucketsEntry" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListAllMyBucketsList", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", propOrder = {
    "bucket"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class ListAllMyBucketsList {

    @XmlElement(name = "Bucket", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<ListAllMyBucketsEntry> bucket;

    /**
     * Gets the value of the bucket property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bucket property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBucket().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListAllMyBucketsEntry }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<ListAllMyBucketsEntry> getBucket() {
        if (bucket == null) {
            bucket = new ArrayList<ListAllMyBucketsEntry>();
        }
        return this.bucket;
    }

}
