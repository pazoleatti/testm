package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_3.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-3. Отношение налоговых платежей к чистой прибыли Банка
 */
public class Etr43Test extends ScriptTestBase {
    private static final int TYPE_ID = 703;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

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
        return getDefaultScriptTestMockHelper(Etr43Test.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());

        // подразделение-период
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        ReportPeriod reportPeriod = new ReportPeriod();
                        reportPeriod.setId(REPORT_PERIOD_ID);
                        result.setReportPeriod(reportPeriod);
                        return result;
                    }
                });

        // периоды
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        ReportPeriod period = new ReportPeriod();
        period.setId(1);
        period.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(period);
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
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertFalse(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = 3; // в файле 3 строки
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
        Assert.assertEquals(93561784.66, dataRows.get(0).getCell("comparePeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(137402815.53, dataRows.get(1).getCell("comparePeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(68.09, dataRows.get(2).getCell("comparePeriod").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(93561685.66, dataRows.get(0).getCell("currentPeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(144441302.15, dataRows.get(1).getCell("currentPeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(64.77, dataRows.get(2).getCell("currentPeriod").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(-99.00, dataRows.get(0).getCell("deltaRub").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(7038486.62, dataRows.get(1).getCell("deltaRub").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-3.32, dataRows.get(2).getCell("deltaRub").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(0, dataRows.get(0).getCell("deltaPercent").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(5.12, dataRows.get(1).getCell("deltaPercent").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-4.87, dataRows.get(2).getCell("deltaPercent").getNumericValue().doubleValue(), 0);
    }
}
