/**
 * Проверка при осуществлении перевода формы в статус "Принята" (checkOnAcceptance.groovy).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * @since 21.03.2013 17:00
 */

departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
    if (department.formTypeId == formData.getFormType().getId()) {
        def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
        // если форма существует и статус "принята"
        if (form != null && form.getState() == WorkflowState.ACCEPTED) {
            logger.error('Принятие сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
        }
    }
}