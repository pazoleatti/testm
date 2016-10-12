package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


@WebService(name = "AuditManagementService", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/")
public interface AuditManagementService {


    @WebMethod(action = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/addAuditLog")
    @WebResult(name = "")
    @RequestWrapper(localName = "addAuditLog", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/", className = "com.aplana.sbrf.taxaccounting.web.module.audit.ws.AddAuditLog")
    @ResponseWrapper(localName = "addAuditLogResponse", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/", className = "com.aplana.sbrf.taxaccounting.web.module.audit.ws.AddAuditLogResponse")
    public String addAuditLog(
            @WebParam(name = "auditLog", targetNamespace = "")
            AuditLog auditLog);

}
