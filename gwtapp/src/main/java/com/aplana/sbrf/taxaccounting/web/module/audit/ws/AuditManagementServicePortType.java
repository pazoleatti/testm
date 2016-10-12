package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
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

    @Autowired
    private AuditService auditService;

    public String addAuditLog(AuditLog auditLog) {
        Integer departmentId = null;
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setName(auditLog.getName());
        user.setLogin(auditLog.getLogin());
        userInfo.setUser(user);
        userInfo.setIp("1.1.1.1");
        auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, departmentId, null, null, null, null, auditLog.getNote(), null, null);

        return "OK";
    }
}
