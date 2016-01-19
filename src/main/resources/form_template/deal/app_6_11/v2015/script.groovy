package form_template.deal.app_6_11.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 6.11. Реализация и приобретение ценных бумаг для продажи
 *
 * formTemplateId=827
 *
 * @author Stanislav Yasinskiy
 */

// fix
// rowNumber        - № п/п
// dealDate         - Дата сделки (поставки)
// dealDate         - Дата сделки (поставки)
// name             - Наименование контрагента и ОПФ
// dealMode         - Режим переговорных сделок
// iksr             - ИНН/КИО контрагента
// countryName      - Страна местонахождения контрагента
// countryCode      - Код страны местонахождения контрагента
// currencySum      - Сумма сделки (с учетом НКД), в валюте расчетов.
// currencyCode     - Валюта расчетов по сделке
// courseCB         - Курс ЦБ РФ
// sum              - Сумма сделки (с учетом НКД), руб.
// docNumber        - Номер договора
// docDate          - Дата договора
// dealDoneDate     - Дата (заключения) сделки
// bondRegCode      - Регистрационный код ценной бумаги
// count            - Количество бумаг по сделке, шт.
// price            - Цена за 1 шт., руб.
// transactionType  - Тип сделки

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
def allColumns = ['fix', 'rowNumber', 'dealDate', 'name', 'dealMode', 'iksr', 'countryName', 'countryCode', 'currencySum',
                  'currencyCode', 'courseCB', 'sum', 'docNumber', 'docDate', 'dealDoneDate', 'bondRegCode', 'count',
                  'price', 'transactionType']

// Редактируемые атрибуты
@Field
def editableColumns = ['dealDate', 'name', 'dealMode', 'currencySum', 'currencyCode', 'courseCB', 'sum', 'docNumber', 'docDate',
                       'dealDoneDate', 'bondRegCode', 'count', 'transactionType']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'price']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['dealDate', 'name', 'currencySum', 'currencyCode', 'courseCB', 'sum', 'docNumber', 'docDate',
                       'dealDoneDate', 'bondRegCode', 'count', 'price', 'transactionType']

@Field
def totalColumns = ['currencySum', 'sum', 'count']

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

// Поиск записи в справочнике по значению (для расчетов)
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

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка корректности даты сделки
        if (row.dealDate && row.dealDoneDate && (row.dealDate < row.dealDoneDate || row.dealDate > getReportPeriodEndDate())) {
            def msg1 = getColumnName(row, 'dealDate')
            def msg2 = getColumnName(row, 'dealDoneDate')
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }

        // 3. Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 4. Проверка корректности даты заключения сделки
        if (row.docDate && row.dealDoneDate && (row.dealDoneDate < row.docDate || row.dealDoneDate > getReportPeriodEndDate())) {
            def msg1 = getColumnName(row, 'dealDoneDate')
            def msg2 = getColumnName(row, 'docDate')
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }

        // 5. Проверка количества бумаг
        if (row.count != null && row.count <= 0) {
            def msg = row.getCell('count').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше нуля!")
        }

        // 5. Проверка цены сделки
        if (row.price != null && row.count && row.price != round((BigDecimal) (row.sum / row.count), 2)) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('sum').column.name
            def msg3 = row.getCell('count').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно отношению значений граф «$msg2» и «$msg3»!")
        }

        // 6. Проверка положительной суммы
        if (row.currencySum && row.currencySum < 0) {
            def msg = row.getCell('currencySum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }
        if (row.sum && row.sum < 0) {
            def msg = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }
    }

    // 7. Проверка итоговых значений по фиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление подитогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        row.price = calc17(row)
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 8
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

def BigDecimal calc17(def row) {
    if (row.sum != null && row.count != null && row.count != 0) {
        return round((BigDecimal) (row.sum / row.count), 2)
    }
    return null
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
    int HEADER_ROW_COUNT = 6
    String TABLE_START_VALUE = 'Полная форма'
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
            ([(headerRows[0][0]): 'Полная форма']),
            ([(headerRows[1][0]): 'Данные для заполнения Уведомления о сделках с ценными бумагами, признаваемых Контролируемыми сделками (заполняются по итогам года)']),
            ([(headerRows[2][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[2][2]): 'Сокращенная форма']),
            ([(headerRows[3][2]): 'Данные для расчета сумм доходов по сделкам']),
            ([(headerRows[4][2]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[4][3]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[4][4]): getColumnName(tmpRow, 'dealMode')]),
            ([(headerRows[4][5]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[4][6]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[4][7]): getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[4][8]): getColumnName(tmpRow, 'currencySum')]),
            ([(headerRows[4][9]): getColumnName(tmpRow, 'currencyCode')]),
            ([(headerRows[4][10]): getColumnName(tmpRow, 'courseCB')]),
            ([(headerRows[4][11]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[4][12]): getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[4][13]): getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[4][15]): getColumnName(tmpRow, 'bondRegCode')]),
            ([(headerRows[4][16]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[4][17]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[4][18]): getColumnName(tmpRow, 'transactionType')]),
            ([(headerRows[5][0]): 'гр. 1']),
            ([(headerRows[5][2]): 'гр. 2']),
            ([(headerRows[5][3]): 'гр. 3']),
            ([(headerRows[5][4]): 'гр. 4']),
            ([(headerRows[5][5]): 'гр. 5']),
            ([(headerRows[5][6]): 'гр. 6.1']),
            ([(headerRows[5][7]): 'гр. 6.2']),
            ([(headerRows[5][8]): 'гр. 7.1']),
            ([(headerRows[5][9]): 'гр. 7.2']),
            ([(headerRows[5][10]): 'гр. 7.3']),
            ([(headerRows[5][11]): 'гр. 7.4']),
            ([(headerRows[5][12]): 'гр. 8']),
            ([(headerRows[5][13]): 'гр. 9']),
            ([(headerRows[5][14]): 'гр. 10']),
            ([(headerRows[5][15]): 'гр. 11']),
            ([(headerRows[5][16]): 'гр. 12']),
            ([(headerRows[5][17]): 'гр. 13']),
            ([(headerRows[5][18]): 'гр. 14'])
    ]
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
    def nameFromFile = values[3]

    def int colIndex = 2

    // графа 2
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    def recordId = getTcoRecordId(nameFromFile, values[5], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 3
    newRow.name = recordId
    colIndex++

    // графа 4
    newRow.dealMode = getRecordIdImport(14, 'MODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 5
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 6.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (map != null) {
            def expectedValues = [map.NAME?.stringValue, map.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(10, values[colIndex], expectedValues, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 6.2                               `
    if (map != null) {
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 8
    newRow.currencySum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    colIndex++

    // графа 10
    newRow.courseCB = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 13
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.bondRegCode = values[colIndex]
    colIndex++

    // графа 16
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 17
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 18
    newRow.transactionType = getRecordIdImport(16, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    return newRow
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 18
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
        def totalColumnsIndexMap = ['currencySum': 8, 'sum': 11, 'count': 16]

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
        def nameFromFile = pure(rowCells[3])
        def recordId = getTcoRecordId(nameFromFile, pure(rowCells[5]), iksrName, fileRowIndex, 3, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
        // графа 2
        newRow.dealDate = parseDate(pure(rowCells[2]), "dd.MM.yyyy", fileRowIndex, 2 + colOffset, logger, true)
        // графа 3
        newRow.name = recordId
        // графа 4
        newRow.dealMode = getRecordIdImport(14, 'MODE', pure(rowCells[4]), fileRowIndex, 4 + colOffset, false)
        // графа 9
        newRow.currencyCode = getRecordIdImport(15, 'CODE', pure(rowCells[9]), fileRowIndex, 9 + colOffset, false)
        // графа 10
        newRow.courseCB = parseNumber(pure(rowCells[10]), fileRowIndex, 10 + colOffset, logger, true)
        // графа 12
        newRow.docNumber = pure(rowCells[12])
        // графа 13
        newRow.docDate = parseDate(pure(rowCells[13]), "dd.MM.yyyy", fileRowIndex, 13 + colOffset, logger, true)
        // графа 14
        newRow.dealDoneDate = parseDate(pure(rowCells[14]), "dd.MM.yyyy", fileRowIndex, 14 + colOffset, logger, true)
        // графа 15
        newRow.bondRegCode = pure(rowCells[15])
        // графа 17
        newRow.price = parseNumber(pure(rowCells[17]), fileRowIndex, 17 + colOffset, logger, true)
        // графа 18
        newRow.transactionType = getRecordIdImport(16, 'CODE', pure(rowCells[18]), fileRowIndex, 18 + colOffset, false)
    }
    // графа 8
    newRow.currencySum = parseNumber(pure(rowCells[8]), fileRowIndex, 8 + colOffset, logger, true)
    // графа 11
    newRow.sum = parseNumber(pure(rowCells[11]), fileRowIndex, 11 + colOffset, logger, true)
    // графа 16
    newRow.count = parseNumber(pure(rowCells[16]), fileRowIndex, 16 + colOffset, logger, true)

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

    // графа 8
    colIndex = 8
    newRow.currencySum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 11
    colIndex = 11
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 16
    colIndex = 16
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

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