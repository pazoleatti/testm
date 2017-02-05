package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.io.IOUtils;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.util.TestUtils.readFile;

/**
 * Хэлпер для работы со скриптами декларации в тестовом режиме
 */
public class DeclarationTestScriptHelper {
    // Id текущего для теста экземпляра декларации
    public final static long CURRENT_DECLARATION_DATA_ID = 1;
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
    public final static String SCRIPT_PATH_PREFIX = "../src/main/resources";
    // Имя файла скрипта
    private final String SCRIPT_PATH_FILE_NAME = "script.groovy";
    // Сервис работы со скриптами
    private static ScriptingService scriptingService = new ScriptingService();
    // Mock-сервисы
    private FormDataService formDataService;
    private DepartmentFormTypeService departmentFormTypeService;
    private ReportPeriodService reportPeriodService;
    private DepartmentService departmentService;
    private RefBookService refBookService;
    private RefBookFactory refBookFactory;
    private RefBookDataProvider refBookDataProvider;
    private DeclarationService declarationService;
    private RefBookPersonService refBookPersonService;
    private FiasRefBookService fiasRefBookService;
    private TransactionHelper transactionHelper;
    private NdflPersonService ndflPersonService;

    // Задаются из конкретного теста

    private InputStream importFileInputStream;

    /**
     * Объект передаваемый в скрипт при формировании спец отчета
     */
    private ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder;

    /**
     * Cписок источников получаемый из скрипта по событию com.aplana.sbrf.taxaccounting.model.FormDataEvent.GET_SOURCES
     */
    private FormSources sources;


    private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();

    // Заданы константно
    private Logger logger = new Logger();
    private TAUser user = new TAUser();
    private Department userDepartment = new Department();

    // Задаются из конкретного теста
    private DeclarationData declarationData;
    private DeclarationTemplate declarationTemplate;
    private final ScriptTestMockHelper mockHelper;

    private StringWriter xmlStringWriter;

    public RefBookPersonService getRefBookPersonService() {
        return refBookPersonService;
    }

    public FiasRefBookService getFiasRefBookService() {
        return fiasRefBookService;
    }

    public InputStream getImportFileInputStream() {
        return importFileInputStream;
    }

    public NdflPersonService getNdflPersonService() {
        return ndflPersonService;
    }

    public void setImportFileInputStream(InputStream importFileInputStream) {
        this.importFileInputStream = importFileInputStream;
    }

    public ScriptSpecificDeclarationDataReportHolder getScriptSpecificReportHolder() {
        return scriptSpecificReportHolder;
    }

    public void setScriptSpecificReportHolder(ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder) {
        this.scriptSpecificReportHolder = scriptSpecificReportHolder;
    }

    /**
     * Сервис работы со скриптами декларации в тестовом режиме
     *
     * @param path            Относительный путь к каталогу со скриптом
     * @param declarationData Экземпляр декларации
     * @param mockHelper      Хэлпер с заглушками других сервисов, можно переопределить
     */
    public DeclarationTestScriptHelper(String path, DeclarationData declarationData, ScriptTestMockHelper mockHelper) {
        super();
        this.declarationData = declarationData;
        this.mockHelper = mockHelper;
        // Id подразделения пользователя совпадает c Id подразделения декларации
        userDepartment.setId(declarationData.getDepartmentId());
        userDepartment.setRegionId(DEPARTMENT_REGION_ID);
        userDepartment.setName(DEPARTMENT_NAME);
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
        refBookService = mockHelper.mockRefBookService();
        refBookFactory = mockHelper.mockRefBookFactory();
        refBookDataProvider = mockHelper.getRefBookDataProvider();
        refBookPersonService = mockHelper.mockRefBookPersonService();
        fiasRefBookService = mockHelper.mockFiasRefBookService();
        declarationService = mockHelper.mockDeclarationService();
        transactionHelper = mockHelper.mockTransactionHelper();
        ndflPersonService = mockHelper.mockNdflPersonService();
    }

    /**
     * Сброс состояния хэлпера в исходное состояние. Чистятся логи.
     */
    public void reset() {
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
        bindings.put("refBookService", refBookService);
        bindings.put("departmentFormTypeService", departmentFormTypeService);
        bindings.put("refBookFactory", refBookFactory);
        bindings.put("refBookDataProvider", refBookDataProvider);
        bindings.put("declarationService", declarationService);
        bindings.put("ndflPersonService", ndflPersonService);

        bindings.put("refBookPersonService", refBookPersonService);
        bindings.put("fiasRefBookService", fiasRefBookService);

        bindings.put("scriptSpecificReportHolder", scriptSpecificReportHolder);

        bindings.put("formDataDepartment", userDepartment);
        bindings.put("declarationData", declarationData);
        bindings.put("logger", logger);
        bindings.put("userInfo", new TAUserInfo());
        bindings.put("user", user);
        bindings.put("applicationVersion", "test-version");

        bindings.put(DeclarationDataScriptParams.DOC_DATE, new Date());


        if (formDataEvent == FormDataEvent.CALCULATE) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.YEAR, 2015);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 12);
            bindings.put(DeclarationDataScriptParams.DOC_DATE, calendar.getTime());
            xmlStringWriter = new StringWriter();
            bindings.put(DeclarationDataScriptParams.XML, xmlStringWriter);
        } else {
            xmlStringWriter = null;
        }


        if (formDataEvent == FormDataEvent.GET_SOURCES) {
            sources = new FormSources();
            bindings.put("sources", sources);
            //bindings.put("light", false);
            //bindings.put("excludeIfNotExist", false);
            //bindings.put("stateRestriction", WorkflowState.ACCEPTED);
            //bindings.put("needSources", true);
            //bindings.put("form", true);
        }


        if (formDataEvent == FormDataEvent.IMPORT || formDataEvent == FormDataEvent.IMPORT_TRANSPORT_FILE) {
            bindings.put("ImportInputStream", importFileInputStream);
            bindings.put("importService", mockHelper.mockImportService());
        }

        try {
            scriptingService.getEngine().eval(script, bindings);
        } catch (ScriptException e) {
            scriptingService.logScriptException(e, logger);
        } finally {

            if (importFileInputStream != null) {
                IOUtils.closeQuietly(importFileInputStream);
            }

            if (scriptSpecificReportHolder != null) {
                IOUtils.closeQuietly(scriptSpecificReportHolder.getFileInputStream());
            }
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
                        + new String(logEntry.getMessage().getBytes("utf8"), "cp1251"));
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
     * Макет декларации
     */
    public DeclarationTemplate getDeclarationTemplate() {
        return declarationTemplate;
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

    /**
     * Получить xml после формирования декларации.
     */
    public StringWriter getXmlStringWriter() {
        return xmlStringWriter;
    }

    /**
     * Получить все записи справочника.
     *
     * @param refBookId идентификатор справочника
     */
    public Map<Long, Map<String, RefBookValue>> getRefBookAllRecords(Long refBookId) {
        return mockHelper.getRefBookAllRecords(refBookId);
    }

    public static ScriptingService getScriptingService() {
        return scriptingService;
    }

    public DeclarationService getDeclarationService() {
        return declarationService;
    }

    public FormSources getSources() {
        return sources;
    }
}