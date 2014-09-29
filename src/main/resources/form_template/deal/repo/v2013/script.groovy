package form_template.deal.repo.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 383 - Сделки РЕПО (8)
 *
 * formTemplateId = 2383
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
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
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
        if (dt1 != null && (dt1 < dFrom || dt1 > dTo)) {
            def msg = row.getCell('date1').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» не может быть больше даты окончания отчётного периода или меньше даты его начала!")
        }

        // Корректность даты совершения сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
   // нет полей для заполнения
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(getColumnName(tmpRow, 'jurName'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 18, 2)

    def headerMapping = [
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'innKio'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'country'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'countryCode'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'contractNum'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'contractDate'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'transactionNum'),
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'transactionDeliveryDate'),
            (xml.row[0].cell[8]): getColumnName(tmpRow, 'dealsMode'),
            (xml.row[0].cell[9]): getColumnName(tmpRow, 'date1'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'date2'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'percentIncomeSum'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'percentConsumptionSum'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'priceFirstCurrency'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'currencyCode'),
            (xml.row[0].cell[16]): getColumnName(tmpRow, 'priceFirstRub'),
            (xml.row[0].cell[17]): getColumnName(tmpRow, 'transactionDate'),
            (xml.row[0].cell[15]): getColumnName(tmpRow, 'courseCB'),
            (xml.row[1].cell[0]): 'Гр. 2',
            (xml.row[1].cell[1]): 'Гр. 3',
            (xml.row[1].cell[2]): 'Гр. 4.1',
            (xml.row[1].cell[3]): 'Гр. 4.2',
            (xml.row[1].cell[4]): 'Гр. 5',
            (xml.row[1].cell[5]): 'Гр. 6',
            (xml.row[1].cell[6]): 'Гр. 7',
            (xml.row[1].cell[7]): 'Гр. 8',
            (xml.row[1].cell[8]): 'Гр. 9',
            (xml.row[1].cell[9]): 'Гр. 10.1',
            (xml.row[1].cell[10]): 'Гр. 10.2',
            (xml.row[1].cell[11]): 'Гр. 11.1',
            (xml.row[1].cell[12]): 'Гр. 11.2',
            (xml.row[1].cell[13]): 'Гр. 12',
            (xml.row[1].cell[14]): 'Гр. 13',
            (xml.row[1].cell[15]): 'Гр. 14',
            (xml.row[1].cell[16]): 'Гр. 15',
            (xml.row[1].cell[17]): 'Гр. 16'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
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

        def newRow = formData.createDataRow()
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
        newRow.jurName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        def map = getRefBookValue(9, newRow.jurName)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 4.1
        if (map != null) {
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.NAME?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            }
        }
        xmlIndexCol++

        // графа 4.2
        if (map != null) {
            formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 5
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        newRow.transactionNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.transactionDeliveryDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 9
        newRow.dealsMode = getRecordIdImport(14, 'MODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10.1
        newRow.date1 = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 10.2
        newRow.date2 = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 11.1
        newRow.percentIncomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 11.2
        newRow.percentConsumptionSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 12
        newRow.priceFirstCurrency = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 13
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        xmlIndexCol++

        // графа 14
        newRow.courseCB = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 15
        newRow.priceFirstRub = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 16
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}