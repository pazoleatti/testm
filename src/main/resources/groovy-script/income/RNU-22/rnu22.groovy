/**
 * Скрипт для РНУ-22 (rnu22.groovy).
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования".
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
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - rowNumber
// графа 2  - contractNumber
// графа 3  - contraclData
// графа 4  - base
// графа 5  - transactionDate
// графа 6  - course
// графа 7  - interestRate
// графа 8  - basisForCalc
// графа 9  - calcPeriodAccountingBeginDate
// графа 10 - calcPeriodAccountingEndDate
// графа 11 - calcPeriodBeginDate
// графа 12 - calcPeriodEndDate
// графа 13 - accruedCommisCurrency
// графа 14 - accruedCommisRub
// графа 15 - commisInAccountingCurrency
// графа 16 - commisInAccountingRub
// графа 17 - accrualPrevCurrency
// графа 18 - accrualPrevRub
// графа 19 - reportPeriodCurrency
// графа 20 - reportPeriodRub

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..12
    ['contractNumber', 'contraclData', 'base', 'transactionDate', 'course',
            'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate',
            'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate'].each {
        newRow.getCell(it).editable = true
    }
    def pos = (currentDataRow != null && !formData.dataRows.isEmpty() ? currentDataRow.getOrder() : formData.dataRows.size)
    formData.dataRows.add(pos, newRow)
    setOrder()
}

/**
 * Удалить строку.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)
    setOrder()
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /** Предыдущий отчётный период. */
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    /** (РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования (За предыдущий отчетный период). */
    def formDataOld = (prevReportPeriod != null ? FormDataService.find(322, FormDataKind.PRIMARY, formDataDepartment.id, prevReportPeriod.id) : null)

    /*
      * Проверка объязательных полей.
      */
    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row)) {
            def colNames = []
            // Список проверяемых столбцов
            def columns = ['contractNumber', 'contraclData', 'base', 'transactionDate', 'course',
                    'interestRate', 'basisForCalc']

            columns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    // TODO (Ramil Timerbaev) из за % в названии заголовка может выдавать ошибки
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = row.getOrder()
                    logger.error("В $index строке не заполнены колонки : $errorMsg.")
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

    // графа 13..20
    formData.dataRows.each { row ->
        // графа 13
        row.accruedCommisCurrency = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)

        // графа 14
        row.accruedCommisRub = round(row.accruedCommisCurrency * row.course, 2)

        // графа 15
        // TODO (Ramil Timerbaev) уточнить у Карины... совпадает с 13ой строкой
        row.commisInAccountingCurrency = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)

        // графа 16
        row.commisInAccountingRub = round(row.commisInAccountingCurrency * row.course, 2)

        // графа 17
        // TODO (Ramil Timerbaev) уточнят
        row.accrualPrevCurrency = getSum(formDataOld, 'reportPeriodCurrency')

        // графа 18
        // TODO (Ramil Timerbaev) уточнят
        row.accrualPrevRub = getSum(formDataOld, 'reportPeriodRub')

        // графа 19
        row.reportPeriodCurrency = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)

        // графа 20
        row.reportPeriodRub = round(row.reportPeriodCurrency * row.course, 2)
    }

    // графа 1
    formData.dataRows.eachWithIndex { row, index ->
        row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
    }

    // добавить строку "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.contractNumber = 'Итого'
    // графа 13..20
    ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency', 'commisInAccountingRub',
            'accrualPrevCurrency', 'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(formData, alias))
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
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
            if (a > row.transactionDate ||
                    row.calcPeriodAccountingEndDate > b ||
                    row.calcPeriodEndDate > b) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                break
            }

            // 2. Проверка на нулевые значения (графа 13..20)
            def hasNull = true
            ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
                    'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                    'reportPeriodCurrency', 'reportPeriodRub'].each { alias ->
                def tmp = row.getCell(alias).getValue()
                if (tmp != null && tmp != 0) {
                    hasNull = false
                }
            }
            if (hasNull) {
                logger.error('Все суммы по операции нулевые!')
                break
            }

            // 3. Проверка на сумму платы (графа 4)
            if (row.base != null && row.base == 0) {
                logger.warn('Суммы платы равны 0!')
                break
            }

            // 4. Проверка задания расчётного периода
            if (row.calcPeriodAccountingBeginDate > row.calcPeriodAccountingEndDate ||
                    row.calcPeriodBeginDate > row.calcPeriodEndDate) {
                logger.warn('Неправильно задан расчётный период!')
                break
            }

            // 5. Проверка на корректность даты договора
            if (row.contraclData > b) {
                logger.error('Дата договора неверная!')
                break
            }


            // 7. Проверка на заполнение поля «<Наименование поля>». Графа 1..8, 13..20
            def colNames = []
            // Список проверяемых столбцов
            ['contractNumber', 'contraclData', 'base', 'transactionDate', 'course',
                    'interestRate', 'basisForCalc', 'accruedCommisCurrency', 'accruedCommisRub',
                    'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
                    'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub'].each {
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
            def checkColumn9and10 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                    (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
            // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
            def checkColumn11and12 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                    (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
            if (checkColumn9and10 || checkColumn11and12) {
                logger.error('Поля в графе 9, 10, 11, 12 заполены неверно!')
                break
            }

            // 9. Проверка на уникальность поля «№ пп» (графа 1)
            if (index != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                break
            }
            index += 1
        }

        // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
        def totalRow = formData.getDataRow('total')
        if (totalRow.commisInAccountingRub > 0 && totalRow.accruedCommisRub >= totalRow.commisInAccountingRub) {
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
    def division = row.basisForCalc * (date2 - date1 + 1)
    if (division == 0) {
        logger.error('Деление на ноль. Возможно неправильно выбраны даты.')
        return 0
    }
    return round((row.base * row.interestRate) / (division), 2)
}

/**
 * Получить сумму столбца.
 */
def getSum(def form, def columnAlias) {
    if (form == null) {
        return 0
    }
    return summ(form, new ColumnRange(columnAlias, 0, form.dataRows.size() - 2))
}