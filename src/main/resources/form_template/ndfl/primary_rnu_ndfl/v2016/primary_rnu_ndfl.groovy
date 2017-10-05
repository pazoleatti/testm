package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.service.script.*
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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

new PrimaryRnuNdfl(this).run();

@TypeChecked
class PrimaryRnuNdfl extends AbstractScriptClass {

    DeclarationService declarationService
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
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
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

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
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

        //Идентификатор подразделения по которому формируется консолидированная форма
        def parentDepartmentId = declarationData.departmentId
        Department department = departmentService.get(parentDepartmentId)
        List<DeclarationData> declarationDataList = declarationService.findAllDeclarationData(CONSOLIDATED_RNU_NDFL_TEMPLATE_ID, department.id, declarationDataReportPeriod.id);
        for (DeclarationData declarationDataDestination : declarationDataList) {
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
        if ('rnu_ndfl_person_db' != reportAlias) {
            throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
        }
        PrepareSpecificReportResult result = new PrepareSpecificReportResult();
        List<Column> tableColumns = createTableColumns();
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
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
                if (!(key in ["fromBirthDay", "toBirthDay"])) {
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
                it.collect { Map<String, Object> param ->
                    (param.value != null ? (((param.value instanceof Date) ? ((Date) param.value).format('dd.MM.yyyy') : (String) param.value) + ";") : "")
                } join " "
            }
            logger.warn("Физическое лицо: " + subreportParamsToString(reportParameters) + " не найдено в форме");
            //throw new ServiceException("Физическое лицо: " + subreportParamsToString(reportParameters)+ " не найдено в форме");
        }

        pagingResult.getRecords().each() { ndflPerson ->
            DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(rowColumns, null));
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
        switch (scriptSpecificReportHolder?.declarationSubreport?.alias) {
            case 'rnu_ndfl_person_db':
                createSpecificReportPersonDb();
                break;
            case 'rnu_ndfl_person_all_db':
                createSpecificReportDb();
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
            logger.error("В ТФ неверно указана «Отчетная дата»: «${reportDate}». Должна быть указана дата окончания периода ТФ, равная «${reportPeriodEndDate}»")
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


    NdflPerson transformNdflPersonNode(NodeChild node) {
        NdflPerson ndflPerson = new NdflPerson()
        ndflPerson.inp = toString((GPathResult) node.getProperty( '@ИНП'))
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

        if (operationNotRelateToCurrentPeriod(incomeAccruedDate, incomePayoutDate, taxDate,
                kpp, oktmo, inp, fio, personIncome)) {
            return null
        }

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
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_INCOME_CODE, personIncome.incomeCode ?: "",
                    R_INCOME_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, personIncome.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError,
                    errMsg)
        }

        return personIncome
    }

    // Проверка на принадлежность операций периоду при загрузке ТФ
    boolean operationNotRelateToCurrentPeriod(LocalDateTime incomeAccruedDate, LocalDateTime incomePayoutDate, LocalDateTime taxDate,
                                              String kpp, String oktmo, String inp, String fio, NdflPersonIncome ndflPersonIncome) {
        // Доход.Дата.Начисление
        boolean incomeAccruedDateOk = dateRelateToCurrentPeriod(C_INCOME_ACCRUED_DATE, incomeAccruedDate, kpp, oktmo, inp, fio, ndflPersonIncome)
        // Доход.Дата.Выплата
        boolean incomePayoutDateOk = dateRelateToCurrentPeriod(C_INCOME_PAYOUT_DATE, incomePayoutDate, kpp, oktmo, inp, fio, ndflPersonIncome)
        // НДФЛ.Расчет.Дата
        boolean taxDateOk = dateRelateToCurrentPeriod(C_TAX_DATE, taxDate, kpp, oktmo, inp, fio, ndflPersonIncome)
        if (incomeAccruedDateOk && incomePayoutDateOk && taxDateOk) {
            return false
        }
        return true
    }

    boolean dateRelateToCurrentPeriod(String paramName, LocalDateTime date, String kpp, String oktmo, String inp, String fio, NdflPersonIncome ndflPersonIncome) {
        //https://jira.aplana.com/browse/SBRFNDFL-581 замена getReportPeriodCalendarStartDate() на getReportPeriodStartDate
        if (date == null || (date.toDate() >= getReportPeriodStartDate() && date.toDate() <= getReportPeriodEndDate())) {
            return true
        }
        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, (ndflPersonIncome.rowNum ? ndflPersonIncome.rowNum.longValue() : ""))
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)
        String errMsg = String.format("Значение гр. %s (\"%s\") не входит в отчетный период налоговой формы (%s), операция %s не загружена в налоговую форму. ФЛ %s, ИНП: %s",
                paramName, ScriptUtils.formatDate(date),
                departmentReportPeriod.reportPeriod.taxPeriod.year + ", " + departmentReportPeriod.reportPeriod.name,
                ndflPersonIncome.operationId,
                fio, inp
        )
        logger.warnExp("%s. %s.", "Проверка соответствия дат операций РНУ НДФЛ отчетному периоду", "", pathError,
                errMsg)
        return false
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
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_TYPE_CODE, personDeduction.typeCode ?: "",
                    R_TYPE_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, personDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInp, pathError,
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

    Integer toInteger(GPathResult  xmlNode) {
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

    Long toLong(GPathResult  xmlNode) {
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

    BigDecimal toBigDecimal(GPathResult  xmlNode) throws NumberFormatException {
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

    LocalDateTime toDate(GPathResult  xmlNode) {
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

    String toString(GPathResult  xmlNode) {
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

    def sourceReportPeriod = null

    Map<Integer, DepartmentReportPeriod> departmentReportPeriodMap = [:]

    Map<Integer, DeclarationTemplate> declarationTemplateMap = [:]

    Map<Integer, String> departmentFullNameMap = [:]

    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"

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

    def getReportPeriod() {
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