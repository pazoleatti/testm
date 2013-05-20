import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState

/**
 * Проверки при переходе "Отменить принятие" (checkOnCancelAcceptance.groovy).
 */

// проверка на террбанк
boolean isTerBank = false
departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
    if (it.departmentId != formData.departmentId) {
        isTerBank = true
    }
}
if (isTerBank) {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY);
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData bank = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (bank != null && (bank.getState() == WorkflowState.APPROVED || bank.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}