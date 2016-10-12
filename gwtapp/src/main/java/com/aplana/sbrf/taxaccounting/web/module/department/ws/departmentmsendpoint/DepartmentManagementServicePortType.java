package com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.ws.DepartmentWS_Manager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Created by lhaziev on 27.09.2016.
 */
@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint.DepartmentManagementService",
        targetNamespace = "http://taxaccounting.sbrf.aplana.com/DepartmentManagementService/",
        serviceName="DepartmentManagementService",
        portName="DepartmentManagementServicePort"//,
        //wsdlLocation="META-INF/wsdl/DepartmentManagementService.wsdl"
)
public class DepartmentManagementServicePortType extends SpringBeanAutowiringSupport implements DepartmentManagementService{

    private static final Log LOG = LogFactory.getLog(DepartmentManagementServicePortType.class);

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private DepartmentWS_Manager departmentWS_manager;

    @Override
    public TaxDepartmentChanges requestAllChanges() {
        TaxDepartmentChanges result = new TaxDepartmentChanges();
        TAUserInfo userInfo = userService.getSystemUserInfo();
        try {
            departmentWS_manager.sendChanges(result, new Logger());
            if (result.getErrorCode() == null || result.getErrorCode().isEmpty()) {
                result.setErrorCode("E0");
                auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userService.getSystemUserInfo(), userService.getSystemUserInfo().getUser().getDepartmentId(),
                        null, null, null, null, "Успешный обмен данными с АС СУНР.", null);
            } else {
                auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userService.getSystemUserInfo(), userService.getSystemUserInfo().getUser().getDepartmentId(),
                        null, null, null, null, result.getErrorText(), null);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setErrorCode("E5");
            result.setErrorText("Произошла непредвиденная ошибка при отправке сообщения в АС СУНР. Текс ошибки: " + e.getMessage());
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, "Произошли непредвиденные ошибки при обмен данными с вебсервисом АС СУНР: " + e.getLocalizedMessage(), null);
        }
        return result;
    }
}
