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
        testHelper = null;
    }

    /**
     * Инициализация перед каждым отдельным тестом
     */
    @Before
    public void init() {
        if (testHelper == null) {
            testHelper = new TestScriptHelper(getFolderPath(), getFormData(), getMockHelper());
        }
        testHelper.getLogger().clear();
        testHelper.initRowData();
    }

    /**
     * Печать вывода скрипта
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
    protected abstract String getFolderPath();

    /**
     * Проверка логгера на наличие ошибок
     */
    protected void checkLogger() {
        Assert.assertFalse("Logger contains error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    /**
     * Файл для импорта из Excel-файлов (из интерфейса)
     */
    protected abstract InputStream getImportXlsInputStream();

    /**
     * Файл для импорта из .rnu-файлов (загрузка ТФ)
     */
    protected abstract InputStream getImportRnuInputStream();

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
