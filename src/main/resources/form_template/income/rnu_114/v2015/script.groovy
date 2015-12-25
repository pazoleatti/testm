package form_template.income.rnu_114.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * РНУ 114. Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению Кредитных
 * продуктов Взаимозависимым лицам и резидентам оффшорных зон процентных ставок, не соответствующих рыночному уровню
 *
 * formTemplateId=829
 *
 * @author Emamedova
 */

// графа    - fix
// графа 1  - rowNumber    -  № п/п
// графа 2  - name         -  Наименование Взаимозависимого лица/резидента оффшорной зоны
// графа 3  - iksr         -  ИНН/ КИО
// графа 4  - countryName  -  Страна регистрации
// графа 5  - code		   -  Код классификации дохода
// графа 6  - docNumber    -  Номер кредитного договора
// графа 7  - docDate      -  Дата кредитного договора
// графа 8  - residual     -  Остаток задолженности
// графа 9  - currencyCode -  Валюта
// графа 10 - courseCB     -  Курс валюты Банка России
// графа 11 - period       -  Количество календарных дней в периоде
// графа 12 - base         -  База года (360/365/366)
// графа 13 - rate1 	   -  Совокупная процентная ставка
// графа 14 - rate2 	   -  Рыночная ставка
// графа 15 - deviation    -  Отклонение Совокупной процентной ставки от рыночного уровня
// графа 16 - sum1         -  Сумма доначисления дохода до рыночного уровня
// графа 17 - sum2  	   -  Сумма фактического процентного дохода

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'code', 'docNumber', 'docDate', 'residual',
                  'currencyCode', 'courseCB', 'period', 'base', 'rate1', 'rate2', 'deviation', 'sum1', 'sum2']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'code', 'code', 'docNumber', 'docDate', 'residual',  'currencyCode', 'courseCB',
                       'period', 'base', 'rate1', 'rate2', 'sum2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['iksr', 'countryName', 'deviation', 'sum1']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'code', 'docNumber', 'docDate', 'residual', 'currencyCode', 'courseCB', 'period', 'base',
                       'rate1', 'rate2', 'sum2']

@Field
def totalColumns = ['sum1']

// Дата окончания отчетного периода
@Field
def endDate = null

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

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка на заполнение граф
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка даты кредитного договора
        if (row.docDate && (row.docDate < Date.parse('dd.MM.yyyy', '01.01.1991') || row.docDate > getReportPeriodEndDate())) {
            def msg1 = row.getCell('docDate').column.name
            def msg2 = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Дата, указанная в графе «$msg1» должна принимать значение из следующего диапазона: 01.01.1991 - $msg2!")
        }

        // Проверка количества календарных дней
        if (row.period != null && row.period <= 0) {
            def msg1 = row.getCell('period').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть больше нуля!")
        }

        // Проверка базы года
        if (row.base != null && !(row.base.intValue() in [360, 365, 366])) {
            def msg1 = row.getCell('base').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно «360» или «365» или «366»!")
        }

        //Проверка соотношения процентных ставок
        if (row.period != null && row.rate1 > row.rate2) {
            def msg1 = row.getCell('rate1').column.name
            def msg2 = row.getCell('rate2').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть меньше или равно значению графы «$msg2»!")
        }

    }

    // Проверка итоговых значений пофиксированной строке «Итого»
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

    // Сортировка
    sortRows(dataRows)

    updateIndexes(dataRows)
    for (row in dataRows) {
        row.deviation = calc15(row)
        row.sum1 = calc16(row)
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

void sortRows(def dataRows) {
    dataRows.sort{ def rowA, def rowB ->
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

def BigDecimal calc15(def row) {
    if (row.rate1 != null && row.rate2 != null) {
        return row.rate2 - row.rate1
    }
    return null
}

def BigDecimal calc16(def row) {
    if (row.residual != null && row.courseCB != null && row.period != null && row.deviation != null && row.base != null) {
        return round((BigDecimal) ((row.residual * row.courseCB *  row.period * row.deviation) / row.base), 2)
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
    int COLUMN_COUNT = 17
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = '№ пп'
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

    def headerMapping = [
            ([(headerRows[0][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'residual')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'currencyCode')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'courseCB')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'period')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'base')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'rate1')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'rate2')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'deviation')]),
            ([(headerRows[0][16]): getColumnName(tmpRow, 'sum1')]),
            ([(headerRows[0][17]): getColumnName(tmpRow, 'sum2')])]

    (1..17).each {
        headerMapping.add([(headerRows[1][it]): it.toString()])
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

    def recordId = getTcoRecordId(nameFromFile, values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (map != null) {
            def expectedValues = [map.NAME?.stringValue, map.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(10, values[colIndex], expectedValues, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5
    newRow.code = values[colIndex]
    colIndex++

    // графа 6
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.residual = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    colIndex++

    // графы 10-17
    ['courseCB', 'period', 'base', 'rate1', 'rate2', 'deviation', 'sum1', 'sum2'].each{
        newRow[it]= parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(dataRows.findAll{ it.getAlias() == null})
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
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    colIndex = 16
    newRow.sum1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}