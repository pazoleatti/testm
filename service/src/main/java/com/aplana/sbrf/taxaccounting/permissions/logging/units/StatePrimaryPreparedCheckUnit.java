package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * Реализация {@link CheckUnit} для проверки находится ли налоговая форма в состоянии "Создана" или "Подготовлена"
 */
@Component
public class StatePrimaryPreparedCheckUnit implements CheckUnit {

    @Autowired
    protected DeclarationTemplateService declarationTemplateService;

    @Autowired
    protected DepartmentService departmentService;

    @Autowired
    protected DepartmentReportPeriodService departmentReportPeriodService;

    @Override
    public boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, String operationName) {
        if (!(declarationData.getState().equals(State.CREATED) || declarationData.getState().equals(State.PREPARED))) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
            Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
            String strCorrPeriod = "";
            if (departmentReportPeriod.getCorrectionDate() != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
            }
            logger.error("Операция \"%s\" не выполнена для формы № %d,  период: \"%s\", " +
                    "подразделение: \"%s\". Идентификация ФЛ не допустима для форм в состоянии \"%s\".",
                    operationName,
                    declarationData.getId(),
                    departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                    department.getName(),
                    declarationData.getState().getTitle());
            return false;
        }
        return true;
    }
}
