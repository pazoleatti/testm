package form_template.ndfl.report_2ndfl_1.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.refbook.*
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils


import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.slurpersupport.GPathResult

import java.util.zip.ZipInputStream


new Check(this).run()

@TypeChecked
class Check extends AbstractScriptClass {

    DeclarationData declarationData
    DepartmentReportPeriodService departmentReportPeriodService
    DepartmentService departmentService
    RefBookService refBookService
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService

    int NDFL_2_1_DECLARATION_TYPE = 102
    int NDFL_2_2_DECLARATION_TYPE = 104
    final int DECLARATION_TYPE_NDFL6_ID = 103
    final String DATE_FORMAT_DOTTED = "dd.MM.yyyy"
    // Дата окончания отчетного периода
    Date reportPeriodEndDate = null

    final String FILE_NODE = "Файл"
    final String DOCUMENT_NODE = "Документ";
    final String SVEDDOH_NODE = "СведДох";
    final String SV_SUM_DOH = "СвСумДох";
    final String SV_SUM_VICH = "СвСумВыч";
    final String PRED_VICH_SSI = "ПредВычССИ";
    final String LAST_NAME = "Фамилия"
    final String FIRST_NAME = "Имя"
    final String MIDDLE_NAME = "Отчество"
    final String NUM_SPR = "НомСпр"
    final String INN_SPR = "ИННФЛ"
    final String CITIZENSHIP_CODE = "Гражд"
    final String ID_DOC_TYPE = "КодУдЛичн"
    final String ID_DOC_NUMBER = "СерНомДок"
    final String TAX_RATE = "Ставка"
    final String CALCULATED_TAX = "НалИсчисл"
    final String TAX_SUM = "НалПеречисл"
    final String WITHHOLDING_TAX = "НалУдерж"
    final String PREPAYMENT_SUM = "АвансПлатФикс"
    final String INCOME_SUM_COMMON = "СумДохОбщ"
    final String SUM_DOHOD = "СумДоход"
    final String INCOME_CODE = "КодДоход"
    final String DEDUCTION_CODE = "КодВычет"
    final String DEDUCTION_SUM = "СумВычет"
    final String TAX_BASE = "НалБаза"
    final String NOT_HOLDING_TAX = "НалНеУдерж"
    final String NODE_NAME_SUM_IT_NAL_PER2 = "СумИтНалПер"
    final String TAX_ORGAN_CODE = "КодНО"
    final String UVED_SOTS_VICH = "УведСоцВыч"
    final String UVED_IMUSCH_VICH = "УведИмущВыч"
    final String IFNS_UVED = "ИФНСУвед"

// Узлы 6 НДФЛ
    final NODE_NAME_SUM_STAVKA6 = "СумСтавка"
    final NODE_NAME_OBOBSH_POKAZ6 = "ОбобщПоказ"
    final NODE_NAME_SUM_DATA6 = "СумДата"
// Атрибуты 6 НДФЛ
    final ATTR_NACHISL_DOH6 = "НачислДох"
    final ATTR_NACHISL_DOH_DIV6 = "НачислДохДив"
    final ATTR_VICHET_NAL6 = "ВычетНал"
    final ATTR_ISCHISL_NAL6 = "ИсчислНал"
    final ATTR_NE_UDERZ_NAL_IT6 = "НеУдержНалИт"
    final ATTR_KOL_FL_DOHOD6 = "КолФЛДоход"
    final ATTR_AVANS_PLAT6 = "АвансПлат"

    // Кэш для справочников
    Map<String, Map<String, RefBookValue>> refBookCache = [:]
    // Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]

    private Check() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Check(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getBinding().getProperty("refBookService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.CHECK:
                check()
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def check() {

        ScriptUtils.checkInterrupted()
        ZipInputStream xmlStream = declarationService.getXmlStream(declarationData.id)

        // Парсим xml и компонуем содержимое в группу объектов со структурой дерева из узлов и листьев
        GPathResult fileNode = new XmlSlurper().parse(xmlStream)
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
            Map<String, RefBookValue> period = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)
            String periodCode = period?.CODE?.stringValue
            String periodName = period?.NAME?.stringValue
            Date calendarStartDate = reportPeriod?.calendarStartDate
            String correctionDateExpression = departmentReportPeriod.correctionDate == null ? "" : ", с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format(DATE_FORMAT_DOTTED)},"
            logger.warnExp("Сравнение физических лиц форм 2-НДФЛ (2) и 2-НДФЛ (1) не выполнено. Не найдена форма 2-НДФЛ (2) со следующими параметрами: КПП: \"%s\", ОКТМО: \"%s\", КодНо: \"%s\", Период: \"%s\", Подразделение: \"%s\"", "Не найдено физическое лицо из 2-НДФЛ (2) в 2-НДФЛ (1)", "", declarationData.kpp, declarationData.oktmo, declarationData.taxOrganCode, "$periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")}" + " года" + correctionDateExpression, department.getName())
        }
        List<Map<String, RefBookValue>> ndfl_2_2ReferencesList = []
        List<Map<String, RefBookValue>> ndfl_2_1ReferencesList = []
        for (DeclarationData declarationData1Ndfl_2_2 : declarationDataNdfl_2_2_List) {
            ndfl_2_2ReferencesList.addAll(getProvider(RefBook.Id.NDFL_REFERENCES.id).getRecords(null, null, "DECLARATION_DATA_ID = ${declarationData1Ndfl_2_2.id}".toString(), null))
        }
        for (DeclarationData declarationData1Ndfl_2_1 : declarationDataNdfl_2_1_List) {
            ndfl_2_1ReferencesList.addAll(getProvider(RefBook.Id.NDFL_REFERENCES.id).getRecords(null, null, "DECLARATION_DATA_ID = ${declarationData1Ndfl_2_1.id}".toString(), null))
        }
        List<Long> ndfl2_1PersonIdList = new ArrayList<>()
        for (Map<String, RefBookValue> ndfl_2_1Reference : ndfl_2_1ReferencesList) {
            ndfl2_1PersonIdList.add((Long) ndfl_2_1Reference.get("PERSON_ID").value)
        }
        for (Map<String, RefBookValue> ndfl_2_2Reference : ndfl_2_2ReferencesList) {
            if (!ndfl2_1PersonIdList.contains(ndfl_2_2Reference.get("PERSON_ID").value)) {
                Long numSpr = (Long) ndfl_2_2Reference.get("NUM").value
                Long ddId = (Long) ndfl_2_2Reference.get("DECLARATION_DATA_ID").value
                StringBuilder fio = new StringBuilder(ndfl_2_2Reference.get("SURNAME").stringValue ?: "")
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

    @TypeChecked(TypeCheckingMode.SKIP)
    def interdocumentary6ndflCheckData() {

        List<DeclarationData> ndfl6declarationDataList = declarationService.find(DECLARATION_TYPE_NDFL6_ID, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp, declarationData.oktmo)
        if (ndfl6declarationDataList.isEmpty()) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            Department department = departmentService.get(departmentReportPeriod.departmentId)
            logger.warn("Выполнить проверку междокументных контрольных соотношений невозможно. Отсутствует форма 6-НДФЛ для подразделения: \"${department.name}\", КПП: ${declarationData.kpp}, ОКТМО: ${declarationData.oktmo}, Код НО: ${declarationData.taxOrganCode}, период: ${departmentReportPeriod.reportPeriod.name} ${departmentReportPeriod.reportPeriod.taxPeriod.year} ${departmentReportPeriod.correctionDate ? " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(DATE_FORMAT_DOTTED) : ""}")
            return
        }
        if (ndfl6declarationDataList.size() > 1) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            Department department = departmentService.get(departmentReportPeriod.departmentId)
            logger.warn("Выполнить проверку междокументных контрольных соотношений невозможно. В Системе должна быть только одна форма 6-НДФЛ для подразделения: \"${department.name}\", КПП: ${declarationData.kpp}, ОКТМО: ${declarationData.oktmo}, Код НО: ${declarationData.taxOrganCode}, период: ${departmentReportPeriod.reportPeriod.name} ${departmentReportPeriod.reportPeriod.taxPeriod.year} ${departmentReportPeriod.correctionDate ? " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(DATE_FORMAT_DOTTED) : ""}")
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
        if (ndfl6Stream == null) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            Department department = departmentService.get(departmentReportPeriod.departmentId)
            logger.warn("Выполнить проверку междокументных контрольных соотношений невозможно. Заблокирована форма 6-НДФЛ для подразделения: \"${department.name}\", КПП: ${ndfl6declarationData.kpp}, ОКТМО: ${ndfl6declarationData.oktmo}, Код НО: ${ndfl6declarationData.taxOrganCode}, период: ${departmentReportPeriod.reportPeriod.name} ${departmentReportPeriod.reportPeriod.taxPeriod.year} ${departmentReportPeriod.correctionDate ? " с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format(DATE_FORMAT_DOTTED) : ""}. Форма находится в процессе создания.")
            return
        }
        def fileNode6Ndfl = new XmlSlurper().parse(ndfl6Stream);
        def sumStavkaNodes6 = fileNode6Ndfl.depthFirst().grep { it.name() == NODE_NAME_SUM_STAVKA6 }
        sumStavkaNodes6.each { sumStavkaNode6 ->
            ScriptUtils.checkInterrupted()
            def stavka6 = sumStavkaNode6.attributes()[TAX_RATE] ? Integer.valueOf(sumStavkaNode6.attributes()[TAX_RATE]) : 0

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
                logger.warnExp(msgErrorRes, "«Сумма начисленного дохода» рассчитана некорректно", "")
            }
        }

        // МежДок5
        if (ScriptUtils.round(nachislDohDiv6, 2) != ScriptUtils.round(sumDohDivObch2, 2)) {
            def msgErrorRes = sprintf(msgError, "«Сумме начисленного дохода» в виде дивидендов")
            logger.warnExp(msgErrorRes, "«Сумма начисленного дохода» рассчитана некорректно", "")
        }

        // МежДок6

        mapIschislNal6.each { stavka6, ischislNal6 ->
            def nalIschisl2 = mapNalIschisl2.get(stavka6)
            if (ischislNal6 != nalIschisl2) {
                def msgErrorRes = sprintf(msgError, "«Сумме налога исчисленного»") + " по «Ставке» " + stavka6
                logger.warnExp(msgErrorRes, "«Сумма налога исчисленного» рассчитана некорректно", "")
            }
        }

        // МежДок7
        if (neUderzNalIt6 != nalNeUderz2) {
            def msgErrorRes = sprintf(msgError, "«Сумме налога, не удержанной налоговым агентом»")
            logger.warnExp(msgErrorRes, "«Сумма налога, не удержанная налоговым агентом» рассчитана некорректно", "")
        }

        // МежДок8
        if (kolFl6 != kolFl2) {
            def msgErrorRes = sprintf(msgError, "количеству физических лиц, получивших доход")
            logger.warnExp(msgErrorRes, "«Количество физических лиц, получивших доход» рассчитано некорректно", "")
        }
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

    /**
     * Интерфейс объявляющий метод проверки
     */
    static interface Checker {
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
            Ndfl2Leaf<String> lastNameAttribute = (Ndfl2Leaf<String>) extractAttribute(LAST_NAME, documentNode)
            Ndfl2Leaf<String> firstNameAttribute = (Ndfl2Leaf<String>) extractAttribute(FIRST_NAME, documentNode)
            Ndfl2Leaf<String> middleNameAttribute = (Ndfl2Leaf<String>) extractAttribute(MIDDLE_NAME, documentNode)
            Ndfl2Leaf<String> numSprAttribute = (Ndfl2Leaf<String>) extractAttribute(NUM_SPR, documentNode)
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
            Ndfl2Leaf<String> lastNameAttribute = (Ndfl2Leaf<String>) extractAttribute(LAST_NAME, documentNode)
            Ndfl2Leaf<String> firstNameAttribute = (Ndfl2Leaf<String>) extractAttribute(FIRST_NAME, documentNode)
            Ndfl2Leaf<String> middleNameAttribute = (Ndfl2Leaf<String>) extractAttribute(MIDDLE_NAME, documentNode)
            Ndfl2Leaf<String> numSprAttribute = (Ndfl2Leaf<String>) extractAttribute(NUM_SPR, documentNode)
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
                    Ndfl2Leaf<Integer> taxRateAttribute = (Ndfl2Leaf<Integer>) extractAttribute(TAX_RATE, svedDohNode)
                    if (taxRateAttribute != null && (taxRateAttribute.getValue() == 13 || taxRateAttribute.getValue() == 15)) {
                        List<Ndfl2Node> svSumDohList = extractNdfl2Nodes(SV_SUM_DOH, svedDohNode)
                        // Сумма разностей Файл.Документ.СведДох.ДохВыч.СвСумДох.СумДоход - Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                        BigDecimal differenceTotalSumDohSumVichForCode1010 = new BigDecimal(0)
                        BigDecimal differenceTotalSumDohSumVichForCodeNot1010 = new BigDecimal(0)
                        boolean existCodeNot1010 = false
                        for (Ndfl2Node svSumDoh : svSumDohList) {
                            Ndfl2Leaf<String> incomeCodeAttribute = (Ndfl2Leaf<String>) extractAttribute(INCOME_CODE, svSumDoh)
                            if (incomeCodeAttribute != null && incomeCodeAttribute.getValue() == "1010") {
                                Ndfl2Leaf<BigDecimal> incomeSumAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(SUM_DOHOD, svSumDoh)
                                List<Ndfl2Node> svSumVichNodeList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                                // Сумма значений Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                                BigDecimal deductionSumValue = new BigDecimal(0)
                                for (Ndfl2Node svSumVich : svSumVichNodeList) {
                                    Ndfl2Leaf<BigDecimal> sumVichAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(DEDUCTION_SUM, svSumVich)
                                    deductionSumValue = deductionSumValue.add(sumVichAttribute.getValue())
                                }
                                //Разность между доходом и вычетом
                                BigDecimal differenceSumDohSumVich = incomeSumAttribute?.getValue().subtract(deductionSumValue)
                                if (differenceSumDohSumVich < new BigDecimal(0)) {
                                    differenceSumDohSumVich = new BigDecimal(0)
                                }
                                differenceTotalSumDohSumVichForCode1010 = differenceTotalSumDohSumVichForCode1010.add(differenceSumDohSumVich)
                            } else {
                                existCodeNot1010 = true;
                                Ndfl2Leaf<BigDecimal> incomeSumAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(SUM_DOHOD, svSumDoh)
                                List<Ndfl2Node> svSumVichNodeList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                                // Сумма значений Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                                BigDecimal deductionSumValue = new BigDecimal(0)
                                for (Ndfl2Node svSumVich : svSumVichNodeList) {
                                    Ndfl2Leaf<BigDecimal> sumVichAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(DEDUCTION_SUM, svSumVich)
                                    deductionSumValue = deductionSumValue.add((BigDecimal) sumVichAttribute.getValue())
                                }
                                //Разность между доходом и вычетом
                                BigDecimal differenceSumDohSumVich = ((BigDecimal) incomeSumAttribute.getValue()).subtract(deductionSumValue)
                                if (differenceSumDohSumVich < new BigDecimal(0)) {
                                    differenceSumDohSumVich = new BigDecimal(0)
                                }
                                differenceTotalSumDohSumVichForCodeNot1010 = differenceTotalSumDohSumVichForCodeNot1010.add(differenceSumDohSumVich)
                            }
                        }
                        if (existCodeNot1010) {
                            List<Ndfl2Node> predVichSSINodeList = extractNdfl2Nodes(PRED_VICH_SSI, svedDohNode)
                            // Сумма Файл.Документ.СведДох.НалВычССИ.ПредВычССИ.СумВычет
                            BigDecimal sumVich = new BigDecimal(0)
                            for (Ndfl2Node predVichSSI : predVichSSINodeList) {
                                Ndfl2Leaf<BigDecimal> vich = (Ndfl2Leaf<BigDecimal>) extractAttribute(DEDUCTION_SUM, predVichSSI)
                                BigDecimal valueVich = (BigDecimal) vich.getValue()
                                sumVich = sumVich.add(valueVich)
                            }
                            differenceTotalSumDohSumVichForCodeNot1010 = differenceTotalSumDohSumVichForCodeNot1010.subtract(sumVich)
                        }
                        //Результат для п.4 Проверка расчета суммы исчисленного налога I.1
                        BigDecimal calclulateTaxCheckValueForIncomeCode1010 = differenceTotalSumDohSumVichForCode1010
                        //Результат для п.4 Проверка расчета суммы исчисленного налога I.2
                        BigDecimal calclulateTaxCheckValueForIncomeCodeNot1010 = differenceTotalSumDohSumVichForCodeNot1010
                        //Результат для п.4 Проверка расчета суммы исчисленного налога I.3
                        BigDecimal calculatedTaxCheckBase = calclulateTaxCheckValueForIncomeCode1010.add(calclulateTaxCheckValueForIncomeCodeNot1010)
                        Ndfl2Leaf<BigDecimal> taxBaseAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(TAX_BASE, svedDohNode)
                        BigDecimal differenceTaxBaseAndCalculatedTaxCheckSum = taxBaseAttribute.getValue().subtract(calculatedTaxCheckBase)
                        if (differenceTaxBaseAndCalculatedTaxCheckSum.abs() > 1) {
                            createErrorMessage(logger, documentNode, "«Налоговая база» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" для «Ставки» (\"${taxRateAttribute.getValue()}\") «Налоговой базы» (\"${taxBaseAttribute.getValue()}\") должна быть равна \"${calculatedTaxCheckBase}\".")
                        }
                        BigDecimal calculatedTaxCheckSum = ScriptUtils.round(calclulateTaxCheckValueForIncomeCode1010.add(calclulateTaxCheckValueForIncomeCodeNot1010).multiply(new BigDecimal(taxRateAttribute.getValue())).divide(new BigDecimal(100)), 0)
                        Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(CALCULATED_TAX, svedDohNode)
                        BigDecimal differenceCalculatedTaxAndCalculatedTaxCheckSum = calculatedTaxAttribute.getValue().subtract(calculatedTaxCheckSum)
                        if (differenceCalculatedTaxAndCalculatedTaxCheckSum.abs() > 1) {
                            createErrorMessage(logger, documentNode, "«Сумма налога исчисленная» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" для «Ставки» (\"${taxRateAttribute.getValue()}\") «Сумма налога исчисленная» (\"${calculatedTaxAttribute.getValue()}\") должна быть равна \"${calculatedTaxCheckSum}\".")
                        }
                    } else if (taxRateAttribute != null && taxRateAttribute.getValue() == 30) {
                        Ndfl2Leaf<BigDecimal> taxBaseAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(TAX_BASE, svedDohNode)
                        BigDecimal calculatedTaxCheckSum = ScriptUtils.round(taxBaseAttribute.getValue().multiply(new BigDecimal(taxRateAttribute.getValue())).divide(new BigDecimal(100)), 2)
                        Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(CALCULATED_TAX, svedDohNode)
                        BigDecimal differenceCalculatedTaxAndCalculatedTaxCheckSum = calculatedTaxAttribute.getValue().subtract(calculatedTaxCheckSum)
                        if (differenceCalculatedTaxAndCalculatedTaxCheckSum.abs() > 1) {
                            createErrorMessage(logger, documentNode, "«Сумма налога исчисленная» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" для «Ставки» (\"${taxRateAttribute.getValue()}\") «Сумма налога исчисленная» (\"${calculatedTaxAttribute.getValue()}\") должна быть равна \"${calculatedTaxCheckSum}\".")
                        }
                    } else {
                        List<Ndfl2Node> svSumDohList = extractNdfl2Nodes(SV_SUM_DOH, svedDohNode)
                        BigDecimal differenceTotalSumDohSumVich = new BigDecimal(0)
                        for (Ndfl2Node svSumDoh : svSumDohList) {
                            Ndfl2Leaf<BigDecimal> incomeSumAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(SUM_DOHOD, svSumDoh)
                            List<Ndfl2Node> svSumVichNodeList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                            // Сумма значений Файл.Документ.СведДох.ДохВыч.СвСумДох.СвСумВыч.СумВычет
                            BigDecimal deductionSumValue = new BigDecimal(0)
                            for (Ndfl2Node svSumVich : svSumVichNodeList) {
                                Ndfl2Leaf<BigDecimal> sumVichAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(DEDUCTION_SUM, svSumVich)
                                deductionSumValue.add(sumVichAttribute.getValue())
                            }
                            //Разность между доходом и вычетом
                            BigDecimal differenceSumDohSumVich = incomeSumAttribute.getValue().subtract(deductionSumValue)
                            if (differenceSumDohSumVich < new BigDecimal(0)) {
                                differenceSumDohSumVich = new BigDecimal(0)
                            }
                            differenceTotalSumDohSumVich = differenceTotalSumDohSumVich.add(differenceSumDohSumVich)
                        }
                        BigDecimal calculatedTaxCheckBase = differenceTotalSumDohSumVich
                        BigDecimal calculatedTaxCheckSum = ScriptUtils.round(calculatedTaxCheckBase.multiply(new BigDecimal(taxRateAttribute.getValue())).divide(new BigDecimal(100)), 0)
                        Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(CALCULATED_TAX, svedDohNode)
                        Ndfl2Leaf<BigDecimal> taxBaseAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(TAX_BASE, svedDohNode)
                        BigDecimal differenceCalculatedTaxAndCalculatedTaxCheckSum = calculatedTaxAttribute.getValue().subtract(calculatedTaxCheckSum)
                        BigDecimal differenceTaxBaseAndCalculatedTaxCheckSum = taxBaseAttribute.getValue().subtract(calculatedTaxCheckBase)
                        if (differenceTaxBaseAndCalculatedTaxCheckSum.abs() > 1) {
                            createErrorMessage(logger, documentNode, "«Налоговая база» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" для «Ставки» (\"${taxRateAttribute.getValue()}\") «Налоговой базы» (\"${taxBaseAttribute.getValue()}\") должна быть равна \"${calculatedTaxCheckBase}\".")
                        }
                        if (differenceCalculatedTaxAndCalculatedTaxCheckSum.abs() > 1) {
                            createErrorMessage(logger, documentNode, "«Сумма налога исчисленная» рассчитана некорректно", "В \"Разделе 5. \"Общие суммы дохода и налога\" для «Ставки» (\"${taxRateAttribute.getValue()}\") «Сумма налога исчисленная» (\"${calculatedTaxAttribute.getValue()}\") должна быть равна \"${calculatedTaxCheckSum}\".")
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
                    Ndfl2Leaf<BigDecimal> withHoldingTaxAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(WITHHOLDING_TAX, svedDohNode)
                    Ndfl2Leaf<BigDecimal> taxSumAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(TAX_SUM, svedDohNode)
                    BigDecimal taxSum = (BigDecimal) taxSumAttribute.getValue()
                    BigDecimal withHoldingTax = (BigDecimal) withHoldingTaxAttribute.getValue()
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
                    Ndfl2Leaf<BigDecimal> prepaymentAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(PREPAYMENT_SUM, svedDohNode)
                    Ndfl2Leaf<BigDecimal> calculatedTaxAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(CALCULATED_TAX, svedDohNode)
                    BigDecimal prepayment = (BigDecimal) prepaymentAttribute.getValue()
                    BigDecimal calculatedTax = (BigDecimal) calculatedTaxAttribute.getValue()
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
                        Ndfl2Leaf<BigDecimal> incomeSumAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(SUM_DOHOD, svSumDoh)
                        sumDohodSum = sumDohodSum.add(incomeSumAttribute.getValue())
                    }
                    Ndfl2Leaf<BigDecimal> incomeSumCommonAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(INCOME_SUM_COMMON, svedDohNode)
                    BigDecimal incomeSumCommon = (BigDecimal) incomeSumCommonAttribute.getValue()
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
                        Ndfl2Leaf<BigDecimal> incomeSumAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(SUM_DOHOD, svSumDoh)
                        List<Ndfl2Node> svSumVichList = extractNdfl2Nodes(SV_SUM_VICH, svSumDoh)
                        BigDecimal income = incomeSumAttribute.getValue()
                        for (Ndfl2Node svSumVich : svSumVichList) {
                            Ndfl2Leaf<BigDecimal> deductionSumAttribute = null
                            if (!svSumVichList.isEmpty()) {
                                deductionSumAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(DEDUCTION_SUM, svSumVich)
                            }
                            BigDecimal deduction = deductionSumAttribute ? (BigDecimal) deductionSumAttribute.getValue() : new BigDecimal(0)
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
                    Ndfl2Leaf<BigDecimal> notHoldingTaxAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(NOT_HOLDING_TAX, svedDohNode)
                    if (((BigDecimal) notHoldingTaxAttribute.value) <= new BigDecimal(0)) {
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
                    Ndfl2Leaf<BigDecimal> withHoldingTaxAttribute = (Ndfl2Leaf<BigDecimal>) extractAttribute(WITHHOLDING_TAX, svedDohNode)
                    BigDecimal withHoldingTax = (BigDecimal) withHoldingTaxAttribute.getValue()
                    if (withHoldingTaxAttribute.getValue() != new BigDecimal(0)) {
                        createErrorMessage(logger, documentNode, "«Сумма налога удержанная» заполнена некорректно", "Сумма налога удержанная» $withHoldingTax в \"Разделе 5. \"Общие суммы дохода и налога\" должна быть равна \"0\"")
                    }
                }
            }
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
 * @param < T >                  - Класс значения атрибута из xml, с которым будем работать
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

    String formatDate(Date date) {
        return ScriptUtils.formatDate(date, DATE_FORMAT_DOTTED)
    }

    /**
     * Разыменование записи справочника
     */
    Map<String, RefBookValue> getRefBookValue(Long refBookId, Long recordId) {
        return refBookService.getRefBookValue(refBookId, recordId, refBookCache)
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
     * Получить "Коды налоговых органов"
     * @return
     */
    def getRefNotifSource() {
        def refBookList = getProvider(RefBook.Id.TAX_INSPECTION.id).getRecords(getReportPeriodEndDate(declarationData.reportPeriodId), null, null, null)
        return refBookList.CODE?.stringValue
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
}

