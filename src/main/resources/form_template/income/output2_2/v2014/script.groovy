package form_template.income.output2_2.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

// Редактируемые атрибуты 2-23
@Field
def editableColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'title', 'zipCode', 'subdivisionRF', 'area',
                       'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic',
                       'phone', 'dividendDate', 'sumDividend', 'sumTax']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['emitent', 'decreeNumber', 'inn', 'kpp', 'recType', 'title', 'dividendDate', 'sumDividend', 'sumTax']

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
            checkControlSumInn(logger, row, 'inn', row.inn, true)
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

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if(sourceFormTypes.contains(it.formTypeId)) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceHelper = formDataService.getDataRowHelper(sourceFormData)
                sourceHelper.allSaved.each { sourceRow ->
                    // До периода формы «9 месяцев 2015» «Графа 17» = 1 и «Графа 16» = 1 и «Графа 22» = «0» или «9» или «13»
                    // Начиная с периода формы «9 месяцев 2015» «Графа 17» = 1 и «Графа 16» <> 2 и «Графа 22» = «0» или «9» или «13»
                    if (sourceRow.status == 1 &&
                            (it.formTypeId != lastSourceFormType && sourceRow.type == 1 || it.formTypeId == lastSourceFormType && sourceRow.type != 2) &&
                            (sourceRow.rate == 0 || sourceRow.rate == 9 || sourceRow.rate == 13)) {
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
            (headerRows[0][0]) : '№ пп.',
            (headerRows[0][1]) : 'Эмитент',
            (headerRows[0][2]) : 'Номер решения о выплате дивидендов',
            (headerRows[0][3]) : 'Получатель',
            (headerRows[0][7]) : 'Место нахождения (адрес)',
            (headerRows[0][16]): 'Руководитель организации',
            (headerRows[0][19]): 'Контактный телефон',
            (headerRows[0][20]): 'Дата перечисления дивидендов',
            (headerRows[0][21]): 'Сумма дивидендов в рублях',
            (headerRows[0][22]): 'Сумма налога в рублях',
            (headerRows[1][3]) : 'ИНН',
            (headerRows[1][4]) : 'КПП',
            (headerRows[1][5]) : 'Тип',
            (headerRows[1][6]) : 'Наименование',
            (headerRows[1][7]) : 'Индекс',
            (headerRows[1][8]) : 'Код региона',
            (headerRows[1][9]) : 'Район',
            (headerRows[1][10]): 'Город',
            (headerRows[1][11]): 'Населённый пункт',
            (headerRows[1][12]): 'Улица',
            (headerRows[1][13]): 'Номер дома (владения)',
            (headerRows[1][14]): 'Номер корпуса (строения)',
            (headerRows[1][15]): 'Номер квартиры (офиса)',
            (headerRows[1][16]): 'Фамилия',
            (headerRows[1][17]): 'Имя',
            (headerRows[1][18]): 'Отчество'
    ]
    (0..22).each { index ->
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

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 23
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try{
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (!isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    showMessages(newRows, logger)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [
                'sumDividend'        : 22,
                'sumTax'             : 23
        ]
        // итоговая строка для сверки сумм
        def totalTmp = formData.createDataRow()
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }
        // подсчет итогов
        for (def row : newRows) {
            if (row.getAlias()) {
                continue
            }
            totalColumnsIndexMap.keySet().asList().each { alias ->
                def value1 = totalTmp.getCell(alias).value
                def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
                totalTmp.getCell(alias).setValue(value1 + value2, null)
            }
        }

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return newRow
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 2..5
    ['emitent', 'decreeNumber', 'inn', 'kpp'].each { alias ->
        colIndex++
        newRow[alias] = pure(rowCells[colIndex])
    }

    // графа 6
    colIndex++
    //Проверяется: если загруженное из Excel значение - цифра от 0 до 9, то спереди подставляется 0, чтобы получилось двузначное число.
    if (pure(rowCells[colIndex]).matches("[0-9]")) {
        newRow.recType = "0" + pure(rowCells[colIndex])
    } else {
        newRow.recType = pure(rowCells[colIndex])
    }

    // графа 7
    colIndex++
    newRow.title = pure(rowCells[colIndex])

    // графа 8
    colIndex++
    newRow.zipCode = pure(rowCells[colIndex])

    // графа 9 - справочник "Коды субъектов Российской Федерации"
    //Проверяется: если загруженное из Excel значение - цифра от 0 до 9, то спереди подставляется 0, чтобы получилось двузначное число.
    colIndex++
    def code = pure(rowCells[colIndex])
    if (code) {
        if (code.matches("[0-9]")) {
            newRow.subdivisionRF = getRecordIdImport(4, 'CODE', "0" + code, fileRowIndex, colIndex + colOffset, false)
        } else {
            newRow.subdivisionRF = getRecordIdImport(4, 'CODE', code, fileRowIndex, colIndex + colOffset, false)
        }
    }

    // графа 10..20
    ['area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone'].each { alias ->
        colIndex++
        newRow[alias] = pure(rowCells[colIndex])
    }

    // графа 21
    colIndex++
    newRow.dividendDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 22
    colIndex++
    newRow.sumDividend = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex++
    newRow.sumTax = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}