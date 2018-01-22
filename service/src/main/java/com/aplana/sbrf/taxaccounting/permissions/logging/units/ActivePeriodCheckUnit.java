package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * Реализация {@link CheckUnit} для проверки является ли период открытым.
 */
@Component
public class ActivePeriodCheckUnit implements CheckUnit {

    @Autowired
    protected DeclarationTemplateService declarationTemplateService;

    @Autowired
    protected DepartmentService departmentService;

    @Autowired
    protected DepartmentReportPeriodService departmentReportPeriodService;

    @Override
    public boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, String operationName) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        if (!departmentReportPeriod.isActive()) {
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
            String strCorrPeriod = "";
            if (departmentReportPeriod.getCorrectionDate() != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
            }
            logger.error("Операция \"%s\" не выполнена для формы № %d,  период: \"%s\"," +
                    " подразделение \"%s\". Период формы закрыт.",
                    operationName,
                    declarationData.getId(),
                    departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                    department.getName());
            return false;
        }
        return true;
    }
}
