package form_template.income.rnu_117.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 809 - РНУ 117. Регистр налогового учёта расходов, возникающих в связи с применением в сделках по операциям
 * Привлечения от Взаимозависимых лиц и резидентов оффшорных зон процентных ставок, не соответствующих рыночному уровню
 *
 * formTemplateId=809
 *
 * @author Stanislav Yasinskiy
 */

// rowNumber    (1) - № пп
// fix
// name         (2) - Наименование Взаимозависимого лица/резидента оффшорной зоны
// countryName  (3) - Страна регистрации Взаимозависимого лица/резидента оффшорной зоны
// iksr         (4) - ИНН / код налогоплательщика в стране регистрации (инкорпорации) или его аналог (при наличии)
// code         (5) - Код классификации расхода
// reasonNumber (6) - Номер
// reasonDate   (7) - Дата
// rate         (8) - Процентная ставка, % годовых
// sum1         (9) - Сумма фактически начисленного расхода, руб.
// rate1        (10) - Процентная ставка, признаваемая рыночной для целей налогообложения, % годовых
// sum2         (11) - Сумма расхода, соответствующая рыночному уровню, руб.
// rate2        (12) - Отклонение (превышение) Процентной ставки от рыночного уровня, % годовых
// sum3         (13) - Сумма отклонения (превышения) расхода фактического от соответствующего рыночному уровню, руб.

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
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
def allColumns = ['fix', 'rowNumber', 'name', 'countryName', 'iksr', 'code', 'reasonNumber', 'reasonDate', 'rate',
                  'sum1', 'rate1', 'sum2', 'rate2', 'sum3']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'code', 'reasonNumber', 'reasonDate', 'rate', 'sum1', 'rate1', 'sum2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['countryName', 'iksr', 'rate2', 'sum3']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'code', 'reasonNumber', 'reasonDate', 'rate', 'sum1', 'rate1', 'sum2']

@Field
def sortColumns = ["name", "reasonNumber", "reasonDate"]

@Field
def totalColumns = ['sum3']

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

        // 2. Проверка даты основания совершения операции
        checkDatePeriod(logger, row, 'reasonDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 3. Проверка положительного значения расходов - графы 9, 11
        ['sum1', 'sum2'].each { alias ->
            if(row[alias] != null  && row[alias] < 0){
                rowError(logger, row, "Строка $rowNum: Значение графы «${row.getCell(alias).column.name}» должно быть больше или равно «0»!")
            }
        }

        // 4. Проверка значения граф 9, 11
        if(row.sum1 != null && row.sum2 != null && row.sum1 < row.sum2){
            def col9Name = row.getCell('sum1').column.name
            def col11Name = row.getCell('sum2').column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$col9Name» должно быть не меньше значения графы «$col11Name»!")
        }

        // 5. Проверка расчётных граф
        def needValue = formData.createDataRow()
        needValue.rate2 = calc12(row)
        needValue.sum3 = calc13(row)
        checkCalc(row, ['rate2', 'sum3'], needValue, logger, true)
    }

    // 6. Проверка итоговых значений пофиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    } else {
        logger.error("Отсутствует итоговая строка!")
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
        row.rate2 = calc12(row)
        row.sum3 = calc13(row)
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
    int COLUMN_COUNT = 13
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
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.equalsIgnoreCase("Итого")) {
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
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
        rows.add(totalRow)
    }
    updateIndexes(rows)

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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][6]): 'Основание для совершения операции']),
            ([(headerRows[1][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[1][6]): 'номер']),
            ([(headerRows[1][7]): 'дата']),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'rate')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'sum1')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'rate1')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'sum2')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'rate2')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'sum3')]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..12).each {
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
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[2]

    def int colIndex = 2

    def recordId = getTcoRecordId(nameFromFile, values[4], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4
    if (map != null) {
        def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
        expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.code = values[colIndex]
    colIndex++

    // графа 6
    newRow.reasonNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.reasonDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графы 8-13
    ['rate', 'sum1', 'rate1', 'sum2', 'rate2', 'sum3'].each{
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
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 13
    colIndex = 13
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // прибыль ТЦО
        // "первый квартал", "полугодие", "девять месяцев", "год"
        specialPeriod.name = reportPeriodService.get(formData.reportPeriodId).accName
        // для справочников начало от 01.01.year (для прибыли start_date)
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
        // конец периода
        specialPeriod.endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
}
