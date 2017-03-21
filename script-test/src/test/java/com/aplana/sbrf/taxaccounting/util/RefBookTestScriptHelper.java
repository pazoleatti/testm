package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.dao.script.BlobDataService;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ScriptStatusHolder;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import net.sf.sevenzipjbinding.IInArchive;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.util.TestUtils.readFile;

/**
 * Хэлпер для работы со скриптами справочников в тестовом режиме
 *
 * @author Yasinskii
 */
public class RefBookTestScriptHelper {

    // Пкть к скрипту
    private String path;
    // Текст скрипта
    private String script;
    // Кодировка скриптов
    private final String charsetName = "UTF-8";
    // Префикс пути скрипта
    private final static String SCRIPT_PATH_PREFIX = "../src/main/resources";
    // Имя файла скрипта
    private final String SCRIPT_PATH_FILE_NAME = "script.groovy";
    // Сервис работы со скриптами
    private static ScriptingService scriptingService = new ScriptingService();
    // Mock-сервисы
    private FormDataService formDataService;
    private ReportPeriodService reportPeriodService;
    private RefBookService refBookService;
    private RefBookFactory refBookFactory;
    private RefBookDataProvider refBookDataProvider;
    private FormTypeService formTypeService;
    private DeclarationService declarationService;
    private ImportFiasDataService importFiasDataService;
    private BlobDataService blobDataService;
    private DepartmentService departmentService;
    private DepartmentReportPeriodService departmentReportPeriodService;

    // Заданы константно
    private Logger logger = new Logger();
    // Задаются из конкретного теста
    private Date validDateFrom;
    private Date validDateTo;
    private Long uniqueRecordId;
    private Long recordCommonId;
    private List<Map<String, RefBookValue>> saveRecords;
    private boolean isNewRecords;
    private ScriptStatusHolder scriptStatusHolder = new ScriptStatusHolder();
    private InputStream importFileInputStream;
    private InputStream importInputStream;
    private File dataFile;
    private String fileName;
    private String uploadFileName;
    private final ScriptTestMockHelper mockHelper;
    private Integer accountPeriodId; // необходим для БО: форма 101 и 102
    private IInArchive fiasArchive;
    private StringBuilder msgBuilder;

    /**
     * Сервис работы со скриптами справочников в тестовом режиме
     *
     * @param path       Относительный путь к каталогу со скриптом
     * @param mockHelper Хэлпер с заглушками других сервисов, можно переопределить
     */
    public RefBookTestScriptHelper(String path, ScriptTestMockHelper mockHelper) {
        super();
        this.mockHelper = mockHelper;

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
        reportPeriodService = mockHelper.mockReportPeriodService();
        refBookService = mockHelper.mockRefBookService();
        refBookFactory = mockHelper.mockRefBookFactory();
        refBookDataProvider = mockHelper.getRefBookDataProvider();
        formTypeService = mockHelper.mockFormTypeService();
        declarationService = mockHelper.mockDeclarationService();
        importFiasDataService = mockHelper.mockImportFiasDataService();
        blobDataService = mockHelper.mockBlobDataService();
        departmentService = mockHelper.mockDepartmentService();
        departmentReportPeriodService = mockHelper.mockDepartmentReportPeriodService();
    }

    public void setImportFileInputStream(InputStream importFileInputStream) {
        this.importFileInputStream = importFileInputStream;
    }

    /**
     * Выполнение части скрипта, связанного с указанным событием
     */
    public void execute(FormDataEvent formDataEvent) {
        Bindings bindings = scriptingService.getEngine().createBindings();

        bindings.put("formDataEvent", formDataEvent);
        bindings.put("refBookFactory", refBookFactory);
        bindings.put("refBookService", refBookService);
        bindings.put("formDataService", formDataService);
        bindings.put("declarationService", declarationService);
        bindings.put("formTypeService", formTypeService);
        bindings.put("departmentService", departmentService);
        bindings.put("departmentReportPeriodService", departmentReportPeriodService);
        bindings.put("blobDataServiceDaoImpl", blobDataService);

        bindings.put("uniqueRecordId", uniqueRecordId);
        bindings.put("recordCommonId", recordCommonId);
        bindings.put("saveRecords", saveRecords);
        bindings.put("validDateFrom", validDateFrom);
        bindings.put("validDateTo", validDateTo);
        bindings.put("isNewRecords", isNewRecords);
        bindings.put("scriptStatusHolder", scriptStatusHolder);
        bindings.put("logger", logger);
        bindings.put("accountPeriodId", accountPeriodId);
        bindings.put("UploadFileName", uploadFileName);
        bindings.put("ImportInputStream", importInputStream);
        bindings.put("importFiasDataService", importFiasDataService);
        bindings.put("userInfo", new TAUserInfo());
        bindings.put("dataFile", dataFile);
        bindings.put("msgBuilder", msgBuilder);

        if (formDataEvent == FormDataEvent.IMPORT_TRANSPORT_FILE ||
                formDataEvent == FormDataEvent.IMPORT ||
                formDataEvent == FormDataEvent.PRE_CALCULATION_CHECK) {

            bindings.put("inputStream", importFileInputStream);
            String name;
            if (fileName == null || fileName.isEmpty()) {
                name = "test-file-name." + (formDataEvent == FormDataEvent.IMPORT_TRANSPORT_FILE ? "rnu" : "xml");
            } else {
                name = fileName;
            }
            bindings.put("fileName", name);
        }

        //для тестов загрузки фиас
        if (fiasArchive != null) {
            bindings.put("archive", fiasArchive);
        }

        if (formDataEvent == FormDataEvent.IMPORT || formDataEvent == FormDataEvent.PRE_CALCULATION_CHECK) {
            try {
                bindings.put("dateFrom", new SimpleDateFormat("dd.MM.yyyy").parse("01.01.2016"));
                bindings.put("dateTo", new SimpleDateFormat("dd.MM.yyyy").parse("01.07.2016"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
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

            if (fiasArchive != null) {
                try {
                    fiasArchive.close();
                } catch (IOException e) {
                }
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
                        + new String(logEntry.getMessage().getBytes("utf8"), Charset.defaultCharset().name()));
            }
        } catch (UnsupportedEncodingException e) {
            // Ignore
        }
    }

    /**
     * Сброс состояния хэлпера в исходное состояние (чистятся логи)
     */
    public void reset() {
        getLogger().clear();
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
     * Mock DeclarationService для реализации mock-логики внутри теста
     */
    public DeclarationService getDeclarationService() {
        return declarationService;
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

    public IInArchive getFiasArchive() {
        return fiasArchive;
    }

    public void setFiasArchive(IInArchive fiasArchive) {
        this.fiasArchive = fiasArchive;
    }

    /**
     * Получить все записи справочника.
     *
     * @param refBookId идентификатор справочника
     */
    public Map<Long, Map<String, RefBookValue>> getRefBookAllRecords(Long refBookId) {
        return mockHelper.getRefBookAllRecords(refBookId);
    }

    public void setValidDateFrom(Date validDateFrom) {
        this.validDateFrom = validDateFrom;
    }

    public void setValidDateTo(Date validDateTo) {
        this.validDateTo = validDateTo;
    }

    public void setUniqueRecordId(Long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }

    public void setRecordCommonId(Long recordCommonId) {
        this.recordCommonId = recordCommonId;
    }

    public void setSaveRecords(List<Map<String, RefBookValue>> saveRecords) {
        this.saveRecords = saveRecords;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public File getDataFile() {
        return dataFile;
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public InputStream getImportInputStream() {
        return importInputStream;
    }

    public void setImportInputStream(InputStream importInputStream) {
        this.importInputStream = importInputStream;
    }

    public BlobDataService getBlobDataService() {
        return blobDataService;
    }

    public void setBlobDataService(BlobDataService blobDataService) {
        this.blobDataService = blobDataService;
    }

    public void setAccountPeriodId(Integer accountPeriodId) {
        this.accountPeriodId = accountPeriodId;
    }

    public DepartmentService getDepartmentService() {
        return departmentService;
    }

    public void setDepartmentService(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    public DepartmentReportPeriodService getDepartmentReportPeriodService() {
        return departmentReportPeriodService;
    }

    public void setDepartmentReportPeriodService(DepartmentReportPeriodService departmentReportPeriodService) {
        this.departmentReportPeriodService = departmentReportPeriodService;
    }

    public ImportFiasDataService getImportFiasDataService() {
        return importFiasDataService;
    }

    public StringBuilder getMsgBuilder() {
        return msgBuilder;
    }

    public void setMsgBuilder(StringBuilder msgBuilder) {
        this.msgBuilder = msgBuilder;
    }
}