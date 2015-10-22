package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_1.v2015;

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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-1. Абсолютная величина налоговых платежей
 */
public class Etr41Test extends ScriptTestBase {
    private static final int TYPE_ID = 701;
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
        return getDefaultScriptTestMockHelper(Etr41Test.class);
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
        int expected = 7; // в файле 7 строк
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(15, dataRows.get(0).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(1).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(14, dataRows.get(2).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(3).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(4).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(4, dataRows.get(5).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(5, dataRows.get(6).getCell("comparePeriod").getNumericValue().intValue());

        Assert.assertEquals(15, dataRows.get(0).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(1).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(14, dataRows.get(2).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(3).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(4).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(4, dataRows.get(5).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(5, dataRows.get(6).getCell("currentPeriod").getNumericValue().intValue());

        Assert.assertEquals(0, dataRows.get(0).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(1).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(2).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(3).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(4).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(5).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(6).getCell("deltaRub").getNumericValue().intValue());

        Assert.assertEquals(0, dataRows.get(0).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(1).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(2).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(3).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(4).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(5).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(6).getCell("deltaPercent").getNumericValue().intValue());
    }
}
