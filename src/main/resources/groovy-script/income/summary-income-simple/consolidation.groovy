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
 * Скрипт для консолидации (consolidation.groovy).
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
 *
 * @author rtimerbaev
 * @since 08.02.2013 13:00
 */

// очистить форму
formData.getDataRows().each{ row ->
    ['rnu6Field10Sum', 'rnu6Field10Field2', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'rnu4Field5PrevTaxPeriod'].each{ alias->
        row.getCell(alias).setValue(null)
    }
}
// получить данные из источников
departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
    def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
    if (child != null && child.state == WorkflowState.ACCEPTED) {
        child.getDataRows().eachWithIndex() { row, i ->
            def rowResult = formData.getDataRows().get(i)
            ['rnu6Field10Sum', 'rnu6Field10Field2', 'rnu6Field12Accepted',
                    'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted', 'rnu4Field5PrevTaxPeriod'].each {
                if (row.getCell(it).getValue() != null) {
                    rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                }
            }
        }
    }
}