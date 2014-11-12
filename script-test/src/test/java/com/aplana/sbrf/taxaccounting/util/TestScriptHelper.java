package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;

/**
 * Хэлпер для работы со скриптами НФ в тестовом режиме
 *
 * @author Levykin
 */
public class TestScriptHelper {
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

    private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();
    private DataRowHelper dataRowHelper;

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
        // Шаблон НФ из файла
        formData.initFormTemplateParams(getTemplate(SCRIPT_PATH_PREFIX + path));
        this.path = SCRIPT_PATH_PREFIX + path + SCRIPT_PATH_FILE_NAME;
        try {
            script = readFile(this.path, charsetName);
        } catch (IOException e) {
            throw new ServiceException("Can't load script with path \"" + this.path + "\".", e);
        }
    }

    /**
     * Получение шаблона НФ из файлов content.xml, headers.xml, rows.xml
     * Затратная по времени операция
     */
    private FormTemplate getTemplate(String path) {
        try {
            formTemplate = new FormTemplate();
            formTemplate.setId(formData.getFormTemplateId());
            // content.xml
            JAXBContext jaxbContext = JAXBContext.newInstance(FormTemplateContent.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FormTemplateContent formTemplateContent = (FormTemplateContent) unmarshaller.unmarshal(
                    new InputStreamReader(new FileInputStream(path + CONTENT_FILE_NAME), XML_ENCODING));
            formTemplateContent.fillFormTemplate(formTemplate);
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
            return formTemplate;
        } catch (Exception e) {
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
     * Событие FormDataEvent.CHECK
     */
    public void check() {
        execute(FormDataEvent.CHECK);
    }

    /**
     * Событие FormDataEvent.CALCULATE
     */
    public void calc() {
        execute(FormDataEvent.CALCULATE);
    }

    /**
     * Событие FormDataEvent.ADD_ROW
     */
    public void addRow() {
        execute(FormDataEvent.ADD_ROW);
    }

    /**
     * Событие FormDataEvent.DELETE_ROW
     */
    public void delRow() {
        execute(FormDataEvent.DELETE_ROW);
    }

    /**
     * Событие FormDataEvent.IMPORT
     */
    public void importExcel() {
        execute(FormDataEvent.IMPORT);
    }

    /**
     * Выполнение части скрипта, связанного с указанным событием
     */
    private void execute(FormDataEvent formDataEvent) {
        Bindings bindings = scriptingService.getEngine().createBindings();
        bindings.put("formDataEvent", formDataEvent);
        bindings.put("formDataService", mockHelper.mockFormDataService());
        bindings.put("reportPeriodService", mockHelper.mockReportPeriodService());
        bindings.put("refBookService", mockHelper.mockRefBookService());
        bindings.put("formData", formData);
        bindings.put("logger", logger);
        bindings.put("user", user);
        bindings.put("applicationVersion", "test-version");
        bindings.put("userDepartment", userDepartment);
        bindings.put("currentDataRow", currentDataRow);

        if (formDataEvent == FormDataEvent.IMPORT) {
            bindings.put("ImportInputStream", importFileInputStream);
            bindings.put("importService", mockHelper.mockImportService());
            bindings.put("UploadFileName", "test-file-name.xlsm");
        }

        // Начальные строки
        mockHelper.getDataRowHelper().save(formTemplate.getRows());

        try {
            scriptingService.getEngine().eval(script, bindings);
        } catch (ScriptException e) {
            scriptingService.logScriptException(e, logger);
        }
        dataRowHelper = mockHelper.getDataRowHelper();
    }

    /**
     * Чтениние из файла в строку
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
                        + new String(logEntry.getMessage().getBytes("utf8"), "cp1251"));
            }
        } catch (UnsupportedEncodingException e) {
            // Ignore
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public DataRowHelper getDataRowHelper() {
        return dataRowHelper;
    }
}