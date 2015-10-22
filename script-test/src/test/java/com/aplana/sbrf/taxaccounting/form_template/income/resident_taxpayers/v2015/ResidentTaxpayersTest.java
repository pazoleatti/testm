package com.aplana.sbrf.taxaccounting.form_template.income.resident_taxpayers.v2015;

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
 * Расчет налога и облагаемой суммы дивидендов по акциям налогоплательщиков-резидентов
 */
public class ResidentTaxpayersTest extends ScriptTestBase {
    private static final int TYPE_ID = 319;
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
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(ResidentTaxpayersTest.class);
    }

    @Before
    public void mockServices() {
        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(false);
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
        Assert.assertTrue("Logger must contain error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue("Logger must contain error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = 4; // в файле 7 строк
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1, dataRows.get(0).getCell("shareCount").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(1).getCell("shareCount").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(2).getCell("shareCount").getNumericValue().intValue());
        Assert.assertEquals(1, dataRows.get(3).getCell("shareCount").getNumericValue().intValue());

        Assert.assertEquals(2, dataRows.get(0).getCell("shareProfit").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(1).getCell("shareProfit").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("shareProfit").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(3).getCell("shareProfit").getNumericValue().intValue());

        Assert.assertEquals(2, dataRows.get(0).getCell("d1").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(1).getCell("d1").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("d1").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(3).getCell("d1").getNumericValue().intValue());

        Assert.assertEquals(3, dataRows.get(0).getCell("d2").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(1).getCell("d2").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(2).getCell("d2").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(3).getCell("d2").getNumericValue().intValue());

        Assert.assertEquals(2, dataRows.get(0).getCell("taxRate").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(1).getCell("taxRate").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("taxRate").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(3).getCell("taxRate").getNumericValue().intValue());

        Assert.assertNull(dataRows.get(0).getCell("kIndex").getStringValue());
        Assert.assertNull(dataRows.get(1).getCell("kIndex").getStringValue());
        Assert.assertNull(dataRows.get(2).getCell("kIndex").getStringValue());
        Assert.assertNull(dataRows.get(3).getCell("kIndex").getStringValue());

        Assert.assertNull(dataRows.get(0).getCell("taxPerShare").getStringValue());
        Assert.assertNull(dataRows.get(1).getCell("taxPerShare").getStringValue());
        Assert.assertNull(dataRows.get(2).getCell("taxPerShare").getStringValue());
        Assert.assertNull(dataRows.get(3).getCell("taxPerShare").getStringValue());

        Assert.assertNull(dataRows.get(0).getCell("dividendPerShareIndividual").getStringValue());
        Assert.assertNull(dataRows.get(1).getCell("dividendPerShareIndividual").getStringValue());
        Assert.assertNull(dataRows.get(2).getCell("dividendPerShareIndividual").getStringValue());
        Assert.assertNull(dataRows.get(3).getCell("dividendPerShareIndividual").getStringValue());

        Assert.assertNull(dataRows.get(0).getCell("dividendPerShareLegal").getStringValue());
        Assert.assertNull(dataRows.get(1).getCell("dividendPerShareLegal").getStringValue());
        Assert.assertNull(dataRows.get(2).getCell("dividendPerShareLegal").getStringValue());
        Assert.assertNull(dataRows.get(3).getCell("dividendPerShareLegal").getStringValue());
    }
}
