package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        when(configurationDao.fetchAllAsModel()).thenReturn(model);

        DepartmentDao departmentDao = mock(DepartmentDao.class);
        when(departmentDao.getDepartment(eq(1))).thenReturn(testDepartment1);
        when(departmentDao.getDepartment(eq(2))).thenReturn(testDepartment2);
        ReflectionTestUtils.setField(service, "departmentDao", departmentDao);

        RefBookFactory refBookFactory = mock(RefBookFactory.class);

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

        ReflectionTestUtils.setField(service, "refBookFactory", refBookFactory);

        ReflectionTestUtils.setField(service, "auditService", auditService);

        RefBookDataProvider providerTax = mock(RefBookDataProvider.class);
        when(refBookFactory.getDataProvider(RefBook.Id.TAX_INSPECTION.getId())).thenReturn(providerTax);
        when(providerTax.getRecordsCount(any(Date.class), eq("code = '0'"))).thenReturn(0);
        when(providerTax.getRecordsCount(any(Date.class), eq("code = '1'"))).thenReturn(1);
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

        service.checkFileSystemAccess(getUser(), model, logger);

        file.delete();
        commonFolder.delete();
        uploadFolder.delete();
        archiveFolder.delete();
        errorFolder.delete();

        Assert.assertEquals(14, logger.getEntries().size());
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

        service.checkFileSystemAccess(getUser(), model, logger);

        file.delete();
        commonFolder.delete();
        uploadFolder.delete();
        archiveFolder.delete();
        errorFolder.delete();

        Assert.assertEquals(14, logger.getEntries().size());
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
        Assert.assertTrue(logger.containsLevel(LogLevel.INFO));
    }

    @Test
    public void checkCommonConfigurationParamsValidTest() {
        Map<ConfigurationParam, String> paramMap = new HashMap<ConfigurationParam, String>();
        paramMap.put(ConfigurationParam.NO_CODE, "1");
        paramMap.put(ConfigurationParam.SBERBANK_INN, "7707083893");

        Logger logger = new Logger();
        service.checkCommonConfigurationParams(paramMap, logger);

        Assert.assertTrue(logger.getEntries().isEmpty());
    }

    @Test
    public void checkCommonConfigurationParamsInvalidTest() {
        Map<ConfigurationParam, String> paramMap = new HashMap<ConfigurationParam, String>();
        paramMap.put(ConfigurationParam.NO_CODE, "2");
        paramMap.put(ConfigurationParam.SBERBANK_INN, "7707083899");

        Logger logger = new Logger();
        service.checkCommonConfigurationParams(paramMap, logger);

        Assert.assertEquals(2, logger.getEntries().size());
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
