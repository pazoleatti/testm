package form_template.etr.etr_4_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Приложение 4-1. Абсолютная величина налоговых платежей
 * formTemplateId = 700
 *
 * @author bkinzyabulatov
 * TODO определение уровня банка
 *
 * графа 1 - rowNum         - № строки
 * графа 2 - taxName        - Наименование налога
 * графа 3 - symbol102      - символ формы 102
 * графа 4 - comparePeriod  - Период сравнения
 * графа 5 - currentPeriod  - Текущий отчетный период
 * графа 6 - deltaRub       - Изменение за период (гр.5-гр.4), тыс.руб.
 * графа 7 - deltaPercent   - Изменение за период (гр.6/гр.4*100),%
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
def allColumns = ['rowNum', 'taxName', 'symbol102', 'comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

@Field
def calcColumns = ['comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

@Field
def check102Columns = ['comparePeriod', 'currentPeriod']

@Field
def checkCalcColumns = ['deltaRub', 'deltaPercent']

@Field
def nonEmptyColumns = calcColumns

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = calcColumns

@Field
def opuMap = ['R3' : ['28101'],
           'R5' : ['26411.01', '26411.02'],
           'R6' : ['26411.03'],
           'R7' : ['26102' ,'26410.09'],
           'R8' : ['26411.12', '26411.13']]

@Field
def endDate = null

@Field
def prevEndDate = null

@Field
def compareEndDate = null

@Field
def prevCompareEndDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getPrevReportPeriodEndDate() {
    if (prevEndDate == null) {
        prevEndDate = reportPeriodService.getEndDate(reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)?.id).time
    }
    return prevEndDate
}

def getComparePeriodEndDate() {
    if (compareEndDate == null) {
        compareEndDate = reportPeriodService.getEndDate(formData.comparativPeriodId).time
    }
    return compareEndDate
}

def getPrevComparePeriodEndDate() {
    if (prevCompareEndDate == null) {
        prevCompareEndDate = reportPeriodService.getEndDate(reportPeriodService.getPrevReportPeriod(formData.comparativPeriodId)?.id).time
    }
    return prevCompareEndDate
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    calcValues(dataRows, dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for(int i = 0; i < dataRows.size(); i++){
        checkNonEmptyColumns(dataRows[i], dataRows[i].rowNum as Integer, nonEmptyColumns, logger, true)
    }
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def tempRows = formTemplate.rows
    calcValues(tempRows, dataRows)
    for(int i = 0; i < dataRows.size(); i++){
        def row = dataRows[i]
        def tempRow = tempRows[i]
        if (opuMap.keySet().contains(row.getAlias()) && (!"R3".equals(row.getAlias()) || isBank())) {
            check102(row, check102Columns, tempRow, logger, true)
        }
        checkCalc(row, checkCalcColumns, tempRow, logger, true)
    }
}

void calcValues(def dataRows, def sourceRows) {
    for (alias in opuMap.keySet()) {
        def row = getDataRow(dataRows, alias)
        def rowSource = getDataRow(sourceRows, alias)
        if ("R3".equals(alias) && !isBank()) {
            row.comparePeriod = rowSource.comparePeriod
            row.currentPeriod = rowSource.currentPeriod
            continue
        }
        row.comparePeriod = get102Sum(rowSource, getReportPeriodEndDate()) - (formData.accruing ? 0 : get102Sum(rowSource, getPrevReportPeriodEndDate()))
        row.currentPeriod = get102Sum(rowSource, getComparePeriodEndDate()) - (formData.accruing ? 0 : get102Sum(rowSource, getPrevComparePeriodEndDate()))
    }
    def row2 = getDataRow(dataRows, "R2")
    def row4 = getDataRow(dataRows, "R4")
    def row3Source = getDataRow(sourceRows, "R3")
    def row4Source = getDataRow(sourceRows, "R4")
    def row5Source = getDataRow(sourceRows, "R5")
    def row6Source = getDataRow(sourceRows, "R6")
    def row7Source = getDataRow(sourceRows, "R7")
    def row8Source = getDataRow(sourceRows, "R8")
    ['comparePeriod', 'currentPeriod'].each {
        row4[it] = (row5Source[it] ?: 0) + (row6Source[it] ?: 0) + (row7Source[it] ?: 0) + (row8Source[it] ?: 0)
        row2[it] = (row3Source[it] ?: 0) + (row4Source[it] ?: 0)
    }
    for(int i = 0; i < dataRows.size(); i++){
        def row = dataRows[i]
        def rowSource = sourceRows[i]
        row.deltaRub = (rowSource.currentPeriod ?: 0) - (rowSource.comparePeriod ?: 0)
        row.deltaPercent = null
        if (rowSource.comparePeriod) {
            row.deltaPercent = ((rowSource.deltaRub ?: BigDecimal.ZERO) as BigDecimal) / rowSource.comparePeriod * 100
        }
    }
}

// Возвращает данные из Формы 102 БО за период
def get102Sum(def row, def date) {
    if (opuMap[row.getAlias()] != null && date != null) {
        def opuCodes = opuMap[row.getAlias()].join("' OR OPU_CODE = '")
        // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
        def records = bookerStatementService.getRecords(52L, formData.departmentId, date, "OPU_CODE = '${opuCodes}'")
        return records?.sum { it.TOTAL_SUM.numberValue } ?: 0
    }
    return 0
}

boolean isBank() {
    return formData.departmentId == 1 // TODO
}

void check102(def row, def calcColumns, def tempRow, def logger, boolean required) {
    List<String> errorColumns = new LinkedList<String>();
    for (String alias : calcColumns) {
        if (tempRow[alias] == null && row[alias] == null) {
            continue;
        }
        if (tempRow[alias] == null || row[alias] == null
                || ((BigDecimal) tempRow.get(alias)).compareTo((BigDecimal) row.getCell(alias).getValue()) != 0) {
            errorColumns.add('«' + getColumnName(row, alias) + '»');
        }
    }
    if (!errorColumns.isEmpty()) {
        String msg = String.format("Строка %s: Значение граф «%s» не соответствует значению поля «Сумма» соответствующего символа ф. 102 за установленный период.", row.rowNum, errorColumns.join(", "));
        if (required) {
            rowError(logger, row, msg);
        } else {
            rowWarning(logger, row, msg);
        }
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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
        def alias = "R" + (rowIndex + 1) // aliasы начинаются с R2
        def dataRow = dataRows.find{ it.getAlias() == alias }
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
    checkHeaderSize(headerRows[1].size(), headerRows.size(), 7, 3)
    def headerMapping = [
            (headerRows[0][0]): '№ строки',
            (headerRows[0][1]): 'Наименование налога',
            (headerRows[0][2]): 'символ формы 102',
            (headerRows[0][3]): 'Период сравнения',
            (headerRows[0][4]): 'Текущий отчетный период',
            (headerRows[0][5]): 'Изменение за период',
            (headerRows[1][5]): '(гр.5-гр.4), тыс.руб.',
            (headerRows[1][6]): '(гр.6/гр.4*100),%'
    ]
    (0..6).each { index ->
        headerMapping.put((headerRows[2][index]), (index + 1).toString())
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
    resetColumns.each { alias ->
        dataRow[alias] = null
    }

    def rowNum = dataRow.rowNum as Integer
    def taxName = normalize(dataRow.taxName)
    def symbol102 = normalize(dataRow.symbol102) ?: ''


    def colIndex = 0
    def rowNumImport = values[colIndex] as Integer

    colIndex++
    def taxNameImport = normalize(values[colIndex])

    colIndex++
    def symbol102Import = normalize(values[colIndex]) ?: ''

    if (!(rowNum == rowNumImport && taxName == taxNameImport && symbol102 == symbol102Import)) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с номером строки = $rowNum.")
        return
    }

    // графа 4..7
    resetColumns.each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}
