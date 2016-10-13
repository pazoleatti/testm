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
            throwException("AUDIT-000", "Internal error", e.getMessage());
        }
    }

    private void validate(AuditLog auditLog) throws AuditManagementServiceException_Exception {
        if (auditLog == null) {
            throwException("AUDIT-000", "Internal error", "Не удалось получить данные для логирования");
        } else {
            if (auditLog.getUserInfo() == null) {
                throwException("AUDIT-001", "Invalid data format", "Не удалось получить информацию о пользователе");
            } else {
                if (auditLog.getUserInfo().getUser() == null) {
                    throwException("AUDIT-001", "Invalid data format", "Не удалось получить информацию о пользователе");
                } else {
                    User user = auditLog.getUserInfo().getUser();
                    if (user.getLogin() == null || user.getLogin().isEmpty()) {
                        throwException("AUDIT-001", "Invalid data format", "Не удалось получить логин пользователя");
                    }
                    /*if (user.getName() == null || user.getName().isEmpty()) {
                        throwException("AUDIT-001", "Invalid data format", "Не удалось получить имя пользователя");
                    }*/
                    if (user.getRole() == null || user.getRole().isEmpty()) {
                        throwException("AUDIT-001", "Invalid data format", "Не удалось получить роли пользователе");
                    }
                }
                /*if (auditLog.getUserInfo().getIp() == null) {
                    throwException("AUDIT-001", "Invalid data format", "Не удалось получить ip пользователя");
                }*/
            }
            if (auditLog.getNote() == null) {
                throwException("AUDIT-001", "Invalid data format", "Не удалось получить текст события");
            }
        }
    }

    private void throwException(String code, String details, String message) throws AuditManagementServiceException_Exception {
        AuditManagementServiceException fault = new AuditManagementServiceException();
        fault.setCode(code);
        fault.setDetails(details);
        throw new AuditManagementServiceException_Exception(message, fault);
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
