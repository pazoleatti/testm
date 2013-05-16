/**
 * Скрипт для РНУ-56 (rnu56.groovy).
 * Форма "(РНУ-55) Регистр налогового учёта процентного дохода по процентным векселям сторонних эмитентов".
 *
 * TODO:
 *      - нет уcловии в проверках соответствия НСИ (потому что нету справочников)
 *		- уточнить про дату окончания отчётного периода (или отчетная дата?!), откуда ее брать?
 *		- уточнить чтз про графу 17, в нф всего графов 15
 *		- уточнить про вычисление 14ой графы, последний блок, по предыдущим условиям туда никогда не попадет
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

// графа 1  - number
// графа 2  - bill
// графа 3  - buyDate
// графа 4  - currency
// графа 5  - nominal
// графа 6  - price
// графа 7  - maturity
// графа 8  - termDealBill
// графа 9  - percIncome
// графа 10 - implementationDate
// графа 11 - sum
// графа 12 - discountInCurrency
// графа 13 - discountInRub
// графа 14 - sumIncomeinCurrency
// графа 15 - sumIncomeinRuble

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.appendDataRow(currentDataRow, null)

    // графа 2..7, 10, 11
    ['bill', 'buyDate', 'currency', 'nominal', 'price',
            'maturity', 'implementationDate', 'sum'].each {
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
            // Список проверяемых столбцов (графа 2..7)
            def requiredColumns = ['bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.number
                def errorMsg = colNames.join(', ')
                if (!isEmpty(index)) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
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
    if (formData.dataRows.isEmpty()) {
        return
    }

    def tmp

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

        // графа 8
        row.termDealBill = row.buyDate - row.maturity + 1

        // графа 9
        row.percIncome = row.nominal - row.price

        // графа 12
        row.discountInCurrency = row.sum - row.price

        // графа 13
        // TODO (Ramil Timerbaev) уточнить чтз про графу 17, в нф всего графов 15
        row.discountInRub = row.discountInCurrency * getRate(row.currency)

        // графа 14
        if (row.implementationDate == null && row.sum == null) {
            countsDays = (row.buyDate >= reportDateStart ?
                reportDate - row.buyDate + 1 : reportDate - reportDateStart)
            if (row.termDealBill != 0) {
                row.sumIncomeinCurrency = row.percIncome / row.termDealBill * countsDays
            } else {
                def index = getIndex(row)
                logger.warn("Невозможно вычислить графу 10 в строке $index. Деление на ноль. Количество дней владения векселем в отчётном периоде равно 0.")
            }
        } else {
            tmp = getCalcPrevColumn(row.bill, 'sumIncomeinCurrency')
            row.sumIncomeinCurrency = (row.implementationDate != null && row.sum == null ?
                row.percIncome : row.discountInCurrency) - tmp
        }

        // графа 15
        if (row.implementationDate == null && row.sum == null) {
            rate = getRate(reportDate)
            row.sumIncomeinRuble = row.sumIncomeinCurrency * rate
        } else if (row.implementationDate != null && row.sum == null) { // последнее условие поменял местами
            tmp = getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
            row.sumIncomeinRuble = row.discountInRub - tmp
        } else if (row.implementationDate != null) {
            rate = getRate(row.implementationDate)
            row.sumIncomeinRuble = row.sumIncomeinCurrency * rate
        }
    }

    // итого (графа 13, 15)
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    setTotalStyle(totalRow)
    ['discountInRub', 'sumIncomeinRuble'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
}

/**
 * Логические проверки.
 *
 * @param checkRequiredColumns проверять ли обязательные графы
 */
void logicalCheck(def checkRequiredColumns) {
    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // суммы строки общих итогов
        def totalSums = [:]

        // графы для которых надо вычислять итого (графа 13, 15)
        def totalColumns = ['discountInRub', 'sumIncomeinRuble']

        // признак наличия итоговых строк
        def hasTotal = false

        /** Отчётный период. */
        def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

        /** Налоговый период. */
        def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)

        /** Дата начала отчетного периода. */
        def a = (taxPeriod != null ? taxPeriod.getStartDate() : null )

        /** Дата окончания отчетного периода. */
        def b = (taxPeriod != null ? taxPeriod.getEndDate() : null)

        /** Отчетная дата. */
        def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
        def reportDate = (tmp ? tmp.getTime() : null) // TODO (Ramil Timerbaev) Уточнить

        def cell
        def hasError
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 1. Обязательность заполнения поля графы 1..15
            def colNames = []
            def requiredColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal',
                    'price', 'maturity', 'termDealBill', 'percIncome',
                    'implementationDate', 'sum', 'discountInCurrency',
                    'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble']
            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                if (!checkRequiredColumns) {
                    return
                }
                def index = row.number
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = getIndex(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
                return
            }

            // TODO (Ramil Timerbaev)
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

            // 5. Проверка на нулевые значения (графа 11)
            // TODO (Ramil Timerbaev)
            if (row.sum != null && false) {
                logger.error('Поле ”<Наименование поля>” при отсутствии сумм не заполняется!')
                return
            }

            // 6. Проверка на нулевые значения (графа 12..15)
            hasError = false
            ['discountInCurrency', 'discountInRub', 'sumIncomeinCurrency',
                    'sumIncomeinRuble'].each {
                if (row.getCell(it).getValue() == 0) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Все суммы по операции нулевые!')
                return
            }

            // 7. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 14 и графы 15
            // TODO (Ramil Timerbaev)
            if (false) {
                logger.error("Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)!")
            }

            // 8. Проверка корректности расчёта дисконта
            if (row.sum - row.price <= 0 && (row.discountInCurrency != 0 || row.discountInRub != 0)) {
                logger.error('Расчёт дисконта некорректен!')
            }

            // 9. Проверка на неотрицательные значения (графа 12, 13)
            hasError = false
            ['discountInCurrency', 'discountInRub'].each {
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

            // 10. Арифметическая проверка графы 8
            if (row.termDealBill != row.buyDate - row.maturity + 1) {
                logger.error('Неверно рассчитана графа «Возможный срок обращения векселя, дней»!')
                return
            }

            // 11. Арифметическая проверка графы 9
            if (row.percIncome != row.nominal - row.price) {
                logger.error('Неверно рассчитана графа «Заявленный процентный доход (дисконт), ед. валюты»!')
                return
            }

            // 12. Арифметическая проверка графы 12
            if (row.discountInCurrency != row.sum - row.price) {
                logger.error('Неверно рассчитана графа «Фактически поступившая сумма дисконта в валюте»!')
                return
            }

            // 13. Арифметическая проверка графы 13
            // TODO (Ramil Timerbaev) уточнить чтз про графу 17, в нф всего графов 15
            if (row.discountInRub != row.discountInCurrency * getRate(row.currency)) {
                logger.error('Неверно рассчитана графа «Фактически поступившая сумма дисконта 			в рублях по курсу Банка России»!')
                return
            }

            // 14. Арифметическая проверка графы 14
            hasError = false
            if (row.implementationDate == null && row.sum == null) {
                countsDays = (row.buyDate >= reportDateStart ?
                    reportDate - row.buyDate + 1 : reportDate - reportDateStart)
                if (row.termDealBill != 0 &&
                        row.sumIncomeinCurrency != row.percIncome / row.termDealBill * countsDays) {
                    hasError = true
                } else {
                    def index = getIndex(row)
                    logger.warn("Невозможно вычислить графу 10 в строке $index. Деление на ноль. Количество дней владения векселем в отчётном периоде равно 0.")
                }
            } else {
                tmp = getCalcPrevColumn(row.bill, 'sumIncomeinCurrency')
                if (row.sumIncomeinCurrency != (row.implementationDate != null && row.sum == null ?
                    row.percIncome : row.discountInCurrency) - tmp) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в валюте»!')
                return
            }

            // 15. Арифметическая проверка графы 15
            hasError = false
            if (row.implementationDate == null && row.sum == null) {
                rate = getRate(reportDate)
                if (row.sumIncomeinRuble != row.sumIncomeinCurrency * rate) {
                    hasError = true
                }
            } else if (row.implementationDate != null && row.sum == null) { // последнее условие поменял местами
                tmp = getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
                if (row.sumIncomeinRuble != row.discountInRub - tmp) {
                    hasError = true
                }
            } else if (row.implementationDate != null) {
                rate = getRate(row.implementationDate)
                if (row.sumIncomeinRuble != row.sumIncomeinCurrency * rate) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в рублях по курсу Банка России»!')
                return
            }

            // 16. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 16. Проверка итогового значений по всей форме (графа 13, 15)
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
    ['number', 'bill', 'buyDate', 'currency', 'nominal',
            'price', 'maturity', 'termDealBill', 'percIncome',
            'implementationDate', 'sum', 'discountInCurrency',
            'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble'].each {
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
 * Cумма ранее начисленного процентного дохода по векселю до отчётного периода» (сумма граф 14 из РНУ-56 предыдущих отчётных (налоговых) периодов) выбирается по графе 2 с даты приобретения (графа3) по дату начала отчетного периода.
 *
 * @param bill вексель
 * @param sumColumnName название графы, по которой суммировать данные
 */
def getCalcPrevColumn(def bill, def sumColumnName) {
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