package form_template.income.rnu_101.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * РНУ 101. Регистр налогового учёта сумм доначисления доходов по прочим операциям при применении в сделках с
 * Взаимозависимыми лицами и резидентами оффшорных зон цен, не соответствующих рыночному уровню
 *
 * formTemplateId=818
 *
 * @author Emamedova
 */

// fix
// rowNumber    		(1) -  № пп
// name         		(2) -  Наименование Взаимозависимого лица (резидента оффшорной зоны)
// iksr					(3) -  Идентификационный номер
// countryName  		(4) -  Страна регистрации
// transDoneDate        (5) -  Дата совершения операции
// course 				(6) -  Курс валюты Банка России
// incomeCode 			(7) -  Код классификации дохода
// reasonNumber 		(8) -  номер
// reasonDate   		(9) -  дата
// count 				(10) - Количество услуг/работ (ед./шт.)
// dealPrice			(11) - Цена за оказанные услуги согласно условиям договора
// taxPrice				(12) - Цена, признаваемая рыночной для целей налогообложения
// sum1					(13) - Сумма фактически начисленного дохода (руб.)
// incomeRate			(14) - Коэффициент корректировки доходов
// sum2					(15) - Сумма дохода, соответствующая рыночному уровню (руб.)
// sum3					(16) - Сумма доначисления дохода до рыночного уровня процентной ставки (руб.)

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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'transDoneDate', 'course', 'incomeCode',
                  'reasonNumber', 'reasonDate', 'count', 'dealPrice', 'taxPrice', 'sum1', 'incomeRate', 'sum2', 'sum3']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'transDoneDate', 'course', 'incomeCode', 'reasonNumber', 'reasonDate', 'count', 'dealPrice',
                       'taxPrice', 'sum1', 'incomeRate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'sum2', 'sum3']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'transDoneDate', 'course', 'incomeCode', 'reasonNumber', 'reasonDate', 'count',
                       'dealPrice', 'sum1', 'sum2', 'sum3']

@Field
def totalColumns = ['sum3']

// Группируемые атрибуты
@Field
def groupColumns = ['incomeCode']

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

        // Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка даты совершения операции
        if (row.transDoneDate && row.reasonDate && row.reasonDate > row.transDoneDate) {
            def msg1 = row.getCell('transDoneDate').column.name
            def msg2 = row.getCell('reasonDate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // Проверка даты совершения операции
        checkDatePeriod(logger, row, 'transDoneDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // Проверка курса валюты
        if (row.course != null && row.course < 0) {
            def msg = row.getCell('course').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка даты основания совершения операции
        checkDatePeriod(logger, row, 'reasonDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // Проверка количества
        if (row.count != null && row.count < 1) {
            def msg = row.getCell('count').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «1»!")
        }

        // Проверка положительной  цены
        if (row.dealPrice != null && row.dealPrice < 0) {
            def msg = row.getCell('dealPrice').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка положительной  суммы доходов
        if (row.sum1 != null && row.sum1 < 0) {
            def msg = row.getCell('sum1').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка положительной цены для целей налогообложения
        if (row.taxPrice != null && row.taxPrice < 0) {
            def msg = row.getCell('taxPrice').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        boolean noOne = (row.taxPrice == null && row.incomeRate == null)
        boolean both = (row.taxPrice != null && row.incomeRate != null)
        String taxPrice = getColumnName(row, 'taxPrice')
        String incomeRate = getColumnName(row, 'incomeRate')
        // Проверка наличия заполнения цены и коэффициента
        if (noOne) {
            logger.error("Строка $rowNum: Должно быть не заполнено или только значение графы «$taxPrice», или только значение графы «$incomeRate»!")
        }
        // Проверка отсутствия заполнения цены и коэффициента
        if (both) {
            logger.error("Строка $rowNum: Должно быть заполнено или только значение графы «$taxPrice», или только значение графы «$incomeRate»!")
        }

        // Проверка наличия заполнения суммы
        if (row.incomeRate != null && row.sum1 != null && row.sum1 <= 0) {
            def msg = row.getCell('sum1').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
        }

        // Проверка положительного коэффициента
        if (row.incomeRate != null && row.incomeRate < 0) {
            def msg = row.getCell('incomeRate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка наличия коэффициента
        if (row.sum1 != null && row.sum1 > 0 && row.incomeRate == null) {
            def msg = row.getCell('incomeRate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть заполнено!")
        }

        // Проверка коэффициента
        if (row.sum1 != null && row.sum1 == 0 && row.incomeRate != null) {
            def msg = row.getCell('incomeRate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть не заполнено!")
        }

        // Проверка положительной суммы дохода
        if (row.sum1 != null && row.sum2 != null && row.sum2 < row.sum1) {
            def msg1 = row.getCell('sum1').column.name
            def msg2 = row.getCell('sum2').column.name
            logger.error("Строка $rowNum: Значение графы «$msg2» должно быть не меньше значения графы «$msg1»!")
        }

        // Проверка корректности суммы дохода
        String msg6 = row.getCell('course').column.name
        String msg10 = row.getCell('count').column.name
        String msg12 = row.getCell('taxPrice').column.name
        String msg13 = row.getCell('sum1').column.name
        String msg14 = row.getCell('incomeRate').column.name
        String msg15 = row.getCell('sum2').column.name
        String msg16 = row.getCell('sum3').column.name
        if (row.sum2 != null) {
            if (row.incomeRate != null && row.sum1 != null && row.sum1 > 0 && row.taxPrice == null) {
                if (row.sum2 != calc15(row)) {
                    logger.error("Строка $rowNum: Значение графы «$msg15» должно быть равно произведению значений граф «$msg13» и «$msg14»!")
                }
            } else if (row.incomeRate == null && row.sum1 != null && row.sum1 > 0 && row.taxPrice != null) {
                if (row.sum2 != calc15(row)) {
                    logger.error("Строка $rowNum: Значение графы «$msg15» должно быть равно произведению значений граф «$msg12», «$msg10» и «$msg6»!")
                }
            } else if (row.sum1 != null && row.sum1 == 0 && row.taxPrice != null) {
                if (row.sum2 != calc15(row)) {
                    logger.error("Строка $rowNum: Значение графы «$msg15» должно быть равно значению графы «$msg12»!")
                }
            } else if (row.sum2 != 0) {
                logger.error("Строка $rowNum: Значение графы «$msg15» заполнено значением «0», т.к. не выполнен порядок заполнения графы!")
            }
        }

        // Проверка корректности суммы доначисления дохода
        if (row.sum1 != null && row.sum1 == 0 && row.taxPrice != null) {
            if (row.sum3 != calc16(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg16» должно быть равно значению графы «$msg12»!")
            }
        } else if (row.sum3 != calc16(row)) {
            logger.error("Строка $rowNum: Значение графы «$msg16» должно быть равно разности значений граф «$msg15» и «$msg13»!")
        }

    }

    //  Проверка наличия всех фиксированных строк
    //  Проверка отсутствия лишних фиксированных строк
    //  Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

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

    for (row in dataRows) {
        row.sum2 = calc15(row)
        row.sum3 = calc16(row)
    }

    // Сортировка
    sortRows(dataRows, groupColumns)

    // Добавление подитогов
    addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, dataRows)
        }
    }, groupColumns)

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    sortFormDataRows(false)
}

def calc15(def row) {
    if (row.incomeRate != null && row.sum1 != null && row.sum1 > 0 && row.taxPrice == null) {
        return roundValue(row.sum1 * row.incomeRate, 2)
    }
    if (row.incomeRate == null && row.sum1 != null && row.sum1 > 0 && row.taxPrice != null) {
        return roundValue(row.taxPrice * row.count * row.course, 2)
    }
    if (row.sum1 != null && row.sum1 == 0 && row.taxPrice != null) {
        return row.taxPrice
    }
    return 0
}

def calc16(def row) {
    if (row.sum1 != null && row.sum1 == 0 && row.taxPrice != null) {
        return row.taxPrice
    } else if (row.sum1 != null && row.sum2 != null) {
        return row.sum2 - row.sum1
    }
    return null
}
/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(BigDecimal value, def precision) {
    value.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Всего'
    totalRow.getCell('fix').colSpan = 3
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 16
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
        if (rowValues[INDEX_FOR_SKIP] == "Всего") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains('Итого')) {
            def subTotalRow = getNewSubTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            if (totalRowFromFileMap[subTotalRow.getIndex()] == null) {
                totalRowFromFileMap[subTotalRow.getIndex()] = []
            }
            totalRowFromFileMap[subTotalRow.getIndex()].add(subTotalRow)
            rows.add(subTotalRow)

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

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        // получить посчитанные подитоги
        def tmpSubTotalRows = calcSubTotalRows(rows)
        tmpSubTotalRows.each { subTotalRow ->
            def totalRows = totalRowFromFileMap[subTotalRow.getIndex()]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getIndex())
            } else {
                row = rows[subTotalRow.getIndex() - 1]
                def groupValue = getValuesByGroupColumn(row)
                rowWarning(logger, null, String.format(GROUP_WRONG_ITOG, groupValue))
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    rowWarning(logger, totalRow, String.format(GROUP_WRONG_ITOG_ROW, totalRow.getIndex()))
                }
            }
        }
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
            ([(headerRows[0][8]): 'Основание для совершения операции']),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'transDoneDate')]),
            ([(headerRows[1][6]): getColumnName(tmpRow, 'course')]),
            ([(headerRows[1][7]): getColumnName(tmpRow, 'incomeCode')]),
            ([(headerRows[1][8]): 'номер']),
            ([(headerRows[1][9]): 'дата']),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'dealPrice')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'taxPrice')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'sum1')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'incomeRate')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'sum2')]),
            ([(headerRows[1][16]): getColumnName(tmpRow, 'sum3')])
    ]
    (2..16).each {
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
    newRow.transDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 6
    newRow.course = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.incomeCode = values[colIndex]
    colIndex++

    // графа 8
    newRow.reasonNumber = values[colIndex]
    colIndex++

    // графа 9
    newRow.reasonDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графы 10-16
    ['count', 'dealPrice', 'taxPrice', 'sum1', 'incomeRate', 'sum2', 'sum3'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
}
/**
 * Получить новую подитоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getSubTotalRow(rowIndex)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 7
    def colIndex = 7
    newRow.incomeCode = values[colIndex]

    // графа 17
    colIndex = 16
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}
/**
 * Получить подитоговую строку с заданными стилями.
 *
 * @param i номер строки
 */
DataRow<Cell> getSubTotalRow(int i) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.fix = 'Итого'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 3
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}
// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}
// Получить посчитанные подитоговые строки
def calcSubTotalRows(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    updateIndexes(tmpRows)
    return tmpRows.findAll { it.getAlias() }
}
// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = getSubTotalRow(i)

    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    calcTotalSum(rows, newRow, totalColumns)
    newRow.incomeCode = dataRows.get(i).incomeCode

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

    // графа 16
    colIndex = 16
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), dataRows.find {
        it.getAlias() == 'total'
    }, true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRows = calcSubTotalRows(dataRows)
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
    // все строки, кроме общего итога
    def groupRows = dataRows.findAll { !'total'.equals(it.getAlias()) }
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            for (def column : totalColumns) {
                if (row1[column] != row2[column]) {
                    return getColumnName(row1, column)
                }
            }
            return null
        }
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def value
    // графа 7
    if (row?.incomeCode) {
        value = row.incomeCode
    } else {
        value = 'графа 7 не задана'
    }
    return value
}