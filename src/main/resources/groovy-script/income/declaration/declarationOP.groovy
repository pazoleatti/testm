/**
 * Формирование XML для декларации налога на прибыль уровня обособленного подразделения (declarationOP.groovy).
 *
 * @author rtimerbaev
 * @since 24.03.2013 11:30
 */

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

/** Расчет налога на прибыль организаций с доходов, исчисленного по ставкам, личным от ставки, указанной в пункте 1 статьи 284 НК. */
def formDataCalcTaxIncome = formDataCollection.find(departmentId, 310, FormDataKind.ADDITIONAL)

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
def virRealVs = getLong(virRealTovSob + virRealImPrav + virRealImProch)
/** ВырРеалЦБВс. Код вида дохода = 11271..11280. */
def virRealCBVs = getComplexIncomeSumRows9(formDataComplexIncome, (24..32))
/** ВыручОп302Ит. Код вида дохода = 10840, 10860, 10870, 10872. */
def viruchOp302It = getComplexIncomeSumRows9(formDataComplexIncome, [9, 13, 14, 16])
/** ДохРеал, ВырРеалИтог. */
def dohReal = getLong(virRealVs + virRealCBVs + viruchOp302It)
/** ДохВнереал. */
def dohVnereal = getDohVnereal(formDataComplexIncome, formDataSimpleIncome, formDataCalcTaxIncome)
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
def rashUmReal = getLong(cosvRashVs + realImushPrav + priobrRealImush +
        priobrRealCB + rashOper32 + ubitRealAmIm + ubitRealZemUch)
/** Убытки, УбытОп302. Код вида расхода = 21659, 21515, 21518, 21397. */
def ubitki = getComplexConsumptionSumRows9(formDataComplexConsumption, [92, 76, 77, 64])
/** ПрибУб. */
def pribUb = getLong(dohReal + dohVnereal - rashUmReal - rashVnereal + ubitki)
def dohIsklPrib = getDohIsklPrib(formDataSimpleIncome, formDataCalcTaxIncome)
def nalBaza = getLong(pribUb - dohIsklPrib - 0 - 0 + 0)
/** НалБазаИсч, НалБазаОрг. */
def nalBazaIsch = getNalBazaIsch(nalBaza, 0)

if (xml == null) {
    return
}

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
            ДатаДок : docDate != null ? docDate: new Date().format("dd.MM.yyyy"),
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
                    Фамилия : departmentParamIncome.signatorySurname,
                    Имя : departmentParamIncome.signatoryFirstName,
                    Отчество : departmentParamIncome.signatoryLastName)
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

                    // 0..1
                    ФедБдж(
                            КБК : kbk,
                            НалПУ : empty)

                    // получение строки текущего подразделения, затем значение столбца «Сумма налога к доплате [100]»
                    def rowForNalPu = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
                    tmpValue2 = (rowForNalPu != null ? rowForNalPu.taxSumToPay : 0)
                    def nalPu = tmpValue2
                    // 0..1
                    СубБдж(
                            КБК : kbk2,
                            НалПУ : getLong(nalPu))
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
                                КБК : kbk,
                                АвПлат1 : avPlat1,
                                АвПлат2 : avPlat2,
                                АвПлат3 : avPlat3)

                        if (!isTaxPeriod) {
                            def appl5List02Row120 = 0
                            // при формировании декларации банка надо брать appl5List02Row120 относящегося к ЦА (как определять пока не ясно, толи по id, толи по id сбербанка, толи по КПП = 775001001), при формировании декларации подразделения надо брать строку appl5List02Row120 относящегося к этому подразделению
                            def rowForAvPlat = getRowAdvanceForCurrentDepartment(formDataAdvance, departmentParam.kpp)
                            appl5List02Row120 = (rowForAvPlat ? rowForAvPlat.everyMontherPaymentAfterPeriod : 0)
                            avPlat3 = (long) appl5List02Row120 / 3
                            avPlat2 = avPlat1
                            avPlat1 = avPlat1 + getTail(appl5List02Row120, 3)
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
                nalBazaDola = getLong(tmpRow.baseTaxOfRub)
                stavNalSubRF = tmpRow.subjectTaxStavka
                sumNal = getLong(tmpRow.subjectTaxSum)
                nalNachislSubRF = getLong(tmpRow.subjectTaxCredit)
                sumNalP = getLong(tmpRow.taxSumToPay)
                nalViplVneRF = getLong(tmpRow.taxSumOutside)
                mesAvPlat = getLong(tmpRow.everyMontherPaymentAfterPeriod)
                mesAvPlat1CvSled = getLong(tmpRow.everyMonthForKvartalNextPeriod)
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
    return (long) round(value, 0)
}

/**
 * Получить остаток (целое и дробное) от деления вдух чисел (например 16,5 / 3 = 1,5).
 */
def getTail(def value, def div) {
    return getLong(value - (long) value + (long) value % div)
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
    return getLong(result)
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
    return getLong(result)
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
    return getLong(result)
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
    return getLong(result)
}

/**
 * Получить внереализационные доходы (ДохВнереал, ВнеРеалДохВс).
 *
 * @param form нф доходы сложные
 * @param formSimple нф доходы простые
 * @param formCalcTaxIncome 5ая выходная нф "Расчет налога на прибыль организаций с доходов, исчисленного по ставкам, личным от ставки, указанной в пункте 1 статьи 284 НК"
 */
def getDohVnereal(def form, def formSimple, def formCalcTaxIncome) {
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

    result += getFormDataSumByColName(formCalcTaxIncome, 'base')

    if (formSimple != null) {
        // Код вида дохода = 13092
        def row	= formSimple.getDataRow('R165')
        result -= getValue(row.rnu4Field5Accepted)
    }

    return getLong(result)
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

    return getLong(result)
}

/**
 * Получить доходы, исключаемые из прибыли (ДохИсклПриб).
 *
 * @param formSimple нф доходы простые
 * @param formCalcTaxIncome 5ая выходная нф "Расчет налога на прибыль организаций с доходов, исчисленного по ставкам, личным от ставки, указанной в пункте 1 статьи 284 НК"
 */
def getDohIsklPrib(def formSimple, def formCalcTaxIncome) {
    def result = 0.0

    if (formSimple != null) {
        // Код вида дохода = 14000
        def row	= formSimple.getDataRow('R220')
        result += getValue(row.rnu4Field5Accepted)
    }

    result += getFormDataSumByColName(formCalcTaxIncome, 'base')

    return getLong(result)
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
    return getLong(row100 - row110)
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

    return getLong(result)
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
        if (kpp == row.kpp) {
            return row
        }
    }
    return null
}