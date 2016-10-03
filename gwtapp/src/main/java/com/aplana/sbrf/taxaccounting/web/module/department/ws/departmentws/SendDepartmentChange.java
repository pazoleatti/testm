
package com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendDepartmentChange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendDepartmentChange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="departmentChange" type="{http://taxaccounting.sbrf.aplana.com/DepartmentWS/}TaxDepartmentChange"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendDepartmentChange", propOrder = {
    "departmentChange"
})
public class SendDepartmentChange {

    @XmlElement(required = true)
    protected TaxDepartmentChange departmentChange;

    /**
     * Gets the value of the departmentChange property.
     * 
     * @return
     *     possible object is
     *     {@link TaxDepartmentChange }
     *     
     */
    public TaxDepartmentChange getDepartmentChange() {
        return departmentChange;
    }

    /**
     * Sets the value of the departmentChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxDepartmentChange }
     *     
     */
    public void setDepartmentChange(TaxDepartmentChange value) {
        this.departmentChange = value;
    }

}
