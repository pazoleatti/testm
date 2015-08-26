package form_template.etr.etr_4_16.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Приложение 4-16. Доходы и расходы, не учитываемые для целей налогообложения по налогу на прибыль, и их влияние на финансовый результат
 * formTemplateId = 716
 *
 * @author bkinzyabulatov
 *
 * графа 1 - rowNum         - № строки
 * графа 2 - indicatorName  - Наименование показателя
 * графа 3 - comparePeriod  - Период сравнения
 * графа 4 - currentPeriod  - Текущий отчетный период
 * графа 5 - deltaRub       - Изменение за период (гр.4-гр.3), тыс.руб.
 * графа 6 - deltaPercent   - Изменение за период (гр.5/гр.3*100),%
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        preCalcCheck()
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
def allColumns = ['rowNum', 'indicatorName', 'comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

@Field
def calcColumns = ['comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

@Field
def check102Columns = ['comparePeriod', 'currentPeriod']

@Field
def checkCalcColumns = ['deltaRub', 'deltaPercent']

@Field
def nonEmptyColumns = calcColumns

// мапа с кодами ОПУ для каждой строки (алиас строки -> список кодов ОПУ)
@Field
def opuMap = [
        'R1' : ['16305.02', '17201.97', '17202.07', '17202.08', '17202.09', '17202.97', '17202.99', '17203.02', '17203.03', '17203.06', '17203.09', '17203.11', '17203.13', '17203.14', '17203.97', '17306.19', '17306.20', '17306.99', '17307'],
        'R2': [
                //1
                '26104.04', '26101.07', '26101.12', '26101.13', '26101.99', '26401.04', '27203.08',
                //2
                '26101.01', '26104.02', '26104.03', '26104.04', '26104.05', '26104.06', '26104.99', '27308.15', '27308.16', '27308.17', '27308.18',
                //3
                '26410.04',
                //4
                '27308.19', '27308.20', '27308.21',
                //5
                '26301.06', '26301.08', '26301.10', '26301.12', '26301.14', '26301.16', '26302.02', '26302.04', '26302.06', '26302.08', '26302.10', '26302.12', '26302.14', '26302.16', '26302.18', '26302.20', '26305.02', '26305.05', '26305.07', '26305.09', '26305.11', '26305.13', '27203.20', '27203.22', '27203.24', '27203.26', '27203.28', '27203.30', '27203.32', '27203.34', '27203.46',
                //6
                '26303.02', '27203.36',
                //7
                '26402.02',
                //8
                '26403.04', '26406.06', '26406.07', '26406.08', '26406.09', '26406.10', '26406.11', '26406.13',
                //9
                '26405.02',
                //10
                '26411.02', '26411.05', '26411.06', '26411.09', '26411.10', '26411.11', '27203.02', '27203.05',
                //11
                '26412.03',
                //12
                '26412.09', '26412.22', '26412.24',
                //13
                '26412.12',
                //14
                '26412.14',
                //15
                '26412.99', '27203.13',
                //16
                '27301',
                //17
                '27203.09', '27203.11',
                //18
                '27103', '27203.15',
                //19
                '25203.03', '25302.02', '26401.02', '26403.02', '26410.06', '26410.99', '26412.10', '26412.27', '27201.97', '27202.99', '27203.38', '27203.40', '27203.42', '27203.44', '27203.97', '27203.99', '27302', '27303', '27304', '27307', '27308.99',
                //20
                '27305', '27306', '27308.24'
        ]
]

@Field
def startDateMap = [:]

def getStartDate(int reportPeriodId) {
    if (startDateMap[reportPeriodId] == null) {
        startDateMap[reportPeriodId] = reportPeriodService.getStartDate(reportPeriodId)?.time
    }
    return startDateMap[reportPeriodId]
}

@Field
def endDateMap = [:]

def getEndDate(int reportPeriodId) {
    if (endDateMap[reportPeriodId] == null) {
        endDateMap[reportPeriodId] = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return endDateMap[reportPeriodId]
}

@Field
def periodMap = [:]

def getReportPeriod(int reportPeriodId) {
    if (periodMap[reportPeriodId] == null) {
        periodMap[reportPeriodId] = reportPeriodService.get(reportPeriodId)
    }
    return periodMap[reportPeriodId]
}

@Field
def prevPeriodMap = [:]

def getPrevReportPeriod(int reportPeriodId) {
    if (prevPeriodMap[reportPeriodId] == null) {
        prevPeriodMap[reportPeriodId] = reportPeriodService.getPrevReportPeriod(reportPeriodId)
    }
    return prevPeriodMap[reportPeriodId]
}

void preCalcCheck() {
    def tmpRow = formData.createDataRow()
    // собираем коды ОПУ
    def final opuCodes = opuMap.values().sum()
    // находим записи для текущего периода и периода сравнения
    ['comparePeriod': getComparativePeriodId(), 'currentPeriod':formData.reportPeriodId].each { key, value ->
        if (value != null) {
            def reportPeriod = getReportPeriod(value)
            if (!formData.accruing && reportPeriod.order != 1) {
                def prevPeriodId = value ? getPrevReportPeriod(value)?.id : null
                checkOpuCodes(key, prevPeriodId, opuCodes, tmpRow)
            }
        }
        checkOpuCodes(key, value, opuCodes, tmpRow)
    }
}

@Field
def comparativPeriodId

def getComparativePeriodId() {
    if (comparativPeriodId == null && formData.comparativPeriodId != null) {
        comparativPeriodId = departmentReportPeriodService.get(formData.comparativPeriodId)?.reportPeriod?.id
    }
    return comparativPeriodId
}

void checkOpuCodes(def alias, def periodId, def opuCodes, def tmpRow) {
    if (periodId == null) {
        logger.warn("Форма 102 бухгалтерской отчетности: Подразделение: \"%s\". Отсутствует отчетный период, соответствующий значениям НФ! При заполнении графы \"%s\" формы значения будут приняты за нулевые.",
                formDataDepartment.name, getColumnName(tmpRow, alias))
        return
    }
    boolean foundBO = true
    def accountPeriodId = bookerStatementService.getAccountPeriodId(formData.departmentId, getEndDate(periodId))
    if (accountPeriodId == null) {
        foundBO = false
    } else {
        def ids = formDataService.getRefBookProvider(refBookFactory, 52L, providerCache).getUniqueRecordIds(new Date(), "ACCOUNT_PERIOD_ID = " + bookerStatementService.getAccountPeriodId(formData.departmentId, getEndDate(periodId)))
        if (ids == null || ids.isEmpty()) {
            foundBO = false
        }
    }
    def reportPeriod = getReportPeriod(periodId)
    if (!foundBO) {
        logger.warn("Не найдена форма 102 бухгалтерской отчетности: Период: \"%s %s\", Подразделение: \"%s\". Ячейки по графе \"%s\", заполняемые из данной формы, будут заполнены нулевым значением.",
                reportPeriod?.getName() ?: "Период не задан", reportPeriod?.getTaxPeriod()?.getYear() ?: "Год не задан", formDataDepartment.name, getColumnName(tmpRow, alias))
    } else {
        // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
        def records = bookerStatementService.getRecords(52L, formData.departmentId, getEndDate(periodId), "OPU_CODE = '${opuCodes.join("' OR OPU_CODE = '")}'")
        def recordOpuCodes = records?.collect { it.OPU_CODE.stringValue }?.unique() ?: []
        def missedCodes = opuCodes.findAll { !recordOpuCodes.contains(it) }
        if (!missedCodes.isEmpty()) {
            logger.warn("Форма 102 бухгалтерской отчетности: Период: \"%s %s\", Подразделение: \"%s\". Отсутствуют значения по следующим символам: '%s'! При заполнении графы \"%s\" формы значения по данным символам будут приняты за нулевые.",
                    reportPeriod?.getName() ?: "Период не задан", reportPeriod?.getTaxPeriod()?.getYear() ?: "Год не задан", formDataDepartment.name, missedCodes.join("', '"), getColumnName(tmpRow, alias))
        }
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    calcValues(dataRows, dataRows, false)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for(int i = 0; i < dataRows.size(); i++){
        checkNonEmptyColumns(dataRows[i], dataRows[i].getIndex(), nonEmptyColumns, logger, true)
    }
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def tempRows = formTemplate.rows
    updateIndexes(tempRows)
    calcValues(tempRows, dataRows, true)
    for(int i = 0; i < dataRows.size(); i++){
        def row = dataRows[i]
        def tempRow = tempRows[i]
        def checkColumns = []
        // делаем проверку БО только для первичных НФ
        if (opuMap.keySet().contains(row.getAlias())) {
            if (formData.kind == FormDataKind.PRIMARY) {
                checkColumns += check102Columns
            }
        } else {
            checkColumns += check102Columns
        }
        checkColumns += checkCalcColumns
        checkCalc(row, checkColumns, tempRow, logger, true)
    }
}

void calcValues(def dataRows, def sourceRows, boolean needShowMessage) {
    // при консолидации не подтягиваем данные при расчете
    if (formDataEvent != FormDataEvent.COMPOSE) {
        for (def alias in opuMap.keySet()) {
            def row = getDataRow(dataRows, alias)
            def rowSource = getDataRow(sourceRows, alias)

            row.comparePeriod = calcBO(rowSource, getComparativePeriodId())
            row.currentPeriod = calcBO(rowSource, formData.reportPeriodId)
        }
    }
    def row4 = getDataRow(dataRows, "R4")
    def row3 = getDataRow(dataRows, "R3")
    def row3Source = getDataRow(sourceRows, "R3")
    def row2Source = getDataRow(sourceRows, "R2")
    def row1Source = getDataRow(sourceRows, "R1")
    ['comparePeriod', 'currentPeriod'].each {
        row3[it] = (row2Source[it] ?: 0) - (row1Source[it] ?: 0)
        row4[it] = (row3Source[it] ?: 0) * 0.2
    }
    for(int i = 0; i < dataRows.size(); i++){
        def row = dataRows[i]
        def rowSource = sourceRows[i]
        row.deltaRub = (rowSource.currentPeriod ?: 0) - (rowSource.comparePeriod ?: 0)
        row.deltaPercent = null
        if (rowSource.comparePeriod) {
            row.deltaPercent = ((rowSource.deltaRub ?: BigDecimal.ZERO) as BigDecimal) * 100 / rowSource.comparePeriod.doubleValue()
        } else {
            row.deltaPercent = 0
            if (needShowMessage) { // выводить только в logicCheck
                rowWarning(logger, row, String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                        row.getIndex(), getColumnName(row, 'deltaPercent')))
            }
        }
    }
}

// Расчет сумм из БО за определенный период
def calcBO(def rowSource, def periodId) {
    def periodSum = 0
    if (periodId != null) {
        def pair = get102Sum(rowSource, periodId)
        boolean isCorrect = pair[1]
        periodSum = isCorrect ? pair[0] : 0
        def reportPeriod = getReportPeriod(periodId)
        if (!formData.accruing && reportPeriod.order != 1 && isCorrect) {
            def prevPeriodId = getPrevReportPeriod(periodId)?.id
            pair = get102Sum(rowSource, prevPeriodId)
            isCorrect = pair[1]
            periodSum = isCorrect ? (periodSum - pair[0]) : 0
        }
    }
    return periodSum
}

// Возвращает данные из Формы 102 БО за период + флаг корректности
def get102Sum(def row, def periodId) {
    if (opuMap[row.getAlias()] != null && periodId != null) {
        def opuCodes = opuMap[row.getAlias()].join("' OR OPU_CODE = '")
        // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
        def records = bookerStatementService.getRecords(52L, formData.departmentId, getEndDate(periodId), "OPU_CODE = '${opuCodes}'")
        if ((records == null || records.isEmpty())) {
            return [0, false]
        }
        return [records.sum { it.TOTAL_SUM.numberValue }, true]
    } else if (periodId == null) {
        return [0, false]
    }
    return [0, true]
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 6
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
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
            (headerRows[0][0]): getColumnName(tmpRow, 'rowNum'),
            (headerRows[0][1]): getColumnName(tmpRow, 'indicatorName'),
            (headerRows[0][2]): getColumnName(tmpRow, 'comparePeriod'),
            (headerRows[0][3]): getColumnName(tmpRow, 'currentPeriod'),
            (headerRows[0][4]): 'Изменение за период',
            (headerRows[1][4]): '(гр.4-гр.3), тыс.руб.',
            (headerRows[1][5]): '(гр.5/гр.3*100),%'
    ]
    (0..5).each { index ->
        headerMapping.put((headerRows[2][index]), (index + 1).toString())
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
    def tmpValues = formData.createStoreMessagingDataRow()

    // графа 1
    def colIndex = 0
    tmpValues.rowNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2
    colIndex++
    tmpValues.indicatorName = values[colIndex]

    // Проверить фиксированные значения
    tmpValues.keySet().toArray().each { alias ->
        def value = tmpValues[alias]?.toString()
        def valueExpected = StringUtils.cleanString(templateRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 3..6
    calcColumns.each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // очистить значимые ячейки
    dataRows.each { row ->
        calcColumns.each { column ->
            row[column] = null
        }
    }
    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId))) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.getLast(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                // суммируем 3, 4-ую графу из источников
                dataRows.each { row ->
                    def sourceRow = getDataRow(sourceRows, row.getAlias())
                    check102Columns.each { column ->
                        row[column] = (row[column] ?: 0) + (sourceRow[column] ?: 0)
                    }
                }
            }
        }
    }
}