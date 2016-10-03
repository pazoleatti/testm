package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.ws;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentChange;
import com.aplana.sbrf.taxaccounting.model.DepartmentChangeOperationType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentChangeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.DepartmentWS;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.TaxDepartmentChange;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.TaxDepartmentChangeStatus;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lhaziev
 */
@Component
public class DepartmentWS_Service {

    @Autowired
    private DepartmentChangeService departmentChangeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RefBookFactory refBookFactory;

    private DepartmentWS getDepartmentWSPort(String address) {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setServiceClass(DepartmentWS.class);
        factoryBean.setAddress(address);
        DepartmentWS departmentWS = (DepartmentWS) factoryBean.create();
        return departmentWS;
    }

    public void sendChange(DepartmentChangeOperationType operationType, int depId, Logger logger) {
        DepartmentChange departmentChange = createChange(operationType, depId);
        if (!departmentChangeService.checkDepartment(departmentChange.getId(), departmentChange.getParentId())) {
            try {
                TaxDepartmentChange taxDepartmentChange = new TaxDepartmentChange();
                taxDepartmentChange.setOperationType(departmentChange.getOperationType().getCode());
                taxDepartmentChange.setId(departmentChange.getId());
                if (departmentChange.getOperationType() != DepartmentChangeOperationType.DELETE) {
                    taxDepartmentChange.setLevel(departmentChange.getLevel());
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
                String address = "http://172.16.121.78:9080/";
                DepartmentWS departmentWS = getDepartmentWSPort(address);
                TaxDepartmentChangeStatus status = departmentWS.sendDepartmentChange(taxDepartmentChange);
                logger.info("Изменение успешно передано СУНР");
            } catch (Exception e) {
                logger.info("Возникли ошибки при отправке изменения подразделения в СУНР");
                departmentChangeService.addChange(departmentChange);
            }
        } else {
            logger.info("Возникли ошибки при отправке изменения подразделения в СУНР");
            departmentChangeService.addChange(departmentChange);
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
}
