package form_template.deal.securities

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 381 - Приобретение и реализация ценных бумаг (долей в уставном капитале) (6)
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
def editableColumns = ['fullNamePerson', 'dealSign', 'incomeSum', 'outcomeSum', 'docNumber', 'docDate', 'okeiCode',
        'count', 'dealDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryCode', 'price', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'fullNamePerson', 'inn', 'countryCode', 'docNumber', 'docDate', 'okeiCode',
        'count', 'price', 'cost', 'dealDate']

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

    def rowNum = 0
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNum++

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        def docDateCell = row.getCell('docDate')
        def okeiCodeCell = row.getCell('okeiCode')

        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.warn("Строка $rowNum: «$msgIn» и «$msgOut» не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Строка $rowNum: Одна из граф «$msgIn» и «$msgOut» должна быть заполнена!")
        }
        // Проверка выбранной единицы измерения           
        def okei = getRefBookValue(12, row.okeiCode)?.CODE?.stringValue
        if (okei != '796' && okei != '744') {
            def msg = okeiCodeCell.column.name
            logger.warn("Строка $rowNum: В графе «$msg» могут быть указаны только следующие элементы: шт., процент!")
        }

        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }

        // Проверка цены
        def sumCell = row.incomeSum != null ? row.getCell('incomeSum') : row.getCell('outcomeSum')
        if (sumCell.value != null) {
            def countCell = row.getCell('count')
            def priceCell = row.getCell('price')
            if (okei == '796' && countCell.value != null && countCell.value != 0
                    && priceCell.value != (sumCell.value / countCell.value).setScale(2, RoundingMode.HALF_UP)) {
                def msg1 = priceCell.column.name
                def msg2 = sumCell.column.name
                def msg3 = countCell.column.name
                logger.warn("Строка $rowNum: «$msg1» не равно отношению «$msg2» и «$msg3»!")
            } else if (okei == '744' && priceCell.value != sumCell.value) {
                def msg1 = priceCell.column.name
                def msg2 = sumCell.column.name
                logger.warn("Строка $rowNum: «$msg1» не равно «$msg2»!")
            }
        }
        // Корректность даты совершения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        // Проверки соответствия НСИ
        checkNSI(9, row, "fullNamePerson")
        checkNSI(10, row, "countryCode")
        checkNSI(36, row, "dealSign")
        checkNSI(12, row, "okeiCode")
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
        def priceValue = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        if (priceValue != null) {
            def okei = getRefBookValue(12, row.okeiCode)?.CODE?.stringValue
            if (okei == '744') {
                row.price = priceValue
            } else if (okei == '796' && row.count != 0 && row.count != null) {
                row.price = priceValue / row.count
            } else {
                row.price = null
            }
        } else {
            row.price = null
        }
        // Расчет поля "Стоимость"
        row.cost = priceValue
        // Расчет полей зависимых от справочников
        def map = getRefBookValue(9, row.fullNamePerson)
        row.inn = map?.INN_KIO?.stringValue
        row.countryCode = map?.COUNTRY?.referenceValue
    }
    dataRowHelper.update(dataRows);
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('Полное наименование юридического лица с указанием ОПФ', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 13, 3)

    def headerMapping = [
            (xml.row[0].cell[1]): 'ИНН/ КИО',
            (xml.row[0].cell[2]): 'Код страны по классификатору ОКСМ',
            (xml.row[0].cell[3]): 'Признак сделки, совершенной в РПС',
            (xml.row[0].cell[4]): 'Сумма доходов (стоимость реализации) Банка, руб.',
            (xml.row[0].cell[5]): 'Сумма расходов (стоимость приобретения) Банка, руб.',
            (xml.row[0].cell[6]): 'Номер договора',
            (xml.row[0].cell[7]): 'Дата договора',
            (xml.row[0].cell[8]): 'Код единицы измерения по ОКЕИ',
            (xml.row[0].cell[9]): 'Количество',
            (xml.row[0].cell[10]): 'Цена',
            (xml.row[0].cell[11]): 'Стоимость',
            (xml.row[0].cell[12]): 'Дата совершения сделки',
            (xml.row[2].cell[0]): 'гр. 2',
            (xml.row[2].cell[1]): 'гр. 3',
            (xml.row[2].cell[2]): 'гр. 4',
            (xml.row[2].cell[3]): 'гр. 5',
            (xml.row[2].cell[4]): 'гр. 6',
            (xml.row[2].cell[5]): 'гр. 7',
            (xml.row[2].cell[6]): 'гр. 8',
            (xml.row[2].cell[7]): 'гр. 9',
            (xml.row[2].cell[8]): 'гр. 10',
            (xml.row[2].cell[9]): 'гр. 11',
            (xml.row[2].cell[10]): 'гр. 12',
            (xml.row[2].cell[11]): 'гр. 13',
            (xml.row[2].cell[12]): 'гр. 14'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int xlsIndexRow = 0
    def int rowOffset = 3
    def int colOffset = 2

    def rows = new LinkedList<DataRow<Cell>>()

    for (def row : xml.row) {
        xmlIndexRow++
        xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
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
            if ((text != null && !text.isEmpty() && !text.equals(map?.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map?.CODE?.stringValue != null)) {
                logger.warn("Строка ${xlsIndexRow} столбец ${xmlIndexCol + colOffset} содержит значение, отсутствующее в справочнике!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.dealSign = getRecordIdImport(36, 'SIGN', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 6
        newRow.incomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 7
        newRow.outcomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 8
        newRow.docNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 9
        newRow.docDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 10
        newRow.okeiCode = getRecordIdImport(12, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 11
        newRow.count = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 13
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 14
        newRow.dealDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}