package form_template.deal.take_corporate_credit.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 2397 - Привлечение ресурсов (20)
 *
 * formTemplateId = 2397
 *
 * @author Stanislav Yasinskiy
 */

// 1.  rowNumber        № п/п
// 2.  fullNamePerson   Полное наименование юридического лица с указанием ОПФ
// 3.  inn              ИНН/КИО
// 4.  countryName      Страна регистрации
// 5.  sum              Сумма расходов Банка, руб.
// 6.  docNumber        Номер договора
// 7.  docDate          Дата договора
// 8.  count            Количество
// 9.  price            Цена
// 10. cost             Стоимость
// 11. dealDate         Дата совершения сделки

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
def editableColumns = ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'dealDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryName', 'count', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'count', 'price', 'cost', 'dealDate']

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
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def costCell = row.getCell('cost')
        def msgSum = sumCell.column.name

        // Проверка цены
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Проверка стоимости
        if (costCell.value != sumCell.value) {
            def msg = costCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть равно значению графы «$msgSum»!")
        }

        // Корректность даты совершения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» может быть только «1»!")
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
    for (row in dataRows) {
        // В поле "Количество" подставляется значение «1»
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Стоимость"
        row.cost = row.sum
    }

    sortFormDataRows(false)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(getColumnName(tmpRow, 'fullNamePerson'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 10, 3)

    def headerMapping = [
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'inn'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'countryName'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'sum'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'docNumber'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'docDate'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'count'),
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[8]): getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[9]): getColumnName(tmpRow, 'dealDate')
    ]

    (0..9).each{
        headerMapping.put(xml.row[1].cell[it], 'гр. ' + (it + 2))
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

        // графа 2
        newRow.fullNamePerson = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, true)
        def map = getRefBookValue(9, newRow.fullNamePerson)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            formDataService.checkReferenceValue(9, row.cell[xmlIndexCol].text(), map.INN_KIO?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 4
        if (map != null) {
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if (map != null) {
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.sum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 6
        newRow.docNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 7
        newRow.docDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 8 newRow.count =
        xmlIndexCol++
        // графа 9 newRow.price =
        xmlIndexCol++
        // графа 10 newRow.cost =
        xmlIndexCol++

        // графа 11
        newRow.dealDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

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