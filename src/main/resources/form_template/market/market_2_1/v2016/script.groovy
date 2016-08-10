package form_template.market.market_2_1.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * 2.1 (Ежемесячный) Реестр выданных Банком гарантий (контргарантий, поручительств).
 *
 * formTemplateId = 909
 * formType = 909
 */

// графа 1  (1.1)  - code
// графа 2  (1.2)  - name
// графа 3  (2)    - rowNum
// графа 4  (3)    - guarantor
// графа 5  (3.1)  - vnd
// графа 6  (3.2)  - level
// графа 7  (4.1)  - procuct1
// графа 8  (4.2)  - procuct2
// графа 9  (4.3)  - procuct3
// графа 10 (5)    - taxpayerName
// графа 11 (6)    - taxpayerInn
// графа 12 (6.1)  - okved
// графа 13 (7.1)  - creditRating              - атрибут 6034 - SHORT_NAME - «Краткое наименование», справочник 603 «Кредитные рейтинги»
// графа 14 (7.2)  - creditClass               - атрибут 6012 - SHORT_NAME - «Краткое наименование», справочник 601 «Классы кредитоспособности»
// графа 15 (8)    - beneficiaryName
// графа 16 (9)    - beneficiaryInn
// графа 17 (10)   - emitentName
// графа 18 (11)   - instructingName
// графа 19 (12)   - number
// графа 20 (13)   - issuanceDate
// графа 21 (14)   - additionDate
// графа 22 (15)   - startDate
// графа 23 (16)   - conditionEffective
// графа 24 (17)   - endDate
// графа 25 (18.1) - sumInCurrency
// графа 26 (18.2) - sumInRub
// графа 27 (19)   - currency                  - атрибут 65 - CODE_2 - «Код валюты. Буквенный», справочник 15 «Общероссийский классификатор валют»
// графа 28 (20)   - debtBalance
// графа 29 (21.1) - isNonRecurring            - атрибут 250 - VALUE - «Значение», справочник 38 «Да/Нет»
// графа 30 (21.2) - paymentPeriodic
// графа 31 (22.1) - isCharged                 - атрибут 250 - VALUE - «Значение», справочник 38 «Да/Нет»
// графа 32 (22.2) - tariff
// графа 33 (22.3) - remuneration
// графа 34 (22.4) - remunerationStartYear
// графа 35 (22.5) - remunerationIssuance
// графа 36 (23)   - provide
// графа 37 (24)   - numberGuarantee
// графа 38 (25)   - numberAddition
// графа 39 (26)   - isGuaranetee              - атрибут 250 - VALUE - «Значение», справочник 38 «Да/Нет»
// графа 40 (26.1) - dateGuaranetee
// графа 41 (26.2) - sumGuaranetee
// графа 42 (26.3) - term
// графа 43 (26.4) - sumDiversion
// графа 44 (27.1) - arrears
// графа 45 (27.2) - arrearsDate
// графа 46 (28.1) - arrearsGuarantee
// графа 47 (28.2) - arrearsGuaranteeDate
// графа 48 (29)   - reserve
// графа 49 (30)   - comment
// графа 50 (31)   - segment

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
def allColumns = ['code', 'name', 'rowNum', 'guarantor', 'vnd', 'level', 'procuct1', 'procuct2', 'procuct3',
        'taxpayerName', 'taxpayerInn', 'okved', 'creditRating', 'creditClass', 'beneficiaryName', 'beneficiaryInn',
        'emitentName', 'instructingName', 'number', 'issuanceDate', 'additionDate', 'startDate', 'conditionEffective',
        'endDate', 'sumInCurrency', 'sumInRub', 'currency', 'debtBalance', 'isNonRecurring', 'paymentPeriodic',
        'isCharged', 'tariff', 'remuneration', 'remunerationStartYear', 'remunerationIssuance', 'provide',
        'numberGuarantee', 'numberAddition', 'isGuaranetee', 'dateGuaranetee', 'sumGuaranetee', 'term', 'sumDiversion',
        'arrears', 'arrearsDate', 'arrearsGuarantee', 'arrearsGuaranteeDate', 'reserve', 'comment', 'segment']

// Редактируемые атрибуты (графа 1, 2, 4..50)
@Field
def editableColumns = ['code', 'name', 'guarantor', 'vnd', 'level', 'procuct1', 'procuct2', 'procuct3', 'taxpayerName',
        'taxpayerInn', 'okved', 'creditRating', 'creditClass', 'beneficiaryName', 'beneficiaryInn', 'emitentName',
        'instructingName', 'number', 'issuanceDate', 'additionDate', 'startDate', 'conditionEffective', 'endDate',
        'sumInCurrency', 'sumInRub', 'currency', 'debtBalance', 'isNonRecurring', 'paymentPeriodic', 'isCharged',
        'tariff', 'remuneration', 'remunerationStartYear', 'remunerationIssuance', 'provide', 'numberGuarantee',
        'numberAddition', 'isGuaranetee', 'dateGuaranetee', 'sumGuaranetee', 'term', 'sumDiversion', 'arrears',
        'arrearsDate', 'arrearsGuarantee', 'arrearsGuaranteeDate', 'reserve', 'comment', 'segment']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..4, 7, 10, 11, 15, 16, 19, 20, 25..29, 31..37)
@Field
def nonEmptyColumns = ['code', 'name', 'rowNum', 'guarantor', 'procuct1', 'taxpayerName', 'taxpayerInn',
        'beneficiaryName', 'beneficiaryInn', 'number', 'issuanceDate', 'sumInCurrency',
        'sumInRub', 'currency', 'debtBalance', 'isNonRecurring', 'isCharged', 'tariff', 'remuneration',
        'remunerationStartYear', 'remunerationIssuance', 'provide', 'numberGuarantee']

@Field
def endDate = null

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

// графа 28, 32..35, 43, 44, 46, 48
@Field
def nonNegativeColumns = ['debtBalance', 'tariff', 'remuneration', 'remunerationStartYear',
        'remunerationIssuance', 'sumDiversion', 'arrears', 'arrearsGuarantee', 'reserve']

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def rowMap = [:]
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Неотрицательность графы
        nonNegativeColumns.each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно 0!", row.getIndex(), getColumnName(row, alias))
            }
        }

        // 3. Положительность графы
        ['sumInCurrency', 'sumInRub'].each { alias ->
            if (row[alias] != null && row[alias] <= 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше 0!", row.getIndex(), getColumnName(row, alias))
            }
        }

        // 4. Проверка даты выдачи кредита
        if (row.issuanceDate && row.endDate && (row.issuanceDate > row.endDate)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                row.getIndex(), getColumnName(row, 'endDate'), getColumnName(row, 'issuanceDate'))
        }

        // 5. Проверка валюты
        if (row.currency != null && ("RUB".equals(getRefBookValue(15, row.currency)?.CODE_2?.value))) {
            logger.error("Строка %s: Для российского рубля должно быть проставлено буквенное значение RUR!", row.getIndex())
        }

        // 6. Проверка на отсутствие нескольких записей по одном и тому же гарантийному обязательству
        def key = getKey(row)
        if (rowMap[key] == null) {
            rowMap[key] = row
        } else {
            logger.error("Строка %s: На форме уже существует строка со значениями граф «%s» = «%s», «%s» = «%s», «%s» = «%s»!",
                    row.getIndex(),
                    getColumnName(row, 'taxpayerInn'), row.taxpayerInn,
                    getColumnName(row, 'number'), row.number,
                    getColumnName(row, 'issuanceDate'), row.issuanceDate?.format('dd.MM.yyyy') ?: '')
        }
    }
}

/** Группировка по графе 11, 19, 20. */
String getKey(def row) {
    return (row.taxpayerInn?.trim() + "#" + row.number?.trim() + "#" + row.issuanceDate?.format('dd.MM.yyyy')).toLowerCase()
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

def getRecords(def inn) {
    def filter = "LOWER(INN) = LOWER('$inn') OR LOWER(KIO) = LOWER('$inn')".toString()
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    if (recordCache[filter] == null) {
        recordCache.put(filter, provider.getRecords(getReportPeriodEndDate(), null, filter, null))
    }
    return recordCache[filter]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        row.taxpayerName = calc10(row)
    }
}

@Field
def exclusiveInns = ['9999999999', '9999999998']

// аналогичный алгоритм в форме 2.6 (Ежемесячный) метод calc7()
def calc10(def row) {
    def tmp = row.taxpayerName
    if (!exclusiveInns.contains(row.taxpayerInn)) {
        def records = getRecords(row.taxpayerInn?.trim()?.toLowerCase())
        if (records != null && records.size() == 1) {
            tmp = records.get(0)?.NAME?.value
        }
    }
    return tmp
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 50
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('code').column.name
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
 * Проверить шапку таблицы.
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = []
    def index = 0
    allColumns.each { alias ->
        // первые графы в объединениях
        if (['procuct1', 'creditRating', 'sumInCurrency', 'isNonRecurring', 'isCharged', 'arrears', 'arrearsGuarantee'].contains(alias)) {
            headerMapping.add([(headerRows[0][index]): headers[0][alias]])
        }
        headerMapping.add([(headerRows[1][index]): headers[1][alias]])
        headerMapping.add([(headerRows[2][index]): headers[2][alias]])
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
    def taxpayerColIndex

    // графа 1  (1.1)
    def colIndex = 0
    newRow.code = values[colIndex]
    // графа 2  (1.2)
    colIndex++
    newRow.name = values[colIndex]
    // графа 3  (2)
    colIndex++
    // графа 4  (3)
    colIndex++
    newRow.guarantor = values[colIndex]
    // графа 5  (3.1)
    colIndex++
    newRow.vnd = values[colIndex]
    // графа 6  (3.2)
    colIndex++
    newRow.level = values[colIndex]
    // графа 7  (4.1)
    colIndex++
    newRow.procuct1 = values[colIndex]
    // графа 8  (4.2)
    colIndex++
    newRow.procuct2 = values[colIndex]
    // графа 9  (4.3)
    colIndex++
    newRow.procuct3 = values[colIndex]
    // графа 10 (5)
    colIndex++
    newRow.taxpayerName = values[colIndex]
    taxpayerColIndex = colIndex + colOffset
    // графа 11 (6)
    colIndex++
    newRow.taxpayerInn = values[colIndex]
    // графа 12 (6.1)
    colIndex++
    newRow.okved = values[colIndex]
    // графа 13 (7.1)
    colIndex++
    newRow.creditRating = getRecordIdImport(603L, 'SHORT_NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 14 (7.2)
    colIndex++
    newRow.creditClass = getRecordIdImport(601L, 'SHORT_NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 15 (8)
    colIndex++
    newRow.beneficiaryName = values[colIndex]
    // графа 16 (9)
    colIndex++
    newRow.beneficiaryInn = values[colIndex]
    // графа 17 (10)
    colIndex++
    newRow.emitentName = values[colIndex]
    // графа 18 (11)
    colIndex++
    newRow.instructingName = values[colIndex]
    // графа 19 (12)
    colIndex++
    newRow.number = values[colIndex]
    // графа 20 (13)
    colIndex++
    newRow.issuanceDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 21 (14)
    colIndex++
    newRow.additionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 22 (15)
    colIndex++
    newRow.startDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 23 (16)
    colIndex++
    newRow.conditionEffective = values[colIndex]
    // графа 24 (17)
    colIndex++
    newRow.endDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 25 (18.1)
    colIndex++
    newRow.sumInCurrency = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 26 (18.2)
    colIndex++
    newRow.sumInRub = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 27 (19)
    colIndex++
    newRow.currency = getRecordIdImport(15L, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 28 (20)
    colIndex++
    newRow.debtBalance = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 29 (21.1)
    colIndex++
    newRow.isNonRecurring = getRecordIdImport(38L, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 30 (21.2)
    colIndex++
    newRow.paymentPeriodic = values[colIndex]
    // графа 31 (22.1)
    colIndex++
    newRow.isCharged = getRecordIdImport(38L, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 32 (22.2)
    colIndex++
    newRow.tariff = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 33 (22.3)
    colIndex++
    newRow.remuneration = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 34 (22.4)
    colIndex++
    newRow.remunerationStartYear = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 35 (22.5)
    colIndex++
    newRow.remunerationIssuance = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 36 (23)
    colIndex++
    newRow.provide = values[colIndex]
    // графа 37 (24)
    colIndex++
    newRow.numberGuarantee = values[colIndex]
    // графа 38 (25)
    colIndex++
    newRow.numberAddition = values[colIndex]
    // графа 39 (26)
    colIndex++
    newRow.isGuaranetee = getRecordIdImport(38L, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    // графа 40 (26.1)
    colIndex++
    newRow.dateGuaranetee = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 41 (26.2)
    colIndex++
    newRow.sumGuaranetee = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 42 (26.3)
    colIndex++
    newRow.term = values[colIndex]
    // графа 43 (26.4)
    colIndex++
    newRow.sumDiversion = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 44 (27.1)
    colIndex++
    newRow.arrears = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 45 (27.2)
    colIndex++
    newRow.arrearsDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 46 (28.1)
    colIndex++
    newRow.arrearsGuarantee = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 47 (28.2)
    colIndex++
    newRow.arrearsGuaranteeDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, false)
    // графа 48 (29)
    colIndex++
    newRow.reserve = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
    // графа 49 (30)
    colIndex++
    newRow.comment = values[colIndex]
    // графа 50 (31)
    colIndex++
    newRow.segment = values[colIndex]

    // Заполнение общей информации о заемщике при загрузке из Excel
    fillDebtorInfo(newRow, 'taxpayerInn', 'taxpayerName', rowIndex, taxpayerColIndex)
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
Map<Long, Map<String, RefBookValue>> records520

Map<Long, Map<String, RefBookValue>> getRecords520() {
    if (records520 == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, 520, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        records520 = provider.getRecordData(uniqueRecordIds)
    }
    return records520
}

void fillDebtorInfo(def newRow, def numberAlias, def debtorAlias, def rowIndex, def debtorIndex) {
    // Найти множество записей справочника «Участники ТЦО», периоды актуальности которых содержат определенную выше дату актуальности
    Map<Long, Map<String, RefBookValue>> records = getRecords520()
    String debtorNumber = newRow[numberAlias]
    String fileDebtorName = newRow[debtorAlias]
    if (debtorNumber == null || debtorNumber.isEmpty() || exclusiveInns.contains(debtorNumber)) {
        return
    }
    // ищем по ИНН и КИО
    def debtorRecords = records.values().findAll { def refBookValueMap ->
        debtorNumber.equalsIgnoreCase(refBookValueMap.INN.stringValue) ||
                debtorNumber.equalsIgnoreCase(refBookValueMap.KIO.stringValue)
    }
    if (debtorRecords.size() > 1) {
        logger.warn("Строка %s: Найдено больше одной записи соотвествующей данным ИНН/КИО = %s", rowIndex, debtorNumber)
        return
    }
    if (debtorRecords.size() == 0) { // если в справочнике ТЦО записей нет
        return
    }
    // else
    // запись в справочнике ТЦО найдена, то берем данные из нее
    newRow.put(debtorAlias, debtorRecords[0].NAME?.stringValue ?: "")
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
        updateIndexes(dataRows)
    }
}