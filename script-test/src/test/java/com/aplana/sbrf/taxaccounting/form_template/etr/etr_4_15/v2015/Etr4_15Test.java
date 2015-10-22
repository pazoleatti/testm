package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_15.v2015;

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
import java.util.HashMap;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-15. Анализ структуры налога на добавленную стоимость (НДС).
 */
public class Etr4_15Test extends ScriptTestBase {
    private static final int TYPE_ID = 715;
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
        return getDefaultScriptTestMockHelper(Etr4_15Test.class);
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

    @Test
    public void checkTest() {
        // ПРОВЕРКА - пустая форма
        testHelper.execute(FormDataEvent.CHECK);
        // ожидаемый результат - должны быть незаполненые обязательные поля, и неправильные подсчеты
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
        int i = 0;
        String [] logMsgs = new String [] {
                "Строка 1: Графа «Период сравнения. НДС всего, тыс. руб.» не заполнена!",
                "Строка 1: Графа «Период сравнения. В том числе НДС не учитываемый, тыс. руб.» не заполнена!",
                "Строка 1: Графа «Период сравнения. Доля НДС не учитываемый, %» не заполнена!",
                "Строка 1: Графа «Период. НДС всего, тыс. руб.» не заполнена!",
                "Строка 1: Графа «Период. В том числе НДС не учитываемый, тыс. руб.» не заполнена!",
                "Строка 1: Графа «Период. Доля НДС не учитываемый, %» не заполнена!",
                "Строка 1: Графа «Изменения за период. НДС всего, тыс. руб.» не заполнена!",
                "Строка 1: Графа «Изменения за период. В том числе НДС не учитываемый, тыс. руб.» не заполнена!",
                "Строка 1: Графа «Изменения за период. Доля НДС не учитываемый, %» не заполнена!",
                "Строка 1: Неверное значение граф: «Период сравнения. Доля НДС не учитываемый, %», «Период. Доля НДС не учитываемый, %», «Изменения за период. Доля НДС не учитываемый, %»!"
        };
        for (String logMsg : logMsgs) {
            Assert.assertEquals(logMsg, testHelper.getLogger().getEntries().get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());

        // получение строк, в форме только одна строка
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> row = dataRows.get(0);
        row.setIndex(1);

        // ПРОВЕРКА - заполнение единицами
        String [] aliases = new String[] { "comparePeriod", "comparePeriodIgnore", "comparePeriodPercent",
                "currentPeriod", "currentPeriodIgnore", "currentPeriodPercent", "delta", "deltaIgnore", "deltaPercent" };
        for (String alias : aliases) {
            row.getCell(alias).setValue(1, null);
        }
        testHelper.getLogger().clear();
        testHelper.execute(FormDataEvent.CHECK);
        // ожидаемый результат - неправильные подсчеты
        Assert.assertEquals(1, testHelper.getLogger().getEntries().size());
        String msg = "Строка 1: Неверное значение граф: «Период сравнения. Доля НДС не учитываемый, %», «Период. Доля НДС не учитываемый, %», «Изменения за период. НДС всего, тыс. руб.», «Изменения за период. В том числе НДС не учитываемый, тыс. руб.», «Изменения за период. Доля НДС не учитываемый, %»!";
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(0).getMessage());

        // ПРОВЕРКА - заполнение посчитанными значениями
        Map<String, Double> map = new HashMap<String, Double>();
        map.put("comparePeriod", 1.0);
        map.put("comparePeriodIgnore", 1.0);
        map.put("comparePeriodPercent", 100.0);
        map.put("currentPeriod", 1.0);
        map.put("currentPeriodIgnore", 1.0);
        map.put("currentPeriodPercent", 100.0);
        map.put("delta", 0.0);
        map.put("deltaIgnore", 0.0);
        map.put("deltaPercent", 0.0);
        for (String alias : map.keySet()) {
            row.getCell(alias).setValue(map.get(alias), null);
        }
        testHelper.getLogger().clear();
        testHelper.execute(FormDataEvent.CHECK);
        // ожидаемый результат - все значения заполнены и правильно посчитаны
        checkLogger();
        Assert.assertEquals(0, testHelper.getLogger().getEntries().size());
        for (String alias : map.keySet()) {
            Cell cell = row.getCell(alias);
            Double value = (cell.getNumericValue() != null ? cell.getNumericValue().doubleValue() : null);
            Double expected = map.get(alias);
            Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
        }

        // ПРОВЕРКА - одно значение неправильное
        row.getCell("deltaPercent").setValue(1, null);
        testHelper.getLogger().clear();
        testHelper.execute(FormDataEvent.CHECK);
        // ожидаемый результат - все значения заполнены и правильно посчитаны
        Assert.assertEquals(1, testHelper.getLogger().getEntries().size());
        msg = "Строка 1: Неверное значение граф: «Изменения за период. Доля НДС не учитываемый, %»!";
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(0).getMessage());
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
        // графа 1..9
        String [] calcColumns = { "comparePeriod", "comparePeriodIgnore", "comparePeriodPercent", "currentPeriod",
                "currentPeriodIgnore", "currentPeriodPercent", "delta", "deltaIgnore", "deltaPercent" };
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            expected++;
            for (String alias : calcColumns) {
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
        // графа 1, 2, 4, 5
        String [] consolidatedColumns = { "comparePeriod", "comparePeriodIgnore", "currentPeriod", "currentPeriodIgnore" };
        // графа 3, 6..9
        String [] calcColumns = { "comparePeriodPercent", "currentPeriodPercent", "delta", "deltaIgnore", "deltaPercent" };
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            expected++;
            // графа 1, 2, 4, 5
            for (String alias : consolidatedColumns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
            }
            // графа 3, 6..9
            for (String alias : calcColumns) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertNotNull("row." + alias + "[" + row.getIndex() + "]", value);
            }
        }
    }
}
