/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * TODO:
 *		- откуда брать последний отчетный день месяца?
 *
 * @author rtimerbaev
 */

/*
 * Проверка объязательных полей.
 */
def hasError = false
formData.dataRows.each { row ->
    if (!isTotal(row)) {
        def colNames = []
        // Список проверяемых столбцов (графа 2..7)
        def requiredColumns = ['series', 'amount', 'moninal', 'shortPositionDate',
                'balance2', 'averageWeightedPrice', 'termBondsIssued']

        requiredColumns.each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            hasError = true
            def index = row.series
            def errorMsg = colNames.join(', ')
            if (index != null) {
                logger.error("В строке \"Серия\" равной $index не заполнены колонки : $errorMsg.")
            } else {
                index = row.getOrder()
                logger.error("В строке $index не заполнены колонки : $errorMsg.")
            }
        }
    }
}
if (hasError) {
    return
}

/*
 * Расчеты.
 */

formData.dataRows.each { row ->
    if (!isTotal(row)) {
        // последний отчетный день месяца
        def lastDay = new Date() // TODO (Ramil Timerbaev) откуда брать
        // графа 8
        def tmp = ((row.moninal - row.averageWeightedPrice) * (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount
        row.percIncome = round(tmp, 2)
    }
}

/** Столбцы для которых надо вычислять итого А и Б. Графа 2, 8. */
def totalColumns = ['amount', 'percIncome']
def totalARow = formData.getDataRow('totalA')
def totalBRow = formData.getDataRow('totalB')
def aRow = formData.getDataRow('A')
def bRow = formData.getDataRow('B')
totalColumns.each { alias ->
    def tmp = summ(formData, new ColumnRange(alias, aRow.getOrder() - 1, totalARow.getOrder() - 2))
    totalARow.getCell(alias).setValue(tmp)
    tmp = summ(formData, new ColumnRange(alias, bRow.getOrder() - 1, totalBRow.getOrder() - 2))
    totalBRow.getCell(alias).setValue(tmp)
}

// посчитать Итого
formData.getDataRow('total').percIncome = totalARow.percIncome - totalBRow.percIncome

/*
 * Вспомогалельные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias() in ['total', 'A', 'B', 'totalA', 'totalB']
}