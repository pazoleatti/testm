/**
 * Скрипт для РНУ-40.2 (rnu40_2.groovy).
 * Форма "(РНУ-40.2) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 2".
 *
 * TODO:
 *      - логические проверки (необходимо получать граничные даты отчетного периода)
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck()
        checkNSI()
        break
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
// графа 2  - name
// графа 3  - code
// графа 4  - cost
// графа 5  - bondsCount
// графа 6  - percent

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 1..6
    ['number', 'name', 'code', 'cost', 'bondsCount', 'percent'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    def index = formData.dataRows.indexOf(currentDataRow)
    if (index == -1) {
        index = 0
    }
    if (index + 1 == formData.dataRows.size()) {
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
    if (isFixedRow(currentDataRow)) {
        return
    }

    formData.dataRows.remove(currentDataRow)
    setOrder()
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {

}

/**
 * Логические проверки.
 */
void logicalCheck() {
    // 1. Обязательность заполнения поля графы 1-6
    def hasError = false
    formData.dataRows.each { row ->
        if (!isFixedRow(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 1..6)
            def requiredColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']

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
                    logger.error("В строке \"Номер территориального банка\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = row.getOrder()
                    logger.error("В $index строке не заполнены колонки : $errorMsg.")
                }
            }
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // 1. Проверка актуальности поля «Номер территориального банка»	(графа 1)
    if (false) {
        logger.warn('Неверный номер территориального банка!')
    }

    // 2. Проверка актуальности поля «Наименование территориального банка / подразделения Центрального аппарата» (графа 2)
    if (false) {
        logger.warn('Неверное наименование территориального банка/ подразделения Центрального аппарата!')
    }

    // 3. Проверка актуальности поля «Код валюты номинала» (графа 3)
    if (false) {
        logger.error('Неверный код валюты!')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}