package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateFile
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificDeclarationDataReportHolder
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.export.JRXlsExporterParameter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

/**
 * Скрипт макета декларации РНУ-НДФЛ(консолидированная)
 */
new ConsolidatedRnuNdfl(this).run();

@TypeChecked
class ConsolidatedRnuNdfl extends AbstractScriptClass {

    DeclarationData declarationData
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
    BlobDataService blobDataService

    private ConsolidatedRnuNdfl() {}

    @TypeChecked(TypeCheckingMode.SKIP)
    public ConsolidatedRnuNdfl(scriptClass) {
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
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataService = (BlobDataService) scriptClass.getProperty("blobDataServiceDaoImpl");
        }
    }

    @Override
    void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.CREATE:
                checkCreate()
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
            DataRow<com.aplana.sbrf.taxaccounting.model.Cell> row = new DataRow<com.aplana.sbrf.taxaccounting.model.Cell>(FormDataUtils.createCells(rowColumns, null));
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
                loadPersonDataToExcel()
                break;
            case SubreportAliasConstants.REPORT_KPP_OKTMO:
                createSpecificReportDb();
                ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
                def reportPeriodName = reportPeriod.getTaxPeriod().year + '_' + reportPeriod.name
                Department department = departmentService.get(declarationData.departmentId)
                scriptSpecificReportHolder.setFileName("Реестр_сформированной_отчетности_${declarationData.id}_${reportPeriodName}_${department.shortName}_${new Date().format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break;
            case SubreportAliasConstants.RNU_NDFL_PERSON_ALL_DB:
                loadAllDeclarationDataToExcel();
                scriptSpecificReportHolder.setFileName("РНУ_НДФЛ_${declarationData.id}_${new Date().format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break;
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


    void loadPersonDataToExcel() {
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
            throw new ServiceException("Не найдены данные для формирования отчета!");
        }
    }

/**
 * Выгрузка в Excel РНУ-НДФЛ
 */
    public void loadAllDeclarationDataToExcel() {

        ScriptUtils.checkInterrupted();

        ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
        Collections.sort(ndflPersonList, new Comparator<NdflPerson>() {
            @Override
            int compare(NdflPerson o1, NdflPerson o2) {
                return o1.id.compareTo(o2.id)
            }
        })

        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
        Collections.sort(ndflPersonIncomeList, new Comparator<NdflPersonIncome>() {
            @Override
            int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                return o1.id.compareTo(o2.id)
            }
        })

        List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
        Collections.sort(ndflPersonDeductionList, new Comparator<NdflPersonDeduction>() {
            @Override
            int compare(NdflPersonDeduction o1, NdflPersonDeduction o2) {
                return o1.id.compareTo(o2.id)
            }
        })
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
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
            def declarationList = declarationService.find(102, prevDepartmentReportPeriod.getId())
            declarationList.addAll(declarationService.find(103, prevDepartmentReportPeriod.getId()))
            declarationList.addAll(declarationService.find(104, prevDepartmentReportPeriod.getId()))
            if (declarationList.isEmpty()) {
                logger.warn("Отсутствуют отчетные налоговые формы в некорректировочном периоде, Отчетные налоговые формы не будут сформированы текущем периоде")
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
 * Фабрика для получения экземплярая SheetFiller по индексу листа
 */
@TypeChecked
class SheetFillerFactory {
    public static SheetFiller getSheetFiller(int sheetIndex) {
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
        List<NdflPersonIncome> ndflPersonIncomeList = context.getNdflPersonIncomeList();
        List<NdflPerson> ndflPersonList = context.getNdflPersonList()
        Map<KppOktmoPair, FlIncomeData> flIncomeDataMap = new HashMap<>()
        Sheet sheet = wb.getSheetAt(0);
        Integer rowNumber = 3;
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
            ScriptUtils.checkInterrupted();
            Row row = sheet.createRow(rowNumber);
            Cell cell1 = row.createCell(1);
            cell1.setCellStyle(textRightStyle)
            cell1.setCellValue(ppNumber);
            Cell cell2 = row.createCell(2);
            cell2.setCellStyle(textLeftStyle)
            cell2.setCellValue(kppOktmoPair.kpp);
            Cell cell3 = row.createCell(3);
            cell3.setCellStyle(textLeftStyle)
            cell3.setCellValue(kppOktmoPair.oktmo);
            Cell cell4 = row.createCell(4);
            cell4.setCellStyle(textRightStyle)
            cell4.setCellValue(flIncomeData.personIdSet.size());
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(textRightStyle)
            cell5.setCellValue(flIncomeData.incomeAccruedSumm?.toString());
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(textRightStyle)
            cell6.setCellValue(flIncomeData.calculatedTax?.toString());
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
    CellStyle createBorderStyleCenterAlignedTypeNumber(){
        CellStyle style = workbook.createCellStyle()
        style.setAlignment(CellStyle.ALIGN_CENTER)
        addThinBorderStyle(style)
        DataFormat format = workbook.createDataFormat()
        style.setDataFormat(format.getFormat("0.00"))
        return style
    }

    XSSFFont createBoldFont(){
        XSSFFont boldFont = workbook.createFont()
        boldFont.setBold(true)
        return boldFont
    }
}
