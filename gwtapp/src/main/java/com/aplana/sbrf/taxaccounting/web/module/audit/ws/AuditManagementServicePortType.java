package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.audit.ws.AuditManagementService",
        targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/",
        serviceName="AuditManagementService",
        portName="AuditManagementServicePort")
public class AuditManagementServicePortType extends SpringBeanAutowiringSupport implements AuditManagementService {

    private static final Log LOG = LogFactory.getLog(AuditManagementServicePortType.class);

    @Autowired
    private AuditService auditService;

    public AddAuditLogResponse addAuditLog(AuditLog auditLog) {
        Integer departmentId = null;
        auditService.add(FormDataEvent.EXTERNAL_INTERACTION, auditLog.getUserInfo(), departmentId, null, null, null, null, auditLog.getNote(), null, null);

        return new AddAuditLogResponse();
    }
}
