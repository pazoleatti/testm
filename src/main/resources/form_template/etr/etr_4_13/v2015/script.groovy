package form_template.etr.etr_4_13.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Приложение 4-13. Анализ структуры доходов и расходов, не учитываемых для целей налогообложения.
 * formTemplateId = 713
 */

// графа 1 - rowNum
// графа 2 - taxName
// графа 3 - symbol102
// графа 4 - comparePeriod
// графа 5 - comparePeriodPercent
// графа 6 - currentPeriod
// графа 7 - currentPeriodPercent
// графа 8 - deltaRub
// графа 9 - deltaPercent

switch (formDataEvent) {
    case FormDataEvent.GET_HEADERS:
        headers.get(1).comparePeriod = 'Сумма, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(1).currentPeriod = 'Сумма, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(1).deltaRub = '(гр.6-гр.4), ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        break
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
def allColumns = ['rowNum', 'taxName', 'symbol102', 'comparePeriod', 'comparePeriodPercent', 'currentPeriod', 'currentPeriodPercent', 'deltaRub', 'deltaPercent']

// графа 4..9
@Field
def calcColumns = ['comparePeriod', 'comparePeriodPercent', 'currentPeriod', 'currentPeriodPercent', 'deltaRub', 'deltaPercent']

// графа 5, 7..9
@Field
def checkConsolidatedColumns = ['comparePeriodPercent', 'currentPeriodPercent', 'deltaRub', 'deltaPercent']

// графа 4..9
@Field
def nonEmptyColumns = calcColumns

// графа 4, 6, 8, 9
@Field
def nonEmptyColumnsIandII = ['comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

// мапа с кодами ОПУ для каждой строки (алиас строки -> список кодов ОПУ)
@Field
def opuMap = [
        'I'    : ['16301', '16302', '16303', '16304', '16305', '16306', '27101', '27102', '27103', '27201', '27202', '27203', '27301', '27302', '27303', '27304', '27305', '27306', '27307', '27308', '27309'],
        'I.I'  : ['16305.02', '17201.97', '17202.07', '17202.08', '17202.09', '17202.97', '17202.99', '17203.02', '17203.03', '17203.06', '17203.09', '17203.11', '17203.13', '17203.14', '17203.97', '17306.19', '17306.20', '17306.99', '17307'],
        'II'   : ['25301', '25302', '25303', '26101', '26102', '26103', '26104', '26201', '26202', '26203', '26204', '26301', '26302', '26303', '26304', '26305', '26306', '26307', '26401', '26402', '26403', '26404', '26405', '26406', '26407', '26408', '26409', '26410', '26411', '26412', '27101', '27102', '27103', '27201', '27202', '27203', '27301', '27302', '27303', '27304', '27305', '27306', '27307', '27308', '27309'],
        '1'    : ['26101.04', '26101.12', '26101.13', '26101.14', '26101.99', '26401.04', '27203.08'],
        '2'    : ['26104.01', '26104.02', '26104.03', '26104.04', '26104.05', '26104.06', '26104.99', '27308.15', '27308.16', '27308.17', '27308.18'],
        '3'    : ['26410.04'],
        '4'    : ['27308.19', '27308.20', '27308.21'],
        '5'    : ['26301.06', '26301.08', '26301.10', '26301.12', '26301.14', '26301.16', '26302.02', '26302.04', '26302.06', '26302.08', '26302.10', '26302.12', '26302.14', '26302.16', '26302.18', '26302.20', '26305.02', '26305.05', '26305.07', '26305.09', '26305.11', '26305.13', '27203.20', '27203.22', '27203.24', '27203.26', '27203.28', '27203.30', '27203.32', '27203.34', '27203.46'],
        '6'    : ['26303.02', '27203.36'],
        '7'    : ['26402.02'],
        '8'    : ['26403.04', '26406.06', '26406.07', '26406.08', '26406.09', '26406.10', '26406.11', '26406.13'],
        '9'    : ['26405.02'],
        // для строки 10 коды опу повторяются по аналогии с 10.1-10.4 (убрал потому что эта строка рассчитывается по подстрокам)
        // '10'   : ['26411.02', '26411.05', '26411.06', '26411.09', '26411.10', '26411.11', '27203.02', '27203.05'],
        '10.1' : ['26411.02', '26411.11', '27203.02'],
        '10.2' : ['26411.05', '26411.10', '27203.05'],
        '10.3' : ['26411.06'],
        '10.4' : ['26411.09'],
        '11'   : ['26412.03'],
        '12'   : ['26412.09', '26412.22', '26412.24'],
        '13'   : ['26412.12'],
        '14'   : ['26412.14'],
        '15'   : ['26412.99', '27203.13'],
        '16'   : ['27301'],
        '17'   : ['27203.09', '27203.11'],
        '18'   : ['27103', '27203.15'],
        '19'   : ['25203.03', '25302.02', '26401.02', '26403.02', '26410.06', '26410.99', '26412.10', '26412.27', '27201.97', '27202.99', '27203.38', '27203.40', '27203.42', '27203.44', '27203.97', '27203.99', '27302', '27303', '27304', '27307', '27308.98', '27308.97'],
        // для строки 20 коды опу повторяются по аналогии с 20.1-20.3 (убрал потому что эта строка рассчитывается по подстрокам)
        // '20'   : ['27305', '27306', '27308.24'],
        '20.1' : ['27305'],
        '20.2' : ['27306'],
        '20.3' : ['27308.24']
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
        logger.warn('Не найдена форма 102 бухгалтерской отчетности: Период: %s %s, Подразделение: %s. ' +
                'Ячейки по графе "%s", заполняемые из данной формы, будут заполнены нулевым значением.',
                getPeriodNameBO(date), date.format('yyyy'), formDataDepartment.name, getColumnName(tmpRow, alias))
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
    calcValues(dataRows, dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // 1. Проверка заполнения ячеек
    for (def row : dataRows) {
        if (!row.getAlias()) {
            continue
        }
        // 1. Проверка заполнения ячеек (обязательные поля) (для строки I и II некоторые графы пропускаются)
        def columns = (row.getAlias() in ['I', 'II'] ? nonEmptyColumnsIandII : nonEmptyColumns)
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def tempRows = formTemplate.rows
    updateIndexes(tempRows)
    // подсчет временных тестовых данных
    calcValues(tempRows, dataRows)

    // 2. Проверка заполнения граф 4 - 9
    for (def row : dataRows) {
        if (!row.getAlias()) {
            continue
        }
        // 2. Проверка заполнения граф 4 - 9 (арифметирческие проверки)
        def tempRow = getDataRow(tempRows, row.getAlias())
        def isConsolidatedCheckRow = (formData.kind == FormDataKind.CONSOLIDATED && opuMap.keySet().contains(row.getAlias()))
        def chekColumns = (isConsolidatedCheckRow ? checkConsolidatedColumns : calcColumns)
        checkCalc(row, chekColumns, tempRow, logger, true)
    }
}

/**
 * Посчитать значения.
 *
 * @param resultRows строки в которые запишутся результаты
 * @param sourceRows строки, данные которых будут использоваться в расчетах
 */
void calcValues(def resultRows, def sourceRows) {
    // мапа для расчетов (алиас строки -> список алиасов строк)
    def map = [
            // для графы 4 и 6 не используется
            'I'    : ['I.I'],
            // для графы 4 и 6 не используется
            'II'   : ['II.I'],
            '10'   : ['10.1', '10.2', '10.3', '10.4'],
            '20'   : ['20.1', '20.2', '20.3'],
            // идет после строки 10 и 20 потому что они используеются при рассчете
            'II.I' : ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20'],
    ]

    // расчет графы 4 и 6 - только для первичных формы
    if (formData.kind != FormDataKind.CONSOLIDATED) {
        // расчет графы 4 и 6 для строк I–II, 1-9, 10.1-19, 20.1–20.3
        for (alias in opuMap.keySet()) {
            def row = getDataRow(resultRows, alias)
            def rowSource = getDataRow(sourceRows, alias)
            // графа 4
            row.comparePeriod = calcBO(rowSource, getComparativePeriodId())
            // графа 6
            row.currentPeriod = calcBO(rowSource, formData.reportPeriodId)
        }

        // расчет графы 4 и 6 для строк II.I, 10, 20
        // ключ - алиас строки, значение - список алиасов строк для суммирования
        for (def alias : map.keySet().toArray()) {
            if (alias in ['I', 'II']) {
                continue
            }
            def row = getDataRow(resultRows, alias)
            def sumRowAliases = map[alias]
            // отобрать нужные строки
            def rows = resultRows.findAll { it.getAlias() in sumRowAliases }
            // графа 4
            row.comparePeriod = calc4or6(rows, 'comparePeriod')
            // графа 6
            row.currentPeriod = calc4or6(rows, 'currentPeriod')
        }
    } else {
        // для консолидлированных форм значения граф 4 и 6 переложить из sourceRows в resultRows
        def rows = resultRows.findAll { it.getAlias() }
        rows.each { row ->
            def sourceRow = getDataRow(sourceRows, row.getAlias())
            row.comparePeriod = sourceRow.comparePeriod
            row.currentPeriod = sourceRow.currentPeriod
        }
    }

    // признак показывать ли сообщение логической проверки 3
    def needShowMsg = resultRows != sourceRows

    // расчет графы 5 для строк I.I, II.I, 1-20.3
    calc5or7(resultRows, map, needShowMsg, 'comparePeriod', 'comparePeriodPercent')

    // расчет графы 7 для строк I.I, II.I, 1-20.3
    calc5or7(resultRows, map, needShowMsg, 'currentPeriod', 'currentPeriodPercent')

    // расчет графы 8 и 9 для всех строк
    def rows = resultRows.findAll { it.getAlias() }
    rows.each { row ->
        // графа 8
        row.deltaRub = calc8(row)

        // графа 9
        row.deltaPercent = calc9(row, needShowMsg)
    }
}

/**
 * Получить значение для графы 4 или 6.
 *
 * @param rows строки по которым надо получить сумму
 * @param alias алиас графы для которой расчитывается значения
 */
def calc4or6(def rows, def alias) {
    def value = rows.sum { it[alias] ?: 0 }
    return value
}
/**
 * Посчитать значения для граф 5 или 7.
 *
 * @param rows строки нф
 * @param map мапа с алиасами строк делителей и списком алиасов расчитываемых строк
 * @param needShowMsg выводить ли сообщение логической проверки 3
 * @param calcAlias алиас графы делителя и делимого (для граф 5 и 7 они при расчетах совпадают)
 * @param resultAlias алиас графы для которой производится расчет
 */
void calc5or7(def rows, def map, def needShowMsg, def calcAlias, def resultAlias) {
    // ключ - алиас строки делителя, значение - список алиасов строк для которых расчитывается значение
    map.each { deviderAlias, calcRowAliases ->
        // строка делителя
        def dividerRow = getDataRow(rows, deviderAlias)
        // отобрать нужные строки
        def calcRows = rows.findAll { it.getAlias() in calcRowAliases }
        calcRows.each { row ->
            // графа 5 или 7
            row[resultAlias] = someCalc(row, dividerRow, calcAlias, calcAlias, needShowMsg, resultAlias)
        }
    }
}

/**
 * Расчитать значение для графы.
 *
 * @param row строка для расчета
 * @param dividerRow строка делителя
 * @param dividendAlias алиас графы числителя
 * @param dividerAlias алиас графы знаменателя
 * @param needShowMsg выводить ли сообщение логической проверки 3
 * @param resultAlias алиас графы для которой производится расчет (нужен для вывода сообщения)
 */
def someCalc(def row, def dividerRow, def dividendAlias, def dividerAlias, def needShowMsg, def resultAlias) {
    def result = null
    def dividend = (row[dividendAlias] ?: 0)
    def divider = dividerRow[dividerAlias]
    // проверка делителя на 0 или null
    if (divider) {
        // расчет
        result = dividend * 100 / divider.doubleValue()
    } else {
        result = 0
        if (needShowMsg) {
            // Логическая проверка 3. Проверка граф знаменателей при расчете граф 5, 7, 9
            def msg = String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                    row.getIndex(), getColumnName(row, resultAlias))
            rowWarning(logger, row, msg)
        }
    }
    return result
}

def calc8(def row) {
    if (row.currentPeriod == null || row.comparePeriod == null) {
        return null
    }
    return row.currentPeriod - row.comparePeriod
}

/**
 * Получить значение для графы 9.
 *
 * @param row строка нф
 * @param needShowMsg выводить ли сообщение логической проверки 3
 */
def calc9(def row, def needShowMsg) {
    if (row.deltaRub == null || row.comparePeriod == null) {
        return null
    }
    return someCalc(row, row, 'deltaRub', 'comparePeriod', needShowMsg, 'deltaPercent')
}

// Расчет сумм из БО за определенный период
def calcBO(def rowSource, def periodId) {
    def periodSum = 0
    if (periodId != null) {
        def reportPeriod = getReportPeriod(periodId)
        def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
        def pair = get102Sum(rowSource, date)
        boolean isCorrect = pair[1]
        periodSum = isCorrect ? pair[0] : 0
        if (!formData.accruing && reportPeriod.order != 1 && isCorrect) {
            def prevDate = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
            pair = get102Sum(rowSource, prevDate)
            isCorrect = pair[1]
            periodSum = isCorrect ? (periodSum - pair[0]) : 0
        }
    }
    return periodSum
}

// Возвращает данные из Формы 102 БО за период + флаг корректности
def get102Sum(def row, def date) {
    if (opuMap[row.getAlias()] != null) {
        def opuCodes = opuMap[row.getAlias()].join("' OR OPU_CODE = '")
        // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
        def records = bookerStatementService.getRecords(52L, formData.departmentId, date, "OPU_CODE = '${opuCodes}'")
        if (records == null || records.isEmpty()) {
            return [0, false]
        }
        def result = records.sum { it.TOTAL_SUM.numberValue } / (isBank() ? 1000000 : 1000)
        return [result, true]
    }
    return [0, true]
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // очистить графу 4..9
    dataRows.each { row ->
        calcColumns.each { alias ->
            row[alias] = null
        }
    }

    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId)).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                // суммируем графу 4..9 из источников
                for (def row : dataRows) {
                    if (!row.getAlias()) {
                        continue
                    }
                    def sourceRow = getDataRow(sourceRows, row.getAlias())
                    ['comparePeriod', 'currentPeriod'].each { column ->
                        row[column] = (row[column] ?: BigDecimal.ZERO) + ((source.departmentId == 1) ? 1000 : 1) * (sourceRow[column] ?: BigDecimal.ZERO)
                    }
                }
            }
        }
    }
    if (isBank()) { // если уровень банка, то тысячи понижаем до миллионов
        dataRows.each { row ->
            ['comparePeriod', 'currentPeriod'].each { column ->
                if (row[column]) {
                    row[column] = (row[column] as BigDecimal).divide(BigDecimal.valueOf(1000), BigDecimal.ROUND_HALF_UP)
                }
            }
        }
    }
}

boolean isBank() {
    return formData.departmentId == 1 // по ЧТЗ
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 9
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null

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

    def dataRows = formDataService.getDataRowHelper(formData).allCached
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
        rowIndex++
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def dataRow = dataRows.get(rowIndex - 1)
        def templateRow = (dataRow.getAlias() ? getDataRow(templateRows, dataRow.getAlias()) : templateRows.get(rowIndex - 1))
        // заполнить строку нф значениями из эксель
        fillRowFromXls(templateRow, dataRow, rowValues, fileRowIndex, rowIndex, colOffset)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    // очистить графу 5 и 7 в строке I и II
    def rows1and2 = dataRows.findAll { it.getAlias() in ['I', 'II'] }
    rows1and2.each { row ->
        row.comparePeriodPercent = null
        row.currentPeriodPercent = null
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][1]): getColumnName(tmpRow, 'taxName')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'symbol102')]),
            ([(headerRows[0][3]): 'Период сравнения']),
            ([(headerRows[0][5]): 'Период']),
            ([(headerRows[0][7]): 'Изменения за период']),
            ([(headerRows[1][3]): ('Сумма, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[1][4]): 'Удельный вес в общей сумме за период, %']),
            ([(headerRows[1][5]): ('Сумма, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[1][6]): 'Удельный вес в общей сумме за период, %']),
            ([(headerRows[1][7]): ('(гр.6-гр.4), ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[1][8]): '(гр.8/гр.4*100), %'])
    ]
    (1..9).each { index ->
        headerMapping.add(([(headerRows[2][index - 1]): index.toString()]))
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

    // графа 1..3
    def colIndex = -1
    ['rowNum', 'taxName', 'symbol102'].each { alias ->
        colIndex++
        tmpValues[alias] = values[colIndex]
    }

    // Проверить фиксированные значения
    tmpValues.keySet().toArray().each { alias ->
        def value = tmpValues[alias]?.toString()
        def valueExpected = StringUtils.cleanString(templateRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 4..9
    calcColumns.each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
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