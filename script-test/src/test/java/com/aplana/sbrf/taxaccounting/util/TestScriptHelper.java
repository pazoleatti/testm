package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.*;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Хэлпер для работы со скриптами НФ в тестовом режиме
 *
 * @author Levykin
 */
public class TestScriptHelper {
    // Id текущего для теста экземпляра НФ
    public final static long CURRENT_FORM_DATA_ID = 1;
    public final static long DEPARTMENT_REGION_ID = 1;
    public final static String DEPARTMENT_NAME = "test department name";

    // Пкть к скрипту
    private String path;
    // Текст скрипта
    private String script;
    // Кодировка скриптов
    private final String charsetName = "UTF-8";
    // Кодировка XML
    private final static String XML_ENCODING = "UTF-8";
    // Префикс пути скрипта
    private final static String SCRIPT_PATH_PREFIX = "../src/main/resources";
    // Имя файла скрипта
    private final String SCRIPT_PATH_FILE_NAME = "script.groovy";
    // Имя файла шаблона
    private final static String CONTENT_FILE_NAME = "content.xml";
    // Имя файла с начальными строками
    private final static String ROWS_FILE_NAME = "rows.xml";
    // Имя файла с заголовками
    private final static String HEADERS_FILE_NAME = "headers.xml";
    // Сервис работы со скриптами
    private static ScriptingService scriptingService = new ScriptingService();
    // Mock-сервисы
    private FormDataService formDataService;
    private DepartmentFormTypeService departmentFormTypeService;
    private ReportPeriodService reportPeriodService;
    private DepartmentService departmentService;
    private BookerStatementService bookerStatementService;
    private RefBookService refBookService;
    private RefBookFactory refBookFactory;
    private RefBookDataProvider refBookDataProvider;
    private DepartmentReportPeriodService departmentReportPeriodService;
    private FormTypeService formTypeService;
    private DeclarationService declarationService;
    private TransactionHelper transactionHelper;

    // Сервисы "Персонифицированные сведения о застрахованных лицах"
    private RaschsvPersSvStrahLicService raschsvPersSvStrahLicService;
    // Сервисы "Сводные данные об обязательствах плательщика страховых взносов"
    private RaschsvObyazPlatSvService raschsvObyazPlatSvService;
    // Сервисы "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
    private RaschsvUplPerService raschsvUplPerService;
    // Сервисы "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    private RaschsvUplPrevOssService raschsvUplPrevOssService;
    // Сервисы "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
    private RaschsvSvOpsOmsService raschsvSvOpsOmsService;
    // Сервисы "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    private RaschsvOssVnmService raschsvOssVnmService;
    // Сервисы "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
    private RaschsvRashOssZakService raschsvRashOssZakService;
    // Сервисы "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
    private RaschsvVyplFinFbService raschsvVyplFinFbService;
    // Сервис "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427"
    private RaschsvPravTarif31427Service raschsvPravTarif31427Service;
    // Сервис "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427"
    private RaschsvPravTarif51427Service raschsvPravTarif51427Service;
    // Сервис "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427"
    private RaschsvPravTarif71427Service raschsvPravTarif71427Service;
    // Сервис "Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427"
    private RaschsvSvPrimTarif91427Service raschsvSvPrimTarif91427Service;

    //Сервисы НДФЛ
    private NdflPersonService ndflPersonService;


    private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();

    // Заданы константно
    private Logger logger = new Logger();
    private TAUser user = new TAUser();
    private Department userDepartment = new Department();
    // Задаются из конкретного теста
    private InputStream importFileInputStream;
    private FormData formData;
    private FormTemplate formTemplate;
    private DataRow<Cell> currentDataRow;
    private ScriptStatusHolder scriptStatusHolder;
    private final ScriptTestMockHelper mockHelper;

    private String importFileName = null;

    /**
     * Сервис работы со скриптами НФ в тестовом режиме
     *
     * @param path       Относительный путь к каталогу со скриптом
     * @param formData   Экземпляр НФ
     * @param mockHelper Хэлпер с заглушками других сервисов, можно переопределить
     */
    public TestScriptHelper(String path, FormData formData, ScriptTestMockHelper mockHelper) {
        super();
        this.formData = formData;
        this.mockHelper = mockHelper;
        // Id подразделения пользователя совпадает c Id подразделения НФ
        userDepartment.setId(formData.getDepartmentId());
        userDepartment.setRegionId(DEPARTMENT_REGION_ID);
        userDepartment.setName(DEPARTMENT_NAME);
        // Шаблон НФ из файла
        FormType formType = formData.getFormType();
        initFormTemplate();
        formData.setFormType(formType); // Сбрасывается в FormData#initFormTemplateParams
        this.path = SCRIPT_PATH_PREFIX + path + SCRIPT_PATH_FILE_NAME;
        try {
            script = readFile(this.path, charsetName);
        } catch (IOException e) {
            throw new ServiceException("Can't load script with path \"" + this.path + "\".", e);
        }
        // Моск сервисов
        initMock();
    }

    /**
     * Для переопределения метода
     */
    protected void initFormTemplate() {
        formData.initFormTemplateParams(getTemplate(SCRIPT_PATH_PREFIX + path, true));
    }

    /**
     * Моск сервисов
     */
    private void initMock() {
        formDataService = mockHelper.mockFormDataService();
        departmentFormTypeService = mockHelper.mockDepartmentFormTypeService();
        reportPeriodService = mockHelper.mockReportPeriodService();
        departmentService = mockHelper.mockDepartmentService();
        bookerStatementService = mockHelper.mockBookerStatementService();
        refBookService = mockHelper.mockRefBookService();
        refBookFactory = mockHelper.mockRefBookFactory();
        refBookDataProvider = mockHelper.getRefBookDataProvider();
        departmentReportPeriodService = mockHelper.mockDepartmentReportPeriodService();
        formTypeService = mockHelper.mockFormTypeService();
        declarationService = mockHelper.mockDeclarationService();
        transactionHelper = mockHelper.mockTransactionHelper();
        ndflPersonService = mockHelper.mockNdflPersonService();
        raschsvPersSvStrahLicService = mockHelper.mockRaschsvPersSvStrahLicService();
        raschsvObyazPlatSvService = mockHelper.mockRaschsvObyazPlatSvService();
        raschsvUplPerService = mockHelper.mockRaschsvUplPerService();
        raschsvUplPrevOssService = mockHelper.mockRaschsvUplPrevOssService();
        raschsvSvOpsOmsService = mockHelper.mockRaschsvSvOpsOmsService();
        raschsvOssVnmService = mockHelper.mockRaschsvOssVnmService();
        raschsvRashOssZakService = mockHelper.mockRaschsvRashOssZakService();
        raschsvVyplFinFbService = mockHelper.mockRaschsvVyplFinFbService();
        raschsvPravTarif31427Service = mockHelper.mockRaschsvPravTarif31427Service();
        raschsvPravTarif51427Service = mockHelper.mockRaschsvPravTarif51427Service();
        raschsvPravTarif71427Service = mockHelper.mockRaschsvPravTarif71427Service();
        raschsvSvPrimTarif91427Service = mockHelper.mockRaschsvSvPrimTarif91427Service();
    }

    /**
     * Получение шаблона НФ из файлов content.xml, headers.xml, rows.xml.
     * Затратная по времени операция, выполняется один раз для одного скрипта.
     * Для получения данных любого макета (что бы можно было формировать строки других форм)
     *
     * @param path путь к каталогу макета
     */
    public FormTemplate getTemplate(String path) {
        return getTemplate(path, false);
    }

    /**
     * Получение шаблона НФ из файлов content.xml, headers.xml, rows.xml
     * Затратная по времени операция, выполняется один раз для одного скрипта.
     *
     * @param path         путь к каталогу макета
     * @param isUsedInside используется внутри тестового хеллпера (задавать ли значение для внутреннего шаблона или только вернуть загруженный)
     */
    private FormTemplate getTemplate(String path, boolean isUsedInside) {
        try {
            FormTemplate formTemplate = new FormTemplate();
            formTemplate.setId(formData.getFormTemplateId());
            // content.xml
            JAXBContext jaxbContext = JAXBContext.newInstance(FormTemplateContent.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FormTemplateContent formTemplateContent = (FormTemplateContent) unmarshaller.unmarshal(
                    new InputStreamReader(new FileInputStream(path + CONTENT_FILE_NAME), XML_ENCODING));
            formTemplateContent.fillFormTemplate(formTemplate);
            // для поправки столбцов с длинными названиями, которые имеют символ переноса
            for (Column column : formTemplate.getColumns()) {
                String value = StringUtils.cleanString(column.getName());
                column.setName(value);
            }
            // rows.xml
            formTemplate.getRows().clear();
            String rowsString = readFile(path + ROWS_FILE_NAME, XML_ENCODING);
            if (rowsString != null && !rowsString.isEmpty()) {
                List<DataRow<Cell>> fixedRows = xmlSerializationUtils.deserialize(rowsString, formTemplate.getColumns(), formTemplate.getStyles(), Cell.class);
                FormDataUtils.setValueOwners(fixedRows);
                formTemplate.getRows().addAll(fixedRows);
            }
            // headers.xml
            formTemplate.getHeaders().clear();
            String headersString = readFile(path + HEADERS_FILE_NAME, XML_ENCODING);
            if (headersString != null && !headersString.isEmpty()) {
                List<DataRow<HeaderCell>> headerRows = xmlSerializationUtils.deserialize(headersString, formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class);
                FormDataUtils.setValueOwners(headerRows);
                formTemplate.getHeaders().addAll(headerRows);
            }
            if (isUsedInside) {
                this.formTemplate = formTemplate;
            }
            return formTemplate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("Get form template error for path \"" + path + "\".", e);
        }
    }

    public void setImportFileInputStream(InputStream importFileInputStream) {
        this.importFileInputStream = importFileInputStream;
    }

    /**
     * Указатель текущей строки (для FormDataEvent.ADD_ROW и FormDataEvent.DELETE_ROW)
     */
    public void setCurrentDataRow(DataRow<Cell> currentDataRow) {
        this.currentDataRow = currentDataRow;
    }

    /**
     * Инициализация строк НФ. Строки подтягиваются из макета.
     */
    public void initRowData() {
        // Строки из шаблона
        mockHelper.getDataRowHelper().save(formTemplate.clone().getRows());
    }

    /**
     * Сброс состояния хэлпера в исходное состояние.
     * Удаляются все строки, кроме строк из макета и чистятся логи
     */
    public void reset() {
        initRowData();
        getLogger().clear();
    }

    public void execute(FormDataEvent formDataEvent) {
        execute(formDataEvent, Collections.<String, Object>emptyMap());
    }


    /**
     * Выполнение части скрипта, связанного с указанным событием
     */
    public void execute(FormDataEvent formDataEvent, Map<String, Object> paramMap) {
        Bindings bindings = scriptingService.getEngine().createBindings();
        bindings.put("formDataEvent", formDataEvent);
        bindings.put("formDataService", formDataService);
        bindings.put("reportPeriodService", reportPeriodService);
        bindings.put("departmentService", departmentService);
        bindings.put("bookerStatementService", bookerStatementService);
        bindings.put("refBookService", refBookService);
        bindings.put("departmentFormTypeService", departmentFormTypeService);
        bindings.put("departmentReportPeriodService", departmentReportPeriodService);
        bindings.put("formTypeService", formTypeService);
        bindings.put("declarationService", declarationService);
        bindings.put("refBookFactory", refBookFactory);
        bindings.put("formDataDepartment", userDepartment);
        bindings.put("formData", formData);
        bindings.put("logger", logger);
        bindings.put("userInfo", new TAUserInfo());
        bindings.put("user", user);
        bindings.put("applicationVersion", "test-version");
        bindings.put("userDepartment", userDepartment);
        bindings.put("currentDataRow", currentDataRow);
        bindings.put("scriptStatusHolder", scriptStatusHolder);

        // Персонифицированные сведения о застрахованных лицах
        bindings.put("raschsvPersSvStrahLicService", raschsvPersSvStrahLicService);
        // Сводные данные об обязательствах плательщика страховых взносов
        bindings.put("raschsvObyazPlatSvService", raschsvObyazPlatSvService);
        // Сумма страховых взносов на пенсионное, медицинское, социальное страхование
        bindings.put("raschsvUplPerService", raschsvUplPerService);
        // Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством
        bindings.put("raschsvUplPrevOssService", raschsvUplPrevOssService);
        // Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование
        bindings.put("raschsvSvOpsOmsService", raschsvSvOpsOmsService);
        // Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством
        bindings.put("raschsvOssVnmService", raschsvOssVnmService);
        // Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации
        bindings.put("raschsvRashOssZakService", raschsvRashOssZakService);
        // Выплаты, произведенные за счет средств, финансируемых из федерального бюджета
        bindings.put("raschsvVyplFinFbService", raschsvVyplFinFbService);
        // Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427
        bindings.put("raschsvPravTarif31427Service", raschsvPravTarif31427Service);
        // Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427
        bindings.put("raschsvPravTarif51427Service", raschsvPravTarif51427Service);
        // Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427
        bindings.put("raschsvPravTarif71427Service", raschsvPravTarif71427Service);
        // Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427
        bindings.put("raschsvSvPrimTarif91427Service", raschsvSvPrimTarif91427Service);

        //ndfl
        bindings.put("ndflPersonService", ndflPersonService);


        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }

        if (formDataEvent == FormDataEvent.IMPORT || formDataEvent == FormDataEvent.IMPORT_TRANSPORT_FILE) {
            bindings.put("ImportInputStream", importFileInputStream);
            bindings.put("importService", mockHelper.mockImportService());
            String name;
            if (importFileName == null || importFileName.isEmpty()) {
                name = "test-file-name." + (formDataEvent == FormDataEvent.IMPORT ? "xlsm" : "rnu");
            } else {
                name = importFileName;
            }
            bindings.put("UploadFileName", name);
        }

        try {
            scriptingService.getEngine().eval(script, bindings);
        } catch (ScriptException e) {
            scriptingService.logScriptException(e, logger);
        } finally {
            if (importFileInputStream != null) {
                try {
                    importFileInputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Чтение из файла в строку
     */
    public static String readFile(String path, String charset) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            Reader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[10240];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } finally {
            stream.close();
        }
    }

    /**
     * Вывод логов после работы скрипта
     */
    public void printLog() {
        if (logger.getEntries().isEmpty()) {
            return;
        }
        System.out.println(path + ":");
        try {
            for (LogEntry logEntry : logger.getEntries()) {
                // Перекодирование для исправления кодировки при выводе в Idea
                System.out.println(logEntry.getLevel() + " "
                        + new String(logEntry.getMessage().getBytes("utf8"), Charset.defaultCharset().name()));
            }
        } catch (UnsupportedEncodingException e) {
            // Ignore
        }
    }

    /**
     * Логгер
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * DataRowHelper НФ
     */
    public DataRowHelper getDataRowHelper() {
        return mockHelper.getDataRowHelper();
    }

    /**
     * Макет НФ
     */
    public FormTemplate getFormTemplate() {
        return formTemplate.clone();
    }

    /**
     * Mock RefBookService для реализации mock-логики внутри теста
     */
    public RefBookService getRefBookService() {
        return refBookService;
    }

    /**
     * Mock ReportPeriodService для реализации mock-логики внутри теста
     */
    public ReportPeriodService getReportPeriodService() {
        return reportPeriodService;
    }

    /**
     * Mock DepartmentService для реализации mock-логики внутри теста
     */
    public DepartmentService getDepartmentService() {
        return departmentService;
    }

    /**
     * Mock BookerStatementService для реализации mock-логики внутри теста
     */
    public BookerStatementService getBookerStatementService() {
        return bookerStatementService;
    }

    /**
     * Mock DepartmentFormTypeService для реализации mock-логики внутри теста
     */
    public DepartmentFormTypeService getDepartmentFormTypeService() {
        return departmentFormTypeService;
    }

    /**
     * Mock FormDataService для реализации mock-логики внутри теста
     */
    public FormDataService getFormDataService() {
        return formDataService;
    }

    /**
     * Mock FormTypeService для реализации mock-логики внутри теста
     */
    public FormTypeService getFormTypeService() {
        return formTypeService;
    }

    /**
     * Mock RefBookFactory для реализации mock-логики внутри теста
     */
    public RefBookFactory getRefBookFactory() {
        return refBookFactory;
    }

    /**
     * Mock RefBookDataProvider для реализации mock-логики внутри теста
     */
    public RefBookDataProvider getRefBookDataProvider() {
        return refBookDataProvider;
    }

    public DepartmentReportPeriodService getDepartmentReportPeriodService() {
        return departmentReportPeriodService;
    }

    /**
     * Получить все записи справочника.
     *
     * @param refBookId идентификатор справочника
     */
    public Map<Long, Map<String, RefBookValue>> getRefBookAllRecords(Long refBookId) {
        return mockHelper.getRefBookAllRecords(refBookId);
    }

    public String getImportFileName() {
        return importFileName;
    }

    public void setImportFileName(String importFileName) {
        this.importFileName = importFileName;
    }

    public FormData getFormData() {
        return formData;
    }

    public void setScriptStatusHolder(ScriptStatusHolder scriptStatusHolder) {
        this.scriptStatusHolder = scriptStatusHolder;
    }
}