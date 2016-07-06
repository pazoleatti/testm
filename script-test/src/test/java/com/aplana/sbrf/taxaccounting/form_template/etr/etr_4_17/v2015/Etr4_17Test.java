package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_17.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Аналитический отчет «Сведения о начисленных и уплачиваемых налогах, сборах и взносах, отнесенных на расходы»
 */
public class Etr4_17Test extends ScriptTestBase {
    private static final int TYPE_ID = 717;
    private static final int DEPARTMENT_ID = 2;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.CALCULATED;

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
        formData.setAccruing(true);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Etr4_17Test.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

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

        // предыдущий период
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setId(0);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        // периоды
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        ReportPeriod period = new ReportPeriod();
        period.setId(1);
        period.setTaxPeriod(taxPeriod);
        period.setOrder(2);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(period);

        // Подразделение с id = 1
        when(testHelper.getDepartmentService().getParentTBId(1)).thenReturn(113);
        Department department1 = new Department();
        department1.setId(1);
        department1.setName("Name1");
        when(testHelper.getDepartmentService().get(1)).thenReturn(department1);

        // период БО
        Long accountPeriodId = 1L;
        when(testHelper.getBookerStatementService().getAccountPeriodId(anyLong(), any(Date.class))).thenReturn(accountPeriodId);

        // провайдер для БО
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), anyLong(),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(testHelper.getRefBookDataProvider());

        // список id записей БО
        when(testHelper.getRefBookDataProvider().getUniqueRecordIds(any(Date.class), anyString())).thenReturn(Arrays.asList(1L, 2L, 3L));

        // записи БО
        Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(52L);
        // возвращаются только необходимые строки из справочника БО
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records.values());
        when(testHelper.getBookerStatementService().getRecords(anyLong(), anyLong(), any(Date.class), anyString())).thenReturn(result);
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

    @Test
    public void checkCalc1() {
        FormData formData = testHelper.getFormData();
        formData.setAccruing(true);
        DataRow<Cell> row1 = formData.createDataRow();
        row1.setIndex(1);
        row1.getCell("department").setValue(1L, 1);
        testHelper.getDataRowHelper().getAll().add(0, row1);
        testHelper.execute(FormDataEvent.CALCULATE);

        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals(dataRows.get(0).getCell("parentTB").getNumericValue(), new BigDecimal(113));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_01").getNumericValue(), new BigDecimal("0.10"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_02").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("sum34").getNumericValue(), new BigDecimal("0.10"));
        Assert.assertEquals(dataRows.get(0).getCell("rate5").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_03").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(0).getCell("rate7").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_13").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(0).getCell("rate9").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_12").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(0).getCell("rate11").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26412").getNumericValue(), new BigDecimal("1.11"));
        Assert.assertEquals(dataRows.get(0).getCell("rate13").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26410_09").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(0).getCell("rate15").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(0).getCell("sum").getNumericValue(), new BigDecimal("1.25"));
        Assert.assertEquals(dataRows.get(0).getCell("rate").getNumericValue(), new BigDecimal("100.00"));

        Assert.assertEquals(dataRows.get(1).getCell("tax26411_01").getNumericValue(), new BigDecimal("0.10"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_02").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("sum34").getNumericValue(), new BigDecimal("0.10"));
        Assert.assertEquals(dataRows.get(1).getCell("rate5").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_03").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(1).getCell("rate7").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_13").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(1).getCell("rate9").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_12").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(1).getCell("rate11").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26412").getNumericValue(), new BigDecimal("1.11"));
        Assert.assertEquals(dataRows.get(1).getCell("rate13").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26410_09").getNumericValue(), new BigDecimal("0.01"));
        Assert.assertEquals(dataRows.get(1).getCell("rate15").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("sum").getNumericValue(), new BigDecimal("1.25"));
        Assert.assertEquals(dataRows.get(1).getCell("rate").getNumericValue(), new BigDecimal("100.00"));

        checkLogger();
    }

    @Test
    public void checkCalc2() {
        FormData formData = testHelper.getFormData();
        formData.setAccruing(false);
        DataRow<Cell> row1 = formData.createDataRow();
        row1.setIndex(1);
        row1.getCell("department").setValue(1L, 1);
        testHelper.getDataRowHelper().getAll().add(0, row1);
        testHelper.execute(FormDataEvent.CALCULATE);

        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals(dataRows.get(0).getCell("parentTB").getNumericValue(), new BigDecimal(113));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_01").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_02").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("sum34").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate5").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_03").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate7").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_13").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate9").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_12").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate11").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26412").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate13").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26410_09").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate15").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("sum").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate").getNumericValue(), new BigDecimal("0.00"));

        Assert.assertEquals(dataRows.get(1).getCell("tax26411_01").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_02").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("sum34").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate5").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_03").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate7").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_13").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate9").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_12").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate11").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26412").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate13").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26410_09").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate15").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("sum").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate").getNumericValue(), new BigDecimal("0.00"));

        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 3; // в файле 3 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals(expected, dataRows.size());

        Assert.assertEquals(dataRows.get(0).getCell("parentTB").getNumericValue(), new BigDecimal(113));
        Assert.assertEquals(dataRows.get(0).getCell("department").getNumericValue(), new BigDecimal(1));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_01").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_02").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("sum34").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate5").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_03").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate7").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_13").getNumericValue(), new BigDecimal("2.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate9").getNumericValue(), new BigDecimal("50.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26411_12").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate11").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26412").getNumericValue(), new BigDecimal("4.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate13").getNumericValue(), new BigDecimal("50.00"));
        Assert.assertEquals(dataRows.get(0).getCell("tax26410_09").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate15").getNumericValue(), new BigDecimal("0.00"));
        Assert.assertEquals(dataRows.get(0).getCell("sum").getNumericValue(), new BigDecimal("6.00"));
        Assert.assertEquals(dataRows.get(0).getCell("rate").getNumericValue(), new BigDecimal("25.00"));

        Assert.assertEquals(dataRows.get(1).getCell("parentTB").getNumericValue(), new BigDecimal(113));
        Assert.assertEquals(dataRows.get(1).getCell("department").getNumericValue(), new BigDecimal(2));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_01").getNumericValue(), new BigDecimal("1.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_02").getNumericValue(), new BigDecimal("2.00"));
        Assert.assertEquals(dataRows.get(1).getCell("sum34").getNumericValue(), new BigDecimal("3.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate5").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_03").getNumericValue(), new BigDecimal("1.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate7").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_13").getNumericValue(), new BigDecimal("2.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate9").getNumericValue(), new BigDecimal("50.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26411_12").getNumericValue(), new BigDecimal("3.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate11").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26412").getNumericValue(), new BigDecimal("4.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate13").getNumericValue(), new BigDecimal("50.00"));
        Assert.assertEquals(dataRows.get(1).getCell("tax26410_09").getNumericValue(), new BigDecimal("5.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate15").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(1).getCell("sum").getNumericValue(), new BigDecimal("18.00"));
        Assert.assertEquals(dataRows.get(1).getCell("rate").getNumericValue(), new BigDecimal("75.00"));

        Assert.assertEquals(dataRows.get(2).getCell("tax26411_01").getNumericValue(), new BigDecimal("1.00"));
        Assert.assertEquals(dataRows.get(2).getCell("tax26411_02").getNumericValue(), new BigDecimal("2.00"));
        Assert.assertEquals(dataRows.get(2).getCell("sum34").getNumericValue(), new BigDecimal("3.00"));
        Assert.assertEquals(dataRows.get(2).getCell("rate5").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(2).getCell("tax26411_03").getNumericValue(), new BigDecimal("1.00"));
        Assert.assertEquals(dataRows.get(2).getCell("rate7").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(2).getCell("tax26411_13").getNumericValue(), new BigDecimal("4.00"));
        Assert.assertEquals(dataRows.get(2).getCell("rate9").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(2).getCell("tax26411_12").getNumericValue(), new BigDecimal("3.00"));
        Assert.assertEquals(dataRows.get(2).getCell("rate11").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(2).getCell("tax26412").getNumericValue(), new BigDecimal("8.00"));
        Assert.assertEquals(dataRows.get(2).getCell("rate13").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(2).getCell("tax26410_09").getNumericValue(), new BigDecimal("5.00"));
        Assert.assertEquals(dataRows.get(2).getCell("rate15").getNumericValue(), new BigDecimal("100.00"));
        Assert.assertEquals(dataRows.get(2).getCell("sum").getNumericValue(), new BigDecimal("24.00"));
        Assert.assertEquals(dataRows.get(2).getCell("rate").getNumericValue(), new BigDecimal("100.00"));

        checkLogger();
    }
}
