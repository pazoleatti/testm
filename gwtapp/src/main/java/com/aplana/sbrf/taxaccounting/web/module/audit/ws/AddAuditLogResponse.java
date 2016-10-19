
package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for addAuditLogResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="addAuditLogResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://taxaccounting.sbrf.aplana.com/AuditManagementService/}StatusInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addAuditLogResponse", propOrder = {
        "status"
})
public class AddAuditLogResponse {

    protected StatusInfo status;

    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link StatusInfo }
     *
     */
    public StatusInfo getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link StatusInfo }
     *
     */
    public void setStatus(StatusInfo value) {
        this.status = value;
    }

}

