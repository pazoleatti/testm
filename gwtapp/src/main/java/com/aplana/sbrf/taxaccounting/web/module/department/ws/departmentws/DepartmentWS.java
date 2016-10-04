
package com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebService(name = "DepartmentWS", targetNamespace = "http://taxaccounting.sbrf.aplana.com/DepartmentWS/")
public interface DepartmentWS {


    /**
     * 
     * @param departmentChange
     * @return
     *     returns com.aplana.sbrf.taxaccounting.departmentws.TaxDepartmentChangeStatus
     */
    @WebMethod(action = "http://taxaccounting.sbrf.aplana.com/DepartmentWS/sendDepartmentChange")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "sendDepartmentChange", targetNamespace = "http://taxaccounting.sbrf.aplana.com/DepartmentWS/", className = "com.aplana.sbrf.taxaccounting.departmentws.SendDepartmentChange")
    @ResponseWrapper(localName = "sendDepartmentChangeResponse", targetNamespace = "http://taxaccounting.sbrf.aplana.com/DepartmentWS/", className = "com.aplana.sbrf.taxaccounting.departmentws.SendDepartmentChangeResponse")
    public TaxDepartmentChangeStatus sendDepartmentChange(
        @WebParam(name = "departmentChange", targetNamespace = "")
        TaxDepartmentChange departmentChange);

}