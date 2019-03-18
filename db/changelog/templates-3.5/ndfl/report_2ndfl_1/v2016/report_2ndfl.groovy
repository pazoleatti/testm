package form_template.ndfl.report_2ndfl_1.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.*
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
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeductionTypeService
import com.aplana.sbrf.taxaccounting.utils.ZipUtils
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.*

import java.nio.charset.Charset

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionMark.INVESTMENT_CODE
import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionMark.OTHERS_CODE
import static java.util.Collections.singletonList
import static java.util.TimeZone.getTimeZone

new Report2Ndfl(this).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class Report2Ndfl extends AbstractScriptClass {

    Date currDate = new Date()
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
    RefBookDeductionTypeService refBookDeductionTypeService
    String applicationVersion
    Map<String, Object> paramMap

    @TypeChecked(TypeCheckingMode.SKIP)
    Report2Ndfl(scriptClass) {
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
        this.refBookDeductionTypeService = (RefBookDeductionTypeService) getSafeProperty("refBookDeductionTypeService")

        this.scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) getSafeProperty("scriptSpecificReportHolder")
        this.applicationVersion = (String) getSafeProperty("applicationVersion")
        this.paramMap = (Map<String, Object>) getSafeProperty("paramMap")
        this.reportFormsCreationParams = (ReportFormsCreationParams) getSafeProperty("reportFormsCreationParams")
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.PREPARE_SPECIFIC_REPORT:
                // Подготовка для последующего формирования спецотчета
                prepareSpecificReport()
                break
            case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
                createSpecificReport()
                break
            case FormDataEvent.CREATE_FORMS: // создание экземпляра
                createReportForms()
                break
            case FormDataEvent.PRE_CREATE_REPORTS:
                preCreateReports()
                break
        }
    }

    /************************************* СОЗДАНИЕ XML *****************************************************************/

    final Charset xmlCharset = Charset.forName("windows-1251")

    // Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]

    final String DATE_FORMAT_FLATTEN = "yyyyMMdd"
    final String DATE_FORMAT_DOTTED = "dd.MM.yyyy"
    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"
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

    /**
     * Создаёт xml-файл по настройке подразделений.
     * @return объект с xml-файлом и дополнительной информацией
     */
    Xml buildXml(DepartmentConfig departmentConfig, List<Operation> operations) {
        Xml xml = null
        File xmlFile = null
        Writer fileWriter = null
        try {
            xmlFile = File.createTempFile("file_for_validate", ".xml")
            fileWriter = new OutputStreamWriter(new FileOutputStream(xmlFile), xmlCharset)
            xml = buildXml(departmentConfig, operations, fileWriter)
            if (xml) {
                xml.xmlFile = xmlFile
            }
            return xml
        } catch (ServiceException e) {
            if (e.getMessage()) {
                logger.warn(e.getMessage())
            }
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

    /**
     * Формирует xml-файл по данным КНФ, относящимся к заданной настройке подразделения. В файле может содержаться не более 3000 справок ФЛ.
     * @return объект с xml-файлом и дополнительной информацией, если формирование прошло успешно, иначе null
     * @throw ServiceException
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    Xml buildXml(DepartmentConfig departmentConfig, List<Operation> operations, Writer writer) {
        ScriptUtils.checkInterrupted()
        Xml xml = new Xml()

        def persons = operations*.person.toSet()
        if (!persons) {
            if (declarationData.declarationTemplateId == DeclarationType.NDFL_2_2) {
                logger.error("Отчетность $declarationTemplate.name для $department.name за период ${formatPeriod(departmentReportPeriod)} не сформирована. " +
                        "В РНУ НДФЛ (консолидированная) № ${sourceKnf.id} для подразделения: $department.name за период ${formatPeriod(departmentReportPeriod)} " +
                        "отсутствуют операции, содержащие сведения о не удержанном налоге.")
            } else {
                logger.error("Отчетность $declarationTemplate.name для $department.name за период ${formatPeriod(departmentReportPeriod)} не сформирована. " +
                        "В РНУ НДФЛ (консолидированная) № ${sourceKnf.id} для подразделения: $department.name за период ${formatPeriod(departmentReportPeriod)} " +
                        "отсутствуют операции.")
            }
            return null
        }
        checkMandatoryFields(persons)
        def operationsByPersonId = operations.groupBy { it.person.id }

        def refPersonIds = []
        ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
        def sberbankInnParam = configurationParamModel?.get(ConfigurationParam.SBERBANK_INN)?.get(0)?.get(0)
        def kodNoProm = configurationParamModel?.get(ConfigurationParam.NO_CODE)?.get(0)?.get(0)
        xml.fileName = generateXmlFileId(departmentConfig, sberbankInnParam, kodNoProm)

        Map<String, RefBookDeductionType> deductionTypesByCode = refBookDeductionTypeService.findAllByVersion(reportPeriod.endDate)
                .collectEntries { [it.code, it] }
        String priznak = definePriznak(departmentConfig)
        int nomSprCounter = 0

        MarkupBuilder builder = new MarkupBuilder(writer)
        builder.setDoubleQuotes(true)
        builder.setOmitNullAttributes(true)
        builder.mkp.xmlDeclaration(version: "1.0", encoding: xmlCharset.name())
        builder.Файл(ИдФайл: xml.fileName,
                ВерсПрог: applicationVersion,
                ВерсФорм: "5.06") {
            Документ(КНД: "1151078",
                    ДатаДок: currDate.format(DATE_FORMAT_DOTTED, getTimeZone('Europe/Moscow')),
                    ОтчетГод: reportPeriod.taxPeriod.year,
                    Признак: priznak,
                    КодНО: departmentConfig.taxOrganCode) {
                СвНА(ОКТМО: departmentConfig.oktmo.code,
                        Тлф: departmentConfig.phone) {
                    СвНАЮЛ(НаимОрг: departmentConfig.name,
                            ИННЮЛ: sberbankInnParam,
                            КПП: declarationData.kpp) {
                        if (departmentConfig.reorganization) {
                            СвРеоргЮЛ(ФормРеорг: departmentConfig.reorganization.code,
                                    ИННЮЛ: departmentConfig.reorgInn,
                                    КПП: departmentConfig.reorgKpp)
                        }
                    }
                }
                Подписант(ПрПодп: departmentConfig.signatoryMark.code) {
                    ФИО(Фамилия: departmentConfig.signatorySurName,
                            Имя: departmentConfig.signatoryFirstName,
                            Отчество: departmentConfig.signatoryLastName) {}
                    if (departmentConfig.signatoryMark.code == "2") {
                        СвПред(НаимДок: departmentConfig.approveDocName,
                                НаимОрг: departmentConfig.approveOrgName) {}
                    }
                }
                for (NdflPerson person : persons) {
                    ScriptUtils.checkInterrupted()
                    person.incomes = (List<NdflPersonIncome>) operationsByPersonId[person.id]*.incomes.flatten()
                    person.deductions = (List<NdflPersonDeduction>) operationsByPersonId[person.id]*.deductions.flatten()
                    person.prepayments = (List<NdflPersonPrepayment>) operationsByPersonId[person.id]*.prepayments.flatten()
                    "НДФЛ-2"(НомСпр: ++nomSprCounter,
                            НомКорр: sprintf('%02d', declarationData.correctionNum ?: 0)) {
                        ПолучДох(ИННФЛ: person.innNp,
                                Статус: person.status,
                                ДатаРожд: ScriptUtils.formatDate(person.birthDay),
                                Гражд: person.citizenship) {
                            ФИО(Фамилия: person.lastName,
                                    Имя: person.firstName,
                                    Отчество: person.middleName)
                            УдЛичнФЛ(КодУдЛичн: person.idDocType,
                                    СерНомДок: formatDocNumber(person.idDocType, person.idDocNumber))
                        }
                        def incomesByRate = person.incomes.groupBy { it.taxRate }.sort { it.key }
                        incomesByRate.remove(null)
                        for (def rate : incomesByRate.keySet()) {
                            СведДох(Ставка: rate) {
                                def rateIncomes = incomesByRate.get(rate)
                                def operationIds = rateIncomes.operationId.toSet()
                                def rateDeductions = сторнированиеВычетов(
                                        person.deductions.findAll { it.operationId in operationIds }
                                )
                                def ratePrepayments = person.prepayments.findAll { it.operationId in operationIds }
                                СумИтНалПер(
                                        СумДохОбщ: ScriptUtils.round(СумДох(rateIncomes, rateDeductions), 2),
                                        НалБаза: ScriptUtils.round(НалБаза(rateIncomes, rateDeductions), 2),
                                        НалИсчисл: НалИсчисл(rateIncomes, ratePrepayments),
                                        АвансПлатФикс: АвансПлатФикс(ratePrepayments),
                                        НалУдерж: is2Ndfl1() ? НалУдерж(rateIncomes) : 0,
                                        НалПеречисл: is2Ndfl1() ? НалПеречисл(person.incomes) : 0,// тут берутся строки перечисления, у которых rate=null
                                        НалУдержЛиш: is2Ndfl1() ? НалУдержЛиш(rateIncomes) : 0,
                                        НалНеУдерж: is2Ndfl1() ? НалНеУдерж(rateIncomes) : СуммаНИ(rateIncomes))
                                НалВычССИ() {
                                    def deductions = rateDeductions.findAll {
                                        it.typeCode && !(deductionTypesByCode[it.typeCode]?.mark?.code in [INVESTMENT_CODE, OTHERS_CODE])
                                    }.sort { a, b ->
                                        a.typeCode <=> b.typeCode ?: a.notifDate <=> b.notifDate ?: a.notifType <=> b.notifType ?:
                                                a.notifNum <=> b.notifNum ?: a.notifSource <=> b.notifSource
                                    }
                                    def deductionsByCode = deductions.groupBy { it.typeCode }
                                    deductionsByCode.each { deductionCode, deductionsOfCode ->
                                        ПредВычССИ(КодВычет: deductionCode,
                                                СумВычет: ScriptUtils.round(СумВыч(deductionsOfCode), 2))
                                    }
                                    def СписокУведомленийОВычетах = deductions.collect([] as Set) {
                                        new УведВычKey(it.notifDate, it.notifType, it.notifNum, it.notifSource)
                                    }.sort { a, b ->
                                        a.notifDate <=> b.notifDate ?: a.notifType <=> b.notifType ?:
                                                a.notifNum <=> b.notifNum ?: a.notifSource <=> b.notifSource
                                    }
                                    for (def row : СписокУведомленийОВычетах) {
                                        УведВыч(КодВидУвед: row.notifType,
                                                НомерУвед: row.notifNum,
                                                ДатаУвед: formatDate(row.notifDate),
                                                ИФНСУвед: row.notifSource)
                                    }
                                }
                                ДохВыч() {
                                    def incomesByMonthAndIncomeCode = rateIncomes.findAll {
                                        isBelongToPeriod(it.incomeAccruedDate) && it.incomeCode
                                    }.groupBy {
                                        new MonthAndIncomeCodeKey(it.incomeAccruedDate[Calendar.MONTH], it.incomeCode)
                                    }.sort { a, b ->
                                        a.key.month <=> b.key.month ?: a.key.incomeCode <=> b.key.incomeCode
                                    }
                                    for (def monthAndIncomeCodeKey : incomesByMonthAndIncomeCode.keySet()) {
                                        def monthAndIncomeCodeIncomes = incomesByMonthAndIncomeCode.get(monthAndIncomeCodeKey)
                                        def monthAndIncomeCodeDeductions = rateDeductions.findAll {
                                            it.incomeCode == monthAndIncomeCodeKey.incomeCode &&
                                                    it.periodCurrDate && it.periodCurrDate[Calendar.MONTH] == monthAndIncomeCodeKey.month &&
                                                    deductionTypesByCode[it.typeCode]?.mark?.code in [INVESTMENT_CODE, OTHERS_CODE]
                                        }
                                        СвСумДох(Месяц: sprintf('%02d', monthAndIncomeCodeKey.month + 1),
                                                КодДоход: monthAndIncomeCodeKey.incomeCode,
                                                СумДоход: ScriptUtils.round(СумДох(monthAndIncomeCodeIncomes, monthAndIncomeCodeDeductions), 2)) {
                                            def deductionsByCode = monthAndIncomeCodeDeductions.groupBy {
                                                it.typeCode
                                            }
                                            deductionsByCode.each { deductionCode, deductionsOfCode ->
                                                СвСумВыч(КодВычет: deductionCode,
                                                        СумВычет: ScriptUtils.round(СумВыч(deductionsOfCode), 2))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!refPersonIds.contains(person.personId)) {
                        refPersonIds << person.personId
                        xml.ndflReferences << buildNdflReference(person.id, person.personId, nomSprCounter, person.lastName, person.firstName, person.middleName, person.birthDay)
                    }
                }
            }
        }
        return xml
    }

    List<NdflPerson> getPersons() {
        def persons = ndflPersonService.findAllFor2Ndfl(sourceKnf.id, declarationData.kpp, declarationData.oktmo, reportPeriod.startDate, reportPeriod.endDate)
        for (def personIterator = persons.iterator(); personIterator.hasNext();) {
            def person = personIterator.next()
            BigDecimal sum = 0
            for (def income : person.incomes) {
                if (is2Ndfl1()) {
                    sum += income.incomeAccruedSumm ?: 0
                } else {
                    sum += (income.notHoldingTax > 0 ? income.notHoldingTax : 0) - (income.overholdingTax > 0 ? income.overholdingTax : 0)
                }
            }
            if (sum <= 0) {
                personIterator.remove()
            }
        }
        return persons
    }

    /**
     * Возвращяет операции, в которых хотя бы одна строка принадлежит отчетному периоду,
     * у тех ФЛ, у которых сумма дохода (для 2-НДЛФЛ(1)) или сумма неудержанного (для 2-НДЛФЛ(2)) больше 0
     * @return
     */
    List<Operation> findOperations() {
        Map<Long, NdflPerson> personsById = ndflPersonService.findNdflPerson(sourceKnf.id)
                .collectEntries { [it.id, it] }
        Map<Long, Map<String, List<NdflPersonIncome>>> incomesByPersonIdAndOperationId = ndflPersonService.findNdflPersonIncome(sourceKnf.id)
                .groupBy({ NdflPersonIncome it -> it.ndflPersonId }, { NdflPersonIncome it -> it.operationId })
        Map<Long, Map<String, List<NdflPersonDeduction>>> deductionsByPersonIdAndOperationId = ndflPersonService.findNdflPersonDeduction(sourceKnf.id)
                .groupBy({ NdflPersonDeduction it -> it.ndflPersonId }, { NdflPersonDeduction it -> it.operationId })
        Map<Long, Map<String, List<NdflPersonPrepayment>>> prepaymentsByPersonIdAndOperationId = ndflPersonService.findNdflPersonPrepayment(sourceKnf.id)
                .groupBy({ NdflPersonPrepayment it -> it.ndflPersonId }, { NdflPersonPrepayment it -> it.operationId })

        List<Operation> operations = [] as LinkedList
        for (def personId : incomesByPersonIdAndOperationId.keySet()) {
            def personIncomesByOperationId = incomesByPersonIdAndOperationId[personId]
            def personIncomes = (List<NdflPersonIncome>) personIncomesByOperationId.values().flatten()
            if (hasPositiveSum(personIncomes)) {
                for (def operationId : personIncomesByOperationId.keySet()) {
                    def operation = new Operation()
                    operation.operationId = operationId
                    operation.person = personsById[personId]
                    operation.incomes = incomesByPersonIdAndOperationId[personId][operationId]
                    operation.deductions = deductionsByPersonIdAndOperationId.get(personId)?.get(operationId) ?: new ArrayList<NdflPersonDeduction>()
                    operation.prepayments = prepaymentsByPersonIdAndOperationId.get(personId)?.get(operationId) ?: new ArrayList<NdflPersonPrepayment>()
                    if (isOperationBelongToPeriod(operation)) {
                        operations.add(operation)
                    }
                }
            }
        }

        return operations
    }

    /**
     * Возвращяет признак наличия суммыФЛ (сумма дохода (для 2-НДЛФЛ(1)) или сумма неудержанного (для 2-НДЛФЛ(2)) больше 0)
     * @param incomes строки 2 раздела ФЛ
     */
    boolean hasPositiveSum(List<NdflPersonIncome> incomes) {
        BigDecimal sum = 0
        for (def income : incomes) {
            if (isBelongToPeriod(income.incomeAccruedDate)) {
                if (is2Ndfl1()) {
                    sum += income.incomeAccruedSumm ?: 0
                } else {
                    sum += (income.notHoldingTax > 0 ? income.notHoldingTax : 0) - (income.overholdingTax > 0 ? income.overholdingTax : 0)
                }
            }
        }
        return sum > 0
    }

    /**
     * Возвращяет признак принадлежности операции отчетному периоду (если хотя бы у 1 строки дата начисления принадлежит отчетному периоду)
     */
    boolean isOperationBelongToPeriod(Operation operation) {
        for (def income : operation.incomes) {
            if (isBelongToPeriod(income.incomeAccruedDate)) {
                return true
            }
        }
        return false
    }

    /**
     * Проверяет заполнены ли обязательные поля у физическмх лиц
     * @param ndflPersonList список ФЛ
     * @throws ServiceException
     */
    void checkMandatoryFields(Collection<NdflPerson> ndflPersonList) {
        for (NdflPerson ndflPerson : ndflPersonList) {
            List<String> mandatoryFields = new LinkedList<>()
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
                String fio = "${ndflPerson.lastName ?: ""}${ndflPerson.firstName ? " " + ndflPerson.firstName : ""}${ndflPerson.middleName ? " " + ndflPerson.middleName : ""}"
                throw new ServiceException("Не удалось создать форму \"$declarationTemplate.name\" за период \"${formatPeriod(departmentReportPeriod)}\", подразделение: \"$department.name\", " +
                        "КПП: \"$declarationData.kpp\", ОКТМО: \"$declarationData.oktmo\". Не заполнены или равны \"0\" обязательные параметры ${mandatoryFields.join(', ')} " +
                        "для ФЛ: $fio, ИНП: $ndflPerson.inp в форме РНУ НДФЛ (консолидированная) № $sourceKnf.id")
            }
        }
    }

    /**
     * Удаляет группы строк вычетов, в которых сумма вычетов занулена
     */
    List<NdflPersonDeduction> сторнированиеВычетов(List<NdflPersonDeduction> deductions) {
        def deductionGroups = deductions.groupBy {
            [it.typeCode, it.notifDate, it.notifNum, it.notifSource, it.incomeCode]
        }
        for (def iterator = deductionGroups.values().iterator(); iterator.hasNext();) {
            def deductionGroup = iterator.next()
            BigDecimal sum = 0
            for (def deduction : deductionGroup) {
                sum += deduction.periodCurrSumm ?: 0
            }
            if (sum == 0) {
                iterator.remove()
            }
        }
        return (List<NdflPersonDeduction>) deductionGroups.values().flatten()
    }

    String formatDocNumber(String idDocType, String idDocNumber) {
        if (idDocType && idDocNumber) {
            idDocNumber = idDocNumber.replaceAll("[^А-Яа-я\\w]", "").toUpperCase()
            if (ScriptUtils.checkDulSymbols(idDocType, idDocNumber)) {
                return ScriptUtils.formatDocNumber(idDocType, idDocNumber)
            }
        }
        return idDocNumber
    }

    BigDecimal СумДох(List<NdflPersonIncome> incomes, List<NdflPersonDeduction> deductions) {
        BigDecimal sum = 0
        int taxRate = incomes[0].taxRate
        if (is2Ndfl1()) {
            for (def income : incomes) {
                if (isBelongToPeriod(income.incomeAccruedDate)) {
                    sum += income.incomeAccruedSumm ?: 0
                }
            }
        } else {
            def НалНеУдерж = НалНеУдерж(incomes)
            if (НалНеУдерж) {
                if (НалУдерж(incomes) > 0) {
                    sum = НалНеУдерж * 100 / taxRate + СумВыч(deductions)
                } else {
                    for (def income : incomes) {
                        if (isBelongToPeriod(income.incomeAccruedDate)) {
                            sum += income.incomeAccruedSumm ?: 0
                        }
                    }
                }
            }
        }
        return sum
    }

    BigDecimal СумВыч(List<NdflPersonDeduction> deductions) {
        BigDecimal sum = 0
        for (def deduction : deductions) {
            sum += deduction.periodCurrSumm ?: 0
        }
        return sum
    }

    BigDecimal НалБаза(List<NdflPersonIncome> incomes, List<NdflPersonDeduction> deductions) {
        BigDecimal sum = 0
        if (is2Ndfl1()) {
            for (def income : incomes) {
                if (isBelongToPeriod(income.incomeAccruedDate)) {
                    sum += income.taxBase ?: 0
                }
            }
            if (sum < 0) {
                sum = 0
            }
        } else {
            sum = СумДох(incomes, deductions) - СумВыч(deductions)
        }
        return sum
    }

    BigDecimal АвансПлатФикс(List<NdflPersonPrepayment> prepayments) {
        BigDecimal sum = 0
        for (def prepayment : prepayments) {
            sum += prepayment.summ ?: 0
        }
        return sum
    }

    BigDecimal НалИсчисл(List<NdflPersonIncome> incomes, List<NdflPersonPrepayment> prepayments) {
        BigDecimal sum = 0
        if (is2Ndfl1()) {
            for (def income : incomes) {
                if (isBelongToPeriod(income.incomeAccruedDate)) {
                    sum += income.calculatedTax ?: 0
                }
            }
            sum += АвансПлатФикс(prepayments)
        } else {
            sum = СуммаНИ(incomes)
        }
        return sum
    }

    BigDecimal НалУдерж(List<NdflPersonIncome> incomes) {
        BigDecimal sum = 0
        for (def income : incomes) {
            if (income.incomePayoutDate) {
                sum += income.withholdingTax ?: 0
            }
        }
        return sum
    }

    Long НалПеречисл(List<NdflPersonIncome> incomes) {
        Long sum = 0
        for (def income : incomes) {
            sum += income.taxSumm ?: 0
        }
        return sum
    }

    BigDecimal НалУдержЛиш(List<NdflPersonIncome> incomes) {
        BigDecimal sum = НалогРасчет(incomes)
        if (sum < 0) {
            return -sum
        }
        return 0
    }

    BigDecimal НалНеУдерж(List<NdflPersonIncome> incomes) {
        BigDecimal sum = НалогРасчет(incomes)
        if (sum > 0) {
            return sum
        }
        return 0
    }

    BigDecimal СуммаНИ(List<NdflPersonIncome> incomes) {
        return НалНеУдерж(incomes)
    }

    BigDecimal НалогРасчет(List<NdflPersonIncome> incomes) {
        BigDecimal sum = 0
        for (def income : incomes) {
            if (income.taxDate) {
                sum += (income.notHoldingTax ?: 0) - (income.overholdingTax ?: 0)
            }
        }
        return sum
    }

    // Сохранение информации о файле в комментариях
    def saveFileInfo(File xmlFile, String fileName) {
        ScriptUtils.checkInterrupted()
        String fileUuid = blobDataService.create(xmlFile, fileName + ".xml", new Date())
        TAUser createUser = declarationService.getSystemUserInfo().getUser()

        RefBookDataProvider fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
        Long fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.OUTGOING_TO_FNS.code}").get(0)

        DeclarationDataFile declarationDataFile = new DeclarationDataFile()
        declarationDataFile.setDeclarationDataId(declarationData.id)
        declarationDataFile.setUuid(fileUuid)
        declarationDataFile.setUserName(createUser.getName())
        declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(createUser.getDepartmentId()))
        declarationDataFile.setFileTypeId(fileTypeId)
        declarationDataFile.setDate(currDate)
        declarationService.saveFile(declarationDataFile)
    }

    def saveNdflRefences(List<RefBookRecord> ndflReferences, long declarationDataId) {
        ScriptUtils.checkInterrupted()
        if (ndflReferences) {
            logger.setTaUserInfo(userInfo)
            for (def ndflReference : ndflReferences) {
                ndflReference.values.put(NDFL_REFERENCES_DECLARATION_DATA_ID, new RefBookValue(RefBookAttributeType.NUMBER, declarationDataId))
            }
            getProvider(RefBook.Id.NDFL_REFERENCES.id).createRecordVersionWithoutLock(logger, new Date(), null, ndflReferences)
        }
    }

    /**
     * Заполнить значение реестра справок
     */
    RefBookRecord buildNdflReference(Long ndflPersonId, Long personId, Long nomSpr, String lastName, String firstName, String middleName, Date birthDay) {
        Map<String, RefBookValue> row = new HashMap<String, RefBookValue>()
        row.put(NDFL_PERSON_ID, new RefBookValue(RefBookAttributeType.NUMBER, ndflPersonId))
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
     */
    String generateXmlFileId(DepartmentConfig departmentConfig, String inn, String kodNoProm) {
        def r_t = "NO_NDFL2"
        def a = kodNoProm
        String k = departmentConfig.taxOrganCode
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

    /**
     * Получение провайдера с использованием кеширования
     */
    RefBookDataProvider getProvider(Long refBookId) {
        if (!providerCache.containsKey(refBookId)) {
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId)
            providerCache.put(refBookId, provider)
        }
        return providerCache.get(refBookId)
    }

    /**
     * Определить признак
     */
    String definePriznak(DepartmentConfig departmentConfig) {
        if (declarationTemplate.type.id == DeclarationType.NDFL_2_1) {
            return !departmentConfig.reorganization ? "1" : "3"
        } else if (declarationTemplate.type.id == DeclarationType.NDFL_2_2) {
            return !departmentConfig.reorganization ? "2" : "4"
        }
        throw new IllegalArgumentException()
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
        Map<KppOktmoPair, List<Operation>> operationsByKppOktmoPair = findOperations().groupBy { new KppOktmoPair(it.kpp, it.oktmo) }
        List<DeclarationData> createdForms = []
        for (def departmentConfig : departmentConfigs) {
            ScriptUtils.checkInterrupted()
            List<DeclarationData> existingDeclarations = declarationService.findAllByTypeIdAndReportPeriodIdAndKppAndOktmo(
                    declarationTemplate.type.id, departmentReportPeriod.reportPeriod.id, departmentConfig.kpp, departmentConfig.oktmo.code)
            declarationData = new DeclarationData()
            declarationData.declarationTemplateId = declarationTemplate.id
            declarationData.kpp = departmentConfig.kpp
            declarationData.oktmo = departmentConfig.oktmo.code
            declarationData.taxOrganCode = departmentConfig.taxOrganCode
            declarationData.adjustNegativeValues = reportFormsCreationParams.adjustNegativeValues
            declarationData.taxRefundReflectionMode = reportFormsCreationParams.taxRefundReflectionMode
            declarationData.docStateId = RefBookDocState.NOT_SENT.id
            declarationData.departmentReportPeriodId = departmentReportPeriod.id
            declarationData.reportPeriodId = departmentReportPeriod.reportPeriod.id
            declarationData.departmentId = departmentReportPeriod.departmentId
            declarationData.state = State.CREATED

            File zipFile = null
            Xml xml = null
            try {
                def kppOktmoOperations = operationsByKppOktmoPair[new KppOktmoPair(departmentConfig.kpp, departmentConfig.oktmo.code)]
                xml = buildXml(departmentConfig, kppOktmoOperations)
                if (xml) {
                    def formsToDelete = existingDeclarations
                    if (deleteForms(formsToDelete)) {
                        createdForms.add(declarationData)
                        declarationData.fileName = xml.fileName
                        create(declarationData)
                        saveNdflRefences(xml.ndflReferences, declarationData.id)
                        // Привязывание xml-файла к форме
                        saveFileInfo(xml.xmlFile, xml.fileName)
                        zipFile = ZipUtils.archive(xml.xmlFile, xml.fileName + ".xml")
                        String uuid = blobDataService.create(zipFile, xml.fileName + ".zip", currDate)
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
        ScriptUtils.checkInterrupted()
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

    boolean deleteForms(List<DeclarationData> formsToDelete) {
        ScriptUtils.checkInterrupted()
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
            LOG.warn(String.format("Временный файл %s не удален", tempFile.getAbsolutePath()))
        }
    }

    /**
     * Структура для хранения информации по xml-файлу с данными 2-НДФЛ
     */
    class Xml {
        // Наименование файла
        String fileName
        // Сам файл
        File xmlFile
        // Реестр справок
        List<RefBookRecord> ndflReferences = []
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

    /************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/
    boolean is2Ndfl1() {
        return declarationData.declarationTemplateId == DeclarationType.NDFL_2_1
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

    /**
     * Принадлежит отчетному периоду
     */
    boolean isBelongToPeriod(Date date) {
        return date != null && (reportPeriod.startDate <= date && date <= reportPeriod.endDate)
    }

    /************************************* СПЕЦОТЧЕТ **********************************************************************/

    final String ALIAS_PRIMARY_RNU_W_ERRORS = "primary_rnu_w_errors"

    final String TRANSPORT_FILE_TEMPLATE = "ТФ"

    // Мапа где ключ идентификатор NdflPerson, значение NdflPerson соответствующий идентификатору
    Map<Long, NdflPerson> ndflpersonFromRNUPrimary = [:]

    //------------------ PREPARE_SPECIFIC_REPORT ----------------------

    def prepareSpecificReport() {
        PrepareSpecificReportResult result = new PrepareSpecificReportResult()
        List<Column> tableColumns = createTableColumns()
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>()
        List<Column> rowColumns = createRowColumns()

        //Проверка, подготовка данных
        def params = scriptSpecificReportHolder.subreportParamValues
        Map<String, Object> reportParameters = scriptSpecificReportHolder.getSubreportParamValues()

        if (reportParameters.isEmpty()) {
            throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.")
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

        dataRows.addAll(addRows(docs, rowColumns))

        result.setTableColumns(tableColumns)
        result.setDataRows(dataRows)
        scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
        scriptSpecificReportHolder.setSubreportParamValues(params)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    List<DataRow<Cell>> addRows(List docs, List<Column> rowColumns) {
        // Формирование списка данных для вывода в таблицу
        List<DataRow<Cell>> toReturn = []
        docs.each() { doc ->
            DataRow<Cell> row = new DataRow<Cell>(ScriptUtils.createCells(rowColumns, null))
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
        PagingResult<Map<String, RefBookValue>> record = provider.getRecords(reportPeriod.endDate, null, "CODE = '$statusCode'".toString(), null)
        return "(" + record.get(0).get("CODE").getValue() + ") " + record.get(0).get("NAME").getValue()
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

        return tableColumns
    }

    List<Column> createRowColumns() {
        List<Column> tableColumns = new ArrayList<Column>()

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

        return tableColumns
    }

    /**
     * Поиск справок согласно фильтру
     */
    int counter = 0

    @TypeChecked(TypeCheckingMode.SKIP)
    List searchData(def params, pageSize, PrepareSpecificReportResult prepareSpecificReportResult) {
        def xmlStr = declarationService.getXmlData(declarationData.id, userInfo)
        def Файл = new XmlSlurper().parseText(xmlStr)
        def docs = []
        Файл.Документ."НДФЛ-2".each { ndfl2Node ->
            boolean passed = true
            String idDoc = null
            if (params[SubreportAliasConstants.ID_DOC_NUMBER] != null) {
                idDoc = params[SubreportAliasConstants.ID_DOC_NUMBER].replaceAll("[\\s-]", "")
            }
            if (params[SubreportAliasConstants.P_NUM_SPRAVKA] != null && !StringUtils.containsIgnoreCase(ndfl2Node.@НомСпр.text(), params[SubreportAliasConstants.P_NUM_SPRAVKA])) passed = false
            if (params[SubreportAliasConstants.LAST_NAME] != null && !StringUtils.containsIgnoreCase(ndfl2Node.ПолучДох.ФИО.@Фамилия.text(), params[SubreportAliasConstants.LAST_NAME])) passed = false
            if (params[SubreportAliasConstants.FIRST_NAME] != null && !StringUtils.containsIgnoreCase(ndfl2Node.ПолучДох.ФИО.@Имя.text(), params[SubreportAliasConstants.FIRST_NAME])) passed = false
            if (params[SubreportAliasConstants.MIDDLE_NAME] != null && !StringUtils.containsIgnoreCase(ndfl2Node.ПолучДох.ФИО.@Отчество.text(), params[SubreportAliasConstants.MIDDLE_NAME])) passed = false
            if (params[SubreportAliasConstants.INN] != null && !StringUtils.containsIgnoreCase(ndfl2Node.ПолучДох.@ИННФЛ.text(), params[SubreportAliasConstants.INN])) passed = false
            if ((params[SubreportAliasConstants.FROM_BIRTHDAY] != null || params[SubreportAliasConstants.TO_BIRTHDAY] != null) && searchBirthDay(params, ndfl2Node.ПолучДох.@ДатаРожд.text())) passed = false
            if (params[SubreportAliasConstants.ID_DOC_NUMBER] != null && !((StringUtils.containsIgnoreCase(ndfl2Node.ПолучДох.УдЛичнФЛ.@СерНомДок.text(), idDoc) ||
                    StringUtils.containsIgnoreCase(ndfl2Node.ПолучДох.УдЛичнФЛ.@СерНомДок.text().replaceAll("[\\s-]", ""), idDoc)))) passed = false
            if (passed) docs << ndfl2Node
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
        DataRow<Cell> row = scriptSpecificReportHolder.getSelectedRecord()
        def params = scriptSpecificReportHolder.subreportParamValues ?: new HashMap<String, Object>()
        params.put(SubreportAliasConstants.P_NUM_SPRAVKA, row.pNumSpravka)

        Map<String, String> subReportViewParams = scriptSpecificReportHolder.getViewParamValues()
        subReportViewParams.put('Номер справки', row.pNumSpravka.toString())
        subReportViewParams.put('Фамилия', row.lastName.toString())
        subReportViewParams.put('Имя', row.firstName.toString())
        subReportViewParams.put('Отчество', row.middleName.toString())
        subReportViewParams.put('Дата рождения', row.birthDay ? row.birthDay.toString() : "")
        subReportViewParams.put('№ ДУЛ', row.idDocNumber.toString())

        def xmlStr = declarationService.getXmlData(declarationData.id, userInfo)
        Node Файл = new XmlParser().parseText(xmlStr)

        PDFont font = PDType0Font.load(PDDocument.newInstance(), scriptSpecificReportHolder.getSubreportParam("font"))

        String xmlName = declarationService.getXmlDataFileName(declarationData.id, userInfo)

        String fileName = xmlName.substring(0, xmlName.length() - 3).concat("pdf")

        Node reference = Файл.Документ.'НДФЛ-2'.find { doc ->
            String a = doc.@НомСпр
            String b = row.pNumSpravka.toString()
            return a == b
        }

        List<DocumentWrapper> allPages = createAndOrderReportPages(Файл, row)

        allPages.eachWithIndex { DocumentWrapper page, int index ->
            PDDocumentCatalog docCatalog = page.document.getDocumentCatalog()
            PDAcroForm acroForm = docCatalog.getAcroForm()

            PDResources resources = acroForm.getDefaultResources()
            resources.put(COSName.getPDFName("CourierNew"), font)
            acroForm.setDefaultAppearance("/CourierNew 16 Tf 0 g")

            if (page instanceof TitlePage) {
                new TitlePageProcessor().fillPage(new PageVisitor(acroForm, Файл, reference, ++index, page.СведДох, page.ПредВычССИList, page.УведВыч, page.СведСумДохDataList))
            } else if (page instanceof IncomePage) {
                new IncomesPageProcessor().fillPage(new PageVisitor(acroForm, Файл, reference, ++index, page.СведДох, page.ПредВычССИList, page.УведВыч, page.СведСумДохDataList))
            } else if (page instanceof DeductionPage) {
                new DeductionsPageProcessor().fillPage(new PageVisitor(acroForm, Файл, reference, ++index, page.СведДох, page.ПредВычССИList, page.УведВыч, page.СведСумДохDataList))
            } else if (page instanceof ApplicationPage) {
                new ApplicationPageProcessor().fillPage(new PageVisitor(acroForm, Файл, reference, ++index, page.СведДох, page.ПредВычССИList, page.УведВыч, page.СведСумДохDataList))
            }
            acroForm.flatten()
        }

        PDDocument destination = new PDDocument();

        PDFMergerUtility merger = new PDFMergerUtility();

        merger.setDestinationStream(scriptSpecificReportHolder.getFileOutputStream())

        for (DocumentWrapper document : allPages) {
            merger.appendDocument(destination, document.document)
        }

        OutputStream writer = scriptSpecificReportHolder.getFileOutputStream()
        destination.save(writer)
        destination.close()

        scriptSpecificReportHolder.setFileName(fileName)
    }

    /**
     * Обертка для PDF документа. Каждая страница представлена отдельным PDF файлом. Как страница заполняется зависит от
     * реализации этого класса.
     */
    abstract class DocumentWrapper {
        protected PDDocument document
        protected int incomeIndex
        protected Node СведДох
        protected List<Node> ПредВычССИList
        protected Node УведВыч
        protected List<СведСумДохData> СведСумДохDataList
    }

    class TitlePage extends DocumentWrapper {

        TitlePage(PDDocument document) {
            this.document = document
        }
    }

    class IncomePage extends DocumentWrapper {

        IncomePage(PDDocument document) {
            this.document = document
        }
    }

    class DeductionPage extends DocumentWrapper {

        DeductionPage(PDDocument document) {
            this.document = document
        }
    }

    class ApplicationPage extends DocumentWrapper {

        ApplicationPage(PDDocument document) {
            this.document = document
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    /**
     * Инкапсулирует поведение по заполнению различных разделов FDF формы
     */
    abstract class FDFiller {
        abstract void accept(PageVisitor pageVisitor)

        @TypeChecked(TypeCheckingMode.SKIP)
        String getOrgName(xmlRoot) {
            StringBuilder orgName = new StringBuilder("------------------------------------------------------------------------------------------------------------------------")
            String orgNameValue = xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@НаимОрг
            orgName = orgName.replace(0, orgNameValue.length(), orgNameValue)
            return orgName.toString()
        }

        void processField(PDField field, String value) {
            if (value == null) value = ""
            COSDictionary fieldDict = field.getCOSObject()
            int maxLength = fieldDict.getInt(COSName.MAX_LEN)

            StringBuilder printingValue = new StringBuilder(value)
            for (int i = 0; i < maxLength - value.length(); i++) {
                printingValue.append("-")
            }
            field.setValue(printingValue.toString())
        }

        void processPageNumField(PDField field, String value) {
            COSDictionary fieldDict = field.getCOSObject()
            int maxLength = fieldDict.getInt(COSName.MAX_LEN)

            StringBuilder printingValue = new StringBuilder(value)
            for (int i = 0; i < maxLength - value.length(); i++) {
                printingValue.insert(0, 0)
            }
            field.setValue(printingValue.toString())
        }

        void processNumIntField(PDField field, String value) {
            if (value == null) value = "0"
            COSDictionary fieldDict = field.getCOSObject()
            int maxLength = fieldDict.getInt(COSName.MAX_LEN)

            StringBuilder printingValue = new StringBuilder(value)
            for (int i = 0; i < maxLength - value.length(); i++) {
                printingValue.append("-")
            }
            field.setValue(printingValue.toString())
        }

        void processNumFractField(PDField field, String value) {
            if (value == null) value = "00"
            field.setValue(value)
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class HeaderFiller extends FDFiller {
        @Override
        void accept(PageVisitor pageVisitor) {
            processField(pageVisitor.acroForm.getField("inn"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@ИННЮЛ)
            processField(pageVisitor.acroForm.getField("kpp"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@КПП)
            processPageNumField(pageVisitor.acroForm.getField("pageNum"), String.valueOf(pageVisitor.pageNum))
            processField(pageVisitor.acroForm.getField("refNum"), pageVisitor.reference.@НомСпр)
            processField(pageVisitor.acroForm.getField("reportYear"), pageVisitor.xmlRoot.Документ[0].@ОтчетГод)
            processField(pageVisitor.acroForm.getField("mark"), pageVisitor.xmlRoot.Документ[0].@Признак)
            processField(pageVisitor.acroForm.getField("corrNum"), pageVisitor.reference.@НомКорр)
            processField(pageVisitor.acroForm.getField("taxAuthority"), pageVisitor.xmlRoot.Документ[0].@КодНО)
            processField(pageVisitor.acroForm.getField("signer"), pageVisitor.xmlRoot.Документ[0].Подписант[0].@ПрПодп)
            processField(pageVisitor.acroForm.getField("signerLastName"), pageVisitor.xmlRoot.Документ[0].Подписант[0].ФИО[0]?.@Фамилия)
            processField(pageVisitor.acroForm.getField("signerFirstName"), pageVisitor.xmlRoot.Документ[0].Подписант[0].ФИО[0]?.@Имя)
            processField(pageVisitor.acroForm.getField("signerMiddleName"), pageVisitor.xmlRoot.Документ[0].Подписант[0].ФИО[0]?.@Отчество)
            processField(pageVisitor.acroForm.getField("approveDoc"), pageVisitor.xmlRoot.Документ[0].Подписант[0].СвПред[0]?.@НаимДок)
            processField(pageVisitor.acroForm.getField("signDay"), pageVisitor.xmlRoot.Документ[0].@ДатаДок.substring(0, 2))
            processField(pageVisitor.acroForm.getField("signMonth"), pageVisitor.xmlRoot.Документ[0].@ДатаДок.substring(3, 5))
            processField(pageVisitor.acroForm.getField("signYear"), pageVisitor.xmlRoot.Документ[0].@ДатаДок.substring(6))
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class ApplicationHeaderFiller extends FDFiller {
        @Override
        void accept(PageVisitor pageVisitor) {
            processField(pageVisitor.acroForm.getField("inn"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@ИННЮЛ)
            processField(pageVisitor.acroForm.getField("kpp"), pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].@КПП)
            processPageNumField(pageVisitor.acroForm.getField("pageNum"), String.valueOf(pageVisitor.pageNum))
            processField(pageVisitor.acroForm.getField("refNum"), pageVisitor.reference.@НомСпр)
            processField(pageVisitor.acroForm.getField("reportYear"), pageVisitor.xmlRoot.Документ[0].@ОтчетГод)
            processField(pageVisitor.acroForm.getField("taxRate"), pageVisitor.СведСумДохDataList[0].taxRate)
            processField(pageVisitor.acroForm.getField("signDate"), pageVisitor.xmlRoot.Документ[0].@ДатаДок)
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class InfoFiller extends FDFiller {
        @Override
        void accept(PageVisitor pageVisitor) {
            String orgName = getOrgName(pageVisitor.xmlRoot)
            processField(pageVisitor.acroForm.getField("taxAgent1"), pageVisitor.pageNum == 1 ? orgName.substring(0, 40) : null)
            processField(pageVisitor.acroForm.getField("taxAgent2"), pageVisitor.pageNum == 1 ? orgName.substring(40, 80) : null)
            processField(pageVisitor.acroForm.getField("taxAgent3"), pageVisitor.pageNum == 1 ? orgName.substring(80, 120) : null)
            processField(pageVisitor.acroForm.getField("liquidation"), pageVisitor.pageNum == 1 ? pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].СвРеоргЮЛ[0]?.@ФормРеорг : null)
            processField(pageVisitor.acroForm.getField("liquidationInn"), pageVisitor.pageNum == 1 ? pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].СвРеоргЮЛ[0]?.@ИННЮЛ : null)
            processField(pageVisitor.acroForm.getField("liquidationKpp"), pageVisitor.pageNum == 1 ? pageVisitor.xmlRoot.Документ[0].СвНА[0].СвНАЮЛ[0].СвРеоргЮЛ[0]?.@КПП : null)
            processField(pageVisitor.acroForm.getField("oktmo"), pageVisitor.pageNum == 1 ? pageVisitor.xmlRoot.Документ[0].СвНА[0].@ОКТМО : null)
            processField(pageVisitor.acroForm.getField("phone"), pageVisitor.pageNum == 1 ? pageVisitor.xmlRoot.Документ[0].СвНА[0].@Тлф : null)

            processField(pageVisitor.acroForm.getField("innPerson"), pageVisitor.reference.ПолучДох[0].@ИННФЛ)
            processField(pageVisitor.acroForm.getField("lastName"), pageVisitor.reference.ПолучДох[0].ФИО[0].@Фамилия)
            processField(pageVisitor.acroForm.getField("firstName"), pageVisitor.reference.ПолучДох[0].ФИО[0].@Имя)
            processField(pageVisitor.acroForm.getField("middleName"), pageVisitor.reference.ПолучДох[0].ФИО[0].@Отчество)
            processField(pageVisitor.acroForm.getField("taxPayerState"), pageVisitor.reference.ПолучДох[0].@Статус)

            processField(pageVisitor.acroForm.getField("birthDay"), pageVisitor.reference.ПолучДох[0].@ДатаРожд.substring(0, 2))
            processField(pageVisitor.acroForm.getField("birthMonth"), pageVisitor.reference.ПолучДох[0].@ДатаРожд.substring(3, 5))
            processField(pageVisitor.acroForm.getField("birthYear"), pageVisitor.reference.ПолучДох[0].@ДатаРожд.substring(6))
            processField(pageVisitor.acroForm.getField("citizenship"), pageVisitor.reference.ПолучДох[0].@Гражд)
            processField(pageVisitor.acroForm.getField("docCode"), pageVisitor.reference.ПолучДох[0].УдЛичнФЛ[0].@КодУдЛичн)
            processField(pageVisitor.acroForm.getField("docNumber"), pageVisitor.reference.ПолучДох[0].УдЛичнФЛ[0].@СерНомДок)
        }
    }

    class EmptyInfoFiller extends FDFiller {

        @Override
        void accept(PageVisitor pageVisitor) {
            processField(pageVisitor.acroForm.getField("taxAgent1"), null)
            processField(pageVisitor.acroForm.getField("taxAgent2"), null)
            processField(pageVisitor.acroForm.getField("taxAgent3"), null)
            processField(pageVisitor.acroForm.getField("liquidation"), null)
            processField(pageVisitor.acroForm.getField("liquidationInn"), null)
            processField(pageVisitor.acroForm.getField("liquidationKpp"), null)
            processField(pageVisitor.acroForm.getField("oktmo"), null)
            processField(pageVisitor.acroForm.getField("phone"), null)

            processField(pageVisitor.acroForm.getField("innPerson"), null)
            processField(pageVisitor.acroForm.getField("lastName"), null)
            processField(pageVisitor.acroForm.getField("firstName"), null)
            processField(pageVisitor.acroForm.getField("middleName"), null)
            processField(pageVisitor.acroForm.getField("taxPayerState"), null)

            processField(pageVisitor.acroForm.getField("birthDay"), null)
            processField(pageVisitor.acroForm.getField("birthMonth"), null)
            processField(pageVisitor.acroForm.getField("birthYear"), null)
            processField(pageVisitor.acroForm.getField("citizenship"), null)
            processField(pageVisitor.acroForm.getField("docCode"), null)
            processField(pageVisitor.acroForm.getField("docNumber"), null)
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class IncomeFiller extends FDFiller {

        @Override
        void accept(PageVisitor pageVisitor) {
            processField(pageVisitor.acroForm.getField("taxRate"), pageVisitor.СведДох.@Ставка)
            processNumIntField(pageVisitor.acroForm.getField("taxSumIntPart"), pageVisitor.СведДох.СумИтНалПер[0].@СумДохОбщ.split("\\.")[0])
            processNumFractField(pageVisitor.acroForm.getField("taxSumFractPart"), pageVisitor.СведДох.СумИтНалПер[0].@СумДохОбщ.split("\\.")[1])
            processNumIntField(pageVisitor.acroForm.getField("fixPayments"), pageVisitor.СведДох.СумИтНалПер[0].@АвансПлатФикс)
            processNumIntField(pageVisitor.acroForm.getField("taxBaseIntPart"), pageVisitor.СведДох.СумИтНалПер[0].@НалБаза.split("\\.")[0])
            processNumFractField(pageVisitor.acroForm.getField("taxBaseFractPart"), pageVisitor.СведДох.СумИтНалПер[0].@НалБаза.split("\\.")[1])
            processNumIntField(pageVisitor.acroForm.getField("transferPayment"), pageVisitor.СведДох.СумИтНалПер[0].@НалПеречисл)
            processNumIntField(pageVisitor.acroForm.getField("calculatedSum"), pageVisitor.СведДох.СумИтНалПер[0].@НалИсчисл)
            processNumIntField(pageVisitor.acroForm.getField("overholdingTax"), pageVisitor.СведДох.СумИтНалПер[0].@НалУдержЛиш)
            processNumIntField(pageVisitor.acroForm.getField("withholdingTax"), pageVisitor.СведДох.СумИтНалПер[0].@НалУдерж)
            processNumIntField(pageVisitor.acroForm.getField("notholdingTax"), pageVisitor.СведДох.СумИтНалПер[0].@НалНеУдерж)
        }
    }

    class EmptyIncomeFiller extends FDFiller {

        @Override
        void accept(PageVisitor pageVisitor) {
            processField(pageVisitor.acroForm.getField("taxRate"), null)
            processNumIntField(pageVisitor.acroForm.getField("taxSumIntPart"), null)
            processNumFractField(pageVisitor.acroForm.getField("taxSumFractPart"), null)
            processNumIntField(pageVisitor.acroForm.getField("fixPayments"), null)
            processNumIntField(pageVisitor.acroForm.getField("taxBaseIntPart"), null)
            processNumFractField(pageVisitor.acroForm.getField("taxBaseFractPart"), null)
            processNumIntField(pageVisitor.acroForm.getField("transferPayment"), null)
            processNumIntField(pageVisitor.acroForm.getField("calculatedSum"), null)
            processNumIntField(pageVisitor.acroForm.getField("overholdingTax"), null)
            processNumIntField(pageVisitor.acroForm.getField("withholdingTax"), null)
            processNumIntField(pageVisitor.acroForm.getField("notholdingTax"), null)
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    class DeductionFiller extends FDFiller {
        @Override
        void accept(PageVisitor pageVisitor) {
            for (int i = 1; i <= 6; i++) {
                if (pageVisitor.ПредВычССИList && pageVisitor.ПредВычССИList.size() >= i) {
                    processField(pageVisitor.acroForm.getField("deductionCode" + (i)), pageVisitor.ПредВычССИList.get(i - 1).@КодВычет)
                    processNumIntField(pageVisitor.acroForm.getField("deductionIntPart" + (i)), pageVisitor.ПредВычССИList.get(i - 1).@СумВычет.split("\\.")[0])
                    processNumFractField(pageVisitor.acroForm.getField("deductionFractPart" + (i)), pageVisitor.ПредВычССИList.get(i - 1).@СумВычет.split("\\.")[1])
                } else {
                    processField(pageVisitor.acroForm.getField("deductionCode" + (i)), null)
                    processNumIntField(pageVisitor.acroForm.getField("deductionIntPart" + (i)), null)
                    processNumFractField(pageVisitor.acroForm.getField("deductionFractPart" + (i)), null)
                }
            }
            if (pageVisitor.УведВыч) {
                processField(pageVisitor.acroForm.getField("notifTypeCode"), pageVisitor.УведВыч.@КодВидУвед)
                processField(pageVisitor.acroForm.getField("notifNumber"), pageVisitor.УведВыч.@НомерУвед)
                processField(pageVisitor.acroForm.getField("notifDay"), pageVisitor.УведВыч.@ДатаУвед.substring(0, 2))
                processField(pageVisitor.acroForm.getField("notifMonth"), pageVisitor.УведВыч.@ДатаУвед.substring(3, 5))
                processField(pageVisitor.acroForm.getField("notifYear"), pageVisitor.УведВыч.@ДатаУвед.substring(6))
                processField(pageVisitor.acroForm.getField("notifTaxAuthCode"), pageVisitor.УведВыч.@НОУвед)
            } else {
                processField(pageVisitor.acroForm.getField("notifTypeCode"), null)
                processField(pageVisitor.acroForm.getField("notifNumber"), null)
                processField(pageVisitor.acroForm.getField("notifDay"), null)
                processField(pageVisitor.acroForm.getField("notifMonth"), null)
                processField(pageVisitor.acroForm.getField("notifYear"), null)
                processField(pageVisitor.acroForm.getField("notifTaxAuthCode"), null)
            }
        }
    }

    class ApplicationDataFiller extends FDFiller {
        @Override
        void accept(PageVisitor pageVisitor) {
            for (int i = 1; i <= 15; i++) {
                if (pageVisitor.СведСумДохDataList && pageVisitor.СведСумДохDataList.size() >= i) {
                    processField(pageVisitor.acroForm.getField("month" + i), !pageVisitor.СведСумДохDataList.get(i - 1).duplicate ? pageVisitor.СведСумДохDataList.get(i - 1).month : null)
                    processField(pageVisitor.acroForm.getField("incomeCode" + i), !pageVisitor.СведСумДохDataList.get(i - 1).duplicate ? pageVisitor.СведСумДохDataList?.get(i - 1)?.incomeCode : null)

                    processNumIntField(pageVisitor.acroForm.getField("incomeSumIntPart" + i), pageVisitor.СведСумДохDataList?.get(i - 1)?.incomeSum && !pageVisitor.СведСумДохDataList.get(i - 1).duplicate ? pageVisitor.СведСумДохDataList?.get(i - 1)?.incomeSum?.split("\\.")[0] : null)
                    processNumFractField(pageVisitor.acroForm.getField("incomeSumFractPart" + i), pageVisitor.СведСумДохDataList?.get(i - 1)?.incomeSum && !pageVisitor.СведСумДохDataList.get(i - 1).duplicate ? pageVisitor.СведСумДохDataList?.get(i - 1)?.incomeSum?.split("\\.")[1] : null)

                    processField(pageVisitor.acroForm.getField("deductionCode" + i), pageVisitor.СведСумДохDataList?.get(i - 1)?.deductionCode)

                    processNumIntField(pageVisitor.acroForm.getField("deductionSumIntPart" + i), pageVisitor.СведСумДохDataList?.get(i - 1)?.deductionSum ? pageVisitor.СведСумДохDataList?.get(i - 1)?.deductionSum?.split("\\.")[0] : null)
                    processNumFractField(pageVisitor.acroForm.getField("deductionSumFractPart" + i), pageVisitor.СведСумДохDataList?.get(i - 1)?.deductionSum ? pageVisitor.СведСумДохDataList?.get(i - 1)?.deductionSum?.split("\\.")[1] : null)
                } else {
                    processField(pageVisitor.acroForm.getField("month" + i), null)
                    processField(pageVisitor.acroForm.getField("incomeCode" + i), null)
                    processNumIntField(pageVisitor.acroForm.getField("incomeSumIntPart" + i), null)
                    processNumFractField(pageVisitor.acroForm.getField("incomeSumFractPart" + i), null)
                    processField(pageVisitor.acroForm.getField("deductionCode" + i), null)
                    processNumIntField(pageVisitor.acroForm.getField("deductionSumIntPart" + i), null)
                    processNumFractField(pageVisitor.acroForm.getField("deductionSumFractPart" + i), null)
                }
            }
        }
    }

    class PageVisitor {
        PDAcroForm acroForm
        Node xmlRoot
        Node reference
        int pageNum
        Node СведДох
        List<Node> ПредВычССИList
        Node УведВыч
        List<СведСумДохData> СведСумДохDataList

        PageVisitor(PDAcroForm acroForm, Node xmlRoot, Node reference, int pageNum, Node СведДох, List<Node> ПредВычССИList, Node УведВыч, List<СведСумДохData> СведСумДохDataList) {
            this.acroForm = acroForm
            this.xmlRoot = xmlRoot
            this.reference = reference
            this.pageNum = pageNum
            this.СведДох = СведДох
            this.ПредВычССИList = ПредВычССИList
            this.УведВыч = УведВыч
            this.СведСумДохDataList = СведСумДохDataList
        }
    }

    /**
     * Содержит набор правил для заполнения страницы
     */
    abstract class FillPageProcessor {
        abstract void fillPage(PageVisitor pageVisitor)
    }

    class TitlePageProcessor extends FillPageProcessor {
        List<FDFiller> pageParts = [new HeaderFiller(), new InfoFiller(), new IncomeFiller(), new DeductionFiller()]

        void fillPage(PageVisitor pageVisitor) {
            for (FDFiller pagePart : pageParts) {
                pagePart.accept(pageVisitor)
            }
        }
    }

    class IncomesPageProcessor extends FillPageProcessor {
        List<FDFiller> pageParts = [new HeaderFiller(), new EmptyInfoFiller(), new IncomeFiller(), new DeductionFiller()]

        void fillPage(PageVisitor pageVisitor) {
            for (FDFiller pagePart : pageParts) {
                pagePart.accept(pageVisitor)
            }
        }
    }

    class DeductionsPageProcessor extends FillPageProcessor {
        List<FDFiller> pageParts = [new HeaderFiller(), new EmptyInfoFiller(), new EmptyIncomeFiller(), new DeductionFiller()]

        void fillPage(PageVisitor pageVisitor) {
            for (FDFiller pagePart : pageParts) {
                pagePart.accept(pageVisitor)
            }
        }
    }

    class ApplicationPageProcessor extends FillPageProcessor {
        List<FDFiller> pageParts = [new ApplicationHeaderFiller(), new ApplicationDataFiller()]

        @Override
        void fillPage(PageVisitor pageVisitor) {
            for (FDFiller pagePart : pageParts) {
                pagePart.accept(pageVisitor)
            }
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    /**
     * Создает и определяет порядок заполнения страниц отчета
     */
    List<DocumentWrapper> createAndOrderReportPages(Node root, DataRow<Cell> row) {
        Node reference = root.Документ.'НДФЛ-2'.find { doc ->
            String a = doc.@НомСпр
            String b = row.pNumSpravka.toString()
            return a == b
        }

        List<Node> СведДохList = reference.СведДох

        СведДохList.sort { o1, o2 ->
            o1.@Ставка <=> o2.@Ставка
        }

        List<DocumentWrapper> documentList = []

        СведДохList.eachWithIndex { Node СведДох, int index ->
            DocumentWrapper basePage = null
            if (index == 0) {
                basePage = new TitlePage(PDDocument.load(declarationService.getTemplateFileContent(declarationData.getDeclarationTemplateId(), DeclarationTemplateFile.NDFL_2_REPORT_BY_PERSON_PAGE_BASE)))
            } else {
                basePage = new IncomePage(PDDocument.load(declarationService.getTemplateFileContent(declarationData.getDeclarationTemplateId(), DeclarationTemplateFile.NDFL_2_REPORT_BY_PERSON_PAGE_BASE)))
                basePage.incomeIndex = index
            }
            basePage.СведДох = СведДох
            documentList << basePage
            if (СведДохList.НалВычССИ) {
                List<Node> ПредВычССИList = СведДох?.НалВычССИ[0]?.ПредВычССИ ?: []
                ПредВычССИList.sort { o1, o2 ->
                    o1.@КодВычет <=> o2.@КодВычет
                }
                List<List<Node>> ПредВычССИListDividedByPage = ПредВычССИList?.collate(6)
                List<Node> УведВычList = СведДох?.НалВычССИ[0]?.УведВыч ?: []
                basePage.ПредВычССИList = ПредВычССИListDividedByPage[0]
                basePage.УведВыч = !УведВычList.isEmpty() ? УведВычList[0] : null
                for (int i = 1; i <= findAdditionalBaseListCount(ПредВычССИList.size(), УведВычList.size()); i++) {
                    DocumentWrapper deductionPage = new DeductionPage(PDDocument.load(declarationService.getTemplateFileContent(declarationData.getDeclarationTemplateId(), DeclarationTemplateFile.NDFL_2_REPORT_BY_PERSON_PAGE_BASE)))
                    if (ПредВычССИListDividedByPage.size() >= i) {
                        deductionPage.ПредВычССИList = ПредВычССИListDividedByPage[i]
                    }
                    if (УведВычList.size() >= i) {
                        deductionPage.УведВыч = УведВычList[i]
                    }
                    documentList << deductionPage
                }
            }
        }

        СведДохList.eachWithIndex { Node СведДох, int index ->

            List<Node> СведСумДохList = СведДох.ДохВыч[0].СвСумДох
            List<СведСумДохData> СведСумДохDataList = []

            СведСумДохList.each { Node СведСумДох ->
                СведСумДохData data = new СведСумДохData()
                data.month = СведСумДох.@Месяц
                data.incomeCode = СведСумДох.@КодДоход
                data.incomeSum = СведСумДох.@СумДоход
                data.taxRate = СведДох.@Ставка

                СведСумДохDataList << data

                List<Node> СвСумВычList = СведСумДох.СвСумВыч

                if (СвСумВычList) {
                    СвСумВычList.eachWithIndex { Node СвСумВыч, int i ->
                        if (i == 0) {
                            data.deductionCode = СвСумВыч.@КодВычет
                            data.deductionSum = СвСумВыч.@СумВычет
                        } else {
                            СведСумДохData deductionData = new СведСумДохData()
                            deductionData.month = СведСумДох.@Месяц
                            deductionData.incomeCode = СведСумДох.@КодДоход
                            deductionData.incomeSum = СведСумДох.@СумДоход
                            deductionData.deductionCode = СвСумВыч.@КодВычет
                            deductionData.deductionSum = СвСумВыч.@СумВычет
                            deductionData.taxRate = СведДох.@Ставка
                            deductionData.duplicate = true

                            СведСумДохDataList << deductionData
                        }
                    }
                }
            }

            List<List<СведСумДохData>> СведСумДохDataListDividedByPage = СведСумДохDataList.collate(15)

            СведСумДохDataListDividedByPage.each { List<СведСумДохData> PageСведСумДохDataList ->
                PageСведСумДохDataList.sort { СведСумДохData o1, СведСумДохData o2 ->
                    Integer.valueOf(o1.month) <=> Integer.valueOf(o2.month) ?: Integer.valueOf(o1.incomeCode) <=> Integer.valueOf(o2.incomeCode) ?: Integer.valueOf(o1.deductionCode) <=> Integer.valueOf(o2.deductionCode)
                }

                DocumentWrapper applicationPage = new ApplicationPage(PDDocument.load(declarationService.getTemplateFileContent(declarationData.getDeclarationTemplateId(), DeclarationTemplateFile.NDFL_2_REPORT_BY_PERSON_PAGE_APPLICATION)))
                applicationPage.СведСумДохDataList = PageСведСумДохDataList
                documentList << applicationPage
            }
        }

        return documentList
    }

    class СведСумДохData {
        String month
        String incomeCode
        String incomeSum
        String deductionCode
        String deductionSum
        String taxRate
        Boolean duplicate
    }

    static int findAdditionalBaseListCount(int ПредВычССИListSize, int УведВычListSize) {
        if (ПредВычССИListSize <= 6 && УведВычListSize <= 1) {
            return 0
        } else {
            return Math.max((int) Math.ceil((ПредВычССИListSize - 6) / 6d), УведВычListSize - 1)
        }
    }

    /**
     * Оставляем только необходимые данные для отчета
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    def filterData(params) {
        def xml = declarationService.getXmlData(declarationData.id, userInfo)
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
            ScriptUtils.checkInterrupted()
            ndflPersonDeductionFromRNUConsolidatedList.addAll(ndflPersonService.findDeductionsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
            ndflPersonPrepaymentFromRNUConsolidatedList.addAll(ndflPersonService.findPrepaymentsByNdflPersonAndOperation(it.ndflPersonId, it.operationId))
        }

        ndflPersonIncomeFromRNUConsolidatedList.each { NdflPersonIncome item ->
            ScriptUtils.checkInterrupted()
            NdflPersonIncome ndflPersonIncomePrimary = ndflPersonService.getIncome(item.sourceId)
            NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonIncomePrimary.ndflPersonId)
            ndflPersonPrimary.incomes.add(ndflPersonIncomePrimary)
        }

        ndflPersonDeductionFromRNUConsolidatedList.each { NdflPersonDeduction item ->
            ScriptUtils.checkInterrupted()
            NdflPersonDeduction ndflPersonDeductionPrimary = ndflPersonService.getDeduction(item.sourceId)
            NdflPerson ndflPersonPrimary = initNdflPersonPrimary(ndflPersonDeductionPrimary.ndflPersonId)
            ndflPersonPrimary.deductions.add(ndflPersonDeductionPrimary)
        }

        ndflPersonPrepaymentFromRNUConsolidatedList.each { NdflPersonPrepayment item ->
            ScriptUtils.checkInterrupted()
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
        // Вид отчетности
        String declarationTypeName = declarationTemplate.type.name
        String note = declarationData.note
        // Период
        int year = departmentReportPeriod.reportPeriod.taxPeriod.year
        String periodName = getProvider(RefBook.Id.PERIOD_CODE.id)
                .getRecords(reportPeriod.endDate, null, "ID = ${departmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).NAME.value
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
        String periodName = getProvider(RefBook.Id.PERIOD_CODE.id)
                .getRecords(rnuDepartmentReportPeriod.reportPeriod.endDate, null, "ID = ${rnuDepartmentReportPeriod.reportPeriod.dictTaxPeriodId}", null).get(0).NAME.value
        // Подразделение
        String departmentName = department.shortName
        // АСНУ
        String asnu = getProvider(RefBook.Id.ASNU.id).getRecords(this.reportPeriod.endDate, null, "ID = ${primaryRnuDeclarationData.asnuId}", null).get(0).NAME.value
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

    String formatDate(Date date) {
        return ScriptUtils.formatDate(date, SharedConstants.DATE_FORMAT)
    }

    /**
     * Группа строк 2-4 разделов с одинаковым operationId
     */
    class Operation {
        String operationId
        NdflPerson person
        List<NdflPersonIncome> incomes = []
        List<NdflPersonDeduction> deductions = []
        List<NdflPersonPrepayment> prepayments = []

        String getKpp() {
            return incomes.first().kpp
        }

        String getOktmo() {
            return incomes.first().oktmo
        }
    }

    @ToString(includePackage = false)
    @EqualsAndHashCode
    class MonthAndIncomeCodeKey {
        int month
        String incomeCode

        MonthAndIncomeCodeKey(int month, String incomeCode) {
            this.month = month
            this.incomeCode = incomeCode
        }
    }

    @ToString(includePackage = false)
    @EqualsAndHashCode
    class УведВычKey {
        Date notifDate
        String notifType
        String notifNum
        String notifSource

        УведВычKey(Date notifDate, String notifType, String notifNum, String notifSource) {
            this.notifDate = notifDate
            this.notifType = notifType
            this.notifNum = notifNum
            this.notifSource = notifSource
        }
    }
}



