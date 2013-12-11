package form_template.income.outcome_simple

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper
import groovy.transform.Field
import java.text.SimpleDateFormat

/**
 * Форма "Расходы, учитываемые в простых РНУ (расходы простые)"
 * formTemplateId=304
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
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED: // Принять из "Создано"
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
    case FormDataEvent.COMPOSE:
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper?.allCached
        isBank() ? consolidationBank(dataRows) : consolidationSummary(dataRows)
        calculationBasicSum(dataRows)
        dataRowHelper.save(dataRows)
        break
}

// Кэш id записей справочника
@Field
def recordCache = [:]

@Field
def allColumns = ['consumptionTypeId', 'consumptionGroup', 'consumptionTypeByOperation', 'consumptionAccountNumber',
        'rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted',
        'logicalCheck', 'accountingRecords', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'difference']

@Field
def nonEmptyColumns = ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']

@Field
def formatY = new SimpleDateFormat('yyyy')

@Field
def format = new SimpleDateFormat('dd.MM.yyyy')

@Field
def isBank

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
    def records = getProvider(ref_id).getRecords(date, null, filter, null)
    if (records.size() == 1) {
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    }
    return null
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    calculationBasicSum(dataRows)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        calculationControlGraphs(dataRows)
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    dataRows.each {row ->
         checkRequiredColumns(row, nonEmptyColumns)
    }
}

void calculationBasicSum(def dataRows) {
    def row50001 = getDataRow(dataRows, 'R107')
    def row50002 = getDataRow(dataRows, 'R212')

    // суммы для графы 5..8
    ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
        row50001[alias] = getSum(dataRows, alias, 'R2', 'R106')
        row50002[alias] = getSum(dataRows, alias, 'R109', 'R211')
    }
    def formDataRNU14 = getFormDataRNU14()
    def dataRowsRNU14 = formDataService.getDataRowHelper(formDataRNU14)?.allCached
    ['R213', 'R214', 'R215', 'R216', 'R217'].each { alias ->
        def row = getDataRow(dataRows, alias)
        if (!isBank()) {
            //при консолидации из первичных
            row.rnu5Field5Accepted = 0
        } else {
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
    def dataRowsComplex = formDataService.getDataRowHelper(getFormDataComplex())?.allCached
    def income102NotFound = []
    for (def row : dataRows) {
        // исключить итоговые строки и пять конечных
        if (row.getAlias() in ['R107', 'R212', 'R1', 'R108', 'R213', 'R214', 'R215', 'R216', 'R217']) {
            continue
        }
        if (row.rnu7Field10Sum && row.rnu7Field12Accepted && row.rnu7Field12PrevTaxPeriod) {
            // графы 9 = ОКРУГЛ(«графа 5» - («графа 6» - «графа 7»); 2)
            tmp = round(row.rnu7Field10Sum - (row.rnu7Field12Accepted - row.rnu7Field12PrevTaxPeriod), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }
        // графа 11
        row.opuSumByEnclosure2 = getSumFromComplex(dataRowsComplex,
                'consumptionBuhSumAccountNumber', 'consumptionBuhSumAccepted', row.consumptionAccountNumber)
        // графа 12
        if (row.getAlias() in ['R105', 'R209']) {
            tmp = calcSum6(dataRows, ['R105', 'R209'])
        } else if (row.getAlias() in ['R106', 'R211']) {
            tmp = calcSum6(dataRows, ['R106', 'R211'])
        } else if (row.getAlias() in ['R104', 'R208']) {
            tmp = calcSum6(dataRows, ['R104', 'R208'])
        } else {
            tmp = row.rnu5Field5Accepted
        }
        row.opuSumByTableP = tmp

        // графа 13
        def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords)
        if (income102 == null || income102.isEmpty()) {
            income102NotFound += row.getIndex() + 2
            tmp = 0
        } else {
            tmp = (income102[0] != null ? income102[0].getTotalSum() : 0)
        }
        row.opuSumTotal = tmp

        // графа 14
        row.difference = (row.opuSumByEnclosure2?:0) + (row.opuSumByTableP?:0) - (row.opuSumTotal?:0)
    }

    if (!income102NotFound.isEmpty()) {
        def rows = income102NotFound.join(', ')
        logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках для строк: $rows")
    }
}

/** Скрипт для консолидации данных из сводных расходов простых уровня ОП в сводные уровня банка. */
def consolidationBank(def dataRows) {
    // очистить форму
    dataRows.each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
            if (row.getCell(alias).isEditable() || row.getAlias() in ['R107', 'R212']) {
                row.getCell(alias).setValue(0)
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    for (departmentFormType in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        def child = formDataService.find(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            DataRowHelper childData = formDataService.getDataRowHelper(child)
            for (DataRow<Cell> row : childData.allCached) {
                if (row.getAlias() == null) {
                    continue
                }
                DataRow<Cell> rowResult = dataRowHelper.getDataRow(dataRows, row.getAlias())
                for (alias in ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted']) {
                    if (row[alias] != null) {
                        rowResult[alias] = summ(rowResult.getCell(alias), row.getCell(alias))
                    }
                }
            }
        }
    }
    logger.info('Формирование сводной формы уровня Банка прошло успешно.')
}

/** Консолидация данных из рну-7 и рну-5 в сводные расходы простые уровня ОП. */
void consolidationSummary(def dataRows) {
    // очистить форму
    dataRows.each { row ->
        ['rnu7Field10Sum', 'rnu7Field12Accepted', 'rnu7Field12PrevTaxPeriod', 'rnu5Field5Accepted'].each { alias ->
            if (row.getCell(alias).isEditable() || row.getAlias() in ['R107', 'R212']) {
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
            def formDataOld = formDataService.find(formData.getFormType().getId(), formData.getKind(), formDataDepartment.id, prevReportPeriod.getId())
            dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.allCached
            if (dataRowsOld != null) {
                // данные за предыдущий отчетный период рну-7
                ([3, 12] + (15..35) + (38..49) + (51..54) + (56..58) + (62..78) + (91..95) + (98..101) +
                        (103..106) + (181..183) + (190..194) + [199, 204, 205] + (207..211)).each {
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
                ((2..106) + (109..211)).each {
                    def alias = 'R' + it
                    def row = getDataRow(dataRows, alias)
                    // графа 8
                    row.rnu5Field5Accepted = getDataRow(dataRowsOld, alias).rnu5Field5Accepted
                }
            }
        }
    }

    def cache = [:]
    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def date = new Date()
            def dataRowsChild = formDataService.getDataRowHelper(child)?.allCached
            switch (child.formType.id) {
            // рну 7
                case 311:
                    ([3, 12] + (15..35) + (38..49) + (51..54) + (56..58) + (62..78) + (91..95) + (98..101) +
                            (103..106) + (181..183) + (190..194) + [199, 204, 205] + (207..211)).each {
                        def alias = 'R' + it
                        def row = getDataRow(dataRows, alias)
                        def recordId = getRecordId(27, 'CODE', row.consumptionTypeId, date)

                        // сумма графы 10 рну-7
                        def sum10 = 0
                        // сумма графы 12 рну-7
                        def sum12 = 0
                        // сумма графы 10 рну-7 для графы 7
                        def sum = 0
                        if (recordId != null) {
                            sum10 = getSumForColumn5or6or8(dataRowsChild, recordId, 'code', 'balance', 'taxAccountingRuble')
                            sum12 = getSumForColumn5or6or8(dataRowsChild, recordId, 'code', 'balance', 'ruble')
                            sum = getSumForColumn7(child, dataRowsChild, recordId)
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
                case 317:
                    ((2..106) + (109..211)).each {
                        def alias = 'R' + it
                        def row = getDataRow(dataRows, alias)
                        def recordId = getRecordId(27, 'CODE', row.consumptionTypeId, date)
                        // сумма графы 5 рну-5
                        def sum5 = 0
                        if (recordId != null) {
                            sum5 = getSumForColumn5or6or8(dataRowsChild, recordId, 'code', 'number', 'sum')
                        }
                        // графа 8
                        row.rnu5Field5Accepted = (row.rnu5Field5Accepted ?: 0) + sum5
                    }
                    break
            }
        }
    }
    logger.info('Формирование сводной формы уровня обособленного подразделения прошло успешно.')
}

/** Проверка на банк. */
def isBank() {
    if (isBank == null) {
        departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
            if (it.departmentId != formData.departmentId) {
                isBank = false
            }
        }
        isBank = true
    }
    return isBank
}

/** Получить сумму диапазона строк определенного столбца. */
def getSum(def dataRows, String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = getDataRow(dataRows, rowFromAlias).getIndex()
    def to = getDataRow(dataRows, rowToAlias).getIndex()
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
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
 * Получить значение для графы 12. Сумма значении графы 6 указанных строк
 * @param dataRows строки НФ
 * @param aliasRows список алиасов значения которых надо просуммировать
 */
def calcSum6(def dataRows, def aliasRows) {
    return aliasRows.sum { alias ->
        getDataRow(dataRows, alias).rnu7Field12Accepted
    }
}

/**
 * Получить данные формы "расходы сложные" (id = 303)
 */
def getFormDataComplex() {
    return formDataService.find(303, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить данные формы РНУ-14 (id = 321)
 */
def getFormDataRNU14() {
    return formDataService.find(321, FormDataKind.UNP, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить сумму строк графы нф соответствующих двум условиям.
 * @param dataRowsChild строки нф источника (рну-7 или рну-5)
 * @param value1 значение приемника для первого условия (id справочника)
 * @param alias1 алиас графы для первого условия
 * @param alias2 алиас графы для второго условия
 * @param resultAlias алиас графы суммирования
 */
def getSumForColumn5or6or8(def dataRowsChild, def value1, def alias1, def alias2, def resultAlias) {
    def sum = 0
    for (row in dataRowsChild) {
        if (value1 == row[alias1] && value1 == row[alias2]) {
            sum += (row[resultAlias] ?: 0)
        }
    }
    return sum
}

/**
 * Получить сумму строк графы нф соответствующих двум условиям.
 * @param form нф источника (рну-7 или рну-5)
 * @param value1 значение приемника для первого условия (id справочника)
 */
def getSumForColumn7(def form, def dataRows, def value1) {
    def sum = 0
    dataRows.each { row ->
        if (row.getAlias() == null) {
            if (value1 == row.code && value1 == row.balance && row.ruble != null && row.ruble != 0) {
                // получить (дату - 3 года)
                def Date dateFrom = format.parse('01.01.' + (Integer.valueOf(formatY.format(row.docDate)) - 3))
                // получить налоговые и отчетные периоды за найденый промежуток времени [(дата - 3года)..дата]
                def List<TaxPeriod> taxPeriods = taxPeriodService.listByTaxTypeAndDate(TaxType.INCOME, dateFrom, row.docDate)
                taxPeriods.each { taxPeriod ->
                    def List<ReportPeriod> reportPeriods = reportPeriodService.listByTaxPeriod(taxPeriod.getId())
                    reportPeriods.each { reportPeriod ->
                        // в каждой форме относящейся к этим периодам ищем соответствующие строки и суммируем по 10 графе
                        def FormData f = formDataService.find(form.getFormType().getId(), FormDataKind.PRIMARY, form.getDepartmentId(), reportPeriod.getId())
                        if (f != null) {
                            def DataRowHelper d = formDataService.getDataRowHelper(f)
                            if (d != null) {
                                d.allCached.each { r ->
                                    // графа  4 - balance
                                    // графа  5 - docNumber
                                    // графа  6 - docDate
                                    // графа 10 - taxAccountingRuble
                                    if (r.balance == row.balance && r.docNumber == row.docNumber && r.docDate == row.docDate) {
                                        sum += (r.taxAccountingRuble ?: 0)
                                    }
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

/** Проверить заполненость обязательных полей. */
void checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each {
        def cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row[it] == '')) {
            colNames.add('"' + getColumnName(row, it) + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.getIndex()
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
    }
}
