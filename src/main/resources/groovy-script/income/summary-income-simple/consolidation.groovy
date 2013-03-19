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
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
 *
 * @author rtimerbaev
 * @since 08.02.2013 13:00
 */

boolean isFirst = true;

// получить данные из источников
departmentFormTypeService.getSources(formDataDepartment.id, formData.formTemplateId, FormDataKind.SUMMARY).each {
    def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
    if (child != null && child.state == WorkflowState.ACCEPTED) {
        if (isFirst) {
            // Удалить все строки
            formData.dataRows.clear()

            isFirst = false;
            child.getDataRows().each { row->
                def newRow = formData.appendDataRow()
                newRow.putAll(row)
                newRow.setAlias(row.getAlias())
            }
            formData.getDataRows().each { row ->
                ['rnu6Field10Sum', 'rnu6Field10Field2', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod',
                        'rnu4Field5Accepted', 'rnu4Field5PrevTaxPeriod'].each { columnAlias ->
                    row[columnAlias] = null
                }
            }
        }
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
logger.info('Формирование сводной формы уровня Банка прошло успешно.')