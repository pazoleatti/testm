/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учёт которых требует применения метода начисления".
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
        def requiredColumns = ['balance', 'date', 'incomeCode', 'docNumber', 'docDate', 'currencyCode',
                'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency']

        // если "графа 7" == 'RUR', то "графа 8" = 1 и ее проверять не надо
        def needColumn8 = (row.currencyCode != null && row.currencyCode == 'RUR')
        if (!needColumn8) {
            requiredColumns -= 'rateOfTheBankOfRussia'
        }

        requiredColumns.each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            hasError = true
            def index = row.number
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

// удалить строки "итого" и "итого по коду"
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
formData.dataRows.sort { it.incomeCode }

// графа 8 (для строк НЕитого)
formData.dataRows.each { row ->
    if (row.currencyCode != null && row.currencyCode == 'RUR') {
        row.rateOfTheBankOfRussia = 1
    }
}

// графа 10, 12
formData.dataRows.each { row ->
    // графа 10 (для строк НЕитого) = графа 9 * графа 8
    row.taxAccountingRuble = round(row.taxAccountingCurrency * row.rateOfTheBankOfRussia, 2)

    // графа 12 (для строк НЕитого) = графа 11 * графа 8
    row.ruble = round(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)
}

/** Столбцы для которых надо вычислять итого и итого по эмитенту. Графа 10, 12. */
def totalColumns = ['taxAccountingRuble', 'ruble']

// добавить строки "итого"
def totalRow = formData.appendDataRow()
totalRow.setAlias('total')
totalRow.incomeCode = 'Итого'
// графа 10, 12 для последней строки "итого"
totalColumns.each { alias ->
    totalRow.getCell(alias).setValue(getSum(alias))
}

// посчитать "итого по коду"
def totalRows = [:]
def tmp = null
def sums = [:]
totalColumns.each {
    sums[it] = 0
}
formData.dataRows.eachWithIndex { row, i ->
    if (!isTotal(row)) {
        if (tmp == null) {
            tmp = row.incomeCode
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.incomeCode) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
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
        tmp = row.incomeCode
    }
}

// добавить "итого по коду" в таблицу
def i = 0
totalRows.each { index, row ->
    formData.dataRows.add(index + i, row)
    i = i + 1
}

// графа 1, + поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.number = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
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
    newRow.incomeCode = 'Итого по коду'
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}