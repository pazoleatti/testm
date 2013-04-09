/* Условие. */
// проверка на террбанк
boolean isTerBank = false
departmentFormTypeService.getDestinations(formData.departmentId, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
    if (it.departmentId != formData.departmentId) {
        isTerBank = true
    }
}
return isTerBank
/* Конец условия. */

/**
 * Проверки при переходе "Отменить принятие" (checkOnCancelAcceptance.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author rtimerbaev
 * @since 15.02.2013 16:30
 */
List<DepartmentFormType> departments = departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY);
def department = departments[0];
def bank = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

if (bank != null && (bank.getState() == WorkflowState.APPROVED || bank.getState() == WorkflowState.ACCEPTED)) {
    logger.error("Отмена принятия налоговой формы невозможно, т.к. уже принята сводная налоговая форма Банка.")
}