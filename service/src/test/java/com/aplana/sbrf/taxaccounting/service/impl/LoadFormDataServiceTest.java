package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Dmitriy Levykin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("LoadFormDataServiceTest.xml")
public class LoadFormDataServiceTest {

    @Autowired
    private LoadFormDataService loadFormDataService;
    @Autowired
    private  DepartmentService departmentService;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private FormDataDao formDataDao;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private SignService signService;

    private static final List<Integer> DEPARTMENT_LIST = Arrays.asList(1, 2, 3, 4, 5);
    private static String FILE_NAME_1 = "____852-4______________147212014__.rnu";
    private static String FILE_NAME_2 = "__________________________________.rnu";
    private static String FILE_NAME_3 = "____852-1______________147212014__.rnu";
    private static String FILE_NAME_4 = "____852-4______________997212014__.rnu";
    private static String FILE_NAME_5 = "____852-4______________999002014__.rnu";
    private static final TAUserInfo USER_INFO = new TAUserInfo();
    private static final TAUserInfo SYSTEM_INFO = new TAUserInfo();
    private TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File uploadFolder;
    private File archiveFolder;
    private File errorFolder;

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
        mockDepartmentService();
        mockAuditService();
        temporaryFolder.create();
        uploadFolder = temporaryFolder.newFolder(ConfigurationParam.FORM_UPLOAD_DIRECTORY.name());
        archiveFolder = temporaryFolder.newFolder(ConfigurationParam.FORM_ARCHIVE_DIRECTORY.name());
        errorFolder = temporaryFolder.newFolder(ConfigurationParam.FORM_ERROR_DIRECTORY.name());
        mockConfigurationDao();
        mockFormTypeService();
        mockPeriodService();
        mockDepartmentFormTypeDao();
        mockFormTemplateService();
        mockFormDataDao();
        mockLockCoreService();
        mockFormDataService();
        mockSignService();
    }

    @After
    public void clean() throws IOException {
        uploadFolder.delete();
        archiveFolder.delete();
        errorFolder.delete();
        temporaryFolder.delete();
    }

    private void mockDepartmentService() {
        Department department147 = new Department();
        department147.setId(147);
        department147.setName("147");
        when(departmentService.getTBDepartmentIds(any(TAUser.class))).thenReturn(DEPARTMENT_LIST);
        when(departmentService.getDepartmentBySbrfCode("147")).thenReturn(department147);
        when(departmentService.getDepartment(anyInt())).thenReturn(department147);
    }

    private void mockAuditService() {
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                System.out.println(invocation);
//                return null;
//            }
//        }).when(auditService).add(any(FormDataEvent.class), any(TAUserInfo.class), any(Integer.class), any(Integer.class),
//                anyString(), anyString(), any(Integer.class), anyString());
        //ReflectionTestUtils.setField(loadFormDataService, "auditService", auditService);
    }

    private void mockFormTypeService() {
        FormType formType852_4 = new FormType();
        formType852_4.setId(1);
        formType852_4.setName("852_4");
        formType852_4.setTaxType(TaxType.INCOME);
        when(formTypeService.getByCode("852-4")).thenReturn(formType852_4);
    }

    private void mockConfigurationDao() {

        ConfigurationParamModel model = new ConfigurationParamModel();
        for (int departmentId : DEPARTMENT_LIST) {
            model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
            model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, departmentId, Arrays.asList("file://" + archiveFolder.getPath() + "/"));
            model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, departmentId, Arrays.asList("file://" + errorFolder.getPath() + "/"));
        }
        when(configurationDao.getByDepartment(anyInt())).thenReturn(model);
    }

    private void mockPeriodService() {
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName("period");
        reportPeriod.setId(1);
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        taxPeriod.setTaxType(TaxType.INCOME);

        taxPeriod.setYear(2014);
        reportPeriod.setTaxPeriod(taxPeriod);
        when(periodService.getByTaxTypedCodeYear(TaxType.INCOME, "21", 2014)).thenReturn(reportPeriod);
        when(periodService.isActivePeriod(1, 147)).thenReturn(true);
    }

    private void mockDepartmentFormTypeDao() {
        when(departmentFormTypeDao.existAssignedForm(147, 1, FormDataKind.PRIMARY)).thenReturn(true);
    }

    private void mockFormTemplateService() {
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setMonthly(false);
        formTemplate.setName("template");
        formTemplate.setId(1);
        when(formTemplateService.getActiveFormTemplateId(1, 1)).thenReturn(1);
        when(formTemplateService.get(1)).thenReturn(formTemplate);
    }

    private void mockFormDataDao() {
        FormData formData = new FormData();
        formData.setState(WorkflowState.CREATED);
        formData.setId(1L);
        when(formDataDao.find(1, FormDataKind.PRIMARY, 147, 1)).thenReturn(null);
        when(formDataDao.get(1L, false)).thenReturn(formData);
    }

    private void mockLockCoreService() {

    }

    private void mockFormDataService() {
        when(formDataService.createFormData(any(Logger.class), any(TAUserInfo.class), eq(1), eq(147),
                eq(FormDataKind.PRIMARY), any(ReportPeriod.class), any(Integer.class))).thenReturn(1L);
    }

    private void mockSignService() {
        when(signService.checkSign(anyString(), anyInt())).thenReturn(true);
    }

    // Успешный импорт ТФ НФ
    @Test
    public void successImportTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_1);
        file.createNewFile();
        ImportCounter importCounter = loadFormDataService.importFormData(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(1, importCounter.getSuccessCounter());
        Assert.assertEquals(0, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(1, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(0, errorFolder.list().length);
    }

    // Неправильное имя файла
    @Test
    public void wrongNameTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_2);
        file.createNewFile();
        ImportCounter importCounter = loadFormDataService.importFormData(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }

    // Несуществующий код формы
    @Test
    public void wrongFormCodeTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_3);
        file.createNewFile();
        ImportCounter importCounter = loadFormDataService.importFormData(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }

    // Несуществующий код подразделения
    @Test
    public void wrongDepartmentCodeTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_4);
        file.createNewFile();
        ImportCounter importCounter = loadFormDataService.importFormData(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }

    // Несуществующий код периода
    @Test
    public void wrongReportPeriodCodeTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_5);
        file.createNewFile();
        ImportCounter importCounter = loadFormDataService.importFormData(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }

    // Отсутствует назначение
    @Test
    public void wrongNominationTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_1);
        file.createNewFile();

        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        when(departmentFormTypeDao.existAssignedForm(147, 1, FormDataKind.PRIMARY)).thenReturn(false);
        ReflectionTestUtils.setField(loadFormDataService, "departmentFormTypeDao", departmentFormTypeDao);

        ImportCounter importCounter = loadFormDataService.importFormData(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }

    // Период закрыт
    @Test
    public void closePeriodTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_1);
        file.createNewFile();

        PeriodService periodService = mock(PeriodService.class);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName("period");
        reportPeriod.setId(1);
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        taxPeriod.setTaxType(TaxType.INCOME);

        taxPeriod.setYear(2014);
        reportPeriod.setTaxPeriod(taxPeriod);
        when(periodService.getByTaxTypedCodeYear(TaxType.INCOME, "21", 2014)).thenReturn(reportPeriod);
        when(periodService.isActivePeriod(1, 147)).thenReturn(false);
        ReflectionTestUtils.setField(loadFormDataService, "periodService", periodService);

        ImportCounter importCounter = loadFormDataService.importFormData(USER_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }

    // Ошибка в скрипте
    @Test
    public void scriptErrorTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_1);
        file.createNewFile();

        FormDataService formDataService = mock(FormDataService.class);
        when(formDataService.createFormData(any(Logger.class), any(TAUserInfo.class), eq(1), eq(147),
                eq(FormDataKind.PRIMARY), any(ReportPeriod.class), any(Integer.class))).thenReturn(1L);

        doThrow(new RuntimeException("Test RuntimeException")).when(formDataService).importFormData(any(Logger.class), any(TAUserInfo.class),
                any(Long.class), any(InputStream.class), anyString(), any(FormDataEvent.class));

        ReflectionTestUtils.setField(loadFormDataService, "formDataService", formDataService);

        ImportCounter importCounter = loadFormDataService.importFormData(SYSTEM_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }
}
