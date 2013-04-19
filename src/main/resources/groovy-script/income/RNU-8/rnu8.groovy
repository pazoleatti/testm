/**
 * Скрипт для РНУ-8 (rnu8.groovy).
 * Форма "(РНУ-8) Простой регистр налогового учёта «Требования»".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 *		- получение даты начала и окончания отчетного периода
 * 		- про нумерацию пока не уточнили, пропустить
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - number
// графа 2  - code
// графа 3  - balance
// графа 4  - name
// графа 5  - income
// графа 6  - outcome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())
    // графа 2..6, 11
    ['code', 'balance', 'name', 'income', 'outcome'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    def pos = (currentDataRow != null && !formData.dataRows.isEmpty() ? currentDataRow.getOrder() : formData.dataRows.size)
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
    if (formData.dataRows.isEmpty()) {
        return
    }
    /*
	 * Проверка объязательных полей.
	 */
    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 2..6)
            def requiredColumns = ['code', 'balance', 'name', 'income', 'outcome']

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
    formData.dataRows.sort { it.code }

    /** Столбцы для которых надо вычислять итого и итого по коду. Графа 5, 5. */
    def totalColumns = ['income', 'outcome']

    // добавить строку "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.code = 'Итого'
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
                tmp = row.code
            }
            // если код расходы поменялся то создать новую строку "итого по коду"
            if (tmp != row.code) {
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
            tmp = row.code
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
}

/**
 * Логические проверки.
 */
void logicalCheck() {

    // 1. Обязательность заполнения поля (графа 1..6)
    // реализована перед расчетами

    // 2. Проверка на уникальность поля «№ пп»	Уникальность поля графы 1 (в рамках текущего года)
    if (!formData.dataRows.isEmpty()) {
        def i = 1
        for (def row : formData.dataRows) {
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                break
            }
            i += 1
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // 1. Проверка балансового счёта для кода классификации дохода - Проверка актуальности «графы 2»
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует!')
    }

    // 2. Проверка кода классификации дохода для данного РНУ - Проверка актуальности «графы 2» на дату с начала до конца отчётного периода
    if (false) {
        logger.error('Операция в РНУ не учитывается!')
    }

    // 3. Проверка балансового счёта для кода классификации дохода (графы 3)
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует!')
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
    newRow.code = 'Итого по коду'
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}