package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.SignService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Dmitriy Levykin
 */
public class ConfigurationServiceTest {

    private final static ConfigurationService service = new ConfigurationServiceImpl();
    private final static Department testDepartment1 = new Department();
    private final static Department testDepartment2 = new Department();
    private final static ConfigurationParamModel model = new ConfigurationParamModel();
    private final static AuditService auditService = mock(AuditService.class);

    static {
        testDepartment1.setId(1);
        testDepartment1.setName("Подразделение один");
        testDepartment2.setId(2);
        testDepartment2.setName("Подразделение два");

        List<String> newUrl = new ArrayList<String>();
        newUrl.add("keyFileUrl");
        newUrl.add("keyFileUrl2");

        Map<Integer, List<String>> newKey = new HashMap<Integer, List<String>>();
        newKey.put(0, newUrl);
        model.put(ConfigurationParam.KEY_FILE, newKey);
        model.put(ConfigurationParam.ENCRYPT_DLL, newKey);
        model.put(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, newKey);

        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, testDepartment1.getId(),
                asList("file://uploadFolder/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, testDepartment1.getId(),
                asList("file://archiveFolder/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, testDepartment1.getId(),
                asList("file://errorFolder/"));
    }

    @BeforeClass
    public static void init() {
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ReflectionTestUtils.setField(service, "configurationDao", configurationDao);
        when(configurationDao.getAll()).thenReturn(model);

        DepartmentDao departmentDao = mock(DepartmentDao.class);
        when(departmentDao.getDepartment(eq(1))).thenReturn(testDepartment1);
        when(departmentDao.getDepartment(eq(2))).thenReturn(testDepartment2);
        ReflectionTestUtils.setField(service, "departmentDao", departmentDao);

        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        RefBookDataProvider providerEmail = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> emailValues = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> config1 = new HashMap<String, RefBookValue>();
        config1.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test.test.p1"));
        config1.put("VALUE", new RefBookValue(RefBookAttributeType.STRING, "test"));
        emailValues.add(config1);
        Map<String, RefBookValue> config2 = new HashMap<String, RefBookValue>();
        config2.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "test.test.p2"));
        config2.put("VALUE", new RefBookValue(RefBookAttributeType.STRING, "tes_"));
        emailValues.add(config2);
        when(providerEmail.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(emailValues);
        when(refBookFactory.getDataProvider(RefBook.EMAIL_CONFIG)).thenReturn(providerEmail);

        RefBookDataProvider providerAsync = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> asyncValues = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> configAsync1 = new HashMap<String, RefBookValue>();
        configAsync1.put("ID", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
        configAsync1.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "Task1"));
        configAsync1.put("SHORT_QUEUE_LIMIT", new RefBookValue(RefBookAttributeType.NUMBER, 100L));
        configAsync1.put("TASK_LIMIT", new RefBookValue(RefBookAttributeType.NUMBER, 1000L));
        asyncValues.add(configAsync1);
        Map<String, RefBookValue> configAsync2 = new HashMap<String, RefBookValue>();
        configAsync2.put("ID", new RefBookValue(RefBookAttributeType.NUMBER, 2L));
        configAsync2.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "Task2"));
        configAsync2.put("SHORT_QUEUE_LIMIT", new RefBookValue(RefBookAttributeType.NUMBER, 10L));
        configAsync2.put("TASK_LIMIT", new RefBookValue(RefBookAttributeType.NUMBER, 500L));
        asyncValues.add(configAsync2);

        when(providerAsync.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(asyncValues);
        when(refBookFactory.getDataProvider(RefBook.ASYNC_CONFIG)).thenReturn(providerAsync);
        RefBook refBookAsyncConfig = new RefBook();
        List<RefBookAttribute> attributes = new ArrayList<RefBookAttribute>();
        attributes.add(new RefBookAttribute(){{
            setAlias("SHORT_QUEUE_LIMIT");
            setName("SHORT_QUEUE_LIMIT");
        }});
        attributes.add(new RefBookAttribute(){{
            setAlias("TASK_LIMIT");
            setName("TASK_LIMIT");
        }});
        refBookAsyncConfig.setAttributes(attributes);
        when(refBookFactory.get(RefBook.ASYNC_CONFIG)).thenReturn(refBookAsyncConfig);
        ReflectionTestUtils.setField(service, "refBookFactory", refBookFactory);

        ReflectionTestUtils.setField(service, "auditService", auditService);
    }

    // Нет прав на сохранение
    @Test(expected = AccessDeniedException.class)
    public void saveAllConfig1Test() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONF);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        service.saveAllConfig(userInfo, new ConfigurationParamModel(), new ArrayList<Map<String, String>>(), new ArrayList<Map<String, String>>(), logger);
    }

    // Сохранение без ошибок
    @Test
    public void saveAllConfig2Test() throws IOException {

        Logger logger = new Logger();
        ConfigurationParamModel model = new ConfigurationParamModel();

        TemporaryFolder commonFolder = new TemporaryFolder();
        TemporaryFolder uploadFolder = new TemporaryFolder();
        TemporaryFolder archiveFolder = new TemporaryFolder();
        TemporaryFolder errorFolder = new TemporaryFolder();
        commonFolder.create();
        uploadFolder.create();
        archiveFolder.create();
        errorFolder.create();

        String path = commonFolder.getRoot().getPath();
        File file = commonFolder.newFile("file");

        // Заполнение всех общих параметров
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (param.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                if (param.isFolder() != null && param.isFolder()) {
                    model.put(param, 0,
                            asList("file://" + path + "/"));
                } else {
                    model.put(param, 0,
                            asList("file://" + path + "/" + file.getName()));
                }
            }
        }

        // Заполнение параметров загрузки НФ
        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, testDepartment1.getId(),
                asList("file://" + uploadFolder.getRoot().getPath() + "/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, testDepartment1.getId(),
                asList("file://" + archiveFolder.getRoot().getPath() + "/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, testDepartment1.getId(),
                asList("file://" + errorFolder.getRoot().getPath() + "/"));

        // Заполнение параметров отправки почты
        //model.put(ConfigurationParam.EMAIL_SERVER, 0, asList("server"));
        //model.put(ConfigurationParam.EMAIL_PORT, 0, asList("25"));
        //model.put(ConfigurationParam.EMAIL_LOGIN, 0, asList("login"));
        //model.put(ConfigurationParam.EMAIL_PASSWORD, 0, asList("password"));
        List<Map<String, String>> params = new ArrayList<Map<String, String>>();

        model.put(ConfigurationParam.SIGN_CHECK, 0, asList(SignService.SIGN_CHECK));

        service.saveAllConfig(getUser(), model, params, new ArrayList<Map<String, String>>(), logger);

        file.delete();
        commonFolder.delete();
        uploadFolder.delete();
        archiveFolder.delete();
        errorFolder.delete();

        Assert.assertTrue(logger.getEntries().isEmpty());
    }

    // Дубль пути для любого подразделения
    @Test
    public void saveAllConfig3Test() throws IOException {
        Logger logger = new Logger();
        ConfigurationParamModel model = new ConfigurationParamModel();

        TemporaryFolder commonFolder = new TemporaryFolder();
        TemporaryFolder uploadFolder = new TemporaryFolder();
        TemporaryFolder errorFolder = new TemporaryFolder();
        commonFolder.create();
        uploadFolder.create();
        errorFolder.create();

        String path = commonFolder.getRoot().getPath();
        File file = commonFolder.newFile("file");

        // Заполнение всех общих параметров
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (param.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                if (param.isFolder() != null && param.isFolder()) {
                    model.put(param, 0,
                            asList("file://" + path + "/"));
                } else {
                    model.put(param, 0,
                            asList("file://" + path + "/" + file.getName()));
                }
            }
        }

        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, testDepartment1.getId(), asList("file://" + path + "/"));
        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, testDepartment1.getId(), asList("file://" + path + "/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, testDepartment1.getId(), asList("file://" + errorFolder.getRoot().getPath() + "/"));

        model.put(ConfigurationParam.SIGN_CHECK, 0, asList(SignService.SIGN_CHECK));

        service.saveAllConfig(getUser(), model, new ArrayList<Map<String, String>>(), new ArrayList<Map<String, String>>(), logger);

        file.delete();
        commonFolder.delete();
        uploadFolder.delete();
        errorFolder.delete();

        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertTrue(logger.getEntries().get(0).getMessage().contains(testDepartment1.getName()));
        Assert.assertTrue(logger.getEntries().get(0).getMessage().contains(path));
        Assert.assertTrue(logger.getEntries().get(0).getMessage().contains(ConfigurationParam.FORM_ARCHIVE_DIRECTORY.getCaption()));
        Assert.assertTrue(logger.getEntries().get(0).getMessage().contains(ConfigurationParam.FORM_UPLOAD_DIRECTORY.getCaption()));
    }

    // Заданы не все параметры
    @Test
    public void saveAllConfig4Test() throws IOException {
        Logger logger = new Logger();
        ConfigurationParamModel model = new ConfigurationParamModel();

        TemporaryFolder commonFolder = new TemporaryFolder();
        TemporaryFolder uploadFolder = new TemporaryFolder();
        TemporaryFolder archiveFolder = new TemporaryFolder();
        commonFolder.create();
        uploadFolder.create();
        archiveFolder.create();

        String path = commonFolder.getRoot().getPath();
        File file = commonFolder.newFile("file");

        // Заполнение всех общих параметров
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (param.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                if (param.isFolder() != null && param.isFolder()) {
                    model.put(param, 0, asList("file://" + path + "/"));
                } else {
                    model.put(param, 0, asList("file://" + path + "/" + file.getName()));
                }
            }
        }

        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, testDepartment1.getId(),
                asList("file://" + uploadFolder.getRoot().getPath() + "/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, testDepartment1.getId(),
                asList("file://" + archiveFolder.getRoot().getPath() + "/"));

        model.put(ConfigurationParam.SIGN_CHECK, 0, asList(SignService.SIGN_CHECK));

        service.saveAllConfig(getUser(), model, new ArrayList<Map<String, String>>(), new ArrayList<Map<String, String>>(), logger);

        file.delete();
        commonFolder.delete();
        uploadFolder.delete();
        archiveFolder.delete();

        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertTrue(logger.getEntries().get(0).getMessage().contains(ConfigurationParam.FORM_ERROR_DIRECTORY.getCaption()));
    }

    // Превышена длина
    @Test
    public void saveAllConfig5Test() throws IOException {
        Logger logger = new Logger();
        ConfigurationParamModel model = new ConfigurationParamModel();
        int length = 500;
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= length) {
            String str = UUID.randomUUID().toString();
            sb.append(str);
        }

        model.put(ConfigurationParam.KEY_FILE, 0, Arrays.asList(sb.toString(), sb.toString(), ""));
        service.saveAllConfig(getUser(), model, new ArrayList<Map<String, String>>(), new ArrayList<Map<String, String>>(), logger);

        // необязательно указывать все общие параметры, поэтому будет 6 ошибок (из них 4 у параметров отправки почты)
        // на превышение длины выше 500 символов
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertTrue(logger.getEntries().get(0).getMessage().contains("(" + length + ")"));
        Assert.assertTrue(logger.getEntries().get(1).getMessage().contains("(" + length + ")"));
    }

    // Путь недоступен
    @Test
    public void checkReadWriteAccess1Test() throws IOException {
        Logger logger = new Logger();
        ConfigurationParamModel model = new ConfigurationParamModel();

        TemporaryFolder commonFolder = new TemporaryFolder();
        TemporaryFolder uploadFolder = new TemporaryFolder();
        TemporaryFolder archiveFolder = new TemporaryFolder();
        TemporaryFolder errorFolder = new TemporaryFolder();
        commonFolder.create();
        uploadFolder.create();
        archiveFolder.create();
        errorFolder.create();

        String path = commonFolder.getRoot().getPath();
        File file = commonFolder.newFile("file");

        // Заполнение всех общих параметров
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (param.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                if (param.isFolder() != null && param.isFolder()) {
                    model.put(param, 0,
                            asList("file://" + path + "/"));
                } else {
                    model.put(param, 0,
                            asList("file://" + path + "/" + file.getName()));
                }
            }
        }

        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, testDepartment1.getId(),
                asList("file://" + uploadFolder.getRoot().getPath() + "/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, testDepartment1.getId(),
                asList("file://" + archiveFolder.getRoot().getPath() + "/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, testDepartment1.getId(),
                asList("badPath"));

        service.checkReadWriteAccess(getUser(), model, logger);

        file.delete();
        commonFolder.delete();
        uploadFolder.delete();
        archiveFolder.delete();
        errorFolder.delete();

        Assert.assertEquals(13, logger.getEntries().size());
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
        Assert.assertTrue(logger.containsLevel(LogLevel.INFO));
    }

    // Путь не папка
    @Test
    public void checkReadWriteAccess2Test() throws IOException {
        Logger logger = new Logger();
        ConfigurationParamModel model = new ConfigurationParamModel();

        TemporaryFolder commonFolder = new TemporaryFolder();
        TemporaryFolder uploadFolder = new TemporaryFolder();
        TemporaryFolder archiveFolder = new TemporaryFolder();
        TemporaryFolder errorFolder = new TemporaryFolder();
        commonFolder.create();
        uploadFolder.create();
        archiveFolder.create();
        errorFolder.create();
        errorFolder.newFile("testFile");

        String path = commonFolder.getRoot().getPath();
        File file = commonFolder.newFile("file");

        // Заполнение всех общих параметров
        for (ConfigurationParam param : ConfigurationParam.values()) {
            if (param.getGroup().equals(ConfigurationParamGroup.COMMON)) {
                if (param.isFolder() != null && param.isFolder()) {
                    model.put(param, 0,
                            asList("file://" + path + "/"));
                } else {
                    model.put(param, 0,
                            asList("file://" + path + "/" + file.getName()));
                }
            }
        }

        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, testDepartment1.getId(),
                asList("file://" + uploadFolder.getRoot().getPath() + "/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, testDepartment1.getId(),
                asList("file://" + archiveFolder.getRoot().getPath() + "/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, testDepartment1.getId(),
                asList("file://" + errorFolder.getRoot().getPath() + "/testFile"));

        service.checkReadWriteAccess(getUser(), model, logger);

        file.delete();
        commonFolder.delete();
        uploadFolder.delete();
        archiveFolder.delete();
        errorFolder.delete();

        Assert.assertEquals(13, logger.getEntries().size());
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
        Assert.assertTrue(logger.containsLevel(LogLevel.INFO));
    }

    /**
     * Не пишем в ЖА когда путь к БОК не менялся
     */
    @Test
    public void saveTest() {
        TAUserInfo userInfo = getUser();
        ((ConfigurationServiceImpl) service).saveAndLog(model, new ArrayList<Map<String, String>>(), new ArrayList<Map<String, String>>(), userInfo);

        verify(auditService, never()).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null, "keyFileUrl", null);
    }

    /**
     * Пишем в ЖА когда путь к БОК изменился
     */
    @Test()
    public void saveTestWhenKeyFileUrlUpdated() {
        ReflectionTestUtils.setField(service, "auditService", auditService);

        ConfigurationParamModel newModel = new ConfigurationParamModel();
        List<String> newUrl = new ArrayList<String>();
        newUrl.add("newKeyFileUrl");
        Map<Integer, List<String>> newKey = new HashMap<Integer, List<String>>();
        newKey.put(0, newUrl);
        // оставили без изменений
        newModel.put(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, 0, asList("keyFileUrl", "keyFileUrl2"));
        // изменили
        newModel.put(ConfigurationParam.KEY_FILE, newKey);
        // добавили
        newModel.put(ConfigurationParam.SIGN_CHECK, 0, asList("1"));
        // удалили
        //newModel.put(ConfigurationParam.ENCRYPT_DLL, newKey);

        // оставили без изменений
        newModel.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, testDepartment1.getId(),
                asList("file://uploadFolder/"));
        // изменинили параметр для testDepartment1
        newModel.put(ConfigurationParam.FORM_ERROR_DIRECTORY, testDepartment1.getId(),
                asList("file://errorFolder2/"));
        // удалили параметр для testDepartment1
        //newModel.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, testDepartment1.getId(),
        //        asList("file://archiveFolder/"));

        //добавили параметр для testDepartment2
        newModel.put(ConfigurationParam.FORM_ERROR_DIRECTORY, testDepartment2.getId(),
                asList("file://errorFolder/"));

        List<Map<String, String>> emailValues = new ArrayList<Map<String, String>>();
        Map<String, String> config1 = new HashMap<String, String>();
        config1.put("NAME", "test.test.p1");
        config1.put("VALUE", "test");
        emailValues.add(config1);
        Map<String, String> config2 = new HashMap<String, String>();
        config2.put("NAME", "test.test.p2");
        config2.put("VALUE", "test");
        emailValues.add(config2);

        List<Map<String, String>> asyncValues = new ArrayList<Map<String, String>>();
        Map<String, String> configAsync1 = new HashMap<String, String>();
        configAsync1.put("ID", "1");
        configAsync1.put("NAME", "Task1");
        configAsync1.put("SHORT_QUEUE_LIMIT", "100");
        configAsync1.put("TASK_LIMIT", "1000");
        asyncValues.add(configAsync1);
        Map<String, String> configAsync2 = new HashMap<String, String>();
        configAsync2.put("ID", "2");
        configAsync2.put("NAME", "Task2");
        configAsync2.put("SHORT_QUEUE_LIMIT", "100");
        configAsync2.put("TASK_LIMIT", "550");
        asyncValues.add(configAsync2);

        // Сохраняем изменения
        TAUserInfo userInfo = getUser();
        reset(auditService);
        ((ConfigurationServiceImpl) service).saveAndLog(newModel, emailValues, asyncValues, userInfo);
        // Проверяем, записали ли в ЖА
        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null,
                "Общие параметры. Удален параметр \"Путь к библиотеке подписи\":\"keyFileUrl;keyFileUrl2\"", null);
        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null,
                "Общие параметры. Изменён параметр \"Путь к файлу/каталогу ключей ЭП (БОК)\":\"keyFileUrl;keyFileUrl2\" -> \"newKeyFileUrl\"", null);
        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null,
                "Общие параметры. Добавлен параметр \"Проверять ЭП (1 - проверять, 0 - не проверять)\":\"1\"", null);

        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                testDepartment1.getId(), null, null, null, null,
                "Параметры загрузки налоговых форм. Удален параметр \"Путь к каталогу архива\":\"file://archiveFolder/\"", null);
        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                testDepartment1.getId(), null, null, null, null,
                "Параметры загрузки налоговых форм. Изменён параметр \"Путь к каталогу ошибок\":\"file://errorFolder/\" -> \"file://errorFolder2/\"", null);
        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                testDepartment2.getId(), null, null, null, null,
                "Параметры загрузки налоговых форм. Добавлен параметр \"Путь к каталогу ошибок\":\"file://errorFolder/\"", null);

        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null,
                "Электронная почта. Изменён параметр \"test.test.p2\":\"tes_\" -> \"test\"", null);

        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null,
                "Параметры асинхронных заданий. Изменён параметр \"TASK_LIMIT\" для задания \"Task2\":\"500\" -> \"550\"", null);
        verify(auditService).add(FormDataEvent.EDIT_CONFIG_PARAMS, userInfo,
                userInfo.getUser().getDepartmentId(), null, null, null, null,
                "Параметры асинхронных заданий. Изменён параметр \"SHORT_QUEUE_LIMIT\" для задания \"Task2\":\"10\" -> \"100\"", null);

        verifyNoMoreInteractions(auditService);
    }


    /**
     * Пользователь с необходимыми полномочиями
     */
    private TAUserInfo getUser() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setDepartmentId(1);
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_ADMIN);
        user.setRoles(asList(role));
        return userInfo;
    }
}
