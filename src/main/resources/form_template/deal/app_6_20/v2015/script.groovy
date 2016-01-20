package form_template.deal.app_6_20.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 831 - 6.20. Привлечение средств на межбанковском рынке
 *
 * formTemplateId=831
 *
 * @author - Bulat.Kinzyabulatov
 */
// графа    - fix
// графа 1  - rowNumber         - № п/п
// графа 2  - name              - Полное наименование юридического лица с указанием ОПФ
// графа 3  - iksr              - ИНН/ КИО
// графа 4  - countryName       - Наименование страны регистрации
// графа 5  - regCountryCode    - Код страны регистрации по классификатору ОКСМ
// графа 6  - docNumber         - Номер договора
// графа 7  - docDate           - Дата договора
// графа 8  - dealNumber        - Номер сделки
// графа 9  - dealDate          - Дата заключения сделки
// графа 10 - count             - Количество
// графа 11 - outcome           - Сумма расходов Банка по данным бухгалтерского учета, руб.
// графа 12 - price             - Цена(тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// графа 13 - cost              - Итого стоимость без учета НДС, акцизов и пошлины, руб.
// графа 14 - dealDoneDate      - Дата совершения сделки

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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'regCountryCode', 'docNumber', 'docDate', 'dealNumber',
                  'dealDate', 'count', 'outcome', 'price', 'cost', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'outcome', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'regCountryCode', 'count', 'price', 'cost']

//Непустые атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealDate', 'count', 'outcome', 'price', 'cost', 'dealDoneDate']

@Field
def totalColumns = ['count', 'outcome', 'cost']

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

        // 1. Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 3. Проверка корректности даты заключения сделки
        if (row.docDate && row.dealDate && (row.dealDate < row.docDate || row.dealDate > getReportPeriodEndDate())) {
            def msg1 = getColumnName(row, 'dealDate')
            def msg2 = getColumnName(row, 'docDate')
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }

        // 4. Проверка количества
        if (row.count && row.count != 1) {
            def msg1 = getColumnName(row, 'count')
            logger.error("Строка $rowNum: Графа «$msg1» должна быть заполнена значением «1»!")
        }

        // 5. Проверка суммы расходов
        if (row.outcome != null && row.outcome < 0) {
            def msg = getColumnName(row, 'outcome')
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // 6, 7. Проверка цены и стоимости
        ['price', 'cost'].each { alias ->
            if (row.outcome != null && row[alias] != row.outcome) {
                def msg = getColumnName(row, 'outcome')
                def msg1 = getColumnName(row, alias)
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg»!")
            }
        }

        // 8. Проверка корректности даты совершения сделки
        if (row.dealDate && row.dealDoneDate && (row.dealDoneDate < row.dealDate || row.dealDoneDate > getReportPeriodEndDate())) {
            def msg1 = getColumnName(row, 'dealDoneDate')
            def msg2 = getColumnName(row, 'dealDate')
            def msg3 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2» и не больше $msg3!")
        }
    }

    // 9. Проверка итоговых значений пофиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }

}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление подитогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        row.count = 1
        // Расчет поля "Цена", "Стоимость"
        row.price = row.outcome
        row.cost = row.outcome
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    sortFormDataRows(false)
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 6
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 14
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общие сведения о контрагенте - юридическом лице'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

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
    def totalRowFromFile = null
    def totalRowFromFileMap = [:] // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

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
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // сравнение итогов
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
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
            ([(headerRows[0][0]) : 'Общие сведения о контрагенте - юридическом лице']),
            ([(headerRows[0][6]) : 'Сведения о сделке']),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'regCountryCode')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][9]) : getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'outcome')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][1]) : 'гр. 1']),
            ([(headerRows[2][2]) : 'гр. 2']),
            ([(headerRows[2][3]) : 'гр. 3']),
            ([(headerRows[2][4]) : 'гр. 4.1']),
            ([(headerRows[2][5]) : 'гр. 4.2'])
    ]
    (6..14).each {
        headerMapping.add(([(headerRows[2][it]): 'гр. '+(it - 1).toString()]))
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
    def nameFromFile = values[2]

    def int colIndex = 2

    def recordId = getTcoRecordId(nameFromFile, values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    def countryMap
    // графа 4
    if (map != null) {
        countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5                               `
    if (countryMap != null) {
        formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 6
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.dealNumber = values[colIndex]
    colIndex++

    // графа 9
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

/**
 * Получить итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 10,11,13
    def colIndex = 10
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    colIndex = 11
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    colIndex = 13
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = dataRows.find { it.getAlias() == 'total'}
    sortRows(refBookService, logger, dataRows, null, totalRow, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}