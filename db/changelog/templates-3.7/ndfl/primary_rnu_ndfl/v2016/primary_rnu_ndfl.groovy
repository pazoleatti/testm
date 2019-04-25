package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DateColumn
import com.aplana.sbrf.taxaccounting.model.DeclarationCheckCode
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateFile
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.FormSources
import com.aplana.sbrf.taxaccounting.model.Ndfl2_6DataReportParams
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.PrepareSpecificReportResult
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificDeclarationDataReportHolder
import com.aplana.sbrf.taxaccounting.model.StringColumn
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants
import com.aplana.sbrf.taxaccounting.model.TAUserInfo
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.CalendarService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.FiasRefBookService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.PersonService
import com.aplana.sbrf.taxaccounting.script.service.RefBookService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.LogBusinessService
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils
import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.time.DateUtils
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.xml.sax.Attributes
import org.xml.sax.Locator
import org.xml.sax.SAXException
import org.xml.sax.ext.Attributes2Impl

import javax.xml.parsers.ParserConfigurationException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.script.SharedConstants.DATE_ZERO_AS_STRING
import static org.apache.commons.lang3.StringUtils.isEmpty
import static form_template.ndfl.primary_rnu_ndfl.v2016.PrimaryRnuNdfl.DataFormatEnum.*
import static org.apache.commons.lang3.StringUtils.isEmpty

new PrimaryRnuNdfl(this).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class PrimaryRnuNdfl extends AbstractScriptClass {

    DeclarationData declarationData
    DeclarationTemplate declarationTemplate
    DepartmentReportPeriod departmentReportPeriod
    ReportPeriod reportPeriod
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
    File dataFile
    BlobDataService blobDataService
    RefBookService refBookService
    LogBusinessService logBusinessService
    List<NdflPerson> ndflPersonCache = new ArrayList<>()
    boolean adjustNegativeValues
    Set<String> kppList
    Date dateFrom, dateTo
    Date formCreationDate = null
    Date date = new Date()

    @TypeChecked(TypeCheckingMode.SKIP)
    PrimaryRnuNdfl(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
            this.declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
            this.departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            this.reportPeriod = departmentReportPeriod.reportPeriod
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo")
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService")
        }
        if (scriptClass.getBinding().hasVariable("scriptSpecificReportHolder")) {
            this.scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) scriptClass.getProperty("scriptSpecificReportHolder")
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getBinding().getProperty("refBookService")
        }
        if (scriptClass.getBinding().hasVariable("needSources")) {
            this.needSources = (Boolean) scriptClass.getProperty("needSources")
        }
        if (scriptClass.getBinding().hasVariable("light")) {
            this.light = (Boolean) scriptClass.getProperty("light")
        }
        if (scriptClass.getBinding().hasVariable("sources")) {
            this.sources = (FormSources) scriptClass.getProperty("sources")
        }
        if (scriptClass.getBinding().hasVariable("dataFile")) {
            this.dataFile = (File) scriptClass.getProperty("dataFile")
        }
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataService = (BlobDataService) scriptClass.getProperty("blobDataServiceDaoImpl")
        }
        if (scriptClass.getBinding().hasVariable("logBusinessService")) {
            this.logBusinessService = (LogBusinessService) scriptClass.getProperty("logBusinessService")
        }
    }

    @Override
    void run() {
        /**
         * Скрипт макета декларации РНУ-НДФЛ(первичная)
         */
        switch (formDataEvent) {
            case FormDataEvent.CREATE:
                checkCreate()
                break
            case FormDataEvent.IMPORT_TRANSPORT_FILE:
                importData()
                break
            case FormDataEvent.PREPARE_SPECIFIC_REPORT:
                // Подготовка для последующего формирования спецотчета
                prepareSpecificReport()
                break
            case FormDataEvent.CREATE_EXCEL_REPORT: //создание xlsx отчета
                createXlsxReport()
                break
            case FormDataEvent.CREATE_SPECIFIC_REPORT:
                // Формирование спецотчета
                createSpecificReport()
                break
            case FormDataEvent.EXPORT_DECLARATION_DATA_TO_EXCEL:
                exportDeclarationDataToExcel()
                break
        }
    }


    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"

    final String TEMPLATE_PERSON_FL_OPER = "%s, ИНП: %s, ID операции: %s"
    final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует справочнику \"%s\""

    final String RNU_NDFL_PERSON_ALL_DB = "rnu_ndfl_person_all_db.xlsx"
    final String REPORT_XLSX = "report.xlsx"

    static SXSSFWorkbook sxssfWorkbook

    /**
     * Получить дату которая используется в качестве версии записей справочника
     * @return дата используемая в качестве даты версии справочника
     */
    def getVersionFrom() {
        return getReportPeriodStartDate()
    }

    //------------------ PREPARE_SPECIFIC_REPORT ----------------------

    def prepareSpecificReport() {
        def reportAlias = scriptSpecificReportHolder?.declarationSubreport?.alias
        if (SubreportAliasConstants.RNU_NDFL_PERSON_DB != reportAlias) {
            throw new ServiceException("Обработка данного спец. отчета не предусмотрена!")
        }
        PrepareSpecificReportResult result = new PrepareSpecificReportResult()
        List<Column> tableColumns = createTableColumns()
        List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows = new ArrayList<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>>()
        List<Column> rowColumns = createRowColumns()

        //Проверка, подготовка данных
        def params = scriptSpecificReportHolder.subreportParamValues
        Map<String, Object> reportParameters = scriptSpecificReportHolder.getSubreportParamValues()

        if (reportParameters.isEmpty()) {
            throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.")
        }

        def resultReportParameters = [:]
        reportParameters.each { key, value ->
            if (value != null) {
                def val = value
                if (!(key in [SubreportAliasConstants.FROM_BIRTHDAY, SubreportAliasConstants.TO_BIRTHDAY])) {
                    val = '%' + value + '%'
                }
                resultReportParameters.put(key, val)
            }
        }

        // Ограничение числа выводимых записей
        int startIndex = 1
        int pageSize = 10

        PagingResult<NdflPerson> personsPage = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters, startIndex, pageSize)

        //Если записи не найдены, то система формирует предупреждение:
        //Заголовок: "Предупреждение"
        //Текст: "Физическое лицо: <Данные ФЛ> не найдено в форме", где <Данные ФЛ> - значение полей формы, по которым выполнялся поиск физического лица, через разделитель " "
        //Кнопки: "Закрыть"

        if (personsPage.isEmpty()) {
            Closure subreportParamsToString = {
                it.collect { Map.Entry<String, Object> param ->
                    (param.value != null ? (((param.value instanceof Date) ? ((Date) param.value).format('dd.MM.yyyy') : (String) param.value) + "") : "")
                } join " "
            }
            logger.warn("Физическое лицо: " + subreportParamsToString(reportParameters) + " не найдено в форме")
            //throw new ServiceException("Физическое лицо: " + subreportParamsToString(reportParameters)+ " не найдено в форме")
        }

        personsPage.each() { ndflPerson ->
            DataRow<com.aplana.sbrf.taxaccounting.model.Cell> row = new DataRow<>(ScriptUtils.createCells(rowColumns, null))
            row.getCell("id").setStringValue(ndflPerson.id.toString())
            row.lastName = ndflPerson.lastName
            row.firstName = ndflPerson.firstName
            row.middleName = ndflPerson.middleName
            row.snils = ndflPerson.snils
            row.innNp = ndflPerson.innNp
            row.inp = ndflPerson.inp
            row.birthDay = ndflPerson.birthDay
            row.idDocNumber = ndflPerson.idDocNumber
            row.statusNp = getPersonStatusName(ndflPerson.status)
            row.innForeign = ndflPerson.innForeign
            dataRows.add(row)
        }

        int countOfAvailableNdflPerson = personsPage.size()

        if (countOfAvailableNdflPerson >= pageSize) {
            countOfAvailableNdflPerson = ndflPersonService.findNdflPersonCountByParameters(declarationData.id, resultReportParameters)
        }

        result.setTableColumns(tableColumns)
        result.setDataRows(dataRows)
        result.setCountAvailableDataRows(countOfAvailableNdflPerson)
        scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
        scriptSpecificReportHolder.setSubreportParamValues(params)
    }

    String getPersonStatusName(String statusCode) {
        RefBookDataProvider provider = getProvider(RefBook.Id.TAXPAYER_STATUS.getId())
        PagingResult<Map<String, RefBookValue>> record = provider.getRecords(getReportPeriodEndDate(), null, "CODE = '$statusCode'", null)
        return record.get(0).get("NAME").getValue()
    }

    List<Column> createTableColumns() {
        List<Column> tableColumns = new ArrayList<Column>()

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
        column4.setAlias("snils")
        column4.setName("СНИЛС")
        column4.setWidth(10)
        tableColumns.add(column4)

        Column column5 = new StringColumn()
        column5.setAlias("innNp")
        column5.setName("ИНН РФ")
        column5.setWidth(10)
        tableColumns.add(column5)

        Column column6 = new StringColumn()
        column6.setAlias("inp")
        column6.setName("ИНП")
        column6.setWidth(10)
        tableColumns.add(column6)

        Column column7 = new DateColumn()
        column7.setAlias("birthDay")
        column7.setName("Дата рождения")
        column7.setWidth(10)
        tableColumns.add(column7)

        Column column8 = new StringColumn()
        column8.setAlias("idDocNumber")
        column8.setName("№ ДУЛ")
        column8.setWidth(10)
        tableColumns.add(column8)

        Column column9 = new StringColumn()
        column9.setAlias("statusNp")
        column9.setName("Статус налогоплательщика")
        column9.setWidth(30)
        tableColumns.add(column9)

        Column column10 = new StringColumn()
        column10.setAlias("innForeign")
        column10.setName("ИНН Страны гражданства")
        column10.setWidth(10)
        tableColumns.add(column10)

        return tableColumns
    }

    List<Column> createRowColumns() {
        List<Column> tableColumns = new ArrayList<Column>()

        Column columnId = new StringColumn()
        columnId.setAlias("id")
        columnId.setName("id")
        columnId.setWidth(10)
        tableColumns.add(columnId)

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
        column4.setAlias("snils")
        column4.setName("СНИЛС")
        column4.setWidth(10)
        tableColumns.add(column4)

        Column column5 = new StringColumn()
        column5.setAlias("innNp")
        column5.setName("ИНН РФ")
        column5.setWidth(10)
        tableColumns.add(column5)

        Column column6 = new StringColumn()
        column6.setAlias("inp")
        column6.setName("ИНП")
        column6.setWidth(10)
        tableColumns.add(column6)

        Column column7 = new DateColumn()
        column7.setAlias("birthDay")
        column7.setName("Дата рождения")
        column7.setWidth(10)
        tableColumns.add(column7)

        Column column8 = new StringColumn()
        column8.setAlias("idDocNumber")
        column8.setName("№ ДУЛ")
        column8.setWidth(10)
        tableColumns.add(column8)

        Column column9 = new StringColumn()
        column9.setAlias("statusNp")
        column9.setName("Статус налогоплательщика")
        column9.setWidth(30)
        tableColumns.add(column9)

        Column column10 = new StringColumn()
        column10.setAlias("innForeign")
        column10.setName("ИНН Страны гражданства")
        column10.setWidth(10)
        tableColumns.add(column10)

        return tableColumns
    }

    //------------------ Create Report ----------------------
/**
 * Создать XLSX отчет
 * @return
 */
    def createXlsxReport() {
        сheckEvent()
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)

        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
        Collections.sort(ndflPersonIncomeList, new Comparator<NdflPersonIncome>() {
            @Override
            int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                return o1.id <=> o2.id
            }
        })

        SheetFillerContext context = new SheetFillerContext(ndflPersonList, ndflPersonIncomeList)
        Workbook sxssfWorkbook = new SXSSFWorkbook(getSpecialReportTemplate(REPORT_XLSX), 100, true)
        sxssfWorkbook.setSheetName(0, "Реестр")
        SheetFillerFactory.getSheetFiller(5).fillSheet(sxssfWorkbook, context)

        OutputStream writer = null
        try {
            StringBuilder fileName = new StringBuilder("Реестр_загруженных_данных_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
            scriptSpecificReportHolder.setFileName(fileName.toString())
            writer = scriptSpecificReportHolder.getFileOutputStream()
            sxssfWorkbook.write(writer)
        } finally {
            writer.close()
        }
    }

    def createSpecificReport() {
        int ndflPersonCount = ndflPersonService.getCountNdflPerson(declarationData.id)
        if (ndflPersonCount == 0) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            Department department = departmentService.get(departmentReportPeriod.departmentId)
            String strCorrPeriod = ""
            if (departmentReportPeriod.getCorrectionDate() != null) {
                strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy")
            }
            logger.error("Спецотчет \"%s\" не сформирован, т.к. в форме %d, Период %s, Подразделение %s отсутствуют данные для формирования спецотчета",
                    scriptSpecificReportHolder.declarationSubreport?.name,
                    declarationData.id,
                    departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod,
                    department.name)
            return
        }
        switch (scriptSpecificReportHolder?.declarationSubreport?.alias) {
            case SubreportAliasConstants.RNU_NDFL_PERSON_DB:
                exportPersonDataToExcel()
                break
            case SubreportAliasConstants.RNU_NDFL_PERSON_ALL_DB:
                exportAllDeclarationDataToExcel()
                scriptSpecificReportHolder.setFileName("РНУ_НДФЛ_${declarationData.id}_${new Date().format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break
            case SubreportAliasConstants.RNU_RATE_REPORT:
                createRateReport()
                scriptSpecificReportHolder.setFileName("Отчет_в_разрезе_ставок_${declarationData.id}_${date.format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break
            case SubreportAliasConstants.RNU_PAYMENT_REPORT:
                createPaymentReport()
                scriptSpecificReportHolder.setFileName("Отчет_в_разрезе_ПП_${declarationData.id}_${date.format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break
            case SubreportAliasConstants.RNU_NDFL_DETAIL_REPORT:
                createNdflDetailReport()
                scriptSpecificReportHolder.setFileName("Детализация_${declarationData.id}_${date.format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break
            case SubreportAliasConstants.RNU_NDFL_2_6_DATA_XLSX_REPORT:
                create2_6NdflDataReport('xlsx')
                scriptSpecificReportHolder.setFileName("Данные_для_2_и_6-НДФЛ_${declarationData.id}_${date.format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break
            case SubreportAliasConstants.RNU_NDFL_2_6_DATA_TXT_REPORT:
                create2_6NdflDataReport('txt')
                scriptSpecificReportHolder.setFileName("Данные_для_2_и_6-НДФЛ_${declarationData.id}_${date.format('yyyy-MM-dd_HH-mm-ss')}.txt")
                break
            default:
                throw new ServiceException("Обработка данного спец. отчета не предусмотрена!")
        }
    }

    void exportPersonDataToExcel() {
        List<NdflPerson> ndflPersonList = []
        NdflPerson ndflPerson = ndflPersonService.get((Long) scriptSpecificReportHolder.subreportParamValues.get("PERSON_ID"))
        ndflPersonList.add(ndflPerson)
        if (ndflPerson != null) {
            List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findIncomes(ndflPerson.id)
            Collections.sort(ndflPersonIncomeList, new Comparator<NdflPersonIncome>() {
                @Override
                int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                    return o1.id <=> o2.id
                }
            })

            List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findDeductions(ndflPerson.id)
            Collections.sort(ndflPersonDeductionList, new Comparator<NdflPersonDeduction>() {
                @Override
                int compare(NdflPersonDeduction o1, NdflPersonDeduction o2) {
                    return o1.id <=> o2.id
                }
            })
            List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findPrepayments(ndflPerson.id)
            Collections.sort(ndflPersonPrepaymentList, new Comparator<NdflPersonPrepayment>() {
                @Override
                int compare(NdflPersonPrepayment o1, NdflPersonPrepayment o2) {
                    return o1.id <=> o2.id
                }
            })
            String departmentName = departmentService.get(declarationData.departmentId)?.name
            String reportDate = getReportPeriodEndDate().format("dd.MM.yyyy") + " г."
            String period = getProvider(RefBook.Id.PERIOD_CODE.getId()).getRecordData(reportPeriod.dictTaxPeriodId)?.NAME?.value
            String year = getReportPeriodEndDate().format("yyyy") + " г."

            SheetFillerContext context = new SheetFillerContext(departmentName, reportDate, period, year, ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

            Workbook xssfWorkbook = getSpecialReportTemplate(RNU_NDFL_PERSON_ALL_DB)

            SheetFillerFactory.getSheetFiller(0).fillSheet(xssfWorkbook, context)

            SheetFillerFactory.getSheetFiller(1).fillSheet(xssfWorkbook, context)

            SheetFillerFactory.getSheetFiller(2).fillSheet(xssfWorkbook, context)

            SheetFillerFactory.getSheetFiller(3).fillSheet(xssfWorkbook, context)

            SheetFillerFactory.getSheetFiller(4).fillSheet(xssfWorkbook, context)

            OutputStream writer = null
            try {
                writer = scriptSpecificReportHolder.getFileOutputStream()
                xssfWorkbook.write(writer)
            } finally {
                writer.close()
            }

            scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")
        } else {
            throw new ServiceException("Не найдены данные для формирования отчета!")
        }
    }

    /**
     * Формат имени файла: РНУ_НДФЛ_<ИД формы>_<ФамилияИО>_<ДУЛ>_<ДатаВремя выгрузки>, где
     * <ИД формы> - ID формы из БД
     * <ФамилияИО> - Фамилия ФЛ полностью + первая буква имени + первая буква отчества (при наличии). Пример: ИвановаИИ
     * <ДУЛ> - Серия и номер документа, удостоверяющего личность в формате "Серия№Номер", Серия и Номер ДУЛ не должны содержать разделителей. Пример: 8888№123321
     * <ДатаВремя выгрузки> - дата и время выгрузки в формате ГГГГММДД_ЧЧММ. Пример: 20160216_1842
     * @return
     */
    String createFileName(NdflPerson ndflPerson) {
        StringBuilder sb = new StringBuilder()
        sb.append("РНУ_НДФЛ_")
        sb.append(declarationData.id).append("_")
        sb.append(capitalize(ndflPerson.lastName))
        sb.append(firstChar(ndflPerson.firstName))
        sb.append(firstChar(ndflPerson.middleName)).append("_")
        sb.append(ndflPerson.idDocNumber?.replaceAll("\\s", "")?.toLowerCase()).append("_")
        sb.append(new SimpleDateFormat("yyyy.MM.dd_HHmm").format(new Date()))
        return sb.toString()
    }


    String firstChar(String str) {
        if (str != null && !str.isEmpty()) {
            return String.valueOf(Character.toUpperCase(str.charAt(0)))
        } else {
            return ""
        }
    }

    String capitalize(String str) {
        int strLen
        if (str == null || (strLen = str.length()) == 0) {
            return str
        }
        return new StringBuilder(strLen)
                .append(Character.toTitleCase(str.charAt(0)))
                .append(str.substring(1).toLowerCase())
                .toString()
    }

    /**
     * Спецотчет РНУ-НДФЛ по всем ФЛ
     */
    void exportAllDeclarationDataToExcel() {

        ScriptUtils.checkInterrupted()
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
        ndflPersonList.sort { it.rowNum }
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
        ndflPersonIncomeList.sort { it.rowNum }
        List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
        ndflPersonDeductionList.sort { it.rowNum }
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
        ndflPersonPrepaymentList.sort { it.rowNum }
        String departmentName = departmentService.get(declarationData.departmentId)?.name
        String reportDate = getReportPeriodEndDate().format("dd.MM.yyyy") + " г."
        String period = getProvider(RefBook.Id.PERIOD_CODE.getId()).getRecordData(reportPeriod.dictTaxPeriodId)?.NAME?.value
        String year = getReportPeriodEndDate().format("yyyy") + " г."

        SheetFillerContext context = new SheetFillerContext(departmentName, reportDate, period, year, ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

        Workbook xssfWorkbook = getSpecialReportTemplate(RNU_NDFL_PERSON_ALL_DB)

        SheetFillerFactory.getSheetFiller(0).fillSheet(xssfWorkbook, context)

        Workbook sxssfWorkbook = new SXSSFWorkbook(xssfWorkbook, 100, true)

        SheetFillerFactory.getSheetFiller(1).fillSheet(sxssfWorkbook, context)

        SheetFillerFactory.getSheetFiller(2).fillSheet(sxssfWorkbook, context)

        SheetFillerFactory.getSheetFiller(3).fillSheet(sxssfWorkbook, context)

        SheetFillerFactory.getSheetFiller(4).fillSheet(sxssfWorkbook, context)

        OutputStream writer = null
        try {
            writer = scriptSpecificReportHolder.getFileOutputStream()
            sxssfWorkbook.write(writer)
        } finally {
            writer.close()
        }
    }

    // Находит в базе данных шаблон спецотчета
    XSSFWorkbook getSpecialReportTemplate(String reportFileName) {
        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        String blobDataId = null
        for (DeclarationTemplateFile declarationTemplateFile : declarationTemplate.declarationTemplateFiles) {
            if (declarationTemplateFile.fileName == reportFileName) {
                blobDataId = declarationTemplateFile.blobDataId
                break
            }
        }
        BlobData blobData = blobDataService.get(blobDataId)
        return new XSSFWorkbook(blobData.getInputStream())
    }

    /**
     * Отчет Карманниковой: Отчет в разрезе ставок
     */
    void createRateReport() {
        List<IncomeExt> incomes = ndflPersonService.findAllIncomesByDeclarationIdByOrderByRowNumAsc(declarationData.id).collect {
            new IncomeExt(it)
        }
        Collection<List<IncomeExt>> operations = incomes.groupBy {
            new Pair<String, Long>(it.operationId, it.asnuId)
        }.values()
        defineTaxRates(operations)
        correctTaxRates(operations)
        def incomesByKey = incomes.groupBy { new RateReportKey(it.kpp, it.asnuId, it.definedTaxRate) }
        List<RateReportRow> rows = []
        incomesByKey.each { key, incomesGroup ->
            def row = new RateReportRow()
            row.key = key
            row.asnuName = incomesGroup.first().asnu
            for (def income : incomesGroup) {
                row.incomeAccruedSum += (income.incomeAccruedSumm ?: 0)
                row.incomePayoutSum += (income.incomePayoutSumm ?: 0)
                row.totalDeductionsSum += (income.totalDeductionsSumm ?: 0)
                row.calculatedTax += (income.calculatedTax ?: 0)
                row.withholdingTax += (income.withholdingTax ?: 0)
                row.refoundTax += (income.refoundTax ?: 0)
                row.notHoldingTax += (income.notHoldingTax ?: 0)
                row.overholdingTax += (income.overholdingTax ?: 0)
                row.taxSum += (income.taxSumm ?: 0)
            }
            rows.add(row)
        }
        rows.sort({ def a, def b ->
            a.kpp <=> b.kpp ?: a.asnuName <=> b.asnuName ?:
                    (a.rate == null && b.rate == null ? 0 : a.rate == null ? 1 : b.rate == null ? -1 : a.rate <=> b.rate)
        })
        new RateReportBuilder(rows).build()
    }

    /**
     * Отчет Карманниковой: Отчет в разрезе платёжных поручений
     */
    void createPaymentReport() {
        List<IncomeExt> incomes = ndflPersonService.findAllIncomesByDeclarationIdByOrderByRowNumAsc(declarationData.id).collect {
            new IncomeExt(it)
        }
        Collection<List<IncomeExt>> operations = incomes.groupBy {
            new Pair<String, Long>(it.operationId, it.asnuId)
        }.values()
        defineTaxRates(operations)
        definePaymentNumber(operations)
        correctTaxRates(operations)
        defineCorrection(operations)
        def incomesByKey = incomes.groupBy {
            new PaymentReportKey(it.kpp, it.asnuId, it.paymentNumber, it.definedTaxRate, it.correction)
        }
        List<PaymentReportRow> rows = []
        incomesByKey.each { key, incomesGroup ->
            def row = new PaymentReportRow()
            row.key = key
            row.asnuName = incomesGroup.first().asnu
            for (def income : incomesGroup) {
                row.calculatedTax += (income.calculatedTax ?: 0)
                row.withholdingTax += (income.withholdingTax ?: 0)
                row.refoundTax += (income.refoundTax ?: 0)
                row.taxSum += (income.taxSumm ?: 0)
            }
            rows.add(row)
        }
        defineOrder(rows, operations)
        rows.sort({ def a, def b ->
            a.kpp <=> b.kpp ?: a.asnuName <=> b.asnuName ?: a.paymentNumber <=> b.paymentNumber ?:
                    a.order <=> b.order ?: a.correction <=> b.correction
        })
        new PaymentReportBuilder(rows).build()
    }

    /**
     * Отчет Карманниковой: Детализация – доходы, вычеты, налоги
     */
    void createNdflDetailReport() {
        List<IncomeExt> incomes = ndflPersonService.findAllIncomesByDeclarationIdByOrderByRowNumAsc(declarationData.id).collect {
            new IncomeExt(it)
        }
        Collection<List<IncomeExt>> operations = incomes.groupBy {
            new Pair<String, Long>(it.operationId, it.asnuId)
        }.values()
        defineTaxRates(operations)
        defineCorrection(operations)
        def incomesByKey = incomes.groupBy {
            new NdflDetailReportKey(it.kpp, it.oktmo, it.asnuId, it.definedTaxRate, it.incomeAccruedDate, it.incomePayoutDate, it.taxDate, it.taxTransferDate)
        }
        List<NdflDetailReportRow> rows = []
        incomesByKey.each { key, incomesGroup ->
            def row = new NdflDetailReportRow()
            row.key = key
            row.asnuName = incomesGroup.first().asnu
            for (def income : incomesGroup) {
                row.incomeAccruedSum += (income.incomeAccruedSumm ?: 0)
                row.incomePayoutSum += (income.incomePayoutSumm ?: 0)
                row.totalDeductionsSum += (income.totalDeductionsSumm ?: 0)
                row.calculatedTax += (income.calculatedTax ?: 0)
                row.withholdingTax += (income.withholdingTax ?: 0)
                row.refoundTax += (income.refoundTax ?: 0)
                row.notHoldingTax += (income.notHoldingTax ?: 0)
                row.overholdingTax += (income.overholdingTax ?: 0)
                row.taxSum += (income.taxSumm ?: 0)
            }
            rows.add(row)
        }
        rows.sort({ def a, def b ->
            a.kpp <=> b.kpp ?: a.oktmo <=> b.oktmo ?: a.asnuName <=> b.asnuName ?: a.orderDate <=> b.orderDate ?: a.rowType <=> b.rowType
        })
        new NdflDetailReportBuilder(rows).build()
    }

    /**
     * Определяет ставки у строк 2 раздела
     */
    void defineTaxRates(Collection<List<IncomeExt>> operations) {
        operations.each { incomesOfOperation ->
            IncomeExt prevRateIncome = null // предыдущая строка, содержащая ставку
            Map<Date, IncomeExt> withholdingIncomesByTransferDate = [:] // строки удержания по сроку перечисления
            for (def income : incomesOfOperation) {
                if (income.taxRate != null && income.taxTransferDate) {
                    withholdingIncomesByTransferDate.put(income.taxTransferDate, income)
                }
            }
            Integer taxRate = findSingleTaxRate(incomesOfOperation)
            if (taxRate != null) {
                // Если ставка только одна, то определяем её для всех строк перечисления
                for (def income : incomesOfOperation) {
                    income.definedTaxRate = taxRate
                }
            } else {
                for (def income : incomesOfOperation) {
                    if (income.taxRate == null) {
                        def withholdingIncome = withholdingIncomesByTransferDate.get(income.taxTransferDate)
                        if (withholdingIncome && withholdingIncome.taxRate != null) {
                            income.definedTaxRate = withholdingIncome.taxRate
                        } else if (prevRateIncome) {
                            income.definedTaxRate = prevRateIncome.taxRate
                        }
                    } else {
                        income.definedTaxRate = income.taxRate
                        prevRateIncome = income
                    }
                }
            }
        }
    }

    /**
     * Если в строках операции только одна ставка, то возвращяет её, иначе null
     */
    Integer findSingleTaxRate(List<IncomeExt> incomesOfOperation) {
        Integer taxRate = null
        for (def income : incomesOfOperation) {
            if (income.taxRate != null) {
                if (taxRate != null && taxRate != income.taxRate) {
                    return null
                }
                taxRate = income.taxRate
            }
        }
        return taxRate
    }

    /**
     * Заполняет номер платежного поручения у строк 2 раздела
     */
    void definePaymentNumber(Collection<List<IncomeExt>> operations) {
        operations.each { incomesOfOperation ->
            IncomeExt prevTransferIncome = null // предыдущая строка перечисления
            for (int i = 0; i < incomesOfOperation.size(); i++) {
                IncomeExt income = incomesOfOperation[i]
                if (income.taxRate == null) {// строка Перечисления
                    prevTransferIncome = income
                } else {
                    IncomeExt nextTransferIncome = null // следующая строка перечисления
                    for (int j = i + 1; j < incomesOfOperation.size(); j++) {
                        if (incomesOfOperation[j].taxRate == null) {
                            nextTransferIncome = incomesOfOperation[j]
                            break
                        }
                    }
                    income.paymentNumber = nextTransferIncome?.definedTaxRate == income.taxRate ? nextTransferIncome.paymentNumber :
                            (prevTransferIncome?.paymentNumber ?: nextTransferIncome?.paymentNumber)
                }
            }
        }
    }

    /**
     * Корректирует ставку у последней строки перечисления
     */
    void correctTaxRates(Collection<List<IncomeExt>> operations) {
        operations.each { incomesOfOperation ->
            IncomeExt lastTransferIncome = null // последняя строка перечисления
            IncomeExt lastRateIncome = null // последняя строка, содержащая ставку
            for (def income : incomesOfOperation) {
                if (income.taxRate == null) {
                    lastTransferIncome = income
                } else {
                    lastRateIncome = income
                }
            }
            if (lastTransferIncome) {
                lastTransferIncome.definedTaxRate = lastRateIncome?.taxRate
            }
        }
    }

    /**
     * Определяет является ли строка корректирующей
     */
    void defineCorrection(Collection<List<IncomeExt>> operations) {
        operations.each { incomesOfOperation ->
            boolean containsTransferIncome = false
            for (def income : incomesOfOperation) {
                if (income.taxRate == null) {
                    containsTransferIncome = true
                    break
                }
            }
            if (containsTransferIncome) {
                // Если содержит строку перечисления, то все строки после последней строки перечисления считаются корректирующими
                for (int i = incomesOfOperation.size() - 1; i >= 0; i--) {
                    IncomeExt income = incomesOfOperation[i]
                    if (income.taxRate != null) {
                        income.correction = true
                    } else {
                        break
                    }
                }
            }
        }
    }

    void defineOrder(List<PaymentReportRow> rows, Collection<List<IncomeExt>> operations) {
        Map<String, Set<String>> operationsIdsByPaymentNumber = [:]
        operations.each { incomesOfOperation ->
            for (def income : incomesOfOperation) {
                if (income.paymentNumber) {
                    def operationIds = operationsIdsByPaymentNumber.get(income.paymentNumber)
                    if (operationIds) {
                        operationIds.add(income.operationId)
                    } else {
                        operationsIdsByPaymentNumber.put(income.paymentNumber, [income.operationId] as Set)
                    }
                }
            }
        }
        def rowsGroups = rows.groupBy {
            new PaymentReportKey(it.key.kpp, it.key.asnuId, it.key.paymentNumber, null, false)
        }
        rowsGroups.each { key, groupRows ->
            if ((operationsIdsByPaymentNumber.get(key.paymentNumber)?.size() ?: 0) == 1) {
                List<Integer> orders = []
                for (def row : groupRows) {
                    int order = orders.indexOf(row.rate)
                    if (order == -1) {
                        order = orders.size()
                        orders.add(row.rate)
                    }
                    row.order = order
                }
            } else {
                for (def row : groupRows) {
                    row.order = row.rate
                }
            }
        }
    }

    /**
     * Отчет Карманниковой: Отчет в разрезе ставок
     */
    class RateReportBuilder extends AbstractReportBuilder {
        List<String> header = ["КПП", "АСНУ", "Ставка", "Сумма дохода начисленного", "Сумма дохода выплаченного", "Сумма вычетов", "Налог исчисленный",
                               "Налог удержанный", "Возврат", "Долг за НП", "Долг за НА", "Налог перечисленный"]
        List<RateReportRow> rows

        RateReportBuilder(List<RateReportRow> rows) {
            super()
            this.rows = rows
            this.sheet = workbook.createSheet("Отчет")
        }

        void build() {
            fillHeader()
            createTableHeaders()
            createDataForTable()
            cellAlignment()
            flush()
        }

        protected void fillHeader() {
            createReportNameRow("Отчет в разрезе ставок")
            createYearRow()
            createPeriodRow()
            createFormTypeRow()
            createForNumRow()
            createDepartmentRow()
            createReportDateRow()
        }

        void createTableHeaders() {
            def style = new StyleBuilder(workbook).borders(true).wrapText(true).hAlign(CellStyle.ALIGN_CENTER).boldweight(Font.BOLDWEIGHT_BOLD).build()
            Row row = sheet.createRow(9)
            for (int colIndex = 0; colIndex < header.size(); colIndex++) {
                Cell cell = row.createCell(colIndex)
                cell.setCellValue(header.get(colIndex))
                cell.setCellStyle(style)
            }
        }

        void createDataForTable() {
            currentRowIndex = 9
            def styleBuilder = new StyleBuilder(workbook).borders(true).wrapText(true)
            def kppStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).build()
            def asnuNameStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).build()
            def rateStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def incomeAccruedSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER_2).build()
            def incomePayoutSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER_2).build()
            def totalDeductionsSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER_2).build()
            def calculatedTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def withholdingTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def refoundTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def deptTaxPayerStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def deptAgentStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def taxSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            for (def dataRow : rows) {
                sheet.createRow(++currentRowIndex)
                int colIndex = 0
                createCell(colIndex, dataRow.kpp, kppStyle)
                createCell(++colIndex, dataRow.asnuName, asnuNameStyle)
                createCell(++colIndex, dataRow.rate, rateStyle)
                createCell(++colIndex, dataRow.incomeAccruedSum, incomeAccruedSumStyle)
                createCell(++colIndex, dataRow.incomePayoutSum, incomePayoutSumStyle)
                createCell(++colIndex, dataRow.totalDeductionsSum, totalDeductionsSumStyle)
                createCell(++colIndex, dataRow.calculatedTax, calculatedTaxStyle)
                createCell(++colIndex, dataRow.withholdingTax, withholdingTaxStyle)
                createCell(++colIndex, dataRow.refoundTax, refoundTaxStyle)
                createCell(++colIndex, dataRow.deptTaxPayer, deptTaxPayerStyle)
                createCell(++colIndex, dataRow.deptAgent, deptAgentStyle)
                createCell(++colIndex, dataRow.taxSum, taxSumStyle)
            }
        }

        void cellAlignment() {
            // фиксированная ширина для столбцов. Если не указана, то будет автоподбор ширины, но в определенном пределе
            Map<Integer, Integer> widths = [0: 16, 2: 10, 3: 16, 4: 16, 5: 16, 6: 16, 7: 16, 8: 16, 9: 16, 10: 16, 11: 16]
            for (int i = 0; i < header.size(); i++) {
                sheet.autoSizeColumn(i)
                if (widths.get(i)) {
                    sheet.setColumnWidth(i, widths.get(i) * 269)
                } else {
                    if (sheet.getColumnWidth(i) > 10000) {
                        sheet.setColumnWidth(i, 10000)
                    } else if (sheet.getColumnWidth(i) < 3000) {
                        sheet.setColumnWidth(i, 3000)
                    }
                }
            }
        }
    }

    /**
     * для Отчет Карманниковой: Отчет в разрезе платёжных поручений
     */
    class PaymentReportBuilder extends AbstractReportBuilder {

        List<String> header = ["КПП", "АСНУ", "Платёжное поручение", "Ставка", "Налог исчисленный",
                               "Налог удержанный", "Возврат", "Налог перечисленный", "Корректировка"]
        List<PaymentReportRow> rows

        PaymentReportBuilder(List<PaymentReportRow> rows) {
            super()
            this.rows = rows
            this.sheet = workbook.createSheet("Отчет")
        }

        void build() {
            fillHeader()
            createTableHeaders()
            createDataForTable()
            cellAlignment()
            flush()
        }

        protected void fillHeader() {
            createReportNameRow("Отчет в разрезе платёжных поручений")
            createYearRow()
            createPeriodRow()
            createFormTypeRow()
            createForNumRow()
            createDepartmentRow()
            createReportDateRow()
        }

        void createTableHeaders() {
            def style = new StyleBuilder(workbook).borders(true).wrapText(true).hAlign(CellStyle.ALIGN_CENTER).boldweight(Font.BOLDWEIGHT_BOLD).build()
            Row row = sheet.createRow(9)
            for (int colIndex = 0; colIndex < header.size(); colIndex++) {
                Cell cell = row.createCell(colIndex)
                cell.setCellValue(header.get(colIndex))
                cell.setCellStyle(style)
            }
        }

        void createDataForTable() {
            currentRowIndex = 9
            def styleBuilder = new StyleBuilder(workbook).borders(true).wrapText(true)
            def kppStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def asnuNameStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def paymentNumberStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def rateStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def calculatedTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def withholdingTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def refoundTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def taxSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def correctionStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            for (def dataRow : rows) {
                sheet.createRow(++currentRowIndex)
                int colIndex = 0
                createCell(colIndex, dataRow.kpp, kppStyle)
                createCell(++colIndex, dataRow.asnuName, asnuNameStyle)
                createCell(++colIndex, dataRow.paymentNumber, paymentNumberStyle)
                createCell(++colIndex, dataRow.rate, rateStyle)
                createCell(++colIndex, dataRow.calculatedTax, calculatedTaxStyle)
                createCell(++colIndex, dataRow.withholdingTax, withholdingTaxStyle)
                createCell(++colIndex, dataRow.refoundTax, refoundTaxStyle)
                createCell(++colIndex, dataRow.taxSum, taxSumStyle)
                createCell(++colIndex, dataRow.correction ? "корр." : "", correctionStyle)
            }
        }

        void cellAlignment() {
            Map<Integer, Integer> widths = [0: 16, 2: 16, 3: 16, 4: 16, 5: 16, 6: 16, 7: 16, 8: 16, 9: 16, 10: 16, 11: 16]
            for (int i = 0; i < header.size(); i++) {
                sheet.autoSizeColumn(i)
                if (widths.get(i)) {
                    sheet.setColumnWidth(i, widths.get(i) * 269)
                } else {
                    if (sheet.getColumnWidth(i) > 10000) {
                        sheet.setColumnWidth(i, 10000)
                    } else if (sheet.getColumnWidth(i) < 3000) {
                        sheet.setColumnWidth(i, 3000)
                    }
                }
            }
        }
    }

    /**
     * для Отчет Карманниковой: Отчет в разрезе платёжных поручений
     */
    class NdflDetailReportBuilder extends AbstractReportBuilder {

        List<String> header = ["КПП", "ОКТМО", "АСНУ", "Ставка", "Дата начисления дохода", "Дата выплаты дохода", "Дата налога", "Срок перечисления",
                               "Сумма начисленного дохода", "Сумма выплаченного дохода", "Сумма вычетов", "Налог исчисленный", "Налог удержанный",
                               "Возврат", "Долг за НП", "Долг за НА", "Налог перечисленный"]
        List<NdflDetailReportRow> rows

        NdflDetailReportBuilder(List<NdflDetailReportRow> rows) {
            super()
            this.rows = rows
            this.sheet = workbook.createSheet("Отчет")
        }

        void build() {
            fillHeader()
            createTableHeaders()
            createDataForTable()
            cellAlignment()
            flush()
        }

        protected void fillHeader() {
            createReportNameRow("Детализация - доходы, вычеты, налоги")
            createYearRow()
            createPeriodRow()
            createFormTypeRow()
            createForNumRow()
            createDepartmentRow()
            createReportDateRow()
        }

        void createTableHeaders() {
            def style = new StyleBuilder(workbook).borders(true).wrapText(true).hAlign(CellStyle.ALIGN_CENTER).boldweight(Font.BOLDWEIGHT_BOLD).build()
            Row row = sheet.createRow(9)
            for (int colIndex = 0; colIndex < header.size(); colIndex++) {
                Cell cell = row.createCell(colIndex)
                cell.setCellValue(header.get(colIndex))
                cell.setCellStyle(style)
            }
        }

        void createDataForTable() {
            currentRowIndex = 9
            def styleBuilder = new StyleBuilder(workbook).borders(true).wrapText(true)
            def kppStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def oktmoStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def asnuNameStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def rateStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def incomeAccruedDateStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(DATE).build()
            def incomePayoutDateStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(DATE).build()
            def taxDateStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(DATE).build()
            def taxTransferDateStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(DATE).build()
            def incomeAccruedSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER_2).build()
            def incomePayoutSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER_2).build()
            def totalDeductionsSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER_2).build()
            def calculatedTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def withholdingTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def refoundTaxStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def deptTaxPayerStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def deptAgentStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            def taxSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            for (def dataRow : rows) {
                sheet.createRow(++currentRowIndex)
                int colIndex = 0
                createCell(colIndex, dataRow.kpp, kppStyle)
                createCell(++colIndex, dataRow.oktmo, oktmoStyle)
                createCell(++colIndex, dataRow.asnuName, asnuNameStyle)
                createCell(++colIndex, dataRow.rate, rateStyle)
                createCell(++colIndex, dataRow.incomeAccruedDate, incomeAccruedDateStyle)
                createCell(++colIndex, dataRow.incomePayoutDate, incomePayoutDateStyle)
                createCell(++colIndex, dataRow.taxDate, taxDateStyle)
                createCell(++colIndex, dataRow.taxTransferDate, taxTransferDateStyle)
                createCell(++colIndex, dataRow.incomeAccruedSum, incomeAccruedSumStyle)
                createCell(++colIndex, dataRow.incomePayoutSum, incomePayoutSumStyle)
                createCell(++colIndex, dataRow.totalDeductionsSum, totalDeductionsSumStyle)
                createCell(++colIndex, dataRow.calculatedTax, calculatedTaxStyle)
                createCell(++colIndex, dataRow.withholdingTax, withholdingTaxStyle)
                createCell(++colIndex, dataRow.refoundTax, refoundTaxStyle)
                createCell(++colIndex, dataRow.deptTaxPayer, deptTaxPayerStyle)
                createCell(++colIndex, dataRow.deptAgent, deptAgentStyle)
                createCell(++colIndex, dataRow.taxSum, taxSumStyle)
            }
        }

        void cellAlignment() {
            Map<Integer, Integer> widths = [0: 16, 1: 18, 3: 10]
            (4..16).each { widths.put((int) it, 13) }
            for (int i = 0; i < header.size(); i++) {
                sheet.autoSizeColumn(i)
                if (widths.get(i)) {
                    sheet.setColumnWidth(i, widths.get(i) * 269)
                } else {
                    if (sheet.getColumnWidth(i) > 10000) {
                        sheet.setColumnWidth(i, 10000)
                    } else if (sheet.getColumnWidth(i) < 2000) {
                        sheet.setColumnWidth(i, 2000)
                    }
                }
            }
        }
    }

    abstract class AbstractReportBuilder {
        protected Workbook workbook
        protected Sheet sheet
        protected int currentRowIndex = 0
        protected CellStyle styleLeftHeader
        protected CellStyle styleNormal

        AbstractReportBuilder() {
            workbook = new SXSSFWorkbook()
            workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK)
            styleLeftHeader = new StyleBuilder(workbook).hAlign(CellStyle.ALIGN_RIGHT).boldweight(Font.BOLDWEIGHT_BOLD).build()
            styleNormal = new StyleBuilder(workbook).build()
        }

        void createReportNameRow(String name) {
            Row row = sheet.createRow(0)
            Cell cell = row.createCell(0)
            cell.setCellStyle(new StyleBuilder(workbook).hAlign(CellStyle.ALIGN_LEFT).boldweight(Font.BOLDWEIGHT_BOLD).fontHeight((short) 14).build())
            cell.setCellValue(name)
        }

        void createYearRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("Год:")
            cell = row.createCell(1)
            cell.setCellStyle(new StyleBuilder(workbook).dataFormat(NUMBER).build())
            cell.setCellValue(departmentReportPeriod.reportPeriod.taxPeriod.year)
        }

        void createPeriodRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("Период:")
            cell = row.createCell(1)
            cell.setCellStyle(styleNormal)
            cell.setCellValue(departmentReportPeriod.reportPeriod.name)
        }

        void createFormTypeRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("Тип формы:")
            cell = row.createCell(1)
            cell.setCellStyle(styleNormal)
            cell.setCellValue(declarationTemplate.getName())
        }

        void createForNumRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("№ формы:")
            cell = row.createCell(1)
            cell.setCellStyle(new StyleBuilder(workbook).dataFormat(NUMBER).build())
            cell.setCellValue(declarationData.id)
        }

        void createDepartmentRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("Подразделение:")
            cell = row.createCell(1)
            cell.setCellStyle(styleNormal)
            cell.setCellValue("/Банк/" + departmentService.getParentsHierarchyShortNames(declarationData.departmentId))
        }

        void createReportDateRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("Сформирован:")
            cell = row.createCell(1)
            cell.setCellStyle(new StyleBuilder(workbook).dataFormat(DATE_TIME).build())
            cell.setCellValue(date)
        }

        protected void createCell(int colIndex, Object value, CellStyle style) {
            Row row = sheet.getRow(currentRowIndex)
            Cell cell = row.createCell(colIndex)
            if (value != null) {
                if (value instanceof String) {
                    cell.setCellValue(value.toString())
                } else if (value instanceof BigDecimal) {
                    cell.setCellValue(((BigDecimal) value).doubleValue())
                } else if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue())
                } else if (value instanceof Date) {
                    def date = (Date) value
                    if (formatDate(date) == SharedConstants.DATE_ZERO_AS_DATE) {
                        cell.setCellValue(SharedConstants.DATE_ZERO_AS_STRING)
                    } else {
                        cell.setCellValue(date)
                    }
                }
            }
            cell.setCellStyle(style)
        }

        protected void flush() {
            OutputStream writer = null
            try {
                writer = scriptSpecificReportHolder.getFileOutputStream()
                workbook.write(writer)
            } finally {
                writer.close()
            }
        }
    }

    @Builder(builderStrategy = SimpleStrategy, prefix = "")
    @AutoClone
    class StyleBuilder {
        Workbook workbook
        boolean borders = false
        boolean wrapText = false
        short vAlign = CellStyle.VERTICAL_CENTER
        short hAlign = CellStyle.ALIGN_LEFT
        short boldweight = Font.BOLDWEIGHT_NORMAL
        short fontHeight = 11
        DataFormatEnum dataFormat = STRING

        StyleBuilder(Workbook workbook) {
            this.workbook = workbook
        }

        CellStyle build() {
            CellStyle style = workbook.createCellStyle()
            if (borders) {
                style.setBorderRight(CellStyle.BORDER_THIN)
                style.setBorderLeft(CellStyle.BORDER_THIN)
                style.setBorderBottom(CellStyle.BORDER_THIN)
                style.setBorderTop(CellStyle.BORDER_THIN)
            }
            if (dataFormat == STRING) {
                style.setDataFormat(workbook.createDataFormat().getFormat("@"))
            } else if (dataFormat == NUMBER) {
                style.setDataFormat((short) 1)
            } else if (dataFormat == NUMBER_2) {
                style.setDataFormat((short) 2)
            } else if (dataFormat == DATE) {
                style.setDataFormat((short) 14)
            } else if (dataFormat == DATE_TIME) {
                style.setDataFormat(workbook.createDataFormat().getFormat("dd.MM.yyyy hh:mm:ss"))
            }
            style.setAlignment(hAlign)
            style.setWrapText(wrapText)
            style.setVerticalAlignment(vAlign)
            Font font = workbook.createFont()
            font.setBoldweight(boldweight)
            font.setFontHeightInPoints(fontHeight)
            style.setFont(font)
            return style
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    enum DataFormatEnum {
        STRING, NUMBER, NUMBER_2, DATE, DATE_TIME
    }

    @EqualsAndHashCode
    class RateReportKey {
        String kpp
        long asnuId
        Integer rate

        RateReportKey(String kpp, long asnuId, Integer rate) {
            this.kpp = kpp
            this.asnuId = asnuId
            this.rate = rate
        }
    }

    class RateReportRow {
        @Delegate
        RateReportKey key
        String asnuName
        BigDecimal incomeAccruedSum = 0
        BigDecimal incomePayoutSum = 0
        BigDecimal totalDeductionsSum = 0
        BigDecimal calculatedTax = 0
        BigDecimal withholdingTax = 0
        Long refoundTax = 0
        BigDecimal notHoldingTax = 0
        BigDecimal overholdingTax = 0
        Long taxSum = 0

        BigDecimal getDeptTaxPayer() {
            return notHoldingTax > overholdingTax ? notHoldingTax - overholdingTax : 0
        }

        BigDecimal getDeptAgent() {
            return notHoldingTax < overholdingTax ? overholdingTax - notHoldingTax : 0
        }
    }

    @EqualsAndHashCode
    class PaymentReportKey {
        String kpp
        long asnuId
        String paymentNumber
        Integer rate
        boolean correction

        PaymentReportKey(String kpp, long asnuId, String paymentNumber, Integer rate, boolean correction) {
            this.kpp = kpp
            this.asnuId = asnuId
            this.paymentNumber = paymentNumber
            this.rate = rate
            this.correction = correction
        }

        @Override
        String toString() {
            return "$kpp, $asnuId, $paymentNumber, $rate, $correction"
        }
    }

    class PaymentReportRow {
        @Delegate
        PaymentReportKey key
        String asnuName
        BigDecimal calculatedTax = 0
        BigDecimal withholdingTax = 0
        Long refoundTax = 0
        BigDecimal taxSum = 0
        int order

        @Override
        String toString() {
            return "$order, $key, $asnuName"
        }
    }

    @EqualsAndHashCode
    class NdflDetailReportKey {
        String kpp
        String oktmo
        long asnuId
        Integer rate
        Date incomeAccruedDate
        Date incomePayoutDate
        Date taxDate
        Date taxTransferDate

        NdflDetailReportKey(String kpp, String oktmo, long asnuId, Integer rate, Date incomeAccruedDate, Date incomePayoutDate, Date taxDate, Date taxTransferDate) {
            this.kpp = kpp
            this.oktmo = oktmo
            this.asnuId = asnuId
            this.rate = rate
            this.incomeAccruedDate = incomeAccruedDate
            this.incomePayoutDate = incomePayoutDate
            this.taxDate = taxDate
            this.taxTransferDate = taxTransferDate
        }
    }

        @Override
        String toString() {
            return "$kpp, $oktmo, $asnuId, $rate, $incomeAccruedDate, $incomePayoutDate, $taxDate, $taxTransferDate"
        }
    }

    class NdflDetailReportRow {
        @Delegate
        NdflDetailReportKey key
        String asnuName
        BigDecimal incomeAccruedSum = 0
        BigDecimal incomePayoutSum = 0
        BigDecimal totalDeductionsSum = 0
        BigDecimal calculatedTax = 0
        BigDecimal withholdingTax = 0
        Long refoundTax = 0
        BigDecimal notHoldingTax = 0
        BigDecimal overholdingTax = 0
        BigDecimal taxSum = 0

        BigDecimal getDeptTaxPayer() {
            return notHoldingTax > overholdingTax ? notHoldingTax - overholdingTax : 0
        }

        BigDecimal getDeptAgent() {
            return notHoldingTax < overholdingTax ? overholdingTax - notHoldingTax : 0
        }

        Date getOrderDate() {
            return taxDate ?: taxTransferDate
        }

        int getRowType() {
            return incomeAccruedDate ? 100 : incomePayoutDate ? 200 : 300
        }

        @Override
        String toString() {
            return "$key, $asnuName, $incomeAccruedSum, $incomePayoutSum, $totalDeductionsSum, $calculatedTax ..."
        }
    }

    /**
     * Расширение {@link NdflPersonIncome}
     */
    class IncomeExt {
        @Delegate
        NdflPersonIncome delegate
        // Та же ставка, что в NdflPersonIncome, но заполненная для всех строк (в том числе для строк перечисления, где она изначально пустая)
        // Отдельно, т.к. нужно будет ещё знать изначальное значение
        Integer definedTaxRate
        // Является ли строка корректирующей
        boolean correction

        IncomeExt(NdflPersonIncome delegate) {
            this.delegate = delegate
        }

        @Override
        String toString() {
            return "{rowNum: $rowNum, taxRate: $taxRate, definedTaxRate: $definedTaxRate, paymentNumber: $paymentNumber, correction: $correction}"
        }
    }

    /**
     * Отчет Данные для включения в 2-НДФЛ и 6-НДФЛ
     */
    void create2_6NdflDataReport(type) {
        readParams()
        def incomes = ndflPersonService.findNdflPersonIncome(declarationData.id)
        if (kppList) {
            incomes = incomes.findAll { it.kpp in kppList }
        }
        Map<Pair<String, String>, List<NdflPersonIncome>> incomesByKppAndOkmto = incomes.groupBy {
            new Pair<String, String>(it.kpp, it.oktmo)
        }
        List<Ndfl2_6Row> rows = []
        String periodCode = refBookService.getRecordData(RefBook.Id.PERIOD_CODE.getId(), departmentReportPeriod.reportPeriod.dictTaxPeriodId)?.CODE?.stringValue
        incomesByKppAndOkmto.each { key, incomesOfKppAndOktmo ->
            def rowsOfKppAndOktmo = collectRowsFor2_6NdflDataReport(incomesOfKppAndOktmo)
            rows.addAll(rowsOfKppAndOktmo)
            for (def row : rowsOfKppAndOktmo) {
                row.periodCode = periodCode
                row.kpp = key.first
                row.oktmo = key.second
            }
        }
        rows.sort { def a, def b ->
            a.kpp <=> b.kpp ?: a.oktmo <=> b.oktmo ?: a.witholdingDate <=> b.witholdingDate
        }
        if (type == "xlsx") {
            new Ndfl2_6DataXlsxReportBuilder(rows).build()
        } else if (type == "txt") {
            new Ndfl2_6DataTxtReportBuilder(rows).build()
        }
    }

    void readParams() {
        def params = (Ndfl2_6DataReportParams) scriptSpecificReportHolder.getSubreportParam("params")
        dateFrom = params.dateFrom
        dateTo = params.dateTo
        adjustNegativeValues = params.adjustNegativeValues
        kppList = params.kppList?.toSet()
    }

    /**
     * Формирует строки для отчета "Данные для включения в 2-НДФЛ и 6-НДФЛ" из строк раздела-2 по определенной паре КПП-ОКТМО
     * !Код скопирован из скрипта report_6ndfl.groovy
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    List<Ndfl2_6Row> collectRowsFor2_6NdflDataReport(List<NdflPersonIncome> incomes) {
        List<Ndfl2_6Row> rows = []
        IncomeList incomeList = new IncomeList(incomes)

        def section2Block = new Section2Block(incomeList)
        if (declarationData.isAdjustNegativeValues()) {
            section2Block.adjustNegativeValues()
        }
        for (def section2Row : section2Block) {
            def row = new Ndfl2_6Row()
            row.incomeDate = section2Row.incomeDate
            row.witholdingDate = section2Row.taxDate
            row.taxTransferDate = section2Row.taxTransferDate
            row.incomeSum = section2Row.incomeSum
            row.withholdingTaxSum = section2Row.withholdingTaxSum
            rows.add(row)
        }
        return rows
    }

    @ToString
    class Ndfl2_6Row {
        // Код периода
        String periodCode
        // КПП
        String kpp
        // ОКТМО
        String oktmo
        // Дата фактического получения дохода
        Date incomeDate
        // Дата удержания налога
        Date witholdingDate
        // Срок перечисления налога
        Date taxTransferDate
        // Сумма фактически полученного дохода
        BigDecimal incomeSum
        // Сумма удержанного налога
        BigDecimal withholdingTaxSum
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
                        isBelongToSelectedPeriod(income.taxTransferDate) ||
                                isZeroDate(income.taxTransferDate) && isBelongToSelectedPeriod(income.taxDate)
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
         * Корректировка отрицательных значений
         */
        void adjustNegativeValues() {
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

    boolean isBelongToSelectedPeriod(Date date) {
        return date != null && (dateFrom <= date && date <= dateTo)
    }

    boolean isZeroDate(Date date) {
        return formatDate(date) == SharedConstants.DATE_ZERO_AS_DATE
    }

    /**
     * для Отчет Данные для включения в 2-НДФЛ и 6-НДФЛ
     */
    class Ndfl2_6DataXlsxReportBuilder extends AbstractReportBuilder {

        List<String> header = ["Код периода", "КПП", "ОКТМО", "Дата фактического получения дохода", "Дата удержания налога",
                               "Срок перечисления налога", "Сумма фактически полученного дохода", "Сумма удержанного налога"]
        List<Ndfl2_6Row> rows

        Ndfl2_6DataXlsxReportBuilder(List<Ndfl2_6Row> rows) {
            super()
            this.rows = rows
            this.sheet = workbook.createSheet("Отчет")
        }

        void build() {
            fillHeader()
            createTableHeaders()
            createDataForTable()
            cellAlignment()
            flush()
        }

        protected void fillHeader() {
            createReportNameRow("Данные для включения в 2-НДФЛ и 6-НДФЛ")
            createYearRow()
            createPeriodRow()
            createFormTypeRow()
            createForNumRow()
            createDepartmentRow()
            createDateFromRow()
            createDateToRow()
            createReportDateRow()
        }

        void createDateFromRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("Дата с:")
            cell = row.createCell(1)
            cell.setCellStyle(new StyleBuilder(workbook).dataFormat(DATE).build())
            cell.setCellValue(dateFrom)
        }

        void createDateToRow() {
            Row row = sheet.createRow(++currentRowIndex)
            Cell cell = row.createCell(0)
            cell.setCellStyle(styleLeftHeader)
            cell.setCellValue("Дата по:")
            cell = row.createCell(1)
            cell.setCellStyle(new StyleBuilder(workbook).dataFormat(DATE).build())
            cell.setCellValue(dateTo)
        }

        void createTableHeaders() {
            currentRowIndex = 11
            def style = new StyleBuilder(workbook).borders(true).wrapText(true).hAlign(CellStyle.ALIGN_CENTER).boldweight(Font.BOLDWEIGHT_BOLD).build()
            Row row = sheet.createRow(currentRowIndex)
            for (int colIndex = 0; colIndex < header.size(); colIndex++) {
                Cell cell = row.createCell(colIndex)
                cell.setCellValue(header.get(colIndex))
                cell.setCellStyle(style)
            }
        }

        void createDataForTable() {
            def styleBuilder = new StyleBuilder(workbook).borders(true).wrapText(true)
            def periodCodeStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(STRING).build()
            def kppStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def oktmoStyle = styleBuilder.hAlign(CellStyle.ALIGN_LEFT).dataFormat(STRING).build()
            def incomeDateStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(DATE).build()
            def witholdingDateStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(DATE).build()
            def taxTransferDateStyle = styleBuilder.hAlign(CellStyle.ALIGN_CENTER).dataFormat(DATE).build()
            def incomeSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER_2).build()
            def withholdingTaxSumStyle = styleBuilder.hAlign(CellStyle.ALIGN_RIGHT).dataFormat(NUMBER).build()
            for (def dataRow : rows) {
                sheet.createRow(++currentRowIndex)
                int colIndex = 0
                createCell(colIndex, dataRow.periodCode, periodCodeStyle)
                createCell(++colIndex, dataRow.kpp, kppStyle)
                createCell(++colIndex, dataRow.oktmo, oktmoStyle)
                createCell(++colIndex, dataRow.incomeDate, incomeDateStyle)
                createCell(++colIndex, dataRow.witholdingDate, witholdingDateStyle)
                createCell(++colIndex, isZeroDate(dataRow.taxTransferDate) ? DATE_ZERO_AS_STRING : formatDate(dataRow.taxTransferDate), taxTransferDateStyle)
                createCell(++colIndex, dataRow.incomeSum, incomeSumStyle)
                createCell(++colIndex, dataRow.withholdingTaxSum, withholdingTaxSumStyle)
            }
        }

        void cellAlignment() {
            Map<Integer, Integer> widths = [0: 16, 2: 16, 3: 16, 4: 16, 5: 16, 6: 16, 7: 16]
            for (int i = 0; i < header.size(); i++) {
                sheet.autoSizeColumn(i)
                if (widths.get(i)) {
                    sheet.setColumnWidth(i, widths.get(i) * 269)
                } else {
                    if (sheet.getColumnWidth(i) > 10000) {
                        sheet.setColumnWidth(i, 10000)
                    } else if (sheet.getColumnWidth(i) < 3000) {
                        sheet.setColumnWidth(i, 3000)
                    }
                }
            }
        }
    }

    class Ndfl2_6DataTxtReportBuilder {
        List<Ndfl2_6Row> rows
        OutputStream outputStream = scriptSpecificReportHolder.getFileOutputStream()
        DecimalFormat formatN_2

        Ndfl2_6DataTxtReportBuilder(List<Ndfl2_6Row> rows) {
            this.rows = rows
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault())
            symbols.setDecimalSeparator('.' as char)
            formatN_2 = new DecimalFormat("0.00", symbols)
        }

        void build() {
            if (rows) {
                for (def row : rows) {
                    StringBuilder stringBuilder = new StringBuilder()
                    stringBuilder << row.periodCode << "|"
                    stringBuilder << row.kpp << "|"
                    stringBuilder << row.oktmo << "|"
                    stringBuilder << format(row.incomeDate) << "|"
                    stringBuilder << format(row.witholdingDate) << "|"
                    stringBuilder << (format(row.taxTransferDate) == SharedConstants.DATE_ZERO_AS_DATE ? SharedConstants.DATE_ZERO_AS_STRING : format(row.taxTransferDate)) << "|"
                    stringBuilder << formatN_2.format(row.incomeSum) << "|"
                    stringBuilder << row.withholdingTaxSum << "|"
                    stringBuilder << "\r\n"
                    write(stringBuilder.toString())
                }
            } else {
                write("\r\n")
            }
        }

        String format(Date date) {
            return date?.format(SharedConstants.DATE_FORMAT) ?: ""
        }

        void write(String string) {
            IOUtils.write(string, outputStream, "IBM866")
        }
    }

    /**
     * Выгрузить данные РНУ НДФЛ в Excel
     */
    void exportDeclarationDataToExcel() {

        SheetFillerContext context = createExportDeclarationDataSheetFillerContext()

        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(scriptSpecificReportHolder.fileInputStream)

        new ExportDeclarationDataSheetFiller().fillSheet(xssfWorkbook, context)
        OutputStream writer = scriptSpecificReportHolder.getFileOutputStream()
        sxssfWorkbook.write(writer)
        scriptSpecificReportHolder.setFileName(createExportDeclarationDataToExcelFileName())
    }

    /**
     * Сформировать название файла для выгрузки данных РНУ НДФЛ в Excel
     * @return имя файла
     */
    String createExportDeclarationDataToExcelFileName() {
        Department department = departmentService.get(declarationData.getDepartmentId())
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())
        String asnuName = ""
        if (declarationData.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId())
            asnuName = asnuProvider.getRecordData(declarationData.getAsnuId()).get("NAME").getStringValue()
        }
        return String.format("ТФ_%s_%s_%s_%s.xlsx",
                declarationData.getId(),
                departmentReportPeriodService.formatPeriodName(reportPeriod, SharedConstants.DATE_FORMAT),
                department.getCode(),
                asnuName)
    }

    /**
     * Создает и инициирует объект {@link SheetFillerContext} данными для выгрузки РНУ НДФЛ в Excel
     * @return
     */
    SheetFillerContext createExportDeclarationDataSheetFillerContext() {
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
        Collections.sort(ndflPersonList, new Comparator<NdflPerson>() {
            @Override
            int compare(NdflPerson o1, NdflPerson o2) {
                return o1.rowNum <=> o2.rowNum
            }
        })
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
        List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
        SheetFillerContext context = new SheetFillerContext(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            context.getIdNdflPersonMap().get(ndflPersonIncome.ndflPersonId).incomes << ndflPersonIncome
        }
        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {
            context.getIdNdflPersonMap().get(ndflPersonDeduction.ndflPersonId).deductions << ndflPersonDeduction
        }
        for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {
            context.getIdNdflPersonMap().get(ndflPersonPrepayment.ndflPersonId).prepayments << ndflPersonPrepayment
        }
        return context
    }

    //------------------ Import Data ----------------------
    @TypeChecked(TypeCheckingMode.SKIP)
    void importData() {

        String dateFormat = SharedConstants.DATE_FORMAT

        logForDebug("Начало загрузки данных первичной налоговой формы ${declarationData.id}. " +
                "Дата начала отчетного периода: ${getReportPeriodStartDate().format(dateFormat)}, " +
                "дата окончания: ${getReportPeriodEndDate().format(dateFormat)}")

        formCreationDate = logBusinessService.getFormCreationDate(declarationData.id)

        // Начинаем парсить файл
        File xmlTF = dataFile
        if (xmlTF == null) {
            throw new ServiceException("Отсутствует значение параметра dataFile!")
        }
        def Файл = new LineNumberingSlurper().parse(xmlTF)

        // Проверка соответствия атрибута ДатаОтч периоду в наименовании файла
        // reportPeriodEndDate получен на основании периода из имени файла
        String reportDate = Файл.СлЧасть.'@ДатаОтч'
        String reportPeriodEndDate = getReportPeriodEndDate().format(dateFormat)
        if (reportPeriodEndDate != reportDate) {
            logger.warn("В ТФ неверно указана «Отчетная дата»: «${reportDate}». Должна быть указана дата окончания периода ТФ, равная «${reportPeriodEndDate}»")
        }

        // Обработка "Информационных частей" файла, каждая из которых отвечает за 1 физлицо

        // Номер итерации = номер физлица в файле
        def personCounter = 1
        // Кол-во добавленных в ПНФ физлиц
        def successfulCount = 0

        for (infoPart in Файл.ИнфЧасть) {
            ScriptUtils.checkInterrupted()
            if (processInfoPart(infoPart, personCounter)) {
                successfulCount++
            }
            personCounter++
        }

        // Сортировка сформированных из файла строк ПНФ

        ndflPersonService.fillNdflPersonIncomeSortFields(ndflPersonCache)

        Collections.sort(ndflPersonCache, NdflPerson.getComparator())
        Long personRowNum = 0L
        BigDecimal incomeRowNum = BigDecimal.ZERO
        BigDecimal deductionRowNum = BigDecimal.ZERO
        BigDecimal prepaymentRowNum = BigDecimal.ZERO

        for (NdflPerson ndflPerson : ndflPersonCache) {
            Collections.sort(ndflPerson.incomes, NdflPersonIncome.getComparator())
            Collections.sort(ndflPerson.deductions, NdflPersonDeduction.getComparator(ndflPerson))
            Collections.sort(ndflPerson.prepayments, NdflPersonPrepayment.getComparator(ndflPerson))

            for (NdflPersonIncome income : ndflPerson.incomes) {
                incomeRowNum = incomeRowNum.add(BigDecimal.ONE)
                income.rowNum = incomeRowNum
            }

            for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                deductionRowNum = deductionRowNum.add(BigDecimal.ONE)
                deduction.rowNum = deductionRowNum
            }

            for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                prepaymentRowNum = prepaymentRowNum.add(BigDecimal.ONE)
                prepayment.rowNum = prepaymentRowNum
            }

            ndflPerson.rowNum = ++personRowNum
        }
        ndflPersonService.save(ndflPersonCache)
        if (successfulCount == 0) {
            logger.error("В ТФ отсутствуют операции, принадлежащие отчетному периоду.")
            logger.error("Налоговая форма не создана.")
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    boolean processInfoPart(infoPart, Integer rowNum) {

        // Раздел физлица "Получатель дохода"

        NodeChild ndflPersonNode = infoPart.'ПолучДох'[0]
        // номер строки в файле, был проставлен в ходе парсинга файла
        String personLineInFile = ndflPersonNode.'@line'

        NdflPerson ndflPerson = transformNdflPersonNode(ndflPersonNode)
        // Проверяется формат заполнения некоторых полей, просто печатает предупреждения.
        checkNdflPerson(ndflPerson, personLineInFile)

        // Раздел операций "Сведения об операциях"

        // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        Map<Long, Map<String, RefBookValue>> incomeCodeMap = getRefIncomeCode()
        // Коды видов вычетов
        List<String> deductionTypeList = getRefDeductionType()

        List<NdflPersonIncomeExt> incomes = new ArrayList<>()

        List<BigDecimal> operInfoIdList = ndflPersonService.generateOperInfoIds(infoPart.'СведОпер'.size())

        infoPart.'СведОпер'.eachWithIndex { NodeChild operationInfo, int index ->
            BigDecimal operInfoId = operInfoIdList.get(index)
            incomes.addAll(operationInfo.'СведДохНал'.collect {
                NodeChild incomeInfo -> transformNdflPersonIncome(incomeInfo, ndflPerson, incomeCodeMap, operInfoId, ndflPerson.inp)
            })
            ndflPerson.deductions.addAll(operationInfo.'СведВыч'.collect {
                NodeChild deductionInfo -> transformNdflPersonDeduction(deductionInfo, ndflPerson, deductionTypeList, operInfoId, ndflPerson.inp)
            })
            ndflPerson.prepayments.addAll(operationInfo.'СведАванс'.collect {
                NodeChild prepaymentInfo -> transformNdflPersonPrepayment(prepaymentInfo, operInfoId, ndflPerson.inp)
            })
        }

        // Фильтруем сведения о доходах, не попадающие в отчетный период
        ndflPerson.incomes.addAll(filterIncomesByDates(incomes, ndflPerson))
        ndflPerson.deductions.removeAll {
            NdflPersonDeduction deduction -> !ndflPerson.incomes.operationId.contains(deduction.operationId)
        }

        ndflPerson.prepayments.removeAll {
            NdflPersonPrepayment prepayment -> !ndflPerson.incomes.operationId.contains(prepayment.operationId)
        }
        // Если сведения о доходах остались, добавляем сведения о физлице в форму
        if (ndflPerson.incomes) {
            //Идентификатор декларации для которой загружаются данные
            ndflPerson.declarationDataId = declarationData.getId()
            ndflPerson.rowNum = rowNum
            ndflPersonCache.add(ndflPerson)
            return true
        } else {
            logger.warn("У ФЛ ($ndflPerson.fullName, ИНП: $ndflPerson.inp) отсутствуют операции, принадлежащие отчетному периоду. ФЛ не загружено в налоговую форму.")
            return false
        }
    }

    /**
     * Проверка данных физлица.
     * @param personLineInFile номер строки в файле, где содержались данные о физлице
     */
    boolean checkNdflPerson(NdflPerson person, String personLineInFile) {
        String emptyParamErrorMsg = "Строка: ${personLineInFile}. ФЛ: $person.fullName, ИНП: $person.inp. Значение параметра %s не указано."
        String valueNotFound = "Строка: ${personLineInFile}. ФЛ: $person.fullName, ИНП: $person.inp. Параметр \"%s\" (%s) содержит значение (%s), которое не найдено в справочнике %s."
        String msgObject = "$person.fullName, ИНП: $person.inp"
        // Проверка поля "Гражданство"
        boolean valid = true
        if (isEmpty(person.citizenship)) {
            logger.warnExp(emptyParamErrorMsg, '', msgObject, '"Код гражданства" (Гражд)')
            valid = false
        } else {
            boolean exists = refBookService.existsCountryByCode(person.citizenship)
            if (!exists) {
                logger.warnExp(valueNotFound, '', msgObject, "Гражд", "Код гражданства", person.citizenship, "ОКСМ")
                valid = false
            }
        }
        // Проверка поля "Код ДУЛ"
        boolean isDocTypeValid = true
        if (isEmpty(person.idDocType)) {
            logger.warnExp(emptyParamErrorMsg, '', msgObject, '"Код вида документа, удостоверяющего личность" (УдЛичнФЛКод)')
            valid = false
            isDocTypeValid = false
        } else {
            boolean exists = refBookService.existsDocTypeByCode(person.idDocType)
            if (!exists) {
                logger.warnExp(valueNotFound, '', msgObject, "УдЛичнФЛКод", "Код вида документа, удостоверяющего личность", person.idDocType, "Коды документов, удостоверяющих личность")
                valid = false
                isDocTypeValid = false
            }
        }
        // Проверка поля "Номер ДУЛ"
        if (isEmpty(person.idDocNumber)) {
            logger.warnExp(emptyParamErrorMsg, '', msgObject, '"Серия и номер документа, удостоверяющего личность" (УдЛичнФЛНом)')
            valid = false
        } else if (isDocTypeValid) {
            String errorMessage = ScriptUtils.checkDul(person.idDocType, person.idDocNumber, "УдЛичнФЛНом")
            if (errorMessage != null) {
                logger.warnExp("Строка: ${personLineInFile}. ФЛ: $person.fullName, ИНП: $person.inp. ${errorMessage}.", '', msgObject)
                valid = false
            }
        }
        // Проверка поля "Статус НП"
        if (isEmpty(person.status)) {
            logger.warnExp(emptyParamErrorMsg, '', msgObject, '"Статус налогоплательщика" (СтатусФЛ)')
            valid = false
        } else {
            boolean exists = refBookService.existsTaxpayerStateByCode(person.status)
            if (!exists) {
                logger.warnExp(valueNotFound, '', msgObject, "СтатусФЛ", "Статус налогоплательщика", person.status, "Статусы налогоплательщиков")
                valid = false
            }
        }

        return valid
    }

    /**
     * Проверка на принадлежность операций физлица периоду.
     */
    List<NdflPersonIncome> filterIncomesByDates(List<NdflPersonIncomeExt> incomes, NdflPerson person) {
        if (!incomes) return []

        List<NdflPersonIncome> result = []

        Map<String, List<NdflPersonIncomeExt>> incomesGroupedByOperationId = incomes.groupBy { it.operationId }
        for (List<NdflPersonIncomeExt> incomeGroup : incomesGroupedByOperationId.values()) {
            if (isIncomeGroupInPeriod(incomeGroup, person)) {
                result.addAll(incomeGroup)
            }
        }
        return result
    }

    /**
     * Проверка вхождения записей о доходах ФЛ в отчетный период.
     */
    boolean isIncomeGroupInPeriod(List<NdflPersonIncomeExt> incomeGroup, NdflPerson person) {

        List<NdflPersonIncomeExt> incomesWithAccruedDate = incomeGroup.findAll { it.incomeAccruedDate }
        boolean isAnyAccruedDateInPeriod = incomesWithAccruedDate.any { isDateInReportPeriod(it.incomeAccruedDate) }
        if (isAnyAccruedDateInPeriod) return true

        List<NdflPersonIncomeExt> incomesWithPayoutDate = incomeGroup.findAll { it.incomePayoutDate }
        boolean isAnyPayoutDateInPeriod = incomesWithPayoutDate.any { isDateInReportPeriod(it.incomePayoutDate) }
        if (isAnyPayoutDateInPeriod) return true

        List<NdflPersonIncomeExt> incomesWithTaxDate = incomeGroup.findAll { it.taxDate }
        boolean isAnyTaxDateInPeriod = incomesWithTaxDate.any { isDateInReportPeriod(it.taxDate) }
        if (isAnyTaxDateInPeriod) return true

        // Если не удалось найти ни одной даты в периоде, печатаем информацию обо всех проверенных.
        incomesWithAccruedDate.each { income ->
            logIncomeDatesWarning(income, income.incomeAccruedDate, person, "ДатаДохНач", "Дата начисления дохода")
        }
        incomesWithPayoutDate.each { income ->
            logIncomeDatesWarning(income, income.incomePayoutDate, person, "ДатаДохВыпл", "Дата выплаты дохода")
        }
        incomesWithTaxDate.each { income ->
            logIncomeDatesWarning(income, income.taxDate, person, "ДатаНалог", "Дата НДФЛ")
        }

        // Если хотя бы одна дата была, валидация не пройдена
        if (incomesWithAccruedDate || incomesWithPayoutDate || incomesWithTaxDate) {
            return false
        }

        // Если все даты выше пустые, но есть хотя бы одна дата платежного поручения, тоже берём строки
        List<NdflPersonIncomeExt> incomesWithPaymentDate = incomeGroup.findAll { it.paymentDate }
        boolean isAnyPaymentDateInPeriod = incomesWithPaymentDate.any { isDateInReportPeriod(it.paymentDate) }
        if (isAnyPaymentDateInPeriod) return true

        incomesWithPaymentDate.each { income ->
            logIncomeDatesWarning(income, income.paymentDate, person, "ПлПоручДат", "Дата платёжного поручения")
        }

        return false
    }

    // Проверка принадлежности даты к периоду формы
    boolean isDateInReportPeriod(Date date) {
        return (date >= getReportPeriodCalendarStartDate() && date <= getReportPeriodEndDate())
    }

    void logIncomeDatesWarning(NdflPersonIncomeExt income, Date date, NdflPerson person, String xmlParam, String explicitParamName) {
        String warningMessage = "Строка: ${income.fileLine}. " +
                "Значение параметра \"${xmlParam}\" (${explicitParamName}) : ${ScriptUtils.formatDate(date)} не входит в ${reportPeriodFullName}. " +
                "Операция (\"${income.operationId}\") не загружена в налоговую форму. ФЛ: $person.fullName, ИНП: $person.inp."
        logger.warnExp(warningMessage, "Даты операций не входят в период", "")
    }

    String getReportPeriodFullName() {
        ReportPeriodType periodType = reportPeriodService.getReportPeriodTypeById(reportPeriod.dictTaxPeriodId)
        String code = periodType.code
        String title = (code == "21" || code == "51") ? "отчетный период" : "последний квартал отчетного периода"
        return "${title}: $reportPeriod.taxPeriod.year, $reportPeriod.name"
    }

    class NdflPersonIncomeExt extends NdflPersonIncome {
        String fileLine
    }

    NdflPerson transformNdflPersonNode(NodeChild node) {
        NdflPerson ndflPerson = new NdflPerson()
        ndflPerson.inp = toString((GPathResult) node.getProperty('@ИНП'))
        ndflPerson.snils = toString((GPathResult) node.getProperty('@СНИЛС'))
        ndflPerson.lastName = toString((GPathResult) node.getProperty('@ФамФЛ'))
        ndflPerson.firstName = toString((GPathResult) node.getProperty('@ИмяФЛ'))
        ndflPerson.middleName = toString((GPathResult) node.getProperty('@ОтчФЛ'))
        ndflPerson.birthDay = toDate((GPathResult) node.getProperty('@ДатаРожд'))
        ndflPerson.citizenship = toString((GPathResult) node.getProperty('@Гражд'))
        ndflPerson.innNp = toString((GPathResult) node.getProperty('@ИННФЛ'))
        ndflPerson.innForeign = toString((GPathResult) node.getProperty('@ИННИно'))
        ndflPerson.idDocType = toString((GPathResult) node.getProperty('@УдЛичнФЛКод'))
        ndflPerson.idDocNumber = toString((GPathResult) node.getProperty('@УдЛичнФЛНом'))
        ndflPerson.status = toString((GPathResult) node.getProperty('@СтатусФЛ'))
        ndflPerson.postIndex = toString((GPathResult) node.getProperty('@Индекс'))
        ndflPerson.regionCode = toString((GPathResult) node.getProperty('@КодРегион'))
        ndflPerson.area = toString((GPathResult) node.getProperty('@Район'))
        ndflPerson.city = toString((GPathResult) node.getProperty('@Город'))
        ndflPerson.locality = toString((GPathResult) node.getProperty('@НаселПункт'))
        ndflPerson.street = toString((GPathResult) node.getProperty('@Улица'))
        ndflPerson.house = toString((GPathResult) node.getProperty('@Дом'))
        ndflPerson.building = toString((GPathResult) node.getProperty('@Корпус'))
        ndflPerson.flat = toString((GPathResult) node.getProperty('@Кварт'))
        ndflPerson.countryCode = toString((GPathResult) node.getProperty('@КодСтрИно'))
        ndflPerson.address = toString((GPathResult) node.getProperty('@АдресИно'))
        ndflPerson.additionalData = toString((GPathResult) node.getProperty('@ДопИнф'))
        ndflPerson.asnuId = declarationData.asnuId
        ndflPerson.modifiedDate = formCreationDate
        return ndflPerson
    }

    NdflPersonIncomeExt transformNdflPersonIncome(NodeChild node, NdflPerson ndflPerson, Map<Long, Map<String, RefBookValue>> incomeCodeMap, BigDecimal operationInfoId, String inp) {
        def operationNode = node.parent()

        NdflPersonIncomeExt personIncome = new NdflPersonIncomeExt()
        personIncome.fileLine = toString((GPathResult) node.getProperty('@line'))

        personIncome.rowNum = toBigDecimal((GPathResult) node.getProperty('@НомСтр'))
        personIncome.incomeCode = toString((GPathResult) node.getProperty('@КодДох'))
        personIncome.incomeType = toString((GPathResult) node.getProperty('@ТипДох'))

        String kpp = toString((GPathResult) operationNode.getProperty('@КПП'))
        String oktmo = toString((GPathResult) operationNode.getProperty('@ОКТМО'))
        personIncome.operationId = generateOperationId(inp, toString((GPathResult) operationNode.getProperty('@ИдОпер')),
                kpp, oktmo, operationInfoId)
        personIncome.oktmo = oktmo
        personIncome.kpp = kpp

        personIncome.incomeAccruedDate = toDate((GPathResult) node.getProperty('@ДатаДохНач'))
        personIncome.incomePayoutDate = toDate((GPathResult) node.getProperty('@ДатаДохВыпл'))
        personIncome.incomeAccruedSumm = toBigDecimal((GPathResult) node.getProperty('@СуммДохНач'))
        personIncome.incomePayoutSumm = toBigDecimal((GPathResult) node.getProperty('@СуммДохВыпл'))
        personIncome.totalDeductionsSumm = toBigDecimal((GPathResult) node.getProperty('@СумВыч'))
        personIncome.taxBase = toBigDecimal((GPathResult) node.getProperty('@НалБаза'))
        personIncome.taxRate = toInteger((GPathResult) node.getProperty('@Ставка'))
        personIncome.taxDate = toDate((GPathResult) node.getProperty('@ДатаНалог'))
        personIncome.calculatedTax = toBigDecimal((GPathResult) node.getProperty('@НИ'))
        personIncome.withholdingTax = toBigDecimal((GPathResult) node.getProperty('@НУ'))
        personIncome.notHoldingTax = toBigDecimal((GPathResult) node.getProperty('@ДолгНП'))
        personIncome.overholdingTax = toBigDecimal((GPathResult) node.getProperty('@ДолгНА'))
        personIncome.refoundTax = toLong((GPathResult) node.getProperty('@ВозврНал'))
        GPathResult taxTransferDateRawValue = (GPathResult) node.getProperty('@СрокПрчслНал')
        if (toString(taxTransferDateRawValue) == SharedConstants.DATE_ZERO_AS_STRING) {
            personIncome.taxTransferDate = Date.parse(SharedConstants.DATE_FORMAT, SharedConstants.DATE_ZERO_AS_DATE)
        } else {
            personIncome.taxTransferDate = toDate(taxTransferDateRawValue)
        }
        personIncome.paymentDate = toDate((GPathResult) node.getProperty('@ПлПоручДат'))
        personIncome.paymentNumber = toString((GPathResult) node.getProperty('@ПлатПоручНом'))
        personIncome.taxSumm = toBigDecimal((GPathResult) node.getProperty('@НалПерСумм'))
        personIncome.asnuId = declarationData.asnuId
        personIncome.modifiedDate = formCreationDate
        personIncome.setOperInfoId(operationInfoId)

        // Спр5 Код вида дохода (Необязательное поле)
        if (personIncome.incomeCode != null && personIncome.incomeAccruedDate != null && !incomeCodeMap.find { key, value ->
            value.CODE?.stringValue == personIncome.incomeCode &&
                    personIncome.incomeAccruedDate >= value.record_version_from?.dateValue &&
                    personIncome.incomeAccruedDate <= value.record_version_to?.dateValue
        }) {
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPerson.fullName, ndflPerson.inp, personIncome.operationId])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_INCOME_CODE, personIncome.incomeCode ?: "",
                    R_INCOME_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, personIncome.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInpAndOperId, pathError,
                    errMsg)
        }

        return personIncome
    }

    NdflPersonDeduction transformNdflPersonDeduction(NodeChild node, NdflPerson ndflPerson, List<String> deductionTypeList, BigDecimal operationInfoId, String inp) {
        def operationNode = node.parent()

        NdflPersonDeduction personDeduction = new NdflPersonDeduction()
        personDeduction.rowNum = toBigDecimal((GPathResult) node.getProperty('@НомСтр'))

        String kpp = toString((GPathResult) operationNode.getProperty('@КПП'))
        String oktmo = toString((GPathResult) operationNode.getProperty('@ОКТМО'))
        personDeduction.operationId = generateOperationId(inp, toString((GPathResult) operationNode.getProperty('@ИдОпер')),
                kpp, oktmo, operationInfoId)

        personDeduction.typeCode = toString((GPathResult) node.getProperty('@ВычетКод'))
        personDeduction.notifType = toString((GPathResult) node.getProperty('@УведТип'))
        personDeduction.notifDate = toDate((GPathResult) node.getProperty('@УведДата'))
        personDeduction.notifNum = toString((GPathResult) node.getProperty('@УведНом'))
        personDeduction.notifSource = toString((GPathResult) node.getProperty('@УведИФНС'))
        personDeduction.notifSumm = toBigDecimal((GPathResult) node.getProperty('@УведСум'))
        personDeduction.incomeAccrued = toDate((GPathResult) node.getProperty('@ДатаДохНач'))
        personDeduction.incomeCode = toString((GPathResult) node.getProperty('@КодДох'))
        personDeduction.incomeSumm = toBigDecimal((GPathResult) node.getProperty('@СуммДохНач'))
        personDeduction.periodPrevDate = toDate((GPathResult) node.getProperty('@ДатаПредВыч'))
        personDeduction.periodPrevSumm = toBigDecimal((GPathResult) node.getProperty('@СумПредВыч'))
        personDeduction.periodCurrDate = toDate((GPathResult) node.getProperty('@ДатаТекВыч'))
        personDeduction.periodCurrSumm = toBigDecimal((GPathResult) node.getProperty('@СумТекВыч'))
        personDeduction.asnuId = declarationData.asnuId
        personDeduction.modifiedDate = formCreationDate
        personDeduction.setOperInfoId(operationInfoId)
        personDeduction.oktmo = toString((GPathResult) operationNode.getProperty('@ОКТМО'))
        personDeduction.kpp = toString((GPathResult) operationNode.getProperty('@КПП'))

        if (!deductionTypeList.contains(personDeduction.typeCode)) {
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPerson.fullName, ndflPerson.inp, personDeduction.operationId])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_TYPE_CODE, personDeduction.typeCode ?: "",
                    R_TYPE_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, personDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInpAndOperId, pathError,
                    errMsg)
        }

        return personDeduction
    }

    NdflPersonPrepayment transformNdflPersonPrepayment(NodeChild node, BigDecimal operationInfoId, String inp) {
        def operationNode = node.parent()

        NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment()
        personPrepayment.rowNum = toBigDecimal((GPathResult) node.getProperty('@НомСтр'))

        String kpp = toString((GPathResult) operationNode.getProperty('@КПП'))
        String oktmo = toString((GPathResult) operationNode.getProperty('@ОКТМО'))
        personPrepayment.operationId = generateOperationId(inp, toString((GPathResult) operationNode.getProperty('@ИдОпер')),
                kpp, oktmo, operationInfoId)

        personPrepayment.summ = toBigDecimal((GPathResult) node.getProperty('@Аванс'))
        personPrepayment.notifNum = toString((GPathResult) node.getProperty('@УведНом'))
        personPrepayment.notifDate = toDate((GPathResult) node.getProperty('@УведДата'))
        personPrepayment.notifSource = toString((GPathResult) node.getProperty('@УведИФНС'))
        personPrepayment.asnuId = declarationData.asnuId
        personPrepayment.modifiedDate = formCreationDate
        personPrepayment.setOperInfoId(operationInfoId)
        personPrepayment.oktmo = toString((GPathResult) operationNode.getProperty('@ОКТМО'))
        personPrepayment.kpp = toString((GPathResult) operationNode.getProperty('@КПП'))

        return personPrepayment
    }

    String generateOperationId(String inp, String operationId, String kpp, String oktmo, BigDecimal operInfoId) {
        String result = operationId
        if ((declarationData.asnuId == 1L || declarationData.asnuId == 13L || declarationData.asnuId == 16L) && operationId != "0" && operationId.length() <= 10) {
            StringBuilder newOperationId = new StringBuilder()
            newOperationId
                    .append(inp)
                    .append("_")
                    .append(operationId)
                    .append("_")
                    .append(kpp)
                    .append("_")
                    .append(oktmo)
                    .append("_")
                    .append(operInfoId)
            result = newOperationId.toString()
        }
        return result
    }

    Integer toInteger(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? Integer.valueOf(xmlNode.text()) : null
            } catch (NumberFormatException ignored) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null
        }
    }

    Long toLong(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? Long.valueOf(xmlNode.text()) : null
            } catch (NumberFormatException ignored) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null
        }
    }

    BigDecimal toBigDecimal(GPathResult xmlNode) throws NumberFormatException {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? new BigDecimal(xmlNode.text()) : null
            } catch (NumberFormatException ignored) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null
        }
    }

    Date toDate(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            SimpleDateFormat format = new SimpleDateFormat(SharedConstants.DATE_FORMAT)
            if (xmlNode.text() != null && !xmlNode.text().isEmpty()) {
                Date date = format.parse(xmlNode.text())
                if (format.format(date) != xmlNode.text()) {
                    throw new ServiceException("Значения атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не существует.")
                }
                return date
            } else {
                return null
            }
        } else {
            return null
        }
    }

    String toString(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            return xmlNode.text() != null && !xmlNode.text().isEmpty() ? StringUtils.cleanString(xmlNode.text()) : null
        } else {
            return null
        }
    }

    String formatDate(date) {
        return ScriptUtils.formatDate((Date) date, SharedConstants.DATE_FORMAT)
    }

    //>------------------< REF BOOK >----------------------<

    // Дата начала отчетного периода
    Date periodStartDate = null
    // "Календарная дата начала отчетного периода" - начало последнего квартала отчетного периода.
    Date periodCalendarStartDate = null
    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Кэш провайдеров справочников
    Map<Long, RefBookDataProvider> providerCache = [:]

    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"

    // Коды видов вычетов
    List<String> deductionTypeCache = []

    /**
     * Данные о периоде из справочника
     */
    Map<String, RefBookValue> getReportPeriodFromRefBook() {
        def refBookPeriod = getProvider(RefBook.Id.PERIOD_CODE.id)
        return refBookPeriod.getRecordData(reportPeriod.dictTaxPeriodId)
    }

    /**
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodStartDate() {
        def periodInfo = getReportPeriodFromRefBook()
        if (periodInfo) {
            return toReportPeriodDate(periodInfo.START_DATE.dateValue)
        } else if (!periodStartDate) {
            periodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
        }
        return periodStartDate
    }

    /**
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodCalendarStartDate() {
        def periodInfo = getReportPeriodFromRefBook()
        if (periodInfo) {
            return toReportPeriodDate(periodInfo.CALENDAR_START_DATE.dateValue)
        } else if (periodCalendarStartDate == null) {
            periodCalendarStartDate = reportPeriodService.getCalendarStartDate(declarationData.reportPeriodId)?.time
        }
        return periodCalendarStartDate
    }

    /**
     * Получить дату окончания отчетного периода
     * @return
     */
    Date getReportPeriodEndDate() {
        def periodInfo = getReportPeriodFromRefBook()
        if (periodInfo) {
            return toReportPeriodDate(periodInfo.END_DATE.dateValue)
        } else if (periodEndDate == null) {
            periodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
        }
        return periodEndDate
    }

    Date toReportPeriodDate(Date refDate) {
        Calendar date = Calendar.getInstance();
        date.setTime(refDate)
        date[Calendar.YEAR] = reportPeriod.taxPeriod.year
        return date.getTime()
    }

    /**
     * Получить "Коды видов доходов"
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRefIncomeCode() {
        // Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        Map<Long, Map<String, RefBookValue>> mapResult = [:]
        PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.INCOME_CODE.id)
        refBookMap.each { Map<String, RefBookValue> refBook ->
            mapResult.put((Long) refBook?.id?.numberValue, refBook)
        }
        return mapResult
    }

    /**
     * Получить "Коды видов вычетов"
     * @return
     */
    List<String> getRefDeductionType() {
        if (deductionTypeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.DEDUCTION_TYPE.id)
            refBookList.each { Map<String, RefBookValue> refBook ->
                deductionTypeCache.add(refBook?.CODE?.stringValue)
            }
        }
        return deductionTypeCache
    }

    /**
     * Получить записи справочника по его идентификатору в отчётном периоде
     * @param refBookId - идентификатор справочника
     * @return - список записей справочника
     */
    PagingResult<Map<String, RefBookValue>> getRefBook(long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        PagingResult<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecordsVersion(getReportPeriodStartDate(), getReportPeriodEndDate(), null, null)
        if (refBookList == null) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
    }

    /**
     * Получение провайдера с использованием кеширования.
     * @param providerId
     * @return
     */
    RefBookDataProvider getProvider(long providerId) {
        if (!providerCache.containsKey(providerId)) {
            providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
        }
        return providerCache.get(providerId)
    }

    //>------------------< UTILS >----------------------<


    final String T_PERSON_INCOME = "2" // "Сведения о доходах и НДФЛ"
    final String T_PERSON_DEDUCTION = "3" // "Сведения о вычетах"
    final String C_INCOME_ACCRUED_DATE = "Дата начисления дохода" //"Доход.Дата.Начисление"
    final String C_INCOME_PAYOUT_DATE = "Дата выплаты дохода" //"Доход.Дата.Выплата"
    final String C_TAX_DATE = "Дата НДФЛ" //"НДФЛ.Расчет.Дата"
    final String C_INCOME_CODE = "Код дохода" //"Доход.Вид.Код"\
    final String C_TYPE_CODE = "Код вычета" //" Код вычета"
    final String R_INCOME_CODE = "Коды видов доходов"
    final String R_TYPE_CODE = "Коды видов вычетов"
    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""

    void checkCreate() {
        def departmentReportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())
        if (departmentReportPeriod.correctionDate != null) {
            def prevDepartmentReportPeriod = departmentReportPeriodService.getFirst(declarationData.getDepartmentId(), declarationData.getReportPeriodId())
            def declarationList = declarationService.findAllByTypeIdAndPeriodId(102, prevDepartmentReportPeriod.getId())
            declarationList.addAll(declarationService.findAllByTypeIdAndPeriodId(103, prevDepartmentReportPeriod.getId()))
            declarationList.addAll(declarationService.findAllByTypeIdAndPeriodId(104, prevDepartmentReportPeriod.getId()))
            if (declarationList.isEmpty()) {
                logger.warn("Отсутствуют отчетные налоговые формы в некорректировочном периоде. Отчетные налоговые формы не будут сформированы текущем периоде")
            }
        }
    }

    /********************** ПРОВЕРКИ *************************************/
    CalendarService calendarService
    FiasRefBookService fiasRefBookService

    PersonService personService

    // Сервис для получения рабочих дней
    DateConditionWorkDay dateConditionWorkDay

    final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" (%d записей)."
    final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" (%d записей)."

    final String T_PERSON_NAME = "Реквизиты"
    final String T_PERSON_INCOME_NAME = "Сведения о доходах и НДФЛ"
    final String T_PERSON_DEDUCTION_NAME = "Сведения о вычетах"
    final String T_PERSON_PREPAYMENT_NAME = "Сведения о доходах в виде авансовых платежей"
    final String T_PERSON = "1" //"Реквизиты"
    final String T_PERSON_PREPAYMENT = "4" //"Сведения о доходах в виде авансовых платежей"

    final String R_PERSON = "Физические лица"
    final String R_CITIZENSHIP = "ОК 025-2001 (Общероссийский классификатор стран мира)"
    final String R_ID_DOC_TYPE = "Коды документов"
    final String R_STATUS = "Статусы налогоплательщика"
    final String R_INCOME_TYPE = "Виды дохода"
    final String R_NOTIF_SOURCE = "Налоговые инспекции"

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String SECTION_LINES_MSG = "Раздел %s. Строки %s"

    final String C_CITIZENSHIP = "Гражданство (код страны)"
    final String C_STATUS = "Статус (код)"
    final String C_INCOME_TYPE = "Признак дохода" //"Доход.Вид.Признак"
    final String C_NOTIF_SOURCE = "Подтверждающий документ. Код источника"
    final String C_PAYMENT_DATE = "Дата платёжного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Дата"
    final String C_PAYMENT_NUMBER = "Номер платёжного поручения"//"НДФЛ.Перечисление в бюджет.Платежное поручение.Номер"
    final String C_TAX_SUMM = "Сумма платёжного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма"
    final String C_INCOME_ACCRUED_SUMM = "Сумма начисленного дохода" //"Доход.Сумма.Начисление"
    final String C_INCOME_PAYOUT_SUMM = "Сумма выплаченного дохода" //"Доход.Сумма.Выплата"
    final String C_OKTMO = "ОКТМО" //"Доход.Источник выплаты.ОКТМО"
    final String C_KPP = "КПП" //"Доход.Источник выплаты.КПП"
    final String C_TOTAL_DEDUCTIONS_SUMM = "Сумма вычета" //"Сумма вычета"
    final String C_TAX_BASE = "Налоговая база" //"Налоговая база"
    final String C_TAX_RATE = "Процентная ставка (%)" //"НДФЛ.Процентная ставка"
    final String C_CALCULATED_TAX = "НДФЛ исчисленный" //" НДФЛ.Расчет.Сумма.Исчисленный"
    final String C_NOT_HOLDING_TAX = "НДФЛ не удержанный" //"НДФЛ.Расчет.Сумма.Не удержанный"
    final String C_OVERHOLDING_TAX = "НДФЛ излишне удержанный" //"НДФЛ.Расчет.Сумма.Излишне удержанный"
    final String C_WITHHOLDING_TAX = "НДФЛ удержанный" //"НДФЛ.Расчет.Сумма.Удержанный"
    final String C_TAX_TRANSFER_DATE = "Срок перечисления в бюджет" //"НДФЛ.Перечисление в бюджет.Срок"
    final String C_PERIOD_CURR_SUMM = "Вычет. Текущий период. Сумма" //" Применение вычета.Текущий период.Сумма"
    final String C_INCOME_ACCRUED = "Доход. Дата" //" Начисленный доход.Дата"
    final String C_INCOME_ACCRUED_CODE = "Доход. Код дохода" //" Начисленный доход.Код дохода"
    final String C_PERIOD_CURR_DATE = "Вычет. Текущий период. Дата" //" Применение вычета.Текущий период.Дата"
    final String C_NOTIF_SUMM = "Подтверждающий документ. Сумма" //" Документ о праве на налоговый вычет.Сумма"
    final String LOG_TYPE_PERSON_MSG_CHECK = "Значение гр. \"%s\" (\"%s\") не соответствует Реестру физических лиц"
    final String LOG_TYPE_PERSON_MSG_2 = "Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\""
    final String LOG_TYPE_2_6 = "\"Дата начисления дохода\" указана некорректно"
    final String LOG_TYPE_2_12 = "\"Сумма вычета\" указана некорректно"
    final String LOG_TYPE_2_14 = "\"Налоговая ставка\" указана некорректно"
    final String LOG_TYPE_2_14_MSG = "Значение гр. \"%s\" (\"%s\") указано некорректно. Для \"Кода дохода\" (\"%s\") и \"Статуса НП\" (\"%s\") предусмотрены ставки: %s"
    final String LOG_TYPE_2_16 = "\"НДФЛ исчисленный\" рассчитан некорректно"
    final String LOG_TYPE_2_18_19 = "\"НДФЛ не удержанный\"/\"НДФЛ излишне удержанный\" рассчитан некорректно"
    final String LOG_TYPE_2_21 = "\"Срок перечисления в бюджет\" указан некорректно"
    final String LOG_TYPE_3_7 = "\"Код источника подтверждающего документа\" указан некорректно"
    final String LOG_TYPE_3_10 = "Строка вычета не соответствует строке начисления"
    final String LOG_TYPE_3_15 = "\"Дата применения вычета в текущем периоде\" не входит в текущий отчетный период"
    final String LOG_TYPE_3_11 = "\"Код начисленного дохода\" указан некорректно"
    final String LOG_TYPE_3_16 = "\"Сумма применения вычета\" указана некорректно"
    final String LOG_TYPE_SECTION4 = "Раздел 4 заполнен некорректно"

    // Сведения о доходах в виде авансовых платежей
    final String P_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление" // графа 7 раздела 4

    // Мапа <ID_Данные о физическом лице - получателе дохода, NdflPersonFL>
    Map<Long, NdflPersonFL> ndflPersonFLMap = [:]
    // Кэш строк раздела 1 по ид
    Map<Long, NdflPerson> personsById

    //Коды стран из справочника
    Map<Long, String> countryCodeCache = [:]

    //Виды документов, удостоверяющих личность
    Map<Long, Map<String, RefBookValue>> documentTypeCache = [:]
    Map<Long, String> documentTypeCodeCache = [:]

    //Коды статуса налогоплательщика
    Map<Long, String> taxpayerStatusCodeCache = [:]

    // Коды налоговых органов
    List<String> taxInspectionCache = []


    final FormDataKind FORM_DATA_KIND = FormDataKind.PRIMARY

    // Дата начала отчетного периода
    Date reportPeriodStartDate = null

    // Сведения о доходах и НДФЛ
    List<NdflPersonIncome> ndflPersonIncomeList

    //Кэш Асну
    Map<Long, RefBookAsnu> asnuCache = [:]

    boolean section_2_15_fatal
    boolean citizenship_fatal
    boolean valueCondition_fatal
    boolean section_2_16Fatal
    boolean section_2_21_fatal
    boolean section_3_10_fatal
    boolean section_3_16_fatal

    void сheckEvent() {
        if (scriptClass.getBinding().hasVariable("personService")) {
            this.personService = (PersonService) scriptClass.getProperty("personService")
        }
        if (scriptClass.getBinding().hasVariable("calendarService")) {
            this.calendarService = (CalendarService) scriptClass.getProperty("calendarService")
        }
        initConfiguration()
        ScriptUtils.checkInterrupted()

        long time = System.currentTimeMillis()
        readChecksFatals()

        Map<Long, RegistryPerson> personMap = getActualRefPersonsByDeclarationDataId(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

        // Реквизиты
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
        personsById = ndflPersonList.collectEntries { [it.id, it] }
        fillNdflPersonFLMap(ndflPersonList, personMap)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_NAME, ndflPersonList.size())

        // Сведения о доходах и НДФЛ
        ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_INCOME_NAME, ndflPersonIncomeList.size())

        // Сведения о вычетах
        List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION_NAME, ndflPersonDeductionList.size())

        // Сведения о доходах в виде авансовых платежей
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT_NAME, ndflPersonPrepaymentList.size())

        logForDebug("Получение записей из таблиц НФДЛ (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()

        ScriptUtils.checkInterrupted()

        logForDebug("Проверки на соответствие справочникам / Выгрузка Реестра физических лиц (" + (System.currentTimeMillis() - time) + " мс)")

        ScriptUtils.checkInterrupted()

        // Проверки на соответствие справочникам
        checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

        ScriptUtils.checkInterrupted()

        // Общие проверки
        checkDataCommon(ndflPersonList, ndflPersonIncomeList, personMap)

        ScriptUtils.checkInterrupted()

        // Проверки сведений о доходах
        checkDataIncome(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

        ScriptUtils.checkInterrupted()

        // Проверки Сведения о вычетах
        checkDataDeduction(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, personMap)

        ScriptUtils.checkInterrupted()

        // Проверки Сведения о доходах в виде авансовых платежей
        checkDataPrepayment(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

        logForDebug("Все проверки (" + (System.currentTimeMillis() - time) + " мс)")
    }

    void readChecksFatals() {
        section_2_15_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_15, declarationData.declarationTemplateId)
        citizenship_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_CITIZENSHIP, declarationData.declarationTemplateId)
        valueCondition_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_VALUE_CONDITION, declarationData.declarationTemplateId)
        section_2_16Fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_16, declarationData.declarationTemplateId)
        section_2_21_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_21, declarationData.declarationTemplateId)
        section_3_10_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_10, declarationData.declarationTemplateId)
        section_3_16_fatal = declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_3_16, declarationData.declarationTemplateId)
    }

    /**
     * Получить актуальные на отчетную дату записи Реестра физических лиц
     * @return
     */
    Map<Long, RegistryPerson> getActualRefPersonsByDeclarationDataId(long declarationDataId) {
        List<RegistryPerson> persons = personService.findActualRefPersonsByDeclarationDataId(declarationDataId)
        Map<Long, RegistryPerson> result = new HashMap<>()
        for (RegistryPerson person : persons) {
            result.put(person.getRecordId(), person)
        }
        return result
    }

    void fillNdflPersonFLMap(List<NdflPerson> ndflPersonList, Map<Long, RegistryPerson> personMap) {
        for (def ndflPerson : ndflPersonList) {
            NdflPersonFL ndflPersonFL
            if (FORM_DATA_KIND == FormDataKind.PRIMARY) {
                // РНУ-НДФЛ первичная
                String fio = (ndflPerson.lastName ?: "") + " " + (ndflPerson.firstName ?: "") + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp ?: "")
            } else {
                // РНУ-НДФЛ консолидированная
                RegistryPerson personRecord = personMap.get(ndflPerson.recordId)
                String fio = (personRecord.lastName ?: "") + " " + (personRecord.firstName ?: "") + " " + (personRecord.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
    }

    /**
     * Проверка. Для ФЛ в разделе 2 есть только одна фиктивная строка
     */
    def checkDummyIncomes(List<NdflPersonIncome> incomes) {
        Map<Long, List<NdflPersonIncome>> incomesByPersonId = incomes.groupBy { NdflPersonIncome it -> it.ndflPersonId }
        for (def personId : incomesByPersonId.keySet()) {
            ScriptUtils.checkInterrupted()
            def incomesOfPerson = incomesByPersonId.get(personId)
            for (def income : incomesOfPerson) {
                NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(income.ndflPersonId)
                String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, income.operationId])
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])
                if (income.isDummy()) {
                    if (incomesOfPerson.size() > 1) {
                        String errMsg = String.format("У ФЛ: %s в Разделе 2 имеется более одной строки, несмотря на то, " +
                                "что текущая строка (для которой ставка налога = 0, ID операции = 0) показывает отсутствие операций по данному ФЛ",
                                fioAndInp
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, income.rowNum ?: "")
                        logger.errorExp("%s. %s.", "Для ФЛ в разделе 2 есть только одна фиктивная строка", fioAndInpAndOperId, pathError, errMsg)
                    }
                }
            }
        }
    }

    /**
     * Проверки на соответствие справочникам
     * @return
     */
    def checkDataReference(
            List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
            List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, RegistryPerson> personMap) {

        long time = System.currentTimeMillis()
        // Страны
        Map<Long, String> citizenshipCodeMap = getRefCountryCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

        // Виды документов, удостоверяющих личность
        Map<Long, String> documentTypeMap = getRefDocumentTypeCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

        // Статус налогоплательщика
        Map<Long, String> taxpayerStatusMap = getRefTaxpayerStatusCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_STATUS, taxpayerStatusMap.size())

        // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        Map<Long, Map<String, RefBookValue>> incomeCodeMap = getRefIncomeCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_CODE, incomeCodeMap.size())

        // Виды доходов Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND>>
        Map<String, List<Map<String, RefBookValue>>> incomeTypeMap = getRefIncomeType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_TYPE, incomeTypeMap.size())

        // Коды видов вычетов
        List<String> deductionTypeList = getRefDeductionType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

        // Коды налоговых органов
        List<String> taxInspectionList = getRefNotifSource()
        logForDebug(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

        logForDebug("Проверки на соответствие справочникам / Выгрузка справочников (" + (System.currentTimeMillis() - time) + " мс)")

        //long timeIsExistsAddress = 0
        time = System.currentTimeMillis()
        //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}
        for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Проверка отключена в рамках https://jira.aplana.com/browse/SBRFNDFL-7256
            /*long tIsExistsAddress = System.currentTimeMillis()


            if (!isPersonAddressEmpty(ndflPerson)) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                if (ndflPerson.postIndex != null && !ndflPerson.postIndex.matches("[0-9]{6}")) {
                    logFiasIndexError(fioAndInp, pathError, "Индекс", ndflPerson.postIndex)
                }
            }

            timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress*/

            // Спр2 Гражданство (Обязательное поле)
            if (ndflPerson.citizenship != null && !citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_CITIZENSHIP, ndflPerson.citizenship ?: "",
                        R_CITIZENSHIP
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        citizenship_fatal,
                        String.format(LOG_TYPE_REFERENCES, R_CITIZENSHIP), fioAndInp, pathError, errMsg)
            }

            // Спр3 Документ удостоверяющий личность.Код (Обязательное поле)
            if (ndflPerson.idDocType != null && !documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2, "ДУЛ Код", ndflPerson.idDocType ?: "", R_ID_DOC_TYPE)
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_ID_DOC_TYPE), fioAndInp, pathError, errMsg)
            }

            // Спр4 Статус (Обязательное поле)
            if (ndflPerson.status != "0" && !taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_STATUS, ndflPerson.status ?: "",
                        R_STATUS
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_STATUS), fioAndInp, pathError, errMsg)
            }

            // Спр10 Наличие связи с "Физическое лицо"
            if (ndflPerson.personId == null || ndflPerson.personId == 0L) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.errorExp("%s. %s.", "Не установлена ссылка на запись Реестра физических лиц", fioAndInp, pathError,
                        "Не установлена ссылка на запись Реестра физических лиц. Выполните операцию идентификации")
            } else {
                RegistryPerson personRecord = personMap.get(ndflPerson.recordId)

                if (!personRecord) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.errorExp("%s. %s.", " Отсутствует актуальная версия связанного ФЛ", fioAndInp, pathError,
                            "Для физического лица из (Реестра физических лиц) определенного по установленной ссылке " +
                                    "отсутствует актуальная на настоящий момент времени версия")
                } else {
                    // Спр11 Фамилия (Обязательное поле)
                    if (personRecord.lastName != null && !ndflPerson.lastName.toLowerCase().equals(personRecord.lastName.toLowerCase())) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, "Фамилия", ndflPerson.lastName ?: ""))
                    }

                    // Спр11 Имя (Обязательное поле)
                    if (personRecord.firstName != null && !ndflPerson.firstName.toLowerCase().equals(personRecord.firstName.toLowerCase())) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, "Имя", ndflPerson.firstName ?: ""))
                    }

                    // Спр11 Отчество (Необязательное поле)
                    if (personRecord.middleName != null && ndflPerson.middleName != null && !ndflPerson.middleName.toLowerCase().equals(personRecord.middleName.toLowerCase())) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, "Отчество", ndflPerson.middleName ?: ""))
                    }

                    if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                        // Спр12 ИНП первичная (Обязательное поле)
                        if (!(ndflPerson.inp == personRecord.snils || personRecord.getPersonIdentityList().inp.contains(ndflPerson.inp))) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: ""))
                        }
                    } else {
                        //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                        //if (formType == CONSOLIDATE){}
                        String recordId = String.valueOf(personRecord.recordId)
                        if (!ndflPerson.inp.equals(recordId)) {
                            //TODO turn_to_error
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG_CHECK, "ИНП", ndflPerson.inp ?: ""))
                        }
                    }
                    // Спр13 Дата рождения (Обязательное поле)
                    if (personRecord.birthDate != null && !personRecord.birthDate.equals(ndflPerson?.birthDay)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Дата рождения не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, "Дата рождения", ndflPerson.birthDay ? ScriptUtils.formatDate(ndflPerson.birthDay) : ""))
                    }

                    // Спр14 Гражданство (Обязательное поле)
                    if (ndflPerson.citizenship != null && !ndflPerson.citizenship.equals(personRecord.citizenship?.code)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Код гражданства не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, C_CITIZENSHIP, ndflPerson.citizenship ?: ""))
                    }

                    // Спр15 ИНН.В Российской федерации (Необязательное поле)
                    if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.inn)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в РФ не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, "ИНН в РФ", ndflPerson.innNp ?: ""))
                    }

                    // Спр16 ИНН.В стране гражданства (Необязательное поле)
                    if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.innForeign)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в ИНО не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, "ИНН в ИНО", ndflPerson.innForeign ?: ""))
                    }

                    if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                        // Спр17 Документ удостоверяющий личность (Первичная) (Обязательное поле)
                        if (ndflPerson.idDocType != null && !ndflPerson.idDocType.equals(personRecord.reportDoc?.docType?.code)) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG_CHECK, "ДУЛ Код", ndflPerson.idDocType ?: ""))
                        }
                        if (ndflPerson.idDocNumber != null && BaseWeightCalculator.prepareStringDul(personRecord.reportDoc?.documentNumber)?.toUpperCase() != BaseWeightCalculator.prepareStringDul(ndflPerson.idDocNumber).toUpperCase()) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG_CHECK, "ДУЛ Номер", ndflPerson.idDocNumber ?: ""))
                        }
                    } else {
                        if (ndflPerson.idDocType != null && !personRecord.documents.docType.code.contains(ndflPerson.idDocType)) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG_CHECK, "ДУЛ Код\" (\"${ndflPerson.idDocType ?: ""}\"), \"ДУЛ Номер", ndflPerson.idDocNumber ?: ""))
                        }
                        for (IdDoc idDoc : personRecord.documents) {
                            if (ndflPerson.idDocNumber != null && BaseWeightCalculator.prepareStringDul(idDoc.documentNumber) != BaseWeightCalculator.prepareStringDul(ndflPerson.idDocNumber).toUpperCase()) {
                                if (personRecord.reportDoc == null || personRecord.reportDoc.id == null) {
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                                    logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют Реестру физических лиц", fioAndInp, pathError,
                                            "\"ДУЛ Номер\" не включается в отчетность")
                                }
                            }
                        }
                    }

                    // Спр18 Статус налогоплательщика
                    if (ndflPerson.status != personRecord.taxPayerState?.code) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Статус налогоплательщика не соответствует Реестру физических лиц", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG_CHECK, C_STATUS, ndflPerson.status ?: ""))
                    }
                }
            }
        }
        logForDebug("Проверки на соответствие справочникам / " + T_PERSON_NAME + " (" + (System.currentTimeMillis() - time) + " мс)")

        /*logForDebug("Проверки на соответствие справочникам / Проверка существования адреса (" + timeIsExistsAddress + " мс)")*/

        time = System.currentTimeMillis()
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            if (!ndflPersonIncome.isDummy()) {
                ScriptUtils.checkInterrupted()

                NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

                // Спр5 Код вида дохода (Необязательное поле)
                if (ndflPersonIncome.incomeCode && ndflPersonIncome.incomeAccruedDate != null &&
                        !incomeCodeMap.find { key, value ->
                            value.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                                    ndflPersonIncome.incomeAccruedDate >= value.record_version_from?.dateValue &&
                                    ndflPersonIncome.incomeAccruedDate <= value.record_version_to?.dateValue
                        }
                ) {
                    String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                            C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                            R_INCOME_CODE
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError, errMsg)
                }

                /*
                Спр6
                При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода

                Доход.Вид.Признак (Графа 5) - (Необязательное поле)
                incomeTypeMap <REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND>>

                Доход.Вид.Код (Графа 4) - (Необязательное поле)
                incomeCodeMap <REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
                 */
                if (ndflPersonIncome.incomeType && ndflPersonIncome.incomeCode) {
                    List<Map<String, RefBookValue>> incomeTypeRowList = incomeTypeMap.get(ndflPersonIncome.incomeType)
                    if (incomeTypeRowList == null || incomeTypeRowList.isEmpty()) {
                        String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                                C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                                R_INCOME_TYPE
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError, errMsg)
                    } else {
                        if (ndflPersonIncome.incomeAccruedDate != null) {
                            List<Map<String, RefBookValue>> incomeCodeRefList = []
                            incomeTypeRowList.each { incomeTypeRow ->
                                if (ndflPersonIncome.incomeAccruedDate >= incomeTypeRow.record_version_from?.dateValue &&
                                        ndflPersonIncome.incomeAccruedDate <= incomeTypeRow.record_version_to?.dateValue) {
                                    RefBookValue refBookValue = incomeTypeRow?.INCOME_TYPE_ID
                                    def incomeCodeRef = incomeCodeMap.get((Long) refBookValue?.getValue())
                                    incomeCodeRefList.add(incomeCodeRef)
                                }
                            }
                            Map<String, RefBookValue> incomeCodeRef = incomeCodeRefList.find { Map<String, RefBookValue> value ->
                                value?.CODE?.stringValue == ndflPersonIncome.incomeCode
                            }
                            if (!incomeCodeRef) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\"), \"%s\" (\"%s\") отсутствует в справочнике \"%s\"",
                                        C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                                        C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                                        R_INCOME_TYPE
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError,
                                        errMsg)
                            }
                        }
                    }
                }
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_INCOME_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр8 Код вычета (Обязательное поле)
            if (ndflPersonDeduction.typeCode != "000" && ndflPersonDeduction.typeCode != null && !deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_TYPE_CODE, ndflPersonDeduction.typeCode ?: "",
                        R_TYPE_CODE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInp, pathError, errMsg)
            }

            // Спр9 Документ о праве на налоговый вычет.Код источника (Обязательное поле)
            if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                        R_NOTIF_SOURCE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_DEDUCTION_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр9 Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Обязательное поле)
            if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        P_NOTIF_SOURCE, ndflPersonPrepayment.notifSource ?: "",
                        R_NOTIF_SOURCE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_PREPAYMENT_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")
    }

    /**
     * Общие проверки
     */
    def checkDataCommon(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, Map<Long, RegistryPerson> personMap) {
        long time = System.currentTimeMillis()
        long timeTotal = time

        logForDebug("Общие проверки: инициализация (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()

        for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Общ1 Корректность ИНН
            if (ndflPerson.citizenship == "643") {
                if (ndflPerson.innNp == null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "\"ИНН\" не указан", fioAndInp, pathError,
                            "Значение гр. \"ИНН в РФ\" не указано.")
                }
            }

            //Общ2 Наличие обязательных реквизитов для формирования отчетности
            boolean checkLastName = checkRequiredAttribute(ndflPerson, fioAndInp, "lastName", "Фамилия")
            boolean checkFirstName = checkRequiredAttribute(ndflPerson, fioAndInp, "firstName", "Имя")
            checkRequiredAttribute(ndflPerson, fioAndInp, "birthDay", "Дата рождения")
            checkRequiredAttribute(ndflPerson, fioAndInp, "citizenship", C_CITIZENSHIP)
            boolean checkIdDocType = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocType", "ДУЛ Код")
            boolean checkIdDocNumber = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocNumber", "ДУЛ Номер")
            checkRequiredAttribute(ndflPerson, fioAndInp, "status", C_STATUS)

            if (checkLastName) {
                List<String> errorMessages = ScriptUtils.checkLastName(ndflPerson.lastName, ndflPerson.citizenship ?: "")
                if (!errorMessages.isEmpty()) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    for (String message : errorMessages) {
                        logger.warnExp("%s. %s.", "\"Фамилия\", \"Имя\" не соответствует формату", fioAndInp, pathError, message)
                    }
                }
            }
            if (checkFirstName) {
                List<String> errorMessages = ScriptUtils.checkFirstName(ndflPerson.firstName, ndflPerson.citizenship ?: "")
                if (!errorMessages.isEmpty()) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    for (String message : errorMessages) {
                        logger.warnExp("%s. %s.", "\"Фамилия\", \"Имя\" не соответствует формату", fioAndInp, pathError, message)
                    }
                }
            }

            if (checkIdDocType && checkIdDocNumber) {
                String checkDul = ScriptUtils.checkDul(ndflPerson.idDocType, ndflPerson.idDocNumber, "ДУЛ Номер")
                if (checkDul != null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "\"ДУЛ\" не соответствует формату", fioAndInp, pathError,
                            checkDul)
                }
            }

            // Общ11 СНИЛС (Необязательное поле)
            if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует формату",
                        "СНИЛС", ndflPerson.snils ?: ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", "\"СНИЛС\" не соответствует формату", fioAndInp, pathError,
                        errMsg)
            }
        }
        logForDebug("Общие проверки / '${T_PERSON_NAME}' (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
        List<ColumnFillConditionData> columnFillConditionDataList = []
        //1 Раздел 2. Графа 4 должна быть заполнена, если заполнена хотя бы одна из граф: "Раздел 2. Графа 10" ИЛИ "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column4Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_CODE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column4Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_CODE,
                        C_INCOME_PAYOUT_SUMM
                )
        )
        //2 Раздел 2. Графа 5 должна быть заполнена, если заполнена хотя бы одна из граф: "Раздел 2. Графа 10" ИЛИ "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column5Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_TYPE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column5Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_TYPE,
                        C_INCOME_PAYOUT_SUMM
                )
        )
        //3 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column6Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_DATE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        //4 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column7Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_DATE,
                        C_INCOME_PAYOUT_SUMM
                )
        )
        //5 Раздел 2. Графа 8 Должна быть всегда заполнена
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column8Fill(),
                String.format("Не заполнена гр. \"%s\"",
                        C_OKTMO
                )
        )
        //6 Раздел 2. Графа 9 Должна быть всегда заполнена
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column9Fill(),
                String.format("Не заполнена гр. \"%s\"",
                        C_KPP
                )
        )
        //7 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column6Fill(),
                new Column10Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_SUMM,
                        C_INCOME_ACCRUED_DATE
                )
        )
        //8 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7Fill(),
                new Column11Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_SUMM,
                        C_INCOME_PAYOUT_DATE
                )
        )
        //9 Раздел 2. Графа 13 Должна быть заполнена, если заполнена Раздел 2. Графа 10.
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column13Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_BASE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        //10 Раздел 2. Графы 14 Должна быть заполнена, если заполнена Раздел 2. Графа 10 ИЛИ Раздел 2. Графа 11.
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column14Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_RATE, C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column14Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_RATE, C_INCOME_PAYOUT_SUMM
                )
        )
        //11 Раздел 2. Графы 15 Должна быть заполнена, если заполнена хотя бы одна из граф: "Раздел 2. Графа 10" ИЛИ "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column15Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_DATE, C_INCOME_ACCRUED_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column15Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_DATE, C_INCOME_PAYOUT_SUMM
                )
        )
        //12 Раздел 2. Графа 16 Должна быть заполнена, если заполнена "Раздел 2. Графа 10"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column16Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_CALCULATED_TAX, C_INCOME_ACCRUED_SUMM
                )
        )
        //13 Раздел 2. Графа 17 Должна быть заполнена, если заполнена "Раздел 2. Графа 11"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column17Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_WITHHOLDING_TAX, C_INCOME_PAYOUT_SUMM
                )
        )
        //14 Раздел 2. Графа 21 Должна быть заполнена, если выполняется одно из условий:
        // 1. заполнена "Раздел 2. Графа 7"
        // 2. одновременно заполнены "Раздел 2. Графа 22" И "Раздел 2. Графа 23" И "Раздел 2. Графа 24"
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7Fill(),
                new Column21Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_TAX_TRANSFER_DATE, C_INCOME_PAYOUT_DATE
                )

        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24Fill(),
                new Column21Fill(),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_TAX_TRANSFER_DATE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )

        )
        //15 Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна из них
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column22And23And24FillOrColumn22And23And24NotFill(),
                String.format("Гр. \"%s\", гр. \"%s\", гр. \"%s\" должны быть заполнены одновременно или не заполнена ни одна из них",
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )

        )

        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            if (!ndflPersonIncome.isDummy()) {
                ScriptUtils.checkInterrupted()

                def operationId = ndflPersonIncome.operationId ?: ""
                NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
                String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

                // Общ5 Принадлежность дат операций к отчетному периоду. Проверка перенесана в событие загрузки ТФ

                // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
                columnFillConditionDataList.each { columnFillConditionData ->
                    if (columnFillConditionData.columnConditionCheckerAsIs.check(ndflPersonIncome) &&
                            !columnFillConditionData.columnConditionCheckerToBe.check(ndflPersonIncome)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.logCheck("%s. %s.",
                                valueCondition_fatal,
                                "Наличие (отсутствие) значения в графе не соответствует алгоритму заполнения РНУ НДФЛ",
                                fioAndInpAndOperId, pathError, columnFillConditionData.conditionMessage)
                    }
                }
            }
        }
        ScriptUtils.checkInterrupted()
        logForDebug("Общие проверки / " + T_PERSON_INCOME_NAME + " (" + (System.currentTimeMillis() - time) + " мс)")

        time = System.currentTimeMillis()
        // Общ16 Для ФЛ в разделе 2 есть только одна фиктивная строка
        checkDummyIncomes(ndflPersonIncomeList)
        logForDebug("Общие проверки / Фиктивные записи (" + (System.currentTimeMillis() - time) + " мс)")

        logForDebug("Общие проверки всего (" + (System.currentTimeMillis() - timeTotal) + " мс)")
    }

    /**
     * Проверки сведений о доходах
     * @param ndflPersonList
     * @param ndflPersonIncomeList
     * @param ndflPersonDeductionList
     * @param ndflPersonPrepaymentList
     */
    def checkDataIncome(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
                        List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, RegistryPerson> personMap) {

        long time = System.currentTimeMillis()

        Map<Long, NdflPerson> personsCache = [:]
        ndflPersonList.each { ndflPerson ->
            personsCache.put(ndflPerson.id, ndflPerson)
        }

        Map<Long, List<NdflPersonPrepayment>> ndflPersonPrepaymentCache = [:]
        ndflPersonPrepaymentList.each { NdflPersonPrepayment ndflPersonPrepayment ->
            List<NdflPersonPrepayment> ndflPersonPrepaymentListByPersonIdList = ndflPersonPrepaymentCache.get(ndflPersonPrepayment.ndflPersonId) ?: new ArrayList<NdflPersonPrepayment>()
            ndflPersonPrepaymentListByPersonIdList.add(ndflPersonPrepayment)
            ndflPersonPrepaymentCache.put(ndflPersonPrepayment.ndflPersonId, ndflPersonPrepaymentListByPersonIdList)
        }

        List<DateConditionData<IncomeAccruedDateConditionChecker>> dateConditionDataList = []
        List<DateConditionData<TaxTransferDateConditionChecker>> dateConditionDataListForBudget = []

        dateConditionWorkDay = new DateConditionWorkDay(calendarService)

        // 1. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["1010", "1011", "1110", "1400", "1552", "2001", "2010", "2012", "2300", "2301",
                                                        "2640", "2641", "2710", "2760", "2762", "2770", "2800", "2900", "3020", "3023", "4800"],
                ["00"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 2. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                        "1541", "1542", "1551", "1552", "1553", "1554"],
                ["01", "02"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 3. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2002", "2003"], ["07"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 4. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2013", "2014", "4800", "2510", "2202", "2740", "2750", "2790", "2520"], ["13"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 5. "Графа 6" = "Графе 7"
        dateConditionDataList << new DateConditionData(["2520", "2720", "2740", "2750", "2790", "4800"], ["14"], new Column6EqualsColumn7(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") для кода дохода и признака дохода, " +
                        "указанных в гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")")

        // 6. Соответствует последнему рабочему календарному дню года
        dateConditionDataList << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                        "1541", "1542", "1544", "1546", "1548", "1551", "1552", "1553", "1554"],
                ["04"], new LastYearWorkDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему рабочему дню года, за который был начислен доход (%4\$s), " +
                        "для Кода дохода = \"%6\$s\" и Признака дохода = \"%8\$s\"")

        // 7. Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2000", "2003"], ["05"], new LastMonthCalendarDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход, " +
                        "для кода дохода и признака дохода, указанных в гр. \"%5\$s\" (\"%6\$s\") и гр. \"%7\$s\" (\"%8\$s\")")

        // 8. Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2000"], ["11"], new LastMonthCalendarDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход, " +
                        "для кода дохода и признака дохода, указанных в гр. \"%5\$s\" (\"%6\$s\") и гр. \"%7\$s\" (\"%8\$s\")")

        // 9. Последний календарный день месяца
        dateConditionDataList << new DateConditionData(["2610"], ["00"], new LastMonthCalendarDay(),
                "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход, " +
                        "для кода дохода и признака дохода, указанных в гр. \"%5\$s\" (\"%6\$s\") и гр. \"%7\$s\" (\"%8\$s\")")

        // 1,2 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["1010", "1011", "3020", "3023",
                                                                 "1110", "1400", "2001", "2010", "2301", "2710", "2760",
                                                                 "2762", "2770", "2900", "4800"], ["00"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 3 "Графа 21" = "Графа 7" + 1 рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2000"], ["05", "06", "11", "12"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 4 "Графа 21" = "Графа 7" + 1 рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2002"], ["07", "08", "09", "10"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 5 "Графа 21" = "Графа 7" + 1 рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2003"], ["05", "06", "07", "08", "09", "10"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 6 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2520", "2740", "2750", "2790", "4800", "2013", "2014",
                                                                 "2510", "2202", "2740", "2750", "2790", "2520"], ["13"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 7,8,9 "Графа 21" = "Графа 7" + "1 рабочий день"
        dateConditionDataListForBudget << new DateConditionData(["2610", "2611", "2640", "2641", "2800"], ["00"],
                new Column21EqualsColumn7Plus1WorkingDay(), null)

        // 10 "Графа 21" <= "Графа 7" + "30 календарных дней"
        dateConditionDataListForBudget << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                                 "1541", "1542", "1551", "1552", "1553", "1554"], ["01", "03"],
                new Column21EqualsColumn7Plus30WorkingDays(), null)

        // 11 "Графа 21" ≤ "Графа 7" + "30 календарных дней"
        dateConditionDataListForBudget << new DateConditionData(["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539",
                                                                 "1541", "1542", "1551", "1552", "1553", "1554"], ["02"],
                new Column21EqualsColumn7Plus30WorkingDays(), null)

        // 12 "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
        dateConditionDataListForBudget << new DateConditionData(["2012", "2300"], ["00"],
                new Column21EqualsColumn7LastDayOfMonth(), null)

        // 13
        dateConditionDataListForBudget << new DateConditionData(["2720", "2740", "2750", "2790", "4800"], ["14"],
                new Column21ForNaturalIncome(), null)

        //14 "Графа 21" = последний календарный день первого месяца года, следующего за годом, указанным в "Графа 7"
        dateConditionDataListForBudget << new DateConditionData([], ["04"],
                new Column21EqualsLastDayOfFirstMonthOfNextYear(), null)

        // Сгруппируем Сведения о доходах на основании принадлежности к плательщику
        Map<Long, List<NdflPersonIncome>> incomesByPersonId = ndflPersonIncomeList.groupBy { it.ndflPersonId }
        Map<Long, Map<String, List<NdflPersonDeduction>>> deductionsByPersonIdAndOperationId =
                ndflPersonDeductionList.groupBy({ NdflPersonDeduction it -> it.ndflPersonId }, { NdflPersonDeduction it -> it.operationId })
        Map<Long, Map<String, List<NdflPersonPrepayment>>> prepaymentsByPersonIdAndOperationId =
                ndflPersonPrepaymentList.groupBy({ NdflPersonPrepayment it -> it.ndflPersonId }, { NdflPersonPrepayment it -> it.operationId })

        Map<String, List<NdflPersonIncome>> incomesByPersonIdForCol16Sec2Check = null

        // Операции по которым уже сформировано сообщение ошибки при проверке Раздела 2 Графы 16 случай "Графа 14" ≠ "13"
        Set<String> operationsCol16Sec2TaxRateNot13 = []

        incomesByPersonId.each { ndflPersonId, allIncomesOfPerson ->
            ScriptUtils.checkInterrupted()

            NdflPerson ndflPerson = personsCache.get(ndflPersonId)
            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonId)
            Collection<List<NdflPersonIncome>> personOperations = allIncomesOfPerson.groupBy {
                new Pair<String, Long>(it.operationId, it.asnuId)
            }.values()
            for (List<NdflPersonIncome> allIncomesOfOperation : personOperations) {
                def operationId = allIncomesOfOperation.first().operationId
                List<NdflPersonDeduction> allDeductionsOfOperation = deductionsByPersonIdAndOperationId.get(ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonDeduction>()
                List<NdflPersonPrepayment> allPrepaymentsOfOperation = prepaymentsByPersonIdAndOperationId.get(ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonPrepayment>()
                String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])
                String rowNums = allIncomesOfOperation?.rowNum?.sort()?.join(", ") ?: ""
                // содержат суммы всех строк операции, если ни в одной строке значение не заполнено, то сумма равна null
                NdflPersonIncome totalOperationIncome = sumIncomes(allIncomesOfOperation)
                NdflPersonDeduction totalOperationDeduction = sumDeductions(allDeductionsOfOperation)
                /**
                 * Проверки по операциям
                 */
                if (!isDummy(allIncomesOfOperation)) {
                    // СведДох2 Сумма вычета (Графа 12)
                    BigDecimal incomesAccruedSum = totalOperationIncome.incomeAccruedSumm ?: 0
                    BigDecimal incomesDeductionsSum = totalOperationIncome.totalDeductionsSumm ?: 0
                    BigDecimal deductionsSum = totalOperationDeduction.periodCurrSumm ?: 0
                    if (incomesAccruedSum && incomesDeductionsSum && signOf(incomesAccruedSum) != signOf(incomesDeductionsSum)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" сумма значений гр. \"Сумма вычета\" (\"%s\") и сумма значений гр. " +
                                "\"Сумма начисленного дохода\" (\"%s\") должны иметь одинаковый знак",
                                operationId, incomesDeductionsSum, incomesAccruedSum)
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (incomesAccruedSum.abs() < incomesDeductionsSum.abs()) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" Модуль суммы значений гр. \"Сумма вычета\" (\"%s\") должен быть меньше " +
                                "или равен модулю суммы значений гр. \"Сумма начисленного дохода\" (\"%s\")",
                                operationId, incomesDeductionsSum, incomesAccruedSum)
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }
                    if (incomesDeductionsSum != deductionsSum) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" сумма значений гр. \"Сумма вычета\" " +
                                "Раздела 2 (\"%s\") должна быть равна сумме значений гр. \"Вычет. Текущий период. Сумма\" Раздела 3 (\"%s\")",
                                operationId, incomesDeductionsSum, deductionsSum)
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInpAndOperId, pathError, errMsg)
                    }

                    // СведДох7 Заполнение Раздела 2 Графы 18 и 19
                    BigDecimal notHoldingTax = totalOperationIncome.notHoldingTax ?: 0
                    BigDecimal overholdingTax = totalOperationIncome.overholdingTax ?: 0
                    BigDecimal calculatedTax = totalOperationIncome.calculatedTax ?: 0
                    BigDecimal withholdingTax = totalOperationIncome.withholdingTax ?: 0
                    if (notHoldingTax - overholdingTax != calculatedTax - withholdingTax) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = "Для строк операции с \"ID операции\"=\"$operationId\" разность сумм значений гр. \"НДФЛ не удержанный\" " +
                                "(\"$notHoldingTax\") и гр. \"НДФЛ излишне удержанный\" (\"$overholdingTax\") " +
                                "должна быть равна разности сумм значений гр.\"НДФЛ исчисленный\" (\"$calculatedTax\") и " +
                                "гр.\"НДФЛ удержанный\" (\"$withholdingTax\") по всем строкам одной операции"
                        String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                        logger.warnExp("%s. %s.", LOG_TYPE_2_18_19, fioAndInpAndOperId, pathError, errMsg)
                    }
                }

                /**
                 * Проверки по строкам
                 */
                for (NdflPersonIncome ndflPersonIncome : allIncomesOfOperation) {
                    // СведДох1 Доход.Дата.Начисление (Графа 6)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.incomeAccruedSumm != null) {
                        dateConditionDataList.each { dateConditionData ->
                            if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                                boolean check = dateConditionData.checker.check(ndflPersonIncome, allIncomesOfOperation)
                                if (!check) {
                                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                                    String errMsg = String.format(dateConditionData.conditionMessage,
                                            C_INCOME_ACCRUED_DATE, formatDate(ndflPersonIncome.incomeAccruedDate),
                                            C_INCOME_PAYOUT_DATE, formatDate(dateConditionData.checker.getDateCompared(ndflPersonIncome)),
                                            C_INCOME_CODE, ndflPersonIncome.incomeCode,
                                            C_INCOME_TYPE, ndflPersonIncome.incomeType
                                    )
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                    logger.warnExp("%s. %s.", LOG_TYPE_2_6, fioAndInpAndOperId, pathError, errMsg)
                                }
                            }
                        }
                    }

                    // Заполнение Раздела 2 Графы 13
                    if (ndflPersonIncome.incomeAccruedDate && ndflPersonIncome.taxBase != (ndflPersonIncome.incomeAccruedSumm ?: 0) - (ndflPersonIncome.totalDeductionsSumm ?: 0)) {
                        String errMsg = "Значение гр. \"Налоговая База\" \"$ndflPersonIncome.taxBase\" не совпадает с расчетным " +
                                "\"${(ndflPersonIncome.incomeAccruedSumm ?: 0) - (ndflPersonIncome.totalDeductionsSumm ?: 0)}\""
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", "\"Налоговая база\" указана некорректно", fioAndInpAndOperId, pathError, errMsg)
                    }

                    // СведДох3 НДФЛ.Процентная ставка (Графа 14)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.taxRate != null) {
                        boolean checkNdflPersonIncomingTaxRateTotal = false

                        boolean presentCitizenship = ndflPerson.citizenship != null
                        boolean presentIncomeCode = ndflPersonIncome.incomeCode != null
                        boolean presentStatus = ndflPerson.status != null
                        boolean presentTaxRate = ndflPersonIncome.taxRate != null
                        def ndflPersonIncomingTaxRates = []
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_13:
                        {
                            if (presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) {
                                Boolean conditionA = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "2"
                                Boolean conditionB = ndflPerson.citizenship == "643" && ["1010", "1011"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status == "1"
                                Boolean conditionC = ndflPerson.citizenship != "643" && ["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status ? Integer.parseInt(ndflPerson.status) : 0 >= 3
                                if (conditionA || conditionB || conditionC) {
                                    if (ndflPersonIncome.taxRate == 13) {
                                        checkNdflPersonIncomingTaxRateTotal = true
                                    } else {
                                        ndflPersonIncomingTaxRates << "\"13\""
                                    }
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_15:
                        {
                            if ((presentIncomeCode && presentStatus && presentTaxRate) && (ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                                if (ndflPersonIncome.taxRate == 15) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"15\""
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_35:
                        {
                            if ((presentIncomeCode && presentStatus && presentTaxRate) && (["2740", "3020", "2610", "3023"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status != "2")) {
                                if (ndflPersonIncome.taxRate == 35) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"35\""
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_30:
                        {
                            if (presentIncomeCode && presentStatus && presentTaxRate) {
                                def conditionA = Integer.parseInt(ndflPerson.status) >= 2 && ndflPersonIncome.incomeCode != "1010"
                                def conditionB = Integer.parseInt(ndflPerson.status) > 2 && !["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode)
                                if (conditionA || conditionB) {
                                    if (ndflPersonIncome.taxRate == 30) {
                                        checkNdflPersonIncomingTaxRateTotal = true
                                    } else {
                                        ndflPersonIncomingTaxRates << "\"30\""
                                    }
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_9:
                        {
                            if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1110" && ndflPerson.status == "1")) {
                                if (ndflPersonIncome.taxRate == 9) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"9\""
                                }
                            }
                        }
                        CHECK_NDFL_PERSON_INCOMING_TAX_RATE_OTHER:
                        {
                            if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship != "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                                if (![13, 15, 35, 30, 9].contains(ndflPersonIncome.taxRate)) {
                                    checkNdflPersonIncomingTaxRateTotal = true
                                } else {
                                    ndflPersonIncomingTaxRates << "\"Специальная ставка\""
                                }
                            }
                        }
                        if (!checkNdflPersonIncomingTaxRateTotal && !ndflPersonIncomingTaxRates.isEmpty()) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format(LOG_TYPE_2_14_MSG, "Процентная ставка (%)", ndflPersonIncome.taxRate,
                                    ndflPersonIncome.incomeCode, ndflPerson.status,
                                    ndflPersonIncomingTaxRates.join(", ")
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_14, fioAndInpAndOperId, pathError, errMsg)
                        }
                    }

                    // СведДох4 НДФЛ.Расчет.Дата (Графа 15)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.taxDate != null) {
                        List<CheckData> logTypeMessagePairList = []
                        // П.1
                        if (ndflPersonIncome.calculatedTax >= 0 && ndflPersonIncome.incomeAccruedDate &&
                                ndflPersonIncome.incomeAccruedDate >= reportPeriod.calendarStartDate &&
                                ndflPersonIncome.incomeAccruedDate <= reportPeriod.endDate) {
                            // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                            if (ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                                logTypeMessagePairList.add(new CheckData("\"Дата исчисленного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") не равно значению гр. " +
                                                "\"${C_INCOME_ACCRUED_DATE}\" (\"${formatDate(ndflPersonIncome.incomeAccruedDate)}\"). " +
                                                "Даты могут не совпадать, если это строка корректировки начисления!").toString(),
                                        section_2_15_fatal))
                            }
                        }
                        // П.2
                        // У проверяемой строки "Графы 16" < "0"
                        if (ndflPersonIncome.calculatedTax != null && ndflPersonIncome.calculatedTax < 0 && ndflPersonIncome.totalDeductionsSumm != null) {
                            // Существует хотя бы одна строка, по которой выполняются условия:
                            // 1. "Раздел 3.Графа 2" = "Раздел 2. Графа 2" проверяемой строки
                            // 2. "Раздел 3. Графа 9" = "Раздел 2. Графа 3" проверяемой строки
                            // 3. "Раздел 3. Графа 15" = "Раздел 2. Графа 15" проверяемой строки
                            def isDeductionExists = allDeductionsOfOperation.find {
                                it.periodCurrDate == ndflPersonIncome.taxDate
                            } != null
                            if (!isDeductionExists) {
                                logTypeMessagePairList.add(new CheckData("\"Дата исчисленного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") отсутствует в гр. " +
                                                "\"${C_PERIOD_CURR_DATE}\" хотя бы одной строки операции Раздела 3.").toString(),
                                        section_2_15_fatal))
                            }
                        }
                        // П.3
                        // В проверяемой строке: "Графа 19" НЕ заполнена и "Графа 7" заполнена
                        if (ndflPersonIncome.overholdingTax == null && ndflPersonIncome.incomePayoutDate != null) {
                            def countPositiveCol17 = allIncomesOfOperation.count {
                                it.withholdingTax != null && it.withholdingTax > 0
                            }
                            // По операции («Графа 3»), указанной в проверяемой строке, существует только одна строка, у которой "Графа 17" > "0"
                            if (countPositiveCol17 == 1) {
                                // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                                if (ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                    logTypeMessagePairList.add(new CheckData("\"Дата удержанного налога\" указана некорректно",
                                            ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно " +
                                                    "значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${formatDate(ndflPersonIncome.incomePayoutDate)}\").").toString(),
                                            section_2_15_fatal))
                                }
                            }
                        }
                        // П.4
                        // Одновременно выполняются условия :
                        // 1. "Графа 18" > 0
                        // 2. "Графа 7" заполнена
                        // 3. "Графа 4" не равна {1530, 1531, 1532, 1533, 1535, 1536, 1537, 1539, 1541, 1542, 1551, 1552, 1553, 1554}
                        if (ndflPersonIncome.notHoldingTax != null && ndflPersonIncome.notHoldingTax > 0 &&
                                ndflPersonIncome.incomePayoutDate != null &&
                                ndflPersonIncome.incomeCode != null &&
                                !(ndflPersonIncome.incomeCode in ["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1551", "1552", "1553", "1554"])
                        ) {
                            // "Графа 15" = "Графа 7"
                            if (ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                logTypeMessagePairList.add(new CheckData("\"Дата не удержанного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно " +
                                                "значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${formatDate(ndflPersonIncome.incomePayoutDate)}\").").toString(),
                                        section_2_15_fatal))
                            }
                        }
                        // П.5
                        // Для проверяемой строки выполняются все условия:
                        //  - "Графа 18" > 0
                        //  - "Графа 6" заполнена
                        //  - "Графа 4" = {1530, 1531, 1532, 1533, 1535, 1536, 1537, 1539, 1541, 1542, 1551, 1552, 1553, 1554}
                        if (ndflPersonIncome.notHoldingTax != null && ndflPersonIncome.notHoldingTax > 0 &&
                                ndflPersonIncome.incomeAccruedDate != null &&
                                ndflPersonIncome.incomeCode in ["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1551", "1552", "1553", "1554"]
                        ) {
                            // Существует строка, по которой выполняются условия:
                            //    - Заполнена "Графа 7"
                            //    - "Графа 2" = "Графа 2" проверяемой строки
                            //    - "Графа 3" = "Графа 3" проверяемой строки
                            //    - "Графа 4" = "Графа 4" проверяемой строки
                            //    - Если найдено несколько строк, то брать одну строку, у которой значение "Граф 7" является максимальной.
                            //      При этом если найдено несколько строк с одинаковыми значениями максимальной даты, то брать строку, созданную первой
                            def foundIncomeMaxCol7 = allIncomesOfOperation.findAll {
                                it.incomePayoutDate != null && it.incomeCode == ndflPersonIncome.incomeCode
                            }.max { it.incomePayoutDate }
                            // Для найденной в предыдущем пункте строки «Графа 7» принадлежит отчетному периоду
                            if (foundIncomeMaxCol7 != null && dateRelateToCurrentPeriod(foundIncomeMaxCol7.incomePayoutDate)) {
                                // "Графа 15" = "Графа 6"
                                if (ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                                    logTypeMessagePairList.add(new CheckData("\"Дата не удержанного налога\" указана некорректно",
                                            ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно " +
                                                    "значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${formatDate(ndflPersonIncome.incomeAccruedDate)}\").").toString(),
                                            section_2_15_fatal))
                                }
                            }
                        }
                        // П.6
                        // Одновременно выполняются условия :
                        // 1. "Графа 18" > 0
                        // 2. "Графа 4" равна {1530, 1531, 1532, 1533, 1535, 1536, 1537, 1539, 1541, 1542, 1551, 1552, 1553, 1554}
                        // 3. "Графа 6" НЕ принадлежит отчетному периоду
                        if (ndflPersonIncome.notHoldingTax != null && ndflPersonIncome.notHoldingTax > 0 &&
                                ndflPersonIncome.incomeCode in ["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1551", "1552", "1553", "1554"] &&
                                ndflPersonIncome.incomeAccruedDate != null && !dateRelateToCurrentPeriod(ndflPersonIncome.incomeAccruedDate)
                        ) {
                            // "Графа 15" соответствует маске 31.12.20**
                            if (ndflPersonIncome.taxDate != null) {
                                Calendar calendarPayout = Calendar.getInstance()
                                calendarPayout.setTime(ndflPersonIncome.taxDate)
                                int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
                                int month = calendarPayout.get(Calendar.MONTH)
                                if (!(dayOfMonth == 31 && month == 12)) {
                                    logTypeMessagePairList.add(new CheckData("\"Дата не удержаннного налога\" указана некорректно",
                                            ("Значение гр. \"${C_TAX_DATE}\" (\"${formatDate(ndflPersonIncome.taxDate)}\") должно быть равно последнему календарному дню года налогового периода.").toString()))
                                }
                            }
                        }
                        // П.7
                        // Одновременно выполняются условия:
                        // 1. "Графа 19" > 0
                        // 2. "Графа 7" заполнена
                        if (ndflPersonIncome.overholdingTax != null && ndflPersonIncome.overholdingTax > 0 &&
                                ndflPersonIncome.incomePayoutDate != null) {
                            // "Графа 15" = "Графа 7"
                            if (ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                logTypeMessagePairList.add(new CheckData("\"Дата излишне удержанного налога\" указана некорректно",
                                        ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") " +
                                                "должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" " +
                                                "(\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\").").toString(), section_2_15_fatal))
                            }
                        }
                        // П.8
                        // Проверка не должна выполняться.
                        /*if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.refoundTax ?: 0 > 0) &&
                                (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                                (ndflPersonIncome.overholdingTax ?: 0) &&
                                ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                            // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                            if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                                checkTaxDate = false
                                logTypeMessagePairList.add(new CheckData("\"Дата расчета возвращенного налогоплательщику налога\" указана некорректно", ("Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ScriptUtils.formatDate(ndflPersonIncome.taxDate) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ScriptUtils.formatDate(ndflPersonIncome.incomePayoutDate) : ""}\")").toString(), section_2_15_fatal))
                            }
                        }*/
                        if (!logTypeMessagePairList.isEmpty()) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            for (CheckData checkData : logTypeMessagePairList) {
                                logger.logCheck("%s. %s", checkData.fatal, checkData.msgFirst, fioAndInpAndOperId, pathError, checkData.msgLast)
                            }
                        }
                    }

                    // СведДох5 НДФЛ.Расчет.Сумма.Исчисленный (Заполнение Раздела 2 Графы 16)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.calculatedTax != null) {
                        if (ndflPersonIncome.taxRate != 13) {
                            if (!operationsCol16Sec2TaxRateNot13.contains(ndflPersonIncome.operationId)) {
                                // условие | ∑ Р.2.Гр.16 - ∑ ОКРУГЛ(Р.2.Гр.13 x Р.2.Гр.14/100) – ∑ Р.4.Гр.4 | < 1
                                // ∑ Р.2.Гр.16
                                BigDecimal var1 = (BigDecimal) allIncomesOfOperation.sum { NdflPersonIncome income -> income.calculatedTax ?: 0 }
                                // ∑ ОКРУГЛ(Р.2.Гр.13 x Р.2.Гр.14/100)
                                BigDecimal var2 = (BigDecimal) allIncomesOfOperation.sum { NdflPersonIncome income ->
                                    income.calculatedTax != null ? ScriptUtils.round((income.taxBase ?: 0) * (income.taxRate ?: 0) / 100) : 0
                                }
                                // ∑ Р.4.Гр.4
                                BigDecimal var3 = (BigDecimal) allPrepaymentsOfOperation?.sum { NdflPersonPrepayment prepayment -> prepayment.summ ?: 0 } ?: 0
                                BigDecimal ВычисленноеЗначениеНалога = var2 - var3
                                if (!((var1 - ВычисленноеЗначениеНалога).abs() < 1)) {
                                    operationsCol16Sec2TaxRateNot13 << ndflPersonIncome.operationId
                                    String errMsg = String.format("Для строк операции с \"ID операции\"=\"%s\" значение налога исчисленного в гр. 16 (%s р) не совпадает с расчетным (%s р)",
                                            operationId, var1, ВычисленноеЗначениеНалога
                                    )
                                    String pathError = String.format(SECTION_LINES_MSG, T_PERSON_INCOME, rowNums)
                                    logger.logCheck("%s. %s.",
                                            section_2_16Fatal,
                                            LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                                }
                            }
                        } else {
                            if (ndflPersonIncome.incomeCode == "1010") {
                                // условие | Р.2.Гр.16 - ОКРУГЛ (Р.2.Гр.13 x Р.2.Гр.14/100) | < 1
                                // ОКРУГЛ (Р.2.Гр.13 x Р.2.Гр.14/100)
                                BigDecimal ВычисленноеЗначениеНалога = ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * (ndflPersonIncome.taxRate ?: 0)) / 100, 0)
                                if (!((ndflPersonIncome.calculatedTax - ВычисленноеЗначениеНалога).abs() < 1)) {
                                    String errMsg = String.format("Значение налога исчисленного в гр. 16 (%s р) не совпадает с расчетным (%s р)",
                                            ndflPersonIncome.calculatedTax, ВычисленноеЗначениеНалога
                                    )
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                    logger.logCheck("%s. %s.",
                                            section_2_16Fatal,
                                            LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                                }
                            } else {
                                def groupKey = { NdflPersonIncome income ->
                                    personsCache.get(income.ndflPersonId).inp + "_" + income.kpp + "_" + income.oktmo + "_" + income.incomeAccruedDate?.getAt(Calendar.YEAR)
                                }
                                // Группы сортировки, вычисляем 1 раз для всех строк
                                if (incomesByPersonIdForCol16Sec2Check == null) {
                                    // отбираем все строки формы с заполненной графой 16 и у которых одновременно: 1) Р.2.Гр.14 = «13». 2) Р.2.Гр.4 ≠ «1010».
                                    // затем группирует по инп, кпп и октмо
                                    incomesByPersonIdForCol16Sec2Check = ndflPersonIncomeList.findAll {
                                        it.calculatedTax != null && it.taxRate == 13 && it.incomeCode != "1010"
                                    }.groupBy {
                                        groupKey(it)
                                    }
                                    // сортируем внутри каждой группы
                                    incomesByPersonIdForCol16Sec2Check.each { k, v ->
                                        v.sort(true, { NdflPersonIncome a, NdflPersonIncome b ->
                                            a.incomeAccruedDate <=> b.incomeAccruedDate ?: a.taxDate <=> b.taxDate ?: a.incomeCode <=> b.incomeCode ?:
                                                    a.incomeType <=> b.incomeType ?: a.operationId <=> b.operationId
                                        })
                                    }
                                }
                                def groupIncomes = incomesByPersonIdForCol16Sec2Check.get(groupKey(ndflPersonIncome))
                                def groupPrepayments = ndflPersonPrepaymentList.findAll {
                                    it.operationId in groupIncomes.operationId
                                }
                                BigDecimal АвансовыеПлатежиПоГруппе = (BigDecimal) groupPrepayments.sum { NdflPersonPrepayment prepayment -> prepayment.summ ?: 0 } ?: 0
                                BigDecimal taxBaseSum = (BigDecimal) groupIncomes.sum { NdflPersonIncome income ->
                                    income.calculatedTax != null && income.taxBase ? income.taxBase : 0
                                } ?: 0
                                BigDecimal calculatedTaxSum = (BigDecimal) groupIncomes.sum { NdflPersonIncome income -> income.calculatedTax ?: 0 } ?: 0
                                BigDecimal ОбщаяДельта = (ScriptUtils.round(taxBaseSum * 13 / 100) - АвансовыеПлатежиПоГруппе - calculatedTaxSum)
                                        .abs()

                                if (ОбщаяДельта >= 1) {
                                    // "S1" = ∑ Р.2.Гр.13 с первой строки группы сортировки по проверяемую строку включительно.
                                    // Суммируются только строки, для которых значение Р.2.Гр.10 не пустое.
                                    BigDecimal s1 = 0
                                    for (def income : groupIncomes) {
                                        s1 += income.taxBase ?: 0
                                        if (income == ndflPersonIncome) {
                                            break
                                        }
                                    }
                                    // ∑ Р.2.Гр.16 с первой строки группы сортировки до проверяемой строки (проверяемая строка не включается).
                                    // Суммируются только строки, для которых значение Р.2.Гр.16 не пустое.
                                    BigDecimal s2 = 0
                                    for (def income : groupIncomes) {
                                        if (income == ndflPersonIncome) {
                                            break
                                        }
                                        s2 += income.calculatedTax ?: 0
                                    }
                                    // "S3" = ∑ Р.4.Гр.4 по операции, указанной в проверяемой строке
                                    BigDecimal s3 = new BigDecimal("0")
                                    List<String> operationIdList = []
                                    for (NdflPersonIncome income : groupIncomes) {
                                        operationIdList << income.operationId
                                        if (income == ndflPersonIncome) {
                                            break
                                        }
                                    }
                                    for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {
                                        NdflPerson incomePerson = personsCache.get(ndflPersonIncome.getNdflPersonId())
                                        NdflPerson prepaymentPerson = personsCache.get(ndflPersonPrepayment.getNdflPersonId())
                                        if (incomePerson.inp == prepaymentPerson.inp && operationIdList.contains(ndflPersonPrepayment.operationId)) {
                                            s3 = s3.add(ndflPersonPrepayment.summ)
                                        }
                                    }

                                    // ОКРУГЛ (S1 x Р.2.Гр.14 / 100)
                                    BigDecimal var1 = ScriptUtils.round(s1 * (ndflPersonIncome.taxRate ?: 0) / 100)
                                    // где ВычисленноеЗначениеНалога = ОКРУГЛ (S1 x Р.2.Гр.14 / 100) - S2 - S3
                                    BigDecimal ВычисленноеЗначениеНалога = var1 - s2 - s3

                                    // Для ПНФ: | Р.2.Гр.16 – ВычисленноеЗначениеНалога | <= 1
                                    if (!((ndflPersonIncome.calculatedTax - ВычисленноеЗначениеНалога).abs() <= 1)) {
                                        String errMsg = String.format("Значение налога исчисленного в гр. 16 (%s р) не совпадает с расчетным (%s р)",
                                                ndflPersonIncome.calculatedTax, ВычисленноеЗначениеНалога)
                                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                        logger.logCheck("%s. %s.",
                                                section_2_16Fatal,
                                                LOG_TYPE_2_16, fioAndInpAndOperId, pathError, errMsg)
                                    }
                                }
                            }
                        }
                    }

                    /* todo Убрал проверку Заполнения Раздела 2 Графы 17, в SBRFNDFL-3997 её доработают
                    // СведДох6 НДФЛ.Расчет.Сумма.Удержанный (Графа 17)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.withholdingTax != null && ndflPersonIncome.withholdingTax != 0) {
                        // СведДох7.1
                        if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                                || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                                     "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                                && (ndflPersonIncome.overholdingTax == null || ndflPersonIncome.overholdingTax == 0)
                        ) {
                            // «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2»
                            if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax
                                    && ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значениям гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                        C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        } else if (((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                                || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                                     "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType != "02"))
                                && ndflPersonIncome.overholdingTax > 0
                        ) {
                            // «Графа 17 Раздел 2» = («Графа 16 Раздел 2» + «Графа 16 Раздел 2» предыдущей записи) = «Графа 24 Раздел 2» и «Графа 17 Раздел 2» <= ((«Графа 13 Раздел 2» - «Графа 16 Раздел 2») × 50%)
                            List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: new ArrayList<NdflPersonIncome>()
                            NdflPersonIncome ndflPersonIncomePreview = null
                            if (!ndflPersonIncomeCurrentList.isEmpty()) {
                                for (NdflPersonIncome ndflPersonIncomeCurrent in ndflPersonIncomeCurrentList) {
                                    if (ndflPersonIncomeCurrent.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                                            (ndflPersonIncomePreview == null || ndflPersonIncomePreview.incomeAccruedDate < ndflPersonIncomeCurrent.incomeAccruedDate)) {
                                        ndflPersonIncomePreview = ndflPersonIncomeCurrent
                                    }
                                }
                            }
                            if (!(ndflPersonIncome.withholdingTax == (ndflPersonIncome.calculatedTax ?: 0) + (ndflPersonIncomePreview.calculatedTax ?: 0))) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно сумме значений гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\") предыдущей записи",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncomePreview.calculatedTax ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                            if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                            if (!(ndflPersonIncome.withholdingTax <= (ScriptUtils.round(ndflPersonIncome.taxBase ?: 0, 0) - ndflPersonIncome.calculatedTax ?: 0) * 0.50)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не должно превышать 50%% от разности значение гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                        C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        } else if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14")
                                || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1544", "1545",
                                     "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                        ) {
                            if (!(ndflPersonIncome.withholdingTax == 0 || ndflPersonIncome.withholdingTax == null)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно \"0\"",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        } else if (!(ndflPersonIncome.incomeCode != null)) {
                            if (!(ndflPersonIncome.withholdingTax != ndflPersonIncome.taxSumm ?: 0)) {
                                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                        C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                        C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                                )
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                                logger.logCheck("%s. %s.",
                                        declarationService.isCheckFatal(DeclarationCheckCode.RNU_SECTION_2_17, declarationData.declarationTemplateId),
                                        LOG_TYPE_2_17, fioAndInpAndOperId, pathError, errMsg)
                            }
                        }
                    }*/

                    // СведДох9 НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
                    // Проверка не должна выполняться.
                    /*if (!ndflPersonIncome.isDummy() && ndflPersonIncome.refoundTax != null && ndflPersonIncome.refoundTax > 0) {
                        if (!(refoundTaxSum <= overholdingTaxSum)) {
                            String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") не должна превышать сумму значений гр.\"%s\" (\"%s\") для всех строк одной операции",
                                    C_REFOUND_TAX, refoundTaxSum ?: "0",
                                    C_OVERHOLDING_TAX, overholdingTaxSum ?: "0"
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.errorExp("%s. %s.", LOG_TYPE_2_20, fioAndInpAndOperId, pathError, errMsg)
                        }
                    }*/

                    // СведДох10 НДФЛ.Перечисление в бюджет.Срок (Графа 21)
                    if (!ndflPersonIncome.isDummy() && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxTransferDate != null) {
                        dateConditionDataListForBudget.each { dateConditionData ->
                            if ((dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) || dateConditionData.incomeCodes.isEmpty()) &&
                                    dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                                def checkedIncome = ndflPersonIncome
                                String errMsg = dateConditionData.checker.check(checkedIncome)
                                if (checkedIncome != null && errMsg != null) {
                                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, checkedIncome.rowNum ?: "")
                                    logger.logCheck("%s. %s.",
                                            section_2_21_fatal,
                                            LOG_TYPE_2_21, fioAndInpAndOperId, pathError, errMsg)
                                }
                            }
                        }
                    }
                }
            }
        }

        logForDebug("Проверки сведений о доходах (" + (System.currentTimeMillis() - time) + " мс)")
    }

    /**
     * Формирует суммарную строку дохода по нескольким строкам. Если ни в одной строке значение не заполнено, то сумма равна null
     */
    NdflPersonIncome sumIncomes(List<NdflPersonIncome> incomes) {
        def totalIncome = new NdflPersonIncome()
        for (def income : incomes) {
            if (income.totalDeductionsSumm != null) {
                totalIncome.totalDeductionsSumm = (totalIncome.totalDeductionsSumm ?: new BigDecimal(0)) + income.totalDeductionsSumm
            }
            if (income.incomePayoutSumm != null) {
                totalIncome.incomePayoutSumm = (totalIncome.incomePayoutSumm ?: new BigDecimal(0)) + income.incomePayoutSumm
            }
            if (income.incomeAccruedSumm != null) {
                totalIncome.incomeAccruedSumm = (totalIncome.incomeAccruedSumm ?: new BigDecimal(0)) + income.incomeAccruedSumm
            }
            if (income.refoundTax != null) {
                totalIncome.refoundTax = (totalIncome.refoundTax ?: 0L) + income.refoundTax
            }
            if (income.calculatedTax != null) {
                totalIncome.calculatedTax = (totalIncome.calculatedTax ?: new BigDecimal(0)) + income.calculatedTax
            }
            if (income.withholdingTax != null) {
                totalIncome.withholdingTax = (totalIncome.withholdingTax ?: new BigDecimal(0)) + income.withholdingTax
            }
            if (income.overholdingTax != null) {
                totalIncome.overholdingTax = (totalIncome.overholdingTax ?: new BigDecimal(0)) + income.overholdingTax
            }
            if (income.notHoldingTax != null) {
                totalIncome.notHoldingTax = (totalIncome.notHoldingTax ?: new BigDecimal(0)) + income.notHoldingTax
            }
            if (income.taxSumm != null) {
                totalIncome.taxSumm = (totalIncome.taxSumm ?: new BigDecimal(0)) + income.taxSumm
            }
            if (income.taxBase != null) {
                totalIncome.taxBase = (totalIncome.taxBase ?: new BigDecimal(0)) + income.taxBase
            }
        }
        return totalIncome
    }

    /**
     * Формирует суммарную строку дохода по нескольким строкам. Если ни в одной строке значение не заполнено, то сумма равна null
     */
    NdflPersonDeduction sumDeductions(List<NdflPersonDeduction> deductions) {
        def totalDeduction = new NdflPersonDeduction()
        for (def deduction : deductions) {
            if (deduction.incomeSumm != null) {
                totalDeduction.incomeSumm = (totalDeduction.incomeSumm ?: new BigDecimal(0)) + deduction.incomeSumm
            }
            if (deduction.notifSumm != null) {
                totalDeduction.notifSumm = (totalDeduction.notifSumm ?: new BigDecimal(0)) + deduction.notifSumm
            }
            if (deduction.periodCurrSumm != null) {
                totalDeduction.periodCurrSumm = (totalDeduction.periodCurrSumm ?: new BigDecimal(0)) + deduction.periodCurrSumm
            }
            if (deduction.periodPrevSumm != null) {
                totalDeduction.periodPrevSumm = (totalDeduction.periodPrevSumm ?: new BigDecimal(0)) + deduction.periodPrevSumm
            }
        }
        return totalDeduction
    }

    /**
     * Проверки Сведения о вычетах
     */
    def checkDataDeduction(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList,
                           List<NdflPersonDeduction> ndflPersonDeductionList, Map<Long, RegistryPerson> personMap) {

        long time = System.currentTimeMillis()

        Map<String, Map<String, NdflPersonIncome>> mapNdflPersonIncome = [:]
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            String operationIdNdflPersonId = "${ndflPersonIncome.operationId}_${ndflPersonIncome.ndflPersonId}"
            if (!mapNdflPersonIncome.containsKey(operationIdNdflPersonId)) {
                mapNdflPersonIncome.put(operationIdNdflPersonId, new LinkedHashMap<String, NdflPersonIncome>())
            }
            mapNdflPersonIncome.get(operationIdNdflPersonId).put(ndflPersonIncome.incomeAccruedDate ? ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate) : "", ndflPersonIncome)
        }

        Map<Long, Map<String, List<NdflPersonIncome>>> incomesByPersonIdAndOperationId =
                ndflPersonIncomeList.groupBy({ NdflPersonIncome it -> it.ndflPersonId }, { NdflPersonIncome it -> it.operationId })

        def col16CheckDeductionGroups_1 = ndflPersonDeductionList.findAll {
            it.notifType == "2"
        }.groupBy {
            new Col16CheckDeductionGroup_1Key(it.ndflPersonId, it.operationId, it.notifDate, it.notifNum, it.notifSource, it.notifSumm)
        }

        def col16CheckDeductionGroups_2 = ndflPersonDeductionList.findAll {
            it.notifType == "1"
        }.groupBy { new Col16CheckDeductionGroup_2Key(it.ndflPersonId, it.operationId) }

        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {
            ScriptUtils.checkInterrupted()

            def operationId = ndflPersonDeduction.operationId
            def allIncomesOfOperation = incomesByPersonIdAndOperationId.get(ndflPersonDeduction.ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonIncome>()

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

            // Выч0 Строка Раздела 3 не относится к операции с фиктивной строкой
            for (def income : allIncomesOfOperation) {
                if (income.isDummy()) {
                    String errMsg = "относится к операции, для которой в Разделе 2 имеется строка $income.rowNum (ФЛ: $ndflPersonFL.fio, " +
                            "ИНП: $ndflPersonFL.inp, ставка налога = 0, ID операции = 0), показывающая отсутствие операций по данному ФЛ"
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.errorExp("%s %s", "", fioAndInpAndOperId, pathError, errMsg)
                    break
                }
            }
            // Выч1 Документ о праве на налоговый вычет.Код источника (Графа 7)
            if (ndflPersonDeduction.notifType == "1" && ndflPersonDeduction.notifSource != "0000") {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                        C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                        C_TYPE_CODE, ndflPersonDeduction.typeCode ?: ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_7, fioAndInpAndOperId, pathError, errMsg)
            }

            // Выч2 Начисленный доход.Дата (Графы 10)
            // Если одновременно заполнены графы проверяемой строки Раздела 3 "Графа 10","Графа 11","Графа 12", "Графа 15"
            if (ndflPersonDeduction.incomeAccrued != null && ndflPersonDeduction.incomeCode != null &&
                    ndflPersonDeduction.incomeSumm != null && ndflPersonDeduction.periodCurrDate != null) {
                // Существует строка Раздела 2, для которой одновременно выполняются условия:
                // - "Раздел 2. Графа 2" = "Раздел 3. Графа 2" проверяемой строки (ИНП)
                // - "Раздел 2. Графа 3" = "Раздел 3. Графа 9" проверяемой строки (ID операции)
                // - "Раздел 2. Графа 6" = "Раздел 3. Графа 10" проверяемой строки (Дата начисления дохода)
                // - "Раздел 2. Графа 4" = "Раздел 3. Графа 11" проверяемой строки (Код дохода)
                // - "Раздел 2. Графа 10" = "Раздел 3. Графа 12" проверяемой строки (Сумма дохода)
                // - "Раздел 2. Графа 15" = "Раздел 3. Графа 15" проверяемой строки (Дата применения вычета)
                // - "Раздел 2. Графа 12" заполнена
                // Если выполняется хотя бы одно из условий:
                // КодАСНУ не равна ни одному из значений: 1000; 1001; 1002
                // Дата, указанная в "Раздел 3.Графа 15" не является последним календарным днём месяца
                List<NdflPersonIncome> incomeExists = allIncomesOfOperation.findAll { NdflPersonIncome income ->
                    String asnuCode = getRefAsnu().get(income.asnuId).code

                    income.incomeAccruedDate == ndflPersonDeduction.incomeAccrued && income.incomeCode == ndflPersonDeduction.incomeCode &&
                            income.incomeAccruedSumm == ndflPersonDeduction.incomeSumm && income.totalDeductionsSumm != null &&
                            (!["1000", "1001", "1002"].contains(asnuCode) || !isLastMonthDay(ndflPersonDeduction.getPeriodCurrDate()))
                }

                if (!incomeExists.isEmpty() && incomeExists.find { NdflPersonIncome income ->
                    income.taxDate == ndflPersonDeduction.periodCurrDate
                } == null) {
                    String errMsg = "В разделе 2 отсутствует соответствующая строка начисления, содержащая информацию о вычете, с параметрами " +
                            "\"ID операции\": $ndflPersonDeduction.operationId, \"Дата начисления\": ${formatDate(ndflPersonDeduction.incomeAccrued)}, " +
                            "\"Код дохода\": $ndflPersonDeduction.incomeCode, \"Сумма начисленного дохода\": $ndflPersonDeduction.incomeSumm, " +
                            "\"Дата НДФЛ\": ${formatDate(ndflPersonDeduction.periodCurrDate)}"
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.logCheck("%s. %s.",
                            section_3_10_fatal,
                            LOG_TYPE_3_10, fioAndInpAndOperId, pathError, errMsg)
                }
            }

            // Выч3 Применение вычета.Текущий период.Дата (Графы 15)
            if (ndflPersonDeduction.periodCurrDate != null) {
                // "Графа 15" принадлежит к отчетному периоду
                if (!dateRelateToCurrentPeriod(ndflPersonDeduction.periodCurrDate)) {
                    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
                    String strCorrPeriod = ""
                    if (departmentReportPeriod.getCorrectionDate() != null) {
                        strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy")
                    }
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\")\" не входит в отчетный период налоговой формы \"%s\"",
                            C_PERIOD_CURR_DATE, formatDate(ndflPersonDeduction.periodCurrDate),
                            departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + strCorrPeriod)
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.logCheck("%s. %s.",
                            section_2_15_fatal,
                            LOG_TYPE_3_15, fioAndInpAndOperId, pathError, errMsg)
                }
            }

            // Выч6 Применение вычета.Текущий период.Сумма (Графы 16)
            if (ndflPersonDeduction.notifType == "2") {
                def key = new Col16CheckDeductionGroup_1Key(ndflPersonDeduction.ndflPersonId, ndflPersonDeduction.operationId, ndflPersonDeduction.notifDate,
                        ndflPersonDeduction.notifNum, ndflPersonDeduction.notifSource, ndflPersonDeduction.notifSumm)
                List<NdflPersonDeduction> deductionsGroup = col16CheckDeductionGroups_1?.get(key) ?: new ArrayList<NdflPersonDeduction>()
                if (deductionsGroup) {
                    BigDecimal sum16 = (BigDecimal) deductionsGroup.sum { NdflPersonDeduction deduction -> deduction.periodCurrSumm ?: 0 } ?: 0
                    if (sum16 > ndflPersonDeduction.notifSumm) {
                        String errMsg = String.format("Раздел 3. ID операции: \"%s\". Для строк документа (тип: \"%s\", номер: \"%s\", дата: \"%s\", " +
                                "код источника:  \"%s\", сумма: \"%s\") сумма значений гр. \"Вычет.Текущий период.Сумма\" (%s) должна быть меньше или равна " +
                                "значения гр. \"Подтверждающий документ.Сумма\" (%s)",
                                ndflPersonDeduction.operationId, ndflPersonDeduction.notifType, ndflPersonDeduction.notifNum,
                                formatDate(ndflPersonDeduction.notifDate), ndflPersonDeduction.notifSource, ndflPersonDeduction.notifSumm,
                                sum16, ndflPersonDeduction.notifSumm
                        )
                        logger.logCheck(errMsg,
                                section_3_16_fatal,
                                LOG_TYPE_3_16, fioAndInpAndOperId)
                    }
                    deductionsGroup.clear()
                }
            }
            // Выч6.1
            if (ndflPersonDeduction.notifType == "1") {
                def key = new Col16CheckDeductionGroup_2Key(ndflPersonDeduction.ndflPersonId, ndflPersonDeduction.operationId)
                List<NdflPersonDeduction> deductionsGroup = col16CheckDeductionGroups_2?.get(key) ?: new ArrayList<NdflPersonDeduction>()
                if (deductionsGroup) {
                    BigDecimal sum16 = (BigDecimal) deductionsGroup.sum { NdflPersonDeduction deduction -> deduction.periodCurrSumm ?: 0 } ?: 0
                    BigDecimal sum8 = (BigDecimal) deductionsGroup.sum { NdflPersonDeduction deduction -> deduction.notifSumm ?: 0 } ?: 0
                    if (sum16 > sum8) {
                        String errMsg = String.format("Раздел 3. ID операции: \"%s\". Для строк, у которых указан тип: \"%s\",  сумма значений гр. \"%s\" (%s) должна быть меньше или равна сумме значений гр. \"%s\" (%s)",
                                ndflPersonDeduction.operationId, ndflPersonDeduction.notifType,
                                C_PERIOD_CURR_SUMM, sum16, C_NOTIF_SUMM, sum8
                        )
                        logger.logCheck(errMsg,
                                section_3_16_fatal,
                                LOG_TYPE_3_16, fioAndInpAndOperId)
                    }
                    deductionsGroup.clear()
                }
            }
        }
        logForDebug("Проверки сведений о вычетах (" + (System.currentTimeMillis() - time) + " мс)")
    }

    /**
     * Проверки для Раздел 4. Сведения о доходах в виде авансовых платежей
     */
    def checkDataPrepayment(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
                            List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, RegistryPerson> personMap) {
        long time = System.currentTimeMillis()

        Map<Long, NdflPerson> personByIdMap = ndflPersonList.collectEntries { [it.id, it] }
        Map<Long, Map<String, List<NdflPersonIncome>>> incomesByPersonIdAndOperationId =
                ndflPersonIncomeList.groupBy({ NdflPersonIncome it -> it.ndflPersonId }, { NdflPersonIncome it -> it.operationId })
        for (def prepayment : ndflPersonPrepaymentList) {
            def operationId = prepayment.operationId
            def allIncomesOfOperation = incomesByPersonIdAndOperationId.get(prepayment.ndflPersonId)?.get(operationId) ?: new ArrayList<NdflPersonIncome>()
            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(prepayment.ndflPersonId)
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [ndflPersonFL.fio, ndflPersonFL.inp, operationId])

            def person = personByIdMap[prepayment.ndflPersonId]

            // 0 Строка Раздела 4 не относится к операции с фиктивной строкой
            for (def income : allIncomesOfOperation) {
                if (income.isDummy()) {
                    String errMsg = "относится к операции, для которой в Разделе 2 имеется строка $income.rowNum (ФЛ: $ndflPersonFL.fio, " +
                            "ИНП: $ndflPersonFL.inp, ставка налога = 0, ID операции = 0), показывающая отсутствие операций по данному ФЛ"
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, prepayment.rowNum ?: "")
                    logger.errorExp("%s %s", "", fioAndInpAndOperId, pathError, errMsg)
                    break
                }
            }
            // 1 Заполнение Раздела 4 только для НП с кодом статуса = "6"
            if (person.status != "6" && person.inp == ndflPersonFL.inp) {
                String errMsg = String.format("Наличие строки некорректно, так как для ФЛ ИНП: %s Статус (Код) не равен \"6\"", person.inp)
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, prepayment.rowNum ?: "")
                logger.logCheck("%s. %s.",
                        true, LOG_TYPE_SECTION4, fioAndInpAndOperId, pathError, errMsg)
            }
        }

        logForDebug("Проверки сведений о доходах в виде авансовых платежей (" + (System.currentTimeMillis() - time) + " мс)")
    }

    boolean isDummy(List<NdflPersonIncome> incomes) {
        for (def income : incomes) {
            if (!income.isDummy()) {
                return false
            }
        }
        return true
    }

    class NdflPersonFL {
        String fio
        String inp

        NdflPersonFL(String fio, String inp) {
            this.fio = fio
            this.inp = inp
        }
    }

    /**
     * Класс для проверки заполненности полей
     */
    class ColumnFillConditionData {
        ColumnFillConditionChecker columnConditionCheckerAsIs
        ColumnFillConditionChecker columnConditionCheckerToBe
        String conditionMessage

        ColumnFillConditionData(ColumnFillConditionChecker columnConditionCheckerAsIs, ColumnFillConditionChecker columnConditionCheckerToBe, String conditionMessage) {
            this.columnConditionCheckerAsIs = columnConditionCheckerAsIs
            this.columnConditionCheckerToBe = columnConditionCheckerToBe
            this.conditionMessage = conditionMessage
        }
    }

    interface ColumnFillConditionChecker {
        boolean check(NdflPersonIncome ndflPersonIncome)
    }
    /**
     * Проверка: "Раздел 2. Графа 4,5 заполнены"
     */
    class Column4Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.incomeCode
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 4,5 заполнены"
     */
    class Column5Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.incomeType
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 6 заполнена"
     */
    class Column6Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 7 заполнена"
     */
    class Column7Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 8 заполнена"
     */
    class Column8Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.oktmo
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 9 заполнена"
     */
    class Column9Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !!ndflPersonIncome.kpp
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 10 заполнена"
     */
    class Column10Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 11 заполнена"
     */
    class Column11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 10 или 11 заполнена"
     */
    class Column10Or11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedSumm != null || ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 заполнены"
     */
    class Column7And11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 13 заполнены"
     */
    class Column13Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxBase != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы  14 заполнены"
     */
    class Column14Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxRate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 15 заполнены"
     */
    class Column15Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxDate != null
        }
    }

    /**
     * Проверка: "Раздел 2. Графы 16 заполнена"
     */
    class Column16Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.calculatedTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 17 заполнена"
     */
    class Column17Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.withholdingTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 18 заполнена"
     */
    class Column18Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.notHoldingTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 19 заполнена"
     */
    class Column19Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.overholdingTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 20 заполнена"
     */
    class Column20Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.refoundTax != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 21 заполнена"
     */
    class Column21Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxTransferDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 21 НЕ заполнена"
     */
    class Column21NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxTransferDate == null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 ИЛИ 22, 23, 24 заполнены"
     */
    class Column7And11Or22And23And24Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return (new Column7And11Fill().check(ndflPersonIncome)) || (new Column22And23And24Fill().check(ndflPersonIncome))
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 И 22, 23, 24 НЕ заполнены"
     */
    class Column7And11And22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !(new Column7And11Fill().check(ndflPersonIncome)) && (new Column22And23And24NotFill().check(ndflPersonIncome))
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 22, 23, 24 НЕ заполнены"
     */
    class Column22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.paymentDate == null && ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm == null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 22, 23, 24 заполнены"
     */
    class Column22And23And24Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.paymentDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm != null
        }
    }
    /**
     * 	Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна их них
     */
    class Column22And23And24FillOrColumn22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return (new Column22And23And24NotFill().check(ndflPersonIncome)) || (new Column22And23And24Fill().check(ndflPersonIncome))
        }
    }
    /**
     * 	Всегда возвращает true
     */
    class ColumnTrueFillOrNotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return true
        }
    }

    /**
     * Класс для получения рабочих дней
     */
    class DateConditionWorkDay {

        // Мапа рабочих дней со сдвигом
        private Map<Date, Date> workDayWithOffset0Cache
        private Map<Date, Date> workDayWithOffset1Cache
        private Map<Date, Date> workDayWithOffset30Cache
        private Map<Integer, Date> lastWorkDayOfTheYear
        CalendarService calendarService

        DateConditionWorkDay(CalendarService calendarService) {
            workDayWithOffset0Cache = [:]
            workDayWithOffset1Cache = [:]
            workDayWithOffset30Cache = [:]
            lastWorkDayOfTheYear = [:]
            this.calendarService = calendarService
        }

        /**
         * Возвращает дату рабочего дня, смещенного относительно даты startDate.
         *
         * @param startDate начальная дата, может быть и рабочим днем и выходным
         * @param offset на сколько рабочих дней необходимо сдвинуть начальную дату. Может быть меньше 0, тогда сдвигается в обратную сторону
         * @return смещенная на offset рабочих дней дата
         */
        Date getWorkDay(Date startDate, int offset) {
            Date resultDate
            if (offset == 0) {
                resultDate = workDayWithOffset0Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset0Cache.put(startDate, resultDate)
                }
            } else if (offset == 1) {
                resultDate = workDayWithOffset1Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset1Cache.put(startDate, resultDate)
                }
            } else if (offset == 30) {
                resultDate = workDayWithOffset30Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset30Cache.put(startDate, resultDate)
                }
            }
            return resultDate
        }

        Date getLastDayOfTheYear(int year) {
            Date resultDate = lastWorkDayOfTheYear.get(year)
            if (resultDate == null) {
                resultDate = calendarService.getLastWorkDayByYear(year)
                lastWorkDayOfTheYear.put(year, resultDate)
            }
            return resultDate
        }
    }

    /**
     * Класс для соотнесения вида проверки в зависимости от значений "Код вида дохода" и "Признак вида дохода"
     */
    class DateConditionData<T> {
        List<String> incomeCodes
        List<String> incomeTypes
        T checker
        String conditionMessage

        DateConditionData(List<String> incomeCodes, List<String> incomeTypes, T checker, String conditionMessage) {
            this.incomeCodes = incomeCodes
            this.incomeTypes = incomeTypes
            this.checker = checker
            this.conditionMessage = conditionMessage
        }
    }

    /**
     * Используется для проверки Доход.Дата.Начисление (Графа 6)
     */
    abstract class IncomeAccruedDateConditionChecker implements DateConditionChecker {
        /**
         * Дата выплаты может находится не в проверяемой строке, в таком случае checker выдаёт ту дату с которой сравнивал
         */
        Date getDateCompared(NdflPersonIncome checkedIncome) {
            return checkedIncome.incomePayoutDate
        }
    }

    /**
     * Используется для проверки НДФЛ.Перечисление в бюджет.Срок (Графа 21)
     */
    abstract class TaxTransferDateConditionChecker implements DateConditionCheckerForBudget {

        /**
         * "Дата перечисления в бюджет" может иметь значение "00.00.0000", для этого форматируем её этим способом.
         */
        static String formatTaxTransferDate(Date taxTransferDate) {
            String dateString = taxTransferDate.format(SharedConstants.DATE_FORMAT)
            if (dateString == SharedConstants.DATE_ZERO_AS_DATE) {
                return SharedConstants.DATE_ZERO_AS_STRING
            } else {
                return dateString
            }
        }
    }

    interface DateConditionChecker {
        /**
         * Выполняет проверку в строке раздела 2
         *
         * @param checkedIncome проверяемая строка раздела 2
         * @param allIncomesOf группа строк, относящиеся к проверяемой каким-то условием (например, по ид операции)
         * @return пройдена ли проверка
         */
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOf)
    }

    interface DateConditionCheckerForBudget {
        /**
         * Выполняет проверку в строке раздела 2
         *
         * @param checkedIncome проверяемая строка раздела 2
         * @return если проверка не пройдена, то сообщение с ошибкой, если пройдена {@code null}
         */
        String check(NdflPersonIncome checkedIncome)
    }

    /**
     * Если существует только одна строка, для которой одновременно выполняются условия:
     * 1) Заполнена "Графа 7"
     * 2) "Графа 2" = "Графа 2" проверяемой строки
     * 3) "Графа 3" = "Графа 3" проверяемой строки
     * 4) "Графа 4" = "Графа 4" проверяемой строки
     * 5) "Графа 5" = "Графа 5" проверяемой строки
     * то "Графе 6" проверяемой строки = "Графа 7" найденной строки
     */
    class Column6EqualsColumn7 extends IncomeAccruedDateConditionChecker {
        Date incomePayoutDate

        @Override
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOfOperation) {
            incomePayoutDate = null

            List<NdflPersonIncome> foundIncomes = allIncomesOfOperation.findAll {
                it.incomePayoutDate && it.incomeCode == checkedIncome.incomeCode && it.incomeType == checkedIncome.incomeType
            }
            if (1 == foundIncomes.size()) {
                incomePayoutDate = foundIncomes.get(0).incomePayoutDate
                return checkedIncome.incomeAccruedDate == foundIncomes.get(0).incomePayoutDate
            }
            return true
        }

        @Override
        Date getDateCompared(NdflPersonIncome checkedIncome) {
            return incomePayoutDate
        }
    }

    /**
     * Проверка "Последний рабочий день года"
     */
    class LastYearWorkDay extends IncomeAccruedDateConditionChecker {
        Date comparedDate

        @Override
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOfOperation) {
            if (checkedIncome.incomeAccruedDate == null) {
                return true
            }
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(checkedIncome.incomeAccruedDate)
            comparedDate = dateConditionWorkDay.getLastDayOfTheYear(calendar.get(Calendar.YEAR))
            return checkedIncome.incomeAccruedDate.equals(comparedDate)
        }

        Date getDateCompared(NdflPersonIncome checkedIncome) {
            return comparedDate
        }
    }

    /**
     * Проверка "Последний календарный день месяца"
     */
    class LastMonthCalendarDay extends IncomeAccruedDateConditionChecker {
        @Override
        boolean check(NdflPersonIncome checkedIncome, List<NdflPersonIncome> allIncomesOfOperation) {
            if (checkedIncome.incomeAccruedDate == null) {
                return true
            }
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(checkedIncome.incomeAccruedDate)
            int currentMonth = calendar.get(Calendar.MONTH)
            calendar.add(calendar.DATE, 1)
            int comparedMonth = calendar.get(Calendar.MONTH)
            return currentMonth != comparedMonth
        }
    }

    /**
     * Проверка: "Графа 21" = "Графа 7" + "1 рабочий день"
     */
    class Column21EqualsColumn7Plus1WorkingDay extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            Calendar calendar21 = Calendar.getInstance()
            calendar21.setTime(checkedIncome.taxTransferDate)

            // "Графа 7" + "1 рабочий день"
            int offset = 1
            Date workDay = dateConditionWorkDay.getWorkDay(checkedIncome.incomePayoutDate, offset)
            Calendar calendar7 = Calendar.getInstance()
            calendar7.setTime(workDay)

            if (calendar21.equals(calendar7)) {
                return null
            }

            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению гр. \"Дата выплаты дохода\" (%s) + 1 рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(calendar7.getTime()))
        }
    }

    /**
     * "Графа 21" == "Графа 7" + "30 дней"
     */
    class Column21EqualsColumn7Plus30WorkingDays extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            // "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
            Date incomePayoutPlus30CalendarDays = DateUtils.addDays(checkedIncome.incomePayoutDate, 30)
            Date incomePayoutPlus30CalendarDaysWorkingDay = dateConditionWorkDay.getWorkDay(incomePayoutPlus30CalendarDays, 0)

            if (checkedIncome.taxTransferDate.equals(incomePayoutPlus30CalendarDaysWorkingDay)) {
                return null
            }
            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению гр. \"Дата выплаты дохода\" (%s) + 30 календарных дней. Если дата попадает на выходной день, то дата переносится на следующий рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(incomePayoutPlus30CalendarDaysWorkingDay))
        }
    }
    /**
     * "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
     */
    class Column21EqualsColumn7LastDayOfMonth extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            if (checkedIncome.taxTransferDate == null || checkedIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance()
            calendar21.setTime(checkedIncome.taxTransferDate)

            Calendar calendar7 = Calendar.getInstance()
            calendar7.setTime(checkedIncome.incomePayoutDate)

            // находим последний день месяца
            calendar7.set(Calendar.DAY_OF_MONTH, calendar7.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date workDay = calendar7.getTime()
            // если последний день месяца приходится на выходной, то следующий первый рабочий день
            int offset = 0
            workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
            calendar7.setTime(workDay)

            if (calendar21.equals(calendar7)) {
                return null
            }
            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно последнему календарному дню месяца, указанного в гр.\"Дата выплаты дохода\" (%s). " +
                    "Если дата попадает на выходной день, то дата переносится на следующий рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(calendar7.getTime()))
        }
    }

    class Column21ForNaturalIncome extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            List<NdflPersonIncome> matchedIncomes = []
            NdflPersonFL checkedIncomeFl = ndflPersonFLMap.get(checkedIncome.ndflPersonId)
            for (NdflPersonIncome income : ndflPersonIncomeList) {
                if (income.getId() != checkedIncome.getId()) {
                    NdflPersonFL ndflPersonFl = ndflPersonFLMap.get(income.ndflPersonId)
                    if (checkedIncomeFl?.inp == ndflPersonFl.inp && !["02", "14"].contains(income.incomeType) && income.incomePayoutDate >= checkedIncome.incomePayoutDate && income.asnuId == checkedIncome.asnuId) {
                        matchedIncomes << income
                    }
                }
            }
            Calendar zeroDate = Calendar.getInstance()
            zeroDate.set(1901, Calendar.JANUARY, 1)
            if (matchedIncomes.isEmpty() && checkedIncome.taxTransferDate != SimpleDateUtils.toStartOfDay(zeroDate.getTime())) {
                return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению (00.00.0000), так как не найдена строка выплаты, у которой \"Признак дохода\" не равен 02 или 14",
                        formatTaxTransferDate(checkedIncome.taxTransferDate))
            } else if (matchedIncomes.isEmpty()) {
                return null
            } else {
                Collections.sort(matchedIncomes, new Comparator<NdflPersonIncome>() {
                    @Override
                    int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                        int payoutDate = o1.incomePayoutDate.compareTo(o2.incomePayoutDate)
                        if (payoutDate != 0) return payoutDate
                        return o1.taxTransferDate.compareTo(o2.taxTransferDate)
                    }
                })
                if (checkedIncome.getTaxTransferDate() == matchedIncomes.get(0).getTaxTransferDate()) {
                    return null
                } else {
                    return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно значению гр. \"Срок перечисления в бюджет\" (%s) строки выплаты, у которой \"Признак дохода\" не равен 02 или 14",
                            formatTaxTransferDate(checkedIncome.taxTransferDate),
                            formatTaxTransferDate(matchedIncomes.get(0).taxTransferDate))
                }
            }

        }
    }

    class Column21EqualsLastDayOfFirstMonthOfNextYear extends TaxTransferDateConditionChecker {
        @Override
        String check(NdflPersonIncome checkedIncome) {
            Calendar calendar7 = Calendar.getInstance()
            calendar7.setTime(checkedIncome.incomePayoutDate)
            calendar7.set(calendar7.get(Calendar.YEAR) + 1, Calendar.JANUARY, 31)

            Date referenceValue = dateConditionWorkDay.getWorkDay(calendar7.getTime(), 0)

            if (referenceValue == SimpleDateUtils.toStartOfDay(checkedIncome.taxTransferDate)) {
                return null
            }
            return String.format("Значение гр. \"Срок перечисления в бюджет\" (%s) должно быть равно последнему календарному дню месяца, следующего за годом указанным в гр. \"Дата выплаты дохода\" (%s). Если дата попадает на выходной день, то она переносится на следующий рабочий день. Корректное значение срока перечисления в бюджет: %s",
                    formatTaxTransferDate(checkedIncome.taxTransferDate),
                    ScriptUtils.formatDate(checkedIncome.incomePayoutDate),
                    ScriptUtils.formatDate(referenceValue))
        }
    }

    String formatDate(Date date) {
        return date ? ScriptUtils.formatDate(date) : ""
    }

    /**
     * Получить "Страны"
     * @return
     */
    Map<Long, String> getRefCountryCode() {
        if (countryCodeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.COUNTRY.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                countryCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return countryCodeCache
    }

    /**
     * Выгрузка из справочников по условию и версии
     * @param refBookId
     * @param whereClause
     * @return
     * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordVersionWhere(Long refBookId, String whereClause, Date version) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataVersionWhere(whereClause, version)
        if (refBookMap == null || refBookMap.size() == 0) {
            //throw new ScriptException("Не найдены записи справочника " + refBookId)
            return Collections.emptyMap()
        }
        return refBookMap
    }

    Map<Long, String> getRefDocumentTypeCode() {
        if (documentTypeCodeCache.size() == 0) {
            Map<Long, Map<String, RefBookValue>> refBookList = getRefDocumentType()
            refBookList.each { Long id, Map<String, RefBookValue> refBookValueMap ->
                documentTypeCodeCache.put(id, refBookValueMap?.get("CODE")?.getStringValue())
            }
        }
        return documentTypeCodeCache
    }

    /**
     * Получить "Статусы налогоплательщика"
     * @return
     */
    Map<Long, String> getRefTaxpayerStatusCode() {
        if (taxpayerStatusCodeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.TAXPAYER_STATUS.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                taxpayerStatusCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return taxpayerStatusCodeCache
    }

    /**
     * Получить "Виды доходов"
     * @return мапа , где ключ значение признака дохода, значение - список записей из справочника "Виды доходов" соответствующие данному признаку
     */
    Map<String, List<Map<String, RefBookValue>>> getRefIncomeType() {
        // Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
        Map<String, List<Map<String, RefBookValue>>> mapResult = [:]
        PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.INCOME_KIND.id)
        refBookList.each { Map<String, RefBookValue> refBookRow ->
            String mark = refBookRow?.MARK?.stringValue
            List<Map<String, RefBookValue>> refBookRowList = mapResult.get(mark)
            if (refBookRowList == null) {
                refBookRowList = []
            }
            refBookRowList.add(refBookRow)
            mapResult.put(mark, refBookRowList)
        }
        return mapResult
    }

    /**
     * Получить "Коды налоговых органов"
     * @return
     */
    List<String> getRefNotifSource() {
        if (taxInspectionCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.TAX_INSPECTION.id)
            refBookList.each { Map<String, RefBookValue> refBook ->
                taxInspectionCache.add(refBook?.CODE?.stringValue)
            }
        }
        return taxInspectionCache
    }

    /**
     * Проверка адреса на пустоту
     * @param Данные о ФЛ из формы
     * @return
     */
    boolean isPersonAddressEmpty(NdflPerson ndflPerson) {
        boolean emptyAddress = ScriptUtils.isEmpty(ndflPerson.regionCode) && ScriptUtils.isEmpty(ndflPerson.area) &&
                ScriptUtils.isEmpty(ndflPerson.city) && ScriptUtils.isEmpty(ndflPerson.locality) &&
                ScriptUtils.isEmpty(ndflPerson.street) && ScriptUtils.isEmpty(ndflPerson.house) &&
                ScriptUtils.isEmpty(ndflPerson.building) && ScriptUtils.isEmpty(ndflPerson.flat)
        return emptyAddress
    }

    void logFiasError(String fioAndInp, String pathError, String name, String value) {
        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "ФИАС"), fioAndInp, pathError,
                "Значение гр. \"" + name + "\" (\"" + (value ?: "") + "\") отсутствует в справочнике \"ФИАС\"")
    }

    void logFiasIndexError(String fioAndInp, String pathError, String name, String value) {
        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "ФИАС"), fioAndInp, pathError,
                "Значение гр. \"" + name + "\" (\"" + (value ?: "") + "\") не соответствует требуемому формату")
    }

    boolean checkRequiredAttribute(NdflPerson ndflPerson, String fioAndInp, String alias, String attributeName) {
        if (ndflPerson[alias] == null || (ndflPerson[alias]) instanceof String && (org.apache.commons.lang3.StringUtils.isBlank((String) ndflPerson[alias]) || ndflPerson[alias] == "0")) {
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            String msg
            if (ndflPerson[alias] == "0") {
                msg = "Значение гр. \"$attributeName\" не может быть равно \"0\""
            } else {
                msg = "Значение гр. \"$attributeName\" не указано"
            }
            logger.warnExp("%s. %s.", "Не указан обязательный реквизит ФЛ", fioAndInp, pathError, msg)
            return false
        }
        return true
    }

    int signOf(BigDecimal number) {
        return number > 0 ? 1 : number < 0 ? -1 : 0
    }

    /**
     * Проверяется заполненность согласно временному решению
     * Если сумма начисленного дохода равна сумме вычетов, будет ноль в графах:
     Раздел 2. Графа 13. Налоговая база
     Раздел 2. Графа 16. Сумма исчисленного налога
     Раздел 2. Графа 17. Сумма удержанного налога
     * @param checkingValue
     * @param incomeAccruedSum
     * @param totalDeductionSum
     * @return true - заполнен, false - не заполнен
     */
    boolean isPresentedByTempSolution(BigDecimal checkingValue, BigDecimal incomeAccruedSum, BigDecimal totalDeductionSum) {
        if (checkingValue == null) {
            return false
        }
        if (incomeAccruedSum != totalDeductionSum && checkingValue == new BigDecimal(0)) {
            return false
        }
        return true
    }

    // Проверка принадлежности даты к периоду формы
    boolean dateRelateToCurrentPeriod(Date date) {
        if (date == null || (date >= getReportPeriodCalendarStartDate() && date <= getReportPeriodEndDate())) {
            return true
        }
        return false
    }

    /**
     * Получить "Виды документов"
     */
    Map<Long, Map<String, RefBookValue>> getRefDocumentType() {
        if (documentTypeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.DOCUMENT_CODES.getId())
            refBookList.each { Map<String, RefBookValue> refBook ->
                documentTypeCache.put((Long) refBook?.id?.numberValue, refBook)
            }
        }
        return documentTypeCache
    }

    /**
     * Выгрузка из справочников по условию
     * @param refBookId
     * @param whereClause
     * @return
     * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordWhere(Long refBookId, String whereClause) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataWhere(whereClause)
        if (refBookMap == null || refBookMap.size() == 0) {
            //throw new ScriptException("Не найдены записи справочника " + refBookId)
            return Collections.emptyMap()
        }
        return refBookMap
    }

    /**
     * Получить "АСНУ"
     * @return
     */
    Map<Long, RefBookAsnu> getRefAsnu() {
        if (asnuCache.isEmpty()) {
            List<RefBookAsnu> asnuList = refBookService.findAllAsnu()
            asnuList.each { RefBookAsnu asnu ->
                Long asnuId = (Long) asnu?.id
                asnuCache.put(asnuId, asnu)
            }
        }
        return asnuCache
    }

    /**
     * Является ди дата последним календарным днем месяца
     * @param date проверяемая дата
     * @return
     */
    boolean isLastMonthDay(Date date) {
        if (!date) return false
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        int currentMonth = calendar.get(Calendar.MONTH)
        calendar.add(calendar.DATE, 1)
        int comparedMonth = calendar.get(Calendar.MONTH)
        return currentMonth != comparedMonth
    }

    /**
     * Получить все записи справочника по его идентификатору и коллекции идентификаторов записей справочника
     * @param refBookId - идентификатор справочника
     * @param recordIds - коллекция идентификаторов записей справочника
     * @return - возвращает мапу
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordIds(long refBookId, List<Long> recordIds) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordData(recordIds)
        if (refBookMap == null || refBookMap.size() == 0) {
            throw new ScriptException("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookMap
    }

    class CheckData {
        String msgFirst
        String msgLast
        boolean fatal

        CheckData(String msgFirst, String msgLast) {
            this.msgFirst = msgFirst
            this.msgLast = msgLast
            this.fatal = false
        }

        CheckData(String msgFirst, String msgLast, boolean fatal) {
            this.msgFirst = msgFirst
            this.msgLast = msgLast
            this.fatal = fatal
        }
    }

    @EqualsAndHashCode
    class Col16CheckDeductionGroup_1Key {
        Long ndflPersonId
        String operationId
        Date notifDate
        String notifNum
        String notifSource
        BigDecimal notifSumm

        public Col16CheckDeductionGroup_1Key(Long ndflPersonId, String operationId, Date notifDate, String notifNum, String notifSource, BigDecimal notifSumm) {
            this.ndflPersonId = ndflPersonId
            this.operationId = operationId
            this.notifDate = notifDate
            this.notifNum = notifNum
            this.notifSource = notifSource
            this.notifSumm = notifSumm
        }
    }

    @EqualsAndHashCode
    class Col16CheckDeductionGroup_2Key {
        long ndflPersonId
        String operationId

        public Col16CheckDeductionGroup_2Key(long ndflPersonId, String operationId) {
            this.ndflPersonId = ndflPersonId
            this.operationId = operationId
        }
    }
}

/**
 * XmlSlurper, проставляющий номера строк на элементы.
 * Честно взят со StackOverflow.
 */
class LineNumberingSlurper extends XmlSlurper {

    private static final String LINE_NUM_ATTR = "line"

    private Locator locator

    LineNumberingSlurper() throws ParserConfigurationException, SAXException {
        super()
    }

    @Override
    void setDocumentLocator(Locator locator) {
        this.locator = locator
    }

    @Override
    void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        Attributes2Impl newAttrs = new Attributes2Impl(attrs)
        newAttrs.addAttribute(uri, LINE_NUM_ATTR, LINE_NUM_ATTR, "ENTITY", locator.lineNumber as String)
        super.startElement(uri, localName, qName, newAttrs)
    }
}

/**
 * Класс инкапсулирующий данные необходимые для заполнения листов Excel файла
 */
class SheetFillerContext {

    private String departmentName

    private String reportDate

    private String period

    private String year

    private List<NdflPerson> ndflPersonList

    private List<NdflPersonIncome> ndflPersonIncomeList

    private List<NdflPersonDeduction> ndflPersonDeductionList

    private List<NdflPersonPrepayment> ndflPersonPrepaymentList

    private Map<Long, NdflPerson> idNdflPersonMap

    SheetFillerContext(String departmentName, String reportDate, String period, String year, List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList, List<NdflPersonPrepayment> ndflPersonPrepaymentList) {
        this.departmentName = departmentName
        this.reportDate = reportDate
        this.period = period
        this.year = year
        this.ndflPersonList = ndflPersonList
        this.ndflPersonIncomeList = ndflPersonIncomeList
        this.ndflPersonDeductionList = ndflPersonDeductionList
        this.ndflPersonPrepaymentList = ndflPersonPrepaymentList
    }

    SheetFillerContext(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList, List<NdflPersonPrepayment> ndflPersonPrepaymentList) {
        this.ndflPersonList = ndflPersonList
        this.ndflPersonIncomeList = ndflPersonIncomeList
        this.ndflPersonDeductionList = ndflPersonDeductionList
        this.ndflPersonPrepaymentList = ndflPersonPrepaymentList
    }

    SheetFillerContext(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList) {
        this.ndflPersonList = ndflPersonList
        this.ndflPersonIncomeList = ndflPersonIncomeList
    }

    String getDepartmentName() {
        return departmentName
    }

    String getReportDate() {
        return reportDate
    }

    String getPeriod() {
        return period
    }

    String getYear() {
        return year
    }

    List<NdflPerson> getNdflPersonList() {
        return ndflPersonList
    }

    List<NdflPersonIncome> getNdflPersonIncomeList() {
        return ndflPersonIncomeList
    }

    List<NdflPersonDeduction> getNdflPersonDeductionList() {
        return ndflPersonDeductionList
    }

    List<NdflPersonPrepayment> getNdflPersonPrepaymentList() {
        return ndflPersonPrepaymentList
    }

    Map<Long, NdflPerson> getIdNdflPersonMap() {
        if (idNdflPersonMap == null) {
            idNdflPersonMap = new HashMap<>()
            for (NdflPerson ndflPerson : ndflPersonList) {
                idNdflPersonMap.put(ndflPerson.getId(), ndflPerson)
            }
        }
        return idNdflPersonMap
    }
}

/**
 * Интерфейс отвечающий за заполнение листа Excel файла
 */
interface SheetFiller {
    void fillSheet(Workbook wb, SheetFillerContext context)
}

/**
 * Фабрика для получения экземплярая {@link SheetFiller} по индексу листа
 */
@TypeChecked
class SheetFillerFactory {
    static SheetFiller getSheetFiller(int sheetIndex) {
        switch (sheetIndex) {
            case 0:
                return new TitleSheetFiller()
            case 1:
                return new RequisitesSheetFiller()
            case 2:
                return new IncomesSheetFiller()
            case 3:
                return new DeductionsSheetFiller()
            case 4:
                return new PrepaymentSheetFiller()
            case 5:
                return new ReportXlsxSheetFiller()
            default: return null
        }
    }
}

/**
 * Содержит логику заполнения заголовка листа спецотчета РНУ НДФЛ
 */
@TypeChecked
class TitleSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, final SheetFillerContext context) {
        Sheet sheet = wb.getSheetAt(0)
        Cell cell0 = sheet.getRow(1).createCell(2)
        cell0.setCellValue(context.getDepartmentName())
        Cell cell1 = sheet.createRow(2).createCell(1)
        cell1.setCellValue(context.getReportDate())
        Cell cell2 = sheet.getRow(4).createCell(2)
        cell2.setCellValue(context.getPeriod() + " " + context.getYear())
    }
}

/**
 * Содержит логику заполнения реквизитов спецотчета РНУ НДФЛ
 */
@TypeChecked
class RequisitesSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        Sheet sheet = wb.getSheetAt(1)
        int index = 3
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        for (NdflPerson np : context.getNdflPersonList()) {
            ScriptUtils.checkInterrupted()
            Row row = sheet.createRow(index)
            Cell cell1 = row.createCell(1)
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(np.getRowNum().intValue())
            Cell cell2 = row.createCell(2)
            cell2.setCellStyle(centeredStyle)
            cell2.setCellValue(np.getInp() != null ? np.getInp() : "")
            Cell cell3 = row.createCell(3)
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(np.getLastName() != null ? np.getLastName() : "")
            Cell cell4 = row.createCell(4)
            cell4.setCellStyle(centeredStyle)
            cell4.setCellValue(np.getFirstName() != null ? np.getFirstName() : "")
            Cell cell5 = row.createCell(5)
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(np.getMiddleName() != null ? np.getMiddleName() : "")
            Cell cell6 = row.createCell(6)
            cell6.setCellStyle(centeredStyleDate)
            if (np.birthDay != null) {
                cell6.setCellValue(np.birthDay)
            }
            Cell cell7 = row.createCell(7)
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(np.getCitizenship() != null ? np.getCitizenship() : "")
            Cell cell8 = row.createCell(8)
            cell8.setCellStyle(centeredStyle)
            cell8.setCellValue(np.getInnNp() != null ? np.getInnNp() : "")
            Cell cell9 = row.createCell(9)
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(np.getInnForeign() != null ? np.getInnForeign() : "")
            Cell cell10 = row.createCell(10)
            cell10.setCellStyle(centeredStyle)
            cell10.setCellValue(np.getIdDocType() != null ? np.getIdDocType() : "")
            Cell cell11 = row.createCell(11)
            cell11.setCellStyle(centeredStyle)
            cell11.setCellValue(np.getIdDocNumber() != null ? np.getIdDocNumber() : "")
            Cell cell12 = row.createCell(12)
            cell12.setCellStyle(centeredStyle)
            cell12.setCellValue(np.getStatus() != null ? np.getStatus() : "")
            Cell cell13 = row.createCell(13)
            cell13.setCellStyle(textCenteredStyle)
            cell13.setCellValue(np.getRegionCode() != null ? np.getRegionCode() : "")
            Cell cell14 = row.createCell(14)
            cell14.setCellStyle(centeredStyle)
            cell14.setCellValue(np.getPostIndex() != null ? np.getPostIndex() : "")
            Cell cell15 = row.createCell(15)
            cell15.setCellStyle(centeredStyle)
            cell15.setCellValue(np.getArea() != null ? np.getArea() : "")
            Cell cell16 = row.createCell(16)
            cell16.setCellStyle(centeredStyle)
            cell16.setCellValue(np.getCity() != null ? np.getCity() : "")
            Cell cell17 = row.createCell(17)
            cell17.setCellStyle(centeredStyle)
            cell17.setCellValue(np.getLocality() != null ? np.getLocality() : "")
            Cell cell18 = row.createCell(18)
            cell18.setCellStyle(centeredStyle)
            cell18.setCellValue(np.getStreet() != null ? np.getStreet() : "")
            Cell cell19 = row.createCell(19)
            cell19.setCellStyle(centeredStyle)
            cell19.setCellValue(np.getHouse() != null ? np.getHouse() : "")
            Cell cell20 = row.createCell(20)
            cell20.setCellStyle(centeredStyle)
            cell20.setCellValue(np.getBuilding() != null ? np.getBuilding() : "")
            Cell cell21 = row.createCell(21)
            cell21.setCellStyle(centeredStyle)
            cell21.setCellValue(np.getFlat() != null ? np.getFlat() : "")
            Cell cell22 = row.createCell(22)
            cell22.setCellStyle(centeredStyle)
            cell22.setCellValue(np.snils != null ? np.snils : "")
            index++
        }
    }
}

/**
 * Содержит логику заполнения сведений о доходах спецотчета РНУ НДФЛ
 */
@TypeChecked
class IncomesSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        List<NdflPersonIncome> ndflPersonIncomeList = context.getNdflPersonIncomeList()
        Sheet sheet = wb.getSheetAt(2)
        int index = 3
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.createBorderStyle()
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPersonIncome npi : ndflPersonIncomeList) {
            ScriptUtils.checkInterrupted()

            Row row = sheet.createRow(index)
            Cell cell1 = row.createCell(1)
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npi.getRowNum().toString())
            Cell cell2 = row.createCell(2)
            cell2.setCellStyle(centeredStyle)
            String inp = context.getIdNdflPersonMap().get(npi.getNdflPersonId()).getInp()
            cell2.setCellValue(inp != null ? inp : "")
            Cell cell3 = row.createCell(3)
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(npi.getOperationId() != null ? npi.getOperationId() : "")
            Cell cell4 = row.createCell(4)
            cell4.setCellStyle(centeredStyle)
            cell4.setCellValue(npi.getIncomeCode() != null ? npi.getIncomeCode() : "")
            Cell cell5 = row.createCell(5)
            cell5.setCellStyle(textCenteredStyle)
            cell5.setCellValue(npi.getIncomeType() != null ? npi.getIncomeType() : "")
            Cell cell6 = row.createCell(6)
            cell6.setCellStyle(centeredStyleDate)
            if (npi.incomeAccruedDate != null) {
                cell6.setCellValue(npi.incomeAccruedDate)
            }
            Cell cell7 = row.createCell(7)
            cell7.setCellStyle(centeredStyleDate)
            if (npi.incomePayoutDate != null) {
                cell7.setCellValue(npi.incomePayoutDate)
            }
            Cell cell8 = row.createCell(8)
            cell8.setCellStyle(textCenteredStyle)
            cell8.setCellValue(npi.getKpp() != null ? npi.getKpp() : "")
            Cell cell9 = row.createCell(9)
            cell9.setCellStyle(textCenteredStyle)
            cell9.setCellValue(npi.getOktmo() != null ? npi.getOktmo() : "")
            Cell cell10 = row.createCell(10)
            cell10.setCellStyle(numberCenteredStyle)
            if (npi.incomeAccruedSumm != null) {
                cell10.setCellValue(npi.incomeAccruedSumm.doubleValue())
            }
            Cell cell11 = row.createCell(11)
            cell11.setCellStyle(numberCenteredStyle)
            if (npi.incomePayoutSumm != null) {
                cell11.setCellValue(npi.incomePayoutSumm.doubleValue())
            }
            Cell cell12 = row.createCell(12)
            cell12.setCellStyle(numberCenteredStyle)
            if (npi.totalDeductionsSumm != null) {
                cell12.setCellValue(npi.totalDeductionsSumm.doubleValue())
            }
            Cell cell13 = row.createCell(13)
            cell13.setCellStyle(numberCenteredStyle)
            if (npi.taxBase != null) {
                cell13.setCellValue(npi.taxBase.doubleValue())
            }
            Cell cell14 = row.createCell(14)
            cell14.setCellStyle(borderStyle)
            if (npi.taxRate != null) {
                cell14.setCellValue(npi.taxRate)
            }
            Cell cell15 = row.createCell(15)
            cell15.setCellStyle(centeredStyleDate)
            if (npi.taxDate != null) {
                cell15.setCellValue(npi.taxDate)
            }

            Cell cell16 = row.createCell(16)
            cell16.setCellStyle(numberCenteredStyle)
            if (npi.calculatedTax != null) {
                cell16.setCellValue(npi.calculatedTax.doubleValue())
            }
            Cell cell17 = row.createCell(17)
            cell17.setCellStyle(numberCenteredStyle)
            if (npi.withholdingTax != null) {
                cell17.setCellValue(npi.withholdingTax.doubleValue())
            }
            Cell cell18 = row.createCell(18)
            cell18.setCellStyle(numberCenteredStyle)
            if (npi.notHoldingTax != null) {
                cell18.setCellValue(npi.notHoldingTax.doubleValue())
            }
            Cell cell19 = row.createCell(19)
            cell19.setCellStyle(numberCenteredStyle)
            if (npi.overholdingTax != null) {
                cell19.setCellValue(npi.overholdingTax.doubleValue())
            }
            Cell cell20 = row.createCell(20)
            cell20.setCellStyle(numberCenteredStyle)
            if (npi.refoundTax != null) {
                cell20.setCellValue(npi.refoundTax.doubleValue())
            }
            Cell cell21 = row.createCell(21)
            if (npi.taxTransferDate != null) {
                if (npi.taxTransferDate.format(SharedConstants.DATE_FORMAT) == SharedConstants.DATE_ZERO_AS_DATE) {
                    cell21.setCellStyle(centeredStyle)
                    cell21.setCellValue(SharedConstants.DATE_ZERO_AS_STRING)
                } else {
                    cell21.setCellStyle(centeredStyleDate)
                    cell21.setCellValue(npi.taxTransferDate)
                }
            } else {
                cell21.setCellStyle(centeredStyle)
            }
            Cell cell22 = row.createCell(22)
            cell22.setCellStyle(centeredStyleDate)
            if (npi.paymentDate != null) {
                cell22.setCellValue(npi.paymentDate)
            }
            Cell cell23 = row.createCell(23)
            cell23.setCellStyle(centeredStyle)
            cell23.setCellValue(npi.getPaymentNumber() != null ? npi.getPaymentNumber() : "")
            Cell cell24 = row.createCell(24)
            cell24.setCellStyle(numberCenteredStyle)
            if (npi.taxSumm != null) {
                cell24.setCellValue(npi.taxSumm.intValue())
            }
            index++
        }
    }
}

/**
 * Содержит логику заполнения сведений о вычетах спецотчета РНУ НДФЛ
 */
@TypeChecked
class DeductionsSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        List<NdflPersonDeduction> ndflPersonDeductionList = context.getNdflPersonDeductionList()
        Sheet sheet = wb.getSheetAt(3)
        int index = 3
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPersonDeduction npd : ndflPersonDeductionList) {
            ScriptUtils.checkInterrupted()

            Row row = sheet.createRow(index)
            Cell cell1 = row.createCell(1)
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npd.rowNum.intValue())
            Cell cell2 = row.createCell(2)
            cell2.setCellStyle(centeredStyle)
            String inp = context.getIdNdflPersonMap().get(npd.getNdflPersonId()).getInp()
            cell2.setCellValue(inp != null ? inp : "")
            Cell cell3 = row.createCell(3)
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(npd.getTypeCode() != null ? npd.getTypeCode() : "")
            Cell cell4 = row.createCell(4)
            cell4.setCellStyle(centeredStyle)
            cell4.setCellValue(npd.getNotifType() != null ? npd.getNotifType() : "")
            Cell cell5 = row.createCell(5)
            cell5.setCellStyle(centeredStyleDate)
            if (npd.notifDate != null) {
                cell5.setCellValue(npd.notifDate)
            }
            Cell cell6 = row.createCell(6)
            cell6.setCellStyle(centeredStyle)
            cell6.setCellValue(npd.getNotifNum() != null ? npd.getNotifNum() : "б/н")
            Cell cell7 = row.createCell(7)
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(npd.getNotifSource() != null ? npd.getNotifSource() : "")
            Cell cell8 = row.createCell(8)
            cell8.setCellStyle(numberCenteredStyle)
            if (npd.notifSumm != null) {
                cell8.setCellValue(npd.notifSumm.doubleValue())
            }
            Cell cell9 = row.createCell(9)
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(npd.getOperationId() != null ? npd.getOperationId() : "")
            Cell cell10 = row.createCell(10)
            cell10.setCellStyle(centeredStyleDate)
            if (npd.incomeAccrued != null) {
                cell10.setCellValue(npd.incomeAccrued)
            }
            Cell cell11 = row.createCell(11)
            cell11.setCellStyle(centeredStyle)
            cell11.setCellValue(npd.getIncomeCode() != null ? npd.getIncomeCode() : "")
            Cell cell12 = row.createCell(12)
            cell12.setCellStyle(numberCenteredStyle)
            if (npd.incomeSumm != null) {
                cell12.setCellValue(npd.incomeSumm.doubleValue())
            }
            Cell cell13 = row.createCell(13)
            cell13.setCellStyle(centeredStyleDate)
            if (npd.periodPrevDate != null) {
                cell13.setCellValue(npd.periodPrevDate)
            }
            Cell cell14 = row.createCell(14)
            cell14.setCellStyle(numberCenteredStyle)
            if (npd.periodPrevSumm != null) {
                cell14.setCellValue(npd.periodPrevSumm.doubleValue())
            }
            Cell cell15 = row.createCell(15)
            cell15.setCellStyle(centeredStyleDate)
            if (npd.periodCurrDate != null) {
                cell15.setCellValue(npd.periodCurrDate)
            }
            Cell cell16 = row.createCell(16)
            cell16.setCellStyle(numberCenteredStyle)
            if (npd.periodCurrSumm != null) {
                cell16.setCellValue(npd.periodCurrSumm.doubleValue())
            }
            index++
        }
    }
}

/**
 * Содержит логику заполнения сведений об авансах спецотчета РНУ НДФЛ
 */
@TypeChecked
class PrepaymentSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = context.getNdflPersonPrepaymentList()
        Sheet sheet = wb.getSheetAt(4)
        int index = 3
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPersonPrepayment npp : ndflPersonPrepaymentList) {
            ScriptUtils.checkInterrupted()

            Row row = sheet.createRow(index)
            Cell cell1 = row.createCell(1)
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npp.rowNum.doubleValue())
            Cell cell2 = row.createCell(2)
            cell2.setCellStyle(centeredStyle)
            String inp = context.getIdNdflPersonMap().get(npp.getNdflPersonId()).getInp()
            cell2.setCellValue(inp != null ? inp : "")
            Cell cell3 = row.createCell(3)
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(npp.getOperationId() != null ? npp.getOperationId() : "")
            Cell cell4 = row.createCell(4)
            cell4.setCellStyle(numberCenteredStyle)
            if (npp.summ != null) {
                cell4.setCellValue(npp.summ.doubleValue())
            }
            Cell cell5 = row.createCell(5)
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(npp.getNotifNum() != null ? npp.getNotifNum() : "")
            Cell cell6 = row.createCell(6)
            cell6.setCellStyle(centeredStyleDate)
            if (npp.notifDate != null) {
                cell6.setCellValue(npp.notifDate)
            }
            Cell cell7 = row.createCell(7)
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(npp.getNotifSource() != null ? npp.getNotifSource() : "")
            index++
        }
    }
}

/**
 * Содержит логику заполнения листов для выгрузки данных формы РНУ НДФЛ в Excel
 */
@TypeChecked
class ExportDeclarationDataSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        // Сдвиг начальной позиции строки на размер шапки таблицы
        final int OFFSET = 2
        // Указатель на индекс позиции строки
        int pointer = OFFSET
        // Максимальное количество строк для заполнеиния на одном листе
        final int MAX_ROWS = 1_000_000
        // Используемый формат даты
        final String DATE_FORMAT = SharedConstants.DATE_FORMAT
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.createVerticalByTopHorizontalByCenter()
        CellStyle centeredStyleDate = styler.createVerticalByTopHorizontalByCenterDate()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()

        List<Map<NdflPerson, List<ExcelTemplateRow>>> excelData = []
        Map<NdflPerson, List<ExcelTemplateRow>> sheetData = [:]
        int sheetRowsCounter = OFFSET
        for (NdflPerson np : context.getNdflPersonList()) {
            List<ExcelTemplateRow> ndflPersonRows = []
            // Сортировка каждого вида операции по № пп
            Collections.sort(np.incomes, new Comparator<NdflPersonIncome>() {
                @Override
                int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                    return o1.rowNum <=> o2.rowNum
                }
            })
            Collections.sort(np.deductions, new Comparator<NdflPersonDeduction>() {
                @Override
                int compare(NdflPersonDeduction o1, NdflPersonDeduction o2) {
                    return o1.rowNum <=> o2.rowNum
                }
            })
            Collections.sort(np.prepayments, new Comparator<NdflPersonPrepayment>() {
                @Override
                int compare(NdflPersonPrepayment o1, NdflPersonPrepayment o2) {
                    return o1.rowNum <=> o2.rowNum
                }
            })

            Map<String, List<NdflPersonIncome>> incomesGroupedByOperationId = [:]
            for (NdflPersonIncome income : np.incomes) {
                List<NdflPersonIncome> group = incomesGroupedByOperationId.get(income.operationId)
                if (!group) {
                    incomesGroupedByOperationId.put(income.operationId, [income])
                } else {
                    group << income
                }
            }

            for (List<NdflPersonIncome> entry : incomesGroupedByOperationId.values()) {
                int rowCounter = 0
                entry.each { NdflPersonIncome income ->
                    List<NdflPersonDeduction> matchedDeductions = np.deductions.findAll { NdflPersonDeduction deduction ->
                        deduction.operationId == income.operationId &&
                                income.incomeAccruedDate == deduction.incomeAccrued &&
                                income.incomeCode == deduction.incomeCode &&
                                income.incomeAccruedSumm == deduction.incomeSumm &&
                                income.taxDate == deduction.periodCurrDate
                    }
                    if (income.totalDeductionsSumm && !matchedDeductions.isEmpty()) {
                        matchedDeductions.eachWithIndex { NdflPersonDeduction deduction, int index ->
                            ExcelTemplateRow row = new ExcelTemplateRow()
                            if (index == 0) {
                                row.setNdflPersonIncome(income)
                                row.setNdflPersonDeduction(deduction)
                                np.deductions.remove(deduction)
                                Iterator<NdflPersonPrepayment> prepaymentIterator = np.prepayments.iterator()
                                while (prepaymentIterator.hasNext()) {
                                    NdflPersonPrepayment prepayment = prepaymentIterator.next()
                                    if (prepayment.operationId == entry.get(0).operationId) {
                                        row.setNdflPersonPrepayment(prepayment)
                                        prepaymentIterator.remove()
                                        break
                                    }
                                }
                            } else {
                                row.setNdflPersonDeduction(deduction)
                                np.deductions.remove(deduction)
                                Iterator<NdflPersonPrepayment> prepaymentIterator = np.prepayments.iterator()
                                while (prepaymentIterator.hasNext()) {
                                    NdflPersonPrepayment prepayment = prepaymentIterator.next()
                                    if (prepayment.operationId == entry.get(0).operationId) {
                                        row.setNdflPersonPrepayment(prepayment)
                                        prepaymentIterator.remove()
                                        break
                                    }
                                }
                            }
                            ndflPersonRows << row
                            rowCounter++
                        }
                    } else {
                        ExcelTemplateRow row = new ExcelTemplateRow()
                        row.setNdflPersonIncome(income)
                        Iterator<NdflPersonPrepayment> prepaymentIterator = np.prepayments.iterator()
                        while (prepaymentIterator.hasNext()) {
                            NdflPersonPrepayment prepayment = prepaymentIterator.next()
                            if (prepayment.operationId == entry.get(0).operationId) {
                                row.setNdflPersonPrepayment(prepayment)
                                prepaymentIterator.remove()
                                break
                            }
                        }
                        ndflPersonRows << row
                        rowCounter++
                    }
                }
                Iterator<NdflPersonDeduction> deductionIterator = np.deductions.iterator()
                while (deductionIterator.hasNext()) {
                    NdflPersonDeduction deduction = deductionIterator.next()
                    if (deduction.operationId == entry.get(0).operationId) {
                        ExcelTemplateRow row = new ExcelTemplateRow()
                        row.setNdflPersonDeduction(deduction)
                        deductionIterator.remove()
                        Iterator<NdflPersonPrepayment> prepaymentIterator = np.prepayments.iterator()
                        while (prepaymentIterator.hasNext()) {
                            NdflPersonPrepayment prepayment = prepaymentIterator.next()
                            if (prepayment.operationId == entry.get(0).operationId) {
                                row.setNdflPersonPrepayment(prepayment)
                                prepaymentIterator.remove()
                                break
                            }
                        }
                        ndflPersonRows << row
                        rowCounter++
                    }
                }
                Iterator<NdflPersonPrepayment> prepaymentIterator = np.prepayments.iterator()
                while (prepaymentIterator.hasNext()) {
                    NdflPersonPrepayment prepayment = prepaymentIterator.next()
                    if (prepayment.operationId == entry.get(0).operationId) {
                        ExcelTemplateRow row = new ExcelTemplateRow()
                        row.setNdflPersonPrepayment(prepayment)
                        prepaymentIterator.remove()
                        ndflPersonRows << row
                        rowCounter++
                    }
                }
            }
            if ((ndflPersonRows.size() + sheetRowsCounter) <= MAX_ROWS) {
                sheetData.put(np, ndflPersonRows)
                sheetRowsCounter += ndflPersonRows.size()
            } else {
                excelData << sheetData
                sheetData = [:]
                sheetData.put(np, ndflPersonRows)
                sheetRowsCounter = ndflPersonRows.size()
            }
        }
        excelData << sheetData
        for (int i = 2; i <= excelData.size(); i++) {
            wb.cloneSheet(1)
            wb.setSheetName(i, "РНУ НДФЛ (" + (i - 1) + ")")
        }
        PrimaryRnuNdfl.sxssfWorkbook = new SXSSFWorkbook(wb as XSSFWorkbook, 100, true)
        int sheetIndex = 1
        for (Map<NdflPerson, List<ExcelTemplateRow>> currSheetData : excelData) {
            sheetIndex++
            Sheet sheet = PrimaryRnuNdfl.sxssfWorkbook.getSheetAt(sheetIndex - 1)
            pointer = OFFSET
            for (Map.Entry<NdflPerson, List<ExcelTemplateRow>> entry : currSheetData) {
                NdflPerson np = entry.getKey()
                for (ExcelTemplateRow rowData : entry.getValue()) {
                    StringBuilder cell0Value = new StringBuilder(np.id.toString())
                    cell0Value.append("_")
                    Row row = sheet.createRow(pointer)
                    // Заполненние данными из раздела "Реквизиты"
                    Cell cell_0 = row.createCell(0)
                    cell_0.setCellStyle(centeredStyle)
                    Cell cell_1 = row.createCell(1)
                    cell_1.setCellStyle(centeredStyle)
                    cell_1.setCellType(Cell.CELL_TYPE_STRING)
                    if (rowData.ndflPersonIncome) {
                        cell_1.setCellValue(rowData.ndflPersonIncome?.rowNum != null ? rowData.ndflPersonIncome?.rowNum?.toString() : "")
                    }
                    Cell cell_2 = row.createCell(2)
                    cell_2.setCellStyle(textCenteredStyle)
                    cell_2.setCellValue(np.getInp() != null ? np.getInp() : "")
                    Cell cell_3 = row.createCell(3)
                    cell_3.setCellStyle(centeredStyle)
                    cell_3.setCellValue(np.getLastName() != null ? np.getLastName() : "")
                    Cell cell_4 = row.createCell(4)
                    cell_4.setCellStyle(centeredStyle)
                    cell_4.setCellValue(np.getFirstName() != null ? np.getFirstName() : "")
                    Cell cell_5 = row.createCell(5)
                    cell_5.setCellStyle(centeredStyle)
                    cell_5.setCellValue(np.getMiddleName() != null ? np.getMiddleName() : "")
                    Cell cell_6 = row.createCell(6)
                    cell_6.setCellStyle(centeredStyleDate)
                    cell_6.setCellValue(np.birthDay?.format(DATE_FORMAT) ?: "")
                    Cell cell_7 = row.createCell(7)
                    cell_7.setCellStyle(textCenteredStyle)
                    cell_7.setCellValue(np.getCitizenship() != null ? np.getCitizenship() : "")
                    Cell cell_8 = row.createCell(8)
                    cell_8.setCellStyle(textCenteredStyle)
                    cell_8.setCellValue(np.getInnNp() != null ? np.getInnNp() : "")
                    Cell cell_9 = row.createCell(9)
                    cell_9.setCellStyle(textCenteredStyle)
                    cell_9.setCellValue(np.getInnForeign() != null ? np.getInnForeign() : "")
                    Cell cell_10 = row.createCell(10)
                    cell_10.setCellStyle(textCenteredStyle)
                    cell_10.setCellValue(np.getIdDocType() != null ? np.getIdDocType() : "")
                    Cell cell_11 = row.createCell(11)
                    cell_11.setCellStyle(textCenteredStyle)
                    cell_11.setCellValue(np.getIdDocNumber() != null ? np.getIdDocNumber() : "")
                    Cell cell_12 = row.createCell(12)
                    cell_12.setCellStyle(centeredStyle)
                    cell_12.setCellValue(np.getStatus() != null ? np.getStatus() : "")
                    Cell cell_13 = row.createCell(13)
                    cell_13.setCellStyle(textCenteredStyle)
                    cell_13.setCellValue(np.getRegionCode() != null ? np.getRegionCode() : "")
                    Cell cell_14 = row.createCell(14)
                    cell_14.setCellStyle(textCenteredStyle)
                    cell_14.setCellValue(np.getPostIndex() != null ? np.getPostIndex() : "")
                    Cell cell_15 = row.createCell(15)
                    cell_15.setCellStyle(textCenteredStyle)
                    cell_15.setCellValue(np.getArea() != null ? np.getArea() : "")
                    Cell cell_16 = row.createCell(16)
                    cell_16.setCellStyle(textCenteredStyle)
                    cell_16.setCellValue(np.getCity() != null ? np.getCity() : "")
                    Cell cell_17 = row.createCell(17)
                    cell_17.setCellStyle(textCenteredStyle)
                    cell_17.setCellValue(np.getLocality() != null ? np.getLocality() : "")
                    Cell cell_18 = row.createCell(18)
                    cell_18.setCellStyle(textCenteredStyle)
                    cell_18.setCellValue(np.getStreet() != null ? np.getStreet() : "")
                    Cell cell_19 = row.createCell(19)
                    cell_19.setCellStyle(textCenteredStyle)
                    cell_19.setCellValue(np.getHouse() != null ? np.getHouse() : "")
                    Cell cell_20 = row.createCell(20)
                    cell_20.setCellStyle(textCenteredStyle)
                    cell_20.setCellValue(np.getBuilding() != null ? np.getBuilding() : "")
                    Cell cell_21 = row.createCell(21)
                    cell_21.setCellStyle(textCenteredStyle)
                    cell_21.setCellValue(np.getFlat() != null ? np.getFlat() : "")
                    Cell cell_22 = row.createCell(22)
                    cell_22.setCellStyle(textCenteredStyle)
                    cell_22.setCellValue(np.snils != null ? np.snils : "")
                    // Заполнение данными из раздела "Сведения о доходах"
                    Cell cell_23 = row.createCell(23)
                    cell_23.setCellStyle(textCenteredStyle)
                    Cell cell_24 = row.createCell(24)
                    cell_24.setCellStyle(textCenteredStyle)
                    Cell cell_25 = row.createCell(25)
                    cell_25.setCellStyle(textCenteredStyle)
                    Cell cell_26 = row.createCell(26)
                    cell_26.setCellStyle(centeredStyleDate)
                    Cell cell_27 = row.createCell(27)
                    cell_27.setCellStyle(centeredStyleDate)
                    Cell cell_28 = row.createCell(28)
                    cell_28.setCellStyle(textCenteredStyle)
                    Cell cell_29 = row.createCell(29)
                    cell_29.setCellStyle(textCenteredStyle)
                    Cell cell_30 = row.createCell(30)
                    cell_30.setCellStyle(numberCenteredStyle)
                    Cell cell_31 = row.createCell(31)
                    cell_31.setCellStyle(numberCenteredStyle)
                    Cell cell_32 = row.createCell(32)
                    cell_32.setCellStyle(numberCenteredStyle)
                    Cell cell_33 = row.createCell(33)
                    cell_33.setCellStyle(numberCenteredStyle)
                    Cell cell_34 = row.createCell(34)
                    cell_34.setCellStyle(centeredStyle)
                    Cell cell_35 = row.createCell(35)
                    cell_35.setCellStyle(centeredStyleDate)
                    Cell cell_36 = row.createCell(36)
                    cell_36.setCellStyle(numberCenteredStyle)
                    Cell cell_37 = row.createCell(37)
                    cell_37.setCellStyle(numberCenteredStyle)
                    Cell cell_38 = row.createCell(38)
                    cell_38.setCellStyle(numberCenteredStyle)
                    Cell cell_39 = row.createCell(39)
                    cell_39.setCellStyle(numberCenteredStyle)
                    Cell cell_40 = row.createCell(40)
                    cell_40.setCellStyle(numberCenteredStyle)
                    Cell cell_41 = row.createCell(41)
                    cell_41.setCellStyle(centeredStyle)
                    Cell cell_42 = row.createCell(42)
                    cell_42.setCellStyle(centeredStyleDate)
                    Cell cell_43 = row.createCell(43)
                    cell_43.setCellStyle(textCenteredStyle)
                    Cell cell_44 = row.createCell(44)
                    cell_44.setCellStyle(numberCenteredStyle)
                    if (rowData.ndflPersonIncome) {
                        NdflPersonIncome npi = rowData.ndflPersonIncome
                        cell0Value.append(npi.id.toString())
                        cell_23.setCellValue(npi.getOperationId() != null ? npi.getOperationId() : "")
                        cell_24.setCellValue(npi.getIncomeCode() != null ? npi.getIncomeCode() : "")
                        cell_25.setCellValue(npi.getIncomeType() != null ? npi.getIncomeType() : "")
                        cell_26.setCellValue(npi.incomeAccruedDate?.format(DATE_FORMAT) ?: "")
                        cell_27.setCellValue(npi.incomePayoutDate?.format(DATE_FORMAT) ?: "")
                        cell_28.setCellValue(npi.getKpp() != null ? npi.getKpp() : "")
                        cell_29.setCellValue(npi.getOktmo() != null ? npi.getOktmo() : "")
                        if (npi.incomeAccruedSumm != null) {
                            cell_30.setCellValue(npi.incomeAccruedSumm.doubleValue())
                        }
                        if (npi.incomePayoutSumm != null) {
                            cell_31.setCellValue(npi.incomePayoutSumm.doubleValue())
                        }
                        if (npi.totalDeductionsSumm != null) {
                            cell_32.setCellValue(npi.totalDeductionsSumm.doubleValue())
                        }
                        if (npi.taxBase != null) {
                            cell_33.setCellValue(npi.taxBase.doubleValue())
                        }
                        if (npi.taxRate != null) {
                            cell_34.setCellValue(npi.taxRate)
                        }
                        cell_35.setCellValue(npi.taxDate?.format(DATE_FORMAT) ?: "")
                        if (npi.calculatedTax != null) {
                            cell_36.setCellValue(npi.calculatedTax.doubleValue())
                        }
                        if (npi.withholdingTax != null) {
                            cell_37.setCellValue(npi.withholdingTax.doubleValue())
                        }
                        if (npi.notHoldingTax != null) {
                            cell_38.setCellValue(npi.notHoldingTax.doubleValue())
                        }
                        if (npi.overholdingTax != null) {
                            cell_39.setCellValue(npi.overholdingTax.doubleValue())
                        }
                        if (npi.refoundTax != null) {
                            cell_40.setCellValue(npi.refoundTax.doubleValue())
                        }
                        if (npi.taxTransferDate?.format(SharedConstants.DATE_FORMAT) == SharedConstants.DATE_ZERO_AS_DATE) {
                            cell_41.setCellStyle(centeredStyle)
                            cell_41.setCellValue(SharedConstants.DATE_ZERO_AS_STRING)
                        } else {
                            cell_41.setCellStyle(centeredStyleDate)
                            cell_41.setCellValue(npi.taxTransferDate?.format(DATE_FORMAT) ?: "")
                        }
                        cell_42.setCellValue(npi.paymentDate?.format(DATE_FORMAT) ?: "")
                        cell_43.setCellValue(npi.getPaymentNumber() != null ? npi.getPaymentNumber() : "")
                        if (npi.taxSumm != null) {
                            cell_44.setCellValue(npi.taxSumm.intValue())
                        }
                    }
                    cell0Value.append("_")
                    // Заполнение данными из раздела "Сведения о вычетах"
                    Cell cell_45 = row.createCell(45)
                    cell_45.setCellStyle(textCenteredStyle)
                    Cell cell_46 = row.createCell(46)
                    cell_46.setCellStyle(centeredStyle)
                    Cell cell_47 = row.createCell(47)
                    cell_47.setCellStyle(centeredStyleDate)
                    Cell cell_48 = row.createCell(48)
                    cell_48.setCellStyle(textCenteredStyle)
                    Cell cell_49 = row.createCell(49)
                    cell_49.setCellStyle(textCenteredStyle)
                    Cell cell_50 = row.createCell(50)
                    cell_50.setCellStyle(numberCenteredStyle)
                    Cell cell_51 = row.createCell(51)
                    cell_51.setCellStyle(textCenteredStyle)
                    Cell cell_52 = row.createCell(52)
                    cell_52.setCellStyle(centeredStyleDate)
                    Cell cell_53 = row.createCell(53)
                    cell_53.setCellStyle(textCenteredStyle)
                    Cell cell_54 = row.createCell(54)
                    cell_54.setCellStyle(numberCenteredStyle)
                    Cell cell_55 = row.createCell(55)
                    cell_55.setCellStyle(centeredStyleDate)
                    Cell cell_56 = row.createCell(56)
                    cell_56.setCellStyle(numberCenteredStyle)
                    Cell cell_57 = row.createCell(57)
                    cell_57.setCellStyle(centeredStyleDate)
                    Cell cell_58 = row.createCell(58)
                    cell_58.setCellStyle(numberCenteredStyle)
                    if (rowData.ndflPersonDeduction) {
                        NdflPersonDeduction npd = rowData.ndflPersonDeduction
                        cell0Value.append(npd.id.toString())
                        cell_45.setCellValue(npd.getTypeCode() != null ? npd.getTypeCode() : "")
                        cell_46.setCellValue(npd.getNotifType() != null ? npd.getNotifType() : "")
                        cell_47.setCellValue(npd.notifDate?.format(DATE_FORMAT) ?: "")
                        cell_48.setCellValue(npd.getNotifNum() != null ? npd.getNotifNum() : "")
                        cell_49.setCellValue(npd.getNotifSource() != null ? npd.getNotifSource() : "")
                        if (npd.notifSumm != null) {
                            cell_50.setCellValue(npd.notifSumm.doubleValue())
                        }
                        cell_51.setCellValue(npd.getOperationId() != null ? npd.getOperationId() : "")
                        cell_52.setCellValue(npd.incomeAccrued?.format(DATE_FORMAT) ?: "")
                        cell_53.setCellValue(npd.getIncomeCode() != null ? npd.getIncomeCode() : "")
                        if (npd.incomeSumm != null) {
                            cell_54.setCellValue(npd.incomeSumm.doubleValue())
                        }
                        cell_55.setCellValue(npd.periodPrevDate?.format(DATE_FORMAT) ?: "")
                        if (npd.periodPrevSumm != null) {
                            cell_56.setCellValue(npd.periodPrevSumm.doubleValue())
                        }
                        cell_57.setCellValue(npd.periodCurrDate?.format(DATE_FORMAT) ?: "")
                        if (npd.periodCurrSumm != null) {
                            cell_58.setCellValue(npd.periodCurrSumm.doubleValue())
                        }
                    }
                    cell0Value.append("_")
                    // Заполнение данными из раздела "Сведения об авансах"
                    Cell cell_59 = row.createCell(59)
                    cell_59.setCellStyle(textCenteredStyle)
                    Cell cell_60 = row.createCell(60)
                    cell_60.setCellStyle(numberCenteredStyle)
                    Cell cell_61 = row.createCell(61)
                    cell_61.setCellStyle(textCenteredStyle)
                    Cell cell_62 = row.createCell(62)
                    cell_62.setCellStyle(centeredStyleDate)
                    Cell cell_63 = row.createCell(63)
                    cell_63.setCellStyle(textCenteredStyle)
                    if (rowData.ndflPersonPrepayment) {
                        NdflPersonPrepayment npp = rowData.ndflPersonPrepayment
                        cell0Value.append(npp.id.toString())
                        cell_59.setCellValue(npp.getOperationId() != null ? npp.getOperationId() : "")
                        if (npp.summ != null) {
                            cell_60.setCellValue(npp.summ.doubleValue())
                        }
                        cell_61.setCellValue(npp.getNotifNum() != null ? npp.getNotifNum() : "")
                        cell_62.setCellValue(npp.notifDate?.format(DATE_FORMAT) ?: "")
                        cell_63.setCellValue(npp.getNotifSource() != null ? npp.getNotifSource() : "")
                    }
                    cell_0.setCellValue(cell0Value.toString())
                    pointer++
                }
            }
        }
    }
}

/**
 * Содержит логику заполнения реестра загруженных данных РНУ НДФЛ
 */
@TypeChecked
class ReportXlsxSheetFiller implements SheetFiller {

    class KppOktmoPair {
        String kpp
        String oktmo

        KppOktmoPair(String kpp, String oktmo) {
            this.kpp = kpp
            this.oktmo = oktmo
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            KppOktmoPair that = (KppOktmoPair) o

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

    class FlIncomeData {
        Set<Long> personIdSet
        BigDecimal incomeAccruedSumm
        BigDecimal calculatedTax

        FlIncomeData(Set<Long> personIdSet, BigDecimal incomeAccruedSumm, BigDecimal calculatedTax) {
            this.personIdSet = personIdSet
            this.incomeAccruedSumm = incomeAccruedSumm
            this.calculatedTax = calculatedTax
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            FlIncomeData that = (FlIncomeData) o

            if (incomeAccruedSumm != that.incomeAccruedSumm) return false
            if (calculatedTax != that.calculatedTax) return false
            if (personIdSet != that.personIdSet) return false

            return true
        }

        int hashCode() {
            int result
            result = (personIdSet != null ? personIdSet.hashCode() : 0)
            result = 31 * result + (incomeAccruedSumm != null ? incomeAccruedSumm.hashCode() : 0)
            result = 31 * result + (calculatedTax != null ? calculatedTax.hashCode() : 0)
            return result
        }
    }


    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        List<NdflPersonIncome> ndflPersonIncomeList = context.getNdflPersonIncomeList()
        Map<KppOktmoPair, FlIncomeData> flIncomeDataMap = new HashMap<>()
        Sheet sheet = wb.getSheetAt(0)
        Integer rowNumber = 3
        Integer ppNumber = 1
        Styler styler = new Styler(wb)
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle textRightStyle = styler.createBorderStyleRightAlignedTypeText()
        CellStyle textLeftStyle = styler.createBorderStyleLeftAlignedTypeText()
        FlIncomeData summaryFlIncomeData = new FlIncomeData(new HashSet<Long>(), new BigDecimal(0).setScale(2), new BigDecimal(0).setScale(2))
        for (NdflPersonIncome npi in ndflPersonIncomeList) {
            KppOktmoPair kppOktmoPair = new KppOktmoPair(npi.kpp, npi.oktmo)
            if (flIncomeDataMap.get(kppOktmoPair) == null) {
                flIncomeDataMap.put(kppOktmoPair, new FlIncomeData(new HashSet<Long>(), new BigDecimal(0).setScale(2), new BigDecimal(0).setScale(2)))
            }
            FlIncomeData flIncomeData = flIncomeDataMap.get(kppOktmoPair)
            flIncomeData.personIdSet.add(npi.ndflPersonId)
            flIncomeData.incomeAccruedSumm = npi.incomeAccruedSumm ? flIncomeData.incomeAccruedSumm.add(npi.incomeAccruedSumm) : flIncomeData.incomeAccruedSumm
            flIncomeData.calculatedTax = npi.calculatedTax ? flIncomeData.calculatedTax.add(npi.calculatedTax) : flIncomeData.calculatedTax
            flIncomeDataMap.put(kppOktmoPair, flIncomeData)

            summaryFlIncomeData.personIdSet.add(npi.ndflPersonId)
            summaryFlIncomeData.incomeAccruedSumm = npi.incomeAccruedSumm ? summaryFlIncomeData.incomeAccruedSumm.add(npi.incomeAccruedSumm) : summaryFlIncomeData.incomeAccruedSumm
            summaryFlIncomeData.calculatedTax = npi.calculatedTax ? summaryFlIncomeData.calculatedTax.add(npi.calculatedTax) : summaryFlIncomeData.calculatedTax
        }

        for (KppOktmoPair kppOktmoPair : flIncomeDataMap.keySet()) {
            FlIncomeData flIncomeData = flIncomeDataMap.get(kppOktmoPair)
            ScriptUtils.checkInterrupted()
            Row row = sheet.createRow(rowNumber)
            Cell cell1 = row.createCell(1)
            cell1.setCellStyle(textRightStyle)
            cell1.setCellValue(ppNumber)
            Cell cell2 = row.createCell(2)
            cell2.setCellStyle(textLeftStyle)
            cell2.setCellValue(kppOktmoPair.kpp)
            Cell cell3 = row.createCell(3)
            cell3.setCellStyle(textLeftStyle)
            cell3.setCellValue(kppOktmoPair.oktmo)
            Cell cell4 = row.createCell(4)
            cell4.setCellStyle(textRightStyle)
            cell4.setCellValue(flIncomeData.personIdSet.size())
            Cell cell5 = row.createCell(5)
            cell5.setCellStyle(textRightStyle)
            cell5.setCellValue(flIncomeData.incomeAccruedSumm?.toString())
            Cell cell6 = row.createCell(6)
            cell6.setCellStyle(textRightStyle)
            cell6.setCellValue(flIncomeData.calculatedTax?.toString())
            rowNumber++
            ppNumber++
        }
        textRightStyle.setFont(styler.createBoldFont())
        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 1, 3))
        Row row = sheet.createRow(rowNumber)
        Cell cell1 = row.createCell(1)
        cell1.setCellStyle(textRightStyle)
        cell1.setCellValue("Итого")
        Cell cell2 = row.createCell(2)
        cell2.setCellStyle(textCenteredStyle)
        cell2.setCellValue("")
        Cell cell3 = row.createCell(3)
        cell3.setCellStyle(textCenteredStyle)
        cell3.setCellValue("")
        Cell cell4 = row.createCell(4)
        cell4.setCellStyle(textRightStyle)
        cell4.setCellValue(summaryFlIncomeData.personIdSet.size())
        Cell cell5 = row.createCell(5)
        cell5.setCellStyle(textRightStyle)
        cell5.setCellValue(summaryFlIncomeData.incomeAccruedSumm.toString())
        Cell cell6 = row.createCell(6)
        cell6.setCellStyle(textRightStyle)
        cell6.setCellValue(summaryFlIncomeData.calculatedTax.toString())
    }
}

/**
 * Класс содержащий набор методов отвечающих за стилизацию ячеек при формирования файла Excel
 */
class Styler {

    Workbook workbook

    Styler(Workbook workbook) {
        this.workbook = workbook
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру
     * @return
     */
    CellStyle createBorderStyleCenterAligned() {
        CellStyle style = workbook.createCellStyle()
        style.setAlignment(CellStyle.ALIGN_CENTER)
        addThinBorderStyle(style)
        return style
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами
     * @return
     */
    CellStyle createBorderStyle() {
        CellStyle style = workbook.createCellStyle()
        addThinBorderStyle(style)
        return style
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру для дат
     * @return
     */
    CellStyle createBorderStyleCenterAlignedDate() {
        CellStyle style = createBorderStyleCenterAligned()
        return addDateFormat(style)
    }

    /**
     * Добавляет к стилю формат даты в ДД.ММ.ГГГГ
     * @param style
     * @return
     */
    CellStyle addDateFormat(CellStyle style) {
        style.setDataFormat(ScriptUtils.createXlsDateFormat(workbook))
        return style
    }

    /**
     * Добавляет к стилю ячейки тонкие границы
     * @param style
     * @return
     */
    static CellStyle addThinBorderStyle(CellStyle style) {
        style.setBorderTop(CellStyle.BORDER_THIN)
        style.setBorderBottom(CellStyle.BORDER_THIN)
        style.setBorderLeft(CellStyle.BORDER_THIN)
        style.setBorderRight(CellStyle.BORDER_THIN)
        return style
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру по горизонтали о по верху
     * по вериткали
     * @return
     */
    CellStyle createVerticalByTopHorizontalByCenter() {
        CellStyle style = workbook.createCellStyle()
        style.setAlignment(HorizontalAlignment.CENTER)
        style.setVerticalAlignment(VerticalAlignment.TOP)
        addThinBorderStyle(style)
        return style
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру по горизонтали о по верху
     * по вериткали
     * @return
     */
    CellStyle createVerticalByTopHorizontalByCenterDate() {
        CellStyle style = createVerticalByTopHorizontalByCenter()
        return addDateFormat(style)
    }

    CellStyle createBorderStyleTypeText(short cellAlign) {
        CellStyle style = workbook.createCellStyle()
        style.setAlignment(cellAlign)
        addThinBorderStyle(style)
        DataFormat format = workbook.createDataFormat()
        style.setDataFormat(format.getFormat("text"))
        return style
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру
     * тип поля Текстовый
     * @return
     */
    CellStyle createBorderStyleCenterAlignedTypeText() {
        return createBorderStyleTypeText(CellStyle.ALIGN_CENTER)
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по левому краю
     * тип поля Текстовый
     * @return
     */
    CellStyle createBorderStyleLeftAlignedTypeText() {
        return createBorderStyleTypeText(CellStyle.ALIGN_LEFT)
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по правому краю
     * тип поля Текстовый
     * @return
     */
    CellStyle createBorderStyleRightAlignedTypeText() {
        return createBorderStyleTypeText(CellStyle.ALIGN_RIGHT)
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру
     * тип поля Числовой
     * @return
     */
    CellStyle createBorderStyleCenterAlignedTypeNumber() {
        CellStyle style = workbook.createCellStyle()
        style.setAlignment(CellStyle.ALIGN_CENTER)
        addThinBorderStyle(style)
        DataFormat format = workbook.createDataFormat()
        style.setDataFormat(format.getFormat("0.00"))
        return style
    }

    XSSFFont createBoldFont() {
        XSSFFont boldFont = (XSSFFont) workbook.createFont()
        boldFont.setBold(true)
        return boldFont
    }
}

/**
 * Класс соответсвующий данным одной строки из шаблона выгрузки в Excel
 */
class ExcelTemplateRow {
    NdflPerson ndflPerson
    NdflPersonIncome ndflPersonIncome
    NdflPersonDeduction ndflPersonDeduction
    NdflPersonPrepayment ndflPersonPrepayment
}
