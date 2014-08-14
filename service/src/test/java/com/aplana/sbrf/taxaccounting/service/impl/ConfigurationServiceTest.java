package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Dmitriy Levykin
 */
public class ConfigurationServiceTest {

    private final static ConfigurationService service = new ConfigurationServiceImpl();
    private final static Department testDepartment1 = new Department();
    private final static Department testDepartment2 = new Department();
    static {
        testDepartment1.setId(1);
        testDepartment1.setName("Подразделение один");
        testDepartment2.setId(2);
        testDepartment2.setName("Подразделение два");
    }

    @BeforeClass
    public static void init() {
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ReflectionTestUtils.setField(service, "configurationDao", configurationDao);

        DepartmentDao departmentDao = mock(DepartmentDao.class);
        when(departmentDao.getDepartment(eq(1))).thenReturn(testDepartment1);
        when(departmentDao.getDepartment(eq(2))).thenReturn(testDepartment2);
        ReflectionTestUtils.setField(service, "departmentDao", departmentDao);
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
        service.saveAllConfig(userInfo, new ConfigurationParamModel(), logger);
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
            if (param.isCommon()) {
                if (param.isFolder()) {
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

        service.saveAllConfig(getUser(), model, logger);

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
            if (param.isCommon()) {
                if (param.isFolder()) {
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

        service.saveAllConfig(getUser(), model, logger);

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
            if (param.isCommon()) {
                if (param.isFolder()) {
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

        service.saveAllConfig(getUser(), model, logger);

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
        service.saveAllConfig(getUser(), model, logger);

        Assert.assertEquals(10, logger.getEntries().size());
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
            if (param.isCommon()) {
                if (param.isFolder()) {
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

        Assert.assertEquals(11, logger.getEntries().size());
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
            if (param.isCommon()) {
                if (param.isFolder()) {
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

        Assert.assertEquals(11, logger.getEntries().size());
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
        Assert.assertTrue(logger.containsLevel(LogLevel.INFO));
    }

    /**
     * Пользователь с необходимыми полномочиями
     */
    private TAUserInfo getUser() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_ADMIN);
        user.setRoles(asList(role));
        return userInfo;
    }
}
