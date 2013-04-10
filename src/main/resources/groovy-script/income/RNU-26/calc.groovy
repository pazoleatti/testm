/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения".
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
        // Список проверяемых столбцов
        def columns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev',
                'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
                'marketQuotationInRub', 'costOnMarketQuotation']

        // если не заполнены графа 11 и графа 12, то графа 13 должна быть заполнена вручную
        if (row.marketQuotation == null && row.rubCourse == null) {
            columns -= 'marketQuotationInRub'
        }

        columns.each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            hasError = true
            def index = row.rowNumber
            def errorMsg = colNames.join(', ')
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        }
    }
}
if (hasError) {
    return
}

/*
 * Расчеты.
 */

// удалить строку "итого" и "итого по Эмитенту: ..."
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
formData.dataRows.sort { it.issuer }

// графа 13
formData.dataRows.each { row ->
    if (row.marketQuotation != null && row.rubCourse != null) {
        row.marketQuotationInRub = row.marketQuotation * row.rubCourse
    }
}

/** Столбцы для которых надо вычислять итого и итого по эмитенту. Графа 6..9, 14..17. */
def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
        'cost', 'costOnMarketQuotation', 'reserveCalcValue',
        'reserveCreation', 'reserveRecovery']
// добавить строку "итого"
def totalRow = formData.appendDataRow()
totalRow.setAlias('total')
totalRow.issuer = 'Итого'

totalColumns.each { alias ->
    totalRow.getCell(alias).setValue(getSum(alias))
}

// посчитать "итого по Эмитента:..."
def totalRows = [:]
def sums = [:]
def tmp = null
totalColumns.each {
    sums[it] = 0
}
formData.dataRows.eachWithIndex { row, i ->
    if (!isTotal(row)) {
        if (tmp == null) {
            tmp = row.issuer
        }
        // если код расходы поменялся то создать новую строку "итого по Эмитента:..."
        if (tmp != row.issuer) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по Эмитента:..."
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
        tmp = row.issuer
    }
}
// добавить "итого по Эмитента:..." в таблицу
def i = 0
totalRows.each { index, row ->
    formData.dataRows.add(index + i, row)
    i = i + 1
}

// графа 1, + поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
    row.setOrder(index + 1)
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
    newRow.issuer = 'Итого по Эмитенту:...'
    newRow.shareType = alias
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}