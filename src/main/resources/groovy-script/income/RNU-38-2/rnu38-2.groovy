/**
 * Скрипт для РНУ-38.2 (rnu38-2.groovy).
 * Форма "РНУ-38.2) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 *
 * @version 59
 *
 * TODO:
 *      - сколько строк в рну?
 *      - консолидация
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - amount
// графа 2  - incomePrev
// графа 3  - incomeShortPosition
// графа 4  - totalPercIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    if (formData.dataRows.size == 0) {
        formData.dataRows.add(formData.createDataRow())
    }
}

/**
 * Удалить строку.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def totalRow = getTotalRowFromRNU38_1()
    if (totalRow == null) {
        // уточнить нужно ли убрать данные если нет данных в рну-38.1
        formData.dataRows.each { row ->
            // графа 1
            row.amount = null

            // графа 2
            row.incomePrev = null

            // графа 3
            row.incomeShortPosition = null

            // графа 4
            row.totalPercIncome = null
        }
        return
    }

    /*
     * Расчеты.
     */

    formData.dataRows.each { row ->
        // графа 1
        row.amount = totalRow.amount

        // графа 2
        row.incomePrev = totalRow.incomePrev

        // графа 3
        row.incomeShortPosition = totalRow.incomeShortPosition

        // графа 4
        row.totalPercIncome = row.incomePrev + row.incomeShortPosition
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    def totalRow = getTotalRowFromRNU38_1()
    if (totalRow == null) {
        logger.warn('Отсутствует РНУ-38.1.')
        return
    }
    if (formData.dataRows.isEmpty() || totalRow == null) {
        return
    }
    def row = formData.dataRows.get(0)

    // 1. Обязательность заполнения поля графы 1..4
    def colNames = []
    // Список проверяемых столбцов (графа 1..4)
    ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome'].each {
        if (row.getCell(it).getValue() == null) {
            colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        def index = formData.dataRows.indexOf(row) + 1
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return
    }

    // 2. Арифметическая проверка графы 1
    if (row.amount != totalRow.amount) {
        logger.error('Неверно рассчитана графа «Количество (шт.)»!')
        return
    }

    // 3. Арифметическая проверка графы 2
    if (row.incomePrev != totalRow.incomePrev) {
        logger.error('Неверно рассчитана графа «Доход с даты погашения предыдущего купона (руб.коп.)»!')
        return
    }

    // 4. Арифметическая проверка графы 3
    if (row.incomeShortPosition != totalRow.incomeShortPosition) {
        logger.error('Неверно рассчитана графа «Доход с даты открытия короткой позиции, (руб.коп.)»!')
        return
    }

    // 5. Арифметическая проверка графы 4
    if (row.totalPercIncome != row.incomePrev + row.incomeShortPosition) {
        logger.error('Неверно рассчитана графа «Всего процентный доход (руб.коп.)»!')
        return
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Получить итоговую строку из нф (РНУ-38.1) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1.
 */
def getTotalRowFromRNU38_1() {
    def formDataRNU_38_1 = FormDataService.find(334, FormDataKind.PRIMARY, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU_38_1 != null) {
        for (def row : formDataRNU_38_1.dataRows) {
            if (row.getAlias() == 'total') {
                return row
            }
        }
    }
    return null
}