package form_template.market.market_7_129.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 7.129 (Ежемесячный) Кредитные договоры в CRM
 * formTemplateId = 905
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNum             - № п/п
 * графа 2  - depName            - Наименование подразделения
 * графа 3  - inn                - ИНН заемщика
 * графа 4  - debtorName         - Наименование заемщика
 * графа 5  - docNum             - № кредитного договора
 * графа 6  - docDate            - Дата кредитного договора
 * графа 7  - creditDate         - Дата выдачи кредита
 * графа 8  - productId          - ID продукта CRM
 * графа 9  - clientId           - ID клиента CRM
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
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
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['rowNum', 'depName', 'inn', 'debtorName', 'docNum', 'docDate', 'creditDate', 'productId', 'clientId']

// Редактируемые атрибуты
@Field
def editableColumns = ['depName', 'inn', 'debtorName', 'docNum', 'docDate', 'creditDate', 'productId', 'clientId']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = []

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['depName', 'inn', 'debtorName', 'docNum', 'docDate']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // групируем по графам 3, 5, 6
    def rowsMap = [:]
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        // 2. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
        // группируем
        def key = getKey(row)
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        }
        rowsMap[key].add(row)
        // 3. Проверка даты выдачи кредита
        if (row.docDate != null && row.creditDate != null && (row.creditDate < row.docDate)) {
            logger.warn("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                row.getIndex(), getColumnName(row, 'creditDate'), getColumnName(row, 'docDate'))
        }
    }
    // 2. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
    rowsMap.each { def key, rows ->
        def size = rows.size()
        // пропускаем первую строку
        for (int i = 1; i < size; i++) {
            def row = rows[i]
            logger.error("Строка %s: На форме уже существует строка со значениями граф «%s» = «%s», «%s» = «%s», «%s» = «%s»!",
                row.getIndex(), getColumnName(row, 'inn'), row.inn, getColumnName(row, 'docNum'), row.docNum, getColumnName(row, 'docDate'), row.docDate?.format('dd.MM.yyyy') ?: '')
        }
    }
}

String getKey(def row) {
    return (row.inn?.trim() + "#" + row.docNum?.trim() + "#" + row.docDate?.format('dd.MM.yyyy')).toLowerCase()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 9
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping =[[:]]
    def index = 0
    allColumns.each { alias ->
        headerMapping.add(([(headerRows[0][index]): headers[0][alias]]))
        headerMapping.add(([(headerRows[1][index]): (index + 1).toString()]))
        index++
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
    def colIndex = 1
    newRow.depName = values[colIndex]
    // графа 3
    colIndex++
    newRow.inn = values[colIndex]
    // графа 4
    colIndex++
    newRow.debtorName = values[colIndex]
    // графа 5
    colIndex++
    newRow.docNum = values[colIndex]
    // графа 6
    colIndex++
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 7
    colIndex++
    newRow.creditDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 8
    colIndex++
    newRow.productId = values[colIndex]
    // графа 9
    colIndex++
    newRow.clientId = values[colIndex]

    return newRow
}

def getNewRow() {
    def newRow = formData.createStoreMessagingDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}