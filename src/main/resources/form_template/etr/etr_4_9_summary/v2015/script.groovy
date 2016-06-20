package form_template.etr.etr_4_9_summary.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Приложение 4-9. Налоговая эффективность по уступке прав требования по проблемным активам в разрезе ТБ (сводная)
 *
 * formTemplateId = 7090
 *
 * @author Stanislav Yasinskiy
 *
 * графа   - fix
 * графа 1 - rowNum       - № п/п
 * графа 2 - department	  - Подразделение Банка
 * графа 3 - sumBU        - БУ, тыс. руб
 * графа 4 - sumNUD       - НУд, тыс. руб
 * графа 5 - sumNUP       - НУп, тыс. руб
 * графа 6 - taxBurden    - Налоговое бремя, тыс. руб.
 */

switch (formDataEvent) {
    case FormDataEvent.GET_HEADERS:
        headers.get(0).sumBU = 'БУ, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(0).sumNUD = 'НУд, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(0).sumNUP = 'НУп, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(0).taxBurden = 'Налоговое бремя, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        break
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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
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

@Field
def providerCache = [:]
@Field
def recordCache = [:]

@Field
def allColumns = ['department', 'sumBU', 'sumNUD', 'sumNUP', 'taxBurden']

// Проверяемые на пустые значения атрибуты в итоговой строке
@Field
def nonEmptyColumnsTotal = ['sumBU', 'sumNUD', 'sumNUP', 'taxBurden']

// итоговые графы (графа 3, 4, 5, 6)
@Field
def totalColumns = ['sumBU', 'sumNUD', 'sumNUP', 'taxBurden']

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

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getEndDate(formData.reportPeriodId), rowIndex, colIndex, logger, required)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // итоги
    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // итоги тоже проверяем
        def columns = (row.getAlias() == null) ? allColumns : nonEmptyColumnsTotal
        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def totalRow = getDataRow(dataRows, 'total')
    dataRows = []
    def sourceFormTypeId = 708
    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId)).each {
        if (it.formTypeId == sourceFormTypeId) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                def row1 = getDataRow(sourceRows, 'R1')
                def row2 = getDataRow(sourceRows, 'R2')
                def row3 = getDataRow(sourceRows, 'R3')

                def newRow = formData.createDataRow()
                newRow.department = source.departmentId
                newRow.sumBU = ((source.departmentId == 1) ? 1000 : 1) * (row1.currentPeriod ?: 0)
                newRow.sumNUD = ((source.departmentId == 1) ? 1000 : 1) * (row2.currentPeriod ?: 0)
                newRow.sumNUP = ((source.departmentId == 1) ? 1000 : 1) * (row3.currentPeriod ?: 0)
                if (isBank()) { // если уровень банка, то тысячи понижаем до миллионов
                    ['sumBU', 'sumNUD', 'sumNUP'].each { column ->
                        if (newRow[column]) {
                            newRow[column] = (newRow[column] as BigDecimal).divide(BigDecimal.valueOf(1000), BigDecimal.ROUND_HALF_UP)
                        }
                    }
                }
                // Графа 6 =(значение Графы 3 – (значение Графы 4 + значение Графы 5))*0.2
                newRow.taxBurden = (newRow.sumBU - (newRow.sumNUD + newRow.sumNUP)) * 0.2
                dataRows.add(newRow)
            }
        }
    }
    // сортируем по наименованию подразделения
    dataRows.sort { getDepartmentName(it.department as Integer) }
    dataRows.add(totalRow)
    updateIndexes(dataRows)

    formDataService.getDataRowHelper(formData).allCached = dataRows
}

@Field
def departmentNameMap = [:]

def getDepartmentName(Integer id) {
    if (id != null && departmentNameMap[id] == null) {
        departmentNameMap[id] = departmentService.get(id).name
    }
    return departmentNameMap[id]
}

boolean isBank() {
    return formData.departmentId == 1 // по ЧТЗ
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 7
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
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
    def totalRowFromFile = null

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
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "В целом по Банку") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, true)
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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    // итоговая строка
    def totalRow = getDataRow(templateRows, 'total')
    rows.add(totalRow)
    updateIndexes(rows)
    // сравнение итогов
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
            ([(headerRows[0][0]): tmpRow.getCell('rowNum').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('department').column.name]),
            ([(headerRows[0][3]): ('БУ, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[0][4]): ('НУд, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[0][5]): ('НУп, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[0][6]): ('Налоговое бремя, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[1][3]): 'симв. ф.102 (26307.02+22204)']),
            ([(headerRows[1][4]): 'КНУ 21490']),
            ([(headerRows[1][5]): 'КНУ 21510']),
            ([(headerRows[1][6]): '(гр.3-(гр.4+гр.5))*20%'])
    ]
    (2..6).each {
        headerMapping.add(([(headerRows[2][it]): it.toString()]))
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
 * @param isTotal признак того что строка итоговая
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2
    colIndex = 2
    if (!isTotal) {
        newRow.department = getRecordIdImport(30, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    }

    // графа 3..5
    ['sumBU', 'sumNUD', 'sumNUP', 'taxBurden'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
    return newRow
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')
    sortRows(refBookService, logger, dataRows, null, totalRow, null)
    dataRowHelper.saveSort()
}