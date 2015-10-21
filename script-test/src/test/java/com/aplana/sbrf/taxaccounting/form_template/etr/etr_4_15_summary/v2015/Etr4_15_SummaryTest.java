package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_15_summary.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
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
 * Приложение 4-15. Анализ структуры налога на добавленную стоимость (НДС) (сводная).
 */
public class Etr4_15_SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 7150;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.CONSOLIDATED;
    private static final int SOURCE_TYPE_ID = 715;

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
        return getDefaultScriptTestMockHelper(Etr4_15_SummaryTest.class);
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
        // ПРОВЕРКА - пустая форма
        testHelper.execute(FormDataEvent.CHECK);
        // ожидаемый результат - должны быть незаполненые обязательные поля в итогах
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
                "Строка 1: Графа «Изменения за период. Доля НДС не учитываемый, %» не заполнена!"
        };
        for (String logMsg : logMsgs) {
            Assert.assertEquals(logMsg, testHelper.getLogger().getEntries().get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());

        // получение строк, в форме одна итогвая строка + добавить еще одну обычную
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        DataRow<Cell> newRow = formData.createDataRow();
        dataRows.add(0, newRow);
        i = 1;
        for (DataRow<Cell> row : dataRows) {
            row.setIndex(i);
            i++;
        }

        // ПРОВЕРКА - заполнение единицами
        String [] aliases = new String[] { "department", "comparePeriod", "comparePeriodIgnore", "comparePeriodPercent",
                "currentPeriod", "currentPeriodIgnore", "currentPeriodPercent", "delta", "deltaIgnore", "deltaPercent" };
        for (String alias : aliases) {
            for (DataRow<Cell> row : dataRows) {
                row.getCell(alias).setValue(1, null);
            }
        }
        testHelper.getLogger().clear();
        testHelper.execute(FormDataEvent.CHECK);
        // ожидаемый результат - нет ошибок
        Assert.assertEquals(0, testHelper.getLogger().getEntries().size());

        // ПРОВЕРКА - заполнение значениями
        Map<String, Double> map = new HashMap<String, Double>();
        map.put("department", 1.0);
        map.put("comparePeriod", 0.0);
        map.put("comparePeriodIgnore", 1.0);
        map.put("comparePeriodPercent", 100.0);
        map.put("currentPeriod", 0.0);
        map.put("currentPeriodIgnore", 1.0);
        map.put("currentPeriodPercent", 100.0);
        map.put("delta", 0.0);
        map.put("deltaIgnore", 0.0);
        map.put("deltaPercent", 0.0);
        for (String alias : map.keySet()) {
            for (DataRow<Cell> row : dataRows) {
                row.getCell(alias).setValue(map.get(alias), null);
            }
        }
        testHelper.getLogger().clear();
        testHelper.execute(FormDataEvent.CHECK);
        // ожидаемый результат - деление на ноль
        checkLogger();
        Assert.assertEquals(2, testHelper.getLogger().getEntries().size());
        for (String alias : map.keySet()) {
            for (DataRow<Cell> row : dataRows) {
                Cell cell = row.getCell(alias);
                Double value = (cell.getNumericValue() != null ? cell.getNumericValue().doubleValue() : null);
                Double expected = map.get(alias);
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
            }
        }
        i = 0;
        String [] logMsgs2 = new String [] {
                "Строка 2: Графа «Период сравнения. Доля НДС не учитываемый, %» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».",
                "Строка 2: Графа «Период. Доля НДС не учитываемый, %» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0»."
        };
        for (String logMsg : logMsgs2) {
            Assert.assertEquals(logMsg, testHelper.getLogger().getEntries().get(i++).getMessage());
        }
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

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 2, 3, 5, 6, 8, 9
        String [] calcColumns1 = { "comparePeriod", "comparePeriodIgnore", "currentPeriod", "currentPeriodIgnore", "delta", "deltaIgnore" };
        // графа 4, 7, 10
        String [] calcColumns2 = { "comparePeriodPercent", "currentPeriodPercent", "deltaPercent" };
        Long expected = 0L;
        for (DataRow<Cell> row : dataRows) {
            expected++;

            // графа 1
            if (!"total".equals(row.getAlias())) {
                // графу 1 проверять только у обычных строк
                Cell cell = row.getCell("department");
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertNotNull("row.department[" + row.getIndex() + "]", value);
            }
            for (String alias : calcColumns1) {
                Cell cell = row.getCell(alias);
                if ("total".equals(row.getAlias())) {
                    Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                    Assert.assertNotNull("row." + alias + "[" + row.getIndex() + "]", value);
                } else {
                    Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                    Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
                }
            }
            for (String alias : calcColumns2) {
                Cell cell = row.getCell(alias);
                Long value = (cell.getNumericValue() != null ? cell.getNumericValue().longValue() : null);
                Assert.assertNotNull("row." + alias + "[" + row.getIndex() + "]", value);
            }
        }
    }

    // Консолидация
    @Test
    public void composeTest() {
        int sourceTemplateId = SOURCE_TYPE_ID;
        FormDataKind kind = FormDataKind.CONSOLIDATED;
        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>();
        int sourceCount = 9;

        for (int i = 1; i <= sourceCount; i++) {
            // источник
            DepartmentFormType departmentFormType = getSource(i, kind, i);
            departmentFormTypes.add(departmentFormType);

            // форма источника
            FormData sourceFormData = getSourceFormData(i, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(anyInt(), eq(kind), eq(i),
                    anyInt(), any(Integer.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceFormData, (long) i);
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
        int expected = sourceCount + 1; // количество источников + 1 строка итоговая
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Получить источник.
     *
     * @param id идентификатор источника
     * @param kind вид формы источника
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
     * @param id идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(int id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//etr//etr_4_15//v2015//");

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
    private List<DataRow<Cell>> getFillDataRows(FormData sourceFormData, Long testLong) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> row = sourceFormData.createDataRow();
        row.setAlias("R1");

        // графа 1
        row.getCell("comparePeriod").setValue(testLong, null);
        // графа 2
        row.getCell("comparePeriodIgnore").setValue(testLong, null);
        // графа 3
        row.getCell("comparePeriodPercent").setValue(testLong, null);
        // графа 4
        row.getCell("currentPeriod").setValue(testLong, null);
        // графа 5
        row.getCell("currentPeriodIgnore").setValue(testLong, null);
        // графа 6
        row.getCell("currentPeriodPercent").setValue(testLong, null);
        // графа 7
        row.getCell("delta").setValue(testLong, null);
        // графа 8
        row.getCell("deltaIgnore").setValue(testLong, null);
        // графа 9
        row.getCell("deltaPercent").setValue(testLong, null);

        dataRows.add(row);
        return dataRows;
    }
}
