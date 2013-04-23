/**
 * Скрипт для РНУ-23 (rnu23.groovy).
 * Форма "(РНУ-23) Регистр налогового учёта доходов по выданным гарантиям".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 *		- получение даты начала и окончания отчетного периода
 * 		- про нумерацию пока не уточнили, пропустить
 *		- графа 17 и графа 18 уточняют
 *		- при выводе незаполненных ячейках, выдает ошибку "Conversion = ')'" если в названии колонки есть знак %
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
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..12
    ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction',
            'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation',
            'accrualAccountingStartDate', 'accrualAccountingEndDate',
            'preAccrualsStartDate', 'preAccrualsEndDate'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    def pos = (currentDataRow != null && !formData.dataRows.isEmpty() ? currentDataRow.getOrder() : formData.dataRows.size)
    formData.dataRows.add(pos, newRow)
    setOrder()
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {

    /** Идентификатор предыдущего периода. */
    def reportPeriodIdOld = null

    /** Данные предыдущего периода. */
    def formDataOld = (reportPeriodIdOld != null ? FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodIdOld) : null)

    def totalRowOld = (formDataOld != null ? formDataOld.getDataRow('total').taxPeriodCurrency : null)

    /*
	 * Проверка объязательных полей.
	 */
    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 2..8)
            def requiredColumns = ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction',
                    'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation']

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
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
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

    /** Столбцы для которых надо вычислять итого и итого по эмитенту. Графа 13..20. */
    def totalColumns = ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
            'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

    // добавить строки "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.contract = 'Итого'
    // графа 13..20 для последней строки "итого"
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(formData, alias))
    }

    setOrder()
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    // 8.		Проверка на уникальность поля «№ пп»	Уникальность поля графы 1 (в рамках текущего года) 	1	Нарушена уникальность номера по порядку!

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    /** Налоговый период. */
    def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)

    /** Дата начала отчетного периода. */
    def a = (taxPeriod != null ? taxPeriod.getStartDate() : null)

    /** Дата окончания отчетного периода. */
    def b = (taxPeriod != null ? taxPeriod.getEndDate() : null)

    if (!formData.dataRows.isEmpty()) {
        def index = 1
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
            if (a != null && b != null &&
                    ((row.dateOfTransaction != null && (row.dateOfTransaction < a || b < row.dateOfTransaction)) ||
                            (row.accrualAccountingEndDate != null && (row.accrualAccountingEndDate < a || b < row.accrualAccountingEndDate)) ||
                            (row.preAccrualsEndDate != null && (row.preAccrualsEndDate < a || b < row.preAccrualsEndDate)))
            ) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                break
            }

            // 2. Проверка на нулевые значения (графа 13..20)
            def hasNull = true
            ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
                    'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble'].each { alias ->
                def tmp = row.getCell(alias).getValue()
                if (tmp != null && tmp != 0) {
                    hasNull = false
                }
            }
            if (hasNull) {
                logger.error('Все суммы по операции нулевые!')
                break
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
                break
            }

            // 7. Проверка на заполнение поля «<Наименование поля>». Графа 1..8, 13..20
            def colNames = []
            // Список проверяемых столбцов
            ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction',
                    'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation',
                    'incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
                    'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble'].each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                def errorMsg = colNames.join(', ')
                logger.error("Поля $errorMsg не заполнены!")
                break
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
            if (index != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                break
            }
            index += 1
        }

        // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 16, 14, 18)
        def totalRow = formData.getDataRow('total')
        if (totalRow.accountingRuble < 0 || (totalRow.incomeRuble + totalRow.preChargeRuble) < totalRow.accountingRuble) {
            logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
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
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
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