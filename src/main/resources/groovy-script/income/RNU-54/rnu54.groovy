/**
 * Скрипт для РНУ-54 (rnu54.groovy).
 * Форма "(РНУ-54) Регистр налогового учёта открытых сделок РЕПО с обязательством покупки по 2-й части".
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- как получить отчетную дату?
 *		- при вычислении 12 графы: 365 или 366 дней в году? как определять?
 *		- откуда брать курс ЦБ РФ на отчётную дату для подсчета графы 12 и для 5ой и 6ой логической проверки
 *
 * @author rtimerbaev
 */

import java.text.SimpleDateFormat

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

// графа 1  - tadeNumber
// графа 2  - securityName
// графа 3  - currencyCode
// графа 4  - nominalPriceSecurities
// графа 5  - salePrice
// графа 6  - acquisitionPrice
// графа 7  - part1REPODate
// графа 8  - part2REPODate
// графа 9  - income
// графа 10 - outcome
// графа 11 - rateBR
// графа 12 - outcome269st
// графа 13 - outcomeTax

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.appendDataRow(currentDataRow, null)

    // графа 1..10
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'salePrice', 'acquisitionPrice', 'part1REPODate', 'part2REPODate',
            'income', 'outcome'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
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
            // Список проверяемых столбцов (графа 1..10)
            def requiredColumns = ['tadeNumber', 'securityName', 'currencyCode',
                    'nominalPriceSecurities', 'salePrice', 'acquisitionPrice',
                    'part1REPODate', 'part2REPODate', 'income', 'outcome']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    // TODO (Ramil Timerbaev) из за % в названии заголовка может выдавать ошибки
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.tadeNumber
                def errorMsg = colNames.join(', ')
                if (!isEmpty(index)) {
                    logger.error("В строке \"Номер сделки\" равной $index не заполнены колонки : $errorMsg.")
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
        formData.dataRows.remove(getIndex(row))
    }

    /** Отчетная дата. */
    def reportDate = new Date() // TODO (Ramil Timerbaev) как получить отчетную дату

    /** Дата нужная при подсчете графы 12. */
    def someDate = getDate('01.11.2009')

    /** Количество дней в году. */
    def daysInYear = 365 // TODO (Ramil Timerbaev) 365 или 366

    /** Курс ЦБ РФ на отчётную дату. */
    def course = 1 // TODO (Ramil Timerbaev) откуда брать курс ЦБ РФ на отчётную дату

    formData.dataRows.eachWithIndex { row, i ->

        // графа 11
        if (row.outcome == 0 || isEmpty(row.currencyCode)) {
            row.rateBR = null
        } else if (row.currencyCode == '810') {
            // TODO (Ramil Timerbaev) «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на «отчетную дату»
            row.rateBR = 0
        } else {
            if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                row.rateBR = 22
            } else if (inPeriod(reportDate, '01.01.2011', '31.12.2012')) {
                // TODO (Ramil Timerbaev) ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на «отчетную дату»
                row.rateBR = 0
            } else {
                row.rateBR = 15
            }
        }

        // графа 12
        if (row.outcome == 0) {
            row.outcome269st = 0
        } else if (row.outcome > 0 && row.currencyCode == '810') {
            if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                row.outcome269st = calc12Value(row, 1.5, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
                row.outcome269st = calc12Value(row, 2, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
                row.outcome269st = calc12Value(row, 1.8, reportDate, daysInYear)
            } else {
                row.outcome269st = calc12Value(row, 1.1, reportDate, daysInYear)
            }
        } else if (row.outcome > 0 && row.currencyCode != '810') {
            if (inPeriod(reportDate, '01.01.20011', '31.12.2012')) {
                row.outcome269st = calc12Value(row, 0.8, reportDate, daysInYear) * course
            } else {
                row.outcome269st = calc12Value(row, 1, reportDate, daysInYear) * course
            }
        }

        // графа 13
        if (row.outcome == 0) {
            row.outcomeTax = 0
        } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
            row.outcomeTax = row.outcome
        } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
            row.outcomeTax = row.outcome269st
        }
    }

    // строка итого
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.tadeNumber = 'Итого'
    totalRow.getCell('tadeNumber').colSpan = 2
    setTotalStyle(totalRow)
    ['nominalPriceSecurities', 'salePrice', 'acquisitionPrice', 'income',
            'outcome', 'outcome269st', 'outcomeTax'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
}

/**
 * Логические проверки.
 */
void logicalCheck(def checkRequiredColumns) {
    if (!formData.dataRows.isEmpty()) {
        /** Отчетная дата. */
        def reportDate = new Date() // TODO (Ramil Timerbaev) как получить отчетную дату

        /** Дата нужная при подсчете графы 12. */
        def someDate = getDate('01.11.2009')

        /** Количество дней в году. */
        def daysInYear = 365 // TODO (Ramil Timerbaev) 365 или 366

        /** Курс ЦБ РФ на отчётную дату. */
        def course = 1 // TODO (Ramil Timerbaev) откуда брать курс ЦБ РФ на отчётную дату

        def hasTotalRow = false
        def hasError

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }

            // 1. Обязательность заполнения поля графы 12 и 13
            def colNames = []
            // Список проверяемых столбцов (графа 12, 13)
            def requiredColumns = ['outcome269st', 'outcomeTax']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    // TODO (Ramil Timerbaev) из за % в названии заголовка может выдавать ошибки
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            // вывод сообщения
            if (!colNames.isEmpty()) {
                if (!checkRequiredColumns) {
                    return
                }
                def index = row.tadeNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"Номер сделки\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = getIndex(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
                return
            }

            // 2. Проверка даты первой части РЕПО (графа 7)
            if (row.part1REPODate > reportDate) {
                logger.error('Неверно указана дата первой части сделки!')
                return
            }
            // 3. Проверка даты второй части РЕПО (графа 8)
            if (row.part2REPODate <= reportDate) {
                logger.error('Неверно указана дата второй части сделки!')
                return
            }

            // 4. Проверка финансового результата (графа 9, 10, 12, 13)
            if ((row.income > 0 && row.outcome != 0) ||
                    (row.outcome > 0 && row.income != 0) ||
                    (row.outcome == 0 && (row.outcome269st != 0 || row.outcomeTax != 0))) {
                logger.error('Задвоение финансового результата!')
                return
            }

            // 5. Проверка финансового результата
            def tmp = ((row.acquisitionPrice - row.salePrice) * (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
            if (tmp < 0 && row.income != round(Math.abs(tmp), 2)) {
                logger.warn('Неверно определены доходы')
            }

            // 6. Проверка финансового результата
            if (tmp > 0 && row.outcome != round(Math.abs(tmp), 2)) {
                logger.warn('Неверно определены расходы')
            }

            // 7. Арифметическая проверка графы 12
            hasError = false
            if (row.outcome > 0 && row.currencyCode == '810') {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009') &&
                        row.outcome269st != calc12Value(row, 1.5, reportDate, daysInYear)) {
                    hasError = true
                } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') &&
                        row.part1REPODate < someDate &&
                        row.outcome269st != calc12Value(row, 2, reportDate, daysInYear)) {
                    hasError = true
                } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012') &&
                        row.outcome269st != calc12Value(row, 1.8, reportDate, daysInYear)) {
                    hasError = true
                } else if (row.outcome269st != calc12Value(row, 1.1, reportDate, daysInYear)) {
                    hasError = true
                }
            } else if (row.outcome > 0 && row.currencyCode != '810') {
                if (inPeriod(reportDate, '01.01.20011', '31.12.2012') &&
                        row.outcome269st != calc12Value(row, 0.8, reportDate, daysInYear) * course) {
                    hasError = true
                } else if (row.outcome269st != calc12Value(row, 1, reportDate, daysInYear) * course) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)»!')
                return
            }

            // 8. Арифметическая проверка графы 13
            hasError = false
            if (row.outcome == 0 && row.outcomeTax != 0) {
                hasError = true
            } else if (row.outcome > 0 && row.outcome <= row.outcome269st &&
                    row.outcomeTax != row.outcome) {
                hasError = true
            } else if (row.outcome > 0 && row.outcome > row.outcome269st &&
                    row.outcomeTax != row.outcome269st) {
                hasError = true
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)»!')
                return
            }
        }

        // 9. Проверка итоговых значений формы	Заполняется автоматически (графа 4..6, 9, 10, 12, 13).
        if (hasTotalRow) {
            def totalRow = formData.getDataRow('total')
            def totalSumColumns = ['nominalPriceSecurities', 'salePrice', 'acquisitionPrice', 'income',
                    'outcome', 'outcome269st', 'outcomeTax']
            for (def alias : totalSumColumns) {
                if (totalRow.getCell(alias).getValue() != getSum(alias)) {
                    logger.error('Итоговые значения формы рассчитаны неверно!')
                    return
                }
            }
        }
    }
}

/**
 * Проверки соответствия НСИ.
 *
 * @param checkRequiredColumns проверять ли обязательные графы
 */
void checkNSI() {
    if (!formData.dataRows.isEmpty()) {
        /** Отчетная дата. */
        def reportDate = new Date() // TODO (Ramil Timerbaev) как получить отчетную дату

        def hasError = false
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // 1. Проверка кода валюты со справочным (графа 3)
            if (false) {
                logger.warn('Неверный код валюты!')
            }

            // 2. Проверка соответствия ставки рефинансирования ЦБ (графа 11) коду валюты (графа 3)
            hasError = false
            if (((row.outcome == 0 || isEmpty(row.currencyCode)) && row.rateBR == null)) {
                hasError = false
            } else if ((row.outcome == 0 || isEmpty(row.currencyCode)) && row.rateBR != null) {
                hasError = true
            } else if (row.currencyCode == '810' && true) {
                // TODO (Ramil Timerbaev) условие: «графа 11» != ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на «отчетную дату»
                // row.rateBR != значнеие из справочника
                hasError = true
            } else if (row.currencyCode != '810') {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009') && row.rateBR != 22) {
                    hasError = true
                } else if (inPeriod(reportDate, '01.01.2011', '31.12.2012') && true) {
                    // TODO (Ramil Timerbaev) условие: графа 11 != ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на «отчетную дату»
                    // row.rateBR != значнеие из справочника
                    hasError = true
                } else if (row.rateBR != 15) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Неверно указана ставка Банка России!')
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
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Проверить попадает ли указанная дата в период
 */
def inPeriod(def date, def from, to) {
    if (date == null) {
        return false
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def dateFrom = format.parse(from)
    def dateTo = format.parse(to)
    return (dateFrom < date && date <= dateTo)
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value) {
    if (isEmpty(value)) {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    return format.parse(value)
}

/**
 * Посчитать значение для графы 12.
 *
 * @paam row строка нф
 * @paam coef коэфициент
 * @paam reportDate отчетная дата
 * @paam days количество дней в году
 */
def calc12Value(def row, def coef, def reportDate, def days) {
    def tmp = (row.salePrice * row.rateBR * coef) * ((reportDate - row.part1REPODate) / days) / 100
    return round(tmp, 2)
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
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'salePrice', 'acquisitionPrice', 'part1REPODate', 'part2REPODate',
            'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}