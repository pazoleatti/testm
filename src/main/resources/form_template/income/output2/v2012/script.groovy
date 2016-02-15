package form_template.income.output2.v2012

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Расчет налога на прибыль с доходов, удерживаемого налоговым агентом
 *
 * formTemplateId=307
 */

// графа 1  - rowNumber
// графа 2  - title
// графа 3  - zipCode
// графа 4  - subdivisionRF
// графа 5  - area
// графа 6  - city
// графа 7  - region
// графа 8  - street
// графа 9  - homeNumber
// графа 10  - corpNumber
// графа 11 - apartment
// графа 12 - surname
// графа 13 - name
// графа 14 - patronymic
// графа 15 - phone
// графа 16 - sumDividend
// графа 17 - dividendDate
// графа 18 - dividendNum
// графа 19 - dividendSum
// графа 20 - taxDate
// графа 21 - taxNum
// графа 22 - sumTax
// графа 23 - reportYear

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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
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

// Редактируемые атрибуты 2-18, 20-23
@Field
def editableColumns = ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber',
                       'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone', 'sumDividend',
                       'dividendDate', 'dividendNum', 'taxDate', 'taxNum', 'sumTax', 'reportYear']

// Проверяемые на пустые значения атрибуты 1, 2, 4, 12, 13, 16, 17, 22
@Field
def nonEmptyColumns = ['title', 'subdivisionRF', 'surname', 'name', 'sumDividend', 'dividendDate', 'sumTax']

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

void calc(){
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (!dataRows.isEmpty()) {
        for (def row in dataRows) {
            row.dividendSum = (row.sumDividend ?: 0) - (row.sumTax ?: 0)
        }
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
    }
}

void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

void importData() {
    int COLUMN_COUNT = 23
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп.'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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
        if (!rowValues) {
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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): '№ пп.']),
            ([(headerRows[0][1]): 'Получатель']),
            ([(headerRows[0][2]): 'Место нахождения (адрес)']),
            ([(headerRows[0][11]): 'Руководитель организации']),
            ([(headerRows[0][14]): 'Контактный телефон']),
            ([(headerRows[0][15]): 'Сумма начисленных дивидендов']),
            ([(headerRows[0][16]): 'Перечисление дивидендов']),
            ([(headerRows[0][19]): 'Перечисление налога']),
            ([(headerRows[0][22]): 'Отчётный год']),
            ([(headerRows[1][2]): 'Индекс']),
            ([(headerRows[1][3]): 'Код региона']),
            ([(headerRows[1][4]): 'Район']),
            ([(headerRows[1][5]): 'Город']),
            ([(headerRows[1][6]): 'Населённый пункт (село, посёлок и т.п.)']),
            ([(headerRows[1][7]): 'Улица (проспект, переулок и т.д.)']),
            ([(headerRows[1][8]): 'Номер дома (владения)']),
            ([(headerRows[1][9]): 'Номер корпуса (строения)']),
            ([(headerRows[1][10]): 'Номер офиса (квартиры)']),
            ([(headerRows[1][11]): 'Фамилия']),
            ([(headerRows[1][12]): 'Имя']),
            ([(headerRows[1][13]): 'Отчество']),
            ([(headerRows[1][16]): 'Дата']),
            ([(headerRows[1][17]): 'Номер платёжного поручения']),
            ([(headerRows[1][18]): 'Сумма']),
            ([(headerRows[1][19]): 'Дата']),
            ([(headerRows[1][20]): 'Номер платёжного поручения']),
            ([(headerRows[1][21]): 'Сумма'])
    ]
    (0..22).each { index ->
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
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

    // графа 2
    def colIndex = 1
    newRow.title = values[colIndex]

    // графа 3
    colIndex++
    newRow.zipCode = values[colIndex]

    // графа 4 - справочник "Коды субъектов Российской Федерации"
    colIndex++
    newRow.subdivisionRF = getRecordIdImport(4, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 5..15
    ['area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname',
            'name', 'patronymic', 'phone'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 16
    colIndex++
    newRow.sumDividend = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 17
    colIndex++
    newRow.dividendDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 18
    colIndex++
    newRow.dividendNum = values[colIndex]

    // графа 19
    colIndex++
    newRow.dividendSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 20
    colIndex++
    newRow.taxDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 21
    colIndex++
    newRow.taxNum = values[colIndex]

    // графа 22
    colIndex++
    newRow.sumTax = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex++
    newRow.reportYear = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

void importTransportData() {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 23
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try{
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
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    showMessages(newRows, logger)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [
                'sumDividend'        : 16,
                'sumTax'             : 22
        ]
        // итоговая строка для сверки сумм
        def totalTmp = formData.createDataRow()
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }
        // подсчет итогов
        for (def row : newRows) {
            if (row.getAlias()) {
                continue
            }
            totalColumnsIndexMap.keySet().asList().each { alias ->
                def value1 = totalTmp.getCell(alias).value
                def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
                totalTmp.getCell(alias).setValue(value1 + value2, null)
            }
        }

        // сравнение контрольных сумм
        checkTFSum(totalTmp, totalTF, totalColumnsIndexMap.keySet().asList(), totalTF?.getImportIndex(), logger, false)
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return newRow
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 2
    colIndex++
    newRow.title = pure(rowCells[colIndex])

    // графа 3
    colIndex++
    newRow.zipCode = pure(rowCells[colIndex])

    // графа 4 - справочник "Коды субъектов Российской Федерации"
    colIndex++
    if (pure(rowCells[colIndex])) {
        newRow.subdivisionRF = getRecordIdImport(4, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    }

    // графа 5..15
    ['area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment', 'surname',
     'name', 'patronymic', 'phone'].each { alias ->
        colIndex++
        newRow[alias] = pure(rowCells[colIndex])
    }

    // графа 16
    colIndex++
    newRow.sumDividend = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 17
    colIndex++
    newRow.dividendDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 18
    colIndex++
    newRow.dividendNum = pure(rowCells[colIndex])

    // графа 19
    colIndex++
//    newRow.dividendSum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 20
    colIndex++
    newRow.taxDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 21
    colIndex++
    newRow.taxNum = pure(rowCells[colIndex])

    // графа 22
    colIndex++
    newRow.sumTax = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex++
    newRow.reportYear = parseDate(pure(rowCells[colIndex]), "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}