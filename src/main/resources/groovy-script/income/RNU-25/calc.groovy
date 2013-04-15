/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * TODO:
 * 		- про нумерацию пока не уточнили, пропустить
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
        // Список проверяемых столбцов (графа 2..13)
        def columns = ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', 'reserve',
                'cost', 'signSecurity', 'marketQuotation', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

        columns.each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }

        if (!colNames.isEmpty()) {
            hasError = true
            def index = row.rowNumber
            def errorMsg = colNames.join(', ')
            if (index != null) {
                logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
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

// удалить строку "итого" и "итого по ГРН: ..."
def delRow = []
formData.dataRows.each { row ->
    if (isTotal(row)) {
        delRow += row
    }
}
delRow.each { row ->
    formData.dataRows.remove(formData.dataRows.indexOf(row))
}

// отсортировать/группировать
formData.dataRows.sort { it.regNumber }

/** Столбцы для которых надо вычислять итого и итого по ГРН. Графа 4..7, 10..13. */
def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
// добавить строку "итого"
def totalRow = formData.appendDataRow()
totalRow.setAlias('total')
totalRow.regNumber = 'Итого'
totalColumns.each { alias ->
    totalRow.getCell(alias).setValue(getSum(alias))
}

// посчитать "итого по ГРН:..."
def totalRows = [:]
def tmp = null
def sums = [:]
totalColumns.each {
    sums[it] = 0
}
formData.dataRows.eachWithIndex { row, i ->
    if (!isTotal(row)) {
        if (tmp == null) {
            tmp = row.regNumber
        }
        // если код расходы поменялся то создать новую строку "итого по ГРН:..."
        if (tmp != row.regNumber) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по ГРН:..."
        if (i == formData.dataRows.size() - 2) {
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            totalRows.put(i + 1, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            sums[it] += row.getCell(it).getValue()
        }
        tmp = row.regNumber
    }
}
// добавить "итого по ГРН:..." в таблицу
def i = 0
totalRows.each { index, row ->
    formData.dataRows.add(index + i, row)
    i = i + 1
}

// графа 1, + поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    if (row.getAlias() != 'total') {
        row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
        row.setOrder(index + 1)
    }
}

/*
 * Вспомогалельные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    return summ(formData, new ColumnRange(columnAlias, 0, formData.dataRows.size() - 2))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.regNumber = 'Итого по ГРН:...'
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}