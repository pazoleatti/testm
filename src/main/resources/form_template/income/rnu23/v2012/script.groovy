package form_template.income.rnu23.v2012

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Форма "(РНУ-23) Регистр налогового учёта доходов по выданным гарантиям"
 * formTemplateId=323
 *
 * @author rtimerbaev
 */

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

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        def cols = (isBalancePeriod() ? balanceEditableColumns : editableColumns)
        formDataService.addRow(formData, currentDataRow, cols, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationTotal(formData, logger, userInfo, ['total'])
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все атрибуты
@Field
def allColumns = ['number', 'contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction',
                  'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation', 'accrualAccountingStartDate',
                  'accrualAccountingEndDate', 'preAccrualsStartDate', 'preAccrualsEndDate', 'incomeCurrency',
                  'incomeRuble', 'accountingCurrency', 'accountingRuble', 'preChargeCurrency', 'preChargeRuble',
                  'taxPeriodCurrency', 'taxPeriodRuble']

// Редактируемые атрибуты (графа 2..12)
@Field
def editableColumns = ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction', 'rateOfTheBankOfRussia',
                       'interestRate', 'baseForCalculation', 'accrualAccountingStartDate', 'accrualAccountingEndDate',
                       'preAccrualsStartDate', 'preAccrualsEndDate']
@Field
def balanceEditableColumns = allColumns - 'number'

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 5, 3, 2)
@Field
def groupColumns = ['dateOfTransaction', 'contractDate', 'contract']

// Проверяемые на пустые значения атрибуты (графа 1..8, 13..20)
@Field
def nonEmptyColumns = ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction', 'rateOfTheBankOfRussia',
                       'interestRate', 'baseForCalculation', 'incomeCurrency', 'incomeRuble', 'accountingCurrency',
                       'accountingRuble', 'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 13..20)
@Field
def totalColumns = ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble', 'preChargeCurrency',
                       'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

// Дата окончания отчетного периода
@Field
def endDate = null

// Признак периода ввода остатков
@Field
def Boolean isBalancePeriod = null

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(
        def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
        boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // РНУ-23 предыдущего периода
    def totalRowOld
    if (formData.kind == FormDataKind.PRIMARY) {
        def prevDataRows = getPrevDataRows()
        totalRowOld = (prevDataRows ? getDataRow(prevDataRows, 'total') : null)
    }

    // Итоговая строка
    def totalRow = getDataRow(dataRows, 'total')
    // Очистка итогов
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(null, null)
    }

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // графа 1, 13..20
    if (formData.kind == FormDataKind.PRIMARY) {
        dataRows.eachWithIndex { row, i ->
            // графа 13
            row.incomeCurrency = calc13or15(row)
            // графа 14
            row.incomeRuble = calc14(row)
            // графа 15
            row.accountingCurrency = calc13or15(row)
            // графа 16
            row.accountingRuble = calc16(row)
            // графа 17
            row.preChargeCurrency = calc17(totalRowOld)
            // графа 18
            row.preChargeRuble = calc18(totalRowOld)
            // графа 19 (дата графа 11 и 12)
            row.taxPeriodCurrency = calc19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)
            // графа 20
            row.taxPeriodRuble = calc20(row)
        }
    }
    // Итого
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // алиасы графов для арифметической проверки (графа 13..20)
    def arithmeticCheckAlias = ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
                                'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // РНУ-23 предыдущего периода
    def prevTotalRow = null
    if (formData.kind == FormDataKind.PRIMARY) {
        def prevDataRows = getPrevDataRows()
        prevTotalRow = (prevDataRows == null || prevDataRows.isEmpty() ? null : getDataRow(prevDataRows, 'total'))
    }

    /** Дата начала отчетного периода. */
    def a = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time

    /** Дата окончания отчетного периода. */
    def b = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def tmp

    for (def row : dataRows) {
        if (row.getAlias() == 'total') {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
        if (a != null && b != null &&
                ((row.dateOfTransaction != null && (row.dateOfTransaction < a || b < row.dateOfTransaction)) ||
                        (row.accrualAccountingEndDate != null && (row.accrualAccountingEndDate < a || b < row.accrualAccountingEndDate)) ||
                        (row.preAccrualsEndDate != null && (row.preAccrualsEndDate < a || b < row.preAccrualsEndDate)))
        ) {
            loggerError(row, errorMsg + 'Дата совершения операции вне границ отчётного периода!')
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
            loggerError(row, errorMsg + 'Все суммы по операции нулевые!')
        }

        // 3. Проверка на сумму гарантии (графа 4)
        if (row.amountOfTheGuarantee != null && row.amountOfTheGuarantee == 0) {
            rowWarning(logger, row, errorMsg + 'Суммы гарантии равны нулю!')
        }

        // 4. Проверка задания расчётного периода (графа 9, 10, 11, 12)
        if (row.accrualAccountingStartDate > row.accrualAccountingEndDate ||
                row.preAccrualsStartDate > row.preAccrualsEndDate) {
            rowWarning(logger, row, errorMsg + 'Неправильно задан расчётный период!')
        }

        // 5. Проверка на корректность даты договора (графа 3)
        if (row.contractDate > b) {
            loggerError(row, errorMsg + 'Дата договора неверная!')
        }

        // 7. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 8. Проверка на заполнение поля «<Наименование поля>»
        // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
        def checkColumn9and10 = row.accrualAccountingStartDate != null && row.accrualAccountingEndDate != null &&
                (row.preAccrualsStartDate != null || row.preAccrualsEndDate != null)
        // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
        def checkColumn11and12 = (row.accrualAccountingStartDate != null || row.accrualAccountingEndDate != null) &&
                row.preAccrualsStartDate != null && row.preAccrualsEndDate != null
        if (checkColumn9and10 || checkColumn11and12) {
            loggerError(row, errorMsg + 'Поля в графе 9, 10, 11, 12 заполены неверно!')
        }

        if (formData.kind == FormDataKind.PRIMARY) {
            // 10. Арифметическая проверка графы 13..20
            needValue['incomeCurrency'] = calc13or15(row)
            needValue['incomeRuble'] = calc14(row)
            needValue['accountingCurrency'] = calc13or15(row)
            needValue['accountingRuble'] = calc16(row)
            needValue['preChargeCurrency'] = calc17(prevTotalRow)
            needValue['preChargeRuble'] = calc18(prevTotalRow)
            needValue['taxPeriodCurrency'] = calc19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)
            needValue['taxPeriodRuble'] = calc20(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())
        }
    }

    def totalRow = getDataRow(dataRows, 'total')
    // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 16, 14, 18)
    if (totalRow.incomeRuble != null && totalRow.preChargeRuble != null && totalRow.accountingRuble != null &&
            totalRow.incomeRuble + totalRow.preChargeRuble < totalRow.accountingRuble) {
        logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
    }

    // 18. Проверка итогового значений по всей форме (графа 13..20)
    checkTotalSum(dataRows, totalColumns, logger, true)
}

/*
 * Вспомогательные методы.
 */

/** Получить значение графы 13 и 15. */
def calc13or15(def row) {
    def date1
    def date2
    if (row.accrualAccountingStartDate != null && row.accrualAccountingEndDate != null) {
        // графа 9 и 10
        date1 = row.accrualAccountingStartDate
        date2 = row.accrualAccountingEndDate
    } else if (row.preAccrualsStartDate != null && row.preAccrualsEndDate != null) {
        // графа 11 и 12
        date1 = row.preAccrualsStartDate
        date2 = row.preAccrualsEndDate
    } else {
        return null
    }
    return calc19(row, date1, date2)
}

/**
 * Получить значение графы 19.
 *
 * @param row строка нф
 * @param date1 дата начала
 * @param date2 дата окончания
 */
def calc19(def row, def date1, def date2) {
    if (date1 == null || date2 == null) {
        return roundValue(0, 2)
    }
    if (row.baseForCalculation == null || row.amountOfTheGuarantee == null || row.interestRate == null) {
        return null
    }
    def division = row.baseForCalculation * (date2 - date1 + 1)
    if (division == 0) {
        return null
    }
    return roundValue((row.amountOfTheGuarantee * row.interestRate) / (division), 2)
}

def calc14(def row) {
    if (row.incomeCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return roundValue(row.incomeCurrency * row.rateOfTheBankOfRussia, 2)
}

def calc16(def row) {
    if (row.accountingCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return roundValue(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)
}

/**
 * Получить значение графы 17.
 *
 * @param totalRowOld итоговая строка рну 23 предыдущего отчетного периода
 */
def calc17(def totalRowOld) {
    def tmp = (totalRowOld != null && totalRowOld.taxPeriodCurrency != null ? totalRowOld.taxPeriodCurrency : 0)
    return roundValue(tmp, 2)
}

/**
 * Получить значение графы 18.
 *
 * @param totalRowOld итоговая строка рну 23 предыдущего отчетного периода
 */
def calc18(def totalRowOld) {
    return roundValue((totalRowOld != null && totalRowOld.taxPeriodRuble != null ? totalRowOld.taxPeriodRuble : 0), 2)
}

def calc20(def row) {
    if (row.rateOfTheBankOfRussia == null || row.taxPeriodCurrency == null) {
        return null
    }
    return roundValue(row.taxPeriodCurrency * row.rateOfTheBankOfRussia, 2)
}

/** Получить данные за предыдущий отчетный период. */
def getPrevDataRows() {
    def prevFormData = formDataService.getFormDataPrev(formData)
    if (prevFormData != null) {
        return formDataService.getDataRowHelper(prevFormData)?.allSaved
    }
    return null
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 20, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    def rows = []

    for (def row : xml.row) {
        rnuIndexRow++

        def rnuIndexCol
        def newRow = getNewRow()

        // графа 1
        rnuIndexCol = 1
        newRow.number = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 2
        rnuIndexCol = 2
        newRow.contract = row.cell[rnuIndexCol].text()

        // графа 3
        rnuIndexCol = 3
        newRow.contractDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 4
        rnuIndexCol = 4
        newRow.amountOfTheGuarantee = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 5
        rnuIndexCol = 5
        newRow.dateOfTransaction = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 6
        rnuIndexCol = 6
        newRow.rateOfTheBankOfRussia = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 7
        rnuIndexCol = 7
        newRow.interestRate = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 8
        rnuIndexCol = 8
        newRow.baseForCalculation = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 9
        rnuIndexCol = 9
        newRow.accrualAccountingStartDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 10
        rnuIndexCol = 10
        newRow.accrualAccountingEndDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 11
        rnuIndexCol = 11
        newRow.preAccrualsStartDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 12
        rnuIndexCol = 12
        newRow.preAccrualsEndDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 13
        rnuIndexCol = 13
        newRow.incomeCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 14
        rnuIndexCol = 14
        newRow.incomeRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 15
        rnuIndexCol = 15
        newRow.accountingCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 16
        rnuIndexCol = 16
        newRow.accountingRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 17
        rnuIndexCol = 17
        newRow.preChargeCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 18
        rnuIndexCol = 18
        newRow.preChargeRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 19
        rnuIndexCol = 19
        newRow.taxPeriodCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 20
        rnuIndexCol = 20
        newRow.taxPeriodRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }

    // Итоговая строка
    def totalRow = getDataRow(dataRows, 'total')
    // Очистка итогов
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(null, null)
    }
    calcTotalSum(rows, totalRow, totalColumns)
    rows.add(totalRow)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 13
        def rnuIndexCol = 13
        total.incomeCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 14
        rnuIndexCol = 14
        total.incomeRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 15
        rnuIndexCol = 15
        total.accountingCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 16
        rnuIndexCol = 16
        total.accountingRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 17
        rnuIndexCol = 17
        total.preChargeCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 18
        rnuIndexCol = 18
        total.preChargeRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 19
        rnuIndexCol = 19
        total.taxPeriodCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 20
        rnuIndexCol = 20
        total.taxPeriodRuble = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        def colIndexMap = ['incomeCurrency'   : 13, 'incomeRuble': 14, 'accountingCurrency': 15,
                           'accountingRuble'  : 16, 'preChargeCurrency': 17, 'preChargeRuble': 18,
                           'taxPeriodCurrency': 19, 'taxPeriodRuble': 20]
        for (def alias : totalColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = total[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    def cols = (isBalancePeriod() ? balanceEditableColumns : editableColumns)
    cols.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, getDataRow(dataRows, 'total'), null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 21
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFile = null

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // итоговая строка
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows
    def totalRow = getDataRow(templateRows, 'total')
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : tmpRow.getCell('number').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('contract').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('contractDate').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('amountOfTheGuarantee').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('dateOfTransaction').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('rateOfTheBankOfRussia').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('interestRate').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('baseForCalculation').column.name]),
            ([(headerRows[0][9]) : 'Расчётный период']),
            ([(headerRows[0][13]): 'Сумма в налоговом учёте']),
            ([(headerRows[0][15]): 'Сумма в бухгалтерском учёте']),
            ([(headerRows[0][17]): 'Сумма доначисления']),
            ([(headerRows[1][9]) : 'начисление/факт']),
            ([(headerRows[1][11]): 'доначисление']),
            ([(headerRows[1][13]): 'валюта']),
            ([(headerRows[1][14]): 'рубли']),
            ([(headerRows[1][15]): 'валюта']),
            ([(headerRows[1][16]): 'рубли']),
            ([(headerRows[1][17]): 'предыдущий период']),
            ([(headerRows[1][19]): 'отчётный период']),
            ([(headerRows[2][9]) : 'дата начала']),
            ([(headerRows[2][10]): 'дата окончания']),
            ([(headerRows[2][11]): 'дата начала']),
            ([(headerRows[2][12]): 'дата окончания']),
            ([(headerRows[2][17]): 'валюта']),
            ([(headerRows[2][18]): 'рубли']),
            ([(headerRows[2][19]): 'валюта']),
            ([(headerRows[2][20]): 'рубли']),
            ([(headerRows[3][0]) : '1'])
    ]
    (2..20).each {
        headerMapping.add(([(headerRows[3][it]): it.toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2
    def colIndex = 2
    newRow.contract = values[colIndex]

    // графа 3
    colIndex++
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex++
    newRow.amountOfTheGuarantee = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.dateOfTransaction = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6..8
    ['rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 9..12
    ['accrualAccountingStartDate', 'accrualAccountingEndDate', 'preAccrualsStartDate', 'preAccrualsEndDate'].each { alias ->
        colIndex++
        newRow[alias] = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 13..20
    ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble', 'preChargeCurrency',
     'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}