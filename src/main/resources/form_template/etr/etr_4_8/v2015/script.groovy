package form_template.etr.etr_4_8.v2015

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Приложение 4-8. Налоговая эффективность по уступке прав требования по проблемным активам
 * formTemplateId = 708
 *
 * @author bkinzyabulatov
 *
 * графа 1 - rowNum         - № строки
 * графа 2 - taxName        - Наименование показателя
 * графа 3 - symbol102      - символ формы 102/графа РНУ
 * графа 4 - comparePeriod  - Период сравнения, тыс.руб.
 * графа 5 - currentPeriod  - Период, тыс.руб.
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
def recordCache = [:]

@Field
def refBookCache = [:]

@Field // Сводная форма начисленных расходов уровня обособленного подразделения
int sourceFormTypeId = 303

@Field
def allColumns = ['rowNum', 'taxName', 'symbol102', 'comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

@Field
def calcColumns = ['comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

@Field
def nonEmptyColumns = ['comparePeriod', 'currentPeriod', 'deltaRub', 'deltaPercent']

@Field
def check102Columns = ['comparePeriod', 'currentPeriod']

@Field
def checkCalcColumns = ['deltaRub', 'deltaPercent']

@Field
def opuMap = ['R1' : ['26307.02', '22204']]

@Field
def knuMap = ['R2' : '21490', 'R3' : '21510']

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
    ['comparePeriod' : getComparativePeriodId(), 'currentPeriod' : formData.reportPeriodId].each { alias, periodId ->
        def reportPeriod = getReportPeriod(periodId)
        if (!formData.accruing && reportPeriod.order != 1) {
            def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
            checkOpuCodes(alias, date, opuCodes, tmpRow)
            checkOutcome(alias, date, tmpRow, reportPeriod?.order - 1)
        }
        def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
        checkOpuCodes(alias, date, opuCodes, tmpRow)
        checkOutcome(alias, date, tmpRow, reportPeriod?.order)
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

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
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

@Field
def formDataMap = [:]

void checkOutcome(def alias, def date, def tmpRow, def etrPeriodOrder) {
    def periodList = getIncomePeriodList(date)
    if (periodList.size() == 1) {
        def reportPeriod = periodList[0]
        // Проверка наличия принятой формы «Сводная форма начисленных расходов уровня обособленного подразделения»
        def incomeFormData = getIncomeFormData(date)
        if (incomeFormData == null) {
            logger.warn("Не найдена «Сводная форма начисленных расходов уровня обособленного подразделения в статусе «Принята»: Тип:%s, Период: %s %s, Подразделение: %s. Ячейки по графе «%s», заполняемые из данной формы, будут заполнены нулевым значением.",
                    FormDataKind.SUMMARY.title, reportPeriod.name, date.format('yyyy'), formDataDepartment.name, getColumnName(tmpRow, alias))
        }
    } else if (periodList.size() == 0) {
        logger.warn("Не найдена «Сводная форма начисленных расходов уровня обособленного подразделения в статусе «Принята»: Тип:%s, Период: %s %s, Подразделение: %s. Ячейки по графе «%s», заполняемые из данной формы, будут заполнены нулевым значением.",
                FormDataKind.SUMMARY.title, getPeriodName(etrPeriodOrder), date.format('yyyy'), formDataDepartment.name, getColumnName(tmpRow, alias))
    }
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

FormData getIncomeFormData(def date) {
    def dateKey = date?.format('dd.MM.yyyy')
    if (formDataMap[dateKey] == null && formDataMap[dateKey] != -1) {
        def periodList = getIncomePeriodList(date)
        if (periodList.size() == 1) {
            def period = periodList[0]
            // Проверка наличия принятой формы «Сводная форма начисленных расходов уровня обособленного подразделения»
            def incomeFormData = formDataService.getLast(sourceFormTypeId, FormDataKind.SUMMARY, formData.departmentId,  period.id, null, null, false)
            if (incomeFormData != null && incomeFormData.state == WorkflowState.ACCEPTED) {
                formDataMap[dateKey] = incomeFormData
                return incomeFormData
            }
        }
        formDataMap[dateKey] = -1
        return null
    } else if (formDataMap[dateKey] == -1) {
        return null
    } else {
        return formDataMap[dateKey]
    }
}

@Field
def periodListMap = [:]

List<ReportPeriod> getIncomePeriodList(def date) {
    def dateKey = date?.format('dd.MM.yyyy')
    if (periodListMap[dateKey] == null) {
        periodListMap[dateKey] = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, date, date)
    }
    return periodListMap[dateKey]
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    calcValues(dataRows, dataRows, true)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for(int i = 0; i < dataRows.size(); i++){
        if (i == 3) { // пропускаем четвертую строку
            continue
        }
        checkNonEmptyColumns(dataRows[i], dataRows[i].getIndex(), nonEmptyColumns, logger, true)
    }
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def tempRows = formTemplate.rows
    updateIndexes(tempRows)
    calcValues(tempRows, dataRows, false)
    for(int i = 0; i < dataRows.size(); i++){
        if (i == 3) { // пропускаем четвертую строку
            continue
        }
        def row = dataRows[i]
        def tempRow = tempRows[i]
        def checkColumns = []
        // делаем проверку для первичных НФ или расчетных ячеек
        if ((formData.kind != FormDataKind.CONSOLIDATED) || !(opuMap.keySet() + knuMap.keySet()).contains(row.getAlias())) {
            checkColumns += check102Columns
        }
        checkColumns += checkCalcColumns
        checkCalc(row, checkColumns, tempRow, logger, true)
    }
}

void calcValues(def dataRows, def sourceRows, boolean isCalc) {
    // при консолидации не подтягиваем данные при расчете
    for (def row in dataRows) {
        def rowSource = getDataRow(sourceRows, row.getAlias())
        switch (row.getAlias()) {
            case 'R1':
                if (formData.kind != FormDataKind.CONSOLIDATED) {
                    row.comparePeriod = calcBO(rowSource, getComparativePeriodId())
                    row.currentPeriod = calcBO(rowSource, formData.reportPeriodId)
                } else {
                    row.comparePeriod = rowSource.comparePeriod
                    row.currentPeriod = rowSource.currentPeriod
                }
                break
            case 'R2':
            case 'R3':
                if (formData.kind != FormDataKind.CONSOLIDATED) {
                    row.comparePeriod = getSourceValue(getComparativePeriodId(), row)
                    row.currentPeriod = getSourceValue(formData.reportPeriodId, row)
                } else {
                    row.comparePeriod = rowSource.comparePeriod
                    row.currentPeriod = rowSource.currentPeriod
                }
                break
            case 'R4':
                def row1Source = getDataRow(sourceRows, "R1")
                def row2Source = getDataRow(sourceRows, "R2")
                def row3Source = getDataRow(sourceRows, "R3")
                ['comparePeriod', 'currentPeriod'].each {
                    row[it] = ((row1Source[it] ?: BigDecimal.ZERO) - ((row2Source[it] ?: BigDecimal.ZERO) + (row3Source[it] ?: BigDecimal.ZERO))) * 0.2
                }
                break
        }
    }
    for (int i = 1; i <= 4; i++) {
        def row = getDataRow(dataRows, "R$i")
        def rowSource = getDataRow(sourceRows, "R$i")
        row.deltaRub = (rowSource.currentPeriod ?: 0) - (rowSource.comparePeriod ?: 0)
        row.deltaPercent = null
        if (rowSource.comparePeriod) {
            row.deltaPercent = ((rowSource.deltaRub ?: BigDecimal.ZERO) as BigDecimal) * 100 / rowSource.comparePeriod.doubleValue()
        } else {
            row.deltaPercent = 0
            if (!isCalc) { // выводим при проверках и в проверках после расчета
                rowWarning(logger, row, String.format("Строка %s: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                        row.getIndex(), getColumnName(row, 'deltaPercent')))
            }
        }
    }
}

def getSourceValue(def periodId, def row) {
    def knuSum = BigDecimal.ZERO
    if (periodId != null) {
        def reportPeriod = getReportPeriod(periodId)
        // если не нарастающий итог, то, кроме первого квартала, надо вычитать из текущего квартала предыдущий
        def date = getEndDate(periodId)
        def pair = getKnuSumPair(date, row)
        def isCorrect = pair[1]
        knuSum = isCorrect ? pair[0] : 0
        if (!formData.accruing && reportPeriod.order != 1 && isCorrect) {
            def prevDate = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
            def periodList = getIncomePeriodList(prevDate)
            if (periodList.size() == 1) {
                pair = getKnuSumPair(prevDate, row)
                isCorrect = pair[1]
                knuSum = isCorrect ? (knuSum - pair[0]) : BigDecimal.ZERO
            }
        }
    }
    return knuSum
}

def getKnuSumPair(def date, def row) {
    def incomeFormData = getIncomeFormData(date)
    if (incomeFormData == null) {
        return [BigDecimal.ZERO, false]
    }
    def dataRow = formDataService.getDataRowHelper(incomeFormData).allSaved.find{ it ->
        it.consumptionTypeId && knuMap[row.getAlias()] == it.consumptionTypeId
    }
    if (dataRow == null) {
        return [BigDecimal.ZERO, false]
    }
    return [(dataRow.consumptionTaxSumS ?: BigDecimal.ZERO) / 1000, true]
}

// Расчет сумм из БО за определенный период
def calcBO(def rowSource, def periodId) {
    def periodSum = BigDecimal.ZERO
    if (periodId != null) {
        def reportPeriod = getReportPeriod(periodId)
        def date = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order)
        def pair = get102Sum(rowSource, date)
        boolean isCorrect = pair[1]
        periodSum = isCorrect ? pair[0] : BigDecimal.ZERO
        if (!formData.accruing && reportPeriod.order != 1 && isCorrect) {
            def prevDate = getEndDate(reportPeriod?.taxPeriod?.year, reportPeriod?.order - 1)
            pair = get102Sum(rowSource, prevDate)
            isCorrect = pair[1]
            periodSum = isCorrect ? (periodSum - pair[0]) : BigDecimal.ZERO
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
        def result = records.sum { it.TOTAL_SUM.numberValue } / 1000
        return [result, true]
    }
    return [0, true]
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][1]): getColumnName(tmpRow, 'taxName')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'symbol102')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'comparePeriod')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'currentPeriod')]),
            ([(headerRows[0][5]): 'Изменение за период']),
            ([(headerRows[1][5]): '(гр.5-гр.4), тыс.руб.']),
            ([(headerRows[1][6]): '(гр.6/гр.4*100),%'])
    ]
    (0..6).each { index ->
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
    tmpValues.rowNum = values[colIndex]

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
            def source = formDataService.getLast(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceRows = formDataService.getDataRowHelper(source)?.allSaved
                // суммируем 4, 5-ую графу из источников для первых трех строк
                ['R1', 'R2', 'R3'].each { alias ->
                    def row = getDataRow(dataRows, alias)
                    def sourceRow = getDataRow(sourceRows, alias)
                    check102Columns.each { column ->
                        row[column] = (row[column] ?: 0) + (sourceRow[column] ?: 0)
                    }
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