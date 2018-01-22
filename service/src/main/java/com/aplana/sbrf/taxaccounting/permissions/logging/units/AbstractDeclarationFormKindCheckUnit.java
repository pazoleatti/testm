package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;

/**
 * Абстрактная реализация {@link CheckUnit} для проверки вида налоговой формы.
 */
public abstract class AbstractDeclarationFormKindCheckUnit implements CheckUnit {

    @Autowired
    protected DeclarationTemplateService declarationTemplateService;

    @Autowired
    protected DepartmentService departmentService;

    @Autowired
    protected DepartmentReportPeriodService departmentReportPeriodService;

    protected boolean checkType(Logger logger, DeclarationData declarationData, String operationName, DeclarationFormKind comparingFormKind) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationFormKind declarationFormKind = declarationTemplate.getDeclarationFormKind();
        if (!declarationFormKind.equals(comparingFormKind)) {
            createErrorMsg(logger, declarationData, operationName, declarationFormKind.getTitle());
            return false;
        }
        return true;
    }

    protected void createErrorMsg(Logger logger, DeclarationData declarationData, String operationName, String formKind) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        String strCorrPeriod = "";
        if (departmentReportPeriod.getCorrectionDate() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
        }
        logger.error("Операция \"%s\" не выполнена для формы № %d, период: \"%s\", " +
                        "подразделение \"%s\". Идентификация ФЛ не допустима для форм типа \"%s\".",
                operationName,
                declarationData.getId(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                department.getName(),
                formKind);
    }
}
