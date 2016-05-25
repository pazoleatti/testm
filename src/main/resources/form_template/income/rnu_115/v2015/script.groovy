package form_template.income.rnu_115.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * РНУ 115. Регистр налогового учёта доходов и расходов, возникающих в связи с применением в сделках с драгоценными металлами
 * с Взаимозависимыми лицами и резидентами оффшорных зон курса, не соответствующего рыночному уровню
 *
 * formTemplateId =842
 *
 * @author Stanislav Yasinskiy
 */
// fix					() -
// rowNumber			(1) -
// dealNum				(2) - Номер сделки
// dealType				(3) - Вид сделки
// dealDate				(4) - Дата заключения сделки
// dealDoneDate			(5) - Дата окончания сделки
// iksr					(6) - Идентификационный номер
// countryName			(7) - Страна местоположения взаимозависимого лица (резидента оффшорной зоны)
// name					(8) - Контрагент
// dealFocus			(9) - Тип сделки
// reqCurCode			(10) - Код валюты (драгоценных металлов) по сделке приобретения (требования)
// reqVolume			(11) - Объем покупаемой валюты / драгоценных металлов (в граммах)
// guarCurCode			(12) - Код валюты (драгоценных металлов) по сделке продажи (обязательства)
// guarVolume			(13) - Объем продаваемой валюты / драгоценных металлов (в граммах)
// price				(14) - Цена сделки
// reqCourse			(15) - Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб. В отношении требования
// guarCourse			(16) - Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб. В отношении обязательства
// reqSum				(17) - Требования (обязательства) по сделке, руб. Требования
// guarSum				(18) - Требования (обязательства) по сделке, руб. Обязательства
// incomeSum			(19) - Доходы
// outcomeSum			(20) - Расходы
// marketPrice			(21) - Рыночная цена сделки
// incomeDelta			(22) - Отклонения по доходам, в руб
// outcomeDelta			(23) - Отклонения по расходам, в руб


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

@Field
def calcColumns = ['incomeSum', 'outcomeSum','incomeDelta', 'outcomeDelta']

@Field
def sortColumns = ["name", "dealNum"]

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
    direction1 = getRecordId(20, 'DIRECTION', 'покупка')
    direction2 = getRecordId(20, 'DIRECTION', 'продажа')

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
        if (row.dealDate && row.dealDoneDate && (row.dealDoneDate.before(getReportPeriodStartDate()) ||
                row.dealDoneDate.after(getReportPeriodEndDate()) || row.dealDoneDate < row.dealDate)) {
            def msg4 = row.getCell('dealDate').column.name
            def msg5 = row.getCell('dealDoneDate').column.name
            def dateFrom = getReportPeriodStartDate()?.format('dd.MM.yyyy')
            def dateTo = getReportPeriodEndDate()?.format('dd.MM.yyyy')
            logger.error("Строка $rowNum: Дата по графе «$msg5» должна принимать значение из диапазона $dateFrom - $dateTo и быть больше либо равна дате по графе «$msg4»!")
        }

        // Проверка типа сделки
        if (row.dealFocus && (row.dealFocus != direction1 && row.dealFocus != direction2)) {
            def msg = row.getCell('dealFocus').column.name
            logger.error("Строка $rowNum: Графа «$msg» может содержать только одно из значений: «покупка», продажа»!")
        }

        // Проверка положительного значения графы 11, 13, 14-16
        ['reqVolume', 'guarVolume', 'reqCourse', 'guarCourse'].each {
            if (row[it] != null && row[it] < 0) {
                def msg = row.getCell(it).column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
            }
        }

        // Проверка корректности суммы требований
        if (row.reqVolume != null && row.reqCourse != null && (row.dealType == dealType1 || row.dealType == dealType2) &&
                row.reqSum != ScriptUtils.round(row.reqVolume * row.reqCourse, row.getCell('reqSum').getColumn().precision).abs()) {
            def msg1 = row.getCell('reqSum').column.name
            def msg2 = row.getCell('reqVolume').column.name
            def msg3 = row.getCell('reqCourse').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно равняться модулю произведения «$msg2» и «$msg3»!")
        }

        // Проверка суммы обязательств
        if (row.guarSum != null && row.guarSum > 0) {
            def msg = row.getCell('guarSum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть меньше или равно «0»!")
        }

        // Проверка корректности суммы обязательств
        if (row.reqVolume != null && row.reqCourse != null && (row.dealType == dealType1 || row.dealType == dealType2) &&
                row.guarSum != -1 * ScriptUtils.round(row.guarVolume * row.guarCourse, row.getCell('guarSum').getColumn().precision).abs()) {
            flag = false
            def msg1 = row.getCell('guarSum').column.name
            def msg2 = row.getCell('guarVolume').column.name
            def msg3 = row.getCell('guarCourse').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно равняться модулю произведения граф «$msg2» и «$msg3» со знаком «-»!")
        }

        // Проверка рыночной цены
        if (row.marketPrice != null && row.marketPrice <= 0) {
            def msg = row.getCell('marketPrice').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
        }

        // Проверка расчётных граф
        def needValue = formData.createDataRow()
        needValue.incomeSum = calc19(row)
        needValue.outcomeSum = calc20(row)
        needValue.incomeDelta = calc22(row)
        needValue.outcomeDelta = calc23(row)
        checkCalc(row, calcColumns, needValue, logger, true)
    }

    // Проверка итоговых значений по фиксированной строке «Итого»
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
        if (row.getAlias() != null) {
            continue
        }
        // 19, 20
        row.incomeSum = calc19(row)
        row.outcomeSum = calc20(row)
        // 22, 23
        row.incomeDelta = calc22(row)
        row.outcomeDelta = calc23(row)
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

def BigDecimal calc19(def row) {
    if (row.reqSum != null && row.guarSum != null) {
        def diff = row.reqSum + row.guarSum
        return diff > 0 ? diff : 0
    }
    return null
}

def BigDecimal calc20(def row) {
    if (row.reqSum != null && row.guarSum != null) {
        def diff = row.reqSum + row.guarSum
        return diff < 0 ? diff : 0
    }
    return null
}

def BigDecimal calc22(def row) {
    if (row.incomeSum != null) {
        if (row.incomeSum == 0) {
            return 0
        }
        if (row.dealType != null) {
            dealType1 = getRecordId(92, 'NAME', 'Кассовая сделка')
            dealType2 = getRecordId(92, 'NAME', 'Срочная сделка')
            dealType3 = getRecordId(92, 'NAME', 'Премия по опциону')
            if (row.dealType == dealType1 || row.dealType == dealType2) {
                if (row.dealFocus != null && row.price != null && row.marketPrice != null) {
                    direction1 = getRecordId(20, 'DIRECTION', 'покупка')
                    direction2 = getRecordId(20, 'DIRECTION', 'продажа')
                    if (row.dealFocus == direction2 && row.price >= row.marketPrice) {
                        return 0
                    }
                    if (row.dealFocus == direction2 && row.price < row.marketPrice) {
                        if (row.guarVolume != null && row.reqCourse != null) {
                            return ScriptUtils.round(row.guarVolume * (row.marketPrice - row.price) * row.reqCourse, 2)
                        }
                    }
                    if (row.dealFocus == direction1 && row.price <= row.marketPrice) {
                        return 0
                    }
                    if (row.dealFocus == direction1 && row.price > row.marketPrice) {
                        if (row.reqVolume != null && row.guarCourse != null) {
                            return ScriptUtils.round(row.reqVolume * (row.price - row.marketPrice) * row.guarCourse, 2)
                        }
                    }
                }
            } else if (row.dealType == dealType3) {
                if (row.price != null && row.marketPrice != null) {
                    if (row.price > row.marketPrice) {
                        return 0
                    }
                    if (row.price < row.marketPrice) {
                        if (row.reqCourse != null) {
                            return ScriptUtils.round((row.marketPrice - row.price) * row.reqCourse, 2)
                        }
                    }
                    if (row.price == row.marketPrice) {
                        return null
                    }
                }
            }
        }
    }
    return null
}

def BigDecimal calc23(def row) {
    if (row.outcomeSum != null) {
        if (row.outcomeSum == 0) {
            return 0
        }
        if (row.dealType != null) {
            dealType1 = getRecordId(92, 'NAME', 'Кассовая сделка')
            dealType2 = getRecordId(92, 'NAME', 'Срочная сделка')
            dealType3 = getRecordId(92, 'NAME', 'Премия по опциону')
            if (row.dealType == dealType1 || row.dealType == dealType2) {
                direction1 = getRecordId(20, 'DIRECTION', 'покупка')
                direction2 = getRecordId(20, 'DIRECTION', 'продажа')
                if (row.dealFocus != null && row.price != null && row.marketPrice != null) {
                    if (row.dealFocus == direction1 && row.price <= row.marketPrice) {
                        return 0
                    }
                    if (row.dealFocus == direction1 && row.price > row.marketPrice) {
                        if (row.reqVolume != null && row.guarCourse != null) {
                            return ScriptUtils.round(row.reqVolume * (row.price - row.marketPrice) * row.guarCourse, 2)
                        }
                    }
                    if (row.dealFocus == direction2 && row.price >= row.marketPrice) {
                        return 0
                    }
                    if (row.dealFocus == direction2 && row.price < row.marketPrice) {
                        if (row.guarVolume != null && row.reqCourse != null) {
                            return ScriptUtils.round(row.guarVolume * (row.marketPrice - row.price) * row.reqCourse, 2)
                        }
                    }
                }
            } else if (row.dealType == dealType3) {
                if (row.price != null && row.marketPrice != null) {
                    if (row.price < row.marketPrice) {
                        return 0
                    }
                    if (row.price > row.marketPrice) {
                        if (row.guarCourse != null) {
                            return ScriptUtils.round((row.marketPrice - row.price) * row.guarCourse, 2)
                        }
                    }
                    if (row.price == row.marketPrice) {
                        return null
                    }
                }
            }
        }
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
            ([(headerRows[0][2]): getColumnName(tmpRow, 'dealNum')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'dealType')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'dealFocus')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'reqCurCode')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'reqVolume')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'guarCurCode')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'guarVolume')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[0][15]): 'Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб.']),
            ([(headerRows[0][16]): '']),
            ([(headerRows[0][17]): 'Требования (обязательства) по сделке, руб.']),
            ([(headerRows[0][18]): '']),
            ([(headerRows[0][19]): 'Доходы (расходы) учитываемые в целях налога на прибыль по сделке, руб.']),
            ([(headerRows[0][20]): '']),
            ([(headerRows[0][21]): getColumnName(tmpRow, 'marketPrice')]),
            ([(headerRows[0][22]): getColumnName(tmpRow, 'incomeDelta')]),
            ([(headerRows[0][23]): getColumnName(tmpRow, 'outcomeDelta')])
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
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[8]

    def int colIndex = 2

    // графа 2
    newRow.dealNum = values[colIndex]
    colIndex++

    // графа 3
    newRow.dealType = getRecordIdImport(92, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 4
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 5
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    def recordId = getTcoRecordId(nameFromFile, values[6], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 6
    if (map != null) {
        def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
        expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 7
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 8
    newRow.name = recordId
    colIndex++

    // графа 9
    newRow.dealFocus = getRecordIdImport(20, 'DIRECTION', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.reqCurCode = getRecordIdImport(542, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.reqVolume = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.guarCurCode = getRecordIdImport(542, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13
    newRow.guarVolume = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графы 14-23
    ['price', 'reqCourse', 'guarCourse', 'reqSum', 'guarSum', 'incomeSum', 'outcomeSum', 'marketPrice', 'incomeDelta', 'outcomeDelta'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
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

    // графа 22
    def colIndex = 22
    newRow.incomeDelta = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex = 23
    newRow.outcomeDelta = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // прибыль ТЦО
        // "первый квартал", "полугодие", "девять месяцев", "год"
        switch (reportPeriodService.get(formData.reportPeriodId).order) {
            case 2: specialPeriod.name = "полугодие"
                break
            case 3: specialPeriod.name = "девять месяцев"
                break
            case 4: specialPeriod.name = "год"
                break
        }
        // для справочников начало от 01.01.year (для прибыли start_date)
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}
