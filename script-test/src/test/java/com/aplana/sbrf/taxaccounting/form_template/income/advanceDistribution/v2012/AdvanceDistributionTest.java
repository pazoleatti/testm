package com.aplana.sbrf.taxaccounting.form_template.income.advanceDistribution.v2012;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации.
 *
 */
public class AdvanceDistributionTest extends ScriptTestBase {
    private static final int TYPE_ID = 500;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

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
        return getDefaultScriptTestMockHelper(AdvanceDistributionTest.class);
    }

    @Before
    public void mockFormDataService() {
        // для поиска подразделений
        when(testHelper.getFormDataService().getRefBookRecord(anyLong(), anyMap(), anyMap(), anyMap(), anyString(),
                anyString(), any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        String value = (String) invocation.getArguments()[5];
                        if (value == null || "".equals(value.trim())) {
                            return null;
                        }
                        Long departmentId = Long.valueOf(value);
                        return testHelper.getFormDataService().getRefBookValue(33L, departmentId, new HashMap<String, Map<String, RefBookValue>>());
                    }
                });

        // для получения сводных доходов-расходов простых-сложных
        when(testHelper.getFormDataService().getLast(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), anyInt())).thenAnswer(
                new Answer<FormData>() {
                    @Override
                    public FormData answer(InvocationOnMock invocation) throws Throwable {
                        Integer id = (Integer) invocation.getArguments()[0];
                        FormData formData = new FormData();
                        formData.setId(id.longValue());
                        formData.setState(WorkflowState.ACCEPTED);
                        return formData;
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
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size() + 1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        // идентификатор шаблона источников (Приложение 5)
        int sourceTypeId = 372;
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//income//app5//v2012//");

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются в ручную
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        // формируем одну строку источника
        DataRow<Cell> row = sourceFormData.createDataRow();
        // графа 1
        row.getCell("number").setValue(1L, null);
        // графа 2
        row.getCell("regionBank").setValue(1L, null);
        // графа 3
        row.getCell("regionBankDivision").setValue(1L, null);
        // графа 4
        row.getCell("divisionName").setValue("testA", null);
        // графа 5
        row.getCell("kpp").setValue("123", null);
        // графа 5
        row.getCell("avepropertyPricerageCost").setValue(1L, null);
        // графа 6
        row.getCell("workersCount").setValue(1L, null);
        // графа 7
        row.getCell("subjectTaxCredit").setValue(1L, null);
        // графа 8
        row.getCell("decreaseTaxSum").setValue(1L, null);
        // графа 9
        row.getCell("taxRate").setValue(1L, null);
        dataRows.add(row);

        sourceDataRowHelper.save(dataRows);
        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        // должны получить 3 строки в приемнике: 1 строка из источника и 2 итоговые
        Assert.assertEquals(3, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertTrue("Logger must contains error level messages.", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }
}
