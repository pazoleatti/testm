package form_template.deal.bonds_trade.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 2384 - Реализация и приобретение ценных бумаг (9)
 *
 * formTemplateId=2384
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
        formDataService.consolidationSimple(formData, logger)
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
        def courseCB = row.courseCB
        def transactionSumCurrency = row.transactionSumCurrency
        def contractDate = row.contractDate

        // Проверка конверсии
        if (courseCB == null || transactionSumCurrency == null || transactionSumRub != (courseCB * transactionSumCurrency).setScale(0, RoundingMode.HALF_UP)) {
            def msg1 = row.getCell('transactionSumRub').column.name
            def msg2 = row.getCell('transactionSumCurrency').column.name
            def msg3 = row.getCell('courseCB').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно произведению значений графы «$msg2» и графы «$msg3»!")
        }

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

    sortFormDataRows(false)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML('Данные для расчета сумм доходов по сделкам', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 3)

    def headerMapping = [
            (xml.row[1].cell[0]): getColumnName(tmpRow, 'transactionDeliveryDate'),
            (xml.row[1].cell[1]): getColumnName(tmpRow, 'contraName'),
            (xml.row[1].cell[2]): getColumnName(tmpRow, 'transactionMode'),
            (xml.row[1].cell[3]): getColumnName(tmpRow, 'innKio'),
            (xml.row[1].cell[4]): getColumnName(tmpRow, 'contraCountry'),
            (xml.row[1].cell[5]): getColumnName(tmpRow, 'contraCountryCode'),
            (xml.row[1].cell[6]): getColumnName(tmpRow, 'transactionSumCurrency'),
            (xml.row[1].cell[7]): getColumnName(tmpRow, 'currency'),
            (xml.row[1].cell[8]): getColumnName(tmpRow, 'courseCB'),
            (xml.row[1].cell[9]): getColumnName(tmpRow, 'transactionSumRub'),
            (xml.row[1].cell[10]): getColumnName(tmpRow, 'contractNum'),
            (xml.row[1].cell[11]): getColumnName(tmpRow, 'contractDate'),
            (xml.row[1].cell[12]): getColumnName(tmpRow, 'transactionDate'),
            (xml.row[1].cell[13]): getColumnName(tmpRow, 'bondRegCode'),
            (xml.row[1].cell[14]): getColumnName(tmpRow, 'bondCount'),
            (xml.row[1].cell[15]): getColumnName(tmpRow, 'priceOne'),
            (xml.row[1].cell[16]): getColumnName(tmpRow, 'transactionType'),
            (xml.row[2].cell[0]): 'гр. 2',
            (xml.row[2].cell[1]): 'гр. 3',
            (xml.row[2].cell[2]): 'гр. 4',
            (xml.row[2].cell[3]): 'гр. 5',
            (xml.row[2].cell[4]): 'гр. 6.1',
            (xml.row[2].cell[5]): 'гр. 6.2',
            (xml.row[2].cell[6]): 'гр. 7.1',
            (xml.row[2].cell[7]): 'гр. 7.2',
            (xml.row[2].cell[8]): 'гр. 7.3',
            (xml.row[2].cell[9]): 'гр. 7.4'
    ]
    (10..16).each{
        headerMapping.put(xml.row[2].cell[it], 'гр. ' + (it-2))
    }
    checkHeaderEquals(headerMapping, logger)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def int xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createStoreMessagingDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 2
        newRow.transactionDeliveryDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 3
        newRow.contraName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        def map = getRefBookValue(9, newRow.contraName)
        xmlIndexCol++

        // графа 4
        newRow.transactionMode = getRecordIdImport(14, 'MODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        xmlIndexCol++

        // графа 5
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 6.1
        if (map != null) {
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.NAME?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            }
        }
        xmlIndexCol++

        // графа 6.2
        if (map != null) {
            formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 8
        newRow.transactionSumCurrency = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 9
        newRow.currency = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10
        newRow.courseCB = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 11
        newRow.transactionSumRub = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 12
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 13
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 14
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 15
        newRow.bondRegCode = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 16
        newRow.bondCount = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 17
        xmlIndexCol++

        // графа 18
        newRow.transactionType = getRecordIdImport(16, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)

        rows.add(newRow)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
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
        updateIndexes(dataRows)
    }
}