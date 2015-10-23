package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_8.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
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

import java.util.*;

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.getColumnName;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-8. Налоговая эффективность по уступке прав требования по проблемным активам
 */
public class Etr4_8Test extends ScriptTestBase {
    private static final int TYPE_ID = 708;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;
    private static final int SOURCE_TYPE_ID = 303;
    private static final long SOURCE_FORM_DATA_ID = 2L;

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
        formData.setAccruing(false);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Etr4_8Test.class);
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

        // подразделение-период
        when(testHelper.getDepartmentReportPeriodService().get(anyInt())).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        ReportPeriod reportPeriod = new ReportPeriod();
                        reportPeriod.setId(REPORT_PERIOD_ID);
                        reportPeriod.setOrder(1);
                        result.setReportPeriod(reportPeriod);
                        return result;
                    }
                });

        // периоды
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        ReportPeriod period = new ReportPeriod();
        period.setId(1);
        period.setOrder(1);
        period.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(period);

        ArrayList<ReportPeriod> reportPeriods = new ArrayList<ReportPeriod>();
        reportPeriods.add(period);
        when(testHelper.getReportPeriodService().getReportPeriodsByDate(any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(reportPeriods);

        FormData formData = new FormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//outcome_complex//v2012//"));
        formData.setId(SOURCE_FORM_DATA_ID);
        formData.setKind(FormDataKind.SUMMARY);
        formData.setState(WorkflowState.ACCEPTED);
        formData.setDepartmentId(DEPARTMENT_ID);
        formData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        formData.setReportPeriodId(REPORT_PERIOD_ID);

        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.save(getFillDataRows(formData));
        when(testHelper.getFormDataService().getDataRowHelper(formData)).thenReturn(sourceDataRowHelper);

        when(testHelper.getBookerStatementService().getRecords(anyLong(), anyLong(), any(Date.class), anyString())).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Map<String, RefBookValue> valueMap = new HashMap<String, RefBookValue>();
                        valueMap.put("TOTAL_SUM", new RefBookValue(RefBookAttributeType.NUMBER, 2220));
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                        result.add(valueMap);
                        return result;
                    }
                });

        when(testHelper.getFormDataService().getLast(SOURCE_TYPE_ID, FormDataKind.SUMMARY, DEPARTMENT_ID, REPORT_PERIOD_ID, null)).thenReturn(formData);
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

    // Проверка после импорта
    @Test
    public void check1Test() {
        testHelper.setImportFileInputStream(getCustomInputStream("importFileCheck.xlsm"));
        testHelper.execute(FormDataEvent.IMPORT);

        testHelper.execute(FormDataEvent.CHECK);

        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertFalse(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = 5; // в файле 5 строк
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(-1.4, dataRows.get(0).getCell("comparePeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(12.00, dataRows.get(1).getCell("comparePeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(15.00, dataRows.get(2).getCell("comparePeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-5.68, dataRows.get(4).getCell("comparePeriod").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(4.4, dataRows.get(0).getCell("currentPeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0.00, dataRows.get(1).getCell("currentPeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0.00, dataRows.get(2).getCell("currentPeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(0.88, dataRows.get(4).getCell("currentPeriod").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(5.8, dataRows.get(0).getCell("deltaRub").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-12.00, dataRows.get(1).getCell("deltaRub").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-15.00, dataRows.get(2).getCell("deltaRub").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(6.56, dataRows.get(4).getCell("deltaRub").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(-414.29, dataRows.get(0).getCell("deltaPercent").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-100.00, dataRows.get(1).getCell("deltaPercent").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-100.00, dataRows.get(2).getCell("deltaPercent").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(-115.49, dataRows.get(4).getCell("deltaPercent").getNumericValue().doubleValue(), 0);
    }

    private List<DataRow<Cell>> getFillDataRows(FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        DataRow<Cell> row = sourceFormData.createDataRow();
        row.getCell("consumptionTypeId").setValue("21490", null);
        row.getCell("consumptionTaxSumS").setValue(15000.00, null);
        dataRows.add(row);
        row = sourceFormData.createDataRow();
        row.getCell("consumptionTypeId").setValue("21510", null);
        row.getCell("consumptionTaxSumS").setValue(12000.00, null);
        dataRows.add(row);

        return dataRows;
    }
}
