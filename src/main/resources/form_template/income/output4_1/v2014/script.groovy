package form_template.income.output4_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Сведения о суммах налога на прибыль, уплаченного Банком за рубежом
 * formTemplateId=421
 *
 * графа 1 - rowNumber
 * графа 2 - taxName
 * графа 3 - dealDate
 * графа 4 - taxSum
 *
 * @author Bulat Kinzyabulatov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

@Field
def nonEmptyColumns = ['dealDate', 'taxSum']

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row in dataRows) {
        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 4
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
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
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[0][0]) : getColumnName(tmpRow, 'rowNumber'),
            (headerRows[0][1]) : getColumnName(tmpRow, 'taxName'),
            (headerRows[0][2]) : getColumnName(tmpRow, 'dealDate'),
            (headerRows[0][3]) : getColumnName(tmpRow, 'taxSum')
    ]
    (1..4).each { index ->
        headerMapping.put((headerRows[1][index - 1]), index.toString())
    }
    checkHeaderEquals(headerMapping)
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

    def tmpValues = [:]
    colIndex++
    tmpValues.rowNumber = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++
    tmpValues.taxName = values[colIndex]

    // Проверить фиксированные значения (графа 1, 2)
    tmpValues.keySet().asList().each { alias ->
        def value = StringUtils.cleanString(tmpValues[alias]?.toString())
        def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 3
    colIndex = 2
    dataRow.getCell('dealDate').setCheckMode(true)
    dataRow.dealDate = parseDate(values[colIndex], 'dd.MM.yyyy', fileRowIndex, colIndex + colOffset, logger, false)

    // графа 4
    colIndex = 3
    dataRow.getCell('taxSum').setCheckMode(true)
    dataRow.taxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, false)
}