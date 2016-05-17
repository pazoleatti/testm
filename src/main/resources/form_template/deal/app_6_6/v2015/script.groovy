package form_template.deal.app_6_6.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 806 - 6.6. Заключение сделок РЕПО
 *
 * formTemplateId=806
 *
 * @author Stanislav Yasinskiy
 */

// графа 1  (1)    - rowNumber            - № п/п
// графа 2  (2)    - name                 - Полное наименование с указанием ОПФ
// графа 3  (3)    - iksr                 - ИНН/КИО
// графа 4  (4.1)  - countryName          - Наименование страны регистрации
// графа 5  (4.2)  - countryCode          - Код страны регистрации по классификатору ОКСМ
// графа 6  (5)    - docNumber            - Номер договора
// графа 7  (6)    - docDate              - Дата договора
// графа 8  (7)    - dealNumber           - Номер сделки
// графа 9  (8)    - dealDate             - Дата (заключения) сделки
// графа 10 (9)    - dealsMode            - Режим переговорных сделок
// графа 11 (10.1) - date1                - Дата исполнения 1-ой части сделки
// графа 12 (10.2) - date2                - Дата исполнения 2-ой части сделки
// графа 13 (11.1) - incomeSum            - Сумма процентного дохода (руб.)
// графа 14 (11.2) - outcomeSum           - Сумма процентного расхода (руб.)
// графа 15 (12)   - priceFirstCurrency   - Цена 1-ой части сделки, ед. валюты
// графа 16 (13)   - currencyCode         - Код валюты расчетов по сделке
// графа 17 (14)   - courseCB             - Курс ЦБ РФ
// графа 18 (15)   - priceFirstRub        - Цена 1-ой части сделки, руб.
// графа 19 (16)   - dealDoneDate         - Дата совершения сделки

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
def allColumns = ['rowNumber', 'name', 'iksr', 'countryName', 'countryCode', 'docNumber',
        'docDate', 'dealNumber', 'dealDate', 'dealsMode', 'date1', 'date2', 'incomeSum', 'outcomeSum',
        'priceFirstCurrency', 'currencyCode', 'courseCB', 'priceFirstRub', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'date1', 'date2', 'incomeSum',
                       'outcomeSum', 'priceFirstCurrency', 'currencyCode', 'courseCB', 'priceFirstRub']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'dealsMode', 'dealDoneDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealDate', 'date1', 'date2', 'priceFirstCurrency',
                       'currencyCode', 'priceFirstRub']

@Field
def totalColumns = ['incomeSum', 'outcomeSum']

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

    def lastDate2099 = Date.parse('dd.MM.yyyy', '31.12.2099')
    def endDateInStr = getReportPeriodEndDate().format('dd.MM.yyyy')

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 3. Корректность даты заключения сделки
        if (row.dealDate && row.docDate && (row.dealDate < row.docDate || getReportPeriodEndDate() < row.dealDate)) {
            String name9 = row.getCell('dealDate').column.name
            String name7 = row.getCell('docDate').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно быть не меньше значения графы «%s» и не больше %s!", name9, name7, endDateInStr)
        }

        // 4. Проверка режима переговорных сделок
        def countryCode = null
        if (row.name) {
            def map = getRefBookValue(520, row.name)
            if (map) {
                map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
                if (map) {
                    countryCode = map.CODE?.stringValue
                }
            }
        }
        if (countryCode && row.dealsMode != calc10(row.name)) {
            String msg1 = row.getCell('dealsMode').column.name
            String msg2 = row.getCell('countryCode').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению «Да», " +
                    "если графа «$msg2» равна значению «643»!")
        }

        // 5. Проверка корректности даты исполнения 1-ой части сделки
        if (row.date1 && row.dealDate && (row.date1 < row.dealDate || getReportPeriodEndDate() < row.date1)) {
            String name9 = row.getCell('dealDate').column.name
            String name11 = row.getCell('date1').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно быть не меньше значения графы «%s» и не больше %s!", name11, name9, endDateInStr)
        }

        // 6. Проверка корректности даты исполнения 2-ой части сделки
        if (row.date2 && row.date1 && (row.date2 < row.date1 || lastDate2099 < row.date2)) {
            String name11 = row.getCell('date1').column.name
            String name12 = row.getCell('date2').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно быть не меньше значения графы «%s» и не больше 31.12.2099!", name12, name11)
        }

        // 7. Заполнение граф 13 и 14 (сумма дохода, расхода)
        if ((row.incomeSum > 0 && row.outcomeSum) || (row.outcomeSum > 0 && row.incomeSum)) {
            String msg1 = row.getCell('incomeSum').column.name
            String msg2 = row.getCell('outcomeSum').column.name
            logger.error("Строка $rowNum: Графа «$msg1» и графа «$msg2» одновременно не могут быть заполнены!")
        }

        // 8. Проверка суммы дохода/расхода
        if (row.incomeSum <= 0 && row.outcomeSum <= 0) {
                String msg1 = row.getCell('incomeSum').column.name
                String msg2 = row.getCell('outcomeSum').column.name
                logger.error("Строка $rowNum: Значение графы «$msg1» или «$msg2» должно быть больше «0»!")
        }

        // 9. Проверка даты совершения сделки
        if (row.date2) {
            String name19 = row.getCell('dealDoneDate').column.name
            if (row.date2 >= getReportPeriodStartDate() && row.date2 <= getReportPeriodEndDate()) {
                if(row.dealDoneDate != row.date2) {
                    String name12 = row.getCell('date2').column.name
                    logger.error("Строка $rowNum: Значение графы «$name19» должно быть равно значению графы «$name12»!")
                }
            } else if (row.dealDoneDate != getReportPeriodEndDate()) {
                String msg2 = getReportPeriodEndDate().format('dd.MM.yyyy')
                logger.error("Строка $rowNum: Значение графы «$name19» должно быть равно «$msg2»!")
            }
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // Расчет поля "Режим переговорных сделок"
        row.dealsMode = calc10(row.name)
        // Расчет поля "Дата совершения сделки"
        row.dealDoneDate = calc19(row.date2)
    }
}

def String calc10(def recordId) {
    if (recordId) {
        def map = getRefBookValue(520, recordId)
        if (map) {
            map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
            if (map) {
                if ('643'.equals(map.CODE?.stringValue)) {
                    return 'Да'
                }
            }
        }
    }
    return null
}

def Date calc19(Date date2) {
    if (date2) {
        if (date2 >= getReportPeriodStartDate() && date2 <= getReportPeriodEndDate()) {
            return date2
        } else {
            return getReportPeriodEndDate()
        }
    }
    return null
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация о контрагенте - юридическом лице'
    String TABLE_END_VALUE = null

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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : 'Общая информация о контрагенте - юридическом лице']),
            ([(headerRows[0][5]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'dealsMode')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'date1')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'date2')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'incomeSum')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'outcomeSum')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'priceFirstCurrency')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'currencyCode')]),
            ([(headerRows[1][16]): getColumnName(tmpRow, 'courseCB')]),
            ([(headerRows[1][17]): getColumnName(tmpRow, 'priceFirstRub')]),
            ([(headerRows[1][18]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][0]) : 'гр. 1']),
            ([(headerRows[2][1]) : 'гр. 2']),
            ([(headerRows[2][2]) : 'гр. 3']),
            ([(headerRows[2][3]) : 'гр. 4.1']),
            ([(headerRows[2][4]) : 'гр. 4.2']),
            ([(headerRows[2][5]) : 'гр. 5']),
            ([(headerRows[2][6]) : 'гр. 6']),
            ([(headerRows[2][7]) : 'гр. 7']),
            ([(headerRows[2][8]) : 'гр. 8']),
            ([(headerRows[2][9]): 'гр. 9']),
            ([(headerRows[2][10]): 'гр. 10.1']),
            ([(headerRows[2][11]): 'гр. 10.2']),
            ([(headerRows[2][12]): 'гр. 11.1']),
            ([(headerRows[2][13]): 'гр. 11.2']),
            ([(headerRows[2][14]): 'гр. 12']),
            ([(headerRows[2][15]): 'гр. 13']),
            ([(headerRows[2][16]): 'гр. 14']),
            ([(headerRows[2][17]): 'гр. 15']),
            ([(headerRows[2][18]): 'гр. 16'])
    ]
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

    def countryMap
    // графа 4.1
    if (map != null) {
        countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (countryMap != null) {
        formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 6
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.dealNumber = values[colIndex]
    colIndex++

    // графа 8
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.dealsMode = values[colIndex]
    colIndex++

    // графа 10.1
    newRow.date1 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10.2
    newRow.date2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11.1
    newRow.incomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11.2
    newRow.outcomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.priceFirstCurrency = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.currencyCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14
    newRow.courseCB = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.priceFirstRub = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 16
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

void afterLoad() {
    if (binding.variables.containsKey("specialPeriod")) {
        // имя периода и конечная дата корректны
        // устанавливаем дату для справочников
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}