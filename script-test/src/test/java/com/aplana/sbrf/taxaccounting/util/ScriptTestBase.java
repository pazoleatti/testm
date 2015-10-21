package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.mock.DefaultScriptTestMockHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.InputStream;

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
}
