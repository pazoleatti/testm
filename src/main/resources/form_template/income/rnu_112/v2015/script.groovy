package form_template.income.rnu_112.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 824 - РНУ-112. Регистр налогового учёта доходов по сделкам РЕПО, возникающих в связи с применением в
 * сделках c Взаимозависимыми лицами и резидентами оффшорных зон ставок, не соответствующих рыночному уровню
 *
 * formTemplateId=824
 *
 * @author Bulat Kinzyabulatov
 */

// rowNumber        (1)  - № пп
// fix
// dealNum          (2)  - Номер сделки
// name             (3)  - Наименование Взаимозависимого лица/резидента оффшорной зоны
// iksr             (4)  - Идентификационный номер
// countryName      (5)  - Страна местонахождения контрагента
// code             (6)  - Код налогового учёта
// currency         (7)  - Валюта расчетов
// date1Part        (8)  - Дата первой части сделки
// date2Part        (9)  - Дата второй части сделки
// dealRate         (10) - Ставка сделки, % годовых
// dealLeftSum      (11) - Сумма остаточных обязательств (требований) контрагента по сделке
// bondSum          (12) - Сумма выплаты по ценным бумагам
// payDate          (13) - Дата выплаты (гр. 12)
// accrStartDate    (14) - Период начисления доходов на сумму остаточных обязательств контрагента (гр. 9). Дата начала начисления
// accrEndDate      (15) - Период начисления доходов на сумму остаточных обязательств контрагента (гр. 9). Дата окончания начисления
// yearBase         (16) - База (360/365/366)
// dealIncome       (17) - Доходы по сделке
// rateDiff         (18) - Отклонение от рыночной процентной ставки для целей налогообложения
// incomeCorrection (19) - Сумма корректировки доходов

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
def allColumns = ['rowNumber', 'fix', 'dealNum', 'name', 'iksr', 'countryName', 'code', 'currency', 'date1Part', 'date2Part', 'dealRate', 'dealLeftSum',
                  'bondSum', 'payDate', 'accrStartDate', 'accrEndDate', 'yearBase', 'dealIncome', 'rateDiff', 'incomeCorrection']

// Редактируемые атрибуты
@Field
def editableColumns = ['dealNum', 'name', 'code', 'currency', 'date1Part', 'date2Part', 'dealRate', 'dealLeftSum', 'bondSum', 'payDate',
                       'accrStartDate', 'accrEndDate', 'yearBase', 'dealIncome', 'rateDiff']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'incomeCorrection']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['dealNum', 'name', 'code', 'currency', 'date1Part', 'date2Part', 'dealRate', 'dealLeftSum',
                       'accrStartDate', 'accrEndDate', 'yearBase', 'dealIncome', 'rateDiff', 'incomeCorrection']

@Field
def sortColumns = ["dealNum", "accrStartDate"]

@Field
def totalColumns = ['incomeCorrection']

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

        // 1. Проверка на заполнение граф
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка даты первой части сделки
        checkDatePeriod(logger, row, 'date1Part', Date.parse("dd.MM.yyyy", "01.01.1991"), getReportPeriodEndDate(), true)

        // 3. Проверка даты второй части сделки
        if (row.date1Part && row.date2Part && row.date2Part < row.date1Part) {
            logger.error("Строка $rowNum: Дата по графе «${getColumnName(row,'date2Part')}» должна быть не меньше даты по графе «${getColumnName(row,'date1Part')}»!")
        }

        // 4. Проверка положительности сумм и отклонения
        ['dealLeftSum', 'bondSum', 'dealIncome', 'rateDiff', 'incomeCorrection'].each { alias ->
            if (row[alias] != null && row[alias] < 0) {
                logger.error("Строка $rowNum: Значение графы «${getColumnName(row, alias)}» должно быть больше или равно «0»!")
            }
        }

        // 5. Проверка периодов начисления
        if (row.accrStartDate && row.accrEndDate && row.accrStartDate >= row.accrEndDate) {
            logger.error("Строка $rowNum: Значение графы «${getColumnName(row, 'accrEndDate')}» должно быть больше значения графы «${getColumnName(row, 'accrStartDate')}»!")
        }

        // 6. Проверка базы
        if (row.yearBase != null && ![360, 365, 366].contains(row.yearBase?.intValue())) {
            rowError(logger, row, "Строка $rowNum: Графа «${getColumnName(row, 'yearBase')}» должна принимать значение из следующего списка: «360», «365», «366»!")
        }

        // 7. Проверка расчётных граф (арифметические проверки)
        def needValue = formData.createDataRow()
        needValue.incomeCorrection = calc19(row)
        checkCalc(row, ['incomeCorrection'], needValue, logger, true)
    }

    // 8. Проверка итоговых значений пофиксированной строке «Итого»
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
        if(row.getAlias() != null){
            continue
        }
        row.incomeCorrection = calc19 (row)
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

def calc19 (def row) {
    if (row.dealLeftSum != null && row.rateDiff != null && row.accrEndDate != null && row.accrStartDate != null && row.yearBase != null) {
        return ((BigDecimal) ((row.dealLeftSum * row.rateDiff) * (row.accrEndDate - row.accrStartDate + 1))).divide(100 * row.yearBase,
                row.getCell('incomeCorrection').getColumn().precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
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
    int COLUMN_COUNT = 19
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'dealNum')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'currency')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'date1Part')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'date2Part')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'dealRate')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'dealLeftSum')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'bondSum')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'payDate')]),
            ([(headerRows[0][14]): 'Период начисления доходов на сумму остаточных обязательств контрагента (гр. 11)']),
            ([(headerRows[1][14]): 'Дата начала начисления']),
            ([(headerRows[1][15]): 'Дата окончания начисления']),
            ([(headerRows[0][16]): getColumnName(tmpRow, 'yearBase')]),
            ([(headerRows[0][17]): getColumnName(tmpRow, 'dealIncome')]),
            ([(headerRows[0][18]): getColumnName(tmpRow, 'rateDiff')]),
            ([(headerRows[0][19]): getColumnName(tmpRow, 'incomeCorrection')]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..19).each {
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
    def int colIndex = 2

    // графа 2
    newRow.dealNum = values[colIndex]
    colIndex++

    def recordId = getTcoRecordId(values[3], values[4], getColumnName(newRow, 'iksr'), fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 3
    newRow.name = recordId
    colIndex++

    // графа 4
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 6
    newRow.code = values[colIndex]
    colIndex++

    // графа 7
    newRow.currency = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 8
    newRow.date1Part = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.date2Part = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графы 10-11
    ['dealRate', 'dealLeftSum', 'bondSum'].each{
        newRow[it]= parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    // графы 13-15
    ['payDate', 'accrStartDate', 'accrEndDate'].each{
        newRow[it]= parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    // графы 16-19
    ['yearBase', 'dealIncome', 'rateDiff', 'incomeCorrection'].each{
        newRow[it]= parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = sortColumns + (allColumns - sortColumns)
    // Сортировка (без подитогов)
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { columns.contains(it.getAlias())})
    sortRows(dataRows, columns)

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
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
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) { // TODO
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 19
    colIndex = 19
    newRow.incomeCorrection = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}