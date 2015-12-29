package form_template.income.output1_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (03/А)
 * formTemplateId=1411
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 *
 1. 	financialYear                   Отчетный год
 2.     taxPeriod                       Налоговый (отчетный) период
 3.     emitent                         Эмитент
 4.     decreeNumber                    Номер решения о распределении доходов от долевого участия
 5.     dividendType                    Вид дивидендов
 6. 	dividendSumRaspredPeriod        Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Всего
 7.     dividendSumNalogAgent           Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. По которым выступает в качестве налогового агента
 8. 	dividendForgeinOrgAll           Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Организациям
 9. 	dividendForgeinPersonalAll      Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Физическим лицам
 10. 	dividendStavka0                 Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. 0%
 11. 	dividendStavkaLess5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. До 5% включительно
 12. 	dividendStavkaMore5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. Свыше 5% и до 10% включительно
 13. 	dividendStavkaMore10            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. Свыше 10%
 14. 	dividendRussianMembersAll       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Всего
 15. 	dividendRussianOrgStavka9       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Организациям (налоговая ставка - 9%)
 16. 	dividendRussianOrgStavka0       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Организациям (налоговая ставка - 0%)
 17. 	dividendPersonRussia            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Физическим лицам
 18. 	dividendMembersNotRussianTax    Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Не являющихся налогоплательщиками
 19. 	dividendAgentAll                Дивиденды, полученные. Всего
 20. 	dividendAgentWithStavka0        Дивиденды, полученные. В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%
 21. 	dividendSumForTaxAll            Сумма дивидендов, используемых для исчисления налога по российским организациям. Всего
 22. 	dividendSumForTaxStavka9        Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 9%
 23. 	dividendSumForTaxStavka0        Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 0%
 24. 	taxSum                          Исчисленная сумма налога, подлежащая уплате в бюджет
 25. 	taxSumFromPeriod                Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды
 26. 	taxSumFromPeriodAll             Сумма налога, начисленная с дивидендов, выплаченных в отчетном квартале
 *
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
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

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def editableColumns = ['financialYear', 'taxPeriod', 'emitent', 'decreeNumber', 'dividendType', 'dividendSumRaspredPeriod',
                       'dividendSumNalogAgent', 'dividendForgeinOrgAll',
                       'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5',
                       'dividendStavkaMore10', 'dividendRussianMembersAll', 'dividendRussianOrgStavka9',
                       'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
                       'dividendAgentAll', 'dividendAgentWithStavka0', 'dividendSumForTaxAll', 'dividendSumForTaxStavka9',
                       'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumFromPeriodAll']

@Field
def nonEmptyColumns = ['financialYear', 'taxPeriod', 'dividendType', 'dividendSumRaspredPeriod', 'dividendSumNalogAgent',
                       'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5',
                       'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianOrgStavka9',
                       'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
                       'dividendAgentWithStavka0', 'taxSumFromPeriod', 'taxSumFromPeriodAll']

// 7, 8 графа источника
@Field
def keyColumns = ['decisionNumber', 'decisionDate']

@Field
def sbString = "ОАО Сбербанк России"

@Field
def sbString2 = "Открытое акционерное общество \"Сбербанк России\""

@Field
def graph3String = "7707083893"

@Field
def sourceFormType = 10070

@Field
def sourceFormTypeAlt = 419

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

def getLastReportPeriod() {
    ReportPeriod period = reportPeriodService.get(formData.reportPeriodId)
    List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(period.taxPeriod.id)
    return periodList.max{ ReportPeriod rp -> rp.order }
}

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    // расчетов нет
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row in dataRows) {
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
        checkNonEmptyColumns(row, rowNum, ['emitent', 'decreeNumber'], logger, false)

        // Проверка наличия значения графы 2 в справочнике «Коды, определяющие налоговый (отчётный) период»
        def cell = row.getCell('taxPeriod')
        getRecordId(8, 'CODE', cell.value, rowNum, cell.column.name, true)
    }

    // 2. Проверка наличия формы за предыдущий отчётный период
    if (formDataService.getFormDataPrev(formData) == null) {
        logger.warn('Форма за предыдущий отчётный период не создавалась!')
    }
}

def roundValue(BigDecimal value, def int precision) {
    value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

void consolidation() {
    def rows = []

    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def prevPeriodStartDate = reportPeriodService.getCalendarStartDate(prevReportPeriod.id).time
    def prevPeriodEndDate = reportPeriodService.getEndDate(prevReportPeriod.id).time

    def lastPeriod = getLastReportPeriod()
    def lastPeriodStartDate = reportPeriodService.getCalendarStartDate(lastPeriod.id).time
    def lastPeriodEndDate = reportPeriodService.getEndDate(lastPeriod.id).time

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == sourceFormType || it.formTypeId == sourceFormTypeAlt) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceHelper = formDataService.getDataRowHelper(sourceFormData)
                def rowMap = getRowMap(sourceHelper.allSaved)
                rowMap.each { key, sourceRows ->
                    def newRow = formNewRow(sourceRows, prevPeriodStartDate, prevPeriodEndDate, lastPeriodStartDate, lastPeriodEndDate)
                    rows.add(newRow)
                }
            }
        }
    }

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

def getRowMap(def rows) {
    def result = [:]
    rows.each{ row ->
        def keyString = keyColumns.collect{ row[it] ?: 0 }.join("#")
        if(result[keyString] == null) {
            result[keyString] = []
        }
        result[keyString].add(row)
    }
    return result
}

def formNewRow(def rowList, def prevPeriodStartDate, def prevPeriodEndDate, def lastPeriodStartDate, def lastPeriodEndDate) {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    // беру первую строку
    def row = rowList[0]

    // «Графа 1» = Значение «Графы 9» первичной формы
    newRow.financialYear = row.year
    // Графа 2
    newRow.taxPeriod = calcPeriod(row.firstMonth, row.lastMonth)
    // «Графа 3» = «Графа 2» первичной формы
    newRow.emitent = row.emitentName
    // «Графа 4» = «Графа 7» первичной формы
    newRow.decreeNumber = row.decisionNumber
    // Если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «12», то «Графа 5» = «2», иначе «Графа 5» = «1»
    newRow.dividendType = (row.firstMonth == 1 && row.lastMonth == 12) ? '2' : '1'
    // «Графа 6» = «Графа 12» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы
    newRow.dividendSumRaspredPeriod = row.allSum
    // «Графа 7» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы
    newRow.dividendSumNalogAgent = rowList.sum{ it.dividends ?: 0 }
    // «Графа 8» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «1» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendForgeinOrgAll = rowList.sum{ (it.type == 1 && it.status != 1 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 9» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «2» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendForgeinPersonalAll = rowList.sum{ (it.type == 2 && it.status != 1 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 10» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы = «0» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavka0 = rowList.sum{ (it.rate == 0 && it.status != 1 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 11» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы <= «5» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaLess5 = rowList.sum{ (!(it.rate > 5) && it.status != 1 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 12» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «5» и <= «10» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaMore5 = rowList.sum{ ((it.rate > 5 && !(it.rate > 10)) && it.status != 1 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 13» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «10» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaMore10 = rowList.sum{ ((it.rate > 10) && it.status != 1 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 14» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS»
    newRow.dividendRussianMembersAll = rowList.sum{ (it.status == 1 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 15» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9»
    newRow.dividendRussianOrgStavka9 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 9 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 16» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0»
    newRow.dividendRussianOrgStavka0 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 0 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 17» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «2»
    newRow.dividendPersonRussia = rowList.sum{ (it.status == 1 && it.type == 2 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 18» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = 4
    newRow.dividendMembersNotRussianTax = rowList.sum{ (it.status == '4' && it.dividends != null) ? it.dividends : 0 }
    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 19» = «Графа 4» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе не заполняется
    newRow.dividendAgentAll = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String && row.all != null) ? row.all : null
    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 20» = («Графа 4» первичной формы – «Графа 5» первичной формы) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе «Графа 20» = «Графа 6» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы.
    newRow.dividendAgentWithStavka0 = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? ((row.all ?: 0) - (row.rateZero ?: 0)) : (row.distributionSum ?: 0)
    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 21» = («Графа 12» первичной формы – («Графа 4» первичной формы – «Графа 5» первичной формы)) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе «Графа 21» = («Графа 12» первичной формы - «Графа 6» первичной формы) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы.
    newRow.dividendSumForTaxAll = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? ((row.allSum ?: 0) - ((row.all ?: 0) - (row.rateZero ?: 0))) : ((row.allSum ?: 0) - (row.distributionSum ?: 0))
    // Если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9», то «Графа 22» = («Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6»)
    newRow.dividendSumForTaxStavka9 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 9 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }
    // Если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0», то «Графа 23» = («Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6»)
    newRow.dividendSumForTaxStavka0 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 0 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }
    // Графа 24: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8)
    newRow.taxSum = rowList.sum{ (it.status == 1 && it.type == 1 && it.withheldSum != null) ? it.withheldSum : 0 }
    // Графа 25: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8) если дата по графе 28 принадлежит ПРЕДЫДУЩЕМУ отчетному периоду
    newRow.taxSumFromPeriod = rowList.sum{ (it.status == 1 && it.type == 1 && it.withheldDate != null && it.withheldDate.before(prevPeriodEndDate) && it.withheldDate.after(prevPeriodStartDate) && it.withheldSum != null) ? it.withheldSum : 0 }
    // Графа 26: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8) если дата по графе 28 принадлежит последнему кварталу отчетного периода
    newRow.taxSumFromPeriodAll = rowList.sum{ (it.status == 1 && it.type == 1 && it.withheldDate != null && it.withheldDate.before(lastPeriodEndDate) && it.withheldDate.after(lastPeriodStartDate) && it.withheldSum != null) ? it.withheldSum : 0 }

    return newRow
}

def calcPeriod(def firstMonth, def lastMonth) {
    // «Графа 2» = «21», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «3».
    if (firstMonth==1 && lastMonth==3) {
        return '21'
    }
    // «Графа 2» = «31», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «6».
    if (firstMonth==1 && lastMonth==6) {
        return '31'
    }
    // «Графа 2» = «33», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «9».
    if (firstMonth==1 && lastMonth==9) {
        return '33'
    }
    // «Графа 2» = «34», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «12».
    if (firstMonth==1 && lastMonth==12) {
        return '34'
    }
    // «Графа 2» = «35», если «Графа 11» первичной формы - «Графа 10» первичной формы = «0».
    if ((lastMonth - firstMonth) == 0) {
        return '35'
    }
    // «Графа 2» = «36», если «Графа 11» первичной формы - «Графа 10» первичной формы = «1».
    if ((lastMonth - firstMonth) == 1) {
        return '36'
    }
    // «Графа 2» = «37», если «Графа 11» первичной формы - «Графа 10» первичной формы = «2».
    if ((lastMonth - firstMonth) == 2) {
        return '37'
    }
    // «Графа 2» = «38», если «Графа 11» первичной формы - «Графа 10» первичной формы = «3».
    if ((lastMonth - firstMonth) == 3) {
        return '38'
    }
    // «Графа 2» = «39», если «Графа 11» первичной формы - «Графа 10» первичной формы = «4».
    if ((lastMonth - firstMonth) == 4) {
        return '39'
    }
    // «Графа 2» = «40», если «Графа 11» первичной формы - «Графа 10» первичной формы = «5».
    if ((lastMonth - firstMonth) == 5) {
        return '40'
    }
    // «Графа 2» = «41», если «Графа 11» первичной формы - «Графа 10» первичной формы = «6».
    if ((lastMonth - firstMonth) == 6) {
        return '41'
    }
    // «Графа 2» = «42», если «Графа 11» первичной формы - «Графа 10» первичной формы = «7».
    if ((lastMonth - firstMonth) == 7) {
        return '42'
    }
    // «Графа 2» = «43», если «Графа 11» первичной формы - «Графа 10» первичной формы = «8».
    if ((lastMonth - firstMonth) == 8) {
        return '43'
    }
    // «Графа 2» = «44», если «Графа 11» первичной формы - «Графа 10» первичной формы = «9».
    if ((lastMonth - firstMonth) == 9) {
        return '44'
    }
    // «Графа 2» = «45», если «Графа 11» первичной формы - «Графа 10» первичной формы = «10».
    if ((lastMonth - firstMonth) == 10) {
        return '45'
    }
    // «Графа 2» = «46», если «Графа 11» первичной формы - «Графа 10» первичной формы = «11»
    if ((lastMonth - firstMonth) == 11) {
        return '46'
    }
}

void importData() {
    int COLUMN_COUNT = 26
    int HEADER_ROW_COUNT = 5
    String TABLE_START_VALUE = 'Отчетный год'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // формирвание строк нф
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
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
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
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[0][0]) : 'Отчетный год',
            (headerRows[0][1]) : 'Налоговый (отчетный) период',
            (headerRows[0][2]) : 'Эмитент',
            (headerRows[0][3]) : 'Номер решения о распределении доходов от долевого участия',
            (headerRows[0][4]) : 'Вид дивидендов',
            (headerRows[0][5]) : 'Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде',
            (headerRows[0][18]): 'Дивиденды, полученные',
            (headerRows[0][20]): 'Сумма дивидендов, используемых для исчисления налога по российским организациям:',
            (headerRows[0][23]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (headerRows[0][24]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (headerRows[0][25]): 'Сумма налога, начисленная с дивидендов, выплаченных в отчетном квартале',
            (headerRows[1][5]) : 'Всего',
            (headerRows[1][6]) : 'по которым выступает в качестве налогового агента',
            (headerRows[1][7]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России:',
            (headerRows[1][13]): 'Дивиденды, подлежащие распределению российским акционерам (участникам):',
            (headerRows[1][18]): 'Всего',
            (headerRows[1][19]): 'В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%',
            (headerRows[1][20]): 'Всего',
            (headerRows[1][21]): 'налоговая ставка 9%',
            (headerRows[1][22]): 'налоговая ставка 0%',
            (headerRows[2][7]) : 'организациям',
            (headerRows[2][8]) : 'физическим лицам',
            (headerRows[2][9]) : 'из них налоги с которых исчислены по ставке:',
            (headerRows[2][13]): 'Всего',
            (headerRows[2][14]): 'организациям',
            (headerRows[2][16]): 'физическим лицам',
            (headerRows[2][17]): 'не являющихся налогоплательщиками',
            (headerRows[3][9]) : '0%',
            (headerRows[3][10]): 'до 5% включительно',
            (headerRows[3][11]): 'свыше 5% и до 10% включительно',
            (headerRows[3][12]): 'свыше 10%',
            (headerRows[3][14]): 'налоговая ставка - 9%',
            (headerRows[3][15]): 'налоговая ставка - 0%'
    ]
    (0..25).each { index ->
        headerMapping.put((headerRows[4][index]), (index + 1).toString())
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    // графа 2
    def colIndex = 0
    newRow.financialYear = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2..4
    ['taxPeriod', 'emitent', 'decreeNumber', 'dividendType'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 5..26
    ['dividendSumRaspredPeriod', 'dividendSumNalogAgent', 'dividendForgeinOrgAll',
            'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5',
            'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianMembersAll',
            'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0', 'dividendPersonRussia',
            'dividendMembersNotRussianTax', 'dividendAgentAll', 'dividendAgentWithStavka0',
            'dividendSumForTaxAll', 'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum',
            'taxSumFromPeriod', 'taxSumFromPeriodAll'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}