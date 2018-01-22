package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Абстрактная реализация {@link CheckUnit} для проверки прав и ролей пользователя.
 */
public abstract class AbstractCredentialsCheckUnit implements CheckUnit {

    @Autowired
    protected DeclarationTemplateService declarationTemplateService;

    @Autowired
    protected DepartmentService departmentService;

    @Autowired
    protected DepartmentReportPeriodService departmentReportPeriodService;

    protected void createErrorMsg(Logger logger, DeclarationData declarationData, String operationName) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        String strCorrPeriod = "";
        if (departmentReportPeriod.getCorrectionDate() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            strCorrPeriod = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
        }
        logger.error("Операция \"%s\" не выполнена для формы № %d, " +
                        "период: \"%s\", подразделение \"%s\". Недостаточно прав для выполнения операции.",
                operationName,
                declarationData.getId(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                department.getName());
    }

    protected boolean canView(TAUserInfo userInfo, DeclarationData declarationData) {

        TAUser taUser = userInfo.getUser();
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        Long asnuId = declarationData.getAsnuId();

        if (taUser.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            return true;
        }

        //Подразделение формы
        Department declarationDepartment = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

        // Контролёр НС
        if (taUser.hasRole(TARole.N_ROLE_CONTROL_NS)) {
            //ТБ формы
            int declarationTB = departmentService.getParentTB(declarationDepartment.getId()).getId();
            //Подразделение и ТБ пользователя
            int userTB = departmentService.getParentTB(taUser.getDepartmentId()).getId();

            //Подразделение формы и подразделение пользователя должны относиться к одному ТБ или
            if (userTB == declarationTB) {
                return true;
            }

            //ТБ подразделений, для которых подразделение пользователя является исполнителем макетов
            List<Integer> tbDepartments = departmentService.getAllTBPerformers(userTB, declarationTemplate.getType());

            //Подразделение формы относится к одному из ТБ подразделений, для которых подразделение пользователя является исполнителем
            if (tbDepartments.contains(declarationTB)) {
                return true;
            }
        }

        // Оператор
        if (taUser.hasRole(TARole.N_ROLE_OPER)) {
            if (asnuId != null && !checkUserAsnu(taUser, asnuId)) {
                return false;
            }

            List<Integer> executors = departmentService.getTaxDeclarationDepartments(taUser, declarationTemplate.getType());
            if (executors.contains(declarationDepartment.getId())) {
                if (!declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.CONSOLIDATED)) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * Проверяет есть у пользователя права на АСНУ декларации.
     * Если табличка SEC_USER_ASNU пустая, то права есть на все записи.
     *
     * @param user   пользователь
     * @param asnuId АСНУ НФ, для ПНФ значение должно быть задано, для остальных форм null
     */
    private boolean checkUserAsnu(TAUser user, Long asnuId) {
        if (user.getAsnuIds() == null || user.getAsnuIds().isEmpty()) {
            return true;
        }

        return user.getAsnuIds().contains(asnuId);
    }
}
