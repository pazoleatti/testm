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
                        createPdf(tmpXmlFile, referenceNum, createParams.signatory)
                        logger.info("Успешно выполнено создание отчетной формы: \"№: $declarationData.id, Вид: $declarationTemplate.name, " +
                                "для ФЛ: $person.fullName, Период: $reportPeriod.taxPeriod.year $reportPeriod.name, " +
                                "Подразделение: ${department.shortName ?: department.name}, КПП: $declarationData.kpp, ОКТМО: $declarationData.oktmo")
                    }
                }
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

    void createPdf(File tmpXmlFile, int referenceNum, String signatory) {

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
}



