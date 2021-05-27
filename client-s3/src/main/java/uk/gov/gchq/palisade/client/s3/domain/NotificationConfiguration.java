
package uk.gov.gchq.palisade.client.s3.domain;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotificationConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NotificationConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TopicConfiguration" type="{http://s3.amazonaws.com/doc/2006-03-01/}TopicConfiguration" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotificationConfiguration", namespace = "http://s3.amazonaws.com/doc/2006-03-01/", propOrder = {
    "topicConfiguration"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class NotificationConfiguration {

    @XmlElement(name = "TopicConfiguration", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<TopicConfiguration> topicConfiguration;

    /**
     * Gets the value of the topicConfiguration property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the topicConfiguration property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTopicConfiguration().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TopicConfiguration }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2021-05-25T11:43:18+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<TopicConfiguration> getTopicConfiguration() {
        if (topicConfiguration == null) {
            topicConfiguration = new ArrayList<TopicConfiguration>();
        }
        return this.topicConfiguration;
    }

}
