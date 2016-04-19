package form_template.deal.bonds_trade.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 3384 - Реализация и приобретение ценных бумаг (9)
 *
 * formTemplateId=3384
 *
 * @author Dmitriy Levykin
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
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
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
def editableColumns = ['transactionDeliveryDate', 'contraName', 'transactionMode', 'transactionSumCurrency',
        'currency', 'courseCB', 'transactionSumRub', 'contractNum', 'contractDate', 'transactionDate',
        'bondRegCode', 'bondCount', 'transactionType']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'contraCountry', 'contraCountryCode', 'priceOne']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['transactionDeliveryDate', 'contraName', 'transactionSumCurrency',
        'currency', 'courseCB', 'transactionSumRub', 'contractNum', 'contractDate', 'transactionDate', 'bondRegCode',
        'bondCount', 'priceOne', 'transactionType']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xls') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls/xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
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

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        def transactionDate = row.transactionDate
        def transactionSumRub = row.transactionSumRub
        def bondCount = row.bondCount
        def priceOne = row.priceOne
        def contractDate = row.contractDate

        // Корректность даты заключения сделки
        if (transactionDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка цены сделки
        def res = null
        if (transactionSumRub != null && bondCount != null && bondCount != 0) {
            res = (transactionSumRub / bondCount).setScale(0, RoundingMode.HALF_UP)
        }
        if (transactionSumRub == null || bondCount == null || priceOne != res) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('transactionSumRub').column.name
            def msg3 = row.getCell('bondCount').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно отношению значений граф «$msg2» и «$msg3»!")
        }

        // Проверка диапазона дат
        if (row.contractDate) {
            checkDateValid(logger, row, 'contractDate', row.contractDate, true)
        }
        if (row.transactionDate) {
            checkDateValid(logger, row, 'transactionDate', row.transactionDate, true)
        }
        if (row.transactionDeliveryDate) {
            checkDateValid(logger, row, 'transactionDeliveryDate', row.transactionDeliveryDate, true)
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    for (row in dataRows) {
        // Расчет поля "Цена за 1 шт., руб."
        transactionSumRub = row.transactionSumRub
        bondCount = row.bondCount

        if (transactionSumRub != null && bondCount != null && bondCount != 0) {
            row.priceOne = transactionSumRub / bondCount
        }
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 18
    int HEADER_ROW_COUNT = 6
    String TABLE_START_VALUE = 'Полная форма'
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
            ([(headerRows[2][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[2][1]): 'Сокращенная форма']),
            ([(headerRows[3][1]): 'Данные для расчета сумм доходов по сделкам']),
            ([(headerRows[4][1]): getColumnName(tmpRow, 'transactionDeliveryDate')]),
            ([(headerRows[4][2]): getColumnName(tmpRow, 'contraName')]),
            ([(headerRows[4][3]): getColumnName(tmpRow, 'transactionMode')]),
            ([(headerRows[4][4]): getColumnName(tmpRow, 'innKio')]),
            ([(headerRows[4][5]): getColumnName(tmpRow, 'contraCountry')]),
            ([(headerRows[4][6]): getColumnName(tmpRow, 'contraCountryCode')]),
            ([(headerRows[4][7]): getColumnName(tmpRow, 'transactionSumCurrency')]),
            ([(headerRows[4][8]): getColumnName(tmpRow, 'currency')]),
            ([(headerRows[4][9]): getColumnName(tmpRow, 'courseCB')]),
            ([(headerRows[4][10]): getColumnName(tmpRow, 'transactionSumRub')]),
            ([(headerRows[4][11]): getColumnName(tmpRow, 'contractNum')]),
            ([(headerRows[4][12]): getColumnName(tmpRow, 'contractDate')]),
            ([(headerRows[4][13]): getColumnName(tmpRow, 'transactionDate')]),
            ([(headerRows[4][14]): getColumnName(tmpRow, 'bondRegCode')]),
            ([(headerRows[4][15]): getColumnName(tmpRow, 'bondCount')]),
            ([(headerRows[4][16]): getColumnName(tmpRow, 'priceOne')]),
            ([(headerRows[4][17]): getColumnName(tmpRow, 'transactionType')]),
            ([(headerRows[5][0]): 'гр. 1']),
            ([(headerRows[5][1]): 'гр. 2']),
            ([(headerRows[5][2]): 'гр. 3']),
            ([(headerRows[5][3]): 'гр. 4']),
            ([(headerRows[5][4]): 'гр. 5']),
            ([(headerRows[5][5]): 'гр. 6.1']),
            ([(headerRows[5][6]): 'гр. 6.2']),
            ([(headerRows[5][7]): 'гр. 7.1']),
            ([(headerRows[5][8]): 'гр. 7.2']),
            ([(headerRows[5][9]): 'гр. 7.3']),
            ([(headerRows[5][10]): 'гр. 7.4']),
            ([(headerRows[5][11]): 'гр. 8']),
            ([(headerRows[5][12]): 'гр. 9']),
            ([(headerRows[5][13]): 'гр. 10']),
            ([(headerRows[5][14]): 'гр. 11']),
            ([(headerRows[5][15]): 'гр. 12']),
            ([(headerRows[5][16]): 'гр. 13']),
            ([(headerRows[5][17]): 'гр. 14'])
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

    def int colIndex = 1

    // графа 2
    newRow.transactionDeliveryDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 3
    newRow.contraName = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(9, newRow.contraName)
    colIndex++

    // графа 4
    newRow.transactionMode = getRecordIdImport(14, 'MODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 5
    if (map != null) {
        def expectedValue = (map.INN_KIO?.stringValue != null ? map.INN_KIO?.stringValue : "")
        formDataService.checkReferenceValue(9, values[colIndex], expectedValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 6.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 6.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 8
    newRow.transactionSumCurrency = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.currency = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    colIndex++

    // графа 10
    newRow.courseCB = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.transactionSumRub = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.contractNum = values[colIndex]
    colIndex++

    // графа 13
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.transactionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.bondRegCode = values[colIndex]
    colIndex++

    // графа 16
    newRow.bondCount = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 17
    colIndex++

    // графа 18
    newRow.transactionType = getRecordIdImport(16, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

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