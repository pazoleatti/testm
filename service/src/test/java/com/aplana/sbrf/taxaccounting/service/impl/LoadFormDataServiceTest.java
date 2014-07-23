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
public class LoadFormDataServiceTest {

    private LoadFormDataService service;

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
        service = new LoadFormDataServiceImpl();
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
    }

    @After
    public void clean() throws IOException {
        uploadFolder.delete();
        archiveFolder.delete();
        errorFolder.delete();
        temporaryFolder.delete();
    }

    private void mockDepartmentService() {
        DepartmentService departmentService = mock(DepartmentService.class);
        Department department147 = new Department();
        department147.setId(147);
        department147.setName("147");
        when(departmentService.getTBDepartmentIds(any(TAUser.class))).thenReturn(DEPARTMENT_LIST);
        when(departmentService.getDepartmentByCode(147)).thenReturn(department147);
        when(departmentService.getDepartment(anyInt())).thenReturn(department147);
        ReflectionTestUtils.setField(service, "departmentService", departmentService);
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

    private void mockFormTypeService() {
        FormType formType852_4 = new FormType();
        formType852_4.setId(1);
        formType852_4.setName("852_4");
        formType852_4.setTaxType(TaxType.INCOME);
        FormTypeService formTypeService = mock(FormTypeService.class);
        when(formTypeService.getByCode("852-4")).thenReturn(formType852_4);
        ReflectionTestUtils.setField(service, "formTypeService", formTypeService);
    }

    private void mockConfigurationDao() {
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        ConfigurationParamModel model = new ConfigurationParamModel();
        for (int departmentId : DEPARTMENT_LIST) {
            model.put(ConfigurationParam.FORM_UPLOAD_DIRECTORY, departmentId, Arrays.asList("file://" + uploadFolder.getPath() + "/"));
            model.put(ConfigurationParam.FORM_ARCHIVE_DIRECTORY, departmentId, Arrays.asList("file://" + archiveFolder.getPath() + "/"));
            model.put(ConfigurationParam.FORM_ERROR_DIRECTORY, departmentId, Arrays.asList("file://" + errorFolder.getPath() + "/"));
        }
        when(configurationDao.getByDepartment(anyInt())).thenReturn(model);
        ReflectionTestUtils.setField(service, "configurationDao", configurationDao);
    }

    private void mockPeriodService() {
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
        when(periodService.isActivePeriod(1, 147)).thenReturn(true);
        ReflectionTestUtils.setField(service, "periodService", periodService);
    }

    private void mockDepartmentFormTypeDao() {
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        when(departmentFormTypeDao.existAssignedForm(147, 1, FormDataKind.PRIMARY)).thenReturn(true);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);
    }

    private void mockFormTemplateService() {
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setMonthly(false);
        formTemplate.setName("template");
        formTemplate.setId(1);
        FormTemplateService formTemplateService = mock(FormTemplateService.class);
        when(formTemplateService.getActiveFormTemplateId(1, 1)).thenReturn(1);
        when(formTemplateService.get(1)).thenReturn(formTemplate);
        ReflectionTestUtils.setField(service, "formTemplateService", formTemplateService);
    }

    private void mockFormDataDao() {
        FormData formData = new FormData();
        formData.setState(WorkflowState.CREATED);
        formData.setId(1L);
        FormDataDao formDataDao = mock(FormDataDao.class);
        when(formDataDao.find(1, FormDataKind.PRIMARY, 147, 1)).thenReturn(null);
        when(formDataDao.get(1L, false)).thenReturn(formData);
        ReflectionTestUtils.setField(service, "formDataDao", formDataDao);
    }

    private void mockLockCoreService() {
        LockCoreService lockCoreService = mock(LockCoreService.class);
        ReflectionTestUtils.setField(service, "lockCoreService", lockCoreService);
    }

    private void mockFormDataService() {
        FormDataService formDataService = mock(FormDataService.class);
        when(formDataService.createFormData(any(Logger.class), any(TAUserInfo.class), eq(1), eq(147),
                eq(FormDataKind.PRIMARY), any(ReportPeriod.class), any(Integer.class))).thenReturn(1L);
        ReflectionTestUtils.setField(service, "formDataService", formDataService);
    }

    // Успешный импорт ТФ НФ
    @Test
    public void successImportTest() throws IOException {
        File file = new File(uploadFolder.getPath() + "/" + FILE_NAME_1);
        file.createNewFile();
        ImportCounter importCounter = service.importFormData(USER_INFO, new Logger());
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
        ImportCounter importCounter = service.importFormData(USER_INFO, new Logger());
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
        ImportCounter importCounter = service.importFormData(USER_INFO, new Logger());
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
        ImportCounter importCounter = service.importFormData(USER_INFO, new Logger());
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
        ImportCounter importCounter = service.importFormData(USER_INFO, new Logger());
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
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);

        ImportCounter importCounter = service.importFormData(USER_INFO, new Logger());
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
        ReflectionTestUtils.setField(service, "periodService", periodService);

        ImportCounter importCounter = service.importFormData(USER_INFO, new Logger());
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

        ReflectionTestUtils.setField(service, "formDataService", formDataService);

        ImportCounter importCounter = service.importFormData(SYSTEM_INFO, new Logger());
        // Счетчики
        Assert.assertEquals(0, importCounter.getSuccessCounter());
        Assert.assertEquals(1, importCounter.getFailCounter());
        // Архив
        Assert.assertEquals(0, archiveFolder.list().length);
        // Ошибки
        Assert.assertEquals(1, errorFolder.list().length);
    }
}
