package form_template.ndfl.report_6ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.Pair
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
import groovy.io.PlatformLineWriter
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.xml.MarkupBuilder
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.joda.time.LocalDate

import java.nio.charset.Charset

import static com.aplana.sbrf.taxaccounting.script.SharedConstants.DATE_ZERO_AS_STRING
import static java.util.Collections.singletonList

new Report6Ndfl(this).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class Report6Ndfl extends AbstractScriptClass {

    ReportFormsCreationParams reportFormsCreationParams
    DeclarationData declarationData
    DeclarationData prevDeclarationData
    DeclarationTemplate declarationTemplate
    DepartmentReportPeriod departmentReportPeriod
    ReportPeriod reportPeriod
    String periodCode
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
        this.ndflPersonService = (NdflPersonService) getSafeProperty("ndflPersonService")
        this.departmentConfigService = (DepartmentConfigService) getSafeProperty("departmentConfigService")
        this.sourceService = (SourceService) getSafeProperty("sourceService")
        this.reportService = (ReportService) getSafeProperty("reportService")
        this.refBookFactory = (RefBookFactory) getSafeProperty("refBookFactory")
        this.refBookService = (RefBookService) getSafeProperty("refBookService")
        this.blobDataService = (BlobDataService) getSafeProperty("blobDataServiceDaoImpl")
        this.declarationLocker = (DeclarationLocker) getSafeProperty("declarationLocker")
        this.lockDataService = (LockDataService) getSafeProperty("lockDataService")

        this.declarationData = (DeclarationData) getSafeProperty("declarationData")
        if (this.declarationData) {
            this.declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
            this.departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            this.department = departmentService.get(departmentReportPeriod.departmentId)
            this.reportPeriod = this.departmentReportPeriod.reportPeriod
            this.periodCode = refBookService.getRecordData(RefBook.Id.PERIOD_CODE.getId(), reportPeriod.dictTaxPeriodId)?.CODE?.stringValue
        }

        this.scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) getSafeProperty("scriptSpecificReportHolder")
        this.applicationVersion = (String) getSafeProperty("applicationVersion")
        this.paramMap = (Map<String, Object>) getSafeProperty("paramMap")
        this.reportFormsCreationParams = (ReportFormsCreationParams) getSafeProperty("reportFormsCreationParams")
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.CREATE_FORMS: // создание экземпляра
                createReportForms()
                break
            case FormDataEvent.PRE_CREATE_REPORTS:
                preCreateReports()
                break
        }
    }

    final String DATE_FORMAT_UNDERLINE = "yyyyMMdd"

    // Кэш для справочников
    Map<String, Map<String, RefBookValue>> refBookCache = [:]

    /************************************* СОЗДАНИЕ XML *****************************************************************/
    Xml buildXml(DepartmentConfig departmentConfig, List<NdflPersonIncome> incomes) {
        Xml xml = null
        File xmlFile = null
        Writer fileWriter = null
        try {
            xmlFile = File.createTempFile("file_for_validate", ".xml")
            fileWriter = new OutputStreamWriter(new FileOutputStream(xmlFile), Charset.forName("windows-1251"))
            fileWriter.write("<?xml version=\"1.0\" encoding=\"windows-1251\"?>")

            xml = buildXml(departmentConfig, incomes, fileWriter)
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

    @TypeChecked(TypeCheckingMode.SKIP)
    Xml buildXml(DepartmentConfig departmentConfig, List<NdflPersonIncome> incomes,
                 def writer) {
        ScriptUtils.checkInterrupted()
        Xml xml = new Xml()
        def incomeList = new IncomeList(incomes)

        ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
        def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)
        def kodNoProm = configurationParamModel?.get(ConfigurationParam.NO_CODE)?.get(0)?.get(0)
        boolean replaceTaxDate = 1 == Integer.valueOf(configurationParamModel?.get(ConfigurationParam.NDFL6_TAX_DATE_REPLACEMENT)?.get(0)?.get(0))

        // Код периода
        def periodCode = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)?.CODE?.stringValue

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
        def builder = new MarkupBuilder(new PlatformLineWriter(writer))
        builder.setDoubleQuotes(true)
        builder.setOmitNullAttributes(true)
        builder.Файл(ИдФайл: xml.fileName,
                ВерсПрог: applicationVersion,
                ВерсФорм: "5.02") {
            Документ(КНД: "1151099",
                    ДатаДок: xml.date.format(SharedConstants.DATE_FORMAT),
                    Период: getPeriod(departmentConfig, periodCode),
                    ОтчетГод: reportPeriod.taxPeriod.year,
                    КодНО: departmentConfig.taxOrganCode,
                    НомКорр: sprintf('%02d', declarationData.correctionNum),
                    ПоМесту: departmentConfig.presentPlace.code) {
                СвНП(ОКТМО: declarationData.oktmo,
                        Тлф: departmentConfig.phone) {
                    НПЮЛ(НаимОрг: !departmentConfig.reorganization ? departmentConfig.name : departmentConfig.reorgSuccessorName,
                            ИННЮЛ: sberbankInnParam,
                            КПП: !departmentConfig.reorganization ? declarationData.kpp : departmentConfig.reorgSuccessorKpp) {
                        if (departmentConfig.reorganization) {
                            СвРеоргЮЛ(ФормРеорг: departmentConfig.reorganization.code,
                                    КПП: departmentConfig.reorgKpp,
                                    ИННЮЛ: sberbankInnParam
                            )
                        }
                    }
                }
                Подписант(ПрПодп: signatoryId) {
                    if (departmentConfig.signatorySurName) {
                        ФИО(Фамилия: departmentConfig.signatorySurName,
                                Имя: departmentConfig.signatoryFirstName,
                                Отчество: departmentConfig.signatoryLastName) {}
                    }
                    if (signatoryId == 2) {
                        СвПред(НаимДок: departmentConfig.approveDocName,
                                НаимОрг: departmentConfig.approveOrgName) {}
                    }
                }
                НДФЛ6() {
                    def section2Block = new Section2Block(incomeList)
                    if (!section2Block.isEmpty()) {
                        section2Block.adjustRefundTax(incomeList)
                    }
                    if (declarationData.isAdjustNegativeValues()) {
                        section2Block.adjustNegativeValues()
                    }
                    declarationData.negativeIncome = section2Block.negativeIncome.abs()
                    declarationData.negativeTax = section2Block.negativeWithholding.abs()

                    def generalBlock = new GeneralBlock(incomeList, section2Block)
                    def ОбобщПоказAttrs = [КолФЛДоход  : generalBlock.personCount,
                                           УдержНалИт  : generalBlock.incomeWithholdingTotal,
                                           НеУдержНалИт: generalBlock.incomeNotHoldingTotal] as Map<String, Object>

                    if (declarationData.taxRefundReflectionMode == TaxRefundReflectionMode.NORMAL) {
                        ОбобщПоказAttrs.put("ВозврНалИт", generalBlock.refoundTotal)
                    } else if (declarationData.taxRefundReflectionMode == TaxRefundReflectionMode.AS_NEGATIVE_WITHHOLDING_TAX) {
                        if (section2Block.isEmpty()) {
                            ОбобщПоказAttrs.put("ВозврНалИт", section2Block.СуммаВНкРаспределению)
                        } else {
                            ОбобщПоказAttrs.put("ВозврНалИт", 0)
                        }
                    }
                    ОбобщПоказ(ОбобщПоказAttrs) {
                        generalBlock.rows.eachWithIndex { row, index ->
                            ScriptUtils.checkInterrupted()
                            СумСтавка(Ставка: row.rate,
                                    НачислДох: ScriptUtils.round(row.accruedSum, 2),
                                    НачислДохДив: ScriptUtils.round(row.accruedSumForDividend, 2),
                                    ВычетНал: ScriptUtils.round(row.deductionsSum, 2),
                                    ИсчислНал: row.calculatedTaxSum,
                                    ИсчислНалДив: row.calculatedTaxSumForDividend,
                                    АвансПлат: row.prepaymentsSum) {}
                        }
                    }
                    section2Block = section2Block.findAll { it.incomeSum || it.withholdingTaxSum }
                    declarationData.negativeSumsSign = declarationData.isAdjustNegativeValues() && section2Block.isEmpty() ? NegativeSumsSign.FROM_PREV_FORM : NegativeSumsSign.FROM_CURRENT_FORM
                    if (!section2Block.isEmpty()) {
                        ДохНал() {
                            for (def row : section2Block) {
                                if (replaceTaxDate && isZeroDate(row.taxTransferDate)) {
                                    logger.warnExp("В блоке Раздела 2 с параметрами: \"Дата удержания налога\": ${formatDate(row.taxDate)}; \"Срок перечисления налога: 00.00.0000; " +
                                            "\"Дата дохода\": ${formatDate(row.incomeDate)}; исходное значение \"Дата удержания налога\": ${formatDate(row.taxDate)} заменено на \"00.00.0000\".",
                                            "Замена значений \"Дата удержания налога\" на 00.00.0000", "")
                                }
                                СумДата(ДатаФактДох: formatDate(row.incomeDate),
                                        ДатаУдержНал: replaceTaxDate && isZeroDate(row.taxTransferDate) ? DATE_ZERO_AS_STRING : formatDate(row.taxDate),
                                        СрокПрчслНал: isZeroDate(row.taxTransferDate) ? DATE_ZERO_AS_STRING : formatDate(row.taxTransferDate),
                                        ФактДоход: row.incomeSum,
                                        УдержНал: row.withholdingTaxSum) {}
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
        def incomes = ndflPersonService.findNdflPersonIncome(sourceKnf.id)
        def incomesByKppOktmoPair = incomes.groupBy { new KppOktmoPair(it.kpp, it.oktmo) }
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

            prevDeclarationData = declarationService.findPrev(declarationData, RefBookDocState.ACCEPTED, RefBookDocState.WORKED_OUT)
            File zipFile = null
            Xml xml = null
            try {
                def kppOktmoIncomes = incomesByKppOktmoPair[new KppOktmoPair(departmentConfig.kpp, departmentConfig.oktmo.code)]
                xml = buildXml(departmentConfig, kppOktmoIncomes)
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
                        declarationData.fileName = xml.fileName
                        if (!create(declarationData)) {
                            continue
                        }
                        createdForms.add(declarationData)
                        // Привязывание xml-файла к форме
                        saveFileInfo(xml.xmlFile, xml.date, xml.fileName)
                        zipFile = ZipUtils.archive(xml.xmlFile, xml.fileName + ".xml")
                        String uuid = blobDataService.create(zipFile, xml.fileName + ".zip", xml.date)
                        reportService.attachReportToDeclaration(declarationData.id, uuid, DeclarationReportType.XML_DEC)
                        // Добавление информации о источнике созданной отчетной формы.
                        sourceService.addDeclarationConsolidationInfo(declarationData.id, singletonList(sourceKnf.id))

                        if (!prevDeclarationData || prevDeclarationData.adjustNegativeValues == declarationData.adjustNegativeValues) {
                            logger.info("Успешно выполнено создание отчетной формы \"$declarationTemplate.name\": " +
                                    "Период: \"${formatPeriod(departmentReportPeriod)}\", Подразделение: \"$department.name\", " +
                                    "Вид: \"$declarationTemplate.name\", № $declarationData.id, Налоговый орган: \"$departmentConfig.taxOrganCode\", " +
                                    "КПП: \"$departmentConfig.kpp\", ОКТМО: \"$departmentConfig.oktmo.code\".")
                        } else {
                            logger.info("Выполнено создание отчетной формы \"$declarationTemplate.name\": " +
                                    "Период: \"${formatPeriod(departmentReportPeriod)}\", Подразделение: \"$department.name\", " +
                                    "Вид: \"$declarationTemplate.name\", № $declarationData.id, Налоговый орган: \"$departmentConfig.taxOrganCode\", " +
                                    "КПП: \"$departmentConfig.kpp\", ОКТМО: \"$departmentConfig.oktmo.code\". " +
                                    "Внимание !!! Значение параметра \"Корректировать отрицательные значения\" отличается от значения в форме за предыдущий период. " +
                                    "Сформированная форма может быть некорректна!!!")
                        }
                    }
                }
            } finally {
                deleteTempFile(zipFile)
                deleteTempFile(xml?.xmlFile)
            }
        }
        logger.info("Количество успешно созданных форм: %d. Не удалось создать форм: %d.", createdForms.size(), requiredToCreateCount - createdForms.size())
    }

    boolean create(DeclarationData declaration) {
        ScriptUtils.checkInterrupted()
        Logger localLogger = new Logger()
        localLogger.setLogId(logger.getLogId())
        try {
            declarationService.createWithoutChecks(declaration, localLogger, userInfo, true)
        } finally {
            logger.entries.addAll(localLogger.entries)
        }
        return !localLogger.containsLevel(LogLevel.ERROR)
    }

    List<DepartmentConfig> getDepartmentConfigs() {
        if (!ndflPersonService.incomeExistsByDeclarationId(sourceKnf.id)) {
            logger.error("Отчетность $declarationTemplate.name для $department.name за период ${formatPeriod(departmentReportPeriod)} не сформирована. " +
                    "В РНУ НДФЛ (консолидированная) № $sourceKnf.id для подразделения: $department.name за период ${formatPeriod(departmentReportPeriod)} отсутствуют операции.")
            return null
        }
        List<Pair<KppOktmoPair, DepartmentConfig>> kppOktmoPairs = departmentConfigService.findAllByDeclaration(sourceKnf)
        if (reportFormsCreationParams.kppOktmoPairs) {
            kppOktmoPairs = kppOktmoPairs.findAll {
                reportFormsCreationParams.kppOktmoPairs.contains(it.first) ||
                        it.second && reportFormsCreationParams.kppOktmoPairs.contains(new KppOktmoPair(it.second.kpp, it.second.oktmo.code))
            }
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
                    "Отсутствуют пары КПП/ОКТМО, присутствующие одновременно в справочнике \"Настройки подразделений\" (актуальные на текущий момент, либо бывшие актуальными в отчетном периоде) и в КНФ.")
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
                    String error = "Не удалось создать форму $declarationTemplate.name, за период ${formatPeriod(departmentReportPeriod)}, " +
                            "подразделение: $department.name, КПП: $declarationData.kpp, ОКТМО: $declarationData.oktmo. Невозможно удалить старые ОНФ:\n"
                    for (def entry : localLogger.entries) {
                        error += entry.message + "\n"
                    }
                    logger.error(error + "Дождитесь завершения выполнения операций, заблокировавших формы или выполните их отмену вручную.")
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
        List<DeclarationDataFile> declarationDataFileList = declarationService.findAllFilesByDeclarationIdAndType(declarationData.id, AttachFileType.OUTGOING_TO_FNS)
        if (declarationDataFileList.size() != 1) {
            paramMap.put("successfullPreCreate", false)
        } else {
            paramMap.put("successfullPreCreate", true)
        }
    }

    /************************************* ОБЩИЕ МЕТОДЫ *******************************************************************/

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

    Date janStart = new LocalDate(2019, 1, 1).toDate()
    Date janEnd = new LocalDate(2019, 2, 5).toDate()
    /**
     * Принадлежит периоду, в котором возвращенный налог будет принудительно распределяться по разделу 2 из 6-НДФЛ
     */
    boolean isBelongToJanuaryPeriod(Date date) {
        return date != null && (janStart <= date && date <= janEnd)
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

        GeneralBlock(IncomeList incomeList, Section2Block section2Block) {
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
                    if (!isBelongToJanuaryPeriod(income.taxDate)) {
                        refoundTotal += (income.refoundTax ?: 0)
                    }
                }
            }
            if (!section2Block.isEmpty()) {
                incomeWithholdingTotal = incomeWithholdingTotal - (section2Block.СуммаВНкРаспределению - section2Block.negativeWithholding.abs())
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
        // СуммаВНкРаспределению
        Long СуммаВНкРаспределению = 0L

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
            for (def income : incomeList) {
                if (isRefundIncome(income)) {
                    СуммаВНкРаспределению += income.refoundTax
                }
            }
            rowsByKey = rowsByKey.sort { Map.Entry<Section2Key, Section2Row> a, Map.Entry<Section2Key, Section2Row> b ->
                a.key.taxDate <=> b.key.taxDate ?: a.key.taxTransferDate <=> b.key.taxTransferDate ?:
                        a.key.incomeDate <=> b.key.incomeDate
            }
        }

        /**
         * Определение строк с возвращенным налогом
         */
        boolean isRefundIncome(NdflPersonIncome income) {
            return (reportPeriod.taxPeriod.year == 2019 && periodCode == "21" && isBelongToJanuaryPeriod(income.taxDate) ||
                    declarationData.taxRefundReflectionMode == TaxRefundReflectionMode.AS_NEGATIVE_WITHHOLDING_TAX && isBelongToCalendarPeriod(income.taxDate)) &&
                    income.refoundTax
        }

        /**
         * Распределяет возвращенный налог по блокам Раздела2
         */
        void adjustRefundTax(IncomeList incomeList) {
            for (def income : incomeList) {
                if (isRefundIncome(income)) {
                    adjustRefundTax(income)
                }
            }
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
            if (prevDeclarationData) {
                negativeIncome = -prevDeclarationData.negativeIncome
                negativeWithholding = -prevDeclarationData.negativeTax
            }
            def rows = rows.sort(false) { Section2Row a, Section2Row b ->
                a.taxDate <=> b.taxDate ?: (sign(a.incomeSum) != sign(b.incomeSum) ? a.incomeSum <=> b.incomeSum : b.incomeSum <=> a.incomeSum) ?:
                        a.key.taxTransferDate <=> b.key.taxTransferDate ?: a.key.incomeDate <=> b.key.incomeDate
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
                a.taxDate <=> b.taxDate ?: (sign(a.withholdingTaxSum) != sign(b.withholdingTaxSum) ? a.withholdingTaxSum <=> b.withholdingTaxSum : b.withholdingTaxSum <=> a.withholdingTaxSum) ?:
                        a.key.taxTransferDate <=> b.key.taxTransferDate ?: a.key.incomeDate <=> b.key.incomeDate
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

        int sign(BigDecimal d) {
            return d < 0 ? -1 : 1
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