package form_template.income.outcome_simple_1.v2016

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "Расходы, учитываемые в простых РНУ (расходы простые)"
 * formTypeId=310
 * formTemplateId=1310
 *
 * графа  1 - consumptionTypeId
 * графа  2 - consumptionGroup
 * графа  3 - consumptionTypeByOperation
 * графа  4 - consumptionAccountNumber
 * графа  5 - rnu7Field10Sum
 * графа  6 - rnu7Field12Accepted
 * графа  7 - rnu7Field12PrevTaxPeriod
 * графа  8 - rnu5Field5Accepted
 * графа  9 - logicalCheck
 * графа 10 - accountingRecords
 * графа 11 - opuSumByEnclosure2
 * графа 12 - opuSumByTableP
 * графа 13 - opuSumTotal
 * графа 14 - explanation
 * графа 15 - difference
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED: // Принять из "Создано"
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
}

// Кэш id записей справочника
@Field
def recordCache = [:]
@Field
def refBookCache = [:]
@Field
def formDataCache = [:]

@Field
def allColumns = ['consumptionTypeId', 'consumptionGroup', 'consumptionTypeByOperation', 'consumptionAccountNumber',
                  'rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted',
                  'logicalCheck', 'accountingRecords', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'explanation', 'difference']

@Field
def nonEmptyColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']

@Field
def calcColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted',
                    'logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'explanation', 'difference']

@Field
def controlColumns = ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'explanation', 'difference']

@Field
def totalColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']

@Field
def formTypeId_RNU7 = 311

@Field
def formTypeId_RNU5 = 317

@Field
def formTypeId_Tab2 = 852

@Field
def head1Alias = 'R1'
@Field
def first1Alias = 'R2'
@Field
def last1Alias = 'R100'
@Field
def total1Alias = 'R101'
@Field
def head2Alias = 'R102'
@Field
def first2Alias = 'R103'
@Field
def last2Alias = 'R379'
@Field
def total2Alias = 'R380'
@Field
def exceptAlias = 'R39'

@Field
def rowsNotCalc = [head1Alias, total1Alias, head2Alias, total2Alias]

@Field
def chRows = ['R20', 'R23', 'R41', 'R42']

@Field
def rows5679 = [3, 10, 11] + (14..35) + [37] + (43..54) + (57..60) + [62] + (64..66) + [78, 93] + (95..98) + (330..335) + [373] + (375..379)

@Field
def rows8 = (2..100) + (103..379)

@Field
def editableStyle = 'Редактирование (светло-голубой)'

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Получение Id записи из справочника 27 с использованием кэширования
def getRecordId(String knu, String accountNo, Date date) {
    def ref_id = 27
    String filter = getFilterForRefbook27(knu, accountNo)
    if (recordCache[ref_id] != null) {
        if (recordCache[ref_id][filter] != null) {
            return recordCache[ref_id][filter]
        }
    } else {
        recordCache[ref_id] = [:]
    }
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    if (records.size() == 1) {
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    }
    return null
}

// Метод заполняющий в кэш все записи для разыменывавания из справочника 27
void fillRecordsMap(List<String> values, Date date) {
    def ref_id = 27
    def filterList = values.collect {
        "LOWER(CODE) = LOWER('$it')"
    }
    def filter = filterList.join(" OR ")
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    records.each { record ->
        filter = getFilterForRefbook27(record['CODE'].value, record['NUMBER'].value)
        if (recordCache[ref_id] == null) {
            recordCache[ref_id] = [:]
        }
        recordCache[ref_id][filter] = record.get(RefBook.RECORD_ID_ALIAS).numberValue
    }
}

/**
 * Получить фильтр для поиска в справочнике 27.
 *
 * @param knu код налогового учета (графа 1 сводной)
 * @param number балансовый счёт по учёту дохода (графа 4 сводной)
 */
def getFilterForRefbook27(def knu, def number) {
    def tmpNumber = number?.replace('.', '')
    return (knu ? "LOWER(CODE) = LOWER('$knu')" : "CODE is null") + (tmpNumber ? " and LOWER(NUMBER) = LOWER('$tmpNumber')" : "and NUMBER is null")
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    calculationBasicSum(dataRows)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        calculationControlGraphs(dataRows)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def rowIndexes101 = []
    def rowIndexes102 = []
    for (row in dataRows) {
        //пропускаем строки где нет 10-й графы
        if (!row.accountingRecords) {
            continue
        }
        if (row.getAlias() in chRows) {
            def income101Records = getIncome101Data(row)
            if (!income101Records || income101Records.isEmpty()) {
                rowIndexes101 += row.getIndex()
            }
        }
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            if (row.consumptionAccountNumber && row.accountingRecords) {
                def income102Records = getIncome102Data(row)
                if (!income102Records || income102Records.isEmpty()) {
                    rowIndexes102 += row.getIndex()
                }
            }
        }
    }
    if (!rowIndexes101.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes101.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Оборотная ведомость\"")
    }
    if (!rowIndexes102.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes102.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Отчет о прибылях и убытках\"")
    }

    for (def row : dataRows) {
        def columns = []
        nonEmptyColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle) {
                columns.add(alias)
            }
        }
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    def row50001 = getDataRow(dataRows, total1Alias)
    def row50002 = getDataRow(dataRows, total2Alias)
    def need50001 = [:]
    def need50002 = [:]
    totalColumns.each{ alias ->
        need50001[alias] = getSum(dataRows, alias, first1Alias, last1Alias)
        need50002[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }
    checkTotalSum(row50001, need50001)
    checkTotalSum(row50002, need50002)
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(), getReportPeriodStartDate(), getReportPeriodEndDate())
    def isFromSummary = isFromSummary(formSources)
    isFromSummary ? consolidationFromSummary(dataRows, formSources) : consolidationFromPrimary(dataRows, formSources)
    calcExplanation(dataRows, formSources, isFromSummary)
}

boolean isFromSummary(def formSources) {
    def isSummarySource = formSources.find { it.formTypeId == formData.formType.id } != null
    def isPrimarySource = formSources.find { it.formTypeId in [formTypeId_RNU7, formTypeId_RNU5] } != null
    if (isSummarySource && isPrimarySource) {
        logger.warn("Для текущей формы назначены формы-источники по двум видам консолидации: 1. «РНУ-5», «РНУ-7»; 2. «Расходы, учитываемые в простых РНУ». Консолидация выполнена из форм-источников «Расходы, учитываемые в простых РНУ».")
        return true
    } else if (isSummarySource || isPrimarySource) {
        return isSummarySource
    } else {
        logger.warn("Неверно настроены источники формы \"%s\" для подразделения \"%s\"! Не указаны в качестве источников корректные сводные или первичные налоговые формы.",
                formData.formType.name, formDataDepartment.name)
        return true
    }
}

void calculationBasicSum(def dataRows) {
    def row50001 = getDataRow(dataRows, total1Alias)
    def row50002 = getDataRow(dataRows, total2Alias)

    // суммы для графы 5..8
    totalColumns.each { alias ->
        row50001[alias] = getSum(dataRows, alias, first1Alias, last1Alias)
        row50002[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }
}

/**
 * Скрипт для заполнения контрольных полей.
 * В текущей таблице нет 10й графы, следственно нужно учесть что графы > 10 считаются "-1"
 */
void calculationControlGraphs(def dataRows) {
    def message = 'ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ'
    def tmp
    def value
    // для графы 12
    def map12 = [:]
    dataRows.each { row ->
        if (!(row.getAlias() in rowsNotCalc)) {
            if (map12[row.consumptionAccountNumber] == null) {
                map12[row.consumptionAccountNumber] = BigDecimal.ZERO
            }
            map12[row.consumptionAccountNumber] += (row.rnu5Field5Accepted ?: 0)
        }
    }
    for (def row : dataRows) {
        // исключить итоговые строки и пять конечных и 38-ую
        if (row.getAlias() in rowsNotCalc) {
            continue
        }
        if (rows5679.contains(row.getAlias().replace('R', '') as Integer)) {
            // графы 9 = ОКРУГЛ(«графа 5» - («графа 6» - «графа 7»); 2)
            tmp = round((row.rnu7Field10Sum ?: 0) - (row.rnu7Field12Accepted ?: 0) + (row.rnu7Field12PrevTaxPeriod ?: 0), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }
        // графа 11
        row.opuSumByEnclosure2 = BigDecimal.ZERO

        // графа 12
        if (!(row.getAlias() in rowsNotCalc)) {
            row.opuSumByTableP = map12[row.consumptionAccountNumber]
        }

        // графа 13
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            if (row.consumptionAccountNumber && row.accountingRecords) {
                row.opuSumTotal = BigDecimal.ZERO
                def income102Records = getIncome102Data(row)
                for (income102 in income102Records) {
                    row.opuSumTotal += income102.TOTAL_SUM.numberValue
                }
            } else {
                row.opuSumTotal = null
            }
        }
        if (row.getAlias() in chRows) {
            row.opuSumTotal = BigDecimal.ZERO
            def income101Records = getIncome101Data(row)
            for (income101 in income101Records) {
                row.opuSumTotal += income101.DEBET_RATE.numberValue
            }
        }

        // графа 15
        row.difference = (row.opuSumByEnclosure2?:0) + (row.opuSumByTableP?:0) - (row.opuSumTotal?:0) - (row.explanation ?: 0)
    }
}

/** Скрипт для консолидации данных из сводных расходов простых уровня ОП в сводные уровня банка. */
def consolidationFromSummary(def dataRows, def formSources) {

    // очистить форму
    dataRows.each { row ->
        calcColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
                row.getCell(alias).setValue(0, row.getIndex())
            }
        }
        controlColumns.each { alias ->
            row[alias] = null
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    formSources.each { departmentFormType ->
        def child = getFormData(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            def childData = formDataService.getDataRowHelper(child)
            for (DataRow<Cell> row : childData.allSaved) {
                if (row.getAlias() == null) {
                    continue
                }
                DataRow<Cell> rowResult = getDataRow(dataRows, row.getAlias())
                for (alias in calcColumns) {
                    if (rowResult.getCell(alias)?.editable && row[alias] != null) {
                        rowResult[alias] = summ(rowResult.getCell(alias), row.getCell(alias))
                    }
                }
            }
        }
    }
}

/** Консолидация данных из рну-7 и рну-5 в сводные расходы простые уровня ОП. */
void consolidationFromPrimary(def dataRows, def formSources) {

    // очистить форму
    dataRows.each { row ->
        calcColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
                row[alias] = 0
            }
        }
        controlColumns.each { alias ->
            row[alias] = null
        }
    }

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // Предыдущий отчётный период
    def dataRowsOld = null
    if (reportPeriod != null && reportPeriod.order != 1) {
        def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
        if (prevReportPeriod != null) {
            def formDataOld = getFormData(formData.getFormType().getId(), formData.getKind(), formDataDepartment.id, prevReportPeriod.getId(), formData.periodOrder)
            if (formDataOld == null || formDataOld.state != WorkflowState.ACCEPTED) {
                if (prevReportPeriod != null) {
                    // Последний экземпляр
                    formDataOld = getFormData(304, formData.getKind(), formData.getDepartmentId(), prevReportPeriod.getId(), null);
                }
            }
            dataRowsOld = ((formDataOld != null && formDataOld.state == WorkflowState.ACCEPTED) ? formDataService.getDataRowHelper(formDataOld)?.allSaved : null)
            if (dataRowsOld != null) {
                // данные за предыдущий отчетный период рну-7
                rows5679.each {
                    def row = getDataRow(dataRows, 'R' + it)
                    def rowOld = dataRowsOld.find { row.consumptionTypeId == it.consumptionTypeId && row.consumptionAccountNumber == it.consumptionAccountNumber }
                    if (rowOld) {
                        // графа 5
                        row.rnu7Field10Sum = rowOld.rnu7Field10Sum
                        // графа 6
                        row.rnu7Field12Accepted = rowOld.rnu7Field12Accepted
                        // графа 7 заполняется вручную
                    }
                }
                // данные за предыдущий отчетный период рну-5
                rows8.each {
                    def row = getDataRow(dataRows, 'R' + it)
                    def rowOld = dataRowsOld.find { row.consumptionTypeId == it.consumptionTypeId && row.consumptionAccountNumber == it.consumptionAccountNumber }
                    if (rowOld) {
                        // графа 8
                        row.rnu5Field5Accepted = rowOld.rnu5Field5Accepted
                    }
                }
            }
        }
    }

    // Прошел по строкам и получил список кну
    def knuList = rows8.collect{
        def row = getDataRow(dataRows, 'R' + it)
        return row.consumptionTypeId
    }
    fillRecordsMap(knuList, getReportPeriodEndDate())

    def strangeCodesRnu5 = [] as SortedSet<String>
    def strangeCodesRnu7 = [] as SortedSet<String>
    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    formSources.each {
        def child = getFormData(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def dataRowsChild = formDataService.getDataRowHelper(child)?.allSaved
            switch (child.formType.id) {
                // рну 7
                case formTypeId_RNU7:
                    fillFromRnu7(dataRows, dataRowsChild, strangeCodesRnu7)
                    break

                // рну 5
                case formTypeId_RNU5:
                    fillFromRnu5(dataRows, dataRowsChild, strangeCodesRnu5)
                    break
            }
        }
    }
    if (!strangeCodesRnu7.isEmpty()) {
        logger.warn("По строкам с КНУ %s не совпадает номер балансового счета текущей формы и формы-источника «%s»!",
                strangeCodesRnu7.join(', '), formTypeService.get(formTypeId_RNU7).name)
    }
    if (!strangeCodesRnu5.isEmpty()) {
        logger.warn("По строкам с КНУ %s не совпадает номер балансового счета текущей формы и формы-источника «%s»!",
                strangeCodesRnu5.join(', '), formTypeService.get(formTypeId_RNU5).name)
    }
}

void fillFromRnu7(def dataRows, def dataRowsChild, def strangeCodes) {
    def codeBalanceMap = [:]
    def codeMap = [:]
    for (rowRnu7 in dataRowsChild) {
        if (rowRnu7.getAlias() == null) {
            // для проверки кодов
            def map = getRefBookValue(27, rowRnu7.code)
            def codeKey = map.CODE.value
            def balanceKey = map.NUMBER.value?.replace('.', '') ?: ''
            if (codeMap[codeKey] == null) {
                codeMap[codeKey] = []
            }
            codeMap[codeKey].add(balanceKey)

            // ключ состоит из id записи кну и балансового счета
            String key = String.valueOf(rowRnu7.code)
            if (codeBalanceMap[key] == null) {
                codeBalanceMap[key] = ["sum5": 0, "sum6": 0]
            }
            //«графа 5» = сумма графы 10 рну-7
            codeBalanceMap[key].sum5 += (rowRnu7.taxAccountingRuble ?: 0)
            //«графа 6» = сумма графы 12 рну-7
            codeBalanceMap[key].sum6 += (rowRnu7.ruble ?: 0)
            // графа 7 больше не берется из источников
        }
    }
    rows5679.each { rowNum ->
        def row = getDataRow(dataRows, "R$rowNum")
        def knu = row.consumptionTypeId
        def balanceKey = row.consumptionAccountNumber?.replace('.', "") ?: ''
        // удаляем использованные коды
        codeMap.get(knu)?.remove(balanceKey)
        // если остались строки источника с тем же кну, но с другим балансовым счетом, то выводим сообщение
        if (codeMap.get(knu) != null && !(codeMap.get(knu).isEmpty())) {
            strangeCodes.add(knu)
        }
        def recordId = getRecordId(knu, balanceKey, getReportPeriodEndDate())

        if (recordId != null) {
            def sums = codeBalanceMap[String.valueOf(recordId)]
            if (sums != null) {
                row.rnu7Field10Sum = (row.rnu7Field10Sum ?: 0) + (sums.sum5 ?: 0)
                row.rnu7Field12Accepted = (row.rnu7Field12Accepted ?: 0) + (sums.sum6 ?: 0)
                // графа 7 заполняется вручную
            }
        }
    }
    codeMap.clear()
    codeBalanceMap.clear()
}

void fillFromRnu5(def dataRows, def dataRowsChild, def strangeCodes) {
    def codeMap = [:]
    for (rowRnu5 in dataRowsChild) {
        if (rowRnu5.getAlias() != null) {
            continue
        }
        def map = getRefBookValue(27, rowRnu5.number)
        def codeKey = map.CODE.value
        def balanceKey = map.NUMBER.value?.replace('.', '')
        if (codeMap[codeKey] == null) {
            codeMap[codeKey] = [:]
        }
        if (codeMap[codeKey][balanceKey] == null) {
            codeMap[codeKey][balanceKey] = []
        }
        codeMap[codeKey][balanceKey].add(rowRnu5)
    }
    rows8.each { rowNum ->
        def row = getDataRow(dataRows, "R$rowNum")
        String knu = row.consumptionTypeId
        String balanceKey = row.consumptionAccountNumber.replace('.', "")
        // получаем строки источника по КНУ и Балансовому счету
        Map balanceMap = codeMap.get(knu)
        // считаем
        def rowsChild = balanceMap?.get(balanceKey) ?: []
        rowsChild.each { rowChild ->
            row.rnu5Field5Accepted = (row.rnu5Field5Accepted ?: BigDecimal.ZERO) + rowChild.sum
        }
        // удаляем использованные строки источника
        balanceMap?.remove(balanceKey)
        // если остались строки источника с тем же кну, но с другим балансовым счетом, то выводим сообщение
        if (balanceMap != null && !(balanceMap.isEmpty())) {
            strangeCodes.add(knu)
        }
    }
}

void calcExplanation(def dataRows, def formSources, def isFromSummary) {
    def sourcesTab = formSources.findAll { it.formTypeId == formTypeId_Tab2 }
    def rowNumbers = []
    def formDataTab
    if (sourcesTab != null && !sourcesTab.isEmpty()) {
        for (sourceTab in sourcesTab) {
            def tempFormData = formDataService.getLast(sourceTab.formTypeId, sourceTab.kind, sourceTab.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            // один принятый(для ТБ) источник
            if (tempFormData != null && (isFromSummary || tempFormData.state == WorkflowState.ACCEPTED)) {
                formDataTab = tempFormData
                break
            }
        }
    }
    def codeMap = [:]
    if (formDataTab != null) {
        def dataRowHelper = formDataService.getDataRowHelper(formDataTab)
        def sourceRows = dataRowHelper.allSaved
        sourceRows.each { row ->
            def map = getRefBookValue(27, row.code)
            def code = map.CODE?.value ?: ''
            def opu = map.OPU?.value ?: ''
            // одна строка с КНУ
            if (codeMap[code] == null) {
                codeMap[code] = [:]
            }
            if (codeMap[code][opu] == null) {
                codeMap[code][opu] = []
            }
            codeMap[code][opu].add(row)
        }
    }
    def strangeCodes = []
    for (row in dataRows) {
        if (!(row.getAlias() in rowsNotCalc)) {
            def knu = row.consumptionTypeId
            def opuMap = codeMap.get(knu)
            def opuKey = row.accountingRecords ?: ''
            def sourceRows = opuMap?.get(opuKey)
            row.explanation = BigDecimal.ZERO
            if (sourceRows != null && !(sourceRows.isEmpty())) {
                sourceRows.each { sourceRow ->
                    row.explanation += sourceRow.sum
                }
            } else {
                rowNumbers.add(row.getIndex())
            }
            // удаляем использованные строки источника
            opuMap?.remove(opuKey)
            // если остались строки источника с тем же кну, но с другим балансовым счетом, то выводим сообщение
            if (opuMap != null && !(opuMap.isEmpty())) {
                strangeCodes.add(knu)
            }
        }
    }
    if (!rowNumbers.isEmpty()) {
        logger.warn("Строки %s: Графа «%s» заполнена значением «0», т.к. не найдены строки по требуемым КНУ в форме-источнике «%s»!",
                rowNumbers.join(', '), getColumnName(dataRows[0], 'explanation'), formTypeService.get(formTypeId_Tab2).name)
    }
    if (!strangeCodes.isEmpty()) {
        logger.warn("По строкам с КНУ %s не совпадает номер балансового счета текущей формы и формы-источника «%s»!",
                strangeCodes.join(', '), formTypeService.get(formTypeId_Tab2).name)
    }
}

FormData getFormData(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder) {
    String key = "$formTypeId#${kind.id}#$departmentId#$reportPeriodId#$periodOrder"
    if (formDataCache[key] != -1 && formDataCache[key] == null){ // чтобы повторно не искал несуществующие формы
        formDataCache[key] = formDataService.getLast(formTypeId, kind, departmentId, reportPeriodId, periodOrder, formData.comparativePeriodId, formData.accruing) ?: -1
    }
    return (formDataCache[key] != -1) ? formDataCache[key] : null
}

/** Получить сумму диапазона строк определенного столбца. */
def getSum(def dataRows, String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = getDataRow(dataRows, rowFromAlias).getIndex() - 1
    def to = getDataRow(dataRows, rowToAlias).getIndex() - 1
    if (from > to) {
        return 0
    }
    return ((BigDecimal)summ(formData, dataRows, new ColumnRange(columnAlias, from, to))).setScale(2, BigDecimal.ROUND_HALF_UP)
}

/**
 * Получить сумму значений из расходов сложных.
 * @param dataRowsComplex данные формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromComplex(dataRowsComplex, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (dataRowsComplex != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : dataRowsComplex) {
            if (row[columnAliasCheck] == value) {
                sum += (row[columnAliasSum] ?: 0)
            }
        }
    }
    return sum
}

def getBalanceValue(def value) {
    return formDataService.getRefBookValue(27, value, refBookCache)?.NUMBER?.stringValue
}

boolean isEqualNum(String accNum, def balance) {
    def a = accNum?.replace('.', '')
    def b = (balance ? getBalanceValue(balance)?.replace('.', '') : null)
    return a == b
}

// Возвращает данные из Оборотной Ведомости за период, для которого сформирована текущая форма
def getIncome101Data(def row) {
    // Справочник 50 - "Оборотная ведомость (Форма 0409101-СБ)"
    return bookerStatementService.getRecords(50L, formData.departmentId, getReportPeriodEndDate(), "ACCOUNT = '${row.accountingRecords}'")
}

// Возвращает данные из Отчета о прибылях и убытках за период, для которого сформирована текущая форма
def getIncome102Data(def row) {
    // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
    return bookerStatementService.getRecords(52L, formData.departmentId, getReportPeriodEndDate(), "OPU_CODE = '${row.accountingRecords}'")
}

void checkTotalSum(totalRow, needRow){
    def errorColumns = []
    totalColumns.each { totalColumn ->
        if (totalRow[totalColumn] != needRow[totalColumn]) {
            errorColumns += "\"" + getColumnName(totalRow, totalColumn) + "\""
        }
    }
    if (!errorColumns.isEmpty()){
        logger.error("Итоговое значение в строке ${totalRow.getIndex()} рассчитано неверно в графах ${errorColumns.join(", ")}!")
    }
}

void importData() {
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'КНУ'
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

    // формирвание строк нф
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
        def alias = "R" + rowIndex
        def dataRow = getDataRow(dataRows, alias)
        // заполнить строку нф значениями из эксель
        if (alias in [total1Alias, total2Alias]) {
            // итоги
            fillTotalRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
        } else {
            // остальные строки
            fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
        }
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }

    // сравнение итогов
    def row50001Tmp = formData.createStoreMessagingDataRow()
    def row50002Tmp = formData.createStoreMessagingDataRow()
    totalColumns.each { alias ->
        row50001Tmp[alias] = getSum(dataRows, alias, first1Alias, last1Alias)
        row50002Tmp[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }
    def row50001 = getDataRow(dataRows, total1Alias)
    def row50002 = getDataRow(dataRows, total2Alias)
    compareTotalValues(row50001, row50001Tmp, totalColumns, logger, false)
    compareTotalValues(row50002, row50002Tmp, totalColumns, logger, false)

    showMessages(dataRows, logger)
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
    checkHeaderSize(headerRows[0].size(), headerRows.size(), 8, 3)
    def headerMapping = [
            ([(headerRows[0][0]): 'КНУ']),
            ([(headerRows[0][1]): 'Группа расхода']),
            ([(headerRows[0][2]): 'Вид расхода по операциям']),
            ([(headerRows[0][3]): 'Балансовый счёт по учёту расхода']),
            ([(headerRows[0][4]): 'РНУ-7 (графа 10) сумма']),
            ([(headerRows[0][5]): 'РНУ-7 (графа 12) сумма']),
            ([(headerRows[0][7]): 'РНУ-5 (графа 5) сумма']),
            ([(headerRows[1][5]): 'сумма']),
            ([(headerRows[1][6]): 'в т.ч. учтено в предыдущих налоговых периодах по графе 10'])
    ]
    (0..7).each { index ->
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
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

    def knu = normalize(getOwnerValue(dataRow, 'consumptionTypeId'))
    def group = normalize(getOwnerValue(dataRow, 'consumptionGroup'))
    def type = normalize(getOwnerValue(dataRow, 'consumptionTypeByOperation'))
    def num = normalize(getOwnerValue(dataRow, 'consumptionAccountNumber'))

    def colIndex = 0
    def knuImport = normalize(values[colIndex])

    colIndex++
    def groupImport = normalize(values[colIndex])

    colIndex++
    def typeImport = normalize(values[colIndex])

    colIndex++
    def numImport = normalize(values[colIndex])

    //если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
    //то продолжаем обработку строки иначе пропускаем строку
    if (!((knu == knuImport && group == groupImport && type == typeImport && num == numImport) ||
            ((!knuImport.isEmpty() || !groupImport.isEmpty() || !typeImport.isEmpty() || !numImport.isEmpty()) &&
                    (knu.contains(knuImport) && group.contains(groupImport) && type.contains(typeImport) && num.contains(numImport))))) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu.")
        return
    }

    // графа 5..8
    ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
        colIndex++
        if (dataRow.getCell(alias).isEditable()) {
            dataRow[alias] = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
        }
    }
}

/**
 * Заполняет итоговую строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillTotalRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    // графа 5..8
    def colIndex = 3
    totalColumns.each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }
}