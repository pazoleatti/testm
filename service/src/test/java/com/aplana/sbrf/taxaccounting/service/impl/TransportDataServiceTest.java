package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;
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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransportDataServiceTest {
    private static TransportDataService transportDataService = new TransportDataServiceImpl();
    private static String FILE_NAME_1 = "Тестовый файл 1.ууу"; // Mock как справочник
    private static String FILE_NAME_2 = "Тестовый файл 2.zip"; // Архив
    private static String FILE_NAME_2_EXTRACT_1 = "____852-4______________147212014__.rnu"; // ТФ НФ
    private static String FILE_NAME_2_EXTRACT_2 = "Тестовый файл 2.txt"; // Mock как неподходящий файл

    private static String TEST_PATH = "com/aplana/sbrf/taxaccounting/service/impl/";
    private static File folder;

    private static final int TEST_DEPARTMENT_ID = DepartmentType.ROOT_BANK.getCode();

    private static TemporaryFolder temporaryFolder;

    @BeforeClass
    public static void init() throws IOException {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        folder = temporaryFolder.getRoot();
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ConfigurationParamModel model = new ConfigurationParamModel();
        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, null);
        model.put(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/"));
        model.put(ConfigurationParam.KEY_FILE, TEST_DEPARTMENT_ID, asList("smb://", "/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/error/", "smb://"));

        when(configurationDao.getAll()).thenReturn(model);
        ReflectionTestUtils.setField(transportDataService, "configurationDao", configurationDao);
        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(transportDataService, "auditService", auditService);

        RefBookExternalService refBookExternalService = mock(RefBookExternalService.class);
        when(refBookExternalService.isDiasoftFile(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String name = (String) invocation.getArguments()[0];
                if (FILE_NAME_1.equals(name)) {
                    return true;
                }
                if (FILE_NAME_2_EXTRACT_1.equals(name)) {
                    return false;
                }
                if (FILE_NAME_2_EXTRACT_2.equals(name)) {
                    return false;
                }
                return false;
            }
        });
        ReflectionTestUtils.setField(transportDataService, "refBookExternalService", refBookExternalService);

        final Department formDepartment = new Department();
        formDepartment.setId(1);
        formDepartment.setName("TestDepartment");

        DepartmentService departmentService = mock(DepartmentService.class);
        when(departmentService.getDepartment(anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                int departmentId = (Integer) invocation.getArguments()[0];
                if (departmentId == 1) {
                    return formDepartment;
                } else {
                    return null;
                }
            }
        });
        when(departmentService.getDepartmentByCode(147)).thenReturn(formDepartment);
        ReflectionTestUtils.setField(transportDataService, "departmentService", departmentService);

        FormType formType852_4 = new FormType();
        formType852_4.setId(1);
        formType852_4.setTaxType(TaxType.INCOME);
        formType852_4.setName("Test form type 852-4");
        FormTypeService formTypeService = mock(FormTypeService.class);
        when(formTypeService.getByCode("852-4")).thenReturn(formType852_4);
        ReflectionTestUtils.setField(transportDataService, "formTypeService", formTypeService);

        PeriodService periodService = mock(PeriodService.class);
        ReportPeriod reportPeriod21 = new ReportPeriod();
        reportPeriod21.setId(1);
        reportPeriod21.setName("Test period");
        when(periodService.getByTaxTypedCodeYear(TaxType.INCOME, "21", 2014)).thenReturn(reportPeriod21);
        ReflectionTestUtils.setField(transportDataService, "periodService", periodService);

        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        when(departmentFormTypeDao.existAssignedForm(1, 1, FormDataKind.PRIMARY)).thenReturn(true);
        ReflectionTestUtils.setField(transportDataService, "departmentFormTypeDao", departmentFormTypeDao);
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
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(1).getLevel());
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
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(1).getLevel());
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
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(1).getLevel());
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
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(1).getLevel());
        Assert.assertEquals(TransportDataServiceImpl.EMPTY_INPUT_STREAM_ERROR, logger.getEntries().get(0).getMessage());
    }

    // Успешный импорт файла справочника
    @Test
    public void uploadFile5Test() throws IOException {
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
        new File(folder.getPath() + '/' + FILE_NAME_1).delete();
    }

    // Успешный импорт архива НФ
    @Test
    public void uploadFile6Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        transportDataService.uploadFile(userInfo, 1, FILE_NAME_2, getFileAsStream(FILE_NAME_2), logger);
        String[] files = folder.list();
        Assert.assertTrue(files != null && files.length != 0);
        List<String> fileList = asList(files);
        Assert.assertTrue(fileList.contains(FILE_NAME_2_EXTRACT_1));
    }

    @Test
    public void getWorkFilesFromFolderTest() throws IOException {
        // Подготовка тестового каталога
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
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

        File dstFolder = new File(folder.getPath() + "/error/" + calendar.get(Calendar.YEAR) + '/'
                + Months.fromId(calendar.get(Calendar.MONTH)).getName() + '/'
                + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + '/');

        Assert.assertTrue(dstFolder.exists());
        List<String> fileNameList = asList(dstFolder.list());
        Assert.assertEquals(1, fileNameList.size());
        Assert.assertTrue(fileNameList.get(0).endsWith(".zip"));
        File srcFolder = new File(temporaryFolder.getRoot().getPath());
        Assert.assertEquals(0, srcFolder.list().length);
        FileUtils.deleteDirectory(new File(folder.getPath() + "/error/"));
        temporaryFolder.delete();
    }

    private static InputStream getFileAsStream(String fileName) {
        return TransportDataServiceTest.class.getClassLoader().getResourceAsStream(TEST_PATH + fileName);
    }
}
