package form_template.income.output2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
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
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
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

// Редактируемые атрибуты 2-25
@Field
def editableColumns = ['emitent', 'decreeNumber', 'title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region',
                       'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone',
                       'sumDividend', 'dividendDate', 'dividendNum', 'taxDate', 'taxNum', 'sumTax', 'reportYear']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'emitent', 'decreeNumber', 'title', 'subdivisionRF', 'surname', 'name', 'sumDividend', 'dividendDate', 'sumTax']

@Field
def sourceFormType = 10070

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}
//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Получение Id записи с использованием кэширования
def getRecordId(def ref_id, String alias, String value, Date date) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
    if (recordCache[ref_id] != null) {
        if (recordCache[ref_id][filter] != null) {
            return recordCache[ref_id][filter]
        }
    } else {
        recordCache[ref_id] = [:]
    }
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    if (records.size() == 1) {
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    }
    return null
}

void calc() {
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

    sortFormDataRows()
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == sourceFormType) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceHelper = formDataService.getDataRowHelper(sourceFormData)
                sourceHelper.getAll().each { sourceRow ->
                    // «Графа 17» = «RUS» и «Графа 16» = 1 и «Графа 22» = «0» или «9»
                    if ('RUS'.equals(sourceRow.status) && sourceRow.type == 1 && (sourceRow.rate == 0 || sourceRow.rate == 9)) {
                        def newRow = formNewRow(sourceRow)
                        dataRows.add(newRow)
                    }
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

def formNewRow(def row) {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    //«Графа 2» = «Графа 2» первичной формы
    newRow.emitent = row.emitentName
    //«Графа 3» = «Графа 7» первичной формы
    newRow.decreeNumber = row.decisionNumber
    //«Графа 4» = «Графа 13» первичной формы
    newRow.title = row.addresseeName
    //«Графа 5» = «Графа 30» первичной формы
    newRow.zipCode = row.postcode
    //«Графа 6» = «Графа 31» первичной формы
    newRow.subdivisionRF = getRecordId(4, 'CODE', row.region, getReportPeriodEndDate())
    //«Графа 7» = «Графа 32» первичной формы
    newRow.area = row.district
    //«Графа 8» = «Графа 33» первичной формы
    newRow.city = row.city
    //«Графа 9» = «Графа 34» первичной формы
    newRow.region = row.locality
    //«Графа 10» = «Графа 35» первичной формы
    newRow.street = row.street
    //«Графа 11» = «Графа 36» первичной формы
    newRow.homeNumber = row.house
    //«Графа 12» = «Графа 37» первичной формы
    newRow.corpNumber = row.housing
    //«Графа 13» = «Графа 38» первичной формы
    newRow.apartment = row.apartment
    //«Графа 14» = «Графа 39» первичной формы
    newRow.surname = row.surname
    //«Графа 15» = «Графа 40» первичной формы
    newRow.name = row.name
    //«Графа 16» = «Графа 41» первичной формы
    newRow.patronymic = row.patronymic
    //«Графа 17» = «Графа 42» первичной формы
    newRow.phone = row.phone
    //«Графа 18» = «Графа 12» первичной формы
    newRow.sumDividend = row.dividends
    //«Графа 19» = «Графа 25» первичной формы
    newRow.dividendDate = row.date
    //«Графа 24» = «Графа 27» первичной формы
    newRow.sumTax = row.withheldSum

    return newRow
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

        def newRow = formData.createStoreMessagingDataRow()
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
        newRow.reportYear = parseDate(row.cell[xmlIndexCol].text(), "yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).save(rows)
    }
}

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}
