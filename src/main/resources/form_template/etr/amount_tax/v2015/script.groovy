package form_template.etr.amount_tax.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Величины налоговых платежей, вводимые вручную
 * formTemplateId = 700
 *
 * @author Stanislav Yasinskiy
 *
 * графа 1 - rowNum        - № п/п
 * графа 2 - taxName       - Наименование показателя
 * графа 3 - filledBy      - Графа 4 заполняется
 * графа 4 - sum           - Сумма, тыс. руб.
 */

switch (formDataEvent) {
    case FormDataEvent.GET_HEADERS:
        headers.get(0).sum = 'Сумма, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        break
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
    case FormDataEvent.COMPOSE:
        consolidation()
        logicCheck()
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

@Field
def startDate = null

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

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // очистить графу 4
    dataRows.each { row ->
        row.sum = null
    }

    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                // суммируем графу 4 из источников
                for (def row : dataRows) {
                    def sourceRow = getDataRow(sourceRows, row.getAlias())
                    row.sum = (row.sum ?: BigDecimal.ZERO) + ((source.departmentId == 1) ? 1000 : 1) * (sourceRow.sum ?: BigDecimal.ZERO)
                }
            }
        }
    }
    if (isBank()) { // если уровень банка, то тысячи понижаем до миллионов
        dataRows.each { row ->
            if (row.sum) {
                row.sum = (row.sum as BigDecimal).divide(BigDecimal.valueOf(1000), BigDecimal.ROUND_HALF_UP)
            }
        }
    }
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
            ([(headerRows[0][0]): '№ п/п']),
            ([(headerRows[0][1]): 'Наименование показателя']),
            ([(headerRows[0][2]): 'Графа 4 заполняется']),
            ([(headerRows[0][3]): ('Сумма, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))])
    ]
    (0..3).each {
        headerMapping.add(([(headerRows[1][it]): (it + 1).toString()]))
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

    def tmpValues = [:]

    def colIndex = 0
    tmpValues.rowNum = round(parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true), 0)

    colIndex++
    tmpValues.taxName = values[colIndex]

    colIndex++
    tmpValues.filledBy = values[colIndex]

    tmpValues.keySet().asList().each { alias ->
        def value = StringUtils.cleanString(tmpValues[alias]?.toString())
        def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    colIndex++
    dataRow['sum'] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

}
