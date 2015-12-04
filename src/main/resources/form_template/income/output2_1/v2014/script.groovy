package form_template.income.output2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
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
// графа 12 - corpNumber
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
            formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
def sourceFormTypeAlt = 419

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
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (!dataRows.isEmpty()) {
        def number = 0
        for (def row in dataRows) {
            row.rowNumber = ++number
            row.dividendSum = (row.sumDividend ?: 0) - (row.sumTax ?: 0)
        }
    }

    sortFormDataRows(false)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
    }
}

void consolidation() {
    def rows = []

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == sourceFormType || it.formTypeId == sourceFormTypeAlt) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceHelper = formDataService.getDataRowHelper(sourceFormData)
                sourceHelper.allSaved.each { sourceRow ->
                    // «Графа 17» = «RUS» и «Графа 16» = 1 и «Графа 22» = «0» или «9»
                    if (sourceRow.status == 1 && sourceRow.type == 1 && (sourceRow.rate == 0 || sourceRow.rate == 9 || sourceRow.rate == 13)) {
                        def newRow = formNewRow(sourceRow)
                        rows.add(newRow)
                    }
                }
            }
        }
    }
    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
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

void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 25
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)

    // для проверки шапки
    def headerMapping = [
            (headerRows[0][0]) : '№ пп.',
            (headerRows[0][1]) : 'Эмитент',
            (headerRows[0][2]) : 'Номер решения о выплате дивидендов',
            (headerRows[0][3]) : 'Получатель',
            (headerRows[0][4]) : 'Место нахождения (адрес)',
            (headerRows[0][13]): 'Руководитель организации',
            (headerRows[0][16]): 'Контактный телефон',
            (headerRows[0][17]): 'Сумма начисленных дивидендов',
            (headerRows[0][18]): 'Перечисление дивидендов',
            (headerRows[0][21]): 'Перечисление налога',
            (headerRows[0][24]): 'Отчётный год',
            (headerRows[1][4]) : 'Индекс',
            (headerRows[1][5]) : 'Код региона',
            (headerRows[1][6]) : 'Район',
            (headerRows[1][7]) : 'Город',
            (headerRows[1][8]) : 'Населённый пункт',
            (headerRows[1][9]) : 'Улица',
            (headerRows[1][10]): 'Номер дома (владения)',
            (headerRows[1][11]): 'Номер корпуса (строения)',
            (headerRows[1][12]): 'Номер офиса (квартиры)',
            (headerRows[1][13]): 'Фамилия',
            (headerRows[1][14]): 'Имя',
            (headerRows[1][15]): 'Отчество',
            (headerRows[1][18]): 'Дата',
            (headerRows[1][19]): 'Номер платёжного поручения',
            (headerRows[1][20]): 'Сумма',
            (headerRows[1][21]): 'Дата',
            (headerRows[1][22]): 'Номер платёжного поручения',
            (headerRows[1][23]): 'Сумма'
    ]
    (0..24).each { index ->
        headerMapping.put((headerRows[2][index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 1
    def colIndex = 0
    newRow.rowNumber = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2
    colIndex++
    newRow.emitent = values[colIndex]

    // графа 3
    colIndex++
    newRow.decreeNumber = values[colIndex]

    // графа 4
    colIndex++
    newRow.title = values[colIndex]

    // графа 5
    colIndex++
    newRow.zipCode = values[colIndex]

    // графа 6 - справочник "Коды субъектов Российской Федерации"
    colIndex++
    newRow.subdivisionRF = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // графа 7..17
    ['area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 18
    colIndex++
    newRow.sumDividend = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 19
    colIndex++
    newRow.dividendDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 20
    colIndex++
    newRow.dividendNum = values[colIndex]

    // графа 21
    colIndex++
    newRow.dividendSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 22
    colIndex++
    newRow.taxDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex++
    newRow.taxNum = values[colIndex]

    // графа 24
    colIndex++
    newRow.sumTax = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 25
    colIndex++
    newRow.reportYear = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}