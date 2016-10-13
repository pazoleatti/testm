package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.audit.ws.AuditManagementService",
        targetNamespace = "http://taxaccounting.sbrf.aplana.com/AuditManagementService/",
        serviceName="AuditManagementService",
        portName="AuditManagementServicePort")
public class AuditManagementServicePortType extends SpringBeanAutowiringSupport implements AuditManagementService {

    @Autowired
    private AuditService auditService;

    public void addAuditLog(AuditLog auditLog)
            throws AuditManagementServiceException_Exception {

        validate(auditLog);

        try {
            TAUserInfo userInfo = assembleUserInfo(auditLog);
            Integer departmentId = null;
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, departmentId, null, null, null, null, auditLog.getNote(), null, null);
        } catch (Exception e) {
            AuditManagementServiceException fault = new AuditManagementServiceException();
            fault.setCode("AUDIT-000");
            fault.setDetails("Internal error");
            throw new AuditManagementServiceException_Exception(e.toString(), fault);
        }
    }

    private void validate(AuditLog auditLog) {

    }

    private TAUserInfo assembleUserInfo(AuditLog auditLog) {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setLogin(auditLog.getUserInfo().getUser().getLogin());
        user.setName(auditLog.getUserInfo().getUser().getName());
        user.setDepartmentId(auditLog.getUserInfo().getUser().getDepartmentId());
        List<TARole> roles = new ArrayList<TARole>();
        for (Role role : auditLog.getUserInfo().getUser().getRole()) {
            TARole taRole = new TARole();
            taRole.setName(role.getName());
            roles.add(taRole);
        }
        user.setRoles(roles);
        return userInfo;
    }
}
