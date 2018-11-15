package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormSources
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificDeclarationDataReportHolder
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants
import com.aplana.sbrf.taxaccounting.model.TAUserInfo
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.LogBusinessService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

new PrimaryRnuNdfl(this).run();

@TypeChecked
class PrimaryRnuNdfl extends AbstractScriptClass {

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
    String UploadFileName
    InputStream ImportInputStream
    File dataFile
    BlobDataService blobDataService
    LogBusinessService logBusinessService
    List<NdflPerson> ndflPersonCache = new ArrayList<>()
    Date formCreationDate = null

    private PrimaryRnuNdfl() {}

    @TypeChecked(TypeCheckingMode.SKIP)
    public PrimaryRnuNdfl(scriptClass) {
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
        if (scriptClass.getBinding().hasVariable("dataFile")) {
            this.dataFile = (File) scriptClass.getProperty("dataFile");
        }
        if (scriptClass.getBinding().hasVariable("UploadFileName")) {
            this.UploadFileName = (String) scriptClass.getProperty("UploadFileName");
        }
        if (scriptClass.getBinding().hasVariable("ImportInputStream")) {
            this.ImportInputStream = (InputStream) scriptClass.getProperty("ImportInputStream");
        }
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataService = (BlobDataService) scriptClass.getProperty("blobDataServiceDaoImpl");
        }
        if (scriptClass.getBinding().hasVariable("logBusinessService")) {
            this.logBusinessService = (LogBusinessService) scriptClass.getProperty("logBusinessService");
        }
    }

    @Override
    public void run() {
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

    /**
     * Идентификатор шаблона РНУ-НДФЛ (консолидированная)
     */
    final int CONSOLIDATED_RNU_NDFL_TEMPLATE_ID = 101
    final int PRIMARY_RNU_NDFL_TEMPLATE_ID = 100

    final String TEMPLATE_PERSON_FL_OPER = "%s, ИНП: %s, ID операции: %s"
    final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует справочнику \"%s\""

    final String RNU_NDFL_PERSON_ALL_DB = "rnu_ndfl_person_all_db.xlsx"
    final String REPORT_XLSX = "report.xlsx"

    /**
     * Получить дату которая используется в качестве версии записей справочника
     * @return дата используемая в качестве даты версии справочника
     */
    def getVersionFrom() {
        return getReportPeriodStartDate();
    }

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
        Map<String, Object> reportParameters = scriptSpecificReportHolder.getSubreportParamValues();

        if (reportParameters.isEmpty()) {
            throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.");
        }

        def resultReportParameters = [:]
        reportParameters.each { key, value ->
            if (value != null) {
                def val = value;
                if (!(key in [SubreportAliasConstants.FROM_BIRTHDAY, SubreportAliasConstants.TO_BIRTHDAY])) {
                    val = '%' + value + '%'
                }
                resultReportParameters.put(key, val)
            }
        }

        // Ограничение числа выводимых записей
        int startIndex = 1
        int pageSize = 10

        PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters, startIndex, pageSize);

        //Если записи не найдены, то система формирует предупреждение:
        //Заголовок: "Предупреждение"
        //Текст: "Физическое лицо: <Данные ФЛ> не найдено в форме", где <Данные ФЛ> - значение полей формы, по которым выполнялся поиск физического лица, через разделитель "; "
        //Кнопки: "Закрыть"

        if (pagingResult.isEmpty()) {
            Closure subreportParamsToString = {
                it.collect { Map.Entry<String, Object> param ->
                    (param.value != null ? (((param.value instanceof Date) ? ((Date) param.value).format('dd.MM.yyyy') : (String) param.value) + ";") : "")
                } join " "
            }
            logger.warn("Физическое лицо: " + subreportParamsToString(reportParameters) + " не найдено в форме");
            //throw new ServiceException("Физическое лицо: " + subreportParamsToString(reportParameters)+ " не найдено в форме");
        }

        pagingResult.getRecords().each() { ndflPerson ->
            DataRow<com.aplana.sbrf.taxaccounting.model.Cell> row = new DataRow<>(ScriptUtils.createCells(rowColumns, null));
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
            case SubreportAliasConstants.RNU_NDFL_PERSON_ALL_DB:
                loadAllDeclarationDataToExcel();
                scriptSpecificReportHolder.setFileName("РНУ_НДФЛ_${declarationData.id}_${new Date().format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
                break;
            default:
                throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
        }
    }

    void loadPersonDataToExcel() {
        List<NdflPerson> ndflPersonList = []
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

/**
 * Спецотчет РНУ-НДФЛ по всем ФЛ
 */
    public void loadAllDeclarationDataToExcel() {

        ScriptUtils.checkInterrupted();
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
     * Выгрузить данные РНУ НДФЛ в Excel
     */
    void exportDeclarationDataToExcel() {

        SheetFillerContext context = createExportDeclarationDataSheetFillerContext()

        XSSFWorkbook xssfWorkbook = createExportDeclarationDataWorkbook(context)

        Workbook sxssfWorkbook = new SXSSFWorkbook(xssfWorkbook, 100, true)

        new ExportDeclarationDataSheetFiller().fillSheet(sxssfWorkbook, context)
        OutputStream writer = scriptSpecificReportHolder.getFileOutputStream()
        sxssfWorkbook.write(writer)
        scriptSpecificReportHolder.setFileName(createExportDeclarationDataToExcelFileName())
    }

    /**
     * Сформировать название файла для выгрузки данных РНУ НДФЛ в Excel
     * @return имя файла
     */
    String createExportDeclarationDataToExcelFileName() {
        Department department = departmentService.get(declarationData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId());
        String asnuName = "";
        if (declarationData.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
            asnuName = asnuProvider.getRecordData(declarationData.getAsnuId()).get("NAME").getStringValue();
        }
        return String.format("ТФ_%s_%s_%s_%s.xlsx",
                declarationData.getId(),
                departmentReportPeriodService.formatPeriodName(reportPeriod, SharedConstants.DATE_FORMAT),
                department.getCode(),
                asnuName);
    }

    /**
     * Создает и инициирует объект {@link SheetFillerContext} данными для выгрузки РНУ НДФЛ в Excel
     * @return
     */
    SheetFillerContext createExportDeclarationDataSheetFillerContext() {
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
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

    /**
     * Создает книгу и листы книги для выгрузки данных РНУ НДФЛ в Excel
     * @param context данные выгружаемой НФ на основе которых создаются листы
     * @return объект книги
     */
    XSSFWorkbook createExportDeclarationDataWorkbook(SheetFillerContext context) {
        int counter = 2
        int sheetIndex = 1
        XSSFWorkbook workbook = new XSSFWorkbook(scriptSpecificReportHolder.fileInputStream)
        for (NdflPerson ndflPerson : context.getNdflPersonList()) {
            int maxOperationSize = [ndflPerson.incomes.size(), ndflPerson.deductions.size(), ndflPerson.prepayments.size()].max()
            counter += maxOperationSize
            if (counter > 1_000_000) {
                counter = 2
                sheetIndex++
                workbook.cloneSheet(1)
                workbook.setSheetName(sheetIndex, "РНУ НДФЛ (" + (sheetIndex - 1) + ")")
            }
        }
        return workbook
    }

    //------------------ Import Data ----------------------
    @TypeChecked(TypeCheckingMode.SKIP)
    void importData() {

        SimpleDateFormat sdf = new SimpleDateFormat(SharedConstants.DATE_FORMAT);
        logForDebug("Начало загрузки данных первичной налоговой формы " + declarationData.id + ". Дата начала отчетного периода: " + sdf.format(getReportPeriodStartDate()) + ", дата окончания: " + sdf.format(getReportPeriodEndDate()));

        formCreationDate = logBusinessService.getFormCreationDate(declarationData.id)

        InputStream xmlInputStream = ImportInputStream;

        if (xmlInputStream == null) {
            throw new ServiceException("Отсутствует значение параметра ImportInputStream!");
        }

        // "Загрузка ТФ РНУ НДФЛ" п.9
        // Проверка соответствия атрибута ДатаОтч периоду в наименовании файла
        // reportPeriodEndDate создаётся на основании периода из имени файла

        File dFile = dataFile

        if (dFile == null) {
            throw new ServiceException("Отсутствует значение параметра dataFile!")
        }

        def reportPeriodEndDate = getReportPeriodEndDate().format(SharedConstants.DATE_FORMAT)

        def Файл = new XmlSlurper().parse(dFile)
        String reportDate = Файл.СлЧасть.'@ДатаОтч'

        if (reportPeriodEndDate != reportDate) {
            logger.warn("В ТФ неверно указана «Отчетная дата»: «${reportDate}». Должна быть указана дата окончания периода ТФ, равная «${reportPeriodEndDate}»")
        }

        def ndflPersonNum = 1;
        def success = 0

        for (infoPart in Файл.ИнфЧасть) {
            ScriptUtils.checkInterrupted()
            if (processInfoPart(infoPart, ndflPersonNum)) {
                success++
            }
            ndflPersonNum++
        }

        Collections.sort(ndflPersonCache, NdflPerson.getComparator())
        Long personRowNum = 0L
        BigDecimal incomeRowNum = new BigDecimal("0")
        BigDecimal deductionRowNum = new BigDecimal("0")
        BigDecimal prepaymentRowNum = new BigDecimal("0")
        for (NdflPerson ndflPerson : ndflPersonCache) {
            Collections.sort(ndflPerson.incomes, NdflPersonIncome.getComparator(ndflPerson))

            Collections.sort(ndflPerson.deductions, NdflPersonDeduction.getComparator(ndflPerson))

            Collections.sort(ndflPerson.prepayments, NdflPersonPrepayment.getComparator(ndflPerson))

            for (NdflPersonIncome income : ndflPerson.incomes) {
                incomeRowNum = incomeRowNum.add(new BigDecimal("1"))
                income.rowNum = incomeRowNum
            }

            for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                deductionRowNum = deductionRowNum.add(new BigDecimal("1"))
                deduction.rowNum = deductionRowNum
            }

            for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                prepaymentRowNum = prepaymentRowNum.add(new BigDecimal("1"))
                prepayment.rowNum = prepaymentRowNum
            }

            ndflPerson.rowNum = ++personRowNum
        }
        ndflPersonService.save(ndflPersonCache)
        if (success == 0) {
            logger.error("В ТФ отсутствуют операции, принадлежащие отчетному периоду.")
            logger.error("Налоговая форма не создана.")
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    boolean processInfoPart(infoPart, Integer rowNum) {

        NodeChild ndflPersonNode = infoPart.'ПолучДох'[0]

        NdflPerson ndflPerson = transformNdflPersonNode(ndflPersonNode)

        def familia = ndflPerson.lastName != null ? ndflPerson.lastName + " " : ""
        def imya = ndflPerson.firstName != null ? ndflPerson.firstName + " " : ""
        def otchestvo = ndflPerson.middleName != null ? ndflPerson.middleName : ""
        def fio = familia + imya + otchestvo
        Iterator ndflPersonOperations = infoPart.'СведОпер'.iterator()

        // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        Map<Long, Map<String, RefBookValue>> incomeCodeMap = getRefIncomeCode()

        // Коды видов вычетов
        List<String> deductionTypeList = getRefDeductionType()

        ndflPersonOperations.each { NodeChild nodeChild ->
            processNdflPersonOperation(ndflPerson, nodeChild, fio, incomeCodeMap, deductionTypeList)
        }

        //Идентификатор декларации для которой загружаются данные
        if (ndflPerson.incomes != null && !ndflPerson.incomes.isEmpty()) {
            ndflPerson.declarationDataId = declarationData.getId()
            ndflPerson.rowNum = rowNum
            ndflPersonCache.add(ndflPerson)
        } else {
            logger.warn("У ФЛ ($fio, ИНП: ${ndflPerson.inp}) отсутствуют операции, принадлежащие отчетному периоду. ФЛ не загружено в налоговую форму")
            return false
        }
        return true
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void processNdflPersonOperation(NdflPerson ndflPerson, NodeChild ndflPersonOperationsNode, String fio,
                                    Map<Long, Map<String, RefBookValue>> incomeCodeMap, List<String> deductionTypeList) {

        List<NdflPersonIncome> incomes = new ArrayList<NdflPersonIncome>();
        // Проверка операции (не строк)
        if (isDatesInPeriod(ndflPersonOperationsNode, ndflPerson, toString(ndflPersonOperationsNode.'@КПП'), toString(ndflPersonOperationsNode.'@ОКТМО'), ndflPerson.inp, fio)) {
            // При создание объекто операций доходов выполняется проверка на соответствие дат отчетному периоду
            incomes.addAll(ndflPersonOperationsNode.'СведДохНал'.collect {
                transformNdflPersonIncome(it, ndflPerson, toString(ndflPersonOperationsNode.'@КПП'), toString(ndflPersonOperationsNode.'@ОКТМО'), ndflPerson.inp, fio, incomeCodeMap)
            });
            // Если проверка на даты не прошла, то операция не добавляется.
            // https://jira.aplana.com/browse/SBRFNDFL-1350 - если дата не прошла то ничего не загружаем и выводим сообщение
            if (incomes.contains(null)) {
                return
            }

            incomes.each {
                if (it != null) {
                    ndflPerson.incomes.add(it);
                }
            }

            List<NdflPersonDeduction> deductions = new ArrayList<NdflPersonDeduction>();
            deductions.addAll(ndflPersonOperationsNode.'СведВыч'.collect {
                transformNdflPersonDeduction(it, ndflPerson, fio, deductionTypeList)
            });
            ndflPerson.deductions.addAll(deductions)

            List<NdflPersonPrepayment> prepayments = new ArrayList<NdflPersonPrepayment>();
            prepayments.addAll(ndflPersonOperationsNode.'СведАванс'.collect {
                transformNdflPersonPrepayment(it)
            });
            ndflPerson.prepayments.addAll(prepayments);
        }
    }

    /**
     * Проверка на принадлежность операций периоду при загрузке ТФ
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    boolean isDatesInPeriod(NodeChild ndflPersonOperationsNode, NdflPerson ndflPerson, String kpp, String oktmo, String inp, String fio) {

        def operationId = toString((GPathResult) ndflPersonOperationsNode.getProperty('@ИдОпер'))
        def incomeAccruedRows = new ArrayList<Tuple2>();
        def incomePayoutRows = new ArrayList<Tuple2>();
        def taxRows = new ArrayList<Tuple2>();

        // Доход.Дата.Начисление
        boolean incomeAccruedDateOk = false
        // Доход.Дата.Выплата
        boolean incomePayoutDateOk = false
        // НДФЛ.Расчет.Дата
        boolean taxDateOk = false

        SimpleDateFormat formatter = new SimpleDateFormat(SharedConstants.DATE_FORMAT)
        ndflPersonOperationsNode.childNodes().each { node ->
            if (!incomeAccruedDateOk && !incomePayoutDateOk && !taxDateOk) {
                def rowNum = node.attributes['НомСтр']
                if (!incomeAccruedDateOk) {
                    if (node.name() == "СведДохНал" && node.attributes.containsKey('ДатаДохНач') && node.attributes['ДатаДохНач'] != null) {
                        Date incomeAccruedDate = formatter.parse(node.attributes['ДатаДохНач']);
                        incomeAccruedRows.add(new Tuple2(rowNum, incomeAccruedDate))
                        incomeAccruedDateOk = dateRelateToCurrentPeriod(incomeAccruedDate)
                    }
                }
                if (!incomePayoutDateOk) {
                    if (node.name() == "СведДохНал" && node.attributes.containsKey('ДатаДохВыпл') && node.attributes['ДатаДохВыпл'] != null) {
                        Date incomePayoutDate = formatter.parse(node.attributes['ДатаДохВыпл']);
                        incomePayoutRows.add(new Tuple2(rowNum, incomePayoutDate))
                        incomePayoutDateOk = dateRelateToCurrentPeriod(incomePayoutDate)
                    }
                }
                if (!taxDateOk) {
                    if (node.name() == "СведДохНал" && node.attributes.containsKey('ДатаНалог') && node.attributes['ДатаНалог'] != null) {
                        Date taxDate = formatter.parse(node.attributes['ДатаНалог']);
                        taxRows.add(new Tuple2(rowNum, taxDate))
                        taxDateOk = dateRelateToCurrentPeriod(taxDate)
                    }
                }
            }
        }

        if (incomeAccruedDateOk || incomePayoutDateOk || taxDateOk) {
            return true
        } else {
            DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)
            if (!incomeAccruedDateOk && !incomeAccruedRows.empty) {
                // Дата.Начисление не попала в период
                for (Tuple2 row : incomeAccruedRows) {
                    logPeriodError(departmentReportPeriod, row[0], C_INCOME_ACCRUED_DATE, row[1], inp, fio, operationId)
                }
            }
            if (!incomePayoutDateOk && !incomePayoutRows.empty) {
                // Дата.Выплата не попала в период
                for (Tuple2 row : incomePayoutRows) {
                    logPeriodError(departmentReportPeriod, row[0], C_INCOME_PAYOUT_DATE, row[1], inp, fio, operationId)
                }
            }
            if (!taxDateOk && !taxRows.empty) {
                // НДФЛ.Расчет.Дата не попала в период
                for (Tuple2 row : taxRows) {
                    logPeriodError(departmentReportPeriod, row[0], C_TAX_DATE, row[1], inp, fio, operationId)
                }
            }
            return false
        }
    }

    void logPeriodError(DepartmentReportPeriod departmentReportPeriod, String rowNum, String group, Date date, String inp, String fio, String operationId) {
        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, rowNum)
        String baseMessage = "Значения гр. \"%s\" (\"%s\") не входит в отчетный период налоговой формы: \"%s\". Операция (\"%s\") не загружена в налоговую форму. ФЛ: \"%s\", ИНП: \"%s\""
        String errMsg = String.format(baseMessage,
                group,
                date != null ? ScriptUtils.formatDate(date) : "Не определено",
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName(),
                operationId,
                fio, inp
        )
        logger.warnExp("%s. %s.", "Проверка соответствия дат операций РНУ НДФЛ отчетному периоду", "", pathError,
                errMsg)
    }

    // Проверка принадлежности даты к периоду формы
    boolean dateRelateToCurrentPeriod(Date date) {
        //https://jira.aplana.com/browse/SBRFNDFL-581 замена getReportPeriodCalendarStartDate() на getReportPeriodStartDate
        if (date == null || (date >= getReportPeriodCalendarStartDate() && date <= getReportPeriodEndDate())) {
            return true
        }
        return false
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

    NdflPersonIncome transformNdflPersonIncome(NodeChild node, NdflPerson ndflPerson, String kpp, String oktmo, String inp, String fio,
                                               Map<Long, Map<String, RefBookValue>> incomeCodeMap) {
        def operationNode = node.parent();

        Date incomeAccruedDate = toDate((GPathResult) node.getProperty('@ДатаДохНач'))
        Date incomePayoutDate = toDate((GPathResult) node.getProperty('@ДатаДохВыпл'))
        Date taxDate = toDate((GPathResult) node.getProperty('@ДатаНалог'))

        NdflPersonIncome personIncome = new NdflPersonIncome()
        personIncome.rowNum = toBigDecimal((GPathResult) node.getProperty('@НомСтр'))
        personIncome.incomeCode = toString((GPathResult) node.getProperty('@КодДох'))
        personIncome.incomeType = toString((GPathResult) node.getProperty('@ТипДох'))

        personIncome.operationId = toString((GPathResult) operationNode.getProperty('@ИдОпер'))
        personIncome.oktmo = toString((GPathResult) operationNode.getProperty('@ОКТМО'))
        personIncome.kpp = toString((GPathResult) operationNode.getProperty('@КПП'))

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
        personIncome.taxSumm = toLong((GPathResult) node.getProperty('@НалПерСумм'))
        personIncome.asnuId = declarationData.asnuId
        personIncome.modifiedDate = formCreationDate

        // Спр5 Код вида дохода (Необязательное поле)
        if (personIncome.incomeCode != null && personIncome.incomeAccruedDate != null && !incomeCodeMap.find { key, value ->
            value.CODE?.stringValue == personIncome.incomeCode &&
                    personIncome.incomeAccruedDate >= value.record_version_from?.dateValue &&
                    personIncome.incomeAccruedDate <= value.record_version_to?.dateValue
        }) {
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [fio, ndflPerson.inp, personIncome.operationId])
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

    NdflPersonDeduction transformNdflPersonDeduction(NodeChild node, NdflPerson ndflPerson, String fio,
                                                     List<String> deductionTypeList) {

        NdflPersonDeduction personDeduction = new NdflPersonDeduction()
        personDeduction.rowNum = toBigDecimal((GPathResult) node.getProperty('@НомСтр'))
        personDeduction.operationId = toString((GPathResult) node.parent().getProperty('@ИдОпер'))
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

        if (!deductionTypeList.contains(personDeduction.typeCode)) {
            String fioAndInpAndOperId = sprintf(TEMPLATE_PERSON_FL_OPER, [fio, ndflPerson.inp, personDeduction.operationId])
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

    NdflPersonPrepayment transformNdflPersonPrepayment(NodeChild node) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
        personPrepayment.rowNum = toBigDecimal((GPathResult) node.getProperty('@НомСтр'))
        personPrepayment.operationId = toString((GPathResult) node.parent().getProperty('@ИдОпер'))
        personPrepayment.summ = toBigDecimal((GPathResult) node.getProperty('@Аванс'))
        personPrepayment.notifNum = toString((GPathResult) node.getProperty('@УведНом'))
        personPrepayment.notifDate = toDate((GPathResult) node.getProperty('@УведДата'))
        personPrepayment.notifSource = toString((GPathResult) node.getProperty('@УведИФНС'))
        personPrepayment.asnuId = declarationData.asnuId
        personPrepayment.modifiedDate = formCreationDate
        return personPrepayment;
    }

    Integer toInteger(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? Integer.valueOf(xmlNode.text()) : null;
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null;
        }
    }

    Long toLong(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? Long.valueOf(xmlNode.text()) : null;
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null;
        }
    }

    BigDecimal toBigDecimal(GPathResult xmlNode) throws NumberFormatException {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? new BigDecimal(xmlNode.text()) : null;
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null;
        }
    }

    Date toDate(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            SimpleDateFormat format = new java.text.SimpleDateFormat(SharedConstants.DATE_FORMAT)
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
            return null;
        }
    }

    String toString(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            return xmlNode.text() != null && !xmlNode.text().isEmpty() ? StringUtils.cleanString(xmlNode.text()) : null;
        } else {
            return null;
        }
    }

    String formatDate(date) {
        return ScriptUtils.formatDate((Date) date, SharedConstants.DATE_FORMAT)
    }

    //>------------------< REF BOOK >----------------------<

    // Дата начала отчетного периода
    def periodStartDate = null

    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]

    ReportPeriod sourceReportPeriod = null

    Map<Integer, DepartmentReportPeriod> departmentReportPeriodMap = [:]

    Map<Integer, DeclarationTemplate> declarationTemplateMap = [:]

    Map<Integer, String> departmentFullNameMap = [:]

    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"
    final String SECTION_LINE_RANGE_MSG = "Раздел %s. Строки %s"

    // Коды видов вычетов
    List<String> deductionTypeCache = []

    // Дата начала отчетного периода
    Date reportPeriodStartDate = null

    DeclarationTemplate getDeclarationTemplateById(Integer id) {
        if (id != null && declarationTemplateMap.get(id) == null) {
            declarationTemplateMap.put(id, (DeclarationTemplate) declarationService.getTemplate(id))
        }
        return declarationTemplateMap.get(id)
    }

    ReportPeriod getReportPeriod() {
        if (sourceReportPeriod == null) {
            sourceReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        }
        return sourceReportPeriod
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
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodCalendarStartDate() {
        if (reportPeriodStartDate == null) {
            reportPeriodStartDate = reportPeriodService.getCalendarStartDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodStartDate
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
        return mapResult;
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
        return deductionTypeCache;
    }

    /**
     * Получить записи справочника по его идентификатору в отчётном периоде
     * @param refBookId - идентификатор справочника
     * @return - список записей справочника
     */
    PagingResult<Map<String, RefBookValue>> getRefBook(long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        PagingResult<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecordsVersion(getReportPeriodStartDate(), getReportPeriodEndDate(), null, null)
        if (refBookList == null || refBookList.size() == 0) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
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
}

/**
 * Класс инкапсулирующий данные необходимые для заполнения листов Excel файла
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
            idNdflPersonMap = new HashMap<>();
            for (NdflPerson ndflPerson : ndflPersonList) {
                idNdflPersonMap.put(ndflPerson.getId(), ndflPerson);
            }
        }
        return idNdflPersonMap;
    }
}

/**
 * Интерфейс отвечающий за заполнение листа Excel файла
 */
interface SheetFiller {
    void fillSheet(Workbook wb, SheetFillerContext context);
}

/**
 * Фабрика для получения экземплярая {@link SheetFiller} по индексу листа
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
 * Содержит логику заполнения заголовка листа спецотчета РНУ НДФЛ
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
 * Содержит логику заполнения реквизитов спецотчета РНУ НДФЛ
 */
@TypeChecked
class RequisitesSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        Sheet sheet = wb.getSheetAt(1);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        for (NdflPerson np : context.getNdflPersonList()) {
            ScriptUtils.checkInterrupted();
            Row row = sheet.createRow(index);
            Cell cell1 = row.createCell(1);
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(np.getRowNum().intValue());
            Cell cell2 = row.createCell(2);
            cell2.setCellStyle(centeredStyle)
            cell2.setCellValue(np.getInp() != null ? np.getInp() : "");
            Cell cell3 = row.createCell(3);
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(np.getLastName() != null ? np.getLastName() : "");
            Cell cell4 = row.createCell(4);
            cell4.setCellStyle(centeredStyle)
            cell4.setCellValue(np.getFirstName() != null ? np.getFirstName() : "");
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(np.getMiddleName() != null ? np.getMiddleName() : "");
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(centeredStyleDate)
            if (np.birthDay != null) {
                cell6.setCellValue(np.birthDay);
            }
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(np.getCitizenship() != null ? np.getCitizenship() : "");
            Cell cell8 = row.createCell(8);
            cell8.setCellStyle(centeredStyle)
            cell8.setCellValue(np.getInnNp() != null ? np.getInnNp() : "");
            Cell cell9 = row.createCell(9);
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(np.getInnForeign() != null ? np.getInnForeign() : "");
            Cell cell10 = row.createCell(10);
            cell10.setCellStyle(centeredStyle)
            cell10.setCellValue(np.getIdDocType() != null ? np.getIdDocType() : "");
            Cell cell11 = row.createCell(11);
            cell11.setCellStyle(centeredStyle)
            cell11.setCellValue(np.getIdDocNumber() != null ? np.getIdDocNumber() : "");
            Cell cell12 = row.createCell(12);
            cell12.setCellStyle(centeredStyle)
            cell12.setCellValue(np.getStatus() != null ? np.getStatus() : "");
            Cell cell13 = row.createCell(13);
            cell13.setCellStyle(textCenteredStyle)
            cell13.setCellValue(np.getRegionCode() != null ? np.getRegionCode() : "");
            Cell cell14 = row.createCell(14);
            cell14.setCellStyle(centeredStyle)
            cell14.setCellValue(np.getPostIndex() != null ? np.getPostIndex() : "");
            Cell cell15 = row.createCell(15);
            cell15.setCellStyle(centeredStyle)
            cell15.setCellValue(np.getArea() != null ? np.getArea() : "");
            Cell cell16 = row.createCell(16);
            cell16.setCellStyle(centeredStyle)
            cell16.setCellValue(np.getCity() != null ? np.getCity() : "");
            Cell cell17 = row.createCell(17);
            cell17.setCellStyle(centeredStyle)
            cell17.setCellValue(np.getLocality() != null ? np.getLocality() : "");
            Cell cell18 = row.createCell(18);
            cell18.setCellStyle(centeredStyle)
            cell18.setCellValue(np.getStreet() != null ? np.getStreet() : "");
            Cell cell19 = row.createCell(19);
            cell19.setCellStyle(centeredStyle)
            cell19.setCellValue(np.getHouse() != null ? np.getHouse() : "");
            Cell cell20 = row.createCell(20);
            cell20.setCellStyle(centeredStyle)
            cell20.setCellValue(np.getBuilding() != null ? np.getBuilding() : "");
            Cell cell21 = row.createCell(21);
            cell21.setCellStyle(centeredStyle)
            cell21.setCellValue(np.getFlat() != null ? np.getFlat() : "");
            Cell cell22 = row.createCell(22)
            cell22.setCellStyle(centeredStyle)
            cell22.setCellValue(np.snils != null ? np.snils : "")
            index++;
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
        List<NdflPersonIncome> ndflPersonIncomeList = context.getNdflPersonIncomeList();
        Sheet sheet = wb.getSheetAt(2);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.createBorderStyle()
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPersonIncome npi : ndflPersonIncomeList) {
            ScriptUtils.checkInterrupted();

            Row row = sheet.createRow(index);
            Cell cell1 = row.createCell(1);
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npi.getRowNum().toString());
            Cell cell2 = row.createCell(2);
            cell2.setCellStyle(centeredStyle)
            String inp = context.getIdNdflPersonMap().get(npi.getNdflPersonId()).getInp();
            cell2.setCellValue(inp != null ? inp : "");
            Cell cell3 = row.createCell(3);
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(npi.getOperationId() != null ? npi.getOperationId() : "");
            Cell cell4 = row.createCell(4);
            cell4.setCellStyle(centeredStyle)
            cell4.setCellValue(npi.getIncomeCode() != null ? npi.getIncomeCode() : "");
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(textCenteredStyle)
            cell5.setCellValue(npi.getIncomeType() != null ? npi.getIncomeType() : "");
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(centeredStyleDate)
            if (npi.incomeAccruedDate != null) {
                cell6.setCellValue(npi.incomeAccruedDate);
            }
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(centeredStyleDate)
            if (npi.incomePayoutDate != null) {
                cell7.setCellValue(npi.incomePayoutDate);
            }
            Cell cell8 = row.createCell(8);
            cell8.setCellStyle(textCenteredStyle)
            cell8.setCellValue(npi.getKpp() != null ? npi.getKpp() : "");
            Cell cell9 = row.createCell(9);
            cell9.setCellStyle(textCenteredStyle)
            cell9.setCellValue(npi.getOktmo() != null ? npi.getOktmo() : "");
            Cell cell10 = row.createCell(10);
            cell10.setCellStyle(numberCenteredStyle)
            if (npi.incomeAccruedSumm != null) {
                cell10.setCellValue(npi.incomeAccruedSumm.doubleValue());
            }
            Cell cell11 = row.createCell(11);
            cell11.setCellStyle(numberCenteredStyle)
            if (npi.incomePayoutSumm != null) {
                cell11.setCellValue(npi.incomePayoutSumm.doubleValue());
            }
            Cell cell12 = row.createCell(12);
            cell12.setCellStyle(numberCenteredStyle)
            if (npi.totalDeductionsSumm != null) {
                cell12.setCellValue(npi.totalDeductionsSumm.doubleValue());
            }
            Cell cell13 = row.createCell(13);
            cell13.setCellStyle(numberCenteredStyle)
            if (npi.taxBase != null) {
                cell13.setCellValue(npi.taxBase.doubleValue());
            }
            Cell cell14 = row.createCell(14);
            cell14.setCellStyle(borderStyle)
            if (npi.taxRate != null) {
                cell14.setCellValue(npi.taxRate);
            }
            Cell cell15 = row.createCell(15);
            cell15.setCellStyle(centeredStyleDate)
            if (npi.taxDate != null) {
                cell15.setCellValue(npi.taxDate);
            }

            Cell cell16 = row.createCell(16);
            cell16.setCellStyle(numberCenteredStyle)
            if (npi.calculatedTax != null) {
                cell16.setCellValue(npi.calculatedTax.doubleValue())
            }
            Cell cell17 = row.createCell(17);
            cell17.setCellStyle(numberCenteredStyle)
            if (npi.withholdingTax != null) {
                cell17.setCellValue(npi.withholdingTax.doubleValue());
            }
            Cell cell18 = row.createCell(18);
            cell18.setCellStyle(numberCenteredStyle)
            if (npi.notHoldingTax != null) {
                cell18.setCellValue(npi.notHoldingTax.doubleValue());
            }
            Cell cell19 = row.createCell(19);
            cell19.setCellStyle(numberCenteredStyle)
            if (npi.overholdingTax != null) {
                cell19.setCellValue(npi.overholdingTax.doubleValue());
            }
            Cell cell20 = row.createCell(20);
            cell20.setCellStyle(numberCenteredStyle)
            if (npi.refoundTax != null) {
                cell20.setCellValue(npi.refoundTax.doubleValue());
            }
            Cell cell21 = row.createCell(21);
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
            Cell cell22 = row.createCell(22);
            cell22.setCellStyle(centeredStyleDate)
            if (npi.paymentDate != null) {
                cell22.setCellValue(npi.paymentDate);
            }
            Cell cell23 = row.createCell(23);
            cell23.setCellStyle(centeredStyle)
            cell23.setCellValue(npi.getPaymentNumber() != null ? npi.getPaymentNumber() : "");
            Cell cell24 = row.createCell(24);
            cell24.setCellStyle(numberCenteredStyle)
            if (npi.taxSumm != null) {
                cell24.setCellValue(npi.taxSumm.intValue());
            }
            index++;
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
        List<NdflPersonDeduction> ndflPersonDeductionList = context.getNdflPersonDeductionList();
        Sheet sheet = wb.getSheetAt(3);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.createBorderStyle()
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPersonDeduction npd : ndflPersonDeductionList) {
            ScriptUtils.checkInterrupted();

            Row row = sheet.createRow(index);
            Cell cell1 = row.createCell(1);
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npd.rowNum.intValue());
            Cell cell2 = row.createCell(2);
            cell2.setCellStyle(centeredStyle)
            String inp = context.getIdNdflPersonMap().get(npd.getNdflPersonId()).getInp();
            cell2.setCellValue(inp != null ? inp : "");
            Cell cell3 = row.createCell(3);
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(npd.getTypeCode() != null ? npd.getTypeCode() : "");
            Cell cell4 = row.createCell(4);
            cell4.setCellStyle(centeredStyle)
            cell4.setCellValue(npd.getNotifType() != null ? npd.getNotifType() : "");
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(centeredStyleDate)
            if (npd.notifDate != null) {
                cell5.setCellValue(npd.notifDate);
            }
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(centeredStyle)
            cell6.setCellValue(npd.getNotifNum() != null ? npd.getNotifNum() : "б/н");
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(npd.getNotifSource() != null ? npd.getNotifSource() : "");
            Cell cell8 = row.createCell(8);
            cell8.setCellStyle(numberCenteredStyle)
            if (npd.notifSumm != null) {
                cell8.setCellValue(npd.notifSumm.doubleValue());
            }
            Cell cell9 = row.createCell(9);
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(npd.getOperationId() != null ? npd.getOperationId() : "");
            Cell cell10 = row.createCell(10);
            cell10.setCellStyle(centeredStyleDate)
            if (npd.incomeAccrued != null) {
                cell10.setCellValue(npd.incomeAccrued);
            }
            Cell cell11 = row.createCell(11);
            cell11.setCellStyle(centeredStyle)
            cell11.setCellValue(npd.getIncomeCode() != null ? npd.getIncomeCode() : "");
            Cell cell12 = row.createCell(12);
            cell12.setCellStyle(numberCenteredStyle)
            if (npd.incomeSumm != null) {
                cell12.setCellValue(npd.incomeSumm.doubleValue());
            }
            Cell cell13 = row.createCell(13);
            cell13.setCellStyle(centeredStyleDate)
            if (npd.periodPrevDate != null) {
                cell13.setCellValue(npd.periodPrevDate);
            }
            Cell cell14 = row.createCell(14);
            cell14.setCellStyle(numberCenteredStyle)
            if (npd.periodPrevSumm != null) {
                cell14.setCellValue(npd.periodPrevSumm.doubleValue());
            }
            Cell cell15 = row.createCell(15);
            cell15.setCellStyle(centeredStyleDate)
            if (npd.periodCurrDate != null) {
                cell15.setCellValue(npd.periodCurrDate);
            }
            Cell cell16 = row.createCell(16);
            cell16.setCellStyle(numberCenteredStyle)
            if (npd.periodCurrSumm != null) {
                cell16.setCellValue(npd.periodCurrSumm.doubleValue());
            }
            index++;
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
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = context.getNdflPersonPrepaymentList();
        Sheet sheet = wb.getSheetAt(4);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.createBorderStyle()
        CellStyle centeredStyle = styler.createBorderStyleCenterAligned()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle centeredStyleDate = styler.createBorderStyleCenterAlignedDate()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPersonPrepayment npp : ndflPersonPrepaymentList) {
            ScriptUtils.checkInterrupted();

            Row row = sheet.createRow(index);
            Cell cell1 = row.createCell(1);
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npp.rowNum.doubleValue());
            Cell cell2 = row.createCell(2);
            cell2.setCellStyle(centeredStyle)
            String inp = context.getIdNdflPersonMap().get(npp.getNdflPersonId()).getInp();
            cell2.setCellValue(inp != null ? inp : "");
            Cell cell3 = row.createCell(3);
            cell3.setCellStyle(centeredStyle)
            cell3.setCellValue(npp.getOperationId() != null ? npp.getOperationId() : "");
            Cell cell4 = row.createCell(4);
            cell4.setCellStyle(numberCenteredStyle)
            if (npp.summ != null) {
                cell4.setCellValue(npp.summ.doubleValue());
            }
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(npp.getNotifNum() != null ? npp.getNotifNum() : "");
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(centeredStyleDate)
            if (npp.notifDate != null) {
                cell6.setCellValue(npp.notifDate);
            }
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(npp.getNotifSource() != null ? npp.getNotifSource() : "");
            index++;
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
        // Счетчик заполненных строк
        int counter = OFFSET
        // Указатель на индекс позиции строки
        int pointer = OFFSET
        // Максимальное количество строк для заполнеиния на одном листе
        final int MAX_ROWS = 1_000_000
        // Индекс листа
        int sheetIndex = 1
        Sheet sheet = wb.getSheetAt(sheetIndex)
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.createVerticalByTopHorizontalByCenter()
        CellStyle centeredStyleDate = styler.createVerticalByTopHorizontalByCenterDate()
        CellStyle textCenteredStyle = styler.createBorderStyleCenterAlignedTypeText()
        CellStyle numberCenteredStyle = styler.createBorderStyleCenterAlignedTypeNumber()
        for (NdflPerson np : context.getNdflPersonList()) {
            ScriptUtils.checkInterrupted()
            // определяем для физлица какой вид операций имеет наибольшее количество строк (доход, вычет, аванс)
            int maxOperationSize = [np.incomes.size(), np.deductions.size(), np.prepayments.size()].max()
            // Определяем сколько строк будет заполнено на листе после окончания заполнения для текущего физлица
            counter += maxOperationSize
            // если количество заполненных строк будет больше допустимого, тогда начинаем заполнять следующий лист
            if (counter > MAX_ROWS) {
                counter = OFFSET + maxOperationSize
                pointer = OFFSET
                sheetIndex++
                sheet = wb.getSheetAt(sheetIndex - 1)
            }
            // Сортировка каждого вида операции по № пп
            Collections.sort(np.incomes, new Comparator<NdflPersonIncome>() {
                @Override
                int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                    return o1.rowNum.compareTo(o2.rowNum)
                }
            })
            Collections.sort(np.deductions, new Comparator<NdflPersonDeduction>() {
                @Override
                int compare(NdflPersonDeduction o1, NdflPersonDeduction o2) {
                    return o1.rowNum.compareTo(o2.rowNum)
                }
            })
            Collections.sort(np.prepayments, new Comparator<NdflPersonPrepayment>() {
                @Override
                int compare(NdflPersonPrepayment o1, NdflPersonPrepayment o2) {
                    return o1.rowNum.compareTo(o2.rowNum)
                }
            })
            // Для каждого физлица формируем  количество строк равное максимальному количеству строк из каждого вида операций
            for (int i = 0; i < maxOperationSize; i++) {
                StringBuilder cell0Value = new StringBuilder(np.id.toString())
                cell0Value.append("_")
                Row row = sheet.createRow(pointer)
                // Заполненние данными из раздела "Реквизиты"
                Cell cell_0 = row.createCell(0)
                cell_0.setCellStyle(centeredStyle)
                Cell cell_1 = row.createCell(1)
                cell_1.setCellStyle(centeredStyle)
                cell_1.setCellType(Cell.CELL_TYPE_STRING)
                if (i < np.incomes.size()) {
                    cell_1.setCellValue(np.incomes.get(i).rowNum != null ? np.incomes.get(i).rowNum.toString() : "")
                }
                Cell cell_2 = row.createCell(2);
                cell_2.setCellStyle(centeredStyle)
                cell_2.setCellValue(np.getInp() != null ? np.getInp() : "");
                Cell cell_3 = row.createCell(3);
                cell_3.setCellStyle(centeredStyle)
                cell_3.setCellValue(np.getLastName() != null ? np.getLastName() : "");
                Cell cell_4 = row.createCell(4);
                cell_4.setCellStyle(centeredStyle)
                cell_4.setCellValue(np.getFirstName() != null ? np.getFirstName() : "");
                Cell cell_5 = row.createCell(5);
                cell_5.setCellStyle(centeredStyle)
                cell_5.setCellValue(np.getMiddleName() != null ? np.getMiddleName() : "");
                Cell cell_6 = row.createCell(6);
                cell_6.setCellStyle(centeredStyleDate)
                if (np.birthDay != null) {
                    cell_6.setCellValue(np.birthDay);
                }
                Cell cell_7 = row.createCell(7);
                cell_7.setCellStyle(centeredStyle)
                cell_7.setCellValue(np.getCitizenship() != null ? np.getCitizenship() : "");
                Cell cell_8 = row.createCell(8);
                cell_8.setCellStyle(centeredStyle)
                cell_8.setCellValue(np.getInnNp() != null ? np.getInnNp() : "");
                Cell cell_9 = row.createCell(9);
                cell_9.setCellStyle(centeredStyle)
                cell_9.setCellValue(np.getInnForeign() != null ? np.getInnForeign() : "");
                Cell cell_10 = row.createCell(10);
                cell_10.setCellStyle(centeredStyle)
                cell_10.setCellValue(np.getIdDocType() != null ? np.getIdDocType() : "");
                Cell cell_11 = row.createCell(11);
                cell_11.setCellStyle(centeredStyle)
                cell_11.setCellValue(np.getIdDocNumber() != null ? np.getIdDocNumber() : "");
                Cell cell_12 = row.createCell(12);
                cell_12.setCellStyle(centeredStyle)
                cell_12.setCellValue(np.getStatus() != null ? np.getStatus() : "");
                Cell cell_13 = row.createCell(13);
                cell_13.setCellStyle(textCenteredStyle)
                cell_13.setCellValue(np.getRegionCode() != null ? np.getRegionCode() : "");
                Cell cell_14 = row.createCell(14);
                cell_14.setCellStyle(centeredStyle)
                cell_14.setCellValue(np.getPostIndex() != null ? np.getPostIndex() : "");
                Cell cell_15 = row.createCell(15);
                cell_15.setCellStyle(centeredStyle)
                cell_15.setCellValue(np.getArea() != null ? np.getArea() : "");
                Cell cell_16 = row.createCell(16);
                cell_16.setCellStyle(centeredStyle)
                cell_16.setCellValue(np.getCity() != null ? np.getCity() : "");
                Cell cell_17 = row.createCell(17);
                cell_17.setCellStyle(centeredStyle)
                cell_17.setCellValue(np.getLocality() != null ? np.getLocality() : "");
                Cell cell_18 = row.createCell(18);
                cell_18.setCellStyle(centeredStyle)
                cell_18.setCellValue(np.getStreet() != null ? np.getStreet() : "");
                Cell cell_19 = row.createCell(19);
                cell_19.setCellStyle(centeredStyle)
                cell_19.setCellValue(np.getHouse() != null ? np.getHouse() : "");
                Cell cell_20 = row.createCell(20);
                cell_20.setCellStyle(centeredStyle)
                cell_20.setCellValue(np.getBuilding() != null ? np.getBuilding() : "");
                Cell cell_21 = row.createCell(21);
                cell_21.setCellStyle(centeredStyle)
                cell_21.setCellValue(np.getFlat() != null ? np.getFlat() : "");
                Cell cell_22 = row.createCell(22)
                cell_22.setCellStyle(centeredStyle)
                cell_22.setCellValue(np.snils != null ? np.snils : "")
                // Заполнение данными из раздела "Сведения о доходах"
                Cell cell_23 = row.createCell(23);
                cell_23.setCellStyle(textCenteredStyle)
                Cell cell_24 = row.createCell(24);
                cell_24.setCellStyle(centeredStyle)
                Cell cell_25 = row.createCell(25);
                cell_25.setCellStyle(textCenteredStyle)
                Cell cell_26 = row.createCell(26);
                cell_26.setCellStyle(centeredStyleDate)
                Cell cell_27 = row.createCell(27);
                cell_27.setCellStyle(centeredStyleDate)
                Cell cell_28 = row.createCell(28);
                cell_28.setCellStyle(textCenteredStyle)
                Cell cell_29 = row.createCell(29);
                cell_29.setCellStyle(textCenteredStyle)
                Cell cell_30 = row.createCell(30);
                cell_30.setCellStyle(numberCenteredStyle)
                Cell cell_31 = row.createCell(31);
                cell_31.setCellStyle(numberCenteredStyle)
                Cell cell_32 = row.createCell(32);
                cell_32.setCellStyle(numberCenteredStyle)
                Cell cell_33 = row.createCell(33);
                cell_33.setCellStyle(numberCenteredStyle)
                Cell cell_34 = row.createCell(34);
                cell_34.setCellStyle(centeredStyle)
                Cell cell_35 = row.createCell(35);
                cell_35.setCellStyle(centeredStyleDate)
                Cell cell_36 = row.createCell(36);
                cell_36.setCellStyle(numberCenteredStyle)
                Cell cell_37 = row.createCell(37);
                cell_37.setCellStyle(numberCenteredStyle)
                Cell cell_38 = row.createCell(38);
                cell_38.setCellStyle(numberCenteredStyle)
                Cell cell_39 = row.createCell(39);
                cell_39.setCellStyle(numberCenteredStyle)
                Cell cell_40 = row.createCell(40);
                cell_40.setCellStyle(numberCenteredStyle)
                Cell cell_41 = row.createCell(41);
                Cell cell_42 = row.createCell(42);
                cell_42.setCellStyle(centeredStyleDate)
                Cell cell_43 = row.createCell(43);
                cell_43.setCellStyle(centeredStyle)
                Cell cell_44 = row.createCell(44);
                cell_44.setCellStyle(numberCenteredStyle)
                if (i < np.incomes.size()) {
                    NdflPersonIncome npi = np.incomes.get(i)
                    cell0Value.append(npi.id.toString())
                    cell_23.setCellValue(npi.getOperationId() != null ? npi.getOperationId() : "")
                    cell_24.setCellValue(npi.getIncomeCode() != null ? npi.getIncomeCode() : "")
                    cell_25.setCellValue(npi.getIncomeType() != null ? npi.getIncomeType() : "")
                    if (npi.incomeAccruedDate != null) {
                        cell_26.setCellValue(npi.incomeAccruedDate);
                    }
                    if (npi.incomePayoutDate != null) {
                        cell_27.setCellValue(npi.incomePayoutDate)
                    }
                    cell_28.setCellValue(npi.getKpp() != null ? npi.getKpp() : "")
                    cell_29.setCellValue(npi.getOktmo() != null ? npi.getOktmo() : "")
                    if (npi.incomeAccruedSumm != null) {
                        cell_30.setCellValue(npi.incomeAccruedSumm.doubleValue())
                    }
                    if (npi.incomePayoutSumm != null) {
                        cell_31.setCellValue(npi.incomePayoutSumm.doubleValue());
                    }
                    if (npi.totalDeductionsSumm != null) {
                        cell_32.setCellValue(npi.totalDeductionsSumm.doubleValue());
                    }
                    if (npi.taxBase != null) {
                        cell_33.setCellValue(npi.taxBase.doubleValue());
                    }
                    if (npi.taxRate != null) {
                        cell_34.setCellValue(npi.taxRate);
                    }
                    if (npi.taxDate != null) {
                        cell_35.setCellValue(npi.taxDate);
                    }
                    if (npi.calculatedTax != null) {
                        cell_36.setCellValue(npi.calculatedTax.doubleValue())
                    }
                    if (npi.withholdingTax != null) {
                        cell_37.setCellValue(npi.withholdingTax.doubleValue());
                    }
                    if (npi.notHoldingTax != null) {
                        cell_38.setCellValue(npi.notHoldingTax.doubleValue());
                    }
                    if (npi.overholdingTax != null) {
                        cell_39.setCellValue(npi.overholdingTax.doubleValue());
                    }
                    if (npi.refoundTax != null) {
                        cell_40.setCellValue(npi.refoundTax.doubleValue());
                    }
                    if (npi.taxTransferDate != null) {
                        if (npi.taxTransferDate?.format(SharedConstants.DATE_FORMAT) == SharedConstants.DATE_ZERO_AS_DATE) {
                            cell_41.setCellStyle(centeredStyle)
                            cell_41.setCellValue(SharedConstants.DATE_ZERO_AS_STRING)
                        } else {
                            cell_41.setCellStyle(centeredStyleDate)
                            cell_41.setCellValue(npi.taxTransferDate);
                        }
                    }
                    if (npi.paymentDate != null) {
                        cell_42.setCellValue(npi.paymentDate);
                    }
                    cell_43.setCellValue(npi.getPaymentNumber() != null ? npi.getPaymentNumber() : "");
                    if (npi.taxSumm != null) {
                        cell_44.setCellValue(npi.taxSumm.intValue());
                    }
                }
                cell0Value.append("_")
                // Заполнение данными из раздела "Сведения о вычетах"
                Cell cell_45 = row.createCell(45);
                cell_45.setCellStyle(centeredStyle)
                Cell cell_46 = row.createCell(46);
                cell_46.setCellStyle(centeredStyle)
                Cell cell_47 = row.createCell(47);
                cell_47.setCellStyle(centeredStyleDate)
                Cell cell_48 = row.createCell(48);
                cell_48.setCellStyle(centeredStyle)
                Cell cell_49 = row.createCell(49);
                cell_49.setCellStyle(centeredStyle)
                Cell cell_50 = row.createCell(50);
                cell_50.setCellStyle(numberCenteredStyle)
                Cell cell_51 = row.createCell(51);
                cell_51.setCellStyle(textCenteredStyle)
                Cell cell_52 = row.createCell(52);
                cell_52.setCellStyle(centeredStyleDate)
                Cell cell_53 = row.createCell(53);
                cell_53.setCellStyle(centeredStyle)
                Cell cell_54 = row.createCell(54);
                cell_54.setCellStyle(numberCenteredStyle)
                Cell cell_55 = row.createCell(55);
                cell_55.setCellStyle(centeredStyleDate)
                Cell cell_56 = row.createCell(56);
                cell_56.setCellStyle(numberCenteredStyle)
                Cell cell_57 = row.createCell(57);
                cell_57.setCellStyle(centeredStyleDate)
                Cell cell_58 = row.createCell(58);
                cell_58.setCellStyle(numberCenteredStyle)
                if (i < np.deductions.size()) {
                    NdflPersonDeduction npd = np.deductions.get(i)
                    cell0Value.append(npd.id.toString())
                    cell_45.setCellValue(npd.getTypeCode() != null ? npd.getTypeCode() : "");
                    cell_46.setCellValue(npd.getNotifType() != null ? npd.getNotifType() : "");
                    if (npd.notifDate != null) {
                        cell_47.setCellValue(npd.notifDate);
                    }
                    cell_48.setCellValue(npd.getNotifNum() != null ? npd.getNotifNum() : "");
                    cell_49.setCellValue(npd.getNotifSource() != null ? npd.getNotifSource() : "");
                    if (npd.notifSumm != null) {
                        cell_50.setCellValue(npd.notifSumm.doubleValue());
                    }
                    cell_51.setCellValue(npd.getOperationId() != null ? npd.getOperationId() : "");
                    if (npd.incomeAccrued != null) {
                        cell_52.setCellValue(npd.incomeAccrued);
                    }
                    cell_53.setCellValue(npd.getIncomeCode() != null ? npd.getIncomeCode() : "");
                    if (npd.incomeSumm != null) {
                        cell_54.setCellValue(npd.incomeSumm.doubleValue());
                    }
                    if (npd.periodPrevDate != null) {
                        cell_55.setCellValue(npd.periodPrevDate);
                    }
                    if (npd.periodPrevSumm != null) {
                        cell_56.setCellValue(npd.periodPrevSumm.doubleValue());
                    }
                    if (npd.periodCurrDate != null) {
                        cell_57.setCellValue(npd.periodCurrDate);
                    }
                    if (npd.periodCurrSumm != null) {
                        cell_58.setCellValue(npd.periodCurrSumm.doubleValue());
                    }
                }
                cell0Value.append("_")
                // Заполнение данными из раздела "Сведения об авансах"
                Cell cell_59 = row.createCell(59);
                cell_59.setCellStyle(textCenteredStyle)
                Cell cell_60 = row.createCell(60);
                cell_60.setCellStyle(numberCenteredStyle)
                Cell cell_61 = row.createCell(61);
                cell_61.setCellStyle(centeredStyle)
                Cell cell_62 = row.createCell(62);
                cell_62.setCellStyle(centeredStyleDate)
                Cell cell_63 = row.createCell(63);
                cell_63.setCellStyle(centeredStyle)
                if (i < np.prepayments.size()) {
                    NdflPersonPrepayment npp = np.prepayments.get(i)
                    cell0Value.append(npp.id.toString())
                    cell_59.setCellValue(npp.getOperationId() != null ? npp.getOperationId() : "");
                    if (npp.summ != null) {
                        cell_60.setCellValue(npp.summ.doubleValue());
                    }
                    cell_61.setCellValue(npp.getNotifNum() != null ? npp.getNotifNum() : "");
                    if (npp.notifDate != null) {
                        cell_62.setCellValue(npp.notifDate);
                    }
                    cell_63.setCellValue(npp.getNotifSource() != null ? npp.getNotifSource() : "");
                }
                cell_0.setCellValue(cell0Value.toString())
                pointer++
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

