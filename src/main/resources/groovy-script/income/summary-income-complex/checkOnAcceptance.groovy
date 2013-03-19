/**
 * Проверка при осуществлении перевода формы в статус "Принята" (checkOnAcceptance.groovy).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * @since 21.02.2013 18:10
 */

departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
    if (department.formTypeId == formData.getFormType().getId()) {
        def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && form.getState() != WorkflowState.CREATED) {
            logger.error('Принятие сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
        }
    }
}