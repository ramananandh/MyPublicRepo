//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-833 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.11 at 04:38:10 AM PDT 
//


package org.ebayopensource.turmeric.common.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceConfigGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceConfigGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="service-config" type="{http://www.ebayopensource.org/turmeric/common/config}ServiceGroupConfig" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceConfigGroup", propOrder = {
    "serviceConfig"
})
public class ServiceConfigGroup {

    @XmlElement(name = "service-config")
    protected ServiceGroupConfig serviceConfig;
    @XmlAttribute(required = true)
    protected String name;

    /**
     * Gets the value of the serviceConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceGroupConfig }
     *     
     */
    public ServiceGroupConfig getServiceConfig() {
        return serviceConfig;
    }

    /**
     * Sets the value of the serviceConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceGroupConfig }
     *     
     */
    public void setServiceConfig(ServiceGroupConfig value) {
        this.serviceConfig = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
