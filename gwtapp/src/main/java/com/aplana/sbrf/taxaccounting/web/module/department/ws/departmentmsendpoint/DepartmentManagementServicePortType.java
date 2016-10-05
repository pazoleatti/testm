package com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentChangeService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

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
    private DepartmentChangeService departmentChangeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private LockDataService lockDataService;

    @Override
    public TaxDepartmentChanges getAllChanges() {
        TaxDepartmentChanges result = new TaxDepartmentChanges();
        TAUserInfo userInfo = userService.getSystemUserInfo();
        try {
            String keySend = departmentChangeService.generateTaskKey(ReportType.SEND_DEPARTMENT_CHANGE);
            String keyTask = departmentChangeService.generateTaskKey(ReportType.GET_ALL_DEPARTMENT_CHANGES);
            LockData lockDataSend = lockDataService.getLock(keySend);
            if (lockDataSend == null && (lockDataService.lock(keyTask, userInfo.getUser().getId(), "")) == null) {
                try{
                    getAllChanges(result, lockDataService.getLock(keyTask));
                    result.setErrorCode("E0");
                    auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userService.getSystemUserInfo(), userService.getSystemUserInfo().getUser().getDepartmentId(),
                            null, null, null, null, "Успешный обмен данными с вебсервисом СУНР.", null);
                } finally {
                    lockDataService.unlock(keyTask, userService.getSystemUserInfo().getUser().getId());
                }
            } else {
                result.setErrorCode("E2");
                result.setErrorText("Справочник «Подразделения» АС «Учет налогов» заблокирован, попробуйте выполнить операцию позже");
                auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                        null, null, null, null, "Произошли ошибки при обмен данными с вебсервисом СУНР: Изменение истории подразделений заблокировано.", null);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            result.setErrorCode("E3");
            result.setErrorText("Произошла непредвиденная ошибка: " + e.getMessage());
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, "Произошли непредвиденные ошибки при обмен данными с вебсервисом СУНР: " + e.getLocalizedMessage(), null);
        }
        return result;
    }

    @Transactional
    private void getAllChanges(TaxDepartmentChanges result, LockData lockData) {
        result.getTaxDepartmentChanges().addAll(convert(departmentChangeService.getAllChanges()));
        departmentChangeService.clear();
        if (!lockDataService.isLockExists(lockData.getKey(), lockData.getDateLock())) {
            result.getTaxDepartmentChanges().clear();
            throw new ServiceException("Задача больше не актуальна.");
        }
    }

    private List<TaxDepartmentChange> convert(List<DepartmentChange> departmentChangeList) {
        List<TaxDepartmentChange> taxDepartmentChangeList = new ArrayList<TaxDepartmentChange>();
        for(DepartmentChange departmentChange: departmentChangeList) {
            TaxDepartmentChange taxDepartmentChange = new TaxDepartmentChange();
            taxDepartmentChange.setOperationType(departmentChange.getOperationType().getCode());
            taxDepartmentChange.setId(departmentChange.getId());
            if (departmentChange.getOperationType() != DepartmentChangeOperationType.DELETE) {
                taxDepartmentChange.setLevel(departmentChange.getId());
                taxDepartmentChange.setName(departmentChange.getName());
                taxDepartmentChange.setShortName(departmentChange.getShortName());
                taxDepartmentChange.setParentId(departmentChange.getParentId());
                taxDepartmentChange.setType(departmentChange.getType().getCode());
                taxDepartmentChange.setTbIndex(departmentChange.getTbIndex());
                taxDepartmentChange.setSbrfCode(departmentChange.getSbrfCode());
                taxDepartmentChange.setRegion(departmentChange.getRegion());
                taxDepartmentChange.setIsActive(departmentChange.getIsActive());
                taxDepartmentChange.setCode(departmentChange.getCode());
                taxDepartmentChange.setGarantUse(departmentChange.getGarantUse());
                taxDepartmentChange.setSunrUse(departmentChange.getSunrUse());
            }
            taxDepartmentChangeList.add(taxDepartmentChange);
        }
        return taxDepartmentChangeList;
    }
}
