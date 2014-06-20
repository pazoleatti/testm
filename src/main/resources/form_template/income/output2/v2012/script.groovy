package form_template.income.output2.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * Расчет налога на прибыль с доходов, удерживаемого налоговым агентом
 *
 * formTemplateId=307
 */

// графа 1  - rowNumber
// графа 2  - title
// графа 3  - zipCode
// графа 4  - subdivisionRF
// графа 5  - area
// графа 6  - city
// графа 7  - region
// графа 8  - street
// графа 9  - homeNumber
// графа 10  - corpNumber
// графа 11 - apartment
// графа 12 - surname
// графа 13 - name
// графа 14 - patronymic
// графа 15 - phone
// графа 16 - sumDividend
// графа 17 - dividendDate
// графа 18 - dividendNum
// графа 19 - dividendSum
// графа 20 - taxDate
// графа 21 - taxNum
// графа 22 - sumTax
// графа 23 - reportYear

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
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

// Редактируемые атрибуты 2-18, 20-23
@Field
def editableColumns = ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber',
        'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone', 'sumDividend', 'dividendDate',
        'dividendNum', 'taxDate', 'taxNum', 'sumTax', 'reportYear']

// Проверяемые на пустые значения атрибуты 1, 2, 4, 12, 13, 16, 17, 22
@Field
def nonEmptyColumns = ['rowNumber', 'title', 'subdivisionRF', 'surname', 'name', 'sumDividend', 'dividendDate', 'sumTax']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

void checkCreation() {
    if (formData.kind != FormDataKind.ADDITIONAL) {
        logger.error("Нельзя создавать форму с типом «${formData.kind?.name}»!")
    }
    formDataService.checkUnique(formData, logger)
}

void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (!dataRows.isEmpty()) {
        def number = 0
        for (def row in dataRows) {
            row.rowNumber = ++number
            row.dividendSum = (row.sumDividend ?: 0) - (row.sumTax ?: 0)
        }
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        String zipCode = (String) row.zipCode;
        if (zipCode != null && (zipCode.length() != 6 || !zipCode.matches('[0-9]*'))) {
            logger.error("Строка $rowNum: Неправильно указан почтовый индекс!")
        }

        // Проверки соответствия НСИ
        // checkNSI(4, row, "subdivisionRF")
    }
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп.', null, 23, 3)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 23, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : '№ пп.',
            (xml.row[0].cell[1]) : 'Получатель',
            (xml.row[0].cell[2]) : 'Место нахождения (адрес)',
            (xml.row[0].cell[11]): 'Руководитель организации',
            (xml.row[0].cell[14]): 'Контактный телефон',
            (xml.row[0].cell[15]): 'Сумма начисленных дивидендов',
            (xml.row[0].cell[16]): 'Перечисление дивидендов',
            (xml.row[0].cell[19]): 'Перечисление налога',
            (xml.row[0].cell[22]): 'Отчётный год',
            (xml.row[1].cell[2]) : 'Индекс',
            (xml.row[1].cell[3]) : 'Код региона',
            (xml.row[1].cell[4]) : 'Район',
            (xml.row[1].cell[5]) : 'Город',
            (xml.row[1].cell[6]) : 'Населённый пункт (село, посёлок и т.п.)',
            (xml.row[1].cell[7]) : 'Улица (проспект, переулок и т.д.)',
            (xml.row[1].cell[8]) : 'Номер дома (владения)',
            (xml.row[1].cell[9]) : 'Номер корпуса (строения)',
            (xml.row[1].cell[10]): 'Номер офиса (квартиры)',
            (xml.row[1].cell[11]): 'Фамилия',
            (xml.row[1].cell[12]): 'Имя',
            (xml.row[1].cell[13]): 'Отчество',
            (xml.row[1].cell[16]): 'Дата',
            (xml.row[1].cell[17]): 'Номер платёжного поручения',
            (xml.row[1].cell[18]): 'Сумма',
            (xml.row[1].cell[19]): 'Дата',
            (xml.row[1].cell[20]): 'Номер платёжного поручения',
            (xml.row[1].cell[21]): 'Сумма'
    ]
    (0..22).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 3)
}

void addData(def xml, headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    // количество графов в таблице
    def columnCount = 23
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        if (row.cell.size() >= columnCount) {
            def newRow = formData.createDataRow()
            newRow.setIndex(rowIndex++)
            newRow.setImportIndex(xlsIndexRow)
            editableColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }

            // графа 1
            def xmlIndexCol = 0
            newRow.rowNumber = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 2
            newRow.title = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 3
            newRow.zipCode = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 4 - справочник "Коды субъектов Российской Федерации"
            newRow.subdivisionRF = getRecordIdImport(4, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
            xmlIndexCol++

            // графа 5
            newRow.area = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 6
            newRow.city = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 7
            newRow.region = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 8
            newRow.street = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 9
            newRow.homeNumber = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 10
            newRow.corpNumber = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 11
            newRow.apartment = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 12
            newRow.surname = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 13
            newRow.name = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 14
            newRow.patronymic = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 15
            newRow.phone = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 16
            newRow.sumDividend = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 17
            newRow.dividendDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 18
            newRow.dividendNum = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 19
            newRow.dividendSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 20
            newRow.taxDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 21
            newRow.taxNum = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 22
            newRow.sumTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 23
            newRow.reportYear = parseDate(row.cell[xmlIndexCol].text(), "yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

            rows.add(newRow)
        }
    }
    dataRowHelper.save(rows)
}