package com.aplana.sbrf.taxaccounting.form_template.property.property_945_5.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
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
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Сводная форма данных бухгалтерского учета для расчета налога на имущество.
 *
 * @author SYasinskiy
 */
public class Property945_5Test extends ScriptTestBase {
    private static final int TYPE_ID = 615;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

    private static final int SOURCE_FORM_TYPE_ID = 610;
    private static final String SOURCE_FORM_TYPE_NAME = "source test name";


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
        return getDefaultScriptTestMockHelper(Property945_5Test.class);
    }

    @Before
    public void mockFormDataService() {
        // Получения названия типа источника
        when(testHelper.getFormTypeService().get(SOURCE_FORM_TYPE_ID)).thenAnswer(
                new Answer<FormType>() {
                    @Override
                    public FormType answer(InvocationOnMock invocation) throws Throwable {
                        FormType formType = new FormType();
                        formType.setName(SOURCE_FORM_TYPE_NAME);
                        return formType;
                    }
                });

        // Получение источников
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(FormDataKind.PRIMARY);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(SOURCE_FORM_TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        sourceFormData.setId((long) SOURCE_FORM_TYPE_ID);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // Определение балансового периода
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        // Добавление (в этой форме ручного добавления нет)
        testHelper.execute(FormDataEvent.ADD_ROW);
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
        int expected = 3; // в файле 3 строк
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        String [] refBookColumns = { "subject", "oktmo" };
        String [] stringColumns = { "taxAuthority", "kpp", "title" };
        // графа 5..18
        String [] numberColumns = { "cost1", "cost2", "cost3", "cost4", "cost5", "cost6", "cost7", "cost8",
                "cost9", "cost10", "cost11", "cost12", "cost13", "cost31_12" };
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            for (String alias : refBookColumns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertNotNull("row." + alias + "[" + row.getIndex() + "]", value);
            }

            for (String alias : stringColumns) {
                Cell cell = row.getCell(alias);
                String value = cell.getStringValue();
                Assert.assertNotNull("row." + alias + "[" + row.getIndex() + "]", value);
            }

            expected++;
            // графа 5..18
            for (String alias : numberColumns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
            }
        }
    }
}
