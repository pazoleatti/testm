package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.mock.DefaultScriptTestMockHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.*;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Базовый класс для тестов скриптов
 *
 * @author Levykin
 */
public abstract class ScriptTestBase {
    // Хэлпер для работы со скриптами НФ в тестовом режиме
    // Один набор тестов для скрипта — один хелпер
    protected static TestScriptHelper testHelper;

    @BeforeClass
    public static void initClass() {
        // Т.к. это поле базового класса статично, то его необходимо сбрасывать перед тестом отдельного скрипта
        testHelper = null;
    }

    /**
     * Инициализация перед каждым отдельным тестом
     */
    @Before
    public void init() {
        // Хэлпер хранится статично для оптимизации, чтобы он был один для всех тестов отдельного скрипта
        if (testHelper == null) {
            String path = getFolderPath();
            if (path == null) {
                throw new ServiceException("Test folder path is null!");
            }
            testHelper = new TestScriptHelper(path, getFormData(), getMockHelper());
        }
        testHelper.reset();
    }

    /**
     * Печать вывода скрипта после каждого теста
     */
    @After
    public void printLog() {
        testHelper.printLog();
    }

    /**
     * Экземпляр НФ
     */
    protected abstract FormData getFormData();

    /**
     * Путь к каталогу НФ
     */
    protected String getFolderPath() {
        // Путь вычисляется из пути к классу теста
        String classPath = this.getClass().getResource(".").toString();
        return classPath.substring(classPath.indexOf("/form_template/"));
    }

    /**
     * Проверка логгера на наличие ошибок
     */
    protected void checkLogger() {
        if (testHelper.getLogger().containsLevel(LogLevel.ERROR)) {
            printLog();
        }
        Assert.assertFalse("Logger contains error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    /**
     * Проверка логгера на наличие ошибок
     */
    protected void checkLoggerErrorOrWarn() {
        if (testHelper.getLogger().containsLevel(LogLevel.ERROR) || testHelper.getLogger().containsLevel(LogLevel.WARNING)) {
            printLog();
        }
        Assert.assertFalse("Logger contains error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    /**
     * Файл для импорта из Excel-файлов (из интерфейса)
     */
    protected InputStream getImportXlsInputStream() {
        return getCustomInputStream("importFile.xlsm");
    }

    /**
     * Файл для импорта из .rnu-файлов (загрузка ТФ)
     */
    protected InputStream getImportRnuInputStream() {
        return getCustomInputStream("importFile.rnu");
    }

    protected InputStream getCustomInputStream(String fileName) {
        return this.getClass().getResourceAsStream(fileName);
    }

    /**
     * Хэлпер с заглушками других сервисов
     */
    protected abstract ScriptTestMockHelper getMockHelper();

    /**
     * Хэлпер с заглушками других сервисов по-умолчанию
     */
    protected ScriptTestMockHelper getDefaultScriptTestMockHelper(Class clazz) {
        try {
            return new DefaultScriptTestMockHelper(RefBookReadHelper.readFromFolder(clazz.getResource(".")));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Read reference book error!", e);
        }
    }

    @Test
    public void checkScriptTest() {
        when(testHelper.getReportPeriodService().getCalendarStartDate(anyInt())).thenCallRealMethod();
        when(testHelper.getReportPeriodService().getStartDate(anyInt())).thenCallRealMethod();
        when(testHelper.getReportPeriodService().getEndDate(anyInt())).thenCallRealMethod();
        when(testHelper.getReportPeriodService().getReportDate(anyInt())).thenCallRealMethod();
        when(testHelper.getReportPeriodService().get(anyInt())).thenCallRealMethod();


        testHelper.execute(FormDataEvent.CHECK_SCRIPT);
        checkLogger();
    }

    public boolean containLog(String text) {
        for (LogEntry logEntry : testHelper.getLogger().getEntries()) {
            if (logEntry.getMessage().contains(text)) {
                return true;
            }
        }

        return false;
    }
}
