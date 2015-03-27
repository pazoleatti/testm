package form_template.income.output1_2.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (03/А)
 * formTemplateId=1414
 * действует с 4 квартала 2014 года
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Bulat Kinzyabulatov
 *
 1		taCategory		            Категория налогового агента
 2		emitent		                Эмитент
 3		inn		                    ИНН организации – эмитента ценных бумаг
 4		decreeNumber		        Номер решения о распределении доходов от долевого участия
 5		dividendType		        Вид дивидендов
 6		financialYear		        Отчетный год
 7		taxPeriod		            Налоговый (отчетный) период (код)
 8		totalDividend		        Общая сумма дивидендов, подлежащая распределению российской организацией в пользу своих получателей (Д1)
 9		dividendSumRaspredPeriod	Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Всего
 10		dividendRussianTotal		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Всего
 11		dividendRussianStavka0		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Налоговая ставка 0%
 12		dividendRussianStavka6		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Налоговая ставка 9%
 13		dividendRussianStavka9		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. По иной ставке
 14		dividendRussianTaxFree		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Распределяемые в пользу акционеров (участников), не являющихся налогоплательщиками
 15		dividendRussianPersonal		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода - физическим лицам, являющимся налоговыми резидентами России
 16		dividendForgeinOrgAll		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Организациям
 17		dividendForgeinPersonalAll	Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Физическим лицам
 18		dividendStavka0		        Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. 0%
 19		dividendStavkaLess5		    Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. До 5% включительно
 20		dividendStavkaMore5		    Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Свыше 5% до 10 % включительно
 21		dividendStavkaMore10		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Свыше 10%
 22		dividendTaxUnknown		    Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – организациям и физическим лицам, налоговый статус которых не установлен
 23		dividendNonIncome		    Дивиденды, перечисленные лицам, не являющимся получателями дохода
 24		dividendAgentAll		    Дивиденды, полученные. Всего
 25		dividendAgentWithStavka0	Дивиденды, полученные. В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%
 26		dividendD1D2		        Сумма дивидендов, распределяемых в пользу всех получателей, уменьшенная на показатель строки 081 (Д1-Д2)
 27		dividendSumForTaxStavka9	Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 9%
 28		dividendSumForTaxStavka0	Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 0%
 29		taxSum		                Исчисленная сумма налога, подлежащая уплате в бюджет
 30		taxSumFromPeriod		    Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды
 31		taxSumLast		            Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего
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
        break
}

@Field
def allColumns = ['taCategory', 'financialYear', 'taxPeriod', 'emitent', 'inn', 'decreeNumber', 'dividendType',
                  'totalDividend', 'dividendSumRaspredPeriod', 'dividendRussianTotal', 'dividendRussianStavka0',
                  'dividendRussianStavka6', 'dividendRussianStavka9', 'dividendRussianTaxFree',
                  'dividendRussianPersonal', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0',
                  'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendTaxUnknown',
                  'dividendNonIncome', 'dividendAgentAll', 'dividendAgentWithStavka0', 'dividendD1D2',
                  'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumLast']

// обязательные поля (графа 1..2, 4..31)
@Field
def nonEmptyColumns = allColumns - ['inn', 'dividendAgentAll', 'dividendAgentWithStavka0']

// редактируемые поля (графа 1..31)
@Field
def editableColumns = allColumns

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
def sourceFormType = 419

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

void calc() {
    // расчетов нет, все поля редактируемые
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (def row in dataRows) {
        def rowNum = row.getIndex()
        def errorMsg = "Строка $rowNum: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка на заполнение «Графы 3»
        if ((row.taCategory == 2) != (row.inn != null && !row.inn.isEmpty())) {
            rowError(logger, row, errorMsg + "Графа «${getColumnName(row, 'inn')}» должна быть заполнена в случае если графа «${getColumnName(row, 'taCategory')}» равна «2»!")
        }

        // 3. Проверка допустимых значений «Графы 1»
        if (row.taCategory != 1 && row.taCategory != 2) {
            errorMessage(row, 'taCategory', errorMsg)
        }

        // 4. Проверка допустимых значений «Графы 7»
        if (!['13', '21', '31', '33', '34', '35', '36', '37', '38', '39', '40', '41', '42', '43',
                                 '44', '45', '46', '50'].contains(row.taxPeriod)) {
            errorMessage(row, 'taxPeriod', errorMsg)
        }

        // 5. Проверка допустимых значений «Графы 5»
        if (!['1', '2'].contains(row.dividendType)) {
            errorMessage(row, 'dividendType', errorMsg)
        }

        // 6. Проверка значения «Графы 1». Если «Графа 1» = «2», то «Графа 24» и «Графа 25» равны значению «0»
        if (row.taCategory == 2) {
            ['dividendAgentAll', 'dividendAgentWithStavka0'].each {
                if (row[it] != null && row[it] != 0) {
                    errorMessage(row, it, errorMsg)
                }
            }
        }
        // 7. Проверка значения «Графы 26»
        if (row.dividendD1D2 < 0) {
            // графа 27..31
            ['dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumLast'].each {
                if (row[it] != 0) {
                    errorMessage(row, it, errorMsg)
                }
            }
        }

        // 8. Проверка правильности расчета «Графы 9»
        if (row.dividendSumRaspredPeriod != calc9(row)) {
            warnMessage9or10(row, 'dividendSumRaspredPeriod', '«Графа 9» = «Графа 10» + «Графа 15» + «Графа 16» + «Графа 17» + «Графа 22»')
        }

        // 9. Проверка правильности расчета «Графы 10»
        if (row.dividendRussianTotal != calc10(row)) {
            warnMessage9or10(row, 'dividendRussianTotal', '«Графа 10» = «Графа 11» + «Графа 12» + «Графа 13» + «Графа 14»')
        }
    }
}

// «Графа 9» = «Графа 10» + «Графа 15» + «Графа16» + «Графа 17» + «Графа 22»
def calc9( def row) {
    def tmp = ['dividendRussianTotal', 'dividendRussianPersonal', 'dividendForgeinOrgAll',
            'dividendForgeinPersonalAll', 'dividendTaxUnknown'].sum { alias ->
        return (row[alias] ?: 0)
    }
    return roundValue(tmp)
}

// «Графа 10» = «Графа 11» + «Графа 12» + «Графа13» + «Графа 14»
def calc10( def row) {
    def tmp = ['dividendRussianStavka0', 'dividendRussianStavka6', 'dividendRussianStavka9',
            'dividendRussianTaxFree'].sum { alias ->
        return (row[alias] ?: 0)
    }
    return roundValue(tmp)
}

void errorMessage(def row, def alias, def errorMsg) {
    rowError(logger, row, errorMsg + "Графа «${getColumnName(row, alias)}» заполнена неверно!")
}

void warnMessage9or10(def row, def alias, def condition) {
    def index = row.getIndex()
    def name = getColumnName(row, alias)
    logger.warn("Строка $index: Графа «$name» заполнена неверно! Не выполняется условие: $condition")
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def prevPeriodStartDate = reportPeriodService.getCalendarStartDate(prevReportPeriod.id).time
    def prevPeriodEndDate = reportPeriodService.getEndDate(prevReportPeriod.id).time

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
                    def newRow = formNewRow(sourceRows, prevPeriodStartDate, prevPeriodEndDate, lastPeriodStartDate, lastPeriodEndDate)
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

def formNewRow(def rowList, def prevPeriodStartDate, def prevPeriodEndDate, def lastPeriodStartDate, def lastPeriodEndDate) {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    // беру первую строку
    def row = rowList[0]

    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» И «Графа 3» первичной формы = «7707083893», то «Графа 1» = «1», иначе «Графа 1» = «2»
    newRow.taCategory = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? 1 : 2

    // «Графа 2» = «Графа 2» первичной формы
    newRow.emitent = row.emitentName

    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» И «Графа 3» первичной формы = «7707083893», то «Графа 3»  не заполняется, иначе «Графа 3» = «Графа 3» первичной формы
    newRow.inn = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? null : row.emitentInn

    // «Графа 4» = «Графа 7» первичной формы
    newRow.decreeNumber = row.decisionNumber

    // Если «Графа 10» первичной формы = «1» и «Графа 11» = «12», то «Графа 5» = «2», иначе «Графа 5» = «1»
    newRow.dividendType = (row.firstMonth == 1 && row.lastMonth == 12) ? '2' : '1'

    // «Графа 6» = «Графа 9» первичной формы
    newRow.financialYear = row.year

    // «Графа 7»
    newRow.taxPeriod = calcPeriod(row.firstMonth, row.lastMonth)

    // «Графа 8» = «Графа 12» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы
    newRow.totalDividend = row.allSum

    // «Графа 9» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы
    newRow.dividendSumRaspredPeriod = rowList.sum{ it.dividends ?: 0 }

    // «Графа 10» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS»
    newRow.dividendRussianTotal = rowList.sum{ (it.status == 'RUS' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 11» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0»
    newRow.dividendRussianStavka0 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 0 && it.dividends != null) ? it.dividends : 0 }

    // «Графа 12» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9»
    // «Графа 15» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9»
    newRow.dividendRussianStavka6 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 9 && it.dividends != null) ? it.dividends : 0 }

    // «Графа 13» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы не равна «9» и «0»
    newRow.dividendRussianStavka9 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate != 9 && it.rate != 0 && it.dividends != null) ? it.dividends : 0 }

    // «Графа 14» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы не заполенена
    newRow.dividendRussianTaxFree = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == null && it.dividends != null) ? it.dividends : 0 }

    // «Графа 15» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «2»
    newRow.dividendRussianPersonal = rowList.sum{ (it.status == 'RUS' && it.type == 2 && it.dividends != null) ? it.dividends : 0 }

    // «Графа 16» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «1» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendForgeinOrgAll = rowList.sum{ (it.type == 1 && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 17» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «2» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendForgeinPersonalAll = rowList.sum{ (it.type == 2 && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 18» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы = «0» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavka0 = rowList.sum{ (it.rate == 0 && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 19» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы <= «5» и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaLess5 = rowList.sum{ (!(it.rate > 5) && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 20» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «5» и <= «10»  и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaMore5 = rowList.sum{ ((it.rate > 5 && !(it.rate > 10)) && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 21» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «10»  и «Графа 17» первичной формы не равна «RUS»
    newRow.dividendStavkaMore10 = rowList.sum{ ((it.rate > 10) && it.status != 'RUS' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 22» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» = «3»
    newRow.dividendTaxUnknown = rowList.sum{ (it.status == '3' && it.dividends != null) ? it.dividends : 0 }

    // «Графа 23» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» = «4»
    newRow.dividendNonIncome = rowList.sum{ (it.status == '4' && it.dividends != null) ? it.dividends : 0 }

    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 24» = «Графа 4» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе не заполняется
    newRow.dividendAgentAll = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String && row.all != null) ? row.all : null

    // «Графа 25» = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 25» =(«Графа 4» первичной формы - «Графа 5» первичной формы) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе не заполняется
    newRow.dividendAgentWithStavka0 = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? ((row.all ?: 0) - (row.rateZero ?: 0)) : null

    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 26» = («Графа 12» первичной формы – («Графа 4» первичной формы – «Графа 5» первичной формы)) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе «Графа 26» = «Графа 6» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы.
    newRow.dividendD1D2 =  ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? ((row.allSum ?: 0) - ((row.all ?: 0) - (row.rateZero ?: 0))) : (row.distributionSum ?: 0)

    // Вычисляется для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы:
    // Если «Графа 17» первичной формы = «RUS»и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9», то «Графа 27» = (Сумма по «Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6» первичной формы)
    newRow.dividendSumForTaxStavka9 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 9 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }

    // Вычисляется для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы:
    // Если «Графа 17» первичной формы = «RUS»и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0», то «Графа 28» = (Сумма по «Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6» первичной формы)
    newRow.dividendSumForTaxStavka0 = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.rate == 0 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }

    // Графа 29: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8)
    newRow.taxSum = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.withheldSum != null) ? it.withheldSum : 0 }

    // Графа 30: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8) если дата по графе 28 принадлежит ПРЕДЫДУЩЕМУ отчетному периоду
    newRow.taxSumFromPeriod = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.withheldDate != null && it.withheldDate.before(prevPeriodEndDate) && it.withheldDate.after(prevPeriodStartDate) && it.withheldSum != null) ? it.withheldSum : 0 }

    // Графа 31: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8) если дата по графе 28 принадлежит последнему кварталу отчетного периода
    newRow.taxSumLast = rowList.sum{ (it.status == 'RUS' && it.type == 1 && it.withheldDate != null && it.withheldDate.before(lastPeriodEndDate) && it.withheldDate.after(lastPeriodStartDate) && it.withheldSum != null) ? it.withheldSum : 0 }

    return newRow
}

def calcPeriod(def firstMonth, def lastMonth) {
    // «Графа 7» = «21», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «3».
    if (firstMonth==1 && lastMonth==3) {
        return '21'
    }
    // «Графа 7» = «31», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «6».
    if (firstMonth==1 && lastMonth==6) {
        return '31'
    }
    // «Графа 7» = «33», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «9».
    if (firstMonth==1 && lastMonth==9) {
        return '33'
    }
    // «Графа 7» = «34», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «12».
    if (firstMonth==1 && lastMonth==12) {
        return '34'
    }
    // «Графа 7» = «35», если «Графа 11» первичной формы - «Графа 10» первичной формы = «0».
    if ((lastMonth - firstMonth) == 0) {
        return '35'
    }
    // «Графа 7» = «36», если «Графа 11» первичной формы - «Графа 10» первичной формы = «1».
    if ((lastMonth - firstMonth) == 1) {
        return '36'
    }
    // «Графа 7» = «37», если «Графа 11» первичной формы - «Графа 10» первичной формы = «2».
    if ((lastMonth - firstMonth) == 2) {
        return '37'
    }
    // «Графа 7» = «38», если «Графа 11» первичной формы - «Графа 10» первичной формы = «3».
    if ((lastMonth - firstMonth) == 3) {
        return '38'
    }
    // «Графа 7» = «39», если «Графа 11» первичной формы - «Графа 10» первичной формы = «4».
    if ((lastMonth - firstMonth) == 4) {
        return '39'
    }
    // «Графа 7» = «40», если «Графа 11» первичной формы - «Графа 10» первичной формы = «5».
    if ((lastMonth - firstMonth) == 5) {
        return '40'
    }
    // «Графа 7» = «41», если «Графа 11» первичной формы - «Графа 10» первичной формы = «6».
    if ((lastMonth - firstMonth) == 6) {
        return '41'
    }
    // «Графа 7» = «42», если «Графа 11» первичной формы - «Графа 10» первичной формы = «7».
    if ((lastMonth - firstMonth) == 7) {
        return '42'
    }
    // «Графа 7» = «43», если «Графа 11» первичной формы - «Графа 10» первичной формы = «8».
    if ((lastMonth - firstMonth) == 8) {
        return '43'
    }
    // «Графа 7» = «44», если «Графа 11» первичной формы - «Графа 10» первичной формы = «9».
    if ((lastMonth - firstMonth) == 9) {
        return '44'
    }
    // «Графа 7» = «45», если «Графа 11» первичной формы - «Графа 10» первичной формы = «10».
    if ((lastMonth - firstMonth) == 10) {
        return '45'
    }
    // «Графа 7» = «46», если «Графа 11» первичной формы - «Графа 10» первичной формы = «11».
    if ((lastMonth - firstMonth) == 11) {
        return '46'
    }
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Категория налогового агента', null, 31, 5)
    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 31, 5)
    def headerMapping = [
            (xml.row[0].cell[0]): 'Категория налогового агента',
            (xml.row[0].cell[1]): 'Эмитент',
            (xml.row[0].cell[2]): 'ИНН организации – эмитента ценных бумаг',
            (xml.row[0].cell[3]): 'Номер решения о распределении доходов от долевого участия',
            (xml.row[0].cell[4]): 'Вид дивидендов',
            (xml.row[0].cell[5]): 'Отчетный год',
            (xml.row[0].cell[6]): 'Налоговый (отчетный) период (код)',
            (xml.row[0].cell[7]): 'Общая сумма дивидендов, подлежащая распределению российской организацией в пользу своих получателей (Д1)',
            (xml.row[0].cell[8]): 'Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде',
            (xml.row[0].cell[22]): 'Дивиденды, перечисленные лицам, не являющимся получателями дохода',
            (xml.row[0].cell[23]): 'Дивиденды, полученные',
            (xml.row[0].cell[25]): 'Сумма дивидендов, распределяемых в пользу всех получателей, уменьшенная на показатель строки 081 (Д1-Д2)',
            (xml.row[0].cell[26]): 'Сумма дивидендов, используемая для исчисления налога, по российским организациям:',
            (xml.row[0].cell[28]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (xml.row[0].cell[29]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (xml.row[0].cell[30]): 'Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего',

            (xml.row[1].cell[8]): 'всего',
            (xml.row[1].cell[9]): 'Дивиденды, начисленные получателям дохода – российским организациям',
            (xml.row[1].cell[14]): 'Дивиденды, начисленные получателям дохода - физическим лицам, являющимся налоговыми резидентами России',
            (xml.row[1].cell[15]): 'Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России',
            (xml.row[1].cell[21]): 'Дивиденды, начисленные получателям дохода – организациям и физическим лицам, налоговый статус которых не установлен',
            (xml.row[1].cell[23]): 'всего',
            (xml.row[1].cell[24]): 'в т.ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%',
            (xml.row[1].cell[26]): 'налоговая ставка 9%',
            (xml.row[1].cell[27]): 'налоговая ставка 0%',

            (xml.row[2].cell[9]): 'всего',
            (xml.row[2].cell[10]): 'налоговая ставка 0%',
            (xml.row[2].cell[11]): 'налоговая ставка 9%',
            (xml.row[2].cell[12]): 'по иной ставке',
            (xml.row[2].cell[13]): 'распределяемые в пользу акционеров (участников), не являющихся налогоплательщиками',
            (xml.row[2].cell[15]): 'организациям',
            (xml.row[2].cell[16]): 'физическим лицам',
            (xml.row[2].cell[17]): 'Из них налоги, с которых исчислены по ставке:',

            (xml.row[3].cell[17]): '0%',
            (xml.row[3].cell[18]): 'до 5% включительно',
            (xml.row[3].cell[19]): 'свыше 5% до 10 % включительно',
            (xml.row[3].cell[20]): 'свыше 10%'
    ]
    (1..31).each { index ->
        headerMapping.put((xml.row[4].cell[index - 1]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 4)
}

void addData(def xml, def headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        newRow.taCategory = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графs 2-5
        for (alias in ['emitent', 'inn', 'decreeNumber', 'dividendType']) {
            newRow[alias] = row.cell[xmlIndexCol].text()
            xmlIndexCol++
        }

        // графа 6
        newRow.financialYear = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        newRow.taxPeriod = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графы 8-31
        for (alias in ['totalDividend', 'dividendSumRaspredPeriod', 'dividendRussianTotal', 'dividendRussianStavka0',
                       'dividendRussianStavka6', 'dividendRussianStavka9', 'dividendRussianTaxFree',
                       'dividendRussianPersonal', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0',
                       'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendTaxUnknown',
                       'dividendNonIncome', 'dividendAgentAll', 'dividendAgentWithStavka0', 'dividendD1D2',
                       'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumLast']) {
            newRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++
        }
        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}

def roundValue(def value, int precision = 0) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}