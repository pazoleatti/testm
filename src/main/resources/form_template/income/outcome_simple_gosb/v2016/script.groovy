package form_template.income.outcome_simple_gosb.v2016

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "Расходы, учитываемые в простых РНУ (расходы простые) (с полугодия 2015)"
 * formTypeId=327
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
 * графа 14 - difference
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()//изменения откатываются, поэтому расчеты перенесены в CALCULATE
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
                  'logicalCheck', 'accountingRecords', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'difference']

@Field
def nonEmptyColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted',
                    'logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'difference']

@Field
def totalColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']

@Field
def formTypeId_RNU7 = 311

@Field
def formTypeId_RNU5 = 317

@Field
def formTypeId_summary = 310

@Field
def head1Alias = 'R1'
@Field
def first1Alias = 'R2'
@Field
def last1Alias = 'R111'
@Field
def total1Alias = 'R112'
@Field
def head2Alias = 'R113'
@Field
def first2Alias = 'R114'
@Field
def last2Alias = 'R218'
@Field
def total2Alias = 'R219'
@Field
def exceptAlias = 'R39'
@Field
def pairOne = ['R110', 'R216']
@Field
def pairTwo = ['R111', 'R218']
@Field
def pairThree = ['R109', 'R215']

@Field
def rows567 = [3, 12] + (15..38) + (42..53) + (55..58) + (60..62) + (66..82) + (95..100) + (103..106) + (108..111) + (187..189) + (196..200) + [205, 211, 212] + (214..218)

@Field
def rows8 = (2..111) + (114..218)

@Field
def rowsEnd = 220..224

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
    if (knu == '') {
        filter = "CODE is null"
    }
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
    def tmpNumber = number.replace('.', '')
    return "LOWER(CODE) = LOWER('$knu') and LOWER(NUMBER) = LOWER('$tmpNumber')"
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
    def rowIndexes102 = []
    for (row in dataRows) {
        //пропускаем строки где нет 10-й графы
        if (!row.accountingRecords) {
            continue
        }
        final income102Data = getIncome102Data(row)
        if (!income102Data || income102Data.isEmpty()) {
            rowIndexes102 += row.getIndex()
        }
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
        need50001[alias] = getSum(dataRows, alias, first1Alias, last1Alias) - (getDataRow(dataRows, exceptAlias).get(alias)?:0)
        need50002[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }
    checkTotalSum(row50001, need50001)
    checkTotalSum(row50002, need50002)
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.getId(), formData.kind, getReportPeriodStartDate(), getReportPeriodEndDate())
    def isFromSummary = isFromSummary(formSources)
    isFromSummary ? consolidationFromSummary(dataRows, formSources) : consolidationFromPrimary(dataRows, formSources)
    calculationBasicSum(dataRows)
}

boolean isFromSummary(def formSources) {
    def isSummarySource = formSources.find { it.formTypeId in [formData.formType.id, formTypeId_summary] } != null
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
        row50001[alias] = getSum(dataRows, alias, first1Alias, last1Alias) - (getDataRow(dataRows, exceptAlias).get(alias)?:0)
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
    def formDataComplex = getFormDataComplex()
    def dataRowsComplex = formDataComplex != null ? formDataService.getDataRowHelper(formDataComplex)?.allSaved : null
    for (def row : dataRows) {
        // исключить итоговые строки и пять конечных и 38-ую
        if (row.getAlias() in ([total1Alias, total2Alias, head1Alias, exceptAlias, head2Alias] + rowsEnd.collect{ "R$it" })) {
            continue
        }
        if (row.getCell('rnu7Field10Sum')?.style?.alias == editableStyle
                && row.getCell('rnu7Field12Accepted')?.style?.alias == editableStyle
                && row.getCell('rnu7Field12PrevTaxPeriod')?.style?.alias == editableStyle) {
            // графы 9 = ОКРУГЛ(«графа 5» - («графа 6» - «графа 7»); 2)
            tmp = round((row.rnu7Field10Sum ?: 0) - ((row.rnu7Field12Accepted ?: 0) - (row.rnu7Field12PrevTaxPeriod ?: 0)), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }
        // графа 11
        row.opuSumByEnclosure2 = getSumFromComplex(dataRowsComplex,
                'consumptionBuhSumAccountNumber', 'consumptionBuhSumAccepted', row.consumptionAccountNumber)
        // графа 12
        if (row.getAlias() in pairOne) {
            tmp = calcSum8(dataRows, pairOne)
        } else if (row.getAlias() in pairTwo) {
            tmp = calcSum8(dataRows, pairTwo)
        } else if (row.getAlias() in pairThree) {
            tmp = calcSum8(dataRows, pairThree)
        } else {
            tmp = row.rnu5Field5Accepted
        }
        row.opuSumByTableP = tmp

        // графа 13
        // получить отчет о прибылях и убытках
        def income102Records = getIncome102Data(row)
        row.opuSumTotal = 0
        for (income102 in income102Records) {
            row.opuSumTotal += income102.TOTAL_SUM.numberValue
        }

        // графа 14
        row.difference = (row.opuSumByEnclosure2?:0) + (row.opuSumByTableP?:0) - (row.opuSumTotal?:0)
    }
}

/** Скрипт для консолидации данных из сводных расходов простых уровня ОП в сводные уровня банка. */
def consolidationFromSummary(def dataRows, def formSources) {

    // очистить форму
    dataRows.each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
                row.getCell(alias).setValue(0, row.getIndex())
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    formSources.each { departmentFormType ->
        def child = getFormData(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id in [formData.formType.id, formTypeId_summary]) {
            def childData = formDataService.getDataRowHelper(child)
            for (DataRow<Cell> row : childData.allSaved) {
                if (row.getAlias() == null) {
                    continue
                }
                DataRow<Cell> rowResult = getDataRow(dataRows, row.getAlias())
                for (alias in ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']) {
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
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
                row[alias] = 0
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
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
            def formDataOld = getFormData(formData.formType.id, formData.kind, formDataDepartment.id, prevReportPeriod.id, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            dataRowsOld = (formDataOld ? formDataService.getDataRowHelper(formDataOld)?.allSaved : null)
            if (dataRowsOld != null) {
                // данные за предыдущий отчетный период рну-7
                rows567.each {
                    def alias = 'R' + it
                    def row = getDataRow(dataRows, alias)
                    def rowOld = dataRowsOld.find { row.consumptionTypeId == it.consumptionTypeId && row.consumptionAccountNumber == it.consumptionAccountNumber }
                    if (rowOld) {
                        // графа 5
                        row.rnu7Field10Sum = rowOld.rnu7Field10Sum
                        // графа 6
                        row.rnu7Field12Accepted = rowOld.rnu7Field12Accepted
                        // графа 7
                        row.rnu7Field12PrevTaxPeriod = rowOld.rnu7Field12PrevTaxPeriod
                    }
                }
                // данные за предыдущий отчетный период рну-5
                rows8.each {
                    def alias = 'R' + it
                    def row = getDataRow(dataRows, alias)
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

    // карта "id формы" : ("номер/дата документа" : "суммы")
    def sum7map = [:]

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    formSources.each {
        def child = getFormData(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def dataRowsChild = formDataService.getDataRowHelper(child)?.allSaved
            switch (child.formType.id) {
            // рну 7
                case formTypeId_RNU7:
                    def codeBalanceMap = [:]
                    dataRowsChild.each { rowRNU7 ->
                        if (rowRNU7.getAlias() == null) {
                            // ключ состоит из id записи кну и балансового счета
                            String key = String.valueOf(rowRNU7.code)
                            if (codeBalanceMap[key] == null) {
                                codeBalanceMap[key] = ["sum5" : 0, "sum6" : 0, "sum7" : 0]
                            }
                            //«графа 5» = сумма графы 10 рну-7
                            codeBalanceMap[key].sum5 += (rowRNU7.taxAccountingRuble ?: 0)
                            //«графа 6» = сумма графы 12 рну-7
                            codeBalanceMap[key].sum6 += (rowRNU7.ruble ?: 0)
                            //графа 7
                            if (rowRNU7.ruble != null && rowRNU7.ruble != 0) {
                                def dateFrom = Date.parse('dd.MM.yyyy', '01.01.' + (Integer.valueOf(rowRNU7.docDate?.format('yyyy')) - 3))
                                def reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, dateFrom, rowRNU7.docDate)
                                reportPeriodList.each { period ->
                                    // ищем формы, в процессе заполняем карту
                                    def primaryRNU7 = getFormData(child.formType.id, child.kind, child.departmentId, period.getId(), null, child.comparativePeriodId, child.accruing)
                                    if (primaryRNU7 != null) {
                                        // для формы достаточно id в качестве ключа
                                        String keyForm = String.valueOf(primaryRNU7.id)
                                        String keyDoc = "${rowRNU7.code}#${rowRNU7.docNumber}#${rowRNU7.docDate}"
                                        if (sum7map[keyForm] == null) { // если карта пустая, то еще не заполняли
                                            sum7map[keyForm] = [:]
                                            def dataPrimary = formDataService.getDataRowHelper(primaryRNU7)
                                            dataPrimary.allSaved.each { rowPrimary ->
                                                if (rowPrimary.getAlias() == null &&
                                                        rowPrimary.code == rowRNU7.code &&
                                                        rowPrimary.docNumber == rowRNU7.docNumber &&
                                                        rowPrimary.docDate == rowRNU7.docDate) {
                                                    String localKeyDoc = "${rowPrimary.code}#${rowPrimary.docNumber}#${rowPrimary.docDate}"
                                                    if (sum7map[keyForm][localKeyDoc] == null) {
                                                        sum7map[keyForm][localKeyDoc] = 0
                                                    }
                                                    sum7map[keyForm][localKeyDoc] += (rowPrimary.taxAccountingRuble ?: 0)
                                                }
                                            }
                                        }
                                        codeBalanceMap[key].sum7 += (sum7map[keyForm][keyDoc] ?: 0)
                                    }
                                }
                            }
                        }
                    }
                    rows567.each {
                        def row = getDataRow(dataRows, 'R' + it)
                        if (row.consumptionTypeId != null && row.consumptionAccountNumber != null) {
                            def recordId = getRecordId(row.consumptionTypeId, row.consumptionAccountNumber, getReportPeriodEndDate())

                            if (isEqualNum(row.consumptionAccountNumber, recordId)) {
                                def sums = codeBalanceMap[String.valueOf(recordId)]
                                if (sums != null) {
                                    row.rnu7Field10Sum = (row.rnu7Field10Sum ?: 0) + (sums.sum5 ?: 0)
                                    row.rnu7Field12Accepted = (row.rnu7Field12Accepted ?: 0) + (sums.sum6 ?: 0)
                                    row.rnu7Field12PrevTaxPeriod = (row.rnu7Field12PrevTaxPeriod ?: 0) + (sums.sum7 ?: 0)
                                }
                            }
                        }
                    }
                    break

            // рну 5
                case formTypeId_RNU5:
                    rows8.each {
                        def alias = 'R' + it
                        def row = getDataRow(dataRows, alias)
                        def recordId = getRecordId(row.consumptionTypeId, row.consumptionAccountNumber, getReportPeriodEndDate())

                        // сумма графы 5 рну-5
                        def sum5 = 0
                        if (recordId != null) {
                            sum5 = dataRowsChild.sum { rowRNU5 ->
                                return (rowRNU5.getAlias() == null && recordId == rowRNU5.number &&
                                        isEqualNum(row.consumptionAccountNumber, rowRNU5.number) && rowRNU5.sum) ? rowRNU5.sum : 0
                            }

                        }
                        // графа 8
                        row.rnu5Field5Accepted = (row.rnu5Field5Accepted ?: 0) + sum5
                    }
                    break
            }
        }
    }
}

FormData getFormData(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
    String key = "$formTypeId#${kind.id}#$departmentId#$reportPeriodId#$periodOrder"
    if (formDataCache[key] != -1 && formDataCache[key] == null){ // чтобы повторно не искал несуществующие формы
        formDataCache[key] = formDataService.getLast(formTypeId, kind, departmentId, reportPeriodId, periodOrder, comparativePeriodId, accruing) ?: -1
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

/**
 * Получить значение для графы 12. Сумма значении графы 8 указанных строк
 * @param dataRows строки НФ
 * @param aliasRows список алиасов значения которых надо просуммировать
 */
def calcSum8(def dataRows, def aliasRows) {
    return aliasRows.sum { alias ->
        (getDataRow(dataRows, alias).rnu5Field5Accepted)?:0
    }
}

/**
 * Получить данные формы "расходы сложные" (id = 303)
 */
def getFormDataComplex() {
    return getFormData(303, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
}

def getBalanceValue(def value) {
    formDataService.getRefBookValue(27, value, refBookCache)?.NUMBER?.stringValue
}

boolean isEqualNum(String accNum, def balance) {
    def a = accNum?.replace('.', '')
    def b = (balance ? getBalanceValue(balance)?.replace('.', '') : null)
    return a == b
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
        row50001Tmp[alias] = getSum(dataRows, alias, first1Alias, last1Alias) - (getDataRow(dataRows, exceptAlias).get(alias) ?: 0)
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
    checkHeaderSize(headerRows, 8, 3)

    def headerMapping = [
            ([(headerRows[0][0]): 'КНУ']),
            ([(headerRows[0][1]): 'Группа расхода']),
            ([(headerRows[0][2]): 'Вид расхода по операциям']),
            ([(headerRows[0][3]): 'Балансовый счёт по учёту расхода']),
            ([(headerRows[0][4]): 'РНУ-7 (графа 10) сумма']),
            ([(headerRows[0][5]): 'РНУ-7 (графа 12)']),
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
    ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }
}