/**
 * Скрипт для РНУ-50 (rnu50.groovy).
 * Форма "(РНУ-50) Регистр налогового учёта «ведомость понесённых убытков от реализации амортизируемого имущества»".
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck(true)
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - rowNumber
// графа 2  - rnu49rowNumber
// графа 3  - invNumber
// графа 4  - lossReportPeriod
// графа 5  - lossTaxPeriod

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..5
    ['rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod'].each {
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
    setOrder()
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isTotal(currentDataRow)) {
        formData.dataRows.remove(currentDataRow)
        setOrder()
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
            def requiredColumns = ['rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

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
                    logger.error("В $index строке не заполнены колонки : $errorMsg.")
                }
            }
        }
    }
    if (hasError) {
        return
    }

    /*
      * Расчеты
      */

    // удалить строку "итого"
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(formData.dataRows.indexOf(row))
    }

    formData.dataRows.eachWithIndex { row, i ->
        // графа 1
        row.rowNumber = i + 1
    }

    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.rnu49rowNumber = 'Итого'
    totalRow.lossReportPeriod = getSum('lossReportPeriod')
    totalRow.lossTaxPeriod = getSum('lossTaxPeriod')
    ['rowNumber', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

    setOrder()
}

/**
 * Логические проверки.
 */
void logicalCheck(def checkRequiredColumns) {
    if (!formData.dataRows.isEmpty()) {
        def hasTotalRow = false
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }

            // 1. Обязательность заполнения полей (графа 1..5)
            def colNames = []

            // Список проверяемых столбцов (графа 1..5)
            def requiredColumns = ['rowNumber', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            // вывод сообщения
            if (!colNames.isEmpty()) {
                if (!checkRequiredColumns) {
                    return
                }
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = row.getOrder()
                    logger.error("В $index строке не заполнены колонки : $errorMsg.")
                }
                return
            }

            // 2. Проверка на нулевые значения
            if (isEmpty(row.lossReportPeriod) && isEmpty(row.lossTaxPeriod)) {
                logger.error('Все суммы по операции нулевые!')
                return
            }

            // 3. Проверка формата номера записи в РНУ-49 (графа 2)
            if (!row.rnu49rowNumber.matches('\\w{2}-\\w{6}')) {
                logger.error('Неправильно указан номер записи в РНУ-49 (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!')
                return
            }
        }


        // 4. Проверка итоговых значений формы	Заполняется автоматически.
        if (hasTotalRow) {
            def totalRow = formData.getDataRow('total')
            def totalSumColumns = ['lossReportPeriod', 'lossTaxPeriod']
            for (def alias : totalSumColumns) {
                if (totalRow.getCell(alias).getValue() != getSum(alias)) {
                    logger.error('Итоговые значения формы рассчитаны неверно!')
                    return
                }
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
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def from = 0
    def to = formData.dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}