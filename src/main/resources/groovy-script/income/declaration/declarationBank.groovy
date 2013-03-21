/**
 * Формирование XML для декларации налога на прибыль (declarationBank.groovy).
 *
 * @author rtimerbaev
 * @since 19.03.2013 16:30
 */

if (formDataCollection == null || formDataCollection.records.isEmpty()) {
    logger.error('Отсутствуют выходные или сводные налоговые формы в статусе "Принят". Формирование декларации невозможно.')
    return
}

logger.info('===== start')
// TODO (Ramil Timerbaev) потом убрать
def getTmp(def o) {
    return o != null ? 'notNull' : 'null'
}

/*
 * Константы.
 */
def empty = 0
def knd = '1151006'
def kbk = '18210101011011000110'
def typeNP = '1'

def isBank = true // TODO (Ramil Timerbaev) потом убрать обособленное подразделение, оставить только код для декларации банка
/** Настройки подразделения. */
def departmentParam = departmentService.getDepartmentParam(departmentId)
logger.info('===== departmentParam = ' + getTmp(departmentParam))
logger.info('===== departmentParam.kpp = ' + (departmentParam != null ? departmentParam.kpp : '---'))
if (departmentParam == null) {
    departmentParam = new DepartmentParam()
}
/** Параметры подразделения по налогу на прибыль. */
def departmentParamIncome = departmentService.getDepartmentParamIncome(departmentId)
logger.info('===== departmentParamIncome = ' + getTmp(departmentParamIncome))
/** Отчётный период. */
def reportPeriod = reportPeriodService.get(reportPeriodId)
logger.info('===== reportPeriod = ' + getTmp(reportPeriod))
/** Предыдущий отчётный период. */
def prevReportPeriod = reportPeriodService.getPrevReportPeriod(reportPeriodId)
logger.info('===== prevReportPeriod = ' + getTmp(prevReportPeriod))
/** ПРЕД_ПРЕД_ыдущий отчётный период. */
def prevReportPeriod2 = (prevReportPeriod != null ? reportPeriodService.getPrevReportPeriod(prevReportPeriod.id) : null)
logger.info('===== prevReportPeriod2 = ' + getTmp(prevReportPeriod2))
/** Налоговый период. */
def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)
logger.info('===== taxPeriod = ' + taxPeriod + ', taxPeriod.startDate = ' + taxPeriod.startDate)
/** Признак налоговый ли это период. */
def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)
logger.info('===== isTaxPeriod = ' + getTmp(isTaxPeriod))

/*
 * Данные налоговых форм.
 */

logger.info('===== formDataCollection = ' + (formDataCollection != null ? formDataCollection.records.size() : 'null'))
/** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
def formDataComplexIncome = formDataCollection.find(departmentId, 302, FormDataKind.SUMMARY)
logger.info('===== formDataComplexIncome = ' + getTmp(formDataComplexIncome))
/** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
def formDataSimpleIncome = formDataCollection.find(departmentId, 301, FormDataKind.SUMMARY)
logger.info('===== formDataSimpleIncome = ' + getTmp(formDataSimpleIncome))
/** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
def formDataComplexConsumption = formDataCollection.find(departmentId, 303, FormDataKind.SUMMARY)
logger.info('===== formDataComplexConsumption = ' + getTmp(formDataComplexConsumption))
/** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
def formDataSimpleConsumption = formDataCollection.find(departmentId, 304, FormDataKind.SUMMARY)
logger.info('===== formDataSimpleConsumption = ' + getTmp(formDataSimpleConsumption))
/** Выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
def formDataAdvance = formDataCollection.find(departmentId, 305, FormDataKind.ADDITIONAL)
logger.info('===== formDataAdvance = ' + getTmp(formDataAdvance))
/** Сведения для расчёта налога с доходов в виде дивидендов. */
def formDataDividend = formDataCollection.find(departmentId, 306, FormDataKind.ADDITIONAL)
logger.info('===== formDataDividend = ' + getTmp(formDataDividend))
/** Расчет налога на прибыль с доходов, удерживаемого налоговым агентом. */
def formDataTaxAgent = formDataCollection.find(departmentId, 307, FormDataKind.ADDITIONAL)
logger.info('===== formDataTaxAgent = ' + getTmp(formDataTaxAgent))
/** Расчет налога на прибыль организаций с доходов, исчисленного по ставкам, личным от ставки, указанной в пункте 1 статьи 284 НК. */
// TODO (Ramil Timerbaev) пока эта форма не реализована, когда появиться, то раскоментировать ее получение
// def formDataCalcTaxIncome = formDataCollection.find(departmentId, ????, FormDataKind.ADDITIONAL)
/** Выходная налоговая форма «Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика». */
def formDataTaxSum = formDataCollection.find(departmentId, 308, FormDataKind.ADDITIONAL)
logger.info('===== formDataTaxSum = ' + getTmp(formDataTaxSum))

/*
 * Данные налоговых форм за предыдущий период.
 */

/** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
def formDataComplexIncomeOld = null
/** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
def formDataSimpleIncomeOld = null
/** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
def formDataComplexConsumptionOld = null
/** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
def formDataSimpleConsumptionOld = null
/** Выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
def formDataAdvanceOld = null

if (prevReportPeriod != null) {
    formDataComplexIncomeOld = FormDataService.find(302, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    formDataSimpleIncomeOld = FormDataService.find(301, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    formDataComplexConsumptionOld = FormDataService.find(303, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    formDataSimpleConsumptionOld = FormDataService.find(304, FormDataKind.SUMMARY, departmentId, prevReportPeriod.id)
    formDataAdvanceOld = FormDataService.find(305, FormDataKind.ADDITIONAL, departmentId, prevReportPeriod.id)
}

/*
 * Данные налоговых форм за ПРЕД_ПРЕД_ыдущий период.
 */

/** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
def formDataComplexIncomeOld2 = null
/** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
def formDataSimpleIncomeOld2 = null
/** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
def formDataComplexConsumptionOld2 = null
/** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
def formDataSimpleConsumptionOld2 = null
/** Выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
def formDataAdvanceOld2 = null

if (prevReportPeriod2 != null) {
    formDataComplexIncomeOld2 = FormDataService.find(302, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    formDataSimpleIncomeOld2 = FormDataService.find(301, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    formDataComplexConsumptionOld2 = FormDataService.find(303, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    formDataSimpleConsumptionOld2 = FormDataService.find(304, FormDataKind.SUMMARY, departmentId, prevReportPeriod2.id)
    formDataAdvanceOld2 = FormDataService.find(305, FormDataKind.ADDITIONAL, departmentId, prevReportPeriod2.id)
}

/*
 * Расчет значений для текущей декларации.
 */

/** Период. */
def period = 0
if (departmentParam.reorgFormCode == 50) {
    period = 50
} else if (reportPeriod.order != null) {
    def values = [21, 31, 33, 34]
    period = values[reportPeriod.order - 1]
}
/** ПрПодп. */
def prPodp = departmentParamIncome.signatoryId
/** ВырРеалТовСоб. */
def virRealTovSob = getVirRealTovSob(formDataComplexIncome, formDataSimpleIncome)
/** ВырРеалИмПрав. Код вида дохода = 10871, 10873. */
def virRealImPrav = getComplexIncomeSumRows9(formDataComplexIncome, [15, 17])
/** ВырРеалИмПроч. Код вида дохода = 10850. */
def virRealImProch = getComplexIncomeSumRows9(formDataComplexIncome, [11])
/** ВырРеалВс. */
def virRealVs = virRealTovSob + virRealImPrav + virRealImProch
/** ВырРеалЦБВс. Код вида дохода = 11271..11280. */
def virRealCBVs = getComplexIncomeSumRows9(formDataComplexIncome, (24..32))
/** ВыручОп302Ит. Код вида дохода = 10840, 10860, 10870, 10872. */
def viruchOp302It = getComplexIncomeSumRows9(formDataComplexIncome, [9, 13, 14, 16])
/** ДохРеал, ВырРеалИтог. */
def dohReal = virRealVs + virRealCBVs + viruchOp302It
/** ДохВнереал. */
def dohVnereal = getDohVnereal(formDataComplexIncome, formDataSimpleIncome)
/** КосвРасхВс. */
def cosvRashVs = getCosvRashVs(formDataComplexConsumption, formDataSimpleConsumption)
/** РасхВнереал, РасхВнереалВС. */
def rashVnereal = getRashVnereal(formDataComplexConsumption, formDataSimpleConsumption)
/** РеалИмущПрав. Код вида расхода = 21653, 21656. */
def realImushPrav = getComplexConsumptionSumRows9(formDataComplexConsumption, [85, 89])
/** ПриобрРеалИмущ. Код вида расхода = 21658. */
def priobrRealImush = getComplexConsumptionSumRows9(formDataComplexConsumption, [91])
/** ПриобРеалЦБ. Код вида расхода = 21662..21675. */
def priobrRealCB = getComplexConsumptionSumRows9(formDataComplexConsumption, (97..109))
/** РасхОпер32, РасхОп302Ит. Код вида расхода = 21657, 21507, 21510, 21396. */
def rashOper32 = getComplexConsumptionSumRows9(formDataComplexConsumption, [90, 72, 73, 63])
/** УбытРеалАмИм. Код вида расхода = 21520, 21525. */
def ubitRealAmIm = getComplexConsumptionSumRows9(formDataComplexConsumption, [78, 79])
/** УбытРеалЗемУч. Код вида расхода = 21397. */
def ubitRealZemUch = getComplexConsumptionSumRows9(formDataComplexConsumption, [64])
/** РасхУмРеал, РасхПризнИтого. */
def rashUmReal = cosvRashVs + realImushPrav + priobrRealImush + priobrRealCB + rashOper32 + ubitRealAmIm + ubitRealZemUch
/** Убытки, УбытОп302. Код вида расхода = 21659, 21515, 21518, 21397. */
def ubitki = getComplexConsumptionSumRows9(formDataComplexConsumption, [92, 76, 77, 64])
def pribUb = dohReal + dohVnereal - rashUmReal - rashVnereal + ubitki
def dohIsklPrib = getDohIsklPrib(formDataSimpleIncome)
def nalBaza = pribUb - dohIsklPrib - 0 - 0 + 0
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIsch = getNalBazaIsch(nalBaza, 0)
/** НалИсчислФБ. */
def nalIschislFB = getNalIschislFB(nalBazaIsch, departmentParamIncome.taxRate)
/** НалИсчислСуб. Столбец «Сумма налога в бюджет субъекта РФ [070]». */
def nalIschislSub = getFormDataSumByColName(formDataAdvance, 'taxAmount')
/** НалИсчисл. */
def nalIschisl = nalIschislFB + nalIschislSub
/** НалВыпл311. */
def nalVipl311 = departmentParamIncome.externalTaxSum
/** НалВыпл311ФБ. */
def nalVipl311FB = 0.1 * departmentParamIncome.externalTaxSum
/** НалВыпл311Суб. */
def nalVipl311Sub = 0.9 * departmentParamIncome.externalTaxSum

/*
 * Расчет значении декларации за ПРЕД_ПРЕД_ыдущий период.
 */

/** ВырРеалТовСоб. */
def virRealTovSobOld2 = getVirRealTovSob(formDataComplexIncomeOld2, formDataSimpleIncome)
/** ВырРеалИмПрав. Код вида дохода = 10871, 10873. */
def virRealImPravOld2 = getComplexIncomeSumRows9(formDataComplexIncomeOld2, [15, 17])
/** ВырРеалИмПроч. Код вида дохода = 10850. */
def virRealImProchOld2 = getComplexIncomeSumRows9(formDataComplexIncomeOld2, [11])
/** ВырРеалВс. */
def virRealVsOld2 = virRealTovSobOld2 + virRealImPravOld2 + virRealImProchOld2
/** ВырРеалЦБВс. Код вида дохода = 11271..11280. */
def virRealCBVsOld2 = getComplexIncomeSumRows9(formDataComplexIncomeOld2, (24..32))
/** ВыручОп302Ит. Код вида дохода = 10840, 10860, 10870, 10872. */
def viruchOp302ItOld2 = getComplexIncomeSumRows9(formDataComplexIncomeOld2, [9, 13, 14, 16])
/** ДохРеал, ВырРеалИтог. */
def dohRealOld2 = virRealVsOld2 + virRealCBVsOld2 + viruchOp302ItOld2
/** ДохВнереал. */
def dohVnerealOld2 = getDohVnereal(formDataComplexIncomeOld2, formDataSimpleIncomeOld2)
/** КосвРасхВс. */
def cosvRashVsOld2 = getCosvRashVs(formDataComplexConsumptionOld2, formDataSimpleConsumptionOld2)
/** РасхВнереал, РасхВнереалВС. */
def rashVnerealOld2 = getRashVnereal(formDataComplexConsumptionOld2, formDataSimpleConsumptionOld2)
/** РеалИмущПрав. Код вида расхода = 21653, 21656. */
def realImushPravOld2 = getComplexConsumptionSumRows9(formDataComplexConsumptionOld2, [85, 89])
/** ПриобрРеалИмущ. Код вида расхода = 21658. */
def priobrRealImushOld2 = getComplexConsumptionSumRows9(formDataComplexConsumptionOld2, [91])
/** ПриобРеалЦБ. Код вида расхода = 21662..21675. */
def priobrRealCBOld2 = getComplexConsumptionSumRows9(formDataComplexConsumptionOld2, (97..109))
/** РасхОпер32, РасхОп302Ит. Код вида расхода = 21657, 21507, 21510, 21396. */
def rashOper32Old2 = getComplexConsumptionSumRows9(formDataComplexConsumptionOld2, [90, 72, 73, 63])
/** УбытРеалАмИм. Код вида расхода = 21520, 21525. */
def ubitRealAmImOld2 = getComplexConsumptionSumRows9(formDataComplexConsumptionOld2, [78, 79])
/** УбытРеалЗемУч. Код вида расхода = 21397. */
def ubitRealZemUchOld2 = getComplexConsumptionSumRows9(formDataComplexConsumptionOld2, [64])
/** РасхУмРеал, РасхПризнИтого. */
def rashUmRealOld2 = cosvRashVsOld2 + realImushPravOld2 + priobrRealImushOld2 + priobrRealCBOld2 + rashOper32Old2 + ubitRealAmImOld2 + ubitRealZemUchOld2
/** Убытки, УбытОп302. Код вида расхода = 21659, 21515, 21518, 21397. */
def ubitkiOld2 = getComplexConsumptionSumRows9(formDataComplexConsumptionOld2, [92, 76, 77, 64])
def pribUbOld2 = dohRealOld2 + dohVnerealOld2 - rashUmRealOld2 - rashVnerealOld2 + ubitkiOld2
def dohIsklPribOld2 = getDohIsklPrib(formDataSimpleIncomeOld2)
def nalBazaOld2 = pribUbOld2 - dohIsklPribOld2 - 0 - 0 + 0
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIschOld2 = getNalBazaIsch(nalBazaOld2, 0)
/** НалИсчислФБ. */
def nalIschislFBOld2 = getNalIschislFB(nalBazaIschOld2, departmentParamIncome.taxRate)

/*
 * Расчет значении декларации за предыдущий период.
 */

/** ВырРеалТовСоб. */
def virRealTovSobOld = getVirRealTovSob(formDataComplexIncomeOld, formDataSimpleIncome)
/** ВырРеалИмПрав. Код вида дохода = 10871, 10873. */
def virRealImPravOld = getComplexIncomeSumRows9(formDataComplexIncomeOld, [15, 17])
/** ВырРеалИмПроч. Код вида дохода = 10850. */
def virRealImProchOld = getComplexIncomeSumRows9(formDataComplexIncomeOld, [11])
/** ВырРеалВс. */
def virRealVsOld = virRealTovSobOld + virRealImPravOld + virRealImProchOld
/** ВырРеалЦБВс. Код вида дохода = 11271..11280. */
def virRealCBVsOld = getComplexIncomeSumRows9(formDataComplexIncomeOld, (24..32))
/** ВыручОп302Ит. Код вида дохода = 10840, 10860, 10870, 10872. */
def viruchOp302ItOld = getComplexIncomeSumRows9(formDataComplexIncomeOld, [9, 13, 14, 16])
/** ДохРеал, ВырРеалИтог. */
def dohRealOld = virRealVsOld + virRealCBVsOld + viruchOp302ItOld
/** ДохВнереал. */
def dohVnerealOld = getDohVnereal(formDataComplexIncomeOld, formDataSimpleIncomeOld)
/** КосвРасхВс. */
def cosvRashVsOld = getCosvRashVs(formDataComplexConsumptionOld, formDataSimpleConsumptionOld)
/** РасхВнереал, РасхВнереалВС. */
def rashVnerealOld = getRashVnereal(formDataComplexConsumptionOld, formDataSimpleConsumptionOld)
/** РеалИмущПрав. Код вида расхода = 21653, 21656. */
def realImushPravOld = getComplexConsumptionSumRows9(formDataComplexConsumptionOld, [85, 89])
/** ПриобрРеалИмущ. Код вида расхода = 21658. */
def priobrRealImushOld = getComplexConsumptionSumRows9(formDataComplexConsumptionOld, [91])
/** ПриобРеалЦБ. Код вида расхода = 21662..21675. */
def priobrRealCBOld = getComplexConsumptionSumRows9(formDataComplexConsumptionOld, (97..109))
/** РасхОпер32, РасхОп302Ит. Код вида расхода = 21657, 21507, 21510, 21396. */
def rashOper32Old = getComplexConsumptionSumRows9(formDataComplexConsumptionOld, [90, 72, 73, 63])
/** УбытРеалАмИм. Код вида расхода = 21520, 21525. */
def ubitRealAmImOld = getComplexConsumptionSumRows9(formDataComplexConsumptionOld, [78, 79])
/** УбытРеалЗемУч. Код вида расхода = 21397. */
def ubitRealZemUchOld = getComplexConsumptionSumRows9(formDataComplexConsumptionOld, [64])
/** РасхУмРеал, РасхПризнИтого. */
def rashUmRealOld = cosvRashVsOld + realImushPravOld + priobrRealImushOld + priobrRealCBOld + rashOper32Old + ubitRealAmImOld + ubitRealZemUchOld
/** Убытки, УбытОп302. Код вида расхода = 21659, 21515, 21518, 21397. */
def ubitkiOld = getComplexConsumptionSumRows9(formDataComplexConsumptionOld, [92, 76, 77, 64])
def pribUbOld = dohRealOld + dohVnerealOld - rashUmRealOld - rashVnerealOld + ubitkiOld
def dohIsklPribOld = getDohIsklPrib(formDataSimpleIncomeOld)
def nalBazaOld = pribUbOld - dohIsklPribOld - 0 - 0 + 0
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIschOld = getNalBazaIsch(nalBazaOld, 0)
/** НалИсчислФБ. */
def nalIschislFBOld = getNalIschislFB(nalBazaIschOld, departmentParamIncome.taxRate)
/** НалИсчислСуб. Столбец «Сумма налога в бюджет субъекта РФ [070]». */
def nalIschislSubOld = getFormDataSumByColName(formDataAdvanceOld, 'taxAmount')
/** НалИсчисл. */
def nalIschislOld = nalIschislFBOld + nalIschislSubOld

/*
 * Расчет значений для текущей декларации (Продолжение).
 */

/** АвПлатМесФБ. */
// def avPlatMesFB = (isTaxPeriod ? empty : nalIschislFB - nalIschislFBOld)
def avPlatMesFB = (isTaxPeriod ? empty : nalIschislFBOld - nalIschislFBOld2)
/** АвНачислФБ. */	// = НалИсчислФБ + АвПлатМесФБ - НалВыпл311ФБ
def avNachislFB = nalIschislFBOld + avPlatMesFB - nalVipl311FB
/** АвНачислСуб. Столбец «Начислено налога в бюджет субъекта РФ. Расчётный [080]». */
def avNachislSub = getFormDataSumByColName(formDataAdvanceOld, 'accruedTax')
def avNachisl = avNachislFB + avNachislSub
def nalDoplFB = getNalDopl(nalIschislFB, avNachislFB, nalVipl311FB)
def nalDoplSub = getNalDopl(nalIschislSub, avNachislSub, nalVipl311Sub)
def nalUmenFB = getNalUmen(avNachislFB, nalVipl311FB, nalIschislFB)
def nalUmenSub = getNalUmen(avNachislSub, nalVipl311Sub, nalIschislSub)
/** ОтклВырЦБМин. Код вида дохода = 11281. */
def otklVirCBMin = getComplexIncomeSumRows9(formDataComplexIncome, [33])
/** ОтклВырЦБРасч. Код вида дохода = 11282. */
def otklVirCBRasch = getComplexIncomeSumRows9(formDataComplexIncome, [36])
/** ВнеРеалДохВс. */
def vneRealDohVs = getDohVnereal(formDataComplexIncome, formDataSimpleIncome)
/** ВнеРеалДохСт. Код вида дохода = 13250. */
def vneRealDohSt = getComplexIncomeSumRows9(formDataComplexIncome, [69])
/** ВнеРеалДохБезв. */
def vneRealDohBezv = getVneRealDohBezv(formDataSimpleIncome)
/** ВнеРеалДохИзл. */
def vneRealDohIzl = getVneRealDohIzl(formDataSimpleIncome)
/** ВнеРеалДохВРасх. Код вида дохода = 10874. */
def vneRealDohVRash = getComplexIncomeSumRows9(formDataComplexIncome, [18])
/** ВнеРеалДохРынЦБДД. Код вида дохода = 13940, 13961. */
def vneRealDohRinCBDD = getComplexIncomeSumRows9(formDataComplexIncome, [113, 115])
/** ВнеРеалДохКор. */
def vneRealDohCor = departmentParamIncome.correctionSum
/** Налоги. Код вида расхода = 20830..20905. */
def nalogi = getCalculatedSimpleConsumption(formDataSimpleConsumption, (34..39))
/** РасхКапВл10. Код вида расхода = 20775. */
def rashCapVl10 = getComplexConsumptionSumRows9(formDataComplexConsumption, [31])
/** РасхКапВл30. Код вида расхода = 20776. */
def rashCapVl30 = getComplexConsumptionSumRows9(formDataComplexConsumption, [32])
/** РасхЗемУч30пр. Код вида расхода = 21393. */
def rashZemUch30pr = getComplexConsumptionSumRows9(formDataComplexConsumption, [60])
/** РаcхЗемУчСрокРас. Код вида расхода = 21394. */
def rashZemUchSrocRas = getComplexConsumptionSumRows9(formDataComplexConsumption, [61])
/** РасхЗемУчСрокАр. Код вида расхода = 21395. */
def rashZemUchSrocAr = getComplexConsumptionSumRows9(formDataComplexConsumption, [62])
/** РасхЗемУчВс. */
def rashZemUchVs = rashZemUch30pr + rashZemUchSrocRas + rashZemUchSrocAr

/** СумАмортПерЛ. Код вида расхода = 20760..20771, 20777. */
def sumAmortPerL = getComplexConsumptionSumRows9(formDataComplexConsumption, ((26..30) + [33]))
/** СумАмортПерНмАЛ. Код вида расхода = 20771. */
def sumAmortPerNmAL = getComplexConsumptionSumRows9(formDataComplexConsumption, [30])
/** РасхВнереалПрДО. */
def rashVnerealPrDO = getRashVnerealPrDO(formDataComplexConsumption, formDataSimpleConsumption)
/** УбытРеалПравТр. Код вида расхода = 22695, 22697. */
def ubitRealPravTr = getComplexConsumptionSumRows9(formDataComplexConsumption, [129, 130])
/** РасхЛиквОС. Код вида расхода = 22690. */
def rashLikvOS = getComplexConsumptionSumRows9(formDataComplexConsumption, [128])
/** РасхШтраф. Код вида расхода = 22750..22811. */
def rashShtraf = getComplexConsumptionSumRows9(formDataComplexConsumption, (131..139))
/** РасхРынЦБДД. Код вида расхода = 23130, 23141. */
def rashRinCBDD = getComplexConsumptionSumRows9(formDataComplexConsumption, [145, 147])

// Приложение № 3 к Листу 02
/** КолОбРеалАИ. Код вида дохода = 10. */
def colObRealAI = getComplexIncomeSumRows9(formDataComplexIncome, [130])
/** КолОбРеалАИУб. Код вида дохода = 20. */
def colObRealAIUb = getComplexIncomeSumRows9(formDataComplexIncome, [131])
/** ВыручРеалАИ. Код вида дохода = 10840. */
def viruchRealAI = getComplexIncomeSumRows9(formDataComplexIncome, [9])
/** ОстСтРеалАИ. Код вида расхода = 21657. */
def OstStRealAI = getComplexConsumptionSumRows9(formDataComplexConsumption, [90])
/** ПрибРеалАИ. Код вида дохода = 10845. */
def pribRealAI = getComplexIncomeSumRows9(formDataComplexIncome, [10])
/** УбытРеалАИ. Код вида расхода = 21659. */
def ubitRealAI = getComplexConsumptionSumRows9(formDataComplexConsumption, [92])
/** ЦенаРеалПравЗУ. Код вида дохода = 10872. */
def cenaRealPravZU = getComplexIncomeSumRows9(formDataComplexIncome, [16])
/** СумНевозмЗатрЗУ. Код вида расхода = 21396. */
def sumNevozmZatrZU = getComplexConsumptionSumRows9(formDataComplexConsumption, [63])
/** УбытРеалЗУ. Код вида расхода = 21397. */
def ubitRealZU = getComplexConsumptionSumRows9(formDataComplexConsumption, [64])
/** ВыручРеалПТДоСр. Код вида дохода = 10860. */
def viruchRealPTDoSr = getComplexIncomeSumRows9(formDataComplexIncome, [13])
/** ВыручРеалПТПосСр. Код вида дохода = 10870. */
def viruchRealPTPosSr = getComplexIncomeSumRows9(formDataComplexIncome, [14])
/** СтоимРеалПТДоСр. Код вида расхода = 21507. */
def stoimRealPTDoSr = getComplexConsumptionSumRows9(formDataComplexConsumption, [72])
/** СтоимРеалПТПосСр. Код вида расхода = 21510. */
def stoimRealPTPosSr = getComplexConsumptionSumRows9(formDataComplexConsumption, [73])
/** Убыт1Соот269. Код вида расхода = 21514. */
def ubit1Soot269 = getComplexConsumptionSumRows9(formDataComplexConsumption, [75])
/** Убыт1Прев269. Код вида расхода = 21515. */
def ubit1Prev269 = getComplexConsumptionSumRows9(formDataComplexConsumption, [76])
/** Убыт2РеалПТ. Код вида расхода = 21518. */
def ubit2RealPT = getComplexConsumptionSumRows9(formDataComplexConsumption, [77])
/** Убыт2ВнРасх. Код вида расхода = 22697. */
def ubit2VnRash = getComplexConsumptionSumRows9(formDataComplexConsumption, [130])
// Приложение № 3 к Листу 02 - конец

/** АвПлатМес. */
def avPlatMes = (isTaxPeriod ? empty : nalIschisl - nalIschislOld)

/** АвПлатМесСуб. */
def avPlatMesSub = (isTaxPeriod ? empty : nalIschislSub - nalIschislSubOld)
/** АвПлатУпл1Кв. */
def avPlatUpl1Cv = (reportPeriod != null && reportPeriod.order == 3 ? nalIschisl - nalIschislOld : empty)
/** АвПлатУпл1КвФБ. */
def avPlatUpl1CvFB = (reportPeriod != null && reportPeriod.order == 3 ? nalIschislFB - nalIschislFBOld : empty)
/** АвПлатУпл1КвСуб. */
def avPlatUpl1CvSub = (reportPeriod != null && reportPeriod.order == 3 ? nalIschislSub - nalIschislSubOld : empty)

// Приложение к налоговой декларации
/** СвЦелСред - блок. Табл. 34. Алгоритмы заполнения отдельных атрибутов «Приложение к налоговой декларации»  декларации Банка по налогу на прибыль. */
svCelSred = new HashMap()

if (formDataSimpleConsumption != null) {
    // Код вида расхода:строка = 770:20321
    [770:'R7'].each { id, rowAlias ->
        def row = formDataSimpleConsumption.getDataRow(rowAlias)
        def result = getValue(row.rnu5Field5Accepted) + getValue(row.rnu7Field10Sum) - getValue(row.rnu5Field5PrevTaxPeriod)
        if (result != 0) {
            svCelSred += [id : result]
        }
    }
}

if (formDataComplexConsumption != null) {
    // Код вида расхода:строка = 670:20760, 671:20761, 677:20762, 700:20777, 812:20691, 813:20690, 890:21280
    [670:'R26', 671:'R27', 677:'R28', 700:'R33', 812:'R23', 813:'R22', 890:'R54'].each { id, rowAlias ->
        def result = getValue(formDataComplexConsumption.getDataRow(rowAlias).consumptionTaxSumS)
        if (result != 0) {
            svCelSred += [id : result]
        }
    }
}

if (formDataSimpleConsumption != null) {
    // Код вида расхода:строка = 780:20503, 790:20504
    [780:'R28', 790:'R29'].each { id, rowAlias ->
        def row = formDataSimpleConsumption.getDataRow(rowAlias)
        def result = getValue(row.rnu5Field5Accepted) + getValue(row.rnu7Field10Sum) - getValue(row.rnu7Field12Accepted)
        if (result != 0) {
            svCelSred += [id : result]
        }
    }

    // Код вида расхода:строка = 940:23040, 950:23050
    [940:'R188', 950:'R190'].each { id, rowAlias ->
        def result = getValue(formDataSimpleConsumption.getDataRow(rowAlias).rnu5Field5Accepted)
        if (result != 0) {
            svCelSred += [id : result]
        }
    }
}
// Приложение к налоговой декларации - конец

/*
 * Формирование XML'ки.
 */

xml.Файл(
        //ИдФайл : '182',
        ИдФайл : declarationService.generateXmlFileId(2, departmentId),
        ВерсПрог : departmentParamIncome.appVersion,
        ВерсФорм : departmentParamIncome.formatVersion) {

    // Титульный лист
    Документ(
            КНД :  knd,
            ДатаДок : new Date().format('MM.dd.yy'),
            Период : period,
            ОтчетГод : (taxPeriod != null ? taxPeriod.startDate.getYear() : empty),
            КодНО : departmentParam.taxOrganCode,
            НомКорр : '0', // TODO (от Айдара) учесть что потом будут корректирующие периоды
            ПоМесту : departmentParamIncome.taxPlaceTypeCode) {

        СвНП(
                ОКВЭД : departmentParam.okvedCode,
                Тлф : departmentParam.phone) {

            НПЮЛ(
                    НаимОрг : departmentParam.name,
                    ИННЮЛ : departmentParam.inn,
                    КПП : departmentParam.kpp) {
                СвРеоргЮЛ(
                        ФормРеорг : departmentParam.reorgFormCode,
                        ИННЮЛ : departmentParam.reorgInn,
                        КПП : departmentParam.reorgKpp)
            }
        }

        Подписант(ПрПодп : prPodp) {
            ФИО(
                    Фамилия : departmentParamIncome.signatoryLastName,
                    Имя : departmentParamIncome.signatoryFirstName,
                    Отчество : departmentParamIncome.signatorySurname)
            СвПред(
                    НаимДок : (prPodp == 1 ? empty : departmentParamIncome.approveDocName),
                    НаимОрг : (prPodp == 1 ? empty : departmentParamIncome.approveOrgName))
        }
        // Титульный лист - конец

        Прибыль() {
            НалПУ() {
                // Раздел 1. Подраздел 1.1
                // 0..n // всегда один
                НалПУАв(
                        ТипНП : typeNP,
                        ОКАТО : departmentParam.okato) {

                    // НалИсчислФБ, АвНачислФБ, НалВыпл311ФБ
                    def tmpValue = getNalPU(nalIschislFB, avNachislFB, nalVipl311FB)
                    def nalPu = isBank ? tmpValue : empty
                    // 0..1
                    ФедБдж(
                            КБК : (isBank ? kbk : empty),
                            НалПУ : nalPu)

                    // НалИсчислСуб, АвНачислСуб, НалВыпл311Суб
                    tmpValue = getNalPU(nalIschislSub, avNachislSub, nalVipl311Sub)
                    // получение строки текущего подразделения, затем значение столбца «Сумма налога к доплате [100]»
                    def rowForNalPu = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
                    logger.info('===== nalIschislSub = ' + nalIschislSub + ', avNachislSub = ' + avNachislSub + ' nalVipl311Sub = ' + nalVipl311Sub)
                    tmpValue2 = (rowForNalPu != null ? rowForNalPu.taxSurchargeAmount : 0)
                    nalPu = isBank ? tmpValue : tmpValue2
                    // 0..1
                    СубБдж(
                            КБК : kbk,
                            НалПУ : nalPu)
                }
                // Раздел 1. Подраздел 1.1 - конец

                // Раздел 1. Подраздел 1.2
                def cvartalIchs = null
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
                            ТипНП : typeNP,
                            КварталИсч : cvartalIch,
                            ОКАТО : departmentParam.okato) {

                        def avPlat1 = empty
                        def avPlat2 = empty
                        def avPlat3 = empty
                        if (isBank && !isTaxPeriod) {
                            list02Row300 = nalIschislFB - nalIschislFBOld

                            avPlat3 = (int) list02Row300 / 3
                            avPlat2 = avPlat3
                            avPlat1 = avPlat3 + getTail(list02Row300, 3)
                        }
                        // 0..1
                        ФедБдж(
                                КБК : (isBank ? kbk : empty),
                                АвПлат1 : avPlat1,
                                АвПлат2 : avPlat2,
                                АвПлат3 : avPlat3)

                        avPlat1 = empty
                        avPlat2 = empty
                        avPlat3 = empty
                        if (isBank && !isTaxPeriod) {
                            def appl5List02Row120 = 0
                            // при формировании декларации банка надо брать appl5List02Row120 относящегося к ЦА (как определять пока не ясно, толи по id, толи по id сбербанка, толи по КПП = 775001001), при формировании декларации подразделения надо брать строку appl5List02Row120 относящегося к этому подразделению
                            def rowForAvPlat = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
                            appl5List02Row120 = (rowForAvPlat ? rowForAvPlat.monthlyAdvancePaymentsForQuarter : 0)
                            avPlat3 = (int) appl5List02Row120 / 3
                            avPlat2 = avPlat1
                            avPlat1 = avPlat1 + getTail(appl5List02Row120, 3)
                        }
                        // 0..1
                        СубБдж(
                                КБК : kbk,
                                АвПлат1 : avPlat1,
                                АвПлат2 : avPlat2,
                                АвПлат3 : avPlat3)
                    }
                }
                // Раздел 1. Подраздел 1.2 - конец

                if (formDataTaxSum != null) {
                    // Раздел 1. Подраздел 1.3
                    formDataTaxSum.dataRows.each { row ->
                        // 0..n
                        НалПУПроц(
                                ВидПлат : row.paymentType,
                                ОКАТО : row.okatoCode,
                                КБК : row.budgetClassificationCode) {

                            // 0..n
                            УплСрок(
                                    Срок : row.dateOfPayment,
                                    НалПУ : row.sumTax)
                        }
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
                        СтавНалФБ : departmentParamIncome.taxRate,
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
                                ВырРеалПред : empty,
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
                                    ВырРеалЦБВС : virRealCBVs,
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
                                ПрямРасхРеал : empty,
                                РеалИмущПрав : realImushPrav,
                                ПриобрРеалИмущ : priobrRealImush,
                                АктивРеалПред : empty,
                                ПриобРеалЦБ : priobrRealCB,
                                СумОтклЦен : departmentParamIncome.sumDifference,
                                РасхОпер32 : rashOper32,
                                УбытПрошОбсл : empty,
                                УбытРеалАмИм : ubitRealAmIm,
                                УбытРеалЗемУч : ubitRealZemUch,
                                НадбПокПред : empty,
                                РасхПризнИтого : rashUmReal) {

                            // 0..1
                            ПрямРасхТорг(ПрямРасхТоргВс : empty)
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
                                    РаcхЗемУчСрокРас : rashZemUchSrocRas,
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
                                РасхВнеРеалВС : rashVnereal,
                                РасхВнереалПрДО : rashVnerealPrDO,
                                РасхВнереалРзрв : empty,
                                УбытРеалПравТр : ubitRealPravTr,
                                РасхЛиквОС : rashLikvOS,
                                РасхШтраф : rashShtraf,
                                РасхРынЦБДД : rashRinCBDD)
                        // 0..1
                        УбытПриравн(
                                УбытПриравнВс : empty,
                                УбытПрошПер : empty,
                                СумБезнадДолг : empty)
                    }
                    // Приложение № 2 к Листу 02 - конец

                    // Приложение № 3 к Листу 02
                    // 0..1
                    РасчРасхОпер(
                            ТипНП : typeNP,
                            КолОбРеалАИ :colObRealAI,
                            КолОбРеалАИУб : colObRealAIUb,
                            ВыручРеалАИ : viruchRealAI,
                            ОстСтРеалАИ : OstStRealAI,
                            ПрибРеалАИ : pribRealAI,
                            УбытРеалАИ : ubitRealAI,
                            ВыручРеалТов : empty,
                            РасхРеалТов : empty,
                            УбытОбОбсл : empty,
                            УбытОбОбслНеобл : empty,
                            ДохДоговДУИ : empty,
                            ДохДоговДУИ_ВнР : empty,
                            РасхДоговДУИ : empty,
                            РасхДоговДУИ_ВнР : empty,
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
                    if (formDataAdvance != null) {
                        formDataAdvance.dataRows.each { row ->
                            /** ОбРасч. Столбец «Код строки 002». */
                            def obRasch = row.stringCode
                            /** НаимОП. Столбец «Наименование подразделения». */
                            def naimOP = row.unitName
                            /** КППОП. Столбец «КПП». */
                            def kppop = row.kpp
                            /** ОбязУплНалОП. Столбец «Отметка о возложении обязанности по уплате налога». */
                            def obazUplNalOP = row.markOnImposingDuties
                            /** ДоляНалБаз. Столбец «Доля налоговой базы (%) [040]». */
                            def dolaNalBaz = row.taxBasePart
                            /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.) [050]». */
                            def nalBazaDola = row.taxBase
                            /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта РФ [060]». */
                            def stavNalSubRF = row.taxRate
                            /** СумНал. Столбец «Сумма налога в бюджет субъекта РФ [070]». */
                            def sumNal = row.taxAmount
                            /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта РФ. Расчётный [080]». */
                            def nalNachislSubRF = row.accruedTax
                            /** СумНалП. Столбец «Сумма налога к доплате [100]». */
                            def sumNalP = row.taxSurchargeAmount
                            /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами РФ [090]». */
                            def nalViplVneRF = row.taxAmountPaidOutsideRussia
                            /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом [120]». */
                            def mesAvPlat = row.monthlyAdvancePaymentsForQuarter
                            /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на 1 квартал следующего налогового периода [121]». */
                            def mesAvPlat1CvSled = row.monthlyAdvancePaymentsForOneQuarter

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
                    // Приложение № 5 к Листу 02 - конец
                }
            } else {
                // для декларации обособленного подразделения

                // Приложение № 5 к Листу 02
                /** ОбРасч. Столбец «Код строки 002». */
                def obRasch = empty
                /** НаимОП. Столбец «Наименование подразделения». */
                def naimOP = empty
                /** КППОП. Столбец «КПП». */
                def kppop = empty
                /** ОбязУплНалОП. Столбец «Отметка о возложении обязанности по уплате налога». */
                def obazUplNalOP = empty
                /** ДоляНалБаз. Столбец «Доля налоговой базы (%) [040]». */
                def dolaNalBaz = empty
                /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.) [050]». */
                def nalBazaDola = empty
                /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта РФ [060]». */
                def stavNalSubRF = empty
                /** СумНал. Столбец «Сумма налога в бюджет субъекта РФ [070]». */
                def sumNal = empty
                /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта РФ. Расчётный [080]». */
                def nalNachislSubRF = empty
                /** СумНалП. Столбец «Сумма налога к доплате [100]». */
                def sumNalP = empty
                /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами РФ [090]». */
                def nalViplVneRF = empty
                /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом [120]». */
                def mesAvPlat = empty
                /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на 1 квартал следующего налогового периода [121]». */
                def mesAvPlat1CvSled = empty

                // получение из нф авансовых платежей строки соответствующей текущему подразделению
                def tmpRow = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
                if (tmpRow != null) {
                    obRasch = tmpRow.stringCode
                    naimOP = tmpRow.unitName
                    kppop = tmpRow.kpp
                    obazUplNalOP = tmpRow.markOnImposingDuties
                    dolaNalBaz = tmpRow.taxBasePart
                    nalBazaDola = tmpRow.taxBase
                    stavNalSubRF = tmpRow.taxRate
                    sumNal = tmpRow.taxAmount
                    nalNachislSubRF = tmpRow.accruedTax
                    sumNalP = tmpRow.taxSurchargeAmount
                    nalViplVneRF = tmpRow.taxAmountPaidOutsideRussia
                    mesAvPlat = tmpRow.monthlyAdvancePaymentsForQuarter
                    mesAvPlat1CvSled = tmpRow.monthlyAdvancePaymentsForOneQuarter
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
                // Лист 03 А
                if (formDataDividend != null) {
                    formDataDividend.dataRows.each { row ->
                        // 0..n
                        НалДохДив(
                                ВидДив : row.dividendType,
                                НалПер : row.taxPeriod,
                                ОтчетГод : row.financialYear,
                                ДивВсего : row.dividendSumRaspredPeriod,
                                НалИсчисл : row.taxSum,
                                НалДивПред : row.taxSumFromPeriod,
                                НалДивПосл : row.taxSumFromPeriodAll) {

                            // 0..1
                            ДивИОФЛНеРез(
                                    ДивИнОрг : row.dividendForgeinOrgAll,
                                    ДивФЛНеРез : row.dividendForgeinPersonalAll,
                                    ДивИсч0 : row.dividendStavka0,
                                    ДивИсч5 : row.dividendStavkaLess5,
                                    ДивИсч10 : row.dividendStavkaMore5,
                                    ДивИсчСв10 : row.dividendStavkaMore10)
                            // 0..1
                            ДивРА(
                                    ДивРАВс : row.dividendRussianMembersAll,
                                    ДивРО9 : row.dividendRussianOrgStavka9,
                                    ДивРО0 : row.dividendRussianOrgStavka0,
                                    ДивФЛРез : row.dividendPersonRussia,
                                    ДивНеНП : row.dividendMembersNotRussianTax)
                            // 0..1
                            ДивНА(
                                    ДивНАдоРас : row.dividendAgentAll,
                                    ДивНАдоРас0 : row.dividendAgentWithStavka0)
                            // 0..1
                            ДивНал(
                                    ДивНалВс : row.dividendSumForTaxAll,
                                    ДивНал9 : row.dividendSumForTaxStavka9,
                                    ДивНал0 : row.dividendSumForTaxStavka0)
                        }
                    }
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

                // Лист 03 В
                if (formDataTaxAgent != null) {
                    formDataTaxAgent.dataRows.each { row ->
                        // 0..n
                        РеестрСумДив(
                                ДатаПерДив : row.dividendDate,
                                СумДив : row.sumDividend,
                                СумНал : row.sumTax) {

                            СвПолуч(
                                    НаимПолуч : row.title,
                                    Тлф : row.phone) {
                                МНПолуч(
                                        Индекс : row.zipCode,
                                        КодРегион : row.subdivisionRF,
                                        Район : row.area,
                                        Город : row.city,
                                        НаселПункт : row.region,
                                        Улица : row.street,
                                        Дом : row.homeNumber,
                                        Корпус : row.corpNumber,
                                        Кварт : row.apartment)
                                // 0..1
                                ФИОРук(
                                        Фамилия : row.surname,
                                        Имя : row.name,
                                        Отчество : row.patronymic)
                            }
                        }
                    }
                }
                // Лист 03 В - конец
            }

            // Лист 04
            /*
                * // TODO (Ramil Timerbaev) используется пятая выходная налоговая форма или "Расчет налога на прибыль организаций с доходов, исчисленного по ставкам, личным от ставки, указанной в пункте 1 статьи 284 НК".
                * пока не готова, будет готова в версии 0.3
                * потом сделать заполнение 9 полей: ВидДоход, НалБаза, ДохУмНалБаз, СтавНал, НалИсчисл, НалДивНеРФПре, НалДивНеРФ, НалНачислПред, НалНачислПосл
                */
            // TODO (Ramil Timerbaev) в нф 6 строк, выводить надо только 4, поэтому потом добавить условие для отбора 4ех строк
            /** 4 строки из нф "Расчет налога на прибыль организаций с доходов, исчисленного по ставкам, личным от ставки, указанной в пункте 1 статьи 284 НК". */
            /*
               def taxIncomeRows = []
               formDataCalcTaxIncome.dataRows.each {
                   if () {
                       taxIncomeRows += [it]
                   }
               }
               taxIncomeRows.each { row ->
                   // 0..n
                   НалДохСтав(
                       ВидДоход : '1',
                       НалБаза : '0',
                       ДохУмНалБаз : '0',
                       СтавНал : '0',
                       НалИсчисл : '0',
                       НалДивНеРФПред : '0',
                       НалДивНеРФ : '0',
                       НалНачислПред : '0',
                       НалНачислПосл : '0')
               }
               */
            // TODO (Ramil Timerbaev) потом убрать этот код
            // начало
            // 0..n
            (1..4).each {
                НалДохСтав(
                        ВидДоход : it,
                        НалБаза : '0',
                        ДохУмНалБаз : '0',
                        СтавНал : '0',
                        НалИсчисл : '0',
                        НалДивНеРФПред : '0',
                        НалДивНеРФ : '0',
                        НалНачислПред : '0',
                        НалНачислПосл : '0')
            }
            // конец
            // Лист 04 - конец

            // Лист 05
            // 0..n
            НалБазОпОсоб(
                    ВидОпер : empty, // в xml принимает 0, в печатном виде 5
                    ДохВыбытПогаш : empty,
                    СумОтклМинЦ : empty,
                    РасхПриобРеал : empty,
                    СумОтклМаксЦ : empty,
                    Прибыль : empty,
                    КорПриб : empty,
                    НалБашаБезУбПред : empty,
                    СумУбытПред : empty,
                    СумУбытУменНБ : empty,
                    СумНеучУбытПер : empty,
                    СумУбытЗСДо2010 : empty,
                    НалБаза : empty)
            // Лист 05 - конец

            // Приложение к налоговой декларации
            // 0..1
            ДохНеУчНБ_РасхУчОКН() {
                svCelSred.each { id, result ->
                    // 1..n
                    СвЦелСред(
                            КодВидРасход : id,
                            СумРасход : getInt(result))
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
def getInt(def value) {
    return (int) round(value, 0)
}

/**
 * Получить остаток (целое и дробное) от деления вдух чисел (например 16,5 / 3 = 1,5).
 */
def getTail(def value, def div) {
    return value - (int) value + (int) value % div
}

/**
 * Получить значение или ноль, если значения нет.
 */
def getValue(def value) {
    return (value != null ? value : 0)
}

/**
 * Получить сумму всех строк указанного столбца.
 *
 * @param form налоговая форма
 * @param colName псевдоним столбца
 */
def getFormDataSumByColName(def form, def colName) {
    def result = 0.0
    if (form != null) {
        result = summ(form, new ColumnRange(colName, 0, form.dataRows.size()))
    }
    return getInt(result)
}

/**
 * Получить сумму значении столбца по указанным строкам.
 *
 * @param form нф
 * @param rows строки которые надо учитывать при суммировании
 * @param column псевдоним столбца по которой суммировать
 */
def getSumRowsByCol(def form, def rows, def column) {
    def result = 0.0
    if (form == null) {
        return result
    }
    rows.each {
        result += getValue(form.getDataRow('R' + it).getCell(column).getValue())
    }
    return getInt(result)
}

/**
 * Получить сумму графы 9 формы доходы сложные.
 *
 * @param form нф доходы сложные
 * @param rows строки которые надо учитывать при суммировании
 */
def getComplexIncomeSumRows9(def form, def rows) {
    return getSumRowsByCol(form, rows, 'incomeTaxSumS')
}

/**
 * Получить сумму графы 9 формы доходы простые.
 *
 * @param form нф доходы простые
 * @param rows строки которые надо учитывать при суммировании
 */
def getSimpleIncomeSumRows9(def form, def rows) {
    return getSumRowsByCol(form, rows, 'rnu4Field5Accepted')
}

/**
 * Получить сумму графы 9 формы расходы сложные.
 *
 * @param form нф расходы сложные
 * @param rows строки которые надо учитывать при суммировании
 */
def getComplexConsumptionSumRows9(def form, def rows) {
    return getSumRowsByCol(form, rows, 'consumptionTaxSumS')
}

/**
 * Подсчет простых расходов: сумма(графа 8 + графа 5 - графа 9 - графа 6).
 */
def getCalculatedSimpleConsumption(def formSimple, def rows) {
    def result = 0.0
    if (formSimple == null) {
        return result
    }
    rows.each {
        def row = formSimple.getDataRow('R' + it)
        result +=
            getValue(row.rnu5Field5Accepted) +
                    getValue(row.rnu7Field10Sum) -
                    getValue(row.rnu5Field5PrevTaxPeriod) -
                    getValue(row.rnu7Field12Accepted)
    }
    return getInt(result)
}

/**
 * Выручка от реализации товаров (работ, услуг) собственного производства (ВырРеалТовСоб).
 *
 * @param form нф доходы сложные
 * @param formSimple нф доходы простые
 */
def getVirRealTovSob(def form, def formSimple) {
    def result = 0.0

    // Код вида дохода = 10571, 10640, 10641, 10650, 10920
    result = getComplexIncomeSumRows9(form, [3, 4, 5, 7, 19])

    if (formSimple != null) {
        // Код вида дохода = 10001 — 10960, 11140, 11160
        ((3..86) + [90, 92]).each {
            def row	= formSimple.getDataRow('R' + it)
            result += getValue(row.rnu4Field5Accepted) + getValue(row.rnu6Field10Sum) -
                    getValue(row.rnu6Field12Accepted) - getValue(row.rnu4Field5PrevTaxPeriod)
        }
    }
    return getInt(result)
}

/**
 * Получить внереализационные доходы (ДохВнереал, ВнеРеалДохВс).
 *
 * @param form нф доходы сложные
 * @param formSimple нф доходы простые
 */
def getDohVnereal(def form, def formSimple) {
    def result = 0.0

    // Код вида дохода = 10874, 11860..13610, 13650..13700, 13920..13961
    result = getComplexIncomeSumRows9(form, ([18] + (43..77) + (82..94) + (112..115)))

    if (formSimple != null) {
        // Код вида дохода = 11380..13080, 13100..13639, 13763, 13930, 14000
        ((98..164) + (171..209) + [213, 217, 220]).each {
            def row	= formSimple.getDataRow('R' + it)
            result += getValue(row.rnu4Field5Accepted) +
                    getValue(row.rnu6Field10Sum) -
                    getValue(row.rnu4Field5PrevTaxPeriod) -
                    getValue(row.rnu6Field12Accepted)
        }
    }

    // TODO (Ramil Timerbaev) Сумма всех строк 010 по Листам 04 - как будет готова форма 5, сделать правильный подсчет
    // result += sumList04Rows010
    result += 0 // костыль

    if (formSimple != null) {
        // Код вида дохода = 13092
        def row	= formSimple.getDataRow('R165')
        result -= getValue(row.rnu4Field5Accepted)
    }

    return getInt(result)
}

/**
 * Получить внереализационные расходы (РасхВнереал, РасхВнереалВС).
 *
 * @param form нф расходы сложные
 * @param formSimple нф расходы простые
 */
def getRashVnereal(def form, def formSimple) {
    def result = 0.0

    // Код вида расхода = 22482..22811, 23110..23141 - графа 9
    result += getComplexConsumptionSumRows9(form, ((116..139) + (144..147)))

    // Код вида расхода = 21680..22840, 23040..23080
    result += getCalculatedSimpleConsumption(formSimple, ((92..184) + (188..193)))

    // Код вида расхода = 22481 - графа 9
    result -= getComplexConsumptionSumRows9(form, [114])

    return getInt(result)
}

/**
 * Получить доходы, исключаемые из прибыли (ДохИсклПриб).
 *
 * @param formSimple нф доходы простые
 */
def getDohIsklPrib(def formSimple) {
    def result = 0.0

    if (formSimple != null) {
        // Код вида дохода = 14000
        def row	= formSimple.getDataRow('R220' + it)
        result += getValue(row.rnu4Field5Accepted)
    }

    // TODO (Ramil Timerbaev) Сумма всех строк 010 по Листам 04 - как будет готова форма 5, сделать правильный подсчет
    // result += sumList04Rows010
    result += 0 // костыль

    return getInt(result)
}

/**
 * Получить налоговую базу для исчисления налога (НалБазаИсч, НалБазаОрг).
 *
 * @param row100 налоговая база
 * @param row110 сумма убытка или части убытка, уменьшающего налоговую базу за отчетный (налоговый) период
 */
def getNalBazaIsch(def row100, def row110) {
    def result = 0.0
    if (row100 != null && row110 != null && (row100 < 0 || row100 == row110)) {
        result = 0.0
    } else {
        result = row100 - row110
    }
    return getInt(row100 - row110)
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
        result = getInt(value1 - value2 - value3)
    }
    return getInt(result)
}

/**
 * Сумма налога к доплате (НалПУ).
 *
 * @param row190 НалИсчислФБ (или НалИсчислСуб)
 * @param row220 АвНачислФБ (или АвНачислСуб)
 * @param row250 НалВыпл311ФБ (или НалВыпл311Суб)
 */
def getNalPU(def row190, def row220, row250) {
    def result = 0
    if (row190 > (row220 + row250)) {
        result = row190 - row220 - row250
    } else {
        result = -((row220 + row250) - row190)
    }
    return getInt(result)
}

/**
 * Получить сумму исчисленного налога на прибыль, в федеральный бюджет (НалИсчислФБ).
 */
def getNalIschislFB(def row120, row150) {
    def result = 0
    if (row120 != null && row120 > 0) {
        result = row120 * row150 / 100
    }
    return getInt(result)
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
    return getInt(result)
}

/**
 * Внереализационные доходы в виде безвозмездно полученного имущества (работ, услуг) или имущественных прав (кроме указанных в ст. 251 НК) (ВнеРеалДохБезв).
 *
 * @param formSimple нф доходы простые
 */
def getVneRealDohBezv(def formSimple) {
    def result = 0
    if (formSimple == null) {
        return result
    }
    // Код вида дохода = 13100 – 13111
    (170..172).each {
        result += getValue(formSimple.getDataRow('R' + it).rnu4Field5Accepted) -
                getValue(formSimple.getDataRow('R' + it).rnu4Field5PrevTaxPeriod)
    }
    return getInt(result)
}

/**
 * Внереализационные доходы в виде стоимости излишков материально-производственных запасов и прочего имущества, которые выявлены в результате инвентаризации (ВнеРеалДохИзл).
 *
 * @param formSimple нф доходы простые
 */
def getVneRealDohIzl(def formSimple) {
    def result = 0
    if (formSimple == null) {
        return result
    }
    // Код вида дохода = 13410
    def row = formSimple.getDataRow('R195')
    return getInt(getValue(row.rnu4Field5Accepted) - getValue(row.rnu4Field5PrevTaxPeriod))
}

/**
 * Косвенные расходы, всего (КосвРасхВс).
 *
 * @param form нф расходы сложные
 * @param formSimple нф расходы простые
 */
def getCosvRashVs(def form, def formSimple) {
    def result = 0
    // Код вида расхода = 20320..21395, 21400..21500, 21530..21652, 21654..21655
    result = getComplexConsumptionSumRows9(form, ((3..62) + (65..70) + (80..83) + (86..88)))

    // Код вида расхода = 20291..21650, 21660
    result += getCalculatedSimpleConsumption(formSimple, ((3..84) + [88]))

    return getInt(result)
}

/**
 * Расходы в виде процентов по долговым обязательствам любого вида, в том числе процентов, начисленных по ценным бумагам и иным обязательствам, выпущенным (эмитированным) налогоплательщиком (РасхВнереалПр-ДО =  РасхВнереалПрДО).
 *
 * @param form нф расходы сложные
 * @param formSimple нф расходы простые
 */
def getRashVnerealPrDO(def form, def formSimple) {
    def result = 0
    // Код вида расхода = 22482, 22513
    result += getComplexConsumptionSumRows9(form, [116, 118])

    // Код вида расхода = 21680..22481
    result += getSumRowsByCol(formSimple, (92..153), 'rnu5Field5Accepted')
    return getInt(result)
}

/**
 * Получить из нф авансовые платежи подраздереления по КПП.
 *
 * @param form выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации».
 * @param kpp КПП 
 */
def getRowAdvanceForCurrentDepartment(def form, def kpp) {
    if (form == null) {
        return null
    }
    for (row in form.dataRows) {
        if (row.kpp == kpp) {
            return row
        }
    }
    return null
}

/**
 * // TODO (Ramil Timerbaev) когда будет передача статуса декларации в скрипт, то проверять статус и запускать логические проверки.
 * Проверки. Не должны выполняться при формировании или обновлении декларации. Выполняются только при нажатии на кнопку "Проверить", "Принять", "Выгрузить в xml".
 */
void validate() {
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
    if ((rashVnerealPrDO + empty + ubitRealPravTr + rashLikvOS + rashShtraf + rashRinCBDD) > rashVnereal) {
        logger.error('Показатель «Внереализационные расходы (всего)» меньше суммы его составляющих!')
        return
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток) от реализации права требования долга до наступления срока платежа, определенной налогоплательщиком в соответствии с п. 1 статьи 279 НК
    // TODO (Ramil Timerbaev) уточнить у аналитика когда выводить сообщение об ошибке
    // строка 100 = ВыручРеалПТДоСр = viruchRealPTDoSr
    // строка 120 = СтоимРеалПТДоСр = stoimRealPTDoSr
    // строка 140 = Убыт1Соот269	= ubit1Soot269
    // строка 150 = Убыт1Прев269	= ubit1Prev269
    if (stoimRealPTDoSr > viruchRealPTDoSr ?
        ubit1Prev269 != stoimRealPTDoSr - viruchRealPTDoSr - ubit1Soot269 : ubit1Prev269 == 0) {
        logger.error('Результат текущего расчёта не прошёл проверку, приведённую в порядке заполнения налоговой декларации по налогу на прибыль организации')
    }

    // Проверки Приложения № 3 к Листу 02 - Проверка отрицательной разницы (убыток), полученной налогоплательщиком при уступке права требования долга после наступления срока платежа в соответствии с п. 2 статьи 279 НК
    // TODO (Ramil Timerbaev) уточнить у аналитика когда выводить сообщение об ошибке
    // строка 110 = ВыручРеалПТПосСр = viruchRealPTPosSr
    // строка 130 = СтоимРеалПТПосСр = stoimRealPTPosSr
    // строка 160 = Убыт2РеалПТ		 = ubit2RealPT
    if (stoimRealPTPosSr > viruchRealPTPosSr ?
        ubit2RealPT != stoimRealPTPosSr - viruchRealPTPosSr : ubit2RealPT == 0) {
        logger.error('Результат текущего расчёта не прошёл проверку, приведённую в порядке заполнения налоговой декларации по налогу на прибыль организации.')
    }
}