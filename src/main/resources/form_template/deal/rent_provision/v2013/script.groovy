package form_template.deal.rent_provision.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 376 - Предоставление нежилых помещений в аренду (1)
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
def editableColumns = ['jurName', 'incomeBankSum', 'outcomeBankSum', 'contractNum', 'contractDate', 'country',
        'region', 'city', 'settlement', 'count', 'price', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'countryCode', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['jurName', 'contractNum', 'contractDate', 'country', 'count', 'price', 'cost', 'transactionDate']

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

        checkNonEmptyColumns(row, rowNum, ['contractNum', 'contractDate'], logger, true)
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns - ['contractNum', 'contractDate'], logger, false)

        def cost = row.cost
        def incomeBankSum = row.incomeBankSum
        def outcomeBankSum = row.outcomeBankSum
        def transactionDate = row.transactionDate
        def contractDate = row.contractDate

        def bankSum = null
        def msgBankSum = row.getCell('incomeBankSum').column.name
        if (incomeBankSum != null && outcomeBankSum == null) {
            bankSum = incomeBankSum
        }
        if (outcomeBankSum != null && incomeBankSum == null) {
            bankSum = outcomeBankSum
            msgBankSum = row.getCell('outcomeBankSum').column.name
        }

        // Наименования колонок
        def contractDateName = row.getCell('contractDate').column.name
        def transactionDateName = row.getCell('transactionDate').column.name
        def costName = row.getCell('cost').column.name

        if (row.count == 0) {
            def countName = row.getCell('count').column.name
            rowWarning(logger, row, "Строка $rowNum: Графа «$countName» не может содержать значение 0")
        }

        // Проверка доходности
        if (cost != bankSum) {
            rowWarning(logger, row, "Строка $rowNum: «$costName» не может отличаться от «$msgBankSum»!")
        }

        // Заполнение граф 5.1 и 5.2
        if (incomeBankSum == null && outcomeBankSum == null) {
            def msg1 = row.getCell('incomeBankSum').column.name
            def msg2 = row.getCell('outcomeBankSum').column.name
            rowWarning(logger, row, "Строка $rowNum: Должна быть заполнена графа «$msg1» или графа «$msg2»!")
        }
        if (incomeBankSum != null && outcomeBankSum != null) {
            def msg1 = row.getCell('incomeBankSum').column.name
            def msg2 = row.getCell('outcomeBankSum').column.name
            rowWarning(logger, row, "Строка $rowNum: Графа «$msg1» и графа «$msg2» не могут быть заполнены одновременно!")
        }

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            rowWarning(logger, row, "Строка $rowNum: «$transactionDateName» не может быть меньше «$contractDateName»!")
        }

        // Проверка заполненности одного из атрибутов
        if (row.city != null && !row.city.toString().isEmpty() && row.settlement != null && !row.settlement.toString().isEmpty()) {
            def cityName = row.getCell('city').column.name
            def settleName = row.getCell('settlement').column.name
            rowWarning(logger, row, "Строка $rowNum: Если заполнена графа «$cityName», то графа «$settleName» не должна быть заполнена!")
        }

        // Проверка заполнения региона
        def country = getRefBookValue(10, row.country)?.CODE?.stringValue
        if (country != null) {
            def regionName = row.getCell('region').column.name
            def countryName = row.getCell('country').column.name
            if (country == '643' && row.region == null) {
                rowWarning(logger, row, "Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
            } else if (country != '643' && row.region != null) {
                rowWarning(logger, row, "Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
            }
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
        def bankSum = null
        if (row.incomeBankSum != null && row.outcomeBankSum == null)
            bankSum = row.incomeBankSum
        if (row.outcomeBankSum != null && row.incomeBankSum == null)
            bankSum = row.outcomeBankSum

        // Расчет поля "Стоимость"
        row.cost = bankSum
    }
    dataRowHelper.update(dataRows)
}

def BigDecimal calc13(def row, def bankSum) {
    if (bankSum != null && row.count != null && row.count != 0) {
        return (bankSum / row.count).setScale(0, RoundingMode.HALF_UP)
    }
    return null
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(getColumnName(tmpRow, 'jurName'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 13, 3)

    def headerMapping = [
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'innKio'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'countryCode'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'incomeBankSum'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'outcomeBankSum'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'contractNum'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'contractDate'),
            (xml.row[0].cell[7]): 'Адрес местонахождения объекта недвижимости',
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'count'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'transactionDate'),
            (xml.row[1].cell[7]): getColumnName(tmpRow, 'country'),
            (xml.row[1].cell[8]): getColumnName(tmpRow, 'region'),
            (xml.row[1].cell[9]): getColumnName(tmpRow, 'city'),
            (xml.row[1].cell[10]): getColumnName(tmpRow, 'settlement'),
            (xml.row[2].cell[0]): 'гр. 2',
            (xml.row[2].cell[1]): 'гр. 3',
            (xml.row[2].cell[2]): 'гр. 4',
            (xml.row[2].cell[3]): 'гр. 5.1',
            (xml.row[2].cell[4]): 'гр. 5.2',
            (xml.row[2].cell[5]): 'гр. 6',
            (xml.row[2].cell[6]): 'гр. 7',
            (xml.row[2].cell[7]): 'гр. 8',
            (xml.row[2].cell[8]): 'гр. 9',
            (xml.row[2].cell[9]): 'гр. 10',
            (xml.row[2].cell[10]): 'гр. 11',
            (xml.row[2].cell[11]): 'гр. 12',
            (xml.row[2].cell[12]): 'гр. 13',
            (xml.row[2].cell[13]): 'гр. 14',
            (xml.row[2].cell[14]): 'гр. 15'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
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
        newRow.jurName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.jurName)
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
                formDataService.checkReferenceValue(10, row.cell[xmlIndexCol].text(), map.CODE?.stringValue, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
            }
        }
        xmlIndexCol++

        // графа 5.1
        newRow.incomeBankSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 5.2
        newRow.outcomeBankSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 6
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 7
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 8
        newRow.country = getRecordIdImport(10, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 9
        newRow.region = getRecordIdImport(4, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10
        newRow.city = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 11
        newRow.settlement = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 12
        newRow.count = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 13
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 14
        xmlIndexCol++

        // графа 15
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}