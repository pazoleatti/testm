package form_template.income.resident_taxpayers.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Расчет налога и облагаемой суммы дивидендов по акциям налогоплательщиков-резидентов
 * formTemplateId = 319
 *
 * @author bkinzyabulatov
 *
 * графа  1 - shareKind
 * графа  2 - shareHolder
 * графа  3 - shareCount
 * графа  4 - shareProfit
 * графа  5 - d1
 * графа  6 - d2
 * графа  7 - taxRate
 * графа  8 - kIndex
 * графа  9 - taxPerShare
 * графа 10 - dividendPerShareIndividual
 * графа 11 - dividendPerShareLegal
 */

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
}

@Field
def allColumns = ['shareKind', 'shareHolder', 'shareCount', 'shareProfit', 'd1', 'd2', 'taxRate', 'kIndex', 'taxPerShare', 'dividendPerShareIndividual', 'dividendPerShareLegal']

@Field
def nonEmptyColumns = ['shareKind', 'shareHolder', 'shareCount', 'shareProfit', 'd1', 'd2', 'taxRate', 'kIndex', 'taxPerShare']

@Field
def checkCalcColumns = ['kIndex', 'taxPerShare', 'dividendPerShareIndividual', 'dividendPerShareLegal']

@Field
String zeroString = "0.0000000000000000"

// Признак периода ввода остатков
@Field
def isBalancePeriod

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    calcValues(dataRows, dataRows)
}

void calcValues(def dataRows, def sourceRows) {
    for (int i = 0; i < dataRows.size(); i++) {
        def row = dataRows[i]
        def rowSource = sourceRows[i]
        // графа 3
        if (rowSource.shareCount == null) {
            row.shareCount = 1
        } else {
            row.shareCount = rowSource.shareCount
        }
        // графа 4-7 заполняется вручную
        // «Графа 8» = ОКРУГЛ («Графа 4»  / «Графа 5»; 16)
        if (rowSource.d1) {
            if (rowSource.shareProfit != null) {
                row.kIndex = rowSource.shareProfit.divide(rowSource.d1, 16, BigDecimal.ROUND_HALF_UP).toPlainString()
            }
        } else {
            row.kIndex = zeroString
        }
        // «Графа 9» = ОКРУГЛ («Графа 8» * «Графа 7» / 100 * («Графа 5» - «Графа 6»); 16)
        if (rowSource.kIndex && rowSource.taxRate != null && rowSource.d1 != null && rowSource.d2 != null) {
            row.taxPerShare = (new BigDecimal(rowSource.kIndex) * rowSource.taxRate * (rowSource.d1 - rowSource.d2)).divide(new BigDecimal(100), 16, BigDecimal.ROUND_HALF_UP).toPlainString()
        }
        // «Графа 10» = ОКРУГЛ («Графа 3» * «Графа 9» / («Графа 7» / 100); 16) для строк 1,2
        if ([0, 1].contains(i)) {
            if (rowSource.taxRate) {
                if (rowSource.shareCount && rowSource.taxPerShare) {
                    row.dividendPerShareIndividual = (new BigDecimal(rowSource.shareCount) * new BigDecimal(rowSource.taxPerShare) * 100).divide(rowSource.taxRate, 16, BigDecimal.ROUND_HALF_UP).toPlainString()
                }
            } else {
                row.dividendPerShareIndividual = zeroString
            }
        } else {
            row.dividendPerShareIndividual = null
        }
        // «Графа 11» = ОКРУГЛ («Графа 8» * («Графа 5» - «Графа 6»); 16) для строк 3,4
        if ([2, 3].contains(i)) {
            if (rowSource.kIndex && rowSource.d1 != null && rowSource.d2 != null) {
                row.dividendPerShareLegal = (new BigDecimal(rowSource.kIndex) * (rowSource.d1 - rowSource.d2)).setScale(16, BigDecimal.ROUND_HALF_UP).toPlainString()
            }
        } else {
            row.dividendPerShareLegal = null
        }
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    for (int i = 0; i < dataRows.size(); i++) {
        def row = dataRows[i]
        def columns = nonEmptyColumns
        if ([0, 1].contains(i)) {
            columns += 'dividendPerShareIndividual'
        }
        if ([2, 3].contains(i)) {
            columns += 'dividendPerShareLegal'
        }
        // 1. Обязательность заполнения поля графы 1..9
        checkNonEmptyColumns(row, i + 1, columns, logger, !isBalancePeriod())
    }

    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def tempRows = formTemplate.rows
    updateIndexes(tempRows)
    calcValues(tempRows, dataRows)
    for(int i = 0; i < dataRows.size(); i++){
        def row = dataRows[i]
        def tempRow = tempRows[i]
        checkCalc(row, checkCalcColumns, tempRow)
    }
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

def checkCalc(def row, List<String> calcColumns, def calcValues) {
    def errorColumns = []
    for (String alias in calcColumns) {
        if (calcValues.get(alias) == null && row[alias] == null) {
            continue
        }
        if (calcValues.get(alias) == null || row.getCell(alias).getValue() == null
                || (calcValues[alias] != row[alias])) {
            errorColumns.add('«' + getColumnName(row, alias) + '»')
        }
    }
    if (!errorColumns.isEmpty()) {
        String msg = String.format(WRONG_CALC, row.getIndex(), errorColumns.join(", "))
        rowError(logger, row, msg);
    }
}

def importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 11
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'shareKind')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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
    def allValuesCount = allValues.size()

    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    // формирование строк нф
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
        // прервать по загрузке нужных строк
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def dataRow = dataRows.get(rowIndex - 1)
        def templateRow = getDataRow(templateRows, dataRow.getAlias())
        // заполнить строку нф значениями из эксель
        fillRowFromXls(templateRow, dataRow, rowValues, fileRowIndex, rowIndex, colOffset)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }
    showMessages(dataRows, logger)
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 */
void checkHeaderXls(def headerRows, def colCount, def rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): getColumnName(tmpRow, 'shareKind')]),
            ([(headerRows[0][1]): getColumnName(tmpRow, 'shareHolder')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'shareCount')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'shareProfit')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'd1')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'd2')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'taxRate')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'kIndex')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'taxPerShare')]),
            ([(headerRows[0][9]): 'Облагаемая сумма дивидендов, руб.']),
            ([(headerRows[1][9]): 'на 1 акцию физических лиц (гр.10=гр.3*гр.9/(гр.7/100)']),
            ([(headerRows[1][10]): 'на 1 акцию юридических лиц (гр.11=гр.8*(гр.5-гр.6))'])
    ]
    (0..10).each { index ->
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param templateRow строка макета
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def templateRow, def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)
    def tmpValues = [:]

    // графа 1
    def colIndex = 0
    tmpValues.shareKind = values[colIndex]

    // графа 2
    colIndex++
    tmpValues.shareHolder = values[colIndex]

    // проверяем пустые ячейки
    // графа 10
    if ([3,4].contains(dataRow.getIndex())) {
        tmpValues.dividendPerShareIndividual = values[9]
    }

    // графа 11
    if ([1,2].contains(dataRow.getIndex())) {
        tmpValues.dividendPerShareLegal = values[10]
    }

    // Проверить фиксированные значения
    tmpValues.keySet().toArray().each { alias ->
        def value = tmpValues[alias]?.toString()
        def valueExpected = StringUtils.cleanString(templateRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 3..7
    ['shareCount', 'shareProfit', 'd1', 'd2', 'taxRate'].each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 8..11
    ['kIndex', 'taxPerShare', 'dividendPerShareIndividual', 'dividendPerShareLegal'].each { alias ->
        colIndex++
        dataRow[alias] = null
    }
}