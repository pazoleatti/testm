/* Условие. */
// проверка на банк
boolean isBank = true
departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
    if (it.departmentId != formData.departmentId) {
        isBank = false
    }
}
return isBank
/* Конец условия. */

/**
 * Проверки наличия декларации Банка при принятии нф (checkDeclarationBankOnAcceptance.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author rtimerbaev
 * @since 22.02.2013 13:10
 */

departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
    def bank = declarationService.find(1, department.departmentId, formData.reportPeriodId)
    if (bank != null && bank.getState() != WorkflowState.CREATED) {
        logger.error("Принятие налоговой формы невозможно, т.к. уже принята декларация Банка.")
    }
}