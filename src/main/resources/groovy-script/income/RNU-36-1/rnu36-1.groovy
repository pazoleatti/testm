/**
 * Скрипт для РНУ-36.1 (rnu36-1.groovy).
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 *		- откуда брать "Отчетная дата" для логических проверок?
 *		- откуда брать последний отчетный день месяца?
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

// графа 1  - series
// графа 2  - amount
// графа 3  - moninal
// графа 4  - shortPositionDate
// графа 5  - balance2
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 1..7
    ['series', 'amount', 'moninal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемые'
    }
    newRow.getCell('percIncome').styleAlias = 'Вычисляемые'

    if (currentDataRow == null) {
        row = formData.getDataRow('totalA')
        formData.dataRows.add(row.getOrder() - 1, newRow)
    } else if (currentDataRow.getAlias() == null) {
        formData.dataRows.add(currentDataRow.getOrder(), newRow)
    } else {
        def row
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
        formData.dataRows.add(row.getOrder() - 1, newRow)
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
    /*
      * Проверка объязательных полей.
      */
    def hasError = false
    formData.dataRows.each { row ->
        if (!isFixedRow(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 2..7)
            def requiredColumns = ['series', 'amount', 'moninal', 'shortPositionDate',
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
    formData.dataRows.each { row ->
        if (!isFixedRow(row)) {
            // последний отчетный день месяца
            def lastDay = new Date() // TODO (Ramil Timerbaev) откуда брать
            // графа 8
            def tmp = ((row.moninal - row.averageWeightedPrice) * (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount
            row.percIncome = round(tmp, 2)
        }
    }

    /** Столбцы для которых надо вычислять итого А и Б. Графа 2, 8. */
    def totalColumns = ['amount', 'percIncome']
    def totalARow = formData.getDataRow('totalA')
    def totalBRow = formData.getDataRow('totalB')
    def aRow = formData.getDataRow('A')
    def bRow = formData.getDataRow('B')
    totalColumns.each { alias ->
        def tmp = summ(formData, new ColumnRange(alias, aRow.getOrder() - 1, totalARow.getOrder() - 2))
        totalARow.getCell(alias).setValue(tmp)
        tmp = summ(formData, new ColumnRange(alias, bRow.getOrder() - 1, totalBRow.getOrder() - 2))
        totalBRow.getCell(alias).setValue(tmp)
    }

    // посчитать Итого
    formData.getDataRow('total').percIncome = totalARow.percIncome - totalBRow.percIncome

}

/**
 * Логические проверки.
 */
void logicalCheck() {
    for (def row : formData.dataRows) {
        if (isFixedRow(row)) {
            continue
        }

        // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
        def reportDay = new Date()
        if (row.shortPositionDate > reportDay) {
            logger.error('Неверно указана дата приобретения (открытия короткой позиции)!')
            break
        }
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
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}