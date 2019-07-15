package form_template.ndfl.report_2ndfl_fl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.action.Create2NdflFLParams
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson
import com.aplana.sbrf.taxaccounting.model.util.AppFileUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.io.IOUtils
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField

import static java.util.Collections.singletonList

new Report2NdflFL(this).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class Report2NdflFL extends AbstractScriptClass {

    ValidateXMLService validateXMLService
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    DepartmentService departmentService
    DepartmentReportPeriodService departmentReportPeriodService
    SourceService sourceService
    BlobDataService blobDataService
    RefBookPersonService refBookPersonService

    DeclarationData declarationData
    DeclarationTemplate declarationTemplate
    RegistryPerson person
    ReportPeriod reportPeriod
    Department department
    Create2NdflFLParams createParams
    Map<Long, File> result = [:]

    @TypeChecked(TypeCheckingMode.SKIP)
    Report2NdflFL(scriptClass) {
        super(scriptClass)
        this.departmentReportPeriodService = (DepartmentReportPeriodService) getSafeProperty("departmentReportPeriodService")
        this.reportPeriodService = (ReportPeriodService) getSafeProperty("reportPeriodService")
        this.departmentService = (DepartmentService) getSafeProperty("departmentService")
        this.reportPeriodService = (ReportPeriodService) getSafeProperty("reportPeriodService")
        this.validateXMLService = (ValidateXMLService) getSafeProperty("validateXMLService")
        this.sourceService = (SourceService) getSafeProperty("sourceService")
        this.refBookFactory = (RefBookFactory) getSafeProperty("refBookFactory")
        this.blobDataService = (BlobDataService) getSafeProperty("blobDataServiceDaoImpl")
        this.refBookPersonService = (RefBookPersonService) getSafeProperty("refBookPersonService")

        this.declarationData = (DeclarationData) getSafeProperty("declarationData")
        if (this.declarationData) {
            this.declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        }
        this.createParams = (Create2NdflFLParams) getSafeProperty("createParams")
        if (createParams) {
            person = refBookPersonService.findById(createParams.personId)
            reportPeriod = reportPeriodService.get(createParams.reportPeriodId)
            createParams.createdReports = [:]
            createParams.createdReportsFileName = [:]
        }
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.CREATE_FORMS:
                createForms()
                break
        }
    }

    void createForms() {
        List<DeclarationDataFile> declarationDataFiles = []
        for (long declaration2Ndfl1Id : createParams.declaration2Ndfl1Ids) {
            File tmpXmlFile = null
            try {
                def declaration2Ndfl1 = declarationService.getDeclarationData(declaration2Ndfl1Id)
                def declarationDataFile = findDeclarationXmlFile(declaration2Ndfl1)
                tmpXmlFile = createTempFile(declarationDataFile)
                validateXml(declaration2Ndfl1, tmpXmlFile, declarationDataFile.fileName)
                declarationDataFiles.add(declarationDataFile)
                def referenceNums = findReferenceNums(declaration2Ndfl1, tmpXmlFile)
                for (int referenceNum : referenceNums) {
                    declarationData = create2NdflFL(declaration2Ndfl1)
                    department = departmentService.get(declarationData.departmentId)
                    if (declarationData) {
                        createPdf(tmpXmlFile, referenceNum)
                        logger.info("Успешно выполнено создание отчетной формы: \"№: $declarationData.id, Вид: $declarationTemplate.name, " +
                                "для ФЛ: $person.fullName, Период: $reportPeriod.taxPeriod.year $reportPeriod.name, " +
                                "Подразделение: ${department.shortName ?: department.name}, КПП: $declarationData.kpp, ОКТМО: $declarationData.oktmo")
                    }
                }
                scriptClass.setProperty("result", result)
            } catch (ServiceException e) {
                logger.error("Не выполнена операция: \"Создание отчетной формы 2-НДФЛ(ФЛ) Сотрудник: $person.fullName, " +
                        "Отчетный период: $reportPeriod.taxPeriod.year $reportPeriod.name. Причина: " + e.getMessage())
            } catch (Exception e) {
                LOG.error(e.message, e)
                logger.error("Не выполнена операция: \"Создание отчетной формы 2-НДФЛ(ФЛ) Сотрудник: $person.fullName, " +
                        "Отчетный период: $reportPeriod.taxPeriod.year $reportPeriod.name. Причина: " + e.getMessage())
            } finally {
                AppFileUtils.deleteTmp(tmpXmlFile);
            }
        }
        if (createParams.declaration2Ndfl1Ids && !declarationDataFiles) {
            logger.error("Не выполнена операция: \"Создание отчетной формы 2-НДФЛ(ФЛ) Сотрудник: $person.fullName, " +
                    "Отчетный период: $reportPeriod.taxPeriod.year $reportPeriod.name. Причина: " +
                    "Ни один xml файл из файлов привязанных к налоговым формам не соответствует требованиям.");
        }
    }

    DeclarationDataFile findDeclarationXmlFile(DeclarationData declaration2Ndfl1) {
        List<DeclarationDataFile> declarationDataFiles = declarationService.findAllFilesByDeclarationIdAndType(declaration2Ndfl1.getId(), AttachFileType.OUTGOING_TO_FNS);
        if (declarationDataFiles.size() == 1) {
            return declarationDataFiles.get(0);
        } else {
            throw new ServiceException("XML файл, привязанный к налоговой форме ${declarationDescription(declaration2Ndfl1)} не соответствует требованиям.")
        }
    }

    void validateXml(DeclarationData declaration2Ndfl1, File xmlFile, String fileName) {
        def logger = new Logger()
        if (!validateXMLService.validate(declaration2Ndfl1, logger, xmlFile, fileName, null)) {
            throw new ServiceException("XML файл, привязанный к налоговой форме ${declarationDescription(declaration2Ndfl1)} не соответствует требованиям.")
        }
    }

    File createTempFile(DeclarationDataFile declarationDataFile) throws IOException {
        BlobData xmlBlobData = blobDataService.get(declarationDataFile.getUuid())
        File tmpXmlFile = File.createTempFile(declarationDataFile.getFileName(), ".xml")
        tmpXmlFile.withOutputStream { outputStream ->
            IOUtils.copy(xmlBlobData.getInputStream(), outputStream)
        }
        return tmpXmlFile;
    }

    String declarationDescription(DeclarationData declaration2Ndfl1) {
        return "№: $declaration2Ndfl1.id, Период: ${formatPeriod(departmentReportPeriodService.get(declaration2Ndfl1.departmentReportPeriodId))}, " +
                "Подразделение: ${departmentService.get(declaration2Ndfl1.departmentId).name}, Вид: ${declarationService.getTemplate(declaration2Ndfl1.declarationTemplateId).name}"
    }

    DeclarationData create2NdflFL(DeclarationData declaration2Ndfl1) {
        DeclarationData declarationData = new DeclarationData()
        declarationData.setDeclarationTemplateId(declarationTemplate.id)
        declarationData.setDepartmentReportPeriodId(departmentReportPeriodService.getFirst(declaration2Ndfl1.departmentId, createParams.reportPeriodId).id)
        declarationData.setReportPeriodId(createParams.reportPeriodId)
        declarationData.setDepartmentId(declaration2Ndfl1.departmentId)
        declarationData.setKpp(declaration2Ndfl1.kpp)
        declarationData.setOktmo(declaration2Ndfl1.oktmo)
        declarationData.setPersonId(createParams.getPersonId())
        declarationData.setSignatory(createParams.getSignatory())
        declarationData.setState(State.CREATED)
        if (create(declarationData)) {
            sourceService.addDeclarationConsolidationInfo(declarationData.id, singletonList(declaration2Ndfl1.id))
            return declarationData
        }
        return null
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

    @TypeChecked(TypeCheckingMode.SKIP)
    List<Integer> findReferenceNums(DeclarationData declaration2Ndfl1, File xmlFile) {
        List<Integer> referenceNums = []
        def records = refBookFactory.getDataProvider(RefBook.Id.NDFL_REFERENCES.getId())
                .getRecordDataVersionWhere(" where declaration_data_id = $declaration2Ndfl1.id and person_id = $createParams.personId", new Date())
        for (def record : records.values()) {
            int referenceNum = record.NUM.numberValue.intValue()
            def Файл = new XmlSlurper().parse(xmlFile)
            def ndfl2NodeList = Файл.Документ."НДФЛ-2".findAll {
                it.@НомСпр == referenceNum
            }
            if (ndfl2NodeList) {
                referenceNums.add(referenceNum)
            }
        }
        return referenceNums
    }

    /**
     * Формирует описание периода в виде "<Период.Год> <Период.Наим>[ с датой сдачи корректировки <Период.ДатаКорр>]"
     */
    String formatPeriod(DepartmentReportPeriod departmentReportPeriod) {
        String corrStr = getCorrectionDateExpression(departmentReportPeriod)
        return "$departmentReportPeriod.reportPeriod.taxPeriod.year ${departmentReportPeriod.reportPeriod.name}$corrStr"
    }

    /**
     * Формирует строку с датой корректировки
     */
    String getCorrectionDateExpression(DepartmentReportPeriod departmentReportPeriod) {
        return departmentReportPeriod.correctionDate == null ? "" : " с датой сдачи корректировки ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")}"
    }

    /*********************************************ФОРМИРОФАНИЕ PDF*******************************************************/

    @TypeChecked(TypeCheckingMode.SKIP)
    void createPdf(File tmpXmlFile, Integer referenceNum) {

        Node Файл = new XmlParser().parse(tmpXmlFile)

        PDFont arial = PDType0Font.load(PDDocument.newInstance(), createParams.arialFont)
        PDFont arialbd = PDType0Font.load(PDDocument.newInstance(), createParams.arialBoldFont)

        Node reference = Файл.Документ.'НДФЛ-2'.find { doc ->
            String a = doc.@НомСпр
            String b = referenceNum.toString()
            return a == b
        }

        List<DocumentWrapper> allPages = createAndOrderReportPages(reference)

        allPages.eachWithIndex { DocumentWrapper page, int index ->
            PDDocumentCatalog docCatalog = page.document.getDocumentCatalog()
            PDAcroForm acroForm = docCatalog.getAcroForm()

            PDResources resources = acroForm.getDefaultResources()
            resources.put(COSName.getPDFName("Arial"), arial)
            resources.put(COSName.getPDFName("ArialBold"), arialbd)

            if (page instanceof BasePage) {
                new BasePageProcessor().fillPage(new PageVisitor(acroForm, Файл, reference, page.СведСумДохDataList, page.ПредВычССИList, page.СведДох, page.taxRate))
            } else {
                new DetailPageProcessor().fillPage(new PageVisitor(acroForm, Файл, reference, page.СведСумДохDataList, page.ПредВычССИList, page.СведДох, page.taxRate))
            }
            acroForm.flatten()
        }

        File pdfFile = File.createTempFile("report", ".pdf");

        PDDocument destination = new PDDocument();

        PDFMergerUtility merger = new PDFMergerUtility();

        for (DocumentWrapper document : allPages) {
            merger.appendDocument(destination, document.document)
        }

        destination.save(pdfFile)
        destination.close()

        createParams.createdReports.put(declarationData.id, pdfFile)
        createParams.createdReportsFileName.put(declarationData.id, createFileName(Файл, reference))
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    String createFileName(Node root, Node reference) {
        String toReturn = "NDFL2_FL_${reference.ПолучДох[0].ФИО[0].@Фамилия ?: ""}_${reference.ПолучДох[0].ФИО[0].@Имя?.substring(0, 1) ?: ""}_${reference.ПолучДох[0].ФИО[0].@Отчество?.substring(0, 1) ?: ""}_${root.Документ[0].@ОтчетГод}_КПП орг_${root.Документ[0].СвНА[0].СвНАЮЛ[0].@КПП}.pdf"
        return toReturn
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    /**
     * Создает и определяет порядок заполнения страниц отчета
     */
    List<DocumentWrapper> createAndOrderReportPages(Node reference) {

        List<Node> СведДохList = reference.СведДох

        СведДохList?.sort { o1, o2 ->
            o1.@Ставка <=> o2.@Ставка
        }

        List<DocumentWrapper> documentList = []



        СведДохList?.eachWithIndex { Node СведДох, int index ->

            List<Node> СведСумДохList = СведДох.ДохВыч[0].СвСумДох
            List<СведСумДохData> СведСумДохDataList = []

            СведСумДохList.eachWithIndex { Node СведСумДох, int СведСумДохIndex ->
                СведСумДохData incomeData = new СведСумДохData()
                incomeData.month = СведСумДох.@Месяц
                incomeData.incomeCode = СведСумДох.@КодДоход
                incomeData.incomeSum = СведСумДох.@СумДоход

                СведСумДохDataList << incomeData

                List<Node> СвСумВычList = СведСумДох.СвСумВыч
                СвСумВычList?.eachWithIndex { Node СвСумВыч, int СвСумВычIndex ->
                    if (СвСумВычIndex == 0) {
                        incomeData.deductionCode = СвСумВыч.@КодВычет
                        incomeData.deductionSum = СвСумВыч.@СумВычет
                    } else {
                        СведСумДохData deductionData = new СведСумДохData()
                        deductionData.month = СведСумДох.@Месяц
                        deductionData.incomeCode = СведСумДох.@КодДоход
                        deductionData.incomeSum = СведСумДох.@СумДоход
                        deductionData.taxRate = СведДох.@Ставка
                        deductionData.deductionCode = СвСумВыч.@КодВычет
                        deductionData.deductionSum = СвСумВыч.@СумВычет
                        СведСумДохDataList << deductionData
                    }
                }
            }

            List<List<СведСумДохData>> СведСумДохDataByPages = СведСумДохDataList.collate(30)

            List<Node> ПредВычССИList = СведДох.НалВычССИ[0]?.ПредВычССИ
            List<ПредВычССИData> ПредВычССИDataList = []
            ПредВычССИList?.eachWithIndex { Node ПредВычССИ, int ПредВычССИIndex ->
                ПредВычССИData data = new ПредВычССИData()
                data.code = ПредВычССИ.@КодВычет
                data.sum = ПредВычССИ.@СумВычет
                ПредВычССИDataList << data
            }

            List<List<ПредВычССИData>> ПредВычССИDataByPages = ПредВычССИDataList.collate(4)

            int maxSize = Math.max(СведСумДохDataByPages.size(), ПредВычССИDataByPages.size())

            for (int i = 0; i < maxSize; i++) {
                DocumentWrapper documentWrapper = null
                if (documentList.isEmpty()) {
                    documentWrapper = new BasePage(PDDocument.load(declarationService.getTemplateFileContent(declarationData.getDeclarationTemplateId(), DeclarationTemplateFile.NDFL_2_REFERENCE_FOR_PERSON_BASE_PAGE)))
                } else {
                    documentWrapper = new AdditionalPage(PDDocument.load(declarationService.getTemplateFileContent(declarationData.getDeclarationTemplateId(), DeclarationTemplateFile.NDFL_2_REFERENCE_FOR_PERSON_ADDITIONAL_PAGE)))
                }
                if (СведСумДохDataByPages.size() > i) {
                    documentWrapper.СведСумДохDataList = СведСумДохDataByPages.get(i)
                }
                if (ПредВычССИDataByPages.size() > i) {
                    documentWrapper.ПредВычССИList = ПредВычССИDataByPages.get(i)
                }

                if (i == 0) {
                    documentWrapper.СведДох = СведДох
                }

                documentWrapper.taxRate = СведДох.@Ставка

                documentList << documentWrapper
            }

        }

        return documentList
    }

    /**
     * Обертка для PDF документа. Каждая страница представлена отдельным PDF файлом. Как страница заполняется зависит от
     * реализации этого класса.
     */
    abstract class DocumentWrapper {
        protected PDDocument document
        protected Node СведДох
        protected List<СведСумДохData> СведСумДохDataList = []
        protected List<ПредВычССИData> ПредВычССИList = []
        protected String taxRate
    }

    class BasePage extends DocumentWrapper {
        BasePage(PDDocument document) {
            this.document = document
        }
    }

    class AdditionalPage extends DocumentWrapper {
        AdditionalPage(PDDocument document) {
            this.document = document
        }
    }

    class СведСумДохData {
        String month
        String incomeCode
        String incomeSum
        String deductionCode
        String deductionSum
        Boolean duplicate
    }

    class ПредВычССИData {
        String code
        String sum
    }

    class PageVisitor {
        PDAcroForm acroForm
        Node xmlRoot
        Node reference
        List<СведСумДохData> СведСумДохDataList = []
        List<ПредВычССИData> ПредВычССИList = []
        Node СведДох
        String taxRate

        PageVisitor(PDAcroForm acroForm, Node xmlRoot, Node reference, List<СведСумДохData> СведСумДохDataList, List<ПредВычССИData> ПредВычССИList, Node СведДох, String taxRate) {
            this.acroForm = acroForm
            this.xmlRoot = xmlRoot
            this.reference = reference
            this.СведСумДохDataList = СведСумДохDataList
            this.ПредВычССИList = ПредВычССИList
            this.СведДох = СведДох
            this.taxRate = taxRate
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    /**
     * Инкапсулирует поведение по заполнению различных разделов FDF формы
     */
    abstract class FDFiller {
        abstract void accept(PageVisitor pageVisitor)

        void processField(PDTextField field, String value, String defaultValue, String style) {
            field.setDefaultAppearance(style)
            if (value) {
                field.setValue(value)
            } else {
                field.setValue(defaultValue)
            }

        }

        String createStyle(float size, boolean bold) {
            String font = "/Arial"
            if (bold) {
                font = "/ArialBold"
            }
            return font + " " + size + " Tf 0 g"
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class BaseFiller extends FDFiller {
        @Override
        void accept(PageVisitor pageVisitor) {
            processField((PDTextField) pageVisitor.acroForm.getField("year"), pageVisitor.xmlRoot.Документ[0].@ОтчетГод, "", createStyle(9.5f, true))
            processField((PDTextField) pageVisitor.acroForm.getField("dateDay"), pageVisitor.xmlRoot.Документ[0].@ДатаДок.substring(0, 2), "", createStyle(9.5f, true))
            processField((PDTextField) pageVisitor.acroForm.getField("dateMonth"), pageVisitor.xmlRoot.Документ[0].@ДатаДок.substring(3, 5), "", createStyle(9.5f, true))
            processField((PDTextField) pageVisitor.acroForm.getField("dateYear"), pageVisitor.xmlRoot.Документ[0].@ДатаДок.substring(6), "", createStyle(9.5f, true))
            processField((PDTextField) pageVisitor.acroForm.getField("oktmo"), pageVisitor.xmlRoot.Документ[0].СвНА[0].@ОКТМО, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("phone"), pageVisitor.xmlRoot.Документ[0].СвНА[0].@Тлф, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("inn"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@ИННЮЛ, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("kpp"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@КПП, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("orgName"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@НаимОрг, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("reorgCode"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].СвРеоргЮЛ[0]?.@ФормРеорг, "-", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("innReorg"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].СвРеоргЮЛ[0]?.@ИННЮЛ, "-", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("kppReorg"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].СвРеоргЮЛ[0]?.@КПП, "-", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("innFl"), pageVisitor.reference.ПолучДох[0].@ИННФЛ, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("lastName"), pageVisitor.reference.ПолучДох[0].ФИО[0].@Фамилия, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("firstName"), pageVisitor.reference.ПолучДох[0].ФИО[0].@Имя, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("middleName"), pageVisitor.reference.ПолучДох[0].ФИО[0].@Отчество, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("taxpayerState"), pageVisitor.reference.ПолучДох[0].@Статус, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("birthDay"), pageVisitor.reference.ПолучДох[0].@ДатаРожд.substring(0, 2), "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("birthMonth"), pageVisitor.reference.ПолучДох[0].@ДатаРожд.substring(3, 5), "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("birthYear"), pageVisitor.reference.ПолучДох[0].@ДатаРожд.substring(6), "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("citizenship"), pageVisitor.reference.ПолучДох[0].@Гражд, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("idDocCode"), pageVisitor.reference.ПолучДох[0].УдЛичнФЛ[0].@КодУдЛичн, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("idDocNumber"), pageVisitor.reference.ПолучДох[0].УдЛичнФЛ[0].@СерНомДок, "", createStyle(8.5f, false))
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class DetailsFiller extends FDFiller {
        @Override
        void accept(PageVisitor pageVisitor) {
            processField((PDTextField) pageVisitor.acroForm.getField("taxRate"), pageVisitor.taxRate, "", createStyle(8.5f, true))
            for (int index = 0; index < 30; index++) {
                if (pageVisitor.СведСумДохDataList.size() > index){
                    СведСумДохData data = pageVisitor.СведСумДохDataList.get(index)
                    processField((PDTextField) pageVisitor.acroForm.getField("month${index + 1}"), data.getMonth(), "", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("incomeCode${index + 1}"), data.getIncomeCode(), "", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("incomeSum${index + 1}"), data.getIncomeSum(), "-", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionCode${index + 1}"), data.getDeductionCode(), "-", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionSum${index + 1}"), data.getDeductionSum(), "-", createStyle(8.5f, false))
                } else {
                    processField((PDTextField) pageVisitor.acroForm.getField("incomeSum${index + 1}"), null, "-", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionCode${index + 1}"), null, "-", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionSum${index + 1}"), null, "-", createStyle(8.5f, false))
                }
            }

            for (int index = 0; index < 4; index++) {
                if (pageVisitor.ПредВычССИList.size() > index){
                    ПредВычССИData data = pageVisitor.ПредВычССИList.get(index)
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionAddCode${index+1}"), data.code, "", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionAddSum${index+1}"), data.sum, "0", createStyle(8.5f, false))
                } else {
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionAddCode${index+1}"), null, "", createStyle(8.5f, false))
                    processField((PDTextField) pageVisitor.acroForm.getField("deductionAddSum${index+1}"), null, "0", createStyle(8.5f, false))
                }
            }
            processField((PDTextField) pageVisitor.acroForm.getField("incomeSumTotal"), pageVisitor.СведДох ? pageVisitor.СведДох.СумИтНалПер[0].@СумДохОбщ : null, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("taxBase"), pageVisitor.СведДох ? pageVisitor.СведДох?.СумИтНалПер[0]?.@НалБаза : null, "", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("calculatedTax"), pageVisitor.СведДох ? pageVisitor.СведДох?.СумИтНалПер[0]?.@НалИсчисл : null, "0", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("prepaymentSum"), pageVisitor.СведДох ? pageVisitor.СведДох?.СумИтНалПер[0]?.@АвансПлатФикс : null, "0", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("withholdingTax"), pageVisitor.СведДох ? pageVisitor.СведДох?.СумИтНалПер[0]?.@НалУдерж : null, "0", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("taxSumm"), pageVisitor.СведДох ? pageVisitor.СведДох?.СумИтНалПер[0]?.@НалПеречисл : null, "0", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("overholdingTax"), pageVisitor.СведДох ? pageVisitor.СведДох?.СумИтНалПер[0]?.@НалУдержЛиш : null, "0", createStyle(8.5f, false))
            processField((PDTextField) pageVisitor.acroForm.getField("notHoldingTax"), pageVisitor.СведДох ? pageVisitor.СведДох?.СумИтНалПер[0]?.@НалНеУдерж : null, "0", createStyle(8.5f, false))

            processField((PDTextField) pageVisitor.acroForm.getField("signatory"), createParams.signatory, "", createStyle(8.5f, false))
        }
    }

    /**
     * Содержит набор правил для заполнения страницы
     */
    abstract class FillPageProcessor {
        abstract void fillPage(PageVisitor pageVisitor)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class BasePageProcessor extends FillPageProcessor {
        def pageParts = [new BaseFiller(), new DetailsFiller()]

        @Override
        void fillPage(PageVisitor pageVisitor) {
            for (FDFiller pagePart : pageParts) {
                pagePart.accept(pageVisitor)
            }
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class DetailPageProcessor extends FillPageProcessor {
        def pageParts = [new DetailsFiller()]

        @Override
        void fillPage(PageVisitor pageVisitor) {
            for (FDFiller pagePart : pageParts) {
                pagePart.accept(pageVisitor)
            }
        }
    }

}



