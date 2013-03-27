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
 * Проверка, наличия и статуса сводной формы уровня Банка  при осуществлении перевода формы в статус "Утверждена" (checkOnApproval.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author auldanov
 * @since 21.03.2013 17:00
 */

departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
    if (department.formTypeId == formData.getFormType().getId()) {
        def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
        // если форма существует и статус "принята"
        if (form != null && form.getState() == WorkflowState.ACCEPTED) {
            logger.error('Утверждение сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
        }
    }
}