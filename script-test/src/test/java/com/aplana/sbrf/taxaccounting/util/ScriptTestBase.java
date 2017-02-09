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

    /**
     * Универсальная проверка значений после импорта:
     * в xls файле должно быть слева направо сверху вниз:
     * 1. для строк : string1, string2, ..., stringN
     * 2. для чисел : 1, 2, ..., N
     * 3. для дат : 01.01.year, 02.01.year, ..., NN.NN.year
     * остальные типы не проверяются
     *
     * @param aliases  алиасы граф для проверки
     * @param rowCount ожидаемое количесвто строк в НФ
     * @param year     год для проверки дат
     * @throws java.text.ParseException
     */
    protected void defaultCheckLoadData(List<String> aliases, int rowCount, String year) {
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse("01.01." + year));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int stringCount = 1;
        int numberCount = 1;

        for (int i = 0; i < rowCount; i++) {
            if (dataRows.get(i).getAlias() != null) {
                continue;
            }
            for (String alias : aliases) {
                Object value = dataRows.get(i).getCell(alias).getValue();
                // все значения д.б. заполнены
                Assert.assertNotNull(value);
                String msg = alias + " at row " + (i + 1) + ":";
                if (value instanceof String) {
                    Assert.assertEquals(msg, "string" + stringCount++, value);
                } else if (value instanceof Number) {
                    Assert.assertEquals(msg, numberCount++, ((Number) value).doubleValue(), 0);
                } else if (value instanceof Date) {
                    Assert.assertEquals(msg, calendar.getTime(), value);
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
            }
        }
        Assert.assertEquals(rowCount, dataRows.size());
    }

    protected void defaultCheckLoadData(List<String> aliases, int rowCount) {
        defaultCheckLoadData(aliases, rowCount, "2015");
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
