
package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for addAuditLog complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="addAuditLog">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="auditLog" type="{http://taxaccounting.sbrf.aplana.com/AuditManagementService/}AuditLog"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addAuditLog", propOrder = {
    "auditLog"
})
public class AddAuditLog {

    @XmlElement(required = true)
    protected AuditLog auditLog;

    /**
     * Gets the value of the auditLog property.
     * 
     * @return
     *     possible object is
     *     {@link AuditLog }
     *     
     */
    public AuditLog getAuditLog() {
        return auditLog;
    }

    /**
     * Sets the value of the auditLog property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuditLog }
     *     
     */
    public void setAuditLog(AuditLog value) {
        this.auditLog = value;
    }

}