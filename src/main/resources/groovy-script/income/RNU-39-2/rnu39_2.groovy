/**
 * Скрипт для РНУ-39.2 (rnu39_2.groovy).
 * Форма "(РНУ-39.2) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 2(квартальный)".
 *
 * TODO:
 *      - логические проверки (необходимо получать граничные даты отчетного периода)
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        // logicalCheck()
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        // logicalCheck()
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - currencyCode
// графа 2  - issuer
// графа 3  - regNumber
// графа 4  - amount
// графа 5  - cost
// графа 6  - shortPositionOpen
// графа 7  - shortPositionClose
// графа 8  - pkdSumOpen
// графа 9  - pkdSumClose
// графа 10 - maturityDatePrev
// графа 11 - maturityDateCurrent
// графа 12 - currentCouponRate
// графа 13 - incomeCurrentCoupon
// графа 14 - couponIncome
// графа 15 - totalPercIncome
// графа 16 - positionType
// графа 17 - securitiesGroup

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())
    // графа 1..17
    ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost', 'shortPositionOpen',
            'shortPositionClose', 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev',
            'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon',
            'couponIncome', 'totalPercIncome', 'positionType', 'securitiesGroup'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    if (currentDataRow == null) {
        row = formData.getDataRow('totalA1')
        formData.dataRows.add(row.getOrder() - 1, newRow)
    } else if (currentDataRow.getAlias() == null) {
        formData.dataRows.add(currentDataRow.getOrder(), newRow)
    } else {
        def alias = currentDataRow.getAlias()
        def row = formData.getDataRow('totalA1')
        if (alias == 'A') {
            row = formData.getDataRow('totalA1')
        } else if (alias == 'B') {
            row = formData.getDataRow('totalB1')
        } else if (alias.contains('total')) {
            row = formData.getDataRow(alias)
        } else {
            row = formData.getDataRow('total' + alias)
        }
        formData.dataRows.add(row.getOrder() - 1, newRow)
    }

    setOrder()
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow.getAlias() != null) {
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
            // Список проверяемых столбцов (графа 1..6, 10..13, 16, 17)
            def requiredColumns = ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost',
                    'shortPositionOpen', 'maturityDatePrev', 'maturityDateCurrent',
                    'currentCouponRate', 'incomeCurrentCoupon',	'positionType', 'securitiesGroup']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.getOrder()
                def errorMsg = colNames.join(', ')
                logger.error("В строке $index не заполнены колонки : $errorMsg.")
            }
        }
    }
    if (hasError) {
        return
    }

    /*
     * Расчеты.
     */
    // подразделы
    ['A1', 'A2', 'A3', 'A4', 'A5', 'B1', 'B2', 'B3', 'B4', 'B5'].each { section ->
        firstRow = formData.getDataRow(section)
        lastRow = formData.getDataRow('total' + section)
        // графы для которых считать итого (графа 4, 5, 8, 9, 14, 15)
        ['amount', 'cost', 'pkdSumOpen', 'pkdSumClose', 'couponIncome', 'totalPercIncome'].each {
            lastRow.getCell(it).setValue(getSum(it, firstRow, lastRow))
        }
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    def reportDateStart = new Date() // TODO (Ramil Timerbaev)
    def reportDateEnd = new Date() // TODO (Ramil Timerbaev)

    for (def row : formData.dataRows) {
        if (isFixedRow(row)) {
            continue
        }

        // 1. Проверка даты первой части сделки
        if (row.shortPositionOpen > reportDateEnd) {
            logger.error('Неверно указана дата первой части сделки!')
            break
        }
    }

    // 2. Проверка даты второй части сделки
    def hasError = false
    for (def row : formData.dataRows) {
        if (!isFixedRow(row)) {
            if (isSectionA(row) && row.shortPositionClose != null) {
                hasError = true
            }
            // TODO (Ramil Timerbaev) уточнить последнее условие
            if (!isSectionA(row) &&
                    (row.shortPositionClose > reportDateEnd || row.shortPositionClose < reportDateStart)) {
                hasError = true
            }
            if (hasError) {
                logger.error('Неверно указана дата второй части сделки!')
                break
            }
        }
    }

    // 3. Обязательность заполнения поля графы 1-6, 10-13 , 16, 17
    // Реализовано перед расчетами (в методе calc())
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // 1. Проверка кода валюты со справочным (графа 1)
    if (false) {
        logger.warn('Неверный код валюты!')
    }

    // 2. Проверка на наличие данных в справочнике купонов ценных бумаг
    if (false) {
        // TODO (Ramil Timerbaev)
        logger.warn('Для ценной бумаги <Номер государственной регистрации  из справочника ценных бумаг> отсутствует купон с датой погашения в отчётном периоде либо позднее!')
    }

    // 3. Проверка актуальности поля «Тип позиции»
    if (false) {
        logger.warn('Тип позиции в справочнике отсутствует!')
    }

    // 4. Проверка актуальности поля «Группа ценных бумаг»
    if (false) {
        logger.warn('Группа ценных бумаг с справочнике отсутствует!')
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
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Проверка принадлежит ли строка разделу A.
 */
def isSectionA(def row) {
    return row != null && row.getOrder() < formData.getDataRow('B').getOrder()
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getOrder()
    def to = rowEnd.getOrder() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}


/**
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}