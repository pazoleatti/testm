package form_template.deal.app_6_25.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 6.25. Расходы по уступке прав требования по кредитным договорам
 * похож на 6.9
 *
 * formTemplateId=836
 *
 * @author Bulat Kinzyabulatov
 */

// rowNumber    - № п/п
// name         - Полное наименование с указанием ОПФ
// iksr         - ИНН/ КИО
// countryCode  - Страна регистрации
// docNumber    - Номер договора
// docDate      - Дата договора
// okeiCode     - Код единицы измерения по ОКЕИ
// count        - Количество
// finResult    - Финансовый результат уступки прав требования, руб.
// price        - Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// cost         - Итого стоимость без учета НДС, акцизов и пошлин, руб.
// dealDoneDate - Дата совершения сделки

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
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
def refBookCache = [:]

@Field
def allColumns = ['rowNumber', 'name', 'iksr', 'countryCode', 'docNumber', 'docDate', 'okeiCode', 'count',
                  'finResult', 'price', 'cost', 'dealDoneDate']

@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'price', 'finResult', 'dealDoneDate']

@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryCode', 'okeiCode', 'count', 'cost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'okeiCode', 'count', 'finResult', 'price', 'cost', 'dealDoneDate']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
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

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 3. Проверка единицы измерения
        def okei
        if (row.okeiCode) {
            okei = getRefBookValue(12, row.okeiCode)?.CODE?.stringValue
            if (okei != '796') {
                def msg = row.getCell('okeiCode').column.name
                logger.error("Строка $rowNum: Графа «$msg» должна быть заполнена значением «796»!")
            }
        }

        // 4. Проверка количества
        if (row.count != null && row.count != 1) {
            def msg = row.getCell('count').column.name
            logger.error("Строка $rowNum: Графа «$msg» должна быть заполнена значением «1»!")
        }

        // 5. Проверка финансового результата
        if (row.finResult != null && row.finResult < 0) {
            def msg = row.getCell('finResult').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // 6. Проверка цены
        if (row.price != null && row.price < 0) {
            def msg = row.getCell('price').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // 7. Проверка стоимости
        if (row.finResult != null && row.cost != row.finResult) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('finResult').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // 8. Корректность даты совершения сделки относительно даты договора
        // TODO (SBRFACCTAX-15094) заменить на checkDatePeriodExt
        checkDatePeriodExtLocal(logger, row, 'dealDoneDate', 'docDate', Date.parse('dd.MM.yyyy', '01.01.' + getReportPeriodEndDate().format('yyyy')), getReportPeriodEndDate(), true)
    }
}

// TODO (SBRFACCTAX-15094) удалить
void checkDatePeriodExtLocal(logger, row, String alias, String startAlias, Date yearStartDate, Date endDate, boolean fatal) {
    // дата проверяемой графы
    Date docDate = row.getCell(alias).getDateValue();
    // дата другой графы
    Date startDate = row.getCell(startAlias).getDateValue();

    if (docDate != null && startDate != null && (docDate.before(yearStartDate) || docDate.after(endDate) || docDate.before(startDate))) {
        logger.error(String.format("Строка %d: Дата по графе «%s» должна принимать значение из диапазона %s - %s и быть больше либо равна дате по графе «%s»!",
                row.getIndex(),
                getColumnName(row, alias),
                formatDate(yearStartDate, "dd.MM.yyyy"),
                formatDate(endDate, "dd.MM.yyyy"),
                getColumnName(row, startAlias)
        ));
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        row.okeiCode = getRecordId(12, 'CODE', '796')
        row.count= 1
        row.cost = row.finResult
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 11
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация о контрагенте - юридическом лице'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
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

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // простая строка
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
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : 'Общая информация о контрагенте - юридическом лице']),
            ([(headerRows[0][4]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'okeiCode')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'finResult')]),
            ([(headerRows[1][9]) : getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'dealDoneDate')])
    ]
    (1..12).each{
        headerMapping.add(([(headerRows[2][it - 1]): 'гр. ' + it]))
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
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[1]

    def int colIndex = 1

    def recordId = getTcoRecordId(nameFromFile, values[2], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
        expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 6
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.okeiCode = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 8
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.finResult = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
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