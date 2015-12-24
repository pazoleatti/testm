package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * @author Dmitriy Levykin
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("UploadTransportDataServiceTest.xml")
public class UploadTransportDataServiceTest {
    private static String FILE_NAME_1 = "1290-39.2_______18_0000_00212014_1.rnu"; // Mock как справочник
    private static String FILE_NAME_2 = "Тестовый файл 2.zip"; // Архив
    private static String FILE_NAME_3 = "____101-1______________147212014__.rnu"; // Архив
    private static String FILE_NAME_2_EXTRACT_1 = "____852-4______________147212014__.rnu"; // ТФ НФ
    private static String FILE_NAME_2_EXTRACT_2 = "Тестовый файл 2.txt"; // Mock как неподходящий файл

    private static String TEST_PATH = "com/aplana/sbrf/taxaccounting/service/impl/";
    private static File folder;

    private static final int TEST_DEPARTMENT_ID = 0;

    private static TemporaryFolder temporaryFolder;

    @Autowired
    ConfigurationDao configurationDao;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    AuditService auditService;
    @Autowired
    LoadRefBookDataService loadRefBookDataService;
    @Autowired
    FormTypeService formTypeService;
    @Autowired
    PeriodService periodService;
    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    UploadTransportDataService uploadTransportDataService;
    @Autowired
    FormTemplateService formTemplateService;

    @Before
    public void init() throws IOException {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        folder = temporaryFolder.getRoot();
        ConfigurationParamModel model = new ConfigurationParamModel();
        model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/"));
        model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, null);
        model.put(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/"));
        model.put(ConfigurationParam.KEY_FILE, TEST_DEPARTMENT_ID, asList("smb://", "/"));
        model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, TEST_DEPARTMENT_ID, asList("file://" + folder.getPath() + "/error/", "smb://"));

        when(configurationDao.getAll()).thenReturn(model);
        when(configurationDao.getByDepartment(TEST_DEPARTMENT_ID)).thenReturn(model);

        when(loadRefBookDataService.isDiasoftFile(anyString())).thenAnswer(new Answer<Object>() {
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

        final Department formDepartment = new Department();
        formDepartment.setId(TEST_DEPARTMENT_ID);
        formDepartment.setName("TestDepartment");

        when(departmentService.getDepartment(anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                int departmentId = (Integer) invocation.getArguments()[0];
                if (departmentId == TEST_DEPARTMENT_ID) {
                    return formDepartment;
                } else {
                    return null;
                }
            }
        });
        when(departmentService.getTaxFormDepartments(any(TAUser.class), anyListOf(TaxType.class), any(Date.class), any(Date.class)))
                .thenReturn(Arrays.asList(TEST_DEPARTMENT_ID));
        when(departmentService.getDepartmentBySbrfCode("147")).thenReturn(formDepartment);
        when(departmentService.getParentTB(TEST_DEPARTMENT_ID)).thenReturn(formDepartment);

        FormType formType852_4 = new FormType();
        formType852_4.setId(1);
        formType852_4.setTaxType(TaxType.INCOME);
        formType852_4.setName("Test form type 852-4");
        when(formTypeService.getByCode("852-4")).thenReturn(formType852_4);

        FormType formType101_1 = new FormType();
        formType101_1.setId(2);
        formType101_1.setTaxType(TaxType.DEAL);
        formType101_1.setName("Test form type 101.1");
        when(formTypeService.getByCode("101-1")).thenReturn(formType101_1);

        ReportPeriod reportPeriod21 = new ReportPeriod();
        reportPeriod21.setId(1);
        reportPeriod21.setName("Test period");
        when(periodService.getByTaxTypedCodeYear(TaxType.INCOME, "21", 2014)).thenReturn(reportPeriod21);
        when(periodService.getByTaxTypedCodeYear(TaxType.DEAL, "21", 2014)).thenReturn(reportPeriod21);

        when(departmentFormTypeDao.existAssignedForm(TEST_DEPARTMENT_ID, formType852_4.getId(), FormDataKind.PRIMARY)).thenReturn(true);
        when(departmentFormTypeDao.existAssignedForm(TEST_DEPARTMENT_ID, formType101_1.getId(), FormDataKind.PRIMARY)).thenReturn(true);

        FormTemplate ft = new FormTemplate();
        ft.setScript("case FormDataEvent."+FormDataEvent.IMPORT_TRANSPORT_FILE.name() + ":");
        FormTemplate ft2 = new FormTemplate();
        ft2.setScript("case FormDataEvent."+FormDataEvent.CALCULATE.name() + ":");

        when(formTemplateService.existFormTemplate(any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(true);
        when(formTemplateService.getActiveFormTemplateId(eq(1), any(Integer.class))).thenReturn(1);
        when(formTemplateService.getActiveFormTemplateId(eq(2), any(Integer.class))).thenReturn(2);
        when(formTemplateService.get(eq(1), any(Logger.class))).thenReturn(ft);
        when(formTemplateService.get(eq(2), any(Logger.class))).thenReturn(ft2);
    }

    @AfterClass
    public static void clean() {
        temporaryFolder.delete();
    }

    // Не задан пользователь
    @Test
    public void uploadFile1Test() throws IOException {
        when(loadRefBookDataService.isDiasoftFile(FILE_NAME_1)).thenReturn(true);

        Logger logger = new Logger();
        UploadResult uploadResult = uploadTransportDataService.uploadFile(null, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(0, uploadResult.getSuccessCounter());
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(UploadTransportDataServiceImpl.USER_NOT_FOUND_ERROR, logger.getEntries().get(0).getMessage());
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
        UploadResult uploadResult = uploadTransportDataService.uploadFile(userInfo, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(0, uploadResult.getSuccessCounter());
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(UploadTransportDataServiceImpl.ACCESS_DENIED_ERROR, logger.getEntries().get(0).getMessage());
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
        UploadResult uploadResult = uploadTransportDataService.uploadFile(userInfo, null, getFileAsStream(FILE_NAME_1), logger);
        Assert.assertEquals(0, uploadResult.getSuccessCounter());
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(UploadTransportDataServiceImpl.NO_FILE_NAME_ERROR, logger.getEntries().get(0).getMessage());
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
        UploadResult uploadResult = uploadTransportDataService.uploadFile(userInfo, FILE_NAME_1, null,  logger);
        Assert.assertEquals(0, uploadResult.getSuccessCounter());
        Assert.assertEquals(2, logger.getEntries().size());
        Assert.assertEquals(LogLevel.ERROR, logger.getEntries().get(0).getLevel());
        Assert.assertEquals(UploadTransportDataServiceImpl.EMPTY_INPUT_STREAM_ERROR, logger.getEntries().get(0).getMessage());
    }

    // Успешный импорт файла справочника
    @Test
    public void uploadFile5Test() throws IOException {
        try {
            TAUserInfo userInfo = new TAUserInfo();
            TAUser user = new TAUser();
            userInfo.setUser(user);
            TARole role = new TARole();
            role.setAlias(TARole.ROLE_CONTROL_UNP);
            user.setRoles(asList(role));
            Logger logger = new Logger();
            UploadResult uploadResult = uploadTransportDataService.uploadFile(userInfo, FILE_NAME_1, getFileAsStream(FILE_NAME_1), logger);
            String[] files = folder.list();
            Assert.assertEquals(1, files.length);
            Assert.assertEquals(uploadResult.getDiasoftFileNameList().get(0), FILE_NAME_1);
            Assert.assertEquals(FILE_NAME_1, files[0]);
            new File(folder.getPath() + '/' + FILE_NAME_1).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        uploadTransportDataService.uploadFile(userInfo, FILE_NAME_2, getFileAsStream(FILE_NAME_2), logger);
        String[] files = folder.list();
        Assert.assertTrue(files != null && files.length != 0);
        List<String> fileList = asList(files);
        Assert.assertTrue(fileList.contains(FILE_NAME_2_EXTRACT_1));
    }

    // Не успешный импорт архива НФ
    @Test
    public void uploadFile7Test() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        userInfo.setUser(user);
        TARole role = new TARole();
        role.setAlias(TARole.ROLE_CONTROL_UNP);
        user.setRoles(asList(role));
        Logger logger = new Logger();
        uploadTransportDataService.uploadFile(userInfo, FILE_NAME_3, getFileAsStream(FILE_NAME_3), logger);
        Assert.assertEquals("Для налоговой формы загружаемого файла \"" + FILE_NAME_3 + "\" не предусмотрена обработка транспортного файла! Загрузка не выполнена.", logger.getEntries().get(3).getMessage());
    }

    @Test
    public void importDataFromFolderTest() {
        // Не реализуется, т.к. логика сложная и сильно завязана на другие сервисы
    }

    private static InputStream getFileAsStream(String fileName) {
        return UploadTransportDataServiceTest.class.getClassLoader().getResourceAsStream(TEST_PATH + fileName);
    }
}
