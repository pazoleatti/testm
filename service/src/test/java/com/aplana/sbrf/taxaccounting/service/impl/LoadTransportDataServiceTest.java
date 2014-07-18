package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Dmitriy Levykin
 */
public class LoadTransportDataServiceTest {

    private static LoadTransportDataService uploadTransportDataService = new LoadTransportDataServiceImpl();
    private static String FILE_NAME_1 = "Тестовый файл 1.ууу"; // Mock как справочник
    private static String FILE_NAME_2 = "Тестовый файл 2.zip"; // Архив
    private static String FILE_NAME_2_EXTRACT_1 = "____852-4______________147212014__.rnu"; // ТФ НФ
    private static String FILE_NAME_2_EXTRACT_2 = "Тестовый файл 2.txt"; // Mock как неподходящий файл

    private static String TEST_PATH = "com/aplana/sbrf/taxaccounting/service/impl/";
    private static File folder;

    private static final int TEST_DEPARTMENT_ID = 0;

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
        when(configurationDao.getByDepartment(TEST_DEPARTMENT_ID)).thenReturn(model);
        ReflectionTestUtils.setField(uploadTransportDataService, "configurationDao", configurationDao);
        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(uploadTransportDataService, "auditService", auditService);

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
        ReflectionTestUtils.setField(uploadTransportDataService, "refBookExternalService", refBookExternalService);

        final Department formDepartment = new Department();
        formDepartment.setId(TEST_DEPARTMENT_ID);
        formDepartment.setName("TestDepartment");

        DepartmentService departmentService = mock(DepartmentService.class);
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
        when(departmentService.getDepartmentByCode(147)).thenReturn(formDepartment);
        ReflectionTestUtils.setField(uploadTransportDataService, "departmentService", departmentService);

        FormType formType852_4 = new FormType();
        formType852_4.setId(1);
        formType852_4.setTaxType(TaxType.INCOME);
        formType852_4.setName("Test form type 852-4");
        FormTypeService formTypeService = mock(FormTypeService.class);
        when(formTypeService.getByCode("852-4")).thenReturn(formType852_4);
        ReflectionTestUtils.setField(uploadTransportDataService, "formTypeService", formTypeService);

        PeriodService periodService = mock(PeriodService.class);
        ReportPeriod reportPeriod21 = new ReportPeriod();
        reportPeriod21.setId(1);
        reportPeriod21.setName("Test period");
        when(periodService.getByTaxTypedCodeYear(TaxType.INCOME, "21", 2014)).thenReturn(reportPeriod21);
        ReflectionTestUtils.setField(uploadTransportDataService, "periodService", periodService);

        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        when(departmentFormTypeDao.existAssignedForm(TEST_DEPARTMENT_ID, 1, FormDataKind.PRIMARY)).thenReturn(true);
        ReflectionTestUtils.setField(uploadTransportDataService, "departmentFormTypeDao", departmentFormTypeDao);
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
        List<String> result = uploadTransportDataService.getWorkFilesFromFolder(temporaryFolder.getRoot().getPath() + "/", null);
        temporaryFolder.delete();
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(fileNames[4]));
        Assert.assertTrue(result.contains(fileNames[5]));
    }
}
