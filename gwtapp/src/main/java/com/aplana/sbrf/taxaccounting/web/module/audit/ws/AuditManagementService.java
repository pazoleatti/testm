
package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.7-b01
 * Generated source version: 2.2
 *
 */
@WebService(name = "AuditManagementService", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/")
public interface AuditManagementService {


    /**
     *
     * @param auditLog
     * @return
     *     returns com.aplana.sbrf.taxaccounting.web.module.audit.ws.StatusInfo
     */
    @WebMethod(action = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/addAuditLog")
    @WebResult(name = "status", targetNamespace = "")
    @RequestWrapper(localName = "addAuditLog", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/", className = "com.aplana.sbrf.taxaccounting.web.module.audit.ws.AddAuditLog")
    @ResponseWrapper(localName = "addAuditLogResponse", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/", className = "com.aplana.sbrf.taxaccounting.web.module.audit.ws.AddAuditLogResponse")
    public StatusInfo addAuditLog(
            @WebParam(name = "auditLog", targetNamespace = "")
                    AuditLog auditLog);

}