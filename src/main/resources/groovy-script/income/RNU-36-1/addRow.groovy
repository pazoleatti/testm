/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * @author rtimerbaev
 */

def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

// графа 1..7
['series', 'amount', 'moninal', 'shortPositionDate',
        'balance2', 'averageWeightedPrice', 'termBondsIssued'].each {
    newRow.getCell(it).editable = true
    newRow.getCell(it).styleAlias = 'Редактируемые'
}
newRow.getCell('percIncome').styleAlias = 'Вычисляемые'

if (currentDataRow == null) {
    row = formData.getDataRow('totalA')
    formData.dataRows.add(row.getOrder() - 1, newRow)
} else if (currentDataRow.getAlias() == null) {
    formData.dataRows.add(currentDataRow.getOrder(), newRow)
} else {
    def row
    switch (currentDataRow.getAlias()) {
        case 'A' :
        case 'totalA' :
            row = formData.getDataRow('totalA')
            break
        case 'B' :
        case 'totalB' :
        case 'total' :
            row = formData.getDataRow('totalB')
            break
    }
    formData.dataRows.add(row.getOrder() - 1, newRow)
}

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}

// графа 1  - series
// графа 2  - amount
// графа 3  - moninal
// графа 4  - shortPositionDate
// графа 5  - balance2
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome