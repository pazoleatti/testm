package com.aplana.sbrf.taxaccounting.form_template.deal.app_4_1.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Приложение 4.1. (6 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения
 *
 * TODO:
 *      - добавить источников в методе getFillDataRows()
 */
public class App_4_1Test extends ScriptTestBase {
    private static final int TYPE_ID = 801;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();
    static {
        formTypeIdByTemplateIdMap.put(800, 800); // ВЗЛ
        formTypeIdByTemplateIdMap.put(810, 810); // Прогноз крупных сделок
        formTypeIdByTemplateIdMap.put(807, 807); // Журнал взаиморасчетов
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
        templatesPathMap.put(800, "..//src/main//resources//form_template//deal//related_persons//v2015//");
        templatesPathMap.put(810, "..//src/main//resources//form_template//deal//forecast_major_transactions//v2015//");
        templatesPathMap.put(807, "..//src/main//resources//form_template//deal//journal_settlements//v2015//");
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

    private static Map<Long, RefBookDataProvider> providers = new HashMap<Long, RefBookDataProvider>();

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
        return getDefaultScriptTestMockHelper(App_4_1Test.class);
    }

    @Before
    public void mockServices() {
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
    public void check1TestNew() throws ParseException {
        // провайдеры и получения записей для справочников
        mockProviders();

        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        String msg;
        int i;
        int rowIndex = row.getIndex();
        String [] nonEmptyColumns = { "name", "sum44", "sum46", "sum54", "sum56", "sum6", "category", "sum8", "sum9", "categoryRevised" };
        // Дополнительная проверка - ошибок быть не должно
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("name").setValue(3L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        checkLogger();
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 1. Проверка заполнения обязательных полей
        // очистить значения
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(null, rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);
        // должно быть много сообщении об незаполненности обязательных полей
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
        i = 0;
        for (String alias : nonEmptyColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        // лишние проверки 2 и 3
        Assert.assertEquals("Строка 1: Организация «null» не является взаимозависимым лицом с общим режимом налогообложения в данном отчетном периоде!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном отчетном периоде!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 2. Проверка на отсутствие в списке не ВЗЛ ОРН
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 1: Организация «A» не является взаимозависимым лицом с общим режимом налогообложения в данном отчетном периоде!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 3. Наличие правила назначения категории
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("name").setValue(3L, rowIndex);
        row.getCell("sum9").setValue(-1L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном отчетном периоде!", entries.get(i++).getMessage());
        // лишняя проверка 4
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов указана неверная категория!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 4. Проверка соответствия категории пороговым значениям
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("name").setValue(3L, rowIndex);
        row.getCell("categoryRevised").setValue(2L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов указана неверная категория!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        int expected = 0;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // консолидация - все источники сразу
    @Test
    public void composeTest() {
        Set<Integer> sourceTemplateIds = formTypeIdByTemplateIdMap.keySet();
        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>(sourceTemplateIds.size());
        FormDataKind kind = FormDataKind.PRIMARY;
        for (int sourceTemplateId : sourceTemplateIds) {
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

            // источник
            DepartmentFormType departmentFormType = getSource(sourceTypeId, sourceTypeId, kind);
            departmentFormTypes.add(departmentFormType);

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
        }
        // источник
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(departmentFormTypes);

        // провайдеры и получения записей для справочников
        mockProviders();

        // предыдущий период
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setId(1);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        // дополнительно для 520
        PagingResult<Map<String, RefBookValue>> recordVersions520 = new PagingResult<Map<String, RefBookValue>>();
        recordVersions520.addAll(testHelper.getRefBookAllRecords(520L).values());
        when(providers.get(520L).getRecordVersionsById(anyLong(), any(PagingParams.class),
                anyString(), any(RefBookAttribute.class))).thenReturn(recordVersions520);

        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        int expected = 2; // 2 подходящие записи из справочника 520
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Получить источник.
     *
     * @param id идентификатор источника
     * @param sourceTypeId идентификатор типа формы источника
     * @param kind вид формы источника
     */
    private DepartmentFormType getSource(int id, int sourceTypeId, FormDataKind kind) {
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setId(id);
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setKind(kind);
        departmentFormType.setTaxType(TaxType.DEAL);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
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
        FormTemplate sourceTemplate = testHelper.getTemplate(templatesPathMap.get(sourceTemplateId));

        FormType formType = new FormType();
        formType.setId(formTypeIdByTemplateIdMap.get(sourceTemplateId));
        formType.setTaxType(TaxType.DEAL);
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
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "test_" + sourceTemplateId;
        Date testDate = new Date();
        Long testLong = 100L;
        Long testRefbookId = 2L;
        DataRow<Cell> row;
        switch (sourceTemplateId) {
            case 800 : // ВЗЛ
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId, null);
                row.getCell("category").setValue(182L, null);
                dataRows.add(row);
                break;
            case 810 : // Прогноз крупных сделок
                row = sourceFormData.createDataRow();
                row.getCell("ikksr").setValue(testRefbookId, null);
                row.getCell("sum").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 807 : // Журнал взаиморасчетов
                row = sourceFormData.createDataRow();
                row.getCell("statReportId1").setValue(testRefbookId, null);
                row.getCell("statReportId2").setValue(testRefbookId, null);
                row.getCell("sbrfCode1").setValue(testLong, null);
                row.getCell("sum").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 818 : // РНУ-101
                row = sourceFormData.createDataRow();
                row.setAlias("testAlias");
                row.getCell("name").setValue(testRefbookId, null);
                row.getCell("sum3").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 820 : // РНУ-102
            case 821 : // РНУ-107
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
                // TODO (Ramil Timerbaev) дополнить
                break;
        }
        return dataRows;
    }

    /** Проверить данные. */
    void checkData(List<DataRow<Cell>> dataRows) {
        // графа 1  (1) - нумерация
        // графа 2  (2)
        Assert.assertEquals(3L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        // графа 3  (3) - завимая графа
        // графа 4  (4.1)
        Assert.assertNull(dataRows.get(0).getCell("sum4").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum4").getNumericValue());
        // графа 5  (4.2)
        Assert.assertNull(dataRows.get(0).getCell("sum42").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum42").getNumericValue());
        // графа 6  (4.3)
        Assert.assertNull(dataRows.get(0).getCell("sum43").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum43").getNumericValue());
        // графа 7  (4.4)
        Assert.assertEquals(0L, dataRows.get(0).getCell("sum44").getNumericValue().longValue());
        Assert.assertEquals(100000L, dataRows.get(1).getCell("sum44").getNumericValue().longValue());
        // графа 8  (4.5)
        Assert.assertNull(dataRows.get(0).getCell("sum45").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum45").getNumericValue());
        // графа 9  (4.6)
        Assert.assertEquals(0L, dataRows.get(0).getCell("sum46").getNumericValue().longValue());
        Assert.assertEquals(100L, dataRows.get(1).getCell("sum46").getNumericValue().longValue());
        // графа 10 (5.1)
        Assert.assertNull(dataRows.get(0).getCell("sum51").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum51").getNumericValue());
        // графа 11 (5.2)
        Assert.assertNull(dataRows.get(0).getCell("sum52").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum52").getNumericValue());
        // графа 12 (5.3)
        Assert.assertNull(dataRows.get(0).getCell("sum53").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum53").getNumericValue());
        // графа 13 (5.4)
        Assert.assertEquals(0L, dataRows.get(0).getCell("sum54").getNumericValue().longValue());
        Assert.assertEquals(0L, dataRows.get(1).getCell("sum54").getNumericValue().longValue());
        // графа 14 (5.5)
        Assert.assertNull(dataRows.get(0).getCell("sum55").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("sum55").getNumericValue());
        // графа 15 (5.6)
        Assert.assertEquals(0L, dataRows.get(0).getCell("sum56").getNumericValue().longValue());
        Assert.assertEquals(0L, dataRows.get(1).getCell("sum56").getNumericValue().longValue());
        // графа 16 (6)
        Assert.assertEquals(0L, dataRows.get(0).getCell("sum6").getNumericValue().longValue());
        DataRow<Cell> row = dataRows.get(1);
        String [] calc17Aliases = { "sum4", "sum42", "sum43", "sum44", "sum45", "sum46", "sum51", "sum52", "sum53", "sum54", "sum55", "sum56" };
        long expected16 = 0L;
        for (String alias : calc17Aliases) {
            BigDecimal valueTmp =  dataRows.get(1).getCell(alias).getNumericValue();
            expected16 += (valueTmp != null ? valueTmp.longValue() : 0);
        }
        Assert.assertEquals(expected16, row.getCell("sum6").getNumericValue().longValue());
        // графа 17 (7)
        Assert.assertEquals(182L, dataRows.get(0).getCell("category").getNumericValue().longValue());
        Assert.assertEquals(182L, dataRows.get(1).getCell("category").getNumericValue().longValue());
        // графа 18 (8)
        long expected18 = expected16 * 2;
        Assert.assertEquals(0L, dataRows.get(0).getCell("sum8").getNumericValue().longValue());
        Assert.assertEquals(expected18, dataRows.get(1).getCell("sum8").getNumericValue().longValue());
        // графа 19 (9)
        long expected19 = expected18 + 100L;
        Assert.assertEquals(0L, dataRows.get(0).getCell("sum9").getNumericValue().longValue());
        Assert.assertEquals(expected19, dataRows.get(1).getCell("sum9").getNumericValue().longValue());
        // графа 20 (10)
        Assert.assertEquals(183L, dataRows.get(0).getCell("categoryRevised").getNumericValue().longValue());
        Assert.assertEquals(3L, dataRows.get(1).getCell("categoryRevised").getNumericValue().longValue());
        // графа 21 (11)
        Assert.assertNull(dataRows.get(0).getCell("categoryPrimary").getNumericValue());
        Assert.assertNull(dataRows.get(1).getCell("categoryPrimary").getNumericValue());
    }

    private void mockProviders() {
        long [] refBookIds = { 505L, 506L, 511L, 515L, 520L, 525L };
        for (long id : refBookIds) {
            mockProvider(id);
        }
    }

    private void mockProvider(final Long refBookId) {
        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
        providers.put(refBookId, provider);
        provider.setRefBookId(refBookId);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(refBookId),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(provider);

        // вернуть все записи справочника
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(new Answer<PagingResult<Map<String, RefBookValue>>>() {
            @Override
            public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
                result.addAll(testHelper.getRefBookAllRecords(refBookId).values());
                return result;
            }
        });
    }
}