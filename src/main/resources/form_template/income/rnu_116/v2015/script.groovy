package form_template.income.rnu_115.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * РНУ 115. Регистр налогового учёта доходов и расходов, возникающих в связи с применением в сделках с драгоценными
 * металлами с Взаимозависимыми лицами и резидентами оффшорных зон курса, не соответствующего рыночному уровню
 *
 * formTemplateId =
 *
 * @author Stanislav Yasinskiy
 */

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
def allColumns = ['fix', 'rowNumber', 'dealNum', 'dealType', 'dealDate', 'dealDoneDate', 'iksr', 'countryName', 'name',
                  'dealFocus', 'reqCurCode', 'reqVolume', 'guarCurCode', 'guarVolume', 'price', 'reqCourse', 'guarCourse',
                  'reqSum', 'guarSum', 'incomeSum', 'outcomeSum', 'marketPrice', 'incomeDelta', 'outcomeDelta']

// Редактируемые атрибуты
@Field
def editableColumns = ['dealNum', 'dealType', 'dealDate', 'dealDoneDate', 'name', 'dealFocus', 'reqCurCode', 'reqVolume',
                       'guarCurCode', 'guarVolume', 'price', 'reqCourse', 'guarCourse', 'reqSum', 'guarSum', 'marketPrice']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['iksr', 'countryName', 'incomeSum', 'outcomeSum', 'incomeDelta', 'outcomeDelta']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['dealNum', 'dealType', 'dealDate', 'dealDoneDate', 'name', 'dealFocus', 'reqCurCode', 'reqVolume',
                       'guarCurCode', 'guarVolume', 'price', 'reqCourse', 'guarCourse', 'reqSum', 'guarSum', 'incomeSum',
                       'outcomeSum', 'marketPrice', 'incomeDelta', 'outcomeDelta']

@Field
def totalColumns = ['incomeDelta', 'outcomeDelta']

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

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    dealType1 = getRecordId(92, 'NAME', 'Кассовая сделка')
    dealType2 = getRecordId(92, 'NAME', 'Срочная сделка')
    dealType3 = getRecordId(92, 'NAME', 'Премия по опциону')
    direction1 = getRecordId(20, 'DIRECTION', 'Покупка')
    direction2 = getRecordId(20, 'DIRECTION', 'Продажа')

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка вида сделки
        if (row.dealType && (row.dealType != dealType1 && row.dealType != dealType2 && row.dealType != dealType3)) {
            def msg = row.getCell('dealType').column.name
            logger.error("Строка $rowNum: Графа «$msg» может содержать только одно из значений: «Кассовая сделка», «Срочная сделка», «Премия по опциону»!")
        }

        // Проверка даты заключения сделки
        checkDatePeriod(logger, row, 'dealDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // Проверка корректности даты заключения сделки
        checkDatePeriod(logger, row, 'dealDoneDate', 'dealDate', getReportPeriodEndDate(), true)

        // Проверка типа сделки
        if (row.dealFocus && (row.dealFocus != direction1 && row.dealFocus != direction2)) {
            def msg = row.getCell('dealFocus').column.name
            logger.error("Строка $rowNum: Графа «$msg» может содержать только одно из значений: «Покупка», Продажа»!")
        }

        // Проверка суммы требований
        if (row.reqSum && row.reqSum < 0) {
            def msg = row.getCell('reqSum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка корректности суммы требований
        if ((row.dealType == dealType1 || row.dealType == dealType2) && row.reqSum != (row.reqVolume * row.reqCourse).abs()) {
            def msg1 = row.getCell('reqSum').column.name
            def msg2 = row.getCell('reqVolume').column.name
            def msg3 = row.getCell('reqCourse').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно равняться модулю произведения «$msg2» и «$msg3»!")
        }

        // Проверка суммы обязательств
        if (row.guarSum && row.guarSum < 0) {
            def msg = row.getCell('guarSum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка корректности суммы обязательств
        if ((row.dealType == dealType1 || row.dealType == dealType2) && row.guarSum != (row.guarVolume * row.guarCourse).abs()) {
            def msg1 = row.getCell('guarSum').column.name
            def msg2 = row.getCell('guarVolume').column.name
            def msg3 = row.getCell('guarCourse').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно равняться модулю произведения «$msg2» и «$msg3»!")
        }

        // TODO
        // Проверка доходов учитываемых в целях налога на прибыль по сделке
        // Проверка расходов учитываемых в целях налога на прибыль по сделке
        // Проверка отклонений по доходам
        // Проверка отклонений по расходам
    }

    // Проверка итоговых значений по фиксированной строке «Итого»
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
        if (row.getAlias() != null) {
            continue
        }
        row.incomeSum = 0
        row.outcomeSum = 0
        def diff = row.reqSum - row.guarSum
        if (diff > 0) {
            row.incomeSum = diff
        } else {
            row.outcomeSum = diff
        }

        // TODO 22, 23
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

def BigDecimal calc12(def row) {
    if (row.rate != null && row.rate1 != null) {
        return row.rate1 - row.rate
    }
    return null
}

def BigDecimal calc13(def row) {
    if (row.sum1 != null && row.sum2 != null) {
        return (row.sum2 - row.sum1).abs()
    }
    return null
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 24
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

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

    // TODO
    def headerMapping = [

    ]
    (1..23).each {
        headerMapping.add([(headerRows[2][it]): it.toString()])
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

    // TODO

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(dataRows.findAll { it.getAlias() == null })
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void sortRows(def dataRows) {
    dataRows.sort { def rowA, def rowB ->
        def aValue = getRefBookValue(520, rowA.name)?.NAME?.value
        def bValue = getRefBookValue(520, rowB.name)?.NAME?.value
        if (aValue != bValue) {
            return aValue <=> bValue
        }
        aValue = rowA.reasonNumber
        bValue = rowB.reasonNumber
        if (aValue != bValue) {
            return aValue <=> bValue
        }
        aValue = rowA.reasonDate
        bValue = rowB.reasonDate
        if (aValue != bValue) {
            return aValue <=> bValue
        }
        return 0
    }
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

    // TODO

    return newRow
}
