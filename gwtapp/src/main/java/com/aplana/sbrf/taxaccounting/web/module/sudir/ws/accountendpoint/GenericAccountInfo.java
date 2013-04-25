
package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GenericAccountInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericAccountInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attributes" type="{http://sberbank.ru/soa/service/sudir/itdi/smallsystem.generic.webservice.connector/1.0.0}ArrayOfGenericAttribute"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericAccountInfo", propOrder = {
    "attributes"
})
public class GenericAccountInfo {

    @XmlElement(required = true, nillable = true)
    protected ArrayOfGenericAttribute attributes;

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfGenericAttribute }
     *     
     */
    public ArrayOfGenericAttribute getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfGenericAttribute }
     *     
     */
    public void setAttributes(ArrayOfGenericAttribute value) {
        this.attributes = value;
    }

}
