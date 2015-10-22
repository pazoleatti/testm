package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_10.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
}
