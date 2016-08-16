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

@Field
def recordCache = [:]

@Field
def providerCache = [:]

/**
 * Получить список записей из справочника "Участники ТЦО" (id = 520) по ИНН или КИО.
 *
 * @param value значение для поиска по совпадению
 */
def getRecords520(def value) {
    return getRecordsByValue(520L, value, ['INN', 'KIO'])
}

// мапа хранящая мапы с записями справочника (ключ "id справочника" -> мапа с записями, ключ "значение атрибута" -> список записией)
// например:
// [ id 520 : мапа с записям ]
//      мапа с записями = [ инн 1234567890 : список подходящих записей ]
@Field
def recordsMap = [:]

/**
 * Получить список записей из справочника атрибуты которых равны заданному значению.
 *
 * @param refBookId id справочника
 * @param value значение для поиска
 * @param attributesForSearch список атрибутов справочника по которым искать совпадения
 */
def getRecordsByValue(def refBookId, def value, def attributesForSearch) {
    if (recordsMap[refBookId] == null) {
        recordsMap[refBookId] = [:]
        // получить все записи справочника и засунуть в мапу
        def allRecords = getAllRecords(refBookId)?.values()
        allRecords.each { record ->
            attributesForSearch.each { attribute ->
                def tmpKey = getKeyValue(record[attribute]?.value)
                if (tmpKey) {
                    if (recordsMap[refBookId][tmpKey] == null) {
                        recordsMap[refBookId][tmpKey] = []
                    }
                    if (!recordsMap[refBookId][tmpKey].contains(record)) {
                        recordsMap[refBookId][tmpKey].add(record)
                    }
                }
            }
        }
    }
    def key = getKeyValue(value)
    return recordsMap[refBookId][key]
}

def getKeyValue(def value) {
    return value?.trim()?.toLowerCase()
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // графа 4
        row.debtorName = calc4(row)
    }
}

@Field
def exclusiveInns = ['9999999999', '9999999998']

def calc4(def row) {
    def tmp = row.debtorName
    if (!exclusiveInns.contains(row.inn)) {
        def records = getRecords520(row.inn)
        if (records != null && records.size() == 1) {
            tmp = records.get(0).NAME.value
        }
    }
    return tmp
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
    def debtorColIndex

    // графа 2
    def colIndex = 1
    newRow.depName = values[colIndex]
    // графа 3
    colIndex++
    newRow.inn = values[colIndex]
    // графа 4
    colIndex++
    newRow.debtorName = values[colIndex]
    debtorColIndex = colIndex + colOffset
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

    // Заполнение общей информации о заемщике при загрузке из Excel
    fillDebtorInfo(newRow, 'inn', 'debtorName', rowIndex, debtorColIndex)
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
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}

void fillDebtorInfo(def newRow, def numberAlias, def debtorAlias, def rowIndex, def debtorIndex) {
    String debtorNumber = newRow[numberAlias]
    String fileDebtorName = newRow[debtorAlias]
    if (debtorNumber == null || debtorNumber.isEmpty() || exclusiveInns.contains(debtorNumber)) {
        return
    }
    // ищем по ИНН и КИО
    def debtorRecords = getRecords520(debtorNumber)
    if (debtorRecords?.size() > 1) {
        logger.warn("Строка %s: Найдено больше одной записи соотвествующей данным ИНН/КИО = %s", rowIndex, debtorNumber)
        return
    }
    if (debtorRecords == null || debtorRecords.size() == 0) { // если в справочнике ТЦО записей нет
        return
    }
    // else
    // запись в справочнике ТЦО найдена, то берем данные из нее
    newRow.put(debtorAlias, debtorRecords[0].NAME?.stringValue ?: "")
    if (! newRow[debtorAlias].equalsIgnoreCase(fileDebtorName)) {
        def refBook = refBookFactory.get(520)
        def inn = debtorRecords[0].INN?.stringValue
        def kio = debtorRecords[0].KIO?.stringValue
        def attrCode
        if (debtorNumber.equalsIgnoreCase(inn)) {
            attrCode = 'INN'
        } else if (debtorNumber.equalsIgnoreCase(kio)) {
            attrCode = 'KIO'
        }
        def refBookAttrName = refBook.getAttribute(attrCode).name
        if (fileDebtorName) {
            logger.warn("Строка %s, столбец %s: На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "В файле указано другое наименование заемщика - «%s»!",
                    rowIndex, getXLSColumnName(debtorIndex), newRow[debtorAlias], refBookAttrName, newRow[numberAlias], fileDebtorName)
        } else {
            logger.warn("Строка %s, столбец %s: На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "Наименование заемщика в файле не заполнено!",
                    rowIndex, getXLSColumnName(debtorIndex), newRow[debtorAlias], refBookAttrName, newRow[numberAlias])
        }
    }
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