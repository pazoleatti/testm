package form_template.deal.interbank_credits.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 389 - Предоставление межбанковских кредитов (14)
 *
 * formTemplateId=389
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
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
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
def editableColumns = ['fullName', 'dealNumber', 'dealDate', 'sum', 'docNumber', 'docDate', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryName', 'countryCode', 'count', 'price', 'total']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['fullName', 'docNumber', 'docDate', 'count', 'sum', 'price', 'dealDoneDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

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
            currentDate, rowIndex, cellName, logger, required)
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

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, ['docNumber', 'docDate'], logger, true)
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns - ['docNumber', 'docDate'], logger, false)

        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def totalCell = row.getCell('total')
        def msgSum = sumCell.column.name

        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg» может быть только «1»!")
        }

        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка цены
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Проверка стоимости
        if (totalCell.value != sumCell.value) {
            def msg = totalCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        // Расчет поля " Количество"
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итого"
        row.total = row.sum
    }
    dataRowHelper.update(dataRows)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(getColumnName(tmpRow, 'fullName'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 13, 2)

    def headerMapping = [
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'inn'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'countryName'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'countryCode'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'docNumber'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'docDate'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'dealNumber'),
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'dealDate'),
            (xml.row[0].cell[8]): getColumnName(tmpRow, 'count'),
            (xml.row[0].cell[9]): getColumnName(tmpRow, 'sum'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'total'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'dealDoneDate'),
            (xml.row[1].cell[0]): 'гр. 2',
            (xml.row[1].cell[1]): 'гр. 3',
            (xml.row[1].cell[2]): 'гр. 4.1',
            (xml.row[1].cell[3]): 'гр. 4.2'
    ]
    (4..12).each {
        headerMapping.put(xml.row[1].cell[it], 'гр. ' + (it + 1))
    }

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
        newRow.fullName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.fullName)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 4
        if (map != null) {
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.NAME?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
            }
        }
        xmlIndexCol++

        // графа 5
        if (map != null) {
            formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 6
        newRow.docNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 7
        newRow.docDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 8
        newRow.dealNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 9
        newRow.dealDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 10
        xmlIndexCol++

        // графа 11
        newRow.sum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        xmlIndexCol++

        // графа 13
        xmlIndexCol++

        // графа 14
        newRow.dealDoneDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}