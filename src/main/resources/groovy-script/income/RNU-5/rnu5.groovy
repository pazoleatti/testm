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
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
}

// графа 1 - rowNumber
// графа 2 - code
// графа 3 - balance
// графа 4 - name
// графа 5 - sum

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())
    ['code', 'balance', 'name', 'sum'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def index = formData.dataRows.indexOf(currentDataRow)

    // если данных еще нет или строка не выбрана
    if (formData.dataRows.isEmpty() || index == -1) {
        formData.dataRows.add(newRow)
    } else {
        formData.dataRows.add(index + 1, newRow)
    }
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
            // Список проверяемых столбцов (графа 2..5)
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
                    index = formData.dataRows.indexOf(row) + 1
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
    formData.dataRows.eachWithIndex { row, i ->
        row.rowNumber = i + 1
        total += row.sum
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sum = 0
    formData.dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            totalRows.put(i, getNewRow(tmp, sum))
            sum = 0
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == formData.dataRows.size() - 1) {
            sum += row.sum
            totalRows.put(i + 1, getNewRow(row.code, sum))
            sum = 0
        }

        sum += row.sum
        tmp = row.code
    }
    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }

    // добавить строку "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.code = 'Итого'
    totalRow.sum = total
    setTotalStyle(totalRow)
}

/**
 * Логические проверки.
 *
 * @param checkRequiredColumns проверять ли обязательные графы
 */
void logicalCheck(def checkRequiredColumns) {
    if (!formData.dataRows.isEmpty()) {
        def i = 1
        def totalSum = 0
        def hasTotal = false
        def sums = [:]
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }
            // 1. Обязательность заполнения полей (графа 1..5)
            def colNames = []
            // Список проверяемых столбцов (графа 1..5)
            ['rowNumber', 'code', 'balance', 'name', 'sum'].each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                if (!checkRequiredColumns) {
                    return
                }
                hasError = true
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = formData.dataRows.indexOf(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
                return
            }

            // 2. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return
            }
            i += 1

            // 3. Проверка итогового значения по коду для графы 5
            sums[row.code] = (sums[row.code] != null ? sums[row.code] : 0) + row.sum

            totalSum += row.sum
        }

        if (hasTotal) {
            // 3. Проверка итогового значения по коду для графы 5
            def hindError = false
            sums.each { code, sum ->
                def row = formData.getDataRow('total' + code)
                if (row.sum != sum && !hindError) {
                    hindError = true
                    logger.error("Неверное итоговое значение по коду $code графы «Сумма расходов за отчётный период (руб.)»!")
                }
            }
            if (hindError) {
                return
            }

            // 4. Проверка итогового значения по всем строкам для графы 5
            def totalRow = formData.getDataRow('total')
            if (totalRow.sum != totalSum) {
                logger.error('Неверное итоговое значение графы «Сумма расходов за отчётный период (руб.)»!')
            }
        }
    }
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
 * Получить новую строку.
 */
def getNewRow(def alias, def sum) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.sum = sum
    newRow.code = 'Итого по коду'
    newRow.balance = alias
    setTotalStyle(newRow)
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'code', 'balance', 'name', 'sum'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}