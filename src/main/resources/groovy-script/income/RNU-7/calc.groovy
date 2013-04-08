/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учёт которых требует применения метода начисления".
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
        def columns = ['balance', 'date', 'outcomeCode', 'docNumber', 'docDate', 'currencyCode',
                'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency']

        // если "графа 7" == 'RUR', то "графа 8" = 1 и ее проверять не надо
        def needColumn8 = (row.currencyCode != null && row.currencyCode == 'RUR')
        if (!needColumn8) {
            columns -= 'rateOfTheBankOfRussia'
        }

        ['balance', 'date', 'outcomeCode', 'docNumber', 'docDate', 'currencyCode',
                'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency'].each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            hasError = true
            def index = row.number
            def errorMsg = colNames.join(', ')
            logger.error("В строке \"№ пп\" (равной $index) не заполнены колонки : $errorMsg.")
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
formData.dataRows.each {
    if (isTotal(it)) {
        delRow += it
    }
}
delRow.each {
    formData.dataRows.remove(formData.dataRows.indexOf(it))
}

// отсортировать/группировать
formData.dataRows.sort { it.outcomeCode }

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

// графа 10, 12 для последней строки "итого"
def total10 = 0
def total12 = 0
formData.dataRows.each { row ->
    total10 += row.taxAccountingRuble
    total12 += row.ruble
}

// посчитать "итого по коду"
def totalRows = [:]
def tmp = null
def sum10 = 0
def sum12 = 0
formData.dataRows.eachWithIndex { row, i ->
    if (tmp == null) {
        tmp = row.outcomeCode
    }
    // если код расходы поменялся то создать новую строку "итого по коду"
    if (tmp != row.outcomeCode) {
        totalRows.put(i, getNewRow(tmp, sum10, sum12))
        sum10 = 0
        sum12 = 0
    }
    // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
    if (i == formData.dataRows.size() - 1) {
        sum10 += row.taxAccountingRuble
        sum12 += row.ruble
        totalRows.put(i + 1, getNewRow(tmp, sum10, sum12))
        sum10 = 0
        sum12 = 0
    }
    sum10 += row.taxAccountingRuble
    sum12 += row.ruble
    tmp = row.outcomeCode

}
// добавить "итого по коду" в таблицу
def i = 0
totalRows.each { index, row ->
    formData.dataRows.add(index + i, row)
    i = i + 1
}

// добавить строки "итого"
def totalRow = formData.appendDataRow()
totalRow.setAlias('total')
totalRow.outcomeCode = 'Итого'
totalRow.taxAccountingRuble = total10
totalRow.ruble = total12

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
 * Получить новую строку.
 */
def getNewRow(def alias, def sum10, def sum12) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.taxAccountingRuble = sum10
    newRow.ruble = sum12
    newRow.outcomeCode = 'Итого по коду'
    newRow.docNumber = alias
    return newRow
}