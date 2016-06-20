package form_template.etr.result_report.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Отчет о результатах исполнения мероприятий по контролю и управлению эффективной налоговой ставкой и налоговыми обязательствами
 * formTemplateId = 730
 *
 * @author Stanislav Yasinskiy
 *
 * графа 1 - rowNum             - № п/п
 * графа 2 - problemZone        - Проблемные зоны/зоны потенциального риска/факторы, оказывающие влияние
 * графа 3 - measures           - Предложенные мероприятия/меры
 * графа 4 - realizationDate    - Сроки реализации
 * графа 5 - performMark        - Отметка о выполнении
 * графа 6 - comments           - Примечание/Комментарии
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
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
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

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['problemZone', 'measures', 'realizationDate', 'performMark', 'comments']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = []

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['problemZone', 'measures', 'realizationDate', 'performMark', 'comments']

@Field
def needShowRegionMsg = true

@Field
def endDate = null

def getEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time
    }
    return endDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}


// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        def rowNum = row.getIndex()
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues)
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
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
 */
void checkHeaderXls(def headerRows) {
    checkHeaderSize(headerRows, 6, 2)

    def headerMapping = [
            ([(headerRows[0][0]): '№ п/п']),
            ([(headerRows[0][1]): 'Проблемные зоны/зоны потенциального риска/факторы, оказывающие влияние']),
            ([(headerRows[0][2]): 'Предложенные мероприятия/меры']),
            ([(headerRows[0][3]): 'Сроки реализации']),
            ([(headerRows[0][4]): 'Отметка о выполнении']),
            ([(headerRows[0][5]): 'Примечание/Комментарии'])
    ]
    (0..5).each {
        headerMapping.add(([(headerRows[1][it]): (it + 1).toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    def colIndex = 0
    newRow.rowNum = round(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true), 0)

    // графа 2
    colIndex++
    if (formDataDepartment.regionId) {
        def filter = "REGION_ID = $formDataDepartment.regionId and NAME like '${values[colIndex]}'"
        def provider = formDataService.getRefBookProvider(refBookFactory, 504, providerCache)
        def records = provider.getRecords(getEndDate(), null, filter, null)
        if (records) {
            newRow.problemZone = records.get(0)?.record_id?.value
        } else {
            def columnIndex = getXLSColumnName(colIndex + colOffset)
            def dateStr = getEndDate().format('dd.MM.yyyy')
            // наименование субъекта РФ для атрибута «Регион» подразделения формы
            def regionName = getRefBookValue(4L, formDataDepartment.regionId)?.NAME?.value
            logger.warn('Строка %s, столбец %s: В региональном справочнике «%s» не найдена запись, актуальная на дату %s: ' +
                    'поле «Код субъекта РФ» = «%s», поле «Проблемная зона» = «%s»',
                    fileRowIndex, columnIndex, getRefBook(504).name, dateStr, regionName, values[colIndex])
        }
    } else if (needShowRegionMsg) {
        needShowRegionMsg = false
        logger.warn('Невозможно выполнить поиск записи в справочнике «%s» для заполнения графы «%s» формы! ' +
                'Атрибут «Регион» подразделения текущей формы не заполнен (справочник «Подразделения»).',
                getRefBook(504).name, getColumnName(newRow, 'problemZone'))
    }

    colIndex++
    newRow.measures = values[colIndex]

    colIndex++
    newRow.realizationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    colIndex++
    newRow.performMark = getRecordIdImport(501, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    colIndex++
    newRow.comments = values[colIndex]

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
        updateIndexes(dataRows)
    }
}

// для хранения информации о справочниках
@Field
def refBooks = [:]

def getRefBook(def id) {
    if (refBooks[id] == null) {
        refBooks[id] = refBookFactory.get(id)
    }
    return refBooks[id]
}