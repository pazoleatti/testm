package com.aplana.sbrf.taxaccounting.form_template.vat.vat_724_1_1.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * (724.1.1) Корректировка сумм НДС и налоговых вычетов за прошедшие налоговые периоды (v.2015).
 */
public class Vat_724_1_1Test extends ScriptTestBase {
    private static final int TYPE_ID = 848;
    private static final int DEPARTMENT_ID = 1;
    private static final int UNP_ID = 1;
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
        return getDefaultScriptTestMockHelper(Vat_724_1_1Test.class);
    }

    @Before
    public void mockFormDataService() {
        Department department = new Department();
        department.setId(UNP_ID);
        department.setName("Управление налогового планирования");
        when(testHelper.getDepartmentService().get(UNP_ID)).thenReturn(department);
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
        when(testHelper.getFormDataService().getRefBookRecord(anyLong(), anyMap(), anyMap(), anyMap(), anyString(),
                anyString(), any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Long refBookId = (Long) invocation.getArguments()[0];
                        if (refBookId != 8L){
                            return null;
                        }
                        String value = (String) invocation.getArguments()[5];
                        if (value == null || "".equals(value.trim())) {
                            return null;
                        }
                        String[] periods = {"первый квартал", "второй квартал", "третий квартал", "четвёртый квартал"};
                        Long periodId = (long) Arrays.asList(periods).indexOf(value) + 1;
                        return testHelper.getFormDataService().getRefBookValue(8L, periodId, new HashMap<String, Map<String, RefBookValue>>());
                    }
                });
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
        testHelper.reset();
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Проверка пустой
    //@Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    ///@Test
    public void check1Test() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        checkLogger();
        testHelper.getLogger().clear();
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAllCached()) {
            dataRow.setImportIndex(null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    //@Test
    public void check2Test() {
        String name = "importFile2.xlsm";
        testHelper.setImportFileInputStream(getCustomInputStream(name));
        testHelper.setImportFileName(name);
        testHelper.execute(FormDataEvent.IMPORT);
        checkLogger();
        testHelper.getLogger().clear();
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAllCached()) {
            dataRow.setImportIndex(null);
        }

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals("Строка 5: Должна быть заполнена хотя бы одна из граф с суммой корректировки (+, -) налоговой базы (раздел 1-7)!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 5: Графа с суммой корректировки (+) НДС (раздел 1-9) должна быть заполнена значением больше «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 5: Графа с суммой корректировки (-) НДС (раздел 1-9) должна быть заполнена значением меньше «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 5: Графа «Ставка НДС» заполнена неверно! Возможные значения (раздел 4): «10/110».", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 5: Графа «Сумма НДС по дополнительным листам книги продаж (разделы 1-7)/книги покупок (разделы 8-9)» должна быть заполнена суммой значений графы с суммой корректировки (+) и графы с суммой корректировки (-) НДС!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 8: Графа с суммой корректировки (+) налоговой базы (раздел 1-7) должна быть заполнена значением больше «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 8: Графа с суммой корректировки (-) налоговой базы (раздел 1-7) должна быть заполнена значением меньше «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 13: Графа «Данные бухгалтерского учёта. Величина корректировки налоговой базы. Налоговый период, за который вносится корректировка» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 13: Графа «Данные бухгалтерского учёта. Величина корректировки налоговой базы. Номер балансового счёта» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 13: Графа «Ставка НДС» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 13: Графа «Сумма НДС по дополнительным листам книги продаж (разделы 1-7)/книги покупок (разделы 8-9)» не заполнена!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 13: Должна быть заполнена хотя бы одна из граф с суммой корректировки (+, -) НДС (раздел 1-9)!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 17: Графы с суммой корректировки (+, -) налоговой базы (раздел 8-9) не должны быть заполнены!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 17: Графа «Данные бухгалтерского учёта. Величина корректировки НДС. Номер балансового счёта» заполнена неверно! Возможные значения (раздел 8): пустое значение, «6030901», «6030904», «6030905».", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «второй квартал» не имеет строки итога!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 10: Строка итога не относится к какой-либо группе!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «четвёртый квартал» не имеет строки итога!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 15: Строка итога не относится к какой-либо группе!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа строк «четвёртый квартал» не имеет строки «ВСЕГО по дополнительному листу книги продаж за четвёртый квартал по ставке 18%»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    // Расчет пустой
    //@Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 1;

        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        // ошибок быть не должно
        checkLogger();
        // Количество строк должно увеличиться на 1
        Assert.assertEquals("Add new row", expected, testHelper.getDataRowHelper().getAll().size());

        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        // Количество строк должно уменьшиться на 1
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        expected--;
        // Количество строк должно уменьшиться на 1
        Assert.assertEquals("Delete row", expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 9;
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        compareRow(dataRows.get(0), "1. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 18%");
        compareRow(dataRows.get(1), "2. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 10%");
        compareRow(dataRows.get(2), "3. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 18/118");
        compareRow(dataRows.get(3), "4. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 10/110");
        compareRow(dataRows.get(4), null, 2L, 1L, 12.00, -11.00, null, 34.00, -24.00, "10/110", 10.00);
        compareRow(dataRows.get(5), "Итого за второй квартал", null, null, 1.00, null, null, 10.00, null, null, 10.00);
        compareRow(dataRows.get(6), "5. Суммы полученной оплаты (частичной оплаты) в счёт предстоящего оказания услуг по расчётной ставке исчисления налога от суммы полученного дохода 18/118");
        compareRow(dataRows.get(7), "6. Суммы, полученные в виде штрафов, пени, неустоек по расчётной ставке исчисления налога от общей суммы полученного дохода 18/118");
        compareRow(dataRows.get(8), null, 3L, 5L, 1.00, -5.00, 5L, 7.00, -1.00, "18/118", 6.00);
        compareRow(dataRows.get(9), "Итого за третий квартал", null, null, -4.00, null, null, 6.00, null, null, 6.00);
        compareRow(dataRows.get(10), null, 4L, 5L, 1.00, -2.00, 5L, 3.00, -4.00, "18/118", -1.00);
        compareRow(dataRows.get(11), "Итого за четвёртый квартал", null, null, -1.00, null, null, -1.00, null, null, -1.00);
        compareRow(dataRows.get(12), "ВСЕГО по разделам 1-6", null, null, null, null, null, 15.00, null, null, null);
        compareRow(dataRows.get(13), "7. Суммы налога, перечисленные налоговым агентом");
        compareRow(dataRows.get(14), "ВСЕГО по дополнительному листу книги продаж за третий квартал по ставке 18%", null, null, -4.00, null, null, 6.00, null, null, 6.00);
        compareRow(dataRows.get(15), "ВСЕГО по дополнительному листу книги продаж за четвёртый квартал по ставке 18%", null, null, -1.00, null, null, -1.00, null, null, -1.00);
        compareRow(dataRows.get(16), "ВСЕГО по дополнительному листу книги продаж за второй квартал по ставке 10%", null, null, 1.00, null, null, 10.00, null, null, 10.00);
        compareRow(dataRows.get(17), "8. Налоговые вычеты по банковским операциям (авансы)");
        compareRow(dataRows.get(18), "9. Налоговые вычеты по банковским операциям (возвраты, уменьшение объема, стоимости услуг)");
    }

    void compareRow(DataRow<Cell> row, Object... args) {
        int skipColumnCount = 1;
        List<Column> columns = testHelper.getFormTemplate().getColumns();
        for (int i = 0; i < (columns.size() - skipColumnCount); i++) {
            int columnCount = i + skipColumnCount;
            Column column = columns.get(columnCount);
            Object expected = null;
            String alias = column.getAlias();
            if (i < args.length) {
                expected = args[i];
            }
            if (expected != null) {
                if (column.getColumnType() == ColumnType.NUMBER) {
                    expected = BigDecimal.valueOf((Double)expected).setScale(((NumericColumn)column).getPrecision(), BigDecimal.ROUND_HALF_UP);
                }
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, row.getCell(alias).getValue());
            } else {
                Assert.assertNull("row." + alias + "[" + row.getIndex() + "]", row.getCell(alias).getValue());
            }
        }
    }

    // Консолидация
    //@Test
    public void composeTest() {
        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        FormType formType = new FormType();
        formType.setId(TYPE_ID);
        formType.setTaxType(TaxType.VAT);

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        sourceFormData.setFormType(formType);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
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
        int expected = testHelper.getDataRowHelper().getAll().size() + 2;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    // Округляет число до требуемой точности
    BigDecimal roundValue(Long value, int precision) {
        if (value != null) {
            return (BigDecimal.valueOf(value)).setScale(precision, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }
}