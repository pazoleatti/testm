/* Условие. */
// проверка на банк
boolean isBank = true
departmentFormTypeService.getDestinations(formData.departmentId, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
    if (it.departmentId != formData.departmentId) {
        isBank = false
    }
}
return isBank
/* конец условия. */

/**
 * Скрипт для консолидации (consolidation.groovy).
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 * @author rtimerbaev
 * @since 21.02.2013 13:50
 */

boolean isFirst = true;

// получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.SUMMARY).each {
    if (it.formTypeId == 303) {
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
                    ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod',
                            'rnu5Field5Accepted',	'rnu5Field5PrevTaxPeriod'].each { columnAlias ->
                        row[columnAlias] = null
                    }
                }
            }
            child.getDataRows().eachWithIndex() { row, i ->
                def rowResult = formData.getDataRows().get(i)
                ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod',
                        'rnu5Field5Accepted', 'rnu5Field5PrevTaxPeriod'].each {
                    if (row.getCell(it).getValue() != null) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
}