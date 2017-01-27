package form_template.ndfl.report_2ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field
import groovy.xml.MarkupBuilder

switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверки
        println "!CHECK!"
        break
    case FormDataEvent.CALCULATE: //формирование xml
        println "!CALCULATE!"
        buildXml()
        break
    case FormDataEvent.COMPOSE: // Консолидирование
        println "!COMPOSE!"
        break
    case FormDataEvent.GET_SOURCES: //формирование списка источников
        println "!GET_SOURCES!"
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
        println "!CREATE_SPECIFIC_REPORT!"
        break
}

// Кэш провайдеров
@Field
def providerCache = [:]

@Field
def departmentParam = [:]

// значение подразделения из справочника 310 (таблица)
@Field
def departmentParamTable = [:]

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

@Field
def deductionParam = [:]


def buildXml() {
//    departmentParam = getDepartmentParam()
//    def departmentParamRow = departmentParam ? departmentParamTable(departmentParam?.record_id?.value) : null
//
//    // Данные для Файл.СвРекв
//    def oktmo = getOKTMO(departmentParamRow)
//    // Данные для Файл.СвРекв.СвЮЛ
//    def kpp = getKPP(departmentParamRow)
//    def otchetGod = 2016 // TODO: убрать времнное значение
//    def priznakF = 1 // TODO: убрать времнное значение
//    def dataNachalaPerioda = new Date(2016, 0, 1) // TODO: убрать времнное значение
//    def dataKontsaPerioda = new Date(2016, 11, 31) // TODO: убрать времнное значение
//
//    // Данные для Файл.Документ.Подписант
//    def prPodp = getPrPodp(departmentParamRow)
//    def signatoryFirstname = getSignatoryFirstname(departmentParamRow)
//    def signatorySurname = getSignatorySurname(departmentParamRow)
//    def signatoryLastname = getSignatoryLastname(departmentParamRow)
//    def naimDoc = getNaimDoc(departmentParamRow, prPodp)
//    def naimOrgApprove = getNaimOrg(departmentParamRow, prPodp)
//    // Данные для Файл.Документ.СвНА-(Данные о налоговом агенте)
//    def tlf = getTlf(departmentParamRow)
//    def naimOrg = getNaimOrg(departmentParamRow)
//    // 	Данные для Файл.Документ.ПолучДох-(Данные о физическом лице - получателе дохода)
//    def listKnf = getKnfData()
//
//
//    def nomSpr = 1

    def builder = new MarkupBuilder(xml)
    builder.Файл(/*ИдФайл: generateXmlFileId(),
            ВерсПрог: applicationVersion,
            ВерсФорм: "5.04"*/) { /*
        СвРекв(ОКТМО: oktmo,
                ОтчетГод: "", //TODO ???создать объект taxPeriodService для TaxPeriodDao и получить год из TaxPeriod???
                ПризнакФ: priznakF) { //TODO как определяется?
            СвЮЛ(ИННЮЛ: "7707083893",
                    КПП: kpp)
            СвФЛ()
        }
        listKnf.collate(3000).each { knfRow ->
            // Данные для Файл.Документ.СведДох-(Сведения о доходах физического лица)
            def ndflPersonIncomesGroupedByTaxRate = groupByTaxRate(knfRow.incomes)
            def ndflPersonDeductions = it.deductions
            Документ(КНД: "1151078",
                    ДатаДок: new Date().format("dd.MM.yyyy"),
                    НомСпр: nomSpr++,
                    ОтчетГод: "",
                    Признак: priznakF,
                    НомКорр: "", // TODO: ???
                    КодНО: "") { // TODO: конечный или промежуточный
                Подписант(ПрПодп: prPodp) {
                    ФИО(Фамилия: signatorySurname,
                            Имя: signatoryLastname,
                            Отчество: signatoryLastname)
                    СвПред(НаимДок: naimDoc,
                            НаимОрг: naimOrgApprove)
                }
                СвНА(ОКТМО: oktmo,
                        Тлф: tlf) {
                    СвНАЮЛ(НаимОрг: naimOrg,
                            ИННЮЛ: "7707083893",
                            КПП: kpp)
                    СвНАФЛ()
                }
                ПолучДох(ИННФЛ: it.innP,
                        ИННИно: it.innForeign,
                        Статус: it.status,
                        ДатаРожд: it.birthDay.format("yyyyMMdd"), //TODO уточнить формат
                        Гражд: it.citizenship) {
                    ФИО(Фамилия: it.lastName,
                            Имя: it.firstName,
                            Отчество: it.middleName)
                    УдЛичнФЛ(КодУдЛичн: it.idDocType,
                            СерНомДок: it.idDocNumber)
                    АдрМЖРФ(Индекс: it.postIndex,
                            КодРегион: it.regionCode,
                            Район: it.area,
                            Город: it.city,
                            НаселПункт: it.locality,
                            Улица: it.street,
                            Дом: it.house,
                            Корпус: it.building,
                            Кварт: it.flat)
                    АдрИНО(КодСтр: it.countryCode,
                            АдрТекст: it.address)
                }
                ndflPersonIncomesGroupedByTaxRate.each { ndflPersonIncomes ->
                    СведДох(Ставка: ndflPersonIncomes.head().taxRate) {
                        def selectedIncomeRows = []
                        ndflPersonIncomes.each {
                            if (priznakF == 1) {
                                if (it.incomeAccruedSumm != 0 &&
                                        ((dataNachalaPerioda.getTime() <= it.taxDate.getTime() && dataKontsaPerioda >= it.taxDate.getTime()) ||
                                                (dataNachalaPerioda.getTime() <= it.paymentDate.getTime() && dataKontsaPerioda >= it.paymentDate.getTime()))) {
                                    selectedIncomeRows << it
                                }
                            } else if (priznakF == 2) {
                                if (it.notHoldingTax > 0 &&
                                        ((dataNachalaPerioda.getTime() <= it.taxDate.getTime() && dataKontsaPerioda >= it.taxDate.getTime()) ||
                                                (dataNachalaPerioda.getTime() <= it.paymentDate.getTime() && dataKontsaPerioda >= it.paymentDate.getTime()))) {
                                    selectedIncomeRows << it
                                }
                            }
                        }
                        def sortedSelectedRows = selectedIncomeRows.toSorted { item1, item2 -> item1.taxDate <=> item2.taxDate }
                        sortedSelectedRows.each() {
                            ДохВыч() {
                                def month = Calendar.getInstance().setTime(it.taxDate).format("MM")
                                def selectedDeductionRows = []
                                [ndflPersonDeductions, selectedIncomeRows].eachCombination { deduction, income ->
                                    if (deduction.operationId == income.operationId &&
                                            deduction.incomeAccrued.getTime() == income.incomeAccruedDate &&
                                            deduction.incomeCode.equalsIgnoreCase(income.incomeCode) &&
                                            deduction.incomeCode.equals(getDeductionParam("Остальные").CODE?.value.toString()) &&
                                            (deduction.periodCurrDate >= dataNachalaPerioda.getTime() && deduction.periodCurrDate <= dataKontsaPerioda.getTime())) {
                                        selectedDeductionRows << deduction
                                    }
                                }
                                СвСумДох(Месяц: month,
                                        КодДоход: it.incomeCode,
                                        СумДоход: getSumDohod(selectedIncomeRows, it.operationId)) {
                                    selectedDeductionRows.each {
                                        СвСумВыч(КодВычет: it.typeCode,
                                                СумВычет: it.periodCurrSumm) {

                                        }
                                    }
                                }
                            }
                        }


                        НалВычССИ() {
                            def selectedDeductionRows = []
                            [ndflPersonDeductions, selectedIncomeRows].eachCombination { deduction, income ->
                                if (income.taxRate == 13 &&
                                        (deduction.incomeCode.equals(getDeductionParam("Социальный").CODE?.value.toString()) ||
                                                deduction.incomeCode.equals(getDeductionParam("Стандартный").CODE?.value.toString()) ||
                                                deduction.incomeCode.equals(getDeductionParam("Имущественный").CODE?.value.toString()) ||
                                                deduction.incomeCode.equals(getDeductionParam("Инвестиционный").CODE?.value.toString())) &&
                                        deduction.incomeCode.equalsIgnoreCase(income.incomeCode) &&
                                        (deduction.periodCurrDate >= dataNachalaPerioda.getTime() && deduction.periodCurrDate <= dataKontsaPerioda.getTime())) {
                                    // TODO: уменьшить количество селектов
                                    selectedDeductionRows << deduction
                                }
                            }
                            def selectedNdflPersonDeductionGroupedByDeductionMark = groupByDeductionMark(selectedDeductionRows)
                            selectedNdflPersonDeductionGroupedByDeductionMark.each { marks ->
                                ПредВычССИ(КодВычет: it.head().typeCode,
                                        СумВычет: marks.sum().periodCurrSumm) {
                                }
                            }
                            selectedNdflPersonDeductionGroupedByDeductionMark.find {
                                it.incomeCode.equals(getDeductionParam("Социальный").CODE?.value.toString())
                            }.each {
                                УведСоцВыч(НомерУвед: it.notifNum,
                                        ДатаУвед: it.notifDate.format("yyyyMMdd"),
                                        ИФНСУвед: it.notifSource)
                            }
                            selectedNdflPersonDeductionGroupedByDeductionMark.find {
                                it.incomeCode.equals(getDeductionParam("Имущественный").CODE?.value.toString())
                            }.each {
                                УведИмущВыч(УведИмущВыч: it.notifNum,
                                        ДатаУвед: it.notifDate.format("yyyyMMdd"),
                                        ИФНСУвед: it.notifSource)
                            }
                        }
                        СумИтНалПер(СумДохОбщ: getSumDohObsh(priznakF, ndflPersonIncomes),
                                НалБаза: getNalBaza(priznakF, ndflPersonIncomes),
                                НалИсчисл: getNalIschisl(priznakF, ndflPersonIncomes),
                                АвансПлатФикс: getAvansPlatFix(priznakF, ndflPersonIncomes),
                                НалУдерж: getNalUderzh(priznakF, ndflPersonIncomes),
                                НалПеречисл: getNalPerechisl(priznakF, ndflPersonIncomes),
                                НалУдержЛиш: getNalUderzhLish(priznakF, ndflPersonIncomes),
                                НалНеУдерж: getNalNeUderzh(priznakF, ndflPersonIncomes)) {
                            if (knfRow.status.equals("6")) {
                                def prepayments = knfRow.prepayments
                                prepayments.each { prepayment ->
                                    УведФиксПлат(НомерУвед: prepayment.notifNum,
                                            ДатаУвед: prepayment.notifDate.format("yyyyMMdd"),
                                            ИФНСУвед: prepayment.notifSource) {

                                    }
                                }
                            }
                        }
                    }
                }
            }

        }*/
    }
}

def generateXmlFileId() {

    def departmentParam = getDepartmentParam()

    def departmentParamRow = departmentParam ? getDepartmentParamTable(departmentParam?.record_id?.value) : null
    def r_t = "NO_NDFL2"
    def a = departmentParamRow?.TAX_ORGAN_CODE_MID?.value
    def k = departmentParamRow?.TAX_ORGAN_CODE?.value
    def o = "7707083893"
    def date = Calendar.getInstance().getTime()?.format("yyyyMMdd")
    def n = UUID.randomUUID().toString().toUpperCase()
    // R_T_A_K_O_GGGGMMDD_N
    def fileId = r_t + '_' +
            a + '_' +
            k + '_' +
            o + '_' +
            date + '_' +
            n
    return fileId
}

def getOKTMO(def departmentParamRow) {
    departmentParamRow?.OKTMO?.value
}

def getKPP(def departmentParamRow) {
    departmentParamRow?.KPP?.value
}

def getPrPodp(def departmentParamRow) {
    departmentParamRow?.SIGNATORY_ID?.value
}

def getSignatoryFirstname(def departmentParamRow) {
    departmentParamRow?.SIGNATORY_FIRSTNAME?.value
}

def getSignatorySurname(def departmentParamRow) {
    departmentParamRow?.SIGNATORY_SURNAME?.value
}

def getSignatoryLastname(def departmentParamRow) {
    departmentParamRow?.SIGNATORY_LASTNAME?.value
}

def getNaimDoc(def departmentParamRow, def prPodp) {
    prPodp == 2 ? departmentParamRow?.APPROVE_DOC_NAME?.value : ""
}

def getNaimOrg(def departmentParamRow, def prPodp) {
    prPodp == 2 ? departmentParamRow?.APPROVE_ORG_NAME?.value : ""
}

def getTlf(def departmentParamRow) {
    departmentParamRow?.PHONE?.value
}

def getNaimOrg(def departmentParamRow) {
    departmentParamRow?.NAME?.value
}

def getKnfData() {
    ndflPersonService.findNdflPerson(declarationData.id)
}

def groupByTaxRate(def incomes) {
    def toReturn = []
    def rates = []
    incomes.each {
        rates << it.taxRate
        rates.toUnique()
        rates.sort()
    }
    rates.each { rate -> toReturn << incomes.findAll { it.taxRate == rate } }
    return toReturn
}

def getSumDohod(def rows, def operationId) {
    def toReturn = 0
    rows.each { if (it.operationId == operationId) toReturn += it.incomeAccruedSumm }
    return toReturn
}

def groupByDeductionMark(def deductions) {
    def toReturn = []
    def marks = []
    deductions.each {
        marks << it.typeCode
        marks.toUnique()
        marks.sort()
    }
    marks.each { mark -> toReturn << deductions.findAll { it.typeCode.equals(mark) } }
    return toReturn
}

def getSumDohObsh(def priznakF, def incomes) {

}

def getNalBaza(def priznakF, def incomes) {

}

def getNalIschisl(def priznakF, def incomes) {

}

def getAvansPlatFix(def priznakF, def incomes) {

}

def getNalUderzh(def priznakF, def incomes) {

}

def getNalPerechisl(def priznakF, def incomes) {

}

def getNalUderzhLish(def priznakF, def incomes) {

}

def getNalNeUderzh(def priznakF, def incomes) {

}

def getDeductionParam(def name) {
    getProvider(921).getRecords(getReportPeriodEndDate() - 1, null, "NAME = '$name'", null).get(0)
}

// Получить параметры подразделения (из справочника 950)

def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(950).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}


def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "LINK = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(951).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

/**
 * Получение провайдера с использованием кеширования
 * @param providerId
 * @return
 */

def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}