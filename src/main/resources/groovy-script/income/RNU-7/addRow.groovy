/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учёт которых требует применения метода начисления".
 *
 * @author rtimerbaev
 */

def row = formData.appendDataRow()
['balance', 'date', 'outcomeCode', 'docNumber', 'docDate', 'currencyCode',
        'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency'].each {
    row.getCell(it).editable = true
}