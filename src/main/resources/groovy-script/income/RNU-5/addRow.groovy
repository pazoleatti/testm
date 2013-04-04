/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-5) Простой регистр налогового учёта «расходы»".
 *
 * @author rtimerbaev
 */

def row = formData.appendDataRow()
['code', 'balance', 'name', 'sum'].each {
    row.getCell(it).editable = true
}