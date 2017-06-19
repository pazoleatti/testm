package form_template.ndfl.report_2ndfl_1.v2016

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
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import java.text.DateFormat
import java.text.SimpleDateFormat

switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверки
        println "!CHECK!"
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
        ndflPersonsList.each { np ->
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

                    def ndflPersonPrepayments = findPrepayments(np.id, startDate, endDate, priznakF)

                    ndflPersonIncomesGroupedByTaxRate.keySet().each { taxRateKey ->
                        ScriptUtils.checkInterrupted();
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
                                            def sumDohod = getSumDohod(priznakF, ndflPersonIncomesGroupedByIncomeCode.get(incomeKey))
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
                            def incomesByTaxDate = ndflPersonService.findIncomesByPeriodAndNdflPersonIdAndTaxDate(np.id, startDate, endDate)
                            Date firstDateOfMarchOfNextPeriod = getFirstMarchOfNextPeriod(endDate)
                            СумИтНалПер(СумДохОбщ: priznakF == "1" ? ScriptUtils.round(getSumDohod(priznakF, ndflPersonIncomesAll), 2) : ScriptUtils.round(sumDohodAll, 2),
                                    НалБаза: priznakF == "1" ? ScriptUtils.round(getNalBaza(ndflPersonIncomesAll), 2) : ScriptUtils.round(sumDohodAll - sumVichAll, 2),
                                    НалИсчисл: getNalIschisl(priznakF, ndflPersonIncomesAll),
                                    АвансПлатФикс: getAvansPlatFix(ndflPersonPrepayments),
                                    НалУдерж: getNalUderzh(priznakF, incomesByTaxDate, startDate, firstDateOfMarchOfNextPeriod),
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
        List<String> mandatoryFields = new LinkedList<>();
        if (ndflPerson.rowNum == null) mandatoryFields << "'№пп'"
        if (ndflPerson.inp == null || ndflPerson.inp.isEmpty()) mandatoryFields << "'Налогоплательщик.ИНП'"
        if (ndflPerson.lastName == null || ndflPerson.lastName.isEmpty()) mandatoryFields << "'Налогоплательщик.Фамилия'"
        if (ndflPerson.firstName == null || ndflPerson.firstName.isEmpty()) mandatoryFields << "'Налогоплательщик.Имя'"
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

            String msg = String.format("Не удалось создать форму \"%s\" за \"%s\", подразделение: \"%s\", КПП: \"%s\", ОКТМО: \"%s\", Код НО: \"%s\". Не заполнены обязательные параметры %s для ФИО: %s, ИНП: %s в консолидированной форме № %s",
                    currDeclarationTemplate.getName(),
                    departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                    department.getName(),
                    declarationData.kpp,
                    declarationData.oktmo,
                    declarationData.taxOrganCode,
                    mandatoryFields.join(', '),
                    fio,
                    ndflPerson.inp,
                    ndflPerson.declarationDataId
            )
            logger.error(msg)
            toReturn = false
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
def findPrepayments(def ndflPersonId, def startDate, def endDate, priznakF) {
    def toReturn = []
    if (priznakF == "1") {
        return ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, true)
    } else {
        return ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, startDate, endDate, false)
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
BigDecimal getSumDohod(def priznakF, List<NdflPersonIncome> rows) {
    def toReturn = new BigDecimal(0)
    if (priznakF == "1") {
        rows.each {
            if (it.incomeAccruedSumm != null && it.incomeAccruedSumm > 0) {
                toReturn = toReturn.add(it.incomeAccruedSumm)
            }
        }
    } else if (priznakF == "2") {
        rows.each {
            if (it.notHoldingTax != null) {
                toReturn = toReturn.add(it.notHoldingTax)
            }
        }
    }
    return toReturn
}

// Вычислить сумму для НалБаза
def getNalBaza(def incomes) {
    def toReturn = new BigDecimal(0)
    incomes.each {
        if (it.taxBase != null) {
            toReturn = toReturn.add(it.taxBase)
        }
    }
    return toReturn
}

//Вычислить сумму для НалИсчисл
def getNalIschisl(def priznakF, def incomes) {
    def toReturn = new BigDecimal(0)
    if (priznakF == "1") {
        incomes.each {
            if (it.calculatedTax != null) {
                toReturn = toReturn.add(it.calculatedTax)
            }
        }
    } else if (priznakF == "2") {
        incomes.each {
            if (it.notHoldingTax != null && it.notHoldingTax > 0) {
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
def getNalUderzh(def priznakF, def incomes, startDate, endDate) {
    def toReturn = 0L
    if (priznakF == "1") {
        incomes.each {
            if (it.withholdingTax != null && it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                toReturn += it.withholdingTax
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
        incomes.each {
            if (it.taxSumm != null && it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                toReturn += it.taxSumm
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
        incomes.each {
            if (it.overholdingTax != null && it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                toReturn += it.overholdingTax
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
    if (priznakF == "1") {
        incomes.each {
            if (it.notHoldingTax != null && it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                toReturn += it.notHoldingTax
            }
        }
    } else if (priznakF == "2") {
        incomes.each {
            if (it.notHoldingTax != null && it.calculatedTax > 0 && it.incomeAccruedDate >= startDate && it.incomePayoutDate < endDate) {
                toReturn += it.notHoldingTax
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
        departmentCache.put(departmentId, departmentParamList?.get(0))
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
        def oktmoReference = getProvider(REF_BOOK_OKTMO_ID).getRecords(getReportPeriodEndDate(reportPeriodId), null, "CODE = '$oktmo'", null).get(0).id.value
        departmentParamRow = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(reportPeriodId), null, "OKTMO = $oktmoReference AND KPP = '$kpp'", null).get(0)
        if (departmentParamRow == null) {
            departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
        }
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
    return deductionTypeList.get(0).DEDUCTION_MARK?.value
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
    List<DeclarationData> listDepartmentNotAcceptedRnu = declarationService.findAllActiveWithNotAcceptedState(RNU_NDFL_DECLARATION_TYPE, drp.reportPeriod.id)

    for (DeclarationData dd : listDepartmentNotAcceptedRnu) {
        // Подразделение
        Long departmentCode = departmentService.get(dd.departmentId)?.code
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
                " года" + correctionDateExpression + " имеются не принятые экземпляры консолидированных налоговых форм для следующих ТБ: '${listDepartmentNotAcceptedRnu.id.join("\", \"")}'," +
                " для которых в системе существуют КНФ в текущем периоде, состояние которых <> 'Принята'. Данные этих форм не включены в отчетность!")
    }
}

@Field
final int RNU_NDFL_DECLARATION_TYPE = 101

@Field
def departmentParamTableList = null

@Field
def departmentParamTableListCache = [:]

def createForm() {
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
    def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    def declarationTypeId = currDeclarationTemplate.type.id
    // Мапа где значение физлица для каждой пары КПП и ОКТМО
    def ndflPersonsIdGroupedByKppOktmo = getNdflPersonsGroupedByKppOktmo()

    // Удаление ранее созданных отчетных форм
    declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId).each {
        ScriptUtils.checkInterrupted();
        declarationService.delete(it.id, userInfo)
    }

    if (ndflPersonsIdGroupedByKppOktmo == null) {
        return
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

        declarations.each { declaration ->
            def pairKppOktmo = new PairKppOktmo(declaration.kpp, declaration.oktmo, declaration.taxOrganCode)
            if (!pairKppOktmoList.contains(pairKppOktmo)) {
                pairKppOktmoList << pairKppOktmo
            }
        }
        declarations.each {
            ScriptUtils.checkInterrupted();
            ndflReferencesWithError.addAll(getNdflReferencesWithError(it.id, it.reportPeriodId))
        }

        if (ndflReferencesWithError.isEmpty()) {
            createCorrPeriodNotFoundMessage(departmentReportPeriod, departmentReportPeriodList, false)
            return null
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
        def oktmoForDepartment = getOktmoByIdList(referencesOktmoList)
        departmentParamTableList.each { dep ->
            ScriptUtils.checkInterrupted();
            if (dep.OKTMO?.value != null) {
                def oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
                if (oktmo != null) {
                    pairKppOktmoList << new PairKppOktmo(dep.KPP?.value, oktmo.CODE.value, dep?.TAX_ORGAN_CODE?.value)
                }
            }
        }
    }
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
                addNdflPersons(ndflPersonsIdGroupedByKppOktmo, pair, ndflPersons)
            } else {

                if (declarationData.declarationTemplateId == NDFL_2_2_DECLARATION_TYPE) {
                    logger.warn("Для подразделения: $depName, КПП: ${pair.kpp}, ОКТМО: ${pair.oktmo} за период $otchetGod ${reportPeriod.name} $strCorrPeriod отсутствуют сведения о не удержанном налоге.")
                } else {
                    logger.warn("Для подразделения: $depName, КПП: ${pair.kpp}, ОКТМО: ${pair.oktmo} за период $otchetGod ${reportPeriod.name} $strCorrPeriod отсутствуют сведения о НДФЛ.")
                }
            }
        }
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
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    String correctionDateExpression = departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")},"
    if (forDepartment) {
        logger.info("Для заданного подразделения ${department.name} и периода ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не найдены КПП для формирования уточненной отчетности")
    } else {
        logger.info("Для заданного подразделения ${department.name} и периода ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не найдены физические лица для формирования уточненной отчетности")
    }
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

def getPrevDepartmentReportPeriod(departmentReportPeriod) {
    def prevDepartmentReportPeriod = departmentReportPeriodService.getPrevLast(declarationData.departmentId, departmentReportPeriod.reportPeriod.id)
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

    if (allDeclarationData.isEmpty()) {
        createEmptyMessage(departmentReportPeriod, false)
        return null
    }

    // удаление форм не со статусом Принята
    def declarationsForRemove = []
    allDeclarationData.each { declaration ->
        if (declaration.state != State.ACCEPTED) {
            declarationsForRemove << declaration
        }
    }

    if (allDeclarationData.isEmpty()) {
        createEmptyMessage(departmentReportPeriod, true)
        return null
    }

    allDeclarationData.removeAll(declarationsForRemove)
    return allDeclarationData
}

@TypeChecked
def createEmptyMessage(DepartmentReportPeriod departmentReportPeriod, boolean acceptChecking) {
    Department department = departmentService.get(departmentReportPeriod.departmentId)
    String correctionDateExpression = departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")},"
    if (acceptChecking) {
        logger.info("Для заданного подразделения ${department.name} и периода ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не найдена форма РНУ НДФЛ (консолидированная) должна быть в состоянии \"Принята\". Примите форму и повторите операцию")
    } else {
        logger.info("Для заданного подразделения ${department.name} и периода ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не найдена форма РНУ НДФЛ (консолидированная)")
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
        String strCorrPeriod = ""
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
        }
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
def getNdflReferencesWithError(declarationDataId, reportPeriodId) {
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
            if (key == "birthDay") {
                resultReportParameters.put(key, ScriptUtils.formatDate(value, "dd.MM.yyyy"))
            } else {
                resultReportParameters.put(key, value)
            }
        }
    }

    // Ограничение числа выводимых записей
    int pageSize = 10

    // Поиск данных по фильтру
    def docs = searchData(resultReportParameters, pageSize)

    // Формирование списка данных для вывода в таблицу
    docs.each() { doc ->
        DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(rowColumns, null));
        row.pNumSpravka = doc.@НомСпр.text()
        row.lastName = doc?.ПолучДох?.ФИО?.@Фамилия?.text()
        row.firstName = doc?.ПолучДох?.ФИО?.@Имя?.text()
        row.middleName = doc?.ПолучДох?.ФИО?.@Отчество?.text()
        row.birthDay = doc?.ПолучДох?.@ДатаРожд?.text()
        row.idDocNumber = doc?.ПолучДох?.УдЛичнФЛ?.@СерНомДок?.text()
        dataRows.add(row)
    }

    int countOfAvailableNdflPerson = docs.size()

    if (countOfAvailableNdflPerson >= pageSize) {
        countOfAvailableNdflPerson = counter;
    }

    result.setTableColumns(tableColumns);
    result.setDataRows(dataRows);
    result.setCountAvailableDataRows(countOfAvailableNdflPerson)
    scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
    scriptSpecificReportHolder.setSubreportParamValues(params)
}

def createTableColumns() {
    List<Column> tableColumns = new ArrayList<Column>()

    Column pNumSpravka = new StringColumn()
    pNumSpravka.setAlias("pNumSpravka")
    pNumSpravka.setName("Номер справки")
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
    column4.setAlias("birthDay")
    column4.setName("Дата рождения")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("idDocNumber")
    column5.setName("№ ДУЛ")
    column5.setWidth(10)
    tableColumns.add(column5)

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
    column4.setAlias("birthDay")
    column4.setName("Дата рождения")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("idDocNumber")
    column5.setName("№ ДУЛ")
    column5.setWidth(10)
    tableColumns.add(column5)

    return tableColumns;
}

/**
 * Поиск справок согласно фильтру
 */
@Field
int counter = 0

def searchData(def params, pageSize) {
    def xmlStr = declarationService.getXmlData(declarationData.id)
    def Файл = new XmlSlurper().parseText(xmlStr)
    def docs = Файл.Документ.findAll { doc ->
        (params['pNumSpravka'] ? StringUtils.containsIgnoreCase(doc.@НомСпр.text(), params['pNumSpravka']) : true) &&
                (params['lastName'] ? StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Фамилия.text(), params['lastName']) : true) &&
                (params['firstName'] ? StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Имя.text(), params['firstName']) : true) &&
                (params['middleName'] ? StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Отчество.text(), params['middleName']) : true) &&
                (params['birthDay'] ? StringUtils.containsIgnoreCase(doc.ПолучДох.@ДатаРожд.text(), params['birthDay']) : true) &&
                (params['idDocNumber'] ? (StringUtils.containsIgnoreCase(doc.ПолучДох.УдЛичнФЛ.@СерНомДок.text(), params['idDocNumber']) ||
                        StringUtils.containsIgnoreCase(doc.ПолучДох.УдЛичнФЛ.@СерНомДок.text().replaceAll("[\\s,.-]", ""), params['idDocNumber'])) : true)
    }
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