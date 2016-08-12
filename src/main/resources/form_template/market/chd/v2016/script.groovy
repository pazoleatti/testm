package form_template.market.chd.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Отчет о кредитах (ЦХД)
 * formTemplateId = 903
 *
 * @author Stanislav Yasinskiy
 *
 * графа 1  - rowNum          - Наименование заёмщика
 * графа 2  - name            - Наименование заёмщика
 * графа 3  - opf             - ОПФ
 * графа 4  - country         - Страна регистрации (местоположения заемщика)
 * графа 5  - innKio          - ИНН / КИО заёмщика
 * графа 6  - creditRating    - Кредитный рейтинг / класс кредитоспособности
 * графа 7  - docNum          - Номер кредитного договора
 * графа 8  - docDate        - Дата кредитного договора (дд.мм.гг.)
 * графа 9  - docDate2        - Дата выдачи (дд.мм.гг.)
 * графа 10 - docDate3        - Дата планируемого погашения с учетом последней пролонгации (дд.мм.гг.)
 * графа 11 - partRepayment   - Частичное погашение основного долга (Да / Нет)
 * графа 12 - creditPeriod    - Срок кредита, лет
 * графа 13 - currencyCode    - Валюта суммы кредита
 * графа 14 - creditSum       - Сумма кредита (по договору), ед. валюты
 * графа 15 - creditRate      - Процентная ставка, % годовых
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['rowNum', 'name', 'opf', 'country', 'innKio', 'creditRating', 'docNum', 'docDate', 'docDate2',
                  'docDate3', 'partRepayment', 'creditPeriod', 'currencyCode', 'creditSum', 'creditRate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'opf', 'country', 'innKio', 'creditRating', 'docNum', 'docDate', 'docDate2',
                       'docDate3', 'partRepayment', 'creditPeriod', 'currencyCode', 'creditSum', 'creditRate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'opf', 'innKio', 'docNum', 'docDate', 'docDate3', 'partRepayment','creditPeriod',
                       'currencyCode', 'creditSum', 'creditRate']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

@Field
def refBookCache = [:]

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // групируем по графам 5, 7, 8
    def rowsMap = [:]
    for (row in dataRows) {

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Неотрицательность графы
        ['creditPeriod', 'creditSum'].each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно 0!", row.getIndex(), getColumnName(row, alias))
            }
        }

        // 3. Проверка даты кредитного договора
        checkDatePeriod(logger, row, 'docDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // 4. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
        // группируем
        def key = getKey(row)
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        }
        rowsMap[key].add(row)

        // 5. Проверка даты выдачи кредита
        if (row.docDate != null && row.docDate2 != null && row.docDate2 < row.docDate) {
            logger.warn("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                    row.getIndex(), getColumnName(row, 'docDate2'), getColumnName(row, 'docDate'))
        }

        // 6. Проверка даты погашения кредита
        if (row.docDate != null && row.docDate3 != null && row.docDate3 < row.docDate) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                    row.getIndex(), getColumnName(row, 'docDate3'), getColumnName(row, 'docDate'))
        }

        // 7. Проверка даты погашения кредита 2
        if (row.docDate2 != null && row.docDate3 != null && row.docDate3 < row.docDate2) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                    row.getIndex(), getColumnName(row, 'docDate3'), getColumnName(row, 'docDate2'))
        }
    }

    // 4. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
    rowsMap.each { def key, rows ->
        def size = rows.size()
        // пропускаем первую строку
        for (int i = 1; i < size; i++) {
            def row = rows[i]
            logger.error("Строка %s: На форме уже существует строка со значениями граф «%s» = «%s», «%s» = «%s», «%s» = «%s»!",
                    row.getIndex(), getColumnName(row, 'innKio'), row.innKio, getColumnName(row, 'docNum'), row.docNum, getColumnName(row, 'docDate'), row.docDate?.format('dd.MM.yyyy') ?: '')
        }
    }
}

String getKey(def row) {
    return (row.innKio?.trim() + "#" + row.docNum?.trim() + "#" + row.docDate?.format('dd.MM.yyyy')).toLowerCase()
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // графа 2
        row.name = calc2(row)
        // графа 4
        row.country = calc4(row)
    }
}

def calc2(def row) {
    return calc2or4(row, 'name', 'NAME')
}

def calc4(def row) {
    return calc2or4(row, 'country', 'COUNTRY_CODE')
}

def calc2or4(def row, def calcAlias, def recordAlias) {
    def tmp = row[calcAlias]
    def records = getRecords520(row.innKio)
    if (records != null && records.size() == 1) {
        tmp = records.get(0)[recordAlias].value
    }
    return tmp
}

/**
 * Получить список записей из справочника "Участники ТЦО" (id = 520) по ИНН или КИО.
 *
 * @param value значение для поиска по совпадению
 */
def getRecords520(def value) {
    return getRecordsByValue(520L, value, ['INN', 'KIO'])
}

/**
 * Получить список записей из справочника "ОК 025-2001 (Общероссийский классификатор стран мира)" (id = 10) по краткому и полному наименованию.
 *
 * @param value значение для поиска по совпадению
 */
def getRecords10(def value) {
    return getRecordsByValue(10L, value, ['FULLNAME', 'NAME'])
}

// мапа хранящая мапы с записями справочника (ключ "id справочника" -> мапа с записями, ключ "значение атрибута" -> список записией)
// например:
// [ id 520 : мапа с записям ]
//      мапа с записями = [ инн 1234567890 : список подходящих записей ]
@Field
def recordsMap = [:]

/**
 * Получить список записей из справочника атрибуты которых равны заданному значению.
 *
 * @param refBookId id справочника
 * @param value значение для поиска
 * @param attributesForSearch список атрибутов справочника по которым искать совпадения
 */
def getRecordsByValue(def refBookId, def value, def attributesForSearch) {
    if (recordsMap[refBookId] == null) {
        recordsMap[refBookId] = [:]
        // получить все записи справочника и засунуть в мапу
        def allRecords = getAllRecords(refBookId)?.values()
        allRecords.each { record ->
            attributesForSearch.each { attribute ->
                def tmpKey = getKeyValue(record[attribute]?.value)
                if (tmpKey) {
                    if (recordsMap[refBookId][tmpKey] == null) {
                        recordsMap[refBookId][tmpKey] = []
                    }
                    if (!recordsMap[refBookId][tmpKey].contains(record)) {
                        recordsMap[refBookId][tmpKey].add(record)
                    }
                }
            }
        }
    }
    def key = getKeyValue(value)
    return recordsMap[refBookId][key]
}

def getKeyValue(def value) {
    return value?.trim()?.toLowerCase()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 15
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
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

    // формирование строк нф
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping =[[:]]
    def index = 0
    headerMapping.add([(headerRows[0][0]): '№ п/п'])
    headerMapping.add([(headerRows[0][1]): 'Заёмщик'])
    headerMapping.add([(headerRows[0][7]): 'Кредитный договор'])
    allColumns.each { alias ->
        headerMapping.add(([(headerRows[1][index]): headers[1][alias]]))
        index++
    }
    headerMapping.add([(headerRows[2][0]): '1'])
    headerMapping.add([(headerRows[2][1]): '2.1'])
    headerMapping.add([(headerRows[2][2]): '2.2'])
    (3..15).each {
        headerMapping.add([(headerRows[2][it]): ''+it])
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
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def required = true
    def countryString
    def countryColIndex
    def debtorColIndex

    // графа 2
    def colIndex = 1
    newRow.name = values[colIndex]
    debtorColIndex = colIndex + colOffset
    // графа 3
    colIndex++
    newRow.opf = getRecordIdImport(605L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 4
    colIndex++
    // заполняем в fillDebtorInfo
    countryString = values[colIndex]
    countryColIndex = colIndex + colOffset
    // графа 5
    colIndex++
    newRow.innKio = values[colIndex]
    // графа 6
    colIndex++
    newRow.creditRating = getRecordIdImport(604L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 7
    colIndex++
    newRow.docNum = values[colIndex]
    // графа 8
    colIndex++
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)
    // графа 9
    colIndex++
    newRow.docDate2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)
    // графа 10
    colIndex++
    newRow.docDate3 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)
    // графа 11
    colIndex++
    newRow.partRepayment = getRecordIdImport(38L, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 12
    colIndex++
    newRow.creditPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 13
    colIndex++
    newRow.currencyCode = getRecordIdImport(15L, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 14
    colIndex++
    newRow.creditSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 15
    colIndex++
    newRow.creditRate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Заполнение общей информации о заемщике при загрузке из Excel
    fillDebtorInfo(newRow, 'innKio', 'name', 'country', countryString, rowIndex, debtorColIndex, countryColIndex)
    return newRow
}

def getNewRow() {
    def newRow = formData.createStoreMessagingDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

@Field
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}

void fillDebtorInfo(def newRow, def numberAlias, def debtorAlias, def countryAlias, def countryString, def rowIndex, def debtorIndex, def countryIndex) {
    String debtorNumber = newRow[numberAlias]
    String fileDebtorName = newRow[debtorAlias]
    if (debtorNumber == null || debtorNumber.isEmpty()) {
        return
    }
    // ищем по ИНН и КИО
    def debtorRecords = getRecords520(debtorNumber)
    if (debtorRecords?.size() > 1) {
        logger.warn("Строка %s: Найдено больше одной записи соотвествующей данным ИНН/КИО = %s", rowIndex, debtorNumber)
        return
    }

    // определение страны
    def countryRecord = null
    if (countryString) {
        def countryId = (debtorRecords?.size() == 1 ? debtorRecords?.get(0)?.COUNTRY_CODE?.value : null)
        if (countryId) {
            // страна определена по записи справочнкиа "Участники ТЦО"
            countryRecord = getAllRecords(10L).get(countryId)
            def shortName = countryRecord?.NAME?.value
            def fullName = countryRecord?.FULLNAME?.value
            if (!shortName.equalsIgnoreCase(countryString) && !fullName.equalsIgnoreCase(countryString)) {
                logger.warn("Строка %s, столбец %s содержит значение «%s», которое не соответствует справочному значению «%s», «%s» " +
                        "графы «Страна регистрации (местоположения заемщика)», найденному для «%s»!",
                        rowIndex, getXLSColumnName(countryIndex), countryString, shortName, fullName, newRow[debtorAlias])
            }
        } else {
            // поиск страны по справочнику стран мира
            def countryRecords = getRecords10(countryString)
            if (countryRecords != null && !countryRecords.isEmpty() && countryRecords.size() == 1) {
                countryRecord = countryRecords[0]
            }
            if (countryRecord == null) {
                logger.warn("Строка %s, столбец %s: Страна с названием «%s» не найдена в справочнике «ОК 025-2001 (Общероссийский классификатор стран мира)»",
                        rowIndex, getXLSColumnName(countryIndex), countryString)
            }
        }
        newRow[countryAlias] = countryRecord?.record_id?.value
    }

    if (debtorRecords == null || debtorRecords.size() == 0) { // если в справочнике ТЦО записей нет
        return
    }
    // else
    // запись в справочнике ТЦО найдена, то берем данные из нее
    newRow[debtorAlias] = debtorRecords[0].NAME?.stringValue ?: ""
    newRow[countryAlias] = debtorRecords[0].COUNTRY_CODE?.value
    if (! newRow[debtorAlias].equalsIgnoreCase(fileDebtorName)) {
        def refBook = refBookFactory.get(520)
        def inn = debtorRecords[0].INN?.stringValue
        def kio = debtorRecords[0].KIO?.stringValue
        def attrCode
        if (debtorNumber.equalsIgnoreCase(inn)) {
            attrCode = 'INN'
        } else if (debtorNumber.equalsIgnoreCase(kio)) {
            attrCode = 'KIO'
        }
        def refBookAttrName = refBook.getAttribute(attrCode).name
        if (fileDebtorName) {
            logger.warn("Строка %s, столбец %s: На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "В файле указано другое наименование заемщика - «%s»!",
                    rowIndex, getXLSColumnName(debtorIndex), newRow[debtorAlias], refBookAttrName, newRow[numberAlias], fileDebtorName)
        } else {
            logger.warn("Строка %s, столбец %s: На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "Наименование заемщика в файле не заполнено!",
                    rowIndex, getXLSColumnName(debtorIndex), newRow[debtorAlias], refBookAttrName, newRow[numberAlias])
        }
    }
}

// Сортировка групп и строк
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