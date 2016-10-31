package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint.DepartmentManagementServicePortType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG = LogFactory.getLog(AuditManagementServicePortType.class);

    @Autowired
    private AuditService auditService;

    @Autowired
    private DepartmentService departmentService;

    public StatusInfo addAuditLog(AuditLog auditLog) {
        StatusInfo result = new StatusInfo();

        try {
            validate(auditLog);

            TAUserInfo userInfo = assembleUserInfo(auditLog);
            Integer departmentId = null;
            auditService.add(FormDataEvent.SUNR_USER_ACTION, userInfo, departmentId, null, null, null, null, auditLog.getNote(), null, null);
            result.setCode("E0");
            result.setText("Успех");
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            result.setCode(e.getFaultInfo().getCode());
            result.setText(e.getMessage() + (e.getFaultInfo().getDetails() != null ? "\n Описание ошибки: " + e.getFaultInfo().getDetails() : ""));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setCode("E7");
            result.setText("Внутренняя ошибка сервиса" + "\n Описание ошибки: " + e.getMessage());
        }

        return result;
    }

    private void validate(AuditLog auditLog) throws ServiceException {
        if (auditLog == null) {
            throwException("E8", "Отсутствует или не заполнен обязательный атрибут \"auditLog\"", "Некорректная структура сообщения");
        } else {
            if (auditLog.getUserInfo() == null) {
                throwException("E8", "Отсутствует или не заполнен обязательный атрибут \"userInfo\"", "Некорректная структура сообщения");
            } else {
                if (auditLog.getUserInfo().getUser() == null) {
                    throwException("E8", "Отсутствует или не заполнен обязательный атрибут \"user\"", "Некорректная структура сообщения");
                } else {
                    User user = auditLog.getUserInfo().getUser();
                    if (user.getLogin() == null || user.getLogin().isEmpty()) {
                        throwException("E8", "Отсутствует или не заполнен обязательный атрибут \"login\"", "Некорректная структура сообщения");
                    }
                    /*if (user.getName() == null || user.getName().isEmpty()) {
                        throwException("E8", "Не удалось получить имя пользователя", "Некорректная структура сообщения");
                    }*/
                    try {
                        departmentService.getDepartment(user.getDepartmentId());
                    } catch (DaoException e) {
                        throwException("E9", null, "Не удалось найти подразделение банка с id = " + user.getDepartmentId());
                    }
                    if (user.getRoles() == null || user.getRoles().isEmpty()) {
                        throwException("E8", "Отсутствует или не заполнен обязательный атрибут \"roles\"", "Некорректная структура сообщения");
                    } else {
                        for (Role role : user.getRoles()) {
                            if (role.getName() == null || role.getName().isEmpty()) {
                                throwException("E8", "Отсутствует или не заполнен обязательный атрибут \"name\"(Role)", "Некорректная структура сообщения");
                            }
                        }
                    }
                }
                /*if (auditLog.getUserInfo().getIp() == null) {
                    throwException("E8", "Не удалось получить ip пользователя", "Некорректная структура сообщения");
                }*/
            }
            if (auditLog.getNote() == null) {
                throwException("E8", "Не удалось получить текст события", "Некорректная структура сообщения");
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
