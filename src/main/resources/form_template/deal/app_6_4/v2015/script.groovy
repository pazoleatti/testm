package form_template.deal.app_6_4.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 6.4. Оказание банковских услуг
 *
 * formTemplateId=813
 *
 * @author Stanislav Yasinskiy
 */
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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'countryCode', 'serviceName', 'sum', 'docNumber',
                  'docDate', 'price', 'cost', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'serviceName', 'sum', 'docNumber', 'docDate', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'serviceName', 'sum', 'docDate', 'price', 'cost', 'dealDoneDate']

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

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка суммы доходов
        if (row.sum != null && row.sum < 0) {
            def msg = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка цены
        if (row.sum != null && row.price != row.sum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Проверка стоимости
        if (row.sum != null && row.cost != row.sum) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // Проверка корректности даты совершения сделки
        checkDatePeriod(logger, row, 'dealDoneDate', 'docDate', getReportPeriodEndDate(), true)
    }

    // Проверка итоговых значений пофиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Удаление итогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        row.price = row.sum
        row.cost = row.sum
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 6
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}


// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 13
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация о клиенте - юридическом лице'
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
    def totalRowFromFile = null

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
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

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

    // сравнение итогов
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
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
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): 'Общая информация о клиенте - юридическом лице']),
            ([(headerRows[0][6]): 'Сведения о сделке']),
            ([(headerRows[1][1]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][6]): getColumnName(tmpRow, 'serviceName')]),
            ([(headerRows[1][7]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][1]): 'гр. 1']),
            ([(headerRows[2][2]): 'гр. 2']),
            ([(headerRows[2][3]): 'гр. 3']),
            ([(headerRows[2][4]): 'гр. 4.1']),
            ([(headerRows[2][5]): 'гр. 4.2'])
    ]
    (6..12).each {
        headerMapping.add(([(headerRows[2][it]): 'гр. ' + (it-1)]))
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
    def nameFromFile = values[2]

    def int colIndex = 2

    def recordId = getTcoRecordId(nameFromFile,  values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (map != null) {
            def expectedValues = [map.NAME?.stringValue, map.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(10, values[colIndex], expectedValues, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.serviceName = getRecordIdImport(13, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 6
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 8
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 12
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2, ..)
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
    def totalTF = null      // итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (rowCells == null || !isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex, true)
                }
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

    // итоговая строка
    def totalRow = calcTotalRow(newRows)
    newRows.add(totalRow)

    // отображать ошибки переполнения разряда
    showMessages(newRows, logger)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['sum': 7, 'price': 10, 'cost': 11]

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = totalTF[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

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

    def int colOffset = 1

    if (!isTotal) {
        def String iksrName = getColumnName(newRow, 'iksr')
        def nameFromFile = pure(rowCells[2])
        def recordId = getTcoRecordId(nameFromFile,  pure(rowCells[3]), iksrName, fileRowIndex, 2, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)

        // графа 2
        newRow.name = recordId
        // графа 5
        newRow.serviceName = getRecordIdImport(13, 'CODE', pure(rowCells[6]), fileRowIndex, 6 + colOffset, false)
        // графа 7
        newRow.docNumber = pure(rowCells[8])
        // графа 8
        newRow.docDate = parseDate(pure(rowCells[9]), "dd.MM.yyyy", fileRowIndex, 9 + colOffset, logger, true)
        // графа 11
        newRow.dealDoneDate = parseDate(pure(rowCells[12]), "dd.MM.yyyy", fileRowIndex, 12 + colOffset, logger, true)
    }
    // графа 6
    newRow.sum = parseNumber(pure(rowCells[7]), fileRowIndex, 7 + colOffset, logger, true)
    // графа 9
    newRow.price = parseNumber(pure(rowCells[10]), fileRowIndex, 10 + colOffset, logger, true)
    // графа 10
    newRow.cost = parseNumber(pure(rowCells[11]), fileRowIndex, 11 + colOffset, logger, true)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

/**
 * Получить итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 7
    def colIndex = 7
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 10
    colIndex = 10
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 11
    colIndex = 11
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, dataRows.find { it.getAlias() == 'total' }, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}