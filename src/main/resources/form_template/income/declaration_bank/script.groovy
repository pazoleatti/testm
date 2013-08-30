package form_template.income.declaration_bank

import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType

/**
 * Формирование XML для декларации налога на прибыль.
 *
 * @version 4
 *
 * @author rtimerbaev
 */

def needLogicalCheck = false

switch (formDataEvent) {
    // создать / обновить
    case FormDataEvent.CREATE :
        break
    // проверить
    case FormDataEvent.CHECK :
        needLogicalCheck = true
        break
    // принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        needLogicalCheck = true
        break
    // после принять из создана
    case FormDataEvent.AFTER_MOVE_CREATED_TO_ACCEPTED :
        break
    // из принять в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        return
        break
}

/*
 * Константы.
 */
def empty = 0
/** Костыльные пустые значения для отсутсвтующих блоков (=null или 0). */
def emptyNull = 0
def knd = '1151006'
def kbk = '18210101011011000110'
def kbk2 = '18210101012021000110'
def typeNP = '1'

def isBank = true
def departmentId = declarationData.departmentId
def reportPeriodId = declarationData.reportPeriodId

// справочник "Параметры подразделения по налогу на прибыль" - начало
def departmentParamIncomeRefDataProvider = refBookFactory.getDataProvider(33)
def departmentParamIncomeRecords = departmentParamIncomeRefDataProvider.getRecords(new Date(), null,
        "DEPARTMENT_ID = '" + departmentId + "'", null);
if (departmentParamIncomeRecords == null || departmentParamIncomeRecords.getRecords().isEmpty()) {
   throw new Exception("Не удалось получить настройки обособленного подразделения.")
}
def incomeParams = departmentParamIncomeRecords.getRecords().getAt(0)
if (incomeParams == null) {
    throw new Exception("Ошибка при получении настроек обособленного подразделения.")
}
def reorgFormCode = refBookService.getStringValue(5, getValue(incomeParams, 'REORG_FORM_CODE'), 'CODE')
def taxOrganCode = getValue(incomeParams, 'TAX_ORGAN_CODE')
def okvedCode = refBookService.getStringValue(34, getValue(incomeParams, 'OKVED_CODE'), 'CODE')
def phone = getValue(incomeParams, 'PHONE')
def name = getValue(incomeParams, 'NAME')
def inn = getValue(incomeParams, 'INN')
def kpp = getValue(incomeParams, 'KPP')
def reorgInn = getValue(incomeParams, 'REORG_INN')
def reorgKpp = getValue(incomeParams, 'REORG_KPP')
def okato = refBookService.getStringValue(3, getValue(incomeParams, 'OKATO'), 'OKATO')
def signatoryId = getValue(incomeParams, 'SIGNATORY_ID')
def taxRate = getValue(incomeParams, 'TAX_RATE')
def sumTax = getValue(incomeParams, 'SUM_TAX') // вместо departmentParamIncome.externalTaxSum
def appVersion = getValue(incomeParams, 'APP_VERSION')
def formatVersion = getValue(incomeParams, 'FORMAT_VERSION')
def taxPlaceTypeCode = refBookService.getStringValue(2, getValue(incomeParams, 'TAX_PLACE_TYPE_CODE'), 'CODE')
def signatorySurname = getValue(incomeParams, 'SIGNATORY_SURNAME')
def signatoryFirstName = getValue(incomeParams, 'SIGNATORY_FIRSTNAME')
def signatoryLastName = getValue(incomeParams, 'SIGNATORY_LASTNAME')
def approveDocName = getValue(incomeParams, 'APPROVE_DOC_NAME')
def approveOrgName = getValue(incomeParams, 'APPROVE_ORG_NAME')
def sumDividends = getValue(incomeParams, 'SUM_DIVIDENDS')
// справочник "Параметры подразделения по налогу на прибыль" - конец

// Проверки значений справочника "Параметры подразделения по налогу на прибыль"
def hasError = false
if (taxRate == null) {
    logger.error("Для подразделения $name не задан параметр \"Ставка налога\"")
    hasError = true
}
if (sumTax == null) {
    logger.error("Для подразделения $name не задан параметр \"Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде\"")
    hasError = true
}
if (hasError) {
    return
}

/** Отчётный период. */
def reportPeriod = reportPeriodService.get(reportPeriodId)

/** Предыдущий отчётный период. */
def prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriodId)

/** ПРЕД_ПРЕД_ыдущий отчётный период. */
def prevReportPeriod2 = (prevReportPeriod != null ? reportPeriodService.getPrevReportPeriod(prevReportPeriod.id) : null)

/** Предыдущий отчётный период. */
def reportPeriodOld = reportPeriodService.get(reportPeriod.getId())

/** Налоговый период. */
def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)

/** Признак налоговый ли это период. */
def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)

/** Признак первый ли это отчетный период. */
def isFirstPeriod = (reportPeriod != null && reportPeriod.order == 1)

/** Признак первый ли это отчетный период. */
def isFirstPeriodOld = (reportPeriodOld != null && reportPeriodOld.order == 1)

/*
 * Данные налоговых форм.
 */

def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)
if (formDataCollection == null || formDataCollection.records.isEmpty()) {
    logger.error('Отсутствуют выходные или сводные налоговые формы в статусе "Принят". Формирование декларации невозможно.')
    return
}

/** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
def formDataComplexIncome = formDataCollection.find(departmentId, 302, FormDataKind.SUMMARY)
def dataRowsHelperComplexIncome = getDataRowHelper(formDataComplexIncome)

/** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
def formDataSimpleIncome = formDataCollection.find(departmentId, 301, FormDataKind.SUMMARY)
def dataRowsHelperSimpleIncome = getDataRowHelper(formDataSimpleIncome)

/** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
def formDataComplexConsumption = formDataCollection.find(departmentId, 303, FormDataKind.SUMMARY)
def dataRowsHelperComplexConsumption = getDataRowHelper(formDataComplexConsumption)

/** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
def formDataSimpleConsumption = formDataCollection.find(departmentId, 304, FormDataKind.SUMMARY)
def dataRowsHelperSimpleConsumption = getDataRowHelper(formDataSimpleConsumption)

/** Выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
def formDataAdvance = formDataCollection.find(departmentId, 500, FormDataKind.SUMMARY)
def dataRowsHelperAdvance = getDataRowHelper(formDataAdvance)

/** Сведения для расчёта налога с доходов в виде дивидендов. */
def formDataDividend = formDataCollection.find(departmentId, 306, FormDataKind.ADDITIONAL)
def dataRowsHelperDividend = getDataRowHelper(formDataDividend)

/** Расчет налога на прибыль с доходов, удерживаемого налоговым агентом. */
def formDataTaxAgent = formDataCollection.find(departmentId, 307, FormDataKind.ADDITIONAL)
def dataRowsHelperTaxAgent = getDataRowHelper(formDataTaxAgent)

/** Выходная налоговая форма «Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика». */
def formDataTaxSum = formDataCollection.find(departmentId, 308, FormDataKind.ADDITIONAL)
def dataRowsHelperTaxSum = getDataRowHelper(formDataTaxSum)

/*
 * Данные налоговых форм за предыдущий период.
 */

/** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
def dataRowsHelperComplexIncomeOld = null

/** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
def dataRowsHelperSimpleIncomeOld = null

/** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
def dataRowsHelperComplexConsumptionOld = null

/** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
def dataRowsHelperSimpleConsumptionOld = null

/** Выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
def dataRowsHelperAdvanceOld = null

if (prevReportPeriod != null) {
    def formDataComplexIncomeOld = formDataService.find(302, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    dataRowsHelperComplexIncomeOld = getDataRowHelper(formDataComplexIncomeOld)

    def formDataSimpleIncomeOld = formDataService.find(301, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    dataRowsHelperSimpleIncomeOld = getDataRowHelper(formDataSimpleIncomeOld)

    def formDataComplexConsumptionOld = formDataService.find(303, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    dataRowsHelperComplexConsumptionOld = getDataRowHelper(formDataComplexConsumptionOld)

    def formDataSimpleConsumptionOld = formDataService.find(304, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    dataRowsHelperSimpleConsumptionOld = getDataRowHelper(formDataSimpleConsumptionOld)

    def formDataAdvanceOld = formDataService.find(500, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    dataRowsHelperAdvanceOld = getDataRowHelper(formDataAdvanceOld)
}
def hasPrevPeriodDeclaration = false
if (dataRowsHelperComplexIncomeOld != null && dataRowsHelperSimpleIncomeOld != null &&
        dataRowsHelperComplexConsumptionOld != null && dataRowsHelperSimpleConsumptionOld != null &&
        dataRowsHelperAdvanceOld != null) {
    hasPrevPeriodDeclaration = true
}

/*
 * Данные налоговых форм за ПРЕД_ПРЕД_ыдущий период.
 */

/** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
def dataRowsHelperComplexIncomeOld2 = null

/** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
def dataRowsHelperSimpleIncomeOld2 = null

/** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
def dataRowsHelperComplexConsumptionOld2 = null

/** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
def dataRowsHelperSimpleConsumptionOld2 = null

/** Выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
def dataRowsHelperAdvanceOld2 = null

if (prevReportPeriod2 != null) {
    def formDataComplexIncomeOld2 = formDataService.find(302, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    dataRowsHelperComplexIncomeOld2 = getDataRowHelper(formDataComplexIncomeOld2)

    def formDataSimpleIncomeOld2 = formDataService.find(301, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    dataRowsHelperSimpleIncomeOld2 = getDataRowHelper(formDataSimpleIncomeOld2)

    def formDataComplexConsumptionOld2 = formDataService.find(303, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    dataRowsHelperComplexConsumptionOld2 = getDataRowHelper(formDataComplexConsumptionOld2)

    def formDataSimpleConsumptionOld2 = formDataService.find(304, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    dataRowsHelperSimpleConsumptionOld2 = getDataRowHelper(formDataSimpleConsumptionOld2)

    def formDataAdvanceOld2 = formDataService.find(500, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    dataRowsHelperAdvanceOld2 = getDataRowHelper(formDataAdvanceOld2)
}

def hasPrevPeriodDeclarationOld = false
if (dataRowsHelperComplexIncomeOld2 != null && dataRowsHelperSimpleIncomeOld2 != null &&
        dataRowsHelperComplexConsumptionOld2 != null && dataRowsHelperSimpleConsumptionOld2 != null &&
        dataRowsHelperAdvanceOld2 != null) {
    hasPrevPeriodDeclarationOld = true
}

/*
 * Расчет значений для текущей декларации.
 */

/** Период. */
def period = 0
if (reorgFormCode == 50) {
    period = 50
} else if (reportPeriod.order != null) {
    def values = [21, 31, 33, 34]
    period = values[reportPeriod.order - 1]
}

/** ВыручРеалТов. Код строки декларации 180. */
def viruchRealTov = empty;
/** ДохДоговДУИ. Код строки декларации 210. */
def dohDolgovDUI = empty;
/** ДохДоговДУИ_ВнР. Код строки декларации 211. */
def dohDolgovDUI_VnR = empty;
/** УбытОбОбслНеобл. Код строки декларации 201. */
def ubitObObslNeobl = empty;
/** УбытДоговДУИ. Код строки декларации 230. */
def ubitDogovDUI = empty;
/** УбытПрошПер. Код строки декларации 301. */
def ubitProshPer = empty;
/** СумБезнадДолг. Код строки декларации 302. */
def sumBeznalDolg = empty;
/** УбытПриравнВс. Код строки декларации 300. */
def ubitPriravnVs = ubitProshPer + sumBeznalDolg

/** ПрПодп. */
def prPodp = signatoryId
/** ВырРеалТовСоб. Код строки декларации 011. */
def virRealTovSob = getVirRealTovSob(dataRowsHelperComplexIncome, dataRowsHelperSimpleIncome)
/** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10855, 10880, 10900. */
def virRealImPrav = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10855, 10880, 10900])
/** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10850. */
def virRealImProch = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10850])
/** ВырРеалВс. Код строки декларации 010. */
def virRealVs = getLong(virRealTovSob + virRealImPrav + virRealImProch)
/** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260. */
def virRealCBVs = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260])
/** ВырРеалПред. Код строки декларации 023. */
def virRealPred = empty
/** ВыручОп302Ит. Код строки декларации 340. */
def viruchOp302It = getViruchOp302It(dataRowsHelperComplexIncome, viruchRealTov, dohDolgovDUI, dohDolgovDUI_VnR)
/** ДохРеал, ВырРеалИтог. */
def dohReal = getLong(virRealVs + virRealCBVs + virRealPred + viruchOp302It)
/** ДохВнереал. Код строки декларации 100. */
def dohVnereal = getDohVnereal(dataRowsHelperComplexIncome, dataRowsHelperSimpleIncome)
/** ПрямРасхРеал. Код строки декларации 010. */
def pramRashReal = empty
/** ПрямРасхТоргВс. Код строки декларации 020. */
def pramRashTorgVs = empty
/** КосвРасхВс. Код строки декларации 040. */
def cosvRashVs = getCosvRashVs(dataRowsHelperComplexConsumption, dataRowsHelperSimpleConsumption)
/** РасхВнереалВС. Строка 200. */
def rashVnerealVs = getRashVnerealVs(dataRowsHelperComplexConsumption, dataRowsHelperSimpleConsumption)
/** РасхВнереал. Строка 200 + строка 300. */
def rashVnereal = rashVnerealVs + ubitPriravnVs
/** ОстСтРеалАИ. Код строки декларации 040. Код вида расхода = 21760. */
def ostStRealAI = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21760])
/** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21450, 21740, 21750. */
def realImushPrav = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21450, 21740, 21750])
/** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21770. */
def priobrRealImush = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21770])
/* АктивРеалПред. Код строки декларации 061. */
def activRealPred = empty
/** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680. */
def priobrRealCB = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680])
/** СумОтклЦен. Код строки декларации 071. Код вида расходов = 21685, 21690, 21695. */
def sumOtklCen = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21685, 21690, 21695])

/** УбытПрошОбсл. Код строки декларации 090. */
def ubitProshObsl = empty
/** СтоимРеалПТДоСр. Код строки декларации 120. Код вида расхода = 21460. */
def stoimRealPTDoSr = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21460])
/** СтоимРеалПТПосСр. Код строки декларации 130. Код вида расхода = 21470. */
def stoimRealPTPosSr = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21470])
/** РасхРеалТов. Код строки декларации 190. */
def rashRealTov = empty
/** РасхДоговДУИ. Код строки декларации 220. */
def rashDolgovDUI = empty
/** РасхДоговДУИ_ВнР. Код строки декларации 221. */
def rashDolgovDUI_VnR = empty
/** СумНевозмЗатрЗУ. Код строки декларации 250. Код вида расхода = 21385. */
def sumNevozmZatrZU = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21385])
/** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
def rashOper32 = getLong(ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr +
        rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + sumNevozmZatrZU)
/** УбытРеалАмИм. Код строки декларации 100. Код вида расхода = 21520, 21530. */
def ubitRealAmIm = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21520, 21530])
/** УбытРеалЗемУч. Код строки декларации 110. */
def ubitRealZemUch = empty
/** НадбПокПред. Код строки декларации 120. */
def nadbPokPred = empty
/** РасхУмРеал, РасхПризнИтого. Код строки декларации 130. */
def rashUmReal = getLong(pramRashReal + pramRashTorgVs + cosvRashVs + realImushPrav +
        priobrRealImush + activRealPred + priobrRealCB + rashOper32 + ubitProshObsl +
        ubitRealAmIm + ubitRealZemUch + nadbPokPred)
/** Убытки, УбытОп302. Код строки декларации 360. */
def ubitki = getUbitki(dataRowsHelperComplexConsumption, ubitObObslNeobl, ubitDogovDUI)
/** ПрибУб. */
def pribUb = getLong(dohReal + dohVnereal - rashUmReal - rashVnereal + ubitki)
/** ДохИсклПриб. */
def dohIsklPrib = getDohIsklPrib(dataRowsHelperComplexIncome, dataRowsHelperSimpleIncome)
def nalBaza = getLong(pribUb - dohIsklPrib - 0 - 0 + 0)
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIsch = getNalBazaIsch(nalBaza, 0)
/** НалИсчислФБ. Код строки декларации 190. */
def nalIschislFB = getNalIschislFB(nalBazaIsch, taxRate)
/** НалИсчислСуб. Код строки декларации 200. Столбец «Сумма налога». */
def nalIschislSub = getTotalFromForm(dataRowsHelperAdvance, 'taxSum')
/** НалИсчисл. Код строки декларации 180. */
def nalIschisl = getLong(nalIschislFB + nalIschislSub)
/** НалВыпл311. */
def nalVipl311 = getLong(sumTax)
/** НалВыпл311ФБ. Код строки декларации 250. */
def nalVipl311FB = getLong(sumTax * 2 / 20)
/** НалВыпл311Суб. Код строки декларации 260. */
def nalVipl311Sub = getLong(sumTax - nalVipl311FB)
/** НалВыпл311ФБ за предыдущий отчетный период. Код строки декларации 250. */
def nalVipl311FBOld = nalVipl311FB
/** НалВыпл311Суб за предыдущий отчетный период. Код строки декларации 260. */
def nalVipl311SubOld = getLong(sumTax - nalVipl311FBOld)

/*
 * Расчет значении декларации за ПРЕД_ПРЕД_ыдущий период.
 */

/** ВырРеалТовСоб. */
def virRealTovSobOld2 = getVirRealTovSob(dataRowsHelperComplexIncomeOld2, dataRowsHelperSimpleIncomeOld2)
/** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10855, 10880, 10900. */
def virRealImPravOld2 = getComplexIncomeSumRows9(dataRowsHelperComplexIncomeOld2, [10855, 10880, 10900])
/** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10850. */
def virRealImProchOld2 = getComplexIncomeSumRows9(dataRowsHelperComplexIncomeOld2, [10850])
/** ВырРеалВс. */
def virRealVsOld2 = getLong(virRealTovSobOld2 + virRealImPravOld2 + virRealImProchOld2)
/** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260. */
def virRealCBVsOld2 = getComplexIncomeSumRows9(dataRowsHelperComplexIncomeOld2, [11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260])
/** ВыручОп302Ит. Код строки декларации 340. */
def viruchOp302ItOld2 = getViruchOp302It(dataRowsHelperComplexIncomeOld2, viruchRealTov, dohDolgovDUI, dohDolgovDUI_VnR)
/** ДохРеал, ВырРеалИтог. */
def dohRealOld2 = getLong(virRealVsOld2 + virRealCBVsOld2 + viruchOp302ItOld2)
/** ДохВнереал. */
def dohVnerealOld2 = getDohVnereal(dataRowsHelperComplexIncomeOld2, dataRowsHelperSimpleIncomeOld2)
/** КосвРасхВс. Код строки декларации 040. */
def cosvRashVsOld2 = getCosvRashVs(dataRowsHelperComplexConsumptionOld2, dataRowsHelperSimpleConsumptionOld2)
/** РасхВнереалВС. Строка 200. */
def rashVnerealVsOld2 = getRashVnerealVs(dataRowsHelperComplexConsumptionOld2, dataRowsHelperSimpleConsumptionOld2)
/** РасхВнереал. Строка 200 + строка 300. */
def rashVnerealOld2 = rashVnerealVsOld2 + ubitPriravnVs
/** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21450, 21740, 21750. */
def realImushPravOld2 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld2, [21450, 21740, 21750])
/** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21770. */
def priobrRealImushOld2 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld2, [21770])
/** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680. */
def priobrRealCBOld2 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld2, [21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680])
/** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
def rashOper32Old2 = getLong(ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr +
        rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + sumNevozmZatrZU)
/** УбытРеалАмИм. Код вида расхода = 21520, 21530. */
def ubitRealAmImOld2 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld2, [21520, 21530])
/** УбытРеалЗемУч. Код строки декларации 110. */
def ubitRealZemUchOld2 = empty
/** РасхУмРеал, РасхПризнИтого. */
def rashUmRealOld2 = getLong(cosvRashVsOld2 + realImushPravOld2 + priobrRealImushOld2 + priobrRealCBOld2 + rashOper32Old2 + ubitRealAmImOld2 + ubitRealZemUchOld2)
/** Убытки, УбытОп302. Код строки декларации 360. */
def ubitkiOld2 = getUbitki(dataRowsHelperComplexConsumptionOld2, ubitObObslNeobl, ubitDogovDUI)
def pribUbOld2 = getLong(dohRealOld2 + dohVnerealOld2 - rashUmRealOld2 - rashVnerealOld2 + ubitkiOld2)
def dohIsklPribOld2 = getDohIsklPrib(dataRowsHelperComplexIncomeOld2, dataRowsHelperSimpleIncomeOld2)
def nalBazaOld2 = getLong(pribUbOld2 - dohIsklPribOld2 - 0 - 0 + 0)
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIschOld2 = getNalBazaIsch(nalBazaOld2, 0)
/** НалИсчислФБ. Код строки декларации 190. */
def nalIschislFBOld2 = getNalIschislFB(nalBazaIschOld2, taxRate)
/** НалИсчислСуб. Столбец «Сумма налога». */
def nalIschislSubOld2 = getTotalFromForm(dataRowsHelperAdvance, 'taxSum')

/*
 * Расчет значении декларации за предыдущий период.
 */

/** ВырРеалТовСоб. */
def virRealTovSobOld = getVirRealTovSob(dataRowsHelperComplexIncomeOld, dataRowsHelperSimpleIncomeOld)
/** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10855, 10880, 10900. */
def virRealImPravOld = getComplexIncomeSumRows9(dataRowsHelperComplexIncomeOld, [10855, 10880, 10900])
/** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10850. */
def virRealImProchOld = getComplexIncomeSumRows9(dataRowsHelperComplexIncomeOld, [10850])
/** ВырРеалВс. */
def virRealVsOld = getLong(virRealTovSobOld + virRealImPravOld + virRealImProchOld)
/** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260. */
def virRealCBVsOld = getComplexIncomeSumRows9(dataRowsHelperComplexIncomeOld, [11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260])
/** ВыручОп302Ит. Код строки декларации. */
def viruchOp302ItOld = getViruchOp302It(dataRowsHelperComplexIncomeOld, viruchRealTov, dohDolgovDUI, dohDolgovDUI_VnR)
/** ДохРеал, ВырРеалИтог. */
def dohRealOld = getLong(virRealVsOld + virRealCBVsOld + viruchOp302ItOld)
/** ДохВнереал. */
def dohVnerealOld = getDohVnereal(dataRowsHelperComplexIncomeOld, dataRowsHelperSimpleIncomeOld)
/** КосвРасхВс. Код строки декларации 040. */
def cosvRashVsOld = getCosvRashVs(dataRowsHelperComplexConsumptionOld, dataRowsHelperSimpleConsumptionOld)
/** РасхВнереалВС. Строка 200. */
def rashVnerealVsOld = getRashVnerealVs(dataRowsHelperComplexConsumptionOld, dataRowsHelperSimpleConsumptionOld)
/** РасхВнереал. Строка 200 + строка 300. */
def rashVnerealOld = rashVnerealVsOld + ubitPriravnVs
/** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21450, 21740, 21750. */
def realImushPravOld = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld, [21450, 21740, 21750])
/** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21770. */
def priobrRealImushOld = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld, [21770])
/** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680. */
def priobrRealCBOld = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld, [21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680])
/** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
def rashOper32Old = getLong(ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr +
        rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + sumNevozmZatrZU)
/** УбытРеалАмИм. Код вида расхода = 21520, 21530. */
def ubitRealAmImOld = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumptionOld, [21520, 21530])
/** УбытРеалЗемУч. Код строки декларации 110. */
def ubitRealZemUchOld = empty
/** РасхУмРеал, РасхПризнИтого. */
def rashUmRealOld = getLong(cosvRashVsOld + realImushPravOld + priobrRealImushOld + priobrRealCBOld + rashOper32Old + ubitRealAmImOld + ubitRealZemUchOld)
/** Убытки, УбытОп302. Код строки декларации 360. */
def ubitkiOld = getUbitki(dataRowsHelperComplexConsumptionOld, ubitObObslNeobl, ubitDogovDUI)
def pribUbOld = getLong(dohRealOld + dohVnerealOld - rashUmRealOld - rashVnerealOld + ubitkiOld)
def dohIsklPribOld = getDohIsklPrib(dataRowsHelperComplexIncomeOld, dataRowsHelperSimpleIncomeOld)
def nalBazaOld = getLong(pribUbOld - dohIsklPribOld - 0 - 0 + 0)
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIschOld = getNalBazaIsch(nalBazaOld, 0)
/** НалИсчислФБ. Код строки декларации 190. */
def nalIschislFBOld = getNalIschislFB(nalBazaIschOld, taxRate)
/** НалИсчислСуб. Столбец «Сумма налога». */
def nalIschislSubOld = getTotalFromForm(dataRowsHelperAdvance, 'taxSum')
// /** НалИсчисл. */ // TODO (Ramil Timerbaev) если не надо, то убрать
// def nalIschislOld = getLong(nalIschislFBOld + nalIschislSubOld)
/** АвПлатМесСуб. Код строки декларации 310. */
def avPlatMesSubOld = getLong(nalIschislSubOld - (isFirstPeriodOld ? nalIschislSubOld2 : 0))
/** АвНачислСуб. Код строки декларации 230. Столбец «Начислено налога в бюджет субъекта РФ. Расчётный [080]». */
def avNachislSubOld = getLong(nalIschislSub - nalVipl311Sub + avPlatMesSubOld)

/*
 * Расчет значений для текущей декларации (Продолжение).
 */

/** АвПлатМесФБ. Код строки декларации 300. */
def avPlatMesFB = getLong(nalIschislFB - (!isFirstPeriod && hasPrevPeriodDeclaration ? nalIschislFBOld : 0))
/** АвПлатМесФБ. Код строки декларации 300. */
def avPlatMesFBOld = getLong(nalIschislFBOld - (!isFirstPeriodOld && hasPrevPeriodDeclarationOld ? nalIschislFBOld2 : 0))
/** АвНачислФБ. Код строки декларации 220. */
def avNachislFB = (hasPrevPeriodDeclaration ? getLong(nalIschislFBOld - nalVipl311FBOld + avPlatMesFBOld) : 0)
/** АвПлатМесСуб. Код строки декларации 310. */
def avPlatMesSub = getLong(nalIschislSub - (isFirstPeriod ? 0 : nalIschislSubOld))
/** АвПлатМес. */
def avPlatMes = getLong(avPlatMesFB + avPlatMesSub)
/** АвПлатУпл1КвФБ. */
def avPlatUpl1CvFB = (reportPeriod != null && reportPeriod.order == 3 ? avPlatMesFB : empty)
/** АвПлатУпл1КвСуб. Код строки декларации 340. */
def avPlatUpl1CvSub = (reportPeriod != null && reportPeriod.order == 3 ? avPlatMesSub : empty)
/** АвПлатУпл1Кв. */
def avPlatUpl1Cv = (reportPeriod != null && reportPeriod.order == 3 ? getLong(avPlatUpl1CvFB + avPlatUpl1CvSub) : empty)
/** АвНачислСуб. Код строки декларации 230. 200 - 260 + 310. */
def avNachislSub = (hasPrevPeriodDeclaration ? getLong(nalIschislSubOld - nalVipl311SubOld + avPlatMesSubOld) : 0)
/** АвНачисл. Код строки декларации 210. */
def avNachisl = getLong(avNachislFB + avNachislSubOld)
/** НалДоплФБ. Код строки декларации 270. */
def nalDoplFB = getNalDopl(nalIschislFB, avNachislFB, nalVipl311FB)
/** НалДоплСуб. Код строки декларации 271. */
def nalDoplSub = getTotalFromForm(dataRowsHelperAdvance, 'taxSumToPay')
/** НалУменФБ. Код строки декларации 280. */
def nalUmenFB = getNalUmen(avNachislFB, nalVipl311FB, nalIschislFB)
/** НалУменСуб. Код строки декларации 281. */
def nalUmenSub = getTotalFromForm(dataRowsHelperAdvance, 'taxSumToReduction')
/** ОтклВырЦБМин. Код строки декларации 021. Код вида дохода = 11270, 11280, 11290. */
def otklVirCBMin = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [11270, 11280, 11290])
/** ОтклВырЦБРасч. Код строки декларации 022. Код вида дохода = 11300, 11310. */
def otklVirCBRasch = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [11300, 11310])
/** ВнеРеалДохВс. Код строки декларации 100. */
def vneRealDohVs = dohVnereal
/** ВнеРеалДохСт. Код строки декларации 102. Код вида дохода = 13250. */
def vneRealDohSt = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13250])
/** ВнеРеалДохБезв. Код строки декларации 103. Код вида дохода = 13410. */
def vneRealDohBezv = getSimpleIncomeSumRows8(dataRowsHelperSimpleIncome, [13410])
/** ВнеРеалДохИзл. Код строки декларации 104. */
def vneRealDohIzl = getSimpleIncomeSumRows8(dataRowsHelperSimpleIncome, [13410])
/** ВнеРеалДохВРасх. Код строки декларации 105. Код вида дохода = 10910. */
def vneRealDohVRash = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10910])
/** ВнеРеалДохРынЦБДД. Код строки декларации 106. Код вида дохода = 13940, 13950, 13960, 13970, 13980, 13990. */
def vneRealDohRinCBDD = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13940, 13950, 13960, 13970, 13980, 13990])
/** ВнеРеалДохКор. Код строки декларации 107. Код вида дохода = 14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290. */
def vneRealDohCor = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [14170, 14180, 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290])
/** Налоги. Код строки декларации 041. */
def nalogi = getNalogi(dataRowsHelperSimpleConsumption)
/** РасхКапВл10. Код строки декларации 042. Код вида расхода = 20760. */
def rashCapVl10 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [20760])
/** РасхКапВл30. Код строки декларации 043. Код вида расхода = 20765. */
def rashCapVl30 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [20765])
/** РасхЗемУч30пр. Код строки декларации 049. Код вида расхода = 21370. */
def rashZemUch30pr = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21370])
/** РасхЗемУчСрокРас. Код строки декларации 050. Код вида расхода = 21380. */
def rashZemUchSrocRas = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21380])
/** РасхЗемУчСрокАр. Код строки декларации 051. Код вида расхода = 21375. */
def rashZemUchSrocAr = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21375])
/** РасхЗемУчВс. Код строки декларации 047. Код вида дохода = 21370, 21375, 21380. */
def rashZemUchVs = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21370, 21375, 21380])

/** СумАмортПерЛ. Код строки декларации 131. Код вида расхода = 20750, 20755, 20770, 20775, 20780, 20785. */
def sumAmortPerL = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [20750, 20755, 20770, 20775, 20780, 20785])
/** СумАмортПерНмАЛ. Код строки декларации 132. Код вида расхода = 20755. */
def sumAmortPerNmAL = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [20755])
/** РасхВнереалПрДО. Код строки декларации 201. */
def rashVnerealPrDO = getRashVnerealPrDO(dataRowsHelperComplexConsumption, dataRowsHelperSimpleConsumption)
/** УбытРеалПравТр. Код строки декларации 203. Код вида расхода = 22695, 22700. */
def ubitRealPravTr = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [22695, 22700])
/** РасхЛиквОС. Код строки декларации 204. Код вида расхода = 22690. */
def rashLikvOS = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [22690])
/** РасхШтраф. Код строки декларации 205. */
def rashShtraf = getRashShtraf(dataRowsHelperSimpleConsumption)
/** РасхРынЦБДД. Код строки декларации 206. Код вида расхода = 23120, 23130, 23140. */
def rashRinCBDD = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [23120, 23130, 23140])

// Приложение № 3 к Листу 02
/** КолОбРеалАИ. Код строки декларации 010. Код вида дохода = 10. */
def colObRealAI = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10])
/** КолОбРеалАИУб. Код строки декларации 020. Код вида дохода = 20. */
def colObRealAIUb = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [20])
/** ВыручРеалАИ. Код строки декларации 030. Код вида дохода = 10840. */
def viruchRealAI = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10840])
/** ПрибРеалАИ. Код строки декларации 040. Код вида дохода = 10845. */
def pribRealAI = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10845])
/** УбытРеалАИ. Код строки декларации 050. Код вида расхода = 21780. */
def ubitRealAI = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21780])
/** ЦенаРеалПравЗУ. Код строки декларации 240. Код вида дохода = 10890. */
def cenaRealPravZU = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10890])
/** УбытРеалЗУ. Код строки декларации 260. Код вида расхода = 21390. */
def ubitRealZU = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21390])
/** ВыручРеалПТДоСр. Код строки декларации 100. Код вида дохода = 10860. */
def viruchRealPTDoSr = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10860])
/** ВыручРеалПТПосСр. Код строки декларации 110. Код вида дохода = 10870. */
def viruchRealPTPosSr = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10870])
/** Убыт1Соот269. Код строки декларации 140. Код вида расхода = 21490. */
def ubit1Soot269 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21490])
/** Убыт1Прев269. Код строки декларации 150. Код вида расхода = 21500. */
def ubit1Prev269 = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21500])
/** Убыт2РеалПТ. Код строки декларации 160. Код вида расхода = 21510. */
def ubit2RealPT = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21510])
/** Убыт2ВнРасх. Код строки декларации 170. Код вида расхода = 22700. */
def ubit2VnRash = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [22700])
// Приложение № 3 к Листу 02 - конец

// Приложение к налоговой декларации
/** СвЦелСред - блок. Табл. 34. Алгоритмы заполнения отдельных атрибутов «Приложение к налоговой декларации»  декларации Банка по налогу на прибыль. */
svCelSred = new HashMap()

if (dataRowsHelperComplexConsumption != null) {
    // 700, 770, 890
    [700:[20750], 770:[20321], 890:[21280]].each { id, codes ->
        def result = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, codes)
        if (result != 0) {
            svCelSred[id] = result
        }
    }

    // 780, 790, 811, 812, 813, 940, 950
    [780:[20530], 790:[20500], 811:[20700], 812:[20698], 813:[20690], 940:[23040], 950:[23050]].each { id, codes ->
        def result = getCalculatedSimpleConsumption(dataRowsHelperSimpleConsumption, codes)
        if (result != 0) {
            svCelSred[id] = result
        }
    }
}
// Приложение к налоговой декларации - конец

/*
 * Логические проверки. Выполняются только при нажатии на кнопку "Принять".
 */
if (needLogicalCheck) {
    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (всего)
    if (nalVipl311 > nalIschisl) {
        logger.error('Сумма налога, выплаченная за пределами РФ (всего) превышает сумму исчисленного налога на прибыль (всего)')
    }

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (в федеральный бюджет)
    if (nalVipl311FB > nalIschislFB) {
        logger.error('Сумма налога, выплаченная за пределами РФ (в федеральный бюджет) превышает сумму исчисленного налога на прибыль (в федеральный бюджет)')
        return
    }

    // Проверки Листа 02 - Превышение суммы налога, выплаченного за пределами РФ (в бюджет субъекта РФ)
    if (nalVipl311Sub > nalIschislSub) {
        logger.error('Сумма налога, выплаченная за пределами РФ (в бюджет субъекта РФ) превышает сумму исчисленного налога на прибыль (в бюджет субъекта РФ)')
        return
    }

    // Проверки Приложения № 1 к Листу 02 - Превышение суммы составляющих над общим показателем («Внереализационные доходы (всего)»)
    // (ВнеРеалДохПр + ВнеРеалДохСт + ВнеРеалДохБезв + ВнеРеалДохИзл + ВнеРеалДохВРасх + ВнеРеалДохРынЦБДД + ВнеРеалДохКор) < ВнеРеалДохВс
    if ((empty + vneRealDohSt + vneRealDohBezv +
            vneRealDohIzl + vneRealDohVRash + vneRealDohRinCBDD +
            vneRealDohCor) > vneRealDohVs) {
        logger.error('Показатель «Внереализационные доходы (всего)» меньше суммы его составляющих!')
        return
    }

    // Проверки Приложения № 2 к Листу 02 - Превышение суммы составляющих над общим показателем («Косвенные расходы (всего)»)
    // КосвРасхВс < (Налоги + РасхКапВл10 + РасхКапВл30 + РасхТрудИнв + РасхОргИнв + РасхЗемУчВс + НИОКР)
    if (cosvRashVs < (nalogi + rashCapVl10 + rashCapVl30 +
            empty + empty + rashZemUchVs + empty)) {
        logger.error('Показатель «Косвенные расходы (всего)» меньше суммы его составляющих!')
        return
    }

    // Проверки Приложения № 2 к Листу 02 - Превышение суммы составляющих над общим показателем («Внереализационные расходы (всего)»)
    // (РасхВнереалПрДО + РасхВнереалРзрв + УбытРеалПравТр + РасхЛиквОС + РасхШтраф + РасхРынЦБДД) > РасхВнеРеалВс
    if ((rashVnerealPrDO + empty + ubitRealPravTr + rashLikvOS + rashShtraf + rashRinCBDD) > rashVnerealVs) {
        logger.error('Показатель «Внереализационные расходы (всего)» меньше суммы его составляющих!')
        return
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток) от реализации права требования долга до наступления срока платежа, определенной налогоплательщиком в соответствии с п. 1 статьи 279 НК
    // строка 100 = ВыручРеалПТДоСр = viruchRealPTDoSr
    // строка 120 = СтоимРеалПТДоСр = stoimRealPTDoSr
    // строка 140 = Убыт1Соот269	= ubit1Soot269
    // строка 150 = Убыт1Прев269	= ubit1Prev269
    if (stoimRealPTDoSr > viruchRealPTDoSr ?
        ubit1Prev269 != stoimRealPTDoSr - viruchRealPTDoSr - ubit1Soot269 : ubit1Prev269 == 0) {
        logger.error('Результат текущего расчёта не прошёл проверку, приведённую в порядке заполнения налоговой декларации по налогу на прибыль организации.')
        return
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток), полученной налогоплательщиком при уступке права требования долга после наступления срока платежа в соответствии с п. 2 статьи 279 НК
    // строка 110 = ВыручРеалПТПосСр = viruchRealPTPosSr
    // строка 130 = СтоимРеалПТПосСр = stoimRealPTPosSr
    // строка 160 = Убыт2РеалПТ		 = ubit2RealPT
    if (stoimRealPTPosSr > viruchRealPTPosSr ?
        ubit2RealPT != stoimRealPTPosSr - viruchRealPTPosSr : ubit2RealPT == 0) {
        logger.error('Результат текущего расчёта не прошёл проверку, приведённую в порядке заполнения налоговой декларации по налогу на прибыль организации.')
        return
    }
    return
}
if (xml == null) {
    return
}

/*
 * Формирование XML'ки.
 */

import groovy.xml.MarkupBuilder;
def xmlbuilder = new MarkupBuilder(xml)
xmlbuilder.Файл(
        ИдФайл : declarationService.generateXmlFileId(2, departmentId),
        ВерсПрог : appVersion,
        ВерсФорм : formatVersion) {

    // Титульный лист
    Документ(
            КНД :  knd,
            ДатаДок : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
            Период : period,
            ОтчетГод : (taxPeriod != null && taxPeriod.startDate != null ? taxPeriod.startDate.format('yyyy') : empty),
            КодНО : taxOrganCode,
            НомКорр : '0', // TODO (от Айдара) учесть что потом будут корректирующие периоды
            ПоМесту : taxPlaceTypeCode) {

        СвНП(
                ОКВЭД : okvedCode,
                Тлф : phone) {

            НПЮЛ(
                    НаимОрг : name,
                    ИННЮЛ : inn,
                    КПП : kpp) {

                if (reorgFormCode != null) {
                    СвРеоргЮЛ(
                            ФормРеорг : reorgFormCode,
                            ИННЮЛ : reorgInn,
                            КПП : reorgKpp)
                }
            }
        }

        Подписант(ПрПодп : prPodp) {
            ФИО(
                    Фамилия : signatorySurname,
                    Имя : signatoryFirstName,
                    Отчество : signatoryLastName)
            if (prPodp != 1) {
                СвПред(
                        [НаимДок : approveDocName] +
                                (approveOrgName != null ? [НаимОрг : approveOrgName] : [:] )
                )
            }
        }
        // Титульный лист - конец

        Прибыль() {
            НалПУ() {
                // Раздел 1. Подраздел 1.1
                // 0..n // всегда один
                НалПУАв(
                        ТипНП : typeNP,
                        ОКАТО : okato) {

                    def nalPu = isBank ? (nalDoplFB != 0 ? nalDoplFB : -nalUmenFB) : empty
                    // 0..1
                    ФедБдж(
                            КБК : kbk,
                            НалПУ : nalPu)

                    // получение строки текущего подразделения, затем значение столбца «Сумма налога к доплате [100]»
                    def rowForNalPu = getRowAdvanceForCurrentDepartment(dataRowsHelperAdvance, kpp)
                    tmpValue = (rowForNalPu != null ? rowForNalPu.taxSumToPay : 0)
                    nalPu = (tmpValue != 0 ? tmpValue : -tmpValue)
                    // 0..1
                    СубБдж(
                            КБК : kbk2,
                            НалПУ : getLong(nalPu))
                }
                // Раздел 1. Подраздел 1.1 - конец

                // Раздел 1. Подраздел 1.2
                def cvartalIchs
                switch (reportPeriod != null ? reportPeriod.order : empty) {
                    case 3 :
                        cvartalIchs = [21, 24]
                        break
                    default:
                        cvartalIchs = [0]
                }
                cvartalIchs.each { cvartalIch ->
                    // 0..n
                    НалПУМес(
                            [ТипНП : typeNP] +
                                    (cvartalIch != 0 ? [КварталИсч : cvartalIch] : [:]) +
                                    [ОКАТО : okato]) {

                        def avPlat1 = empty
                        def avPlat2 = empty
                        def avPlat3 = empty
                        if (isBank && !isTaxPeriod) {
                            def list02Row300 = avPlatMesFB
                            avPlat1 = (long) list02Row300 / 3
                            avPlat2 = avPlat1
                            avPlat3 = getLong(list02Row300 - avPlat1 - avPlat2)
                        }
                        // 0..1
                        ФедБдж(
                                КБК : kbk,
                                АвПлат1 : avPlat1,
                                АвПлат2 : avPlat2,
                                АвПлат3 : avPlat3)

                        avPlat1 = empty
                        avPlat2 = empty
                        avPlat3 = empty
                        if (!isTaxPeriod) {
                            // при формировании декларации банка надо брать appl5List02Row120 относящегося к ЦА
                            // (как определять пока не ясно, толи по id, толи по id сбербанка,
                            // толи по КПП = 775001001), при формировании декларации подразделения
                            // надо брать строку appl5List02Row120 относящегося к этому подразделению
                            def rowForAvPlat = getRowAdvanceForCurrentDepartment(dataRowsHelperAdvance, kpp)
                            appl5List02Row120 = (rowForAvPlat ? rowForAvPlat.everyMontherPaymentAfterPeriod : 0)
                            avPlat1 = (long) appl5List02Row120 / 3
                            avPlat2 = avPlat1
                            avPlat3 = getLong(appl5List02Row120 - avPlat1 - avPlat2)
                        }
                        // 0..1
                        СубБдж(
                                КБК : kbk2,
                                АвПлат1 : avPlat1,
                                АвПлат2 : avPlat2,
                                АвПлат3 : avPlat3)
                    }
                }
                // Раздел 1. Подраздел 1.2 - конец

                if (dataRowsHelperTaxSum != null) {
                    // Раздел 1. Подраздел 1.3
                    def listTaxSum = dataRowsHelperTaxSum.getAllCached()
                    listTaxSum.sort {
                        refBookService.getStringValue(24, it.paymentType, 'CODE') + it.okatoCode + it.budgetClassificationCode
                    }
                    listTaxSum.each { row ->
                        // 0..n
                        НалПУПроц(
                                ВидПлат : refBookService.getStringValue(24, row.paymentType, 'CODE'),
                                ОКАТО : row.okatoCode,
                                КБК : row.budgetClassificationCode) {

                            // 0..n
                            УплСрок(
                                    Срок : (row.dateOfPayment != null ? row.dateOfPayment.format('dd.MM.yyyy') : empty),
                                    НалПУ : getLong(row.sumTax))
                        }
                    }
                } else {
                    // костыль: пустые данные при отсутствии нф или данных в нф

                    // 0..n
                    НалПУПроц(
                            ВидПлат :	emptyNull,
                            ОКАТО :		emptyNull,
                            КБК :		emptyNull) {

                        // 0..n
                        УплСрок(
                                Срок :	emptyNull,
                                НалПУ :	emptyNull)
                    }
                }
                // Раздел 1. Подраздел 1.3 - конец
            }

            if (isBank) {
                // для декларации банка

                // Лист 02
                // 0..3 - всегда один
                РасчНал(
                        ТипНП : typeNP,
                        ДохРеал : dohReal,
                        ДохВнереал : dohVnereal,
                        РасхУмРеал : rashUmReal,
                        РасхВнереал : rashVnereal,
                        Убытки : ubitki,
                        ПрибУб : pribUb,
                        ДохИсклПриб : dohIsklPrib,
                        ПрибБРСт0 : empty,
                        СумЛьгот : empty,
                        НалБаза : nalBaza,
                        УбытУмНБ : empty,
                        НалБазаИсч : nalBazaIsch,
                        НалБазаИсчСуб : empty,
                        СтавНалВсего : empty,
                        СтавНалФБ : taxRate,
                        СтавНалСуб : empty,
                        СтавНалСуб284 : empty,
                        НалИсчисл : nalIschisl,
                        НалИсчислФБ : nalIschislFB,
                        НалИсчислСуб : nalIschislSub,
                        АвНачисл : avNachisl,
                        АвНачислФБ : avNachislFB,
                        АвНачислСуб : avNachislSub,
                        НалВыпл311 : nalVipl311,
                        НалВыпл311ФБ : nalVipl311FB,
                        НалВыпл311Суб : nalVipl311Sub,
                        НалДоплФБ : nalDoplFB,
                        НалДоплСуб : nalDoplSub,
                        НалУменФБ : nalUmenFB,
                        НалУменСуб : nalUmenSub,
                        АвПлатМес : avPlatMes,
                        АвПлатМесФБ : avPlatMesFB,
                        АвПлатМесСуб : avPlatMesSub,
                        АвПлатУпл1Кв : avPlatUpl1Cv,
                        АвПлатУпл1КвФБ : avPlatUpl1CvFB,
                        АвПлатУпл1КвСуб : avPlatUpl1CvSub) {
                    // Лист 02 - конец

                    // Приложение № 1 к Листу 02
                    // 0..1
                    ДохРеалВнеРеал(ТипНП : typeNP) {
                        // 0..1
                        ДохРеал(
                                ВырРеалПред : virRealPred,
                                ВырРеалОпер32 : viruchOp302It,
                                ВырРеалИтог : dohReal) {

                            // 0..1
                            ВырРеал(
                                    ВырРеалВс : virRealVs,
                                    ВырРеалТовСоб : virRealTovSob,
                                    ВырРеалТовПок : empty,
                                    ВырРеалИмПрав : virRealImPrav,
                                    ВырРеалИмПроч : virRealImProch)
                            // 0..1
                            ВырРеалЦБ(
                                    ВырРеалЦБВс : virRealCBVs,
                                    ОтклВырЦБМин : otklVirCBMin,
                                    ОтклВырЦБРасч : otklVirCBRasch)
                        }
                        // 0..1
                        ДохВнеРеал(
                                ВнеРеалДохВс : vneRealDohVs,
                                ВнеРеалДохПр : empty,
                                ВнеРеалДохСт : vneRealDohSt,
                                ВнеРеалДохБезв : vneRealDohBezv,
                                ВнеРеалДохИзл : vneRealDohIzl,
                                ВнеРеалДохВРасх : vneRealDohVRash,
                                ВнеРеалДохРынЦБДД : vneRealDohRinCBDD,
                                ВнеРеалДохКор : vneRealDohCor)
                    }
                    // Приложение № 1 к Листу 02 - конец

                    // Приложение № 2 к Листу 02
                    // 0..1
                    РасхРеалВнеРеал(ТипНП : typeNP) {
                        // 0..1
                        РасхРеал(
                                ПрямРасхРеал : pramRashReal,
                                РеалИмущПрав : realImushPrav,
                                ПриобрРеалИмущ : priobrRealImush,
                                АктивРеалПред : activRealPred,
                                ПриобРеалЦБ : priobrRealCB,
                                СумОтклЦен : sumOtklCen,
                                РасхОпер32 : rashOper32,
                                УбытПрошОбсл : ubitProshObsl,
                                УбытРеалАмИм : ubitRealAmIm,
                                УбытРеалЗемУч : ubitRealZemUch,
                                НадбПокПред : nadbPokPred,
                                РасхПризнИтого : rashUmReal) {

                            // 0..1
                            ПрямРасхТорг(ПрямРасхТоргВс : pramRashTorgVs)
                            // 0..1
                            КосвРасх(
                                    КосвРасхВс : cosvRashVs,
                                    Налоги : nalogi,
                                    РасхКапВл10 : rashCapVl10,
                                    РасхКапВл30 : rashCapVl30,
                                    РасхТрудИнв : empty,
                                    РасхОргИнв : empty,
                                    РасхЗемУчВс : rashZemUchVs,
                                    РасхЗемУчСрокНП : empty,
                                    РасхЗемУч30пр : rashZemUch30pr,
                                    РасхЗемУчСрокРас : rashZemUchSrocRas,
                                    РасхЗемУчСрокАр : rashZemUchSrocAr,
                                    НИОКР : empty,
                                    НИОКРнеПолРез : empty,
                                    НИОКРПер : empty,
                                    НИОКРПерНеРез : empty)
                        }
                        // 0..1
                        СумАморт(
                                СумАмортПерЛ : sumAmortPerL,
                                СумАмортПерНмАЛ : sumAmortPerNmAL,
                                СумАмортПерН : empty,
                                СумАмортПерНмАН : empty,
                                МетодНачАморт : '1')
                        // 0..1
                        РасхВнеРеал(
                                РасхВнеРеалВс : rashVnerealVs,
                                РасхВнереалПрДО : rashVnerealPrDO,
                                РасхВнереалРзрв : empty,
                                УбытРеалПравТр : ubitRealPravTr,
                                РасхЛиквОС : rashLikvOS,
                                РасхШтраф : rashShtraf,
                                РасхРынЦБДД : rashRinCBDD)
                        // 0..1
                        УбытПриравн(
                                УбытПриравнВс : ubitPriravnVs,
                                УбытПрошПер : ubitProshPer,
                                СумБезнадДолг : sumBeznalDolg)
                    }
                    // Приложение № 2 к Листу 02 - конец

                    // Приложение № 3 к Листу 02
                    // 0..1
                    РасчРасхОпер(
                            ТипНП : typeNP,
                            КолОбРеалАИ :colObRealAI,
                            КолОбРеалАИУб : colObRealAIUb,
                            ВыручРеалАИ : viruchRealAI,
                            ОстСтРеалАИ : ostStRealAI,
                            ПрибРеалАИ : pribRealAI,
                            УбытРеалАИ : ubitRealAI,
                            ВыручРеалТов : viruchRealTov,
                            РасхРеалТов : empty,
                            УбытОбОбсл : empty,
                            УбытОбОбслНеобл : ubitObObslNeobl,
                            ДохДоговДУИ : dohDolgovDUI,
                            ДохДоговДУИ_ВнР : dohDolgovDUI_VnR,
                            РасхДоговДУИ : rashDolgovDUI,
                            РасхДоговДУИ_ВнР : rashDolgovDUI_VnR,
                            УбытДоговДУИ : empty,
                            ЦенаРеалПравЗУ : cenaRealPravZU,
                            СумНевозмЗатрЗУ : sumNevozmZatrZU,
                            УбытРеалЗУ : ubitRealZU,
                            ВыручОп302Ит : viruchOp302It,
                            РасхОп302Ит : rashOper32,
                            УбытОп302 : ubitki) {
                        // 0..1
                        ВыручРеалПТ(
                                ВыручРеалПТДоСр : viruchRealPTDoSr,
                                ВыручРеалПТПосСр : viruchRealPTPosSr)
                        // 0..1
                        СтоимРеалПТ(
                                СтоимРеалПТДоСр : stoimRealPTDoSr,
                                СтоимРеалПТПосСр : stoimRealPTPosSr)
                        // 0..1
                        УбытРеалПТ(
                                Убыт1Соот269 : ubit1Soot269,
                                Убыт1Прев269 : ubit1Prev269,
                                Убыт2РеалПТ : ubit2RealPT,
                                Убыт2ВнРасх : ubit2VnRash)
                    }
                    // Приложение № 3 к Листу 02 - конец

                    // Приложение № 5 к Листу 02
                    /** ОбРасч. Столбец «Признак расчёта». */
                    def obRasch = emptyNull
                    /** НаимОП. Столбец «Подразделение территориального банка». */
                    def naimOP = emptyNull
                    /** КППОП. Столбец «КПП». */
                    def kppop = emptyNull
                    /** ОбязУплНалОП. Столбец «Обязанность по уплате налога». */
                    def obazUplNalOP = emptyNull
                    /** ДоляНалБаз. Столбец «Доля налоговой базы (%)». */
                    def dolaNalBaz = emptyNull
                    /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.)». */
                    def nalBazaDola = emptyNull
                    /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта (%)». */
                    def stavNalSubRF = emptyNull
                    /** СумНал. Столбец «Сумма налога». */
                    def sumNal = emptyNull
                    /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта (руб.)». */
                    def nalNachislSubRF = emptyNull
                    /** СумНалП. Столбец «Сумма налога к доплате». */
                    def sumNalP = emptyNull
                    /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами России и засчитываемая в уплату налога». */
                    def nalViplVneRF = emptyNull
                    /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)». */
                    def mesAvPlat = emptyNull
                    /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на I квартал следующего налогового периода». */
                    def mesAvPlat1CvSled = emptyNull

                    if (dataRowsHelperAdvance != null) {
                        dataRowsHelperAdvance.getAllCached().each { row ->
                            if (row.getAlias() != 'total') {
                                obRasch = refBookService.getNumberValue(26, row.calcFlag, 'CODE')
                                naimOP = refBookService.getStringValue(30, row.regionBankDivision, 'NAME')
                                kppop = refBookService.getStringValue(33, row.kpp, 'KPP')
                                obazUplNalOP = refBookService.getNumberValue(25, row.obligationPayTax, 'CODE')
                                dolaNalBaz = row.baseTaxOf
                                nalBazaDola = row.baseTaxOfRub
                                stavNalSubRF = refBookService.getNumberValue(33, row.subjectTaxStavka, 'TAX_RATE')
                                sumNal = row.taxSum
                                nalNachislSubRF = row.subjectTaxCredit
                                sumNalP = row.taxSumToPay
                                nalViplVneRF = row.taxSumOutside
                                mesAvPlat = row.everyMontherPaymentAfterPeriod
                                mesAvPlat1CvSled = row.everyMonthForKvartalNextPeriod

                                // 0..n
                                РаспрНалСубРФ(
                                        ТипНП : typeNP,
                                        ОбРасч : obRasch,
                                        НаимОП : naimOP,
                                        КППОП : kppop,
                                        ОбязУплНалОП : obazUplNalOP,
                                        НалБазаОрг : nalBazaIsch,
                                        НалБазаБезЛиквОП : empty,
                                        ДоляНалБаз : dolaNalBaz,
                                        НалБазаДоля : nalBazaDola,
                                        СтавНалСубРФ : stavNalSubRF,
                                        СумНал : sumNal,
                                        НалНачислСубРФ : nalNachislSubRF,
                                        НалВыплВнеРФ : nalViplVneRF,
                                        СумНалП : sumNalP,
                                        МесАвПлат : mesAvPlat,
                                        МесАвПлат1КвСлед : mesAvPlat1CvSled)
                            }
                        }
                    }
                    // Приложение № 5 к Листу 02 - конец
                }
            } else {
                // для декларации обособленного подразделения

                // Приложение № 5 к Листу 02
                /** ОбРасч. Столбец «Признак расчёта». */
                def obRasch = emptyNull
                /** НаимОП. Столбец «Подразделение территориального банка». */
                def naimOP = emptyNull
                /** КППОП. Столбец «КПП». */
                def kppop = emptyNull
                /** ОбязУплНалОП. Столбец «Обязанность по уплате налога». */
                def obazUplNalOP = emptyNull
                /** ДоляНалБаз. Столбец «Доля налоговой базы (%)». */
                def dolaNalBaz = emptyNull
                /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.)». */
                def nalBazaDola = emptyNull
                /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта (%)». */
                def stavNalSubRF = emptyNull
                /** СумНал. Столбец «Сумма налога». */
                def sumNal = emptyNull
                /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта (руб.)». */
                def nalNachislSubRF = emptyNull
                /** СумНалП. Столбец «Сумма налога к доплате». */
                def sumNalP = emptyNull
                /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами России и засчитываемая в уплату налога». */
                def nalViplVneRF = emptyNull
                /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом (текущий отчёт)». */
                def mesAvPlat = emptyNull
                /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на I квартал следующего налогового периода». */
                def mesAvPlat1CvSled = emptyNull

                // получение из нф авансовых платежей строки соответствующей текущему подразделению
                def tmpRow = getRowAdvanceForCurrentDepartment(dataRowsHelperAdvance, kpp)
                if (tmpRow != null) {
                    obRasch = refBookService.getNumberValue(26, row.calcFlag, 'CODE')
                    naimOP = refBookService.getStringValue(30, row.regionBankDivision, 'NAME')
                    kppop = refBookService.getStringValue(33, row.kpp, 'KPP')
                    obazUplNalOP = refBookService.getNumberValue(25, row.obligationPayTax, 'CODE')
                    dolaNalBaz = row.baseTaxOf
                    nalBazaDola = row.baseTaxOfRub
                    stavNalSubRF = refBookService.getNumberValue(33, row.subjectTaxStavka, 'TAX_RATE')
                    sumNal = row.taxSum
                    nalNachislSubRF = row.subjectTaxCredit
                    sumNalP = row.taxSumToPay
                    nalViplVneRF = row.taxSumOutside
                    mesAvPlat = row.everyMontherPaymentAfterPeriod
                    mesAvPlat1CvSled = row.everyMonthForKvartalNextPeriod
                }

                // 0..n - всегда один
                РаспрНалСубРФ(
                        ТипНП : typeNP,
                        ОбРасч : obRasch,
                        НаимОП : naimOP,
                        КППОП : kppop,
                        ОбязУплНалОП : obazUplNalOP,
                        НалБазаОрг : nalBazaIsch,
                        НалБазаБезЛиквОП : empty,
                        ДоляНалБаз : dolaNalBaz,
                        НалБазаДоля : nalBazaDola,
                        СтавНалСубРФ : stavNalSubRF,
                        СумНал : sumNal,
                        НалНачислСубРФ : nalNachislSubRF,
                        НалВыплВнеРФ : nalViplVneRF,
                        СумНалП : sumNalP,
                        МесАвПлат : mesAvPlat,
                        МесАвПлат1КвСлед : mesAvPlat1CvSled)
                // Приложение № 5 к Листу 02 - конец
            }

            // 0..1
            НалУдНА() {
                if (dataRowsHelperDividend != null) {
                    dataRowsHelperDividend.getAllCached().each { row ->
                        // Лист 03 А
                        // 0..n
                        НалДохДив(
                                ВидДив : row.dividendType,
                                НалПер : row.taxPeriod,
                                ОтчетГод : row.financialYear.format('yyyy'),
                                ДивВсего : getLong(row.dividendSumRaspredPeriod),
                                НалИсчисл : getLong(row.taxSum),
                                НалДивПред : getLong(row.taxSumFromPeriod),
                                НалДивПосл : getLong(row.taxSumFromPeriodAll)) {

                            // 0..1
                            ДивИОФЛНеРез(
                                    ДивИнОрг : getLong(row.dividendForgeinOrgAll),
                                    ДивФЛНеРез : getLong(row.dividendForgeinPersonalAll),
                                    ДивИсч0 : getLong(row.dividendStavka0),
                                    ДивИсч5 : getLong(row.dividendStavkaLess5),
                                    ДивИсч10 : getLong(row.dividendStavkaMore5),
                                    ДивИсчСв10 : getLong(row.dividendStavkaMore10))
                            // 0..1
                            ДивРА(
                                    ДивРАВс : getLong(row.dividendRussianMembersAll),
                                    ДивРО9 : getLong(row.dividendRussianOrgStavka9),
                                    ДивРО0 : getLong(row.dividendRussianOrgStavka0),
                                    ДивФЛРез : getLong(row.dividendPersonRussia),
                                    ДивНеНП : getLong(row.dividendMembersNotRussianTax))
                            // 0..1
                            ДивНА(
                                    ДивНАдоРас : getLong(row.dividendAgentAll),
                                    ДивНАдоРас0 : getLong(row.dividendAgentWithStavka0))
                            // 0..1
                            ДивНал(
                                    ДивНалВс : getLong(row.dividendSumForTaxAll),
                                    ДивНал9 : getLong(row.dividendSumForTaxStavka9),
                                    ДивНал0 : getLong(row.dividendSumForTaxStavka0))
                        }
                        // Лист 03 А - конец
                    }
                    dataRowsHelperDividend.getAllCached().each { row ->
                        // Лист 03 Б
                        // 0..n
                        НалДохЦБ(
                                ВидДоход : '1',
                                НалБаза : empty,
                                СтавНал : empty,
                                НалИсчисл : empty,
                                НалНачислПред : empty,
                                НалНачислПосл : empty)
                        // Лист 03 Б - конец
                    }
                } else {
                    // костыль: пустые данные при отсутствии нф или данных в нф

                    // Лист 03 А
                    // 0..n
                    НалДохДив(
                            ВидДив :		emptyNull,
                            НалПер :		emptyNull,
                            ОтчетГод :		emptyNull,
                            ДивВсего :		emptyNull,
                            НалИсчисл :		emptyNull,
                            НалДивПред :	emptyNull,
                            НалДивПосл :	emptyNull) {

                        // 0..1
                        ДивИОФЛНеРез(
                                ДивИнОрг :		emptyNull,
                                ДивФЛНеРез :	emptyNull,
                                ДивИсч0 :		emptyNull,
                                ДивИсч5 :		emptyNull,
                                ДивИсч10 :		emptyNull,
                                ДивИсчСв10 :	emptyNull)
                        // 0..1
                        ДивРА(
                                ДивРАВс :	emptyNull,
                                ДивРО9 :	emptyNull,
                                ДивРО0 :	emptyNull,
                                ДивФЛРез :	emptyNull,
                                ДивНеНП :	emptyNull)
                        // 0..1
                        ДивНА(
                                ДивНАдоРас :	emptyNull,
                                ДивНАдоРас0 :	emptyNull)
                        // 0..1
                        ДивНал(
                                ДивНалВс :	emptyNull,
                                ДивНал9 :	emptyNull,
                                ДивНал0 :	emptyNull)
                    }
                    // Лист 03 А - конец

                    // Лист 03 Б
                    // 0..n
                    НалДохЦБ(
                            ВидДоход : '1',
                            НалБаза : empty,
                            СтавНал : empty,
                            НалИсчисл : empty,
                            НалНачислПред : empty,
                            НалНачислПосл : empty)
                    // Лист 03 Б - конец
                }

                // Лист 03 В
                if (dataRowsHelperTaxAgent != null) {
                    dataRowsHelperTaxAgent.getAllCached().each { row ->
                        // 0..n
                        РеестрСумДив(
                                ДатаПерДив : (row.dividendDate != null ? row.dividendDate.format('dd.MM.yyyy') : empty),
                                СумДив : getLong(row.sumDividend),
                                СумНал : getLong(row.sumTax)) {

                            СвПолуч(
                                    [НаимПолуч : row.title] +
                                            (!isEmpty(row.phone) ? [Тлф : row.phone] : [:])) {
                                МНПолуч(
                                        (!isEmpty(row.zipCode) ? [Индекс : row.zipCode] : [:]) +
                                                [КодРегион : refBookService.getStringValue(4, row.subdivisionRF, 'CODE')] +
                                                (!isEmpty(row.area)? [Район : row.area] : [:]) +
                                                (!isEmpty(row.city) ? [Город : row.city] : [:]) +
                                                (!isEmpty(row.region) ? [НаселПункт : row.region] : [:]) +
                                                (!isEmpty(row.street) ? [Улица : row.street] : [:]) +
                                                (!isEmpty(row.homeNumber) ? [Дом : row.homeNumber] : [:]) +
                                                (!isEmpty(row.corpNumber) ? [Корпус : row.corpNumber] : [:]) +
                                                (!isEmpty(row.apartment) ? [Кварт : row.apartment] : [:]))
                                // 0..1
                                ФИОРук(
                                        [Фамилия : row.surname] +
                                                [Имя : row.name] +
                                                (!isEmpty(row.patronymic) ? [Отчество : row.patronymic] : [:]))
                            }
                        }
                    }
                } else {
                    // костыль: пустые данные при отсутствии нф или данных в нф
                    // 0..n
                    РеестрСумДив(
                            ДатаПерДив :	emptyNull,
                            СумДив :		emptyNull,
                            СумНал :		emptyNull) {

                        СвПолуч(
                                НаимПолуч :	emptyNull,
                                Тлф :		emptyNull) {
                            МНПолуч(
                                    Индекс :		emptyNull,
                                    КодРегион :		emptyNull,
                                    Район :			emptyNull,
                                    Город :			emptyNull,
                                    НаселПункт :	emptyNull,
                                    Улица :			emptyNull,
                                    Дом :			emptyNull,
                                    Корпус :		emptyNull,
                                    Кварт :			emptyNull)
                            // 0..1
                            ФИОРук(
                                    Фамилия :	emptyNull,
                                    Имя :		emptyNull,
                                    Отчество :	emptyNull)
                        }
                    }
                }
                // Лист 03 В - конец
            }

            // Лист 04
            def nalBaza04 = 0
            def dohUmNalBaz = 0
            def stavNal = 0
            def nalIschisl04 = 0
            def nalDivNeRFPred = 0
            def nalDivNeRF = 0
            def nalNachislPred = 0
            def nalNachislPosl = 0

            // за предыдущий отчетный период
            def nalBaza04Old = 0
            def stavNalOld = 0
            def nalIschisl04Old = 0
            def nalDivNeRFPredOld = 0
            def nalDivNeRFOld = 0
            def nalNachislPredOld = 0
            def nalNachislPoslOld = 0

            def dataOld = getOldXmlData(prevReportPeriod, departmentId)

            (1..6).each {
                nalDivNeRFPred = 0
                nalDivNeRF = 0
                nalDivNeRFPredOld = 0
                nalDivNeRFOld = 0

                switch (it) {
                    case 1:
                        nalBaza04 = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13655, 13675, 13705, 13780, 13785, 13790])
                        stavNal = 15

                        nalBaza04Old = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13655, 13675, 13705, 13780, 13785, 13790])
                        stavNalOld = 15
                        break
                    case 2:
                        nalBaza04 = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13660, 13680, 13695, 13710])
                        stavNal = 9

                        nalBaza04Old = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13660, 13680, 13695, 13710])
                        stavNalOld = 9
                        break
                    case 3:
                        nalBaza04 = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13665, 13685, 13690])
                        stavNal = 0

                        nalBaza04Old = getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [13665, 13685, 13690])
                        stavNalOld = 0
                        break
                    case 4:
                        nalBaza04Old = getSimpleIncomeSumRows8(dataRowsHelperSimpleIncomeOld, [14010])
                        stavNalOld = 9
                        nalDivNeRFPredOld = getOldValue(dataOld, it, 'НалДивНеРФПред')
                        nalDivNeRFOld = getOldValue(dataOld, it, 'НалДивНеРФ')

                        nalBaza04 = getSimpleIncomeSumRows8(dataRowsHelperSimpleIncome, [14010])
                        stavNal = 9
                        nalDivNeRFPred = nalDivNeRFPredOld + nalDivNeRFOld
                        nalDivNeRF = (sumDividends ?: 0)
                        break
                    case 5:
                        nalBaza04 = 0
                        stavNal = 0

                        nalBaza04Old = 0
                        stavNalOld = 0
                        break
                    case 6:
                        nalBaza04 = 0
                        stavNal = 9

                        nalBaza04Old = 0
                        stavNalOld = 9
                        break
                }
                nalIschisl04 = getLong(nalBaza04 * stavNal / 100)
                nalIschisl04Old = getLong(nalBaza04Old * stavNalOld / 100)
                if (it in [3, 5]) {
                    nalNachislPred = 0
                    nalNachislPosl = 0

                    nalNachislPredOld = 0
                    nalNachislPoslOld = 0
                } else {
                    nalNachislPredOld = getOldValue(dataOld, it, 'НалНачислПред')
                    nalNachislPoslOld = nalIschisl04Old - nalDivNeRFPredOld - nalDivNeRFOld - nalNachislPredOld

                    nalNachislPred = nalNachislPredOld + nalNachislPoslOld
                    nalNachislPosl = nalIschisl04 - nalDivNeRFPred - nalDivNeRF - nalNachislPred
                    logger.info('===== НалНачислПред = ' + nalNachislPred + ', НалНачислПосл = ' + nalNachislPosl) // TODO (Ramil Timerbaev)
                }

                НалДохСтав(
                        ВидДоход: it,
                        НалБаза: getLong(nalBaza04),
                        ДохУмНалБаз: getLong(dohUmNalBaz),
                        СтавНал: getLong(stavNal),
                        НалИсчисл: getLong(nalIschisl04),
                        НалДивНеРФПред: getLong(nalDivNeRFPred),
                        НалДивНеРФ: getLong(nalDivNeRF),
                        НалНачислПред: getLong(nalNachislPred),
                        НалНачислПосл: getLong(nalNachislPosl))
            }
            // Лист 04 - конец

            // Лист 05
            // 0..n
            НалБазОпОсоб(
                    ВидОпер : 5,
                    ДохВыбытПогаш : empty,
                    СумОтклМинЦ : empty,
                    РасхПриобРеал : empty,
                    СумОтклМаксЦ : empty,
                    Прибыль : empty,
                    КорПриб : empty,
                    НалБазаБезУбПред : empty,
                    СумУбытПред : empty,
                    СумУбытУменНБ : empty,
                    СумНеучУбытПер : empty,
                    СумУбытЗСДо2010 : empty,
                    НалБаза : empty)
            // Лист 05 - конец

            // Приложение к налоговой декларации
            if (svCelSred.size() > 0) {
                def tmpArray = []
                svCelSred.each { id, value ->
                    tmpArray.add(id)
                }

                // 0..1
                ДохНеУчНБ_РасхУчОКН() {
                    tmpArray.sort().each { id ->
                        // 1..n
                        СвЦелСред(
                                КодВидРасход : id,
                                СумРасход : getLong(svCelSred[id]))
                    }
                }
            } else {
                // костыль: пустые данные при отсутствии нф или данных в нф
                ДохНеУчНБ_РасхУчОКН() {
                    // 1..n
                    СвЦелСред(
                            КодВидРасход :	emptyNull,
                            СумРасход :		emptyNull)
                }
            }
            // Приложение к налоговой декларации - конец
        }
    }
}


/*
 * Вычисления.
 */

/**
 * Получить округленное, целочисленное значение.
 */
def getLong(def value) {
    if (value == null) {
        return 0
    }
    return (long) round(value, 0)
}

/**
 * Получить значение или ноль, если значения нет.
 */
def getValue(def value) {
    return (value != null ? value : 0)
}

/**
 * Получить сумму значении столбца по указанным строкам.
 *
 * @param dataRowsHelper нф
 * @param columnCode псевдоним столбца по которому отбирать данные для суммирования
 * @param columnSum псевдоним столбца значения которого надо суммировать
 * @param codes список значении, которые надо учитывать при суммировании
 */
def getSumRowsByCol(def dataRowsHelper, def columnCode, def columnSum, def codes) {
    def result = 0
    if (dataRowsHelper == null) {
        return result
    }
    def cell
    dataRowsHelper.getAllCached().each { row ->
        cell = row.getCell(columnSum)
        if (row.getCell(columnCode).getValue() in (String [])codes && !cell.hasValueOwner()) {
            result += getValue(cell.getValue())
        }
    }
    return getLong(result)
}

/**
 * Получить сумму графы 9 формы доходы сложные.
 *
 * @param dataRowsHelper нф доходы сложные
 * @param codes коды которые надо учитывать при суммировании
 */
def getComplexIncomeSumRows9(def dataRowsHelper, def codes) {
    return getSumRowsByCol(dataRowsHelper, 'incomeTypeId', 'incomeTaxSumS', codes)
}

/**
 * Получить сумму графы 9 формы расходы сложные.
 *
 * @param dataRowsHelper нф расходы сложные
 * @param codes коды которые надо учитывать при суммировании
 */
def getComplexConsumptionSumRows9(def dataRowsHelper, def codes) {
    return getSumRowsByCol(dataRowsHelper, 'consumptionTypeId', 'consumptionTaxSumS', codes)
}

/**
 * Получить сумму графы 8 формы доходы простые.
 *
 * @param dataRowsHelper нф доходы простые
 * @param codes коды которые надо учитывать при суммировании
 */
def getSimpleIncomeSumRows8(def dataRowsHelper, def codes) {
    getSumRowsByCol(dataRowsHelper, 'incomeTypeId', 'rnu4Field5Accepted', codes)
}

/**
 * Получить сумму графы 8 формы расходы простые.
 *
 * @param dataRowsHelper нф расходы простые
 * @param codes коды которые надо учитывать при суммировании
 */
def getSimpleConsumptionSumRows8(def dataRowsHelper, def codes) {
    return getSumRowsByCol(dataRowsHelper, 'consumptionTypeId', 'rnu5Field5Accepted', codes)
}

/**
 * Подсчет простых расходов: сумма(графа 8 + графа 5 - графа 6).
 */
def getCalculatedSimpleConsumption(def dataRowsHelperSimple, def codes) {
    def result = 0
    if (dataRowsHelperSimple == null) {
        return result
    }
    dataRowsHelperSimple.getAllCached().each { row ->
        if (row.getCell('consumptionTypeId').getValue() in (String [])codes) {
            result +=
                getValue(row.rnu5Field5Accepted) +
                        getValue(row.rnu7Field10Sum) -
                        getValue(row.rnu7Field12Accepted)
        }
    }
    return getLong(result)
}

/**
 * Выручка от реализации товаров (работ, услуг) собственного производства (ВырРеалТовСоб).
 *
 * @param dataRowsHelper нф доходы сложные
 * @param dataRowsHelperSimple нф доходы простые
 */
def getVirRealTovSob(def dataRowsHelper, def dataRowsHelperSimple) {
    def result = 0.0

    // Код вида дохода = 10633, 10634, 10650, 10670
    result += getComplexIncomeSumRows9(dataRowsHelper, [10633, 10634, 10650, 10670])

    // Код вида дохода = 10001, 10006, 10041, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10370,
    // 10380, 10390, 10450, 10460, 10470, 10480, 10490, 10571, 10580, 10590, 10600, 10610, 10630,
    // 10631, 10632, 10640, 10680, 10690, 10740, 10744, 10748, 10752, 10756, 10760, 10770, 10790,
    // 10800, 11140, 11150, 11160, 11170, 11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375
    result += getSimpleIncomeSumRows8(dataRowsHelperSimple, [10001, 10006, 10041, 10300, 10310, 10320,
            10330, 10340, 10350, 10360, 10370, 10380, 10390, 10450, 10460, 10470, 10480, 10490,
            10571, 10580, 10590, 10600, 10610, 10630, 10631, 10632, 10640, 10680, 10690, 10740,
            10744, 10748, 10752, 10756, 10760, 10770, 10790, 10800, 11140, 11150, 11160, 11170,
            11320, 11325, 11330, 11335, 11340, 11350, 11360, 11370, 11375])

    // Код вида доходов = 10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470,
    // 10480, 10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375
    def codes = [10001, 10006, 10300, 10310, 10320, 10330, 10340, 10350, 10360, 10470, 10480,
            10490, 10571, 10590, 10610, 10640, 10680, 10690, 11340, 11350, 11370, 11375]

    // графа 5
    result += getSumRowsByCol(dataRowsHelperSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsHelperSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

    return getLong(result)
}

/**
 * Получить внереализационные доходы (ДохВнереал, ВнеРеалДохВс).
 *
 * @param dataRowsHelper нф доходы сложные
 * @param dataRowsHelperSimple нф доходы простые
 */
def getDohVnereal(def dataRowsHelper, def dataRowsHelperSimple) {
    def result = 0.0

    // Код вида дохода = 11405, 11410, 11415, 13040, 13045, 13050, 13055, 13060, 13065,
    // 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665, 13670,
    // 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
    // 13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180,
    // 14190, 14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290
    result += getComplexIncomeSumRows9(dataRowsHelper, [11405, 11410, 11415, 13040, 13045, 13050, 13055,
            13060, 13065, 13070, 13090, 13100, 13110, 13120, 13250, 13650, 13655, 13660, 13665,
            13670, 13675, 13680, 13685, 13690, 13695, 13700, 13705, 13710, 13715, 13720, 13780,
            13785, 13790, 13940, 13950, 13960, 13970, 13980, 13990, 14140, 14170, 14180, 14190,
            14200, 14210, 14220, 14230, 14240, 14250, 14260, 14270, 14280, 14290])

    // Код вида дохода = 11380, 11385, 11390, 11395, 11400, 11420, 11430, 11840, 11850, 11855,
    // 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030, 12050, 12070, 12090, 12110, 12130,
    // 12150, 12170, 12190, 12210, 12230, 12250, 12270, 12290, 12320, 12340, 12360, 12390, 12400,
    // 12410, 12420, 12430, 12830, 12840, 12850, 12860, 12870, 12880, 12890, 12900, 12910, 12920,
    // 12930, 12940, 12950, 12960, 12970, 12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035,
    // 13080, 13150, 13160, 13170, 13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330,
    // 13340, 13400, 13410, 13725, 13730, 13920, 13925, 13930, 14000, 14010, 14020, 14030, 14040,
    // 14050, 14060, 14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160
    result += getSimpleIncomeSumRows8(dataRowsHelperSimple, [11380, 11385, 11390, 11395, 11400, 11420,
            11430, 11840, 11850, 11855, 11860, 11870, 11880, 11930, 11970, 12000, 12010, 12030,
            12050, 12070, 12090, 12110, 12130, 12150, 12170, 12190, 12210, 12230, 12250, 12270,
            12290, 12320, 12340, 12360, 12390, 12400, 12410, 12420, 12430, 12830, 12840, 12850,
            12860, 12870, 12880, 12890, 12900, 12910, 12920, 12930, 12940, 12950, 12960, 12970,
            12980, 12985, 12990, 13000, 13010, 13020, 13030, 13035, 13080, 13150, 13160, 13170,
            13180, 13190, 13230, 13240, 13290, 13300, 13310, 13320, 13330, 13340, 13400, 13410,
            13725, 13730, 13920, 13925, 13930, 14000, 14010, 14020, 14030, 14040, 14050, 14060,
            14070, 14080, 14090, 14100, 14110, 14120, 14130, 14150, 14160])

    // Код вида дохода = 11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
    // 14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160
    def codes = [11860, 11870, 11880, 11930, 11970, 12000, 13930, 14020, 14030, 14040, 14050,
            14060, 14070, 14080, 14090, 14100, 14110, 14130, 14150, 14160]
    // графа 5
    result += getSumRowsByCol(dataRowsHelperSimple, 'incomeTypeId', 'rnu6Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsHelperSimple, 'incomeTypeId', 'rnu6Field12Accepted', codes)

    // Код вида дохода = 13130, 13140
    result -= getSimpleIncomeSumRows8(dataRowsHelperSimple, [13130, 13140])

    return getLong(result)
}

/**
 * Получить доходы, исключаемые из прибыли (ДохИсклПриб).
 *
 * @param dataRowsHelperComplex нф доходы сложные
 * @param dataRowsHelperSimple нф доходы простые
 */
def getDohIsklPrib(def dataRowsHelperComplex, def dataRowsHelperSimple) {
    def result = 0.0

    if (dataRowsHelperComplex != null) {
        // Код вида доходов = 13655, 13660, 13665, 13675, 13680, 13685, 13690,
        // 13695, 13705, 13710, 13780, 13785, 13790
        result += getComplexIncomeSumRows9(dataRowsHelperComplex,
                [13655, 13660, 13665, 13675, 13680, 13685, 13690, 13695, 13705, 13710, 13780, 13785, 13790])
    }
    if (dataRowsHelperSimple != null) {
        // Код вида дохода = 14000
        result += getSumRowsByCol(dataRowsHelperSimple, 'incomeTypeId', 'rnu4Field5Accepted', [14000, 14010])
    }
    return getLong(result)
}

/**
 * Получить внереализационные расходы (РасхВнереалВС).
 *
 * @param dataRowsHelper нф расходы сложные
 * @param dataRowsHelperSimple нф расходы простые
 */
def getRashVnerealVs(def dataRowsHelper, def dataRowsHelperSimple) {
    def result = 0.0

    // Код вида расхода = 22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
    // 22670, 22690, 22695, 22700, 23120, 23130, 23140, 23240 - графа 9
    result += getComplexConsumptionSumRows9(dataRowsHelper, [22500, 22505, 22585, 22590, 22595, 22660, 22664, 22668,
            22670, 22690, 22695, 22700, 23120, 23130, 23140, 23240])

    // Код вида расхода = 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070, 22080, 22090, 22100, 22110,
    // 22120, 22130, 22140, 22150, 22160, 22170, 22180, 22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260,
    // 22270, 22280, 22290, 22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390, 22395,
    // 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445, 22450, 22455, 22460, 22465, 22470,
    // 22475, 22480, 22485, 22490, 22496, 22498, 22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565,
    // 22570, 22575, 22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800, 22810, 22840,
    // 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210, 23220, 23230, 23250, 23260, 23270, 23280
    def knu = [ 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070,
            22080, 22090, 22100, 22110, 22120, 22130, 22140, 22150, 22160, 22170, 22180,
            22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260, 22270, 22280, 22290,
            22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390,
            22395, 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445,
            22450, 22455, 22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498,
            22530, 22534, 22538, 22540, 22544, 22548, 22550, 22560, 22565, 22570, 22575,
            22580, 22600, 22610, 22640, 22680, 22710, 22715, 22720, 22750, 22760, 22800,
            22810, 22840, 22850, 22860, 22870, 23040, 23050, 23100, 23110, 23200, 23210,
            23220, 23230, 23250, 23260, 23270, 23280 ]
    result += getCalculatedSimpleConsumption(dataRowsHelperSimple, knu)

    // Код вида расхода = 22492, 23150, 23160, 23170 - графа 9
    result -= getComplexConsumptionSumRows9(dataRowsHelper, [22492, 23150, 23160, 23170])

    return getLong(result)
}

/**
 * Получить налоговую базу для исчисления налога (НалБазаИсч, НалБазаОрг).
 *
 * @param row100 налоговая база
 * @param row110 сумма убытка или части убытка, уменьшающего налоговую базу за отчетный (налоговый) период
 */
def getNalBazaIsch(def row100, def row110) {
    def result
    if (row100 != null && row110 != null && (row100 < 0 || row100 == row110)) {
        result = 0.0
    } else {
        result = row100 - row110
    }
    return getLong(result)
}

/**
 * Получить сумму налога на прибыль к доплате в федеральный бюджет (или в бюджет субъекта Российской федерации).
 *
 * @param value1 сумма исчисленного налога на прибыль
 * @param value2 сумма начисленных авансовых платежей за отчетный (налоговый) период
 * @param value3 сумма налога, выплаченная за пределами Российской Федерации и засчитываемая в уплату налога
 */
def getNalDopl(def value1, def value2, value3) {
    def result = 0
    if ((value1 - value2 - value3) > 0) {
        result = getLong(value1 - value2 - value3)
    }
    return getLong(result)
}

/**
 * Получить сумму исчисленного налога на прибыль, в федеральный бюджет (НалИсчислФБ).
 */
def getNalIschislFB(def row120, row150) {
    return getLong(row120 * row150 / 100)
}

/**
 * Получить сумму налога на прибыль к уменьшению в федеральный бюджет (или в бюджет субъекта Российской федерации).
 *
 * @param value1 сумма начисленных авансовых платежей за отчетный (налоговый) период
 * @param value2 сумма налога, выплаченная за пределами Российской Федерации и засчитываемая в уплату налога
 * @param value3 сумма исчисленного налога на прибыль
 */
def getNalUmen(def value1, def value2, value3) {
    def result = 0
    if ((value1 + value2 - value3) > 0) {
        result = value1 + value2 - value3
    }
    return getLong(result)
}

/**
 * Косвенные расходы, всего (КосвРасхВс).
 *
 * @param dataRowsHelper нф расходы сложные
 * @param dataRowsHelperSimple нф расходы простые
 */
def getCosvRashVs(def dataRowsHelper, def dataRowsHelperSimple) {
    def result = 0

    // Код вида расхода = 20320, 20321, 20470, 20750, 20755, 20760, 20765, 20770,
    // 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380
    result += getComplexConsumptionSumRows9(dataRowsHelper, [20320, 20321, 20470, 20750, 20755, 20760, 20765,
            20770, 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380])

    // Код вида расхода = 20291, 20300, 20310, 20330, 20332, 20334, 20336, 20338,
    // 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440, 20442,
    // 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
    // 20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694,
    // 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840,
    // 20850, 20860, 20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970,
    // 21020, 21025, 21030, 21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150,
    // 21154, 21158, 21170, 21270, 21290, 21295, 21300, 21305, 21310, 21315, 21320,
    // 21325, 21340, 21350, 21360, 21400, 21405, 21410, 21580, 21590, 21600, 21610,
    // 21620, 21660, 21700, 21710, 21720, 21730, 21790, 21800, 21810
    result += getSimpleConsumptionSumRows8(dataRowsHelperSimple, [20291, 20300, 20310, 20330, 20332, 20334,
            20336, 20338, 20339, 20340, 20360, 20364, 20368, 20370, 20430, 20434, 20438, 20440,
            20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475,
            20480, 20485, 20490, 20500, 20510, 20520, 20530, 20540, 20550, 20690, 20694, 20698,
            20700, 20710, 20810, 20812, 20814, 20816, 20820, 20825, 20830, 20840, 20850, 20860,
            20870, 20880, 20890, 20920, 20940, 20945, 20950, 20960, 20970, 21020, 21025, 21030,
            21050, 21055, 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21270,
            21290, 21295, 21300, 21305, 21310, 21315, 21320, 21325, 21340, 21350, 21360, 21400,
            21405, 21410, 21580, 21590, 21600, 21610, 21620, 21660, 21700, 21710, 21720, 21730,
            21790, 21800, 21810])

    // графа 5
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810
    result += getSumRowsByCol(dataRowsHelperSimple, 'consumptionTypeId', 'rnu7Field10Sum', [20300, 20360, 20370, 20430,
            20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
            20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
            20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
            20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
            21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
            21620, 21660, 21700, 21710, 21730, 21790, 21800, 21810])

    // графа 6
    // Код вида дохода = 20300, 20360, 20370, 20430, 20434, 20438, 20440, 20442, 20446, 20448, 20450,
    // 20452, 20454, 20456, 20458, 20460, 20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530,
    // 20540, 20550, 20690, 20694, 20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830,
    // 20840, 20850, 20870, 20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055,
    // 21060, 21065, 21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580,
    // 21590, 2162021660, 21700, 21710, 21730, 21790, 21800, 21810
    result -= getSumRowsByCol(dataRowsHelperSimple, 'consumptionTypeId', 'rnu7Field12Accepted', [20300, 20360, 20370, 20430,
            20434, 20438, 20440, 20442, 20446, 20448, 20450, 20452, 20454, 20456, 20458, 20460,
            20464, 20468, 20475, 20480, 20485, 20490, 20500, 20530, 20540, 20550, 20690, 20694,
            20698, 20700, 20710, 20810, 20812, 20814, 20816, 20825, 20830, 20840, 20850, 20870,
            20880, 20890, 20950, 20960, 20970, 21020, 21025, 21030, 21050, 21055, 21060, 21065,
            21080, 21130, 21140, 21150, 21154, 21158, 21170, 21400, 21405, 21410, 21580, 21590,
            2162021660, 21700, 21710, 21730, 21790, 21800, 21810])

    return getLong(result)
}

/**
 * Расходы в виде процентов по долговым обязательствам любого вида, в том числе процентов, начисленных по ценным бумагам и иным обязательствам, выпущенным (эмитированным) налогоплательщиком (РасхВнереалПр-ДО =  РасхВнереалПрДО).
 *
 * @param dataRowsHelper нф расходы сложные
 * @param dataRowsHelperSimple нф расходы простые
 */
def getRashVnerealPrDO(def dataRowsHelper, def dataRowsHelperSimple) {
    def result = 0
    // Код вида расхода = 22500, 22505
    result += getComplexConsumptionSumRows9(dataRowsHelper, [22500, 22505])

    // Код вида расхода = 22000, 22010, 22020, 22030, 22040, 22050, 22060, 22070, 22080, 22090, 22100,
    // 22110, 22120, 22130, 22140, 22150, 22160, 22170, 22180, 22190, 22200, 22210, 22220, 22230,
    // 22240, 22250, 22260, 22270, 22280, 22290, 22300, 22310, 22320, 22330, 22340, 22350, 22360,
    // 22370, 22380, 22385, 22390, 22395, 22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435,
    // 22440, 22445, 22450, 22455, 22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498
    result += getSimpleConsumptionSumRows8(dataRowsHelperSimple, [22000, 22010, 22020, 22030, 22040, 22050,
            22060, 22070, 22080, 22090, 22100, 22110, 22120, 22130, 22140, 22150, 22160, 22170,
            22180, 22190, 22200, 22210, 22220, 22230, 22240, 22250, 22260, 22270, 22280, 22290,
            22300, 22310, 22320, 22330, 22340, 22350, 22360, 22370, 22380, 22385, 22390, 22395,
            22400, 22405, 22410, 22415, 22420, 22425, 22430, 22435, 22440, 22445, 22450, 22455,
            22460, 22465, 22470, 22475, 22480, 22485, 22490, 22496, 22498])
    return getLong(result)
}

/**
 * Штрафы, пени и иные санкции за нарушение договорных или долговых обязательств,
 * возмещение причиненного ущерба (РасхШтраф).
 *
 * @param dataRowsHelper нф расходы простые
 */
def getRashShtraf(def dataRowsHelper) {
    def result = 0
    // Код вида доходов = 22750, 22760, 22800, 22810
    def codes = [22750, 22760, 22800, 22810]

    result += getSimpleConsumptionSumRows8(dataRowsHelper, codes)

    // графа 5
    result += getSumRowsByCol(dataRowsHelper, 'consumptionTypeId', 'rnu7Field10Sum', codes)
    // графа 6
    result -= getSumRowsByCol(dataRowsHelper, 'consumptionTypeId', 'rnu7Field12Accepted', codes)

    return getLong(result)
}

/**
 * Получить из нф авансовые платежи подраздереления по КПП.
 *
 * @param dataRowsHelper выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации».
 * @param kpp КПП
 */
def getRowAdvanceForCurrentDepartment(def dataRowsHelper, def kpp) {
    if (dataRowsHelper == null) {
        return null
    }
    for (row in dataRowsHelper.getAllCached()) {
        if (kpp == refBookService.getStringValue(33, row.kpp, 'KPP')) {
            return row
        }
    }
    return null
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Суммы налогов и сборов, начисленные в порядке, установленном законодательством Российской Федерации
 * о налогах и сборах, за исключением налогов, перечисленных в ст. 270 НК.
 *
 * @param dataRowsHelper расходы простые
 */
def getNalogi(def dataRowsHelper) {
    def result = 0

    // Код вида расхода = 20830, 20840, 20850, 20860, 20870, 20880, 20890
    result += getSimpleConsumptionSumRows8(dataRowsHelper, [20830, 20840, 20850, 20860, 20870, 20880, 20890])

    // графа 5
    // Код вида дохода = 20830, 20840, 20850, 20870, 20880, 20890
    result += getSumRowsByCol(dataRowsHelper, 'consumptionTypeId', 'rnu7Field10Sum',
            [20830, 20840, 20850, 20870, 20880, 20890])

    // графа 6
    // Код вида дохода = 20830, 20840, 20850, 20870, 20880, 20890
    result -= getSumRowsByCol(dataRowsHelper, 'consumptionTypeId', 'rnu7Field12Accepted',
            [20830, 20840, 20850, 20870, 20880, 20890])

    return getLong(result)
}

/**
 * Итого выручка от реализации по операциям, отраженным в Приложении 3 к Листу 02 (ВыручОп302Ит).
 *
 * @param dataRowsHelper доходы сложные
 */
def getViruchOp302It(def dataRowsHelper, def row180, def row210, row211) {
    // строка 030 + строка 100 + строка 110 + строка 240
    def result = getComplexIncomeSumRows9(dataRowsHelper, [10840, 10860, 10870, 10890])

    // строка  180 + (строка 210 - строка 211)
    result += row180 + row210 - row211

    return getLong(result)
}

/**
 * Убытки по операциям, отраженным в Приложении 3 к Листу 02 (УбытОп302).
 *
 * @param dataRowsHelper расходы сложные
 */
def getUbitki(dataRowsHelper, row201, row230) {
    // строка 060 + строка 150 + строка 160 + 260
    def result = getComplexConsumptionSumRows9(dataRowsHelper, [21780, 21500, 21510, 21390])

    // строка 201 + строка 230
    result += row201 + row230

    return getLong(result)
}

/**
 * Получить значение столбца итоговой строки из налоговой формы.
 *
 * @param form налоговая форма
 * @param columnName название столбца
 * @return значение столбца
 */
def getTotalFromForm(def dataRowsHelper, def columnName) {
    if (dataRowsHelper != null && !dataRowsHelper.getAllCached().isEmpty()) {
        def totalRow = dataRowsHelper.getDataRow(dataRowsHelper.getAllCached(), 'total')
        return totalRow.getCell(columnName).getValue()
    }
    return 0
}

/**
 * Получить из xml за предыдущий период значения
 * @param data xml
 * @param kind вид дохода (1..6)
 * @param valueName название значения
 * @return значение или 0, если значение не найдено
 */
def getOldValue(def data, def kind, def valueName) {
    if (data != null) {
        data.Документ.Прибыль.НалДохСтав.each {
            if (it.@ВидДоход == kind) {
                return it.@"$valueName"
            }
        }
    }
    return 0
}

/**
 * Получить xml декларации за предыдущий отчетный период.
 *
 * @param prevReportPeriod
 * @param departmentId
 */
def getOldXmlData(def prevReportPeriod, def departmentId) {
    if (prevReportPeriod != null) {
        // вид декларации 2 - декларация по налогу на прибыль уровня банка
        def declarationTypeId = 2
        DeclarationData declarationData = declarationService.find(declarationTypeId, departmentId, prevReportPeriod.id)
        def declarationDataId = (declarationData != null ? declarationData.declarationTemplateId : null)
        if (declarationDataId != null) {
            def oldXmlString = declarationService.getXmlData(declarationDataId)
            return new XmlSlurper().parseText(oldXmlString)
        }
    }
    return null
}

/**
 * Получить данные нф.
 *
 * @param form нф
 */
def getDataRowHelper(def form) {
    if (form != null && form.id != null) {
        return formDataService.getDataRowHelper(form)
    }
    return null
}

/**
 * Получить значение атрибута строки справочника.

 * @param record строка справочника
 * @param alias алиас
 */
def getValue(def record, def alias) {
    def value = record.get(alias)
    switch (value.getAttributeType()) {
        case RefBookAttributeType.DATE :
            return value.getDateValue()
        case RefBookAttributeType.NUMBER :
            return value.getNumberValue()
        case RefBookAttributeType.STRING :
            return value.getStringValue()
        case RefBookAttributeType.REFERENCE :
            return value.getReferenceValue()
    }
    return null
}