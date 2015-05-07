package form_template.income.output2_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Сведения о дивидендах (доходах от долевого участия в других организациях,
 * созданных на территории Российской Федерации), выплаченных в отчетном квартале
 *
 * formTemplateId=416
 */

// графа 1  - rowNumber
// графа 2  - emitent
// графа 3  - decreeNumber
// графа 4  - inn
// графа 5  - kpp
// графа 6  - recType
// графа 7  - title
// графа 8  - zipCode
// графа 9  - subdivisionRF    - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 10 - area
// графа 11 - city
// графа 12 - region
// графа 13 - street
// графа 14 - homeNumber
// графа 15 - corpNumber
// графа 16 - apartment
// графа 17 - surname
// графа 18 - name
// графа 19 - patronymic
// графа 20 - phone
// графа 21 - dividendDate
// графа 22 - sumDividend
// графа 23 - sumTax

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
        break
    case FormDataEvent.IMPORT:
        importData()
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

// Редактируемые атрибуты 2-23
@Field
def editableColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'title', 'zipCode', 'subdivisionRF', 'area',
                       'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic',
                       'phone', 'dividendDate', 'sumDividend', 'sumTax']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'title', 'dividendDate', 'sumDividend', 'sumTax']

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

void calc() {
    // расчетов нет
    sortFormDataRows()
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()
        def recType = (String) row.recType;
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка одновременного заполнения/не заполнения «Графы 17» и «Графы 18»
        if (row.surname == null && row.name != null || row.name == null && row.surname != null) {
            rowError(logger, row, "Строка ${rowNum}: Графы «Фамилия» и «Имя» должны быть заполнены одновременно (либо обе графы не должны заполняться)!")
        }
        // Проверка допустимых значений «Графы 6» (диапазон 00...99)
        if (!recType?.matches("[0-9]{2}")) {
            logger.error("Строка ${rowNum}: Графа «Получатель. Тип» заполнена неверно!")
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if(it.formTypeId == sourceFormType) {
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
    //«Графа 4» = «Графа 14» первичной формы
    newRow.inn = row.inn
    //«Графа 5» = «Графа 15» первичной формы
    newRow.kpp = row.kpp
    //«Графа 6» = «00»
    newRow.recType = '00'
    //«Графа 7» = «Графа 13» первичной формы
    newRow.title = row.addresseeName
    //«Графа 8» = «Графа 30» первичной формы
    newRow.zipCode = row.postcode
    //«Графа 9» = «Графа 31» первичной формы
    newRow.subdivisionRF = row.region
    //«Графа 10» = «Графа 32» первичной формы
    newRow.area = row.district
    //«Графа 11» = «Графа 33» первичной формы
    newRow.city = row.city
    //«Графа 12» = «Графа 34» первичной формы
    newRow.region = row.locality
    //«Графа 13» = «Графа 35» первичной формы
    newRow.street = row.street
    //«Графа 14» = «Графа 36» первичной формы
    newRow.homeNumber = row.house
    //«Графа 15» = «Графа 37» первичной формы
    newRow.corpNumber = row.housing
    //«Графа 16» = «Графа 38» первичной формы
    newRow.apartment = row.apartment
    //«Графа 17» = «Графа 39» первичной формы
    newRow.surname = row.surname
    //«Графа 18» = «Графа 40» первичной формы
    newRow.name = row.name
    //«Графа 19» = «Графа 41» первичной формы
    newRow.patronymic = row.patronymic
    //«Графа 20» = «Графа 42» первичной формы
    newRow.phone = row.phone
    //«Графа 21» = «Графа 25» первичной формы
    newRow.dividendDate = row.date
    //«Графа 22» = «Графа 23» первичной формы
    newRow.sumDividend = row.dividends
    //«Графа 23» = «Графа 27» первичной формы
    newRow.sumTax = row.withheldSum

    return newRow
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп.', null, 23, 3)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 23, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : '№ пп.',
            (xml.row[0].cell[1]) : 'Эмитент',
            (xml.row[0].cell[2]) : 'Номер решения о выплате дивидендов',
            (xml.row[0].cell[3]) : 'Получатель',
            (xml.row[0].cell[7]) : 'Место нахождения (адрес)',
            (xml.row[0].cell[16]): 'Руководитель организации',
            (xml.row[0].cell[19]): 'Контактный телефон',
            (xml.row[0].cell[20]): 'Дата перечисления дивидендов',
            (xml.row[0].cell[21]): 'Сумма дивидендов в рублях',
            (xml.row[0].cell[22]): 'Сумма налога в рублях',
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
            (xml.row[1].cell[18]): 'Отчество'
    ]
    (0..22).each { index ->
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

    // количество граф в таблице
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

        def newRow = formData.createStoreMessagingDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 1
        xmlIndexCol = 1

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
        //Проверяется: если загруженное из Excel значение - цифра от 0 до 9, то спереди подставляется 0, чтобы получилось двузначное число.
        if (row.cell[xmlIndexCol].text().matches("[0-9]")) {
            newRow.recType = "0" + row.cell[xmlIndexCol].text()
        } else {
            newRow.recType = row.cell[xmlIndexCol].text()
        }
        xmlIndexCol++

        // графа 7
        newRow.title = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.zipCode = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 9 - справочник "Коды субъектов Российской Федерации"
        //Проверяется: если загруженное из Excel значение - цифра от 0 до 9, то спереди подставляется 0, чтобы получилось двузначное число.
        if (row.cell[xmlIndexCol].text().matches("[0-9]")) {
            newRow.subdivisionRF = getRecordIdImport(4, 'CODE', "0" + row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)
        } else {
            newRow.subdivisionRF = getRecordIdImport(4, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)
        }
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
        newRow.dividendDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 22
        newRow.sumDividend = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 23
        newRow.sumTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

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