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
 * Скрипт для консолидации (consolidation.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author rtimerbaev
 * @since 22.02.2013 15:30
 */

// очистить форму
formData.getDataRows().each{ row ->
    ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { it ->
        row.getCell(it).setValue(null)
    }
}

// получить консолидированные формы из источников
departmentFormTypeService.getSources(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY).each {
    def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
    if (child != null && child.state == WorkflowState.ACCEPTED
            && child.formType.id == 303) {
        child.getDataRows().eachWithIndex() { row, i ->
            def rowResult = formData.getDataRows().get(i)
            ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
                if (row.getCell(it).getValue() != null) {
                    rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                }
            }
        }
    }
}
logger.info('Формирование сводной формы уровня Банка прошло успешно.')