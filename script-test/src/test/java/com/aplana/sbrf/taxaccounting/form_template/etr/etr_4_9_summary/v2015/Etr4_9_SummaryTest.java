package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_9_summary.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
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
 * Приложение 4-9.
 */
public class Etr4_9_SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 7090;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.CONSOLIDATED;
    private static final int SOURCE_TYPE_ID = 708;

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
        formData.setComparativePeriodId(1);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Etr4_9_SummaryTest.class);
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
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//etr//etr_4_9_summary//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // для попадания в ЛП:
        // Проверка заполнения граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «БУ, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «НУд, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «НУп, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Налоговое бремя, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Подразделение Банка» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «БУ, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «НУд, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «НУп, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Налоговое бремя, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
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
        int expected = 17;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 2
        for (int i = 0; i < 15; i++) {
            Assert.assertEquals(i + 1, dataRows.get(i).getCell("department").getNumericValue().doubleValue(), 0);
        }

        // первая строка
        Assert.assertEquals(1234567.45, dataRows.get(0).getCell("sumBU").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(567345.75, dataRows.get(0).getCell("sumNUD").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(668645.34, dataRows.get(0).getCell("sumNUP").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-284.73, dataRows.get(0).getCell("taxBurden").getNumericValue().doubleValue(), 0);

        // последняя строка
        Assert.assertEquals(1114567.73, dataRows.get(15).getCell("sumBU").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(557345.75, dataRows.get(15).getCell("sumNUD").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1168612.35, dataRows.get(15).getCell("sumNUP").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-122278.07, dataRows.get(15).getCell("taxBurden").getNumericValue().doubleValue(), 0);
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
                    anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

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

    /**
     * Проверить загруженные данные.
     */
    void checkComposeData(List<DataRow<Cell>> dataRows) {
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            if ("total".equals(row.getAlias())) {
                continue;
            }
            expected++;

            Cell cell = row.getCell("sumBU");
            Long sumBU = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
            Assert.assertEquals("row.sumBU[" + row.getIndex() + "]", expected, sumBU);


            cell = row.getCell("sumNUD");
            Long sumNUD = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
            Assert.assertEquals("row.sumNUD[" + row.getIndex() + "]", (Object) (expected * 2), sumNUD);


            cell = row.getCell("sumNUP");
            Long sumNUP = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
            Assert.assertEquals("row.sumNUP[" + row.getIndex() + "]", (Object) (expected * 3), sumNUP);


            cell = row.getCell("taxBurden");
            Double taxBurden = (cell.getNumericValue() != null ? cell.getNumericValue().doubleValue() : null);
            Assert.assertEquals("row.taxBurden[" + row.getIndex() + "]", ((sumBU - sumNUD - sumNUP) * 0.2), taxBurden, 0.001);

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
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//etr//etr_4_8//v2015//");

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
        sourceFormData.setDepartmentId(id);

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
        row.getCell("currentPeriod").setValue(i, null);
        dataRows.add(row);

        DataRow<Cell> row2 = sourceFormData.createDataRow();
        row2.setAlias("R2");
        row2.getCell("currentPeriod").setValue(i * 2, null);
        dataRows.add(row2);

        DataRow<Cell> row3 = sourceFormData.createDataRow();
        row3.setAlias("R3");
        row3.getCell("currentPeriod").setValue(i * 3, null);
        dataRows.add(row3);

        return dataRows;
    }
}
