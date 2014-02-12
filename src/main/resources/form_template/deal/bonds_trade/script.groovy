package form_template.deal.bonds_trade

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 384 - Реализация и приобретение ценных бумаг (9)
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
def editableColumns = ['transactionDeliveryDate', 'contraName', 'transactionMode', 'transactionSumCurrency',
        'currency', 'courseCB', 'transactionSumRub', 'contractNum', 'contractDate', 'transactionDate',
        'bondRegCode', 'bondCount', 'transactionType']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum', 'innKio', 'contraCountry', 'contraCountryCode', 'priceOne']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'transactionDeliveryDate', 'contraName', 'transactionMode', 'contraCountry',
        'transactionSumCurrency', 'currency', 'courseCB', 'transactionSumRub', 'contractNum',
        'contractDate', 'transactionDate', 'bondRegCode', 'bondCount', 'priceOne', 'transactionType']

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

        def transactionDeliveryDate = row.transactionDeliveryDate
        def transactionDate = row.transactionDate
        def transactionSumRub = row.transactionSumRub
        def bondCount = row.bondCount
        def priceOne = row.priceOne
        def courseCB = row.courseCB
        def transactionSumCurrency = row.transactionSumCurrency
        def contractDate = row.contractDate

        // Корректность даты сделки
        if (transactionDeliveryDate < transactionDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('transactionDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка конверсии
        if (courseCB == null || transactionSumCurrency == null || transactionSumRub != (courseCB * transactionSumCurrency).setScale(0, RoundingMode.HALF_UP)) {
            def msg1 = row.getCell('transactionSumRub').column.name
            def msg2 = row.getCell('courseCB').column.name
            def msg3 = row.getCell('transactionSumCurrency').column.name
            logger.warn("Строка $rowNum: «$msg1» не соответствует «$msg2» с учетом данных «$msg3»!")
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg» в строке $rowNum не может быть вне налогового периода!")
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка цены сделки
        def res = null

        if (transactionSumRub != null && bondCount != null) {
            res = (transactionSumRub / bondCount).setScale(0, RoundingMode.HALF_UP)
        }

        if (transactionSumRub == null || bondCount == null || priceOne != res) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('transactionSumRub').column.name
            def msg3 = row.getCell('bondCount').column.name
            logger.warn("Строка $rowNum: «$msg1» не равно отношению «$msg2» и «$msg3»!")
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def int index = 1
    for (row in dataRows) {
        // Порядковый номер строки
        row.rowNum = index++
        // Расчет поля "Цена за 1 шт., руб."
        transactionSumRub = row.transactionSumRub
        bondCount = row.bondCount

        if (transactionSumRub != null && bondCount != null && bondCount != 0) {
            row.priceOne = transactionSumRub / bondCount
        }

        // Расчет полей зависимых от справочников
        row.contraCountry = getRefBookValue(9, row.contraName)?.COUNTRY?.referenceValue
    }
    dataRowHelper.update(dataRows)
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('Сокращенная форма\nДанные для расчета сумм доходов по сделкам', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 3)

    def headerMapping = [
            (xml.row[0].cell[10]): 'Номер договора',
            (xml.row[0].cell[11]): 'Дата договора',
            (xml.row[0].cell[12]): 'Дата (заключения) сделки',
            (xml.row[0].cell[13]): 'Регистрационный код ценной бумаги',
            (xml.row[0].cell[14]): 'Количество бумаг по сделке, шт.',
            (xml.row[0].cell[15]): 'Цена за 1 шт., руб. ',
            (xml.row[0].cell[16]): 'Тип сделки',
            (xml.row[1].cell[0]): 'Дата сделки (поставки)',
            (xml.row[1].cell[1]): 'Наименование контрагента и ОПФ',
            (xml.row[1].cell[2]): 'Режим переговорных сделок',
            (xml.row[1].cell[3]): 'ИНН/ КИО контрагента',
            (xml.row[1].cell[4]): 'Страна местонахождения контрагента',
            (xml.row[1].cell[5]): 'Код страны местонахождения контрагента',
            (xml.row[1].cell[6]): 'Сумма сделки (с учетом НКД), в валюте расчетов',
            (xml.row[1].cell[7]): 'Валюта расчетов по сделке',
            (xml.row[1].cell[8]): 'Курс ЦБ РФ',
            (xml.row[1].cell[9]): 'Сумма сделки (с учетом НКД), руб.',
            (xml.row[2].cell[0]): 'гр. 2',
            (xml.row[2].cell[1]): 'гр. 3',
            (xml.row[2].cell[2]): 'гр. 4',
            (xml.row[2].cell[3]): 'гр. 5',
            (xml.row[2].cell[4]): 'гр. 6.1',
            (xml.row[2].cell[5]): 'гр. 6.2',
            (xml.row[2].cell[6]): 'гр. 7.1',
            (xml.row[2].cell[7]): 'гр. 7.2',
            (xml.row[2].cell[8]): 'гр. 7.3',
            (xml.row[2].cell[9]): 'гр. 7.4',
            (xml.row[2].cell[10]): 'гр. 8',
            (xml.row[2].cell[11]): 'гр. 9',
            (xml.row[2].cell[12]): 'гр. 10',
            (xml.row[2].cell[13]): 'гр. 11',
            (xml.row[2].cell[14]): 'гр. 12',
            (xml.row[2].cell[15]): 'гр. 13',
            (xml.row[2].cell[16]): 'гр. 14'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int xmlIndexRow = -1
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
        newRow.transactionDeliveryDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 3
        newRow.contraName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.contraName)
        xmlIndexCol++

        // графа 4
        newRow.transactionMode = getRecordIdImport(14, 'MODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 5
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO?.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 6.1
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map?.NAME?.stringValue)) || ((text == null || text.isEmpty()) && map?.NAME?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 6.2
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map.CODE?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 8
        newRow.transactionSumCurrency = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        newRow.currency = getRecordIdImport(15, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10
        newRow.courseCB = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 11
        newRow.transactionSumRub = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        newRow.contractNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 13
        newRow.contractDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 14
        newRow.transactionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 15
        newRow.bondRegCode = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 16
        newRow.bondCount = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 17
        xmlIndexCol++

        // графа 18
        newRow.transactionType = getRecordIdImport(16, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}