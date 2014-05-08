package form_template.deal.tech_service.v2013

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 377 - Техническое обслуживание нежилых помещений (2)
 *
 * formTemplateId=377
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
def editableColumns = ['jurName', 'bankSum', 'contractNum', 'contractDate', 'country', 'region', 'city',
        'settlement', 'count', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'countryCode', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'jurName', 'countryCode', 'bankSum', 'contractNum', 'contractDate',
        'country', 'price', 'cost', 'transactionDate']

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

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        def cost = row.cost
        def price = row.price
        def count = row.count
        def bankSum = row.bankSum
        def transactionDate = row.transactionDate
        def contractDate = row.contractDate

        // Наименования колонок
        def contractDateName = row.getCell('contractDate').column.name
        def transactionDateName = row.getCell('transactionDate').column.name
        def priceName = row.getCell('price').column.name
        def bankSumName = row.getCell('bankSum').column.name
        def countName = row.getCell('count').column.name
        def costName = row.getCell('cost').column.name

        // Проверка стоимости
        if (price != null && count != null && cost != price * count) {
            logger.warn("Строка $rowNum: «$costName» не равна произведению «$countName» и «$priceName»!")
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            logger.warn("Строка $rowNum: «$contractDateName» не может быть вне налогового периода!")
        }

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            logger.warn("Строка $rowNum: «$transactionDateName» не может быть меньше «$contractDateName»!")
        }

        // Проверка цены сделки
        if (count != null) {
            def res = null

            if (bankSum != null && count != null && count != 0) {
                res = (bankSum / count).setScale(0, RoundingMode.HALF_UP)
            }

            if (bankSum == null || count == null || price != res) {
                logger.warn("Строка $rowNum: «$priceName» не равно отношению «$bankSumName» и «$countName»!")
            }
        } else {
            if (price != bankSum) {
                logger.warn("Строка $rowNum: «$priceName» не равно «$bankSumName»!")
            }
        }

        // Проверка расходов
        if (cost != bankSum) {
            logger.warn("«$costName» не равно «$bankSumName» в строке $rowNum!")
        }

        // Проверка заполнения региона
        def country = getRefBookValue(10, row.country)?.CODE?.stringValue
        if (country != null) {
            def regionName = row.getCell('region').column.name
            def countryName = row.getCell('country').column.name
            if (country == '643' && row.region == null) {
                logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
            } else if (country != '643' && row.region != null) {
                logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
            }
        }

        // Проверка заполненности одного из атрибутов
        if (row.city != null && !row.city.toString().isEmpty() && row.settlement != null && !row.settlement.toString().isEmpty()) {
            def cityName = row.getCell('city').column.name
            def settleName = row.getCell('settlement').column.name
            logger.warn("Строка $rowNum: Если заполнена графа «$settleName», то графа «$cityName» не должна быть заполнена!")
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

    def index = 1
    for (row in dataRows) {
        // Порядковый номер строки
        row.rowNum = index++
        // Расчет поля "Цена"
        row.price = null
        bankSum = row.bankSum
        if (bankSum != null) {
            count = row.count
            row.price = count == null || count == 0 ? bankSum : bankSum / count
        }
        // Расчет поля "Стоимость"
        row.cost = bankSum
        // Расчет полей зависимых от справочников
        row.countryCode = getRefBookValue(9, row.jurName)?.COUNTRY?.referenceValue
    }
    dataRowHelper.update(dataRows);
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(getColumnName(tmpRow, 'jurName'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 14, 2)

    def headerMapping = [
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'innKio'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'countryCode'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'bankSum'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'contractNum'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'contractDate'),
            (xml.row[0].cell[6]): 'Адрес местонахождения объекта недвижимости',
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'count'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'price'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'transactionDate'),
            (xml.row[1].cell[6]): getColumnName(tmpRow, 'country'),
            (xml.row[1].cell[7]): getColumnName(tmpRow, 'region'),
            (xml.row[1].cell[8]): getColumnName(tmpRow, 'city'),
            (xml.row[1].cell[9]): getColumnName(tmpRow, 'settlement')
    ]

    (0..13).each{
        headerMapping.put(xml.row[2].cell[it], 'гр. ' + (it + 2))
    }

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
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def xmlIndexCol = 0

        // графа 1
        newRow.rowNum = xmlIndexRow - 2

        // графа 2
        newRow.jurName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.jurName)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO?.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 4
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if ((text != null && !text.isEmpty() && !text.isEmpty() && !text.equals(map?.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map?.CODE?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.bankSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
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