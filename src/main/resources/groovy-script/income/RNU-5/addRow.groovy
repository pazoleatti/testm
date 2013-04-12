/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-5) Простой регистр налогового учёта «расходы»".
 *
 * @author rtimerbaev
 */

def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())
['code', 'balance', 'name', 'sum'].each {
    newRow.getCell(it).editable = true
}

formData.dataRows.add(currentDataRow.getOrder(), newRow)

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}