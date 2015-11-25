package form_template.deal.securities.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 3381 - Приобретение и реализация ценных бумаг (долей в уставном капитале) (6)
 *
 * formTemplateId=3381
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
def editableColumns = ['fullNamePerson', 'dealSign', 'incomeSum', 'outcomeSum', 'docNumber', 'docDate', 'okeiCode',
        'count', 'dealDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryCode', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['fullNamePerson', 'docNumber', 'docDate', 'okeiCode', 'count', 'price', 'cost', 'dealDate']

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

        def docDateCell = row.getCell('docDate')
        def okeiCodeCell = row.getCell('okeiCode')

        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            rowError(logger, row, "Строка $rowNum: Графа «$msgIn» не может быть заполнена одновременно с графой «$msgOut»!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            rowError(logger, row, "Строка $rowNum: Графа «$msgOut» должна быть заполнена, если не заполнена графа «$msgIn»!")
        }

        // Проверка выбранной единицы измерения           
        def okei = getRefBookValue(12, row.okeiCode)?.CODE?.stringValue
        if (okei != '796' && okei != '744') {
            def msg = okeiCodeCell.column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg» может содержать только одно из значений: шт., процент!")
        }

        // Проверка цены
        def sumCell = row.incomeSum != null ? row.getCell('incomeSum') : row.getCell('outcomeSum')
        if (sumCell.value != null) {
            def countCell = row.getCell('count')
            def priceCell = row.getCell('price')
            if (okei == '796' && countCell.value != null && countCell.value != 0
                    && priceCell.value != (sumCell.value / countCell.value).setScale(0, RoundingMode.HALF_UP)) {
                def msg1 = priceCell.column.name
                def msg2 = sumCell.column.name
                def msg3 = countCell.column.name
                rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно отношению значений граф «$msg2» и «$msg3»!")
            } else if (okei == '744' && priceCell.value != sumCell.value) {
                def msg1 = priceCell.column.name
                def msg2 = sumCell.column.name
                rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
            }
        }

        // Проверка стоимости
        def costCell = row.getCell('cost')
        if (incomeSumCell.value != null && outcomeSumCell.value == null && costCell.value != incomeSumCell.value) {
            rowError(logger, row, "Строка $rowNum: Значение «${costCell.column.name}» должно быть равно значению «$msgIn»!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value != null && costCell.value != outcomeSumCell.value) {
            rowError(logger, row, "Строка $rowNum: Значение «${costCell.column.name}» должно быть равно значению «$msgOut»!")
        }

        // Корректность даты совершения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка количества
        if (row.count == 0) {
            def countName = getColumnName(row, 'count')
            rowError(logger, row, "Строка $rowNum: Графа «$countName» не может содержать значение 0.")
        }

        // Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
        }
        if (row.dealDate) {
            checkDateValid(logger, row, 'dealDate', row.dealDate, true)
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        // Расчет поля "Цена"
        def priceValue = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        if (priceValue != null) {
            def okei = getRefBookValue(12, row.okeiCode)?.CODE?.stringValue
            if (okei == '744') {
                row.price = priceValue
            } else if (okei == '796' && row.count != 0 && row.count != null) {
                row.price = priceValue / row.count
            } else {
                row.price = null
            }
        } else {
            row.price = null
        }
        // Расчет поля "Стоимость"
        row.cost = priceValue
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 14
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация'
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
            ([(headerRows[0][0]) : 'Общая информация']),
            ([(headerRows[0][5]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'fullNamePerson')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'inn')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'dealSign')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'incomeSum')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'outcomeSum')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][9]) : getColumnName(tmpRow, 'okeiCode')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'dealDate')])
    ]
    (0..13).each{
        headerMapping.add(([(headerRows[2][it]): 'гр. ' + (it + 1).toString()]))
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

    def int colIndex = 1

    // графа 2
    newRow.fullNamePerson = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(9, newRow.fullNamePerson)
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(9, values[colIndex], map.INN_KIO?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5
    newRow.dealSign = getRecordIdImport(36, 'SIGN', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 6
    newRow.incomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.outcomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 9
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.okeiCode = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

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