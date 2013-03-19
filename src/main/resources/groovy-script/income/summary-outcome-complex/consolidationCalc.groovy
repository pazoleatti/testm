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
 * Получения данных из простых расходов (консолидация) (consolidationCalc.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 22.02.2013 12:40
 */

/**
 * Копирует данные для строк 500x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 */
void copyFor500x(String fromRowA, String toRowA, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)
    // строка для копирования из другой нф
    fromRow = fromForm.getDataRow(fromRowA)

    // 7 графа
    toRow.consumptionBuhSumPrevTaxPeriod = summ(toRow.getCell('consumptionBuhSumPrevTaxPeriod'), fromRow.getCell('rnu5Field5PrevTaxPeriod'))
    // 9 графа
    toRow.consumptionTaxSumS = summ(toRow.getCell('consumptionTaxSumS'), fromRow.getCell('rnu5Field5Accepted'))
}

/**
 * Копирует данные для строк 700x.
 *
 * @param fromRowA псевдоним строки нф откуда копируем данные
 * @param toRowA псевдоним текущей строки куда копируем данные
 */
void copyFor700x(String fromRowA, String toRowA, def fromForm) {
    // строка для заполнения из текущей нф
    toRow = formData.getDataRow(toRowA)
    // строка для копирования из другой нф
    fromRow = fromForm.getDataRow(fromRowA)

    // 6 графа
    toRow.consumptionBuhSumAccepted = summ(toRow.getCell('consumptionBuhSumAccepted'), fromRow.getCell('rnu7Field12Accepted'))
    // 9 графа
    toRow.consumptionTaxSumS = summ(toRow.getCell('consumptionTaxSumS'), fromRow.getCell('rnu7Field10Sum'))
}

// получение нф расходов простых
departmentFormTypeService.getSources(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY).each {
    def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
    if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == 304) {
        // 50001 - 93 строка
        copyFor500x('R85', 'R93', child)
        // 50002 - 110 строка
        copyFor500x('R89', 'R110', child)
        // 50011 - 140 строка
        copyFor500x('R194', 'R140', child)

        // 70001 - 94 строка
        copyFor700x('R86', 'R94', child)
        // 70011 - 141 строка
        copyFor700x('R195', 'R141', child)
    }
}