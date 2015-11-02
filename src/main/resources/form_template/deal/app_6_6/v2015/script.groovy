package form_template.deal.app_6_6.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 806 - 6.6. Заключение сделок РЕПО
 *
 * formTemplateId=804
 *
 * @author Stanislav Yasinskiy
 */

// rowNumber            - № п/п
// name                 - Полное наименование с указанием ОПФ
// iksr                 - ИНН/КИО
// countryName          - Наименование страны регистрации
// countryCode          - Код страны регистрации по классификатору ОКСМ
// docNumber            - Номер договора
// docDate              - Дата договора
// dealNumber           - Номер сделки
// dealDate             - Дата (заключения) сделки
// dealsMode            - Режим переговорных сделок
// date1                - Дата исполнения 1-ой части сделки
// date2                - Дата исполнения 2-ой части сделки
// incomeSum            - Сумма процентного дохода (руб.)
// outcomeSum           - Сумма процентного расхода (руб.)
// priceFirstCurrency   - Цена 1-ой части сделки, ед. валюты
// currencyCode         - Код валюты расчетов по сделке
// courseCB             - Курс ЦБ РФ
// priceFirstRub        - Цена 1-ой части сделки, руб.
// dealDoneDate         - Дата совершения сделки

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

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'date1', 'date2', 'incomeSum',
                       'outcomeSum', 'priceFirstCurrency', 'currencyCode', 'courseCB', 'priceFirstRub']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'countryCode', 'dealsMode', 'dealDoneDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'date1', 'date2', 'priceFirstCurrency',
                       'currencyCode', 'courseCB', 'priceFirstRub']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
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

    Date dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    Date dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time

    String dateFormat = 'yyyy'
    String formYear = (String) reportPeriodService.get(formData.reportPeriodId).getTaxPeriod().getYear()
    Date formDate = Date.parse('dd.MM.yyyy', "31.12.$formYear")

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка возможности заполнения режима переговорных сделок
        def countryCode = null
        if (row.name) {
            def map = getRefBookValue(520, row.name)
            if (map) {
                map = getRefBookValue(10, map.COUNTRY?.referenceValue)
                if (map) {
                    countryCode = map.NAME?.stringValue
                }
            }
        }
        if (!countryCode) {
            String msg1 = row.getCell('dealsMode').column.name
            String msg2 = row.getCell('countryCode').column.name
            logger.error("Строка $rowNum: Выполнение расчета графы «$msg1» невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2»!")
        }

        // 3. Проверка режима переговорных сделок
        if (countryCode) {
            if (row.dealsMode != calc10(row.countryCode)) {
                String msg1 = row.getCell('dealsMode').column.name
                String msg2 = row.getCell('countryCode').column.name
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению «Да», " +
                        "если графа «$msg2» равна значению «643»!")
            }
        }

        // 4. Проверка возможности заполнения даты совершения сделки
        if (!row.date2) {
            String msg1 = row.getCell('dealDoneDate').column.name
            String msg2 = row.getCell('date2').column.name
            logger.error("Строка $rowNum: Выполнение расчета графы «$msg1» невозможно, так как не заполнена " +
                    "используемая в расчете графа «$msg2»!")
        }

        // 5. Проверка даты совершения сделки
        if (row.date2) {
            if (row.dealDoneDate != calc19(row.date2, formYear, formDate, dateFormat)) {
                String msg = row.getCell('dealDoneDate').column.name
                logger.error("Строка $rowNum: Графа «$msg» заполнена неверно!")
            }
        }

        // 6. Заполнение граф 13 и 14 (сумма дохода, расхода)
        boolean noOne = (row.incomeSum == null && row.outcomeSum == null)
        boolean both = (row.incomeSum != null && row.outcomeSum != null)
        if (noOne) {
            String msg1 = row.getCell('outcomeSum').column.name
            String msg2 = row.getCell('incomeSum').column.name
            logger.error("Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
        }
        if (both) {
            String msg1 = row.getCell('outcomeSum').column.name
            String msg2 = row.getCell('incomeSum').column.name
            logger.error("Строка $rowNum: Графа «$msg1» не может быть заполнена одновременно с графой «$msg2»!")
        }

        // 7. Проверка положительной суммы дохода/расхода
        if (!noOne && !both) {
            sum = (row.incomeSum != null) ? row.incomeSum : row.outcomeSum
            if (sum <= 0) {
                String msg = (row.incomeSum != null) ? row.getCell('incomeSum').column.name : row.getCell('outcomeSum').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше значения «0»!")
            }
        }

        // 8. Корректность даты (заключения) сделки
        if (row.dealDate && row.docDate && row.dealDate < row.docDate) {
            String msg1 = row.getCell('dealDate').column.name
            String msg2 = row.getCell('docDate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // 9. Корректность даты исполнения 1–ой части сделки (проверка даты окончания периода)
        // 10. Корректность даты исполнения 1–ой части сделки (проверка даты начала периода)
        Date date1 = row.date1
        if (date1 != null) {
            if (date1 > dTo) {
                String msg = row.getCell('date1').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» не может быть больше даты окончания отчётного периода!")
            }
            if (date1 < dFrom) {
                String msg = row.getCell('date1').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» не может быть меньше даты начала отчётного периода!")
            }
        }

        // 11. Корректность даты совершения сделки
        if (row.dealDoneDate && row.dealDate && row.dealDoneDate < row.dealDate) {
            String msg1 = row.getCell('dealDoneDate').column.name
            String msg2 = row.getCell('dealDate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть не меньше значения графы «$msg2»!")
        }

        // 12. Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    String dateFormat = 'yyyy'
    def formYear = (String) reportPeriodService.get(formData.reportPeriodId).getTaxPeriod().getYear()
    def formDate = Date.parse('dd.MM.yyyy', "31.12.$formYear")

    for (row in dataRows) {
        // Расчет поля "Режим переговорных сделок"
        row.dealsMode = calc10(row.countryCode)
        // Расчет поля "Дата совершения сделки"
        row.dealDoneDate = calc19(row.date2, formYear, formDate, dateFormat)
    }
}

def String calc10(def countryCode) {
    if (countryCode != null) {
        def country = refBookService.getStringValue(10, countryCode, 'CODE')
        if (country == '643') {
            return 'Да'
        }
    }
    return null
}

def Date calc19(Date date2, String formYear, Date formDate, String dateFormat) {
    if (date2) {
        String date2Year = date2.format(dateFormat)
        if (date2Year == formYear) {
            return date2
        } else {
            return formDate
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

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
        // простая строка
        rowIndex++
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
            ([(headerRows[0][0]): 'Общая информация о контрагенте - юридическом лице']),
            ([(headerRows[0][5]): 'Сведения о сделке']),
            ([(headerRows[1][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][1]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][2]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][3]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][4]): getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][5]): getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][6]): getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][7]): getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][8]): getColumnName(tmpRow, 'dealDate')]),
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
            ([(headerRows[2][0]): 'гр. 1']),
            ([(headerRows[2][1]): 'гр. 2']),
            ([(headerRows[2][2]): 'гр. 3']),
            ([(headerRows[2][3]): 'гр. 4.1']),
            ([(headerRows[2][4]): 'гр. 4.2']),
            ([(headerRows[2][5]): 'гр. 5']),
            ([(headerRows[2][6]): 'гр. 6']),
            ([(headerRows[2][7]): 'гр. 7']),
            ([(headerRows[2][8]): 'гр. 8']),
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

    def int colIndex = 1

    // TODO уточнить id и алиас в справочнике SBRFACCTAX-12861
    // графа 2
    newRow.name = getRecordIdImport(520, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    def map = getRefBookValue(520, newRow.name)
    colIndex++

    // TODO уточнить id и алиас в справочнике SBRFACCTAX-12861
    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY?.referenceValue)
        if (map != null) {
            formDataService.checkReferenceValue(10, values[colIndex], map.NAME?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (map != null) {
        formDataService.checkReferenceValue(10, values[colIndex], map.CODE?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
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