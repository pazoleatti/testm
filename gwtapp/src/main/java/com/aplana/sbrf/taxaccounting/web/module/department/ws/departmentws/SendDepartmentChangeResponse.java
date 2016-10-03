
package com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendDepartmentChangeResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendDepartmentChangeResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://taxaccounting.sbrf.aplana.com/DepartmentWS/}TaxDepartmentChangeStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendDepartmentChangeResponse", propOrder = {
    "_return"
})
public class SendDepartmentChangeResponse {

    @XmlElement(name = "return", required = true)
    protected TaxDepartmentChangeStatus _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link TaxDepartmentChangeStatus }
     *     
     */
    public TaxDepartmentChangeStatus getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxDepartmentChangeStatus }
     *     
     */
    public void setReturn(TaxDepartmentChangeStatus value) {
        this._return = value;
    }

}
