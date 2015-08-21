package form_template.etr.etr_4_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Приложение 4-1. Абсолютная величина налоговых платежей
 * formTemplateId = 701
 *
 * @author bkinzyabulatov
 *
 * графа 1 - rowNum         - № строки
 * графа 2 - taxName        - Наименование налога
 * графа 3 - symbol102      - символ формы 102
 * графа 4 - comparePeriod  - Период сравнения
 * графа 5 - currentPeriod  - Период
 * графа 6 - deltaRub       - Изменение за период (гр.5-гр.4), тыс.руб.
 * графа 7 - deltaPercent   - Изменение за период (гр.6/гр.4*100),%
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
int sourceFormTypeId = 700

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

@Field
def opuMap = ['R3' : ['28101'],
           'R5' : ['26411.01', '26411.02'],
           'R6' : ['26411.03'],
           'R7' : ['26102' ,'26410.09'],
           'R8' : ['26411.12', '26411.13']]

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
    def final opuCodes = opuMap.findAll { key, value -> !("R3".equals(key)) || isBank() }.values().sum()
    // находим записи для текущего периода и периода сравнения
    ['comparePeriod': getComparativePeriodId(), 'currentPeriod':formData.reportPeriodId].each { key, value ->
        if (value != null) {
            def reportPeriod = getReportPeriod(value)
            if (formData.accruing && reportPeriod.order != 1) {
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
    calcValues(dataRows, dataRows)
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
    calcValues(tempRows, dataRows)
    for(int i = 0; i < dataRows.size(); i++){
        def row = dataRows[i]
        def tempRow = tempRows[i]
        def checkColumns = []
        // делаем проверку БО только для первичных НФ
        if (opuMap.keySet().contains(row.getAlias()) && (!"R3".equals(row.getAlias()) || isBank())) {
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

void calcValues(def dataRows, def sourceRows) {
    // при консолидации не подтягиваем данные при расчете
    if (formDataEvent != FormDataEvent.COMPOSE) {
        for (def alias in opuMap.keySet()) {
            def row = getDataRow(dataRows, alias)
            def rowSource = getDataRow(sourceRows, alias)
            if ("R3".equals(alias) && !isBank()) {
                row.comparePeriod = getSourceValue(getComparativePeriodId(), row, 'comparePeriod')
                row.currentPeriod = getSourceValue(formData.reportPeriodId, row, 'currentPeriod')
                continue
            }

            row.comparePeriod = calcBO(rowSource, getComparativePeriodId(), 'comparePeriod')
            row.currentPeriod = calcBO(rowSource, formData.reportPeriodId, 'currentPeriod')
        }
    }
    def row2 = getDataRow(dataRows, "R2")
    def row4 = getDataRow(dataRows, "R4")
    def row3Source = getDataRow(sourceRows, "R3")
    def row4Source = getDataRow(sourceRows, "R4")
    def row5Source = getDataRow(sourceRows, "R5")
    def row8Source = getDataRow(sourceRows, "R8")
    ['comparePeriod', 'currentPeriod'].each {
        def smallSum = (row5Source[it] ?: 0) + (row8Source[it] ?: 0)
        row4[it] = smallSum
        def largeSum = (row3Source[it] ?: 0) + (row4Source[it] ?: 0)
        row2[it] = largeSum
    }
    for(int i = 0; i < dataRows.size(); i++){
        def row = dataRows[i]
        def rowSource = sourceRows[i]
        row.deltaRub = (rowSource.currentPeriod ?: 0) - (rowSource.comparePeriod ?: 0)
        row.deltaPercent = null
        if (rowSource.comparePeriod) {
            row.deltaPercent = ((rowSource.deltaRub ?: BigDecimal.ZERO) as BigDecimal) / rowSource.comparePeriod * 100
        } else if (dataRows != sourceRows) { // выводить только при расчете
            rowError(logger, row, String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно)",
                    row.getIndex(), getColumnName(row, 'deltaPercent')))
        }
    }
}

def getSourceValue(def periodId, def row, def alias) {
    boolean found = false
    if (periodId != null) {
        def reportPeriod = getReportPeriod(periodId)
        for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
                getStartDate(periodId), getEndDate(periodId))) {
            if (formDataSource.formTypeId == sourceFormTypeId) {
                found = true
                def source = formDataService.getLast(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, periodId, formData.periodOrder)
                if (source != null && source.state == WorkflowState.ACCEPTED) {
                    sourceForm = formDataService.getDataRowHelper(source)
                    return sourceForm.allSaved?.get(0)?.sum
                } else {
                    logger.warn("Не найдена форма-источник «Величины налоговых платежей, вводимые вручную» в статусе «Принята»: Тип формы: \"%s\", Период: \"%s %s\", Подразделение: \"%s\". Ячейки по графе «%s», заполняемые из данной формы, будут заполнены нулевым значением.",
                            formDataSource.kind.name, reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear(), departmentService.get(formDataSource.departmentId)?.name, getColumnName(row, alias))
                }
            }
        }
    }
    if (!found) {
        String periodString = ""
        if (periodId != null) {
            def reportPeriod = getReportPeriod(periodId)
            periodString = String.format(": Период: \"%s %s\"", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear())
        }
        logger.warn("Не найдена форма «Величины налоговых платежей, вводимые вручную» в списке назначенных источников%s. Ячейки по графе \"%s\", заполняемые из данной формы, будут заполнены нулевым значением.",
                periodString, getColumnName(row, alias))
    }
    return 0
}

// Расчет сумм из БО за определенный период
def calcBO(def rowSource, def periodId, def alias) {
    def periodSum = 0
    if (periodId != null) {
        def pair = get102Sum(rowSource, periodId)
        boolean isCorrect = pair[1]
        periodSum = isCorrect ? pair[0] : 0
        def reportPeriod = getReportPeriod(periodId)
        if (formData.accruing && reportPeriod.order != 1 && isCorrect) {
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

boolean isBank() {
    return formData.departmentId == 1 // по ЧТЗ
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 7
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
            (headerRows[0][1]): getColumnName(tmpRow, 'taxName'),
            (headerRows[0][2]): getColumnName(tmpRow, 'symbol102'),
            (headerRows[0][3]): getColumnName(tmpRow, 'comparePeriod'),
            (headerRows[0][4]): getColumnName(tmpRow, 'currentPeriod'),
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
    tmpValues.taxName = values[colIndex]

    // графа 3
    colIndex++
    tmpValues.symbol102 = values[colIndex]

    // Проверить фиксированные значения
    tmpValues.keySet().toArray().each { alias ->
        def value = tmpValues[alias]?.toString()
        def valueExpected = StringUtils.cleanString(templateRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 4..7
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
                // суммируем 4, 5-ую графу из источников
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