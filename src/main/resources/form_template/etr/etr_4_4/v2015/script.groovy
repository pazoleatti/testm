package form_template.etr.etr_4_4.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Приложение 4-4. Отношение налоговых платежей к операционным доходам.
 * formTemplateId = 704
 *
 * графа 1 - rowNum         - № строки
 * графа 2 - taxName        - Наименование налога
 * графа 3 - comparePeriod  - Период сравнения
 * графа 4 - currentPeriod  - Период
 * графа 5 - deltaRub       - Изменение за период (гр.4-гр.3), тыс.руб.
 * графа 6 - deltaPercent   - Изменение за период (гр.5/гр.3*100),%
 */

switch (formDataEvent) {
    case FormDataEvent.GET_HEADERS:
        headers.get(0).comparePeriod = 'Период сравнения, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(0).currentPeriod = 'Период, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
        headers.get(1).deltaRub = '(гр.4-гр.3), ' + (isBank() ? 'млн. руб.' : 'тыс. руб.')
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
int sourceFormTypeId = 700

@Field
def allColumns = ['rowNum', 'taxName', 'comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

// графа 3..6
@Field
def calcColumns = ['comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

// графа 5, 6
@Field
def someCalcColumns = ['deltaRub', 'deltaPercent']

// графа 3..6
@Field
def nonEmptyColumns = calcColumns

@Field // используется только для предрасчетных проверок
def opuMap = [ 'R1' : ['28101', '26411.01', '26411.02', '26411.03', '26102', '26410.09', '26411.12', '26411.13'] ]

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

@Field
def comparativePeriodId

def getComparativePeriodId() {
    if (comparativePeriodId == null && formData.comparativePeriodId != null) {
        comparativePeriodId = departmentReportPeriodService.get(formData.comparativePeriodId)?.reportPeriod?.id
    }
    return comparativePeriodId
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
    calcValues(dataRows, dataRows, true)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // 1. Проверка заполнения ячеек
    for (def row : dataRows) {
        // 1. Проверка заполнения ячеек
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def tempRows = formTemplate.rows
    updateIndexes(tempRows)
    // подсчет временных тестовых данных
    calcValues(tempRows, dataRows, false)

    // 2. Арифметирческие проверки
    for (int i = 0; i < dataRows.size(); i++) {
        def row = dataRows[i]
        def tempRow = tempRows[i]

        // 2. Проверка заполнения граф 3..6 (арифметирческие проверки)
        def isConsolidatedCheckRow = (formData.kind == FormDataKind.CONSOLIDATED && opuMap.keySet().contains(row.getAlias()))
        def chekColumns = (isConsolidatedCheckRow ? someCalcColumns : calcColumns)
        checkCalc(row, chekColumns, tempRow, logger, true)
    }
}

void calcValues(def dataRows, def sourceRows, boolean isCalc) {
    def row1 = getDataRow(dataRows, "R1")
    def row2 = getDataRow(dataRows, "R2")
    def row3 = getDataRow(dataRows, "R3")
    def row1Source = getDataRow(sourceRows, "R1")
    def row2Source = getDataRow(sourceRows, "R2")
    def row3Source = getDataRow(sourceRows, "R3")

    // графа 3, 4
    if (formData.kind != FormDataKind.CONSOLIDATED) {
        // графа 3 строки 1
        row1.comparePeriod = calcBO(row1Source, getComparativePeriodId())
        // графа 4 строки 1
        row1.currentPeriod = calcBO(row1Source, formData.reportPeriodId)

        // графа 3 строки 2
        row2.comparePeriod = getSourceValue(getComparativePeriodId(), row2, 'comparePeriod', isCalc)
        // графа 4 строки 2
        row2.currentPeriod = getSourceValue(formData.reportPeriodId, row2, 'currentPeriod', isCalc)
    } else {
        row1.comparePeriod = row1Source.comparePeriod
        row1.currentPeriod = row1Source.currentPeriod
        row2.comparePeriod = row2Source.comparePeriod
        row2.currentPeriod = row2Source.currentPeriod
    }

    // графа 3, 4 для строки 3
    ['comparePeriod', 'currentPeriod'].each {
        row3[it] = calc3or4Row3(row3Source, row1Source, row2Source, it, isCalc)
    }

    // графа 5, 6
    for (int i = 0; i < dataRows.size(); i++) {
        def row = dataRows[i]
        def rowSource = sourceRows[i]

        // графа 5
        row.deltaRub = calc5(rowSource)
        // графа 6
        row.deltaPercent = calc6(rowSource, isCalc)
    }
}

def calc5(def row) {
    if (row.currentPeriod == null || row.comparePeriod == null) {
        return null
    }
    return row.currentPeriod - row.comparePeriod
}

def calc6(def row, def needShowMsg) {
    if (row.deltaRub == null || row.comparePeriod == null) {
        return null
    }
    return someCalc(row, row, row, 'deltaRub', 'comparePeriod', needShowMsg, 'deltaPercent')
}

/**
 * Расчет графы 3 или 4 для строки 3.
 *
 * @param row строка 3 (строка для которой производится расчет)
 * @param row1 строка 1 (строка делимого)
 * @param row2 строка 2 (строка делителя)
 * @param alias алиас графы 3 или 4
 * @param needShowMsg выводить ли сообщение логической проверки 3
 */
def calc3or4Row3(def row, def row1, def row2, def alias, def needShowMsg) {
    return someCalc(row, row1, row2, alias, alias, needShowMsg, alias)
}

/**
 * Расчитать значение для графы.
 *
 * @param row строка для расчета
 * @param dividendRow строка делимого
 * @param dividerRow строка делителя
 * @param dividendAlias алиас графы числителя
 * @param dividerAlias алиас графы знаменателя
 * @param needShowMsg выводить ли сообщение логической проверки 3
 * @param resultAlias алиас графы для которой производится расчет (нужен для вывода сообщения)
 */
def someCalc(def row, def dividendRow, def dividerRow, def dividendAlias, def dividerAlias, def needShowMsg, def resultAlias) {
    def result = 0
    def dividend = (dividendRow[dividendAlias] ?: 0)
    def divider = dividerRow[dividerAlias]
    // проверка делителя на 0 или null
    if (divider) {
        // расчет
        result = dividend * 100 / divider.doubleValue()
    } else if (needShowMsg) {
        // Логическая проверка 3. Проверка граф знаменателей при расчете граф 3, 4, 6
        def msg = String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                row.getIndex(), getColumnName(row, resultAlias))
        rowWarning(logger, row, msg)
    }
    return result
}

def getSourceValue(def periodId, def row, def alias, def isCalc) {
    def sum = BigDecimal.ZERO
    boolean notFound404 = false
    if (periodId != null) {
        def reportPeriod = getReportPeriod(periodId)
        // если нарастающий итог, то собираем формы с начала года
        int startOrder = formData.accruing ? 1 : reportPeriod.order
        def periods = reportPeriodService.listByTaxPeriod(reportPeriod.taxPeriod.id).findAll{ it.order <= reportPeriod.order && it.order >= startOrder}
        // получить номера периодов, не заведенных в системе
        List<Integer> allPeriodOrders = (startOrder..reportPeriod.order)
        def existPeriodOrders = periods.collect { def period -> Integer.valueOf(period.order) }
        (allPeriodOrders - existPeriodOrders).each { order ->
            notFound404 = true
            if (isCalc) { // выводить только при расчете
                // 4. Проверка наличия принятой источника «Величины налоговых платежей, вводимые вручную» (предрасчетные проверки)
                logger.warn("Не найдена форма-источник «Величины налоговых платежей, вводимые вручную» в статусе «Принята»: Тип: \"%s/%s\", Период: \"%s %s\", Подразделение: \"%s\". Ячейки по графе «%s», заполняемые из данной формы, будут заполнены нулевым значением.",
                        FormDataKind.CONSOLIDATED.title, FormDataKind.PRIMARY.title, getPeriodName(order), reportPeriod.getTaxPeriod().getYear(), departmentService.get(formData.departmentId)?.name, getColumnName(row, alias))
            }
        }
        periods.each { period ->
            // берем консолидированную, если ее нет, то берем первичную (подразумевается, что форма одна, в 0.8 исправить на множество источников)
            def sourceForm = getSourceForm(FormDataKind.CONSOLIDATED, period.id)
            if (sourceForm == null) {
                sourceForm = getSourceForm(FormDataKind.PRIMARY, period.id)
            }
            if (sourceForm != null) {
                sum += (sourceForm?.allSaved?.get(2)?.sum ?: 0)
            } else {
                notFound404 = true
                if (isCalc) { // выводить только при расчете
                    // 4. Проверка наличия принятой источника «Величины налоговых платежей, вводимые вручную» (предрасчетные проверки)
                    logger.warn("Не найдена форма-источник «Величины налоговых платежей, вводимые вручную» в статусе «Принята»: Тип: \"%s/%s\", Период: \"%s %s\", Подразделение: \"%s\". Ячейки по графе «%s», заполняемые из данной формы, будут заполнены нулевым значением.",
                            FormDataKind.CONSOLIDATED.title, FormDataKind.PRIMARY.title, period.getName(), period.getTaxPeriod().getYear(), departmentService.get(formData.departmentId)?.name, getColumnName(row, alias))
                }
            }
        }
    }
    return notFound404 ? BigDecimal.ZERO : sum
}

def getPeriodName(def order) {
    switch (order) {
        case 1:
            return "первый квартал"
            break
        case 2:
            return "второй квартал"
            break
        case 3:
            return "третий квартал"
            break
        case 4:
            return "четвертый квартал"
            break
    }
}

def getSourceForm(def formDataKind, def periodId) {
    def source = formDataService.getLast(sourceFormTypeId, formDataKind, formData.departmentId, periodId, formData.periodOrder, null, false)
    if (source != null && source.state == WorkflowState.ACCEPTED) {
        return formDataService.getDataRowHelper(source)
    }
    return null
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

boolean isBank() {
    return formData.departmentId == 1 // по ЧТЗ
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][1]): getColumnName(tmpRow, 'taxName')]),
            ([(headerRows[0][2]): ('Период сравнения, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[0][3]): ('Период, ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[0][4]): 'Изменение за период']),
            ([(headerRows[1][4]): ('(гр.4-гр.3), ' + (isBank() ? 'млн. руб.' : 'тыс. руб.'))]),
            ([(headerRows[1][5]): '(гр.5/гр.3*100),%'])
    ]
    (0..5).each { index ->
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
    def tmpValues = formData.createStoreMessagingDataRow()

    // графа 1
    def colIndex = 0
    tmpValues.rowNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2
    colIndex++
    tmpValues.taxName = values[colIndex]

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
    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind,
            getStartDate(formData.reportPeriodId), getEndDate(formData.reportPeriodId))) {
        if (formDataSource.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                // суммируем 3, 4-ую графу из источников
                dataRows.each { row ->
                    def sourceRow = getDataRow(sourceRows, row.getAlias())
                    ['comparePeriod', 'currentPeriod'].each { column ->
                        row[column] = (row[column] ?: BigDecimal.ZERO) + ((formDataSource.departmentId == 1) ? 1000 : 1) * (sourceRow[column] ?: BigDecimal.ZERO)
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