package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * @author Dmitriy Levykin
 */
public class LoadRefBookDataServiceTest {
    LoadRefBookDataService service;

    private static String FILE_NAME_1 = "____852-4______________147212014__.rnu";
    private static String FILE_NAME_2 = "DS999999.nsi";
    private static String FILE_NAME_3 = "OKA99VVV.RR";

    private TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File uploadFolder;
    private File archiveFolder;
    private File errorFolder;

    private static final TAUserInfo USER_INFO = new TAUserInfo();
    private static final TAUserInfo SYSTEM_INFO = new TAUserInfo();

    static {
        // Пользователь
        TAUser user = new TAUser();
        user.setDepartmentId(6);
        user.setId(1);
        USER_INFO.setUser(user);
        // Система
        user = new TAUser();
        user.setId(TAUser.SYSTEM_USER_ID);
        user.setDepartmentId(3);
        SYSTEM_INFO.setUser(user);
    }

    @Before
    public void init() throws IOException {
        service = new LoadRefBookDataServiceImpl();
        mockAuditService();
        temporaryFolder.create();
        uploadFolder = temporaryFolder.newFolder("UPLOAD_DIRECTORY");
        archiveFolder = temporaryFolder.newFolder(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY.name());
        errorFolder = temporaryFolder.newFolder(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY.name());
        mockConfigurationDao();
        mockRefBookScriptingService();
    }

    @After
    public void clean() throws IOException {
        uploadFolder.delete();
        archiveFolder.delete();
        archiveFolder.delete();
        temporaryFolder.delete();
    }

    private void mockAuditService() {
        AuditService auditService = mock(AuditService.class);
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                System.out.println(invocation);
//                return null;
//            }
//        }).when(auditService).add(any(FormDataEvent.class), any(TAUserInfo.class), any(Integer.class), any(Integer.class),
//                anyString(), anyString(), any(Integer.class), anyString());
        ReflectionTestUtils.setField(service, "auditService", auditService);
    }

    private void mockConfigurationDao() {
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ConfigurationParamModel model = new ConfigurationParamModel();
        model.put(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.OKATO_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.REGION_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY, 0, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
        model.put(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY, 0, Arrays.asList("file://" + archiveFolder.getPath() + "/"));
        model.put(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY, 0, Arrays.asList("file://" + errorFolder.getPath() + "/"));
        when(configurationDao.getByDepartment(anyInt())).thenReturn(model);
        ReflectionTestUtils.setField(service, "configurationDao", configurationDao);
    }

    private void mockRefBookScriptingService() {
        RefBookScriptingService refBookScriptingService = mock(RefBookScriptingService.class);
        // Эмуляция успешного выполнения скрипта
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map map = (Map) invocation.getArguments()[4];
                ScriptStatusHolder scriptStatusHolder = (ScriptStatusHolder) map.get("scriptStatusHolder");
                scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS);
                return null;
            }
        }).when(refBookScriptingService).executeScript(any(TAUserInfo.class), anyInt(), any(FormDataEvent.class),
                any(Logger.class), any(Map.class));
        ReflectionTestUtils.setField(service, "refBookScriptingService", refBookScriptingService);
    }


    @Test
    public void isNSIFileTest() {
        // ЦАС НСИ
        for (String name : new String[]{"OKA99VVV.RR", "payments.OKATO.9999.VVV.RR", "OKAdd777.99",
                "payments.OKATO.f00f.h0h.00", "RNU00VVV.RR", "generaluse.AS_RNU.VVV.RR", "bookkeeping.Bookkeeping.VVV.RR"}) {
            Assert.assertTrue("File \"" + name + "\" is NSI file!", service.isNSIFile(name));
        }
        // Не ЦАС НСИ
        for (String name : new String[]{null, "OKA99VVVV.RR", "OKA99VVV..RR", "OKA.", "OKA99VVVV.",
                "payments.OKATO.9999......RR", "payments.OKATO.99999VVV.RR", "OKAdd77799", "payments.OKATO.f00f.h0h.000",
                "RNU.", "generaluse.AS_RNU.", "bookkeeping.Bookkeeping.", "bookkeeping.Bookkeeping.000000"}) {
            Assert.assertFalse("File \"" + name + "\" is not NSI file!", service.isNSIFile(name));
        }
    }

    @Test
    public void isDiasoftFileTest() {
        // Diasoft Custody
        for (String name : new String[]{"DS240512.nsi", "ds240512.NSI", "DS000000.nsi", "DS999999.nsi"}) {
            Assert.assertTrue("File \"" + name + "\" is Diasoft Custody file!", service.isDiasoftFile(name));
        }
        // Не Diasoft Custody
        for (String name : new String[]{null, "/", "DS2405121.nsi", "DS240512nsi", "DS240512.nsi0", "DS240512.nsi.", "DS240512..nsi"}) {
            Assert.assertFalse("File \"" + name + "\" is not Diasoft Custody file!", service.isDiasoftFile(name));
        }
    }

    // Файл, не подходящий по маске
    @Test
    public void otherFileTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_1);
        file.createNewFile();
        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(0, importCounter.getFailCounter());
        // Каталог загрузки
        Assert.assertEquals(1, uploadFolder.list().length);
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(0, errorFolder.list().length);
    }

    // Успешный импорт Diasoft — Эмитенты
    @Test
    public void successEmitentLoadTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_2);
        file.createNewFile();
        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(1, importCounter.getSuccessCounter());
        Assert.assertEquals(0, importCounter.getFailCounter());
        // Каталог загрузки
        Assert.assertEquals(0, uploadFolder.list().length);
        // Архив
        Assert.assertEquals(1, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(0, errorFolder.list().length);
    }

    // Успешный импорт Diasoft — Ценные бумаги
    @Test
    public void successBondLoadTest() throws IOException {
        RefBookScriptingService refBookScriptingService = mock(RefBookScriptingService.class);
        final Queue<ScriptStatus> statusQueue = new LinkedList<ScriptStatus>(Arrays.asList(ScriptStatus.SKIP, ScriptStatus.SUCCESS));
        // Первый раз вернет SKIP, второй раз SUCCESS
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map map = (Map) invocation.getArguments()[4];
                ScriptStatusHolder scriptStatusHolder = (ScriptStatusHolder) map.get("scriptStatusHolder");
                scriptStatusHolder.setScriptStatus(statusQueue.poll());
                return null;
            }
        }).when(refBookScriptingService).executeScript(any(TAUserInfo.class), anyInt(), any(FormDataEvent.class),
                any(Logger.class), any(Map.class));
        ReflectionTestUtils.setField(service, "refBookScriptingService", refBookScriptingService);

        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_2);
        file.createNewFile();
        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(1, importCounter.getSuccessCounter());
        Assert.assertEquals(0, importCounter.getFailCounter());
        // Каталог загрузки
        Assert.assertEquals(0, uploadFolder.list().length);
        // Архив
        Assert.assertEquals(1, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(0, errorFolder.list().length);
    }

    // Ошибка в скрипте
    @Test
    public void scriptErrorTest() throws IOException {
        RefBookScriptingService refBookScriptingService = mock(RefBookScriptingService.class);
        // Первый раз вернет SKIP, второй раз SUCCESS
        doThrow(new RuntimeException("Test exception")).when(refBookScriptingService).executeScript(any(TAUserInfo.class),
                anyInt(), any(FormDataEvent.class), any(Logger.class), any(Map.class));
        ReflectionTestUtils.setField(service, "refBookScriptingService", refBookScriptingService);

        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_2);
        file.createNewFile();
        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Каталог загрузки
        Assert.assertEquals(0, uploadFolder.list().length);
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }

    // Успешный импорт НСИ — не должен перемещаться
    @Test
    public void successNsiLoadTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_3);
        file.createNewFile();
        ImportCounter importCounter = service.importRefBookNsi(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(1, importCounter.getSuccessCounter());
        Assert.assertEquals(0, importCounter.getFailCounter());
        // Каталог загрузки
        Assert.assertEquals(1, uploadFolder.list().length);
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(0, errorFolder.list().length);
    }
}
