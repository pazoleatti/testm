package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_10.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-10.
 */
public class Etr4_10Test extends ScriptTestBase {
    private static final int TYPE_ID = 710;
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
        return getDefaultScriptTestMockHelper(Etr4_10Test.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());

        // подразделение-период
        when(testHelper.getDepartmentService().get(any(Integer.class))).thenAnswer(
                new Answer<Department>() {
                    @Override
                    public Department answer(InvocationOnMock invocation) throws Throwable {
                        Department result = new Department();
                        result.setName("test" + invocation.getArguments()[0]);
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
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//etr//etr_4_10//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // для попадания в ЛП:
        // Проверка заполнения ячеек
        DataRow<Cell> row = dataRows.get(0);
        row.setIndex(1);

        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Графа «Сумма увеличения базы по налогу на прибыль, в том числе не учитываемые расходы, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Сумма увеличения базы по налогу на прибыль, в том числе доначисление доходов, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Налоговое бремя, тыс. руб.» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());

        // для попадания в ЛП:
        // Проверка заполнения графы 3
        row.getCell("sum1").setValue(1, null);
        row.getCell("sum2").setValue(2, null);
        row.getCell("taxBurden").setValue(3, null);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        Assert.assertEquals("Строка 1: Неверное значение граф: «Налоговое бремя, тыс. руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());

        // для удачного прохождения всех ЛП
        row.getCell("sum1").setValue(1, null);
        row.getCell("sum2").setValue(2, null);
        row.getCell("taxBurden").setValue(0.6, null);

        testHelper.execute(FormDataEvent.CHECK);

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(0, dataRows.get(0).getCell("sum1").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(22348, dataRows.get(0).getCell("sum2").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4469.6, dataRows.get(0).getCell("taxBurden").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, testHelper.getDataRowHelper().getCount());
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
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size());
        checkComposeData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить сконсолидированные данные.
     */
    void checkComposeData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(55, dataRows.get(0).getCell("sum1").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(155, dataRows.get(0).getCell("sum2").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(42, dataRows.get(0).getCell("taxBurden").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(1, testHelper.getDataRowHelper().getCount());
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
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//etr//etr_4_11//v2015//");

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
        row.getCell("sum1").setValue(i, null);
        row.getCell("sum2").setValue(i + 10, null);
        dataRows.add(row);

        return dataRows;
    }
}
