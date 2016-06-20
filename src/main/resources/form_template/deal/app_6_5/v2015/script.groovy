package form_template.deal.app_6_5.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 6.5. Приобретение услуг по техническому обслуживанию нежилых помещений
 *
 * formTemplateId=814
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
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
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNumber', 'name', 'iksr', 'countryCode', 'sum', 'docNumber', 'docDate', 'country', 'region',
                  'city', 'settlement', 'count', 'price', 'cost', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'sum', 'docNumber', 'docDate', 'country', 'region', 'city', 'settlement', 'count',
                       'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryCode', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'sum', 'docDate', 'country', 'price', 'cost', 'dealDoneDate']

@Field
def totalColumns = ['sum', 'price', 'cost']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
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

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    def yearStartDate = Date.parse('dd.MM.yyyy', '01.01.' + getReportPeriodEndDate().format('yyyy'))
    def country643 = getRecordId(10, 'CODE', '643')

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка суммы расходов
        if (row.sum != null && row.sum < 0) {
            def msg = getColumnName(row, 'sum')
            logger.warn("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // Проверка заполнения кода региона
        if (row.country == country643 && !row.region) {
            def msg = getColumnName(row, 'region')
            logger.error("Строка $rowNum: Графа «$msg» должна быть заполнена, т.к. указанная страна местонахождения объекта недвижимости Россия!")
        }
        if (row.country && row.country != country643 && row.region) {
            def msg = getColumnName(row, 'region')
            logger.error("Строка $rowNum: Графа «$msg» не должна быть заполнена, т.к. указанная страна местонахождения объекта недвижимости не Россия!")
        }

        // Проверка заполнения населенного пункта
        if (!row.city && !row.settlement) {
            def msg1 = getColumnName(row, 'city')
            def msg2 = getColumnName(row, 'settlement')
            logger.error("Строка $rowNum: Должна быть заполнена графа «$msg1» или «$msg2»!")
        }

        // Проверка количества
        if (row.count != null && row.count <= 0) {
            def countName = getColumnName(row, 'count')
            logger.error("Строка $rowNum: Значение графы «$countName» должно быть больше «0»!")
        }

        // Проверка цены с учетом количества
        if (row.sum && row.count > 0 && row.price != round((BigDecimal) (row.sum / row.count), 2)) {
            def msg1 = getColumnName(row, 'price')
            def msg2 = getColumnName(row, 'sum')
            def msg3 = getColumnName(row, 'count')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно отношению графы «$msg2» к графе «$msg3»," +
                    " если графа «$msg3» заполнена (значением > 0)!")
        }

        // Проверка цены без учета количества
        if (row.sum != null && row.count == null && row.price != row.sum) {
            def msg1 = getColumnName(row, 'price')
            def msg2 = getColumnName(row, 'sum')
            def msg3 = getColumnName(row, 'count')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»," +
                    " если графа «$msg3» не заполнена!")
        }

        // Проверка стоимости
        if (row.sum != null && row.cost != row.sum) {
            def msg1 = getColumnName(row, 'cost')
            def msg2 = getColumnName(row, 'sum')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Проверка корректности даты совершения сделки
        checkDatePeriodExt(logger, row, 'dealDoneDate', 'docDate', yearStartDate, getReportPeriodEndDate(), true)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        row.price = calc13(row)
        row.cost = row.sum
    }
}

def BigDecimal calc13(def row) {
    if (row.count != null && row.count != 0) {
        return (row.sum ?: 0) / row.count
    } else {
        return row.sum
    }
    return null
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Общая информация'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0


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
        rowIndex++
        // простая строка
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
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): 'Общая информация']),
            ([(headerRows[0][4]): 'Сведения о сделке']),
            ([(headerRows[1][0]): tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[1][1]): tmpRow.getCell('name').column.name]),
            ([(headerRows[1][2]): tmpRow.getCell('iksr').column.name]),
            ([(headerRows[1][3]): tmpRow.getCell('countryCode').column.name]),
            ([(headerRows[1][4]): tmpRow.getCell('sum').column.name]),
            ([(headerRows[1][5]): tmpRow.getCell('docNumber').column.name]),
            ([(headerRows[1][6]): tmpRow.getCell('docDate').column.name]),
            ([(headerRows[1][7]): 'Адрес нахождения объекта недвижимости']),
            ([(headerRows[1][11]): tmpRow.getCell('count').column.name]),
            ([(headerRows[1][12]): tmpRow.getCell('price').column.name]),
            ([(headerRows[1][13]): tmpRow.getCell('cost').column.name]),
            ([(headerRows[1][14]): tmpRow.getCell('dealDoneDate').column.name]),
            ([(headerRows[2][7]): tmpRow.getCell('country').column.name]),
            ([(headerRows[2][8]): tmpRow.getCell('region').column.name]),
            ([(headerRows[2][9]): tmpRow.getCell('city').column.name]),
            ([(headerRows[2][10]): tmpRow.getCell('settlement').column.name])
    ]

    (0..14).each {
        headerMapping.add(([(headerRows[3][it]): 'гр. ' + (it+1)]))
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
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[1]

    def int colIndex = 1

    def recordId = getTcoRecordId(nameFromFile, values[2], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
        expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 6
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.country = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, true)
    colIndex++

    // графа 9
    newRow.region = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, true)
    colIndex++

    // графа 10
    newRow.city = values[colIndex]
    colIndex++

    // графа 11
    newRow.settlement = values[colIndex]
    colIndex++

    // графа 12
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

void importTransportData() {
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 15
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2, ..)
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try {
        // проверить первые строки тф - заголовок и пустая строка
        checkFirstRowsTF(reader, logger)

        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                break
            }
            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex, false)
            if (newRow) {
                newRows.add(newRow)
            }
        }
    } finally {
        reader.close()
    }

    // отображать ошибки переполнения разряда
    showMessages(newRows, logger)

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param isTotal признак итоговой строки
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, def isTotal) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    def int colOffset = 1

    if (!isTotal) {
        def String iksrName = getColumnName(newRow, 'iksr')
        def nameFromFile = pure(rowCells[2])
        def recordId = getTcoRecordId(nameFromFile, pure(rowCells[3]), iksrName, fileRowIndex, 2, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
        def map = getRefBookValue(520, recordId)

        // графа 2
        newRow.name = recordId
        // графа 3
        if (map != null) {
            def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
            expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
            formDataService.checkReferenceValue(pure(rowCells[3]), expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, 3 + colOffset, logger, false)
        }
        // графа 4
        if (map != null) {
            def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
            if (countryMap != null) {
                formDataService.checkReferenceValue(pure(rowCells[4]), [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, 4 + colOffset, logger, false)
            }
        }
        // графа 6
        newRow.docNumber = pure(rowCells[6])
        // графа 7
        newRow.docDate = parseDate(pure(rowCells[7]), "dd.MM.yyyy", fileRowIndex, 7 + colOffset, logger, true)
        // графа 8
        newRow.country = getRecordIdImport(10L, 'CODE', pure(rowCells[8]), fileRowIndex, 8 + colOffset, false)
        // графа 9
        newRow.region = getRecordIdImport(4L, 'CODE', pure(rowCells[9]), fileRowIndex, 9 + colOffset, false)
        // графа 10
        newRow.city = pure(rowCells[10])
        // графа 11
        newRow.settlement = pure(rowCells[11])
        // графа 15
        newRow.dealDoneDate = parseDate(pure(rowCells[15]), "dd.MM.yyyy", fileRowIndex, 15 + colOffset, logger, true)
    }
    // графа 5
    newRow.sum = parseNumber(pure(rowCells[5]), fileRowIndex, 5 + colOffset, logger, true)
    // графа 12
    newRow.count = parseNumber(pure(rowCells[12]), fileRowIndex, 12 + colOffset, logger, true)
    // графа 13
    newRow.price = parseNumber(pure(rowCells[13]), fileRowIndex, 13 + colOffset, logger, true)
    // графа 14
    newRow.cost = parseNumber(pure(rowCells[14]), fileRowIndex, 14 + colOffset, logger, true)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
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

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // имя периода и конечная дата корректны
        // устанавливаем дату для справочников
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}