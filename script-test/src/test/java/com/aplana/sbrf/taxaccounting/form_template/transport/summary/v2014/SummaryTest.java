package com.aplana.sbrf.taxaccounting.form_template.transport.summary.v2014;

import com.aplana.sbrf.taxaccounting.form_template.transport.vehicles.v2014.Vehicles1Test;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Расчет суммы налога по каждому транспортному средству
 *
 * @author Kinzyabulatov
 */
public class SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 203;
    private static final int SOURCE_TYPE_ID = 204;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;
    private static final FormDataKind SOURCE_KIND = FormDataKind.PRIMARY;

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formType.setId(TYPE_ID);
        formData.setId(TestScriptHelper.CURRENT_FORM_DATA_ID);
        formData.setFormType(formType);
        formData.setFormTemplateId(TYPE_ID);
        formData.setKind(KIND);
        formData.setState(WorkflowState.CREATED);
        formData.setDepartmentId(DEPARTMENT_ID);
        formData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        formData.setReportPeriodId(REPORT_PERIOD_ID);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(SummaryTest.class);
    }

    @Before
    public void mockRefBookDataProvider() {
        // Для работы проверок
        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        String filter = (String)invocation.getArguments()[2];
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        if (filter.equals("DECLARATION_REGION_ID = 1 and OKTMO = 1")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("REGION_ID", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        } else if (filter.toLowerCase().contains("OKTMO_DEFINITION like ".toLowerCase()) || filter.toLowerCase().contains("CODE like ".toLowerCase())) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        } else if (filter.contains("(YEAR_FROM <")) {
                            Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                            map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            map.put("COEF", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                            result.add(map);
                        }
                        return result;
                    }
                });
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
    }

    @Test
    public void afterCreateTest() {
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        checkLogger();
    }

    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // собирается из первички транспорта
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(SOURCE_KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(SOURCE_TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Один экземпляр-источник(первичная)
        FormData sourceFormData = new FormData();
        FormType formType = new FormType();
        formType.setId(SOURCE_TYPE_ID);
        sourceFormData.setId(2L);
        sourceFormData.setKind(SOURCE_KIND);
        sourceFormData.setFormType(formType);
        sourceFormData.setDepartmentId(DEPARTMENT_ID);
        sourceFormData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        sourceFormData.setReportPeriodId(REPORT_PERIOD_ID);
        sourceFormData.setState(WorkflowState.ACCEPTED);

        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");

        // DataRowHelper источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        TestScriptHelper sourceTestHelper = new TestScriptHelper("/form_template/transport/vehicles/v2014/", sourceFormData, getDefaultScriptTestMockHelper(Vehicles1Test.class));
        sourceTestHelper.setImportFileInputStream(getCustomInputStream("sourceImportFile.xlsm"));
        sourceTestHelper.initRowData();

        when(sourceTestHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });

        sourceTestHelper.execute(FormDataEvent.IMPORT);
        sourceDataRowHelper.save(sourceTestHelper.getDataRowHelper().getAll());

        when(testHelper.getFormDataService().getLast(eq(SOURCE_TYPE_ID), eq(SOURCE_KIND), eq(DEPARTMENT_ID), anyInt(),
                any(Integer.class))).thenReturn(sourceFormData);

        when(testHelper.getDepartmentService().get(DEPARTMENT_ID)).thenReturn(department);

        // Консолидация
        testHelper.initRowData();
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertTrue("Logger must contain error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }
}
