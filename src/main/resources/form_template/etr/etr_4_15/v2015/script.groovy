package form_template.etr.etr_4_15.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Приложение 4-15. Анализ структуры налога на добавленную стоимость (НДС).
 * formTemplateId = 715
 */

// графа 1 - comparePeriod			Период сравнения. НДС всего, тыс. руб.
// графа 2 - comparePeriodIgnore	Период сравнения. В том числе НДС не учитываемый, тыс. руб.
// графа 3 - comparePeriodPercent	Период сравнения. Доля НДС не учитываемый, %
// графа 4 - currentPeriod			Период. НДС всего, тыс. руб.
// графа 5 - currentPeriodIgnore	Период. В том числе НДС не учитываемый, тыс. руб.
// графа 6 - currentPeriodPercent	Период. Доля НДС не учитываемый, %
// графа 7 - delta					Изменения за период. НДС всего, тыс. руб.
// графа 8 - deltaIgnore			Изменения за период. В том числе НДС не учитываемый, тыс. руб.
// графа 9 - deltaPercent			Изменения за период. Доля НДС не учитываемый, %

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
def allColumns = ['comparePeriod', 'comparePeriodIgnore', 'comparePeriodPercent', 'currentPeriod',
        'currentPeriodIgnore', 'currentPeriodPercent', 'delta', 'deltaIgnore', 'deltaPercent']

@Field
def calcColumns = allColumns

// графа 3, 6..9
@Field
def checkConsolidatedColumns = ['comparePeriodPercent', 'currentPeriodPercent', 'delta', 'deltaIgnore', 'deltaPercent']

@Field
def nonEmptyColumns = allColumns

// мапа с кодами ОПУ для каждой графа 1, 2, 4, 5 (алиас графы -> список кодов ОПУ)
@Field
def opuMap = [
        'comparePeriod'       : [ '26411.01', '26411.02', '26411.11', '27203.01', '27203.02' ],
        'comparePeriodIgnore' : [ '26411.02', '26411.11', '27203.02' ],
        'currentPeriod'       : [ '26411.01', '26411.02', '26411.11', '27203.01', '27203.02' ],
        'currentPeriodIgnore' : [ '26411.02', '26411.11', '27203.02' ]
]

@Field
def startDateMap = [:]

@Field
def endDateMap = [:]

@Field
def comparativePeriodId = null

@Field
def reportPeriodsMap = [:]

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

def getComparativePeriodId() {
    if (formData.comparativePeriodId != null && comparativePeriodId == null) {
        comparativePeriodId = departmentReportPeriodService.get(formData.comparativePeriodId)?.reportPeriod?.id
    }
    return comparativePeriodId
}

def getReportPeriod(def id) {
    if (reportPeriodsMap[id] == null) {
        reportPeriodsMap[id] = reportPeriodService.get(id)
    }
    return reportPeriodsMap[id]
}

void preCalcCheck() {
    def tmpRow = formData.createDataRow()
    // собираем коды ОПУ
    def final opuCodes = opuMap.values().sum()
    // находим записи для текущего периода и периода сравнения
    ['comparePeriod' : getComparativePeriodId(), 'currentPeriod' : formData.reportPeriodId].each { key, value ->
        def reportPeriod = getReportPeriod(value)
        if (!formData.accruing && reportPeriod.order != 1) {
            def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
            checkOpuCodes(key, date, opuCodes, tmpRow)
        }
        def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
        checkOpuCodes(key, date, opuCodes, tmpRow)
    }
}

void checkOpuCodes(def alias, def date, def opuCodes, def tmpRow) {
    // 3. Проверка наличия значений в ф.102 «Отчет о финансовых результатах» по символам (предрасчетные проверки)
    def accountPeriodId = bookerStatementService.getAccountPeriodId(formData.departmentId, date)
    if (accountPeriodId == null) {
        logger.warn('Форма 102 бухгалтерской отчетности: Подразделение: %s. Отсутствует отчетный период, соответствующий значениям НФ! При заполнении графы "%s" формы значения будут приняты за нулевые.',
                formDataDepartment.name, getColumnName(tmpRow, alias))
        return
    }

    // 2. Проверка наличия ф.102 «Отчет о финансовых результатах» (предрасчетные проверки)
    boolean foundBO = true
    if (accountPeriodId == null) {
        foundBO = false
    } else {
        def provider = formDataService.getRefBookProvider(refBookFactory, 52L, providerCache)
        def ids = provider.getUniqueRecordIds(new Date(), "ACCOUNT_PERIOD_ID = $accountPeriodId")
        if (ids == null || ids.isEmpty()) {
            foundBO = false
        }
    }
    if (!foundBO) {
        logger.warn('Не найдена форма 102 бухгалтерской отчетности: Период: %s %s, Подразделение: %s. ' +
                'Ячейки по графе "%s", заполняемые из данной формы, будут заполнены нулевым значением.',
                getPeriodNameBO(date), date.format('yyyy'), formDataDepartment.name, getColumnName(tmpRow, alias))
    } else {
        // 3. Проверка наличия значений в ф.102 «Отчет о финансовых результатах» по символам (предрасчетные проверки)
        // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
        def filter = "OPU_CODE = '${opuCodes.join("' OR OPU_CODE = '")}'"
        def records = bookerStatementService.getRecords(52L, formData.departmentId, date, filter)
        def recordOpuCodes = records?.collect { it.OPU_CODE.stringValue }?.unique() ?: []
        def missedCodes = opuCodes.findAll { !recordOpuCodes.contains(it) }
        if (!missedCodes.isEmpty()) {
            logger.warn('Форма 102 бухгалтерской отчетности: Период: %s %s, Подразделение: %s. ' +
                    'Отсутствуют значения по следующим символам: %s! При заполнении графы "%s" формы значения по данным символам будут приняты за нулевые.',
                    getPeriodNameBO(date), date.format('yyyy'), formDataDepartment.name, missedCodes.join(', '), getColumnName(tmpRow, alias))
        }
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def row = dataRows[0]
    calcValues(row, row)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def row = dataRows[0]

    // 1. Проверка заполнения ячеек
    checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

    // получить строки из шаблона
    def tempRow = formData.createDataRow()
    tempRow.setIndex(1)
    // подсчет временных тестовых данных
    calcValues(tempRow, row)

    // 2. Проверка заполнения граф (арифметирческие проверки)
    def chekColumns = (formData.kind == FormDataKind.CONSOLIDATED ? checkConsolidatedColumns : calcColumns)
    checkCalc(row, chekColumns, tempRow, logger, true)
}

/**
 * Посчитать значения.
 *
 * @param resultRow строка в которую запишутся результаты
 * @param sourceRow строка, данные которой будут использоваться в расчетах
 */
void calcValues(def resultRow, def sourceRow) {
    // признак показывать ли сообщение логической проверки 3
    def needShowMsg = resultRow != sourceRow

    // расчет графы 1, 2, 4, 5 - только для первичных формы
    if (formData.kind != FormDataKind.CONSOLIDATED) {
        // расчет графы 1, 2, 4, 5
        for (alias in opuMap.keySet()) {
            def reportPeriodId = (alias.contains('compare') ? getComparativePeriodId() : formData.reportPeriodId)
            resultRow[alias] = calcBO(alias, reportPeriodId)
        }
    } else {
        // для консолидлированных форм значения граф 1, 2, 4, 5 переложить из sourceRow в resultRow
        for (alias in opuMap.keySet()) {
            resultRow[alias] = sourceRow[alias]
        }
    }
    // графа 3
    resultRow.comparePeriodPercent = calc3(resultRow, needShowMsg)
    // графа 6
    resultRow.currentPeriodPercent = calc6(resultRow, needShowMsg)
    // графа 7
    resultRow.delta = calc7(resultRow)
    // графа 8
    resultRow.deltaIgnore = calc8(resultRow)
    // графа 9
    resultRow.deltaPercent = calc9(resultRow)
}

def calc3(def row, def needShowMsg) {
    return calc3or6(row, 'comparePeriodIgnore', 'comparePeriod', needShowMsg, 'comparePeriodPercent')
}

def calc6(def row, def needShowMsg) {
    return calc3or6(row, 'currentPeriodIgnore', 'currentPeriod', needShowMsg, 'currentPeriodPercent')
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
def calc3or6(def row, def dividendAlias, def dividerAlias, def needShowMsg, def resultAlias) {
    def result = null
    def dividend = (row[dividendAlias] ?: 0)
    def divider = row[dividerAlias]
    // проверка делителя на 0 или null
    if (divider) {
        // расчет
        result = dividend * 100 / divider.doubleValue()
    } else {
        result = 0
        if (needShowMsg) {
            // Логическая проверка 3. Проверка графы 2 при расчете графы 3
            // Логическая проверка 4. Проверка графы 2 при расчете графы 6
            def msg = String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                    row.getIndex(), getColumnName(row, resultAlias))
            rowWarning(logger, row, msg)
        }
    }
    return result
}

def calc7(def row) {
    return calcSome(row, 'currentPeriod', 'comparePeriod')
}

def calc8(def row) {
    return calcSome(row, 'currentPeriodIgnore', 'comparePeriodIgnore')
}

def calc9(def row) {
    return calcSome(row, 'currentPeriodPercent', 'comparePeriodPercent')
}

def calcSome(def row, def alias1, def alias2) {
    if (row[alias1] == null || row[alias2] == null) {
        return null
    }
    return row[alias1] - row[alias2]
}

// Расчет сумм из БО за определенный период
def calcBO(def columnAlias, def periodId) {
    def periodSum = 0
    if (periodId != null) {
        def reportPeriod = getReportPeriod(periodId)
        def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
        def pair = get102Sum(columnAlias, date)
        boolean isCorrect = pair[1]
        periodSum = isCorrect ? pair[0] : 0
        if (!formData.accruing && reportPeriod.order != 1 && isCorrect) {
            def prevDate = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
            pair = get102Sum(columnAlias, prevDate)
            isCorrect = pair[1]
            periodSum = isCorrect ? (periodSum - pair[0]) : 0
        }
    }
    return periodSum
}

// Возвращает данные из Формы 102 БО за период + флаг корректности
def get102Sum(def columnAlias, def date) {
    if (opuMap[columnAlias] != null) {
        def filter = "OPU_CODE = '${opuMap[columnAlias].join("' OR OPU_CODE = '")}'"
        // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
        def records = bookerStatementService.getRecords(52L, formData.departmentId, date, filter)
        if (records == null || records.isEmpty()) {
            return [0, false]
        }
        def result = records.sum { it.TOTAL_SUM.numberValue } / 1000
        return [result, true]
    }
    return [0, true]
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // очистить графу 1..9
    dataRows.each { row ->
        allColumns.each { alias ->
            row[alias] = null
        }
    }
    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId)).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                // суммируем графу 1, 2, 4, 5 из источников
                for (def row : dataRows) {
                    def sourceRow = getDataRow(sourceRows, row.getAlias())
                    opuMap.keySet().each { column ->
                        row[column] = (row[column] ?: 0) + (sourceRow[column] ?: 0)
                    }
                }
            }
        }
    }
}

void importData() {
    int COLUMN_COUNT = 9
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Период сравнения'
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
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): 'Период сравнения']),
            ([(headerRows[0][3]): 'Период']),
            ([(headerRows[0][6]): 'Изменения за период']),
            ([(headerRows[1][0]): 'НДС всего, тыс. руб.']),
            ([(headerRows[1][1]): 'В том числе НДС не учитываемый, тыс. руб.']),
            ([(headerRows[1][2]): 'Доля НДС не учитываемый, %']),
            ([(headerRows[1][3]): 'НДС всего, тыс. руб.']),
            ([(headerRows[1][4]): 'В том числе НДС не учитываемый, тыс. руб.']),
            ([(headerRows[1][5]): 'Доля НДС не учитываемый, %']),
            ([(headerRows[1][6]): 'НДС всего, тыс. руб.']),
            ([(headerRows[1][7]): 'В том числе НДС не учитываемый, тыс. руб.']),
            ([(headerRows[1][8]): 'Доля НДС не учитываемый, %']),
            ([(headerRows[2][0]) : 'символ формы 102 (26411.01+26411.02+26411.11+27203.01+27203.02)']),
            ([(headerRows[2][1]) : 'символ формы 102 (26411.02+26411.11+27203.02)']),
            ([(headerRows[2][2]) : '(гр.2/гр.1)*100']),
            ([(headerRows[2][3]) : 'символ формы 102 (26411.01+26411.02+26411.11+27203.01+27203.02)']),
            ([(headerRows[2][4]) : 'символ формы 102 (26411.02+26411.11+27203.02)']),
            ([(headerRows[2][5]) : '(гр.5/гр.4)*100']),
            ([(headerRows[2][6]) : 'гр.4-гр.1']),
            ([(headerRows[2][7]) : 'гр.5-гр.2']),
            ([(headerRows[2][8]): 'гр.6-гр.3'])
    ]
    (1..9).each { index ->
        headerMapping.add(([(headerRows[3][index - 1]): index.toString()]))
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
   // графа 1..9
    def colIndex = -1
    allColumns.each { alias ->
        colIndex++
        row[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
    return row
}

@Field
def endDateBOMap = [:]

def getEndDate(def year, def order) {
    def key = year + "#" + order
    if (endDateBOMap[key] == null) {
        endDateBOMap[key] = getEndDateBO(year, order)
    }
    return endDateBOMap[key]
}

/**
 * Получить последний день периода БО.
 *
 * @param year год
 * @param order номер периода БО (1, 2, 3, 4)
 */
def getEndDateBO(def year, def order) {
    def dateStr
    switch (order) {
        case 1:
            dateStr = "31.03.$year"
            break
        case 2:
            dateStr = "30.06.$year"
            break
        case 3:
            dateStr = "30.09.$year"
            break
        default:
            dateStr = "31.12.$year"
            break
    }
    return Date.parse('dd.MM.yyyy', dateStr)
}

@Field
def periodNameBOMap = [:]

/* Получить название периода БО по дате. */
def getPeriodNameBO(def date) {
    if (periodNameBOMap[date] == null) {
        periodNameBOMap[date] = bookerStatementService.getPeriodValue(date)?.NAME?.value
    }
    return periodNameBOMap[date]
}