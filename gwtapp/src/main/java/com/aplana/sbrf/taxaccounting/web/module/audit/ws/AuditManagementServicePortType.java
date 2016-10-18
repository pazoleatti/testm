package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.io.PrintWriter;
import java.io.StringWriter;
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
            throws ServiceException {

        try {
            validate(auditLog);

            TAUserInfo userInfo = assembleUserInfo(auditLog);
            Integer departmentId = null;
            auditService.add(FormDataEvent.SUNR_USER_ACTION, userInfo, departmentId, null, null, null, null, auditLog.getNote(), null, null);
        } catch (ServiceException e) {
            String newMessage = e.getMessage() + "\n" + (auditLog != null ? auditLog.toString() : "");
            throw new ServiceException(newMessage, e.getFaultInfo());
        } catch (Exception e) {
            String newMessage = e.getMessage();
            throwException("E0", "Внутренняя ошибка сервиса", newMessage);
        }
    }

    private void validate(AuditLog auditLog) throws ServiceException {
        if (auditLog == null) {
            throwException("E1", "Некорректная структура сообщения", "Не удалось получить данные для логирования");
        } else {
            if (auditLog.getUserInfo() == null) {
                throwException("E1", "Некорректная структура сообщения", "Не удалось получить информацию о пользователе");
            } else {
                if (auditLog.getUserInfo().getUser() == null) {
                    throwException("E1", "Некорректная структура сообщения", "Не удалось получить информацию о пользователе");
                } else {
                    User user = auditLog.getUserInfo().getUser();
                    if (user.getLogin() == null || user.getLogin().isEmpty()) {
                        throwException("E1", "Некорректная структура сообщения", "Не удалось получить логин пользователя");
                    }
                    /*if (user.getName() == null || user.getName().isEmpty()) {
                        throwException("E1", "Некорректная структура сообщения", "Не удалось получить имя пользователя");
                    }*/
                    if (user.getRoles() == null || user.getRoles().isEmpty()) {
                        throwException("E1", "Некорректная структура сообщения", "Не удалось получить роли пользователя");
                    } else {
                        for (Role role : user.getRoles()) {
                            if (role.getName() == null || role.getName().isEmpty()) {
                                throwException("E1", "Некорректная структура сообщения", "Не удалось получить наименование роли пользователя");
                            }
                        }
                    }
                }
                /*if (auditLog.getUserInfo().getIp() == null) {
                    throwException("E1", "Некорректная структура сообщения", "Не удалось получить ip пользователя");
                }*/
            }
            if (auditLog.getNote() == null) {
                throwException("E1", "Некорректная структура сообщения", "Не удалось получить текст события");
            }
        }
    }

    private TAUserInfo assembleUserInfo(AuditLog auditLog) {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(assembleUser(auditLog));
        userInfo.setIp(auditLog.getUserInfo().getIp());
        return userInfo;
    }

    private TAUser assembleUser(AuditLog auditLog) {
        TAUser user = new TAUser();
        user.setLogin(auditLog.getUserInfo().getUser().getLogin());
        user.setName(auditLog.getUserInfo().getUser().getName());
        user.setDepartmentId(auditLog.getUserInfo().getUser().getDepartmentId());
        user.setRoles(assembleRoles(auditLog));
        return user;
    }

    private List<TARole> assembleRoles(AuditLog auditLog) {
        List<TARole> roles = new ArrayList<TARole>();
        for (Role role : auditLog.getUserInfo().getUser().getRoles()) {
            TARole taRole = new TARole();
            taRole.setName(role.getName());
            roles.add(taRole);
        }
        return roles;
    }

    private void throwException(String code, String details, String message) throws ServiceException {
        FaultInfo fault = new FaultInfo();
        fault.setCode(code);
        fault.setDetails(details);
        throw new ServiceException(message, fault);
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
