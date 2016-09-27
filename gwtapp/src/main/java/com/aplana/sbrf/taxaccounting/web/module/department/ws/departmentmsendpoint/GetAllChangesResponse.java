
package com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAllChangesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getAllChangesResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://taxaccounting.sbrf.aplana.com/DepartmentManagementService/}TaxDepartmentChanges" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAllChangesResponse", propOrder = {
    "_return"
})
public class GetAllChangesResponse {

    @XmlElement(name = "return")
    protected TaxDepartmentChanges _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link TaxDepartmentChanges }
     *     
     */
    public TaxDepartmentChanges getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxDepartmentChanges }
     *     
     */
    public void setReturn(TaxDepartmentChanges value) {
        this._return = value;
    }

}
