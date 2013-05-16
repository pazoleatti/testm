/**
 * Скрипт для РНУ-36.1 (rnu36-1.groovy).
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- откуда брать "Отчетная дата" для логических проверок?
 *		- откуда брать последний отчетный день месяца?
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

// графа 1  - series
// графа 2  - amount
// графа 3  - nominal
// графа 4  - shortPositionDate
// графа 5  - balance2
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        row = formData.getDataRow('totalA')
        formData.dataRows.add(getIndex(row), newRow)
    } else if (currentDataRow.getAlias() == null) {
        formData.dataRows.add(getIndex(currentDataRow), newRow)
    } else {
        def row = formData.getDataRow('totalA')
        switch (currentDataRow.getAlias()) {
            case 'A' :
            case 'totalA' :
                row = formData.getDataRow('totalA')
                break
            case 'B' :
            case 'totalB' :
            case 'total' :
                row = formData.getDataRow('totalB')
                break
        }
        formData.dataRows.add(getIndex(row), newRow)
    }

    // графа 1..7
    ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    newRow.getCell('percIncome').styleAlias = 'Вычисляемая'
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isFixedRow(currentDataRow)) {
        formData.dataRows.remove(currentDataRow)
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
        if (!isFixedRow(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 2..7)
            def requiredColumns = ['series', 'amount', 'nominal', 'shortPositionDate',
                    'balance2', 'averageWeightedPrice', 'termBondsIssued']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.series
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"Серия\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = getIndex(row) + 1
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

    // последний отчетный день месяца
    // TODO (Ramil Timerbaev) откуда брать
    def lastDay = new Date()
    formData.dataRows.each { row ->
        if (!isFixedRow(row)) {
            // графа 8
            row.percIncome = getColumn8(row, lastDay)
        }
    }

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def totalColumns = ['amount', 'percIncome']
    def tmp
    def sum = 0
    ['A', 'B'].each {
        def row = formData.getDataRow(it)
        def totalRow = formData.getDataRow('total' + it)
        totalColumns.each { alias ->
            tmp = summ(formData, new ColumnRange(alias, getIndex(row) + 1, getIndex(totalRow) - 1))
            totalRow.getCell(alias).setValue(tmp)
        }
        sum += totalRow.percIncome
    }

    // посчитать Итого
    formData.getDataRow('total').percIncome = sum
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def totalColumns = ['amount', 'percIncome']

    // последний день отчетного месяца
    // TODO (Ramil Timerbaev) откуда брать?
    def lastDay = new Date()
    def tmp
    for (def row : formData.dataRows) {
        if (isFixedRow(row)) {
            continue
        }

        // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
        // TODO (Ramil Timerbaev) откуда брать
        // отчетная дата
        tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
        def reportDay = (tmp ? tmp.getTime() : null) // TODO (Ramil Timerbaev) Уточнить
        if (row.shortPositionDate > reportDay) {
            logger.error('Неверно указана дата приобретения (открытия короткой позиции)!')
            return
        }

        // 2. Арифметическая проверка графы 8
        if (row.termBondsIssued != null || row.termBondsIssued != 0) {
            if (row.percIncome > getColumn8(row, lastDay)) {
                logger.error('Неверно рассчитана графа «Процентный доход с даты приобретения»!')
                return
            }
        }
    }

    def hasError = false

    // 3, 4. Проверка итоговых значений по разделу А и B
    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def sum = 0
    ['A', 'B'].each { section ->
        def row = formData.getDataRow(section)
        def totalRow = formData.getDataRow('total' + section)
        totalColumns.each { alias ->
            tmp = summ(formData, new ColumnRange(alias, getIndex(row) + 1, getIndex(totalRow) - 1))
            if (totalRow.getCell(alias).getValue() != tmp) {
                hasError = true
                logger.error("Итоговые значений для раздела $section рассчитаны неверно!")
            }
        }
        sum += totalRow.percIncome
    }
    if (hasError) {
        return
    }

    // 5. Проверка итоговых значений по всей форме
    def totalRow = formData.getDataRow('total')
    if (totalRow.percIncome != sum) {
        logger.error('Итоговые значений рассчитаны неверно!')
        return
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // графа 5
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует! ')
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
 *	Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def getColumn8(def row, def lastDay) {
    return round(((row.nominal - row.averageWeightedPrice) * (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount, 2)
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}