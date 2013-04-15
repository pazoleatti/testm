/**
 * Удалить строку (deleteRow.groovy).
 * Форма "(РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учёт которых требует применения метода начисления".
 *
 * @author rtimerbaev
 */

formData.dataRows.remove(currentDataRow)

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}