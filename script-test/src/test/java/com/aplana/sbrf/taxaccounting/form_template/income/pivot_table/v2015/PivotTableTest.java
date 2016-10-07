package com.aplana.sbrf.taxaccounting.form_template.income.pivot_table.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 850 - Сводная таблица - Лист 08 декларации по прибыли
 */
public class PivotTableTest extends ScriptTestBase {
    private static final int TYPE_ID = 850;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();
    static {
        formTypeIdByTemplateIdMap.put(818, 818); // РНУ-101
        formTypeIdByTemplateIdMap.put(820, 820); // РНУ-102
        formTypeIdByTemplateIdMap.put(821, 821); // РНУ-107
        formTypeIdByTemplateIdMap.put(822, 822); // РНУ-110
        formTypeIdByTemplateIdMap.put(808, 808); // РНУ-111
        formTypeIdByTemplateIdMap.put(824, 824); // РНУ-112
        formTypeIdByTemplateIdMap.put(829, 829); // РНУ-114
        formTypeIdByTemplateIdMap.put(842, 842); // РНУ-115
        formTypeIdByTemplateIdMap.put(844, 844); // РНУ-116
        formTypeIdByTemplateIdMap.put(809, 809); // РНУ-117
        formTypeIdByTemplateIdMap.put(840, 840); // РНУ-122
        formTypeIdByTemplateIdMap.put(841, 841); // РНУ-123
        formTypeIdByTemplateIdMap.put(843, 843); // РНУ-171
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();
    static {
        templatesPathMap.put(818, "..//src/main//resources//form_template//income//rnu_101//v2015//");
        templatesPathMap.put(820, "..//src/main//resources//form_template//income//rnu_102//v2015//");
        templatesPathMap.put(821, "..//src/main//resources//form_template//income//rnu_107//v2015//");
        templatesPathMap.put(822, "..//src/main//resources//form_template//income//rnu_110//v2015//");
        templatesPathMap.put(808, "..//src/main//resources//form_template//income//rnu_111//v2015//");
        templatesPathMap.put(824, "..//src/main//resources//form_template//income//rnu_112//v2015//");
        templatesPathMap.put(829, "..//src/main//resources//form_template//income//rnu_114//v2015//");
        templatesPathMap.put(842, "..//src/main//resources//form_template//income//rnu_115//v2015//");
        templatesPathMap.put(844, "..//src/main//resources//form_template//income//rnu_116//v2015//");
        templatesPathMap.put(809, "..//src/main//resources//form_template//income//rnu_117//v2015//");
        templatesPathMap.put(840, "..//src/main//resources//form_template//income//rnu_122//v2015//");
        templatesPathMap.put(841, "..//src/main//resources//form_template//income//rnu_123//v2015//");
        templatesPathMap.put(843, "..//src/main//resources//form_template//income//rnu_171//v2015//");
    }

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
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(PivotTableTest.class);
    }

    @Before
    public void mockProvider() {
        long refBookId = 541L;
        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refBookId);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(refBookId),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(provider);

        // вернуть все записи справочника
        Map<Long, Map<String, RefBookValue>> refBookAllRecords = testHelper.getRefBookAllRecords(refBookId);
        ArrayList<Long> recordIds = new ArrayList<Long>(refBookAllRecords.keySet());
        when(provider.getUniqueRecordIds(any(Date.class), isNull(String.class))).thenReturn(recordIds);
        when(provider.getRecordData(eq(recordIds))).thenReturn(refBookAllRecords);
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

    // 1. Проверка на заполнение граф
    @Test
    public void check1Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        String[] nonEmptyColumns = {"corrType", "base", "name"};
        testHelper.execute(FormDataEvent.CHECK);

        for (String alias : nonEmptyColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format(WRONG_NON_EMPTY, rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 2. Заполнение идентификационного номера для иностранной организации
    @Test
    public void check2Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("123;", rowIndex);
        row.getCell("name").setValue(1L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Для иностранной организации должна быть заполнена хотя бы одна из граф: «%s», «%s»!",
                rowIndex, getColumnName(row, "innKio"), getColumnName(row, "rsk"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 3. Заполнение ИНН для российской организации
    @Test
    public void check3Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("123;", rowIndex);
        row.getCell("name").setValue(2L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Графа «%s» для российской организации обязательна к заполнению!",
                rowIndex, getColumnName(row, "innKio"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 4. Уникальные основания отнесения сделки к контролируемой
    @Test
    public void check4Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("123;123;", rowIndex);
        row.getCell("name").setValue(3L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Значения в графе «%s» не должны повторяться!",
                rowIndex, getColumnName(row, "base"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 5. Корректное заполнение основания отнесения сделки к контролируемой
    @Test
    public void check5Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();

        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("122;131;", rowIndex);
        row.getCell("name").setValue(3L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Коды 122 и 123 не могут быть одновременно указаны с любым из кодов 131- 135!", rowIndex);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        row.getCell("base").setValue("123;135;", rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        i = 0;
        msg = String.format("Строка %s: Коды 122 и 123 не могут быть одновременно указаны с любым из кодов 131- 135!", rowIndex);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // Проверка наличия кодов в справочнике "Коды основания отнесения сделки к контролируемой"
    @Test
    public void checkBaseTest() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();

        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("122;125;", rowIndex);
        row.getCell("name").setValue(3L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Строка %s: Записи с кодами «125» отсутствуют в справочнике «Коды основания отнесения сделки к контролируемой»!", rowIndex);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 6. Проверка сумм доходов и расходов
    @Test
    public void check6Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("121;123;", rowIndex);
        row.getCell("name").setValue(3L, rowIndex);
        row.getCell("sum010").setValue(0L, rowIndex);
        row.getCell("sum020").setValue(-1L, rowIndex);
        row.getCell("sum030").setValue(-2L, rowIndex);
        row.getCell("sum040").setValue(0L, rowIndex);
        row.getCell("sum050").setValue(0L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);

        for (String column : new String[]{"sum010", "sum020", "sum030", "sum040"}){
            msg = String.format("Строка %s: Значение графы «%s» должно быть больше 0!", row.getIndex(), getColumnName(row, column));
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // 6. Проверка сумм доходов и расходов
    @Test
    public void check7Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        String msg;
        int i = 0;
        int rowIndex = row.getIndex();
        row.getCell("code").setValue("19000", rowIndex);
        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("121;123;", rowIndex);
        row.getCell("name").setValue(3L, rowIndex);
        row.getCell("sum010").setValue(10L, rowIndex);
        row.getCell("sum020").setValue(11L, rowIndex);
        row.getCell("sum030").setValue(12L, rowIndex);
        row.getCell("sum040").setValue(13L, rowIndex);
        row.getCell("sum050").setValue(14L, rowIndex);

        DataRow codeDataRow = getDataRow(dataRows, "code1_1");
        rowIndex = codeDataRow.getIndex();
        codeDataRow.getCell("sum010").setValue(11L, rowIndex); // правильно 10
        codeDataRow.getCell("sum050").setValue(11L, rowIndex); // правильно 10
        testHelper.execute(FormDataEvent.CHECK);

        msg = String.format("Неверное итоговое значение в графе «%s» по группе КНУ «%s»!",
                getColumnName(codeDataRow, "sum010"), "19000");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Неверное итоговое значение в графе «%s» по группе КНУ «%s»!",
                getColumnName(codeDataRow, "sum050"), "19000");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    @Test
    public void calc2Test() {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(2, row);

        int rowIndex = row.getIndex();
        row.getCell("code").setValue("19000", rowIndex);
        row.getCell("corrType").setValue(1L, rowIndex);
        row.getCell("base").setValue("122;123;", rowIndex);
        row.getCell("name").setValue(3L, rowIndex);
        row.getCell("sum010").setValue(10L, rowIndex);
        row.getCell("sum020").setValue(11L, rowIndex);
        row.getCell("sum030").setValue(12L, rowIndex);
        row.getCell("sum040").setValue(13L, rowIndex);
        row.getCell("sum050").setValue(14L, rowIndex);

        DataRow codeDataRow = getDataRow(dataRows, "code1_1");
        rowIndex = codeDataRow.getIndex();
        codeDataRow.getCell("sum010").setValue(10L, rowIndex); // правильно 10
        codeDataRow.getCell("sum050").setValue(10L, rowIndex); // правильно 10

        testHelper.execute(FormDataEvent.CALCULATE);

        DataRow totalRow = getDataRow(dataRows, "total");

        Assert.assertEquals(new BigDecimal("10"), totalRow.getCell("sum010").getValue());
        Assert.assertEquals(BigDecimal.ZERO, totalRow.getCell("sum020").getValue());
        Assert.assertEquals(BigDecimal.ZERO, totalRow.getCell("sum030").getValue());
        Assert.assertEquals(BigDecimal.ZERO, totalRow.getCell("sum040").getValue());
        Assert.assertEquals(new BigDecimal("10"), totalRow.getCell("sum050").getValue());
        checkLogger();
    }

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        when(testHelper.getFormDataService().getFormTemplate(TYPE_ID)).thenReturn(testHelper.getFormTemplate());
        int expected = 34;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // консолидация - все источники сразу
    @Test
    public void composeTest() {
        Set<Integer> sourceTemplateIds = formTypeIdByTemplateIdMap.keySet();
        List<Relation> sourcesInfo = new ArrayList<Relation>(sourceTemplateIds.size());
        FormDataKind kind = FormDataKind.PRIMARY;
        for (int sourceTemplateId : sourceTemplateIds) {
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

            long sourceFormDataId = sourceTemplateId;
            // источник
            Relation relation = getSource(sourceFormDataId, sourceTypeId, kind);
            sourcesInfo.add(relation);

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceFormDataId, sourceTemplateId);
            when(testHelper.getFormDataService().get(eq(sourceFormDataId), isNull(Boolean.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
        }
        // источник
        when(testHelper.getFormDataService().getSourcesInfo(eq(testHelper.getFormData()), eq(false), eq(true), eq(WorkflowState.ACCEPTED),
                any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        int expected = 34 + 5;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Получить источник.
     * @param id идентификатор источника
     * @param sourceTypeId идентификатор типа формы источника
     * @param kind вид формы источника
     */
    private Relation getSource(long id, final int sourceTypeId, FormDataKind kind) {
        Relation relation = new Relation();
        relation.setFormDataId(id);
        relation.setFormType(new FormType() {{
            setId(sourceTypeId);
        }});
        relation.setFormDataKind(kind);
        return relation;
    }

    /**
     * Получить форму источника.
     *  @param id идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(long id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate(templatesPathMap.get(sourceTemplateId));

        FormType formType = new FormType();
        formType.setId(formTypeIdByTemplateIdMap.get(sourceTemplateId));
        formType.setTaxType(TaxType.DEAL);
        formType.setName(sourceTemplate.getName());

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId(id);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        sourceFormData.setFormType(formType);
        sourceFormData.setFormTemplateId(sourceTemplateId);

        return sourceFormData;
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        Long testLong = 100L;
        Long testRefbookId = 3L;
        Long testRefbookId2 = 4L;
        Long testRefbookId3 = 5L;
        Long testRefbookId4 = 6L;
        DataRow<Cell> row;
        switch (sourceTemplateId) {
            case 818 : // РНУ-101 (2 варианта заполнения графы 5)
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId, null);
                row.getCell("incomeCode").setValue("19000", null);
                row.getCell("sum3").setValue(testLong, null);
                dataRows.add(row);
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId2, null);
                row.getCell("incomeCode").setValue("19030", null);
                row.getCell("sum3").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 820 : // РНУ-102 (еще 2 варианта заполнения графы 5)
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId3, null);
                row.getCell("outcomeCode").setValue("19360", null);
                row.getCell("sum3").setValue(testLong, null);
                dataRows.add(row);
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId4, null);
                row.getCell("outcomeCode").setValue("19390", null);
                row.getCell("sum3").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 821 : // РНУ-107 (2 строки сливаются в 1 в приемнике)
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId, null);
                row.getCell("code").setValue("19060", null);
                row.getCell("sum4").setValue(testLong, null);
                dataRows.add(row);
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId, null);
                row.getCell("code").setValue("19060", null);
                row.getCell("sum4").setValue(testLong, null);
                dataRows.add(row);
                break;
            // остальные формы подобны
            case 822 : // РНУ-110
            case 808 : // РНУ-111
            case 824 : // РНУ-112
            case 829 : // РНУ-114
            case 842 : // РНУ-115
            case 844 : // РНУ-116
            case 809 : // РНУ-117
            case 840 : // РНУ-122
            case 841 : // РНУ-123
            case 843 : // РНУ-171
                break;
        }
        return dataRows;
    }
    /** Проверить данные. */
    private void checkData(List<DataRow<Cell>> dataRows) {
        Long testLong = 100L;
        Long testRefbookId = 3L;
        Long testRefbookId2 = 4L;
        Long testRefbookId3 = 5L;
        Long testRefbookId4 = 6L;
        String baseVzlTaxStatus1 = "121;134;";
        String baseVzlTaxStatus2 = "121;131;";
        String baseNl = "121;";
        String baseRoz = "123;";
        // РНУ-101
        DataRow<Cell> row = dataRows.get(2);
        Assert.assertEquals("19000", row.getCell("code").getValue());
        Assert.assertEquals(1L, row.getCell("corrType").getValue());
        Assert.assertEquals(testRefbookId, row.getCell("name").getValue());
        Assert.assertEquals(baseNl, row.getCell("base").getValue());
        Assert.assertEquals(new BigDecimal("1"), row.getCell("sign010").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum010").getValue());
        Assert.assertEquals(new BigDecimal("1"), row.getCell("sign050").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum050").getValue());
        row = dataRows.get(4);
        Assert.assertEquals("19030", row.getCell("code").getValue());
        Assert.assertEquals(1L, row.getCell("corrType").getValue());
        Assert.assertEquals(testRefbookId2, row.getCell("name").getValue());
        Assert.assertEquals(baseVzlTaxStatus1, row.getCell("base").getValue());
        Assert.assertEquals(new BigDecimal("1"), row.getCell("sign020").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum020").getValue());
        Assert.assertEquals(new BigDecimal("1"), row.getCell("sign050").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum050").getValue());
        // РНУ-102
        row = dataRows.get(7);
        Assert.assertEquals("19360", row.getCell("code").getValue());
        Assert.assertEquals(1L, row.getCell("corrType").getValue());
        Assert.assertEquals(testRefbookId3, row.getCell("name").getValue());
        Assert.assertEquals(baseVzlTaxStatus2, row.getCell("base").getValue());
        Assert.assertEquals(new BigDecimal("0"), row.getCell("sign030").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum030").getValue());
        Assert.assertEquals(new BigDecimal("0"), row.getCell("sign050").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum050").getValue());
        row = dataRows.get(9);
        Assert.assertEquals("19390", row.getCell("code").getValue());
        Assert.assertEquals(1L, row.getCell("corrType").getValue());
        Assert.assertEquals(testRefbookId4, row.getCell("name").getValue());
        Assert.assertEquals(baseRoz, row.getCell("base").getValue());
        Assert.assertEquals(new BigDecimal("0"), row.getCell("sign040").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum040").getValue());
        Assert.assertEquals(new BigDecimal("0"), row.getCell("sign050").getValue());
        Assert.assertEquals(new BigDecimal(testLong.toString()), row.getCell("sum050").getValue());
        // РНУ-107
        row = dataRows.get(12);
        Assert.assertEquals("19060", row.getCell("code").getValue());
        Assert.assertEquals(1L, row.getCell("corrType").getValue());
        Assert.assertEquals(testRefbookId, row.getCell("name").getValue());
        Assert.assertEquals(baseNl, row.getCell("base").getValue());
        Assert.assertEquals(new BigDecimal("1"), row.getCell("sign010").getValue());
        Assert.assertEquals(new BigDecimal(String.valueOf(2 * testLong)), row.getCell("sum010").getValue());
        Assert.assertEquals(new BigDecimal("1"), row.getCell("sign050").getValue());
        Assert.assertEquals(new BigDecimal(String.valueOf(2 * testLong)), row.getCell("sum050").getValue());
    }
}