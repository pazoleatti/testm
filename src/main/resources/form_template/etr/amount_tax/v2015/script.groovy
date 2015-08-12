package form_template.etr.amount_tax.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Величины налоговых платежей, вводимые вручную
 * formTemplateId = 700
 *
 * @author Stanislav Yasinskiy
 *
 * графа 1 - rowNum        - № п/п
 * графа 2 - taxName       - Наименование налога
 * графа 3 - filledBy      - Графа 4 заполняется
 * графа 4 - sum           - Сумма, тыс. руб.
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
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

// Редактируемые атрибуты
@Field
def editableColumns = ['sum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = []

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['sum']

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        def rowNum = row.getIndex()
        if ((isBank() && rowNum in [3, 4, 5]) || !isBank()) { // для банка проверяем только 3-5 строки
            checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
        }
    }
}

boolean isBank() {
    return formData.departmentId == 1
}

void importData() {
    def tmpRow = formData.createDataRow()
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues)
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
    def allValuesCount = allValues.size()

    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // формирование строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[i]
        fileRowIndex++
        rowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            break
        }
        // прервать по загрузке нужных строк
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def alias = "R" + rowIndex
        def dataRow = dataRows.find { it.getAlias() == alias }
        if (dataRow == null) {
            continue
        }
        // заполнить строку нф значениями из эксель
        fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(dataRows)
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 */
void checkHeaderXls(def headerRows) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[1].size(), headerRows.size(), 4, 2)
    def headerMapping = [
            (headerRows[0][0]): '№ п/п',
            (headerRows[0][1]): 'Наименование налога',
            (headerRows[0][2]): 'Графа 4 заполняется',
            (headerRows[0][3]): 'Сумма, тыс. руб.'
    ]
    (0..3).each {
        headerMapping.put((headerRows[1][it]), (it + 1).toString())
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

    //очищаем столбцы
    dataRow['sum'] = null

    def rowNum = dataRow.rowNum as Integer
    def taxName = normalize(dataRow.taxName)
    def filledBy = normalize(dataRow.filledBy)


    def colIndex = 0
    def rowNumImport = values[colIndex] as Integer

    colIndex++
    def taxNameImport = normalize(values[colIndex])

    colIndex++
    def filledByImport = normalize(values[colIndex])

    if (!(rowNum == rowNumImport && taxName == taxNameImport && filledBy == filledByImport)) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с номером строки = $rowNum.")
        return
    }

    colIndex++
    dataRow['sum'] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

}
