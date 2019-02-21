package form_template.ndfl.report_6ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.LockDataService
import com.aplana.sbrf.taxaccounting.service.ReportService
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService
import com.aplana.sbrf.taxaccounting.utils.ZipUtils
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.xml.MarkupBuilder
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.*

import java.nio.charset.Charset

import static java.util.Collections.singletonList

new Report6Ndfl(this).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class Report6Ndfl extends AbstractScriptClass {

    ReportFormsCreationParams reportFormsCreationParams
    DeclarationData declarationData
    DeclarationTemplate declarationTemplate
    DepartmentReportPeriod departmentReportPeriod
    ReportPeriod reportPeriod
    Department department
    NdflPersonService ndflPersonService
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    DepartmentService departmentService
    DeclarationLocker declarationLocker
    LockDataService lockDataService
    ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder
    DepartmentReportPeriodService departmentReportPeriodService
    DepartmentConfigService departmentConfigService
    SourceService sourceService
    ReportService reportService
    RefBookService refBookService
    BlobDataService blobDataService
    String applicationVersion
    Map<String, Object> paramMap

    @TypeChecked(TypeCheckingMode.SKIP)
    Report6Ndfl(scriptClass) {
        super(scriptClass)
        this.departmentReportPeriodService = (DepartmentReportPeriodService) getSafeProperty("departmentReportPeriodService")
        this.reportPeriodService = (ReportPeriodService) getSafeProperty("reportPeriodService")
        this.departmentService = (DepartmentService) getSafeProperty("departmentService")
        this.reportPeriodService = (ReportPeriodService) getSafeProperty("reportPeriodService")
        this.declarationData = (DeclarationData) getSafeProperty("declarationData")
        if (this.declarationData) {
            this.declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
            this.departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            this.department = departmentService.get(departmentReportPeriod.departmentId)
            this.reportPeriod = this.departmentReportPeriod.reportPeriod
        }
        this.ndflPersonService = (NdflPersonService) getSafeProperty("ndflPersonService")
        this.departmentConfigService = (DepartmentConfigService) getSafeProperty("departmentConfigService")
        this.sourceService = (SourceService) getSafeProperty("sourceService")
        this.reportService = (ReportService) getSafeProperty("reportService")
        this.refBookFactory = (RefBookFactory) getSafeProperty("refBookFactory")
        this.refBookService = (RefBookService) getSafeProperty("refBookService")
        this.blobDataService = (BlobDataService) getSafeProperty("blobDataServiceDaoImpl")
        this.declarationLocker = (DeclarationLocker) getSafeProperty("declarationLocker")
        this.lockDataService = (LockDataService) getSafeProperty("lockDataService")

        this.scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) getSafeProperty("scriptSpecificReportHolder")
        this.applicationVersion = (String) getSafeProperty("applicationVersion")
        this.paramMap = (Map<String, Object>) getSafeProperty("paramMap")
        this.reportFormsCreationParams = (ReportFormsCreationParams) getSafeProperty("reportFormsCreationParams")
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
                println "!CREATE_SPECIFIC_REPORT!"
                createSpecificReport()
                break
            case FormDataEvent.CREATE_FORMS: // создание экземпляра
                println "!CREATE_FORMS!"
                createReportForms()
                break
            case FormDataEvent.PRE_CREATE_REPORTS:
                preCreateReports()
                break
        }
    }

    // Коды, определяющие налоговый (отчётный) период
    final long REF_BOOK_PERIOD_CODE_ID = RefBook.Id.PERIOD_CODE.id

    final long REPORT_PERIOD_TYPE_ID = 8

    final String DATE_FORMAT_UNDERLINE = "yyyyMMdd"
    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"
    final String OUTCOMING_ATTACH_FILE_TYPE = "Исходящий в ФНС"

    // Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]

    // Кэш для справочников
    Map<String, Map<String, RefBookValue>> refBookCache = [:]

    // Мапа где ключ идентификатор NdflPerson, значение NdflPerson соответствующий идентификатору
    Map<Long, NdflPerson> ndflpersonFromRNUPrimary = [:]

    /************************************* СОЗДАНИЕ XML *****************************************************************/
    Xml buildXml(DepartmentConfig departmentConfig) {
        Xml xml = null
        File xmlFile = null
        Writer fileWriter = null
        try {
            xmlFile = File.createTempFile("file_for_validate", ".xml")
            fileWriter = new OutputStreamWriter(new FileOutputStream(xmlFile), Charset.forName("windows-1251"))
            fileWriter.write("<?xml version=\"1.0\" encoding=\"windows-1251\"?>")

            xml = buildXml(departmentConfig, fileWriter, false)
            if (xml) {
                xml.xmlFile = xmlFile
            }
            return xml
        } catch (Exception e) {
            LOG.error(e.getMessage(), e)
            logger.warn("Не удалось создать форму \"$declarationTemplate.name\" за период \"${formatPeriod(departmentReportPeriod)}\", " +
                    "подразделение: \"$department.name\", КПП: \"$declarationData.kpp\", ОКТМО: \"$declarationData.oktmo\". Ошибка: $e.message")
        } finally {
            IOUtils.closeQuietly(fileWriter)
            if (!xml) {
                deleteTempFile(xmlFile)
            }
        }
        return null
    }

    Xml buildXmlForSpecificReport(def writer) {
        def departmentConfig = departmentConfigService.findByKppAndOktmoAndDate(declarationData.kpp, declarationData.oktmo, reportPeriod.endDate)
        return buildXml(departmentConfig, writer, true)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Xml buildXml(DepartmentConfig departmentConfig, def writer, boolean isForSpecificReport) {
        ScriptUtils.checkInterrupted()
        Xml xml = new Xml()

        ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
        // Получим ИНН из справочника "Общие параметры"
        def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)
        // Получим код НО пром из справочника "Общие параметры"
        def kodNoProm = configurationParamModel?.get(ConfigurationParam.NO_CODE)?.get(0)?.get(0)

        // Код периода
        def periodCode = getRefBookValue(REF_BOOK_PERIOD_CODE_ID, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

        String strCorrPeriod = getCorrectionDateExpression(departmentReportPeriod)
        def errMsg = sprintf("Не удалось создать форму %s, за %s, подразделение: %s, КПП: %s, ОКТМО: %s.",
                DeclarationType.NDFL_6_NAME,
                "${departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear()} ${departmentReportPeriod.getReportPeriod().getName()}${strCorrPeriod}",
                department.getName(),
                declarationData.kpp,
                declarationData.oktmo)
        if (!departmentConfig.presentPlace) {
            logger.warn(errMsg + " В \"Настройках подразделений\" не указан \"Код места, по которому представляется документ\".")
            return null
        }

        // Признак лица, подписавшего документ
        def signatoryId = departmentConfig.signatoryMark?.getCode()

        // Текущая дата
        xml.date = new Date()
        xml.fileName = generateXmlFileId(departmentConfig, sberbankInnParam, declarationData.kpp, kodNoProm)
        def builder = new MarkupBuilder(writer)
        builder.setDoubleQuotes(true)
        builder.setOmitNullAttributes(true)
        builder.Файл(
                ИдФайл: xml.fileName,
                ВерсПрог: applicationVersion,
                ВерсФорм: "5.02"
        ) {
            Документ(
                    КНД: "1151099",
                    ДатаДок: xml.date.format(SharedConstants.DATE_FORMAT),
                    Период: getPeriod(departmentConfig, periodCode),
                    ОтчетГод: reportPeriod.taxPeriod.year,
                    КодНО: departmentConfig.taxOrganCode,
                    НомКорр: sprintf('%02d', declarationData.correctionNum),
                    ПоМесту: departmentConfig.presentPlace.code
            ) {
                def svNP = ["ОКТМО": declarationData.oktmo]
                // Атрибут Тлф необязателен
                if (departmentConfig.phone) {
                    svNP.put("Тлф", departmentConfig.phone)
                }
                СвНП(svNP) {
                    НПЮЛ(
                            НаимОрг: departmentConfig.name,
                            ИННЮЛ: sberbankInnParam,
                            КПП: declarationData.kpp
                    )
                }
                Подписант(
                        ПрПодп: signatoryId
                ) {
                    // Узел ФИО необязателен
                    if (departmentConfig.signatorySurName) {
                        def fio = ["Фамилия": departmentConfig.signatorySurName, "Имя": departmentConfig.signatoryFirstName]
                        // Атрибут Отчество необязателен
                        if (departmentConfig.signatoryLastName) {
                            fio.put("Отчество", departmentConfig.signatoryLastName)
                        }
                        ФИО(fio) {}
                    }
                    if (signatoryId == 2) {
                        def svPred = ["НаимДок": departmentConfig.approveDocName]
                        if (departmentConfig.approveOrgName) {
                            svPred.put("НаимОрг", departmentConfig.approveOrgName)
                        }
                        СвПред(svPred) {}
                    }
                }
                НДФЛ6() {
                    def incomeList = new IncomeList(ndflPersonService.findAllIncomesByDeclarationIdAndKppAndOktmo(sourceKnf.id, departmentConfig.kpp, departmentConfig.oktmo.code))

                    GeneralBlock generalBlock = new GeneralBlock(incomeList)
                    def section2Block = new Section2Block(incomeList)
                    def ОбобщПоказAttrs = [КолФЛДоход  : generalBlock.personCount,
                                           УдержНалИт  : generalBlock.incomeWithholdingTotal,
                                           НеУдержНалИт: generalBlock.incomeNotHoldingTotal] as Map<String, Object>

                    if (declarationData.taxRefundReflectionMode == TaxRefundReflectionMode.NORMAL) {
                        ОбобщПоказAttrs.put("ВозврНалИт", generalBlock.refoundTotal)
                    } else if (declarationData.taxRefundReflectionMode == TaxRefundReflectionMode.AS_NEGATIVE_WITHHOLDING_TAX) {
                        def refundTax = section2Block.adjustRefundTax(incomeList)
                        if (refundTax != null) {
                            ОбобщПоказAttrs.put("ВозврНалИт", refundTax)
                        }
                    }
                    ОбобщПоказ(ОбобщПоказAttrs) {
                        generalBlock.rows.eachWithIndex { row, index ->
                            ScriptUtils.checkInterrupted()
                            def СумСтавкаAttrs = [Ставка      : row.rate,
                                                  НачислДох   : ScriptUtils.round(row.accruedSum, 2),
                                                  НачислДохДив: ScriptUtils.round(row.accruedSumForDividend, 2),
                                                  ВычетНал    : ScriptUtils.round(row.deductionsSum, 2),
                                                  ИсчислНал   : row.calculatedTaxSum,
                                                  ИсчислНалДив: row.calculatedTaxSumForDividend,
                                                  АвансПлат   : row.prepaymentsSum] as Map<String, Object>
                            if (isForSpecificReport) {
                                СумСтавкаAttrs.put("НомСтр", index + 1)
                            }
                            СумСтавка(СумСтавкаAttrs) {}
                        }
                    }

                    if (declarationData.isAdjustNegativeValues() && section2Block.isEmpty()) {
                        declarationData.negativeSumsSign = NegativeSumsSign.FROM_PREV_FORM
                    }
                    if (!section2Block.isEmpty()) {
                        if (declarationData.isAdjustNegativeValues()) {
                            section2Block.adjustNegativeValues()
                            declarationData.negativeIncome = section2Block.negativeIncome.abs()
                            declarationData.negativeTax = section2Block.negativeWithholding.abs()
                            declarationData.negativeSumsSign = NegativeSumsSign.FROM_CURRENT_FORM
                        }
                        ДохНал() {
                            for (def row : section2Block) {
                                // Исключение из списка строки, для которых СуммаФактическогоДохода =0 И СуммаУдержанногоНалога =0
                                // ИЛИ СрокПеречисленияНалога НЕ принадлежит последним 3 месяцам отчетного периода
                                if (row.incomeSum || row.withholdingTaxSum) {
                                    СумДата(
                                            ДатаФактДох: formatDate(row.incomeDate),
                                            ДатаУдержНал: formatDate(row.taxDate),
                                            СрокПрчслНал: isZeroDate(row.taxTransferDate) ? SharedConstants.DATE_ZERO_AS_STRING : formatDate(row.taxTransferDate),
                                            ФактДоход: row.incomeSum,
                                            УдержНал: row.withholdingTaxSum
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }
        }

        return xml
    }

    def saveFileInfo(File xmlFile, Date currDate, String fileName) {
        String fileUuid = blobDataService.create(xmlFile, fileName + ".xml", new Date())
        def createUser = declarationService.getSystemUserInfo().getUser()

        def fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
        def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.OUTGOING_TO_FNS.code}").get(0)

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
    def generateXmlFileId(DepartmentConfig departmentConfig, String INN, String KPP, String kodNoProm) {
        String R_T = "NO_NDFL6"
        String A = kodNoProm
        String K = departmentConfig.taxOrganCode
        String O = INN + KPP
        String currDate = new Date().format(DATE_FORMAT_UNDERLINE)
        String N = UUID.randomUUID().toString().toUpperCase()
        String res = R_T + "_" + A + "_" + K + "_" + O + "_" + currDate + "_" + N
        return res
    }

    /**
     * Период
     */
    String getPeriod(DepartmentConfig departmentConfig, String periodCode) {
        if (departmentConfig.reorganization) {
            String result
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
            return result
        } else {
            return periodCode
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
    // консолидированная форма рну-ндфл по которой будут создаваться отчетные формы
    DeclarationData sourceKnf
    int requiredToCreateCount

    void createReportForms() {
        sourceKnf = declarationService.getDeclarationData(reportFormsCreationParams.sourceKnfId)
        List<DepartmentConfig> departmentConfigs = getDepartmentConfigs()
        if (!departmentConfigs) {
            return
        }
        List<DeclarationData> createdForms = []
        for (def departmentConfig : departmentConfigs) {
            ScriptUtils.checkInterrupted()

            List<DeclarationData> existingDeclarations = declarationService.findAllByTypeIdAndReportPeriodIdAndKppAndOktmo(
                    declarationTemplate.type.id, departmentReportPeriod.reportPeriod.id, departmentConfig.kpp, departmentConfig.oktmo.code)
            def lastSentForm = existingDeclarations.find { it.docStateId != RefBookDocState.NOT_SENT.id }
            if (reportFormsCreationParams.reportFormCreationMode == ReportFormCreationModeEnum.UNACCEPTED_BY_FNS) {
                if (lastSentForm && !(lastSentForm.docStateId in [RefBookDocState.REQUIRES_CLARIFICATION.id, RefBookDocState.REJECTED.id, RefBookDocState.ERROR.id])) {
                    // Формировать не требуется
                    continue
                }
            }
            declarationData = new DeclarationData()
            declarationData.declarationTemplateId = declarationTemplate.id
            declarationData.kpp = departmentConfig.kpp
            declarationData.oktmo = departmentConfig.oktmo.code
            declarationData.taxOrganCode = departmentConfig.taxOrganCode
            declarationData.adjustNegativeValues = reportFormsCreationParams.adjustNegativeValues
            declarationData.taxRefundReflectionMode = reportFormsCreationParams.taxRefundReflectionMode
            declarationData.docStateId = RefBookDocState.NOT_SENT.id
            declarationData.correctionNum = lastSentForm ? (
                    lastSentForm.docStateId in [RefBookDocState.REJECTED.id, RefBookDocState.ERROR.id] ? lastSentForm.correctionNum : lastSentForm.correctionNum + 1
            ) : 0
            declarationData.departmentReportPeriodId = departmentReportPeriod.id
            declarationData.reportPeriodId = departmentReportPeriod.reportPeriod.id
            declarationData.departmentId = departmentReportPeriod.departmentId
            declarationData.state = State.CREATED

            File zipFile = null
            Xml xml = null
            try {
                xml = buildXml(departmentConfig)
                if (xml) {
                    if (reportFormsCreationParams.reportFormCreationMode == ReportFormCreationModeEnum.BY_NEW_DATA && existingDeclarations) {
                        if (!hasDifference(xml, existingDeclarations.first())) {
                            // Формировать не требуется
                            continue
                        }
                    }
                    def formsToDelete = existingDeclarations.findAll {
                        it.docStateId == RefBookDocState.NOT_SENT.id && departmentReportPeriodService.get(it.departmentReportPeriodId).isActive()
                    }
                    if (deleteForms(formsToDelete)) {
                        createdForms.add(declarationData)
                        declarationData.fileName = xml.fileName
                        create(declarationData)
                        // Привязывание xml-файла к форме
                        saveFileInfo(xml.xmlFile, xml.date, xml.fileName)
                        zipFile = ZipUtils.archive(xml.xmlFile, xml.fileName + ".xml")
                        String uuid = blobDataService.create(zipFile, xml.fileName + ".zip", xml.date)
                        reportService.attachReportToDeclaration(declarationData.id, uuid, DeclarationDataReportType.XML_DEC)
                        // Добавление информации о источнике созданной отчетной формы.
                        sourceService.addDeclarationConsolidationInfo(declarationData.id, singletonList(sourceKnf.id))

                        logger.info("Успешно выполнено создание отчетной формы \"$declarationTemplate.name\": " +
                                "Период: \"${formatPeriod(departmentReportPeriod)}\", Подразделение: \"$department.name\", " +
                                "Вид: \"$declarationTemplate.name\", № $declarationData.id, Налоговый орган: \"$departmentConfig.taxOrganCode\", " +
                                "КПП: \"$departmentConfig.kpp\", ОКТМО: \"$departmentConfig.oktmo.code\".")
                    }
                }
            } finally {
                deleteTempFile(zipFile)
                deleteTempFile(xml?.xmlFile)
            }
        }
        logger.info("Количество успешно созданных форм: %d. Не удалось создать форм: %d.", createdForms.size(), requiredToCreateCount - createdForms.size())
    }

    void create(DeclarationData declaration) {
        Logger localLogger = new Logger()
        try {
            declarationService.createWithoutChecks(declaration, localLogger, userInfo, true)
        } finally {
            logger.entries.addAll(localLogger.entries)
        }
    }

    List<DepartmentConfig> getDepartmentConfigs() {
        if (!ndflPersonService.incomeExistsByDeclarationId(sourceKnf.id)) {
            logger.error("Отчетность $declarationTemplate.name для $department.name за период ${formatPeriod(departmentReportPeriod)} не сформирована. " +
                    "В РНУ НДФЛ (консолидированная) № $sourceKnf.id для подразделения: $department.name за период ${formatPeriod(departmentReportPeriod)} отсутствуют операции.")
            return null
        }
        List<Pair<KppOktmoPair, DepartmentConfig>> kppOktmoPairs = departmentConfigService.findAllByDeclaration(sourceKnf)
        if (reportFormsCreationParams.kppOktmoPairs) {
            kppOktmoPairs = kppOktmoPairs.findAll { reportFormsCreationParams.kppOktmoPairs.contains(it.first) }
        }
        def missingDepartmentConfigs = kppOktmoPairs.findResults { it.first == null ? it.second : null }
        for (def departmentConfig : missingDepartmentConfigs) {
            logger.error("Не удалось создать форму $declarationTemplate.name, за период ${formatPeriod(departmentReportPeriod)}, " +
                    "подразделение: $department.name, КПП: $departmentConfig.kpp, ОКТМО: $departmentConfig.oktmo.code. " +
                    "В РНУ НДФЛ (консолидированная) № $sourceKnf.id для подразделения: $department.name за период ${formatPeriod(departmentReportPeriod)} " +
                    "отсутствуют операции для указанных КПП и ОКТМО")
        }
        def missingKppOktmoPairs = kppOktmoPairs.findResults { it.second == null ? it.first : null }
        for (def kppOktmoPair : missingKppOktmoPairs) {
            logger.error("Для подразделения $department.name отсутствуют настройки подразделений для КПП: $kppOktmoPair.kpp, " +
                    "ОКТМО: $kppOktmoPair.oktmo в справочнике \"Настройки подразделений\". " +
                    "Данные формы РНУ НДФЛ (консолидированная) № $sourceKnf.id по указанным КПП и ОКТМО источника выплаты не включены в отчетность.")
        }
        requiredToCreateCount = kppOktmoPairs.count { it.second }.toInteger()
        List<DepartmentConfig> departmentConfigs = kppOktmoPairs.findAll { it.first && it.second }.collect { it.second }
        if (!departmentConfigs) {
            logger.error("Отчетность $declarationTemplate.name для $department.name за период ${formatPeriod(departmentReportPeriod)} не сформирована. " +
                    "Отсутствуют настройки указанного подразделения в справочнике \"Настройки подразделений\"")
        }
        return departmentConfigs
    }

    @TypeChecked(value = TypeCheckingMode.SKIP)
    boolean hasDifference(Xml xml, DeclarationData declarationData) {
        def xmlOld = declarationService.getXmlData(declarationData.id, userInfo)
        if (!xmlOld || !xml) {
            return true
        }
        def xmlNew = FileUtils.readFileToString(xml.xmlFile, "windows-1251")
        def ФайлOld = new XmlParser().parseText(xmlOld)
        def ФайлNew = new XmlParser().parseText(xmlNew)
        if (ФайлOld.@ВерсФорм != ФайлNew.@ВерсФорм) {
            return true
        }
        def ДокументOld = ФайлOld.Документ
        def ДокументNew = ФайлNew.Документ
        if (ДокументOld.@КодНО != ДокументNew.@КодНО || ДокументOld.@ПоМесту != ДокументNew.@ПоМесту) {
            return true
        }
        if (nodesHasDifference(ДокументOld.СвНП?.first(), ДокументNew.СвНП?.first())) {
            return true
        }
        if (nodesHasDifference(ДокументOld.Подписант?.first(), ДокументNew.Подписант?.first())) {
            return true
        }
        if (nodesHasDifference(ДокументOld.НДФЛ6?.first(), ДокументNew.НДФЛ6?.first())) {
            return true
        }
        return false
    }

    boolean nodesHasDifference(Node a, Node b) {
        return a.toString() != b.toString()
    }

    boolean deleteForms(List<DeclarationData> formsToDelete) {
        if (formsToDelete) {
            List<LockData> locks = []
            List<DeclarationData> errorForms = []
            try {
                Logger localLogger = new Logger()
                for (def formToDelete : formsToDelete) {
                    LockData lockData = declarationLocker.establishLock(formToDelete.id, OperationType.DELETE_DEC, userInfo, localLogger)
                    if (lockData) {
                        locks.add(lockData)
                    } else {
                        errorForms.add(formToDelete)
                    }
                }
                if (errorForms.isEmpty()) {
                    for (def formToDelete : formsToDelete) {
                        declarationService.delete(formToDelete.id, userInfo)
                    }
                } else {
                    logger.error("Не удалось создать форму $declarationTemplate.name, за период ${formatPeriod(departmentReportPeriod)}, " +
                            "подразделение: $department.name, КПП: $declarationData.kpp, ОКТМО: $declarationData.oktmo. Невозможно удалить старые ОНФ:")
                    logger.entries.addAll(localLogger.entries)
                    logger.error("Дождитесь завершения выполнения операций, заблокировавших формы или выполните их отмену вручную.")
                    return false
                }
            } finally {
                // удаляем блокировки
                for (LockData lockData : locks) {
                    lockDataService.unlock(lockData.getKey())
                }
            }
        }
        return true
    }

    void deleteTempFile(File tempFile) {
        if (tempFile != null && !tempFile.delete()) {
            LOG.warn(String.format("Временный файл %s не удален", tempFile.getAbsolutePath()));
        }
    }

    /**
     * Структура для хранения информации по xml-файлу с данными 6-НДФЛ
     */
    class Xml {
        // Наименование файла
        String fileName
        // Дата создания файла
        Date date
        // Сам файл
        File xmlFile
    }

    /************************************* Для выгрузки отчетности по ОНФ *******************************************************************/

    void preCreateReports() {
        ScriptUtils.checkInterrupted()
        List<DeclarationDataFile> declarationDataFileList = declarationService.findFilesWithSpecificType(declarationData.id, OUTCOMING_ATTACH_FILE_TYPE)
        if (declarationDataFileList.size() != 1) {
            paramMap.put("successfullPreCreate", false)
        } else {
            paramMap.put("successfullPreCreate", true)
        }
    }

    /************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

    String formatPeriod(DepartmentReportPeriod departmentReportPeriod) {
        String corrStr = getCorrectionDateExpression(departmentReportPeriod)
        return "$departmentReportPeriod.reportPeriod.taxPeriod.year ${departmentReportPeriod.reportPeriod.name}$corrStr"
    }

    /**
     * Форммирует строку с датой корректировки
     */
    String getCorrectionDateExpression(DepartmentReportPeriod departmentReportPeriod) {
        return departmentReportPeriod.correctionDate == null ? "" : " с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")}"
    }

    /**
     * Получить все записи справочника по его идентификатору
     * @param refBookId - идентификатор справочника
     * @return - возвращает лист
     */
    List<Map<String, RefBookValue>> getRefBook(Long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        List<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecords(reportPeriod.endDate, null, null, null)
        if (refBookList == null || refBookList.size() == 0) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
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
        })

        declarationService.exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream())
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
        // Вид отчетности
        String declarationTypeName = declarationTemplate.type.name
        String note = declarationData.note
        // Период
        int year = departmentReportPeriod.reportPeriod.taxPeriod.year
        String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
                .getRecords(reportPeriod.endDate, null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}".toString(), null).get(0).NAME.value
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
        DepartmentReportPeriod rnuDepartmentReportPeriod = departmentReportPeriodService.get(primaryRnuDeclarationData.departmentReportPeriodId)
        Department department = departmentService.get(rnuDepartmentReportPeriod.departmentId)
        // Период
        int year = rnuDepartmentReportPeriod.reportPeriod.taxPeriod.year
        String periodName = getProvider(REPORT_PERIOD_TYPE_ID)
                .getRecords(rnuDepartmentReportPeriod.reportPeriod.endDate, null, "ID = ${rnuDepartmentReportPeriod.reportPeriod.dictTaxPeriodId}".toString(), null).get(0).NAME.stringValue
        // Подразделение
        String departmentName = department.shortName
        // АСНУ
        String asnu = getProvider(RefBook.Id.ASNU.getId()).getRecords(this.reportPeriod.endDate, null, "ID = ${primaryRnuDeclarationData.asnuId}".toString(), null).get(0).NAME.stringValue
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
        def blobData = blobDataService.get(scriptSpecificReportHolder.getDeclarationSubreport().getBlobDataId())
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

    boolean isZeroDate(Date date) {
        return formatDate(date) == SharedConstants.DATE_ZERO_AS_DATE
    }

    String formatDate(Date date) {
        return ScriptUtils.formatDate(date, SharedConstants.DATE_FORMAT)
    }

    /**
     * Принадлежит отчетному периоду
     */
    boolean isBelongToPeriod(Date date) {
        return date != null && (reportPeriod.startDate <= date && date <= reportPeriod.endDate)
    }

    /**
     * Принадлежит последний 3 месяцам отчетного периода
     */
    boolean isBelongToCalendarPeriod(Date date) {
        return date != null && (reportPeriod.calendarStartDate <= date && date <= reportPeriod.endDate)
    }

    /**
     * Список строк дохода и всякие группировки, чтобы лишний раз не группировать
     */
    class IncomeList extends ArrayList<NdflPersonIncome> {
        Map<String, List<NdflPersonIncome>> incomesByOperationId
        Map<Integer, List<NdflPersonIncome>> incomesByRate

        IncomeList(List<NdflPersonIncome> incomes) {
            super(incomes)
        }

        Map<String, List<NdflPersonIncome>> groupByOperationId() {
            if (!incomesByOperationId) {
                incomesByOperationId = this.groupBy { it.operationId }
            }
            return incomesByOperationId
        }

        Map<Integer, List<NdflPersonIncome>> groupByTaxRate() {
            if (!incomesByRate) {
                incomesByRate = this.groupBy { it.taxRate }
            }
            return incomesByRate
        }
    }

    /**
     * Элемент Файл.Документ.НДФЛ6.ОбобщПоказ из xml
     */
    class GeneralBlock {
        // КолФЛДоход
        int personCount
        // Сумма удержанная
        BigDecimal incomeWithholdingTotal = 0
        // Сумма не удержанная
        BigDecimal incomeNotHoldingTotal = 0
        // Сумма возвращенная
        Long refoundTotal = 0L
        // Строки раздела, общие данные по ставкке
        List<GeneralBlockRow> rows = []

        GeneralBlock(IncomeList incomeList) {
            Set<Long> personIds = []
            for (def income : incomeList) {
                if (isBelongToPeriod(income.incomeAccruedDate) && income.incomeAccruedSumm > 0) {
                    personIds.add(income.ndflPersonId)
                }
            }
            personCount = personIds.size()
            BigDecimal incomeNotHoldingTaxSum = 0
            BigDecimal incomeOverholdingTaxSum = 0

            for (def income : incomeList) {
                ScriptUtils.checkInterrupted()
                if (income.withholdingTax != null && isBelongToPeriod(income.taxTransferDate) && isBelongToPeriod(income.incomePayoutDate)) {
                    incomeWithholdingTotal += income.withholdingTax
                }
                if (isBelongToPeriod(income.incomeAccruedDate) || isBelongToPeriod(income.incomePayoutDate)) {
                    incomeNotHoldingTaxSum += (income.notHoldingTax ?: 0)
                    incomeOverholdingTaxSum += (income.overholdingTax ?: 0)
                    refoundTotal += (income.refoundTax ?: 0)
                }
            }
            if (incomeNotHoldingTaxSum > incomeOverholdingTaxSum) {
                incomeNotHoldingTotal = incomeNotHoldingTaxSum - incomeOverholdingTaxSum
            }

            buildRows(incomeList)
        }

        void buildRows(IncomeList incomeList) {
            def incomesByRate = incomeList.groupByTaxRate()
            for (Integer rate : incomesByRate.keySet()) {
                ScriptUtils.checkInterrupted()
                if (rate != null) {
                    List<NdflPersonIncome> rateIncomes = incomesByRate.get(rate)
                    GeneralBlockRow row = new GeneralBlockRow(rate)
                    rows.add(row)

                    List<Long> incomeIds = incomesByRate.get(rate).id
                    List<List<Long>> incomeIdsBy1000 = incomeIds.collate(1000)

                    List<NdflPersonPrepayment> prepayments = []
                    incomeIdsBy1000.each {
                        ScriptUtils.checkInterrupted()
                        prepayments.addAll(ndflPersonService.fetchPrepaymentByIncomesIdAndAccruedDate(it, reportPeriod.startDate, reportPeriod.endDate))
                    }
                    row.prepaymentsSum = calculateSumOfPrepayments(prepayments)

                    for (NdflPersonIncome income : rateIncomes) {
                        ScriptUtils.checkInterrupted()

                        if (isBelongToPeriod(income.incomeAccruedDate)) {
                            if (income.incomeAccruedSumm != null) {
                                row.accruedSum += income.incomeAccruedSumm
                                if (income.incomeCode == "1010") {
                                    row.accruedSumForDividend += income.incomeAccruedSumm
                                }
                            }
                            if (income.totalDeductionsSumm != null) {
                                row.deductionsSum += income.totalDeductionsSumm
                            }
                            if (income.calculatedTax != null) {
                                row.calculatedTaxSum += income.calculatedTax
                                if (income.incomeCode == "1010") {
                                    row.calculatedTaxSumForDividend += income.calculatedTax
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class GeneralBlockRow {
        int rate
        BigDecimal accruedSum = 0
        BigDecimal accruedSumForDividend = 0
        BigDecimal deductionsSum = 0
        BigDecimal prepaymentsSum = 0
        BigDecimal calculatedTaxSum = 0
        BigDecimal calculatedTaxSumForDividend = 0

        GeneralBlockRow(int rate) {
            this.rate = rate
        }
    }

    /**
     * Элемент Файл.Документ.НДФЛ6.ДохНал из xml (БлокРаздела2)
     */
    class Section2Block implements Iterable<Section2Row> {
        Map<Section2Key, Section2Row> rowsByKey = [:]
        // Отрицательная сумма дохода, оставшаяся после корректировки отрицательных значений
        BigDecimal negativeIncome = new BigDecimal(0)
        // Отрицательная сумма налога, оставшаяся после корректировки отрицательных значений
        BigDecimal negativeWithholding = new BigDecimal(0)

        Collection<Section2Row> getRows() {
            return rowsByKey.values()
        }

        Section2Block(IncomeList incomeList) {
            def incomesByOperationId = incomeList.groupByOperationId()
            for (NdflPersonIncome income : incomeList) {
                if (income.incomePayoutDate != null && (
                        isBelongToCalendarPeriod(income.taxTransferDate) ||
                                isZeroDate(income.taxTransferDate) && isBelongToCalendarPeriod(income.taxDate)
                )) {
                    List<Date> incomeAccruedDateList = []
                    for (NdflPersonIncome incomeGrouped : incomesByOperationId.get(income.operationId)) {
                        if (incomeGrouped.incomeAccruedDate != null) {
                            incomeAccruedDateList << incomeGrouped.incomeAccruedDate
                        }
                    }
                    def accruedDate = incomeAccruedDateList.isEmpty() ? income.incomePayoutDate : Collections.min(incomeAccruedDateList)
                    def key = new Section2Key(accruedDate, income.taxDate, income.taxTransferDate)

                    if (!rowsByKey.containsKey(key)) {
                        rowsByKey.put(key, new Section2Row(key))
                    }
                    rowsByKey.get(key).withholdingRows.add(income)
                }
            }

            for (def row : rows) {
                row.incomeSum = 0
                row.withholdingTaxSum = 0
                for (def income : row.withholdingRows) {
                    row.incomeSum += income.incomePayoutSumm ?: 0
                    row.withholdingTaxSum += income.withholdingTax ?: 0
                }
            }
            rowsByKey = rowsByKey.sort { Map.Entry<Section2Key, Section2Row> a, Map.Entry<Section2Key, Section2Row> b ->
                a.key.taxDate <=> b.key.taxDate ?: a.key.taxTransferDate <=> b.key.taxTransferDate ?:
                        a.key.incomeDate <=> b.key.incomeDate
            }
        }

        /**
         * Распределяет возвращенный налог по блокам Раздела2, если блоков Раздела2 нет, то возвращяет сумму возвращенного налога
         */
        Long adjustRefundTax(IncomeList incomeList) {
            Long refoundTaxSum = null
            for (def income : incomeList) {
                if (isBelongToCalendarPeriod(income.taxDate) && income.refoundTax != null) {
                    if (!rows.isEmpty()) {
                        adjustRefundTax(income)
                    } else {
                        refoundTaxSum = (refoundTaxSum ?: 0) + income.refoundTax
                    }
                }
            }
            return refoundTaxSum
        }

        void adjustRefundTax(NdflPersonIncome income) {
            Section2Row rowFound = null
            for (def row : rows) {
                rowFound = row
                if (row.taxDate >= income.taxDate) {
                    break
                }
            }
            rowFound.withholdingTaxSum -= income.refoundTax
        }

        /**
         * Корректировка отрицательных значений
         */
        void adjustNegativeValues() {
            def rows = rows.sort(false) { Section2Row a, Section2Row b ->
                a.taxDate <=> b.taxDate ?: a.incomeSum <=> b.incomeSum ?: a.key.taxTransferDate <=> b.key.taxTransferDate ?:
                        a.key.incomeDate <=> b.key.incomeDate
            }
            for (def row : rows) {
                if (row.incomeSum > 0) {
                    def tmp = row.incomeSum
                    row.incomeSum += negativeIncome
                    negativeIncome += tmp
                    if (negativeIncome > 0) {
                        negativeIncome = 0
                    }
                } else {
                    negativeIncome += row.incomeSum
                }

                if (row.incomeSum < 0) {
                    row.incomeSum = 0
                }
            }
            rows = rows.sort(false) { Section2Row a, Section2Row b ->
                a.taxDate <=> b.taxDate ?: a.withholdingTaxSum <=> b.withholdingTaxSum
            }
            for (def row : rows) {
                if (row.withholdingTaxSum > 0) {
                    def tmp = row.withholdingTaxSum
                    row.withholdingTaxSum += negativeWithholding
                    negativeWithholding += tmp
                    if (negativeWithholding > 0) {
                        negativeWithholding = 0
                    }
                } else {
                    negativeWithholding += row.withholdingTaxSum
                }

                if (row.withholdingTaxSum < 0) {
                    row.withholdingTaxSum = 0
                }
            }
        }

        @Override
        Iterator<Section2Row> iterator() {
            return rows.iterator()
        }

        boolean isEmpty() {
            return rows.isEmpty()
        }
    }

    /**
     * Элемент Файл.Документ.НДФЛ6.ДохНал.СумДата из xml (в постановке СтрокаБлокаРаздела2)
     */
    @ToString(includePackage = false)
    class Section2Row {
        @Delegate
        Section2Key key
        // Строки удержания налога
        List<NdflPersonIncome> withholdingRows = []
        BigDecimal incomeSum
        BigDecimal withholdingTaxSum

        Section2Row(Section2Key key) {
            this.key = key
        }
    }

    @EqualsAndHashCode
    @ToString(includePackage = false)
    class Section2Key {
        // Дата Фактического Получения Дохода
        Date incomeDate
        // Дата Удержания Налога
        Date taxDate
        // Срок Перечисления Налога
        Date taxTransferDate

        Section2Key(Date incomeDate, Date taxDate, Date taxTransferDate) {
            this.incomeDate = incomeDate
            this.taxDate = taxDate
            this.taxTransferDate = taxTransferDate
        }
    }
}