package form_template.etr.etr_4_10.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Приложение 4-10. Трансфертное ценообразование – сделки с взаимозависимыми лицами (ВЗЛ) и резидентами оффшорных зон (РОЗ)
 * formTemplateId = 710
 *
 * @author Stanislav Yasinskiy
 *
 * графа 1 - sum1         - Сумма увеличения базы по налогу на прибыль, в том числе не учитываемые расходы, тыс. руб.
 * графа 2 - sum2         - Сумма увеличения базы по налогу на прибыль, в том числе доначисление доходов, тыс. руб.
 * графа 3 - taxBurden    - Налоговое бремя, тыс. руб.
 */

switch (formDataEvent) {
    case FormDataEvent.GET_HEADERS:
        headers.get(0).taxBurden = 'Налоговое бремя, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(1).sum1 = 'не учитываемые расходы, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(1).sum2 = 'доначисление доходов, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        break
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['sum1', 'sum2', 'taxBurden']

// Редактируемые атрибуты
@Field
def editableColumns = ['sum1', 'sum2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['taxBurden']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = allColumns

@Field
def startDateMap = [:]

@Field
def endDateMap = [:]

def getStartDate(int reportPeriodId) {
    if (startDateMap[reportPeriodId] == null) {
        startDateMap[reportPeriodId] = reportPeriodService.getStartDate(reportPeriodId)?.time
    }
    return startDateMap[reportPeriodId]
}

def getEndDate(int reportPeriodId) {
    if (endDateMap[reportPeriodId] == null) {
        endDateMap[reportPeriodId] = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return endDateMap[reportPeriodId]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        row.taxBurden = calc3(row)
    }
}

def BigDecimal calc3(def row) {
    // Графа 3 = (значение графы 1+ значение графы 2) * 0,2
    if (row.sum1 != null && row.sum2 != null) {
        return (row.sum1 + row.sum2) * 0.2
    }
    return null
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        def rowNum = row.getIndex()
        // Проверка заполнения ячеек
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка заполнения граф ы 3
        def needValue = [:]
        needValue['taxBurden'] = calc3(row)
        checkCalc(row, needValue.keySet().asList(), needValue, logger, true)
    }
}

boolean isBank() {
    return formData.departmentId == 1 // по ЧТЗ
}

void importData() {
    int COLUMN_COUNT = 3
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = "Сумма увеличения базы по налогу на прибыль, в том числе"
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        def rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // простая строка
        def row = templateRows[rowIndex]
        rowIndex++
        // заполнить строку нф значениями из эксель
        fillRowFromXls(row, rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(row)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()

        break
    }
    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[2].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): 'Сумма увеличения базы по налогу на прибыль, в том числе']),
            ([(headerRows[0][2]): ('Налоговое бремя, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[1][0]): ('не учитываемые расходы, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[1][1]): ('доначисление доходов, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[2][0]): '(РНУ-108 гр.13+РНУ-115 гр.20+ РНУ-116 гр.20)']),
            ([(headerRows[2][1]): '(РНУ-107 гр.12+ РНУ-110 гр.11+ РНУ-111 гр.13+ РНУ-115 гр.19+ РНУ-116 гр.19+РНУ-114 гр.16)']),
            ([(headerRows[2][3]): '(гр.1+гр.2)*20%'])
    ]
    (0..2).each {
        headerMapping.add(([(headerRows[3][it]): (it + 1).toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param row строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def row, def values, def colOffset, def fileRowIndex, def rowIndex) {
    row.setIndex(rowIndex)
    row.setImportIndex(fileRowIndex)
    // графа 1..3
    def colIndex = -1
    allColumns.each { alias ->
        colIndex++
        row[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
    return row
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