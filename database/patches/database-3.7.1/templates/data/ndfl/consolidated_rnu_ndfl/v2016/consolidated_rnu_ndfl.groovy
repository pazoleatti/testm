package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DateColumn
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateFile
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormSources
import com.aplana.sbrf.taxaccounting.model.Ndfl2_6DataReportParams
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.PrepareSpecificReportResult
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificDeclarationDataReportHolder
import com.aplana.sbrf.taxaccounting.model.StringColumn
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.RefBookService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.export.JRXlsExporterParameter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import org.apache.commons.io.IOUtils
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.script.SharedConstants.DATE_ZERO_AS_STRING
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.formatDate
import static form_template.ndfl.consolidated_rnu_ndfl.v2016.ConsolidatedRnuNdfl.DataFormatEnum.*

/**
 * Скрипт макета декларации РНУ-НДФЛ(консолидированная)
 */
new ConsolidatedRnuNdfl(this).run();

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class ConsolidatedRnuNdfl extends AbstractScriptClass {

    DeclarationData declarationData
    DeclarationTemplate declarationTemplate
    DepartmentReportPeriod departmentReportPeriod
    ReportPeriod reportPeriod
    NdflPersonService ndflPersonService
    DeclarationService declarationService;
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    DepartmentService departmentService
    Boolean needSources
    Boolean light
    FormSources sources
    ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder
    DepartmentReportPeriodService departmentReportPeriodService
    FileWriter xml
    BlobDataService blobDataService
    RefBookService refBookService
    boolean adjustNegativeValues
    Set<String> kppList
    Date dateFrom, dateTo
    Date date = new Date()

    @TypeChecked(TypeCheckingMode.SKIP)
    ConsolidatedRnuNdfl(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
        this.declarationService = (DeclarationService) getSafeProperty("declarationService");
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
            this.declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
            this.departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
            this.reportPeriod = departmentReportPeriod.reportPeriod
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
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
        if (scriptClass.getBinding().hasVariable("scriptSpecificReportHolder")) {
            this.scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) scriptClass.getProperty("scriptSpecificReportHolder");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getBinding().getProperty("refBookService")
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
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataService = (BlobDataService) scriptClass.getProperty("blobDataServiceDaoImpl");
        }
    }

    @Override
    void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.CREATE:
                break
            case FormDataEvent.AFTER_CALCULATE: // Формирование pdf-отчета формы
                declarationService.createPdfReport(logger, declarationData, userInfo)
                break
            case FormDataEvent.CREATE_EXCEL_REPORT: //создание xlsx отчета
                createXlsxReport()
                break
            case FormDataEvent.PREPARE_SPECIFIC_REPORT:
                // Подготовка для последующего формирования спецотчета
                prepareSpecificReport()
                break
            case FormDataEvent.CREATE_SPECIFIC_REPORT:
                // Формирование спецотчета
                createSpecificReport()
                break
        }
    }

    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"

    public final static String RNU_NDFL_PERSON_ALL_DB = "rnu_ndfl_person_all_db.xlsx"
    public final static String REPORT_XLSX = "report.xlsx"

    /**
     * Идентификатор шаблона РНУ-НДФЛ (консолидированная)
     */
    final int PRIMARY_RNU_NDFL_TEMPLATE_ID = 100
    final int NDFL_2_1_TEMPLATE_ID = 102
    final int NDFL_2_2_TEMPLATE_ID = 104
    final int NDFL_6_TEMPLATE_ID = 103

//------------------ PREPARE_SPECIFIC_REPORT ----------------------

    def prepareSpecificReport() {
        def reportAlias = scriptSpecificReportHolder?.declarationSubreport?.alias;
        if (SubreportAliasConstants.RNU_NDFL_PERSON_DB != reportAlias) {
            throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
        }
        PrepareSpecificReportResult result = new PrepareSpecificReportResult();
        List<Column> tableColumns = createTableColumns();
        List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows = new ArrayList<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>>();
        List<Column> rowColumns = createRowColumns()

        //Проверка, подготовка данных
        def params = scriptSpecificReportHolder.subreportParamValues
        def reportParameters = scriptSpecificReportHolder.getSubreportParamValues();

        if (reportParameters.isEmpty()) {
            throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.");
        }

        def resultReportParameters = [:]
        reportParameters.each { key, value ->
            if (value != null) {
                def val = value;
                if (key in ["lastName", "firstName", "middleName", "inp"]) {
                    val = '%' + value + '%'
                }
                resultReportParameters.put(key, val)
            }
        }

        // Ограничение числа выводимых записей
        int startIndex = 1
        int pageSize = 10

        PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters, startIndex, pageSize);

        if (pagingResult.isEmpty()) {
            Closure subreportParamsToString = {
                it.collect { Map<String, Object> param ->
                    (param.value != null ? (((param.value instanceof Date) ? ((Date) param.value).format('dd.MM.yyyy') : (String) param.value) + ";") : "")
                } join " "
            }
            logger.warn("Физическое лицо: " + subreportParamsToString(reportParameters) + " не найдено в форме");
            //throw new ServiceException("Физическое лицо: " + subreportParamsToString(reportParameters)+ " не найдено в форме");
        }

        pagingResult.getRecords().each() { ndflPerson ->
            DataRow<com.aplana.sbrf.taxaccounting.model.Cell> row = new DataRow<com.aplana.sbrf.taxaccounting.model.Cell>(ScriptUtils.createCells(rowColumns, null));
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

        int countOfAvailableNdflPerson = pagingResult.size()

        if (countOfAvailableNdflPerson >= pageSize) {
            countOfAvailableNdflPerson = ndflPersonService.findNdflPersonCountByParameters(declarationData.id, resultReportParameters);
        }

        result.setTableColumns(tableColumns);
        result.setDataRows(dataRows);
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

        return tableColumns;
    }

    List<Column> createRowColumns() {
        List<Column> tableColumns = new ArrayList<Column>();

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

        return tableColumns;
    }

//------------------ Create Report ----------------------

/**
 * Создать XLSX отчет
 * @return
 */
    def createXlsxReport() {
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)

        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
        Collections.sort(ndflPersonIncomeList, new Comparator<NdflPersonIncome>() {
            @Override
            int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                return o1.id.compareTo(o2.id)
            }
        })

        SheetFillerContext context = new SheetFillerContext(ndflPersonList, ndflPersonIncomeList)
        Workbook sxssfWorkbook = new SXSSFWorkbook(getSpecialReportTemplate(REPORT_XLSX), 100, true)
        sxssfWorkbook.setSheetName(0, "Реестр")
        new SheetFillerFactory().getSheetFiller(5).fillSheet(sxssfWorkbook, context)

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
                strCorrPeriod = ", с датой сдачи корректировки " + departmentReportPeriod.getCorrectionDate().format("dd.MM.yyyy");
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
            case SubreportAliasConstants.REPORT_KPP_OKTMO:
                createSpecificReportDb()
                ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
                def reportPeriodName = reportPeriod.getTaxPeriod().year + '_' + reportPeriod.name
                Department department = departmentService.get(declarationData.departmentId)
                scriptSpecificReportHolder.setFileName("Реестр_сформированной_отчетности_${declarationData.id}_${reportPeriodName}_${department.shortName}_${new Date().format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
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
                throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
        }
    }
/**
 * Спец. отчет "РНУ НДФЛ по физическому лицу". Данные макет извлекает непосредственно из бд
 */
    def createSpecificReportPersonDb() {
        NdflPerson ndflPerson = ndflPersonService.get((Long) scriptSpecificReportHolder.subreportParamValues.get("PERSON_ID"));
        if (ndflPerson != null) {
            def params = [NDFL_PERSON_ID: (Object) ndflPerson.id];
            JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params);
            exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
            scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")
        } else {
            throw new ServiceException("Не найдены данные для формирования отчета!");
        }
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
 * Формирует спец. отчеты, данные для которых макет извлекает непосредственно из бд
 */
    def createSpecificReportDb() {
        def params = [declarationId: (Object) declarationData.id]
        JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params);
        exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
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
        StringBuilder sb = new StringBuilder();
        sb.append("РНУ_НДФЛ_");
        sb.append(declarationData.id).append("_");
        sb.append(capitalize(ndflPerson.lastName));
        sb.append(firstChar(ndflPerson.firstName));
        sb.append(firstChar(ndflPerson.middleName)).append("_");
        sb.append(ndflPerson.idDocNumber?.replaceAll("\\s", "")?.toLowerCase()).append("_");
        sb.append(new SimpleDateFormat("yyyy.MM.dd_HHmm").format(new Date()));
        return sb.toString();
    }


    String firstChar(String str) {
        if (str != null && !str.isEmpty()) {
            return String.valueOf(Character.toUpperCase(str.charAt(0)));
        } else {
            return "";
        }
    }

    String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toTitleCase(str.charAt(0)))
                .append(str.substring(1).toLowerCase())
                .toString();
    }


    void exportPersonDataToExcel() {
        List<NdflPerson> ndflPersonList = []
        ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        NdflPerson ndflPerson = ndflPersonService.get((Long) scriptSpecificReportHolder.subreportParamValues.get("PERSON_ID"));
        ndflPersonList.add(ndflPerson)
        if (ndflPerson != null) {
            List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findIncomes(ndflPerson.id)
            Collections.sort(ndflPersonIncomeList, new Comparator<NdflPersonIncome>() {
                @Override
                int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                    return o1.id.compareTo(o2.id)
                }
            })

            List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findDeductions(ndflPerson.id)
            Collections.sort(ndflPersonDeductionList, new Comparator<NdflPersonDeduction>() {
                @Override
                int compare(NdflPersonDeduction o1, NdflPersonDeduction o2) {
                    return o1.id.compareTo(o2.id)
                }
            })
            List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findPrepayments(ndflPerson.id)
            Collections.sort(ndflPersonPrepaymentList, new Comparator<NdflPersonPrepayment>() {
                @Override
                int compare(NdflPersonPrepayment o1, NdflPersonPrepayment o2) {
                    return o1.id.compareTo(o2.id)
                }
            })
            String departmentName = departmentService.get(declarationData.departmentId)?.name
            String reportDate = getReportPeriodEndDate().format("dd.MM.yyyy") + " г."
            String period = getProvider(RefBook.Id.PERIOD_CODE.getId()).getRecordData(reportPeriod.dictTaxPeriodId)?.NAME?.value
            String year = getReportPeriodEndDate().format("yyyy") + " г."

            SheetFillerContext context = new SheetFillerContext(departmentName, reportDate, period, year, ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

            Workbook xssfWorkbook = getSpecialReportTemplate(RNU_NDFL_PERSON_ALL_DB)

            new SheetFillerFactory().getSheetFiller(0).fillSheet(xssfWorkbook, context)

            new SheetFillerFactory().getSheetFiller(1).fillSheet(xssfWorkbook, context)

            new SheetFillerFactory().getSheetFiller(2).fillSheet(xssfWorkbook, context)

            new SheetFillerFactory().getSheetFiller(3).fillSheet(xssfWorkbook, context)

            new SheetFillerFactory().getSheetFiller(4).fillSheet(xssfWorkbook, context)

            OutputStream writer = null
            try {
                writer = scriptSpecificReportHolder.getFileOutputStream()
                xssfWorkbook.write(writer)
            } finally {
                writer.close()
            }

            scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")
        } else {
            throw new ServiceException("Не найдены данные для формирования отчета!");
        }
    }

    /**
     * Выгрузка в Excel РНУ-НДФЛ
     */
    public void exportAllDeclarationDataToExcel() {

        ScriptUtils.checkInterrupted();

        ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
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

        new SheetFillerFactory().getSheetFiller(0).fillSheet(xssfWorkbook, context)

        Workbook sxssfWorkbook = new SXSSFWorkbook(xssfWorkbook, 100, true)

        new SheetFillerFactory().getSheetFiller(1).fillSheet(sxssfWorkbook, context)

        new SheetFillerFactory().getSheetFiller(2).fillSheet(sxssfWorkbook, context)

        new SheetFillerFactory().getSheetFiller(3).fillSheet(sxssfWorkbook, context)

        new SheetFillerFactory().getSheetFiller(4).fillSheet(sxssfWorkbook, context)

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
        String blobDataId = null;
        for (DeclarationTemplateFile declarationTemplateFile : declarationTemplate.declarationTemplateFiles) {
            if (declarationTemplateFile.fileName.equals(reportFileName)) {
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
        Long taxSum = 0
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
        Long taxSum = 0

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

    //Далее и до конца файла идет часть проверок общая для первичной и консолидированно,
    //если проверки различаются то используется параметр {@link #FORM_DATA_KIND}
    //При внесении изменений учитывается что эта чать скрипта используется(копируется) и в первичной и в консолидированной

    //>------------------< REF BOOK >----------------------<

    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]

    Map<Integer, DepartmentReportPeriod> departmentReportPeriodMap = [:]

    Map<Integer, DeclarationTemplate> declarationTemplateMap = [:]

    Map<Integer, String> departmentFullNameMap = [:]

// Дата окончания отчетного периода
    Date reportPeriodEndDate = null

    DeclarationTemplate getDeclarationTemplateById(Integer id) {
        if (id != null && declarationTemplateMap.get(id) == null) {
            declarationTemplateMap.put(id, (DeclarationTemplate) declarationService.getTemplate(id))
        }
        return declarationTemplateMap.get(id)
    }

    DepartmentReportPeriod getDepartmentReportPeriodById(int id) {
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

/**
 * Получить дату окончания отчетного периода
 * @return
 */
    Date getReportPeriodEndDate() {
        if (periodEndDate == null) {
            periodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
        }
        return periodEndDate
    }

/**
 * Получение провайдера с использованием кеширования.
 * @param providerId
 * @return
 */
    RefBookDataProvider getProvider(def long providerId) {
        if (!providerCache.containsKey(providerId)) {
            providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
        }
        return providerCache.get(providerId)
    }

    void checkCreate() {
        def departmentReportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())
        if (departmentReportPeriod.correctionDate != null) {
            def prevDepartmentReportPeriod = departmentReportPeriodService.getFirst(declarationData.getDepartmentId(), declarationData.getReportPeriodId())
            def declarationList = declarationService.findAllByTypeIdAndPeriodId(102, prevDepartmentReportPeriod.getId())
            declarationList.addAll(declarationService.findAllByTypeIdAndPeriodId(103, prevDepartmentReportPeriod.getId()))
            declarationList.addAll(declarationService.findAllByTypeIdAndPeriodId(104, prevDepartmentReportPeriod.getId()))
            if (declarationList.isEmpty()) {
                logger.warn("Отсутствуют отчетные налоговые формы в некорректировочном периоде, Отчетные налоговые формы не будут сформированы текущем периоде")
            }
        }
    }


    /**
     * Фабрика для получения экземплярая SheetFiller по индексу листа
     */
    class SheetFillerFactory {
        SheetFiller getSheetFiller(int sheetIndex) {
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
                    return new ReportXlsxSheetFiller(ndflPersonService, declarationService, departmentService)
                default: return null
            }
        }
    }
}

/**
 * Класс инкапсулирующий данные
 */
public class SheetFillerContext {

    private String departmentName;

    private String reportDate;

    private String period;

    private String year;

    private List<NdflPerson> ndflPersonList;

    private List<NdflPersonIncome> ndflPersonIncomeList;

    private List<NdflPersonDeduction> ndflPersonDeductionList;

    private List<NdflPersonPrepayment> ndflPersonPrepaymentList;

    private Map<Long, NdflPerson> idNdflPersonMap;

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
            idNdflPersonMap = new HashMap<>();
            for (NdflPerson ndflPerson : ndflPersonList) {
                idNdflPersonMap.put(ndflPerson.getId(), ndflPerson);
            }
        }
        return idNdflPersonMap;
    }
}

/**
 * Интерфейс определяющий заполнение листа и состояние классов реализующих интерфеййс
 */
interface SheetFiller {
    void fillSheet(Workbook wb, SheetFillerContext context);
}

/**
 * Заполнитель заголовка
 */
@TypeChecked
class TitleSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, final SheetFillerContext context) {
        Sheet sheet = wb.getSheetAt(0);
        Cell cell0 = sheet.getRow(1).createCell(2);
        cell0.setCellValue(context.getDepartmentName());
        Cell cell1 = sheet.createRow(2).createCell(1);
        cell1.setCellValue(context.getReportDate());
        Cell cell2 = sheet.getRow(4).createCell(2);
        cell2.setCellValue(context.getPeriod() + " " + context.getYear());
    }
}

/**
 * Заполнитель реквизитов
 */
@TypeChecked
class RequisitesSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        Sheet sheet = wb.getSheetAt(1)
        int index = 3
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        for (NdflPerson np : context.getNdflPersonList()) {
            ScriptUtils.checkInterrupted()
            Row row = sheet.createRow(index)
            Cell cell1 = row.createCell(1)
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(np.getRowNum().intValue());
            Cell cell2 = row.createCell(2)
            cell2.setCellStyle(centeredStyle)
            cell2.setCellValue(np.getInp() != null ? np.getInp() : "")
            Cell cell3 = row.createCell(3)
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(np.getLastName() != null ? np.getLastName() : "")
            Cell cell4 = row.createCell(4)
            cell4.setCellStyle(centeredStyle)
            cell4.setCellValue(np.getFirstName() != null ? np.getFirstName() : "")
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(np.getMiddleName() != null ? np.getMiddleName() : "")
            Cell cell6 = row.createCell(6)
            cell6.setCellStyle(centeredStyleDate)
            cell6.setCellValue(np.birthDay)
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
            cell13.setCellStyle(centeredStyle)
            cell13.setCellValue(np.getRegionCode() != null ? np.getRegionCode() : "")
            Cell cell14 = row.createCell(14)
            cell14.setCellStyle(centeredStyle)
            cell14.setCellValue(np.getPostIndex() != null ? np.getPostIndex() : "");
            Cell cell15 = row.createCell(15)
            cell15.setCellStyle(centeredStyle)
            cell15.setCellValue(np.getArea() != null ? np.getArea() : "");
            Cell cell16 = row.createCell(16)
            cell16.setCellStyle(centeredStyle)
            cell16.setCellValue(np.getCity() != null ? np.getCity() : "");
            Cell cell17 = row.createCell(17)
            cell17.setCellStyle(centeredStyle)
            cell17.setCellValue(np.getLocality() != null ? np.getLocality() : "");
            Cell cell18 = row.createCell(18)
            cell18.setCellStyle(centeredStyle)
            cell18.setCellValue(np.getStreet() != null ? np.getStreet() : "");
            Cell cell19 = row.createCell(19)
            cell19.setCellStyle(centeredStyle)
            cell19.setCellValue(np.getHouse() != null ? np.getHouse() : "");
            Cell cell20 = row.createCell(20)
            cell20.setCellStyle(centeredStyle)
            cell20.setCellValue(np.getBuilding() != null ? np.getBuilding() : "");
            Cell cell21 = row.createCell(21)
            cell21.setCellStyle(centeredStyle)
            cell21.setCellValue(np.getFlat() != null ? np.getFlat() : "");
            Cell cell22 = row.createCell(22)
            cell22.setCellStyle(centeredStyle)
            cell22.setCellValue(np.snils != null ? np.snils : "")
            index++
        }
    }
}

/**
 * Заполнитель сведений о доходах
 */
@TypeChecked
class IncomesSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        List<NdflPersonIncome> ndflPersonIncomeList = context.getNdflPersonIncomeList();
        Sheet sheet = wb.getSheetAt(2)
        int index = 3
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.createBorderStyle()
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPersonIncome npi : ndflPersonIncomeList) {
            ScriptUtils.checkInterrupted()

            Row row = sheet.createRow(index)
            Cell cell1 = row.createCell(1)
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npi.getRowNum().intValue());
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
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(npi.getIncomeType() != null ? npi.getIncomeType() : "")
            Cell cell6 = row.createCell(6)
            cell6.setCellStyle(centeredStyleDate)
            if (npi.incomeAccruedDate != null) {
                cell6.setCellValue(npi.incomeAccruedDate);
            }
            Cell cell7 = row.createCell(7)
            cell7.setCellStyle(centeredStyleDate)
            if (npi.incomePayoutDate != null) {
                cell7.setCellValue(npi.incomePayoutDate);
            }
            Cell cell8 = row.createCell(8)
            cell8.setCellStyle(centeredStyle)
            cell8.setCellValue(npi.getKpp() != null ? npi.getKpp() : "");
            Cell cell9 = row.createCell(9)
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(npi.getOktmo() != null ? npi.getOktmo() : "");
            Cell cell10 = row.createCell(10)
            cell10.setCellStyle(numberCenteredStyle)
            if (npi.incomeAccruedSumm != null) {
                cell10.setCellValue(npi.incomeAccruedSumm.doubleValue());
            }
            Cell cell11 = row.createCell(11)
            cell11.setCellStyle(numberCenteredStyle)
            if (npi.incomePayoutSumm != null) {
                cell11.setCellValue(npi.incomePayoutSumm.doubleValue());
            }
            Cell cell12 = row.createCell(12)
            cell12.setCellStyle(numberCenteredStyle)
            if (npi.totalDeductionsSumm != null) {
                cell12.setCellValue(npi.totalDeductionsSumm.doubleValue());
            }
            Cell cell13 = row.createCell(13)
            cell13.setCellStyle(numberCenteredStyle)
            if (npi.taxBase != null) {
                cell13.setCellValue(npi.taxBase.doubleValue());
            }
            Cell cell14 = row.createCell(14)
            cell14.setCellStyle(borderStyle)
            if (npi.taxRate != null) {
                cell14.setCellValue(npi.taxRate);
            }
            Cell cell15 = row.createCell(15)
            cell15.setCellStyle(centeredStyleDate)
            if (npi.taxDate != null) {
                cell15.setCellValue(npi.taxDate);
            }

            Cell cell16 = row.createCell(16)
            cell16.setCellStyle(numberCenteredStyle)
            if (npi.calculatedTax != null) {
                cell16.setCellValue(npi.calculatedTax.doubleValue())
            }
            Cell cell17 = row.createCell(17)
            cell17.setCellStyle(numberCenteredStyle)
            if (npi.withholdingTax != null) {
                cell17.setCellValue(npi.withholdingTax.doubleValue());
            }
            Cell cell18 = row.createCell(18)
            cell18.setCellStyle(numberCenteredStyle)
            if (npi.notHoldingTax != null) {
                cell18.setCellValue(npi.notHoldingTax.doubleValue());
            }
            Cell cell19 = row.createCell(19)
            cell19.setCellStyle(numberCenteredStyle)
            if (npi.overholdingTax != null) {
                cell19.setCellValue(npi.overholdingTax.doubleValue());
            }
            Cell cell20 = row.createCell(20)
            cell20.setCellStyle(numberCenteredStyle)
            if (npi.refoundTax != null) {
                cell20.setCellValue(npi.refoundTax.doubleValue());
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
 * Заполнитель сведений о вычетах
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
                cell5.setCellValue(npd.notifDate);
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
                cell8.setCellValue(npd.notifSumm.doubleValue());
            }
            Cell cell9 = row.createCell(9)
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(npd.getOperationId() != null ? npd.getOperationId() : "")
            Cell cell10 = row.createCell(10)
            cell10.setCellStyle(centeredStyleDate)
            if (npd.incomeAccrued != null) {
                cell10.setCellValue(npd.incomeAccrued);
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
                cell15.setCellValue(npd.periodCurrDate);
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
 * Заполнитель сведений об авансах
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
 * Содержит логику заполнения реестра загруженных данных РНУ НДФЛ
 */
@TypeChecked
class ReportXlsxSheetFiller implements SheetFiller {

    NdflPersonService ndflPersonService
    DeclarationService declarationService
    DepartmentService departmentService

    ReportXlsxSheetFiller(NdflPersonService ndflPersonService, DeclarationService declarationService, DepartmentService departmentService) {
        this.ndflPersonService = ndflPersonService
        this.declarationService = declarationService
        this.departmentService = departmentService
    }

    @EqualsAndHashCode
    class XlsxReportRowKey {
        String kpp
        String oktmo
        String primaryTB
        String period

        XlsxReportRowKey(String kpp, String oktmo, String primaryTB, String period) {
            this.kpp = kpp
            this.oktmo = oktmo
            this.primaryTB = primaryTB
            this.period = period
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
    }

    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        List<NdflPersonIncome> ndflPersonIncomeList = context.getNdflPersonIncomeList()
        Map<XlsxReportRowKey, FlIncomeData> flIncomeDataMap = new HashMap<>()
        Sheet sheet = wb.getSheetAt(0);
        Integer rowNumber = 3;
        Integer ppNumber = 1
        Styler styler = new Styler(wb)
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle textRightStyle = styler.createBorderStyleRightAlignedTypeText()
        CellStyle textLeftStyle = styler.createBorderStyleLeftAlignedTypeText()
        FlIncomeData summaryFlIncomeData = new FlIncomeData(new HashSet<Long>(), new BigDecimal(0).setScale(2), new BigDecimal(0).setScale(2))
        for (NdflPersonIncome npi in ndflPersonIncomeList) {
            if (npi.incomeAccruedDate) {
                Department primaryTB = getPrimaryTB(npi)
                String period = getPeriod(npi.incomeAccruedDate)
                XlsxReportRowKey rowKey = new XlsxReportRowKey(npi.kpp, npi.oktmo, primaryTB?.name, period)
                if (flIncomeDataMap.get(rowKey) == null) {
                    flIncomeDataMap.put(rowKey, new FlIncomeData(new HashSet<Long>(), new BigDecimal(0).setScale(2), new BigDecimal(0).setScale(2)))
                }
                FlIncomeData flIncomeData = flIncomeDataMap.get(rowKey)
                flIncomeData.personIdSet.add(npi.ndflPersonId)
                flIncomeData.incomeAccruedSumm = npi.incomeAccruedSumm ? flIncomeData.incomeAccruedSumm.add(npi.incomeAccruedSumm) : flIncomeData.incomeAccruedSumm
                flIncomeData.calculatedTax = npi.calculatedTax ? flIncomeData.calculatedTax.add(npi.calculatedTax) : flIncomeData.calculatedTax
                flIncomeDataMap.put(rowKey, flIncomeData)

                summaryFlIncomeData.personIdSet.add(npi.ndflPersonId)
                summaryFlIncomeData.incomeAccruedSumm = npi.incomeAccruedSumm ? summaryFlIncomeData.incomeAccruedSumm.add(npi.incomeAccruedSumm) : summaryFlIncomeData.incomeAccruedSumm
                summaryFlIncomeData.calculatedTax = npi.calculatedTax ? summaryFlIncomeData.calculatedTax.add(npi.calculatedTax) : summaryFlIncomeData.calculatedTax
            }
        }
        flIncomeDataMap = flIncomeDataMap.sort { e1, e2 -> e1.key.kpp <=> e2.key.kpp ?: e1.key.oktmo <=> e2.key.oktmo }

        for (XlsxReportRowKey reportRowKey : flIncomeDataMap.keySet()) {
            FlIncomeData flIncomeData = flIncomeDataMap.get(reportRowKey)
            ScriptUtils.checkInterrupted();
            Row row = sheet.createRow(rowNumber);
            Cell cell1 = row.createCell(1);
            cell1.setCellStyle(textRightStyle)
            cell1.setCellValue(ppNumber);
            Cell cell2 = row.createCell(2);
            cell2.setCellStyle(textLeftStyle)
            cell2.setCellValue(reportRowKey.kpp);
            Cell cell3 = row.createCell(3);
            cell3.setCellStyle(textLeftStyle)
            cell3.setCellValue(reportRowKey.oktmo);
            Cell cell4 = row.createCell(4);
            cell4.setCellStyle(textRightStyle)
            cell4.setCellValue(flIncomeData.personIdSet.size());
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(textRightStyle)
            cell5.setCellValue(flIncomeData.incomeAccruedSumm?.toString());
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(textRightStyle)
            cell6.setCellValue(flIncomeData.calculatedTax?.toString());
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(textCenteredStyle)
            cell7.setCellValue(reportRowKey.primaryTB ?: "");
            Cell cell8 = row.createCell(8);
            cell8.setCellStyle(textLeftStyle)
            cell8.setCellValue(reportRowKey.period ?: "");
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
        Cell cell7 = row.createCell(7)
        cell7.setCellStyle(textRightStyle)
        cell7.setCellValue("")
        Cell cell8 = row.createCell(8)
        cell8.setCellStyle(textLeftStyle)
        cell8.setCellValue("")

    }

    Department getPrimaryTB(NdflPersonIncome income) {
        if (income.sourceId) {
            NdflPersonIncome primaryIncome = ndflPersonService.getIncome(income.sourceId)
            NdflPerson primaryPerson = ndflPersonService.get(primaryIncome.ndflPersonId)
            DeclarationData primaryDeclaration = declarationService.getDeclarationData(primaryPerson.declarationDataId)
            return departmentService.getParentTB(primaryDeclaration.departmentId)
        }
        return null
    }

    String getPeriod(Date date) {
        return date[Calendar.YEAR] + ", " + getQuarter(date)
    }

    String getQuarter(Date date) {
        int month = date[Calendar.MONTH]
        if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
            return "первый квартал"
        } else if (Calendar.APRIL <= month && month <= Calendar.JUNE) {
            return "полугодие "
        } else if (Calendar.JULY <= month && month <= Calendar.SEPTEMBER) {
            return "девять месяцев"
        } else {
            return "год"
        }
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
    CellStyle addThinBorderStyle(CellStyle style) {
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
        XSSFFont boldFont = workbook.createFont()
        boldFont.setBold(true)
        return boldFont
    }
}
