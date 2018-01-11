package form_template.ndfl.primary_rnu_ndfl.v2016

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.export.JRXlsExporterParameter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

import javax.xml.namespace.QName
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.*
import java.text.SimpleDateFormat
import com.aplana.sbrf.taxaccounting.script.service.*

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
                // Формирование pdf-отчета формы
                declarationService.createPdfReport(logger, declarationData, userInfo)
                break
            case FormDataEvent.PREPARE_SPECIFIC_REPORT:
                // Подготовка для последующего формирования спецотчета
                prepareSpecificReport()
                break
            case FormDataEvent.GET_SOURCES: //формирование списка приемников
                getSourcesListForTemporarySolution()
                break
            case FormDataEvent.CREATE_EXCEL_REPORT: //создание xlsx отчета
                createXlsxReport()
                break
            case FormDataEvent.CREATE_SPECIFIC_REPORT:
                // Формирование спецотчета
                createSpecificReport()
                break
        }
    }


    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"
    final String DATE_FORMAT = "dd.MM.yyyy"

    /**
     * Идентификатор шаблона РНУ-НДФЛ (консолидированная)
     */
    final int CONSOLIDATED_RNU_NDFL_TEMPLATE_ID = 101
    final int PRIMARY_RNU_NDFL_TEMPLATE_ID = 100

    final String TEMPLATE_PERSON_FL_OPER = "%s, ИНП: %s, ID операции: %s"
    final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует справочнику \"%s\""

    /**
     * Получить дату которая используется в качестве версии записей справочника
     * @return дата используемая в качестве даты версии справочника
     */
    def getVersionFrom() {
        return getReportPeriodStartDate();
    }

    //------------------ GET_SOURCES ----------------------

    List<Relation> getDestinationInfo(boolean isLight) {

        List<Relation> destinationInfo = new ArrayList<Relation>();

        //отчетный период в котором выполняется консолидация
        ReportPeriod declarationDataReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)

        //Получаем список всех родительских подразделений и ищем для них консолидированные формы в нужном периоде
        List<Integer> parentDepartments = departmentService.fetchAllParentDepartmentsIds(declarationData.departmentId)
        List<DeclarationData> declarationDataList = declarationService.fetchAllDeclarationData(CONSOLIDATED_RNU_NDFL_TEMPLATE_ID, parentDepartments, declarationDataReportPeriod.id);
        for (DeclarationData declarationDataDestination : declarationDataList) {
            Department department = departmentService.get(declarationDataDestination.departmentId)
            if (departmentReportPeriod.correctionDate != null) {
                DepartmentReportPeriod departmentReportPeriodDestination = getDepartmentReportPeriodById(declarationDataDestination.departmentReportPeriodId)
                if (departmentReportPeriodDestination.correctionDate == null || departmentReportPeriod.correctionDate > departmentReportPeriodDestination.correctionDate) {
                    continue
                }
            }
            //Формируем связь источник-приемник
            Relation relation = getRelation(declarationDataDestination, department, declarationDataReportPeriod, isLight)
            destinationInfo.add(relation)
        }

        return destinationInfo;
    }


    def getSourcesListForTemporarySolution() {
        if (needSources) {
            return
        }

        for (Relation relation : getDestinationInfo(light)) {
            sources.sourceList.add(relation)
        }
        sources.sourcesProcessedByScript = true
    }

    /**
     * Получить запись для источника-приемника.
     *
     * @param declarationData первичная форма
     * @param department подразделение
     * @param period период нф
     * @param monthOrder номер месяца (для ежемесячной формы)
     */
    Relation getRelation(DeclarationData declarationData, Department department, ReportPeriod period, boolean isLight) {

        Relation relation = new Relation()

        //Привязка отчетных периодов к подразделениям
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData?.departmentReportPeriodId) as DepartmentReportPeriod

        //Макет НФ
        DeclarationTemplate declarationTemplate = getDeclarationTemplateById(declarationData?.declarationTemplateId)

        def isSource = (declarationTemplate.id == PRIMARY_RNU_NDFL_TEMPLATE_ID)
        ReportPeriod rp = departmentReportPeriod.getReportPeriod();

        if (isLight) {
            //Идентификатор подразделения
            relation.departmentId = department.id
            //полное название подразделения
            relation.fullDepartmentName = getDepartmentFullName(department.id)
            //Дата корректировки
            relation.correctionDate = departmentReportPeriod?.correctionDate
            //Вид нф
            relation.declarationTypeName = declarationTemplate?.name
            //Год налогового периода
            relation.year = period.taxPeriod.year
            //Название периода
            relation.periodName = period.name
        }

        //Общие параметры

        //подразделение
        relation.department = department
        //Период
        relation.departmentReportPeriod = departmentReportPeriod
        //Статус ЖЦ
        relation.declarationState = declarationData?.state
        //форма/декларация создана/не создана
        relation.created = (declarationData != null)
        //является ли форма источников, в противном случае приемник
        relation.source = isSource;
        // Введена/выведена в/из действие(-ия)
        relation.status = declarationTemplate.status == VersionedObjectStatus.NORMAL
        // Налог
        relation.taxType = TaxType.NDFL

        //Параметры НФ

        // Идентификатор созданной формы
        relation.declarationDataId = declarationData?.id
        // Вид НФ
        relation.declarationTemplate = declarationTemplate
        return relation

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
            DataRow<com.aplana.sbrf.taxaccounting.model.Cell> row = new DataRow<>(FormDataUtils.createCells(rowColumns, null));
            row.getCell("id").setStringValue(ndflPerson.id.toString())
            row.lastName = ndflPerson.lastName
            row.firstName = ndflPerson.firstName
            row.middleName = ndflPerson.middleName
            row.snils = ndflPerson.snils
            row.innNp = ndflPerson.innNp
            row.inp = ndflPerson.inp
            row.birthDay = new Date((Long) ndflPerson.birthDay.getLocalMillis())
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
        def params = new HashMap<String, Object>()
        params.put("declarationId", declarationData.getId());

        JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, declarationService.getXmlStream(declarationData.id));

        StringBuilder fileName = new StringBuilder("Реестр_загруженных_данных_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
        exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
        scriptSpecificReportHolder.setFileName(fileName.toString())
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
    /**
     * Спец. отчет "РНУ НДФЛ по физическому лицу". Данные макет извлекает непосредственно из бд
     */
    def createSpecificReportPersonDb() {
        DataRow<com.aplana.sbrf.taxaccounting.model.Cell> row = scriptSpecificReportHolder.getSelectedRecord()
        NdflPerson ndflPerson = null
        if (row != null) {
            ndflPerson = ndflPersonService.get(Long.valueOf(row.id))

            Map<String, String> subReportViewParams = scriptSpecificReportHolder.getViewParamValues()
            subReportViewParams.put('Фамилия', (String) row.lastName)
            subReportViewParams.put('Имя', (String) row.firstName)
            subReportViewParams.put('Отчество', (String) row.middleName)
            subReportViewParams.put('Дата рождения', row.birthDay ? ((Date) row.birthDay)?.format(DATE_FORMAT) : "")
            subReportViewParams.put('№ ДУЛ', (String) row.idDocNumber)

        } else {
            ndflPerson = ndflPersonService.get((Long) scriptSpecificReportHolder.subreportParamValues.get("PERSON_ID"));
        }
        if (ndflPerson != null) {
            Map<String, Object> params = [NDFL_PERSON_ID: (Object) ndflPerson.id];

            JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params);
            exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
            scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")
        } else {
            throw new ServiceException("Не найдены данные для формирования отчета!");
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

            Workbook xssfWorkbook = getSpecialReportTemplate()

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

/**
 * Выгрузка в Excel РНУ-НДФЛ
 */
    public void loadAllDeclarationDataToExcel() {

        ScriptUtils.checkInterrupted();
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

        Workbook xssfWorkbook = getSpecialReportTemplate()

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
    XSSFWorkbook getSpecialReportTemplate() {
        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        String blobDataId = null;
        for (DeclarationTemplateFile declarationTemplateFile : declarationTemplate.declarationTemplateFiles) {
            if (declarationTemplateFile.fileName.equals("rnu_ndfl_person_all_db.xlsx")) {
                blobDataId = declarationTemplateFile.blobDataId
                break
            }
        }
        BlobData blobData = blobDataService.get(blobDataId)
        return new XSSFWorkbook(blobData.getInputStream())
    }

    //------------------ Import Data ----------------------
    @TypeChecked(TypeCheckingMode.SKIP)
    void importData() {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        logForDebug("Начало загрузки данных первичной налоговой формы " + declarationData.id + ". Дата начала отчетного периода: " + sdf.format(getReportPeriodStartDate()) + ", дата окончания: " + sdf.format(getReportPeriodEndDate()));

        //валидация по схеме
        declarationService.validateDeclaration(declarationData, userInfo, logger, dataFile, UploadFileName.substring(0, UploadFileName.lastIndexOf('.')))
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException("ТФ не соответствует XSD-схеме. Загрузка невозможна.");
        }

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

        def reportPeriodEndDate = getReportPeriodEndDate().format(DATE_FORMAT)

        def Файл = new XmlSlurper().parse(dFile)
        String reportDate = Файл.СлЧасть.'@ДатаОтч'

        if (reportPeriodEndDate != reportDate) {
            logger.warn("В ТФ неверно указана «Отчетная дата»: «${reportDate}». Должна быть указана дата окончания периода ТФ, равная «${reportPeriodEndDate}»")
        }

        //Каждый элемент ИнфЧасть содержит данные об одном физ лице, максимальное число элементов в документе 15000
        QName infoPartName = QName.valueOf('ИнфЧасть')

        //Используем StAX парсер для импорта
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        XMLEventReader reader = xmlFactory.createXMLEventReader(xmlInputStream)

        def ndflPersonNum = 1;
        def success = 0
        def sb;
        try {
            while (reader.hasNext()) {
                ScriptUtils.checkInterrupted()
                XMLEvent event = reader.nextEvent()

                if (event.isCharacters() && ((Characters) event).isWhiteSpace()) {
                    continue;
                }

                if (!event.isStartElement() && !event.isEndElement()) {
                    continue;
                }

                //Последовательно обрабатываем все элементы ИнфЧасть в документе
                if (event.isStartElement() && event.getName().equals(infoPartName)) {
                    sb = new StringBuilder()
                }

                if (event.isStartElement()) {
                    sb?.append(processStartElement(event.asStartElement()))
                }

                if (event.isEndElement()) {
                    sb?.append(processEndElement(event.asEndElement()))
                }

                if (event.isEndElement() && event.getName().equals(infoPartName)) {
                    String personData = sb.toString();
                    if (personData != null && !personData.isEmpty()) {
                        def infoPart = new XmlSlurper().parseText(sb.toString())
                        if (processInfoPart(infoPart, ndflPersonNum)) {
                            success++
                        }
                        ndflPersonNum++
                    }
                }
            }
        } finally {
            reader?.close()
        }
        if (success == 0) {
            logger.error("В ТФ отсутствуют операции, принадлежащие отчетному периоду. Налоговая форма не создана")
        }
    }

    String processStartElement(StartElement start) {
        String var1 = "<" + start.getName().getLocalPart();
        Iterator var2;
        Attribute var3;
        if (start.getAttributes() != null) {
            var2 = start.getAttributes();
            for (var3 = null; var2.hasNext(); var1 = var1 + " " + processAttr(var3)) {
                //println processAttr(var3)
                var3 = (Attribute) var2.next();
            }
        }
        var1 = var1 + ">";
        return var1;
    }

    String processAttr(Attribute attr) {
        if (attr != null) {
            return attr.getName().getLocalPart() + "=\'" + attr.getValue() + "\'"
        } else {
            return "";
        }
    }

    String processEndElement(EndElement end) {
        StringBuffer var1 = new StringBuffer();
        var1.append("</").append(end.getName().getLocalPart()).append(">");
        return var1.toString();
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
            ndflPersonService.save(ndflPerson)
        } else {
            logger.warn("ФЛ ФИО = $fio ФЛ ИНП = ${ndflPerson.inp} Не загружен в систему поскольку не имеет операций в отчетном периоде")
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
        LocalDateTime incomeAccruedDate = null
        def incomeAccruedRowNum = null;
        LocalDateTime incomePayoutDate = null
        def incomePayoutRowNum = null;
        def allRowNums = new ArrayList<String>();


        ndflPersonOperationsNode.childNodes().each { node ->
            def rowNum = node.attributes['НомСтр']
            allRowNums.add(rowNum)
            if (node.attributes.containsKey('ДатаДохНач') && node.attributes['ДатаДохНач'] != null) {
                incomeAccruedDate = LocalDateTime.parse(node.attributes['ДатаДохНач'], DateTimeFormat.forPattern(DATE_FORMAT));
                incomeAccruedRowNum = rowNum
            }
            if (node.attributes.containsKey('ДатаДохВыпл') && node.attributes['ДатаДохВыпл'] != null) {
                incomePayoutDate = LocalDateTime.parse(node.attributes['ДатаДохВыпл'], DateTimeFormat.forPattern(DATE_FORMAT));
                incomePayoutRowNum = rowNum
            }
        }

        // Доход.Дата.Начисление
        boolean incomeAccruedDateOk = dateRelateToCurrentPeriod(incomeAccruedDate)
        // Доход.Дата.Выплата
        boolean incomePayoutDateOk = dateRelateToCurrentPeriod(incomePayoutDate)
        if (incomeAccruedDateOk || incomePayoutDateOk) {
            return true
        } else {
            def rowNums = new ArrayList<String>();
            if (!incomeAccruedDateOk && incomeAccruedRowNum != null) {
                // Дата.Начисление не попала в период
                rowNums.add(incomeAccruedRowNum)
            }
            if (!incomePayoutDateOk && incomePayoutRowNum != null) {
                // Дата.Выплата не попала в период
                rowNums.add(incomePayoutRowNum)
            }
            if (incomeAccruedRowNum == null && incomePayoutRowNum == null) {
                // Обе даты пустые - выводим сообщение по каждой строке операции (т.к не знаем где должна была быть каждая дата)
                rowNums = allRowNums
            }
            for (String rowNum : rowNums) {
                String pathError = String.format(SECTION_LINE_RANGE_MSG, T_PERSON_INCOME, rowNum)
                logPeriodError(pathError, incomeAccruedDate, incomePayoutDate, inp, fio, operationId)
            }
            return false
        }
    }

    void logPeriodError(String pathError, LocalDateTime incomeAccruedDate, LocalDateTime incomePayoutDate, String inp, String fio, String operationId) {
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)

        String errMsg = String.format("Значения гр. %s (\"%s\") и гр. %s (\"%s\") не входят в отчетный период налоговой формы (%s), операция %s не загружена в налоговую форму. ФЛ %s, ИНП: %s",
                C_INCOME_ACCRUED_DATE,
                incomeAccruedDate != null ? ScriptUtils.formatDate(incomeAccruedDate) : "Не определено",
                C_INCOME_PAYOUT_DATE,
                incomePayoutDate != null ? ScriptUtils.formatDate(incomePayoutDate) : "Не определено",
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName(),
                operationId,
                fio, inp
        )
        logger.warnExp("%s. %s.", "Проверка соответствия дат операций РНУ НДФЛ отчетному периоду", "", pathError,
                errMsg)
    }

    // Проверка принадлежности даты к периоду формы
    boolean dateRelateToCurrentPeriod(LocalDateTime date) {
        //https://jira.aplana.com/browse/SBRFNDFL-581 замена getReportPeriodCalendarStartDate() на getReportPeriodStartDate
        if (date == null || (date.toDate() >= getReportPeriodStartDate() && date.toDate() <= getReportPeriodEndDate())) {
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
        return ndflPerson
    }

    NdflPersonIncome transformNdflPersonIncome(NodeChild node, NdflPerson ndflPerson, String kpp, String oktmo, String inp, String fio,
                                               Map<Long, Map<String, RefBookValue>> incomeCodeMap) {
        def operationNode = node.parent();

        LocalDateTime incomeAccruedDate = toDate((GPathResult) node.getProperty('@ДатаДохНач'))
        LocalDateTime incomePayoutDate = toDate((GPathResult) node.getProperty('@ДатаДохВыпл'))
        LocalDateTime taxDate = toDate((GPathResult) node.getProperty('@ДатаНалог'))

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
        personIncome.taxTransferDate = toDate((GPathResult) node.getProperty('@СрокПрчслНал'))
        personIncome.paymentDate = toDate((GPathResult) node.getProperty('@ПлПоручДат'))
        personIncome.paymentNumber = toString((GPathResult) node.getProperty('@ПлатПоручНом'))
        personIncome.taxSumm = toLong((GPathResult) node.getProperty('@НалПерСумм'))

        // Спр5 Код вида дохода (Необязательное поле)
        if (personIncome.incomeCode != null && personIncome.incomeAccruedDate != null && !incomeCodeMap.find { key, value ->
            value.CODE?.stringValue == personIncome.incomeCode &&
                    personIncome.incomeAccruedDate >= new LocalDateTime(value.record_version_from?.dateValue) &&
                    personIncome.incomeAccruedDate <= new LocalDateTime(value.record_version_to?.dateValue)
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

    LocalDateTime toDate(GPathResult xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            if (xmlNode.text() != null && !xmlNode.text().isEmpty()) {
                LocalDateTime date = LocalDateTime.parse(xmlNode.text(), DateTimeFormat.forPattern(DATE_FORMAT));
                if (date.toString(DATE_FORMAT) != xmlNode.text()) {
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
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).toString(DATE_FORMAT)
        } else {
            return ScriptUtils.formatDate((Date) date, DATE_FORMAT)
        }
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
            def declarationList = declarationService.find(102, prevDepartmentReportPeriod.getId())
            declarationList.addAll(declarationService.find(103, prevDepartmentReportPeriod.getId()))
            declarationList.addAll(declarationService.find(104, prevDepartmentReportPeriod.getId()))
            if (declarationList.isEmpty()) {
                logger.warn("Отсутствуют отчетные налоговые формы в некорректировочном периоде. Отчетные налоговые формы не будут сформированы текущем периоде")
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
                return new TitleSheetFiller();
            case 1:
                return new RequisitesSheetFiller();
            case 2:
                return new IncomesSheetFiller();
            case 3:
                return new DeductionsSheetFiller();
            case 4:
                return new PrepaymentSheetFiller();
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
        Sheet sheet = wb.getSheetAt(1);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle centeredStyle = styler.getBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.getBorderStyleCenterAlignedDate()
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
                cell6.setCellValue(np.birthDay.toDate());
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
            cell13.setCellStyle(centeredStyle)
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
 * Заполнитель сведений о доходах
 */
@TypeChecked
class IncomesSheetFiller implements SheetFiller {
    @Override
    void fillSheet(Workbook wb, SheetFillerContext context) {
        List<NdflPersonIncome> ndflPersonIncomeList = context.getNdflPersonIncomeList();
        Sheet sheet = wb.getSheetAt(2);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.getBorderStyle()
        CellStyle centeredStyle = styler.getBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.getBorderStyleCenterAlignedDate()
        for (NdflPersonIncome npi : ndflPersonIncomeList) {
            ScriptUtils.checkInterrupted();

            Row row = sheet.createRow(index);
            Cell cell1 = row.createCell(1);
            cell1.setCellStyle(centeredStyle)
            cell1.setCellValue(npi.getRowNum().intValue());
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
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(npi.getIncomeType() != null ? npi.getIncomeType() : "");
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(centeredStyleDate)
            if (npi.incomeAccruedDate != null) {
                cell6.setCellValue(npi.incomeAccruedDate.toDate());
            }
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(centeredStyleDate)
            if (npi.incomePayoutDate != null) {
                cell7.setCellValue(npi.incomePayoutDate.toDate());
            }
            Cell cell8 = row.createCell(8);
            cell8.setCellStyle(centeredStyle)
            cell8.setCellValue(npi.getKpp() != null ? npi.getKpp() : "");
            Cell cell9 = row.createCell(9);
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(npi.getOktmo() != null ? npi.getOktmo() : "");
            Cell cell10 = row.createCell(10);
            cell10.setCellStyle(borderStyle)
            if (npi.incomeAccruedSumm != null) {
                cell10.setCellValue(npi.incomeAccruedSumm.doubleValue());
            }
            Cell cell11 = row.createCell(11);
            cell11.setCellStyle(borderStyle)
            if (npi.incomePayoutSumm != null) {
                cell11.setCellValue(npi.incomePayoutSumm.doubleValue());
            }
            Cell cell12 = row.createCell(12);
            cell12.setCellStyle(borderStyle)
            if (npi.totalDeductionsSumm != null) {
                cell12.setCellValue(npi.totalDeductionsSumm.doubleValue());
            }
            Cell cell13 = row.createCell(13);
            cell13.setCellStyle(borderStyle)
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
                cell15.setCellValue(npi.taxDate.toDate());
            }

            Cell cell16 = row.createCell(16);
            cell16.setCellStyle(borderStyle)
            if (npi.calculatedTax != null) {
                cell16.setCellValue(npi.calculatedTax.doubleValue())
            }
            Cell cell17 = row.createCell(17);
            cell17.setCellStyle(borderStyle)
            if (npi.withholdingTax != null) {
                cell17.setCellValue(npi.withholdingTax.doubleValue());
            }
            Cell cell18 = row.createCell(18);
            cell18.setCellStyle(borderStyle)
            if (npi.notHoldingTax != null) {
                cell18.setCellValue(npi.notHoldingTax.doubleValue());
            }
            Cell cell19 = row.createCell(19);
            cell19.setCellStyle(borderStyle)
            if (npi.overholdingTax != null) {
                cell19.setCellValue(npi.overholdingTax.doubleValue());
            }
            Cell cell20 = row.createCell(20);
            cell20.setCellStyle(borderStyle)
            if (npi.refoundTax != null) {
                cell20.setCellValue(npi.refoundTax.doubleValue());
            }
            Cell cell21 = row.createCell(21);
            cell21.setCellStyle(centeredStyleDate)
            if (npi.taxTransferDate != null) {
                cell21.setCellValue(npi.taxTransferDate.toDate());
            }
            Cell cell22 = row.createCell(22);
            cell22.setCellStyle(centeredStyleDate)
            if (npi.paymentDate != null) {
                cell22.setCellValue(npi.paymentDate.toDate());
            }
            Cell cell23 = row.createCell(23);
            cell23.setCellStyle(centeredStyle)
            cell23.setCellValue(npi.getPaymentNumber() != null ? npi.getPaymentNumber() : "");
            Cell cell24 = row.createCell(24);
            cell24.setCellStyle(borderStyle)
            if (npi.taxSumm != null) {
                cell24.setCellValue(npi.taxSumm.intValue());
            }
            index++;
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
        List<NdflPersonDeduction> ndflPersonDeductionList = context.getNdflPersonDeductionList();
        Sheet sheet = wb.getSheetAt(3);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.getBorderStyle()
        CellStyle centeredStyle = styler.getBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.getBorderStyleCenterAlignedDate()
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
                cell5.setCellValue(npd.notifDate.toDate());
            }
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(centeredStyle)
            cell6.setCellValue(npd.getNotifNum() != null ? npd.getNotifNum() : "б/н");
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(npd.getNotifSource() != null ? npd.getNotifSource() : "");
            Cell cell8 = row.createCell(8);
            cell8.setCellStyle(borderStyle)
            if (npd.notifSumm != null) {
                cell8.setCellValue(npd.notifSumm.doubleValue());
            }
            Cell cell9 = row.createCell(9);
            cell9.setCellStyle(centeredStyle)
            cell9.setCellValue(npd.getOperationId() != null ? npd.getOperationId() : "");
            Cell cell10 = row.createCell(10);
            cell10.setCellStyle(centeredStyleDate)
            if (npd.incomeAccrued != null) {
                cell10.setCellValue(npd.incomeAccrued.toDate());
            }
            Cell cell11 = row.createCell(11);
            cell11.setCellStyle(centeredStyle)
            cell11.setCellValue(npd.getIncomeCode() != null ? npd.getIncomeCode() : "");
            Cell cell12 = row.createCell(12);
            cell12.setCellStyle(borderStyle)
            if (npd.incomeSumm != null) {
                cell12.setCellValue(npd.incomeSumm.doubleValue());
            }
            Cell cell13 = row.createCell(13);
            cell13.setCellStyle(centeredStyleDate)
            if (npd.periodPrevDate != null) {
                cell13.setCellValue(npd.periodPrevDate.toDate());
            }
            Cell cell14 = row.createCell(14);
            cell14.setCellStyle(borderStyle)
            if (npd.periodPrevSumm != null) {
                cell14.setCellValue(npd.periodPrevSumm.doubleValue());
            }
            Cell cell15 = row.createCell(15);
            cell15.setCellStyle(centeredStyleDate)
            if (npd.periodCurrDate != null) {
                cell15.setCellValue(npd.periodCurrDate.toDate());
            }
            Cell cell16 = row.createCell(16);
            cell16.setCellStyle(borderStyle)
            if (npd.periodCurrSumm != null) {
                cell16.setCellValue(npd.periodCurrSumm.doubleValue());
            }
            index++;
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
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = context.getNdflPersonPrepaymentList();
        Sheet sheet = wb.getSheetAt(4);
        int index = 3;
        Styler styler = new Styler(wb)
        CellStyle borderStyle = styler.getBorderStyle()
        CellStyle centeredStyle = styler.getBorderStyleCenterAligned()
        CellStyle centeredStyleDate = styler.getBorderStyleCenterAlignedDate()
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
            cell4.setCellStyle(borderStyle)
            if (npp.summ != null) {
                cell4.setCellValue(npp.summ.doubleValue());
            }
            Cell cell5 = row.createCell(5);
            cell5.setCellStyle(centeredStyle)
            cell5.setCellValue(npp.getNotifNum() != null ? npp.getNotifNum() : "");
            Cell cell6 = row.createCell(6);
            cell6.setCellStyle(centeredStyleDate)
            if (npp.notifDate != null) {
                cell6.setCellValue(npp.notifDate.toDate());
            }
            Cell cell7 = row.createCell(7);
            cell7.setCellStyle(centeredStyle)
            cell7.setCellValue(npp.getNotifSource() != null ? npp.getNotifSource() : "");
            index++;
        }
    }
}

class Styler {

    Workbook workbook

    Styler(Workbook workbook) {
        this.workbook = workbook
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру
     * @return
     */
    CellStyle getBorderStyleCenterAligned() {
        CellStyle style = workbook.createCellStyle()
        style.setAlignment(CellStyle.ALIGN_CENTER)
        thinBorderStyle(style)
        return style
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами
     * @return
     */
    CellStyle getBorderStyle() {
        CellStyle style = workbook.createCellStyle()
        thinBorderStyle(style)
        return style
    }

    /**
     * Создать стиль ячейки с нормальным шрифтом с тонкими границами и выравниваем по центру для дат
     * @return
     */
    CellStyle getBorderStyleCenterAlignedDate() {
        CellStyle style = getBorderStyleCenterAligned()
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
    CellStyle thinBorderStyle(CellStyle style) {
        style.setBorderTop(CellStyle.BORDER_THIN)
        style.setBorderBottom(CellStyle.BORDER_THIN)
        style.setBorderLeft(CellStyle.BORDER_THIN)
        style.setBorderRight(CellStyle.BORDER_THIN)
        return style
    }
}

