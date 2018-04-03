package form_template.ndfl.report_6ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.xml.MarkupBuilder
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.ss.usermodel.CellStyle
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.RefBookService
import org.apache.commons.lang3.StringUtils
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.script.SharedConstants

new Report6Ndfl(this).run();

@TypeChecked
class Report6Ndfl extends AbstractScriptClass {

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
    String applicationVersion
    Map<String, Object> paramMap

    private Report6Ndfl() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Report6Ndfl(scriptClass) {
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
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
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
        if (scriptClass.getBinding().hasVariable("applicationVersion")) {
            this.applicationVersion = (String) scriptClass.getBinding().getProperty("applicationVersion");
        }
        if (scriptClass.getBinding().hasVariable("paramMap")) {
            this.paramMap = (Map<String, Object>) scriptClass.getBinding().getProperty("paramMap")
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.CALCULATE: //формирование xml
                println "!CALCULATE!"
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
                println "!GET_SOURCES!"
                getSources()
                break
            case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
                println "!CREATE_SPECIFIC_REPORT!"
                createSpecificReport()
                break
            case FormDataEvent.CREATE_FORMS: // создание экземпляра
                println "!CREATE_FORMS!"
                checkDataConsolidated()
                createForm()
                break
            case FormDataEvent.PRE_CREATE_REPORTS:
                preCreateReports()
                break
        }
    }

// Альяс для списка идентифокаторов физлиц из КНФ
    final String NDFL_PERSON_KNF_ID = "ndflPersonKnfId"

// Коды, определяющие налоговый (отчётный) период
    final long REF_BOOK_PERIOD_CODE_ID = RefBook.Id.PERIOD_CODE.id

// Коды представления налоговой декларации по месту нахождения (учёта)
    final long REF_BOOK_TAX_PLACE_TYPE_CODE_ID = RefBook.Id.PRESENT_PLACE.id

// Признак лица, подписавшего документ
    final long REF_BOOK_MARK_SIGNATORY_CODE_ID = RefBook.Id.MARK_SIGNATORY_CODE.id

// Настройки подразделений по НДФЛ
    final long REF_BOOK_NDFL_ID = RefBook.Id.NDFL.id

// Настройки подразделений по НДФЛ (таблица)
    final long REF_BOOK_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

    final long REF_BOOK_OKTMO_ID = 96;
    final long REPORT_PERIOD_TYPE_ID = 8

    final FORM_NAME_NDFL6 = "6-НДФЛ"
    final FORM_NAME_NDFL2 = "2-НДФЛ (1)"
    final int DECLARATION_TYPE_RNU_NDFL_ID = 101
    final int DECLARATION_TYPE_NDFL2_1_ID = 102
    final int DECLARATION_TYPE_NDFL2_2_ID = 104
    final int DECLARATION_TYPE_NDFL6_ID = 103

    final String DATE_FORMAT_UNDERLINE = "yyyyMMdd"
    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"
    int pairKppOktmoSize = 0
    final String OUTCOMING_ATTACH_FILE_TYPE = "Исходящий в ФНС"

// Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]

// значение подразделения из справочника
    Map<String, RefBookValue> departmentParam = null

// Кэш подразделений из справочника
    Map<Integer, Map<String, RefBookValue>> departmentCache = [:]

    Map<Long, List<Map<String, RefBookValue>>> departmentParamTableListCache = [:];

// значение подразделения из справочника
    Map<String, RefBookValue> departmentParamTable = null

// Дата окончания отчетного периода
    Date reportPeriodEndDate = null

    // Дата начала отчетного периода
    Date reportPeriodStartDate

    // Календарная дата начала отчетного периода
    Date reportPeriodCalendarStartDate

// Кэш для справочников
    Map<String, Map<String, RefBookValue>> refBookCache = [:]

// Коды мест предоставления документа
    Map<Long, Map<String, RefBookValue>> presentPlaceCodeCache = [:]

    final Map<Long, Map<String, RefBookValue>> OKTMO_CACHE = [:]

// Мапа где ключ идентификатор NdflPerson, значение NdflPerson соответствующий идентификатору
    Map<Long, NdflPerson> ndflpersonFromRNUPrimary = [:]
/************************************* СОЗДАНИЕ XML *****************************************************************/
    def buildXml(def writer) {
        buildXml(writer, false)
    }

    def buildXmlForSpecificReport(def writer) {
        buildXml(writer, true)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def buildXml(def writer, boolean isForSpecificReport) {
        ScriptUtils.checkInterrupted()
        ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
        // Получим ИНН из справочника "Общие параметры"
        def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)
        // Получим код НО пром из справочника "Общие параметры"
        def kodNoProm = configurationParamModel?.get(ConfigurationParam.NO_CODE)?.get(0)?.get(0)

        // Параметры подразделения
        def departmentParam = getDepartmentParam(declarationData.departmentId)
        def departmentParamIncomeRow = getDepartmentParamDetails(declarationData.kpp, declarationData.oktmo)

        // Отчетный период
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

        // Код периода
        def periodCode = getRefBookValue(REF_BOOK_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

        // Коды представления налоговой декларации по месту нахождения (учёта)
        def poMestuParam = getRefPresentPlace().get(departmentParamIncomeRow?.PRESENT_PLACE?.referenceValue)

        def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        Department department = departmentService.get(departmentReportPeriod.departmentId)
        String strCorrPeriod = ""
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
        }
        def errMsg = sprintf("Не удалось создать форму %s, за %s, подразделение: %s, КПП: %s, ОКТМО: %s.",
                FORM_NAME_NDFL6,
                "${departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear()} ${departmentReportPeriod.getReportPeriod().getName()}${strCorrPeriod}",
                department.getName(),
                declarationData.kpp,
                declarationData.oktmo)
        if (poMestuParam == null) {
            logger.warn(errMsg + " В \"Настройках подразделений\" не указан \"Код места, по которому представляется документ\".")
            calculateParams.put("notReplaceXml", true)
            calculateParams.put("createForm", false)
            return
        }
        def taxPlaceTypeCode = poMestuParam?.CODE?.value
        if (taxPlaceTypeCode == null) {
            logger.warn(errMsg + " \"Код места, по которому представляется документ\", не соответствует справочнику \"Коды места представления расчета\" в \"Настройках подразделений\".")
            calculateParams.put("notReplaceXml", true)
            calculateParams.put("createForm", false)
            return
        }

        // Признак лица, подписавшего документ
        def signatoryId = getRefBookValue(REF_BOOK_MARK_SIGNATORY_CODE_ID, departmentParamIncomeRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue

        // Текущая дата
        def currDate = new Date()

        def fileName = generateXmlFileId(departmentParamIncomeRow, sberbankInnParam, declarationData.kpp, kodNoProm)
        def builder = new MarkupBuilder(writer)
        builder.setDoubleQuotes(true)
        builder.setOmitNullAttributes(true)
        builder.Файл(
                ИдФайл: fileName,
                ВерсПрог: applicationVersion,
                ВерсФорм: "5.02"
        ) {
            Документ(
                    КНД: "1151099",
                    ДатаДок: currDate.format(SharedConstants.DATE_FORMAT),
                    Период: getPeriod(departmentParamIncomeRow, periodCode),
                    ОтчетГод: reportPeriod.taxPeriod.year,
                    КодНО: departmentParamIncomeRow?.TAX_ORGAN_CODE?.value,
                    НомКорр: sprintf('%02d', findCorrectionNumber()),
                    ПоМесту: taxPlaceTypeCode
            ) {
                def svNP = ["ОКТМО": declarationData.oktmo]
                // Атрибут Тлф необязателен
                if (departmentParamIncomeRow.PHONE && !departmentParamIncomeRow.PHONE.empty) {
                    svNP.put("Тлф", departmentParamIncomeRow.PHONE)
                }
                СвНП(svNP) {
                    НПЮЛ(
                            НаимОрг: departmentParamIncomeRow.NAME,
                            ИННЮЛ: sberbankInnParam,
                            КПП: declarationData.kpp
                    )
                }
                Подписант(
                        ПрПодп: signatoryId
                ) {
                    // Узел ФИО необязателен
                    if (departmentParamIncomeRow.SIGNATORY_SURNAME && !departmentParamIncomeRow.SIGNATORY_SURNAME.empty) {
                        def fio = ["Фамилия": departmentParamIncomeRow.SIGNATORY_SURNAME, "Имя": departmentParamIncomeRow.SIGNATORY_FIRSTNAME]
                        // Атрибут Отчество необязателен
                        if (departmentParamIncomeRow.SIGNATORY_LASTNAME && !departmentParamIncomeRow.SIGNATORY_LASTNAME.empty) {
                            fio.put("Отчество", departmentParamIncomeRow.SIGNATORY_LASTNAME)
                        }
                        ФИО(fio) {}
                    }
                    if (signatoryId == 2) {
                        def svPred = ["НаимДок": departmentParamIncomeRow.APPROVE_DOC_NAME]
                        if (departmentParamIncomeRow.APPROVE_ORG_NAME && !departmentParamIncomeRow.APPROVE_ORG_NAME.empty) {
                            svPred.put("НаимОрг", departmentParamIncomeRow.APPROVE_ORG_NAME)
                        }
                        СвПред(svPred) {}
                    }
                }
                НДФЛ6() {
                    //Все доходы
                    def ndflPersonIncomeList = []
                    // Группировка id ndflPerson в список по 1000 штук поскольку Oracle не работает со списком размером более 1000
                    def ndflPersonidForSearch = ndflPersonKnfId.collate(1000)

                    int personCount = 0

                    // Поиск и добавление доходов
                    ndflPersonidForSearch.each {
                        ScriptUtils.checkInterrupted()
                        ndflPersonIncomeList.addAll(ndflPersonService.findIncomesForPersonByKppOktmo(it, declarationData.kpp, declarationData.oktmo))
                        personCount += ndflPersonService.findInpCountForPersonsAndIncomeAccruedDatePeriod(it, reportPeriod.startDate, reportPeriod.endDate)
                    }
                    // Сумма удержанная
                    BigDecimal incomeWithholdingTotal = new BigDecimal(0)
                    // Сумма не удержанная
                    BigDecimal incomeNotHoldingTotal = new BigDecimal(0)
                    BigDecimal incomeNotHoldingTaxSum = new BigDecimal(0)
                    BigDecimal incomeOverholdingTaxSum = new BigDecimal(0)
                    // Сумма возвращенная
                    Long refoundTotal = 0L

                    ndflPersonIncomeList.each { NdflPersonIncome item ->
                        ScriptUtils.checkInterrupted()
                        if (item.withholdingTax != null
                                && (item.taxTransferDate >= getReportPeriodStartDate() && item.taxTransferDate <= getReportPeriodEndDate())
                                && (item.incomePayoutDate >= getReportPeriodStartDate() && item.incomePayoutDate <= getReportPeriodEndDate())) {
                            incomeWithholdingTotal = incomeWithholdingTotal.add(item.withholdingTax)
                        }
                        if (item.notHoldingTax != null
                                && ((item.incomeAccruedDate != null && (item.incomeAccruedDate >= getReportPeriodStartDate() && item.incomeAccruedDate <= getReportPeriodEndDate()))
                                || (item.incomePayoutDate != null && (item.incomePayoutDate >= getReportPeriodStartDate() && item.incomePayoutDate <= getReportPeriodEndDate())))) {
                            incomeNotHoldingTaxSum = incomeNotHoldingTaxSum.add(item.notHoldingTax)
                        }
                        if (item.overholdingTax != null
                                && ((item.incomeAccruedDate != null && (item.incomeAccruedDate >= getReportPeriodStartDate() && item.incomeAccruedDate <= getReportPeriodEndDate()))
                                || (item.incomePayoutDate != null && (item.incomePayoutDate >= getReportPeriodStartDate() && item.incomePayoutDate <= getReportPeriodEndDate())))) {
                            incomeOverholdingTaxSum = incomeOverholdingTaxSum.add(item.overholdingTax)
                        }
                        if (item.refoundTax != null
                                && ((item.incomeAccruedDate != null && (item.incomeAccruedDate >= getReportPeriodStartDate() && item.incomeAccruedDate <= getReportPeriodEndDate()))
                                || (item.incomePayoutDate != null && (item.incomePayoutDate >= getReportPeriodStartDate() && item.incomePayoutDate <= getReportPeriodEndDate())))) {
                            refoundTotal += item.refoundTax
                        }
                    }
                    if (incomeNotHoldingTaxSum > incomeOverholdingTaxSum) {
                        incomeNotHoldingTotal = incomeNotHoldingTaxSum.subtract(incomeOverholdingTaxSum)
                    }

                    ОбобщПоказ(
                            КолФЛДоход: personCount,
                            УдержНалИт: incomeWithholdingTotal,
                            НеУдержНалИт: incomeNotHoldingTotal,
                            ВозврНалИт: refoundTotal
                    ) {

                        // Доходы сгруппированыые по ставке, ключ ставка - значение список операций
                        Map<Integer, List<NdflPersonIncome>> incomesGroupedByRate = groupByTaxRate(ndflPersonIncomeList)
                        Map<Integer, BigDecimal> accruedSumByRate = [:]
                        Map<Integer, BigDecimal> accruedSumForDividendByRate = [:]
                        Map<Integer, BigDecimal> deductionsSumByRate = [:]
                        Map<Integer, BigDecimal> prepaymentsSumByRate = [:]
                        Map<Integer, BigDecimal> calculatedTaxSumByRate = [:]
                        Map<Integer, BigDecimal> calculatedTaxSumDividendByRate = [:]

                        for (Integer rate : incomesGroupedByRate.keySet()) {
                            ScriptUtils.checkInterrupted()
                            if (rate != null) {
                                List<NdflPersonIncome> incomes = incomesGroupedByRate.get(rate)
                                BigDecimal accruedSum = new BigDecimal(0)
                                BigDecimal accruedSumForDividend = new BigDecimal(0)
                                BigDecimal deductionsSum = new BigDecimal(0)
                                BigDecimal prepaymentsSum = new BigDecimal(0)
                                BigDecimal calculatedTaxSum = new BigDecimal(0)
                                BigDecimal calculatedTaxSumForDividend = new BigDecimal(0)

                                List<Long> ndflpersonIdList = incomesGroupedByRate.get(rate).id

                                List<Long> ndflpersonIdListForSearch = ndflpersonIdList.collate(1000)

                                List<NdflPersonPrepayment> prepayments = []

                                ndflpersonIdListForSearch.each {
                                    ScriptUtils.checkInterrupted()
                                    prepayments.addAll(ndflPersonService.fetchPrepaymentByIncomesIdAndAccruedDate(it, getReportPeriodStartDate(), getReportPeriodEndDate()))
                                }
                                prepaymentsSum = calculateSumOfPrepayments(prepayments)
                                prepaymentsSumByRate.put(rate, prepaymentsSum)

                                for (NdflPersonIncome income : incomes) {
                                    ScriptUtils.checkInterrupted()

                                    if (income.incomeAccruedDate != null && (income.incomeAccruedDate >= getReportPeriodStartDate() && income.incomeAccruedDate <= getReportPeriodEndDate())) {
                                        if (income.incomeAccruedSumm != null) {
                                            accruedSum = accruedSum.add(income.incomeAccruedSumm)
                                            if (income.incomeCode == "1010") {
                                                accruedSumForDividend = accruedSumForDividend.add(income.incomeAccruedSumm)
                                            }
                                        }
                                        if (income.totalDeductionsSumm != null) {
                                            deductionsSum = deductionsSum.add(income.totalDeductionsSumm)
                                        }
                                        if (income.calculatedTax != null) {
                                            calculatedTaxSum = calculatedTaxSum.add(income.calculatedTax)
                                            if (income.incomeCode == "1010") {
                                                calculatedTaxSumForDividend = calculatedTaxSumForDividend.add(income.calculatedTax)
                                            }
                                        }
                                    }
                                }
                                accruedSumByRate.put(rate, accruedSum)
                                accruedSumForDividendByRate.put(rate, accruedSumForDividend)
                                deductionsSumByRate.put(rate, deductionsSum)
                                calculatedTaxSumByRate.put(rate, calculatedTaxSum.add(prepaymentsSum))
                                calculatedTaxSumDividendByRate.put(rate, calculatedTaxSumForDividend)
                            }
                        }

                        incomesGroupedByRate.keySet().eachWithIndex { rate, index ->
                            ScriptUtils.checkInterrupted()
                            if (rate != null) {
                                if (isForSpecificReport) {
                                    СумСтавка(
                                            Ставка: rate,
                                            НачислДох: ScriptUtils.round(accruedSumByRate.get(rate), 2),
                                            НачислДохДив: ScriptUtils.round(accruedSumForDividendByRate.get(rate), 2),
                                            ВычетНал: ScriptUtils.round(deductionsSumByRate.get(rate), 2),
                                            ИсчислНал: calculatedTaxSumByRate.get(rate),
                                            ИсчислНалДив: calculatedTaxSumDividendByRate.get(rate),
                                            АвансПлат: prepaymentsSumByRate.get(rate),
                                            НомСтр: index + 1
                                    ) {}
                                } else {
                                    СумСтавка(
                                            Ставка: rate,
                                            НачислДох: ScriptUtils.round(accruedSumByRate.get(rate), 2),
                                            НачислДохДив: ScriptUtils.round(accruedSumForDividendByRate.get(rate), 2),
                                            ВычетНал: ScriptUtils.round(deductionsSumByRate.get(rate), 2),
                                            ИсчислНал: calculatedTaxSumByRate.get(rate),
                                            ИсчислНалДив: calculatedTaxSumDividendByRate.get(rate),
                                            АвансПлат: prepaymentsSumByRate.get(rate)
                                    ) {}
                                }
                            }
                        }
                    }

                    // Необходимо сгруппировать доходы по ИдОперации для поиска даты начисления
                    def pairOperationIdMap = [:]
                    ndflPersonIncomeList.each {
                        def operationId = it.operationId
                        def incomesGroup = pairOperationIdMap.get(operationId)
                        if (incomesGroup == null) {
                            pairOperationIdMap.put(operationId, [it])
                        } else {
                            incomesGroup << it
                        }
                    }

                    // Список содержащий данные для формирования раздела 2
                    List<Section2DataHolder> section2Data = []

                    // Определяем строки для заполнения раздела 2
                    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
                        if (ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxTransferDate != null
                                && (getReportPeriodCalendarStartDate() <= ndflPersonIncome.taxTransferDate && getReportPeriodEndDate() >= ndflPersonIncome.taxTransferDate)) {
                            List<Date> incomeAccruedDateList = []
                            for (NdflPersonIncome incomeGrouped : pairOperationIdMap.get(ndflPersonIncome.operationId)) {
                                if (incomeGrouped.incomeAccruedDate != null) {
                                    incomeAccruedDateList << incomeGrouped.incomeAccruedDate
                                }
                            }
                            section2Data << new Section2DataHolder(ndflPersonIncome, incomeAccruedDateList.isEmpty() ? Date.parse(SharedConstants.DATE_FORMAT, SharedConstants.DATE_ZERO_AS_DATE) : Collections.min(incomeAccruedDateList), ndflPersonIncome.taxDate, ndflPersonIncome.taxTransferDate)
                        }
                    }

                    /* Объекты {@code Section2DataHolder} сравниваются через equals по полям ДатаФактическогоПолученияДохода,
                    ДатаУдержанияНалога, СрокПеречисленияНалога. Если есть одинаковые объекты, то суммируем их значения по гр11(ФактДоход) и гр17(УдержНал)
                    {@code TreeMap} поскольку делаем сортировку по датам
                     */
                    Map<Section2DataHolder, BigDecimal> virtuallyIncomeSum = new TreeMap<>(new Comparator<Section2DataHolder>() {
                        @Override
                        int compare(Section2DataHolder o1, Section2DataHolder o2) {
                            int withholdingDateComp = o1.witholdingDate.compareTo(o2.witholdingDate)
                            if (withholdingDateComp != 0) {
                                return withholdingDateComp
                            }
                            int taxTransferDateComp = o1.taxTransferDate.compareTo(o2.taxTransferDate)
                            if (taxTransferDateComp != 0) {
                                return taxTransferDateComp
                            }
                            return o1.virtuallyReceivedIncomeDate.compareTo(o2.virtuallyReceivedIncomeDate)
                        }
                    })
                    Map<Section2DataHolder, BigDecimal> withholdingTaxSum = [:]

                    for (Section2DataHolder section2DataItem : section2Data) {
                        BigDecimal virtuallyIncome = virtuallyIncomeSum.get(section2DataItem) ?: new BigDecimal(0)
                        BigDecimal withholdingTax = withholdingTaxSum.get(section2DataItem) ?: new BigDecimal(0)
                        if (section2DataItem.withholdingRow.incomePayoutSumm != null) {
                            virtuallyIncome = virtuallyIncome.add(section2DataItem.withholdingRow.incomePayoutSumm)
                            virtuallyIncomeSum.put(section2DataItem, virtuallyIncome)
                        }
                        if (section2DataItem.withholdingRow.withholdingTax != null) {
                            withholdingTax = withholdingTax.add(section2DataItem.withholdingRow.withholdingTax)
                            withholdingTaxSum.put(section2DataItem, withholdingTax)
                        }
                    }

                    if (!section2Data.isEmpty()) {

                        ДохНал() {
                            for (Section2DataHolder section2DataItem : virtuallyIncomeSum.keySet()) {
                                СумДата(
                                        ДатаФактДох: formatDate(section2DataItem.virtuallyReceivedIncomeDate).equals(SharedConstants.DATE_ZERO_AS_DATE) ? SharedConstants.DATE_ZERO_AS_STRING : formatDate(section2DataItem.virtuallyReceivedIncomeDate),
                                        ДатаУдержНал: formatDate(section2DataItem.witholdingDate),
                                        СрокПрчслНал: formatDate(section2DataItem.taxTransferDate).equals(SharedConstants.DATE_ZERO_AS_DATE) ? SharedConstants.DATE_ZERO_AS_STRING : formatDate(section2DataItem.taxTransferDate),
                                        ФактДоход: virtuallyIncomeSum.get(section2DataItem),
                                        УдержНал: withholdingTaxSum.get(section2DataItem)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
        ScriptUtils.checkInterrupted()
        saveFileInfo(currDate, fileName)

        //    println(writer)
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
            declarations.addAll(declarationService.find(DECLARATION_TYPE_NDFL2_1_ID, drp.id))
            declarations.addAll(declarationService.find(DECLARATION_TYPE_NDFL2_2_ID, drp.id))
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

    def saveFileInfo(Date currDate, String fileName) {
        String fileUuid = blobDataServiceDaoImpl.create(xmlFile, fileName + ".xml", new Date())
        def createUser = declarationService.getSystemUserInfo().getUser()

        def fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
        def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_2.code}").get(0)

        DeclarationDataFile declarationDataFile = new DeclarationDataFile()
        declarationDataFile.setDeclarationDataId(declarationData.id)
        declarationDataFile.setUuid(fileUuid)
        declarationDataFile.setUserName(createUser.getName())
        declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(createUser.getDepartmentId()))
        declarationDataFile.setFileTypeId(fileTypeId)
        declarationDataFile.setDate(currDate)
        declarationService.saveFile(declarationDataFile)
    }

    /**
     * Групирует доходы по налоговой ставке
     * @param incomes список объектов доходов
     * @return возвращает маппу где ключ налоговая ставка, список операций доходов по этой налоговой ставке
     */
    Map<Integer, List<NdflPersonIncome>> groupByTaxRate(List<NdflPersonIncome> incomes) {
        Map<Integer, List<NdflPersonIncome>> toReturn = [:]
        List<Integer> rates = []
        incomes.each { NdflPersonIncome income ->
            List<NdflPersonIncome> groupedIncomes = toReturn.get(income.taxRate)
            if (groupedIncomes == null) {
                toReturn.put(income.taxRate, [income])
            } else {
                groupedIncomes << income
            }
        }
        return toReturn
    }

    /**
     * Вычисляет сумму фиксированного авансового платежа
     * @param prepayments список объектов авансов для которых вычисляется сумма
     * @return сумма фиксированного авансового платежа
     */
    BigDecimal calculateSumOfPrepayments(List<NdflPersonPrepayment> prepayments) {
        BigDecimal toReturn = new BigDecimal(0)
        prepayments.each { NdflPersonPrepayment item ->
            if (item.summ != null) {
                toReturn = toReturn.add(item.summ)
            }
        }
        return toReturn
    }

/**
 * Генерация значения атрибута ИдФайл R_T_A_K_O_GGGGMMDD_N
 * R_T - NO_NDFL6
 * A - идентификатор получателя, которому направляется файл обмена;
 * K - идентификатор конечного получателя, для которого предназначена информация из данного файла обмена;
 * O - 	Девятнадцатиразрядный код (идентификационный номер налогоплательщика (далее - ИНН) и код причины постановки на учет (далее - КПП) организации (обособленного подразделения);
 * GGGG - Год формирования передаваемого файла
 * MM - Месяц формирования передаваемого файла
 * DD - День формирования передаваемого файла
 * N - Идентификационный номер файла должен обеспечивать уникальность файла, длина - от 1 до 36 знаков
 */
    def generateXmlFileId(Map<String, RefBookValue> departmentParamIncomeRow, String INN, String KPP, String kodNoProm) {
        String R_T = "NO_NDFL6"
        String A = kodNoProm
        String K = departmentParamIncomeRow?.TAX_ORGAN_CODE?.stringValue
        String O = INN + KPP
        String currDate = new Date().format(DATE_FORMAT_UNDERLINE)
        String N = UUID.randomUUID().toString().toUpperCase()
        String res = R_T + "_" + A + "_" + K + "_" + O + "_" + currDate + "_" + N
        return res
    }

/**
 * Период
 */
    String getPeriod(Map<String, RefBookValue> departmentParamIncomeRow, String periodCode) {
        if (departmentParamIncomeRow.REORG_FORM_CODE && !departmentParamIncomeRow.REORG_FORM_CODE.empty) {
            String result;
            switch (periodCode) {
                case "21":
                    result = "51"
                    break
                case "31":
                    result = "52"
                    break
                case "33":
                    result = "53"
                    break
                case "34":
                    result = "90"
                    break
            }
            return result;
        } else {
            return periodCode;
        }
    }

/**
 * Получение провайдера с использованием кеширования.
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
 * Разыменование записи справочника
 */
    Map<String, RefBookValue> getRefBookValue(Long refBookId, Long recordId) {
        return refBookService.getRefBookValue(refBookId, recordId, refBookCache)
    }

/************************************* СОЗДАНИЕ ФОРМЫ *****************************************************************/

    List<Map<String, RefBookValue>> departmentParamTableList = null;

    final long REF_BOOK_DOC_STATE = 929

    List<PairKppOktmo> getPairKppOktmoList() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        List<PairKppOktmo> pairKppOktmoList = []
        departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false)
        String depName = departmentService.get((Integer) departmentParam.DEPARTMENT_ID.value).name
        def reportPeriod = departmentReportPeriod.reportPeriod
        def otchetGod = reportPeriod.taxPeriod.year
        def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        def declarationTypeId = currDeclarationTemplate.type.id
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
                if (!declarations.isEmpty() || drp.correctionDate == null) {
                    break
                }
            }

            def declarationsForRemove = []
            declarations.each { DeclarationData declaration ->
                ScriptUtils.checkInterrupted()
                Long stateDocReject = (Long) getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Отклонен'", null).get(0).id.value
                Long stateDocNeedClarify = (Long) getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Требует уточнения'", null).get(0).id.value
                Long stateDocError = (Long) getProvider(REF_BOOK_DOC_STATE).getRecords(null, null, "NAME = 'Ошибка'", null).get(0).id.value
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

            declarations.each { DeclarationData declaration ->
                PairKppOktmo pairKppOktmo = new PairKppOktmo(declaration.kpp, declaration.oktmo, declaration.taxOrganCode)
                if (!pairKppOktmoList.contains(pairKppOktmo)) {
                    pairKppOktmoList << pairKppOktmo
                }
            }
            // Поиск КПП и ОКТМО для некорр периода
        } else {
            // Поиск дочерних подразделений. Поскольку могут существовать пары КПП+ОКТМО в ref_book_ndfl_detail ссылающиеся
            // только на обособленные подразделения тербанка
            List<Department> childrenDepartments = departmentService.getAllChildren(departmentReportPeriod.departmentId)
            def referencesOktmoList = []
            Map<String, RefBookValue> departmentParam
            List<Map<String, RefBookValue>> departmentParamTableList = []
            for (Department childrenDepartment in childrenDepartments) {
                departmentParam = getDepartmentParam(childrenDepartment.id, departmentReportPeriod.reportPeriod.id, false)
                if (departmentParam != null) {
                    departmentParamTableList.addAll(getDepartmentParamTableList((Long) departmentParam?.id.value, departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false))
                    referencesOktmoList.addAll(departmentParamTableList.OKTMO?.value)
                }
            }
            referencesOktmoList.removeAll([null])
            if (referencesOktmoList.isEmpty()) {
                logger.error("Отчетность %s  для %s за период %s не сформирована. Отсутствуют настройки указанного подразделения в справочнике \"Настройки подразделений", FORM_NAME_NDFL6, depName, "$otchetGod ${reportPeriod.name}")
                return
            }
            Map<Long, Map<String, RefBookValue>> oktmoForDepartment = getOktmoByIdList(referencesOktmoList)
            departmentParamTableList.each { Map<String, RefBookValue> dep ->
                ScriptUtils.checkInterrupted()
                if (dep.OKTMO?.value != null) {
                    Map<String, RefBookValue> oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
                    if (oktmo != null) {
                        PairKppOktmo pairKppOktmo = new PairKppOktmo(dep.KPP?.stringValue, oktmo.CODE.stringValue, dep?.TAX_ORGAN_CODE?.stringValue)
                        if (!pairKppOktmoList.contains(pairKppOktmo)) {
                            pairKppOktmoList << pairKppOktmo
                        }
                    }
                }
            }
            if (pairKppOktmoList.isEmpty()) {
                logger.error("Отчетность %s  для %s за период %s не сформирована. Отсутствуют настройки указанного подразделения в справочнике \"Настройки подразделений", FORM_NAME_NDFL6, depName, "$otchetGod ${reportPeriod.name}")
                return
            }
        }
        pairKppOktmoSize = pairKppOktmoList.size()
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
            logger.error("Уточненная отчетность $FORM_NAME_NDFL6 для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для подразделения ${department.name} и периода ${prevDrp.reportPeriod.taxPeriod.year}, ${prevDrp.reportPeriod.name}" + getCorrectionDateExpression(prevDrp) + " не найдены отчетные формы, \"Состояние ЭД\" которых равно \"Отклонен\", \"Требует уточнения\" или \"Ошибка\".")
        } else {
            logger.error("Уточненная отчетность $FORM_NAME_NDFL6 для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для заданного В отчетных формах подразделения ${department.name} и периода ${prevDrp.reportPeriod.taxPeriod.year}, ${prevDrp.reportPeriod.name}" + getCorrectionDateExpression(prevDrp) + " не найдены физические лица, \"Текст ошибки от ФНС\" которых заполнен. Уточненная отчетность формируется только для указанных физических лиц.")
        }
    }

    Map<PairKppOktmo, List<NdflPerson>> getNdflPersonsGroupedByKppOktmo() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        List<PairKppOktmo> pairKppOktmoList = getPairKppOktmoList()
        if (pairKppOktmoList == null) {
            return null
        }

        def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        def declarationTypeId = currDeclarationTemplate.type.id
        def reportPeriod = departmentReportPeriod.reportPeriod
        def otchetGod = reportPeriod.taxPeriod.year
        String strCorrPeriod = ""
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
        }
        departmentParam = getDepartmentParam(departmentReportPeriod.departmentId, departmentReportPeriod.reportPeriod.id, false)
        String depName = departmentService.get((Integer) departmentParam.DEPARTMENT_ID.value).name
        // список форм рну-ндфл для отчетного периода всех ТБ
        List<DeclarationData> allDeclarationData = findAllTerBankDeclarationData(departmentReportPeriod)
        if (allDeclarationData == null) {
            return null
        }
        // Список физлиц для каждой пары КПП и ОКТМО
        Map<PairKppOktmo, List<NdflPerson>> ndflPersonsGroupedByKppOktmo = [:]

        if (!allDeclarationData.isEmpty()) {
            pairKppOktmoList.each { PairKppOktmo pair ->
                ScriptUtils.checkInterrupted()
                List<NdflPerson> ndflPersons = ndflPersonService.findNdflPersonByPairKppOktmo((List<Long>) allDeclarationData.id, pair.kpp.toString(), pair.oktmo.toString(), false)
                if (ndflPersons != null && ndflPersons.size() != 0) {
                    addNdflPersons(ndflPersonsGroupedByKppOktmo, pair, ndflPersons)
                } else {
                    String depChildName = departmentService.getDepartmentNameByPairKppOktmo(pair.kpp, pair.oktmo, departmentReportPeriod.reportPeriod.endDate)
                    logger.warn("Не удалось создать форму $FORM_NAME_NDFL6, за период $otchetGod ${reportPeriod.name}$strCorrPeriod, подразделение: ${depChildName ?: ""}, КПП: ${pair.kpp}, ОКТМО: ${pair.oktmo}. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName, за период $otchetGod ${reportPeriod.name} $strCorrPeriod отсутствуют операции о НДФЛ для указанных КПП и ОКТМО.")
                }
            }
        }
        if (ndflPersonsGroupedByKppOktmo == null || ndflPersonsGroupedByKppOktmo.isEmpty()) {
            logger.error("Отчетность $FORM_NAME_NDFL6 для $depName за период $otchetGod ${reportPeriod.name} $strCorrPeriod не сформирована. В РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated.id} для подразделения: $depName за период $otchetGod ${reportPeriod.name} $strCorrPeriod отсутствуют операции.")
            checkPresentedPairsKppOktmo()
        }
        return ndflPersonsGroupedByKppOktmo
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
        List<DeclarationData> declarationDataList = declarationService.findAllActive(DECLARATION_TYPE_RNU_NDFL_ID, drp.reportPeriod.id)

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
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        ReportPeriod reportPeriod = departmentReportPeriod.reportPeriod
        Map<String, RefBookValue> period = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)
        String periodCode = period?.CODE?.stringValue
        String periodName = period?.NAME?.stringValue
        Date calendarStartDate = reportPeriod?.calendarStartDate
        String correctionDateExpression = departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format(SharedConstants.DATE_FORMAT)},"
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

    def createForm() {
        try {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            def currDeclarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
            def declarationTypeId = currDeclarationTemplate.type.id
            // Список физлиц для каждой пары КПП и ОКТМО
            Map<PairKppOktmo, List<NdflPerson>> ndflPersonsGroupedByKppOktmo = getNdflPersonsGroupedByKppOktmo()

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

            if (ndflPersonsGroupedByKppOktmo == null || ndflPersonsGroupedByKppOktmo.isEmpty()) {
                return
            }
            checkPresentedPairsKppOktmo()

            ndflPersonsGroupedByKppOktmo.each { Map.Entry<PairKppOktmo, List<NdflPerson>> npGroup ->
                ScriptUtils.checkInterrupted()
                Map<String, Object> params
                String oktmo = npGroup.key.oktmo
                String kpp = npGroup.key.kpp
                String taxOrganCode = npGroup.key.taxOrganCode
                List<Long> npGropSourcesIdList = npGroup.value.id
                Long ddId
                params = new HashMap<String, Object>()
                ddId = declarationService.create(logger, declarationData.declarationTemplateId, userInfo,
                        departmentReportPeriodService.get(declarationData.departmentReportPeriodId), taxOrganCode, kpp.toString(), oktmo.toString(), null, null, null, false)

                params.put(NDFL_PERSON_KNF_ID, npGropSourcesIdList)
                formMap.put(ddId, params)
            }
        } finally {
            scriptParams.put("pairKppOktmoTotal", pairKppOktmoSize)
        }
    }

// Пары КПП/ОКТМО отсутствующие в справочнике настройки подразделений
    def checkPresentedPairsKppOktmo() {
        declarationDataConsolidated = declarationDataConsolidated ?: declarationService.find(DECLARATION_TYPE_RNU_NDFL_ID, declarationData.departmentReportPeriodId).get(0)
        List<Pair<String, String>> kppOktmoNotPresentedInRefBookList = declarationService.findNotPresentedPairKppOktmo(declarationDataConsolidated.id);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        Department department = departmentService.get(departmentReportPeriod.getDepartmentId())
        for (Pair<String, String> kppOktmoNotPresentedInRefBook : kppOktmoNotPresentedInRefBookList) {
            logger.warn("Для подразделения %s отсутствуют настройки подразделений для КПП: %s, ОКТМО: %s в справочнике \"Настройки подразделений\". Данные формы РНУ НДФЛ (консолидированная) № %d по указанным КПП и ОКТМО источника выплаты не включены в отчетность.",
                    department.getName(), kppOktmoNotPresentedInRefBook.getFirst(), kppOktmoNotPresentedInRefBook.getSecond(), declarationDataConsolidated.id)
        }
    }

    Map<Long, Map<String, RefBookValue>> getOktmoByIdList(List<Long> idList) {
        RefBookDataProvider provider = getProvider(REF_BOOK_OKTMO_ID)
        return provider.getRecordData(idList)
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
            ScriptUtils.checkInterrupted()
            allDeclarationData.addAll(declarationService.find(DECLARATION_TYPE_RNU_NDFL_ID, item))
        }

        if (!checkExistingConsDDForCurrTB(departmentReportPeriod, allDeclarationData)) {
            createEmptyMessage(departmentReportPeriod, false)
            return null;
        }
        if (declarationDataConsolidated == null) {
            declarationDataConsolidated = declarationService.find(DECLARATION_TYPE_RNU_NDFL_ID, departmentReportPeriod.id).get(0)
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
            logger.error("Отчетность $FORM_NAME_NDFL6 для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для указанного подразделения и периода форма РНУ НДФЛ (консолидированная) № ${declarationDataConsolidated?.id} должна быть в состоянии \"Принята\". Примите форму и повторите операцию")
        } else {
            logger.error("Отчетность $FORM_NAME_NDFL6 для ${department.name} за период ${departmentReportPeriod.reportPeriod.taxPeriod.year}, ${departmentReportPeriod.reportPeriod.name}" + correctionDateExpression + " не сформирована. Для указанного подразделения и периода не найдена форма РНУ НДФЛ (консолидированная).")
        }
    }

    def addNdflPersons(Map<PairKppOktmo, List<NdflPerson>> ndflPersonsGroupedByKppOktmo, PairKppOktmo pairKppOktmoBeingComparing, List<NdflPerson> ndflPersonList) {

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

    Map<Integer, DepartmentReportPeriod> departmentReportPeriodMap = [:]

// Мапа для хранения полного названия подразделения (id подразделения  -> полное название)
    Map<Integer, String> departmentFullNameMap = [:]


    ReportPeriod getReportPeriod() {
        if (sourceReportPeriod == null) {
            sourceReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        }
        return sourceReportPeriod
    }

/** Получить результат для события FormDataEvent.GET_SOURCES. */
    void getSources() {
        ScriptUtils.checkInterrupted()
        if (!(needSources)) {
            // формы-приемники, декларации-истчоники, декларации-приемники не переопределять
            return
        }
        ReportPeriod reportPeriod = getReportPeriod()
        Integer sourceTypeId = 101
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        List<Integer> allDepartmentReportPeriodIds = departmentReportPeriodService.getIdsByDepartmentTypeAndReportPeriod(DepartmentType.TERR_BANK.getCode(), departmentReportPeriod.id)
        // Найти подразделения в РНУ которых имеются операции из декларации
        List<DeclarationData> tmpDeclarationDataList = []
        allDepartmentReportPeriodIds.each { Integer item ->
            ScriptUtils.checkInterrupted();
            DepartmentReportPeriod tmpDepartmentReportPeriod = departmentReportPeriodService.get(item)
            DeclarationData tmpDeclaration = declarationService.findDeclarationDataByKppOktmoOfNdflPersonIncomes(sourceTypeId, item, tmpDepartmentReportPeriod.departmentId, tmpDepartmentReportPeriod.reportPeriod.id, declarationData.kpp, declarationData.oktmo)
            if (tmpDeclaration != null) {
                tmpDeclarationDataList << tmpDeclaration
            }
        }
        List<DeclarationData> declarationsForRemove = []
        tmpDeclarationDataList.each { DeclarationData declaration ->
            if (declaration.state != State.ACCEPTED) {
                declarationsForRemove << declaration
            }
        }
        tmpDeclarationDataList.removeAll(declarationsForRemove)
        tmpDeclarationDataList.each { DeclarationData tmpDeclarationData ->
            ScriptUtils.checkInterrupted()
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
        Boolean isSource = sourceTypeId == 101

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

    DepartmentReportPeriod getDepartmentReportPeriodById(Integer id) {
        if (id != null && departmentReportPeriodMap.get(id) == null) {
            departmentReportPeriodMap.put(id, departmentReportPeriodService.get(id))
        }
        return departmentReportPeriodMap.get(id)
    }
/** Получить полное название подразделения по id подразделения. */
    String getDepartmentFullName(Integer id) {
        if (departmentFullNameMap.get(id) == null) {
            departmentFullNameMap.put(id, departmentService.getParentsHierarchy(id))
        }
        return departmentFullNameMap.get(id)
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

// Получить список детали подразделения из справочника для некорректировочного периода
    List<Map<String, RefBookValue>> getDepartmentParamTableList(Long departmentParamId, Integer departmentId, Integer reportPeriodId, boolean throwIfEmpty) {
        if (!departmentParamTableListCache.containsKey(departmentParamId)) {
            String filter = "REF_BOOK_NDFL_ID = $departmentParamId".toString()
            departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(reportPeriodId), null, filter, null)
            if ((departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) && throwIfEmpty) {
                departmentParamException(departmentId, reportPeriodId)
            }
            departmentParamTableListCache.put(departmentParamId, departmentParamTableList)
        }
        return departmentParamTableListCache.get(departmentParamId)
    }

    Map<String, RefBookValue> getDepartmentParam(Integer departmentId, Integer reportPeriodId, boolean throwIfEmpty) {
        if (!departmentCache.containsKey(departmentId)) {
            PagingResult<Map<String, RefBookValue>> departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate(reportPeriodId), null, "DEPARTMENT_ID = $departmentId", null)
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
 * Получить настройки подразделения
 * @return
 */
    def getDepartmentParam(Integer departmentId) {
        if (departmentParam == null) {
            PagingResult<Map<String, RefBookValue>> departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate(), null, "DEPARTMENT_ID = $departmentId".toString(), null)
            if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
                departmentParamException(departmentId, declarationData.reportPeriodId)
            }
            departmentParam = departmentParamList?.get(departmentParamList.size() - 1)
        }
        return departmentParam
    }

/**
 * Получить параметры подразделения
 * @param departmentParamId
 * @return
 */
    def getDepartmentParamTable(Long departmentParamId) {
        if (departmentParamTable == null) {
            String filter = "REF_BOOK_NDFL_ID = $departmentParamId and KPP ='${declarationData.kpp}'".toString()
            PagingResult<Map<String, RefBookValue>> departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(), null, filter, null)
            if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
                departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
            }
            List<Long> referencesOktmoList = (List<Long>) departmentParamTableList.OKTMO?.value
            referencesOktmoList.removeAll([null])
            Map<Long, Map<String, RefBookValue>> oktmoForDepartment = getOktmoByIdList(referencesOktmoList)
            departmentParamTable = departmentParamTableList.find { dep ->
                Map<String, RefBookValue> oktmo = oktmoForDepartment.get(dep.OKTMO?.value)
                if (oktmo != null) {
                    declarationData.kpp.equals(dep.KPP?.stringValue) && declarationData.oktmo.equals(oktmo.CODE.value)
                }
            }
            if (departmentParamTable == null) {
                departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
            }
        }
        return departmentParamTable
    }

/**
 * Получить детали подразделения из справочника по кпп и октмо формы
 * @param departmentParamId
 * @param reportPeriodId
 * @return
 */
    Map<String, RefBookValue> getDepartmentParamDetails(String kpp, String oktmo) {
        List<Map<String, RefBookValue>> departmentParamRowList = []
        PagingResult<Map<String, RefBookValue>> oktmoReferenceList = getProvider(REF_BOOK_OKTMO_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "CODE = '$oktmo'".toString(), null)
        Long oktmoReference = (Long) oktmoReferenceList.get(oktmoReferenceList.size() - 1).id.value
        List<Department> deps = departmentService.getAllChildren(declarationData.departmentId)
        deps.each { Department item ->
            PagingResult<Map<String, RefBookValue>> departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate(), null, "DEPARTMENT_ID = ${item.id}".toString(), null)
            if (departmentParamList != null && departmentParamList.size() > 0) {
                Map<String, RefBookValue> departmentParamRow = departmentParamList.get(0)
                if (departmentParamRow != null) {
                    departmentParamRowList.addAll(getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "REF_BOOK_NDFL_ID = ${departmentParamRow.id} and OKTMO = $oktmoReference AND KPP = '$kpp'".toString(), null))
                }
            }
        }

        if (departmentParamRowList == null || departmentParamRowList.isEmpty()) {
            departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
        }

        return departmentParamRowList.get(0)
    }

    Map<String, RefBookValue> getOktmoById(Long id) {
        Map<String, RefBookValue> oktmo = OKTMO_CACHE.get(id)
        if (oktmo == null) {
            Date rpe = getReportPeriodEndDate(declarationData.reportPeriodId)
            PagingResult<Map<String, RefBookValue>> oktmoList = getProvider(REF_BOOK_OKTMO_ID).getRecords(rpe, null, "ID = ${id}".toString(), null)
            if (oktmoList.size() != 0) {
                oktmo = oktmoList.get(0)
                OKTMO_CACHE.put(id, oktmo)
            }
        }
        return oktmo
    }

    Date getReportPeriodEndDate(Integer reportPeriodId) {
        if (reportPeriodEndDate == null) {
            reportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodId)?.time
        }
        return reportPeriodEndDate
    }

/**
 * Получить дату окончания отчетного периода
 * @return
 */
    Date getReportPeriodEndDate() {
        if (reportPeriodEndDate == null) {
            reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodEndDate
    }

    /**
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodStartDate() {
        if (reportPeriodStartDate == null) {
            reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodStartDate
    }

    /**
     * Получить календарную дату начала отчетного периода
     * @return
     */
    Date getReportPeriodCalendarStartDate() {
        if (reportPeriodCalendarStartDate == null) {
            reportPeriodCalendarStartDate = reportPeriodService.getCalendarStartDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodCalendarStartDate
    }

/**
 * Получить "Коды мест предоставления документа"
 * @return
 */
    Map<Long, Map<String, RefBookValue>> getRefPresentPlace() {
        if (presentPlaceCodeCache.size() == 0) {
            List<Map<String, RefBookValue>> refBookMap = getRefBook(REF_BOOK_TAX_PLACE_TYPE_CODE_ID)
            refBookMap.each { Map<String, RefBookValue> refBook ->
                presentPlaceCodeCache.put((Long) refBook?.id?.value, refBook)
            }
        }
        return presentPlaceCodeCache
    }

/**
 * Получить все записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - возвращает лист
 */
    List<Map<String, RefBookValue>> getRefBook(Long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        List<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate(), null, null, null)
        if (refBookList == null || refBookList.size() == 0) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
    }

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
    }
/************************************* СПЕЦОТЧЕТ **********************************************************************/

    final String ALIAS_PRIMARY_RNU_W_ERRORS = "primary_rnu_w_errors"

    final String TRANSPORT_FILE_TEMPLATE = "ТФ"

    def createSpecificReport() {
        def alias = scriptSpecificReportHolder.getDeclarationSubreport().getAlias()
        if (alias == ALIAS_PRIMARY_RNU_W_ERRORS) {
            createPrimaryRnuWithErrors()
            return
        }

        def params = scriptSpecificReportHolder.subreportParamValues ?: new HashMap<String, Object>()

        def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
            buildXmlForSpecificReport(it)
        });

        declarationService.exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
        scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".xlsx")
    }

/**
 * Создать Спецотчет Первичные РНУ с ошибками
 * @return
 */
    def createPrimaryRnuWithErrors() {
        // Сведения о доходах из КНФ, которая является источником для входящей ОНФ и записи в реестре справок соответствующим доходам физлицам имеют ошибки
        List<NdflPersonIncome> ndflPersonIncomeFromRNUConsolidatedList = ndflPersonService.findNdflPersonIncomeConsolidatedRNU6Ndfl(declarationData.id, declarationData.kpp, declarationData.oktmo)
        // Сведения о вычетах имеющие такой же operationId как и сведения о доходах
        List<NdflPersonDeduction> ndflPersonDeductionFromRNUConsolidatedList = []
        // Сведения об авансах имеющие такой же operationId как и сведения о доходах
        List<NdflPersonPrepayment> ndflPersonPrepaymentFromRNUConsolidatedList = []

        ndflPersonIncomeFromRNUConsolidatedList.each {
            ScriptUtils.checkInterrupted()
            ndflPersonDeductionFromRNUConsolidatedList.addAll(ndflPersonService.findDeductionsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
            ndflPersonPrepaymentFromRNUConsolidatedList.addAll(ndflPersonService.findPrepaymentsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
        }

        ndflPersonIncomeFromRNUConsolidatedList.each {
            ScriptUtils.checkInterrupted()
            NdflPersonIncome ndflPersonIncomePrimary = ndflPersonService.getIncome(it.sourceId)
            NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonIncomePrimary.ndflPersonId)
            ndflPersonPrimary.incomes.add(ndflPersonIncomePrimary)
        }

        ndflPersonDeductionFromRNUConsolidatedList.each {
            ScriptUtils.checkInterrupted()
            NdflPersonDeduction ndflPersonDeductionPrimary = ndflPersonService.getDeduction(it.sourceId)
            NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonDeductionPrimary.ndflPersonId)
            ndflPersonPrimary.deductions.add(ndflPersonDeductionPrimary)
        }

        ndflPersonPrepaymentFromRNUConsolidatedList.each {
            ScriptUtils.checkInterrupted()
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
        OutputStream writer = scriptSpecificReportHolder.getFileOutputStream()
        XSSFWorkbook workbook = getSpecialReportTemplate()
        fillGeneralData(workbook)
        fillPrimaryRnuNDFLWithErrorsTable(workbook)
        workbook.write(writer)
        writer.close()
        StringBuilder fileName = new StringBuilder("Первичные_РНУ_с_ошибками_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
        scriptSpecificReportHolder
                .setFileName(fileName.toString())
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
        String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
                .getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}".toString(), null).get(0).NAME.value
        // Территориальный банк
        String departmentName = department.name
        // КПП
        String kpp = declarationData.kpp
        //	Дата сдачи корректировки
        String dateDelivery = departmentReportPeriod.correctionDate?.format(SharedConstants.DATE_FORMAT)
        // ОКТМО
        String oktmo = declarationData.oktmo
        // Код НО (конечный)
        String taxOrganCode = declarationData.taxOrganCode
        // Дата формирования
        String currentDate = new Date().format(SharedConstants.DATE_FORMAT, TimeZone.getTimeZone('Europe/Moscow'))

        XSSFCell cell1 = sheet.getRow(2).createCell(1)

        cell1.setCellValue(StringUtils.defaultString(declarationTypeName) + " " + StringUtils.defaultString(note))
        cell1.setCellStyle(style)
        XSSFCell cell2 = sheet.getRow(3).createCell(1)
        cell2.setCellValue(year + ":" + StringUtils.defaultString(periodName))
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
        def startIndex = 12
        ndflpersonFromRNUPrimary.values().each { ndflPerson ->
            ndflPerson.incomes.each { income ->
                ScriptUtils.checkInterrupted()
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
        String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
                .getRecords(getReportPeriodEndDate(primaryRnuDeclarationData.reportPeriodId), null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}".toString(), null).get(0).NAME.stringValue
        // Подразделение
        String departmentName = department.shortName
        // АСНУ
        String asnu = getProvider(RefBook.Id.ASNU.getId()).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, "ID = ${primaryRnuDeclarationData.asnuId}".toString(), null).get(0).NAME.stringValue
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

    String formatDate(Date date) {
        return ScriptUtils.formatDate(date, SharedConstants.DATE_FORMAT)
    }

    /**
     * Класс содержащий данные неоходимые для формирования раздела 2 формы 6НДФЛ
     */
    class Section2DataHolder {
        // Строка удержания налога
        NdflPersonIncome withholdingRow
        // Дата Фактического Получения Дохода
        Date virtuallyReceivedIncomeDate
        // Дата Удержания Налога
        Date witholdingDate
        // Срок Перечисления Налога
        Date taxTransferDate

        Section2DataHolder(NdflPersonIncome withholdingRow, Date virtuallyReceivedIncomeDate, Date witholdingDate, Date taxTransferDate) {
            this.withholdingRow = withholdingRow
            this.virtuallyReceivedIncomeDate = virtuallyReceivedIncomeDate
            this.witholdingDate = witholdingDate
            this.taxTransferDate = taxTransferDate


        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            Section2DataHolder that = (Section2DataHolder) o

            if (taxTransferDate != that.taxTransferDate) return false
            if (virtuallyReceivedIncomeDate != that.virtuallyReceivedIncomeDate) return false
            if (witholdingDate != that.witholdingDate) return false

            return true
        }

        int hashCode() {
            int result
            result = (virtuallyReceivedIncomeDate != null ? virtuallyReceivedIncomeDate.hashCode() : 0)
            result = 31 * result + (witholdingDate != null ? witholdingDate.hashCode() : 0)
            result = 31 * result + (taxTransferDate != null ? taxTransferDate.hashCode() : 0)
            return result
        }
    }
}