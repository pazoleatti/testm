/* Условие. */
// проверка на банк
boolean isBank = true
departmentFormTypeService.getDestinations(formData.departmentId, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
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
 * @since 21.03.2013 11:00
 */
departmentFormTypeService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.SUMMARY).each { department ->
    def bank = declarationService.find(2, department.departmentId, formData.reportPeriodId)
    if (bank != null && bank.accepted) {
        logger.error('Принятие налоговой формы невозможно, т.к. уже принята декларация Банка.')
    }
}