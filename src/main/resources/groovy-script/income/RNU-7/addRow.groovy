/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учёт которых требует применения метода начисления".
 *
 * @author rtimerbaev
 */

def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())
['balance', 'date', 'outcomeCode', 'docNumber', 'docDate', 'currencyCode',
        'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency'].each {
    newRow.getCell(it).editable = true
}
formData.dataRows.add(currentDataRow.getOrder(), newRow)

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}