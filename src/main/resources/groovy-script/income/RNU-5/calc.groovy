/**
 * Расчеты. Алгоритмы заполнения полей формы (calc.groovy).
 * Форма "(РНУ-5) Простой регистр налогового учёта «расходы»".
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
        ['code', 'balance', 'name', 'sum'].each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            hasError = true
            def index = row.rowNumber
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

/** Сумма "Итого". */
def total = 0

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
formData.dataRows.sort { it.code }

// нумерация (графа 1) и посчитать "итого"
formData.dataRows.eachWithIndex { it, i ->
    it.rowNumber = i + 1
    total += it.sum
}

// посчитать "итого по коду"
def totalRows = [:]
def tmp = null
def sum = 0
formData.dataRows.eachWithIndex { it, i ->
    if (tmp == null) {
        tmp = it.code
    }
    if (tmp == it.code) {
        // если код расхода не изменился то считать сумму итого по коду
        sum += it.sum
    } else {
        // если код расходы поменялся то создать новую строку "итого по коду"
        totalRows.put(i, getNewRow(tmp, sum))
        sum = 0
    }
    // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
    if (i == formData.dataRows.size() - 1) {
        sum += it.sum
        totalRows.put(i + 1, getNewRow(tmp, sum))
        sum = 0
    }
    tmp = it.code
}
// добавить "итого по коду" в таблицу
def i = 0
totalRows.each { index, row ->
    formData.dataRows.add(index + i, row)
    i = i + 1
}

// добавить строки "итого"
def row = formData.appendDataRow()
row.setAlias('total')
row.code = 'Итого'
row.sum = total

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
def getNewRow(def alias, def sum) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.sum = sum
    newRow.code = 'Итого по коду'
    newRow.balance = alias
    return newRow
}