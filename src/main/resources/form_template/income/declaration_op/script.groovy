package form_template.income.declaration_op

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType

/**
 * Формирование XML для декларации налога на прибыль уровня обособленного подразделения.
 *
 * @version 4
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    // создать / обновить
    case FormDataEvent.CREATE :
        break
    // проверить
    case FormDataEvent.CHECK :
        return
        break
    // принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        return
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

def departmentId = declarationData.departmentId
def reportPeriodId = declarationData.reportPeriodId

/** Предпослденяя дата отчетного периода на которую нужно получить настройки подразделения из справочника. */
def reportDate = reportPeriodService.getEndDate(reportPeriodId)
if (reportDate != null) {
    reportDate = reportDate.getTime() - 1
}

// справочник "Параметры подразделения по налогу на прибыль" - начало
def departmentParamIncomeRefDataProvider = refBookFactory.getDataProvider(33)
def departmentParamIncomeRecords = departmentParamIncomeRefDataProvider.getRecords(reportDate, null,
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
def signatoryId = refBookService.getNumberValue(35, getValue(incomeParams, 'SIGNATORY_ID'), 'CODE')
def appVersion = getValue(incomeParams, 'APP_VERSION')
def formatVersion = getValue(incomeParams, 'FORMAT_VERSION')
def taxPlaceTypeCode = refBookService.getStringValue(2, getValue(incomeParams, 'TAX_PLACE_TYPE_CODE'), 'CODE')
def signatorySurname = getValue(incomeParams, 'SIGNATORY_SURNAME')
def signatoryFirstName = getValue(incomeParams, 'SIGNATORY_FIRSTNAME')
def signatoryLastName = getValue(incomeParams, 'SIGNATORY_LASTNAME')
def approveDocName = getValue(incomeParams, 'APPROVE_DOC_NAME')
def approveOrgName = getValue(incomeParams, 'APPROVE_ORG_NAME')
// справочник "Параметры подразделения по налогу на прибыль" - конец

/** Отчётный период. */
def reportPeriod = reportPeriodService.get(reportPeriodId)

/** Налоговый период. */
def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.getTaxPeriod().getId()) : null)

/** Признак налоговый ли это период. */
def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)

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

/*
 * Расчет значений для текущей декларации.
 */

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
/** УбытПрошПер. Код строки декларации 301. */
def ubitProshPer = empty;
/** СумБезнадДолг. Код строки декларации 302. */
def sumBeznalDolg = empty;
/** УбытПриравнВс. Код строки декларации 300. */
def ubitPriravnVs = ubitProshPer + sumBeznalDolg
/** РасхРеалТов. Код строки декларации 190. */
def rashRealTov = empty
/** РасхДоговДУИ. Код строки декларации 220. */
def rashDolgovDUI = empty
/** РасхДоговДУИ_ВнР. Код строки декларации 221. */
def rashDolgovDUI_VnR = empty
/** ПрямРасхРеал. Код строки декларации 010. */
def pramRashReal = empty
/** ПрямРасхТоргВс. Код строки декларации 020. */
def pramRashTorgVs = empty
/* АктивРеалПред. Код строки декларации 061. */
def activRealPred = empty
/** УбытПрошОбсл. Код строки декларации 090. */
def ubitProshObsl = empty
/** НадбПокПред. Код строки декларации 120. */
def nadbPokPred = empty
/** УбытОбОбслНеобл. Код строки декларации 201. */
def ubitObObslNeobl = empty;
/** УбытДоговДУИ. Код строки декларации 230. */
def ubitDogovDUI = empty;
/** ВырРеалПред. Код строки декларации 023. */
def virRealPred = empty

// Приложение № 3 к Листу 02
/** ВыручРеалАИ. Код строки декларации 030. Код вида дохода = 10840. */
def viruchRealAI = getLong(getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10840]))
/** УбытРеалАИ. Код строки декларации 050. Код вида расхода = 21780. */
def ubitRealAI = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21780]))
/** ЦенаРеалПравЗУ. Код строки декларации 240. Код вида дохода = 10890. */
def cenaRealPravZU = getLong(getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10890]))
/** УбытРеалЗУ. Код строки декларации 260. Код вида расхода = 21390. */
def ubitRealZU = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21390]))
/** ВыручРеалПТДоСр. Код строки декларации 100. Код вида дохода = 10860. */
def viruchRealPTDoSr = getLong(getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10860]))
/** ВыручРеалПТПосСр. Код строки декларации 110. Код вида дохода = 10870. */
def viruchRealPTPosSr = getLong(getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10870]))
/** Убыт1Прев269. Код строки декларации 150. Код вида расхода = 21500. */
def ubit1Prev269 = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21500]))
/** Убыт2РеалПТ. Код строки декларации 160. Код вида расхода = 21510. */
def ubit2RealPT = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21510]))
// Приложение № 3 к Листу 02 - конец

/** ПрПодп. */
def prPodp = signatoryId
/** ВырРеалТовСоб. */
def virRealTovSob = getVirRealTovSob(dataRowsHelperComplexIncome, dataRowsHelperSimpleIncome)
/** ВырРеалИмПрав. Строка декларации 013. Код вида дохода = 10855, 10880, 10900. */
def virRealImPrav = getLong(getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10855, 10880, 10900]))
/** ВырРеалИмПроч. Строка декларации 014. Код вида дохода = 10850. */
def virRealImProch = getLong(getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [10850]))
/** ВырРеалВс. Код строки декларации 010. */
def virRealVs = virRealTovSob + virRealImPrav + virRealImProch
/** ВырРеалЦБВс. Код строки декларации 020. Код вида дохода = 11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260. */
def virRealCBVs = getLong(getComplexIncomeSumRows9(dataRowsHelperComplexIncome, [11180, 11190, 11200, 11210, 11220, 11230, 11240, 11250, 11260]))
/** ВыручОп302Ит. Код строки декларации 340. Строка 030 + строка 100 + строка 110 + строка 180 + (строка 210 – строка 211) + строка 240. */
def viruchOp302It = viruchRealAI + viruchRealPTDoSr + viruchRealPTPosSr + viruchRealTov + dohDolgovDUI - dohDolgovDUI_VnR + cenaRealPravZU
/** ДохРеал, ВырРеалИтог. */
def dohReal = virRealVs + virRealCBVs + virRealPred + viruchOp302It
/** ДохВнереал. Код строки декларации 100. */
def dohVnereal = getDohVnereal(dataRowsHelperComplexIncome, dataRowsHelperSimpleIncome)
/** КосвРасхВс. Код строки декларации 040. */
def cosvRashVs = getCosvRashVs(dataRowsHelperComplexConsumption, dataRowsHelperSimpleConsumption)
/** РасхВнереалВС. Строка 200. */
def rashVnerealVs = getRashVnerealVs(dataRowsHelperComplexConsumption, dataRowsHelperSimpleConsumption)
/** РасхВнереал. Строка 200 + строка 300. */
def rashVnereal = rashVnerealVs + ubitPriravnVs
/** РеалИмущПрав. Код строки декларации 059. Код вида расхода = 21450, 21740, 21750. */
def realImushPrav = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21450, 21740, 21750]))
/** ПриобрРеалИмущ. Код строки декларации 060. Код вида расхода = 21770. */
def priobrRealImush = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21770]))
/** ПриобРеалЦБ. Код строки декларации 070. Код вида расхода = 21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680. */
def priobrRealCB = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21662, 21664, 21666, 21668, 21670, 21672, 21674, 21676, 21678, 21680]))
/** СтоимРеалПТДоСр. Код строки декларации 120. Код вида расхода = 21460. */
def stoimRealPTDoSr = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21460]))
/** СтоимРеалПТПосСр. Код строки декларации 130. Код вида расхода = 21470. */
def stoimRealPTPosSr = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21470]))
/** СумНевозмЗатрЗУ. Код строки декларации 250. Код вида расхода = 21385. */
def sumNevozmZatrZU = getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21385])
/** ОстСтРеалАИ. Код строки декларации 040. Код вида расхода = 21760. */
def ostStRealAI = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21760]))
/** РасхОпер32, РасхОп302Ит. Код строки декларации = 080 или 350. */
def rashOper32 = ostStRealAI + stoimRealPTDoSr + stoimRealPTPosSr + rashRealTov + (rashDolgovDUI - rashDolgovDUI_VnR) + sumNevozmZatrZU
/** УбытРеалАмИм. Код строки декларации 100. Код вида расхода = 21520, 21530. */
def ubitRealAmIm = getLong(getComplexConsumptionSumRows9(dataRowsHelperComplexConsumption, [21520, 21530]))
/** УбытРеалЗемУч. Код строки декларации 110. */
def ubitRealZemUch = empty
/** РасхУмРеал, РасхПризнИтого. Код строки декларации 130. */
def rashUmReal = pramRashReal + pramRashTorgVs + cosvRashVs + realImushPrav +
        priobrRealImush + activRealPred + priobrRealCB + rashOper32 + ubitProshObsl +
        ubitRealAmIm + ubitRealZemUch + nadbPokPred
/** Убытки, УбытОп302. Код строки декларации 360. Cтрока 060 + строка 150 + строка 160 + строка 201+ строка 230 + строка 260. */
def ubitki = ubitRealAI + ubit1Prev269 + ubit2RealPT + ubitObObslNeobl + ubitDogovDUI + ubitRealZU
/** ПрибУб. */
def pribUb = dohReal + dohVnereal - rashUmReal - rashVnereal + ubitki
/** ДохИсклПриб. */
def dohIsklPrib = getDohIsklPrib(dataRowsHelperComplexIncome, dataRowsHelperSimpleIncome)
def nalBaza = pribUb - dohIsklPrib - 0 - 0 + 0
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIsch = getNalBazaIsch(nalBaza, 0)

if (xml == null) {
    return
}

/*
 * Формирование XML'ки.
 */

import groovy.xml.MarkupBuilder;
def xmlbuilder = new MarkupBuilder(xml)

xmlbuilder.Файл(
        ИдФайл : declarationService.generateXmlFileId(5, departmentId, declarationData.reportPeriodId),
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

                    // 0..1
                    ФедБдж(
                            КБК : kbk,
                            НалПУ : empty)

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

                        // 0..1
                        ФедБдж(
                                КБК : kbk,
                                АвПлат1 : avPlat1,
                                АвПлат2 : avPlat2,
                                АвПлат3 : avPlat3)

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
            }

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
                obRasch = refBookService.getNumberValue(26, tmpRow.calcFlag, 'CODE')
                naimOP = refBookService.getStringValue(30, tmpRow.regionBankDivision, 'NAME')
                kppop = refBookService.getStringValue(33, tmpRow.kpp, 'KPP')
                obazUplNalOP = refBookService.getNumberValue(25, tmpRow.obligationPayTax, 'CODE')
                dolaNalBaz = tmpRow.baseTaxOf
                nalBazaDola = tmpRow.baseTaxOfRub
                stavNalSubRF = refBookService.getNumberValue(33, tmpRow.subjectTaxStavka, 'TAX_RATE')
                sumNal = tmpRow.taxSum
                nalNachislSubRF = tmpRow.subjectTaxCredit
                sumNalP = tmpRow.taxSumToPay
                nalViplVneRF = tmpRow.taxSumOutside
                mesAvPlat = tmpRow.everyMontherPaymentAfterPeriod
                mesAvPlat1CvSled = tmpRow.everyMonthForKvartalNextPeriod
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
    return roundValue(value, 0)
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
    return result
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
    return result
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
 * Косвенные расходы, всего (КосвРасхВс).
 *
 * @param dataRowsHelper нф расходы сложные
 * @param dataRowsHelperSimple нф расходы простые
 */
def getCosvRashVs(def dataRowsHelper, def dataRowsHelperSimple) {
    def result = 0

    // Код вида расхода = 20320, 20321, 20470, 20750, 20755, 20760, 20765, 20770,
    // 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640
    result += getComplexConsumptionSumRows9(dataRowsHelper, [20320, 20321, 20470, 20750, 20755, 20760, 20765,
            20770, 20775, 20780, 20785, 21210, 21280, 21345, 21355, 21365, 21370, 21375, 21380, 21630, 21640])

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
 * Получить данные нф.
 *
 * @param form нф
 */
def getDataRowHelper(def form) {
    return (form != null ? formDataService.getDataRowHelper(form) : null)
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

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, def precision) {
    ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
}