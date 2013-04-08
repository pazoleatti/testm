/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования".
 *
 * @author rtimerbaev
 */

def row = formData.appendDataRow()
// графа 2..12
['contractNumber', 'contraclData', 'base', 'transactionDate', 'course',
        'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate',
        'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate'].each {
    row.getCell(it).editable = true
}