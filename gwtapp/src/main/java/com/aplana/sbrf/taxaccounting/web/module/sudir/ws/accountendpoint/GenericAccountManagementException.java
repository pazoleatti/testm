
package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GenericAccountManagementException complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericAccountManagementException">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="causeMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="details" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="genericSudirStatusCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="proprietarySystemStatusCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericAccountManagementException", propOrder = {
    "causeMessage",
    "details",
    "genericSudirStatusCode",
    "proprietarySystemStatusCode"
})
public class GenericAccountManagementException {

    @XmlElement(required = true, nillable = true)
    protected String causeMessage;
    @XmlElement(required = true, nillable = true)
    protected String details;
    @XmlElement(required = true, nillable = true)
    protected String genericSudirStatusCode;
    @XmlElement(required = true, nillable = true)
    protected String proprietarySystemStatusCode;

    /**
     * Gets the value of the causeMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCauseMessage() {
        return causeMessage;
    }

    /**
     * Sets the value of the causeMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCauseMessage(String value) {
        this.causeMessage = value;
    }

    /**
     * Gets the value of the details property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the value of the details property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetails(String value) {
        this.details = value;
    }

    /**
     * Gets the value of the genericSudirStatusCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGenericSudirStatusCode() {
        return genericSudirStatusCode;
    }

    /**
     * Sets the value of the genericSudirStatusCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGenericSudirStatusCode(String value) {
        this.genericSudirStatusCode = value;
    }

    /**
     * Gets the value of the proprietarySystemStatusCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProprietarySystemStatusCode() {
        return proprietarySystemStatusCode;
    }

    /**
     * Sets the value of the proprietarySystemStatusCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProprietarySystemStatusCode(String value) {
        this.proprietarySystemStatusCode = value;
    }

}
