package form_template.ndfl.report_2ndfl_1.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType
import com.aplana.sbrf.taxaccounting.model.StringColumn
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants
import groovy.transform.TypeCheckingMode
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.apache.commons.lang3.StringUtils
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
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
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.RefBookService
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import java.util.zip.ZipInputStream
import org.joda.time.LocalDateTime

new Report2Ndfl(this).run();

@TypeChecked
class Report2Ndfl extends AbstractScriptClass {

    DeclarationData declarationData
    TAUserInfo userInfo
    NdflPersonService ndflPersonService
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    DepartmentService departmentService
    Boolean needSources
    Boolean light
    FormSources sources
    ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder
    DepartmentReportPeriodService departmentReportPeriodService
    FileWriter xml
    RefBookService refBookService
    Map<String, Object> calculateParams
    BlobDataService blobDataServiceDaoImpl
    File xmlFile
    List<Long> ndflPersonKnfId
    Map<Long, Map<String, Object>> formMap
    Map<String, Object> scriptParams
    Boolean excludeIfNotExist
    State stateRestriction
    Integer partNumber
    String applicationVersion
    Map<String, Object> paramMap

    private Report2Ndfl() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Report2Ndfl(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo");
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
        if (scriptClass.getBinding().hasVariable("scriptSpecificReportHolder")) {
            this.scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) scriptClass.getProperty("scriptSpecificReportHolder");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("needSources")) {
            this.needSources = (Boolean) scriptClass.getProperty("needSources");
        }
        if (scriptClass.getBinding().hasVariable("light")) {
            this.light = (Boolean) scriptClass.getProperty("light");
        }
        if (scriptClass.getBinding().hasVariable("sources")) {
            this.sources = (FormSources) scriptClass.getProperty("sources");
        }
        if (scriptClass.getBinding().hasVariable("xml")) {
            this.xml = (FileWriter) scriptClass.getProperty("xml");
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getBinding().getProperty("refBookService");
        }
        if (scriptClass.getBinding().hasVariable("calculateParams")) {
            this.calculateParams = (Map<String, Object>) scriptClass.getProperty("calculateParams");
        }
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataServiceDaoImpl = (BlobDataService) scriptClass.getBinding().getProperty("blobDataServiceDaoImpl");
        }
        if (scriptClass.getBinding().hasVariable("xmlFile")) {
            this.xmlFile = (File) scriptClass.getBinding().getProperty("xmlFile");
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonKnfId")) {
            this.ndflPersonKnfId = (List<Long>) scriptClass.getBinding().getProperty("ndflPersonKnfId");
        }
        if (scriptClass.getBinding().hasVariable("formMap")) {
            this.formMap = (Map<Long, Map<String, Object>>) scriptClass.getBinding().getProperty("formMap");
        }
        if (scriptClass.getBinding().hasVariable("scriptParams")) {
            this.scriptParams = (Map<String, Object>) scriptClass.getBinding().getProperty("scriptParams");
        }
        if (scriptClass.getBinding().hasVariable("excludeIfNotExist")) {
            this.excludeIfNotExist = (Boolean) scriptClass.getBinding().getProperty("excludeIfNotExist");
        }
        if (scriptClass.getBinding().hasVariable("stateRestriction")) {
            this.stateRestriction = (State) scriptClass.getBinding().getProperty("stateRestriction");
        }
        if (scriptClass.getBinding().hasVariable("partNumber")) {
            this.partNumber = (Integer) scriptClass.getBinding().getProperty("partNumber");
        }
        if (scriptClass.getBinding().hasVariable("applicationVersion")) {
            this.applicationVersion = (String) scriptClass.getBinding().getProperty("applicationVersion");
        }
        if (scriptClass.getBinding().hasVariable("paramMap")) {
            this.paramMap = (Map<String, Object>) scriptClass.getBinding().getProperty("paramMap")
        }
        reportType = declarationData.declarationTemplateId == NDFL_2_1_DECLARATION_TYPE ? "2-НДФЛ (1)" : "2-НДФЛ (2)"
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.CALCULATE: //формирование xml
                try {
                    buildXml(xml)
                } catch (Exception e) {
                    calculateParams.put("notReplaceXml", true)
                    calculateParams.put("createForm", false)
                    def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
                    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
                    String strCorrPeriod = ""
                    if (departmentReportPeriod.getCorrectionDate() != null) {
                        strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
                    }
                    Department department = departmentService.get(departmentReportPeriod.departmentId)
                    String msg = String.format("Не удалось создать форму \"%s\" за период \"%s\", подразделение: \"%s\", КПП: \"%s\", ОКТМО: \"%s\". Ошибка: %s",
                            currDeclarationTemplate.getName(),
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                            department.getName(),
                            declarationData.kpp,
                            declarationData.oktmo,
                            e.getMessage())
                    logger.warn(msg)
                } finally {
                    break
                }
            case FormDataEvent.GET_SOURCES: //формирование списка источников
                getSources()
                break
            case FormDataEvent.PREPARE_SPECIFIC_REPORT:
                // Подготовка для последующего формирования спецотчета
                prepareSpecificReport()
                break
            case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
                createSpecificReport()
                break
            case FormDataEvent.CREATE_EXCEL_REPORT: //создание xlsx отчета
                createXlsxReport()
                break
            case FormDataEvent.CREATE_FORMS: // создание экземпляра
                checkDataConsolidated()
                createForm()
                break
            case FormDataEvent.PRE_CREATE_REPORTS:
                preCreateReports()
                break
        }
    }

/************************************* ДАННЫЕ ДЛЯ ОБЩИХ СОБЫТИЙ *******************************************************/

/************************************* СОЗДАНИЕ XML *****************************************************************/

// Количество физических лиц в одном xml-файле
    final int NUMBER_OF_PERSONS = 3000

// Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]

// запись подразделения в справочнике
    Map<String, RefBookValue> departmentParam = null

// Кэш подразделений из справочника
    Map<Integer, Map<String, RefBookValue>> departmentCache = [:]

// детали подразделения из справочника
    Map<String, RefBookValue> departmentParamRow = null

// отчетный период
    ReportPeriod reportPeriod = null

// Дата окончания отчетного периода
    Date reportPeriodEndDate = null

    Map<String, RefBookValue> formType = null

    final String DATE_FORMAT_FLATTEN = "yyyyMMdd"

    final String DATE_FORMAT_DOTTED = "dd.MM.yyyy"

    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"

    final int REF_BOOK_NDFL_DETAIL_ID = 951

    final int REB_BOOK_FORM_TYPE_ID = 931

    final int REF_BOOK_DEDUCTION_TYPE_ID = 921

    final int REF_BOOK_DEDUCTION_MARK_ID = 927

    final int REF_BOOK_DOC_STATE = 929

    final int REPORT_PERIOD_TYPE_ID = 8

    int REF_BOOK_ASNU_ID = 900

    int REF_BOOK_SIGNATORY_MARK_ID = 35

    int NDFL_2_1_DECLARATION_TYPE = 102

    int NDFL_2_2_DECLARATION_TYPE = 104

    final int DECLARATION_TYPE_NDFL6_ID = 103

    final String NDFL_2_S_PRIZNAKOM_1 = "2 НДФЛ (1)"

    final String NDFL_2_S_PRIZNAKOM_2 = "2 НДФЛ (2)"

    final String VERS_FORM = "5.04"

    final String KND = "1151078"

    final String PRIZNAK_KODA_VICHETA_IMUSCHESTVENNIY = "Имущественный"

    final String PRIZNAK_KODA_VICHETA_SOTSIALNIY = "Социальный"

    final String PART_NUMBER = "partNumber"

    final String PART_TOTAL = "partTotal"

    final String NDFL_PERSON_KNF_ID = "ndflPersonKnfId"

    List<RefBookRecord> ndflReferencess = []

    final String NDFL_PERSON_ID = "NDFL_PERSON_ID"

    final String NDFL_REFERENCES_DECLARATION_DATA_ID = "DECLARATION_DATA_ID"

    final String NDFL_REFERENCES_PERSON_ID = "PERSON_ID"

    final String NDFL_REFERENCES_NUM = "NUM"

    final String NDFL_REFERENCES_SURNAME = "SURNAME"

    final String NDFL_REFERENCES_NAME = "NAME"

    final String NDFL_REFERENCES_LASTNAME = "LASTNAME"

    final String NDFL_REFERENCES_BIRTHDAY = "BIRTHDAY"

    final String NDFL_REFERENCES_ERRTEXT = "ERRTEXT"

    final String OUTCOMING_ATTACH_FILE_TYPE = "Исходящий в ФНС"

    final Map<Long, Map<String, RefBookValue>> OKTMO_CACHE = [:]

    int pairKppOktmoSize = 0
    String reportType

    PagingResult<Map<String, RefBookValue>> ndflReferencesWithError = []

    def buildXml(Writer writer) {
        buildXml(writer, false, null, null)
    }

    def buildXmlForSpecificReport(Writer writer, Long xmlPartNumber, Long pNumSpravka) {
        buildXml(writer, true, xmlPartNumber, pNumSpravka)
    }

/**
 * Сформировать xml
 * @param writer
 * @param isForSpecificReport
 * @return
 */
    @TypeChecked(TypeCheckingMode.SKIP)
    def buildXml(Writer writer, boolean isForSpecificReport, Long xmlPartNumber, Long pNumSpravka) {
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
        String depName = departmentService.get((Integer) departmentParam.DEPARTMENT_ID.value).name
        // Имя файла
        def fileName = generateXmlFileId(sberbankInnParam, kodNoProm)
        // Отчетный период
        reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

        // Макет НФ
        def declarationTemplate = declarationService.getTemplate(declarationData.getDeclarationTemplateId())

        // Вид НФ
        formType = getFormType(declarationTemplate.declarationFormTypeId)

        // Данные для Файл.СвРекв
        String oktmo = getOktmoById(departmentParamRow?.OKTMO?.value)?.CODE?.stringValue
        // Данные для Файл.СвРекв.СвЮЛ
        String kpp = departmentParamRow?.KPP?.stringValue
        Integer otchetGod = reportPeriod.taxPeriod.year
        String priznakF = definePriznakF()
        def startDate = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()
        def endDate = reportPeriodService.getEndDate(declarationData.reportPeriodId).getTime()

        // Данные для Файл.Документ.Подписант
        String prPodp = getProvider(REF_BOOK_SIGNATORY_MARK_ID).getRecordData(departmentParamRow?.SIGNATORY_ID?.value).CODE.value
        String signatoryFirstname = departmentParamRow?.SIGNATORY_FIRSTNAME?.stringValue
        String signatorySurname = departmentParamRow?.SIGNATORY_SURNAME?.stringValue
        String signatoryLastname = departmentParamRow?.SIGNATORY_LASTNAME?.stringValue
        String naimDoc = departmentParamRow?.APPROVE_DOC_NAME?.stringValue
        String naimOrgApprove = departmentParamRow?.APPROVE_ORG_NAME?.stringValue

        // Данные для Файл.Документ.СвНА-(Данные о налоговом агенте)
        String tlf = departmentParamRow?.PHONE?.stringValue
        String naimOrg = departmentParamRow?.NAME?.stringValue

        // 	Данные для Файл.Документ.ПолучДох-(Данные о физическом лице - получателе дохода)
        List<NdflPerson> ndflPersonsList = null
        if (pNumSpravka == null) {
            ndflPersonsList = getNdflPersons()
        } else {
            Long ndflPersonId = (Long) getProvider(RefBook.Id.NDFL_REFERENCES.id).getRecords(null, null, "DECLARATION_DATA_ID = ${declarationData.id} and NUM = ${pNumSpravka}", null).get(0).NDFL_PERSON_ID.value
            if (ndflPersonId != null) {
                ndflPersonsList = getNdflPersons(ndflPersonId)
            } else {
                ndflPersonsList = []
            }
        }
        if (!checkMandatoryFields(ndflPersonsList)) {
            return
        }
        // Порядковый номер физического лица
        Integer nomSpr = (currentPageNumber - 1) * NUMBER_OF_PERSONS
        Integer nomSprCorr = 0

        // Текущая дата
        def currDate = Calendar.getInstance().getTime()
        def dateDoc = currDate.format(DATE_FORMAT_DOTTED, TimeZone.getTimeZone('Europe/Moscow'))

        // Номер корректировки
        def nomKorr = findCorrectionNumber()
        String kodNo = departmentParamRow?.TAX_ORGAN_CODE?.stringValue
        MarkupBuilder builder = new MarkupBuilder(writer)
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
                List<NdflPersonIncome> ndflPersonIncomesAll = filterIncomesByKppOktmp(findAllIncomes(np.id, startDate, endDate, priznakF))
                Set<String> ndflPersonIncomesOperationIds = ndflPersonIncomesAll.collect { it.operationId }.toSet()

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
                                nomSpr = getProvider(RefBook.Id.NDFL_REFERENCES.id).getRecords(new Date(), null, "PERSON_ID = ${np.personId} AND DECLARATION_DATA_ID = ${uncorretctedPeriodDd.id}", null).get(0).NUM.value
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
                                    def references = getProvider(RefBook.Id.NDFL_REFERENCES.id).getRecords(new Date(), null, "PERSON_ID = ${np.personId} AND " + filter, null)
                                    if (!references.isEmpty()) {
                                        nomSpr = references.get(0).NUM.value
                                    } else {
                                        references = getProvider(RefBook.Id.NDFL_REFERENCES.id).getRecords(new Date(), new PagingParams(0, 1), filter, refBookFactory.get(NDFL_REFERENCES).getAttribute("NUM"), false)
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
                                ДатаРожд: ScriptUtils.formatDate(np.birthDay),
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
                        def deductionsSelectedForDeductionsInfo = filterDeductionsByKppOktmp(ndflPersonService.findDeductionsWithDeductionsMarkOstalnie(np.id, startDate, endDate), ndflPersonIncomesOperationIds)
                        // Сведения о вычетах с признаком "Социльный;Стандартный;Имущественный;Инвестиционный"
                        def deductionsSelectedForDeductionsSum = filterDeductionsByKppOktmp(ndflPersonService.findDeductionsWithDeductionsMarkNotOstalnie(np.id, startDate, endDate, (priznakF == "1")), ndflPersonIncomesOperationIds)
                        def deductionsSelectedGroupedByDeductionTypeCode = groupByDeductionTypeCode(deductionsSelectedForDeductionsSum)
                        // Объединенные строки сведений об уведомлении, подтверждающие право на вычет
                        def unionDeductions = unionDeductionsForDeductionType(deductionsSelectedGroupedByDeductionTypeCode)

                        ndflPersonIncomesGroupedByTaxRate.keySet().each { taxRateKey ->
                            ScriptUtils.checkInterrupted();
                            def ndflPersonPrepayments = findPrepayments(np.id, taxRateKey, startDate, endDate, priznakF, ndflPersonIncomesOperationIds)
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

                                                СвСумДох(Месяц: sprintf('%02d', monthKey),
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
                                                            ДатаУвед: ScriptUtils.formatDate(it.notifDate),
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
                                                            ДатаУвед: ScriptUtils.formatDate(it.notifDate),
                                                            ИФНСУвед: it.notifSource)
                                                }
                                            }
                                        }
                                    }
                                }
                                // Доходы отобранные по датам для поля tax_date(Дата НДФЛ)
                                List<NdflPersonIncome> incomesByTaxDate = filterIncomesByKppOktmp(ndflPersonService.findIncomesByPeriodAndNdflPersonIdAndTaxDate(np.id, taxRateKey, startDate, endDate))
                                List<NdflPersonIncome> incomesByPayoutDate = filterIncomesByKppOktmp(ndflPersonService.findIncomesByPayoutDate(np.id, taxRateKey, startDate, endDate))
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
                                                    ДатаУвед: ScriptUtils.formatDate(prepayment.notifDate),
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
                        ndflReferencess << createRefBookAttributesForNdflReference(np.id, np.personId, nomSpr, np.lastName, np.firstName, np.middleName, new Date(np.birthDay.getLocalMillis()))
                    }
                }
            }
            if (!presentNotHoldingTax && priznakF == "2") {
                logger.info("\"Для подразделения: $depName, КПП: $kpp, ОКТМО: $oktmo за период $otchetGod $reportPeriod.name отсутствуют сведения о не удержанном налоге.\"")
                if (calculateParams != null) {
                    calculateParams.put("notReplaceXml", true)
                    calculateParams.put("createForm", false)
                }
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

    @TypeChecked(TypeCheckingMode.SKIP)
    def createXmlNode(builder, name, attributes) {
        builder.name(attributes)
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
                    logger.warn(msg)
                    calculateParams.put("notReplaceXml", true)
                    calculateParams.put("createForm", false)
                    toReturn = false
                }
            }
        }
        return toReturn
    }

// Сохранение информации о файле в комментариях
    def saveFileInfo(Date currDate, String fileName) {
        String fileUuid = blobDataServiceDaoImpl.create(xmlFile, fileName + ".xml", LocalDateTime.now())
        TAUser createUser = declarationService.getSystemUserInfo().getUser()

        RefBookDataProvider fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
        Long fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_2.code}").get(0)

        DeclarationDataFile declarationDataFile = new DeclarationDataFile()
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
            if (depReportPeriod.id == declarationData.departmentReportPeriodId || depReportPeriod.correctionDate != null && depReportPeriod.correctionDate > departmentReportPeriod.correctionDate) {
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
        getProvider(RefBook.Id.NDFL_REFERENCES.id).createRecordVersion(logger, new Date(), null, ndflReferencess)
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
    RefBookRecord createRefBookAttributesForNdflReference(
            Long ndflPersonId, Long personId, Long nomSpr, String lastName, String firstName, String middleName, Date birthDay) {
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
    def generateXmlFileId(String inn, String kodNoProm) {
        Map<String, RefBookValue> departmentParamRow = departmentParam ? getDepartmentParamDetails(declarationData.kpp, declarationData.oktmo) : null
        def r_t = "NO_NDFL2"
        def a = kodNoProm
        String k = departmentParamRow?.TAX_ORGAN_CODE?.stringValue
        String o = inn + declarationData.kpp
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
    List<NdflPerson> getNdflPersons() {
        List<NdflPerson> toReturn = []
        List<List<Long>> queryParameterList = ndflPersonKnfId.collate(1000)
        queryParameterList.each { List<Long> item ->
            if (!item.isEmpty()) {
                toReturn.addAll(ndflPersonService.findByIdList(item))
            }
        }
        return toReturn
    }

// Получение данных
    List<NdflPerson> getNdflPersons(Long personId) {
        def toReturn = []
        toReturn.addAll(ndflPersonService.findByIdList([personId]))
        return toReturn
    }

    List<NdflPersonIncome> findAllIncomes(Long ndflPersonId, Date startDate, Date endDate, String priznakF) {
        List<NdflPersonIncome> toReturn = []
        // Временно строки отбираются без учета условия КНФ.Раздел 2.Графа 10 ≠ 0
        if (priznakF == "1") {
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
    def filterDeductionsByIncomeCode(List<NdflPersonIncome> ndflPersonIncomes, List<NdflPersonDeduction> ndflPersonDeductions) {
        def toReturn = []
        ndflPersonIncomes.each { NdflPersonIncome ndflPersonIncome ->

            for (NdflPersonDeduction d in ndflPersonDeductions) {

                if (d.incomeCode == ndflPersonIncome.incomeCode && d.operationId == ndflPersonIncome.operationId &&
                        ndflPersonIncome.incomeAccruedDate.getMonthOfYear() == d.incomeAccrued.getMonthOfYear() && !toReturn.contains(d)) toReturn << d

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
    def filterDeductions(List<NdflPersonIncome> ndflPersonIncomes, List<NdflPersonDeduction> ndflPersonDeductions) {
        def toReturn = []
        for (NdflPersonDeduction d in ndflPersonDeductions) {
            for (NdflPersonIncome ndflPersonIncome in ndflPersonIncomes) {

                if (d.incomeCode == ndflPersonIncome.incomeCode && d.operationId == ndflPersonIncome.operationId &&
                        ndflPersonIncome.incomeAccruedDate.getMonthOfYear() == d.incomeAccrued.getMonthOfYear() && !toReturn.contains(d)) {
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
    List<NdflPersonPrepayment> findPrepayments(
            Long ndflPersonId, Integer taxRate, Date startDate, Date endDate, String priznakF, Set<String> ndflPersonIncomesIds) {
        List<NdflPersonPrepayment> findedPrepayments = []
        List<NdflPersonPrepayment> toReturn = []
        if (priznakF == "1") {
            findedPrepayments = ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, taxRate, startDate, endDate, true)
        } else {
            findedPrepayments = ndflPersonService.findPrepaymentsByPeriodAndNdflPersonId(ndflPersonId, taxRate, startDate, endDate, false)
        }
        for (NdflPersonPrepayment prepayment in findedPrepayments) {
            if (ndflPersonIncomesIds.contains(prepayment.operationId)) {
                toReturn << prepayment
            }
        }
        return toReturn
    }

// группирока по налоговой ставке
    Map<Integer, List<NdflPersonIncome>> groupByTaxRate(List<NdflPersonIncome> incomes) {
        Map<Integer, List<NdflPersonIncome>> toReturn = [:]
        List<Integer> rates = []
        incomes.each { NdflPersonIncome income ->
            if (!rates.contains(income.taxRate)) {
                rates << income.taxRate
            }
        }
        rates.each { Integer rate ->
            List<NdflPersonIncome> pickedIncomes = []
            for (NdflPersonIncome income in incomes) {
                if (income.taxRate.equals(rate)) {
                    pickedIncomes << income
                }
            }
            toReturn.put(rate, pickedIncomes)
        }
        return toReturn
    }
/**
 * Метод возвращает мапу, где ключ Integer сооветстувующий значениям месяцев из класа java.util.Calendar,
 * а значение мапа, где ключ код дохода, а значение список соответствующих объектов NdflPersonIncome
 * @param incomes
 * @return
 */
    Map<Integer, Map<String, List<NdflPersonIncome>>> groupIncomesByMonth(List<NdflPersonIncome> incomes) {
        Map<Integer, List<NdflPersonIncome>> groupByMonth = [:]
        List<Integer> monthes = []
        Map<Integer, Map<String, List<NdflPersonIncome>>> toReturn = [:]
        incomes.each { NdflPersonIncome income ->
            Integer month = income.incomeAccruedDate.getMonthOfYear()
            if (!monthes.contains(month)) {
                monthes.add(month)
            }
        }
        monthes = monthes.sort()
        monthes.each { Integer month ->
            List<NdflPersonIncome> pickedIncomes = []
            for (NdflPersonIncome income in incomes) {
                if (month == income.incomeAccruedDate.getMonthOfYear()) {
                    pickedIncomes.add(income)
                }
                groupByMonth.put(month, pickedIncomes)
            }
        }
        groupByMonth.keySet().each { Integer key ->
            List<NdflPersonIncome> gm = groupByMonth.get(key)
            Map<String, List<NdflPersonIncome>> groupByIncomeCode = [:]
            List<String> incomeCodes = []
            gm.each { NdflPersonIncome incomesItem ->
                if (!incomeCodes.contains(incomesItem.incomeCode)) {
                    incomeCodes << incomesItem.incomeCode
                }
            }
            incomeCodes.each { String incomeCode ->
                List<NdflPersonIncome> pickedIncomes = []
                for (NdflPersonIncome income in gm) {
                    if (income.incomeCode.equals(incomeCode)) {
                        pickedIncomes.add(income)
                    }
                }
                groupByIncomeCode.put(incomeCode, pickedIncomes)
            }
            toReturn.put(key, groupByIncomeCode)
        }
        return toReturn
    }

// Группировка по коду вычета
    Map<String, List<NdflPersonDeduction>> groupByDeductionTypeCode(List<NdflPersonDeduction> deductions) {
        Map<String, List<NdflPersonDeduction>> toReturn = [:]
        List<String> typeCodes = []
        deductions.each { NdflPersonDeduction deduction ->
            if (!typeCodes.contains(deduction.typeCode)) {
                typeCodes << deduction.typeCode
            }
        }
        typeCodes.each { String typeCode ->
            List<NdflPersonDeduction> pickedDeductions = []
            for (NdflPersonDeduction deduction in deductions) {
                if (deduction.typeCode.equals(typeCode)) {
                    pickedDeductions.add(deduction)
                }
            }
            toReturn.put(typeCode, pickedDeductions)
        }
        return toReturn
    }

    Map<String, List<NdflPersonDeduction>> unionDeductionsForDeductionType(Map<String, List<NdflPersonDeduction>> deductionGroups) {
        Map<String, List<NdflPersonDeduction>> toReturn = [:]
        deductionGroups.keySet().each { String key ->
            List<NdflPersonDeduction> tempDeductions = []
            deductionGroups.get(key).each { NdflPersonDeduction deduction1 ->
                if (!notifPresent(deduction1, tempDeductions)) {
                    tempDeductions << deduction1
                }
            }
            toReturn.put(key, tempDeductions)
        }
        return toReturn
    }

    boolean notifPresent(NdflPersonDeduction deduction, List<NdflPersonDeduction> deductions) {
        boolean toReturn = false
        deductions.each { NdflPersonDeduction item ->
            if (item.typeCode == deduction.typeCode && item.notifType == deduction.notifType
                    && item.notifDate == deduction.notifDate && item.notifNum == deduction.notifNum
                    && item.notifSource == deduction.notifSource && item.notifSumm == deduction.notifSumm) {
                toReturn = true
                return toReturn
            }
        }
        return toReturn
    }

    List<Object> getSvSumVich(List<NdflPersonDeduction> deductionsFilteredForCurrIncome) {
        List<Object> toReturn = []
        deductionsFilteredForCurrIncome.each { NdflPersonDeduction item ->
            List<NdflPersonDeduction> deductionsForSum = []
            if (item.periodCurrSumm != null && !item.periodCurrSumm.equals(new BigDecimal(0))) {
                deductionsForSum << item
            }

            if (!deductionsForSum.isEmpty()) {
                toReturn << [КодВычет: deductionsForSum.get(0).typeCode,
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
    BigDecimal getNalBaza(List<NdflPersonIncome> incomes, Integer taxRate) {
        BigDecimal toReturn = new BigDecimal(0)
        incomes.each { NdflPersonIncome item ->
            if (item.taxBase != null && item.taxRate == taxRate) {
                toReturn = toReturn.add(item.taxBase)
            }
        }
        return toReturn
    }

//Вычислить сумму для НалИсчисл
    BigDecimal getNalIschisl(String priznakF, List<NdflPersonIncome> incomes, int taxRate) {
        BigDecimal toReturn = new BigDecimal(0)
        if (priznakF == "1") {
            incomes.each { NdflPersonIncome item ->
                if (item.calculatedTax != null && taxRate == item.taxRate) {
                    toReturn = toReturn.add(item.calculatedTax)
                }
            }
        } else if (priznakF == "2") {
            incomes.each { NdflPersonIncome item ->
                if (item.notHoldingTax != null && item.notHoldingTax > 0 && taxRate == item.taxRate) {
                    toReturn = toReturn.add(item.notHoldingTax)
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
    BigDecimal getAvansPlatFix(List<NdflPersonPrepayment> prepayments) {
        BigDecimal toReturn = new BigDecimal(0)
        prepayments.each { NdflPersonPrepayment item ->
            if (item.summ != null) {
                toReturn = toReturn.add(item.summ)
            }
        }
        return toReturn
    }

/**
 * /Вычислить сумму вычета в текущем периоде
 * @param deductions
 * @return
 */
    BigDecimal getDeductionCurrPeriodSum(List<NdflPersonDeduction> deductions) {
        BigDecimal toReturn = new BigDecimal(0)
        deductions.each { NdflPersonDeduction item ->
            if (item.periodCurrSumm != null) {
                toReturn = toReturn.add(item.periodCurrSumm)
            }
        }
        return toReturn
    }

//Вычислить сумму для НалУдерж
    BigDecimal getNalUderzh(String priznakF, List<NdflPersonIncome> incomes, Date startDate, Date endDate) {
        BigDecimal toReturn = new BigDecimal(0)
        if (priznakF == "1") {
            Map<String, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { NdflPersonIncome income -> income.operationId }
            // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода
            incomesGroupedByOperationId.each { String k, List<NdflPersonIncome> v ->
                boolean correctDate = false
                v.each { NdflPersonIncome income ->
                    if (income.incomeAccruedDate >= new LocalDateTime(startDate) && income.incomePayoutDate < new LocalDateTime(endDate)) {
                        correctDate = true
                    }
                }
                if (correctDate) {
                    v.each { NdflPersonIncome income ->
                        if (income.withholdingTax != null) {
                            toReturn = toReturn.add(income.withholdingTax)
                        }
                    }
                }
            }
        }
        return toReturn
    }

//Вычислить сумму для НалПеречисл
    Long getNalPerechisl(String priznakF, List<NdflPersonIncome> incomes, Date startDate, Date endDate) {
        Long toReturn = 0L
        if (priznakF == "1") {
            Map<String, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { NdflPersonIncome income -> income.operationId }
            // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода
            incomesGroupedByOperationId.each { String k, List<NdflPersonIncome> v ->
                boolean correctDate = false
                v.each { NdflPersonIncome income ->
                    if (income.incomeAccruedDate >= new LocalDateTime(startDate) && income.incomePayoutDate < new LocalDateTime(endDate)) {
                        correctDate = true
                    }
                }
                if (correctDate) {
                    v.each { NdflPersonIncome income ->
                        if (income.taxSumm != null) {
                            toReturn += income.taxSumm
                        }
                    }
                }
            }
        }
        return toReturn
    }

//Вычислить сумму для НалУдержЛиш
    BigDecimal getNalUderzhLish(String priznakF, List<NdflPersonIncome> incomes, Date startDate, Date endDate) {
        BigDecimal toReturn = new BigDecimal(0)
        if (priznakF == "1") {
            Map<String, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { NdflPersonIncome income -> income.operationId }
            // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода
            incomesGroupedByOperationId.each { String k, List<NdflPersonIncome> v ->
                boolean correctDate = false
                v.each { NdflPersonIncome income ->
                    if (income.incomeAccruedDate >= new LocalDateTime(startDate) && income.incomePayoutDate < new LocalDateTime(endDate)) {
                        correctDate = true
                    }
                }
                if (correctDate) {
                    v.each { NdflPersonIncome income ->
                        if (income.overholdingTax != null) {
                            toReturn = toReturn.add(income.overholdingTax)
                        }
                    }
                }
            }
        }
        return toReturn
    }

//Вычислить сумму для НалНеУдерж
    BigDecimal getNalNeUderzh(String priznakF, List<NdflPersonIncome> incomes, Date startDate, Date endDate) {
        BigDecimal toReturn = new BigDecimal(0)
        Map<String, List<NdflPersonIncome>> incomesGroupedByOperationId = incomes.groupBy { NdflPersonIncome income -> income.operationId }
        // начисление дохода производится в рамках текущего отчетного периода, а дата удержания налога не превышает последний день февраля следующего периода

        incomesGroupedByOperationId.each { String k, List<NdflPersonIncome> v ->
            boolean correctDate = false
            v.each { NdflPersonIncome income ->
                if (income.incomeAccruedDate >= new LocalDateTime(startDate) && income.incomePayoutDate < new LocalDateTime(endDate)) {
                    correctDate = true
                }
            }
            if (priznakF == "1" && correctDate) {
                v.each { NdflPersonIncome income ->
                    if (income.notHoldingTax != null) {
                        toReturn = toReturn.add(income.notHoldingTax)
                    }
                }
            } else if (priznakF == "2" && correctDate) {
                boolean calculatedTaxGreaterZero = false
                v.each { NdflPersonIncome income ->
                    if (income.calculatedTax > 0) {
                        calculatedTaxGreaterZero = true
                    }
                }
                if (calculatedTaxGreaterZero) {
                    v.each { NdflPersonIncome income ->
                        if (income.notHoldingTax != null) {
                            toReturn = toReturn.add(income.notHoldingTax)
                        }
                    }
                }
            }
        }
        return toReturn
    }

    Date getFirstMarchOfNextPeriod(Date periodEndDate) {
        Calendar calendar = new GregorianCalendar()
        calendar.setTime(periodEndDate)
        int currYear = calendar.get(Calendar.YEAR)
        Calendar returnCalendar = new GregorianCalendar();
        returnCalendar.set(currYear + 1, 02, 01)
        return returnCalendar.getTime()
    }

    BigDecimal getSumVichOfPeriodCurrSumm(List<NdflPersonDeduction> deductions) {
        BigDecimal toReturn = new BigDecimal(0)
        deductions.each { NdflPersonDeduction item ->
            toReturn = toReturn.add(item.periodCurrSumm)
        }
        return toReturn
    }

    Map<String, RefBookValue> getOktmoById(Long id) {
        Map<String, RefBookValue> oktmo = OKTMO_CACHE.get(id)
        if (oktmo == null && id != null) {
            Date rpe = getReportPeriodEndDate(declarationData.reportPeriodId)
            RefBookDataProvider provider = getProvider(RefBook.Id.OKTMO.id)
            PagingResult<Map<String, RefBookValue>> oktmoList = provider.getRecords(rpe, null, "ID = ${id}", null)
            if (oktmoList.size() != 0) {
                oktmo = oktmoList.get(0)
                OKTMO_CACHE.put(id, oktmo)
            }

        }
        return oktmo
    }

    Map<Long, Map<String, RefBookValue>> getOktmoByIdList(List<Long> idList) {
        RefBookDataProvider provider = getProvider(RefBook.Id.OKTMO.id)
        return provider.getRecordData(idList)
    }

// Получить параметры подразделения (из справочника 950)
    Map<String, RefBookValue> getDepartmentParam(Integer departmentId, Integer reportPeriodId, boolean throwIfEmpty) {
        if (!departmentCache.containsKey(departmentId)) {
            Date rpe = getReportPeriodEndDate(reportPeriodId)
            RefBookDataProvider provider = getProvider(RefBook.Id.NDFL.id)
            PagingResult<Map<String, RefBookValue>> departmentParamList = provider.getRecords(rpe, null, "DEPARTMENT_ID = $departmentId", null)
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
    def getDepartmentParamDetails(Long departmentParamId, Integer departmentId, Integer reportPeriodId) {
        if (departmentParamRow == null) {
            PagingResult<Map<String, RefBookValue>> departmentParamTableList = getProvider(RefBook.Id.NDFL_DETAIL.id).getRecords(getReportPeriodEndDate(reportPeriodId), null, "REF_BOOK_NDFL_ID = $departmentParamId".toString(), null)
            if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
                departmentParamException(departmentId, reportPeriodId)
            }
            List<Long> referencesOktmoList = (List<Long>) departmentParamTableList.OKTMO?.value
            referencesOktmoList.removeAll([null])
            Map<Long, Map<String, RefBookValue>> oktmoForDepartment = getOktmoByIdList(referencesOktmoList)
            departmentParamRow = departmentParamTableList.find { Map<String, RefBookValue> dep ->
                Map<String, RefBookValue> oktmo = oktmoForDepartment.get((Long) dep.OKTMO?.value)
                if (oktmo != null) {
                    declarationData.kpp.equals(dep.KPP?.stringValue) && declarationData.oktmo.equals(oktmo.CODE.value)
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
    Map<String, RefBookValue> getDepartmentParamDetails(String kpp, String oktmo) {
        if (departmentParamRow == null) {
            List<Map<String, RefBookValue>> departmentParamRowList = []
            PagingResult<Map<String, RefBookValue>> oktmoReferenceList = getProvider(RefBook.Id.OKTMO.id).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "CODE = '$oktmo'".toString(), null)
            Long oktmoReference = (Long) oktmoReferenceList.get(oktmoReferenceList.size() - 1).id.value
            List<Department> deps = departmentService.getAllChildren(declarationData.departmentId)
            deps.each { Department item ->
                PagingResult<Map<String, RefBookValue>> departmentParamList = getProvider(RefBook.Id.NDFL.id).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "DEPARTMENT_ID = ${item.id}".toString(), null)
                if (departmentParamList != null && departmentParamList.size() > 0) {
                    Map<String, RefBookValue> departmentParamRow = departmentParamList.get(0)
                    if (departmentParamRow != null) {
                        departmentParamRowList.addAll(getProvider(RefBook.Id.NDFL_DETAIL.id).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "REF_BOOK_NDFL_ID = ${departmentParamRow.id} and OKTMO = $oktmoReference AND KPP = '$kpp'".toString(), null))
                    }
                }
            }

            if (departmentParamRowList == null || departmentParamRowList.isEmpty()) {
                departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
            }

            departmentParamRow = departmentParamRowList.get(0)
        }

        return departmentParamRow
    }
/**
 * Получение провайдера с использованием кеширования
 * @param providerId
 * @return
 */

    RefBookDataProvider getProvider(Long providerId) {
        if (!providerCache.containsKey(providerId)) {
            RefBookDataProvider provider = refBookFactory.getDataProvider(providerId)
            providerCache.put(providerId, provider)
        }
        return providerCache.get(providerId)
    }

/**
 * Получить дату начала отчетного периода
 * @return
 */
    Date getReportPeriodStartDate() {
        return reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
    }

/**
 * Получение даты окончания периода
 * @param reportPeriodId
 * @return
 */

    Date getReportPeriodEndDate(Integer reportPeriodId) {
        if (reportPeriodEndDate == null) {
            reportPeriodEndDate = reportPeriodService?.getEndDate(reportPeriodId)?.getTime()
        }
        return reportPeriodEndDate
    }

// Получить вид формы
    Map<String, RefBookValue> getFormType(Long id) {
        if (formType == null) {
            formType = getProvider(RefBook.Id.DECLARATION_DATA_TYPE_REF_BOOK.id).getRecordData(id)
        }
        return formType;
    }

/**
 * Определить признакФ
 */

    String definePriznakF() {
        String code = formType?.CODE?.stringValue
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
    Integer getDeductionType(String code) {
        String filter = "CODE = '$code'".toString()
        PagingResult<Map<String, RefBookValue>> deductionTypeList = getProvider(RefBook.Id.DEDUCTION_TYPE.id).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, filter, null)
        if (deductionTypeList == null || deductionTypeList.size() == 0 || deductionTypeList.get(0) == null) {
            throw new Exception("Ошибка при получении кодов вычета. Коды вычета заполнены не полностью")
        }
        return (Integer) deductionTypeList.get(deductionTypeList.size() - 1).DEDUCTION_MARK?.value
    }

/**
 * Получить признак вычета
 * @param id
 * @return
 */
    String getDeductionMark(Long id) {
        getProvider(RefBook.Id.DEDUCTION_MARK.id).getRecordData(id).NAME?.stringValue
    }

/************************************* СОЗДАНИЕ ФОРМЫ *****************************************************************/
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
            if (!dd.state.equals(State.ACCEPTED) && mapDepartmentNotExistRnu[departmentCode] != null) {
                listDepartmentNotAcceptedRnu << mapDepartmentNotExistRnu[departmentCode]
            }
            mapDepartmentNotExistRnu.remove(departmentCode)
        }

        // Период
        def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        def reportPeriod = departmentReportPeriod.reportPeriod
        Map<String, RefBookValue> period = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)
        String periodCode = period?.CODE?.stringValue
        String periodName = period?.NAME?.stringValue
        def calendarStartDate = reportPeriod?.calendarStartDate
        String correctionDateExpression = departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format(DATE_FORMAT_DOTTED)},"
        if (!mapDepartmentNotExistRnu.isEmpty()) {
            def listDepartmentNotExistRnu = []
            mapDepartmentNotExistRnu.each {
                listDepartmentNotExistRnu.add(it.value)
            }
            logger.warn("За период $periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")}" +
                    " года" + correctionDateExpression + " не созданы экземпляры консолидированных налоговых форм для следующих ТБ: \"${listDepartmentNotExistRnu.join("\", \"")}\"" +
                    ". Данные этих форм не включены в отчетность!")
        }

        if (!listDepartmentNotAcceptedRnu.isEmpty()) {
            logger.warn("За период $periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")}" +
                    " года" + correctionDateExpression + " имеются не принятые экземпляры консолидированных налоговых форм для следующих ТБ: \"${listDepartmentNotAcceptedRnu.join("\", \"")}\"" +
                    ". Данные этих форм не включены в отчетность!")
        }
    }

    final int RNU_NDFL_DECLARATION_TYPE = 101

    def departmentParamTableList = null

    Map<Long, PagingResult<Map<String, RefBookValue>>> departmentParamTableListCache = [:]

    def createForm() {
        try {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
            def declarationTypeId = currDeclarationTemplate.type.id
            // Мапа где значение физлица для каждой пары КПП и ОКТМО
            Map<PairKppOktmo, List<NdflPerson>> ndflPersonsIdGroupedByKppOktmo = getNdflPersonsGroupedByKppOktmo()

            // Удаление ранее созданных отчетных форм
            List<Pair<Long, DeclarationDataReportType>> notDeletedDeclarationPair = declarationService.deleteForms(declarationTypeId, declarationData.departmentReportPeriodId, logger, userInfo)
            if (!notDeletedDeclarationPair.isEmpty()) {
                logger.error("Невозможно выполнить повторное создание отчетных форм. Заблокировано удаление ранее созданных отчетных форм выполнением операций:")
                notDeletedDeclarationPair.each() {
                    logger.error("Форма %d, выполняется операция \"%s\"",
                            it.first, declarationService.getDeclarationFullName(it.first, it.second)
                    )
                }
                logger.error("Дождитесь завершения выполнения операций или выполните отмену операций вручную.")
                return
            }

            if (ndflPersonsIdGroupedByKppOktmo == null || ndflPersonsIdGroupedByKppOktmo.isEmpty()) {
                return
            }
            checkPresentedPairsKppOktmo()
            // Создание ОНФ для каждой пары КПП и ОКТМО
            ndflPersonsIdGroupedByKppOktmo.each { Map.Entry<PairKppOktmo, List<NdflPerson>> npGroup ->
                ScriptUtils.checkInterrupted();
                def oktmo = npGroup.key.oktmo
                def kpp = npGroup.key.kpp
                String taxOrganCode = npGroup.key.taxOrganCode

                List<List<NdflPerson>> npGroupValue = npGroup.value.collate(NUMBER_OF_PERSONS)
                Integer partTotal = npGroupValue.size()
                npGroupValue.eachWithIndex { List<NdflPerson> part, Integer index ->
                    ScriptUtils.checkInterrupted();
                    List<Long> npGropSourcesIdList = part.id
                    if (npGropSourcesIdList == null || npGropSourcesIdList.isEmpty()) {
                        if (departmentReportPeriod.correctionDate != null) {
                            logger.info("Для КПП $kpp - ОКТМО $oktmo отсутствуют данные физических лиц, содержащих ошибки от ФНС в справке 2НДФЛ. Создание формы 2НДФЛ невозможно.")
                        }
                        return;
                    }
                    Map<String, Object> params
                    Long ddId
                    Integer indexFrom1 = ++index
                    def note = "Часть ${indexFrom1} из ${partTotal}"
                    params = new HashMap<String, Object>()
                    ddId = declarationService.create(logger, declarationData.declarationTemplateId, userInfo,
                            departmentReportPeriodService.get(declarationData.departmentReportPeriodId), taxOrganCode, kpp.toString(), oktmo.toString(), null, null, note.toString(), false)
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

// Пары КПП/ОКТМО отсутствующие в справочнике настройки подразделений
    def checkPresentedPairsKppOktmo() {
        declarationDataConsolidated = declarationDataConsolidated ?: declarationService.find(RNU_NDFL_DECLARATION_TYPE, declarationData.departmentReportPeriodId).get(0)
        List<Pair<String, String>> kppOktmoNotPresentedInRefBookList = declarationService.findNotPresentedPairKppOktmo(declarationDataConsolidated.id);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        Department department = departmentService.get(departmentReportPeriod.getDepartmentId())
        for (Pair<String, String> kppOktmoNotPresentedInRefBook : kppOktmoNotPresentedInRefBookList) {
            logger.warn("Для подразделения %s отсутствуют настройки подразделений для КПП: %s, ОКТМО: %s в справочнике \"Настройки подразделений\". Данные формы РНУ НДФЛ (консолидированная) № %d по указанным КПП и ОКТМО источника выплаты не включены в отчетность.",
                    department.getName(), kppOktmoNotPresentedInRefBook.getFirst(), kppOktmoNotPresentedInRefBook.getSecond(), declarationDataConsolidated.id)
        }
    }

    Map<PairKppOktmo, List<NdflPerson>> getNdflPersonsGroupedByKppOktmo() {
        def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        def pairKppOktmoList = getPairKppOktmoList()
        if (pairKppOktmoList == null) {
            return null
        }
        def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        formType = getFormType(currDeclarationTemplate.declarationFormTypeId)
        Map<String, RefBookValue> departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false)
        String depName = departmentService.get((Integer) departmentParam.DEPARTMENT_ID.value)?.name
        def reportPeriod = departmentReportPeriod.reportPeriod
        def otchetGod = reportPeriod.taxPeriod.year
        String strCorrPeriod = ""
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
        }
        pairKppOktmoSize = pairKppOktmoList?.size() ?: 0
        // Все подходящие КНФ
        List<DeclarationData> allDeclarationData = findAllTerBankDeclarationData(departmentReportPeriod)
        if (allDeclarationData == null) {
            return null
        }
        /*declarationService.findDeclarationDataIdByTypeStatusReportPeriod(declarationData.reportPeriodId, departmentParam?.id.value,
RNU_NDFL_DECLARATION_TYPE, DepartmentType.TERR_BANK.getCode(),
Boolean.TRUE, State.ACCEPTED.getId())*/
        // Мапа где значение физлица для каждой пары КПП и ОКТМО
        def ndflPersonsIdGroupedByKppOktmo = [:]
        if (!allDeclarationData.isEmpty()) {
            pairKppOktmoList.each { PairKppOktmo pair ->
                ScriptUtils.checkInterrupted();
                // Поиск физлиц по КПП и ОКТМО операций относящихся к ФЛ

                List<NdflPerson> ndflPersons
                List<Long> idList = []
                for (DeclarationData dd in allDeclarationData) {
                    idList.add(dd.id)
                }
                if (declarationData.declarationTemplateId == NDFL_2_2_DECLARATION_TYPE) {
                    ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(idList, pair.kpp.toString(), pair.oktmo.toString(), true)
                } else {
                    ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo(idList, pair.kpp.toString(), pair.oktmo.toString(), false)
                }

                if (ndflPersons != null && !ndflPersons.isEmpty()) {
                    if (departmentReportPeriod.correctionDate != null) {
                        def ndflPersonsPicked = []
                        ndflReferencesWithError.each { Map<String, RefBookValue> reference ->
                            ndflPersons.each { person ->
                                if (((Long) reference.PERSON_ID?.value).equals(person.personId)) {
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
                        logger.warn("Не удалось создать форму $reportType, за период $otchetGod ${reportPeriod.name}$strCorrPeriod, подразделение: ${depChildName ?: ""}, КПП: ${pair.kpp}, ОКТМО: ${pair.oktmo}. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName, за период $otchetGod ${reportPeriod.name}$strCorrPeriod отсутствуют операции, содержащие сведения о не удержанном налоге для указанных КПП и ОКТМО.")
                    } else {
                        logger.warn("Не удалось создать форму $reportType, за период $otchetGod ${reportPeriod.name}$strCorrPeriod, подразделение: ${depChildName ?: ""}, КПП: ${pair.kpp}, ОКТМО: ${pair.oktmo}. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName, за период $otchetGod ${reportPeriod.name}$strCorrPeriod отсутствуют операции для указанных КПП и ОКТМО.")
                    }
                }
            }
        }
        if (ndflPersonsIdGroupedByKppOktmo == null || ndflPersonsIdGroupedByKppOktmo.isEmpty()) {
            if (declarationData.declarationTemplateId == NDFL_2_2_DECLARATION_TYPE) {
                logger.error("Отчетность $reportType  для $depName за период $otchetGod ${reportPeriod.name}$strCorrPeriod не сформирована. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName за период $otchetGod ${reportPeriod.name}$strCorrPeriod отсутствуют операции, содержащие сведения о не удержанном налоге.")
            } else {
                logger.error("Отчетность $reportType  для $depName за период $otchetGod ${reportPeriod.name}$strCorrPeriod не сформирована. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName за период $otchetGod ${reportPeriod.name}$strCorrPeriod отсутствуют операции.")
            }
            checkPresentedPairsKppOktmo()
        }
        return ndflPersonsIdGroupedByKppOktmo
    }

    List<PairKppOktmo> getPairKppOktmoList() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        def pairKppOktmoList = []
        def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        formType = getFormType(currDeclarationTemplate.declarationFormTypeId)
        def priznakF = definePriznakF()
        def declarationTypeId = currDeclarationTemplate.type.id
        Map<String, RefBookValue> departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false)
        String depName = departmentService.get((Integer) departmentParam.DEPARTMENT_ID.value).name
        def reportPeriod = departmentReportPeriod.reportPeriod
        def otchetGod = reportPeriod.taxPeriod.year
        // Поиск КПП и ОКТМО для корр периода
        if (departmentReportPeriod.correctionDate != null) {
            List<DeclarationData> declarations = []

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

            List<DeclarationData> declarationsForRemove = []
            declarations.each { DeclarationData declaration ->
                ScriptUtils.checkInterrupted()

                Long stateDocReject = (Long) getProvider(RefBook.Id.DOC_STATE.id).getRecords(null, null, "NAME = 'Отклонен'", null).get(0).id.value
                Long stateDocNeedClarify = (Long) getProvider(RefBook.Id.DOC_STATE.id).getRecords(null, null, "NAME = 'Требует уточнения'", null).get(0).id.value
                Long stateDocError = (Long) getProvider(RefBook.Id.DOC_STATE.id).getRecords(null, null, "NAME = 'Ошибка'", null).get(0).id.value
                DeclarationTemplate declarationTemplate = declarationService.getTemplate(declaration.declarationTemplateId)
                if (!(declarationTemplate.declarationFormKind == DeclarationFormKind.REPORTS && (declaration.docState == stateDocReject
                        || declaration.docState == stateDocNeedClarify || declaration.docState == stateDocError))) {
                    declarationsForRemove << declaration
                }
            }
            declarations.removeAll(declarationsForRemove)

            if (declarations.isEmpty() && formDataEvent == FormDataEvent.CREATE_FORMS) {
                createCorrPeriodNotFoundMessage(departmentReportPeriod, true)
                return null
            }

            // список НФ у которых нет справок с ошибками
            List<DeclarationData> declarationWithoutNdflReferencesWithError = []
            declarations.each { DeclarationData item ->
                ScriptUtils.checkInterrupted();
                PagingResult<Map<String, RefBookValue>> ndflReferencesWithErrorForDeclarationData = getNdflReferencesWithError(item.id)
                if (ndflReferencesWithErrorForDeclarationData.isEmpty()) {
                    declarationWithoutNdflReferencesWithError.add(item)
                    if (formDataEvent == FormDataEvent.CREATE_FORMS) {
                        createNdflReferencesNotFoundMessageForDeclarationData(departmentReportPeriod, item)
                    }
                }
                ndflReferencesWithError.addAll(getNdflReferencesWithError(item.id))
            }

            if (ndflReferencesWithError.isEmpty() && formDataEvent == FormDataEvent.CREATE_FORMS) {
                createCorrPeriodNotFoundMessage(departmentReportPeriod, false)
                return null
            }

            declarations.removeAll(declarationWithoutNdflReferencesWithError)

            declarations.each { DeclarationData declaration ->
                PairKppOktmo pairKppOktmo = new PairKppOktmo(declaration.kpp, declaration.oktmo, declaration.taxOrganCode)
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
            PagingResult<Map<String, RefBookValue>> departmentParamTableList = new PagingResult<Map<String, RefBookValue>>()
            for (Department childrenDepartment in childrenDepartments) {
                departmentParam = getDepartmentParam(childrenDepartment.id, departmentReportPeriod.reportPeriod.id, false)
                if (departmentParam != null) {
                    departmentParamTableList.addAll(getDepartmentParamDetailsList((Long) departmentParam?.id.value, departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false))
                    referencesOktmoList.addAll(departmentParamTableList.OKTMO?.value)
                }
            }
            referencesOktmoList.removeAll([null])
            if (referencesOktmoList.isEmpty()) {
                logger.error("Отчетность %s для %s за период %s не сформирована. Отсутствуют настройки указанного подразделения в справочнике \"Настройки подразделений", reportType, depName, "$otchetGod ${reportPeriod.name}")
                return null
            }
            Map<Long, Map<String, RefBookValue>> oktmoForDepartment = getOktmoByIdList(referencesOktmoList)
            for (Map<String, RefBookValue> dep : departmentParamTableList) {
                ScriptUtils.checkInterrupted();
                if (dep.OKTMO?.value != null) {
                    Map<String, RefBookValue> oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
                    if (oktmo != null) {
                        PairKppOktmo pairKppOktmo = new PairKppOktmo(dep.KPP?.stringValue, oktmo.CODE?.stringValue, dep?.TAX_ORGAN_CODE?.stringValue)
                        if (!pairKppOktmoList.contains(pairKppOktmo)) {
                            pairKppOktmoList << pairKppOktmo
                        }
                    }
                }
            }
            if (pairKppOktmoList.isEmpty()) {
                logger.error("Отчетность %s для %s за период %s не сформирована. Отсутствуют настройки указанного подразделения в справочнике \"Настройки подразделений", reportType, depName, "$otchetGod ${reportPeriod.name}")
                return null
            }
        }
        return pairKppOktmoList
    }

/**
 * Добавляет в логгер сообщение о том что не найдены формы для корректирующего периода
 * @param departmentReportPeriod
 * @param forDepartment
 * @return
 */
    def createCorrPeriodNotFoundMessage(DepartmentReportPeriod departmentReportPeriod, boolean forDepartment) {
        DepartmentReportPeriod prevDrp = getPrevDepartmentReportPeriod(departmentReportPeriod)
        Department department = departmentService.get(departmentReportPeriod.departmentId)
        String correctionDateExpression = getCorrectionDateExpression(departmentReportPeriod)
        if (forDepartment) {
            logger.error("Уточненная отчетность $reportType для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для подразделения ${department.name} и периода ${prevDrp.reportPeriod.taxPeriod.year}, ${prevDrp.reportPeriod.name}" + getCorrectionDateExpression(prevDrp) + " не найдены отчетные формы, \"Состояние ЭД\" которых равно \"Отклонен\", \"Требует уточнения\" или \"Ошибка\".")
        } else {
            logger.error("Уточненная отчетность $reportType для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для заданного В отчетных формах подразделения ${department.name} и периода ${prevDrp.reportPeriod.taxPeriod.year}, ${prevDrp.reportPeriod.name}" + getCorrectionDateExpression(prevDrp) + " не найдены физические лица, \"Текст ошибки от ФНС\" которых заполнен. Уточненная отчетность формируется только для указанных физических лиц.")
        }
    }

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
    def addNdflPersons(Map<PairKppOktmo, List<NdflPerson>> ndflPersonsGroupedByKppOktmo, PairKppOktmo pairKppOktmoBeingComparing, List<NdflPerson> ndflPersonList) {
        // Физлица для каждой пары КПП и октмо
        List<NdflPerson> kppOktmoNdflPersons = ndflPersonsGroupedByKppOktmo.get(pairKppOktmoBeingComparing)
        if (kppOktmoNdflPersons == null) {
            ndflPersonsGroupedByKppOktmo.put(pairKppOktmoBeingComparing, ndflPersonList)
        } else {
            def kppOktmoNdflPersonsEntrySet = ndflPersonsGroupedByKppOktmo.entrySet()
            kppOktmoNdflPersonsEntrySet.each { Map.Entry<PairKppOktmo, List<NdflPerson>> item ->
                if (item.getKey().equals(pairKppOktmoBeingComparing)) {
                    if (item.getKey().taxOrganCode != pairKppOktmoBeingComparing.taxOrganCode) {
                        logger.warn("Для КПП = ${pairKppOktmoBeingComparing.kpp} ОКТМО = ${pairKppOktmoBeingComparing.oktmo} в справочнике \"Настройки подразделений\" задано несколько значений Кода НО (кон).")
                    }
                    //Если Коды НО совпадают, для всех дублей пар КПП+ОКТМО создается одна ОНФ, в которой указывается совпадающий Код НО.
                    item.getValue().addAll(ndflPersonList)
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
    List<DeclarationData> findAllTerBankDeclarationData(DepartmentReportPeriod departmentReportPeriod) {
        List<Integer> allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.id)
        List<DeclarationData> allDeclarationData = []
        allDepartmentReportPeriodIds.each { Integer item ->
            ScriptUtils.checkInterrupted();
            allDeclarationData.addAll(declarationService.find(RNU_NDFL_DECLARATION_TYPE, item))
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
        List<DeclarationData> declarationsForRemove = []
        allDeclarationData.each { DeclarationData declaration ->
            if (declaration.state != State.ACCEPTED) {
                declarationsForRemove << declaration
            }
        }

        allDeclarationData.removeAll(declarationsForRemove)

        return allDeclarationData
    }

    boolean checkExistingAcceptedConsDDForCurrTB(DepartmentReportPeriod departmentReportPeriod, List<DeclarationData> ddList) {
        for (DeclarationData dd : ddList) {
            if (dd.departmentReportPeriodId == departmentReportPeriod.id && dd.state == State.ACCEPTED) {
                return true;
            }
        }
        return false;
    }

    boolean checkExistingConsDDForCurrTB(DepartmentReportPeriod departmentReportPeriod, List<DeclarationData> ddList) {
        for (DeclarationData dd : ddList) {
            if (dd.departmentReportPeriodId == departmentReportPeriod.id) {
                return true;
            }
        }
        return false;
    }

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

    void preCreateReports() {
        ScriptUtils.checkInterrupted()
        List<DeclarationDataFile> declarationDataFileList = declarationService.findFilesWithSpecificType(declarationData.id, OUTCOMING_ATTACH_FILE_TYPE)
        if (declarationDataFileList.size() != 1) {
            paramMap.put("successfullPreCreate", false)
        } else {
            paramMap.put("successfullPreCreate", true)
        }
    }

/*********************************ПОЛУЧИТЬ ИСТОЧНИКИ*******************************************************************/
    ReportPeriod sourceReportPeriod = null

    ReportPeriod getReportPeriod() {
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
        ReportPeriod reportPeriod = getReportPeriod()
        Integer sourceTypeId = 101
        def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        def allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.id)
        List<DeclarationData> tmpDeclarationDataList = []
        allDepartmentReportPeriodIds.each {
            ScriptUtils.checkInterrupted();
            def tmpDepartmentReportPeriod = departmentReportPeriodService.get(it)
            def tmpDeclaration = declarationService.findDeclarationDataByKppOktmoOfNdflPersonIncomes(sourceTypeId, it, tmpDepartmentReportPeriod.departmentId, tmpDepartmentReportPeriod.reportPeriod.id, declarationData.kpp, declarationData.oktmo)
            if (tmpDeclaration != null) {
                tmpDeclarationDataList << tmpDeclaration
            }
        }
        def declarationsForRemove = []
        tmpDeclarationDataList.each { DeclarationData declaration ->
            if (declaration.state != State.ACCEPTED) {
                declarationsForRemove << declaration
            }
        }
        tmpDeclarationDataList.removeAll(declarationsForRemove)
        tmpDeclarationDataList.each { DeclarationData tmpDeclarationData ->
            ScriptUtils.checkInterrupted();
            DepartmentReportPeriod tmpDepartmentReportPeriod = departmentReportPeriodService.get(tmpDeclarationData.departmentReportPeriodId)
            if (tmpDepartmentReportPeriod.correctionDate != departmentReportPeriod.correctionDate) {
                return
            }
            Department department = departmentService.get(tmpDeclarationData.departmentId)
            Relation relation = getRelation(tmpDeclarationData, department, reportPeriod, sourceTypeId)
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
    Relation getRelation(DeclarationData tmpDeclarationData, Department department, ReportPeriod period, Integer sourceTypeId) {
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
    PagingResult<Map<String, RefBookValue>> getDepartmentParamDetailsList(
            Long departmentParamId, Integer departmentId, Integer reportPeriodId, boolean throwIfEmpty) {
        if (!departmentParamTableListCache.containsKey(departmentParamId)) {
            String filter = "REF_BOOK_NDFL_ID = $departmentParamId".toString()
            Date rpe = getReportPeriodEndDate(reportPeriodId)
            RefBookDataProvider provider = getProvider(RefBook.Id.NDFL_DETAIL.id)
            PagingResult<Map<String, RefBookValue>> departmentParamTableList = provider.getRecords(rpe, null, filter, null)
            if ((departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) && throwIfEmpty) {
                departmentParamException(departmentId, reportPeriodId)
            }
            departmentParamTableListCache.put(departmentParamId, departmentParamTableList)
        }
        return departmentParamTableListCache.get(departmentParamId)
    }

// Получить список из реестра справок с ошибкой ФНС
    PagingResult<Map<String, RefBookValue>> getNdflReferencesWithError(declarationDataId) {
        String filter = "DECLARATION_DATA_ID = ${declarationDataId}".toString()
        PagingResult<Map<String, RefBookValue>> allNdflReferences = getProvider(RefBook.Id.NDFL_REFERENCES.id).getRecords(new Date(), null, filter, null)
        List<Map<String, RefBookValue>> ndflReferencesForRemove = []
        allNdflReferences.each { Map<String, RefBookValue> item ->
            if (item.ERRTEXT?.value == null) {
                ndflReferencesForRemove << item
            }
        }
        allNdflReferences.removeAll(ndflReferencesForRemove)
        return allNdflReferences
    }

/**
 * Класс инкапсулирующий информацию о КПП, ОКТМО и кон налогового органа подразделения
 */
    class PairKppOktmo {
        String kpp
        String oktmo
        String taxOrganCode

        PairKppOktmo(String kpp, String oktmo, String taxOrganCode) {
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
    Map<String, Map<String, RefBookValue>> refBookCache = [:]

/**
 * Разыменование записи справочника
 */
    Map<String, RefBookValue> getRefBookValue(Long refBookId, Long recordId) {
        return refBookService.getRefBookValue(refBookId, recordId, refBookCache)
    }
/************************************* СПЕЦОТЧЕТ **********************************************************************/


    final String ALIAS_PRIMARY_RNU_W_ERRORS = "primary_rnu_w_errors"

    final String TRANSPORT_FILE_TEMPLATE = "ТФ"

// Мапа где ключ идентификатор NdflPerson, значение NdflPerson соответствующий идентификатору
    Map<Long, NdflPerson> ndflpersonFromRNUPrimary = [:]

//------------------ PREPARE_SPECIFIC_REPORT ----------------------

    def prepareSpecificReport() {
        def reportAlias = scriptSpecificReportHolder?.declarationSubreport?.alias;
        if (SubreportAliasConstants.REPORT_2NDFL != reportAlias) {
            throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
        }
        PrepareSpecificReportResult result = new PrepareSpecificReportResult();
        List<Column> tableColumns = createTableColumns();
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        List<Column> rowColumns = createRowColumns()

        //Проверка, подготовка данных
        def params = scriptSpecificReportHolder.subreportParamValues
        Map<String, Object> reportParameters = scriptSpecificReportHolder.getSubreportParamValues();

        if (reportParameters.isEmpty()) {
            throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.");
        }

        Map<String, Object> resultReportParameters = [:]
        reportParameters.each { String key, Object value ->
            if (value != null) {
                if (key == SubreportAliasConstants.TO_BIRTHDAY || key == SubreportAliasConstants.FROM_BIRTHDAY) {
                    resultReportParameters.put(key, ScriptUtils.formatDate((Date) value, "dd.MM.yyyy"))
                } else {
                    resultReportParameters.put(key, value)
                }
            }
        }

        // Ограничение числа выводимых записей
        int pageSize = 10

        // Поиск данных по фильтру
        List docs = searchData(resultReportParameters, pageSize, result)

        dataRows.addAll(addRows(docs, rowColumns));

        result.setTableColumns(tableColumns);
        result.setDataRows(dataRows);
        scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
        scriptSpecificReportHolder.setSubreportParamValues(params)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    List<DataRow<Cell>> addRows(List docs, List<Column> rowColumns) {
        // Формирование списка данных для вывода в таблицу
        List<DataRow<Cell>> toReturn = []
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
            toReturn.add(row)
        }
        return toReturn
    }

    String getPersonStatusName(String statusCode) {
        RefBookDataProvider provider = getProvider(RefBook.Id.TAXPAYER_STATUS.getId())
        PagingResult<Map<String, RefBookValue>> record = provider.getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "CODE = '$statusCode'".toString(), null)
        return record.get(0).get("NAME").getValue()
    }

    List<Column> createTableColumns() {
        List<Column> tableColumns = new ArrayList<Column>()

        Column pNumSpravka = new StringColumn()
        pNumSpravka.setAlias("pNumSpravka")
        pNumSpravka.setName("№ справки 2НДФЛ")
        pNumSpravka.setWidth(13)
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
        column5.setWidth(11)
        tableColumns.add(column5)

        Column column6 = new StringColumn()
        column6.setAlias("idDocNumber")
        column6.setName("№ ДУЛ")
        column6.setWidth(10)
        tableColumns.add(column6)

        Column column7 = new StringColumn()
        column7.setAlias("statusNp")
        column7.setName("Статус налогоплательщика")
        column7.setWidth(25)
        tableColumns.add(column7)

        Column column8 = new StringColumn()
        column8.setAlias("innForeign")
        column8.setName("ИНН Страны гражданства")
        column8.setWidth(20)
        tableColumns.add(column8)

        return tableColumns;
    }

    List<Column> createRowColumns() {
        List<Column> tableColumns = new ArrayList<Column>();

        Column pNumSpravka = new StringColumn()
        pNumSpravka.setAlias("pNumSpravka")
        pNumSpravka.setName("pNumSpravka")
        pNumSpravka.setWidth(15)
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
        column8.setWidth(15)
        tableColumns.add(column8)

        return tableColumns;
    }

/**
 * Поиск справок согласно фильтру
 */
    int counter = 0

    @TypeChecked(TypeCheckingMode.SKIP)
    List searchData(def params, pageSize, PrepareSpecificReportResult prepareSpecificReportResult) {
        def xmlStr = declarationService.getXmlData(declarationData.id)
        def Файл = new XmlSlurper().parseText(xmlStr)
        def docs = []
        Файл.Документ.each { doc ->
            boolean passed = true
            String idDoc = null
            if (params[SubreportAliasConstants.ID_DOC_NUMBER] != null) {
                idDoc = params[SubreportAliasConstants.ID_DOC_NUMBER].replaceAll("[\\s-]", "")
            }
            if (params[SubreportAliasConstants.P_NUM_SPRAVKA] != null && !StringUtils.containsIgnoreCase(doc.@НомСпр.text(), params[SubreportAliasConstants.P_NUM_SPRAVKA])) passed = false
            if (params[SubreportAliasConstants.LAST_NAME] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Фамилия.text(), params[SubreportAliasConstants.LAST_NAME])) passed = false
            if (params[SubreportAliasConstants.FIRST_NAME] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Имя.text(), params[SubreportAliasConstants.FIRST_NAME])) passed = false
            if (params[SubreportAliasConstants.MIDDLE_NAME] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.ФИО.@Отчество.text(), params[SubreportAliasConstants.MIDDLE_NAME])) passed = false
            if (params[SubreportAliasConstants.INN] != null && !StringUtils.containsIgnoreCase(doc.ПолучДох.@ИННФЛ.text(), params[SubreportAliasConstants.INN])) passed = false
            if ((params[SubreportAliasConstants.FROM_BIRTHDAY] != null || params[SubreportAliasConstants.TO_BIRTHDAY] != null) && searchBirthDay(params, doc.ПолучДох.@ДатаРожд.text())) passed = false
            if (params[SubreportAliasConstants.ID_DOC_NUMBER] != null && !((StringUtils.containsIgnoreCase(doc.ПолучДох.УдЛичнФЛ.@СерНомДок.text(), idDoc) ||
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
        return result
    }

    def searchBirthDay(Map<String, Object> params, String birthDate) {
        Date date = ScriptUtils.parseDate(DATE_FORMAT_DOTTED, birthDate)
        if (params.get(SubreportAliasConstants.FROM_BIRTHDAY) != null && params.get(SubreportAliasConstants.TO_BIRTHDAY) != null) {
            if (date >= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, (String) params.get(SubreportAliasConstants.FROM_BIRTHDAY)) && date <= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, (String) params.get(SubreportAliasConstants.TO_BIRTHDAY))) {
                return false
            }
        } else if (params.get(SubreportAliasConstants.FROM_BIRTHDAY) != null) {
            if (date >= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, (String) params.get(SubreportAliasConstants.FROM_BIRTHDAY))) {
                return false
            }
        } else if (params.get(SubreportAliasConstants.TO_BIRTHDAY) != null) {
            if (date <= ScriptUtils.parseDate(DATE_FORMAT_DOTTED, (String) params.get(SubreportAliasConstants.TO_BIRTHDAY))) {
                return false
            }
        }

        return true
    }

/**
 * Создать спецотчет
 * @return
 */
    @TypeChecked(TypeCheckingMode.SKIP)
    def createSpecificReport() {
        def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
        if (alias == ALIAS_PRIMARY_RNU_W_ERRORS) {
            createPrimaryRnuWithErrors()
            return
        }
        def row = scriptSpecificReportHolder.getSelectedRecord()
        def params = scriptSpecificReportHolder.subreportParamValues ?: new HashMap<String, Object>()
        params.put(SubreportAliasConstants.P_NUM_SPRAVKA, row.pNumSpravka)

        Map<String, String> subReportViewParams = scriptSpecificReportHolder.getViewParamValues()
        subReportViewParams.put('Номер справки', row.pNumSpravka.toString())
        subReportViewParams.put('Фамилия', row.lastName.toString())
        subReportViewParams.put('Имя', row.firstName.toString())
        subReportViewParams.put('Отчество', row.middleName.toString())
        subReportViewParams.put('Дата рождения', row.birthDay ? row.birthDay.toString() : "")
        subReportViewParams.put('№ ДУЛ', row.idDocNumber.toString())

        def xmlStr = declarationService.getXmlData(declarationData.id)
        def Файл = new XmlSlurper().parseText(xmlStr)
        long xmlPartNumber = 1 + (long) ((new Long(Файл.Документ.find { doc -> true }.@НомСпр.text())) / NUMBER_OF_PERSONS)

        def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
            buildXmlForSpecificReport(it, xmlPartNumber, Long.valueOf(row.pNumSpravka))
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
    def createXlsxReport() {
        ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) getProperty("scriptSpecificReportHolder")
        def params = new HashMap<String, Object>()
        params.put("declarationId", declarationData.getId());

        JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, declarationService.getXmlStream(declarationData.id));

        StringBuilder fileName = new StringBuilder("Реестр_справок_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
        exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
        scriptSpecificReportHolder.setFileName(fileName.toString())
    }

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
    @TypeChecked(TypeCheckingMode.SKIP)
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

        ndflPersonIncomeFromRNUConsolidatedList.each { NdflPersonIncome item ->
            ScriptUtils.checkInterrupted();
            NdflPersonIncome ndflPersonIncomePrimary = ndflPersonService.getIncome(item.sourceId)
            NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonIncomePrimary.ndflPersonId)
            ndflPersonPrimary.incomes.add(ndflPersonIncomePrimary)
        }

        ndflPersonDeductionFromRNUConsolidatedList.each { NdflPersonDeduction item ->
            ScriptUtils.checkInterrupted();
            NdflPersonDeduction ndflPersonDeductionPrimary = ndflPersonService.getDeduction(item.sourceId)
            NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonDeductionPrimary.ndflPersonId)
            ndflPersonPrimary.deductions.add(ndflPersonDeductionPrimary)
        }

        ndflPersonPrepaymentFromRNUConsolidatedList.each { NdflPersonPrepayment item ->
            ScriptUtils.checkInterrupted();
            NdflPersonPrepayment ndflPersonPrepaymentPrimary = ndflPersonService.getPrepayment(item.sourceId)
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
        OutputStream writer = scriptSpecificReportHolder.getFileOutputStream()
        XSSFWorkbook workbook = getSpecialReportTemplate()
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
    def fillGeneralData(XSSFWorkbook workbook) {
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
        String periodName = getProvider(RefBook.Id.PERIOD_CODE.id)
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

    NdflPerson initNdflPersonPrimary(Long ndflPersonId) {
        NdflPerson ndflPersonPrimary = ndflpersonFromRNUPrimary.get(ndflPersonId)
        if (ndflPersonPrimary == null) {
            ndflPersonPrimary = ndflPersonService.get(ndflPersonId)
            ndflPersonPrimary.incomes.clear()
            ndflPersonPrimary.deductions.clear()
            ndflPersonPrimary.prepayments.clear()
            ndflpersonFromRNUPrimary.put(ndflPersonId, ndflPersonPrimary)
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
        int startIndex = 12
        ndflpersonFromRNUPrimary.values().each { NdflPerson ndflPerson ->
            ndflPerson.incomes.each { NdflPersonIncome income ->
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
    def fillPrimaryRnuNDFLWithErrorsRow(
            final XSSFWorkbook workbook, NdflPerson ndflPerson, NdflPersonIncome operation, String sectionName, int index) {
        XSSFSheet sheet = workbook.getSheetAt(0)
        XSSFRow row = sheet.createRow(index)
        XSSFCellStyle styleLeftAligned = makeStyleLeftAligned(workbook)
        styleLeftAligned = thinBorderStyle(styleLeftAligned)
        XSSFCellStyle styleCenterAligned = makeStyleCenterAligned(workbook)
        styleCenterAligned = thinBorderStyle(styleCenterAligned)
        styleCenterAligned.setDataFormat(ScriptUtils.createXlsDateFormat(workbook))
        // Первичная НФ
        DeclarationData primaryRnuDeclarationData = declarationService.getDeclarationData(ndflPerson.declarationDataId)
        DeclarationDataFile primaryRnuDeclarationDataFile = declarationService.findFilesWithSpecificType(ndflPerson.declarationDataId, TRANSPORT_FILE_TEMPLATE).get(0)
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(primaryRnuDeclarationData.departmentReportPeriodId)
        Department department = departmentService.get(departmentReportPeriod.departmentId)
        // Период
        int year = departmentReportPeriod.reportPeriod.taxPeriod.year
        String periodName = getProvider(RefBook.Id.PERIOD_CODE.id)
                .getRecords(getReportPeriodEndDate(primaryRnuDeclarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).NAME.value
        // Подразделение
        String departmentName = department.shortName
        // АСНУ
        String asnu = getProvider(RefBook.Id.ASNU.id).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${primaryRnuDeclarationData.asnuId}", null).get(0).NAME.value
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
        Date birthDay = new Date((Long) ndflPerson.birthDay.getLocalMillis())

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
    XSSFWorkbook getSpecialReportTemplate() {
        def blobData = blobDataServiceDaoImpl.get(scriptSpecificReportHolder.getDeclarationSubreport().getBlobDataId())
        new XSSFWorkbook(blobData.getInputStream())
    }

/**
 * Создать стиль ячейки с выравниваем слева
 * @param workbook
 * @return
 */

    XSSFCellStyle makeStyleLeftAligned(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle()
        style.setAlignment(CellStyle.ALIGN_LEFT)
        return style
    }

/**
 * Создать стиль ячейки с выравниваем по центру
 * @param workbook
 * @return
 */

    XSSFCellStyle makeStyleCenterAligned(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle()
        style.setAlignment(CellStyle.ALIGN_CENTER)
        return style
    }

/**
 * Добавляет к стилю ячейки тонкие границы
 * @param style
 * @return
 */
    XSSFCellStyle thinBorderStyle(final XSSFCellStyle style) {
        style.setBorderTop(CellStyle.BORDER_THIN)
        style.setBorderBottom(CellStyle.BORDER_THIN)
        style.setBorderLeft(CellStyle.BORDER_THIN)
        style.setBorderRight(CellStyle.BORDER_THIN)
        return style
    }

    void departmentParamException(int departmentId, int reportPeriodId) {
        ReportPeriod reportPeriod = reportPeriodService.get(reportPeriodId)
        throw new ServiceException("Отсутствуют настройки подразделения \"%s\" периода \"%s\". Необходимо выполнить настройку в разделе меню \"Налоги->НДФЛ->Настройки подразделений\"",
                departmentService.get(departmentId).getName(),
                reportPeriod.getTaxPeriod().getYear() + ", " + reportPeriod.getName()
        ) as Throwable
    }

    /**
     * Фильтрация операци по КПП/ОКТМО
     * @param ndflPersonIncomes
     * @return
     */
    List<NdflPersonIncome> filterIncomesByKppOktmp(List<NdflPersonIncome> ndflPersonIncomes) {
        return ndflPersonIncomes.findAll() { NdflPersonIncome it ->
            it.kpp == declarationData.kpp && it.oktmo == declarationData.oktmo
        }
    }

    /**
     * Фильтрация вычетов по operationId
     * @param ndflPersonDeductions
     * @param ndflPersonIncomesAllIds
     * @return
     */
    List<NdflPersonDeduction> filterDeductionsByKppOktmp(List<NdflPersonDeduction> ndflPersonDeductions, Set<String> ndflPersonIncomesIds) {
        return ndflPersonDeductions.findAll() { NdflPersonDeduction it ->
            ndflPersonIncomesIds.contains(it.operationId)
        }
    }
}



