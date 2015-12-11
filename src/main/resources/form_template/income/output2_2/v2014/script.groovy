package form_template.income.output2_2.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
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
    /*case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break*/
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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
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

@Field
def allColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'title', 'zipCode', 'subdivisionRF', 'area',
                  'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic',
                  'phone', 'dividendDate', 'sumDividend', 'sumTax']

// Редактируемые атрибуты 2-23
@Field
def editableColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'title', 'zipCode', 'subdivisionRF', 'area',
                       'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic',
                       'phone', 'dividendDate', 'sumDividend', 'sumTax']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'title', 'subdivisionRF', 'dividendDate', 'sumDividend', 'sumTax']

@Field
def lastSourceFormType = 314 // с 9 месяцев 2015

@Field
def sourceFormTypes = [419, 10070, lastSourceFormType]

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
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def wasError = [false, false]

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
        // Проверки паттернов
        if (row.inn && checkPattern(logger, row, 'inn', row.inn, INN_JUR_PATTERN, wasError[1] ? null : INN_JUR_MEANING, true)) {
            checkControlSumInn(logger, row, 'inn', row.inn, false)
        } else if (row.inn){
            wasError[1] = true
        }
        if (row.kpp && !checkPattern(logger, row, 'kpp', row.kpp, KPP_PATTERN, wasError[2] ? null : KPP_MEANING, true)) {
            wasError[2] = true
        }
        // Проверка формата дат
        if (row.dividendDate) {
            checkDateValid(logger, row, 'dividendDate', row.dividendDate, true)
        }
    }
}

void consolidation() {
    def rows = []

    def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
    // на дату корректировки по ключу строки (состоящему из граф строки) находит строку
    Map<String, DataRow<Cell>> fullCorrMap = [:]
    Map<String, DataRow<Cell>> tinyCorrMap = [:]

    if (departmentReportPeriod.correctionDate != null) {
        // получить дату корректировки
        def correctionDate = departmentReportPeriod.correctionDate
        // получить последнюю форму по корректировкам
        def formDataCorrection = formDataService.getLastByDate(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId, formData.periodOrder, correctionDate ? (correctionDate - 1) : null, formData.comparativePeriodId, formData.accruing)
        if (formDataCorrection != null && formDataCorrection.id != formData.id) {
            def dataRowsCorr = formDataService.getDataRowHelper(formDataCorrection).allSaved
            // карта строк по почти всем графам
            fullCorrMap = getDataRowsMap(dataRowsCorr, true)
            // карта строк по двум графам
            tinyCorrMap = getDataRowsMap(dataRowsCorr, false)
        }
    }

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if(sourceFormTypes.contains(it.formTypeId)) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceHelper = formDataService.getDataRowHelper(sourceFormData)
                sourceHelper.allSaved.each { sourceRow ->
                    // До периода формы «9 месяцев 2015» «Графа 17» = 1 и «Графа 16» = 1 и «Графа 22» = «0» или «9» или «13»
                    // Начиная с периода формы «9 месяцев 2015» «Графа 17» = 1 и «Графа 16» <> 2 и «Графа 22» = «0» или «9» или «13»
                    if (sourceRow.status == 1 &&
                            (it.formTypeId != lastSourceFormType && sourceRow.type == 1 || it.formTypeId == lastSourceFormType && sourceRow.type != 2) &&
                            (sourceRow.rate == 0 || sourceRow.rate == 9 || sourceRow.rate == 13)) {
                        def newRow = formNewRow(sourceRow, departmentReportPeriod, fullCorrMap, tinyCorrMap, rows.size())
                        rows.add(newRow)
                    }
                }
            }
        }
    }

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

Map<String,DataRow<Cell>> getDataRowsMap(def dataRows, boolean isComplex) {
    Map<String,DataRow<Cell>> map = [:]
    if (dataRows == null)
        return map
    dataRows.each { row ->
        String key = isComplex ? getComplexRowKey(row) : getSimpleRowKey(row)
        map[key] = row
    }
    return map
}

// делаем сложный ключ по всем значениям строки кроме 6-й графы
String getComplexRowKey(def row) {
    return (allColumns - 'recType').collect { alias -> row[alias]?.toString()}.join("#")
}

// делаем простой ключ по ИНН и КПП
String getSimpleRowKey(def row) {
    return ['inn', 'kpp'].collect { alias -> row[alias]?.toString()}.join("#")
}

def formNewRow(def row, def departmentReportPeriod, def fullCorrectionMap, def tinyCorrectionMap, def rowSize) {
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

    //«Графа 6» - пользуем текущую строку, поэтому расчет после остальных граф
    newRow.recType = calc6(newRow, departmentReportPeriod, fullCorrectionMap, tinyCorrectionMap, rowSize)

    return newRow
}

def calc6(def row, def departmentReportPeriod, def fullCorrectionMap, def tinyCorrectionMap, def rowSize) {
    //если период формы не корректирующий
    if (departmentReportPeriod?.correctionDate == null) {
        return '00'
    } else {
        def dataRowCorrection = fullCorrectionMap[getComplexRowKey(row)]
        // если предыдущая корректировка не корректировка
        if (reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) == 1) {
            // если форма предыдущей корректировки содержит строку, идентичную текущей, кроме графы 6
            if (dataRowCorrection != null) {
                return "00"
            } else {
                return "01"
            }
        } else {
            if (dataRowCorrection != null) {
                // добавляем единичку к числу в строковом формате
                return dataRowCorrection.recType
            } else {
                // Если в форме предыдущего периода найдена строка, в которой графы 4 и 5 (ИНН и КПП получателя) равны графам 4 и 5 заполняемой строки текущей формы
                dataRowCorrection = tinyCorrectionMap[getSimpleRowKey(row)]
                if (dataRowCorrection != null) {
                    return (Integer.parseInt(dataRowCorrection.recType) + 1).toString().padLeft(2,"0")
                } else {
                    logger.warn("Строка %s: Графа «%s» заполнена Системой значением «00»! " +
                            "В форме предыдущего периода не найдена строка, в которой графа «%s» = «%s» и графа «%s» = «%s»",
                            rowSize + 1, getColumnName(row,'recType'), getColumnName(row,'inn'), row.inn, getColumnName(row,'kpp'), row.kpp
                    )
                    return "00"
                }
            }
        }
    }
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
    int COLUMN_COUNT = 23
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп.'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): '№ пп.']),
            ([(headerRows[0][1]): 'Эмитент']),
            ([(headerRows[0][2]): 'Номер решения о выплате дивидендов']),
            ([(headerRows[0][3]): 'Получатель']),
            ([(headerRows[0][7]): 'Место нахождения (адрес)']),
            ([(headerRows[0][16]): 'Руководитель организации']),
            ([(headerRows[0][19]): 'Контактный телефон']),
            ([(headerRows[0][20]): 'Дата перечисления дивидендов']),
            ([(headerRows[0][21]): 'Сумма дивидендов в рублях']),
            ([(headerRows[0][22]): 'Сумма налога в рублях']),
            ([(headerRows[1][3]): 'ИНН']),
            ([(headerRows[1][4]): 'КПП']),
            ([(headerRows[1][5]): 'Тип']),
            ([(headerRows[1][6]): 'Наименование']),
            ([(headerRows[1][7]): 'Индекс']),
            ([(headerRows[1][8]): 'Код региона']),
            ([(headerRows[1][9]): 'Район']),
            ([(headerRows[1][10]): 'Город']),
            ([(headerRows[1][11]): 'Населённый пункт']),
            ([(headerRows[1][12]): 'Улица']),
            ([(headerRows[1][13]): 'Номер дома (владения)']),
            ([(headerRows[1][14]): 'Номер корпуса (строения)']),
            ([(headerRows[1][15]): 'Номер квартиры (офиса)']),
            ([(headerRows[1][16]): 'Фамилия']),
            ([(headerRows[1][17]): 'Имя']),
            ([(headerRows[1][18]): 'Отчество'])
    ]
    (0..22).each { index ->
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
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
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def colIndex = 0

    // графа 2..5
    ['emitent', 'decreeNumber', 'inn', 'kpp'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 6
    colIndex++
    //Проверяется: если загруженное из Excel значение - цифра от 0 до 9, то спереди подставляется 0, чтобы получилось двузначное число.
    if (values[colIndex].matches("[0-9]")) {
        newRow.recType = "0" + values[colIndex]
    } else {
        newRow.recType = values[colIndex]
    }

    // графа 7
    colIndex++
    newRow.title = values[colIndex]

    // графа 8
    colIndex++
    newRow.zipCode = values[colIndex]

    // графа 9 - справочник "Коды субъектов Российской Федерации"
    //Проверяется: если загруженное из Excel значение - цифра от 0 до 9, то спереди подставляется 0, чтобы получилось двузначное число.
    colIndex++
    if (values[colIndex].matches("[0-9]")) {
        newRow.subdivisionRF = getRecordIdImport(4, 'CODE', "0" + values[colIndex], fileRowIndex, colIndex + colOffset, false)
    } else {
        newRow.subdivisionRF = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    }

    // графа 10..20
    ['area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 21
    colIndex++
    newRow.dividendDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 22
    colIndex++
    newRow.sumDividend = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex++
    newRow.sumTax = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}