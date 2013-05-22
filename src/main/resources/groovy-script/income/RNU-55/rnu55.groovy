/**
 * Скрипт для РНУ-55 (rnu55.groovy).
 * Форма "(РНУ-55) Регистр налогового учёта процентного дохода по процентным векселям сторонних эмитентов".
 *
 * @version 59
 *
 * TODO:
 *      - нет уcловии в проверках соответствия НСИ (потому что нету справочников)
 *		- уточнить отчётною дату, откуда ее брать?
 *		- уточнить про логические проверки 5, 6, проверять на незаполнение если какие-то суммы не введены
 *		- уточнить про логическую проверку 8 (проверять с даты графы 3 до начала отчетного периода?)
 *      - консолидация
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
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - number
// графа 2  - bill
// графа 3  - buyDate
// графа 4  - currency
// графа 5  - nominal
// графа 6  - percent
// графа 7  - implementationDate
// графа 8  - percentInCurrency
// графа 9  - percentInRuble
// графа 10 - sumIncomeinCurrency
// графа 11 - sumIncomeinRuble

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // графа 2..9
    ['bill', 'buyDate', 'currency', 'nominal', 'percent', 'implementationDate',
            'percentInCurrency', 'percentInRuble'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
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
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 2..6)
    def requiredColumns = ['bill', 'buyDate', 'currency', 'nominal', 'percent']

    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            hasError = true
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
    if (formData.dataRows.isEmpty()) {
        return
    }

    def tmp

    /** Количество дней в году. */
    def daysInYear = getCountDaysInYaer(new Date())

    /** Отчетная дата. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def reportDate = (tmp ? tmp.getTime() : null) // TODO (Ramil Timerbaev) Уточнить

    /** Начальная дата отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def reportDateStart = (tmp ? tmp.getTime() : null)

    /** Количество дней владения векселем в отчетном периоде. */
    def countsDays = 1
    /** Курс банка России. */
    def rate

    formData.dataRows.eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1

        // графа 10
        if (row.percentInCurrency == null && row.percentInRuble == null) {
            countsDays = (row.buyDate >= reportDateStart ?
                reportDate - row.buyDate + 1 : reportDate - reportDateStart)
            if (countsDays != 0) {
                row.sumIncomeinCurrency = row.nominal * row.percent / 100 * countsDays / daysInYear
            } else {
                def index = getIndex(row)
                logger.warn("Невозможно вычислить графу 10 в строке $index. Деление на ноль. Количество дней владения векселем в отчётном периоде равно 0.")
            }
        } else if (row.percentInCurrency != null && row.percentInRuble != null) {
            tmp = getCalcPrevColumn10(row.bill, 'sumIncomeinCurrency')
            row.sumIncomeinCurrency = row.percentInCurrency - tmp
        }

        // графа 11
        if (row.percentInCurrency == null && row.percentInRuble == null) {
            if (row.implementationDate != null) {
                rate = getRate(row.implementationDate)
            } else {
                // TODO (Ramil Timerbaev) сделать получение курса Банка России из справочника по графе 4
                rate = 1
            }
            row.sumIncomeinRuble = row.sumIncomeinCurrency * rate
        } else if (row.percentInCurrency != null && row.percentInRuble != null) {
            tmp = getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
            row.sumIncomeinRuble = row.percentInRuble - tmp
        }
    }

    // итого
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    setTotalStyle(totalRow)
    ['percentInRuble', 'sumIncomeinRuble'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
void logicalCheck(def useLog) {
    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 1..11)
        def requiredColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal',
                'percent', 'implementationDate', 'percentInCurrency',
                'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble']

        // суммы строки общих итогов
        def totalSums = [:]

        // графы для которых надо вычислять итого (графа 9, 11)
        def totalColumns = ['percentInRuble', 'sumIncomeinRuble']

        // признак наличия итоговых строк
        def hasTotal = false

        /** Отчётный период. */
        def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

        /** Налоговый период. */
        def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYaer(new Date())

        /** Дата начала отчетного периода. */
        def a = (taxPeriod != null ? taxPeriod.getStartDate() : null )

        /** Дата окончания отчетного периода. */
        def b = (taxPeriod != null ? taxPeriod.getEndDate() : null)

        /** Отчетная дата. */
        tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
        def reportDate = (tmp ? tmp.getTime() : null) // TODO (Ramil Timerbaev) Уточнить

        def cell
        def hasError
        def tmp
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 1. Обязательность заполнения поля графы 1..11
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return
            }

            // 2. Проверка даты приобретения и границ отчетного периода (графа 3)
            if (row.buyDate > b) {
                logger.error('Дата приобретения вне границ отчетного периода!')
                return
            }

            // 3. Проверка даты реализации (погашения)  и границ отчетного периода (графа 7)
            if (row.implementationDate < a || b < row.implementationDate) {
                logger.error('Дата реализации (погашения) вне границ отчетного периода!')
                return
            }

            // 4. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                return
            }
            i = i + 1

            // 5. Проверка на нулевые значения (графа 8)
            // TODO (Ramil Timerbaev)
            if (row.percentInCurrency != null && false) {
                logger.error('Поле ”<Наименование поля>” при отсутствии сумм не заполняется!')
                return
            }

            // 6. Проверка на нулевые значения (графа 9)
            // TODO (Ramil Timerbaev)
            if (row.percentInRuble != null && false) {
                logger.error('Поле ”<Наименование поля>” при отсутствии сумм не заполняется!')
                return
            }

            // 7. Проверка на нулевые значения (графа 8, 9, 10, 11)
            if (row.percentInCurrency == 0 &&
                    row.percentInRuble == 0 &&
                    row.sumIncomeinCurrency == 0 &&
                    row.sumIncomeinRuble == 0) {
                logger.error('Все суммы по операции нулевые!')
                return
            }

            // 8. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 10 и графы 11
            // TODO (Ramil Timerbaev)
            if (false) {
                logger.error("Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)!")
            }

            // 9. Проверка на неотрицательные значения
            hasError = false
            ['percentInCurrency', 'percentInRuble'].each {
                cell = row.getCell(it)
                if (cell.getValue() != null && cell.getValue() < 0) {
                    def name = cell.getColumn().getName()
                    logger.error("Значение графы \"$name\"  отрицательное!")
                    hasError = true
                }
            }
            if (hasError) {
                return
            }

            // 10. Арифметическая проверка графы 10
            hasError = false
            tmp = getCalcPrevColumn10(row.bill, 'sumIncomeinCurrency')
            if (row.percentInCurrency == null && row.percentInRuble == null) {
                def countsDays = (row.buyDate >= reportDateStart ?
                    reportDate - row.buyDate + 1 : reportDate - reportDateStart)
                if (countsDays != 0 &&
                        row.sumIncomeinCurrency != row.nominal * row.percent / 100 * countsDays / daysInYear) {
                    hasError = true
                } else if (useLog) {
                    def index = getIndex(row)
                    logger.warn("Невозможно вычислить графу 10 в строке $index. Деление на ноль. Количество дней владения векселем в отчётном периоде равно 0.")
                }
            } else if (row.percentInCurrency != null && row.percentInRuble != null &&
                    row.sumIncomeinCurrency != row.percentInCurrency - tmp) {
                hasError = true
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в валюте»!')
                return
            }

            // 11. Арифметическая проверка графы 11
            hasError = false
            tmp = getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
            if (row.percentInCurrency == null && row.percentInRuble == null) {
                if (row.implementationDate != null) {
                    rate = getRate(row.implementationDate)
                } else {
                    // TODO (Ramil Timerbaev) сделать получение курса Банка России из справочника по графе 4
                    rate = 1
                }
                if (row.sumIncomeinRuble != row.sumIncomeinCurrency * rate) {
                    hasError = false
                }
            } else if (row.percentInCurrency != null && row.percentInRuble != null &&
                    row.sumIncomeinRuble != row.percentInRuble - tmp) {
                hasError = true
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в рублях по курсу Банка России»!')
                return
            }

            // 12. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 12. Проверка итогового значений по всей форме (графа 9, 11)
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('Итоговые значения рассчитаны неверно!')
                    return
                }
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

            // 1. Проверка кода валюты со справочным (графа 4)
            if (false) {
                logger.warn('Неверный код валюты!')
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
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'bill', 'buyDate', 'currency', 'nominal', 'percent',
            'implementationDate', 'percentInCurrency', 'percentInRuble',
            'sumIncomeinCurrency', 'sumIncomeinRuble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}

// TODO (Ramil Timerbaev) учесть графу 3 при суммировании
/**
 * Cумма ранее начисленного процентного дохода по векселю до отчётного периода» (сумма граф 10 из РНУ-55 предыдущих отчётных (налоговых) периодов) выбирается по графе 2 с даты приобретения (графа3) по дату начала отчетного периода.
 *
 * @param bill вексель
 * @param sumColumnName название графы, по которой суммировать данные
 */
def getCalcPrevColumn10(def bill, def sumColumnName) {
    def formDataOld = getFormDataOld()
    def sum = 0
    if (formDataOld == null) {
        return 0
    }
    formDataOld.dataRows.each {
        if (bill == row.bill) {
            sum += getValue(row.getCell(sumColumnName).getValue())
        }
    }
    return sum
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-55 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, FormDataKind.PRIMARY, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * Получить значение или ноль, если значения нет.
 */
def getValue(def value) {
    return (value != null ? value : 0)
}

/**
 * Получить курс банка России на указанную дату.
 */
def getRate(def date) {
    // TODO (Ramil Timerbaev) откуда брать?
    return 1
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
 * Получить количество дней в году по указанной дате.
 */
def getCountDaysInYaer(def date) {
    if (date == null) {
        return 0
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def year = date.format('yyyy')
    def end = format.parse("31.12.$year")
    def begin = format.parse("01.01.$year")
    return end - begin + 1
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}