package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookFactoryImpl;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.SignService;
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
import java.util.*;

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
    private static String FILE_NAME_4 = "test.xlsx";

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
        RefBookFactory refBookFactory = mock(RefBookFactoryImpl.class);
        ReflectionTestUtils.setField(service, "refBookFactory", refBookFactory);
        mockAuditService();
        temporaryFolder.create();
        uploadFolder = temporaryFolder.newFolder("UPLOAD_DIRECTORY");
        archiveFolder = temporaryFolder.newFolder(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY.name());
        errorFolder = temporaryFolder.newFolder(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY.name());
        mockConfigurationDao();
        mockRefBookScriptingService();
        mockSignService();
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

    private void mockSignService() {
        SignService signService = mock(SignService.class);
        when(signService.checkSign(anyString(), anyString(), anyInt(), any(Logger.class))).thenReturn(new Pair<Boolean, Set<String>>(true, new HashSet<String>()));
        ReflectionTestUtils.setField(service, "signService", signService);

        LockDataService lockService = mock(LockDataService.class);
        when(lockService.lock(anyString(), anyInt(), anyString(), anyString())).thenReturn(null);
        ReflectionTestUtils.setField(service, "lockService", lockService);

        RefBookDao refBookDao = mock(RefBookDao.class);
        RefBook refBook = new RefBook();
        refBook.setAttributes(new ArrayList<RefBookAttribute>());
        when(refBookDao.get(anyLong())).thenReturn(refBook);
        ReflectionTestUtils.setField(service, "refBookDao", refBookDao);
    }

    @Test
    public void isNSIFileTest() {
        // ЦАС НСИ
        for (String name : new String[]{"OKA99VVV.RR", "payments.OKATO.9999.VVV.RR", "OKAdd777.99",
                "payments.OKATO.f00f.h0h.00", "RNU00VVV.RR", "BUH00VVV.RR", "generaluse.AS_RNU.VVV.RR", "bookkeeping.Bookkeeping.VVV.RR"}) {
            Assert.assertTrue("File \"" + name + "\" is NSI file!", service.isNSIFile(name));
        }
        // Не ЦАС НСИ
        for (String name : new String[]{null, "OKA99VVVV.RR", "OKA99VVV..RR", "OKA.", "OKA99VVVV.",
                "payments.OKATO.9999......RR", "payments.OKATO.99999VVV.RR", "OKAdd77799", "payments.OKATO.f00f.h0h.000",
                "RNU.", "generaluse.AS_RNU.", "BUH00VVV.RRR", "bookkeeping.Bookkeeping.", "bookkeeping.Bookkeeping.000000"}) {
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
        for (String name : new String[]{null, "/", "DS2405121.nsi", "DS240512nsi", "DS240512.nsi0", "DS240512.nsi.",
                "DS240512..nsi", "___852-50_______49_0000_00212014.rnu"}) {
            Assert.assertFalse("File \"" + name + "\" is not Diasoft Custody file!", service.isDiasoftFile(name));
        }
    }

    // Файл, не подходящий по маске
    @Test
    public void otherFileTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_1);
        file.createNewFile();
        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger(), "1", false);
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

    // Успешный импорт Diasoft — Эмитенты
    @Test
    public void successEmitentLoadTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_2);
        file.createNewFile();
        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger(), "1", false);
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
                if (scriptStatusHolder.getScriptStatus().equals(ScriptStatus.SKIP)) scriptStatusHolder.setStatusMessage("SKIP");
                return null;
            }
        }).when(refBookScriptingService).executeScript(any(TAUserInfo.class), anyInt(), any(FormDataEvent.class),
                any(Logger.class), any(Map.class));
        ReflectionTestUtils.setField(service, "refBookScriptingService", refBookScriptingService);

        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_2);
        file.createNewFile();

        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger(), "1", false);

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
        ImportCounter importCounter = service.importRefBookDiasoft(USER_INFO, new Logger(), "1", false);
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
        ImportCounter importCounter = service.importRefBookNsi(USER_INFO, new Logger(), "1", false);
        // Счетчики
        Assert.assertEquals(1, importCounter.getSuccessCounter());
        // importRefBookNsi грузит в 3 справочника: ОКАТО, Субъекты РФ, План счетов - в 1ый удачно, для остальные два справочника файл будет пропущен
        Assert.assertEquals(2, importCounter.getFailCounter());
        // Каталог загрузки
        Assert.assertEquals(1, uploadFolder.list().length);
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(0, errorFolder.list().length);
    }

    // Проверка сортировки
    // TODO Проверить сортировку другим способом, после изменения логики загрузки этот способ не годится
//    @Test
//    public void sortLoadTest() throws IOException {
//        String name1 = "bookkeeping.Bookkeeping.109.01";
//        String name2 = "generaluse.AS_RNU.001.66";
//        String name3 = "Bookkeeping.Bookkeeping.108.02";
//        String name4 = "bookkeeping.Bookkeeping.107.01";
//        String name5 = "Generaluse.AS_RNU.001.65";
//        String name6 = "generaluse.AS_RNU.001.67";
//
//        List<String> unsortNames = Arrays.asList(name1, name2, name3, name4, name5, name6);
//
//        for (String name : unsortNames) {
//            File file = new File(uploadFolder.getPath() + "/" + name);
//            file.createNewFile();
//        }
//
//        Logger logger = new Logger();
//        ImportCounter importCounter = service.importRefBookNsi(USER_INFO, logger);
//
//        // Счетчики
//        Assert.assertEquals(6, importCounter.getSuccessCounter());
//        Assert.assertEquals(0, importCounter.getFailCounter());
//        // Каталог загрузки
//        Assert.assertEquals(6, uploadFolder.list().length);
//        // Архив
//        Assert.assertEquals(0, archiveFolder.list().length);
//        // Ошибки
//        Assert.assertEquals(0, errorFolder.list().length);
//
//        int pos1 = getPosition(name1, logger.getEntries());
//        int pos2 = getPosition(name2, logger.getEntries());
//        int pos3 = getPosition(name3, logger.getEntries());
//        int pos4 = getPosition(name4, logger.getEntries());
//        int pos5 = getPosition(name5, logger.getEntries());
//        int pos6 = getPosition(name6, logger.getEntries());
//
//        Assert.assertTrue(pos1 > 0 && pos2 > 0 && pos3 > 0 && pos4 > 0 && pos5 > 0 && pos6 > 0);
//        Assert.assertTrue(pos5 < pos2 && pos2 < pos6);
//        Assert.assertTrue(pos3 < pos4 && pos4 < pos1);
//    }
//
//    private int getPosition(String str, List<LogEntry> entryList) {
//        for (LogEntry entry : entryList) {
//            if (entry.getMessage().contains(str)) {
//                return entryList.indexOf(entry);
//            }
//        }
//        return -1;
//    }
}
