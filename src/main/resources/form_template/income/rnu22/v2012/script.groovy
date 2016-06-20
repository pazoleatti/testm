package form_template.income.rnu22.v2012

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования"
 * formTemplateId=322
 *
 * @version 59
 *
 * @author rtimerbaev
 *
 * графа 1  - rowNumber
 * графа 2  - contractNumber
 * графа 3  - contractData
 * графа 4  - base
 * графа 5  - transactionDate
 * графа 6  - course
 * графа 7  - interestRate
 * графа 8  - basisForCalc
 * графа 9  - calcPeriodAccountingBeginDate
 * графа 10 - calcPeriodAccountingEndDate
 * графа 11 - calcPeriodBeginDate
 * графа 12 - calcPeriodEndDate
 * графа 13 - accruedCommisCurrency
 * графа 14 - accruedCommisRub
 * графа 15 - commisInAccountingCurrency
 * графа 16 - commisInAccountingRub
 * графа 17 - accrualPrevCurrency
 * графа 18 - accrualPrevRub
 * графа 19 - reportPeriodCurrency
 * графа 20 - reportPeriodRub
 */

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def cols = isBalancePeriod() ? (allColumns - 'rowNumber') : editableColumns
        formDataService.addRow(formData, currentDataRow, cols, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
        formDataService.consolidationSimple(formData, logger, userInfo)
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

// Все аттрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'contractNumber', 'contractData', 'base', 'transactionDate',
                  'course', 'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate', 'calcPeriodAccountingEndDate',
                  'calcPeriodBeginDate', 'calcPeriodEndDate', 'accruedCommisCurrency', 'accruedCommisRub',
                  'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                  'reportPeriodCurrency', 'reportPeriodRub']

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ['accruedCommisCurrency', 'accruedCommisRub',
                    'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
                    'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']

// Редактируемые атрибуты
@Field
def editableColumns = ['contractNumber', 'contractData', 'base', 'transactionDate', 'course',
                       'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate',
                       'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate']

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ['contractNumber', 'contractData', 'base',
                       'transactionDate', 'course', 'interestRate', 'basisForCalc']

@Field
def sortColumns = ["transactionDate", "contractData", "contractNumber"]

@Field
def arithmeticCheckAlias = ['accruedCommisCurrency', 'accruedCommisRub',
                            'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                            'reportPeriodCurrency', 'reportPeriodRub']

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod = null

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def dataRowsOld = null
    if (formData.kind == FormDataKind.PRIMARY) {
        dataRowsOld = getPrevDataRows()
    }
    def tmp
    def dFrom = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time

    for (def DataRow row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
        if (row.transactionDate != null && dFrom > row.transactionDate
                || row.calcPeriodAccountingEndDate != null && row.calcPeriodAccountingEndDate > dTo
                || row.calcPeriodEndDate != null && row.calcPeriodEndDate > dTo) {
            loggerError(row, errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 2. Проверка на нулевые значения (графа 13..20)
        def allNull = true
        def allNullCheck = ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
                            'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                            'reportPeriodCurrency', 'reportPeriodRub']
        for (alias in allNullCheck) {
            tmp = row[alias]
            if (tmp != null && tmp != 0) {
                allNull = false
                break
            }
        }
        if (allNull) {
            loggerError(row, errorMsg + 'Все суммы по операции нулевые!')
        }

        // 3. Проверка на сумму платы (графа 4)
        if (row.base != null && !(row.base > 0)) {
            rowWarning(logger, row, errorMsg + 'Суммы платы равны 0!')
        }

        // 4. Проверка задания расчётного периода
        if (row.calcPeriodAccountingBeginDate > row.calcPeriodAccountingEndDate ||
                row.calcPeriodBeginDate > row.calcPeriodEndDate) {
            rowWarning(logger, row, errorMsg + 'Неправильно задан расчётный период!')
        }

        // 5. Проверка на корректность даты договора
        if (row.contractData > dTo) {
            loggerError(row, errorMsg + 'Дата договора неверная!')
        }

        // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 14, 16)
        if (row.accruedCommisRub < row.commisInAccountingRub) {
            rowWarning(logger, row, errorMsg + "Сумма данных бухгалтерского учёта превышает сумму начисленных платежей для документа ${row.contractNumber}")
        }

        // 8. Проверка на заполнение поля «<Наименование поля>»
        // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
        def checkColumn9and10 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
        // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
        def checkColumn11and12 = (row.calcPeriodAccountingBeginDate != null || row.calcPeriodAccountingEndDate != null) &&
                row.calcPeriodBeginDate != null && row.calcPeriodEndDate != null
        if (checkColumn9and10 || checkColumn11and12) {
            loggerError(row, errorMsg + 'Поля в графах 9, 10, 11, 12 заполены неверно!')
        }

        def date1 = row.calcPeriodBeginDate
        def date2 = row.calcPeriodEndDate
        if (date1 != null && date2 != null && row.basisForCalc != null && row.basisForCalc * (date2 - date1 + 1) == 0) {
            loggerError(row, errorMsg + "Деление на ноль. Возможно неправильно выбраны даты.")
        }

        if (formData.kind == FormDataKind.PRIMARY) {
            def rowPrev = null
            for (def rowOld in dataRowsOld) {
                if (rowOld.contractNumber == row.contractNumber) {
                    rowPrev = rowOld
                    break
                }
            }
            def values = [:]

            tmp = getGraph13_15(row)
            values.accruedCommisCurrency = tmp
            values.commisInAccountingCurrency = tmp
            values.accruedCommisRub = getGraph14(row)
            values.commisInAccountingRub = getGraph16(row)
            values.accrualPrevCurrency = rowPrev?.reportPeriodCurrency
            values.accrualPrevRub = rowPrev?.reportPeriodRub
            values.reportPeriodCurrency = getGraph19(row)
            values.reportPeriodRub = getGraph20(row)
            checkCalc(row, arithmeticCheckAlias, values, logger, !isBalancePeriod())
        }
    }
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def dataRowsOld = null
    if (formData.kind == FormDataKind.PRIMARY) {
        dataRowsOld = getPrevDataRows()
    }

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // графа 1, 13..20
    dataRows.each { DataRow row ->

        if (formData.kind == FormDataKind.PRIMARY) {
            def rowPrev = null
            for (def rowOld in dataRowsOld) {
                if (rowOld.contractNumber == row.contractNumber) {
                    rowPrev = rowOld
                    break
                }
            }

            // графа 13, 15
            def temp = getGraph13_15(row)
            row.accruedCommisCurrency = temp
            row.commisInAccountingCurrency = temp
            // графа 14
            row.accruedCommisRub = getGraph14(row)
            // графа 16
            row.commisInAccountingRub = getGraph16(row)
            // графа 17
            row.accrualPrevCurrency = rowPrev?.reportPeriodCurrency
            // графа 18
            row.accrualPrevRub = rowPrev?.reportPeriodRub
            // графа 19
            row.reportPeriodCurrency = getGraph19(row)
            // графа 20
            row.reportPeriodRub = getGraph20(row)
        }
    }

    // добавить строку "итого"
    def DataRow totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
}

BigDecimal getGraph13_15(def DataRow row) {
    def date1, date2
    if (row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null) {
        date1 = row.calcPeriodAccountingBeginDate
        date2 = row.calcPeriodAccountingEndDate
    } else {
        date1 = row.calcPeriodBeginDate
        date2 = row.calcPeriodEndDate
    }
    return calcFor13or15or19(row, date1, date2)
}

BigDecimal getGraph14(def DataRow row) {
    if (row.accruedCommisCurrency != null && row.course != null) {
        return (row.accruedCommisCurrency * row.course).setScale(2, RoundingMode.HALF_UP)
    }
    return null
}

BigDecimal getGraph16(def DataRow row) {
    if (row.commisInAccountingCurrency != null && row.course != null) {
        return (row.commisInAccountingCurrency * row.course).setScale(2, RoundingMode.HALF_UP)
    }
    return null
}

BigDecimal getGraph19(def DataRow row) {
    return calcFor13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)
}

BigDecimal calcFor13or15or19(def row, def date1, def date2) {
    def tmp = BigDecimal.ZERO
    if (date1 == null || date2 == null || row.basisForCalc == null ||
            row.base == null || row.interestRate == null) {
        return tmp.setScale(2, RoundingMode.HALF_UP)
    }
    def division = row.basisForCalc * (date2 - date1 + 1)
    if (division != 0) {
        tmp = (row.base * row.interestRate) / division
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph20(def DataRow row) {
    if (row.reportPeriodCurrency != null && row.course != null) {
        return (row.reportPeriodCurrency * row.course).setScale(2, RoundingMode.HALF_UP)
    }
    return null
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (isBalancePeriod() || isConsolidated) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    return (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 20, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def int rnuIndexRow = 2
    def int colOffset = 1

    def rows = []

    for (def row : xml.row) {
        rnuIndexRow++

        def rnuIndexCol
        def newRow = getNewRow()

        // графа 1
        rnuIndexCol = 1
        newRow.rowNumber = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 2
        rnuIndexCol = 2
        newRow.contractNumber = row.cell[rnuIndexCol].text()

        // графа 3
        rnuIndexCol = 3
        newRow.contractData = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 4
        rnuIndexCol = 4
        newRow.base = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 5
        rnuIndexCol = 5
        newRow.transactionDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 6
        rnuIndexCol = 6
        newRow.course = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 7
        rnuIndexCol = 7
        newRow.interestRate = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 8
        rnuIndexCol = 8
        newRow.basisForCalc = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 9
        rnuIndexCol = 9
        newRow.calcPeriodAccountingBeginDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 10
        rnuIndexCol = 10
        newRow.calcPeriodAccountingEndDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 11
        rnuIndexCol = 11
        newRow.calcPeriodBeginDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 12
        rnuIndexCol = 12
        newRow.calcPeriodEndDate = parseDate(row.cell[rnuIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 13
        rnuIndexCol = 13
        newRow.accruedCommisCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 14
        rnuIndexCol = 14
        newRow.accruedCommisRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 15
        rnuIndexCol = 15
        newRow.commisInAccountingCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 16
        rnuIndexCol = 16
        newRow.commisInAccountingRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 17
        rnuIndexCol = 17
        newRow.accrualPrevCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 18
        rnuIndexCol = 18
        newRow.accrualPrevRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 19
        rnuIndexCol = 19
        newRow.reportPeriodCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 20
        rnuIndexCol = 20
        newRow.reportPeriodRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }

    // Итоговая строка
    def totalRow = getTotalRow(rows)
    rows.add(totalRow)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 13
        def rnuIndexCol = 13
        total.accruedCommisCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 14
        rnuIndexCol = 14
        total.accruedCommisRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 15
        rnuIndexCol = 15
        total.commisInAccountingCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 16
        rnuIndexCol = 16
        total.commisInAccountingRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 17
        rnuIndexCol = 17
        total.accrualPrevCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 18
        rnuIndexCol = 18
        total.accrualPrevRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 19
        rnuIndexCol = 19
        total.reportPeriodCurrency = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        // графа 20
        rnuIndexCol = 20
        total.reportPeriodRub = parseNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, logger, true)

        def colIndexMap = ['accruedCommisCurrency': 13, 'accruedCommisRub': 14, 'commisInAccountingCurrency': 15,
                           'commisInAccountingRub': 16, 'accrualPrevCurrency': 17, 'accrualPrevRub': 18,
                           'reportPeriodCurrency' : 19, 'reportPeriodRub': 20]
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
    def cols = isBalancePeriod() ? (allColumns - 'rowNumber') : editableColumns
    cols.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

def getTotalRow(def dataRows) {
    def DataRow totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 11
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
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
    String TABLE_START_VALUE = tmpRow.getCell('rowNumber').column.name
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
    def totalRow = getTotalRow(rows)
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
            ([(headerRows[0][0]) : tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('contractNumber').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('contractData').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('base').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('transactionDate').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('course').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('interestRate').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('basisForCalc').column.name]),
            ([(headerRows[0][9]) : 'Расчётный период']),
            ([(headerRows[1][9]) : 'начисление']),
            ([(headerRows[1][11]): 'доначисление']),
            ([(headerRows[2][9]) : 'дата начала']),
            ([(headerRows[2][10]): 'дата окончания']),
            ([(headerRows[2][11]): 'дата начала']),
            ([(headerRows[2][12]): 'дата окончания']),
            ([(headerRows[0][13]): 'Сумма в налоговом учёте']),
            ([(headerRows[1][13]): 'валюта']),
            ([(headerRows[1][14]): 'рубли']),
            ([(headerRows[0][15]): 'Сумма в бухгалтерском учёте']),
            ([(headerRows[1][15]): 'валюта']),
            ([(headerRows[1][16]): 'рубли']),
            ([(headerRows[0][17]): 'Сумма доначисления']),
            ([(headerRows[1][17]): 'предыдущий квартал']),
            ([(headerRows[1][19]): 'отчётный квартал']),
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

    // графа 1
    def colIndex = 0
    newRow.rowNumber = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2
    colIndex = 2
    newRow.contractNumber = values[colIndex]

    // графа 3
    colIndex = 3
    newRow.contractData = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex = 4
    newRow.base = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 5
    newRow.transactionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6..8
    ['course', 'interestRate', 'basisForCalc'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 9..12
    ['calcPeriodAccountingBeginDate', 'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate'].each { alias ->
        colIndex++
        newRow[alias] = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 13..20
    ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency', 'commisInAccountingRub',
     'accrualPrevCurrency', 'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}