package form_template.deal.credit_contract.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 2385 - Уступка прав требования по кредитным договорам (10)
 *
 * formTemplateId=2385
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
def editableColumns = ['name', 'contractNum', 'contractDate', 'price', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'country', 'okeiCode', 'count', 'totalCost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'contractNum', 'contractDate', 'okeiCode', 'count', 'price', 'totalCost', 'transactionDate']

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

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, cellName, logger, required)
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
        def contractDate = row.contractDate
        def totalCost = row.totalCost
        def price = row.price

        // Проверка выбранной единицы измерения        
        if (getRefBookValue(12, row.okeiCode)?.CODE?.stringValue != '796') {
            def msg = row.getCell('okeiCode').column.name
            rowError(logger, row, "Строка $rowNum: В графе «$msg» могут быть указаны только следующие элементы: шт.!")
        }

        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('transactionDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» может быть только «1»!")
        }

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка заполнения стоимости сделки
        if (totalCost != price) {
            def msg1 = row.getCell('totalCost').column.name
            def msg2 = row.getCell('price').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Проверка диапазона дат
        if (row.contractDate) {
            checkDateValid(logger, row, 'contractDate', row.contractDate, true)
        }
        if (row.transactionDate) {
            checkDateValid(logger, row, 'transactionDate', row.transactionDate, true)
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    for (row in dataRows) {
        // Количество
        row.count = 1
        // Итого стоимость без учета НДС, акцизов и пошлин, руб.
        row.totalCost = row.price
        row.okeiCode = getRecordId(12, 'CODE', '796', -1, null, true)
    }

    sortFormDataRows(false)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(getColumnName(tmpRow, 'name'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 8, 3)

    def headerMapping = [
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'innKio'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'country'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'contractNum'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'contractDate'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'okeiCode'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'count'),
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[8]): getColumnName(tmpRow, 'totalCost'),
            (xml.row[0].cell[9]): getColumnName(tmpRow, 'transactionDate')
    ]
    (0..9).each{
        headerMapping.put(xml.row[1].cell[it], 'гр. ' + (it+2))
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 1)
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

        // графа 1
        newRow.rowNum = xmlIndexRow - headRowCount

        // графа 2
        newRow.name = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        def map = getRefBookValue(9, newRow.name)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 4
        if (map != null) {
            map = refBookService.getRecordData(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.NAME?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        newRow.okeiCode = getRecordIdImport(12, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        xmlIndexCol++

        // графа 8
        xmlIndexCol++

        // графа 9
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 10
        xmlIndexCol++

        // графа 11
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

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