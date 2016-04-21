package form_template.deal.app_6_10_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 6.10-1. Отчет в отношении доходов ПАО Сбербанк, связанных с предоставлением гарантий
 *
 * formTemplateId = 823
 *
 * @author EMamedova
 */

// графа 1  - rowNumber    -  № п/п
// графа 2  - name         -  Полное наименование с указанием ОПФ
// графа 3  - iksr         -  ИНН/ КИО
// графа 4  - countryCode  -  Страна регистрации
// графа 5  - docNumber    -  Номер договора
// графа 6  - docDate      -  Дата договора
// графа 7  - dealNumber   -  Номер сделки
// графа 8  - dealDate     -  Дата сделки
// графа 9  - sum          -  Сумма доходов Банка по данным бухгалтерского учета, руб.
// графа 10 - price        -  Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// графа 11 - cost         -  Итого стоимость без учета НДС, акцизов и пошлины, руб.
// графа 12 - dealDoneDate -  Дата совершения сделки
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
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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
def allColumns = ['rowNumber', 'name', 'iksr', 'countryCode', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'price', 'cost', 'dealDoneDate']
// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryCode', 'price', 'cost']

// Группируемые атрибуты
@Field
def groupColumns = ['name', 'docNumber', 'docDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'price', 'cost', 'dealDoneDate']

@Field
def totalColumns = ['sum', 'price', 'cost']

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
    def yearStartDate = Date.parse('dd.MM.yyyy', '01.01.' + getReportPeriodEndDate().format('yyyy'))

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // Проверка корректности даты заключения сделки
        checkDatePeriod(logger, row, 'dealDate', 'docDate', getReportPeriodEndDate(), true)

        // Проверка суммы доходов
        if (row.sum!=null && row.sum < 0) {
            def msg = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка цены
        if (row.sum!=null && row.price != row.sum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Проверка стоимости
        if (row.sum!=null && row.cost != row.sum) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('sum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
        }

        // Проверка корректности даты совершения сделки
        checkDatePeriodExt(logger, row, 'dealDoneDate', 'dealDate', yearStartDate, getReportPeriodEndDate(), true)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итого"
        row.cost = row.sum
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 12
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Общая информация о контрагенте - юридическом лице'
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
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'sum')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'dealDoneDate')])
    ]
    (0..11).each{
        headerMapping.add(([(headerRows[2][it]): 'гр. ' + (it + 1)]))
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

    // графа 4
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
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
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 10
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

void importTransportData() {
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 12
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2, ..)
    int rowIndex = 0        // номер строки в НФ (1, 2, ..)
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (rowCells == null || !isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                break
            }
            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex, false)
            if (newRow) {
                newRows.add(newRow)
            }
        }
    } finally {
        reader.close()
    }

    // отображать ошибки переполнения разряда
    showMessages(newRows, logger)

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param isTotal признак итоговой строки
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, def isTotal) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    def int colOffset = 1

    if (!isTotal) {
        def String iksrName = getColumnName(newRow, 'iksr')
        def nameFromFile = pure(rowCells[2])
        def recordId = getTcoRecordId(nameFromFile, pure(rowCells[3]), iksrName, fileRowIndex, 2, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
        def map = getRefBookValue(520, recordId)

        // графа 2
        newRow.name = recordId
        // графа 3
        if (map != null) {
            def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
            expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
            formDataService.checkReferenceValue(pure(rowCells[3]), expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, 3 + colOffset, logger, false)
        }
        // графа 4
        if (map != null) {
            def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
            if (countryMap != null) {
                formDataService.checkReferenceValue(pure(rowCells[4]), [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, 4 + colOffset, logger, false)
            }
        }
        // графа 5
        newRow.docNumber = pure(rowCells[5])
        // графа 6
        newRow.docDate = parseDate(pure(rowCells[6]), "dd.MM.yyyy", fileRowIndex, 7 + colOffset, logger, true)
        // графа 7
        newRow.dealNumber = pure(rowCells[7])
        // графа 8
        newRow.dealDate =  parseDate(pure(rowCells[8]), "dd.MM.yyyy", fileRowIndex, 8 + colOffset, logger, true)
        // графа 12
        newRow.dealDoneDate = parseDate(pure(rowCells[12]), "dd.MM.yyyy", fileRowIndex, 12 + colOffset, logger, true)
    }
    // графа 9
    newRow.sum = parseNumber(pure(rowCells[9]), fileRowIndex, 9 + colOffset, logger, true)
    // графа 10
    newRow.price = parseNumber(pure(rowCells[10]), fileRowIndex, 10 + colOffset, logger, true)
    // графа 11
    newRow.cost = parseNumber(pure(rowCells[11]), fileRowIndex, 11 + colOffset, logger, true)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
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