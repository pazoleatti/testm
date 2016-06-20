package form_template.deal.repo.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 3383 - Сделки РЕПО (8)
 *
 * formTemplateId = 3383
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
//    case FormDataEvent.CALCULATE:
//        calc()
//        logicCheck()
//        formDataService.saveCachedDataRows(formData, logger)
//        break
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
//        calc()
//        logicCheck()
//        formDataService.saveCachedDataRows(formData, logger)
//        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        //calc()
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
def editableColumns = ['jurName', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
                       'dealsMode', 'date1', 'date2', 'percentIncomeSum', 'percentConsumptionSum', 'priceFirstCurrency',
                       'currencyCode', 'courseCB', 'priceFirstRub', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'country', 'countryCode']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['jurName', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
                       'date1', 'date2', 'priceFirstCurrency', 'currencyCode', 'courseCB', 'priceFirstRub', 'transactionDate']

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

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        def contractDate = row.contractDate
        def transactionDate = row.transactionDate
        def transactionDeliveryDate = row.transactionDeliveryDate
        def percentIncomeSum = row.percentIncomeSum
        def percentConsumptionSum = row.percentConsumptionSum

        // Заполнение граф 13 и 14
        if (percentIncomeSum == null && percentConsumptionSum == null) {
            def msg1 = row.getCell('percentConsumptionSum').column.name
            def msg2 = row.getCell('percentIncomeSum').column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
        }
        if (percentIncomeSum != null && percentConsumptionSum != null) {
            def msg1 = row.getCell('percentConsumptionSum').column.name
            def msg2 = row.getCell('percentIncomeSum').column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg1» не может быть заполнена одновременно с графой «$msg2»!")
        }

        // Корректность даты (заключения) сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Корректность даты исполнения 1–ой части сделки
        def dt1 = row.date1
        if (dt1 != null) {
            if ( dt1 > dTo) {
                def msg = row.getCell('date1').column.name
                rowError(logger, row, "Строка $rowNum: Значение графы «$msg» не может быть больше даты окончания отчётного периода!")
            }
            if (dt1 < dFrom ) {
                def msg = row.getCell('date1').column.name
                rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg» не может быть меньше даты начала отчётного периода!")
            }
        }

        // Корректность даты совершения сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
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

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация о контрагенте - юридическом лице'
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
            ([(headerRows[0][0]) : 'Общая информация о контрагенте - юридическом лице']),
            ([(headerRows[0][5]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : tmpRow.getCell('rowNum').column.name]),
            ([(headerRows[1][1]) : tmpRow.getCell('jurName').column.name]),
            ([(headerRows[1][2]) : tmpRow.getCell('innKio').column.name]),
            ([(headerRows[1][3]) : tmpRow.getCell('country').column.name]),
            ([(headerRows[1][4]) : tmpRow.getCell('countryCode').column.name]),
            ([(headerRows[1][5]) : tmpRow.getCell('contractNum').column.name]),
            ([(headerRows[1][6]) : tmpRow.getCell('contractDate').column.name]),
            ([(headerRows[1][7]) : tmpRow.getCell('transactionNum').column.name]),
            ([(headerRows[1][8]) : tmpRow.getCell('transactionDeliveryDate').column.name]),
            ([(headerRows[1][9]) : tmpRow.getCell('dealsMode').column.name]),
            ([(headerRows[1][10]): tmpRow.getCell('date1').column.name]),
            ([(headerRows[1][11]): tmpRow.getCell('date2').column.name]),
            ([(headerRows[1][12]): tmpRow.getCell('percentIncomeSum').column.name]),
            ([(headerRows[1][13]): tmpRow.getCell('percentConsumptionSum').column.name]),
            ([(headerRows[1][14]): tmpRow.getCell('priceFirstCurrency').column.name]),
            ([(headerRows[1][15]): tmpRow.getCell('currencyCode').column.name]),
            ([(headerRows[1][16]): tmpRow.getCell('courseCB').column.name]),
            ([(headerRows[1][17]): tmpRow.getCell('priceFirstRub').column.name]),
            ([(headerRows[1][18]): tmpRow.getCell('transactionDate').column.name]),
            ([(headerRows[2][0]) : 'Гр. 1']),
            ([(headerRows[2][1]) : 'Гр. 2']),
            ([(headerRows[2][2]) : 'Гр. 3']),
            ([(headerRows[2][3]) : 'Гр. 4.1']),
            ([(headerRows[2][4]) : 'Гр. 4.2']),
            ([(headerRows[2][5]) : 'Гр. 5']),
            ([(headerRows[2][6]) : 'Гр. 6']),
            ([(headerRows[2][7]) : 'Гр. 7']),
            ([(headerRows[2][8]) : 'Гр. 8']),
            ([(headerRows[2][9]) : 'Гр. 9']),
            ([(headerRows[2][10]): 'Гр. 10.1']),
            ([(headerRows[2][11]): 'Гр. 10.2']),
            ([(headerRows[2][12]): 'Гр. 11.1']),
            ([(headerRows[2][13]): 'Гр. 11.2']),
            ([(headerRows[2][14]): 'Гр. 12']),
            ([(headerRows[2][15]): 'Гр. 13']),
            ([(headerRows[2][16]): 'Гр. 14']),
            ([(headerRows[2][17]): 'Гр. 15']),
            ([(headerRows[2][18]): 'Гр. 16'])
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
    newRow.jurName = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(9, newRow.jurName)
    colIndex++

    // графа 3
    if (map != null) {
        def expectedValue = (map.INN_KIO?.stringValue != null ? map.INN_KIO?.stringValue : "")
        formDataService.checkReferenceValue(9, values[colIndex], expectedValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.contractNum = values[colIndex]
    colIndex++

    // графа 6
    newRow.contractDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.transactionNum = values[colIndex]
    colIndex++

    // графа 8
    newRow.transactionDeliveryDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.dealsMode = getRecordIdImport(14, 'MODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10.1
    newRow.date1 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10.2
    newRow.date2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11.1
    newRow.percentIncomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11.2
    newRow.percentConsumptionSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.priceFirstCurrency = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14
    newRow.courseCB = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.priceFirstRub = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 16
    newRow.transactionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

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