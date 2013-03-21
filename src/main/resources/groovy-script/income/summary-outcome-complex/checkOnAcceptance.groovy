/* Условие. */
// проверка на террбанк
boolean isTerBank = false
departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
    if (it.departmentId != formData.departmentId) {
        isTerBank = true
    }
}
return isTerBank
/* Конец условия. */

/**
 * Проверка при осуществлении перевода формы в статус "Принята" (checkOnAcceptance.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @since 20.03.2013 16:00
 */

departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
    if (department.formTypeId == formData.getFormType().getId()) {
        def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && form.getState() != WorkflowState.ACCEPTED) {
            logger.error('Принятие сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
        }
    }
}