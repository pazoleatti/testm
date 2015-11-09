package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.Charset;
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
    private TransactionHelper transactionHelper;

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
    private final ScriptTestMockHelper mockHelper;

    private String importFileName = null;

    /**
     * Сервис работы со скриптами НФ в тестовом режиме
     *
     * @param path Относительный путь к каталогу со скриптом
     * @param formData Экземпляр НФ
     * @param mockHelper Хэлпер с заглушками других сервисов, можно переопределить
     */
    public TestScriptHelper(String path, FormData formData, ScriptTestMockHelper mockHelper) {
        super();
        this.formData = formData;
        this.mockHelper = mockHelper;
        // Id подразделения пользователя совпадает c Id подразделения НФ
        userDepartment.setId(formData.getDepartmentId());
        userDepartment.setRegionId(DEPARTMENT_REGION_ID);
        // Шаблон НФ из файла
        FormType formType = formData.getFormType();
        formData.initFormTemplateParams(getTemplate(SCRIPT_PATH_PREFIX + path, true));
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
        transactionHelper = mockHelper.mockTransactionHelper();
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
     * @param path путь к каталогу макета
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
                formTemplate.getRows().addAll(xmlSerializationUtils.deserialize(rowsString,
                        formTemplate.getColumns(), formTemplate.getStyles(), Cell.class));
            }
            // headers.xml
            formTemplate.getHeaders().clear();
            String headersString = readFile(path + HEADERS_FILE_NAME, XML_ENCODING);
            if (headersString != null && !headersString.isEmpty()) {
                formTemplate.getHeaders().addAll(xmlSerializationUtils.deserialize(headersString,
                        formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class));
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

    /**
     * Выполнение части скрипта, связанного с указанным событием
     */
    public void execute(FormDataEvent formDataEvent) {
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
        bindings.put("refBookFactory", refBookFactory);
        bindings.put("formDataDepartment", userDepartment);
        bindings.put("formData", formData);
        bindings.put("logger", logger);
        bindings.put("userInfo", new TAUserInfo());
        bindings.put("user", user);
        bindings.put("applicationVersion", "test-version");
        bindings.put("userDepartment", userDepartment);
        bindings.put("currentDataRow", currentDataRow);

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
}