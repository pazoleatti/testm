/**
 * Скрипт для РНУ-23 (rnu23.groovy).
 * Форма "(РНУ-23) Регистр налогового учёта доходов по выданным гарантиям".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 * 		- про нумерацию пока не уточнили, пропустить
 *		- графа 17 и графа 18 уточняют
 *      - консолидация	
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
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - number
// графа 2  - contract
// графа 3  - contractDate
// графа 4  - amountOfTheGuarantee
// графа 5  - dateOfTransaction
// графа 6  - rateOfTheBankOfRussia
// графа 7  - interestRate
// графа 8  - baseForCalculation
// графа 9  - accrualAccountingStartDate
// графа 10 - accrualAccountingEndDate
// графа 11 - preAccrualsStartDate
// графа 12 - preAccrualsEndDate
// графа 13 - incomeCurrency
// графа 14 - incomeRuble
// графа 15 - accountingCurrency
// графа 16 - accountingRuble
// графа 17 - preChargeCurrency
// графа 18 - preChargeRuble
// графа 19 - taxPeriodCurrency
// графа 20 - taxPeriodRuble

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    // если данных еще нет или строка не выбрана
    if (formData.dataRows.isEmpty() || currentDataRow == null || getIndex(currentDataRow) == -1) {
        formData.dataRows.add(newRow)
    } else {
        formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)
    }

    // графа 2..12
    ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction',
            'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation',
            'accrualAccountingStartDate', 'accrualAccountingEndDate',
            'preAccrualsStartDate', 'preAccrualsEndDate'].each {
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
    // РНУ-22 предыдущего периода
    def formDataOld = getFormDataOld()

    def totalRowOld = (formDataOld != null ? formDataOld.getDataRow('total').taxPeriodCurrency : null)

    /*
	 * Проверка объязательных полей.
	 */

    // список проверяемых столбцов (графа 2..8)
    def requiredColumns = ['contract', 'contractDate', 'amountOfTheGuarantee',
            'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation']

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
     * Расчеты.
     */

    // удалить строку "итого"
    def total = null
    if (formData != null && !formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (row.getAlias() == 'total') {
                total = row
                break
            }
        }
    }
    if (total != null) {
        formData.dataRows.remove(total)
    }
    if (hasError) {
        return
    }

    // графа 1, 13..20
    formData.dataRows.eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1

        // графа 13
        row.incomeCurrency = getColumn13or15or19(row, row.accrualAccountingStartDate, row.accrualAccountingEndDate)

        // графа 14
        row.incomeRuble = round(row.incomeCurrency * row.rateOfTheBankOfRussia, 2)

        // графа 15
        row.accountingCurrency = getColumn13or15or19(row, row.accrualAccountingStartDate, row.accrualAccountingEndDate)

        // графа 16
        row.accountingRuble = round(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)

        // графа 17
        // TODO (Ramil Timerbaev) уточнят
        row.preChargeCurrency = (totalRowOld != null ? totalRowOld.taxPeriodCurrency : 0)

        // графа 18
        // TODO (Ramil Timerbaev) уточнят
        row.preChargeRuble = (totalRowOld != null ? totalRowOld.taxPeriodRuble : 0)

        // графа 19
        row.taxPeriodCurrency = getColumn13or15or19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)

        // графа 20
        row.taxPeriodRuble = round(row.taxPeriodCurrency * row.rateOfTheBankOfRussia, 2)
    }

    // Графы для которых надо вычислять итого (графа 13..20)
    def totalColumns = ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
            'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.contract = 'Итого'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(formData, alias))
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
void logicalCheck(def useLog) {
    def tmp

    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 1..8, 13..20)
        def requiredColumns = ['number', 'contract', 'contractDate', 'amountOfTheGuarantee',
                'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation',
                'incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
                'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']
        // суммы строки общих итогов
        def totalSums = [:]

        // графы для которых надо вычислять итого (графа 13..20)
        def totalColumns = ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
                'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

        // признак наличия итоговых строк
        def hasTotal = false

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 7. Обязательность заполнения поля графы 1..8, 13..20
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return
            }

            // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
            if (a != null && b != null &&
                    ((row.dateOfTransaction != null && (row.dateOfTransaction < a || b < row.dateOfTransaction)) ||
                            (row.accrualAccountingEndDate != null && (row.accrualAccountingEndDate < a || b < row.accrualAccountingEndDate)) ||
                            (row.preAccrualsEndDate != null && (row.preAccrualsEndDate < a || b < row.preAccrualsEndDate)))
            ) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                return
            }

            // 2. Проверка на нулевые значения (графа 13..20)
            def hasNull = true
            ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
                    'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble'].each { alias ->
                tmp = row.getCell(alias).getValue()
                if (tmp != null && tmp != 0) {
                    hasNull = false
                }
            }
            if (hasNull) {
                logger.error('Все суммы по операции нулевые!')
                return
            }

            // 3. Проверка на сумму гарантии (графа 4)
            if (row.amountOfTheGuarantee != null && row.amountOfTheGuarantee == 0) {
                logger.warn('Суммы гарантии равны нулю!')
            }

            // 4. Проверка задания расчётного периода (графа 9, 10, 11, 12)
            if (row.accrualAccountingStartDate > row.accrualAccountingEndDate ||
                    row.preAccrualsStartDate > row.preAccrualsEndDate) {
                logger.warn('Неправильно задан расчётный период!')
            }

            // 5. Проверка на корректность даты договора (графа 3)
            if (row.contractDate > b) {
                logger.error('Дата договора неверная!')
                return
            }

            // 8. Проверка на заполнение поля «<Наименование поля>»
            // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
            def checkColumn9and10 = row.accrualAccountingStartDate != null && row.accrualAccountingEndDate != null &&
                    (row.preAccrualsStartDate != null || row.preAccrualsEndDate != null)
            // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
            def checkColumn11and12 = (row.accrualAccountingStartDate != null || row.accrualAccountingEndDate != null) &&
                    row.preAccrualsStartDate != null && row.preAccrualsEndDate != null
            if (checkColumn9and10 || checkColumn11and12) {
                logger.error('Поля в графе 9, 10, 11, 12 заполены неверно!')
                break
            }

            // 9. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                break
            }
            i += 1

            // 10. Арифметическая проверка графы 13
            tmp = getColumn13or15or19(row, row.accrualAccountingStartDate, row.accrualAccountingEndDate)
            if (row.incomeCurrency != tmp) {
                logger.error('Неверно рассчитана графа «Сумма начисленного дохода. Валюта»!')
                return
            }

            // 11. Арифметическая проверка графы 14
            if (row.incomeRuble != round(row.incomeCurrency * row.rateOfTheBankOfRussia, 2)) {
                logger.error('Неверно рассчитана графа «Сумма начисленного дохода. Рубли»!')
                return
            }

            // 12. Арифметическая проверка графы 15
            tmp = getColumn13or15or19(row, row.accrualAccountingStartDate, row.accrualAccountingEndDate)
            if (row.accountingCurrency != tmp) {
                logger.error('Неверно рассчитана графа «Сумма дохода, отражённая в бухгалтерском учёте. Валюта»!')
                return
            }

            // 13. Арифметическая проверка графы 16
            if (row.accountingRuble != round(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)) {
                logger.error('Неверно рассчитана графа «Сумма дохода, отражённая в бухгалтерском учёте. Рубли»!')
                return
            }

            // 14. Арифметическая проверка графы 17
            // TODO (Ramil Timerbaev) уточнят
            tmp = getSum(formDataOld, 'taxPeriodCurrency')
            if (row.preChargeCurrency != tmp) {
                logger.error('Неверно рассчитана графа «Сумма доначисления. Предыдущий период. Валюта»!')
                return
            }

            // 15. Арифметическая проверка графы 18
            // TODO (Ramil Timerbaev) уточнят
            tmp = getSum(formDataOld, 'taxPeriodRuble')
            if (row.preChargeRuble != tmp) {
                logger.error('Неверно рассчитана графа «Сумма доначисления. Предыдущий период. Рубли»!')
                return
            }

            // 16. Арифметическая проверка графы 19
            tmp = getColumn13or15or19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)
            if (row.taxPeriodCurrency != tmp) {
                logger.error('Неверно рассчитана графа «Сумма доначисления. Отчётный период. Валюта»!')
                return
            }

            // 17. Арифметическая проверка графы 20
            if (row.taxPeriodRuble != round(row.taxPeriodCurrency * row.rateOfTheBankOfRussia, 2)) {
                logger.error('Неверно рассчитана графа «Сумма доначисления. Отчётный период. Рубли»!')
                return
            }

            // 18. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 16, 14, 18)
            if (totalRow.incomeRuble + totalRow.preChargeRuble < totalRow.accountingRuble) {
                logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
            }

            // 18. Проверка итогового значений по всей форме (графа 13..20)
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    hindError = true
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
    // 1. Проверка курса валюты со справочным - Проверка актуальности значения» графы 6» на дату по «графе 5»
    if (false) {
        logger.warn('Неверный курс валюты!')
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
 * Получить значение графа 13 (аналогично для графа 15 и графа 19)
 *
 * @param row строка нф
 * @param date1 дата начала
 * @param date2 дата окончания
 */
def getColumn13or15or19(def row, def date1, def date2) {
    if (date1 == null || date2 == null) {
        return 0
    }
    def division = row.baseForCalculation * (date2 - date1 + 1)

    if (division == 0) {
        logger.error('Деление на ноль. Возможно неправильно выбраны даты.')
        return 0
    }
    return round((row.amountOfTheGuarantee * row.interestRate) / (division), 2)
}

/**
 * Получить сумму столбца.
 */
def getSum(def form, def columnAlias) {
    if (form == null) {
        return 0
    }
    def to = 0
    def from = form.dataRows.size() - 2
    if (to > from) {
        return 0
    }
    return summ(form, new ColumnRange(columnAlias, to, from))
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // (РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования (За предыдущий отчетный период)
    def formDataOld = (prevReportPeriod != null ? FormDataService.find(formData.formType.id, FormDataKind.PRIMARY, formDataDepartment.id, prevReportPeriod.id) : null)

    return formDataOld
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'contract', 'contractDate', 'amountOfTheGuarantee',
            'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate',
            'baseForCalculation', 'accrualAccountingStartDate', 'accrualAccountingEndDate',
            'preAccrualsStartDate', 'preAccrualsEndDate', 'incomeCurrency', 'incomeRuble',
            'accountingCurrency', 'accountingRuble', 'preChargeCurrency', 'preChargeRuble',
            'taxPeriodCurrency', 'taxPeriodRuble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
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
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}