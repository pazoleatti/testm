package form_template.etr.etr_4_15_summary.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Приложение 4-15. Анализ структуры налога на добавленную стоимость (НДС) (сводная).
 * formTemplateId = 7150
 */

// графа    - fix
// графа 1  - rowNum	            № п/п
// графа 2  - department	        Подразделение Банка
// графа 3  - comparePeriod			Период сравнения. НДС всего, тыс. руб.
// графа 4  - comparePeriodIgnore	Период сравнения. В том числе НДС не учитываемый, тыс. руб.
// графа 5  - comparePeriodPercent	Период сравнения. Доля НДС не учитываемый, %
// графа 6  - currentPeriod			Период. НДС всего, тыс. руб.
// графа 7  - currentPeriodIgnore	Период. В том числе НДС не учитываемый, тыс. руб.
// графа 8  - currentPeriodPercent	Период. Доля НДС не учитываемый, %
// графа 9  - delta					Изменения за период. НДС всего, тыс. руб.
// графа 10 - deltaIgnore			Изменения за период. В том числе НДС не учитываемый, тыс. руб.
// графа 11 - deltaPercent			Изменения за период. Доля НДС не учитываемый, %

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
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

@Field
def providerCache = [:]
@Field
def recordCache = [:]

@Field
def allColumns = ['department', 'comparePeriod', 'comparePeriodIgnore', 'comparePeriodPercent', 'currentPeriod',
        'currentPeriodIgnore', 'currentPeriodPercent', 'delta', 'deltaIgnore', 'deltaPercent']

// общие графы у сводной и первичной/консолидированой 4-15 (графа 2..10)
@Field
def commonColumns = ['comparePeriod', 'comparePeriodIgnore', 'comparePeriodPercent', 'currentPeriod',
        'currentPeriodIgnore', 'currentPeriodPercent', 'delta', 'deltaIgnore', 'deltaPercent']

@Field
def nonEmptyColumns = allColumns

// итоговые графы (графа 3, 4, 6, 7, 9, 10)
@Field
def totalColumns = ['comparePeriod', 'comparePeriodIgnore', 'currentPeriod', 'currentPeriodIgnore', 'delta', 'deltaIgnore']

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
    calcValues(dataRows, dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // 1. Проверка заполнения ячеек
        def columns = (row.getAlias() ? commonColumns : nonEmptyColumns)
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def tempRows = formTemplate.rows
    updateIndexes(tempRows)
    // 2. Проверка графы 3 при расчете графы 5
    // 3. Проверка графы 5 при расчете графы 8
    calcValues(tempRows, dataRows)
}

/**
 * Посчитать значения.
 *
 * @param resultRows строки в которые запишутся результаты
 * @param sourceRows строки, данные которых будут использоваться в расчетах
 */
void calcValues(def resultRows, def sourceRows) {
    def resultRow = getDataRow(resultRows, 'total')
    def sourceRow = getDataRow(sourceRows, 'total')

    // признак показывать ли сообщение логической проверки 3
    def needShowMsg = resultRows != sourceRows

    // графа 3, 4, 6, 7, 9, 10
    calcTotalSum(sourceRows, resultRow, totalColumns)

    // графа 5
    resultRow.comparePeriodPercent = calc5(sourceRow, needShowMsg)
    // графа 8
    resultRow.currentPeriodPercent = calc8(sourceRow, needShowMsg)
    // графа 11
    resultRow.deltaPercent = calc11(resultRow)
}

def calc5(def row, def needShowMsg) {
    return calc5or8(row, 'comparePeriodIgnore', 'comparePeriod', needShowMsg, 'comparePeriodPercent')
}

def calc8(def row, def needShowMsg) {
    return calc5or8(row, 'currentPeriodIgnore', 'currentPeriod', needShowMsg, 'currentPeriodPercent')
}

/**
 * Расчитать значение для графы.
 *
 * @param row строка для расчета
 * @param dividendAlias алиас графы числителя
 * @param dividerAlias алиас графы знаменателя
 * @param needShowMsg выводить ли сообщение логической проверки 3
 * @param resultAlias алиас графы для которой производится расчет (нужен для вывода сообщения)
 */
def calc5or8(def row, def dividendAlias, def dividerAlias, def needShowMsg, def resultAlias) {
    def result = 0
    def dividend = (row[dividendAlias] ?: 0)
    def divider = row[dividerAlias]
    // проверка делителя на 0 или null
    if (divider) {
        // расчет
        result = dividend * 100 / divider
    } else if (needShowMsg) {
        // Логическая проверка 2. Проверка графы 3 при расчете графы 5
        // Логическая проверка 3. Проверка графы 6 при расчете графы 8
        def msg = String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                row.getIndex(), getColumnName(row, resultAlias))
        rowWarning(logger, row, msg)
    }
    return result
}

def calc11(def row) {
    if (row.currentPeriodPercent == null || row.comparePeriodPercent == null) {
        return null
    }
    return row.currentPeriodPercent - row.comparePeriodPercent
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def totalRow = getDataRow(dataRows, 'total')
    dataRows = []
    def sourceFormTypeId = 715
    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId)).each {
        if (it.formTypeId == sourceFormTypeId) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                def row = getDataRow(sourceRows, 'R1')
                def newRow = formData.createDataRow()
                // графа 2
                newRow.department = it.departmentId
                // графа 3..11
                commonColumns.each { column ->
                    newRow[column] = row[column]
                }
                dataRows.add(newRow)
            }
        }
    }
    // сортируем по наименованию подразделения
    dataRows.sort { departmentService.get(it.department as Integer).name }
    dataRows.add(totalRow)
    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

void importData() {
    int COLUMN_COUNT = 11
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = '№ п/п'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

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

    if (totalRowFromFile) {
        commonColumns.each {
            totalRow[it] = totalRowFromFile[it]
        }
        // подсчет итогов
        totalRowFromFile.setAlias('total')
        calcValues([totalRowFromFile], rows)
        // сравнение итогов
        compareTotalValues(totalRow, totalRowFromFile, commonColumns, logger, false);
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
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            (headerRows[0][0]) : '№ п/п',
            (headerRows[0][1]) : 'Подразделение Банка',
            (headerRows[0][2]) : 'Период сравнения',
            (headerRows[0][5]) : 'Период',
            (headerRows[0][8]) : 'Изменения за период',
            (headerRows[1][2]) : 'НДС всего, тыс. руб.',
            (headerRows[1][3]) : 'В том числе НДС не учитываемый, тыс. руб.',
            (headerRows[1][4]) : 'Доля НДС не учитываемый, %',
            (headerRows[1][5]) : 'НДС всего, тыс. руб.',
            (headerRows[1][6]) : 'В том числе НДС не учитываемый, тыс. руб.',
            (headerRows[1][7]) : 'Доля НДС не учитываемый, %',
            (headerRows[1][8]) : 'НДС всего, тыс. руб.',
            (headerRows[1][9]) : 'В том числе НДС не учитываемый, тыс. руб.',
            (headerRows[1][10]): 'Доля НДС не учитываемый, %',
            (headerRows[2][2]) : 'символ формы 102 (26411.01+26411.02+26411.11+27203.01+27203.02)',
            (headerRows[2][3]) : 'символ формы 102 (26411.02+26411.11+27203.02)',
            (headerRows[2][4]) : '(гр.4/гр.3)*100',
            (headerRows[2][5]) : 'символ формы 102 (26411.01+26411.02+26411.11+27203.01+27203.02)',
            (headerRows[2][6]) : 'символ формы 102 (26411.02+26411.11+27203.02)',
            (headerRows[2][7]) : '(гр.7/гр.6)*100',
            (headerRows[2][8]) : 'гр.6-гр.3',
            (headerRows[2][9]) : 'гр.7-гр.4',
            (headerRows[2][10]): 'гр.8-гр.5'
    ]
    (2..11).each { index ->
        headerMapping.put((headerRows[3][index-1]), index.toString())
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
    // графа 1..10
    def colIndex = -1
    allColumns.each { alias ->
        colIndex++
        row[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
    return row
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
    def colIndex = 1
    if (!isTotal) {
        newRow.department = getRecordIdImport(30, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    }

    // графа 3..11
    colIndex = 1
    commonColumns.each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
    return newRow
}