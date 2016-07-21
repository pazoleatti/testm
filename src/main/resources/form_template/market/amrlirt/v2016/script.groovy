package form_template.market.amrlirt.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * Данные АМРЛИРТ.
 *
 * formTemplateId = 902
 * formType = 902
 */

// графа 1 - crmId
// графа 2 - name
// графа 3 - inn
// графа 4 - code
// графа 5 - lgd

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
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
}

@Field
def allColumns = ['crmId', 'name', 'inn', 'code', 'lgd']

@Field
def editableColumns = allColumns

@Field
def autoFillColumns = []

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = allColumns

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

@Field
def refBookCache = [:]

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def rowsMap = [:]
    for (def row : dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Неотрицательность графы
        if (row.lgd != null && row.lgd < 0) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно 0!",
                    row.getIndex(), getColumnName(row, 'lgd'))
        }

        // 3. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
        def key = getKey(row)
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        } else {
            logger.error("Строка %s: На форме уже существует строка со значением графы «%s» = «%s»!",
                    row.getIndex(), getColumnName(row, 'crmId'), row.crmId)
        }
        rowsMap[key].add(row)
    }
}

String getKey(def row) {
    return row.crmId?.trim()?.toLowerCase()
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

def getRecords(def inn) {
    def filter = 'INN = \'' + inn + '\' OR KIO = \'' + inn + '\''
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    if (recordCache[filter] == null) {
        recordCache.put(filter, provider.getRecords(getReportPeriodEndDate(), null, filter, null))
    }
    return recordCache[filter]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        // графа 2
        row.name = calc2(row)
    }
}

def calc2(def row) {
    if (row.inn == null) {
        return row.name
    }
    def records = getRecords(row.inn)
    if (records?.size() == 1) {
        row.name = records.get(0).NAME.value
    }
    return row.name
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 5
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'crmId')
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
        formDataService.getDataRowHelper(formData).save(rows)
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

    def colIndex = -1

    // графа 1..4
    ['crmId', 'name', 'inn', 'code'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 5
    colIndex++
    newRow.lgd = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // Заполнение общей информации о заемщике при загрузке из Excel
    fillDebtorInfo(newRow)

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

@Field
Map<Long, Map<String, RefBookValue>> records520

Map<Long, Map<String, RefBookValue>> getRecords520() {
    if (records520 == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, 520, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        records520 = provider.getRecordData(uniqueRecordIds)
    }
    return records520
}

void fillDebtorInfo(def newRow) {
    // Найти множество записей справочника «Участники ТЦО», периоды актуальности которых содержат определенную выше дату актуальности
    Map<Long, Map<String, RefBookValue>> records = getRecords520()
    String debtorNumber = newRow.inn
    if (debtorNumber == null || debtorNumber.isEmpty()) {
        return
    }
    def debtorRecords = records.values().findAll { def refBookValueMap ->
        debtorNumber.equalsIgnoreCase(refBookValueMap.INN.stringValue) || debtorNumber.equalsIgnoreCase(refBookValueMap.KIO.stringValue)
    }
    if (debtorRecords.size() > 1) {
        logger.warn("Найдено больше одной записи соотвествующей данным ИНН/КИО = " + debtorNumber)
        return
    }
    if (debtorRecords.size() == 0) {
        return
    }
    // else
    String fileDebtorName = newRow.debtorName
    newRow.debtorName = debtorRecords[0].NAME?.stringValue ?: ""
    if (! newRow.debtorName.equalsIgnoreCase(fileDebtorName)) {
        def refBook = refBookFactory.get(520)
        def refBookAttrName = refBook.getAttribute('INN').name + '/' + refBook.getAttribute('KIO').name
        if (fileDebtorName) {
            rowWarning(logger, newRow, String.format("На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "В файле указано другое наименование заемщика - «%s»!",
                    newRow.debtorName, refBookAttrName, newRow.inn, fileDebtorName))
        } else {
            rowWarning(logger, newRow, String.format("На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "Наименование заемщика в файле не заполнено!",
                    newRow.debtorName, refBookAttrName, newRow.inn))
        }
    }
}