/**
 * Проверка, наличия и статуса сводной формы уровня Банка при осуществлении перевода формы в статус "Утверждена" (checkOnApproval.groovy).
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 * @author auldanov
 * @since 21.02.2013 14:00
 */

departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
    if (department.formTypeId == formData.getFormType().getId()) {
        def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
        if (form != null && form.getState() != WorkflowState.CREATED) {
            /*
                * 1.	Система должна выдать Пользователю сообщение  о том,
                *		что Утверждение сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.
                */
            logger.error('Утверждение сводной налоговой формы невозможно, т.к. уже подготовлена сводная налоговая форма Банка.')
            /*
                * 2.	Система не должна выполнять изменение статуса формы на "Утверждена". logger error который вызывается выше решает эту проблему
                */
        }
    }
}