/**
 * Удалить строку (deleteRow.groovy).
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * @author rtimerbaev
 */

formData.dataRows.remove(currentDataRow)

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}