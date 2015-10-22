package form_template.income.output5.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Сведения о уплаченных суммах налога по операциям с ГЦБ.
 * formTemplateId=420
 *
 * TODO:
 *      - колонтитул
 *      - код формы
 *
 * @author Ramil Timerbaev
 */

// графа 1 - rowNum
// графа 2 - name
// графа 3 - date
// графа 4 - sum

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
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
}

// Проверяемые на пустые значения атрибуты (графа 1..4)
@Field
def nonEmptyColumns = ['date', 'sum']

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        def index = row.getIndex()

        // 1. Проверка на заполнение
        if (row.getAlias() != 'R2') {
            if(row.getAlias() == 'R1'){
                checkNonEmptyColumns(row, index, nonEmptyColumns - 'date', logger, true)
            } else{
                checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)
            }
        }
        // Проверка диапазона дат
        if (row.date) {
            checkDateValid(logger, row, 'date', row.date, true)
        }
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 4
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
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

    def dataRows = formDataService.getDataRowHelper(formData).allCached

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
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def dataRow = dataRows.get(rowIndex - 1)
        // заполнить строку нф значениями из эксель
        fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(dataRows)
        formDataService.getDataRowHelper(formData).allCached = dataRows
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
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[0][0]): getColumnName(tmpRow, 'rowNum'),
            (headerRows[0][1]): getColumnName(tmpRow, 'name'),
            (headerRows[0][2]): 'Отчётный квартал',
            (headerRows[1][2]): 'дата',
            (headerRows[1][3]): 'сумма',
    ]
    (1..4).each { index ->
        headerMapping.put((headerRows[2][index - 1]), index.toString())
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)
    def colIndex = -1
    def tmpRow = formData.createStoreMessagingDataRow()

    // графа 1
    colIndex++
    tmpRow.rowNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 2
    colIndex++
    tmpRow.name = values[colIndex]

    // Проверить фиксированные значения (графа 1, 2)
    ['rowNum', 'name'].each { alias ->
        def value = StringUtils.cleanString(tmpRow[alias]?.toString())
        def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, rowIndex, alias, logger, true)
    }

    // графа 3
    colIndex++
    if(rowIndex != 1 && rowIndex != 2){
        dataRow.getCell('date').setCheckMode(true)
        dataRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    }else{
        dataRow.date = null
    }

    // графа 4
    colIndex++
    if(rowIndex != 2) {
        dataRow.getCell('sum').setCheckMode(true)
        dataRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    } else {
        dataRow.sum = null
    }
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 4
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRows = formDataService.getDataRowHelper(formData).allCached

    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (!isEmptyCells(rowCells)) {
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
                    totalTF = formData.createStoreMessagingDataRow()
                    fillRow(totalTF, rowCells, COLUMN_COUNT, fileRowIndex, false)
                }
                break
            }
            setValues(dataRows, rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
        }
    } finally {
        reader.close()
    }

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['sum' : 4]

        // подсчет итогов
        def itogValues = calcItog(dataRows)

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = itogValues[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    }
    showMessages(dataRows, logger)
}

def calcItog(def dataRows) {
    def totalColumns = ['sum']
    def itogValues = [:]
    totalColumns.each {alias ->
        itogValues[alias] = BigDecimal.ZERO
    }
    for (def row in dataRows) {
        if (row.getAlias() == 'R2') {
            continue
        }
        totalColumns.each { alias ->
            itogValues[alias] += (row.getCell(alias).value ?: 0)
        }
    }
    return itogValues
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

/** Устанавливает значения из тф в строку нф. */
def setValues(def dataRows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    if (rowIndex - 1 >= dataRows.size()) {
        return false
    }
    // найти нужную строку нф
    def dataRow = dataRows.get(rowIndex - 1)
    // заполнить строку нф значениями из тф
    return fillRow(dataRow, rowCells, columnCount, fileRowIndex, true)
}

/**
 * Заполняет заданную строку нф (любую) значениями из тф.
 *
 * @param dataRow строка нф
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param checkFixedValues проверить ли фиксированные значения (при заполнении итоговой строки это не нужно делать)
 *
 * @return вернет true или false, если количество значений в строке тф меньше
 */
def fillRow(def dataRow, String[] rowCells, def columnCount, def fileRowIndex, def checkFixedValues) {
    dataRow.setImportIndex(fileRowIndex)
    if (rowCells.length != columnCount + 2) {
        rowError(logger, dataRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return false
    }
    def colOffset = 1
    def colIndex

    if (checkFixedValues) {
        def values = [:]

        // графа 1
        colIndex = 1
        values.rowNum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

        // графа 2
        colIndex = 2
        values.name = pure(rowCells[colIndex])

        // графа 3
        colIndex = 3
        fillTempOrRow(values, dataRow, 'date', parseDate(pure(rowCells[colIndex]), 'dd.MM.yyyy', fileRowIndex, colIndex + colOffset, logger, true))

        // графа 4
        colIndex = 4
        fillTempOrRow(values, dataRow, 'sum', parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true))

        // Проверить фиксированные значения
        values.keySet().each { alias ->
            def value = values[alias]?.toString()
            def valueExpected = pure(dataRow.getCell(alias).value?.toString())
            checkFixedValue(dataRow, value, valueExpected, fileRowIndex, alias, logger, true)
        }

        return true
    }

    // графа 3
    colIndex = 3
    dataRow.date = parseDate(pure(rowCells[colIndex]), 'dd.MM.yyyy', fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4
    colIndex = 4
    dataRow.sum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)

    return true
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

// заполняем временную карту или строку
void fillTempOrRow(def tmpValues, def dataRow, String alias, def value) {
    if (dataRow.getCell(alias)?.editable) {
        dataRow[alias] = value
    } else {
        tmpValues[alias] = value
    }
}

