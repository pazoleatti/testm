package form_template.income.rnu_111.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 808 - РНУ 111. Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению
 * межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон процентных ставок, не соответствующих рыночному уровню
 *
 * formTemplateId=808
 *
 * @author Stanislav Yasinskiy
 */

// fix
// rowNumber    (1) - № пп
// name         (2) - Наименование Взаимозависимого лица (резидента оффшорной зоны)
// countryName  (3) - Страна местоположения Взаимозависимого лица (резидента оффшорной зоны)
// iksr         (4) - Идентификационный номер
// code         (5) - Код налогового учёта
// reasonNumber (6) - номер
// reasonDate   (7) - дата
// base         (8) - База для расчёта процентного дохода (дней в году)
// sum          (9) - Сумма кредита (ед. валюты)
// currency     (10) - Валюта
// time         (11) - Срок
// rate         (12) - Процентная ставка, (% годовых)
// sum1         (13) - Сумма фактически начисленного дохода (руб.)
// rate1        (14) - Процентная ставка, признаваемая рыночной для целей налогообложения (% годовых)
// sum2         (15) - Сумма дохода, соответствующая рыночному уровню (руб.)
// rate2        (16) - Отклонение процентной ставки от рыночного уровня, (% годовых)
// sum3         (17) - Сумма доначисления дохода до рыночного уровня процентной ставки (руб.)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
def allColumns = ['fix', 'rowNumber', 'name', 'countryName', 'iksr', 'code', 'reasonNumber', 'reasonDate', 'base',
                  'sum', 'currency', 'time', 'rate', 'sum1', 'rate1', 'sum2', 'rate2', 'sum3']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'countryName', 'code', 'reasonNumber', 'reasonDate', 'sum', 'currency', 'time', 'rate',
                       'sum1', 'rate1', 'sum2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['countryName', 'iksr', 'base', 'rate2', 'sum3']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'code', 'reasonNumber', 'reasonDate', 'sum', 'currency', 'time', 'rate', 'sum1', 'rate1', 'sum2']

@Field
def totalColumns = ['sum1', 'sum2', 'sum3']

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

        // 2. Проверка заполнения графы 2 справочным значением по ВЗЛ/РОЗ
        if (row.name) {
            def typeId = getRefBookValue(520, row.name).TYPE?.referenceValue
            if (typeId) {
                def type = getRefBookValue(525, typeId).CODE?.stringValue
                if (!['ВЗЛ', 'РОЗ'].contains(type)) {
                    def msg1 = row.getCell('name').column.name
                    rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть заполнено наименованием Взаимозависимого лица/резидента оффшорной зоны!")
                }
            }
        }

        // 3. Проверка возможности заполнения графы 16
        if (!row.rate && !row.rate1) {
            def msg1 = row.getCell('rate2').column.name
            def msg2 = row.getCell('rate').column.name
            def msg3 = row.getCell('rate1').column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg1»: выполнение расчета невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2», «$msg3»!")
        } else if (!row.rate || !row.rate1) {
            def msg1 = row.getCell('rate2').column.name
            def msg2 = (!row.rate) ? row.getCell('rate').column.name : row.getCell('rate1').column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg1»: выполнение расчета невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2»!")
        }

        // 4. Проверка возможности заполнения графы 17
        if (row.sum1==null && row.sum2==null) {
            def msg1 = row.getCell('sum3').column.name
            def msg2 = row.getCell('sum1').column.name
            def msg3 = row.getCell('sum2').column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg1»: выполнение расчета невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2», «$msg3»!")
        } else if (row.sum1==null || row.sum2==null) {
            def msg1 = row.getCell('sum3').column.name
            def msg2 = (row.sum1==null) ? row.getCell('sum1').column.name : row.getCell('sum2').column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg1»: выполнение расчета невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2»!")
        }

        // 5. Проверка значения графы 16
        if (row.rate1 && row.rate && row.rate2 != calc16(row)) {
            def msg1 = row.getCell('rate2').column.name
            def msg2 = row.getCell('rate1').column.name
            def msg3 = row.getCell('rate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно разности значений графы «$msg2» и «$msg3»!")
        }

        // 6. Проверка значения графы 17
        if (row.sum2 && row.sum1 && row.sum3 != calc17(row)) {
            def msg1 = row.getCell('sum3').column.name
            def msg2 = row.getCell('sum2').column.name
            def msg3 = row.getCell('sum1').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно разности по модулю значений графы «$msg2» и «$msg3»!")
        }

        // 7. Проверка возможности заполнения графы 8
        if(!row.reasonDate){
            def msg1 = row.getCell('base').column.name
            def msg2 = row.getCell('reasonDate').column.name
            rowError(logger, row, "Строка $rowNum: Графа «$msg1»: выполнение расчета невозможно, так как не заполнена используемая в расчете графа «$msg2»!")

        }

        // 8. Проверка значения графы 8
        if (row.reasonDate  && row.base != calc8(row)) {
            def msg1 = row.getCell('rate2').column.name
            def msg2 = row.getCell('reasonDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть равно количеству дней для даты, указанной по графе «$msg2»!")
        }

        // 9. Проверка значения графы 13, 15
        if(row.sum1 != null && row.sum2 != null && row.sum1 < row.sum2){
            def msg1 = row.getCell('sum1').column.name
            def msg2 = row.getCell('sum2').column.name
            rowWarning(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // 10. Проверка положительного значения графы 13, 15
        if(row.sum1 != null  && row.sum1 < 0){
            msg = row.getCell('sum1').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }
        if(row.sum2 != null  && row.sum2 < 0){
            msg = row.getCell('sum2').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // 11. Проверка значения графы 11
        if(row.reasonDate && row.time && (row.time > getDays(row.reasonDate))){
            def msg1 = row.getCell('time').column.name
            def msg2 = row.getCell('reasonDate').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» должно быть не больше количества дней равному 15 лет " +
                    "начиная с дня даты, указанной по графе «$msg2»!")
        }
    }

    // 12. Проверка итоговых значений пофиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}


def getDays(def date) {
    def year = Integer.valueOf(date.format('yyyy'))
    def count = 0
    for(int i=0; i < 15; i++){
        def end = Date.parse('dd.MM.yyyy', "31.12.$year")
        def begin = Date.parse('dd.MM.yyyy', "01.01.$year")
        count += end - begin + 1
        year++
    }
    return count
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление итогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        if(row.getAlias() != null){
            continue
        }
        row.base = calc8(row)
        row.rate2 = calc16(row)
        row.sum3 = calc17(row)
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)
}

def BigDecimal calc8(def row) {
    if (row.reasonDate != null) {
        def year = row.reasonDate.format('yyyy')
        def end = Date.parse('dd.MM.yyyy', "31.12.$year")
        def begin = Date.parse('dd.MM.yyyy', "01.01.$year")
        return end - begin + 1
    }
    return null
}

def BigDecimal calc16(def row) {
    if (row.rate != null && row.rate1 != null) {
        return row.rate1 - row.rate
    }
    return null
}

def BigDecimal calc17(def row) {
    if (row.sum1 != null && row.sum2 != null) {
        return (row.sum2 - row.sum1).abs()
    }
    return null
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
    int COLUMN_COUNT = 17
    int HEADER_ROW_COUNT = 3
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
            ([(headerRows[0][6]): 'Основание для совершения операции']),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[1][6]): 'номер']),
            ([(headerRows[1][7]): 'дата']),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'base')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'currency')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'time')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'rate')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'sum1')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'rate1')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'sum2')]),
            ([(headerRows[1][16]): getColumnName(tmpRow, 'rate2')]),
            ([(headerRows[1][17]): getColumnName(tmpRow, 'sum3')])
    ]
    (1..16).each {
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
        map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4
    colIndex++

    // графа 5
    newRow.code = getRecordIdImport(28, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 6
    newRow.reasonNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.reasonDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.base = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.currency = getRecordIdImport(15, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графы 11-17
    ['time', 'rate', 'sum1', 'rate1', 'sum2', 'rate2', 'sum3'].each{
        newRow[it]= parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

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

    // графа 13
    def colIndex = 13
    newRow.sum1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 15
    colIndex = 15
    newRow.sum2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 17
    colIndex = 17
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, dataRows.find { it.getAlias() == 'total' }, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}