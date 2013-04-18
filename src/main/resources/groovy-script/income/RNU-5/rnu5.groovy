/**
 * Скрипт для РНУ-5 (rnu5.groovy).
 * Форма "(РНУ-5) Простой регистр налогового учёта «расходы»".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
    case FormDataEvent.CALCULATE :
        calc()
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - rowNumber
// графа 2  - code
// графа 3  - balance
// графа 4  - name
// графа 5  - sum

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())
    ['code', 'balance', 'name', 'sum'].each {
        newRow.getCell(it).editable = true
    }
    def pos = (currentDataRow != null ? currentDataRow.getOrder() : formData.dataRows.size)
    formData.dataRows.add(pos, newRow)
    setOrder()
}

/**
 * Удалить строку.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)
    setOrder()
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
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
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != it.code) {
            totalRows.put(i, getNewRow(tmp, sum))
            sum = 0
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == formData.dataRows.size() - 1) {
            sum += it.sum
            totalRows.put(i + 1, getNewRow(tmp, sum))
            sum = 0
        }

        sum += it.sum
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

    setOrder()
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    if (!formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // Проверка балансового счёта для кода классификации расхода (графа 2)
            if (false) {
                logger.warn('Балансовый счёт в справочнике отсутствует!')
                break
            }

            // Проверка кода классификации расхода для данного РНУ (графа 2)
            if (false) {
                logger.error('Операция в РНУ не учитывается!')
                break
            }

            // Проверка балансового счёта для кода классификации расхода (графа 3)
            if (false) {
                logger.warn('Балансовый счёт в справочнике отсутствует!')
                break
            }
        }
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
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