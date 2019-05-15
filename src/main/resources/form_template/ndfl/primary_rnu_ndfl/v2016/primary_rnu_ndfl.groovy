package form_template.ndfl.primary_rnu_ndfl.v2016

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
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
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

        formCreationDate = declarationData.createdDate

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
            return (BigDecimal) null
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
