package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.ws;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentChangeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.*;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.TaxDepartmentChange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lhaziev
 */
@Component
public class DepartmentWS_Manager {

    private static final Log LOG = LogFactory.getLog(DepartmentWS_Manager.class);

    @Autowired
    private DepartmentChangeService departmentChangeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService userService;

    private DepartmentWS getDepartmentWSPort() throws MalformedURLException {
        long timeout = 5000;
        String address = "http://172.16.121.78:9080/";
        DepartmentWS_Service departmentWS_Service = new DepartmentWS_Service(address+"DepartmentWS?wsdl");
        return departmentWS_Service.getDepartmentWSPort();
    }

    @Transactional
    public void sendChange(DepartmentChangeOperationType operationType, int depId, Logger logger) {
        DepartmentChange departmentChange = createChange(operationType, depId);
        departmentChangeService.addChange(departmentChange);
        sendChanges(logger);
    }

    @Transactional
    public void sendChanges(Logger logger) {
        TAUserInfo userInfo = userService.getSystemUserInfo();
        try {
            List<DepartmentChange> departmentChanges = departmentChangeService.getAllChanges();
            if (departmentChanges.size() > 0) {
                DepartmentWS departmentWS = getDepartmentWSPort();
                TaxDepartmentChangeStatus status = departmentWS.sendDepartmentChange(convert(departmentChanges));
                if (status.getErrorCode().equalsIgnoreCase("E0")) {
                    departmentChangeService.clear();
                    String msg = "Изменения подразделении успешно переданы в СУНР";
                    logger.info(msg);
                    auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                            null, null, null, null, msg, null);
                } else {
                    String msg = String.format("Изменения подразделении не былы отпралено в СУНР. Код ошибки: %s, текст ошибки: %s.", status.getErrorCode(), status.getErrorText());
                    logger.warn(msg);
                    auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                            null, null, null, null, msg, null);
                }
            }
        } catch (Exception e) {
            LOG.error("Возникли ошибки при отправке изменении подразделении в СУНР", e);
            String msg = String.format("Возникла ошибка при отправке изменении подразделении в СУНР. Ошибка: %s.", e.getLocalizedMessage());
            logger.warn(msg);
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, msg, null);
        }
    }

    private DepartmentChange createChange(DepartmentChangeOperationType operationType, int depId) {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setId(depId);
        departmentChange.setOperationType(operationType);
        if (operationType != DepartmentChangeOperationType.DELETE) {
            Department department = departmentService.getDepartment(depId);
            departmentChange.setParentId(department.getParentId());
            departmentChange.setLevel(departmentService.getHierarchyLevel(depId));
            departmentChange.setName(department.getName());
            departmentChange.setShortName(department.getShortName());
            departmentChange.setParentId(department.getParentId());
            departmentChange.setType(department.getType());
            departmentChange.setTbIndex(department.getTbIndex());
            departmentChange.setSbrfCode(department.getSbrfCode());

            if (department.getRegionId() != null) {
                RefBookDataProvider dataProvider = refBookFactory.getDataProvider(4L);
                Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(department.getRegionId());
                departmentChange.setRegion(refBookValueMap.get("NAME").getStringValue());
            } else {
                departmentChange.setRegion(null);
            }
            departmentChange.setIsActive(department.isActive());
            departmentChange.setCode(department.getCode());
            departmentChange.setGarantUse(department.isGarantUse());
            departmentChange.setSunrUse(department.isSunrUse());
        }
        return departmentChange;
    }

    private List<TaxDepartmentChange> convert(List<DepartmentChange> departmentChangeList) {
        List<TaxDepartmentChange> taxDepartmentChangeList = new ArrayList<TaxDepartmentChange>();
        for(DepartmentChange departmentChange: departmentChangeList) {
            TaxDepartmentChange taxDepartmentChange = new TaxDepartmentChange();
            taxDepartmentChange.setOperationType(departmentChange.getOperationType().getCode());
            taxDepartmentChange.setId(departmentChange.getId());
            taxDepartmentChange.setChange_datetime(departmentChange.getLogDate());
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
