package form_template.deal.auctions_property

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 380 - Приобретение услуг по организации и проведению торгов по реализации имущества (5)
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
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
        break
}

//// Кэши и константы
@Field def providerCache = [:]
@Field def recordCache = [:]
@Field def refBookCache = [:]

// Редактируемые атрибуты
@Field def editableColumns = ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'count', 'date']

// Автозаполняемые атрибуты
@Field def autoFillColumns = ['rowNumber', 'inn', 'countryCode', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field def nonEmptyColumns = ['rowNumber', 'fullNamePerson', 'inn', 'countryCode', 'sum', 'docNumber', 'docDate', 'count',
        'price', 'cost', 'date']

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

// Текущая дата
@Field def currentDate = new Date()

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
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()
    def rowNum = 0

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNum++
        def docDateCell = row.getCell('docDate')

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Проверка доходов
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def costCell = row.getCell('cost')
        def msgSum = sumCell.column.name
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }
        if (costCell.value != sumCell.value) {
            def msg = costCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }

        // Корректность даты совершения сделки
        def dealDateCell = row.getCell('date')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверки соответствия НСИ
        checkNSI(9, row, "fullNamePerson")
        checkNSI(10, row, "countryCode")
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
        row.rowNumber = index++
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Стоимость"
        row.cost = row.sum

        // Расчет полей зависимых от справочников
            def map =  getRefBookValue(9, row.fullNamePerson)
            row.inn = map?.INN_KIO?.stringValue
            row.countryCode = map?.COUNTRY?.referenceValue

    }
    dataRowHelper.update(dataRows);
}

// Получение импортируемых данных
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }
    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }
    if (!fileName.endsWith('.xls')) {
        logger.error('Выбранный файл не соответствует формату xls!')
        return
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование юридического лица с указанием ОПФ', null)
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 9, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): 'Полное наименование юридического лица с указанием ОПФ',
            (xml.row[0].cell[1]): 'ИНН/ КИО',
            (xml.row[0].cell[2]): 'Код страны по классификатору ОКСМ',
            (xml.row[0].cell[3]): 'Сумма расходов Банка, руб.',
            (xml.row[0].cell[4]): 'Номер договора',
            (xml.row[0].cell[5]): 'Дата договора',
            (xml.row[0].cell[6]): 'Количество сделок',
            (xml.row[0].cell[7]): 'Цена',
            (xml.row[0].cell[8]): 'Стоимость',
            (xml.row[0].cell[9]): 'Дата совершения сделки',
            (xml.row[2].cell[0]): 'гр. 2',
            (xml.row[2].cell[1]): 'гр. 3',
            (xml.row[2].cell[2]): 'гр. 4',
            (xml.row[2].cell[3]): 'гр. 5',
            (xml.row[2].cell[4]): 'гр. 6',
            (xml.row[2].cell[5]): 'гр. 7',
            (xml.row[2].cell[6]): 'гр. 8',
            (xml.row[2].cell[7]): 'гр. 9',
            (xml.row[2].cell[8]): 'гр. 10',
            (xml.row[2].cell[9]): 'гр. 11'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml)
}

// Заполнить форму данными
def addData(def xml) {
    reportPeriodEndDate = reportPeriodService?.get(formData?.reportPeriodId)?.taxPeriod?.getEndDate()
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int xlsIndexRow = 0
    def int rowOffset= 3
    def int colOffset = 2

    def rows = new LinkedList<DataRow<Cell>>()

    for (def row : xml.row) {
        xmlIndexRow++
        xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= 2) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def xmlIndexCol = 0

        // графа 1
        newRow.rowNumber = xmlIndexRow - 2

        // графа 2
        newRow.fullNamePerson = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.fullNamePerson)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO?.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 4
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map.CODE?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.sum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 6
        newRow.docNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 7
        newRow.docDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 8
        newRow.count =  parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        xmlIndexCol++

        // графа 10
        xmlIndexCol++

        // графа 11
        newRow.date =  parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}