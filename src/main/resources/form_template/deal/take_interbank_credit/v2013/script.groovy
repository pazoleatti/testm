package form_template.deal.take_interbank_credit.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 2402 - Привлечение средств на межбанковском рынке (23)
 *
 * formTemplateId=2402
 *
 * @author LHaziev
 */

// 1.  rowNumber        № п/п
// 2.  fullNamePerson   Полное наименование юридического лица с указанием ОПФ
// 3.  inn              ИНН/КИО
// 4.  countryName      Наименование страны регистрации
// 5.  countryCode      Код страны регистрации по классификатору ОКСМ
// 6.  docNum           Номер договора
// 7.  docDate          Дата договора
// 8.  dealNumber       Номер сделки
// 9.  dealDate         Дата заключения сделки
// 10. count            Количество
// 11. sum              Сумма расходов Банка по данным бухгалтерского учета, руб.
// 12. price            Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// 13. total            Итого стоимость без учета НДС, акцизов и пошлины, руб.
// 14. dealDoneDate     Дата совершения сделки

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
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
            formDataService.saveCachedDataRows(formData, logger)
        }
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
def editableColumns = ['fullNamePerson', 'docNum', 'docDate', 'dealNumber', 'dealDate', 'sum', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryName', 'countryCode', 'count', 'price', 'total']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['fullNamePerson', 'docNum', 'docDate', 'dealNumber', 'dealDate', 'count', 'sum', 'price',
        'total', 'dealDoneDate']

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

        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def totalCell = row.getCell('total')
        def msgSum = sumCell.column.name
        def docDateCell = row.getCell('docDate')            // графа 7
        def dealDateCell = row.getCell('dealDate')          // графа 9
        def dealDoneDateCell = row.getCell('dealDoneDate')  // графа 14

        // Проверка цены
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Проверка стоимости
        if (totalCell.value != sumCell.value) {
            def msg = totalCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Корректность даты заключения сделки относительно даты договора
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Корректность даты совершения сделки относительно даты заключения сделки
        if (dealDateCell.value > dealDoneDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка количества
        if (row.count == 0) {
            def countName = getColumnName(row, 'count')
            rowError(logger, row, "Строка $rowNum: Значение графы «$countName» не может содержать значение 0.")
        }

        // Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
        }
        if (row.dealDate) {
            checkDateValid(logger, row, 'dealDate', row.dealDate, true)
        }
        if (row.dealDoneDate) {
            checkDateValid(logger, row, 'dealDoneDate', row.dealDoneDate, true)
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
        // В поле "Количество" подставляется значение «1»
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Стоимость"
        row.total = row.sum
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 14
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общие сведения о контрагенте - юридическом лице'
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
            ([(headerRows[0][0]) : 'Общие сведения о контрагенте - юридическом лице']),
            ([(headerRows[0][5]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[1][1]) : tmpRow.getCell('fullNamePerson').column.name]),
            ([(headerRows[1][2]) : tmpRow.getCell('inn').column.name]),
            ([(headerRows[1][3]) : tmpRow.getCell('countryName').column.name]),
            ([(headerRows[1][4]) : tmpRow.getCell('countryCode').column.name]),
            ([(headerRows[1][5]) : tmpRow.getCell('docNum').column.name]),
            ([(headerRows[1][6]) : tmpRow.getCell('docDate').column.name]),
            ([(headerRows[1][7]) : tmpRow.getCell('dealNumber').column.name]),
            ([(headerRows[1][8]) : tmpRow.getCell('dealDate').column.name]),
            ([(headerRows[1][9]) : tmpRow.getCell('count').column.name]),
            ([(headerRows[1][10]): tmpRow.getCell('sum').column.name]),
            ([(headerRows[1][11]): tmpRow.getCell('price').column.name]),
            ([(headerRows[1][12]): tmpRow.getCell('total').column.name]),
            ([(headerRows[1][13]): tmpRow.getCell('dealDoneDate').column.name]),
            ([(headerRows[2][0]) : 'гр. 1']),
            ([(headerRows[2][1]) : 'гр. 2']),
            ([(headerRows[2][2]) : 'гр. 3']),
            ([(headerRows[2][3]) : 'гр. 4.1']),
            ([(headerRows[2][4]) : 'гр. 4.2'])
    ]
    (5..13).each{
        headerMapping.add(([(headerRows[2][it]): 'гр. ' + it.toString()]))
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
    newRow.fullNamePerson = getRecordIdImport(9, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset)
    def map = getRefBookValue(9, newRow.fullNamePerson)
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
    newRow.docNum = values[colIndex]
    colIndex++

    // графа 6
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.dealNumber = values[colIndex]
    colIndex++

    // графа 8
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    colIndex++

    // графа 10
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    colIndex++

    // графа 12
    colIndex++

    // графа 13
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

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