package form_template.etr.etr_4_11.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Приложение 4-11. Статистика доначислений
 * formTemplateId = 711
 *
 * @author Stanislav Yasinskiy
 *
 * графа 1 - rowNum       - № п/п
 * графа 2 - name         - Наименование сделки
 * графа 3 - sum1         - Сумма фактического дохода/расхода по нерыночным сделкам, тыс. руб. (налоговый учет)
 * графа 4 - sum2         - Сумма доначислений до рыночного уровня, тыс. руб.
 * графа 5 - level        - Уровень доначислений/ не учитываемых расходов (в % от факта)
 * графа 6 - taxBurden    - Налоговое бремя, тыс. руб.
 */

switch (formDataEvent) {
    case FormDataEvent.GET_HEADERS:
        headers.get(0).sum1 = 'Сумма фактического дохода/расхода по нерыночным сделкам, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.') + ' (налоговый учет)'
        headers.get(0).sum2 = 'Сумма доначислений до рыночного уровня, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
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
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
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

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'sum1', 'sum2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['level', 'taxBurden']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'sum1', 'sum2', 'level', 'taxBurden']

@Field
def calcColumns = ['level', 'taxBurden']

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
        row.level = calc5(row)
        row.taxBurden = calc6(row)
    }
}

def BigDecimal calc5(def row) {
    if (row.sum2 != null && row.sum1 != null && row.sum1 != 0) {
        return (row.sum2 * 100 as BigDecimal).divide(row.sum1, 2, BigDecimal.ROUND_HALF_UP)
    } else if (row.sum1 == BigDecimal.ZERO) {
        return BigDecimal.ZERO
    }
    return null
}

def BigDecimal calc6(def row) {
    if (row.sum2 != null) {
        return row.sum2 * 0.2
    }
    return null
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        def rowNum = row.getIndex()
        // Проверка заполнения ячеек
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка заполнения граф 5,6
        def needValue = formData.createDataRow()
        needValue['level'] = calc5(row)
        needValue['taxBurden'] = calc6(row)
        checkCalc(row, calcColumns, needValue, logger, true)

        // Проверка графы 3 при расчете графы 5
        if (row.sum1 == null || row.sum1 == 0) {
            rowWarning(logger, row, String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                    row.getIndex(), getColumnName(row, 'level')))
        }
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    dataRows = []
    def sourceFormTypeId = 711
    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId)).each {
        if (it.formTypeId == sourceFormTypeId) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                for (sourceRow in sourceRows) {
                    def dataRow = dataRows.find { itRow -> itRow.name == sourceRow.name }
                    if (dataRow) {
                        dataRow.sum1 = (dataRow.sum1 ?: 0) + ((source.departmentId == 1) ? 1000 : 1) * (sourceRow.sum1 ?: 0)
                        dataRow.sum2 = (dataRow.sum2 ?: 0) + ((source.departmentId == 1) ? 1000 : 1) * (sourceRow.sum2 ?: 0)
                    } else {
                        def newRow = formData.createDataRow()
                        newRow.name = sourceRow.name
                        ['sum1', 'sum2'].each { column ->
                            newRow[column] = ((source.departmentId == 1) ? 1000 : 1) * sourceRow[column]
                        }
                        dataRows.add(newRow)
                    }
                }
            }
        }
    }
    if (isBank()) { // если уровень банка, то тысячи понижаем до миллионов
        dataRows.each { row ->
            ['sum1', 'sum2'].each { column ->
                if (row[column]) {
                    row[column] = (row[column] as BigDecimal).divide(BigDecimal.valueOf(1000), BigDecimal.ROUND_HALF_UP)
                }
            }
        }
    }
    // сортируем по наименованию сделки
    dataRows.sort { it.name }
    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

boolean isBank() {
    return formData.departmentId == 1 // по ЧТЗ
}

void importData() {
    def tmpRow = formData.createDataRow()
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, tmpRow)
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
 */
void checkHeaderXls(def headerRows, def tmpRow) {
    checkHeaderSize(headerRows, 6, 3)

    def headerMapping = [
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),//'№ п/п']),
            ([(headerRows[0][1]): getColumnName(tmpRow, 'name')]),//'Наименование сделки']),
            ([(headerRows[0][2]): ('Сумма фактического дохода/расхода по нерыночным сделкам, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.') + ' (налоговый учет)')]),//'Сумма фактического дохода/расхода по нерыночным сделкам, тыс. руб. (налоговый учет)']),
            ([(headerRows[0][3]): ('Сумма доначислений до рыночного уровня, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),//'Сумма доначислений до рыночного уровня, тыс. руб.']),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'level')]),//'Уровень доначислений/ не учитываемых расходов (в % от факта)']),
            ([(headerRows[0][5]): ('Налоговое бремя, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),//'Налоговое бремя, тыс. руб.']),
            ([(headerRows[1][2]): 'данные из РНУ по соответствующему виду операций/сделок/продуктов']),
            ([(headerRows[1][3]): 'данные из РНУ по соответствующему виду операций/сделок/продуктов']),
            ([(headerRows[1][4]): '(гр.4/гр.3)*100']),
            ([(headerRows[1][5]): 'гр.4*20%'])
    ]
    (0..5).each {
        headerMapping.add(([(headerRows[2][it]): (it + 1).toString()]))
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

    def colIndex = 0

    colIndex++
    newRow.name = values[colIndex]

    // графы 3..6
    ['sum1', 'sum2', 'level', 'taxBurden'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

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