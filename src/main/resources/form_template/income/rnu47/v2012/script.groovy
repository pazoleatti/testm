package form_template.income.rnu47.v2012

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-47) Регистр налогового учёта «ведомость начисленной амортизации по основным средствам,
 * а также расходов в виде капитальных вложений»"
 * formTypeId=344
 *
 * @author vsergeev
 */

// графа 1 - number                 Строка
// графа 2 - amortGroup             Амортизационные группы
// графа 3 - sumCurrentPeriodTotal  За отчётный месяц
// графа 4 - sumTaxPeriodTotal      С начала налогового периода
// графа 5 - amortPeriod            За отчётный месяц
// графа 6 - amortTaxPeriod         С начала налогового периода

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        afterCreate()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkRNU()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        break
    case FormDataEvent.DELETE_ROW:
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        checkRNU()
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
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы

// Все аттрибуты
@Field
def allColumns = ["number", "amortGroup", "sumCurrentPeriodTotal", "sumTaxPeriodTotal", "amortPeriod", "amortTaxPeriod"]

// Автозаполняемые атрибуты (графа 3..6)
@Field
def arithmeticCheckAlias = ["sumCurrentPeriodTotal", "sumTaxPeriodTotal", "amortPeriod", "amortTaxPeriod"]

@Field
def dateFormat = 'dd.MM.yyyy'

/** Признак периода ввода остатков. */
@Field
def isBalance = null

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalance == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        if (!departmentReportPeriod.isBalance() || formData.periodOrder == null) {
            isBalance = false
        } else {
            isBalance = (formData.periodOrder - 1) % 3 == 0
        }
    }
    return isBalance
}

// Получить данные из формы РНУ-46
def getRnu46DataRowHelper() {
    def formData46 = formDataService.getLast(342, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (formData46 != null) {
        return formDataService.getDataRowHelper(formData46)
    }
    return null
}

/** Расчет значений ячеек, заполняющихся автоматически */
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    if (!isMonthBalance()) {
        // расчет для первых 11 строк
        def row1_11 = calcRows1_11()

        dataRows.eachWithIndex { row, index ->
            if (index < 11) {
                row1_11[index].each { k, v ->
                    row[k] = v
                }
            }
        }
    }
    // расчет для строк 12-13
    def totalValues = getTotalValues(dataRows)
    dataRows.eachWithIndex { row, index ->
        if (index == 11 || index == 12) {
            row.sumCurrentPeriodTotal = totalValues[index].sumCurrentPeriodTotal
            row.sumTaxPeriodTotal = totalValues[index].sumTaxPeriodTotal
        }
    }
}

/** Расчет строк 1-11 */
def calcRows1_11() {
    def rnu46Rows = getRnu46DataRowHelper()?.allSaved
    def groupList = 0..10
    def value = [:]
    groupList.each { group ->
        value[group] = calc3_6(rnu46Rows, group)
    }
    return value
}

/** Расчет строк 12-13 */
def getTotalValues(def dataRows) {
    def group12 = ['R1', 'R2', 'R8', 'R9', 'R10']
    def group13 = ['R3', 'R4', 'R5', 'R6', 'R7']
    def value = [11: [:], 12: [:]]

    // расчет для строк 12-13
    dataRows.each { row ->
        if (group12.contains(row.getAlias())) {
            value[11].sumCurrentPeriodTotal = round((value[11].sumCurrentPeriodTotal ?: BigDecimal.ZERO) + (row.sumCurrentPeriodTotal ?: BigDecimal.ZERO))
            value[11].sumTaxPeriodTotal = round((value[11].sumCurrentPeriodTotal ?: BigDecimal.ZERO) + (row.sumCurrentPeriodTotal ?: BigDecimal.ZERO))
        } else if (group13.contains(row.getAlias())) {
            value[12].sumCurrentPeriodTotal = round((value[12].sumCurrentPeriodTotal ?: BigDecimal.ZERO) + (row.sumCurrentPeriodTotal ?: BigDecimal.ZERO))
            value[12].sumTaxPeriodTotal = round((value[12].sumTaxPeriodTotal ?: BigDecimal.ZERO) + (row.sumTaxPeriodTotal ?: BigDecimal.ZERO))
        }
    }
    return value
}
/** Расчет столбцов 3-6 для строк 1-11 */
def calc3_6(def rows, def group) {
    def value = [
            sumCurrentPeriodTotal: BigDecimal.ZERO,
            sumTaxPeriodTotal    : BigDecimal.ZERO,
            amortPeriod          : BigDecimal.ZERO,
            amortTaxPeriod       : BigDecimal.ZERO
    ]
    rows.each { row ->
        def amortGroup = refBookService.getNumberValue(71, row.amortGroup, 'GROUP')
        if (amortGroup != null && amortGroup == group) {
            value.sumCurrentPeriodTotal += round(row.cost10perMonth ?: BigDecimal.ZERO)
            value.sumTaxPeriodTotal += round(row.cost10perTaxPeriod ?: BigDecimal.ZERO)
            value.amortPeriod += round(row.amortMonth ?: BigDecimal.ZERO)
            value.amortTaxPeriod += round(row.amortTaxPeriod ?: BigDecimal.ZERO)
        }
    }
    return value
}

/** Логические проверки (таблица 149) */
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (formData.kind == FormDataKind.PRIMARY) {
        if (!isMonthBalance()) {
            def hasData = false
            def groupList = 0..10
            for (def row : rnu46DataRowHelper.allCached) {
                if (refBookService.getNumberValue(71, row.amortGroup, 'GROUP').intValue() in groupList) {
                    hasData = true
                    break
                }
            }
            if (!hasData) {
                logger.error("Отсутствуют данные РНУ-46!")
            }
        }

        def groupRowsAliases = ['R0', 'R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10']
        //вынес сюда проверку на первый месяц
        def formDataOld = formData.periodOrder != 1 ? formDataService.getFormDataPrev(formData) : null
        def dataRowsOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allSaved : null
        // значения для первых 11 строк
        def row1_11 = calcRows1_11()

        def startOld
        def endOld
        if (formDataOld?.periodOrder != null) {
            startOld = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formDataOld.periodOrder).time
            endOld = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formDataOld.periodOrder).time
        }
        for (def row : dataRows) {
            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            if (row.getAlias() in groupRowsAliases) {
                // Проверка на заполнение поля
                checkNonEmptyColumns(row, index, allColumns, logger, !isMonthBalance())
            } else {
                continue
            }

            if (isMonthBalance()) {
                // Оставшиеся проверки не для периода ввода остатков
                continue;
            }
            //2.		Проверка суммы расходов в виде капитальных вложений с начала года
            //2.1	графа 4 ? графа 3;
            def invalidCapitalForm = errorMsg + "Неверная сумма расходов в виде капитальных вложений с начала года!"
            if (row.sumTaxPeriodTotal != null && row.sumCurrentPeriodTotal != null) {
                if (row.sumTaxPeriodTotal < row.sumCurrentPeriodTotal) {
                    loggerError(invalidCapitalForm)
                } else
                //2.2	графа 4 = графа 3 + графа 4 за предыдущий месяц;
                // (если текущий отчетный период – январь, то слагаемое «по графе 4 за предыдущий месяц» в формуле считается равным «0.00»)
                if (row.sumTaxPeriodTotal != (row.sumCurrentPeriodTotal + getFieldFromPreviousMonth(dataRowsOld, row.getAlias(), "sumTaxPeriodTotal"))) {
                    invalidCapitalForm += " Экземпляр за период ${formatDate(startOld, dateFormat)} - ${formatDate(endOld, dateFormat)} не существует (отсутствуют первичные данные для расчёта)"
                    loggerError(invalidCapitalForm)
                } else
                //2.3	графа 4 = (сумма)графа 3 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
                if (row.sumTaxPeriodTotal != getFieldSumForAllPeriods(row.getAlias(), "sumCurrentPeriodTotal")) {
                    def periodOrderList = getFieldInvalidPeriods(row.getAlias(), "sumCurrentPeriodTotal")
                    if (!periodOrderList.isEmpty()) {
                        invalidCapitalForm += " Экземпляр за периоды "
                        periodOrderList.eachWithIndex { periodOrder, i ->
                            if (i != 0) {
                                invalidCapitalForm += ", "
                            }
                            def start = reportPeriodService.getMonthStartDate(formData.reportPeriodId, periodOrder).time
                            def end = reportPeriodService.getMonthEndDate(formData.reportPeriodId, periodOrder).time
                            invalidCapitalForm += "${formatDate(start, dateFormat)} - ${formatDate(end, dateFormat)}"
                        }
                        invalidCapitalForm += " не существует (отсутствуют первичные данные для расчёта)"
                        loggerError(invalidCapitalForm)
                    }
                }
            }

            //3.    Проверка суммы начисленной амортизации с начала года
            def invalidAmortSumms = errorMsg + "Неверная сумма начисленной амортизации с начала года!"
            //3.1.	графа 6 ? графа 5
            if (row.amortTaxPeriod != null && row.amortPeriod != null) {
                if (row.amortTaxPeriod < row.amortPeriod) {
                    loggerError(invalidAmortSumms)
                } else
                //3.2   графа 6 = графа 5 + графа 6 за предыдущий месяц;
                //  (если текущий отчетный период – январь, то слагаемое «по графе 6 за предыдущий месяц» в формуле считается равным «0.00»)
                if (row.amortTaxPeriod != (row.amortPeriod + getFieldFromPreviousMonth(dataRowsOld, row.getAlias(), "amortTaxPeriod"))) {
                    invalidAmortSumms += " Экземпляр за период ${formatDate(startOld, dateFormat)} - ${formatDate(endOld, dateFormat)} не существует (отсутствуют первичные данные для расчёта)"
                    loggerError(invalidAmortSumms)
                } else
                //3.3   графа 6 = (сумма)графа 5 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
                if (row.amortTaxPeriod != getFieldSumForAllPeriods(row.getAlias(), "amortPeriod")) {
                    def periodOrderList = getFieldInvalidPeriods(row.getAlias(), "amortPeriod")
                    if (!periodOrderList.isEmpty()) {
                        invalidAmortSumms += " Экземпляр за периоды "
                        periodOrderList.eachWithIndex { periodOrder, i ->
                            if (i != 0) {
                                invalidAmortSumms += ", "
                            }
                            def start = reportPeriodService.getMonthStartDate(formData.reportPeriodId, periodOrder).time
                            def end = reportPeriodService.getMonthEndDate(formData.reportPeriodId, periodOrder).time
                            invalidAmortSumms += "${formatDate(start, dateFormat)} - ${formatDate(end, dateFormat)}"
                        }
                        invalidAmortSumms += " не существует (отсутствуют первичные данные для расчёта)"
                        loggerError(invalidAmortSumms)
                    }
                }
            }

            if (--index < 11) {
                row1_11[index].each { k, v ->
                    row[k] = v
                }
            }
        }
    }

    def totalValues = getTotalValues(dataRows)
    for (row in dataRows) {
        def index = dataRows.indexOf(row)
        def errorMsg = "Строка ${index + 1}: "
        if (index == 11 || index == 12) {
            for (def col in ['sumCurrentPeriodTotal', 'sumTaxPeriodTotal']) {
                if (row[col] != totalValues[index][col]) {
                    loggerError(errorMsg + WRONG_TOTAL, getColumnName(row, col))
                }
            }
        }
    }
}

/** Получить данные за определенный месяц */
def FormData getFormDataPeriod(def reportPeriodId, def periodOrder) {
    if (reportPeriodId != null && periodOrder != null) {
        return formDataService.getLast(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodId, periodOrder, formData.comparativePeriodId, formData.accruing)
    }
}

/** Возвращает значение графы 4 или 6 за предыдущий месяц */
def getFieldFromPreviousMonth(def dataRows, def alias, def field) {
    if (dataRows != null) {
        def row = getDataRow(dataRows, alias)
        if (row != null) {
            return row[field] ?: BigDecimal.ZERO
        }
    }
    return BigDecimal.ZERO
}

/** Возвращает сумму значений графы (3 или 5) за все месяцы текущего года, включая текущий отчетный период */
def getFieldSumForAllPeriods(def alias, def field) {
    def sum = 0
    for (def periodOrder = 1; periodOrder <= formData.periodOrder; periodOrder++) {
        def formDataPeriod = getFormDataPeriod(formData.reportPeriodId, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allSaved : null
        def DataRow row = dataRows != null ? getDataRow(dataRows, alias) : null
        def value = row?.getCell(field)?.getValue()
        if (value != null) {
            sum += value
        }
    }
    return sum
}

/** Возвращает периоды с некорректными данными для расчета графы 4 или 6. field - графа 3 или 5*/
def getFieldInvalidPeriods(def alias, def field) {
    def periods = []
    for (def periodOrder = 1; periodOrder <= formData.periodOrder; periodOrder++) {
        def formDataPeriod = getFormDataPeriod(formData.reportPeriodId, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allSaved : null
        def DataRow row = dataRows != null ? getDataRow(dataRows, alias) : null
        if (row?.getCell(field)?.getValue() == null) {
            periods += periodOrder
        }
    }
    return periods
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // удалить все строки и собрать из источников их строки
    dataRows.each { row ->
        arithmeticCheckAlias.each { column ->
            row[column] = null
        }
    }
    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.getLast(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = formDataService.getDataRowHelper(source)
                addRowsToRows(dataRows, sourceForm.allCached)
            }
        }
    }
}

void addRowsToRows(def rows, def addRows) {
    rows.each { row ->
        def addRow = null
        for (def dataRow : addRows) {
            if (row.getAlias() == dataRow.getAlias()) {
                addRow = dataRow
                break
            }
        }
        arithmeticCheckAlias.each { column ->
            def value = row[column]
            row[column] = (value == null) ? addRow[column] : (value + (addRow[column] ?: BigDecimal.ZERO))
        }
    }
}

BigDecimal round(BigDecimal value, int newScale = 2) {
    return value?.setScale(newScale, RoundingMode.HALF_UP)
}

def loggerError(def msg, Object... args) {
    if (isMonthBalance()) {
        logger.warn(msg, args)
    } else {
        logger.error(msg, args)
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 6, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def int rnuIndexRow = 2
    def int colOffset = 1
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (int i = 0; i < 17; i++) {
        rnuIndexRow++

        def row = xml.row[i]

        // графа 3
        def xmlIndexCol = 3
        dataRows[i].sumCurrentPeriodTotal = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 4
        xmlIndexCol = 4
        dataRows[i].sumTaxPeriodTotal = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 5
        xmlIndexCol = 5
        dataRows[i].amortPeriod = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 6
        dataRows[i].amortTaxPeriod = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol)
    }
}

void checkRNU() {
    if (!isMonthBalance() && formData.kind == FormDataKind.PRIMARY) {
        // проверить рну-47 за прошлый период
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true, formData.comparativePeriodId, formData.accruing)

        // проверить рну-46 за текущий период
        formDataService.checkMonthlyFormExistAndAccepted(342, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, false, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

def afterCreate() {
    // для периода ввода остатков сделать редактируемыми ячейки, в которых могут быть данные.
    if (isMonthBalance()) {
        def dataRows = formDataService.getDataRowHelper(formData).allCached
        for (def row : dataRows) {
            def columns = arithmeticCheckAlias
            def isTotal = row.number == 12 || row.number == 13
            if (isTotal) {
                columns = ['sumCurrentPeriodTotal', 'sumTaxPeriodTotal']
            } else if (row.number > 13) {
                columns = ['amortTaxPeriod']
            }
            columns.each {
                row.getCell(it).editable = true
                if (!isTotal) {
                    row.getCell(it).styleAlias = 'Редактируемая'
                }
            }
        }
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 6
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

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
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

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
        def templateRow = getDataRow(templateRows, dataRow.getAlias())
        // заполнить строку нф значениями из эксель
        fillRowFromXls(templateRow, dataRow, rowValues, fileRowIndex, rowIndex, colOffset)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    showMessages(dataRows, logger)
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
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): tmpRow.getCell('number').column.name]),
            ([(headerRows[0][1]): tmpRow.getCell('amortGroup').column.name]),
            ([(headerRows[0][2]): 'Сумма расходов в виде капитальных вложений, предусмотренных п. 9 ст. 258 НК РФ']),
            ([(headerRows[1][2]): 'За отчётный месяц']),
            ([(headerRows[1][3]): 'С начала налогового периода']),
            ([(headerRows[0][4]): 'Сумма начисленной амортизации']),
            ([(headerRows[1][4]): 'За отчётный месяц']),
            ([(headerRows[1][5]): 'С начала налогового периода'])
    ]
    (1..6).each { index ->
        headerMapping.add(([(headerRows[2][it - 1]): it.toString()]))
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
void fillRowFromXls(def templateRow, def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)
    def tmpValues = formData.createStoreMessagingDataRow()

    // графа 1
    def colIndex = 0
    tmpValues.number = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2
    colIndex++
    tmpValues.amortGroup = values[colIndex]

    // Проверить фиксированные значения
    tmpValues.keySet().toArray().each { alias ->
        def value = tmpValues[alias]?.toString()
        def valueExpected = StringUtils.cleanString(templateRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 3..6
    ['sumCurrentPeriodTotal', 'sumTaxPeriodTotal', 'amortPeriod', 'amortTaxPeriod'].each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}