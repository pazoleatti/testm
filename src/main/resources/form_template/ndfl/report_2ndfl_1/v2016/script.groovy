package form_template.ndfl.report_2ndfl_1.v2016

import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel
import com.aplana.sbrf.taxaccounting.model.StringColumn
import groovy.transform.CompileStatic
import groovy.xml.XmlUtil
import org.apache.commons.lang3.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import groovy.transform.Field
import groovy.transform.TypeChecked
import groovy.xml.MarkupBuilder
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificDeclarationDataReportHolder
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.PagingParams
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter
import com.aplana.sbrf.taxaccounting.model.util.Pair
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import java.util.zip.ZipInputStream

switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверки
        println "!CHECK!"
        check()
        break
    case FormDataEvent.CALCULATE: //формирование xml
        println "!CALCULATE!"
        buildXml(xml)
        // Формирование pdf-отчета формы
        //        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
    case FormDataEvent.COMPOSE: // Консолидирование
        println "!COMPOSE!"
        break
    case FormDataEvent.GET_SOURCES: //формирование списка источников
        println "!GET_SOURCES!"
        getSources()
        break
    case FormDataEvent.PREPARE_SPECIFIC_REPORT:
        // Подготовка для последующего формирования спецотчета
        prepareSpecificReport()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
        println "!CREATE_SPECIFIC_REPORT!"
        createSpecificReport()
        break
    case FormDataEvent.CREATE_EXCEL_REPORT: //создание xlsx отчета
        createXlsxReport()
        break
    case FormDataEvent.CREATE_FORMS: // создание экземпляра
        println "!CREATE_FORMS!"
        checkDataConsolidated()
        createForm()
        break
    case FormDataEvent.PRE_CREATE_REPORTS:
        preCreateReports()
        break
    case FormDataEvent.CREATE_REPORTS:
        println "!CREATE_REPORTS!"
        createReports()
        break
}

@Field
final ReportPeriodService reportPeriodService = getProperty("reportPeriodService")
@Field
final DepartmentService departmentService = getProperty("departmentService")
@Field
final DeclarationService declarationService = getProperty("declarationService")
@Field
final DepartmentReportPeriodService departmentReportPeriodService = getProperty("departmentReportPeriodService")
@Field
final DeclarationData declarationData = getProperty("declarationData")
@Field
final Logger logger = getProperty("logger")

def getProperty(String name) {
    try {
        return super.getProperty(name)
    } catch (MissingPropertyException e) {
        return null
    }
}

/************************************* ДАННЫЕ ДЛЯ ОБЩИХ СОБЫТИЙ *******************************************************/

/************************************* СОЗДАНИЕ XML *****************************************************************/

// Количество физических лиц в одном xml-файле
@Field
final int NUMBER_OF_PERSONS = 3000

// Кэш провайдеров
@Field
def providerCache = [:]

// запись подразделения в справочнике
@Field
def departmentParam = null

// Кэш подразделений из справочника
@Field
def departmentCache = [:]

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
def formType = null

@Field
final String DATE_FORMAT_FLATTEN = "yyyyMMdd"

@Field
final String DATE_FORMAT_DOTTED = "dd.MM.yyyy"

@Field
final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"

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
final int REPORT_PERIOD_TYPE_ID = 8

@Field
int REF_BOOK_OKTMO_ID = 96;

@Field
int REF_BOOK_ASNU_ID = 900

@Field
int REF_BOOK_SIGNATORY_MARK_ID = 35

@Field
int NDFL_2_1_DECLARATION_TYPE = 102

@Field
int NDFL_2_2_DECLARATION_TYPE = 104

@Field final int DECLARATION_TYPE_NDFL6_ID = 103

@Field
final String NDFL_2_S_PRIZNAKOM_1 = "2 НДФЛ (1)"

@Field
final String NDFL_2_S_PRIZNAKOM_2 = "2 НДФЛ (2)"

@Field
final String VERS_FORM = "5.04"

@Field
final String KND = "1151078"

@Field
final String PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY = "Имущественный"

@Field
final String PRIZNAK_KODA_VICHETA_SOTSIALNIY = "Социальный"

@Field
final String PART_NUMBER = "partNumber"

@Field
final String PART_TOTAL = "partTotal"

@Field
final String NDFL_PERSON_KNF_ID = "ndflPersonKnfId"

@Field
def ndflReferencess = []

@Field
final NDFL_PERSON_ID = "NDFL_PERSON_ID"

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

@Field
final OKTMO_CACHE = [:]

@Field
int pairKppOktmoSize = 0
@Field
String reportType = declarationData.declarationTemplateId == NDFL_2_1_DECLARATION_TYPE ? "2-НДФЛ (1)" : "2-НДФЛ (2)"

def buildXml(def writer) {
    buildXml(writer, false, null, null)
}

def buildXmlForSpecificReport(def writer, Long xmlPartNumber, Long pNumSpravka) {
    buildXml(writer, true, xmlPartNumber, pNumSpravka)
}

/**
 * Сформировать xml
 * @param writer
 * @param isForSpecificReport
 * @return
 */
def buildXml(def writer, boolean isForSpecificReport, Long xmlPartNumber, Long pNumSpravka) {
    boolean presentNotHoldingTax = false
    def refPersonIds = []
    ScriptUtils.checkInterrupted();
    ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
    // Получим ИНН из справочника "Общие параметры"
    def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)
    // Получим код НО пром из справочника "Общие параметры"
    def kodNoProm = configurationParamModel?.get(ConfigurationParam.NO_CODE)?.get(0)?.get(0)

    //Текущая страница представляющая порядковый номер файла
    def currentPageNumber = xmlPartNumber ?: partNumber

    // инициализация данных о подразделении
    departmentParam = getDepartmentParam(declarationData.departmentId, declarationData.reportPeriodId, true)
    departmentParamRow = getDepartmentParamDetails(declarationData.kpp, declarationData.oktmo)
    String depName = departmentService.get(departmentParam.DEPARTMENT_ID.value.toInteger()).name
    // Имя файла
    def fileName = generateXmlFileId(sberbankInnParam, kodNoProm)
    // Отчетный период
    reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    // Макет НФ
    def declarationTemplate = declarationService.getTemplate(declarationData.getDeclarationTemplateId())

    // Вид НФ
    formType = getFormType(declarationTemplate.declarationFormTypeId)

    // Данные для Файл.СвРекв
    def oktmo = getOktmoById(departmentParamRow?.OKTMO?.value)?.CODE?.value
    // Данные для Файл.СвРекв.СвЮЛ
    def kpp = departmentParamRow?.KPP?.value
    def otchetGod = reportPeriod.taxPeriod.year
    def priznakF = definePriznakF()
    def startDate = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()
    def endDate = reportPeriodService.getEndDate(declarationData.reportPeriodId).getTime()

    // Данные для Файл.Документ.Подписант
    def prPodp = getProvider(REF_BOOK_SIGNATORY_MARK_ID).getRecordData(departmentParamRow?.SIGNATORY_ID?.value).CODE.value
    def signatoryFirstname = departmentParamRow?.SIGNATORY_FIRSTNAME?.value
    def signatorySurname = departmentParamRow?.SIGNATORY_SURNAME?.value
    def signatoryLastname = departmentParamRow?.SIGNATORY_LASTNAME?.value
    def naimDoc = departmentParamRow?.APPROVE_DOC_NAME?.value
    def naimOrgApprove = departmentParamRow?.APPROVE_ORG_NAME?.value

    // Данные для Файл.Документ.СвНА-(Данные о налоговом агенте)
    def tlf = departmentParamRow?.PHONE?.value
    def naimOrg = departmentParamRow?.NAME?.value

    // 	Данные для Файл.Документ.ПолучДох-(Данные о физическом лице - получателе дохода)
    def ndflPersonsList
    if (pNumSpravka == null) {
        ndflPersonsList = getNdflPersons()
    } else {
        def ndflPersonId = getProvider(NDFL_REFERENCES).getRecords(null, null, "DECLARATION_DATA_ID = ${declarationData.id} and NUM = ${pNumSpravka}", null).get(0).NDFL_PERSON_ID.value
        if (ndflPersonId != null) {
            ndflPersonsList = getNdflPersons(ndflPersonId.longValue())
        } else {
            ndflPersonsList = []
        }
    }
    if (!checkMandatoryFields(ndflPersonsList)) {
        return
    }
    // Порядковый номер физического лица
    def nomSpr = (currentPageNumber - 1) * NUMBER_OF_PERSONS
    def nomSprCorr = 0

    // Текущая дата
    def currDate = Calendar.getInstance().getTime()
    def dateDoc = currDate.format(DATE_FORMAT_DOTTED, TimeZone.getTimeZone('Europe/Moscow'))

    // Номер корректировки
    def nomKorr = findCorrectionNumber()
    def kodNo = departmentParamRow?.TAX_ORGAN_CODE?.value
    def builder = new MarkupBuilder(writer)
    builder.setDoubleQuotes(true)
    builder.setOmitNullAttributes(true)
    builder.Файл(ИдФайл: fileName,
            ВерсПрог: applicationVersion,
            ВерсФорм: VERS_FORM) {
        СвРекв(ОКТМО: oktmo,
                ОтчетГод: otchetGod,
                ПризнакФ: priznakF) {
            СвЮЛ(ИННЮЛ: sberbankInnParam,
                    КПП: kpp) {}
        }
        for (NdflPerson np : ndflPersonsList) {
            ScriptUtils.checkInterrupted();

            boolean includeNdflPersonToReport = false
            // Данные для Файл.Документ.СведДох-(Сведения о доходах физического лица)
            def ndflPersonIncomesAll = findAllIncomes(np.id, startDate, endDate, priznakF)

            if (priznakF == "1") {
                includeNdflPersonToReport = true
            } else {
                if (!ndflPersonIncomesAll.isEmpty()) {
                    includeNdflPersonToReport = true
                    presentNotHoldingTax = true
                }
            }
            if (includeNdflPersonToReport) {
                // Порядковый номер физического лица
                if (pNumSpravka == null) {
                    if (nomKorr != 0) {
                        if (priznakF == "1") {
                            def uncorrectPeriodDrp = departmentReportPeriodService.getFirst(declarationData.departmentId, declarationData.reportPeriodId)
                            def uncorretctedPeriodDd = declarationService.find(NDFL_2_1_DECLARATION_TYPE, uncorrectPeriodDrp.id, declarationData.kpp, declarationData.oktmo, null, null, null)
                            nomSpr = getProvider(NDFL_REFERENCES).getRecords(new Date(), null, "PERSON_ID = ${np.personId} AND DECLARATION_DATA_ID = ${uncorretctedPeriodDd.id}", null).get(0).NUM.value
                        } else {
                            def declarations = declarationService.findAllDeclarationData(NDFL_2_2_DECLARATION_TYPE, declarationData.departmentId, declarationData.reportPeriodId)
                            declarations = declarations.findAll {
                                it.kpp == declarationData.kpp && it.oktmo == declarationData.oktmo
                            }
                            if (!declarations.isEmpty()) {
                                boolean first = true
                                String filter = "("
                                for (d in declarations) {
                                    if (!first) {
                                        filter += "OR "
                                    } else {
                                        first = false
                                    }
                                    filter += "DECLARATION_DATA_ID = ${d.id} "
                                }
                                filter += ")"
                                def references = getProvider(NDFL_REFERENCES).getRecords(new Date(), null, "PERSON_ID = ${np.personId} AND " + filter, null)
                                if (!references.isEmpty()) {
                                    nomSpr = references.get(0).NUM.value
                                } else {
                                    references = getProvider(NDFL_REFERENCES).getRecords(new Date(), new PagingParams(0, 1), filter, refBookFactory.get(NDFL_REFERENCES).getAttribute("NUM"), false)
                                    if (!references.isEmpty()) {
                                        nomSpr = references.get(0).NUM.value + 1
                                    } else {
                                        nomSpr = (++nomSprCorr)
                                    }
                                }
                            } else {
                                nomSpr = (++nomSprCorr)
                            }
                        }
                    } else {
                        nomSpr++
                    }
                } else {
                    nomSpr = pNumSpravka;
                }
                Документ(КНД: KND,
                        ДатаДок: dateDoc,
                        НомСпр: nomSpr,
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
                                ИННЮЛ: sberbankInnParam,
                                КПП: kpp)
                    }
                    ПолучДох(ИННФЛ: np.innNp,
                            ИННИно: np.innForeign,
                            Статус: np.status,
                            ДатаРожд: np.birthDay?.format(DATE_FORMAT_DOTTED),
                            Гражд: np.citizenship) {
                        ФИО(Фамилия: np.lastName,
                                Имя: np.firstName,
                                Отчество: np.middleName)
                        УдЛичнФЛ(КодУдЛичн: np.idDocType,
                                СерНомДок: np.idDocNumber)
                        if (np.postIndex != null || np.regionCode != null || np.area != null || np.city != null ||
                                np.locality != null || np.street != null || np.house != null || np.building != null ||
                                np.flat != null) {
                            АдрМЖРФ(Индекс: np.postIndex,
                                    КодРегион: np.regionCode,
                                    Район: np.area,
                                    Город: np.city,
                                    НаселПункт: np.locality,
                                    Улица: np.street,
                                    Дом: np.house,
                                    Корпус: np.building,
                                    Кварт: np.flat)
                        }
                        if (np.countryCode != null) {
                            АдрИНО(КодСтр: np.countryCode,
                                    АдрТекст: np.address)
                        }
                    }

                    // Сведения о доходах сгруппированные по ставке
                    def ndflPersonIncomesGroupedByTaxRate = groupByTaxRate(ndflPersonIncomesAll)

                    // Сведения о вычетах с признаком "Остальные"
                    def deductionsSelectedForDeductionsInfo = ndflPersonService.findDeductionsWithDeductionsMarkOstalnie(np.id, startDate, endDate)
                    // Сведения о вычетах с признаком "Социльный;Стандартный;Имущественный;Инвестиционный"
                    def deductionsSelectedForDeductionsSum = ndflPersonService.findDeductionsWithDeductionsMarkNotOstalnie(np.id, startDate, endDate, (priznakF == "1"))
                    def deductionsSelectedGroupedByDeductionTypeCode = groupByDeductionTypeCode(deductionsSelectedForDeductionsSum)
                    // Объединенные строки сведений об уведомлении, подтверждающие право на вычет
                    def unionDeductions = unionDeductionsForDeductionType(deductionsSelectedGroupedByDeductionTypeCode)

                    ndflPersonIncomesGroupedByTaxRate.keySet().each { taxRateKey ->
                        ScriptUtils.checkInterrupted();
                        def ndflPersonPrepayments = findPrepayments(np.id, taxRateKey, startDate, endDate, priznakF)
                        СведДох(Ставка: taxRateKey) {

                            def sumDohodAll = new BigDecimal(0)
                            def sumVichAll = new BigDecimal(0)

                            def ndflpersonIncomesForTaxRate = ndflPersonIncomesGroupedByTaxRate.get(taxRateKey)
                            // Сведения о доходах сгруппированные по коду дохода
                            def ndflPersonIncomesGroupedByMonthAndIncomeCode = groupIncomesByMonth(ndflpersonIncomesForTaxRate)
                            ДохВыч() {
                                int index = 1, countIncome = 0
                                if (isForSpecificReport) {
                                    ndflPersonIncomesGroupedByMonthAndIncomeCode.keySet().each { monthKey ->
                                        ScriptUtils.checkInterrupted();
                                        def ndflPersonIncomesGroupedByIncomeCode = ndflPersonIncomesGroupedByMonthAndIncomeCode.get(monthKey)
                                        ndflPersonIncomesGroupedByIncomeCode.keySet().eachWithIndex { incomeKey, int i ->
                                            ScriptUtils.checkInterrupted();
                                            def ndflPersonIncomesFromGroup = ndflPersonIncomesGroupedByIncomeCode.get(incomeKey)
                                            def ndflPersonIncomesWhereIncomeAccruedSumGreaterZero = []
                                            ndflPersonIncomesFromGroup.each {
                                                if (it.incomeAccruedSumm != null && it.incomeAccruedSumm > new BigDecimal(0)) {
                                                    ndflPersonIncomesWhereIncomeAccruedSumGreaterZero << it
                                                }
                                            }
                                            countIncome += ndflPersonIncomesWhereIncomeAccruedSumGreaterZero.size()
                                        }
                                    }
                                }
                                index = 1
                                ndflPersonIncomesGroupedByMonthAndIncomeCode.keySet().each { monthKey ->
                                    ScriptUtils.checkInterrupted();
                                    def ndflPersonIncomesGroupedByIncomeCode = ndflPersonIncomesGroupedByMonthAndIncomeCode.get(monthKey)
                                    ndflPersonIncomesGroupedByIncomeCode.keySet().eachWithIndex { incomeKey, int i ->
                                        ScriptUtils.checkInterrupted();
                                        def ndflPersonIncomesFromGroup = ndflPersonIncomesGroupedByIncomeCode.get(incomeKey)
                                        def ndflPersonIncomesWhereIncomeAccruedSumGreaterZero = []
                                        ndflPersonIncomesFromGroup.each {
                                            if (it.incomeAccruedSumm != null && it.incomeAccruedSumm > new BigDecimal(0)) {
                                                ndflPersonIncomesWhereIncomeAccruedSumGreaterZero << it
                                            }
                                        }
                                        if (ndflPersonIncomesWhereIncomeAccruedSumGreaterZero.size() > 0) {
                                            /**
                                             * Предрасчет СвСумВыч
                                             */
                                            def svSumVich = getSvSumVich(filterDeductionsByIncomeCode(ndflPersonIncomesWhereIncomeAccruedSumGreaterZero, deductionsSelectedForDeductionsInfo))
                                            def sumDohod = getSumDohod(priznakF, ndflPersonIncomesGroupedByIncomeCode.get(incomeKey), taxRateKey)
                                            if (priznakF == "2") {
                                                def svSumVichNotOstalnie = getSvSumVich(filterDeductions(ndflPersonIncomesWhereIncomeAccruedSumGreaterZero, deductionsSelectedForDeductionsSum))
                                                def svSumVichA = (!svSumVich.isEmpty() ? (svSumVich*.СумВычет.sum() ?: 0) : 0) + (!svSumVichNotOstalnie.isEmpty() ? (svSumVichNotOstalnie*.СумВычет.sum() ?: 0) : 0)
                                                sumDohod = sumDohod / taxRateKey * 100 + svSumVichA
                                            }
                                            sumDohod = ScriptUtils.round(sumDohod, 2)
                                            sumDohodAll += sumDohod

                                            СвСумДох(Месяц: sprintf('%02d', monthKey + 1),
                                                    КодДоход: incomeKey,
                                                    СумДоход: sumDohod,
                                                    Страница: isForSpecificReport ? (index <= ((countIncome + 1) / 2) ? 1 : 2) : null
                                            ) {
                                                if (!svSumVich.isEmpty()) {
                                                    svSumVich.each {
                                                        СвСумВыч(КодВычет: it.КодВычет,
                                                                СумВычет: it.СумВычет) {
                                                        }
                                                        sumVichAll += it.СумВычет
                                                        index++
                                                    }
                                                } else if (isForSpecificReport) {
                                                    СвСумВыч(КодВычет: "",
                                                            СумВычет: "") {}
                                                    index++
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!(deductionsSelectedGroupedByDeductionTypeCode.isEmpty())) {
                                if (taxRateKey == 13) {
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
                                                    deductionsSelectedGroupedByDeductionTypeCode.keySet().eachWithIndex { deductionTypeKey, index ->
                                                        ScriptUtils.checkInterrupted();
                                                        def lowestIndex = countInLine * line
                                                        if (index >= lowestIndex && index < lowestIndex + countInLine) {
                                                            def deductionCurrPeriodSum = ScriptUtils.round(getDeductionCurrPeriodSum(deductionsSelectedGroupedByDeductionTypeCode.get(deductionTypeKey)), 2)
                                                            if (deductionCurrPeriodSum != 0) {
                                                                ПредВычССИ(КодВычет: deductionTypeKey,
                                                                        СумВычет: deductionCurrPeriodSum) {
                                                                }
                                                                sumVichAll += deductionCurrPeriodSum
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            deductionsSelectedGroupedByDeductionTypeCode.keySet().each { deductionTypeKey ->
                                                ScriptUtils.checkInterrupted();
                                                def deductionCurrPeriodSum = ScriptUtils.round(getDeductionCurrPeriodSum(deductionsSelectedGroupedByDeductionTypeCode.get(deductionTypeKey)), 2)
                                                if (deductionCurrPeriodSum != 0) {
                                                    ПредВычССИ(КодВычет: deductionTypeKey,
                                                            СумВычет: deductionCurrPeriodSum) {
                                                    }
                                                    sumVichAll += deductionCurrPeriodSum
                                                }
                                            }
                                        }
                                        unionDeductions.keySet().findAll { deductionTypeKey ->
                                            ScriptUtils.checkInterrupted();
                                            getDeductionMark(getDeductionType(deductionTypeKey)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_SOTSIALNIY)
                                        }.each { selected ->
                                            ScriptUtils.checkInterrupted();
                                            unionDeductions.get(selected).each {
                                                УведСоцВыч(НомерУвед: it.notifNum,
                                                        ДатаУвед: it.notifDate?.format(DATE_FORMAT_DOTTED),
                                                        ИФНСУвед: it.notifSource)
                                            }
                                        }
                                        unionDeductions.keySet().findAll { deductionTypeKey ->
                                            ScriptUtils.checkInterrupted();
                                            getDeductionMark(getDeductionType(deductionTypeKey)).equalsIgnoreCase(PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY)
                                        }.each { selected ->
                                            ScriptUtils.checkInterrupted();
                                            unionDeductions.get(selected).each {
                                                УведИмущВыч(НомерУвед: it.notifNum,
                                                        ДатаУвед: it.notifDate?.format(DATE_FORMAT_DOTTED),
                                                        ИФНСУвед: it.notifSource)
                                            }
                                        }
                                    }
                                }
                            }
                            // Доходы отобранные по датам для поля tax_date(Дата НДФЛ)
                            def incomesByTaxDate = ndflPersonService.findIncomesByPeriodAndNdflPersonIdAndTaxDate(np.id, taxRateKey, startDate, endDate)
                            List<NdflPersonIncome> incomesByPayoutDate = ndflPersonService.findIncomesByPayoutDate(np.id, taxRateKey, startDate, endDate)
                            Date firstDateOfMarchOfNextPeriod = getFirstMarchOfNextPeriod(endDate)
                            СумИтНалПер(СумДохОбщ: priznakF == "1" ? ScriptUtils.round(getSumDohod(priznakF, ndflPersonIncomesAll, taxRateKey), 2) : ScriptUtils.round(sumDohodAll, 2),
                                    НалБаза: priznakF == "1" ? ScriptUtils.round(getNalBaza(ndflPersonIncomesAll, taxRateKey), 2) : ScriptUtils.round(sumDohodAll - sumVichAll, 2),
                                    НалИсчисл: getNalIschisl(priznakF, ndflPersonIncomesAll, taxRateKey),
                                    АвансПлатФикс: getAvansPlatFix(ndflPersonPrepayments),
                                    НалУдерж: getNalUderzh(priznakF, incomesByPayoutDate, startDate, firstDateOfMarchOfNextPeriod),
                                    НалПеречисл: getNalPerechisl(priznakF, incomesByTaxDate, startDate, firstDateOfMarchOfNextPeriod),
                                    НалУдержЛиш: getNalUderzhLish(priznakF, incomesByTaxDate, startDate, firstDateOfMarchOfNextPeriod),
                                    НалНеУдерж: getNalNeUderzh(priznakF, incomesByTaxDate, startDate, firstDateOfMarchOfNextPeriod)) {

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

                    if (isForSpecificReport && ndflPersonIncomesGroupedByTaxRate.keySet().isEmpty()) {
                        СведДох() {
                        }
                    }
                }
                if (!refPersonIds.contains(np.personId)) {
                    refPersonIds << np.personId
                    ndflReferencess << createRefBookAttributesForNdflReference(np.id, np.personId, nomSpr, np.lastName, np.firstName, np.middleName, np.birthDay)
                }
            }
        }
        if (!presentNotHoldingTax && priznakF == "2") {
            logger.info("\"Для подразделения: $depName, КПП: $kpp, ОКТМО: $oktmo за период $otchetGod $reportPeriod.name отсутствуют сведения о не удержанном налоге.\"")
            calculateParams.put("notReplaceXml", true)
            calculateParams.put("createForm", false)
        }
    }
    ScriptUtils.checkInterrupted();
    if (!isForSpecificReport) {
        if (!ndflReferencess.isEmpty()) {
            saveNdflRefences()
        }
        ScriptUtils.checkInterrupted();
        saveFileInfo(currDate, fileName)
    }
}

/**
 * Проверяет заполнены ли обязательные поля у физическмх лиц
 * @param ndflPerson
 * @return true - заполнены обязательные поля, false - не заполнены обязательные поля
 */
boolean checkMandatoryFields(List<NdflPerson> ndflPersonList) {
    boolean toReturn = true
    for (NdflPerson ndflPerson : ndflPersonList) {
        MANDATORY_FIELDS:
        {
            List<String> mandatoryFields = new LinkedList<>();
            if (ndflPerson.rowNum == null) mandatoryFields << "'№пп'"
            if (ndflPerson.inp == null || ndflPerson.inp.isEmpty()) mandatoryFields << "'Налогоплательщик.ИНП'"
            if (ndflPerson.lastName == null || ndflPerson.lastName.isEmpty() || ndflPerson.lastName == "0") mandatoryFields << "'Налогоплательщик.Фамилия'"
            if (ndflPerson.firstName == null || ndflPerson.firstName.isEmpty() || ndflPerson.firstName == "0") mandatoryFields << "'Налогоплательщик.Имя'"
            if (ndflPerson.birthDay == null) mandatoryFields << "'Налогоплательщик.Дата рождения'"
            boolean checkCitizenship = true
            if (ndflPerson.citizenship == null || ndflPerson.citizenship.isEmpty()) {
                mandatoryFields << "'Гражданство (код страны)'"
                checkCitizenship = false
            }
            if (ndflPerson.idDocType == null || ndflPerson.idDocType.isEmpty()) mandatoryFields << "'Документ удостоверяющий личность.Код'"
            if (ndflPerson.idDocNumber == null || ndflPerson.idDocNumber.isEmpty()) mandatoryFields << "'Документ удостоверяющий личность.Номер'"
            if (ndflPerson.status == null || ndflPerson.status.isEmpty()) mandatoryFields << "'Статус (Код)'"
            if (checkCitizenship) {
                if (ndflPerson.citizenship == "643") {
                    if (StringUtils.isBlank(ndflPerson.regionCode)) {
                        mandatoryFields << "'Код субъекта'"
                    }
                } else {
                    if (StringUtils.isBlank(ndflPerson.countryCode)) {
                        mandatoryFields << "'Код страны проживания вне РФ'"
                    }
                    if (StringUtils.isBlank(ndflPerson.address)) {
                        mandatoryFields << "'Адрес проживания вне РФ'"
                    }
                }
            }
            if (!mandatoryFields.isEmpty()) {
                def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
                def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
                String strCorrPeriod = ""
                if (departmentReportPeriod.getCorrectionDate() != null) {
                    strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
                }
                Department department = departmentService.get(departmentReportPeriod.departmentId)
                String lastname = ndflPerson.lastName != null ? ndflPerson.lastName + " " : ""
                String firstname = ndflPerson.firstName != null ? ndflPerson.firstName + " " : ""
                String middlename = ndflPerson.middleName != null ? ndflPerson.middleName : ""
                String fio = lastname + firstname + middlename

                String msg = String.format("Не удалось создать форму \"%s\" за период \"%s\", подразделение: \"%s\", КПП: \"%s\", ОКТМО: \"%s\". Не заполнены или равны \"0\" обязательные параметры %s для ФЛ: %s, ИНП: %s в форме РНУ НДФЛ (консолидированная) № %s",
                        currDeclarationTemplate.getName(),
                        departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                        department.getName(),
                        declarationData.kpp,
                        declarationData.oktmo,
                        mandatoryFields.join(', '),
                        fio,
                        ndflPerson.inp,
                        ndflPerson.declarationDataId
                )
                logger.error(msg)
                toReturn = false
            }
        }
    }
    return toReturn
}

// Сохранение информации о файле в комментариях
def saveFileInfo(currDate, fileName) {
    def fileUuid = blobDataServiceDaoImpl.create(xmlFile, fileName + ".xml", new Date())
    def createUser = declarationService.getSystemUserInfo().getUser()

    def fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
    def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_2.id}").get(0)

    def declarationDataFile = new DeclarationDataFile()
    declarationDataFile.setDeclarationDataId(declarationData.id)
    declarationDataFile.setUuid(fileUuid)
    declarationDataFile.setUserName(createUser.getName())
    declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(createUser.getDepartmentId()))
    declarationDataFile.setFileTypeId(fileTypeId)
    declarationDataFile.setDate(currDate)
    declarationService.saveFile(declarationDataFile)
}

int findCorrectionNumber() {
    int toReturn = 0
    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    if (departmentReportPeriod.correctionDate == null) {
        return toReturn
    }
    DepartmentReportPeriodFilter departmentReportPeriodFilter = new com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter();
    departmentReportPeriodFilter.setDepartmentIdList([declarationData.departmentId])
    departmentReportPeriodFilter.setReportPeriodIdList([declarationData.reportPeriodId])
    departmentReportPeriodFilter.setTaxTypeList([TaxType.NDFL])

    List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(departmentReportPeriodFilter)
    Iterator<DepartmentReportPeriod> it = departmentReportPeriodList.iterator();
    while (it.hasNext()) {
        DepartmentReportPeriod depReportPeriod = it.next();
        if (depReportPeriod.id == declarationData.departmentReportPeriodId) {
            it.remove();
        }
    }
    departmentReportPeriodList.sort(true, new Comparator<DepartmentReportPeriod>() {
        @Override
        int compare(DepartmentReportPeriod o1, DepartmentReportPeriod o2) {
            if (o1.correctionDate == null) {
                return 1;
            } else if (o2.correctionDate == null) {
                return -1;
            } else {
                return o2.correctionDate.compareTo(o1.correctionDate);
            }
        }
    })

    for (DepartmentReportPeriod drp in departmentReportPeriodList) {
        def declarations = []
        declarations.addAll(declarationService.find(DECLARATION_TYPE_NDFL6_ID, drp.id))
        declarations.addAll(declarationService.find(NDFL_2_1_DECLARATION_TYPE, drp.id))
        declarations.addAll(declarationService.find(NDFL_2_2_DECLARATION_TYPE, drp.id))
        if (!declarations.isEmpty()) {
            for (DeclarationData dd in declarations) {
                if (dd.kpp == declarationData.kpp && dd.oktmo == declarationData.oktmo) {
                    toReturn++
                    break
                }
            }
        }
    }
    return toReturn
}

def saveNdflRefences() {
    logger.setTaUserInfo(userInfo)
    getProvider(NDFL_REFERENCES).createRecordVersion(logger, new Date(), null, ndflReferencess)
}

/**
 * Заполнить значение реестра справок
 * @param personId
 * @param nomSpr
 * @param lastName
 * @param firstName
 * @param middleName
 * @param birthDay
 * @return
 */
def createRefBookAttributesForNdflReference(
        def ndflPersonId, def personId, def nomSpr, def lastName, def firstName, def middleName, def birthDay) {
    Map<String, RefBookValue> row = new HashMap<String, RefBookValue>();
    row.put(NDFL_PERSON_ID, new RefBookValue(RefBookAttributeType.NUMBER, ndflPersonId))
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

/**
 * Генерация имени файла
 * @return
 */
def generateXmlFileId(inn, kodNoProm) {
    def departmentParamRow = departmentParam ? getDepartmentParamDetails(declarationData.kpp, declarationData.oktmo) : null
    def r_t = "NO_NDFL2"
    def a = kodNoProm
    def k = departmentParamRow?.TAX_ORGAN_CODE?.value
    def o = inn + declarationData.kpp
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
    def toReturn = []
    def queryParameterList = ndflPersonKnfId.collate(1000)
    queryParameterList.each {
        if (!it.isEmpty()) {
            toReturn.addAll(ndflPersonService.findByIdList(it))
        }
    }
    return toReturn
    //ndflPersonService.findNdflPersonByParameters(declarationData.id, null, pageNumber * 3000 - 2999, NUMBER_OF_PERSONS)
}

// Получение данных
def getNdflPersons(Long personId) {
    def toReturn = []
    toReturn.addAll(ndflPersonService.findByIdList([personId]))
    return toReturn
}

/**
 * Получить доходы для ФЛ за период
 * @param ndflPersonId
 * @param priznakF
 * @param startDate
 * @param endDate
 * @return
 */
def filterIncomes(selectedIncomeRows, def priznakF) {
    def toReturn = new ArrayList<NdflPersonIncome>(selectedIncomeRows)
    if (priznakF == "1") {
        toReturn.removeAll { it.incomeAccruedSumm == null || it.incomeAccruedSumm == 0 }
    } else if (priznakF == "2") {
        toReturn.removeAll { it.notHoldingTax == null || it.notHoldingTax < 0 }
    }
    return toReturn
}

def findAllIncomes(def ndflPersonId, def startDate, def endDate, priznakF) {
    def toReturn
    // Временно строки отбираются без учета условия КНФ.Раздел 2.Графа 10 ≠ 0
    if (priznakF == "1") {
        //toReturn = ndflPersonService.findIncomesByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, Boolean.TRUE)
        toReturn = ndflPersonService.findIncomesByPeriodAndNdflPersonIdTemp(ndflPersonId, startDate, endDate, Boolean.TRUE)
    } else if (priznakF == "2") {
        toReturn = ndflPersonService.findIncomesByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, Boolean.FALSE)
    }
    return toReturn
}

/**
 * отфильтровать вычеты код дохода которых не совпадает
 * @param ndflPersonIncomes
 * @param ndflPersonDeductions
 * @return
 */
def filterDeductionsByIncomeCode(ndflPersonIncomes, def ndflPersonDeductions) {
    def toReturn = []
    ndflPersonIncomes.each { ndflPersonIncome ->
        Calendar taxDateCalIncome = new GregorianCalendar();
        taxDateCalIncome.setTime(ndflPersonIncome.incomeAccruedDate)

        for (d in ndflPersonDeductions) {
            Calendar taxDateCalDeduction = new GregorianCalendar();
            taxDateCalDeduction.setTime(d.incomeAccrued)
            if (d.incomeCode == ndflPersonIncome.incomeCode && d.operationId == ndflPersonIncome.operationId &&
                    taxDateCalIncome.get(Calendar.MONTH) == taxDateCalDeduction.get(Calendar.MONTH) && !toReturn.contains(d)) toReturn << d

        }
    }
    return toReturn
}

/**
 * отфильтровать вычеты по месяцу
 * @param ndflPersonIncomes
 * @param ndflPersonDeductions
 * @return
 */
def filterDeductions(ndflPersonIncomes, def ndflPersonDeductions) {
    def toReturn = []
    for (d in ndflPersonDeductions) {
        Calendar taxDateCalDeduction = new GregorianCalendar();
        taxDateCalDeduction.setTime(d.incomeAccrued)
        for (ndflPersonIncome in ndflPersonIncomes) {
            Calendar taxDateCalIncome = new GregorianCalendar();
            taxDateCalIncome.setTime(ndflPersonIncome.incomeAccruedDate)

            if (d.incomeCode == ndflPersonIncome.incomeCode && d.operationId == ndflPersonIncome.operationId &&
                    taxDateCalIncome.get(Calendar.MONTH) == taxDateCalDeduction.get(Calendar.MONTH) && !toReturn.contains(d)) {
                toReturn << d
                break
            }
        }
    }
    return toReturn
}

/**
 * Получить авансы для ФЛ за период для доходов с одинаковым номером операции
 * @param ndflPersonId
 * @param startDate
 * @param endDate
 *
 * @return
 */
def findPrepayments(def ndflPersonId, def taxRate, def startDate, def endDate, priznakF) {
    def toReturn = []
    if (priznakF == "1") {
        return ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, taxRate, startDate, endDate, true)
    } else {
        return ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, taxRate, startDate, endDate, false)
    }
}

// группирока по налоговой ставке
def groupByTaxRate(def incomes) {
    def toReturn = [:]
    def rates = []
    incomes.each {
        if (!rates.contains(it.taxRate)) {
            rates << it.taxRate
        }
    }
    rates.each { rate -> toReturn[rate] = incomes.findAll { it.taxRate.equals(rate) } }
    return toReturn
}
/**
 * Метод возвращает мапу, где ключ Integer сооветстувующий значениям месяцев из класа java.util.Calendar,
 * а значение мапа, где ключ код дохода, а значение список соответствующих объектов NdflPersonIncome
 * @param incomes
 * @return
 */
def groupIncomesByMonth(incomes) {
    def groupByMonth = [:]
    def monthes = []
    def toReturn = [:]
    incomes.each {
        Calendar accruedDateCal = new GregorianCalendar();
        accruedDateCal.setTime(it.incomeAccruedDate)
        def month = accruedDateCal.get(Calendar.MONTH)
        if (!monthes.contains(month)) {
            monthes.add(month)
        }
    }
    monthes = monthes.sort()
    monthes.each { month ->
        groupByMonth[month] = incomes.findAll {
            Calendar incomeCal = new GregorianCalendar()
            incomeCal.setTime(it.incomeAccruedDate)
            month == incomeCal.get(Calendar.MONTH)
        }
    }
    groupByMonth.keySet().each { key ->
        def gm = groupByMonth.get(key)
        def groupByIncomeCode = [:]
        def incomeCodes = []
        gm.each {
            if (!incomeCodes.contains(it.incomeCode)) {
                incomeCodes << it.incomeCode
            }
        }
        incomeCodes.each { incomeCode ->
            groupByIncomeCode[incomeCode] = gm.findAll {
                it.incomeCode.equals(incomeCode)
            }
        }
        toReturn[key] = groupByIncomeCode
    }
    return toReturn
}

// Группировка по коду вычета
def groupByDeductionTypeCode(def deductions) {
    def toReturn = [:]
    def typeCodes = []
    deductions.each {
        if (!typeCodes.contains(it.typeCode)) {
            typeCodes << it.typeCode
        }
    }
    typeCodes.each { typeCode -> toReturn[typeCode] = deductions.findAll { it.typeCode == typeCode } }
    return toReturn
}

def unionDeductionsForDeductionType(deductionGroups) {

    def toReturn = [:]
    deductionGroups.keySet().each { key ->
        def tempDeductions = []
        deductionGroups.get(key).each { deduction1 ->
            if (!notifPresent(deduction1, tempDeductions)) {
                tempDeductions << deduction1
            }
        }
        toReturn[key] = tempDeductions
    }
    return toReturn
}

def notifPresent(deduction, deductions) {
    def toReturn = false
    deductions.each {
        if (it.typeCode == deduction.typeCode && it.notifType == deduction.notifType
                && it.notifDate == deduction.notifDate && it.notifNum == deduction.notifNum
                && it.notifSource == deduction.notifSource && it.notifSumm == deduction.notifSumm) {

            toReturn = true
        }
    }
    return toReturn
}

def getSvSumVich(def deductionsFilteredForCurrIncome) {
    def toReturn = []
    deductionsFilteredForCurrIncome.each { group ->
        def deductionsForSum = []
        group.each {
            if (it.periodCurrSumm != null && it.periodCurrSumm != 0) {
                deductionsForSum << it
            }
        }
        if (!deductionsForSum.isEmpty()) {
            toReturn << [КодВычет: deductionsForSum[0].typeCode,
                         СумВычет: ScriptUtils.round(getSumVichOfPeriodCurrSumm(deductionsForSum), 2)]
        }
    }
    return toReturn
}

// Вычислить сумму для СумДоход
BigDecimal getSumDohod(def priznakF, List<NdflPersonIncome> rows, def taxRate) {
    def toReturn = new BigDecimal(0)
    if (priznakF == "1") {
        rows.each {
            if (it.incomeAccruedSumm != null && it.incomeAccruedSumm > 0 && it.taxRate == taxRate) {
                toReturn = toReturn.add(it.incomeAccruedSumm)
            }
        }
    } else if (priznakF == "2") {
        rows.each {
            if (it.notHoldingTax != null && it.taxRate == taxRate) {
                toReturn = toReturn.add(it.notHoldingTax)
            }
        }
    }
    return toReturn
}

// Вычислить сумму для НалБаза
def getNalBaza(def incomes, def taxRate) {
    def toReturn = new BigDecimal(0)
    incomes.each {
        if (it.taxBase != null && it.taxRate == taxRate) {
            toReturn = toReturn.add(it.taxBase)
        }
    }
    return toReturn
}

//Вычислить сумму для НалИсчисл
def getNalIschisl(def priznakF, def incomes, int taxRate) {
    def toReturn = new BigDecimal(0)
    if (priznakF == "1") {
        incomes.each {
            if (it.calculatedTax != null && taxRate == it.taxRate) {
                toReturn = toReturn.add(it.calculatedTax)
            }
        }
    } else if (priznakF == "2") {
        incomes.each {
            if (it.notHoldingTax != null && it.notHoldingTax > 0 && taxRate == it.taxRate) {
                toReturn = toReturn.add(it.notHoldingTax)
            }
        }
    }
    return toReturn
}

/**
 * Вычислить сумму для АвансПлатФикс
 * @param prepayments
 * @return
 */
def getAvansPlatFix(def prepayments) {
    def toReturn = new BigDecimal(0)
    prepayments.each {
        if (it.summ != null) {
            toReturn = toReturn.add(it.summ)
        }
    }
    return toReturn
}

/**
 * /Вычислить сумму вычета в текущем периоде
 * @param deductions
 * @return
 */
def getDeductionCurrPeriodSum(def deductions) {
    def toReturn = new BigDecimal(0)
    deductions.each {
        if (it.periodCurrSumm != null) {
            toReturn = toReturn.add(it.periodCurrSumm)
        }
    }
    return toReturn
}

//Вычислить сумму для НалУдерж
Long getNalUderzh(def priznakF, def incomes, startDate, endDate) {
    def toReturn = 0L
    if (priznakF == "1") {
        Map<Long, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { income -> income.operationId }
        // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода
        incomesGroupedByOperationId.each { k, v ->
            boolean correctDate = false
            v.each {
                if (it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                    correctDate = true
                }
            }
            if (correctDate) {
                v.each {
                    if (it.withholdingTax != null) {
                        toReturn += it.withholdingTax
                    }
                }
            }
        }
        return toReturn
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалПеречисл
def getNalPerechisl(def priznakF, def incomes, startDate, endDate) {
    def toReturn = 0L
    if (priznakF == "1") {
        Map<Long, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { income -> income.operationId }
        // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода
        incomesGroupedByOperationId.each { k, v ->
            boolean correctDate = false
            v.each {
                if (it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                    correctDate = true
                }
            }
            if (correctDate) {
                v.each {
                    if (it.taxSumm != null) {
                        toReturn += it.taxSumm
                    }
                }
            }
        }
        return toReturn
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалУдержЛиш
def getNalUderzhLish(def priznakF, def incomes, startDate, endDate) {
    def toReturn = 0L
    if (priznakF == "1") {
        Map<Long, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { income -> income.operationId }
        // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода
        incomesGroupedByOperationId.each { k, v ->
            boolean correctDate = false
            v.each {
                if (it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                    correctDate = true
                }
            }
            if (correctDate) {
                v.each {
                    if (it.overholdingTax != null) {
                        toReturn += it.overholdingTax
                    }
                }
            }
        }
        return toReturn
    } else if (priznakF == "2") {
        return 0
    }
}

//Вычислить сумму для НалНеУдерж
def getNalNeUderzh(priznakF, incomes, startDate, endDate) {
    def toReturn = 0L
    Map<Long, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { income -> income.operationId }
    // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода

    incomesGroupedByOperationId.each { k, v ->
        boolean correctDate = false
        v.each {
            if (it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                correctDate = true
            }
        }
        if (priznakF == "1" && correctDate) {
            v.each {
                if (it.notHoldingTax != null) {
                    toReturn += it.notHoldingTax
                }
            }
        } else if (priznakF == "2" && correctDate) {
            boolean calculatedTaxGreaterZero = false
            v.each {
                if (it.calculatedTax > 0) {
                    calculatedTaxGreaterZero = true
                }
            }
            if (calculatedTaxGreaterZero) {
                v.each {
                    if (it.notHoldingTax != null) {
                        toReturn += it.notHoldingTax
                    }
                }
            }
        }
    }
    return toReturn
}

Date getFirstMarchOfNextPeriod(def periodEndDate) {
    Calendar calendar = new GregorianCalendar()
    calendar.setTime(periodEndDate)
    int currYear = calendar.get(Calendar.YEAR)
    Calendar returnCalendar = new GregorianCalendar();
    returnCalendar.set(currYear + 1, 02, 01)
    return returnCalendar.getTime()
}

def getSumVichOfPeriodCurrSumm(deductions) {
    def toReturn = new BigDecimal(0)
    deductions.each {
        toReturn = toReturn.add(it.periodCurrSumm)
    }
    return toReturn
}

def getOktmoById(id) {
    def oktmo = OKTMO_CACHE.get(id)
    if (oktmo == null && id != null) {
        def rpe = getReportPeriodEndDate(declarationData.reportPeriodId)
        def provider = getProvider(REF_BOOK_OKTMO_ID)
        def oktmoList = provider.getRecords(rpe, null, "ID = ${id}", null)
        if (oktmoList.size() != 0) {
            oktmo = oktmoList.get(0)
            OKTMO_CACHE[id] = oktmo
        }

    }
    return oktmo
}

def getOktmoByIdList(idList) {
    def provider = getProvider(REF_BOOK_OKTMO_ID)
    return provider.getRecordData(idList)
}

def getOktmoByCode(code) {
    def oktmo = null
    def rpe = getReportPeriodEndDate(declarationData.reportPeriodId)
    def oktmoList = getProvider(REF_BOOK_OKTMO_ID).getRecords(rpe, null, "CODE = '${code}'", null)
    if (oktmoList.size() != 0) {
        oktmo = oktmoList.get(0)
        OKTMO_CACHE[oktmo.id.value] = oktmo
    }
    return oktmo
}

// Получить параметры подразделения (из справочника 950)
def getDepartmentParam(def departmentId, def reportPeriodId, boolean throwIfEmpty) {
    if (!departmentCache.containsKey(departmentId)) {
        def rpe = getReportPeriodEndDate(reportPeriodId)
        def provider = getProvider(REF_BOOK_NDFL_ID)
        def departmentParamList = provider.getRecords(rpe, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            if (throwIfEmpty) {
                departmentParamException(departmentId, reportPeriodId)
            } else {
                return null
            }
        }
        departmentCache.put(departmentId, departmentParamList?.get(departmentParamList.size() - 1))
    }
    return departmentCache.get(departmentId)
}

/**
 * Получить детали подразделения из справочника
 * @param departmentParamId
 * @param reportPeriodId
 * @return
 */
def getDepartmentParamDetails(def departmentParamId, def departmentId, def reportPeriodId) {
    if (departmentParamRow == null) {
        def departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(reportPeriodId), null, "REF_BOOK_NDFL_ID = $departmentParamId", null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            departmentParamException(departmentId, reportPeriodId)
        }
        def referencesOktmoList = departmentParamTableList.OKTMO?.value
        referencesOktmoList.removeAll([null])
        def oktmoForDepartment = getOktmoByIdList(referencesOktmoList)
        departmentParamRow = departmentParamTableList.find { dep ->
            def oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
            if (oktmo != null) {
                declarationData.kpp.equals(dep.KPP?.value) && declarationData.oktmo.equals(oktmo.CODE.value)
            }
        }
        if (departmentParamRow == null) {
            departmentParamException(departmentId, reportPeriodId)
        }
    }
    return departmentParamRow
}

/**
 * Получить детали подразделения из справочника по кпп и октмо формы
 * @param departmentParamId
 * @param reportPeriodId
 * @return
 */
def getDepartmentParamDetails(String kpp, String oktmo) {
    if (departmentParamRow == null) {
        def oktmoReferenceList = getProvider(REF_BOOK_OKTMO_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "CODE = '$oktmo'", null)
        def oktmoReference = oktmoReferenceList.get(oktmoReferenceList.size() - 1).id.value
        def departmentParamRowList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "OKTMO = $oktmoReference AND KPP = '$kpp'", null)
        if (departmentParamRowList == null || departmentParamRowList.isEmpty()) {
            departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
        }
        departmentParamRow = departmentParamRowList.get(departmentParamRowList.size() - 1)
    }
    return departmentParamRow
}
/**
 * Получение провайдера с использованием кеширования
 * @param providerId
 * @return
 */

RefBookDataProvider getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        def provider = refBookFactory.getDataProvider(providerId)
        providerCache.put(providerId, provider)
    }
    return providerCache.get(providerId)
}

/**
 * Получить дату начала отчетного периода
 * @return
 */
Date getReportPeriodStartDate() {
    return reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
}

/**
 * Получение даты окончания периода
 * @param reportPeriodId
 * @return
 */
def getReportPeriodEndDate(def reportPeriodId) {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Получить вид формы
def getFormType(def id) {
    if (formType == null) {
        formType = getProvider(REB_BOOK_FORM_TYPE_ID).getRecordData(id)
    }
    return formType;
}

/**
 * Определить признакФ
 */

def definePriznakF() {
    def code = formType?.CODE?.value
    switch (code) {
        case (NDFL_2_S_PRIZNAKOM_1): return "1"
        case (NDFL_2_S_PRIZNAKOM_2): return "2"

        default: return "0"
    }
}

/**
 * Получить вид вычета по коду вычета
 * @param code
 * @return
 */
def getDeductionType(def code) {
    def filter = "CODE = '$code'"
    def deductionTypeList = getProvider(REF_BOOK_DEDUCTION_TYPE_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, filter, null)
    if (deductionTypeList == null || deductionTypeList.size() == 0 || deductionTypeList.get(0) == null) {
        throw new Exception("Ошибка при получении кодов вычета. Коды вычета заполнены не полностью")
    }
    return deductionTypeList.get(deductionTypeList.size() - 1).DEDUCTION_MARK?.value
}

/**
 * Получить признак вычета
 * @param id
 * @return
 */
def getDeductionMark(def id) {
    getProvider(REF_BOOK_DEDUCTION_MARK_ID).getRecordData(id).NAME?.value
}

/************************************* СОЗДАНИЕ ФОРМЫ *****************************************************************/
@Field
DeclarationData declarationDataConsolidated;
/**
 * Проверки которые относятся только к консолидированной
 * @return
 */
def checkDataConsolidated() {

    // Map<DEPARTMENT.CODE, DEPARTMENT.NAME>
    def mapDepartmentNotExistRnu = [
            4L  : 'Байкальский банк',
            8L  : 'Волго-Вятский банк',
            20L : 'Дальневосточный банк',
            27L : 'Западно-Сибирский банк',
            32L : 'Западно-Уральский банк',
            37L : 'Московский банк',
            44L : 'Поволжский банк',
            52L : 'Северный банк',
            64L : 'Северо-Западный банк',
            82L : 'Сибирский банк',
            88L : 'Среднерусский банк',
            97L : 'Уральский банк',
            113L: 'Центральный аппарат ПАО Сбербанк',
            102L: 'Центрально-Чернозёмный банк',
            109L: 'Юго-Западный банк'
    ]
    DepartmentReportPeriod drp = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    List<String> listDepartmentNotAcceptedRnu = []
    List<DeclarationData> declarationDataList = declarationService.findAllActive(RNU_NDFL_DECLARATION_TYPE, drp.reportPeriod.id)

    for (DeclarationData dd : declarationDataList) {
        // Подразделение
        Long departmentCode = departmentService.get(dd.departmentId)?.code

        // Если налоговая форма не принята
        if (!dd.state.equals(State.ACCEPTED)) {
            listDepartmentNotAcceptedRnu << mapDepartmentNotExistRnu[departmentCode]
        }
        mapDepartmentNotExistRnu.remove(departmentCode)
    }

    // Период
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def reportPeriod = departmentReportPeriod.reportPeriod
    def period = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)
    def periodCode = period?.CODE?.stringValue
    def periodName = period?.NAME?.stringValue
    def calendarStartDate = reportPeriod?.calendarStartDate
    String correctionDateExpression = departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format(DATE_FORMAT_DOTTED)},"
    if (!mapDepartmentNotExistRnu.isEmpty()) {
        def listDepartmentNotExistRnu = []
        mapDepartmentNotExistRnu.each {
            listDepartmentNotExistRnu.add(it.value)
        }
        logger.warn("За период $periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")}" +
                " года" + correctionDateExpression + " не созданы экземпляры консолидированных налоговых форм для следующих ТБ: '${listDepartmentNotExistRnu.join("\", \"")}'." +
                " Данные этих форм не включены в отчетность!")
    }

    if (!listDepartmentNotAcceptedRnu.isEmpty()) {
        logger.warn("За период $periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")}" +
                " года" + correctionDateExpression + " имеются не принятые экземпляры консолидированных налоговых форм для следующих ТБ: '${listDepartmentNotAcceptedRnu.join("\", \"")}'," +
                ". Данные этих форм не включены в отчетность!")
    }
}

@Field
final int RNU_NDFL_DECLARATION_TYPE = 101

@Field
def departmentParamTableList = null

@Field
def departmentParamTableListCache = [:]

def createForm() {
    try {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        def declarationTypeId = currDeclarationTemplate.type.id
        // Мапа где значение физлица для каждой пары КПП и ОКТМО
        def ndflPersonsIdGroupedByKppOktmo = getNdflPersonsGroupedByKppOktmo()

        // Удаление ранее созданных отчетных форм
        declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
            ScriptUtils.checkInterrupted();
            declarationService.delete(it.id, userInfo)
        }

        if (ndflPersonsIdGroupedByKppOktmo == null || ndflPersonsIdGroupedByKppOktmo.isEmpty()) {
            return
        }
        // Пары КПП/ОКТМО отсутствующие в справочнике настройки подразделений
        declarationDataConsolidated = declarationDataConsolidated ?: declarationService.find(RNU_NDFL_DECLARATION_TYPE, departmentReportPeriod.id).get(0)
        List<Pair<String, String>> kppOktmoNotPresentedInRefBookList = declarationService.findNotPresentedPairKppOktmo(declarationDataConsolidated.id);
        for (Pair<String, String> kppOktmoNotPresentedInRefBook : kppOktmoNotPresentedInRefBookList) {
            logger.warn("Для подразделения Тербанк отсутствуют настройки подразделений для КПП: %s, ОКТМО: %s в справочнике \"Настройки подразделений\". Данные формы РНУ НДФЛ (консолидированная) № %d по указанным КПП и ОКТМО источника выплаты не включены в отчетность.", kppOktmoNotPresentedInRefBook.getFirst(), kppOktmoNotPresentedInRefBook.getSecond(), declarationDataConsolidated.id)
        }
        // Создание ОНФ для каждой пары КПП и ОКТМО
        ndflPersonsIdGroupedByKppOktmo.each { npGroup ->
            ScriptUtils.checkInterrupted();
            def oktmo = npGroup.key.oktmo
            def kpp = npGroup.key.kpp
            def taxOrganCode = npGroup.key.taxOrganCode

            def npGroupValue = npGroup.value.collate(NUMBER_OF_PERSONS)
            def partTotal = npGroupValue.size()
            npGroupValue.eachWithIndex { part, index ->
                ScriptUtils.checkInterrupted();
                def npGropSourcesIdList = part.id
                if (npGropSourcesIdList == null || npGropSourcesIdList.isEmpty()) {
                    if (departmentReportPeriod.correctionDate != null) {
                        logger.info("Для КПП $kpp - ОКТМО $oktmo отсутствуют данные физических лиц, содержащих ошибки от ФНС в справке 2НДФЛ. Создание формы 2НДФЛ невозможно.")
                    }
                    return;
                }
                Map<String, Object> params
                Long ddId
                def indexFrom1 = ++index
                def note = "Часть ${indexFrom1} из ${partTotal}"
                params = new HashMap<String, Object>()
                ddId = declarationService.create(logger, declarationData.declarationTemplateId, userInfo,
                        departmentReportPeriodService.get(declarationData.departmentReportPeriodId), taxOrganCode, kpp.toString(), oktmo.toString(), null, null, note.toString())
                //appendNdflPersonsToDeclarationData(ddId, part)
                params.put(PART_NUMBER, indexFrom1)
                params.put(PART_TOTAL, partTotal)
                params.put(NDFL_PERSON_KNF_ID, npGropSourcesIdList)

                formMap.put(ddId, params)
            }
        }
    } finally {
        scriptParams.put("pairKppOktmoTotal", pairKppOktmoSize)
    }
}

Map<PairKppOktmo, List<NdflPerson>> getNdflPersonsGroupedByKppOktmo() {
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def pairKppOktmoList = []
    def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    formType = getFormType(currDeclarationTemplate.declarationFormTypeId)
    def priznakF = definePriznakF()
    def declarationTypeId = currDeclarationTemplate.type.id
    def ndflReferencesWithError = []
    def departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false)
    String depName = departmentService.get(departmentParam.DEPARTMENT_ID.value.toInteger()).name
    def reportPeriod = departmentReportPeriod.reportPeriod
    def otchetGod = reportPeriod.taxPeriod.year
    String strCorrPeriod = ""
    if (departmentReportPeriod.getCorrectionDate() != null) {
        strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
    }
    // Поиск КПП и ОКТМО для корр периода
    if (departmentReportPeriod.correctionDate != null) {
        def declarations = []

        DepartmentReportPeriodFilter departmentReportPeriodFilter = new com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList([departmentReportPeriod.departmentId])
        departmentReportPeriodFilter.setReportPeriodIdList([departmentReportPeriod.reportPeriod.id])
        departmentReportPeriodFilter.setTaxTypeList([TaxType.NDFL])

        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodService.getListByFilter(departmentReportPeriodFilter)
        Iterator<DepartmentReportPeriod> it = departmentReportPeriodList.iterator();
        while (it.hasNext()) {
            DepartmentReportPeriod depReportPeriod = it.next();
            if (depReportPeriod.id == declarationData.departmentReportPeriodId) {
                it.remove()
            }
            if (depReportPeriod.correctionDate != null && depReportPeriod.correctionDate > departmentReportPeriod.correctionDate) {
                it.remove()
            }
        }
        departmentReportPeriodList.sort(true, new Comparator<DepartmentReportPeriod>() {
            @Override
            int compare(DepartmentReportPeriod o1, DepartmentReportPeriod o2) {
                if (o1.correctionDate == null) {
                    return 1;
                } else if (o2.correctionDate == null) {
                    return -1;
                } else {
                    return o2.correctionDate.compareTo(o1.correctionDate);
                }
            }
        })

        for (DepartmentReportPeriod drp in departmentReportPeriodList) {
            declarations = declarationService.find(declarationTypeId, drp.id)
            if (priznakF == "2") {
                declarations.addAll(declarationService.find(NDFL_2_1_DECLARATION_TYPE, drp.id))
            }
            if (!declarations.isEmpty() || drp.correctionDate == null) {
                break
            }
        }

        def declarationsForRemove = []
        declarations.each { declaration ->
            ScriptUtils.checkInterrupted()

            def stateDocReject = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Отклонен'", null).get(0).id.value
            def stateDocNeedClarify = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Требует уточнения'", null).get(0).id.value
            def stateDocError = getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Ошибка'", null).get(0).id.value
            def declarationTemplate = declarationService.getTemplate(declaration.declarationTemplateId)
            if (!(declarationTemplate.declarationFormKind == DeclarationFormKind.REPORTS && (declaration.docState == stateDocReject
                    || declaration.docState == stateDocNeedClarify || declaration.docState == stateDocError))) {
                declarationsForRemove << declaration
            }
        }
        declarations.removeAll(declarationsForRemove)

        if (declarations.isEmpty()) {
            createCorrPeriodNotFoundMessage(departmentReportPeriod, true)
            return null
        }

        // список НФ у которых нет справок с ошибками
        List<DeclarationData> declarationWithoutNdflReferencesWithError = []
        declarations.each {
            ScriptUtils.checkInterrupted();
            def ndflReferencesWithErrorForDeclarationData = getNdflReferencesWithError(it.id)
            if (ndflReferencesWithErrorForDeclarationData.isEmpty()) {
                declarationWithoutNdflReferencesWithError.add(it)
                createNdflReferencesNotFoundMessageForDeclarationData(departmentReportPeriod, it)
            }
            ndflReferencesWithError.addAll(getNdflReferencesWithError(it.id))
        }

        if (ndflReferencesWithError.isEmpty()) {
            createCorrPeriodNotFoundMessage(departmentReportPeriod, false)
            return null
        }

        declarations.removeAll(declarationWithoutNdflReferencesWithError)

        declarations.each { declaration ->
            def pairKppOktmo = new PairKppOktmo(declaration.kpp, declaration.oktmo, declaration.taxOrganCode)
            if (!pairKppOktmoList.contains(pairKppOktmo)) {
                pairKppOktmoList << pairKppOktmo
            }
        }

    } else {
        // Поиск КПП и ОКТМО для некорр периода
        // Поиск дочерних подразделений. Поскольку могут существовать пары КПП+ОКТМО в ref_book_ndfl_detail ссылающиеся
        // только на обособленные подразделения тербанка
        List<Department> childrenDepartments = departmentService.getAllChildren(departmentReportPeriod.departmentId)
        def referencesOktmoList = []
        def departmentParamTableList = []
        for (Department childrenDepartment in childrenDepartments) {
            departmentParam = getDepartmentParam(childrenDepartment.id, departmentReportPeriod.reportPeriod.id, false)
            if (departmentParam != null) {
                departmentParamTableList.addAll(getDepartmentParamDetailsList(departmentParam?.id, departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false))
                referencesOktmoList.addAll(departmentParamTableList.OKTMO?.value)
            }
        }
        referencesOktmoList.removeAll([null])
        if (referencesOktmoList.isEmpty()) {
            logger.error("Отчетность %s  для %s за период %s не сформирована. Отсутствуют настройки указанного подразделения в справочнике \"Настройки подразделений", reportType, depName, "$otchetGod ${reportPeriod.name}")
            return
        }
        def oktmoForDepartment = getOktmoByIdList(referencesOktmoList)
        departmentParamTableList.each { dep ->
            ScriptUtils.checkInterrupted();
            if (dep.OKTMO?.value != null) {
                def oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
                if (oktmo != null) {
                    def pairKppOktmo = new PairKppOktmo(dep.KPP?.value, oktmo.CODE.value, dep?.TAX_ORGAN_CODE?.value)
                    if (!pairKppOktmoList.contains(pairKppOktmo)) {
                        pairKppOktmoList << pairKppOktmo
                    }
                }
            }
        }
        if (pairKppOktmoList.isEmpty()) {
            logger.error("Отчетность %s для %s за период %s не сформирована. Отсутствуют настройки указанного подразделения в справочнике \"Настройки подразделений", reportType, depName, "$otchetGod ${reportPeriod.name}")
            return
        }
    }
    pairKppOktmoSize = pairKppOktmoList.size()
    // Все подходящие КНФ
    def allDeclarationData = findAllTerBankDeclarationData(departmentReportPeriod)
    if (allDeclarationData == null) {
        return null
    }
    /*declarationService.findDeclarationDataIdByTypeStatusReportPeriod(declarationData.reportPeriodId, departmentParam?.id.value,
    RNU_NDFL_DECLARATION_TYPE, DepartmentType.TERR_BANK.getCode(),
    Boolean.TRUE, State.ACCEPTED.getId())*/
    // Мапа где значение физлица для каждой пары КПП и ОКТМО
    def ndflPersonsIdGroupedByKppOktmo = [:]
    if (!allDeclarationData.isEmpty()) {
        pairKppOktmoList.each { pair ->
            ScriptUtils.checkInterrupted();
            // Поиск физлиц по КПП и ОКТМО операций относящихся к ФЛ

            def ndflPersons
            if (declarationData.declarationTemplateId == NDFL_2_2_DECLARATION_TYPE) {
                ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(allDeclarationData.id, pair.kpp.toString(), pair.oktmo.toString(), true)
            } else {
                ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(allDeclarationData.id, pair.kpp.toString(), pair.oktmo.toString(), false)
            }

            if (ndflPersons != null && !ndflPersons.isEmpty()) {
                if (departmentReportPeriod.correctionDate != null) {
                    def ndflPersonsPicked = []
                    ndflReferencesWithError.each { reference ->
                        ndflPersons.each { person ->
                            if (reference.PERSON_ID?.value == person.personId) {
                                if (!ndflPersonsPicked.contains(person)) {
                                    ndflPersonsPicked << person
                                }
                            }
                        }
                    }
                    ndflPersons = ndflPersonsPicked
                }
            }
            if (ndflPersons != null && !ndflPersons.isEmpty()) {
                addNdflPersons(ndflPersonsIdGroupedByKppOktmo, pair, ndflPersons)
            } else {
                String depChildName = departmentService.getDepartmentNameByPairKppOktmo(pair.kpp, pair.oktmo, departmentReportPeriod.reportPeriod.endDate)
                if (declarationData.declarationTemplateId == NDFL_2_2_DECLARATION_TYPE) {
                    logger.warn("Не удалось создать форму $reportType, за период $otchetGod ${reportPeriod.name}$strCorrPeriod, подразделение: ${depChildName ?: ""}, КПП: ${pair.kpp}, ОКТМО: ${pair.oktmo}. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName, за период $otchetGod ${reportPeriod.name}$strCorrPeriod отсутствуют сведения о не удержанном налоге для указанных КПП и ОКТМО.")
                } else {
                    logger.warn("Не удалось создать форму $reportType, за период $otchetGod ${reportPeriod.name}$strCorrPeriod, подразделение: ${depChildName ?: ""}, КПП: ${pair.kpp}, ОКТМО: ${pair.oktmo}. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName, за период $otchetGod ${reportPeriod.name}$strCorrPeriod отсутствуют сведения о НДФЛ для указанных КПП и ОКТМО.")
                }
            }
        }
    }
    if (ndflPersonsIdGroupedByKppOktmo == null || ndflPersonsIdGroupedByKppOktmo.isEmpty()) {
        logger.error("Отчетность $reportType  для $depName за период $otchetGod ${reportPeriod.name}$strCorrPeriod не сформирована. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName за период $otchetGod ${reportPeriod.name}$strCorrPeriod отсутствуют сведения о не удержанном налоге.")
    }
    return ndflPersonsIdGroupedByKppOktmo
}

/**
 * Добавляет в логгер сообщение о том что не найдены формы для корректирующего периода
 * @param departmentReportPeriod
 * @param forDepartment
 * @return
 */
@TypeChecked
def createCorrPeriodNotFoundMessage(DepartmentReportPeriod departmentReportPeriod, boolean forDepartment) {
    DepartmentReportPeriod prevDrp = getPrevDepartmentReportPeriod(departmentReportPeriod)
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    String correctionDateExpression = getCorrectionDateExpression(departmentReportPeriod)
    if (forDepartment) {
        logger.error("Уточненная отчетность $reportType для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для подразделения ${department.name} и периода ${prevDrp.reportPeriod.taxPeriod.year}, ${prevDrp.reportPeriod.name}" + getCorrectionDateExpression(prevDrp) + " не найдены отчетные формы, \"Состояние ЭД\" которых равно \"Отклонен\", \"Требует уточнения\" или \"Ошибка\".")
    } else {
        logger.error("Уточненная отчетность <$reportType для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для заданного В отчетных формах подразделения ${department.name} и периода ${prevDrp.reportPeriod.taxPeriod.year}, ${prevDrp.reportPeriod.name}" + getCorrectionDateExpression(prevDrp) + " не найдены физические лица, \"Текст ошибки от ФНС\" которых заполнен. Уточненная отчетность формируется только для указанных физических лиц.")
    }
}

@TypeChecked
def createNdflReferencesNotFoundMessageForDeclarationData(DepartmentReportPeriod departmentReportPeriod, DeclarationData declarationData) {
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    logger.warn("Не удалось создать форму $reportType, за ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + getCorrectionDateExpression(departmentReportPeriod) + ", подразделение: ${department.name}, КПП: ${declarationData.kpp}, ОКТМО: ${declarationData.oktmo}. В отчетной форме № ${declarationData.id} не найдены физические лица, \"Текст ошибки от ФНС\" которых заполнен. Уточненная отчетность формируется только для указанных физических лиц.")
}

/**
 * Добавить ФЛ к паре КПП и ОКТМО. Если какая-то пара КПП+ОКТМО указана в справочнике больше 1 раза,
 * прозводится анализ значения "Код НО конечный", указанные для этих повтояющихся пар
 * @param ndflPersonsGroupedByKppOktmo мапа где ключ пара КПП и ОКТМО
 * @param pairKppOktmoBeingComparing пара КПП и ОКТМО, наличие которой проверяется в ndflPersonsGroupedByKppOktmo
 * @param ndflPersonList список ФЛ относящихся к pairKppOktmoBeingComparing
 * @return
 */
def addNdflPersons(ndflPersonsGroupedByKppOktmo, pairKppOktmoBeingComparing, ndflPersonList) {
    // Физлица для каждой пары КПП и октмо
    def kppOktmoNdflPersons = ndflPersonsGroupedByKppOktmo.get(pairKppOktmoBeingComparing)
    if (kppOktmoNdflPersons == null) {
        ndflPersonsGroupedByKppOktmo.put(pairKppOktmoBeingComparing, ndflPersonList)
    } else {
        def kppOktmoNdflPersonsEntrySet = ndflPersonsGroupedByKppOktmo.entrySet()
        kppOktmoNdflPersonsEntrySet.each {
            if (it.getKey().equals(pairKppOktmoBeingComparing)) {
                if (it.getKey().taxOrganCode != pairKppOktmoBeingComparing.taxOrganCode) {
                    logger.warn("Для КПП = ${pairKppOktmoBeingComparing.kpp} ОКТМО = ${pairKppOktmoBeingComparing.oktmo} в справочнике \"Настройки подразделений\" задано несколько значений Кода НО (кон).")
                }
                //Если Коды НО совпадают, для всех дублей пар КПП+ОКТМО создается одна ОНФ, в которой указывается совпадающий Код НО.
                it.getValue().addAll(ndflPersonList)
            }
        }
    }
}

DepartmentReportPeriod getPrevDepartmentReportPeriod(DepartmentReportPeriod departmentReportPeriod) {
    DepartmentReportPeriod prevDepartmentReportPeriod = departmentReportPeriodService.getPrevLast(declarationData.departmentId, departmentReportPeriod.reportPeriod.id)
    if (prevDepartmentReportPeriod == null) {
        prevDepartmentReportPeriod = departmentReportPeriodService.getFirst(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id)
    }
    return prevDepartmentReportPeriod
}

/**
 * получить id всех ТБ для данного отчетного периода
 * @param departmentReportPeriod
 * @return
 */
def findAllTerBankDeclarationData(def departmentReportPeriod) {
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.id)
    def allDeclarationData = []
    allDepartmentReportPeriodIds.each {
        ScriptUtils.checkInterrupted();
        allDeclarationData.addAll(declarationService.find(RNU_NDFL_DECLARATION_TYPE, it))
    }

    if (!checkExistingConsDDForCurrTB(departmentReportPeriod, allDeclarationData)) {
        createEmptyMessage(departmentReportPeriod, false)
        return null;
    }
    if (declarationDataConsolidated == null) {
        declarationDataConsolidated = declarationService.find(RNU_NDFL_DECLARATION_TYPE, departmentReportPeriod.id).get(0)
    }
    if (!checkExistingAcceptedConsDDForCurrTB(departmentReportPeriod, allDeclarationData)) {
        createEmptyMessage(departmentReportPeriod, true)
        return null;
    }

    // удаление форм не со статусом Принята
    def declarationsForRemove = []
    allDeclarationData.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }

    allDeclarationData.removeAll(declarationsForRemove)

    return allDeclarationData
}

@TypeChecked
boolean checkExistingAcceptedConsDDForCurrTB(DepartmentReportPeriod departmentReportPeriod, List<DeclarationData> ddList) {
    for (DeclarationData dd : ddList) {
        if (dd.departmentReportPeriodId == departmentReportPeriod.id && dd.state == State.ACCEPTED) {
            return true;
        }
    }
    return false;
}

@TypeChecked
boolean checkExistingConsDDForCurrTB(DepartmentReportPeriod departmentReportPeriod, List<DeclarationData> ddList) {
    for (DeclarationData dd : ddList) {
        if (dd.departmentReportPeriodId == departmentReportPeriod.id) {
            return true;
        }
    }
    return false;
}

@TypeChecked
def createEmptyMessage(DepartmentReportPeriod departmentReportPeriod, boolean acceptChecking) {
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    String correctionDateExpression = getCorrectionDateExpression(departmentReportPeriod)
    if (acceptChecking) {
        logger.error("Отчетность $reportType для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для указанного подразделения и периода форма РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated?.id} должна быть в состоянии \"Принята\". Примите форму и повторите операцию")
    } else {
        logger.error("Отчетность $reportType для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для указанного подразделения и периода не найдена форма РНУ НДФЛ (консолидированная).")
    }
}
/************************************* ВЫГРУЗКА ***********************************************************************/

/**
 * Выгрузка архива с созданными xml
 * @return
 */
def createReports() {
    if (!preCreateReports()) {
        return
    }
    ZipArchiveOutputStream zos = new ZipArchiveOutputStream(outputStream);
    scriptParams.put("fileName", "reports.zip")
    try {
        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId);
        Department department = departmentService.get(departmentReportPeriod.departmentId);
        String strCorrPeriod = getCorrectionDateExpression(departmentReportPeriod)
        String path = String.format("Отчетность %s, %s, %s, %s%s",
                declarationTemplate.getName(),
                department.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(), departmentReportPeriod.getReportPeriod().getName(), strCorrPeriod)
                .replaceAll('[~!@/\\#\$%^&*=|`]', "_").replaceAll('"', "'");
        scriptParams.put("fileName", path + ".zip")
        def declarationTypeId = declarationService.getTemplate(declarationData.declarationTemplateId).type.id
        declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
            ScriptUtils.checkInterrupted();
            if (it.fileName == null) {
                return
            }
            ZipArchiveEntry ze = new ZipArchiveEntry(path + "/" + it.taxOrganCode + "/" + it.fileName + ".xml");
            zos.putArchiveEntry(ze);
            IOUtils.copy(declarationService.getXmlStream(it.id), zos)
            zos.closeArchiveEntry();
        }
    } finally {
        IOUtils.closeQuietly(zos);
    }
}


@TypeChecked
boolean preCreateReports() {
    ScriptUtils.checkInterrupted()
    Map<String, Object> paramMap = (Map<String, Object>) getProperty("paramMap")
    DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId);
    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId);
    Department department = departmentService.get(departmentReportPeriod.departmentId);
    String strCorrPeriod = "";
    if (departmentReportPeriod.getCorrectionDate() != null) {
        strCorrPeriod = " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
    }
    def declarationTypeId = declarationService.getTemplate(declarationData.declarationTemplateId).type.id
    def declarationList = declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId)
    if (declarationList.isEmpty()) {
        String msg = String.format("Отсутствуют отчетность \"%s\" для \"%s\", \"%s\". Сформируйте отчетность и повторите операцию",
                declarationTemplate.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                department.getName())
        logger.error(msg)
        if (paramMap != null) {
            paramMap.put("errMsg", msg)
        }
        return false
    }

    Set<PairKppOktmo> pairKppOktmoList = getNdflPersonsGroupedByKppOktmo()?.keySet()
    if (pairKppOktmoList == null) {
        String msg = "Сформируйте отчетность и повторите операцию"
        logger.error(msg)
        if (paramMap != null) {
            paramMap.put("errMsg", msg)
        }
        return false
    }

    declarationList.each {
        pairKppOktmoList.remove(new PairKppOktmo(it.kpp, it.oktmo, it.taxOrganCode))
    }

    if (!pairKppOktmoList.isEmpty()) {
        List<String> kppOktmo = []
        pairKppOktmoList.each {
            kppOktmo.add("" + it.kpp + "/" + it.oktmo)
        }
        String msg = String.format("Отсутствуют отчетные формы для следующих КПП+ОКТМО: %s. Сформируйте отчетность и повторите операцию",
                com.aplana.sbrf.taxaccounting.model.util.StringUtils.join(kppOktmo.toArray(), ", ", null))
        logger.error(msg)
        if (paramMap != null) {
            paramMap.put("errMsg", msg)
        }

        return false
    }
    return true
}

/*********************************ПОЛУЧИТЬ ИСТОЧНИКИ*******************************************************************/
@Field
def sourceReportPeriod = null

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
    def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.id)
    def tmpDeclarationDataList = []
    allDepartmentReportPeriodIds.each {
        ScriptUtils.checkInterrupted();
        def tmpDepartmentReportPeriod = departmentReportPeriodService.get(it)
        def tmpDeclaration = declarationService.findDeclarationDataByKppOktmoOfNdflPersonIncomes(sourceTypeId, it, tmpDepartmentReportPeriod.departmentId, tmpDepartmentReportPeriod.reportPeriod.id, declarationData.kpp, declarationData.oktmo)
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
        ScriptUtils.checkInterrupted();
        DepartmentReportPeriod tmpDepartmentReportPeriod = departmentReportPeriodService.get(tmpDeclarationData.departmentReportPeriodId)
        if (tmpDepartmentReportPeriod.correctionDate != departmentReportPeriod.correctionDate) {
            return
        }
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
    def isSource = sourceTypeId == 101

    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(tmpDeclarationData?.departmentReportPeriodId)
    DeclarationTemplate declarationTemplate = declarationService.getTemplate(sourceTypeId)

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        /** Идентификатор подразделения */
        relation.departmentId = department.id
        /** полное название подразделения */
        relation.fullDepartmentName = departmentService.getParentsHierarchy(department.id)
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

/************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

/**
 * Получить строку о дате корректировки
 * @param departmentReportPeriod
 * @return
 */
String getCorrectionDateExpression(DepartmentReportPeriod departmentReportPeriod) {
    return departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")},"
}

/**
 * Получить список детали подразделения из справочника
 * @param departmentParamId
 * @param reportPeriodId
 * @return
 */
def getDepartmentParamDetailsList(def departmentParamId, def departmentId, def reportPeriodId, boolean throwIfEmpty) {
    if (!departmentParamTableListCache.containsKey(departmentParamId)) {
        def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
        def rpe = getReportPeriodEndDate(reportPeriodId)
        def provider = getProvider(REF_BOOK_NDFL_DETAIL_ID)
        def departmentParamTableList = provider.getRecords(rpe, null, filter, null)
        if ((departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) && throwIfEmpty) {
            departmentParamException(departmentId, reportPeriodId)
        }
        departmentParamTableListCache.put(departmentParamId, departmentParamTableList)
    }
    return departmentParamTableListCache.get(departmentParamId)
}

// Получить список из реестра справок с ошибкой ФНС
def getNdflReferencesWithError(declarationDataId) {
    def filter = "DECLARATION_DATA_ID = ${declarationDataId}"
    def allNdflReferences = getProvider(NDFL_REFERENCES).getRecords(new Date(), null, filter, null)
    def ndflReferencesForRemove = []
    allNdflReferences.each {
        if (it.ERRTEXT?.value == null) {
            ndflReferencesForRemove << it
        }
    }
    allNdflReferences.removeAll(ndflReferencesForRemove)
    return allNdflReferences
}

/**
 * Класс инкапсулирующий информацию о КПП, ОКТМО и кон налогового органа подразделения
 */
class PairKppOktmo {
    def kpp
    def oktmo
    def taxOrganCode

    PairKppOktmo(def kpp, def oktmo, def taxOrganCode) {
        this.kpp = kpp
        this.oktmo = oktmo
        this.taxOrganCode = taxOrganCode
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PairKppOktmo that = (PairKppOktmo) o

        if (kpp != that.kpp) return false
        if (oktmo != that.oktmo) return false

        return true
    }

    int hashCode() {
        int result
        result = (kpp != null ? kpp.hashCode() : 0)
        result = 31 * result + (oktmo != null ? oktmo.hashCode() : 0)
        return result
    }

    @Override
    String toString() {
        return kpp + " " + oktmo + " " + taxOrganCode
    }
}

// Кэш для справочников
@Field Map<String, Map<String, RefBookValue>> refBookCache = [:]

/**
 * Разыменование записи справочника
 */
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}
/************************************* СПЕЦОТЧЕТ **********************************************************************/


@Field final String ALIAS_PRIMARY_RNU_W_ERRORS = "primary_rnu_w_errors"

@Field final String TRANSPORT_FILE_TEMPLATE = "ТФ"

// Мапа где ключ идентификатор NdflPerson, значение NdflPerson соответствующий идентификатору
@Field Map<Long, NdflPerson> ndflpersonFromRNUPrimary = [:]

//------------------ PREPARE_SPECIFIC_REPORT ----------------------

def prepareSpecificReport() {
    def reportAlias = scriptSpecificReportHolder?.declarationSubreport?.alias;
    if ('report_2ndfl' != reportAlias) {
        throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
    }
    PrepareSpecificReportResult result = new PrepareSpecificReportResult();
    List<Column> tableColumns = createTableColumns();
    List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
    def rowColumns = createRowColumns()

    //Проверка, подготовка данных
    def params = scriptSpecificReportHolder.subreportParamValues
    def reportParameters = scriptSpecificReportHolder.getSubreportParamValues();

    if (reportParameters.isEmpty()) {
        throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.");
    }

    def resultReportParameters = [:]
    reportParameters.each { key, value ->
        if (value != null) {
            if (key == "toBirthDay" || key == "fromBirthDay") {
                resultReportParameters.put(key, ScriptUtils.formatDate(value, "dd.MM.yyyy"))
            } else {
                resultReportParameters.put(key, value)
            }
        }
    }

    // Ограничение числа выводимых записей
    int pageSize = 10

    // Поиск данных по фильтру
    def docs = searchData(resultReportParameters, pageSize, result)

    // Формирование списка данных для вывода в таблицу
    docs.each() { doc ->
        DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(rowColumns, null));
        row.pNumSpravka = doc.@НомСпр.text()
        row.lastName = doc?.ПолучДох?.ФИО?.@Фамилия?.text()
        row.firstName = doc?.ПолучДох?.ФИО?.@Имя?.text()
        row.middleName = doc?.ПолучДох?.ФИО?.@Отчество?.text()
        row.innNp = doc?.ПолучДох?.@ИННФЛ.text()
        row.birthDay = doc?.ПолучДох?.@ДатаРожд?.text()
        row.idDocNumber = doc?.ПолучДох?.УдЛичнФЛ?.@СерНомДок?.text()
        row.statusNp = getPersonStatusName(doc?.ПолучДох?.@Статус.text())
        row.innForeign = doc?.ПолучДох?.@ИННИно.text()
        dataRows.add(row)
    }

    result.setTableColumns(tableColumns);
    result.setDataRows(dataRows);
    scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
    scriptSpecificReportHolder.setSubreportParamValues(params)
}

String getPersonStatusName(String statusCode) {
    RefBookDataProvider provider = getProvider(RefBook.Id.TAXPAYER_STATUS.getId())
    PagingResult<Long, Map<String, RefBookValue>> record = provider.getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "CODE = '$statusCode'", null)
    return record.get(0).get("NAME").getValue()
}

def createTableColumns() {
    List<Column> tableColumns = new ArrayList<Column>()

    Column pNumSpravka = new StringColumn()
    pNumSpravka.setAlias("pNumSpravka")
    pNumSpravka.setName("№ справки 2НДФЛ")
    pNumSpravka.setWidth(5)
    tableColumns.add(pNumSpravka)

    Column column1 = new StringColumn()
    column1.setAlias("lastName")
    column1.setName("Фамилия")
    column1.setWidth(10)
    tableColumns.add(column1)

    Column column2 = new StringColumn()
    column2.setAlias("firstName")
    column2.setName("Имя")
    column2.setWidth(10)
    tableColumns.add(column2)

    Column column3 = new StringColumn()
    column3.setAlias("middleName")
    column3.setName("Отчество")
    column3.setWidth(10)
    tableColumns.add(column3)

    Column column4 = new StringColumn()
    column4.setAlias("innNp")
    column4.setName("ИНН РФ")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("birthDay")
    column5.setName("Дата рождения")
    column5.setWidth(10)
    tableColumns.add(column5)

    Column column6 = new StringColumn()
    column6.setAlias("idDocNumber")
    column6.setName("№ ДУЛ")
    column6.setWidth(10)
    tableColumns.add(column6)

    Column column7 = new StringColumn()
    column7.setAlias("statusNp")
    column7.setName("Статус налогоплательщика")
    column7.setWidth(30)
    tableColumns.add(column7)

    Column column8 = new StringColumn()
    column8.setAlias("innForeign")
    column8.setName("ИНН Страны гражданства")
    column8.setWidth(10)
    tableColumns.add(column8)

    return tableColumns;
}

def createRowColumns() {
    List<Column> tableColumns = new ArrayList<Column>();

    Column pNumSpravka = new StringColumn()
    pNumSpravka.setAlias("pNumSpravka")
    pNumSpravka.setName("pNumSpravka")
    pNumSpravka.setWidth(10)
    tableColumns.add(pNumSpravka)

    Column column1 = new StringColumn()
    column1.setAlias("lastName")
    column1.setName("Фамилия")
    column1.setWidth(10)
    tableColumns.add(column1)

    Column column2 = new StringColumn()
    column2.setAlias("firstName")
    column2.setName("Имя")
    column2.setWidth(10)
    tableColumns.add(column2)

    Column column3 = new StringColumn()
    column3.setAlias("middleName")
    column3.setName("Отчество")
    column3.setWidth(10)
    tableColumns.add(column3)

    Column column4 = new StringColumn()
    column4.setAlias("innNp")
    column4.setName("ИНН РФ")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("birthDay")
    column5.setName("Дата рождения")
    column5.setWidth(10)
    tableColumns.add(column5)

    Column column6 = new StringColumn()
    column6.setAlias("idDocNumber")
    column6.setName("№ ДУЛ")
    column6.setWidth(10)
    tableColumns.add(column6)

    Column column7 = new StringColumn()
    column7.setAlias("statusNp")
    column7.setName("Статус налогоплательщика")
    column7.setWidth(30)
    tableColumns.add(column7)

    Column column8 = new StringColumn()
    column8.setAlias("innForeign")
    column8.setName("ИНН Страны гражданства")
    column8.setWidth(10)
    tableColumns.add(column8)

    return tableColumns;
}

/**
 * Поиск справок согласно фильтру
 */
@Field
int counter = 0

def searchData(def params, pageSize, PrepareSpecificReportResult prepareSpecificReportResult) {
    def xmlStr = declarationService.getXmlData(declarationData.id)
    def Файл = new XmlSlurper().parseText(xmlStr)
    def docs = []
    Файл.Документ.each { doc ->
        boolean passed = true
        String idDoc = null
        if (params['idDocNumber'] != null) {
            idDoc = params['idDocNumber'].replaceAll("[\\s-]", "")
        }
        if (params['pNumSpravka'] != null && !StringUtils.containsIgnoreCase(doc.@НомСпр.text(), params['pNumSpravka'])) passed = false
        if (params['lastName'] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Фамилия.text(), params['lastName'])) passed = false
        if (params['firstName'] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Имя.text(), params['firstName'])) passed = false
        if (params['middleName'] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Отчество.text(), params['middleName'])) passed = false
        if (params['inn'] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.@ИННФЛ.text(), params['inn'])) passed = false
        if ((params['fromBirthDay'] != null || params['toBirthDay'] != null) && searchBirthDay(params, doc.ПолучДох.@ДатаРожд.text())) passed = false
        if (params['idDocNumber'] != null && !((StringUtils.containsIgnoreCase(doc.ПолучДох.УдЛичнФЛ.@СерНомДок.text(), idDoc) ||
                StringUtils.containsIgnoreCase(doc.ПолучДох.УдЛичнФЛ.@СерНомДок.text().replaceAll("[\\s-]", ""), idDoc)))) passed = false
        if (passed) docs << doc
    }
    prepareSpecificReportResult.countAvailableDataRows = docs.size()
    // ограничиваем размер выборки
    def result = []
    docs.each {
        if (result.size() < pageSize) {
            result << it
        }
        counter++
    }
    result
}

def searchBirthDay(def params, String birthDate) {
    Date date = ScriptUtils.parseDate(DATE_FORMAT_DOTTED, birthDate)
    if (params['fromBirthDay'] != null && params['toBirthDay'] != null) {
        if (date >= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, params['fromBirthDay']) && date <= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, params['toBirthDay'])) {
            return false
        }
    } else if (params['fromBirthDay'] != null) {
        if (date >= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, params['fromBirthDay'])) {
            return false
        }
    } else if (params['toBirthDay'] != null) {
        if (date <= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, params['toBirthDay'])) {
            return false
        }
    }

    return true
}

/**
 * Создать спецотчет
 * @return
 */
def createSpecificReport() {
    def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
    if (alias == ALIAS_PRIMARY_RNU_W_ERRORS) {
        createPrimaryRnuWithErrors()
        return
    }
    def row = scriptSpecificReportHolder.getSelectedRecord()
    def params = scriptSpecificReportHolder.subreportParamValues ?: new HashMap<String, Object>()
    params['pNumSpravka'] = row.pNumSpravka

    def subReportViewParams = scriptSpecificReportHolder.getViewParamValues()
    subReportViewParams['Номер справки'] = row.pNumSpravka.toString()
    subReportViewParams['Фамилия'] = row.lastName
    subReportViewParams['Имя'] = row.firstName
    subReportViewParams['Отчество'] = row.middleName
    subReportViewParams['Дата рождения'] = row.birthDay ? row.birthDay : ""
    subReportViewParams['№ ДУЛ'] = row.idDocNumber

    def xmlStr = declarationService.getXmlData(declarationData.id)
    def Файл = new XmlSlurper().parseText(xmlStr)
    def xmlPartNumber = 1 + (long) ((new Long(Файл.Документ.find { doc -> true }.@НомСпр.text())) / NUMBER_OF_PERSONS)

    def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
        buildXmlForSpecificReport(it, xmlPartNumber, new Long(row.pNumSpravka))
        it.flush()
    });

    DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    StringBuilder fileName = new StringBuilder(declarationTemplate.name).append("_").append(declarationData.id).append("_").append(row.lastName ?: "").append(" ").append(row.firstName ?: "").append(" ").append(row.middleName ?: "").append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
    declarationService.exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.setFileName(fileName.toString())
}

/**
 * Создать XLSX отчет
 * @return
 */
@TypeChecked
def createXlsxReport() {
    ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) getProperty("scriptSpecificReportHolder")
    def params = new HashMap<String, Object>()
    params.put("declarationId", declarationData.getId());

    JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, declarationService.getXmlStream(declarationData.id));

    StringBuilder fileName = new StringBuilder("Реестр_справок_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
    exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.setFileName(fileName.toString())
}

@TypeChecked
void exportXLSX(JasperPrint jasperPrint, OutputStream data) {
    try {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT,
                jasperPrint);
        exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, data);
        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
                Boolean.TRUE);
        exporter.setParameter(
                JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                Boolean.FALSE);

        exporter.exportReport();
        exporter.reset();
    } catch (Exception e) {
        throw new ServiceException(
                "Невозможно экспортировать отчет в XLSX", e) as Throwable
    }
}

/**
 * Оставляем только необходимые данные для отчета
 */
def filterData(params) {
    def xml = declarationService.getXmlData(declarationData.id)
    def Файл = new XmlParser().parseText(xml)
    Файл.Документ.each { doc ->
        if (doc.@НомСпр != params.pNumSpravka) {
            doc.replaceNode {}
        }
    }
    def result = XmlUtil.serialize(Файл)
    result.replace("windows-1251", "utf-8") // сведения о кодировке должны соответствовать содержимому
}

/**
 * Создать Спецотчет Первичные РНУ с ошибками
 * @return
 */
def createPrimaryRnuWithErrors() {
    // Сведения о доходах из КНФ, которая является источником для входящей ОНФ и записи в реестре справок соответствующим доходам физлицам имеют ошибки
    List<NdflPersonIncome> ndflPersonIncomeFromRNUConsolidatedList = ndflPersonService.findNdflPersonIncomeConsolidatedRNU2Ndfl(declarationData.id, declarationData.kpp, declarationData.oktmo)
    // Сведения о вычетах имеющие такой же operationId как и сведения о доходах
    List<NdflPersonDeduction> ndflPersonDeductionFromRNUConsolidatedList = []
    // Сведения об авансах имеющие такой же operationId как и сведения о доходах
    List<NdflPersonPrepayment> ndflPersonPrepaymentFromRNUConsolidatedList = []

    ndflPersonIncomeFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
        ndflPersonDeductionFromRNUConsolidatedList.addAll(ndflPersonService.findDeductionsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
        ndflPersonPrepaymentFromRNUConsolidatedList.addAll(ndflPersonService.findPrepaymentsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
    }

    ndflPersonIncomeFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
        NdflPersonIncome ndflPersonIncomePrimary = ndflPersonService.getIncome(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonIncomePrimary.ndflPersonId)
        ndflPersonPrimary.incomes.add(ndflPersonIncomePrimary)
    }

    ndflPersonDeductionFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
        NdflPersonDeduction ndflPersonDeductionPrimary = ndflPersonService.getDeduction(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonDeductionPrimary.ndflPersonId)
        ndflPersonPrimary.deductions.add(ndflPersonDeductionPrimary)
    }

    ndflPersonPrepaymentFromRNUConsolidatedList.each {
        ScriptUtils.checkInterrupted();
        NdflPersonPrepayment ndflPersonPrepaymentPrimary = ndflPersonService.getPrepayment(it.sourceId)
        NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonPrepaymentPrimary.ndflPersonId)
        ndflPersonPrimary.prepayments.add(ndflPersonPrepaymentPrimary)
    }
    fillPrimaryRnuWithErrors()
}

/**
 * Заполнение печатного представления спецотчета "Первичные РНУ с ошибками"
 * @return
 */
def fillPrimaryRnuWithErrors() {
    def writer = scriptSpecificReportHolder.getFileOutputStream()
    def workbook = getSpecialReportTemplate()
    fillGeneralData(workbook)
    fillPrimaryRnuNDFLWithErrorsTable(workbook)
    workbook.write(writer)
    writer.close()
    StringBuilder fileName = new StringBuilder("Первичные_РНУ_с_ошибками_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
    scriptSpecificReportHolder.setFileName(fileName.toString())
}

/**
 * Заполнение шапки Спецотчета Первичные РНУ с ошибками
 */
def fillGeneralData(workbook) {
    XSSFSheet sheet = workbook.getSheetAt(0)
    XSSFCellStyle style = makeStyleLeftAligned(workbook)
    DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    // Вид отчетности
    String declarationTypeName = declarationTemplate.type.name
    String note = declarationData.note
    // Период
    int year = departmentReportPeriod.reportPeriod.taxPeriod.year
    String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
            .getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).NAME.value
    // Территориальный банк
    String departmentName = department.name
    // КПП
    String kpp = declarationData.kpp
    //	Дата сдачи корректировки
    String dateDelivery = departmentReportPeriod.correctionDate?.format(DATE_FORMAT_DOTTED)
    // ОКТМО
    String oktmo = declarationData.oktmo
    // Код НО (конечный)
    String taxOrganCode = declarationData.taxOrganCode
    // Дата формирования
    String currentDate = new Date().format(DATE_FORMAT_DOTTED, TimeZone.getTimeZone('Europe/Moscow'))

    XSSFCell cell1 = sheet.getRow(2).createCell(1)
    cell1.setCellValue(declarationTypeName + " " + note)
    cell1.setCellStyle(style)
    XSSFCell cell2 = sheet.getRow(3).createCell(1)
    cell2.setCellValue(year + ":" + periodName)
    cell2.setCellStyle(style)
    XSSFCell cell3 = sheet.getRow(4).createCell(1)
    cell3.setCellValue(dateDelivery)
    cell3.setCellStyle(style)
    XSSFCell cell4 = sheet.getRow(5).createCell(1)
    cell4.setCellValue(departmentName)
    cell4.setCellStyle(style)
    XSSFCell cell5 = sheet.getRow(6).createCell(1)
    cell5.setCellValue(kpp)
    cell5.setCellStyle(style)
    XSSFCell cell6 = sheet.getRow(7).createCell(1)
    cell6.setCellValue(oktmo)
    cell6.setCellStyle(style)
    XSSFCell cell7 = sheet.getRow(8).createCell(1)
    cell7.setCellValue(taxOrganCode)
    cell7.setCellStyle(style)
    XSSFCell cell8 = sheet.getRow(2).createCell(11)
    cell8.setCellValue(currentDate)
    cell8.setCellStyle(style)
}

def initNdflPersonPrimary(ndflPersonId) {
    NdflPerson ndflPersonPrimary = ndflpersonFromRNUPrimary[ndflPersonId]
    if (ndflPersonPrimary == null) {
        ndflPersonPrimary = ndflPersonService.get(ndflPersonId)
        ndflPersonPrimary.incomes.clear()
        ndflPersonPrimary.deductions.clear()
        ndflPersonPrimary.prepayments.clear()
        ndflpersonFromRNUPrimary[ndflPersonId] = ndflPersonPrimary
    }
    return ndflPersonPrimary
}

/**
 * Заполнить таблицу Спецотчета Первичные РНУ с ошибками
 * @param workbook
 * @return
 */
def fillPrimaryRnuNDFLWithErrorsTable(final XSSFWorkbook workbook) {
    XSSFSheet sheet = workbook.getSheetAt(0)
    def startIndex = 12
    ndflpersonFromRNUPrimary.values().each { ndflPerson ->
        ndflPerson.incomes.each { income ->
            ScriptUtils.checkInterrupted();
            fillPrimaryRnuNDFLWithErrorsRow(workbook, ndflPerson, income, "Свед о дох", startIndex)
            startIndex++
        }
    }
}

/**
 * Заполнение строки для таблицы Спецотчета Первичные РНУ с ошибками
 * @param workbook
 * @param ndflPerson
 * @param operation операция отражающая доход, вычет или аванс
 * @param sectionName Название раздела, в котором содержится операция
 * @param index текущий индекс строки таблицы
 * @return
 */
def fillPrimaryRnuNDFLWithErrorsRow(final XSSFWorkbook workbook, ndflPerson, operation, sectionName, index) {
    XSSFSheet sheet = workbook.getSheetAt(0)
    XSSFRow row = sheet.createRow(index)
    def styleLeftAligned = makeStyleLeftAligned(workbook)
    styleLeftAligned = thinBorderStyle(styleLeftAligned)
    def styleCenterAligned = makeStyleCenterAligned(workbook)
    styleCenterAligned = thinBorderStyle(styleCenterAligned)
    styleCenterAligned.setDataFormat(ScriptUtils.createXlsDateFormat(workbook))
    // Первичная НФ
    DeclarationData primaryRnuDeclarationData = declarationService.getDeclarationData(ndflPerson.declarationDataId)
    DeclarationDataFile primaryRnuDeclarationDataFile = declarationService.findFilesWithSpecificType(ndflPerson.declarationDataId, TRANSPORT_FILE_TEMPLATE).get(0)
    DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(primaryRnuDeclarationData.departmentReportPeriodId)
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    // Период
    int year = departmentReportPeriod.reportPeriod.taxPeriod.year
    String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
            .getRecords(getReportPeriodEndDate(primaryRnuDeclarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).NAME.value
    // Подразделение
    String departmentName = department.shortName
    // АСНУ
    String asnu = getProvider(REF_BOOK_ASNU_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${primaryRnuDeclarationData.asnuId}", null).get(0).NAME.value
    // Имя ТФ
    String transportFileName = primaryRnuDeclarationDataFile.fileName
    // Загрузил ТФ
    String userName = primaryRnuDeclarationDataFile.userName
    // Дата загрузки ТФ
    Date uploadDate = primaryRnuDeclarationDataFile.date
    // Строка с ошибкой.Строка
    String rowNum = operation.rowNum.toString()
    // Строка с ошибкой.ID операции
    String operationId = operation.operationId.toString()
    // 	Физическое лицо, к которому относится ошибочная строка. Документ
    String idDocNumber = ndflPerson.idDocNumber
    // Физическое лицо, к которому относится ошибочная строка. ФИО
    String lastname = ndflPerson.lastName != null ? ndflPerson.lastName + " " : ""
    String firstname = ndflPerson.firstName != null ? ndflPerson.firstName + " " : ""
    String middlename = ndflPerson.middleName != null ? ndflPerson.middleName : ""
    String fio = lastname + firstname + middlename
    // Физическое лицо, к которому относится ошибочная строка. Дата рождения
    Date birthDay = ndflPerson.birthDay

    XSSFCell cell1 = row.createCell(0)
    cell1.setCellValue(periodName + ":" + year)
    cell1.setCellStyle(styleCenterAligned)
    XSSFCell cell2 = row.createCell(1)
    cell2.setCellValue(departmentName)
    cell2.setCellStyle(styleLeftAligned)
    XSSFCell cell3 = row.createCell(2)
    cell3.setCellValue(asnu)
    cell3.setCellStyle(styleCenterAligned)
    XSSFCell cell4 = row.createCell(3)
    cell4.setCellValue(transportFileName)
    cell4.setCellStyle(styleLeftAligned)
    XSSFCell cell5 = row.createCell(4)
    cell5.setCellValue(userName)
    cell5.setCellStyle(styleCenterAligned)
    XSSFCell cell6 = row.createCell(5)
    cell6.setCellValue(uploadDate)
    cell6.setCellStyle(styleCenterAligned)
    XSSFCell cell7 = row.createCell(6)
    cell7.setCellValue(sectionName)
    cell7.setCellStyle(styleCenterAligned)
    XSSFCell cell8 = row.createCell(7)
    cell8.setCellValue(rowNum)
    cell8.setCellStyle(styleCenterAligned)
    XSSFCell cell9 = row.createCell(8)
    cell9.setCellValue(operationId)
    cell9.setCellStyle(styleCenterAligned)
    XSSFCell cell10 = row.createCell(9)
    cell10.setCellValue(idDocNumber)
    cell10.setCellStyle(styleCenterAligned)
    XSSFCell cell11 = row.createCell(10)
    cell11.setCellValue(fio)
    cell11.setCellStyle(styleCenterAligned)
    XSSFCell cell12 = row.createCell(11)
    cell12.setCellValue(birthDay)
    cell12.setCellStyle(styleCenterAligned)
}

// Находит в базе данных шаблон спецотчета по физическому лицу и возвращает его
def getSpecialReportTemplate() {
    def blobData = blobDataServiceDaoImpl.get(scriptSpecificReportHolder.getDeclarationSubreport().getBlobDataId())
    new XSSFWorkbook(blobData.getInputStream())
}

/**
 * Создать стиль ячейки с выравниваем слева
 * @param workbook
 * @return
 */

def makeStyleLeftAligned(XSSFWorkbook workbook) {
    def XSSFCellStyle style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_LEFT)
    return style
}

/**
 * Создать стиль ячейки с выравниваем по центру
 * @param workbook
 * @return
 */

def makeStyleCenterAligned(XSSFWorkbook workbook) {
    def XSSFCellStyle style = workbook.createCellStyle()
    style.setAlignment(CellStyle.ALIGN_CENTER)
    return style
}

/**
 * Добавляет к стилю ячейки тонкие границы
 * @param style
 * @return
 */
def thinBorderStyle(final style) {
    style.setBorderTop(CellStyle.BORDER_THIN)
    style.setBorderBottom(CellStyle.BORDER_THIN)
    style.setBorderLeft(CellStyle.BORDER_THIN)
    style.setBorderRight(CellStyle.BORDER_THIN)
    return style
}

@TypeChecked
void departmentParamException(int departmentId, int reportPeriodId) {
    ReportPeriod reportPeriod = reportPeriodService.get(reportPeriodId)
    throw new ServiceException("Отсутствуют настройки подразделения \"%s\" периода \"%s\". Необходимо выполнить настройку в разделе меню \"Налоги->НДФЛ->Настройки подразделений\"",
            departmentService.get(departmentId).getName(),
            reportPeriod.getTaxPeriod().getYear() + ", " + reportPeriod.getName()
    ) as Throwable
}

//******************************* ПРОВЕРКИ *****************************************************************************

@Field
final String FILE_NODE = "Файл"
@Field
final String DOCUMENT_NODE = "Документ";
@Field
final String SVEDDOH_NODE = "СведДох";
@Field
final String SV_SUM_DOH = "СвСумДох";
@Field
final String SV_SUM_VICH = "СвСумВыч";
@Field
final String PRED_VICH_SSI = "ПредВычССИ";
@Field
final String LAST_NAME = "Фамилия"
@Field
final String FIRST_NAME = "Имя"
@Field
final String MIDDLE_NAME = "Отчество"
@Field
final String NUM_SPR = "НомСпр"
@Field
final String INN_SPR = "ИННФЛ"
@Field
final String CITIZENSHIP_CODE = "Гражд"
@Field
final String ID_DOC_TYPE = "КодУдЛичн"
@Field
final String ID_DOC_NUMBER = "СерНомДок"
@Field
final String TAX_RATE = "Ставка"
@Field
final String CALCULATED_TAX = "НалИсчисл"
@Field
final String TAX_SUM = "НалПеречисл"
@Field
final String WITHHOLDING_TAX = "НалУдерж"
@Field
final String PREPAYMENT_SUM = "АвансПлатФикс"
@Field
final String INCOME_SUM_COMMON = "СумДохОбщ"
@Field
final String SUM_DOHOD = "СумДоход"
@Field
final String INCOME_CODE = "КодДоход"
@Field
final String DEDUCTION_CODE = "КодВычет"
@Field
final String DEDUCTION_SUM = "СумВычет"
@Field
final String TAX_BASE = "НалБаза"
@Field
final String NOT_HOLDING_TAX = "НалНеУдерж"
@Field
final String NODE_NAME_SUM_IT_NAL_PER2 = "СумИтНалПер"
@Field
final String TAX_ORGAN_CODE = "КодНО"
@Field
final String UVED_SOTS_VICH = "УведСоцВыч"
@Field
final String UVED_IMUSCH_VICH = "УведИмущВыч"
@Field
final String IFNS_UVED = "ИФНСУвед"

// Узлы 6 НДФЛ
@Field final NODE_NAME_SUM_STAVKA6 = "СумСтавка"
@Field final NODE_NAME_OBOBSH_POKAZ6 = "ОбобщПоказ"
@Field final NODE_NAME_SUM_DATA6 = "СумДата"
// Атрибуты 6 НДФЛ
@Field final ATTR_NACHISL_DOH6 = "НачислДох"
@Field final ATTR_NACHISL_DOH_DIV6 = "НачислДохДив"
@Field final ATTR_VICHET_NAL6 = "ВычетНал"
@Field final ATTR_ISCHISL_NAL6 = "ИсчислНал"
@Field final ATTR_NE_UDERZ_NAL_IT6 = "НеУдержНалИт"
@Field final ATTR_KOL_FL_DOHOD6 = "КолФЛДоход"
@Field final ATTR_AVANS_PLAT6 = "АвансПлат"

/**
 * Получить "Коды налоговых органов"
 * @return
 */
def getRefNotifSource() {
    def refBookList = getProvider(RefBook.Id.TAX_INSPECTION.id).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, null, null)
    return refBookList.CODE?.stringValue
}


def check() {
    //List<DeclarationData> declarationDataList = declarationService.find(declarationData.declarationTemplateId, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp, declarationData.oktmo);
    if (false) {
        logger.warnExp();
    }
    ScriptUtils.checkInterrupted()
    ZipInputStream xmlStream = declarationService.getXmlStream(declarationData.id)

    // Парсим xml и компонуем содержимое в группу объектов со структурой дерева из узлов и листьев
    def fileNode = new XmlSlurper().parse(xmlStream)
    Ndfl2Node fileNdfl2Node = new Ndfl2Node(FILE_NODE);
    def documentNodes = fileNode.depthFirst().grep {
        it.name() == DOCUMENT_NODE
    }

    documentNodes.each { docNodeItem ->
        Ndfl2Node documentNdfl2Node = new Ndfl2Node(DOCUMENT_NODE)
        Ndfl2Leaf<String> lastNameLeaf = new Ndfl2Leaf<>(LAST_NAME, docNodeItem.ПолучДох.ФИО.@Фамилия.text(), String.class)
        Ndfl2Leaf<String> firstNameLeaf = new Ndfl2Leaf<>(FIRST_NAME, docNodeItem.ПолучДох.ФИО.@Имя.text(), String.class)
        Ndfl2Leaf<String> middleNameLeaf = new Ndfl2Leaf<>(MIDDLE_NAME, docNodeItem.ПолучДох.ФИО.@Отчество.text(), String.class)
        Ndfl2Leaf<String> numSprLeaf = new Ndfl2Leaf<>(NUM_SPR, docNodeItem.@НомСпр.text(), Integer.class)
        Ndfl2Leaf<String> innLeaf = new Ndfl2Leaf<>(INN_SPR, docNodeItem.ПолучДох.@ИННФЛ.text(), String.class)
        Ndfl2Leaf<String> citizenshipLeaf = new Ndfl2Leaf<>(CITIZENSHIP_CODE, docNodeItem.ПолучДох.@Гражд.text(), String.class)
        Ndfl2Leaf<String> idDocTypeLeaf = new Ndfl2Leaf<>(ID_DOC_TYPE, docNodeItem.ПолучДох.УдЛичнФЛ.@КодУдЛичн.text(), String.class)
        Ndfl2Leaf<String> idDocNumberLeaf = new Ndfl2Leaf<>(ID_DOC_NUMBER, docNodeItem.ПолучДох.УдЛичнФЛ.@СерНомДок.text(), String.class)
        Ndfl2Leaf<String> taxOrganCodeLeaf = new Ndfl2Leaf<>(TAX_ORGAN_CODE, docNodeItem.@КодНО.text(), String.class)
        documentNdfl2Node.addLeaf(lastNameLeaf)
        documentNdfl2Node.addLeaf(firstNameLeaf)
        documentNdfl2Node.addLeaf(middleNameLeaf)
        documentNdfl2Node.addLeaf(numSprLeaf)
        documentNdfl2Node.addLeaf(innLeaf)
        documentNdfl2Node.addLeaf(citizenshipLeaf)
        documentNdfl2Node.addLeaf(idDocTypeLeaf)
        documentNdfl2Node.addLeaf(idDocNumberLeaf)
        documentNdfl2Node.addLeaf(taxOrganCodeLeaf)
        def svedDohNodes = docNodeItem.depthFirst().grep {
            it.name() == SVEDDOH_NODE
        }
        svedDohNodes.each { svedDohNodeItem ->
            Ndfl2Node svedDohNdfl2Node = new Ndfl2Node(SVEDDOH_NODE)
            Ndfl2Leaf<Integer> taxRateLeaf = new Ndfl2Leaf<>(TAX_RATE, svedDohNodeItem.@Ставка.text(), Integer.class)
            Ndfl2Leaf<BigDecimal> calculatedTaxLeaf = new Ndfl2Leaf<>(CALCULATED_TAX, svedDohNodeItem.СумИтНалПер.@НалИсчисл.text(), BigDecimal.class)
            Ndfl2Leaf<BigDecimal> taxSummLeaf = new Ndfl2Leaf<>(TAX_SUM, svedDohNodeItem.СумИтНалПер.@НалПеречисл.text(), BigDecimal.class)
            Ndfl2Leaf<BigDecimal> withholdingTaxLeaf = new Ndfl2Leaf<>(WITHHOLDING_TAX, svedDohNodeItem.СумИтНалПер.@НалУдерж.text(), BigDecimal.class)
            Ndfl2Leaf<Long> prepaymentSumLeaf = new Ndfl2Leaf<>(PREPAYMENT_SUM, svedDohNodeItem.СумИтНалПер.@АвансПлатФикс.text(), Long.class)
            Ndfl2Leaf<BigDecimal> incomeSumCommonLeaf = new Ndfl2Leaf<>(INCOME_SUM_COMMON, svedDohNodeItem.СумИтНалПер.@СумДохОбщ.text(), BigDecimal.class)
            Ndfl2Leaf<BigDecimal> taxBaseLeaf = new Ndfl2Leaf<>(TAX_BASE, svedDohNodeItem.СумИтНалПер.@НалБаза.text(), BigDecimal.class)
            Ndfl2Leaf<BigDecimal> notHoldingTaxLeaf = new Ndfl2Leaf<>(NOT_HOLDING_TAX, svedDohNodeItem.СумИтНалПер.@НалНеУдерж.text(), BigDecimal.class)
            Ndfl2Leaf<String> uvedFixPlatTaxOrganCode = new Ndfl2Leaf<>(IFNS_UVED, svedDohNodeItem.СумИтНалПер.УведФиксПлат.@ИФНСУвед.text(), String.class)
            svedDohNdfl2Node.addLeaf(taxRateLeaf)
            svedDohNdfl2Node.addLeaf(calculatedTaxLeaf)
            svedDohNdfl2Node.addLeaf(taxSummLeaf)
            svedDohNdfl2Node.addLeaf(withholdingTaxLeaf)
            svedDohNdfl2Node.addLeaf(prepaymentSumLeaf)
            svedDohNdfl2Node.addLeaf(incomeSumCommonLeaf)
            svedDohNdfl2Node.addLeaf(taxBaseLeaf)
            svedDohNdfl2Node.addLeaf(notHoldingTaxLeaf)
            svedDohNdfl2Node.addLeaf(uvedFixPlatTaxOrganCode)
            def svSumDoh = svedDohNodeItem.ДохВыч.depthFirst().grep {
                it.name() == SV_SUM_DOH
            }
            svSumDoh.each { svSumDohItem ->
                Ndfl2Node svSumDohNdfl2Node = new Ndfl2Node(SV_SUM_DOH)
                Ndfl2Leaf<BigDecimal> incomeSumLeaf = new Ndfl2Leaf<>(SUM_DOHOD, svSumDohItem.@СумДоход.text(), BigDecimal.class)
                Ndfl2Leaf<String> incomeCodeLeaf = new Ndfl2Leaf<>(INCOME_CODE, svSumDohItem.@КодДоход.text(), String.class)
                svSumDohNdfl2Node.addLeaf(incomeSumLeaf)
                svSumDohNdfl2Node.addLeaf(incomeCodeLeaf)
                def svSumVich = svSumDohItem.depthFirst().grep {
                    it.name() == SV_SUM_VICH
                }
                svSumVich.each { svSumVichItem ->
                    Ndfl2Node svSumVichNdfl2Node = new Ndfl2Node(SV_SUM_VICH)
                    Ndfl2Leaf<BigDecimal> deductionSumLeaf = new Ndfl2Leaf<>(DEDUCTION_SUM, svSumVichItem.@СумВычет.text(), BigDecimal.class)
                    Ndfl2Leaf<String> deductionCodeLeaf = new Ndfl2Leaf<>(DEDUCTION_CODE, svSumVichItem.@КодВычет.text(), String.class)
                    svSumVichNdfl2Node.addLeaf(deductionSumLeaf)
                    svSumVichNdfl2Node.addLeaf(deductionCodeLeaf)
                    svSumDohNdfl2Node.addChild(svSumVichNdfl2Node)
                }
                svedDohNdfl2Node.addChild(svSumDohNdfl2Node)
            }

            def predVichSSI = svedDohNodeItem.НалВычССИ.depthFirst().grep {
                it.name() == PRED_VICH_SSI
            }
            def uvedSotsVich = svedDohNodeItem.НалВычССИ.depthFirst().grep {
                it.name() == UVED_SOTS_VICH
            }
            def uvedImuschVich = svedDohNodeItem.НалВычССИ.depthFirst().grep {
                it.name() == UVED_IMUSCH_VICH
            }
            predVichSSI.each { predVichSSIItem ->
                Ndfl2Node predVichSSINdfl2Node = new Ndfl2Node(PRED_VICH_SSI)
                Ndfl2Leaf<BigDecimal> predVichSSIDeductionSumLeaf = new Ndfl2Leaf<>(DEDUCTION_SUM, predVichSSIItem.@СумВычет.text(), BigDecimal.class)
                predVichSSINdfl2Node.addLeaf(predVichSSIDeductionSumLeaf)
                svedDohNdfl2Node.addChild(predVichSSINdfl2Node)
            }
            uvedSotsVich.each { uvedSotsVichItem ->
                Ndfl2Node uvedSotsVichNdfl2Node = new Ndfl2Node(UVED_SOTS_VICH)
                Ndfl2Leaf<String> taxOrganCode = new Ndfl2Leaf<>(IFNS_UVED, uvedSotsVichItem.@ИФНСУвед.text(), String.class)
                uvedSotsVichNdfl2Node.addLeaf(taxOrganCode)
                svedDohNdfl2Node.addChild(uvedSotsVichNdfl2Node)
            }
            uvedImuschVich.each { uvedImuschVichItem ->
                Ndfl2Node uvedImuschVichNdfl2Node = new Ndfl2Node(UVED_IMUSCH_VICH)
                Ndfl2Leaf<String> taxOrganCode = new Ndfl2Leaf<>(IFNS_UVED, uvedImuschVichItem.@ИФНСУвед.text(), String.class)
                uvedImuschVichNdfl2Node.addLeaf(taxOrganCode)
                svedDohNdfl2Node.addChild(uvedImuschVichNdfl2Node)
            }
            documentNdfl2Node.addChild(svedDohNdfl2Node)
        }
        fileNdfl2Node.addChild(documentNdfl2Node)
    }
    // Общие проверки
    Checker commonChecker = new CommonChecker(fileNdfl2Node)
    commonChecker.check(logger)

    // Внутридокументарные проверки инкапсулированы в классы реализующие интерфейс Checker, в конструктор передается корневой узел созданного дерева
    Checker calculatedTaxChecker = new CalculatedTaxChecker(fileNdfl2Node)
    calculatedTaxChecker.check(logger)
    Checker taxSummAndWithHoldingTaxChecker = new TaxSummAndWithHoldingTaxChecker(fileNdfl2Node)
    taxSummAndWithHoldingTaxChecker.check(logger)
    Checker calculatedTaxPrepaymentChecker = new CalculatedTaxPrepaymentChecker(fileNdfl2Node)
    calculatedTaxPrepaymentChecker.check(logger)
    Checker commonIncomeSumChecker = new CommonIncomeSumChecker(fileNdfl2Node)
    commonIncomeSumChecker.check(logger)
    Checker incomeSumAndDeductionChecker = new IncomeSumAndDeductionChecker(fileNdfl2Node)
    incomeSumAndDeductionChecker.check(logger)
    Checker taxOrganChecker = new TaxOrganChecker(fileNdfl2Node, getRefNotifSource())
    taxOrganChecker.check(logger)
    if (declarationData.declarationTemplateId == NDFL_2_2_DECLARATION_TYPE) {
        Checker notHoldingTaxChecker = new NotHoldingTaxChecker(fileNdfl2Node)
        notHoldingTaxChecker.check(logger)
        Checker withHoldingTaxChecker = new WithHoldingTaxChecker(fileNdfl2Node)
        withHoldingTaxChecker.check(logger)
    }
    // Междокументарные проверки
    if (declarationData.declarationTemplateId == NDFL_2_1_DECLARATION_TYPE) {
        interdocumentaryCheckData()
    }
}

/**
 * Компонент дерева представляющий узел
 */
class Ndfl2Node {
    String name;
    List<Ndfl2Node> childNodes = new ArrayList<>()
    List<Ndfl2Leaf<?>> attributes = new ArrayList<>()

    Ndfl2Node(String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    void addChild(Ndfl2Node ndfl2Node) {
        childNodes.add(ndfl2Node)
    }

    void addLeaf(Ndfl2Leaf<?> ndfl2Leaf) {
        attributes.add(ndfl2Leaf)
    }

    List<Ndfl2Node> getChildNodes() {
        return childNodes
    }

    List<Ndfl2Leaf<?>> getAttributes() {
        return attributes
    }
}

/**
 * Компонент дерева представляющий атрибут
 * @param < T >      - Класс значения атрибута из xml, с которым будем работать
 */
class Ndfl2Leaf<T> {
    String name;
    T value;

    Ndfl2Leaf(String name, String value, Class<T> clazz) {
        this.name = name
        if (value == null) {
            this.value == null
        } else if (clazz == BigDecimal.class) {
            this.value = value ? new BigDecimal(value) : null
        } else if (clazz == Integer.class) {
            this.value = value ? Integer.valueOf(value) : null
        } else if (clazz == Long.class) {
            this.value = value ? Long.valueOf(value) : null
        } else if (clazz == String.class) {
            this.value = value ?: ""
        } else {
            this.value = null
        }
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    T getValue() {
        return value
    }

    void setValue(T value) {
        this.value = value
    }


    @Override
    public String toString() {
        return value.toString()
    }
}

/**
 * Интерфейс объявляющий метод проверки
 */
interface Checker {
    void check(Logger logger)
}

/**
 * Абстрактная реализация интерфейса Checker
 */
abstract class AbstractChecker implements Checker {
    final String LAST_NAME = "Фамилия"
    final String FIRST_NAME = "Имя"
    final String MIDDLE_NAME = "Отчество"
    final String NUM_SPR = "НомСпр"
    final String INN_FL = "ИННФЛ"
    final String CITIZENSHIP_CODE = "Гражд"
    final String ID_DOC_TYPE = "КодУдЛичн"
    final String ID_DOC_NUMBER = "СерНомДок"
    final String TAX_RATE = "Ставка"
    final String SVEDDOH_NODE = "СведДох"
    final String SV_SUM_DOH = "СвСумДох"
    final String INCOME_CODE = "КодДоход"
    final String DEDUCTION_CODE = "КодВычет"
    final String SUM_DOHOD = "СумДоход"
    final String SV_SUM_VICH = "СвСумВыч"
    final String DEDUCTION_SUM = "СумВычет"
    final String PRED_VICH_SSI = "ПредВычССИ";
    final String CALCULATED_TAX = "НалИсчисл"
    final String DOCUMENT_NODE = "Документ";
    final String TAX_BASE = "НалБаза"
    final String WITHHOLDING_TAX = "НалУдерж"
    final String TAX_SUM = "НалПеречисл"
    final String PREPAYMENT_SUM = "АвансПлатФикс"
    final String INCOME_SUM_COMMON = "СумДохОбщ"
    final String NOT_HOLDING_TAX = "НалНеУдерж"
    final String TAX_ORGAN_CODE = "КодНО"
    final String UVED_SOTS_VICH = "УведСоцВыч"
    final String UVED_IMUSCH_VICH = "УведИмущВыч"
    final String IFNS_UVED = "ИФНСУвед"

    Ndfl2Node headNode;

    List<String> taxOrganCodeList;

    AbstractChecker(Ndfl2Node headNode) {
        this.headNode = headNode
    }

    AbstractChecker(Ndfl2Node headNode, List<String> taxOrganCodeList) {
        this.headNode = headNode
        this.taxOrganCodeList = taxOrganCodeList
    }

    List<Ndfl2Node> extractNdfl2Nodes(String name, Ndfl2Node parentNode) {
        List<Ndfl2Node> toReturn = new ArrayList<>()
        for (Ndfl2Node node : parentNode.getChildNodes()) {
            if (node.getName() == name) {
                toReturn.add(node)
            }
        }
        return toReturn
    }

    Ndfl2Leaf<?> extractAttribute(String name, Ndfl2Node node) {
        for (Ndfl2Leaf<?> attribute : node.getAttributes()) {
            if (attribute.getName() == name) {
                return attribute
            }
        }
        return null
    }

    void createErrorMessage(Logger logger, Ndfl2Node documentNode, String type, String message) {
        Ndfl2Leaf<String> lastNameAttribute = extractAttribute(LAST_NAME, documentNode)
        Ndfl2Leaf<String> firstNameAttribute = extractAttribute(FIRST_NAME, documentNode)
        Ndfl2Leaf<String> middleNameAttribute = extractAttribute(MIDDLE_NAME, documentNode)
        Ndfl2Leaf<String> numSprAttribute = extractAttribute(NUM_SPR, documentNode)
        StringBuilder fioAndNumSpr = new StringBuilder(lastNameAttribute.getValue() ? (String) lastNameAttribute.getValue() : "")
                .append(" ")
                .append(firstNameAttribute.getValue() ? (String) firstNameAttribute.getValue() : "")
                .append(" ")
                .append(middleNameAttribute.getValue() ? (String) middleNameAttribute.getValue() : "")
                .append(", Номер справки: ")
                .append(numSprAttribute.getValue()?.toString())
        logger.errorExp(message, type, fioAndNumSpr.toString())
    }

    void createWarnMessage(Logger logger, Ndfl2Node documentNode, String type, String message) {
        Ndfl2Leaf<String> lastNameAttribute = extractAttribute(LAST_NAME, documentNode)
        Ndfl2Leaf<String> firstNameAttribute = extractAttribute(FIRST_NAME, documentNode)
        Ndfl2Leaf<String> middleNameAttribute = extractAttribute(MIDDLE_NAME, documentNode)
        Ndfl2Leaf<String> numSprAttribute = extractAttribute(NUM_SPR, documentNode)
        StringBuilder fioAndNumSpr = new StringBuilder(lastNameAttribute.getValue() ? (String) lastNameAttribute.getValue() : "")
                .append(" ")
                .append(firstNameAttribute.getValue() ? (String) firstNameAttribute.getValue() : "")
                .append(" ")
                .append(middleNameAttribute.getValue() ? (String) middleNameAttribute.getValue() : "")
                .append(", Номер справки: ")
                .append(numSprAttribute.getValue()?.toString())
        logger.warnExp(message, type, fioAndNumSpr.toString())
    }

}

/**
 *  Справочник Налоговые инспекции
 */
class TaxOrganChecker extends AbstractChecker {

    TaxOrganChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    TaxOrganChecker(Ndfl2Node headNode, List<String> taxOrganCodeList) {
        super(headNode, taxOrganCodeList)
    }

    void check(Logger logger) {
        def taxInspectionList = taxOrganCodeList
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            String documentIfns = extractAttribute(TAX_ORGAN_CODE, documentNode)?.getValue()
            if (documentIfns != null && documentIfns != "" && !taxInspectionList.contains(documentIfns)) {
                createErrorMessage(logger, documentNode, "Значение не соответствует справочнику \"Налоговые инспекции\"", "Значение параметра \"Файл.Документ.КодНО\" (\"$documentIfns\") отсутствует в справочнике \"Налоговые инспекции\".")
            }
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                String uvedFixPlatIfns = extractAttribute(IFNS_UVED, svedDohNode)?.getValue()
                if (uvedFixPlatIfns != null && uvedFixPlatIfns != "" && !taxInspectionList.contains(uvedFixPlatIfns)) {
                    createErrorMessage(logger, documentNode, "Значение не соответствует справочнику \"Налоговые инспекции\"", "Значение параметра \"Файл.Документ.СведДох.СумИтНалПер.УведФиксПлат.ИФНСУвед\" (\"$uvedFixPlatIfns\") отсутствует в справочнике \"Налоговые инспекции\".")
                }
                def uvedSotsVichNodeList = extractNdfl2Nodes(UVED_SOTS_VICH, svedDohNode)
                def uvedImuschVichNodeList = extractNdfl2Nodes(UVED_IMUSCH_VICH, svedDohNode)
                for (Ndfl2Node uvedSotsVichNode : uvedSotsVichNodeList) {
                    String uvedSotsVichIfns = extractAttribute(IFNS_UVED, uvedSotsVichNode)?.getValue()
                    if (uvedSotsVichIfns != null && uvedSotsVichIfns != "" && !taxInspectionList.contains(uvedSotsVichIfns)) {
                        createErrorMessage(logger, documentNode, "Значение не соответствует справочнику \"Налоговые инспекции\"", "Значение параметра \"Файл.Документ.СведДох.НалВычССИ.УведСоцВыч.ИФНСУвед\" (\"$uvedSotsVichIfns\") отсутствует в справочнике \"Налоговые инспекции\".")
                    }
                }
                for (Ndfl2Node uvedImuschVichNode : uvedImuschVichNodeList) {
                    String uvedImuschVichIfns = extractAttribute(IFNS_UVED, uvedImuschVichNode)?.getValue()
                    if (uvedImuschVichIfns != null && uvedImuschVichIfns != "" && !taxInspectionList.contains(uvedImuschVichIfns)) {
                        createErrorMessage(logger, documentNode, "Значение не соответствует справочнику \"Налоговые инспекции\"", "Значение параметра \"Файл.Документ.СведДох.НалВычССИ.УведИмущВыч.ИФНСУвед\" (\"$uvedImuschVichIfns\") отсутствует в справочнике \"Налоговые инспекции\".")
                    }
                }
            }
        }
    }
}

/**
 * Общие проверки
 */
@CompileStatic
class CommonChecker extends AbstractChecker {

    CommonChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            String citizenship = extractAttribute(CITIZENSHIP_CODE, documentNode).getValue()
            if (citizenship == "643") {
                String inn = extractAttribute(INN_FL, documentNode).getValue()
                String lastName = extractAttribute(LAST_NAME, documentNode).getValue()
                String firstName = extractAttribute(FIRST_NAME, documentNode).getValue()
                if (inn == null || inn.trim() == "") {
                    createWarnMessage(logger, documentNode, "Проверка заполнения поля ИНН в РФ у гражданина РФ", "Значение гр. \"ИНН в РФ\" не указано. Прием налоговым органом обеспечивается, может быть предупреждение.")
                } else {
                    String checkInn = ScriptUtils.checkInn(inn)
                    if (checkInn != null) {
                        createErrorMessage(logger, documentNode, "Проверка корректности ИНН ФЛ", checkInn)
                    }
                }
                String checkName = ScriptUtils.checkName(lastName, "Фамилия")
                if (checkName != null) {
                    createErrorMessage(logger, documentNode, "Фамилия, Имя не соответствует формату", checkName)
                }
                checkName = ScriptUtils.checkName(firstName, "Имя")
                if (checkName != null) {
                    createErrorMessage(logger, documentNode, "Фамилия, Имя не соответствует формату", checkName)
                }
            }
            ID_DOC_CHECK:
            {
                String idDocType = extractAttribute(ID_DOC_TYPE, documentNode).getValue()
                String idDocNumber = extractAttribute(ID_DOC_NUMBER, documentNode).getValue()
                String checkDul = ScriptUtils.checkDul(idDocType, idDocNumber, "Документ удостоверяющий личность.Номер")
                if (checkDul != null) {
                    createErrorMessage(logger, documentNode, "Проверка соответствия Серии и Номера ДУЛ формату", checkDul)
                }
            }
        }
    }
}

/**
 * п.4 Проверка расчета суммы исчисленного налога
 */
class CalculatedTaxChecker extends AbstractChecker {

    CalculatedTaxChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                Ndfl2Leaf<Integer> taxRateAttribute = extractAttribute(TAX_RATE, svedDohNode)
                if (taxRateAttribute != null && (taxRateAttribute.getValue() == 13 || taxRateAttribute.getValue() == 15)) {
                    List<Ndfl2Node> svSumDohList = extractNdfl2Nodes(SV_SUM_DOH, svedDohNode)
                    // Сумма разностей Файл.Документ.СведДох.ДохВыч.СвСумДох.СумДоход - Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                    BigDecimal differenceTotalSumDohSumVichForCode1010 = new BigDecimal(0)
                    BigDecimal differenceTotalSumDohSumVichForCodeNot1010 = new BigDecimal(0)
                    for (Ndfl2Node svSumDoh : svSumDohList) {
                        Ndfl2Leaf<String> incomeCodeAttribute = extractAttribute(INCOME_CODE, svSumDoh)
                        if (incomeCodeAttribute != null && incomeCodeAttribute.getValue() == "1010") {
                            Ndfl2Leaf<BigDecimal> incomeSumAttribute = extractAttribute(SUM_DOHOD, svSumDoh)
                            List<Ndfl2Node> svSumVichNodeList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                            // Сумма значений Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                            BigDecimal deductionSumValue = new BigDecimal(0)
                            for (Ndfl2Node svSumVich : svSumVichNodeList) {
                                Ndfl2Leaf<BigDecimal> sumVichAttribute = extractAttribute(DEDUCTION_SUM, svSumVich)
                                deductionSumValue = deductionSumValue.add(sumVichAttribute.getValue())
                            }
                            //Разность между доходом и вычетом
                            BigDecimal differenceSumDohSumVich = incomeSumAttribute?.getValue().subtract(deductionSumValue)
                            if (differenceSumDohSumVich < new BigDecimal(0)) {
                                differenceSumDohSumVich = new BigDecimal(0)
                            }
                            differenceTotalSumDohSumVichForCode1010 = differenceTotalSumDohSumVichForCode1010.add(differenceSumDohSumVich)
                        } else {
                            Ndfl2Leaf<BigDecimal> incomeSumAttribute = extractAttribute(SUM_DOHOD, svSumDoh)
                            List<Ndfl2Node> svSumVichNodeList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                            // Сумма значений Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                            BigDecimal deductionSumValue = new BigDecimal(0)
                            for (Ndfl2Node svSumVich : svSumVichNodeList) {
                                Ndfl2Leaf<BigDecimal> sumVichAttribute = extractAttribute(DEDUCTION_SUM, svSumVich)
                                deductionSumValue = deductionSumValue.add(sumVichAttribute.getValue())
                            }
                            //Разность между доходом и вычетом
                            BigDecimal differenceSumDohSumVich = incomeSumAttribute.getValue().subtract(deductionSumValue)
                            if (differenceSumDohSumVich < new BigDecimal(0)) {
                                differenceSumDohSumVich = new BigDecimal(0)
                            }
                            differenceTotalSumDohSumVichForCodeNot1010 = differenceTotalSumDohSumVichForCodeNot1010.add(differenceSumDohSumVich)
                            List<Ndfl2Node> predVichSSINodeList = extractNdfl2Nodes(PRED_VICH_SSI, svedDohNode)
                            // Сумма Файл.Документ.СведДох.НалВычССИ.ПредВычССИ.СумВычет
                            BigDecimal sumVich = new BigDecimal(0)
                            for (Ndfl2Node predVichSSI : predVichSSINodeList) {
                                Ndfl2Leaf<BigDecimal> vich = extractAttribute(DEDUCTION_SUM, predVichSSI)
                                BigDecimal valueVich = (BigDecimal) vich.getValue()
                                sumVich = sumVich.add(valueVich)
                            }
                            differenceTotalSumDohSumVichForCodeNot1010 = differenceTotalSumDohSumVichForCodeNot1010.subtract(sumVich)
                        }
                    }
                    //Результат для п.4 Проверка расчета суммы исчисленного налога I.1
                    BigDecimal calclulateTaxCheckValueForIncomeCode1010 = differenceTotalSumDohSumVichForCode1010.multiply(new BigDecimal(taxRateAttribute.getValue())).divide(new BigDecimal(100))
                    //Результат для п.4 Проверка расчета суммы исчисленного налога I.2
                    BigDecimal calclulateTaxCheckValueForIncomeCodeNot1010 = differenceTotalSumDohSumVichForCodeNot1010.multiply(new BigDecimal(taxRateAttribute.getValue())).divide(new BigDecimal(100))
                    //Результат для п.4 Проверка расчета суммы исчисленного налога I.3
                    BigDecimal calculatedTaxCheckSum = calclulateTaxCheckValueForIncomeCode1010.add(calclulateTaxCheckValueForIncomeCodeNot1010)
                    Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = extractAttribute(CALCULATED_TAX, svedDohNode)
                    BigDecimal differenceCalculatedTaxAndCalculatedTaxCheckSum = calculatedTaxAttribute.getValue().subtract(ScriptUtils.round(calculatedTaxCheckSum, 0))
                    if (differenceCalculatedTaxAndCalculatedTaxCheckSum.abs() > 1) {
                        createErrorMessage(logger, documentNode, "«Сумма налога исчисленная» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" «Сумма налога исчисленная» ${calculatedTaxAttribute.getValue()} должна быть равна произведению «Ставки» ${taxRateAttribute.getValue()} и «Налоговой базы» ${extractAttribute(TAX_BASE, svedDohNode).getValue()}.")
                    }
                } else if (taxRateAttribute != null && taxRateAttribute.getValue() == 30) {
                    Ndfl2Leaf<BigDecimal> taxBaseAttribute = extractAttribute(TAX_BASE, svedDohNode)
                    BigDecimal calculatedTaxCheckSum = taxBaseAttribute.getValue().multiply(new BigDecimal(taxRateAttribute.getValue())).divide(new BigDecimal(100))
                    Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = extractAttribute(CALCULATED_TAX, svedDohNode)
                    BigDecimal differenceCalculatedTaxAndCalculatedTaxCheckSum = calculatedTaxAttribute.getValue().subtract(ScriptUtils.round(calculatedTaxCheckSum, 0))
                    if (differenceCalculatedTaxAndCalculatedTaxCheckSum.abs() > 1) {
                        createErrorMessage(logger, documentNode, "«Сумма налога исчисленная» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" «Сумма налога исчисленная» ${calculatedTaxAttribute.getValue()} должна быть равна произведению «Ставки» ${taxRateAttribute.getValue()} и «Налоговой базы» ${taxBaseAttribute.getValue()}.")
                    }
                } else {
                    List<Ndfl2Node> svSumDohList = extractNdfl2Nodes(SV_SUM_DOH, svedDohNode)
                    BigDecimal differenceTotalSumDohSumVich = new BigDecimal(0)
                    for (Ndfl2Node svSumDoh : svSumDohList) {
                        Ndfl2Leaf<BigDecimal> incomeSumAttribute = extractAttribute(SUM_DOHOD, svSumDoh)
                        List<Ndfl2Node> svSumVichNodeList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                        // Сумма значений Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                        BigDecimal deductionSumValue = new BigDecimal(0)
                        for (Ndfl2Node svSumVich : svSumVichNodeList) {
                            Ndfl2Leaf<BigDecimal> sumVichAttribute = extractAttribute(DEDUCTION_SUM, svSumVich)
                            deductionSumValue.add(sumVichAttribute.getValue())
                        }
                        //Разность между доходом и вычетом
                        BigDecimal differenceSumDohSumVich = incomeSumAttribute.getValue().subtract(deductionSumValue)
                        if (differenceSumDohSumVich < new BigDecimal(0)) {
                            differenceSumDohSumVich = new BigDecimal(0)
                        }
                        differenceTotalSumDohSumVich = differenceTotalSumDohSumVich.add(differenceSumDohSumVich)
                    }
                    BigDecimal calculatedTaxCheckSum = differenceTotalSumDohSumVich.multiply(new BigDecimal(taxRateAttribute.getValue())).divide(new BigDecimal(100))
                    Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = extractAttribute(CALCULATED_TAX, svedDohNode)
                    BigDecimal differenceCalculatedTaxAndCalculatedTaxCheckSum = calculatedTaxAttribute.getValue().subtract(ScriptUtils.round(calculatedTaxCheckSum, 0))
                    if (differenceCalculatedTaxAndCalculatedTaxCheckSum.abs() > 1) {
                        createErrorMessage(logger, documentNode, "«Сумма налога исчисленная» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" «Сумма налога исчисленная» ${calculatedTaxAttribute.getValue()} должна быть равна произведению «Ставки» ${taxRateAttribute.getValue()} и «Налоговой базы» ${extractAttribute(TAX_BASE, svedDohNode).getValue()}.")
                    }
                }
            }
        }
    }
}

/**
 * п.5 Сравнение сумм перечисленного и удержанного налога
 */
class TaxSummAndWithHoldingTaxChecker extends AbstractChecker {

    TaxSummAndWithHoldingTaxChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                Ndfl2Leaf<BigDecimal> withHoldingTaxAttribute = extractAttribute(WITHHOLDING_TAX, svedDohNode)
                Ndfl2Leaf<BigDecimal> taxSumAttribute = extractAttribute(TAX_SUM, svedDohNode)
                BigDecimal taxSum = taxSumAttribute.getValue()
                BigDecimal withHoldingTax = withHoldingTaxAttribute.getValue()
                if (taxSum > withHoldingTax) {
                    createErrorMessage(logger, documentNode, "«Сумма налога перечисленная» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" «Сумма налога перечисленная» $taxSum не должна превышать «Сумму налога удержанную» $withHoldingTax.")
                }
            }
        }
    }
}

/**
 * п.6 Сравнение сумм исчисленного налога и авансовых платежей
 */
class CalculatedTaxPrepaymentChecker extends AbstractChecker {
    CalculatedTaxPrepaymentChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                Ndfl2Leaf<BigDecimal> prepaymentAttribute = extractAttribute(PREPAYMENT_SUM, svedDohNode)
                Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = extractAttribute(CALCULATED_TAX, svedDohNode)
                BigDecimal prepayment = prepaymentAttribute.getValue()
                BigDecimal calculatedTax = calculatedTaxAttribute.getValue()
                if (prepayment > calculatedTax) {
                    createErrorMessage(logger, documentNode, "«Сумма фиксированных авансовых платежей» заполнена некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" «Сумма фиксированных авансовых платежей» $prepayment не должна превышать «Сумму налога исчисленного» $calculatedTax.")
                }
            }
        }
    }
}

/**
 * п.7 Расчет общей суммы дохода
 */
class CommonIncomeSumChecker extends AbstractChecker {
    CommonIncomeSumChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                List<Ndfl2Node> svSumDohList = extractNdfl2Nodes(SV_SUM_DOH, svedDohNode)
                // Сумма атрибутов(Файл.Документ.СведДох.ДохВыч.СвСумДох.СумДоход)
                BigDecimal sumDohodSum = new BigDecimal(0)
                for (Ndfl2Node svSumDoh : svSumDohList) {
                    Ndfl2Leaf<BigDecimal> incomeSumAttribute = extractAttribute(SUM_DOHOD, svSumDoh)
                    sumDohodSum = sumDohodSum.add(incomeSumAttribute.getValue())
                }
                Ndfl2Leaf<BigDecimal> incomeSumCommonAttribute = extractAttribute(INCOME_SUM_COMMON, svedDohNode)
                BigDecimal incomeSumCommon = incomeSumCommonAttribute.getValue()
                if (incomeSumCommon != sumDohodSum) {
                    createErrorMessage(logger, documentNode, "«Общая сумма дохода» рассчитана некорректно", "В \"Раздел 5. \"Общие суммы дохода и налога\" «Общая сумма дохода» $incomeSumCommon должна быть равна «Сумме доходов по всем месяцам» $sumDohodSum \"Раздела 3. \"Доходы, облагаемые по ставке ${extractAttribute(TAX_RATE, svedDohNode).getValue()} %%\"")
                }
            }
        }
    }
}

/**
 * п.8 Сравнение сумм дохода и вычета
 */
class IncomeSumAndDeductionChecker extends AbstractChecker {
    IncomeSumAndDeductionChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                List<Ndfl2Node> svSumDohList = extractNdfl2Nodes(SV_SUM_DOH, svedDohNode)

                for (Ndfl2Node svSumDoh : svSumDohList) {
                    Ndfl2Leaf<BigDecimal> incomeSumAttribute = extractAttribute(SUM_DOHOD, svSumDoh)
                    List<Ndfl2Node> svSumVichList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                    BigDecimal income = incomeSumAttribute.getValue()
                    for (Ndfl2Node svSumVich : svSumVichList) {
                        Ndfl2Leaf<BigDecimal> deductionSumAttribute = null
                        if (!svSumVichList.isEmpty()) {
                            deductionSumAttribute = extractAttribute(DEDUCTION_SUM, svSumVich)
                        }
                        BigDecimal deduction = deductionSumAttribute ? deductionSumAttribute.getValue() : new BigDecimal(0)
                        if (income < deduction) {
                            createErrorMessage(logger, documentNode, "«Сумма вычета» заполнена некорректно", "В \"Разделе 3. \"Доходы, облагаемые по ставке ${extractAttribute(TAX_RATE, svedDohNode).getValue()} %%\" «Сумма вычета» $deduction. по коду ${extractAttribute(DEDUCTION_CODE, svSumVich).value} превышает «Сумму полученного дохода» $income, к которому он применен.")
                        }
                    }
                }
            }
        }
    }
}

/**
 * п.9 Заполнение поля суммы не удержанного налога (только для 2-НДФЛ (2))
 */
class NotHoldingTaxChecker extends AbstractChecker {
    NotHoldingTaxChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                Ndfl2Leaf<BigDecimal> notHoldingTaxAttribute = extractAttribute(NOT_HOLDING_TAX, svedDohNode)
                if (notHoldingTaxAttribute.value <= new BigDecimal(0)) {
                    createErrorMessage(logger, documentNode, "«Сумма налога, не удержанная налоговым агентом» заполнена некорректно", "В соответствии с п.5 ст.226 НК РФ должна быть больше «0» «Сумма налога, не удержанная налоговым агентом» в \"Разделе 5. \"Общие суммы дохода и налога\"")
                }
            }
        }
    }
}

/**
 * п.10 Отсутствие суммы налога удержанного (только для 2-НДФЛ (2))
 */
class WithHoldingTaxChecker extends AbstractChecker {

    WithHoldingTaxChecker(Ndfl2Node headNode) {
        super(headNode)
    }

    void check(Logger logger) {
        List<Ndfl2Node> documentNodeList = extractNdfl2Nodes(DOCUMENT_NODE, headNode)
        for (Ndfl2Node documentNode : documentNodeList) {
            List<Ndfl2Node> svedDohNodeList = extractNdfl2Nodes(SVEDDOH_NODE, documentNode)
            for (Ndfl2Node svedDohNode : svedDohNodeList) {
                Ndfl2Leaf<BigDecimal> withHoldingTaxAttribute = extractAttribute(WITHHOLDING_TAX, svedDohNode)
                BigDecimal withHoldingTax = withHoldingTaxAttribute.getValue()
                if (withHoldingTaxAttribute.getValue() != new BigDecimal(0)) {
                    createErrorMessage(logger, documentNode, "«Сумма налога удержанная» заполнена некорректно", "Сумма налога удержанная» $withHoldingTax в \"Разделе 5. \"Общие суммы дохода и налога\" должна быть равна \"0\"")
                }
            }
        }
    }
}

def interdocumentaryCheckData() {
    interdocumentary2ndflCheckData()
    interdocumentary6ndflCheckData()
}

def interdocumentary2ndflCheckData() {
    List<DeclarationData> declarationDataNdfl_2_1_List = declarationService.find(declarationData.declarationTemplateId, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp, declarationData.oktmo);
    List<DeclarationData> declarationDataNdfl_2_2_List = declarationService.find(NDFL_2_2_DECLARATION_TYPE, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp, declarationData.oktmo);
    if (declarationDataNdfl_2_2_List.isEmpty()) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        Department department = departmentService.get(departmentReportPeriod.departmentId)
        ReportPeriod reportPeriod = departmentReportPeriod.reportPeriod
        def period = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)
        def periodCode = period?.CODE?.stringValue
        def periodName = period?.NAME?.stringValue
        def calendarStartDate = reportPeriod?.calendarStartDate
        String correctionDateExpression = departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format(DATE_FORMAT_DOTTED)},"
        logger.warnExp("Сравнение физических лиц форм 2-НДФЛ (2) и 2-НДФЛ (1) не выполнено. Не найдена форма 2-НДФЛ (2) со следующими параметрами: КПП: \"%s\", ОКТМО: \"%s\", КодНо: \"%s\", Период: \"%s\", Подразделение: \"%s\"", "Не найдено физическое лицо из 2-НДФЛ (2) в 2-НДФЛ (1)", "", declarationData.kpp, declarationData.oktmo, declarationData.taxOrganCode, "$periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")}" + " года" + correctionDateExpression, department.getName())
    }
    PagingResult<Map<String, RefBookValue>> ndfl_2_2ReferencesList = new PagingResult<>()
    PagingResult<Map<String, RefBookValue>> ndfl_2_1ReferencesList = new PagingResult<>()
    for (DeclarationData declarationData1Ndfl_2_2 : declarationDataNdfl_2_2_List) {
        ndfl_2_2ReferencesList.addAll(getProvider(NDFL_REFERENCES).getRecords(null, null, "DECLARATION_DATA_ID = ${declarationData1Ndfl_2_2.id}", null))
    }
    for (DeclarationData declarationData1Ndfl_2_1 : declarationDataNdfl_2_1_List) {
        ndfl_2_1ReferencesList.addAll(getProvider(NDFL_REFERENCES).getRecords(null, null, "DECLARATION_DATA_ID = ${declarationData1Ndfl_2_1.id}", null))
    }
    List<Long> ndfl2_1PersonIdList = new ArrayList<>()
    for (Map<String, RefBookValue> ndfl_2_1Reference : ndfl_2_1ReferencesList) {
        ndfl2_1PersonIdList.add(ndfl_2_1Reference.get("PERSON_ID").value)
    }
    for (Map<String, RefBookValue> ndfl_2_2Reference : ndfl_2_2ReferencesList) {
        if (!ndfl2_1PersonIdList.contains(ndfl_2_2Reference.get("PERSON_ID").value)) {
            Long numSpr = ndfl_2_2Reference.get("NUM").value
            Long ddId = ndfl_2_2Reference.get("DECLARATION_DATA_ID").value
            StringBuilder fio = new StringBuilder(ndfl_2_2Reference.get("SURNAME").value ?: "")
                    .append(" ")
                    .append(ndfl_2_2Reference.get("NAME").value ?: "")
                    .append(" ")
                    .append(ndfl_2_2Reference.get("LASTNAME").value ?: "")
            StringBuilder fioAndNumSpr = fio.append(", Номер справки: ")
                    .append(numSpr.toString())
            logger.warnExp("Ошибка сравнения физических лиц форм 2-НДФЛ (2) и 2-НДФЛ (1). В форме 2-НДФЛ (1) не найдено физическое лицо \"%s\" формы 2-НДФЛ (2) со следующими параметрами: «Номер формы»: \"%d\", «Номер справки»: \"%d\"", "Не найдено физическое лицо из 2-НДФЛ (2) в 2-НДФЛ (1)", fioAndNumSpr.toString(), fio.toString(), ddId, numSpr)
        }
    }
}

def interdocumentary6ndflCheckData() {

    List<DeclarationData> ndfl6declarationDataList = declarationService.find(DECLARATION_TYPE_NDFL6_ID, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp, declarationData.oktmo)
    if (ndfl6declarationDataList.isEmpty()) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        Department department = departmentService.get(departmentReportPeriod.departmentId)
        logger.error("Выполнить проверку междокументных контрольных соотношений невозможно. Отсутствует форма 6-НДФЛ для подразделения: \"${department.name}\", КПП: ${declarationData.kpp}, ОКТМО: ${declarationData.oktmo}, Код НО: ${declarationData.taxOrganCode}, Период: ${departmentReportPeriod.reportPeriod.name} ${departmentReportPeriod.reportPeriod.taxPeriod.year} ${departmentReportPeriod.correctionDate ? " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(DATE_FORMAT_DOTTED) : ""}")
        return
    }
    if (ndfl6declarationDataList.size() > 1) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        Department department = departmentService.get(departmentReportPeriod.departmentId)
        logger.error("Выполнить проверку междокументных контрольных соотношений невозможно. В Системе должна быть только одна форма 6-НДФЛ для подразделения: \"${department.name}\", КПП: ${declarationData.kpp}, ОКТМО: ${declarationData.oktmo}, Код НО: ${declarationData.taxOrganCode}, Период: ${departmentReportPeriod.reportPeriod.name} ${departmentReportPeriod.reportPeriod.taxPeriod.year} ${departmentReportPeriod.correctionDate ? " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(DATE_FORMAT_DOTTED) : ""}")
        return
    }
    DeclarationData ndfl6declarationData = ndfl6declarationDataList.get(0)
    List<DeclarationData> ndfl2declarationDataList = declarationService.find(NDFL_2_1_DECLARATION_TYPE, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp, declarationData.oktmo)

    String msgError = "6-НДФЛ КПП: \"%s\" ОКТМО: \"%s\" не соответствуют форме 2-НДФЛ (1) КПП: \"%s\" ОКТМО: \"%s\""
    msgError = "Контрольные соотношения по %s формы " + sprintf(msgError, declarationData.kpp, declarationData.oktmo, declarationData.kpp, declarationData.oktmo)

    // МежДок4
    // Мапа <Ставка, НачислДох>
    def mapNachislDoh6 = [:]
    // Мапа <Ставка, Сумма(СумИтНалПер.СумДохОбщ)>
    def mapSumDohObch2 = [:]

    // МежДок5
    // НачислДохДив
    def nachislDohDiv6 = 0.0
    // Сумма(СвСумДох.СумДоход)
    def sumDohDivObch2 = 0.0

    // МежДок6
    // Мапа <Ставка, ИсчислНал>
    def mapIschislNal6 = [:]
    // Мапа <Ставка, Сумма(СумИтНалПер.НалИсчисл)>
    def mapNalIschisl2 = [:]

    // МежДок7
    // НеУдержНалИт
    def neUderzNalIt6 = 0
    // Сумма(СумИтНалПер.НалНеУдерж)
    def nalNeUderz2 = 0

    // МежДок8
    def kolFl6 = 0
    def kolFl2 = 0

    def ndfl6Stream = declarationService.getXmlStream(ndfl6declarationData.id)
    def fileNode6Ndfl = new XmlSlurper().parse(ndfl6Stream);
    def sumStavkaNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
    sumStavkaNodes6.each { sumStavkaNode6 ->
        ScriptUtils.checkInterrupted()
        def stavka6 = Integer.valueOf(sumStavkaNode6.attributes()[TAX_RATE]) ?: 0

        // МежДок4
        def nachislDoh6 = ScriptUtils.round(Double.valueOf(sumStavkaNode6.attributes()[ATTR_NACHISL_DOH6]), 2) ?: 0
        mapNachislDoh6.put(stavka6, nachislDoh6)

        // МежДок5
        if (stavka6 == 13) {
            nachislDohDiv6 = ScriptUtils.round(Double.valueOf(sumStavkaNode6.attributes()[ATTR_NACHISL_DOH_DIV6]), 2) ?: 0
        }

        // МежДок6
        def ischislNal6 = Long.valueOf(sumStavkaNode6.attributes()[ATTR_ISCHISL_NAL6]) ?: 0
        mapIschislNal6.put(stavka6, ischislNal6)
    }

    def obobshPokazNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_OBOBSH_POKAZ6 }
    obobshPokazNodes6.each { obobshPokazNode6 ->
        ScriptUtils.checkInterrupted()
        // МежДок7
        neUderzNalIt6 = Long.valueOf(obobshPokazNode6.attributes()[ATTR_NE_UDERZ_NAL_IT6]) ?: 0

        // МежДок8
        kolFl6 = Integer.valueOf(obobshPokazNode6.attributes()[ATTR_KOL_FL_DOHOD6]) ?: 0
    }

    // Суммы значений всех 2-НДФЛ сравниваются с одним 6-НДФЛ
    ndfl2declarationDataList.id.each { ndfl2DeclarationDataId ->
        ScriptUtils.checkInterrupted()
        def ndfl2Stream = declarationService.getXmlStream(ndfl2DeclarationDataId)
        def fileNode2Ndfl = new XmlSlurper().parse(ndfl2Stream);

        // МежДок8
        def documentNodes = fileNode2Ndfl.depthFirst().findAll { it.name() == DOCUMENT_NODE }
        kolFl2 += documentNodes.size()

        def svedDohNodes = fileNode2Ndfl.depthFirst().grep { it.name() == SVEDDOH_NODE }
        svedDohNodes.each { svedDohNode ->
            ScriptUtils.checkInterrupted()
            def stavka2 = Integer.valueOf(svedDohNode.attributes()[TAX_RATE]) ?: 0

            // МежДок4
            def sumDohObch2 = mapSumDohObch2.get(stavka2)
            sumDohObch2 = sumDohObch2 == null ? 0 : sumDohObch2

            // МежДок6
            def nalIschisl2 = mapNalIschisl2.get(stavka2)
            nalIschisl2 = nalIschisl2 == null ? 0 : nalIschisl2

            def sumItNalPerNodes = svedDohNode.depthFirst().grep { it.name() == NODE_NAME_SUM_IT_NAL_PER2 }
            sumItNalPerNodes.each { sumItNalPerNode ->
                sumDohObch2 += ScriptUtils.round(Double.valueOf(sumItNalPerNode.attributes()[INCOME_SUM_COMMON]), 2) ?: 0
                nalIschisl2 += Long.valueOf(sumItNalPerNode.attributes()[CALCULATED_TAX]) ?: 0

                // МежДок7
                nalNeUderz2 += Long.valueOf(sumItNalPerNode.attributes()[NOT_HOLDING_TAX]) ?: 0
            }
            mapSumDohObch2.put(stavka2, sumDohObch2)
            mapNalIschisl2.put(stavka2, nalIschisl2)

            // МежДок5
            if (stavka2 == 13) {
                def svSumDohNodes = svedDohNode.depthFirst().grep { it.name() == SV_SUM_DOH }
                svSumDohNodes.each { svSumDohNode ->
                    if (svSumDohNode.attributes()[INCOME_CODE].toString() == "1010") {
                        sumDohDivObch2 += Double.valueOf(svSumDohNode.attributes()[SUM_DOHOD] ?: 0)
                    }
                }
            }
        }
    }

    // МежДок4
    mapNachislDoh6.each { stavka6, nachislDoh6 ->
        ScriptUtils.checkInterrupted()
        def sumDohObch2 = mapSumDohObch2.get(stavka6)

        if (ScriptUtils.round(nachislDoh6, 2) != ScriptUtils.round(sumDohObch2, 2)) {
            def msgErrorRes = sprintf(msgError, "«Сумме начисленного дохода»") + " по «Ставке» " + stavka6
            logger.errorExp(msgErrorRes, "«Сумма начисленного дохода» рассчитана некорректно", "")
        }
    }

    // МежДок5
    if (ScriptUtils.round(nachislDohDiv6, 2) != ScriptUtils.round(sumDohDivObch2, 2)) {
        def msgErrorRes = sprintf(msgError, "«Сумме начисленного дохода» в виде дивидендов")
        logger.errorExp(msgErrorRes, "«Сумма начисленного дохода» рассчитана некорректно", "")
    }

    // МежДок6

    mapIschislNal6.each { stavka6, ischislNal6 ->
        def nalIschisl2 = mapNalIschisl2.get(stavka6)
        if (ischislNal6 != nalIschisl2) {
            def msgErrorRes = sprintf(msgError, "«Сумме налога исчисленного»") + " по «Ставке» " + stavka6
            logger.errorExp(msgErrorRes, "«Сумма налога исчисленного» рассчитана некорректно", "")
        }
    }

    // МежДок7
    if (neUderzNalIt6 != nalNeUderz2) {
        def msgErrorRes = sprintf(msgError, "«Сумме налога, не удержанной налоговым агентом»")
        logger.errorExp(msgErrorRes, "«Сумма налога, не удержанная налоговым агентом» рассчитана некорректно", "")
    }

    // МежДок8
    if (kolFl6 != kolFl2) {
        def msgErrorRes = sprintf(msgError, "количеству физических лиц, получивших доход")
        logger.errorExp(msgErrorRes, "«Количество физических лиц, получивших доход» рассчитано некорректно", "")
    }
}