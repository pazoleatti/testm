package form_template.income.output1_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.text.SimpleDateFormat

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
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
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
                       'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSumFromPeriod', 'taxSumFromPeriodAll']

// 7, 8 графа источника
@Field
def keyColumns = ['decisionNumber', 'decisionDate']

@Field
def sbString = "ОАО Сбербанк России"

@Field
def graph3String = "7707083893"

@Field
def sourceFormType = 419

@Field
def startDate = null

@Field
def endDate = null

@Field
def format = new SimpleDateFormat('dd.MM.yyyy')

@Field
def formatY = new SimpleDateFormat('yyyy')

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

    sortFormDataRows()
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    def periodStartDate = getReportPeriodStartDate()
    def periodEndDate = getReportPeriodEndDate()

    def lastPeriod = getLastReportPeriod()
    def lastPeriodStartDate = reportPeriodService.getCalendarStartDate(lastPeriod.id).time
    def lastPeriodEndDate = reportPeriodService.getEndDate(lastPeriod.id).time

    // получить формы-источники в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if(it.formTypeId == sourceFormType) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceHelper = formDataService.getDataRowHelper(sourceFormData)
                def rowMap = getRowMap(sourceHelper.getAll())
                rowMap.each { key, sourceRows ->
                    def newRow = formNewRow(sourceRows, periodStartDate, periodEndDate, lastPeriodStartDate, lastPeriodEndDate)
                    dataRows.add(newRow)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
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

def formNewRow(def rowList, def periodStartDate, def periodEndDate, def lastPeriodStartDate, def lastPeriodEndDate) {
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
    newRow.dividendSumRaspredPeriod = rowList.sum{ it.allSum ?: 0 }
    // «Графа 7» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы
    newRow.dividendSumNalogAgent = rowList.sum{ it.dividends ?: 0 }
    // «Графа 8» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «1» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendForgeinOrgAll = rowList.sum{ (it.type == 1 && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }
    // «Графа 9» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «2» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendForgeinPersonalAll = rowList.sum{ (it.type == 2 && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }
    // «Графа 10» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы = «0» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavka0 = rowList.sum{ (it.rate == 0 && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }
    // «Графа 11» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы <= «5» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaLess5 = rowList.sum{ (!(it.rate > 5) && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }
    // «Графа 12» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «5» и <= «10» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaMore5 = rowList.sum{ ((it.rate > 5 && !(it.rate > 10)) && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }
    // «Графа 13» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «10» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaMore10 = rowList.sum{ ((it.rate > 10) && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }
    // «Графа 14» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS»
    newRow.dividendRussianMembersAll = rowList.sum{ (it.status == 'RUS' && it.dividends != null) ? it.dividends : 0 }
    // «Графа 15» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9»
    newRow.dividendRussianOrgStavka9 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 9 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 16» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0»
    newRow.dividendRussianOrgStavka0 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 0 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 17» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «2»
    newRow.dividendPersonRussia = rowList.sum{ (it.status == 'RUS' && it.type == 2 && it.dividends != null) ? it.dividends : 0 }
    // «Графа 18» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = 4
    newRow.dividendMembersNotRussianTax = rowList.sum{ (it.status == '4' && it.dividends != null) ? it.dividends : 0 }
    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 19» = «Графа 4» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе не заполняется
    newRow.dividendAgentAll = (row.emitentName == sbString && row.emitentInn == graph3String && row.all != null) ? row.all : 0
    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 20» = («Графа 4» первичной формы – «Графа 5» первичной формы) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе «Графа 20» = «Графа 6» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы.
    newRow.dividendAgentWithStavka0 = (row.emitentName == sbString && row.emitentInn == graph3String) ? ((row.all ?: 0) - (row.rateZero ?: 0)) : (row.distributionSum ?: 0)
    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 21» = («Графа 12» первичной формы – («Графа 4» первичной формы – «Графа 5» первичной формы)) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе «Графа 21» = («Графа 12» первичной формы - «Графа 6» первичной формы) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы.
    newRow.dividendSumForTaxAll = (row.emitentName == sbString && row.emitentInn == graph3String) ? ((row.allSum ?: 0) - ((row.all ?: 0) - (row.rateZero ?: 0))) : ((row.allSum ?: 0) - (row.distributionSum ?: 0))
    // Если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9», то «Графа 22» = («Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6»)
    newRow.dividendSumForTaxStavka9 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 9 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }
    // Если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0», то «Графа 23» = («Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6»)
    newRow.dividendSumForTaxStavka0 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 0 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }
    // «Графа 24» = Сумма по «Графа 27» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы
    newRow.taxSum = rowList.sum{ it.withheldSum ?: 0 }
    // «Графа 25» = Сумма по «Графа 27» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если дата по «Графе 28 » первичной формы принадлежит текущему отчетному периоду
    newRow.taxSumFromPeriod = rowList.sum{ if (it.withheldDate != null && it.withheldDate.before(periodEndDate) && it.withheldDate.after(periodStartDate)) { it.withheldSum ?: 0 } else { 0 } }
    // «Графа 26» = Сумма по «Графа 27» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если дата по «Графе 28 » первичной формы принадлежит последнему кварталу отчетного года
    newRow.taxSumFromPeriodAll = rowList.sum{ if (it.withheldDate != null && it.withheldDate.before(lastPeriodEndDate) && it.withheldDate.after(lastPeriodStartDate)) { it.withheldSum ?: 0 } else { 0 }}

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
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Отчетный год', null, 26, 5)

    checkHeaderSize(xml.row[4].cell.size(), xml.row.size(), 26, 5)

    def headerMapping = [
            (xml.row[0].cell[0]) : 'Отчетный год',
            (xml.row[0].cell[1]) : 'Налоговый (отчетный) период',
            (xml.row[0].cell[2]) : 'Эмитент',
            (xml.row[0].cell[3]) : 'Номер решения о распределении доходов от долевого участия',
            (xml.row[0].cell[4]) : 'Вид дивидендов',
            (xml.row[0].cell[5]) : 'Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде',
            (xml.row[0].cell[18]): 'Дивиденды, полученные',
            (xml.row[0].cell[20]): 'Сумма дивидендов, используемых для исчисления налога по российским организациям:',
            (xml.row[0].cell[23]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (xml.row[0].cell[24]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (xml.row[0].cell[25]): 'Сумма налога, начисленная с дивидендов, выплаченных в отчетном квартале',
            (xml.row[1].cell[5]) : 'Всего',
            (xml.row[1].cell[6]) : 'по которым выступает в качестве налогового агента',
            (xml.row[1].cell[7]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России:',
            (xml.row[1].cell[13]): 'Дивиденды, подлежащие распределению российским акционерам (участникам):',
            (xml.row[1].cell[18]): 'Всего',
            (xml.row[1].cell[19]): 'В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%',
            (xml.row[1].cell[20]): 'Всего',
            (xml.row[1].cell[21]): 'налоговая ставка 9%',
            (xml.row[1].cell[22]): 'налоговая ставка 0%',
            (xml.row[2].cell[7]) : 'организациям',
            (xml.row[2].cell[8]) : 'физическим лицам',
            (xml.row[2].cell[9]) : 'из них налоги с которых исчислены по ставке:',
            (xml.row[2].cell[13]): 'Всего',
            (xml.row[2].cell[14]): 'организациям',
            (xml.row[2].cell[16]): 'физическим лицам',
            (xml.row[2].cell[17]): 'не являющихся налогоплательщиками',
            (xml.row[3].cell[9]) : '0%',
            (xml.row[3].cell[10]): 'до 5% включительно',
            (xml.row[3].cell[11]): 'свыше 5% и до 10% включительно',
            (xml.row[3].cell[12]): 'свыше 10%',
            (xml.row[3].cell[14]): 'налоговая ставка - 9%',
            (xml.row[3].cell[15]): 'налоговая ставка - 0%'
    ]
    (0..25).each { index ->
        headerMapping.put((xml.row[4].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 5)
}

void addData(def xml, headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    // количество графов в таблице
    def columnCount = 26
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        if (row.cell.size() >= columnCount) {
            def newRow = formData.createDataRow()
            newRow.setIndex(rowIndex++)
            editableColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }

            def xmlIndexCol = 0
            newRow.financialYear = parseDate(row.cell[xmlIndexCol].text(), "yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 1
            newRow.taxPeriod = row.cell[xmlIndexCol].text()
            xmlIndexCol = 2
            newRow.emitent = row.cell[xmlIndexCol].text()
            xmlIndexCol = 3
            newRow.decreeNumber = row.cell[xmlIndexCol].text()
            xmlIndexCol = 4
            newRow.dividendType = row.cell[xmlIndexCol].text()
            xmlIndexCol = 5
            newRow.dividendSumRaspredPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 6
            newRow.dividendSumNalogAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 7
            newRow.dividendForgeinOrgAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 8
            newRow.dividendForgeinPersonalAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 9
            newRow.dividendStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 10
            newRow.dividendStavkaLess5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 11
            newRow.dividendStavkaMore5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 12
            newRow.dividendStavkaMore10 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 13
            newRow.dividendRussianMembersAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 14
            newRow.dividendRussianOrgStavka9 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 15
            newRow.dividendRussianOrgStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 16
            newRow.dividendPersonRussia = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 17
            newRow.dividendMembersNotRussianTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 18
            newRow.dividendAgentAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 19
            newRow.dividendAgentWithStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 20
            newRow.dividendSumForTaxAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 21
            newRow.dividendSumForTaxStavka9 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 22
            newRow.dividendSumForTaxStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 23
            newRow.taxSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 24
            newRow.taxSumFromPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 25
            newRow.taxSumFromPeriodAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

            rows.add(newRow)
        }
    }
    dataRowHelper.save(rows)
}

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}
