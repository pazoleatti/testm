package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_13.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-13. Анализ структуры доходов и расходов, не учитываемых для целей налогообложения.
 */
public class Etr4_13Test extends ScriptTestBase {
    private static final int TYPE_ID = 713;
    private static final int DEPARTMENT_ID = 2;
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
        return getDefaultScriptTestMockHelper(Etr4_13Test.class);
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
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(period);

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
        // возвращаются все строки из справочника БО, делать отбор по фильтру не стал, т.к. лишние записи не вызывают ошибок, а только предупреждения
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
        // должны быть незаполненые обязательные поля
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = testHelper.getDataRowHelper().getAll().size();
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 4..9 - для всех строк, кроме строк I, II
        String [] calcColumns = { "comparePeriod", "comparePeriodPercent", "currentPeriod", "currentPeriodPercent", "deltaRub", "deltaPercent" };
        // графа 4, 6, 8, 9 - для строк I, II
        String [] calcColumns1_2 = { "comparePeriod", "currentPeriod", "deltaRub", "deltaPercent" };
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() == null || "".equals(row.getAlias())) {
                continue;
            }
            expected++;
            String [] columns = ("I".equals(row.getAlias()) || "II".equals(row.getAlias()) ? calcColumns1_2 : calcColumns);
            for (String alias : columns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
            }
        }
    }

    // Консолидация
    @Test
    public void composeTest() {
        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(FormDataKind.PRIMARY);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(FormDataKind.PRIMARY), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются импортом
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        sourceDataRowHelper.save(testHelper.getDataRowHelper().getAll());
        testHelper.initRowData();

        // Консолидация
        int expected = testHelper.getDataRowHelper().getAll().size();
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());

        checkConsolidatedData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить сконсолидированные данные. */
    void checkConsolidatedData(List<DataRow<Cell>> dataRows) {
        // графа 4, 6 - для всех строк, кроме строк I, II
        String [] consolidatedColumns = { "comparePeriod", "currentPeriod" };
        // графа 4..9 - для всех строк, кроме строк I, II
        String [] calcColumns = { "comparePeriod", "comparePeriodPercent", "currentPeriod", "currentPeriodPercent", "deltaRub", "deltaPercent" };
        // графа 4, 6, 8, 9 - для строк I, II
        String [] calcColumns1_2 = { "comparePeriod", "currentPeriod", "deltaRub", "deltaPercent" };
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() == null || "".equals(row.getAlias())) {
                continue;
            }
            expected++;
            // графа 4, 6
            for (String alias : consolidatedColumns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
            }

            // графа 5, 7, 8, 9
            String [] columns = ("I".equals(row.getAlias()) || "II".equals(row.getAlias()) ? calcColumns1_2 : calcColumns);
            for (String alias : columns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertNotNull("row." + alias + "[" + row.getIndex() + "]", value);
            }
        }
    }
}
