package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;


@WebService(name = "AuditManagementService", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/")
public interface AuditManagementService {


    @WebMethod(action = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/addAuditLog")
    @WebResult(name = "addAuditLogResponse", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/", partName = "result")
    public AddAuditLogResponse addAuditLog(
            @WebParam(name = "addAuditLogRequest", targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/", partName = "auditLog")
            AuditLog auditLog);

}
