package form_template.market.market_2_6.v2016

import com.aplana.sbrf.taxaccounting.model.ColumnType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.RefBookColumn
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 2.6 (Ежемесячный) Отчет о состоянии кредитного портфеля
 * formTemplateId = 900
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNum             - № п/п
 * графа 2  - codeBank           - Код банка
 * графа 3  - nameBank           - Наименование банка
 * графа 4  - depNumber          - Номер отделения, выдавшего кредит / кредитующее подразделение ЦА, к компетенции которого относится договор
 * графа 5  - okved              - Код отрасли по ОКВЭД
 * графа 6  - opf                - Организационно-правовая форма
 * графа 7  - debtorName         - Наименование заемщика
 * графа 8  - inn                - ИНН заемщика
 * графа 9  - sign               - Признак СМП
 * графа 10 - direction          - Направление бизнес плана, к которому относится кредит
 * графа 11 - law                - Номер Регламента, в рамках которого предоставлен кредит
 * графа 12 - creditType         - Тип кредита
 * графа 13 - docNum             - № кредитного договора
 * графа 14 - docDate            - Дата кредитного договора
 * графа 15 - creditDate         - Дата выдачи кредита
 * графа 16 - closeDate          - Дата погашения с учетом последней пролонгации
 * графа 17 - extendNum          - Количество пролонгаций
 * графа 18 - creditMode         - Режим кредитования
 * графа 19 - currencySum        - Валюта суммы кредита (лимита кредитной линии)
 * графа 20 - sumDoc             - Сумма кредита (по договору), лимит кредитной линии, тыс. ед. валюты
 * графа 21 - sumGiven           - Сумма выданного кредита, тыс. ед. валюты
 * графа 22 - rate               - Действующая процентная ставка
 * графа 23 - payFrequency       - Периодичность уплаты процентов
 * графа 24 - currencyCredit     - Валюта выдачи кредита
 * графа 25 - debtSum            - Остаток задолженности на отчетную дату (тыс. руб.). Ссудная задолженность, всего
 * графа 26 - inTimeDebtSum      - Остаток задолженности на отчетную дату (тыс. руб.). В т.ч. срочная
 * графа 27 - overdueDebtSum     - Остаток задолженности на отчетную дату (тыс. руб.). В т.ч. просроченная
 * графа 28 - percentDebtSum     - Остаток задолженности на отчетную дату (тыс. руб.). Задолженность по просроченным %
 * графа 29 - deptDate           - Дата вынесения на просрочку основного долга
 * графа 30 - percentDate        - Дата вынесения на просрочку процентов
 * графа 31 - percentPeriod      - Срок нахождения на счетах просроченных требований и/или процентов, дней
 * графа 32 - provision          - Обеспечение
 * графа 33 - provisionComment   - Примечание к обеспечению
 * графа 34 - loanSign           - Признак реструктурированной ссуды
 * графа 35 - loanQuality        - Категория качества ссуды
 * графа 36 - finPosition        - Финансовое положение
 * графа 37 - debtService        - Обслуживание долга
 * графа 38 - creditRisk         - Категория кредитного риска / класс кредитоспособности
 * графа 39 - portfolio          - Портфель однородных требований
 * графа 40 - reservePercent     - Величина отчислений в резерв, %
 * графа 41 - reserveSum         - Сформированный резерв в тыс. руб.
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
def allColumns = ['rowNum', 'codeBank', 'nameBank', 'depNumber', 'okved', 'opf', 'debtorName', 'inn', 'sign', 'direction',
                  'law', 'creditType', 'docNum', 'docDate', 'creditDate', 'closeDate', 'extendNum', 'creditMode',
                  'currencySum', 'sumDoc', 'sumGiven', 'rate', 'payFrequency', 'currencyCredit', 'debtSum', 'inTimeDebtSum',
                  'overdueDebtSum', 'percentDebtSum', 'deptDate', 'percentDate', 'percentPeriod', 'provision', 'provisionComment',
                  'loanSign', 'loanQuality', 'finPosition', 'debtService', 'creditRisk', 'portfolio', 'reservePercent', 'reserveSum']

// Редактируемые атрибуты
@Field
def editableColumns = ['rowNum', 'codeBank', 'nameBank', 'depNumber', 'okved', 'opf', 'debtorName', 'inn', 'sign', 'direction',
                       'law', 'creditType', 'docNum', 'docDate', 'creditDate', 'closeDate', 'extendNum', 'creditMode',
                       'currencySum', 'sumDoc', 'sumGiven', 'rate', 'payFrequency', 'currencyCredit', 'debtSum', 'inTimeDebtSum',
                       'overdueDebtSum', 'percentDebtSum', 'deptDate', 'percentDate', 'percentPeriod', 'provision', 'provisionComment',
                       'loanSign', 'loanQuality', 'finPosition', 'debtService', 'creditRisk', 'portfolio', 'reservePercent', 'reserveSum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = []

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'codeBank', 'nameBank', 'depNumber', 'okved', 'opf', 'debtorName', 'inn', 'sign', 'direction',
                       'law', 'creditType', 'docNum', 'docDate', 'closeDate', 'extendNum', 'creditMode',
                       'currencySum', 'sumDoc', 'sumGiven', 'rate', 'payFrequency', 'currencyCredit', 'debtSum', 'inTimeDebtSum',
                       'overdueDebtSum', 'percentDebtSum', 'provision', 'loanSign', 'loanQuality', 'finPosition', 'debtService', 'creditRisk', 'reservePercent', 'reserveSum']

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
def nonNegativeColumns = ['extendNum', 'sumDoc', 'sumGiven', 'rate', 'debtSum', 'inTimeDebtSum',
                  'overdueDebtSum', 'percentDebtSum', 'percentPeriod', 'reservePercent', 'reserveSum']

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // групируем по графам 8, 13, 14
    def rowsMap = [:]
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        // 2. 17, 20, 21, 22,  25, 26, 27, 28, 31, 40, 41 неотрицательность графы
        nonNegativeColumns.each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно 0!", row.getIndex(), getColumnName(row, alias))
            }
        }
        // 3. Положительность графы
        if (row.payFrequency != null && row.payFrequency <= 0) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше 0!", row.getIndex(), getColumnName(row, 'payFrequency'))
        }
        // 4. Проверка даты выдачи кредита
        if (row.docDate != null && row.creditDate != null && (row.creditDate < row.docDate)) {
            logger.warn("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                row.getIndex(), getColumnName(row, 'creditDate'), getColumnName(row, 'docDate'))
        }
        // 5. Проверка даты погашения кредита
        if (row.docDate != null && row.closeDate != null && (row.closeDate < row.docDate)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                    row.getIndex(), getColumnName(row, 'closeDate'), getColumnName(row, 'docDate'))
        }
        // 6. Проверка даты погашения кредита 2
        if (row.creditDate != null && row.closeDate != null && (row.closeDate < row.creditDate)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                    row.getIndex(), getColumnName(row, 'closeDate'), getColumnName(row, 'creditDate'))
        }
        // 7. Проверка валюты
        if (row.currencySum != null && ("RUB".equals(getRefBookValue(15, row.currencySum).CODE_2.value))) {
            logger.error("Строка %s: Для российского рубля должно быть проставлено буквенное значение RUR!", row.getIndex())
        }
        // 8. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
        // группируем
        def key = getKey(row)
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        }
        rowsMap[key].add(row)
    }
    // 8. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
    rowsMap.each { def key, rows ->
        def size = rows.size()
        // пропускаем первую строку
        for (int i = 1; i < size; i++) {
            def row = rows[i]
            logger.error("Строка %s: На форме уже существует строка со значениями граф «%s» = «%s», «%s» = «%s», «%s» = «%s»!",
                row.getIndex(), getColumnName(row, 'inn'), row.inn, getColumnName(row, 'docNum'), row.docNum, getColumnName(row, 'docDate'), row.docDate?.format('dd.MM.yyyy') ?: '')
        }
    }
}

String getKey(def row) {
    return (row.inn?.trim() + "#" + row.docNum?.trim() + "#" + row.docDate?.format('dd.MM.yyyy')).toLowerCase()
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

def getRecords(def inn) {
    def filter = 'LOWER(INN) = LOWER(\'' + inn + '\') OR LOWER(KIO) = LOWER(\'' + inn + '\')'
    def provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    if (recordCache[filter] == null) {
        recordCache.put(filter, provider.getRecords(getReportPeriodEndDate(), null, filter, null))
    }
    return recordCache[filter]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        calc7(row)
    }
}

@Field
def exclusiveInns = ['9999999999', '9999999998']

void calc7(def row) {
    //Если значение графы 8 не равно значениям “9999999999”, “9999999998”, тогда:
    //1.	Найти в справочнике «Участники ТЦО» запись, для которой выполнено одно из условий:
    //        -	Значение поля «ИНН (заполняется для резидентов, некредитных организаций)» = значение графы 8;
    //-	Значение поля «КИО (заполняется для нерезидентов)» = значение графы 8.
    //2.	Если запись найдена, тогда:
    //Графа 7 = значение поля «Полное наименование юридического лица с указанием ОПФ».
    //3.	Если запись не найдена, тогда графа 7 не рассчитывается (если до выполнения расчета в графе 7 было указано значение, то это значение должно сохраниться)
    if (!exclusiveInns.contains(row.inn)) {
        def records = getRecords(row.inn?.trim()?.toLowerCase())
        if (records != null && records.size() == 1) {
            row.debtorName = records.get(0).NAME.value
        }
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 41
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

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping =[[:]]
    def index = 0
    allColumns.each { alias ->
        if (!['inTimeDebtSum', 'overdueDebtSum', 'percentDebtSum'].contains(alias)) {
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
                def refBookAttribute = ((RefBookColumn) formColumn).refBookAttribute
                def refBookId = refBookFactory.getByAttribute(refBookAttribute.id).id
                def refBookAttrAlias = refBookAttribute.alias
                def value = values[colIndex]
                if (RefBookAttributeType.NUMBER.equals(refBookAttribute.attributeType)) {
                    value = new BigDecimal(value).setScale(refBookAttribute.precision, RoundingMode.HALF_UP).toString()
                }
                def recordId = getRecordIdImport(refBookId, refBookAttrAlias, value, fileRowIndex, colIndex + colOffset, false)
                newRow[formColumn.alias] = recordId
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
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        records520 = provider.getRecordData(uniqueRecordIds)
    }
    return records520
}

void fillDebtorInfo(def newRow) {
    // Найти множество записей справочника «Участники ТЦО», периоды актуальности которых содержат определенную выше дату актуальности
    Map<Long, Map<String, RefBookValue>> records = getRecords520()
    String debtorNumber = newRow.inn
    if (debtorNumber == null || debtorNumber.isEmpty() || exclusiveInns.contains(debtorNumber)) {
        return
    }
    def debtorRecords = records.values().findAll { def refBookValueMap ->
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
            rowWarning(logger, newRow, String.format("На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "В файле указано другое наименование заемщика - «%s»!",
                    newRow.debtorName, refBookAttrName, newRow.inn, fileDebtorName))
        } else {
            rowWarning(logger, newRow, String.format("На форме графы с общей информацией о заемщике заполнены данными записи справочника «Участники ТЦО», " +
                    "в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», атрибут «%s» = «%s». " +
                    "Наименование заемщика в файле не заполнено!",
                    newRow.debtorName, refBookAttrName, newRow.inn))
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
