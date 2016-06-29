package form_template.market.mis.v2016

import com.aplana.sbrf.taxaccounting.model.ColumnType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
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
            logger.warn("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы «%s»!",
                    row.getIndex(), getColumnName(row, 'docDate2'), getColumnName(row, 'docDate'))
        }

        // 6. Проверка даты погашения кредита
        if (row.docDate != null && row.docDate3 != null && row.docDate3 < row.docDate) {
            logger.warn("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы «%s»!",
                    row.getIndex(), getColumnName(row, 'docDate3'), getColumnName(row, 'docDate'))
        }

        // 7. Проверка даты погашения кредита 2
        if (row.docDate2 != null && row.docDate3 != null && row.docDate3 < row.docDate2) {
            logger.warn("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы «%s»!",
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

def getRecords(def inn) {
    def filter = 'INN = \'' + inn + '\' OR KIO = \'' + inn + '\''
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    if (recordCache[filter] == null) {
        recordCache.put(filter, provider.getRecords(getReportPeriodEndDate(), null, filter, null))
    }
    return recordCache[filter]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        def records = getRecords(row.innKio)
        if (records.size() == 1) {
            row.name = records.get(0).RECORD_ID.value
            row.country = records.get(0).COUNTRY_CODE.value
        }
    }
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
        formDataService.getDataRowHelper(formData).save(rows)
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

    def headers = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId).headers
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

    def colIndex = 0
    for (formColumn in formData.formColumns) {
        switch (formColumn.columnType) {
            case ColumnType.AUTO:
                break
            case ColumnType.DATE:
                newRow[formColumn.alias] = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)
                break
            case ColumnType.NUMBER:
                newRow[formColumn.alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)
                break
            case ColumnType.STRING:
                newRow[formColumn.alias] = values[colIndex]
                break
            case ColumnType.REFBOOK:
                switch (colIndex){
                    case 2:
                        newRow.opf = getRecordIdImport(605, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
                        break
                    case 3:
                        newRow.country = getRecordIdImport(10, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
                        break
                    case 5:
                        newRow.creditRating = getRecordIdImport(604, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
                        break
                    case 10:
                        newRow.partRepayment = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
                        break
                    case 12:
                        newRow.currencyCode = getRecordIdImport(15, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
                        break
                }
                break
        }
        colIndex++
    }
    // Заполнение общей информации о заемщике при загрузке из Excel
    fillDebtorInfo(newRow)
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
        // найдем все версии больше даты актуальности
        def versions = provider.getVersions(date + 1, null)
        // найдем минимальную дату среди этих версий
        def minVersion = versions.min()

        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(minVersion, null)
        records520 = provider.getRecordData(uniqueRecordIds)
    }
    return records520
}

void fillDebtorInfo(def newRow) {
    // Найти множество записей справочника «Участники ТЦО», периоды актуальности которых содержат определенную выше дату актуальности
    Map<Long, Map<String, RefBookValue>> records = getRecords520()
    String debtorNumber = newRow.innKio
    if (debtorNumber == null || debtorNumber.isEmpty()) {
        return
    }
    def debtorRecords = records.findAll { def uniqueRecordId, refBookValueMap ->
        debtorNumber.equalsIgnoreCase(refBookValueMap.INN.stringValue) || debtorNumber.equalsIgnoreCase(refBookValueMap.KIO.stringValue)
    }
    if (debtorRecords.size() > 1) {
        logger.warn("Найдено больше одной записи соотвествующей данным ИНН/КИО = " + debtorNumber)
        return
    }
    if (debtorRecords.size() == 0) {
        return
    }
    // else
    String fileDebtorName = newRow.debtorName
    newRow.debtorName = debtorRecords[0].NAME?.stringValue ?: ""
    if (! newRow.debtorName.equalsIgnoreCase(fileDebtorName)) {
        def refBook = refBookFactory.get(520)
        def refBookAttrName = refBook.getAttribute('INN').name + '/' + refBook.getAttribute('KIO').name
        if (fileDebtorName) {
            rowWarning(logger, newRow, "На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "В файле указано другое наименование заемщика - «%s»!",
                    newRow.debtorName, refBookAttrName, newRow.innKio, fileDebtorName)
        } else {
            rowWarning(logger, newRow, "На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "Наименование заемщика в файле не заполнено!",
                    newRow.debtorName, refBookAttrName, newRow.innKio)
        }
    }
}