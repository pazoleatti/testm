/**
 * Удалить строку (deleteRow.groovy).
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * @author rtimerbaev
 */

if (isTotal(currentDataRow)) {
    return
}

formData.dataRows.remove(currentDataRow)

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}

/*
 * Вспомогалельные методы.
 */

/**
 * Проверка является ли строка итоговой. // TODO (Ramil Timerbaev)
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias() in ['total', 'A', 'B', 'totalA', 'totalB']
}