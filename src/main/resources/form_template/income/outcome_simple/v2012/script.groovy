package form_template.income.outcome_simple.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "Расходы, учитываемые в простых РНУ (расходы простые)"
 * formTypeId=304
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
        checkRnu14Accepted()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
}

// Кэш id записей справочника
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

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
def row50001alias = 'R108'
@Field
def row50002alias = 'R213'

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

// Получение Id записи с использованием кэширования
def getRecordId(def ref_id, String alias, String value, Date date) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
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

// Метод заполняющий в кэш все записи для разыменывавания
void fillRecordsMap(def ref_id, String alias, List<String> values, Date date) {
    def filterList = values.collect {
        "LOWER($alias) = LOWER('$it')"
    }
    def filter = filterList.join(" OR ")
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    records.each { record ->
        filter = "LOWER($alias) = LOWER('${record[alias]}')"
        if (recordCache[ref_id] == null) {
            recordCache[ref_id] = [:]
        }
        recordCache[ref_id][filter] = record.get(RefBook.RECORD_ID_ALIAS).numberValue
    }
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

    dataRows.each {row ->
         checkRequiredColumns(row, nonEmptyColumns)
    }

    def row50001 = getDataRow(dataRows, row50001alias)
    def row50002 = getDataRow(dataRows, row50002alias)
    def need50001 = [:]
    def need50002 = [:]
    totalColumns.each{ alias ->
        need50001[alias] = getSum(dataRows, alias, 'R2', 'R107') - (getDataRow(dataRows, 'R36').get(alias)?:0)
        need50002[alias] = getSum(dataRows, alias, 'R110', 'R212')
    }
    checkTotalSum(row50001, need50001)
    checkTotalSum(row50002, need50002)
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(), getReportPeriodStartDate(), getReportPeriodEndDate())
    def isFromSummary = isFromSummary(formSources)
    isFromSummary ? consolidationFromSummary(dataRows, formSources) : consolidationFromPrimary(dataRows, formSources)
    calculationBasicSum(dataRows)
}

boolean isFromSummary(def formSources) {
    def isSummarySource = formSources.find { it.formTypeId == formData.formType.id } != null
    def isPrimarySource = formSources.find { it.formTypeId in [formTypeId_RNU7, formTypeId_RNU5] } != null
    if (isSummarySource && isPrimarySource) {
        logger.warn("Неверно настроены источники формы \"%s\" для подразделения \"%s\"! Одновременно указаны в качестве источников сводные и первичные налоговые формы. Консолидация произведена из сводных налоговых форм.",
                formData.formType.name, formDataDepartment.name)
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
    def row50001 = getDataRow(dataRows, row50001alias)
    def row50002 = getDataRow(dataRows, row50002alias)

    // суммы для графы 5..8
    totalColumns.each { alias ->
        row50001[alias] = getSum(dataRows, alias, 'R2', 'R107') - (getDataRow(dataRows, 'R36').get(alias)?:0)
        row50002[alias] = getSum(dataRows, alias, 'R110', 'R212')
    }
    def formDataRNU14 = getFormDataRNU14()
    def dataRowsRNU14 = (formDataRNU14 ? formDataService.getDataRowHelper(formDataRNU14)?.allSaved : null)
    ['R214', 'R215', 'R216', 'R217', 'R218'].each { alias ->
        def row = getDataRow(dataRows, alias)
        if (isBank()) {
            //Строки 213-217 расчет 8-й графы (при консолидации из сводных)
            if (formDataRNU14 != null) {
                for (def rowRNU14 : dataRowsRNU14) {
                    if (rowRNU14.inApprovedNprms != rowRNU14.sum && row.consumptionTypeId == rowRNU14.knu) {
                        row.rnu5Field5Accepted = rowRNU14.inApprovedNprms
                    }
                }
            }
        }
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
        // исключить итоговые строки и пять конечных и 36-ую
        if (row.getAlias() in [row50001alias, row50002alias, 'R1', 'R36', 'R109', 'R214', 'R215', 'R216', 'R217', 'R218']) {
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
        if (row.getAlias() in ['R106', 'R210']) {
            tmp = calcSum8(dataRows, ['R106', 'R210'])
        } else if (row.getAlias() in ['R107', 'R212']) {
            tmp = calcSum8(dataRows, ['R107', 'R212'])
        } else if (row.getAlias() in ['R105', 'R209']) {
            tmp = calcSum8(dataRows, ['R105', 'R209'])
        } else if (row.getAlias() in ['R35', 'R36']) {
            tmp = calcSum8(dataRows, ['R35', 'R36'])
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
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [row50001alias, row50002alias]) {
                row.getCell(alias).setValue(0, row.getIndex())
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    formSources.each { departmentFormType ->
        def child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            def childData = formDataService.getDataRowHelper(child)
            for (DataRow<Cell> row : childData.allSaved) {
                if (row.getAlias() == null) {
                    continue
                }
                DataRow<Cell> rowResult = getDataRow(dataRows, row.getAlias())
                for (alias in ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']) {
                    if (row[alias] != null) {
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
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [row50001alias, row50002alias]) {
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
            def formDataOld = formDataService.getLast(formData.getFormType().getId(), formData.getKind(), formDataDepartment.id, prevReportPeriod.getId(), formData.periodOrder)
            dataRowsOld = (formDataOld ? formDataService.getDataRowHelper(formDataOld)?.allSaved : null)
            if (dataRowsOld != null) {
                // данные за предыдущий отчетный период рну-7
                ([3, 12] + (15..35) + (39..50) + (52..55) + (57..59) + (63..79) + (92..96) + (99..102) +
                        (104..107) + (182..184) + (191..195) + [200, 205, 206] + (208..212)).each {
                    def alias = 'R' + it
                    def row = getDataRow(dataRows, alias)
                    def rowOld = getDataRow(dataRowsOld, alias)
                    // графа 5
                    row.rnu7Field10Sum = rowOld.rnu7Field10Sum
                    // графа 6
                    row.rnu7Field12Accepted = rowOld.rnu7Field12Accepted
                    // графа 7
                    row.rnu7Field12PrevTaxPeriod = rowOld.rnu7Field12PrevTaxPeriod
                }
                // данные за предыдущий отчетный период рну-5
                ((2..107) + (110..212)).each {
                    def alias = 'R' + it
                    def row = getDataRow(dataRows, alias)
                    // графа 8
                    row.rnu5Field5Accepted = getDataRow(dataRowsOld, alias).rnu5Field5Accepted
                }
            }
        }
    }

    // Прошел по строкам и получил список кну
    def knuList = ((2..106) + (109..211)).collect{
        def row = getDataRow(dataRows, 'R' + it)
        return row.consumptionTypeId
    }
    fillRecordsMap(27, 'CODE', knuList, getReportPeriodEndDate())

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    formSources.each {
        def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def dataRowsChild = formDataService.getDataRowHelper(child)?.allSaved
            switch (child.formType.id) {
            // рну 7
                case formTypeId_RNU7:
                    ([3, 12] + (15..35) + (39..50) + (52..55) + (57..59) + (63..79) + (92..96) + (99..102) +
                            (104..107) + (182..184) + (191..195) + [200, 205, 206] + (208..212)).each {
                        def alias = 'R' + it
                        def row = getDataRow(dataRows, alias)

                        def recordId = getRecordId(27, 'CODE', row.consumptionTypeId, getReportPeriodEndDate())

                        // сумма графы 10 рну-7
                        def sum10 = 0
                        // сумма графы 12 рну-7
                        def sum12 = 0
                        // сумма графы 10 рну-7 для графы 7
                        def sum = 0
                        if (recordId != null) {
                            sum10 = getSumForColumn5or6or8(dataRowsChild, recordId, 'code', row.consumptionAccountNumber, 'taxAccountingRuble')
                            sum12 = getSumForColumn5or6or8(dataRowsChild, recordId, 'code', row.consumptionAccountNumber, 'ruble')
                            sum = getSumForColumn7(child, dataRowsChild, recordId, row.consumptionAccountNumber)
                        }

                        // графа 5
                        row.rnu7Field10Sum = (row.rnu7Field10Sum ?: 0) + sum10
                        // графа 6
                        row.rnu7Field12Accepted = (row.rnu7Field12Accepted ?: 0) + sum12
                        // графа 7
                        row.rnu7Field12PrevTaxPeriod = (row.rnu7Field12PrevTaxPeriod ?: 0) + sum
                    }
                    break

            // рну 5
                case formTypeId_RNU5:
                    ((2..107) + (110..212)).each {
                        def alias = 'R' + it
                        def row = getDataRow(dataRows, alias)
                        def recordId = getRecordId(27, 'CODE', row.consumptionTypeId, getReportPeriodEndDate())

                        // сумма графы 5 рну-5
                        def sum5 = 0
                        if (recordId != null) {
                            sum5 = getSumForColumn5or6or8(dataRowsChild, recordId, 'number', row.consumptionAccountNumber, 'sum')
                        }
                        // графа 8
                        row.rnu5Field5Accepted = (row.rnu5Field5Accepted ?: 0) + sum5
                    }
                    break
            }
        }
    }
}

// Проверка на банк
def isBank() {
    boolean isBank = true
    // получаем список приемников
    def list = departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formType.id, FormDataKind.SUMMARY, getReportPeriodStartDate(), getReportPeriodEndDate())
    // если есть приемники в других подразделениях, то это не банк, а ОП
    list.each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
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
    return formDataService.getLast(303, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder)
}

/**
 * Получить данные формы РНУ-14 (id = 321)
 */
def getFormDataRNU14() {
    return formDataService.getLast(321, FormDataKind.UNP, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder)
}

/**
 * Получить сумму строк графы нф.
 * @param dataRowsChild строки нф источника (рну-7 или рну-5)
 * @param value1 значение приемника для первого условия (id справочника)
 * @param alias1 алиас графы для первого условия
 * @param value2 значение балансового счета для второго условия
 * @param resultAlias алиас графы суммирования
 */
def getSumForColumn5or6or8(def dataRowsChild, def value1, def alias1, def value2, def resultAlias) {
    def sum = 0
    for (row in dataRowsChild) {
        if (value1 == row[alias1] && isEqualNum(value2, row[alias1])) {
            sum += (row[resultAlias] ?: 0)
        }
    }
    return sum
}

/**
 * Получить сумму строк графы нф соответствующих двум условиям.
 * @param form нф источника (рну-7 или рну-5)
 * @param value1 значение приемника для первого условия (id справочника)
 * @param value2 значение балансового счета для второго условия
 */
def getSumForColumn7(def form, def dataRows, def value1, def value2) {
    def sum = 0
    dataRows.each { row ->
        if (row.getAlias() == null) {
            if (value1 == row.code && isEqualNum(value2, row.code) && row.ruble != null && row.ruble != 0) {
                // получить (дату - 3 года)
                def Date dateFrom = Date.parse('dd.MM.yyyy','01.01.' + (Integer.valueOf(row.docDate?.format('yyyy')) - 3))
                // получить отчетные периоды за найденый промежуток времени [(дата - 3года)..дата]
                def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, dateFrom, row.docDate)
                reportPeriods.each { reportPeriod ->
                    // в каждой форме относящейся к этим периодам ищем соответствующие строки и суммируем по 10 графе
                    def FormData f = formDataService.getLast(form.getFormType().getId(), form.kind, form.getDepartmentId(), reportPeriod.getId(), form.periodOrder)
                    if (f != null) {
                        def d = formDataService.getDataRowHelper(f)
                        if (d != null) {
                            d.allSaved.each { r ->
                                // графа  4 - code
                                // графа  5 - docNumber
                                // графа  6 - docDate
                                // графа 10 - taxAccountingRuble
                                if (r.code == row.code && r.docNumber == row.docNumber && r.docDate == row.docDate) {
                                    sum += (r.taxAccountingRuble ?: 0)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return sum
}

def getBalanceValue(def value) {
    formDataService.getRefBookValue(27, value, refBookCache)?.NUMBER?.stringValue
}

boolean isEqualNum(String accNum, def balance) {
    return accNum.replace('.', '') == getBalanceValue(balance).replace('.', '')
}

/** Проверить заполненость обязательных полей. */
void checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each {
        def cell = row.getCell(it)
        if (cell?.style?.alias == editableStyle && (cell.getValue() == null || row[it] == '')) {
            colNames.add('"' + getColumnName(row, it) + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.getIndex()
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
    }
}

// для уроня Банка:	проверка наличия и принятия РНУ-14
void checkRnu14Accepted() {
    if (!isBank()) {
        return
    }
    def formData14 = getFormDataRNU14()
    if (formData14 == null || formData14.state != WorkflowState.ACCEPTED) {
        logger.error("Принятие сводной налоговой формы невозможно, т.к. форма РНУ-14 не сформирована или имеет статус, отличный от «Принята»")
    }
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
        def dataRow = getDataRow(dataRows, "R" + rowIndex)
        // заполнить строку нф значениями из эксель
        fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(dataRows)
        formDataService.getDataRowHelper(formData).allCached = dataRows
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
    checkHeaderSize(headerRows[0].size(), headerRows.size(), 8, 3)
    def headerMapping = [
            (headerRows[0][0]): 'КНУ',
            (headerRows[0][1]): 'Группа расхода',
            (headerRows[0][2]): 'Вид расхода по операциям',
            (headerRows[0][3]): 'Балансовый счёт по учёту расхода',
            (headerRows[0][4]): 'РНУ-7 (графа 10) сумма',
            (headerRows[0][5]): 'РНУ-7 (графа 12)',
            (headerRows[0][7]): 'РНУ-5 (графа 5) сумма',
            (headerRows[1][5]): 'сумма',
            (headerRows[1][6]): 'в т.ч. учтено в предыдущих налоговых периодах по графе 10'
    ]
    (0..7).each { index ->
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