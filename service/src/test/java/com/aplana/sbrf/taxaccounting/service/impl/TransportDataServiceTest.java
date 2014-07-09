package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.service.TransportDataService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
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

    private static final int TEST_DEPARTMENT_ID = DepartmentType.ROOT_BANK.getCode();

    private static TemporaryFolder temporaryFolder;

    @BeforeClass
    public static void init() throws IOException {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        folder = temporaryFolder.getRoot();
        System.out.println("Test common folder is \"" + folder.getAbsolutePath() + "\"");
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ConfigurationParamModel model = new ConfigurationParamModel();
        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, null);
        model.put(ConfigurationParam.KEY_FILE, TEST_DEPARTMENT_ID, asList("smb://", "/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/", "smb://"));

        when(configurationDao.getAll()).thenReturn(model);
        ReflectionTestUtils.setField(transportDataService, "configurationDao", configurationDao);
        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(transportDataService, "auditService", auditService);

        RefBookExternalService refBookExternalService = mock(RefBookExternalService.class);
        when(refBookExternalService.isDiasoftFile(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String name = (String)invocation.getArguments()[0];
                if (FILE_NAME_1.equals(name)) {
                    return true;
                }
                if (FILE_NAME_2_EXTRACT_1.equals(name)) {
                    return true;
                }
                if (FILE_NAME_2_EXTRACT_2.equals(name)) {
                    return true;
                }
                return null;
            }
        });
        ReflectionTestUtils.setField(transportDataService, "refBookExternalService", refBookExternalService);
    }

    @AfterClass
    public static void clean() {
        temporaryFolder.delete();
    }

    // Не задан пользователь
    @Test
    public void uploadFile1Test() throws IOException {
        Logger logger = new Logger();
        transportDataService.uploadFile(null, 1, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
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
        transportDataService.uploadFile(userInfo, 1, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
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
        transportDataService.uploadFile(userInfo, 1, null, getFileAsStream(FILE_NAME_1), logger);
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
        transportDataService.uploadFile(userInfo, 1, FILE_NAME_1, null, logger);
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
        transportDataService.uploadFile(userInfo, 2, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(1, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(TransportDataServiceImpl.NO_CATALOG_UPLOAD_ERROR, logger.getEntries().get(0).getMessage());
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
        transportDataService.uploadFile(userInfo, 1, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
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
        transportDataService.uploadFile(userInfo, 1, FILE_NAME_2, getFileAsStream(FILE_NAME_2), logger);
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
        System.out.println("Test src folder getWorkFilesFromFolderTest is \"" + temporaryFolder.getRoot().getAbsoluteFile() + "\"");
        // Создание тестовых файлов
        String[] fileNames = {"file1.txt", "file2.doc", "file3.zip", "file4.zip",
                "____852-4______________147212014__.rnu", "1290-40.1______________151222015_6.rnu"};
        List<File> fileList = new LinkedList<File>();
        for (String fileName : fileNames) {
            fileList.add(temporaryFolder.newFile(fileName));
        }
        temporaryFolder.newFolder("folder");
        // Получение «рабочих» (подходящих файлов)
        List<String> result = transportDataService.getWorkFilesFromFolder(temporaryFolder.getRoot().getPath() + "/");
        temporaryFolder.delete();
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(fileNames[4]));
        Assert.assertTrue(result.contains(fileNames[5]));
    }

    @Test
    public void importDataFromFolderTest() {
        // Не реализуется, т.к. логика сложная и сильно завязана на другие сервисы
    }

    @Test
    public void moveToErrorDirectoryTest() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        System.out.println("Test src folder moveToErrorDirectoryTest is \"" + temporaryFolder.getRoot().getAbsolutePath() + "\"");
        FileWrapper errorFile = new FileWrapper(temporaryFolder.newFile("Тестовый файл.rnu"));
        Logger logger = new Logger();
        logger.error("Тестовая ошибка!");
        logger.warn("Тестовое предупреждение!");
        logger.info("Тестовое сообщение!");

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        ((TransportDataServiceImpl) transportDataService).moveToErrorDirectory(errorFile, userInfo, logger);

        File dstFolder = new File(folder.getPath() + "/" + calendar.get(Calendar.YEAR) + "/"
                + Months.fromId(calendar.get(Calendar.MONTH)).getName() + "/"
                + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "/");

        Assert.assertTrue(dstFolder.exists());
        List<String> fileNameList = asList(dstFolder.list());
        Assert.assertEquals(1, fileNameList.size());
        Assert.assertTrue(fileNameList.get(0).endsWith(".zip"));
        File srcFolder = new File(temporaryFolder.getRoot().getPath());
        Assert.assertEquals(0, srcFolder.list().length);
        temporaryFolder.delete();
    }

    private static InputStream getFileAsStream(String fileName) {
        return TransportDataServiceTest.class.getClassLoader().getResourceAsStream(TEST_PATH + fileName);
    }
}
