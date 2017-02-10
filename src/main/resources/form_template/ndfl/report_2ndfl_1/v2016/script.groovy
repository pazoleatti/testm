package form_template.ndfl.report_2ndfl_1.v2016

import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils;
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord

switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверки
        println "!CHECK!"
        break
    case FormDataEvent.CALCULATE: //формирование xml
        println "!CALCULATE!"
        buildXml(xml)
        break
    case FormDataEvent.COMPOSE: // Консолидирование
        println "!COMPOSE!"
        break
    case FormDataEvent.GET_SOURCES: //формирование списка источников
        println "!GET_SOURCES!"
        getSources()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
        println "!CREATE_SPECIFIC_REPORT!"
        createSpecificReport()
        break
    case FormDataEvent.CREATE_FORMS: // создание экземпляра
        println "!CREATE_FORMS!"
        createForm()
        break
    case FormDataEvent.CREATE_REPORTS:
        println "!CREATE_REPORTS!"
        createReports()
        break
}
/************************************* ДАННЫЕ ДЛЯ ОБЩИХ СОБЫТИЙ *******************************************************/

/************************************* СОЗДАНИЕ XML *****************************************************************/
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
final int NDFL_REFERENCES = 964

@Field
final int REF_BOOK_DOC_STATE = 929

@Field
final String NDFL_2_S_PRIZNAKOM_1 = "2 НДФЛ (1)"

@Field
final String NDFL_2_S_PRIZNAKOM_2 = "2 НДФЛ (2)"

@Field
final String NDFL_6 = "6 НДФЛ"

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

@Field
final String PART_NUMBER = "partNumber"

@Field
final String PART_TOTAL = "partTotal"

@Field
def ndflReferencess = []

@Field
final NDFL_REFERENCES_DECLARATION_DATA_ID = "DECLARATION_DATA_ID"

@Field
final NDFL_REFERENCES_PERSON_ID = "PERSON_ID"

@Field
final NDFL_REFERENCES_NUM = "NUM"

@Field
final NDFL_REFERENCES_SURNAME = "SURNAME"

@Field
final NDFL_REFERENCES_NAME = "NAME"

@Field
final NDFL_REFERENCES_LASTNAME = "LASTNAME"

@Field
final NDFL_REFERENCES_BIRTHDAY = "BIRTHDAY"

@Field
final NDFL_REFERENCES_ERRTEXT = "ERRTEXT"


def buildXml(def writer) {
    buildXml(writer, false)
}

def buildXmlForSpecificReport(def writer) {
    buildXml(writer, true)
}

def buildXml(def writer, boolean isForSpecificReport) {
    if (hasProperty(PART_NUMBER)) {
        pageNumber = formMap[PART_NUMBER]
    }
    //Текущая страница представляющая порядковый номер файла
    def currentPageNumber = pageNumber

    ndflPersons = getNdflPersons()

    // инициализация данных о подразделении
    departmentParam = getDepartmentParam(declarationData.departmentId, declarationData.reportPeriodId)
    departmentParamRow = getDepartmentParamTable(departmentParam?.id, declarationData.reportPeriodId)

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
    def startDate = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()
    def endDate = reportPeriodService.getEndDate(declarationData.reportPeriodId).getTime()

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
    def listKnf = ndflPersons

    // Порядковый номер физического лица
    def nomSpr = (currentPageNumber - 1) * NUMBER_OF_PERSONS + 1

    def dateDoc = Calendar.getInstance().getTime()?.format(DATE_FORMAT_DOTTED, TimeZone.getTimeZone('Europe/Moscow'))

    // Номер корректировки
    def nomKorr = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    def kodNo = departmentParamRow?.TAX_ORGAN_CODE?.value

    def builder = new MarkupBuilder(writer)

    builder.setOmitNullAttributes(true)
    builder.Файл(ИдФайл: generateXmlFileId(),
            ВерсПрог: applicationVersion,
            ВерсФорм: VERS_FORM) {
        СвРекв(ОКТМО: oktmo,
                ОтчетГод: otchetGod,
                ПризнакФ: priznakF) {
            СвЮЛ(ИННЮЛ: INN_YUR,
                    КПП: kpp) {}
        }
        listKnf.each { np ->
            // Порядковый номер физического лица
            if (isCorrectionPeriod()) {
                nomspr = getProvider(NDFL_REFERENCES).getRecords(getReportPeriodEndDate(reportPeriodId) - 1, null, "PERSON_ID = ${np.personId}", null).get(0).num
            }
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
                ПолучДох(ИННФЛ: np.innNp,
                        ИННИно: np.innForeign,
                        Статус: np.status,
                        ДатаРожд: np.birthDay.format(DATE_FORMAT_DOTTED),
                        Гражд: np.citizenship) {
                    ФИО(Фамилия: np.lastName,
                            Имя: np.firstName,
                            Отчество: np.middleName)
                    УдЛичнФЛ(КодУдЛичн: np.idDocType,
                            СерНомДок: np.idDocNumber)
                    АдрМЖРФ(Индекс: np.postIndex,
                            КодРегион: np.regionCode,
                            Район: np.area,
                            Город: np.city,
                            НаселПункт: np.locality,
                            Улица: np.street,
                            Дом: np.house,
                            Корпус: np.building,
                            Кварт: np.flat)
                    АдрИНО(КодСтр: np.countryCode,
                            АдрТекст: np.address)
                }

                // Данные для Файл.Документ.СведДох-(Сведения о доходах физического лица)
                def ndflPersonIncomes = findIncomes(np.id, priznakF, startDate, endDate)
                // Сведения о вычетах
                def ndflPersonDeductions = findDeductions(np.id, startDate, endDate, ndflPersonIncomes)
                // Сведения о доходах сгруппированные по ставке
                def ndflPersonIncomesGroupedByTaxRate = groupByTaxRate(ndflPersonIncomes)
                // Сведения о доходах сгруппированные по коду дохода
                def ndflPersonIncomesGroupedByIncomeCode = groupByIncomeCode(ndflPersonIncomes)
                // Сведения о вычетах с признаком "Остальные"
                def deductionsSelectedForDeductionsInfo = ndflPersonDeductions.findAll {
                    getDeductionMark(getDeductionType(it?.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_OSTALNIE)
                }
                // Сведения о вычетах сгруппированные по коду вычета
                def deductionsGroupedByTypeCodeForDeductionsInfo = groupByDeductionTypeCode(deductionsSelectedForDeductionsInfo)

                def deductionsSelectedForDeductionsSum = selectDeductionsForDeductionsSum(ndflPersonDeductions)
                def deductionsSelectedGroupedByDeductionTypeCode = groupByDeductionTypeCode(deductionsSelectedForDeductionsSum)

                def ndflPersonPrepayments = findPrepayments(np.id, startDate, endDate, ndflPersonIncomes)
                ndflPersonIncomesGroupedByTaxRate.each { taxRateGroup ->
                    СведДох(Ставка: taxRateGroup.head()?.taxRate) {

                        taxRateGroup.each { ndflPersonIncome ->
                            ДохВыч() {
                                ndflPersonIncomesGroupedByIncomeCode.each { incomeCodeGroup ->
                                    def sortedIncomeCodeGroup = incomeCodeGroup.toSorted { item1, item2 -> item1.taxDate <=> item2.taxDate }
                                    def incomeCode = sortedIncomeCodeGroup.get(0)?.incomeCode
                                    incomeCodeGroup.eachWithIndex { incomeCodeNdflPersonIncome, index ->
                                        if (isForSpecificReport) {
                                            СвСумДох(Месяц: incomeCodeNdflPersonIncome.taxDate?.format(DATE_FORMAT_MONTH),
                                                    КодДоход: incomeCode,
                                                    СумДоход: ScriptUtils.round(getSumDohod(incomeCodeGroup), 2),
                                                    Страница: index < incomeCodeGroup.size() / 2 ? 1 : 2
                                            ) {

                                                deductionsGroupedByTypeCodeForDeductionsInfo.each { deductionByTypeCodeGroup ->
                                                    def deductionsFilteredByIncomeCode = filterDeductionsByIncomeCode(sortedIncomeCodeGroup, deductionByTypeCodeGroup)

                                                    СвСумВыч(КодВычет: deductionsFilteredByIncomeCode.get(0)?.typeCode,
                                                            СумВычет: ScriptUtils.round(deductionsFilteredByIncomeCode.sum().periodCurrSumm, 2)) {
                                                    }
                                                }
                                            }
                                        } else {
                                            СвСумДох(Месяц: incomeCodeNdflPersonIncome.taxDate?.format(DATE_FORMAT_MONTH),
                                                    КодДоход: incomeCode,
                                                    СумДоход: ScriptUtils.round(getSumDohod(incomeCodeGroup), 2)
                                            ) {

                                                deductionsGroupedByTypeCodeForDeductionsInfo.each { deductionByTypeCodeGroup ->
                                                    def deductionsFilteredByIncomeCode = filterDeductionsByIncomeCode(sortedIncomeCodeGroup, deductionByTypeCodeGroup)

                                                    СвСумВыч(КодВычет: deductionsFilteredByIncomeCode.get(0)?.typeCode,
                                                            СумВычет: ScriptUtils.round(deductionsFilteredByIncomeCode.sum().periodCurrSumm, 2)) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (taxRateGroup.head()?.taxRate == 13) {
                            НалВычССИ() {
                                if (isForSpecificReport) {
                                    int countInLine = 4
                                    int linesCount
                                    if (deductionsSelectedGroupedByDeductionTypeCode.size() % countInLine == 0) {
                                        linesCount = deductionsSelectedGroupedByDeductionTypeCode.size() / countInLine
                                    } else {
                                        linesCount = deductionsSelectedGroupedByDeductionTypeCode.size() / countInLine + 1
                                    }
                                    for (int line = 0; line < linesCount; line++) {
                                        Строка() {
                                            deductionsSelectedGroupedByDeductionTypeCode.eachWithIndex { group, index ->
                                                lowestIndex = countInLine * line
                                                if (index >= lowestIndex && index < lowestIndex + countInLine) {
                                                    ПредВычССИ(КодВычет: group.head().typeCode,
                                                            СумВычет: group.sum().periodCurrSumm) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    deductionsSelectedGroupedByDeductionTypeCode.each { group ->
                                        ПредВычССИ(КодВычет: group.head().typeCode,
                                                СумВычет: group.sum().periodCurrSumm) {
                                        }
                                    }
                                }
                                deductionsSelectedGroupedByDeductionTypeCode.find { group ->
                                    getDeductionMark(getDeductionType(group.head()?.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_SOTSIALNIY)
                                }.each {
                                    УведСоцВыч(НомерУвед: it.notifNum,
                                            ДатаУвед: it.notifDate?.format(DATE_FORMAT_DOTTED),
                                            ИФНСУвед: it.notifSource)
                                }
                                deductionsSelectedGroupedByDeductionTypeCode.find { group ->
                                    getDeductionMark(getDeductionType(group.head()?.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY)
                                }.each {
                                    УведИмущВыч(НомерУвед: it.notifNum,
                                            ДатаУвед: it.notifDate.format(DATE_FORMAT_DOTTED),
                                            ИФНСУвед: it.notifSource)
                                }
                            }
                        }
                        СумИтНалПер(СумДохОбщ: ScriptUtils.round(getSumDohObsh(ndflPersonIncomes), 2),
                                НалБаза: ScriptUtils.round(getNalBaza(ndflPersonIncomes), 2),
                                НалИсчисл: getNalIschisl(ndflPersonIncomes),
                                АвансПлатФикс: getAvansPlatFix(ndflPersonPrepayments),
                                НалУдерж: getNalUderzh(priznakF, ndflPersonIncomes),
                                НалПеречисл: getNalPerechisl(priznakF, ndflPersonIncomes),
                                НалУдержЛиш: getNalUderzhLish(priznakF, ndflPersonIncomes),
                                НалНеУдерж: getNalNeUderzh(ndflPersonIncomes)) {

                            if (np.status == "6") {
                                ndflPersonPrepayments.each { prepayment ->
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

            ndflReferencess << createRefBookAttributesForNdflReference(np.personId, nomSpr, np.lastName, np.firstName, np.middleName, np.birthDay)

        }
    }
    saveNdflRefences()
//    println(writer)
}

def saveNdflRefences() {
    logger.setTaUserInfo(userInfo)
    getProvider(NDFL_REFERENCES).createRecordVersion(logger, new Date(), null, ndflReferencess)
}

def createRefBookAttributesForNdflReference(
        def personId, def nomSpr, def lastName, def firstName, def middleName, def birthDay) {
    Map<String, RefBookValue> row = new HashMap<String, RefBookValue>();
    row.put(NDFL_REFERENCES_DECLARATION_DATA_ID, new RefBookValue(RefBookAttributeType.NUMBER, declarationData.id))
    row.put(NDFL_REFERENCES_PERSON_ID, new RefBookValue(RefBookAttributeType.REFERENCE, personId))
    row.put(NDFL_REFERENCES_NUM, new RefBookValue(RefBookAttributeType.NUMBER, nomSpr))
    row.put(NDFL_REFERENCES_SURNAME, new RefBookValue(RefBookAttributeType.STRING, lastName))
    row.put(NDFL_REFERENCES_NAME, new RefBookValue(RefBookAttributeType.STRING, firstName))
    row.put(NDFL_REFERENCES_LASTNAME, new RefBookValue(RefBookAttributeType.STRING, middleName))
    row.put(NDFL_REFERENCES_BIRTHDAY, new RefBookValue(RefBookAttributeType.DATE, birthDay))
    row.put(NDFL_REFERENCES_ERRTEXT, new RefBookValue(RefBookAttributeType.STRING, null))
    RefBookRecord record = new RefBookRecord()
    record.setValues(row)
    return record
}

// Генерация имени файла
def generateXmlFileId() {
    def departmentParamRow = departmentParam ? getDepartmentParamTable(departmentParam?.record_id?.value, declarationData.reportPeriodId) : null
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
def getNdflPersons() {
    ndflPersonService.findNdflPersonByParameters(declarationData.id, null, pageNumber * 3000 - 2999, NUMBER_OF_PERSONS)
}

// Получить доходы
def findIncomes(def ndflPersonId, def priznakF, def startDate, def endDate) {
    def selectedIncomeRows = ndflPersonService.findIncomesByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate)
    if (priznakF == "1") {
        selectedIncomeRows.removeAll { it.incomeAccruedSumm == 0 }
    } else if (priznakF == "2") {
        selectedIncomeRows.removeAll { it.notHoldingTax < 0 }
    }
    return selectedIncomeRows
}

// Получить вычеты
def findDeductions(def ndflPersonId, def startDate, def endDate, def ndflPersonIncomes) {
    def toReturn = []
    def selectedDeductionRows = ndflPersonService.findDeductionsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate)

    for (d in selectedDeductionRows) {
        for (i in ndflPersonIncomes) {
            if (d.operationId == i.operationId) toReturn << d
        }
    }
    return toReturn
}

// отфильтровать вычеты код дохода которых не совпадает
def filterDeductionsByIncomeCode(def ndflPersonIncomes, def ndflPersonDeductions) {
    def toReturn = []
    for (d in ndflPersonDeductions) {
        for (i in ndflPersonIncomes) {
            if (d.incomeCode == i.incomeCode) toReturn << d
        }
    }
    return toReturn
}

// Получить авансы
def findPrepayments(def ndflPersonId, def startDate, def endDate, def ndflPersonIncomes) {
    def toReturn = []
    def selectedPrepayments = ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate)
    for (p in selectedPrepayments) {
        for (i in ndflPersonIncomes) {
            if (p.operationId == i.operationId) toReturn << p
        }
    }
    return toReturn
}

// Фильтрация налоговых вычетов для сведений о суммах предоставленных налоговых вычетов
def selectDeductionsForDeductionsSum(def ndflPersonDeductions) {
    ndflPersonDeductions.findAll { deduction ->
        getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_SOTSIALNIY) ||
                getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_STANDARTNIY) ||
                getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY) ||
                getDeductionMark(getDeductionType(deduction.typeCode)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_INVESTITSIONNIY)
    }
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

// Вычислить сумму для СумДоход
def getSumDohod(def rows) {
    rows.incomeAccruedSumm.sum()
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
def getAvansPlatFix(def prepayments) {
    prepayments.sum { it.summ }
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
def getDepartmentParam(def departmentId, def reportPeriodId) {
    if (departmentParam == null) {
        def departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate(reportPeriodId) - 1, null, "DEPARTMENT_ID = $departmentId", null)

        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

// Получить детали подразделения из справочника
def getDepartmentParamTable(def departmentParamId, def reportPeriodId) {
    if (departmentParamRow == null) {
        def filter = null
        if (declarationData.oktmo != null) {
            filter = "REF_BOOK_NDFL_ID = $departmentParamId and KPP ='${declarationData.kpp}' and OKTMO = ${declarationData.oktmo}"
        } else {
            filter = "REF_BOOK_NDFL_ID = $departmentParamId and KPP ='${declarationData.kpp}'"
        }

        def departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(reportPeriodId) - 1, null, filter, null)
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


def getReportPeriodEndDate(def reportPeriodId) {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
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
        formType = getProvider(REB_BOOK_FORM_TYPE_ID).getRecordData(id)
    }
    return formType;
}

//Определить признакФ
def definePriznakF() {
    def code = formType?.CODE?.value
    switch (code) {
        case (NDFL_2_S_PRIZNAKOM_1): return "1"
        case (NDFL_2_S_PRIZNAKOM_2): return "2"

        default: return "0"
    }
}

// Получить вид вычета
def getDeductionType(def code) {
    def toReturn = deductionTypes["$code"]
    if (toReturn == null) {
        def filter = "CODE = '$code'"
        def deductionTypeList = getProvider(REF_BOOK_DEDUCTION_TYPE_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId) - 1, null, filter, null)
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
        def deductionMarkList = getProvider(REF_BOOK_DEDUCTION_MARK_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId) - 1, null, filter, null)
        if (deductionMarkList == null || deductionMarkList.size() == 0 || deductionMarkList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        deductionMarks["$code"] = deductionMarkList.get(0).NAME?.value
        toReturn = deductionMarks["$code"]
    }
    return toReturn
}

/************************************* СОЗДАНИЕ ФОРМЫ *****************************************************************/

@Field
final int RNU_NDFL_DECLARATION_TYPE = 101

@Field
def departmentParamTableList = null;



def createForm() {
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def korrPeriod = isCorrectionPeriod()
    def pairKppOktmoList = []

    def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    def declarationTypeId = currDeclarationTemplate.type.id
    def ndflReferencesWithError = null
    if (korrPeriod) {
        def prevDepartmentPeriodReport = departmentReportPeriodService.getPrevLast(declarationData.departmentId, departmentReportPeriod.reportPeriod.id)
        def declarations = declarationService.find(declarationTypeId, prevDepartmentPeriodReport.id)
        def declarationsForRemove = []
        declarations.each { declaration ->
            def stateDocReject = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Отклонен'", null).get(0).id
            def stateDocNeedClarify = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Требует уточнения'", null).get(0).id
            def stateDocError = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Ошибка'", null).get(0).id
            def declarationTemplate = declarationService.getTemplate(declaration.declarationTemplateId)
            if (declarationTemplate.declarationFormKind != DeclarationFormKind.REPORTS || (declaration.docState != stateDocReject
            || declaration.docState != stateDocNeedClarify || declaration.docState != stateDocError)) {
                declarationsForRemove << declaration
            }
        }
        declarations.removeAll(declarationsForRemove)
        declarations.each { declaration ->
            pairKppOktmoList << new PairKppOktmo(Integer.valueOf(declaration.kpp), declaration.oktmo)
        }
        formType = getFormType(currDeclarationTemplate)
        if (definePriznakF() != "0") {
            ndflReferencesWithError = getNdflReferencesWithError()

        }
    } else {

        departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id)
        departmentParamTableList = getDepartmentParamTableList(departmentParam?.id, departmentReportPeriod.reportPeriod.id)
        departmentParamTableList.each { dep ->
            pairKppOktmoList << new PairKppOktmo(dep.KPP?.value, dep.OKTMO?.value, dep?.TAX_ORGAN_CODE?.value)
        }
    }

    def allDeclarationData = findAllTerBankDeclarationData(departmentReportPeriod)

    // Список физлиц для каждой пары КПП и ОКТМО
    def ndflPersonsGroupedByKppOktmo = [:]

    allDeclarationData.each { declaration ->
        pairKppOktmoList.each { np ->
            def ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(declaration.id, np.kpp.toString(), np.oktmo.toString())
            if (ndflPersons != null && ndflPersons.size != 0) {
                if (isCorrectionPeriod()) {
                    def ndflPersonsPicked = []
                    ndflReferencesWithError.each { reference ->
                        ndflPersons.each { person ->
                            if (reference.PERSON_ID?.value == person.id) {
                                ndflPersonsPicked << person
                            }
                        }
                    }
                    ndflPersons = ndflPersonsPicked
                }
                ndflPersonsGroupedByKppOktmo[np] = ndflPersons
            }
        }
    }

    initNdflPersons(ndflPersonsGroupedByKppOktmo)

    declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
        declarationService.delete(it.id, userInfo)
    }

    ndflPersonsGroupedByKppOktmo.each { npGroup ->
        def oktmo = npGroup.key.oktmo
        def kpp = npGroup.key.kpp
        def taxOrganCode = npGroup.key.taxOrganCode

        def npGroupValue = npGroup.value.collate(NUMBER_OF_PERSONS)
        def partTotal = npGroupValue.size()
        npGroupValue.eachWithIndex { part, index ->
            Map<String, Object> params
            Long ddId
            def indexFrom1 = ++index
            def note = "Часть ${indexFrom1} из ${partTotal}"
            params = new HashMap<String, Object>()
            ddId = declarationService.create(logger, declarationData.declarationTemplateId, userInfo,
                    departmentReportPeriodService.get(declarationData.departmentReportPeriodId), taxOrganCode, kpp.toString(), oktmo, null, null, note)
            appendNdflPersonsToForm(ddId, part)
            params.put(PART_NUMBER, indexFrom1)
            params.put(PART_TOTAL, partTotal)
            formMap.put(ddId, params)
        }
    }
}

def findAllTerBankDeclarationData(def departmentReportPeriod) {
    // получить id всех ТБ для данного отчетного периода
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.reportPeriod.id)
    def allDeclarationData = []
    allDepartmentReportPeriodIds.each {
        allDeclarationData.addAll(declarationService.find(RNU_NDFL_DECLARATION_TYPE, it))
    }
    // удаление форм не со статусом принята
    def declarationsForRemove = []
    allDeclarationData.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }
    allDeclarationData.removeAll(declarationsForRemove)
    return allDeclarationData
}

def initNdflPersons(def ndflPersonsGroupedByKppOktmo) {
    ndflPersonsGroupedByKppOktmo.each { npGroup ->
        def oktmo = npGroup.key.oktmo
        def kpp = npGroup.key.kpp
        npGroup.value.each {
            def incomes = ndflPersonService.findIncomesForPersonByKppOktmo(it.id, kpp, oktmo)
            resetId(incomes)
            def deductions = ndflPersonService.findDeductions(it.id)
            resetId(deductions)
            def prepayments = ndflPersonService.findPrepayments(it.id)
            resetId(prepayments)
            it.setIncomes(incomes)
            it.setDeductions(deductions)
            it.setPrepayments(prepayments)
        }
    }
}

def appendNdflPersonsToForm(def declarationDataId, def ndflPersons) {
    ndflPersons.each {
        it.setId(null)
        it.setDeclarationDataId(declarationDataId)
        ndflPersonService.save(it)
    }

}


def resetId(def list) {
    list.each {
        it.setId(null)
    }
}

/************************************* ВЫГРУЗКА ***********************************************************************/

def createReports() {
    ZipArchiveOutputStream zos = new ZipArchiveOutputStream(outputStream);
    scriptParams.put("fileName", "reports.zip")
    try {
        Department department = departmentService.get(declarationData.departmentId);
        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId);
        String strCorrPeriod = "";
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.get().format(departmentReportPeriod.getCorrectionDate());
        }
        String path = String.format("Отчетность %s, %s, %s, %s%s",
                declarationTemplate.getName(),
                department.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(), departmentReportPeriod.getReportPeriod().getName(), strCorrPeriod);
        def declarationTypeId = declarationService.getTemplate(declarationData.declarationTemplateId).type.id
        declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
            if (it.fileName == null) {
                return
            }
            ZipArchiveEntry ze = new ZipArchiveEntry(path + "/" + it.taxOrganCode + "/" + it.fileName);
            zos.putArchiveEntry(ze);
            IOUtils.copy(declarationService.getXmlStream(it.id), zos)
            zos.closeArchiveEntry();
        }
    } finally {
        IOUtils.closeQuietly(zos);
    }
}
/*********************************ПОЛУЧИТЬ ИСТОЧНИКИ*******************************************************************/
@Field
def sourceReportPeriod = null

@Field
def departmentReportPeriodMap = [:]

// Мапа для хранения полного названия подразделения (id подразделения  -> полное название)
@Field
def departmentFullNameMap = [:]


def getReportPeriod() {
    if (sourceReportPeriod == null) {
        sourceReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return sourceReportPeriod
}

/** Получить результат для события FormDataEvent.GET_SOURCES. */
void getSources() {
    if (!(needSources)) {
        // формы-приемники, декларации-истчоники, декларации-приемники не переопределять
        return
    }
    def reportPeriod = getReportPeriod()
    def sourceTypeId = 101
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.reportPeriod.id)
    def tmpDeclarationDataList = []
    allDepartmentReportPeriodIds.each {
        tmpDepartmentReportPeriod = departmentReportPeriodService.get(it)
        tmpDeclaration = declarationService.findDeclarationDataByKppOktmoOfNdflPersonIncomes(sourceTypeId, it, tmpDepartmentReportPeriod.departmentId, tmpDepartmentReportPeriod.reportPeriod.id, declarationData.kpp, declarationData.oktmo)
        if (tmpDeclaration != null) {
            tmpDeclarationDataList << tmpDeclaration
        }
   }
    def declarationsForRemove = []
    tmpDeclarationDataList.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }
    tmpDeclarationDataList.removeAll(declarationsForRemove)
    tmpDeclarationDataList.each { tmpDeclarationData ->
        def department = departmentService.get(tmpDeclarationData.departmentId)
        def relation = getRelation(tmpDeclarationData, department, reportPeriod, sourceTypeId)
        if (relation) {
            sources.sourceList.add(relation)
        }
    }
    sources.sourcesProcessedByScript = true
}

/**
 * Получить запись для источника-приемника.
 *
 * @param tmpDeclarationData нф
 * @param department подразделение
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(DeclarationData tmpDeclarationData, Department department, ReportPeriod period, def sourceTypeId) {
    // boolean excludeIfNotExist - исключить несозданные источники

    if (excludeIfNotExist && tmpDeclarationData == null) {
        return null
    }
    // WorkflowState stateRestriction - ограничение по состоянию для созданных экземпляров
    if (stateRestriction && tmpDeclarationData != null && stateRestriction != tmpDeclarationData.state) {
        return null
    }
    Relation relation = new Relation()
    def isSource = sourceTypeId != 101

    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(tmpDeclarationData?.departmentReportPeriodId) as DepartmentReportPeriod
    DeclarationTemplate declarationTemplate = declarationService.getTemplate(sourceTypeId)

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        /** Идентификатор подразделения */
        relation.departmentId = department.id
        /** полное название подразделения */
        relation.fullDepartmentName = getDepartmentFullName(department.id)
        /** Дата корректировки */
        relation.correctionDate = departmentReportPeriod?.correctionDate
        /** Вид нф */
        relation.declarationTypeName = declarationTemplate?.name
        /** Год налогового периода */
        relation.year = period.taxPeriod.year
        /** Название периода */
        relation.periodName = period.name
    }
    /**************  Общие параметры ***************/
    /** подразделение */
    relation.department = department
    /** Период */
    relation.departmentReportPeriod = departmentReportPeriod
    /** Статус ЖЦ */
    relation.declarationState = tmpDeclarationData?.state
    /** форма/декларация создана/не создана */
    relation.created = (tmpDeclarationData != null)
    /** является ли форма источников, в противном случае приемник*/
    relation.source = isSource
    /** Введена/выведена в/из действие(-ия) */
    relation.status = declarationTemplate.status == VersionedObjectStatus.NORMAL
    /** Налог */
    relation.taxType = TaxType.NDFL
    /**************  Параметры НФ ***************/
    /** Идентификатор созданной формы */
    relation.declarationDataId = tmpDeclarationData?.id
    /** Вид НФ */
    relation.declarationTemplate = declarationTemplate
    /** Тип НФ */
    //relation.formDataKind = tmpDeclarationData.kind

    return relation
}



def getDepartmentReportPeriodById(def id) {
    if (id != null && departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}


/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
}
/************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

// Получить список детали подразделения из справочника для некорректировочного периода
def getDepartmentParamTableList(def departmentParamId, def reportPeriodId) {
    if (departmentParamTableList == null) {
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
        departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(reportPeriodId) - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
    }
    return departmentParamTableList
}

// Получить список из реестра справок с ошибкой ФНС
def getNdflReferencesWithError() {
    def filter = "DECLARATION_DATA_ID = ${declarationData.id} AND ERRTEXT IS NOT NULL"
    getProvider(NDFL_REFERENCES).getRecords(getReportPeriodEndDate(reportPeriodId) - 1, null, filter, null)
}

def isCorrectionPeriod() {
    def nomKorr = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    if (nomKorr != 0) {
        return true
    }
}

class PairKppOktmo {
    def kpp
    def oktmo
    def taxOrganCode

    PairKppOktmo(def kpp, def oktmo, def taxOrganCode) {
        this.kpp = kpp
        this.oktmo = oktmo
        this.taxOrganCode = taxOrganCode
    }
}

def createSpecificReport() {
    def params = scriptSpecificReportHolder.subreportParamValues ?: new HashMap<String, Object>()

    def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
        buildXmlForSpecificReport(it)
    });

    declarationService.exportPDF(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".pdf")
}