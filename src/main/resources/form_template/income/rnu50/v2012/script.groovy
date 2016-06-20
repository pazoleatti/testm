package form_template.income.rnu50.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-50) Регистр налогового учёта «ведомость понесённых убытков от реализации амортизируемого имущества»"
 * formTypeId=365
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа    - fix
// графа 2  - rnu49rowNumber
// графа 3  - invNumber
// графа 4  - lossReportPeriod
// графа 5  - lossTaxPeriod

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkSourceAccepted()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        checkSourceAccepted()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        checkSourceAccepted()
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
}

// Все атрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['rnu49rowNumber', 'invNumber']

// Проверяемые на пустые значения атрибуты (графа 1..5)
@Field
def nonEmptyColumns = ['rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 5)
@Field
def totalColumns = ['lossReportPeriod', 'lossTaxPeriod']

// Дата начала отчетного периода
@Field
def reportPeriodStartDate = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

@Field
def sourceFormData = null

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (!isConsolidated && formDataEvent != FormDataEvent.IMPORT) {
        // удалить все строки
        dataRows.clear()
        dataRows = getCalcDataRows()
    } else {
        // удалить строку "итого"
        deleteAllAliased(dataRows)
    }

    // итоги
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей (графа 1..5)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Проверка на нулевые значения
        if (row.lossReportPeriod == 0 && row.lossTaxPeriod == 0) {
            rowError(logger, row, errorMsg + "Все суммы по операции нулевые!")
        }

        // 4. Проверка формата номера записи в РНУ-49 (графа 2)
        if (!row.rnu49rowNumber.matches('\\w{2}-\\w{6}')) {
            rowError(logger, row, errorMsg + "Неправильно указан номер записи в РНУ-49 (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!")
        }
    }

    // 8. Проверка итоговых значений формы	Заполняется автоматически
    checkTotalSum(dataRows, totalColumns, logger, true)

    if (isConsolidated) {
        return
    }

    // 5, 6, 7. Проверка соответствия данным формы РНУ-49. Арифметическая проверка графы 1..5
    def currentRows = dataRows.findAll { row -> row.getAlias() == null }
    def tmpRows = getCalcDataRows()
    if ((!currentRows && tmpRows) || (currentRows && !tmpRows)) {
        logger.error('Значения не соответствуют данным РНУ-49')
    } else if (currentRows && tmpRows) {
        def arithmeticCheckAlias = nonEmptyColumns - 'rowNumber'
        def errorRows = []
        for (def row : currentRows) {
            def tmpRow = tmpRows.find { it.invNumber == row.invNumber }
            if (tmpRow == null) {
                errorRows.add(row.getIndex())
                continue
            }
            def msg = []
            arithmeticCheckAlias.each { alias ->
                def value1 = row.getCell(alias).value
                def value2 = tmpRow.getCell(alias).value
                if (value1 != value2) {
                    msg.add('«' + getColumnName(row, alias) + '»')
                }
            }
            if (!msg.isEmpty()) {
                def columns = msg.join(', ')
                def index = row.getIndex()
                rowError(logger, row, "Строка $index: Неверное значение граф: $columns")
            }
            tmpRows.remove(tmpRow)
        }
        if (!errorRows.isEmpty()) {
            def indexes = errorRows.join(', ')
            logger.error("Значения не соответствуют данным РНУ-49 в строках: $indexes")
        }
        if (!tmpRows.isEmpty()) {
            logger.error('Значения не соответствуют данным РНУ-49. Необходимо рассчитать данные')
        }
    }
}

// Выполняется при расчете. Получение данных из рну 49
def getCalcDataRows() {
    def dataRows = []
    def dataRows49 = getDataRowsFromSource()
    if (dataRows49 == null) {
        return dataRows
    }

    def start = getStartDate()
    def end = getEndDate()

    for (def row49 : dataRows49) {
        if (row49.getAlias() == null && row49.usefullLifeEnd != null &&
                row49.monthsLoss != null && row49.expensesSum != null) {
            def newRow = formData.createDataRow()

            // графа 3
            newRow.invNumber = calc3(row49)
            // графа 2
            newRow.rnu49rowNumber = calc2(row49, newRow)
            // графа 4
            newRow.lossReportPeriod = calc4(row49, start, end)
            // графа 5
            newRow.lossTaxPeriod = calc5(row49, start, end)

            dataRows.add(newRow)
        }
    }
    return dataRows
}

/** Получить данные формы РНУ-49 (id = 312) */
def getFormDataSource() {
    if (sourceFormData == null) {
        sourceFormData = formDataService.getLast(312, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    }
    return sourceFormData
}

void checkSourceAccepted() {
    if (!isConsolidated) {
        formDataService.checkFormExistAndAccepted(312, FormDataKind.PRIMARY, formData.departmentId,
                formData.reportPeriodId, false, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

/** Получить строки из нф РНУ-32.1. */
def getDataRowsFromSource() {
    def formDataSource = getFormDataSource()
    if (formDataSource != null) {
        return formDataService.getDataRowHelper(formDataSource)?.allSaved
    }
    return null
}

def getStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)?.time
    }
    return reportPeriodStartDate
}

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

def calc3(def row49) {
    return row49.invNumber
}

def calc2(def row49, def row) {
    return (row.invNumber ? row49.firstRecordNumber : null)
}

def calc4(def row49, def startDate, def endDate) {
    def result = null
    def column3 = row49.operationDate
    def column18 = row49.usefullLifeEnd
    def column20 = row49.expensesSum

    if (startDate <= column3 && column3 <= endDate) {
        if (column18 > column3) {
            result = column20 * (endDate[Calendar.MONTH] - column3[Calendar.MONTH])
        } else {
            result = column20
        }
    }
    return result?.setScale(2, RoundingMode.HALF_UP)
}

def calc5(def row49, def startDate, def endDate) {
    def result = null
    def column18 = row49.usefullLifeEnd
    def column20 = row49.expensesSum

    if (column18 < startDate) {
        result = column20 * 3
    } else if (startDate <= column18 && column18 <= endDate) {
        result = column20 * (endDate[Calendar.MONTH] - column18[Calendar.MONTH])
    }
    return result?.setScale(2, RoundingMode.HALF_UP)
}

def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.rnu49rowNumber = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 5, 1)
    addTransportData(xml)

    // TODO (Ramil Timerbaev) возможно надо поменять на общее сообщение TRANSPORT_FILE_SUM_ERROR
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRows = formDataService.getDataRowHelper(formData).allCached
        checkTotalSum(dataRows, totalColumns, logger, false)
    }
}

void addTransportData(def xml) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)

        def xmlIndexCol = 0

        // графа 1
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.rnu49rowNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.invNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 4
        newRow.lossReportPeriod = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 5
        newRow.lossTaxPeriod = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)


        rows.add(newRow)
    }

    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.rnu49rowNumber = 'Итого'
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    rows.add(totalRow)

    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2

        def row = xml.rowTotal[0]

        // графа 4
        totalRow.lossReportPeriod = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)
        // графа 5
        totalRow.lossTaxPeriod = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)

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

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 6
    int HEADER_ROW_COUNT = 3
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
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // получить строки из шаблона
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
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('rnu49rowNumber').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('invNumber').column.name]),
            ([(headerRows[0][4]): 'Убыток, приходящийся на отчётный период']),
            ([(headerRows[1][4]): 'от реализации в отчётном налоговом периоде']),
            ([(headerRows[1][5]): 'от реализации в предыдущих налоговых периодах']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..5).each {
        headerMapping.add(([(headerRows[2][it]): it.toString()]))
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
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2
    def colIndex = 2
    newRow.rnu49rowNumber = values[colIndex]

    // графа 3
    colIndex++
    newRow.invNumber = values[colIndex]

    // графа 4
    colIndex++
    newRow.lossReportPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.lossTaxPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}