package form_template.deal.repo

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 383 - Сделки РЕПО (8)
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
def editableColumns = ['jurName', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
        'dealsMode', 'date1', 'date2', 'percentIncomeSum', 'percentConsumptionSum', 'priceFirstCurrency',
        'currencyCode', 'courseCB', 'priceFirstRub', 'transactionDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'country', 'countryCode']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'jurName', 'innKio', 'country', 'countryCode', 'contractNum', 'contractDate',
        'transactionNum', 'transactionDeliveryDate', 'dealsMode', 'date1', 'date2', 'priceFirstCurrency',
        'currencyCode', 'courseCB', 'priceFirstRub', 'transactionDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
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
    if (!fileName.endsWith('.xls')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
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

        def contractDate = row.contractDate
        def transactionDate = row.transactionDate
        def transactionDeliveryDate = row.transactionDeliveryDate
        def percentIncomeSum = row.percentIncomeSum
        def percentConsumptionSum = row.percentConsumptionSum

        // Заполнение граф 13 и 14
        if (percentIncomeSum == null && percentConsumptionSum == null) {
            def msg1 = row.getCell('percentIncomeSum').column.name
            def msg2 = row.getCell('percentConsumptionSum').column.name
            logger.warn("Строка $rowNum: Должна быть заполнена графа «$msg1» или графа «$msg2»!")
        }
        if (percentIncomeSum != null && percentConsumptionSum != null) {
            def msg1 = row.getCell('percentIncomeSum').column.name
            def msg2 = row.getCell('percentConsumptionSum').column.name
            logger.warn("Строка $rowNum: Графа «$msg1» и графа «$msg2» не могут быть заполнены одновременно!")
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Корректность даты (заключения) сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Корректность даты исполнения 1–ой части сделки
        def dt1 = row.date1
        if (dt1 != null && (dt1 < dFrom || dt1 > dTo)) {
            def msg = row.getCell('date1').column.name
            if (dt1 > dTo) {
                logger.warn("Строка $rowNum: «$msg» не может быть больше даты окончания отчётного периода!")
            }
            if (dt1 < dFrom) {
                logger.warn("Строка $rowNum: «$msg» не может быть меньше даты начала отчётного периода!")
            }
        }

        // Корректность даты совершения сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверки соответствия НСИ
        checkNSI(9, row, "jurName")
        checkNSI(10, row, "country")
        checkNSI(10, row, "countryCode")
        checkNSI(14, row, "dealsMode")
        checkNSI(15, row, "currencyCode")
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
        // Расчет полей зависимых от справочников
        def map = getRefBookValue(9, row.jurName)
        row.innKio = map?.INN_KIO?.stringValue
        row.country = map?.COUNTRY?.referenceValue
        row.countryCode = map?.COUNTRY?.referenceValue
    }
    dataRowHelper.update(dataRows);
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('Полное наименование с указанием ОПФ', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 18, 2)

    def headerMapping = [
            (xml.row[0].cell[1]): 'ИНН/ КИО',
            (xml.row[0].cell[2]): 'Наименование страны регистрации',
            (xml.row[0].cell[3]): 'Код страны регистрации по классификатору ОКСМ',
            (xml.row[0].cell[4]): 'Номер договора',
            (xml.row[0].cell[5]): 'Дата договора',
            (xml.row[0].cell[6]): 'Номер сделки',
            (xml.row[0].cell[7]): 'Дата (заключения) сделки',
            (xml.row[0].cell[8]): 'Режим переговорных сделок',
            (xml.row[0].cell[9]): 'Дата исполнения  1-ой части сделки',
            (xml.row[0].cell[10]): 'Дата исполнения  2-ой части сделки',
            (xml.row[0].cell[11]): 'Сумма процентного дохода (руб.)',
            (xml.row[0].cell[12]): 'Сумма процентного расхода (руб.)',
            (xml.row[0].cell[13]): 'Цена 1-ой части сделки, ед. валюты',
            (xml.row[0].cell[14]): 'Код валюты расчетов по сделке',
            (xml.row[0].cell[16]): 'Цена 1-ой части сделки, руб.',
            (xml.row[0].cell[17]): 'Дата совершения сделки',
            (xml.row[0].cell[15]): 'Курс ЦБ РФ',
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
    def int rowOffset = 3
    def int colOffset = 2

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
        newRow.rowNum = xmlIndexRow - headRowCount

        // графа 2
        newRow.jurName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.jurName)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            def String text = row.cell[xmlIndexCol].text()
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
            if ((text != null && !text.isEmpty() && !text.equals(map?.NAME?.stringValue)) || ((text == null || text.isEmpty()) && map?.NAME?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 5
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map.CODE?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 6
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 7
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 8
        newRow.transactionNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 9
        newRow.transactionDeliveryDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 10
        newRow.dealsMode = getRecordIdImport(14, 'MODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 11
        newRow.date1 = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        newRow.date2 = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 13
        newRow.percentIncomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 14
        newRow.percentConsumptionSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 15
        newRow.priceFirstCurrency = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 16
        newRow.currencyCode = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 17
        newRow.courseCB = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 18
        newRow.priceFirstRub = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 19
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}