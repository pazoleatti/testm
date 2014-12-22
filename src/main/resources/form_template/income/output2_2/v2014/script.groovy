package form_template.income.output2_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * Сведения о дивидендах (доходах от долевого участия в других организациях,
 * созданных на территории Российской Федерации), выплаченных в отчетном квартале
 *
 * formTemplateId=10540
 */

// графа 1  -  rowNumber
// графа 2  -  emitent
// графа 3  -  decreeNumber
// графа 4  -  inn
// графа 5  -  kpp
// графа 6  -  recType
// графа 7  -  recName
// графа 8  -  zipCode
// графа 9  -  subdivisionRF
// графа 10  -  area
// графа 11  -  city
// графа 12  -  region
// графа 13  -  street
// графа 14  -  homeNumber
// графа 15  -  corpNumber
// графа 16  -  apartment
// графа 17  -  surname
// графа 18  -  name
// графа 19  -  patronymic
// графа 20  -  phone
// графа 21  -  sumDividend
// графа 22  -  dividendDate
// графа 23  -  dividendNum
// графа 24  -  dividendSum
// графа 25  -  taxDate
// графа 26  -  taxNum
// графа 27  -  sumTax
// графа 28  -  reportYear

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
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

// Редактируемые атрибуты 2-28
@Field
def editableColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'recName', 'zipCode', 'subdivisionRF', 'area',
                       'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic',
                       'phone', 'sumDividend', 'dividendDate', 'dividendNum', 'dividendSum', 'taxDate', 'taxNum', 'sumTax', 'reportYear']

// Проверяемые на пустые значения атрибуты 1-7, 21, 22, 24, 27
@Field
def nonEmptyColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'recName', 'sumDividend', 'dividendDate', 'dividendSum', 'sumTax']

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
    // расчетов нет
    sortFormDataRows()
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка одновременного заполнения/не заполнения «Графы 17» и «Графы 18»
        if (row.surname == null && row.name != null || row.name == null && row.surname != null) {
            rowError(logger, row, "Графы «Фамилия» и «Имя» должны быть заполнены одновременно (либо обе графы не должны заполняться)!")
        }
    }
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп.', null, 28, 3)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 28, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : '№ пп.',
            (xml.row[0].cell[1]) : 'Эмитент',
            (xml.row[0].cell[2]) : 'Номер решения о выплате дивидендов',
            (xml.row[0].cell[3]) : 'Получатель',
            (xml.row[0].cell[7]) : 'Место нахождения (адрес)',
            (xml.row[0].cell[16]): 'Руководитель организации',
            (xml.row[0].cell[19]): 'Контактный телефон',
            (xml.row[0].cell[20]): 'Сумма начисленных дивидендов',
            (xml.row[0].cell[21]): 'Перечисление дивидендов',
            (xml.row[0].cell[24]): 'Перечисление налога',
            (xml.row[0].cell[27]): 'Отчётный год',
            (xml.row[1].cell[3]) : 'ИНН',
            (xml.row[1].cell[4]) : 'КПП',
            (xml.row[1].cell[5]) : 'Тип',
            (xml.row[1].cell[6]) : 'Наименование',
            (xml.row[1].cell[7]) : 'Индекс',
            (xml.row[1].cell[8]) : 'Код региона',
            (xml.row[1].cell[9]) : 'Район',
            (xml.row[1].cell[10]): 'Город',
            (xml.row[1].cell[11]): 'Населённый пункт',
            (xml.row[1].cell[12]): 'Улица',
            (xml.row[1].cell[13]): 'Номер дома (владения)',
            (xml.row[1].cell[14]): 'Номер корпуса (строения)',
            (xml.row[1].cell[15]): 'Номер квартиры (офиса)',
            (xml.row[1].cell[16]): 'Фамилия',
            (xml.row[1].cell[17]): 'Имя',
            (xml.row[1].cell[18]): 'Отчество',
            (xml.row[1].cell[21]): 'Дата',
            (xml.row[1].cell[22]): 'Номер платёжного поручения',
            (xml.row[1].cell[23]): 'Сумма',
            (xml.row[1].cell[24]): 'Дата',
            (xml.row[1].cell[25]): 'Номер платёжного поручения',
            (xml.row[1].cell[26]): 'Сумма'
    ]
    (0..27).each { index ->
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
    def columnCount = 28
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

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 1
        def xmlIndexCol = 1

        // графа 2
        newRow.emitent = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 3
        newRow.decreeNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 4
        newRow.inn = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 5
        newRow.kpp = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.recType = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 7
        newRow.recName = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.zipCode = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 9 - справочник "Коды субъектов Российской Федерации"
        newRow.subdivisionRF = getRecordIdImport(4, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)
        xmlIndexCol++

        // графа 10
        newRow.area = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 11
        newRow.city = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 12
        newRow.region = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 13
        newRow.street = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 14
        newRow.homeNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 15
        newRow.corpNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 16
        newRow.apartment = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 17
        newRow.surname = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 18
        newRow.name = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 19
        newRow.patronymic = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 20
        newRow.phone = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 21
        newRow.sumDividend = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 22
        newRow.dividendDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 23
        newRow.dividendNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 24
        newRow.dividendSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 25
        newRow.taxDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 26
        newRow.taxNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 27
        newRow.sumTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 28
        newRow.reportYear = parseDate(row.cell[xmlIndexCol].text(), "yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 28, 0)
    addTransportData(xml)
}


void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 1
        def xmlIndexCol = 1

        // графа 2
        newRow.emitent = row.cell[2].text()
        xmlIndexCol++

        // графа 3
        newRow.decreeNumber = row.cell[3].text()
        xmlIndexCol++

        // графа 4
        newRow.inn = row.cell[4].text()
        xmlIndexCol++

        // графа 5
        newRow.kpp = row.cell[5].text()
        xmlIndexCol++

        // графа 6
        newRow.recType = row.cell[6].text()
        xmlIndexCol++

        // графа 7
        newRow.recName = row.cell[7].text()
        xmlIndexCol++

        // графа 8
        newRow.zipCode = row.cell[8].text()
        xmlIndexCol++

        // графа 9
        newRow.recType = row.cell[9].text()
        xmlIndexCol++

        // графа 10
        newRow.area = row.cell[10].text()
        xmlIndexCol++

        // графа 11
        newRow.city = row.cell[11].text()
        xmlIndexCol++

        // графа 12
        newRow.region = row.cell[12].text()
        xmlIndexCol++

        // графа 13
        newRow.street = row.cell[13].text()
        xmlIndexCol++

        // графа 14
        newRow.homeNumber = row.cell[14].text()
        xmlIndexCol++

        // графа 15
        newRow.corpNumber = row.cell[15].text()
        xmlIndexCol++

        // графа 16
        newRow.apartment = row.cell[16].text()
        xmlIndexCol++

        // графа 17
        newRow.surname = row.cell[17].text()
        xmlIndexCol++

        // графа 18
        newRow.name = row.cell[18].text()
        xmlIndexCol++

        // графа 19
        newRow.patronymic = row.cell[19].text()
        xmlIndexCol++

        // графа 20
        newRow.phone = row.cell[20].text()
        xmlIndexCol++

        // графа 21
        newRow.sumDividend = parseNumber(row.cell[21].text(), rnuIndexRow, 21 + colOffset, logger, true)
        xmlIndexCol++

        // графа 22
        newRow.dividendDate = parseDate(row.cell[22].text(), "dd.MM.yyyy", rnuIndexRow, 22 + colOffset, logger, true)
        xmlIndexCol++

        // графа 23
        newRow.dividendNum = row.cell[23].text()
        xmlIndexCol++

        // графа 24
        newRow.dividendSum = parseNumber(row.cell[24].text(), rnuIndexRow, 24 + colOffset, logger, true)
        xmlIndexCol++

        // графа 25
        newRow.taxDate = parseDate(row.cell[25].text(), "dd.MM.yyyy", rnuIndexRow, 25 + colOffset, logger, true)
        xmlIndexCol++

        // графа 26
        newRow.taxNum = row.cell[26].text()
        xmlIndexCol++

        // графа 27
        newRow.sumTax = parseNumber(row.cell[27].text(), rnuIndexRow, 27 + colOffset, logger, true)
        xmlIndexCol++

        // графа 28
        newRow.reportYear = parseDate(row.cell[28].text(), "yyyy", rnuIndexRow, 28 + colOffset, logger, true)

        rows.add(newRow)
    }

    dataRowHelper.save(rows)
}

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}
