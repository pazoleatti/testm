/**
 * Формирование XML для декларации налога на прибыль уровня обособленного подразделения (declarationOP.groovy).
 *
 * @author rtimerbaev
 * @since 24.03.2013 11:30
 */

if (formDataCollection == null || formDataCollection.records.isEmpty()) {
    logger.error('Отсутствуют выходные или сводные налоговые формы в статусе "Принят". Формирование декларации невозможно.')
    return
}

/*
* Константы.
*/
def empty = 0
/** Костыльные пустые значения для отсутсвтующих блоков (=null или 0). */
def emptyNull = 0
def knd = '1151006'
def kbk = '18210101011011000110'
def typeNP = '1'

/** Настройки подразделения. */
def departmentParam = departmentService.getDepartmentParam(departmentId)
if (departmentParam == null) {
    departmentParam = new DepartmentParam()
}
/** Параметры подразделения по налогу на прибыль. */
def departmentParamIncome = departmentService.getDepartmentParamIncome(departmentId)
/** Отчётный период. */
def reportPeriod = reportPeriodService.get(reportPeriodId)
/** Налоговый период. */
def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)
/** Признак налоговый ли это период. */
def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)

/*
 * Данные налоговых форм.
 */

logger.info('===== formDataCollection = ' + (formDataCollection != null ? formDataCollection.records.size() : 'null'))
/** Доходы сложные уровня Банка "Сводная форма начисленных доходов". */
def formDataComplexIncome = formDataCollection.find(departmentId, 302, FormDataKind.SUMMARY)

/** Доходы простые уровня Банка "Расшифровка видов доходов, учитываемых в простых РНУ". */
def formDataSimpleIncome = formDataCollection.find(departmentId, 301, FormDataKind.SUMMARY)

/** Расходы сложные уровня Банка "Сводная форма начисленных расходов". */
def formDataComplexConsumption = formDataCollection.find(departmentId, 303, FormDataKind.SUMMARY)

/** Расходы простые уровня Банка "Расшифровка видов расходов, учитываемых в простых РНУ". */
def formDataSimpleConsumption = formDataCollection.find(departmentId, 304, FormDataKind.SUMMARY)

/** Выходная налоговая формы Банка «Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации». */
def formDataAdvance = formDataCollection.find(departmentId, 309, FormDataKind.ADDITIONAL)

/** Сведения для расчёта налога с доходов в виде дивидендов. */
def formDataDividend = formDataCollection.find(departmentId, 306, FormDataKind.ADDITIONAL)

/** Расчет налога на прибыль с доходов, удерживаемого налоговым агентом. */
def formDataTaxAgent = formDataCollection.find(departmentId, 307, FormDataKind.ADDITIONAL)

/** Расчет налога на прибыль организаций с доходов, исчисленного по ставкам, личным от ставки, указанной в пункте 1 статьи 284 НК. */
// TODO (Ramil Timerbaev) пока эта форма не реализована, когда появиться, то раскоментировать ее получение
// def formDataCalcTaxIncome = formDataCollection.find(departmentId, ????, FormDataKind.ADDITIONAL)

/** Выходная налоговая форма «Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика». */
def formDataTaxSum = formDataCollection.find(departmentId, 308, FormDataKind.ADDITIONAL)

/*
 * Расчет значений для текущей декларации.
 */

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
def virRealVs = getInt(virRealTovSob + virRealImPrav + virRealImProch)
/** ВырРеалЦБВс. Код вида дохода = 11271..11280. */
def virRealCBVs = getComplexIncomeSumRows9(formDataComplexIncome, (24..32))
/** ВыручОп302Ит. Код вида дохода = 10840, 10860, 10870, 10872. */
def viruchOp302It = getComplexIncomeSumRows9(formDataComplexIncome, [9, 13, 14, 16])
/** ДохРеал, ВырРеалИтог. */
def dohReal = getInt(virRealVs + virRealCBVs + viruchOp302It)
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
def rashUmReal = getInt(cosvRashVs + realImushPrav + priobrRealImush +
        priobrRealCB + rashOper32 + ubitRealAmIm + ubitRealZemUch)
/** Убытки, УбытОп302. Код вида расхода = 21659, 21515, 21518, 21397. */
def ubitki = getComplexConsumptionSumRows9(formDataComplexConsumption, [92, 76, 77, 64])
/** ПрибУб. */
def pribUb = getInt(dohReal + dohVnereal - rashUmReal - rashVnereal + ubitki)
def dohIsklPrib = getDohIsklPrib(formDataSimpleIncome)
def nalBaza = getInt(pribUb - dohIsklPrib - 0 - 0 + 0)
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIsch = getNalBazaIsch(nalBaza, 0)
/** НалИсчислФБ. */
def nalIschislFB = getNalIschislFB(nalBazaIsch, departmentParamIncome.taxRate)
/** НалИсчислСуб. Столбец «Сумма налога в бюджет субъекта РФ [070]». */
def nalIschislSub = getFormDataSumByColName(formDataAdvance, 'subjectTaxSum')
/** НалИсчисл. */
def nalIschisl = getInt(nalIschislFB + nalIschislSub)
/** НалВыпл311. */
def nalVipl311 = getInt(departmentParamIncome.externalTaxSum)
/** НалВыпл311ФБ. */
def nalVipl311FB = getInt(0.1 * departmentParamIncome.externalTaxSum)
/** НалВыпл311Суб. */
def nalVipl311Sub = getInt(0.9 * departmentParamIncome.externalTaxSum)
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
def vneRealDohCor = getInt(departmentParamIncome.correctionSum)
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
def rashZemUchVs = getInt(rashZemUch30pr + rashZemUchSrocRas + rashZemUchSrocAr)
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
        ИдФайл : declarationService.generateXmlFileId(5, departmentId),
        ВерсПрог : departmentParamIncome.appVersion,
        ВерсФорм : departmentParamIncome.formatVersion) {

    // Титульный лист
    Документ(
            КНД :  knd,
            ДатаДок : new Date().format('dd.MM.yyyy'),
            Период : period,
            ОтчетГод : (taxPeriod != null && taxPeriod.startDate != null ? taxPeriod.startDate.format('yyyy') : empty),
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

                if (departmentParam.reorgFormCode != null) {
                    СвРеоргЮЛ(
                            ФормРеорг : departmentParam.reorgFormCode,
                            ИННЮЛ : departmentParam.reorgInn,
                            КПП : departmentParam.reorgKpp)
                }
            }
        }

        Подписант(ПрПодп : prPodp) {
            ФИО(
                    Фамилия : departmentParamIncome.signatoryLastName,
                    Имя : departmentParamIncome.signatoryFirstName,
                    Отчество : departmentParamIncome.signatorySurname)
            if (prPodp != 1) {
                СвПред(
                        [НаимДок : departmentParamIncome.approveDocName] +
                                (departmentParamIncome.approveOrgName != null ? [НаимОрг : departmentParamIncome.approveOrgName] : [:] )
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
                        ОКАТО : departmentParam.okato) {

                    def nalPu = empty
                    // 0..1
                    ФедБдж(
                            КБК : empty,
                            НалПУ : nalPu)

                    // получение строки текущего подразделения, затем значение столбца «Сумма налога к доплате [100]»
                    def rowForNalPu = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
                    tmpValue2 = (rowForNalPu != null ? rowForNalPu.taxSumToPay : 0)
                    nalPu = tmpValue2
                    // 0..1
                    СубБдж(
                            КБК : kbk,
                            НалПУ : getInt(nalPu))
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
                            [ТипНП : typeNP] +
                                    (cvartalIch != 0 ? [КварталИсч : cvartalIch] : [:]) +
                                    [ОКАТО : departmentParam.okato]) {

                        def avPlat1 = empty
                        def avPlat2 = empty
                        def avPlat3 = empty

                        // 0..1
                        ФедБдж(
                                КБК : empty,
                                АвПлат1 : avPlat1,
                                АвПлат2 : avPlat2,
                                АвПлат3 : avPlat3)

                        if (!isTaxPeriod) {
                            def appl5List02Row120 = 0
                            // при формировании декларации банка надо брать appl5List02Row120 относящегося к ЦА (как определять пока не ясно, толи по id, толи по id сбербанка, толи по КПП = 775001001), при формировании декларации подразделения надо брать строку appl5List02Row120 относящегося к этому подразделению
                            def rowForAvPlat = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
                            appl5List02Row120 = (rowForAvPlat ? rowForAvPlat.everyMontherPaymentAfterPeriod : 0)
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
                                    Срок : (row.dateOfPayment != null ? row.dateOfPayment.format('dd.MM.yyyy') : empty),
                                    НалПУ : getInt(row.sumTax))
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

            // Приложение № 5 к Листу 02
            /** ОбРасч. Столбец «Код строки 002». */
            def obRasch = emptyNull
            /** НаимОП. Столбец «Наименование подразделения». */
            def naimOP = emptyNull
            /** КППОП. Столбец «КПП». */
            def kppop = emptyNull
            /** ОбязУплНалОП. Столбец «Отметка о возложении обязанности по уплате налога». */
            def obazUplNalOP = emptyNull
            /** ДоляНалБаз. Столбец «Доля налоговой базы (%) [040]». */
            def dolaNalBaz = emptyNull
            /** НалБазаДоля. Столбец «Налоговая база исходя из доли (руб.) [050]». */
            def nalBazaDola = emptyNull
            /** СтавНалСубРФ. Столбец «Ставка налога % в бюджет субъекта РФ [060]». */
            def stavNalSubRF = emptyNull
            /** СумНал. Столбец «Сумма налога в бюджет субъекта РФ [070]». */
            def sumNal = emptyNull
            /** НалНачислСубРФ. Столбец «Начислено налога в бюджет субъекта РФ. Расчётный [080]». */
            def nalNachislSubRF = emptyNull
            /** СумНалП. Столбец «Сумма налога к доплате [100]». */
            def sumNalP = emptyNull
            /** НалВыплВнеРФ. Столбец «Сумма налога, выплаченная за пределами РФ [090]». */
            def nalViplVneRF = emptyNull
            /** МесАвПлат. Столбец «Ежемесячные авансовые платежи в квартале, следующем за отчётным периодом [120]». */
            def mesAvPlat = emptyNull
            /** МесАвПлат1КвСлед. Столбец «Ежемесячные авансовые платежи на 1 квартал следующего налогового периода [121]». */
            def mesAvPlat1CvSled = emptyNull

            // получение из нф авансовых платежей строки соответствующей текущему подразделению
            def tmpRow = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
            if (tmpRow != null) {
                obRasch = tmpRow.stringCode
                naimOP = tmpRow.divisionName
                kppop = tmpRow.kpp
                obazUplNalOP = tmpRow.labalAboutPaymentTax
                dolaNalBaz = tmpRow.baseTaxOf
                nalBazaDola = getInt(tmpRow.baseTaxOfRub)
                stavNalSubRF = tmpRow.subjectTaxStavka
                sumNal = getInt(tmpRow.subjectTaxSum)
                nalNachislSubRF = getInt(tmpRow.subjectTaxCredit)
                sumNalP = getInt(tmpRow.taxSumToPay)
                nalViplVneRF = getInt(tmpRow.taxSumOutside)
                mesAvPlat = getInt(tmpRow.everyMontherPaymentAfterPeriod)
                mesAvPlat1CvSled = getInt(tmpRow.everyMonthForKvartalNextPeriod)
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

            // 0..1
            НалУдНА() {
                // Лист 03 А
                if (formDataDividend != null) {
                    formDataDividend.dataRows.each { row ->
                        // Лист 03 А
                        // 0..n
                        НалДохДив(
                                ВидДив : row.dividendType,
                                НалПер : row.taxPeriod,
                                ОтчетГод : row.financialYear,
                                ДивВсего : getInt(row.dividendSumRaspredPeriod),
                                НалИсчисл : getInt(row.taxSum),
                                НалДивПред : getInt(row.taxSumFromPeriod),
                                НалДивПосл : getInt(row.taxSumFromPeriodAll)) {

                            // 0..1
                            ДивИОФЛНеРез(
                                    ДивИнОрг : getInt(row.dividendForgeinOrgAll),
                                    ДивФЛНеРез : getInt(row.dividendForgeinPersonalAll),
                                    ДивИсч0 : getInt(row.dividendStavka0),
                                    ДивИсч5 : getInt(row.dividendStavkaLess5),
                                    ДивИсч10 : getInt(row.dividendStavkaMore5),
                                    ДивИсчСв10 : getInt(row.dividendStavkaMore10))
                            // 0..1
                            ДивРА(
                                    ДивРАВс : getInt(row.dividendRussianMembersAll),
                                    ДивРО9 : getInt(row.dividendRussianOrgStavka9),
                                    ДивРО0 : getInt(row.dividendRussianOrgStavka0),
                                    ДивФЛРез : getInt(row.dividendPersonRussia),
                                    ДивНеНП : getInt(row.dividendMembersNotRussianTax))
                            // 0..1
                            ДивНА(
                                    ДивНАдоРас : getInt(row.dividendAgentAll),
                                    ДивНАдоРас0 : getInt(row.dividendAgentWithStavka0))
                            // 0..1
                            ДивНал(
                                    ДивНалВс : getInt(row.dividendSumForTaxAll),
                                    ДивНал9 : getInt(row.dividendSumForTaxStavka9),
                                    ДивНал0 : getInt(row.dividendSumForTaxStavka0))
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
                if (formDataTaxAgent != null) {
                    formDataTaxAgent.dataRows.each { row ->
                        // 0..n
                        РеестрСумДив(
                                ДатаПерДив : (row.dividendDate != null ? row.dividendDate.format('dd.MM.yyyy') : empty),
                                СумДив : getInt(row.sumDividend),
                                СумНал : getInt(row.sumTax)) {

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
            // костыль: пустые данные при отсутствии нф или данных в нф
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
                    ВидОпер : 5, // в xml принимает 0, в печатном виде 5
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
            if (svCelSred.size() > 0) {
                // 0..1
                ДохНеУчНБ_РасхУчОКН() {
                    svCelSred.each { id, result ->
                        // 1..n
                        СвЦелСред(
                                КодВидРасход : id,
                                СумРасход : getInt(result))
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
def getInt(def value) {
    return (int) round(value, 0)
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
        result = summ(form, new ColumnRange(colName, 0, form.dataRows.size() - 1))
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
    def result = 0
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
    def result = 0
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
        def row	= formSimple.getDataRow('R220')
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
        def tmp = new BigDecimal(kpp)
        if (tmp == row.kpp) {
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