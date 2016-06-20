package form_template.income.explanation_outcome.v2016

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Таблица 2. Пояснение отклонений от ОФР в простом регистре налогового учёта «Расходы»
 * formTemplateId=852
 */

// графа 1 - rowNum
// графа 2 - code
// графа 3 - symbol
// графа 4 - sum
// графа 5 - explanation

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, (allColumns - editableColumns))
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger, userInfo)
        groupRows() // объединяет строки с одинаковыми кодами
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
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

@Field
def allColumns = ['rowNum', 'code', 'symbol', 'sum', 'explanation']

// Редактируемые атрибуты
@Field
def editableColumns = ['code', 'sum', 'explanation']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['code', 'sum']

@Field
def startDate = null

@Field
def endDate = null

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Получить новую строку с заданными стилями
def getNewRow() {
    def row = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    (allColumns - editableColumns).each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return row
}

void calc() {
    // расчетов нет
}

// объединяет строки с одинаковыми кодами
void groupRows() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def codeMap = [:]
    dataRows.each { row ->
        if (codeMap[row.code] == null) {
            codeMap[row.code] = []
        }
        codeMap[row.code].add(row)
    }
    def rows = []
    codeMap.each {codeId, codeRows ->
        def row = codeRows[0]
        row.sum = codeRows.sum { it.sum }
        row.explanation = null
        rows.add(row)
    }
    sortRows(refBookService, logger, rows, null, null, null)
    formDataService.getDataRowHelper(formData).setAllCached(rows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def codeMap = [:]
    for (def row : dataRows) {
        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка уникальности значения поля 2
        if (row.code) {
            def code = getRefBookValue(27, row.code)?.CODE.value
            if (codeMap[code] == null) {
                codeMap[code] = []
            }
            codeMap[code].add(row)
        }
    }

    // 2. Проверка уникальности значения поля 2
    codeMap.each { code, rows ->
        if (rows.size() > 1) {
            def rowNumbers = rows.collect { it.getIndex() }.join(', ')
            logger.error("Строка $rowNumbers: Значение графы «${getColumnName(rows[0], 'code')}» не уникально!")
        }
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 5
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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

    // формирование строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
            ([(headerRows[0][0]): tmpRow.getCell('rowNum').column.name]),
            ([(headerRows[0][1]): tmpRow.getCell('code').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('symbol').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('sum').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('explanation').column.name]),
    ]
    (1..5).each { index ->
        headerMapping.add(([(headerRows[1][index - 1]): index.toString()]))
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
    def colIndex

    // графа 2 - code       - атрибут 130 - CODE, справочник 27
    // графа 3 - symbol       - зависит от графы 2 - атрибут 134 - OPU, справочник 27

    // графа 2
    colIndex = 1
    def map = getRecordImport(27L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.code = map?.record_id?.value

    // графа 3
    colIndex = 2
    if (map != null) {
        formDataService.checkReferenceValue(27, values[colIndex], map.OPU?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex = 3
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex = 4
    newRow.explanation = values[colIndex]

    return newRow
}