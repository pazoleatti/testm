package form_template.deal.app_6_17.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 811 - 6.17: Купля-продажа иностранной валюты
 *
 * formTemplateId=811
 *
 * @author - Emamedova
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
// графа 10 - currencyCode      - Код валюты по сделке
// графа 11 - dealCountryCode   - Код страны происхождения предмета сделки по классификатору ОКСМ
// графа 12 - income            - Сумма доходов Банка по данным бухгалтерского учета, руб.
// графа 13 - outcome           - Сумма расходов Банка по данным бухгалтерского учета, руб.
// графа 14 - price             - Цена
// графа 15 - cost              - Стоимость
// графа 16 - dealDoneDate      - Дата совершения сделки

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
                  'dealDate', 'currencyCode', 'dealCountryCode', 'income', 'outcome', 'price', 'cost', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealDate', 'dealNumber', 'currencyCode', 'dealCountryCode', 'income', 'outcome', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'regCountryCode', 'price', 'cost']

//Непустые атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'currencyCode', 'dealCountryCode', 'dealDoneDate']

// Группируемые атрибуты
@Field
def groupColumns = ['name']

@Field
def totalColumns = ['income', 'outcome']

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
    String dateFormat = 'dd.MM.yyyy'

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка на заполнение доходов и расходов
        boolean noOne = (row.income == null && row.outcome == null)
        boolean both = (row.income != null && row.outcome != null)
        if (noOne) {
            String msg1 = getColumnName(row, 'outcome')
            String msg2 = getColumnName(row, 'income')
            logger.error("Строка $rowNum: Графа «$msg1» должна быть заполнена, если не заполнена графа «$msg2»!")
        }
        if (both) {
            String msg1 = getColumnName(row, 'outcome')
            String msg2 = getColumnName(row, 'income')
            logger.error("Строка $rowNum: Графа «$msg1» не может быть заполнена одновременно с графой «$msg2»!")
        }

        // 3. Проверка положительной суммы дохода/расхода
        if (!noOne && !both) {
            sum = (row.income != null) ? row.income : row.outcome
            if (sum < 0) {
                msg = (row.income != null) ? getColumnName(row, 'income') : getColumnName(row, 'outcome')
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
            }
        }

        // 4. Проверка цены и стоимости
        if (!noOne && !both) {
            boolean comparePrice = row.price && row.price != sum
            boolean compareCost = row.cost && row.cost != sum
            if (comparePrice && compareCost) {
                msg = (row.income != null) ? getColumnName(row, 'income') : getColumnName(row, 'outcome')
                String msg1 = getColumnName(row, 'price')
                String msg2 = getColumnName(row, 'cost')
                logger.error("Строка $rowNum: Значение графы «$msg1» и графы «$msg2» должно быть равно значению графы «$msg»!")
            }
        }

        // 5. Корректность даты заключения сделки
        if (row.docDate && row.dealDate && row.docDate > row.dealDate) {
            def msg1 = getColumnName(row, 'docDate')
            def msg2 = getColumnName(row, 'dealDate')
            logger.error("Строка $rowNum: Значение графы «$msg2» должно быть не меньше значения графы «$msg1»!")
        }

        // 6. Корректность даты совершения сделки
        if (row.dealDoneDate && row.dealDate && row.dealDate > row.dealDoneDate) {
            def msg1 = getColumnName(row, 'dealDate')
            def msg2 = getColumnName(row, 'dealDoneDate')
            logger.error("Строка $rowNum: Значение графы «$msg2» должно быть не меньше значения графы «$msg1»!")
        }

        // 7. Проверка пересечения даты сделки с отчетным периодом
        checkDealDoneDate(logger, row, 'dealDoneDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // 8. Проверка диапазона дат
        if (row.docDate) {
            checkDateValid(logger, row, 'docDate', row.docDate, true)
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
        // Расчет поля "Цена", "Стоимость"
        if(row.income != null && row.outcome == null){
            row.price = row.income
            row.cost = row.income
        }
        else if(row.outcome != null && row.income == null){
            row.price = row.outcome
            row.cost = row.outcome

        } else if(row.outcome == null && row.income == null){
            row.price = null
            row.cost = null
        }
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
    int COLUMN_COUNT = 17
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
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
            ([(headerRows[1][10]): getColumnName(tmpRow, 'currencyCode')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'dealCountryCode')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'income')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'outcome')]),
            ([(headerRows[1][14]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][15]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][16]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][1]) : 'гр. 1']),
            ([(headerRows[2][2]) : 'гр. 2']),
            ([(headerRows[2][3]) : 'гр. 3']),
            ([(headerRows[2][4]) : 'гр. 4.1']),
            ([(headerRows[2][5]) : 'гр. 4.2'])
    ]
    (6..16).each {
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

    // графа 4.1
    if (map != null) {
        map = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (map != null) {
            def expectedValues = [map.NAME?.stringValue, map.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(10, values[colIndex], expectedValues, fileRowIndex, colIndex + colOffset, logger, false)
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
    newRow.currencyCode = getRecordIdImport(15, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    //графа 10
    newRow.dealCountryCode = getRecordIdImport(10, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
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

    // графа 13
    def colIndex = 13
    newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 14
    colIndex = 14
    newRow.outcome = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, dataRows.find { it.getAlias() == 'total' }, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}