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

// Временная переменная имитирующая номер страницы для разбиения на части количество физических лиц в декларации
@Field
int pageNumber = 1

// Количество физических лиц в одном xml-файле
@Field
final int NUMBER_OF_PERSONS = 3000

// Список физических лиц для форммирования xml файла
@Field
final ndflPersons = []

// Кэш провайдеров
@Field
def providerCache = [:]

// запись подразделения в справочнике
@Field
def departmentParam = null

// детали подразделения из справочника
@Field
def departmentParamRow = null

// отчетный период
@Field
def reportPeriod = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

@Field
def declarationTemplate = null

@Field
def formType = null

// Кэш видов доходов, где ключ код вида дохода, значение признак кода вычета
@Field
def deductionTypes = [:]

// Кэш признаков кодов вычета, где ключ код вычета, значение название кода вычета
@Field
def deductionMarks = [:]

@Field
final String DATE_FORMAT_FLATTEN = "yyyyMMdd"

@Field
final String DATE_FORMAT_DOTTED = "dd.MM.yyyy"

@Field
final String DATE_FORMAT_MONTH = "MM"

@Field
final int REF_BOOK_NDFL_ID = 950

@Field
final int REF_BOOK_NDFL_DETAIL_ID = 951

@Field
final int REB_BOOK_FORM_TYPE_ID = 931

@Field
final int REF_BOOK_DEDUCTION_TYPE_ID = 921

@Field
final int REF_BOOK_DEDUCTION_MARK_ID = 927

@Field
final String NDFL_2_S_PRIZNAKOM_1 = "2 НДФЛ (1)"

@Field
final String NDFL_2_S_PRIZNAKOM_2 = "2 НДФЛ (2)"

@Field
final String VERS_FORM = "5.04"

@Field
final String INN_YUR = "7707083893"

@Field
final String KND = "1151078"

@Field
final String PRIZNAK_KODA_VICHETA_STANDARTNIY = "Стандартный"

@Field
final String PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY = "Имущественный"

@Field
final String PRIZNAK_KODA_VICHETA_SOTSIALNIY = "Социальный"

@Field
final String PRIZNAK_KODA_VICHETA_INVESTITSIONNIY = "Инвестиционный"

@Field
final String PRIZNAK_KODA_VICHETA_OSTALNIE = "Остальные"

def buildXml() {
    //Текущая страница представляющая порядковый номер файла
    def currentPageNumber = pageNumber

    ndflPersons = getKnfData()

    // инициализация данных о подразделении
    departmentParam = getDepartmentParam()
    departmentParamRow = getDepartmentParamTable(departmentParam?.record_id?.value)

    // Отчетный период
    reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)


    declarationTemplate = getDeclarationTemplate()

    formType = getFormType(declarationTemplate.declarationFormTypeId)

    // Данные для Файл.СвРекв
    def oktmo = departmentParamRow?.OKTMO?.value
    // Данные для Файл.СвРекв.СвЮЛ
    def kpp = departmentParamRow?.KPP?.value
    def otchetGod = reportPeriod.taxPeriod.year
    def priznakF = definePriznakF()
    def dataNachalaPerioda = reportPeriodService.getStartDate(declarationData.reportPeriodId)
    def dataKontsaPerioda = reportPeriodService.getEndDate(declarationData.reportPeriodId)

    // Данные для Файл.Документ.Подписант
    def prPodp = departmentParamRow?.SIGNATORY_ID?.value
    def signatoryFirstname = departmentParamRow?.SIGNATORY_FIRSTNAME?.value
    def signatorySurname = departmentParamRow?.SIGNATORY_SURNAME?.value
    def signatoryLastname = departmentParamRow?.SIGNATORY_LASTNAME?.value
    def naimDoc = departmentParamRow?.APPROVE_DOC_NAME?.value
    def naimOrgApprove = departmentParamRow?.APPROVE_ORG_NAME?.value

    // Данные для Файл.Документ.СвНА-(Данные о налоговом агенте)
    def tlf = departmentParamRow?.PHONE?.value
    def naimOrg = departmentParamRow?.NAME?.value

    // 	Данные для Файл.Документ.ПолучДох-(Данные о физическом лице - получателе дохода)
    def listKnf = ndflPersons.collate(NUMBER_OF_PERSONS).get(currentPageNumber - 1)

    def dateDoc = Calendar.getInstance().getTime()?.format(DATE_FORMAT_DOTTED, TimeZone.getTimeZone('Europe/Moscow'))

    // Порядковый номер физического лица
    def nomSpr = (currentPageNumber - 1) * NUMBER_OF_PERSONS + 1

    // Номер корректировки
    def nomKorr = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    def kodNo = departmentParamRow?.TAX_ORGAN_CODE?.value

    def builder = new MarkupBuilder(xml)
    builder.setOmitNullAttributes(true)
    builder.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")

    builder.Файл(ИдФайл: generateXmlFileId(),
            ВерсПрог: applicationVersion,
            ВерсФорм: VERS_FORM) {
        СвРекв(ОКТМО: oktmo,
                ОтчетГод: otchetGod,
                ПризнакФ: priznakF) {
            СвЮЛ(ИННЮЛ: INN_YUR,
                    КПП: kpp) {}
        }
        listKnf.each { knfRow ->

            // Данные для Файл.Документ.СведДох-(Сведения о доходах физического лица)
            def ndflPersonIncomes = knfRow.incomes
            def ndflPersonDeductions = knfRow.deductions
            Документ(КНД: KND,
                    ДатаДок: dateDoc,
                    НомСпр: nomSpr++,
                    ОтчетГод: otchetGod,
                    Признак: priznakF,
                    НомКорр: sprintf('%02d', nomKorr),
                    КодНО: kodNo) {
                Подписант(ПрПодп: prPodp) {
                    ФИО(Фамилия: signatorySurname,
                            Имя: signatoryFirstname,
                            Отчество: signatoryLastname) {}
                    if (prPodp == 2) {
                        СвПред(НаимДок: naimDoc,
                                НаимОрг: naimOrgApprove) {}
                    }
                }
                СвНА(ОКТМО: oktmo,
                        Тлф: tlf) {
                    СвНАЮЛ(НаимОрг: naimOrg,
                            ИННЮЛ: INN_YUR,
                            КПП: kpp)
                }
                ПолучДох(ИННФЛ: knfRow.innNp,
                        ИННИно: knfRow.innForeign,
                        Статус: knfRow.status,
                        ДатаРожд: knfRow.birthDay.format(DATE_FORMAT_DOTTED),
                        Гражд: knfRow.citizenship) {
                    ФИО(Фамилия: knfRow.lastName,
                            Имя: knfRow.firstName,
                            Отчество: knfRow.middleName)
                    УдЛичнФЛ(КодУдЛичн: knfRow.idDocType,
                            СерНомДок: knfRow.idDocNumber)
                    АдрМЖРФ(Индекс: knfRow.postIndex,
                            КодРегион: knfRow.regionCode,
                            Район: knfRow.area,
                            Город: knfRow.city,
                            НаселПункт: knfRow.locality,
                            Улица: knfRow.street,
                            Дом: knfRow.house,
                            Корпус: knfRow.building,
                            Кварт: knfRow.flat)
                    АдрИНО(КодСтр: knfRow.countryCode,
                            АдрТекст: knfRow.address)
                }
                def ndflPersonIncomesGroupedByTaxRate = groupByTaxRate(ndflPersonIncomes)
                ndflPersonIncomesGroupedByTaxRate.each { taxRateGroup ->
                    СведДох(Ставка: taxRateGroup.head()?.taxRate) {
                        def selectedIncomeRows = selectIncomesForSvSumDoh(taxRateGroup, priznakF, dataNachalaPerioda, dataKontsaPerioda)
                        selectedIncomeRows.each() { row ->
                            ДохВыч() {
                                def month = row.taxDate?.format(DATE_FORMAT_MONTH)

                                def incomesGroupedByIncomeCode = groupByIncomeCode(selectedIncomeRows)
                                incomesGroupedByIncomeCode.each { incomesGroup ->
                                    def sortedIncomesGroup = incomesGroup.toSorted { item1, item2 -> item1.taxDate <=> item2.taxDate }
                                    def incomeCode = sortedIncomesGroup.get(0)?.incomeCode
                                    СвСумДох(Месяц: month,
                                            КодДоход: incomeCode,
                                            СумДоход: 100.00) {
                                        def deductionsGroupedByTypeCode = groupByTypeCode(ndflPersonDeductions)
                                        deductionsGroupedByTypeCode.each { deductionsGroup ->
                                            def selectedDeductionRows = []

                                            [deductionsGroup, sortedIncomesGroup].eachCombination { deduction, inc ->

                                                if (deduction.operationId == inc.operationId &&
                                                        deduction.incomeAccrued.getTime() == deduction.incomeAccrued.getTime() &&
                                                        deduction.incomeCode == inc.incomeCode &&
                                                        getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_OSTALNIE) &&
                                                        (deduction.periodCurrDate >= dataNachalaPerioda.getTime() && deduction.periodCurrDate <= dataKontsaPerioda.getTime())) {
                                                    selectedDeductionRows << deduction
                                                }
                                            }
                                            selectedDeductionRows.each { selectedDeduction ->
                                                СвСумВыч(КодВычет: selectedDeduction.typeCode,
                                                        СумВычет: selectedDeduction.periodCurrSumm) {

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        НалВычССИ() {
                            def selectedDeductionRows = []
                            if (taxRateGroup.head()?.taxRate == 13) {
                                [taxRateGroup, ndflPersonDeductions].eachCombination { income, deduction ->
                                    if (income.operationId == deduction.operationId &&
                                            (getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_SOTSIALNIY) ||
                                                    getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_STANDARTNIY) ||
                                                    getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY) ||
                                                    getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_INVESTITSIONNIY)) &&
                                            (deduction.periodCurrDate >= dataNachalaPerioda.getTime() && deduction.periodCurrDate <= dataKontsaPerioda.getTime())) {
                                        selectedDeductionRows << deduction
                                    }
                                }
                            }
                            def selectedDeductionsGroupedByDeductionCodeType = groupByDeductionTypeCode(selectedDeductionRows)
                            selectedDeductionsGroupedByDeductionCodeType.each { group ->
                                ПредВычССИ(КодВычет: group.head().typeCode,
                                        СумВычет: group.sum().periodCurrSumm) {
                                }
                            }
                            selectedDeductionsGroupedByDeductionCodeType.find { group ->
                                getDeductionMark(getDeductionType(group.head()?.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_SOTSIALNIY)
                            }.each {
                                УведСоцВыч(НомерУвед: it.notifNum,
                                        ДатаУвед: it.notifDate?.format(DATE_FORMAT_DOTTED),
                                        ИФНСУвед: it.notifSource)
                            }
                            selectedDeductionsGroupedByDeductionCodeType.find { group ->
                                getDeductionMark(getDeductionType(group.head()?.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY)
                            }.each {
                                УведИмущВыч(НомерУвед: it.notifNum,
                                        ДатаУвед: it.notifDate.format(DATE_FORMAT_DOTTED),
                                        ИФНСУвед: it.notifSource)
                            }
                        }
                        СумИтНалПер(СумДохОбщ: getSumDohObsh(selectedIncomeRows),
                                НалБаза: getNalBaza(selectedIncomeRows),
                                НалИсчисл: getNalIschisl(selectedIncomeRows),
                                АвансПлатФикс: getAvansPlatFix(knfRow.prepayments),
                                НалУдерж: getNalUderzh(priznakF, selectedIncomeRows),
                                НалПеречисл: getNalPerechisl(priznakF, selectedIncomeRows),
                                НалУдержЛиш: getNalUderzhLish(priznakF, selectedIncomeRows),
                                НалНеУдерж: getNalNeUderzh(selectedIncomeRows)) {

                            if (knfRow.status == "6") {
                                def prepayments = knfRow.prepayments
                                def selectedPrepayments = selectPrepaymentsForUvedFixPlat(prepayments, selectedIncomeRows, priznakF, dataNachalaPerioda, dataKontsaPerioda)
                                selectedPrepayments.each { prepayment ->
                                    УведФиксПлат(НомерУвед: prepayment.notifNum,
                                            ДатаУвед: prepayment.notifDate?.format(DATE_FORMAT_DOTTED),
                                            ИФНСУвед: prepayment.notifSource) {

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    println(xml)
}


// Генерация имени файла
def generateXmlFileId() {
    def departmentParamRow = departmentParam ? getDepartmentParamTable(departmentParam?.record_id?.value) : null
    def r_t = "NO_NDFL2"
    def a = departmentParamRow?.TAX_ORGAN_CODE_MID?.value
    def k = departmentParamRow?.TAX_ORGAN_CODE?.value
    def o = "7707083893" + declarationData.kpp
    def date = Calendar.getInstance().getTime()?.format(DATE_FORMAT_FLATTEN)
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

// Получение данных
def getKnfData() {
    ndflPersonService.findNdflPerson(declarationData.id)
}

// Выборка строк для Файл.Документ.СведДох.ДохВыч.СвСумДох
def selectIncomesForSvSumDoh(def taxRateGroup, def priznakF, def dataNachalaPerioda, def dataKontsaPerioda) {
    def selectedIncomeRows = []
    taxRateGroup.each { incomeRow ->
        if (priznakF == "1") {
            if (incomeRow.incomeAccruedSumm != 0 &&
                    ((dataNachalaPerioda?.getTime().getTime() <= incomeRow.taxDate?.getTime() && dataKontsaPerioda.getTime().getTime() >= incomeRow.taxDate?.getTime()) ||
                            (dataNachalaPerioda?.getTime().getTime() <= incomeRow.paymentDate?.getTime() && dataKontsaPerioda?.getTime().getTime() >= incomeRow?.paymentDate.getTime()))) {
                selectedIncomeRows << incomeRow
            }
        } else if (priznakF == "2") {
            if (incomeRow.notHoldingTax > 0 &&
                    ((dataNachalaPerioda?.getTime().getTime() <= incomeRow.taxDate?.getTime() && dataKontsaPerioda?.getTime().getTime() >= incomeRow.taxDate?.getTime()) ||
                            (dataNachalaPerioda?.getTime().getTime() <= incomeRow.paymentDate?.getTime() && dataKontsaPerioda?.getTime().getTime() >= incomeRow.paymentDate?.getTime()))) {
                selectedIncomeRows << incomeRow
            }
        }
    }
    return selectedIncomeRows
}

// Файл.Документ.СведДох.СумИтНалПер.УведФиксПлат
def selectPrepaymentsForUvedFixPlat(def prepayments, def selectedIncomeRows, def priznakF, def dataNachalaPerioda, def dataKontsaPerioda) {
    def selectedPrepayments = []
    prepayments.each { prepayment ->
        if (priznakF == "1" &&
                (dataNachalaPerioda?.getTime().getTime() <= prepayment.notifDate.getTime() &&
                        dataKontsaPerioda.getTime().getTime() >= prepayment.notifDate.getTime())) {
            selectedPrepayments << prepayment
        } else if (priznakF == "2" &&
                (dataNachalaPerioda?.getTime().getTime() <= prepayment.notifDate.getTime() &&
                        dataKontsaPerioda.getTime().getTime() >= prepayment.notifDate.getTime())) {
            selectedIncomeRows.each { income ->
                if(income.operationId == prepayment.operationId) {
                    selectedPrepayments << prepayment
                }
            }
        }
    }
    return selectedPrepayments
}

// группирока по налоговой ставке
def groupByTaxRate(def incomes) {
    def toReturn = []
    def rates = []
    incomes.each {
        rates << it.taxRate
        rates.toUnique()
    }
    rates.each { rate -> toReturn << incomes.findAll { it.taxRate.equals(rate) } }
    return toReturn
}

// группировка по коду дохода
def groupByIncomeCode(def incomes) {
    def toReturn = []
    def incomeCodes = []
    incomes.each {
        incomeCodes << it.incomeCode
        incomeCodes.toUnique()
    }
    incomeCodes.each { code -> toReturn << incomes.findAll { it.incomeCode.equals(code) } }
    return toReturn
}

// Группировка по виду дохода
def groupByTypeCode(def deductions) {
    def toReturn = []
    def typeCodes = []
    deductions.each {
        typeCodes << it.typeCode
        typeCodes.toUnique()
    }
    typeCodes.each { code -> toReturn << deductions.findAll { it.typeCode.equals(code) } }
    return toReturn
}

// Группировка по коду вычета
def groupByDeductionTypeCode(def deductions) {
    def toReturn = []
    def typeCodes = []
    deductions.each {
        typeCodes << it.typeCode
        typeCodes.toUnique()
    }
    typeCodes.each { typeCode -> toReturn << deductions.findAll { it.typeCode.equals(typeCode) } }
    return toReturn
}

// Вычислить сумму для СумДохОбщ
def getSumDohObsh(def incomes) {
    incomes.incomeAccruedSumm.sum()
}

// Вычислить сумму для НалБаза
def getNalBaza(def incomes) {
    incomes.taxBase.sum()
}

//Вычислить сумму для НалИсчисл
def getNalIschisl(def incomes) {
    incomes.sum { it.calculatedTax }
}

//Вычислить сумму для АвансПлатФикс
def getAvansPlatFix(def incomes) {
    incomes.sum{it.summ} //TODO: требуется найти сумму id операций???
}

//Вычислить сумму для НалУдерж
def getNalUderzh(def priznakF, def incomes) {
    if (priznakF == "1") {
        return incomes.sum { it.withholdingTax }
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалПеречисл
def getNalPerechisl(def priznakF, def incomes) {
    if (priznakF == "1") {
        return incomes.sum { it.taxSumm }
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалУдержЛиш
def getNalUderzhLish(def priznakF, def incomes) {
    if (priznakF == "1") {
        return incomes.sum { it.overholdingTax }
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для 	НалНеУдерж
def getNalNeUderzh(def incomes) {
    incomes.sum { it.notHoldingTax }
}


// Получить параметры подразделения (из справочника 950)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)

        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

// Получить детали подразделения из справочника
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamRow == null) {
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamRow = departmentParamTableList.get(0)
    }
    return departmentParamRow
}

/**
 * Получение провайдера с использованием кеширования
 * @param providerId
 * @return
 */

def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        def provider = refBookFactory.getDataProvider(providerId)
        providerCache.put(providerId, provider)
    }
    return providerCache.get(providerId)
}



def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Получить шаблон декларации
def getDeclarationTemplate() {
    if (declarationTemplate == null) {
        declarationTemplate = declarationService.getTemplate(declarationData.getDeclarationTemplateId())
    }
    return declarationTemplate
}

// Получить вид формы
def getFormType(def id) {
    if (formType == null) {
        def filter = "ID = '$id'"
        def formTypeList = getProvider(REB_BOOK_FORM_TYPE_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (formTypeList == null || formTypeList.size() == 0 || formTypeList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        formType = formTypeList.get(0)
    }
    return formType;
}

//Определить признакФ
def definePriznakF() {
    def code = formType?.CODE?.value
    switch (code) {
        case (NDFL_2_S_PRIZNAKOM_1): return "1"
        case (NDFL_2_S_PRIZNAKOM_2): return "2"
    }
}

// Получить вид вычета
def getDeductionType(def code) {
    def toReturn = deductionTypes["$code"]
    if (toReturn == null) {
        def filter = "CODE = '$code'"
        def deductionTypeList = getProvider(REF_BOOK_DEDUCTION_TYPE_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (deductionTypeList == null || deductionTypeList.size() == 0 || deductionTypeList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        deductionTypes["$code"] = deductionTypeList.get(0).DEDUCTION_MARK?.value
        toReturn = deductionTypes["$code"]
    }
    return toReturn
}

//Получить признак вычета
def getDeductionMark(def code) {
    def toReturn = deductionMarks["$code"]
    if (toReturn == null) {
        def filter = "CODE = $code"
        def deductionMarkList = getProvider(REF_BOOK_DEDUCTION_MARK_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (deductionMarkList == null || deductionMarkList.size() == 0 || deductionMarkList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        deductionMarks["$code"] = deductionMarkList.get(0).NAME?.value
        toReturn = deductionMarks["$code"]
    }
    return toReturn
}