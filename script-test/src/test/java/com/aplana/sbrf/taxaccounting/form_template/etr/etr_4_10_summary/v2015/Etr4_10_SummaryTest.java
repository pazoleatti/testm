package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_10_summary.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-10.
 */
public class Etr4_10_SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 7100;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.CONSOLIDATED;
    private static final int SOURCE_TYPE_ID = 710;

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
        formData.setPeriodOrder(1);
        formData.setComparativPeriodId(1);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Etr4_10_SummaryTest.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());

        // подразделение-период
        final Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(30L);
        when(testHelper.getDepartmentService().get(any(Integer.class))).thenAnswer(
                new Answer<Department>() {
                    @Override
                    public Department answer(InvocationOnMock invocation) throws Throwable {
                        Department result = new Department();
                        Integer id = (Integer) invocation.getArguments()[0];
                        Map<String, RefBookValue> record = records.get(id.longValue());
                        if (record == null) {
                            return null;
                        }
                        String name = record.get("NAME").getStringValue();
                        result.setName("test" + name);
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

    // Проверка со входом во все ЛП
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
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        int expected = 2; // 1 строка из файла + 1 строка итога
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 2
        Assert.assertEquals(1, dataRows.get(0).getCell("department").getNumericValue().doubleValue(), 0);
        // графа 3
        Assert.assertEquals(1000, dataRows.get(0).getCell("sum1").getNumericValue().doubleValue(), 0);
        // графа 4
        Assert.assertEquals(2000, dataRows.get(0).getCell("sum2").getNumericValue().doubleValue(), 0);
        // графа 5
        Assert.assertEquals(600, dataRows.get(0).getCell("taxBurden").getNumericValue().doubleValue(), 0);
    }

    // Консолидация
    @Test
    public void composeTest() {
        int sourceTemplateId = SOURCE_TYPE_ID;
        FormDataKind kind = FormDataKind.CONSOLIDATED;
        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
        int sourceCount = 10;

        for (int i = 1; i <= sourceCount; i++) {
            // источник
            DepartmentFormType departmentFormType = getSource(i, kind, i);
            departmentFormTypes.add(departmentFormType);

            // форма источника
            FormData sourceFormData = getSourceFormData(i, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(anyInt(), eq(kind), eq(i),
                    anyInt(), any(Integer.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceFormData, i);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
        }
        // источник
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);

        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        // проверка значении
        int expected = sourceCount + 1; // 10 строк из источников + 1 строка итога
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkComposeData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkComposeData(List<DataRow<Cell>> dataRows) {
        // графа 3, 4
        String [] calcColumns = { "sum1", "sum2" };
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            if ("total".equals(row.getAlias())) {
                continue;
            }
            expected++;

            // 3, 4
            for (String alias : calcColumns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
            }

            // графа 5
            Cell cell = row.getCell("taxBurden");
            Double value5 = (cell.getNumericValue() != null ? cell.getNumericValue().doubleValue() : null);
            Assert.assertNotNull("row.taxBurden[" + row.getIndex() + "]", value5);

            // графа 2
            cell = row.getCell("department");
            Long value2 = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
            Assert.assertNotNull("row.department[" + row.getIndex() + "]", value2);
        }
    }

    /**
     * Получить источник.
     *
     * @param id           идентификатор источника
     * @param kind         вид формы источника
     * @param departmentId подразделение
     */
    private DepartmentFormType getSource(int id, FormDataKind kind, int departmentId) {
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setId(id);
        departmentFormType.setFormTypeId(SOURCE_TYPE_ID);
        departmentFormType.setKind(kind);
        departmentFormType.setDepartmentId(departmentId);
        return departmentFormType;
    }

    /**
     * Получить форму источника.
     *
     * @param id               идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(int id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//etr//etr_4_10//v2015//");

        FormType formType = new FormType();
        formType.setId(SOURCE_TYPE_ID);
        formType.setTaxType(TaxType.ETR);
        formType.setName(sourceTemplate.getName());

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId((long) id);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        sourceFormData.setFormType(formType);
        sourceFormData.setFormTemplateId(sourceTemplateId);

        return sourceFormData;
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceFormData форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(FormData sourceFormData, int i) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        DataRow<Cell> row = sourceFormData.createDataRow();
        row.setAlias("R1");
        row.getCell("sum1").setValue(i, null);
        row.getCell("sum2").setValue(i, null);
        row.getCell("taxBurden").setValue(i, null);
        dataRows.add(row);

        return dataRows;
    }
}
