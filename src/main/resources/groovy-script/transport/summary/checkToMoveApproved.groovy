/**
 * Проверки при переводe формы в статус "Утверждена"
 * Проверка, наличия и статуса сводной формы уровня Банка  при осуществлении перевода формы в статус "Утверждена".
 * Форма "Расчет суммы налога по каждому транспортному средству".
 *
 * @author auldanov
 * @since 22.02.2013 13:30
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
             * 2.	Система не должна выполнять изменение статуса формы на "Утверждена".
             *		logger error который вызывается выше решает эту проблему
             */
        }
    }
}