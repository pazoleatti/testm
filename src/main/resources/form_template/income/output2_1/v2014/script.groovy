package form_template.income.output2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * Сведения о дивидендах (доходах от долевого участия в других организациях,
 * созданных на территории Российской Федерации), выплаченных в отчетном квартале
 *
 * formTemplateId=1413
 */

// графа 1  - rowNumber
// графа 2  - emitent
// графа 3  - decreeNumber
// графа 4  - title
// графа 5  - zipCode
// графа 6  - subdivisionRF
// графа 7  - area
// графа 8  - city
// графа 9  - region
// графа 10  - street
// графа 11 - homeNumber
// графа 12  - corpNumber
// графа 13 - apartment
// графа 14 - surname
// графа 15 - name
// графа 16 - patronymic
// графа 17 - phone
// графа 18 - sumDividend
// графа 19 - dividendDate
// графа 20 - dividendNum
// графа 21 - dividendSum
// графа 22 - taxDate
// графа 23 - taxNum
// графа 24 - sumTax
// графа 25 - reportYear

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

// Редактируемые атрибуты 2-25
@Field
def editableColumns = ['emitent', 'decreeNumber', 'title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region',
                       'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone',
                       'sumDividend', 'dividendDate', 'dividendNum', 'dividendSum', 'taxDate', 'taxNum', 'sumTax', 'reportYear']

// Проверяемые на пустые значения атрибуты 1-16, 18-25
@Field
def nonEmptyColumns = ['rowNumber', 'emitent', 'decreeNumber', 'title', 'zipCode', 'sumDividend', 'dividendDate', 'dividendSum', 'sumTax']

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

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (!dataRows.isEmpty()) {
        def number = 0
        for (def row in dataRows) {
            row.rowNumber = ++number
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
    }
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп.', null, 25, 3)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 25, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : '№ пп.',
            (xml.row[0].cell[1]) : 'Эмитент',
            (xml.row[0].cell[2]) : 'Номер решения о выплате дивидендов',
            (xml.row[0].cell[3]) : 'Получатель',
            (xml.row[0].cell[4]) : 'Место нахождения (адрес)',
            (xml.row[0].cell[13]): 'Руководитель организации',
            (xml.row[0].cell[16]): 'Контактный телефон',
            (xml.row[0].cell[17]): 'Сумма начисленных дивидендов',
            (xml.row[0].cell[18]): 'Перечисление дивидендов',
            (xml.row[0].cell[21]): 'Перечисление налога',
            (xml.row[0].cell[24]): 'Отчётный год',
            (xml.row[1].cell[4]) : 'Индекс',
            (xml.row[1].cell[5]) : 'Код региона',
            (xml.row[1].cell[6]) : 'Район',
            (xml.row[1].cell[7]) : 'Город',
            (xml.row[1].cell[8]) : 'Населённый пункт',
            (xml.row[1].cell[9]) : 'Улица',
            (xml.row[1].cell[10]) : 'Номер дома (владения)',
            (xml.row[1].cell[11]) : 'Номер корпуса (строения)',
            (xml.row[1].cell[12]): 'Номер офиса (квартиры)',
            (xml.row[1].cell[13]): 'Фамилия',
            (xml.row[1].cell[14]): 'Имя',
            (xml.row[1].cell[15]): 'Отчество',
            (xml.row[1].cell[18]): 'Дата',
            (xml.row[1].cell[19]): 'Номер платёжного поручения',
            (xml.row[1].cell[20]): 'Сумма',
            (xml.row[1].cell[21]): 'Дата',
            (xml.row[1].cell[22]): 'Номер платёжного поручения',
            (xml.row[1].cell[23]): 'Сумма'
    ]
    (0..24).each { index ->
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
    def columnCount = 25
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
            editableColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }

            // графа 1
            def xmlIndexCol = 0
            newRow.rowNumber = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 2
            newRow.emitent = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 3
            newRow.decreeNumber = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 4
            newRow.title = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 5
            newRow.zipCode = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 6 - справочник "Коды субъектов Российской Федерации"
            newRow.subdivisionRF = getRecordIdImport(4, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)
            xmlIndexCol++

            // графа 7
            newRow.area = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 8
            newRow.city = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 9
            newRow.region = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 10
            newRow.street = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 11
            newRow.homeNumber = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 12
            newRow.corpNumber = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 13
            newRow.apartment = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 14
            newRow.surname = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 15
            newRow.name = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 16
            newRow.patronymic = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 17
            newRow.phone = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 18
            newRow.sumDividend = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 19
            newRow.dividendDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 20
            newRow.dividendNum = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 21
            newRow.dividendSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 22
            newRow.taxDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 23
            newRow.taxNum = row.cell[xmlIndexCol].text()
            xmlIndexCol++

            // графа 24
            newRow.sumTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++

            // графа 25
            newRow.reportYear = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

            rows.add(newRow)
        }
    }
    dataRowHelper.save(rows)
}