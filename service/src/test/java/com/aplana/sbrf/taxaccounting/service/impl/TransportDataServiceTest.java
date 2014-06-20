package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.TransportDataService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransportDataServiceTest {

    private static TransportDataService transportDataService = new TransportDataServiceImpl();
    private static String FILE_NAME_1 = "Тестовый файл 1.ууу";
    private static String FILE_NAME_2 = "Тестовый файл 2.zip";
    private static String FILE_NAME_2_EXTRACT_1 = "Тестовый файл 1.txt";
    private static String FILE_NAME_2_EXTRACT_2 = "Тестовый файл 2.txt";

    private static String TEST_PATH = "com/aplana/sbrf/taxaccounting/service/impl/";
    private static File folder;

    @BeforeClass
    public static void init() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        folder = temporaryFolder.getRoot();
        System.out.println("Test folder1 is \"" + folder.getAbsoluteFile() + "\"");
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ConfigurationParamModel model = new ConfigurationParamModel();
        model.put(ConfigurationParam.FORM_DATA_DIRECTORY, asList("file://" + folder.getAbsolutePath() + "/"));
        model.put(ConfigurationParam.REF_BOOK_DIASOFT_DIRECTORY, null);
        model.put(ConfigurationParam.REF_BOOK_KEY_FILE, asList("smb://", "/"));

        when(configurationDao.loadParams()).thenReturn(model);
        ReflectionTestUtils.setField(transportDataService, "configurationDao", configurationDao);
        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(transportDataService, "auditService", auditService);
    }

    // Не задан пользователь
    @Test
    public void uploadFile1Test() throws IOException {
        Logger logger = new Logger();
        transportDataService.uploadFile(null, ConfigurationParam.FORM_DATA_DIRECTORY, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(TransportDataServiceImpl.USER_NOT_FOUND_ERROR, logger.getEntries().get(0).getMessage());
    }

    // Пользователь не имеет нужную роль
    @Test
    public void uploadFile2Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONF);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        transportDataService.uploadFile(userInfo, ConfigurationParam.FORM_DATA_DIRECTORY, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(TransportDataServiceImpl.ACCESS_DENIED_ERROR, logger.getEntries().get(0).getMessage());
    }

    // Не задано имя файла
    @Test
    public void uploadFile3Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        transportDataService.uploadFile(userInfo, ConfigurationParam.FORM_DATA_DIRECTORY, null, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(TransportDataServiceImpl.NO_FILE_NAME_ERROR, logger.getEntries().get(0).getMessage());
    }

    // Не задан поток файла
    @Test
    public void uploadFile4Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        transportDataService.uploadFile(userInfo, ConfigurationParam.FORM_DATA_DIRECTORY, FILE_NAME_1, null, logger);
        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(TransportDataServiceImpl.EMPTY_INPUT_STREAM_ERROR, logger.getEntries().get(0).getMessage());
    }

    // Не указан каталог загрузки
    @Test
    public void uploadFile5Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        transportDataService.uploadFile(userInfo, ConfigurationParam.REF_BOOK_DIASOFT_DIRECTORY, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(TransportDataServiceImpl.NO_CATALOG_ERROR, logger.getEntries().get(0).getMessage());
    }

    // Успешный импорт файла
    @Test
    public void uploadFile6Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        transportDataService.uploadFile(userInfo, ConfigurationParam.FORM_DATA_DIRECTORY, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
        String[] files = folder.list();
        Assert.assertEquals(1, files.length);
        Assert.assertEquals(FILE_NAME_1, files[0]);
    }

    // Успешный импорт архива
    @Test
    public void uploadFile7Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        transportDataService.uploadFile(userInfo, ConfigurationParam.FORM_DATA_DIRECTORY, FILE_NAME_2, getFileAsStream(FILE_NAME_2), logger);
        String[] files = folder.list();
        List<String> fileList = asList(files);
        Assert.assertEquals(3, fileList.size());
        Assert.assertTrue(fileList.contains(FILE_NAME_1));
        Assert.assertTrue(fileList.contains(FILE_NAME_2_EXTRACT_1));
        Assert.assertTrue(fileList.contains(FILE_NAME_2_EXTRACT_2));
    }

    @Test
    public void getWorkFilesFromFolderTest() throws IOException {
        // Подготовка тестового каталога
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        System.out.println("Test folder2 is \"" + temporaryFolder.getRoot().getAbsoluteFile() + "\"");
        // Создание тестовых файлов
        String[] fileNames = {"file1.txt", "file2.doc", "file3.zip", "file4.zip",
                "____852-4______________147212014__.rnu", "1290-40.1______________151222015_6.rnu"};
        List<File> fileList = new LinkedList<File>();
        for (String fileName : fileNames) {
            fileList.add(temporaryFolder.newFile(fileName));
        }
        temporaryFolder.newFolder("folder");
        // Получение «рабочих» (подходящих файлов)
        List<String> result = transportDataService.getWorkFilesFromFolder(temporaryFolder.getRoot().getAbsolutePath() + "/");
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(fileNames[4]));
        Assert.assertTrue(result.contains(fileNames[5]));
        temporaryFolder.delete();
    }

    @Test
    public void importDataFromFolderTest() {
        // TODO Подготовить тесты после реализации в сервисе
    }

    private static InputStream getFileAsStream(String fileName) {
        return TransportDataServiceTest.class.getClassLoader().getResourceAsStream(TEST_PATH + fileName);
    }
}
