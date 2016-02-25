package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_11.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
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
 * Приложение 4-11.
 */
public class Etr4_11Test extends ScriptTestBase {
    private static final int TYPE_ID = 711;
    private static final int DEPARTMENT_ID = 2;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.CONSOLIDATED;
    private static final int SOURCE_TYPE_ID = 711;

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
        return getDefaultScriptTestMockHelper(Etr4_11Test.class);
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

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertFalse(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    // Проверка со входом во все ЛП
    @Test
    public void check1Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//etr//etr_4_11//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // для попадания в ЛП:
        // Проверка заполнения граф 5,6
        DataRow<Cell> row1 = formData.createDataRow();
        row1.setIndex(1);
        row1.getCell("name").setValue("name_1", null);
        row1.getCell("sum1").setValue(-12345678901234567890.12, null);
        row1.getCell("sum2").setValue(12345678901234567890.12, null);
        row1.getCell("level").setValue(12345.12, null);
        row1.getCell("taxBurden").setValue(12345678901234567890.12, null);
        dataRows.add(row1);

        // для попадания в ЛП:
        // Проверка заполнения ячеек
        // Проверка графы 3 при расчете графы 5
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        dataRows.add(row2);

        // для попадания в ЛП:
        // Проверка графы 3 при расчете графы 5
        DataRow<Cell> row3 = formData.createDataRow();
        row3.setIndex(3);
        row3.getCell("name").setValue("name_3", null);
        row3.getCell("sum1").setValue(0, null);
        row3.getCell("sum2").setValue(0, null);
        row3.getCell("level").setValue(0, null);
        row3.getCell("taxBurden").setValue(0, null);
        dataRows.add(row3);

        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 1: Неверное значение граф: «Уровень доначислений/ не учитываемых расходов (в % от факта)», «Налоговое бремя, тыс. руб.»!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 2, "Наименование сделки"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 2, "Сумма фактического дохода/расхода по нерыночным сделкам, тыс. руб. (налоговый учет)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 2, "Сумма доначислений до рыночного уровня, тыс. руб."), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 2, "Уровень доначислений/ не учитываемых расходов (в % от факта)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 2, "Налоговое бремя, тыс. руб."), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Графа «Уровень доначислений/ не учитываемых расходов (в % от факта)» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 3: Графа «Уровень доначислений/ не учитываемых расходов (в % от факта)» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».", entries.get(i++).getMessage());

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
        // 2 строки из файла + 1 строка итоговая
        int expected = testHelper.getDataRowHelper().getAll().size() + 2;
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals("Сделки по предоставлению кредитных ресурсов", dataRows.get(0).getCell("name").getStringValue());
        Assert.assertEquals("Сделки с инвалютой (доходы)", dataRows.get(1).getCell("name").getStringValue());

        Assert.assertEquals(2897.09, dataRows.get(0).getCell("sum1").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0.00, dataRows.get(1).getCell("sum1").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(234.89, dataRows.get(0).getCell("sum2").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0.00, dataRows.get(1).getCell("sum2").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(8.11, dataRows.get(0).getCell("level").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0.00, dataRows.get(1).getCell("level").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(46.98, dataRows.get(0).getCell("taxBurden").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0.00, dataRows.get(1).getCell("taxBurden").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(2, testHelper.getDataRowHelper().getCount());
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
                    anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceFormData);
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
        Assert.assertEquals(2, testHelper.getDataRowHelper().getAll().size());
        checkComposeData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить сконсолидированные данные.
     */
    void checkComposeData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals("name_1", dataRows.get(0).getCell("name").getStringValue());
        Assert.assertEquals("name_2", dataRows.get(1).getCell("name").getStringValue());

        Assert.assertEquals(20, dataRows.get(0).getCell("sum1").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(10, dataRows.get(1).getCell("sum1").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(40, dataRows.get(0).getCell("sum2").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(20, dataRows.get(1).getCell("sum2").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(200, dataRows.get(0).getCell("level").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(200, dataRows.get(1).getCell("level").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(8, dataRows.get(0).getCell("taxBurden").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4, dataRows.get(1).getCell("taxBurden").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(2, testHelper.getDataRowHelper().getCount());
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
    private List<DataRow<Cell>> getFillDataRows(FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        DataRow<Cell> row1 = sourceFormData.createDataRow();
        row1.getCell("name").setValue("name_1", null);
        row1.getCell("sum1").setValue(1, null);
        row1.getCell("sum2").setValue(2, null);
        dataRows.add(row1);

        DataRow<Cell> row2 = sourceFormData.createDataRow();
        row2.getCell("name").setValue("name_2", null);
        row2.getCell("sum1").setValue(1, null);
        row2.getCell("sum2").setValue(2, null);
        dataRows.add(row2);

        DataRow<Cell> row3 = sourceFormData.createDataRow();
        row3.getCell("name").setValue("name_1", null);
        row3.getCell("sum1").setValue(1, null);
        row3.getCell("sum2").setValue(2, null);
        dataRows.add(row3);

        return dataRows;
    }
}
