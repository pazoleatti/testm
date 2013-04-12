/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * TODO:
 * 		- про нумерацию пока не уточнили, пропустить
 *		- неясность с алгоритмом заполнения строки «Итого за текущий месяц» (после каких строк считать или по каким значениям группировать строки). Временно сгруппировал по графе 4 "Выпуск"
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
        // Список проверяемых столбцов (графа 2..14, 16, 18..20)
        def requiredColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate',
                'implementationDate', 'bondsCount','purchaseCost', 'costs',
                'redemptionVal', 'exercisePrice', 'exerciseRuble',
                'marketPricePercent', 'marketPriceRuble', 'costsRetirement',
                'parPaper', 'averageWeightedPricePaper', 'issueDays']

        requiredColumns.each {
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

// удалить строку "итого" и "Итого за текущий месяц"
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
formData.dataRows.sort { it.issue } // TODO (Ramil Timerbaev) уточнить по какому полю группировать

formData.dataRows.each { row ->

    // графа 15
    if (row.code == 1 ||
            ((row.code == 2 || row.code == 5) && row.exercisePrice > row.marketPricePercent && row.exerciseRuble > row.marketPriceRuble) ||
            ((row.code == 2 || row.code == 5) && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
        row.exercisePriceRetirement = row.exerciseRuble
    } else if (row.code == 4) {
        row.exercisePriceRetirement = row.redemptionVal
    } else if ((row.code == 2 || row.code == 5) && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
        row.exercisePriceRetirement = row.marketPriceRuble
    } else {
        row.exercisePriceRetirement = 0
    }

    // графа 17
    row.allCost = row.purchaseCost + row.costs + row.costsRetirement

    // графа 21
    row.tenureSkvitovannymiBonds = row.implementationDate - row.purchaseDate

    // графа 22
    def column22 = (row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays
    row.interestEarned = round(column22, 0)

    // графа 23
    row.profitLoss = row.exercisePriceRetirement - row.allCost - Math.abs(row.interestEarned)

    // графа 24
    row.excessOfTheSellingPrice = (row.code != 4 ? row.exercisePriceRetirement - row.exerciseRuble : 0)
}

/** Столбцы для которых надо вычислять итого и итого по эмитенту. Графа 7..10, 12, 14..17, 22..24. */
def totalColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal',
        'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement',
        'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
// добавить строку "Итого за текущий отчётный (налоговый) период"
def totalRow = formData.appendDataRow()
totalRow.setAlias('total')
totalRow.getCell('valuablePaper').setColSpan(4)
totalRow.getCell('valuablePaper').setValue('Итого за текущий отчётный (налоговый) период')
totalColumns.each { alias ->
    totalRow.getCell(alias).setValue(getSum(alias))
}

// посчитать "Итого за текущий месяц"
def totalRows = [:]
def sums = [:]
def tmp = null
totalColumns.each {
    sums[it] = 0
}
formData.dataRows.eachWithIndex { row, i ->
    if (!isTotal(row)) {
        if (tmp == null) {
            tmp = row.issue // TODO (Ramil Timerbaev) уточнить по какому полю группировать
        }
        // если код расходы поменялся то создать новую строку "Итого за текущий месяц"
        if (tmp != row.issue) { // TODO (Ramil Timerbaev) уточнить по какому полю группировать
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "Итого за текущий месяц"
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
        tmp = row.issue // TODO (Ramil Timerbaev) уточнить по какому полю группировать
    }
}
// добавить "Итого за текущий месяц" в таблицу
def i = 0
totalRows.each { index, row ->
    formData.dataRows.add(index + i, row)
    i = i + 1
}

// графа 1, + поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    if (!isTotal(row)) {
        row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
    }
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
    newRow.valuablePaper = 'Итого за текущий месяц'
    newRow.getCell('valuablePaper').setColSpan(4)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}