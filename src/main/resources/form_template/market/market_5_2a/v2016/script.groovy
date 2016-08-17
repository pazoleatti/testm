package form_template.market.market_5_2a.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 5.2(а) Отчет о выданных Банком инструментах торгового финансирования
 * formTemplateId = 911
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNum          - № п/п
 * графа 2  - nameBank        - Наименование банка-эмитента
 * графа 3  - country         - Страна регистрации (местоположения)
 * графа 4  - swift           - SWIFT
 * графа 5  - creditRating    - Кредитный рейтинг
 * графа 6  - tool            - Референс инструмента
 * графа 7  - issueDate       - Дата выдачи
 * графа 8  - expireDate      - Дата окончания действия
 * графа 9  - period          - Срок обязательства (дней)
 * графа 10 - currency        - Валюта обязательства
 * графа 11 - sum             - Сумма обязательства, тыс. ед. валюты
 * графа 12 - payRate         - Плата, % годовых
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
def allColumns = ['rowNum', 'nameBank', 'country', 'swift', 'creditRating', 'tool', 'issueDate', 'expireDate', 'period', 'currency', 'sum', 'payRate']

// Редактируемые атрибуты
@Field
def editableColumns = ['nameBank', 'country', 'swift', 'creditRating', 'tool', 'issueDate', 'expireDate', 'period', 'currency', 'sum', 'payRate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['nameBank', 'swift', 'creditRating', 'tool', 'issueDate', 'expireDate', 'period', 'currency', 'sum', 'payRate']

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

@Field
def nonNegativeColumns = ['period', 'payRate']

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // групируем по графам 4, 6, 7
    def rowsMap = [:]
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        // 2. 9, 12 неотрицательность графы
        nonNegativeColumns.each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно 0!", row.getIndex(), getColumnName(row, alias))
            }
        }
        // 3. Положительность графы
        if (row.sum != null && row.sum <= 0) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше 0!", row.getIndex(), getColumnName(row, 'sum'))
        }
        // 4. Дата выдачи должна попадать в отчетный период
        checkDatePeriod(logger, row, 'issueDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)
        // 5. Проверка даты выдачи обязательства
        if (row.issueDate != null && row.expireDate != null && (row.expireDate < row.issueDate)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                    row.getIndex(), getColumnName(row, 'expireDate'), getColumnName(row, 'issueDate'))
        }
        // 6. Проверка валюты
        if (row.currency != null && ("RUB".equals(getRefBookValue(15, row.currency).CODE_2.value))) {
            logger.error("Строка %s: Для российского рубля должно быть проставлено буквенное значение RUR!", row.getIndex())
        }
        // 7. Проверка на отсутствие нескольких записей по одном и тому же ИТФ
        // группируем
        def key = getKey(row)
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        }
        rowsMap[key].add(row)
    }
    // 7. Проверка на отсутствие нескольких записей по одном и тому же ИТФ
    rowsMap.each { def key, rows ->
        def size = rows.size()
        // пропускаем первую строку
        for (int i = 1; i < size; i++) {
            def row = rows[i]
            logger.error("Строка %s: На форме уже существует строка со значениями граф «%s» = «%s», «%s» = «%s», «%s» = «%s»!",
                row.getIndex(), getColumnName(row, 'swift'), row.swift, getColumnName(row, 'tool'), row.tool, getColumnName(row, 'issueDate'), row.issueDate?.format('dd.MM.yyyy') ?: '')
        }
    }
}

String getKey(def row) {
    return (row.swift?.trim() + "#" + row.tool?.trim() + "#" + row.issueDate?.format('dd.MM.yyyy')).toLowerCase()
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // графа 2
        row.nameBank = calc2(row)
        // графа 3
        row.country = calc3(row)
    }
}

def calc2(def row) {
    return calc2or3(row, 'nameBank', 'NAME')
}

def calc3(def row) {
    return calc2or3(row, 'country', 'COUNTRY_CODE')
}

def calc2or3(def row, def calcAlias, def recordAlias) {
    def tmp = row[calcAlias]
    def records = getRecords520(row.swift)
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
    return getRecordsByValue(520L, value, ['SWIFT', 'INN', 'KIO'])
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
    int COLUMN_COUNT = 12
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
        return
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
    allColumns.each { alias ->
        if (['rowNum', 'nameBank', 'tool'].contains(alias)) {
            headerMapping.add(([(headerRows[0][index]): headers[0][alias]]))
        }
        headerMapping.add(([(headerRows[1][index]): headers[1][alias]]))
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
        index++
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
    newRow.nameBank = values[colIndex]
    debtorColIndex = colIndex + colOffset
    // графа 3
    colIndex++
    // заполняем в fillDebtorInfo
    countryString = values[colIndex]
    countryColIndex = colIndex + colOffset
    // графа 4
    colIndex++
    newRow.swift = values[colIndex]
    // графа 5
    colIndex++
    newRow.creditRating = getRecordIdImport(603L, 'SHORT_NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 6
    colIndex++
    newRow.tool = values[colIndex]
    // графа 7
    colIndex++
    newRow.issueDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)
    // графа 8
    colIndex++
    newRow.expireDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)
    // графа 9
    colIndex++
    newRow.period = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 10
    colIndex++
    newRow.currency = getRecordIdImport(15L, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 11
    colIndex++
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 12
    colIndex++
    newRow.payRate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Заполнение общей информации о заемщике при загрузке из Excel
    fillDebtorInfo(newRow, 'swift', 'nameBank', 'country', countryString, rowIndex, debtorColIndex, countryColIndex)
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
    // ищем по ИНН, КИО и SWIFT
    def debtorRecords = getRecords520(debtorNumber)
    if (debtorRecords?.size() > 1) {
        logger.warn("Строка %s: Найдено больше одной записи соотвествующей данным ИНН/КИО/SWIFT = %s", rowIndex, debtorNumber)
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

    if (debtorRecords == null || debtorRecords?.size() == 0) { // если в справочнике ТЦО записей нет
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
        def swift = debtorRecords[0].SWIFT?.stringValue
        def attrCode
        if (debtorNumber.equalsIgnoreCase(inn)) {
            attrCode = 'INN'
        } else if (debtorNumber.equalsIgnoreCase(kio)) {
            attrCode = 'KIO'
        } else if (debtorNumber.equalsIgnoreCase(swift)) {
            attrCode = 'SWIFT'
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
        updateIndexes(dataRows)
    }
}