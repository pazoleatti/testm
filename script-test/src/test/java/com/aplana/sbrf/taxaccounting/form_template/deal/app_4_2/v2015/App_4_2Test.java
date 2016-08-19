package com.aplana.sbrf.taxaccounting.form_template.deal.app_4_2.v2015;

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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Приложение 4.2. Отчет в отношении доходов и расходов Банка по сделкам с ВЗЛ, РОЗ, НЛ по итогам окончания Налогового периода
 *
 * TODO:
 *      - добавить источников в методе getFillDataRows()
 *      - тесты для спецочета
 */
public class App_4_2Test extends ScriptTestBase {
    private static final int TYPE_ID = 803;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();
    static {
        formTypeIdByTemplateIdMap.put(807, 807); // Журнал взаиморасчетов
        formTypeIdByTemplateIdMap.put(854, 854); // Приложение 9
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
        formTypeIdByTemplateIdMap.put(816, 816); // 6.1
        formTypeIdByTemplateIdMap.put(804, 804); // 6.2
        formTypeIdByTemplateIdMap.put(812, 812); // 6.3
        formTypeIdByTemplateIdMap.put(813, 813); // 6.4
        formTypeIdByTemplateIdMap.put(814, 814); // 6.5
        formTypeIdByTemplateIdMap.put(806, 806); // 6.6
        formTypeIdByTemplateIdMap.put(805, 805); // 6.7
        formTypeIdByTemplateIdMap.put(815, 815); // 6.8
        formTypeIdByTemplateIdMap.put(817, 817); // 6.9
        formTypeIdByTemplateIdMap.put(823, 823); // 6.10-1
        formTypeIdByTemplateIdMap.put(825, 825); // 6.10-2
        formTypeIdByTemplateIdMap.put(827, 827); // 6.11
        formTypeIdByTemplateIdMap.put(819, 819); // 6.12
        formTypeIdByTemplateIdMap.put(826, 826); // 6.13
        formTypeIdByTemplateIdMap.put(835, 835); // 6.14
        formTypeIdByTemplateIdMap.put(837, 837); // 6.15
        formTypeIdByTemplateIdMap.put(839, 839); // 6.16
        formTypeIdByTemplateIdMap.put(811, 811); // 6.17
        formTypeIdByTemplateIdMap.put(838, 838); // 6.18
        formTypeIdByTemplateIdMap.put(828, 828); // 6.19
        formTypeIdByTemplateIdMap.put(831, 831); // 6.20
        formTypeIdByTemplateIdMap.put(830, 830); // 6.21
        formTypeIdByTemplateIdMap.put(834, 834); // 6.22
        formTypeIdByTemplateIdMap.put(832, 832); // 6.23
        formTypeIdByTemplateIdMap.put(833, 833); // 6.24
        formTypeIdByTemplateIdMap.put(836, 836); // 6.25
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();
    static {
        templatesPathMap.put(807, "..//src/main//resources//form_template//deal//journal_settlements//v2015//");
        templatesPathMap.put(854, "..//src/main//resources//form_template//deal//app_9//v2015//");
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
        templatesPathMap.put(816, "..//src/main//resources//form_template//deal//app_6_1//v2015//");
        templatesPathMap.put(804, "..//src/main//resources//form_template//deal//app_6_2//v2015//");
        templatesPathMap.put(812, "..//src/main//resources//form_template//deal//app_6_3//v2015//");
        templatesPathMap.put(813, "..//src/main//resources//form_template//deal//app_6_4//v2015//");
        templatesPathMap.put(814, "..//src/main//resources//form_template//deal//app_6_5//v2015//");
        templatesPathMap.put(806, "..//src/main//resources//form_template//deal//app_6_6//v2015//");
        templatesPathMap.put(805, "..//src/main//resources//form_template//deal//app_6_7//v2015//");
        templatesPathMap.put(815, "..//src/main//resources//form_template//deal//app_6_8//v2015//");
        templatesPathMap.put(817, "..//src/main//resources//form_template//deal//app_6_9//v2015//");
        templatesPathMap.put(823, "..//src/main//resources//form_template//deal//app_6_10_1//v2015//");
        templatesPathMap.put(825, "..//src/main//resources//form_template//deal//app_6_10_2//v2015//");
        templatesPathMap.put(827, "..//src/main//resources//form_template//deal//app_6_11//v2015//");
        templatesPathMap.put(819, "..//src/main//resources//form_template//deal//app_6_12//v2015//");
        templatesPathMap.put(826, "..//src/main//resources//form_template//deal//app_6_13//v2015//");
        templatesPathMap.put(835, "..//src/main//resources//form_template//deal//app_6_14//v2015//");
        templatesPathMap.put(837, "..//src/main//resources//form_template//deal//app_6_15//v2015//");
        templatesPathMap.put(839, "..//src/main//resources//form_template//deal//app_6_16//v2015//");
        templatesPathMap.put(811, "..//src/main//resources//form_template//deal//app_6_17//v2015//");
        templatesPathMap.put(838, "..//src/main//resources//form_template//deal//app_6_18//v2015//");
        templatesPathMap.put(828, "..//src/main//resources//form_template//deal//app_6_19//v2015//");
        templatesPathMap.put(831, "..//src/main//resources//form_template//deal//app_6_20//v2015//");
        templatesPathMap.put(830, "..//src/main//resources//form_template//deal//app_6_21//v2015//");
        templatesPathMap.put(834, "..//src/main//resources//form_template//deal//app_6_22//v2015//");
        templatesPathMap.put(832, "..//src/main//resources//form_template//deal//app_6_23//v2015//");
        templatesPathMap.put(833, "..//src/main//resources//form_template//deal//app_6_24//v2015//");
        templatesPathMap.put(836, "..//src/main//resources//form_template//deal//app_6_25//v2015//");
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
        return getDefaultScriptTestMockHelper(App_4_2Test.class);
    }

    @Before
    public void mockServices() {
    }

    @After
    public void resetMock() {
        for (RefBookDataProvider provider : providers.values()) {
            reset(provider);
        }
        reset(testHelper.getRefBookDataProvider());
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

    // Расчет с данными
    @Test
    public void calc1Test() {
        // провайдеры и получения записей для справочников
        mockProviders();

        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        int rowIndex = row.getIndex();
        String [] nonEmptyColumns = { "name", "group", "sum51", "sum52", "sum53", "sum54", "sum55", "sum56", "sum61", "sum62", "sum63",
                "sum64", "sum65", "sum66", "sum7", "thresholdValue", "sign", "categoryRevised" };

        // ошибок быть не должно
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();

        // проверка расчета графы 4, 17..20
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("name").setValue(4L, rowIndex);
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
        // графа 4 - РОЗ и НЛ
        Assert.assertEquals(3L, row.getCell("group").getNumericValue().longValue());
        // графа 17
        Assert.assertEquals(12L, row.getCell("sum7").getNumericValue().longValue());
        // графа 18
        Assert.assertEquals(3L, row.getCell("thresholdValue").getNumericValue().longValue());
        // графа 19
        Assert.assertEquals(2L, row.getCell("sign").getNumericValue().longValue());
        // графа 20
        Assert.assertEquals(5L, row.getCell("categoryRevised").getNumericValue().longValue());

        // проверка расчета только графы 4 - ИВЗЛ
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("name").setValue(2L, rowIndex);
        testHelper.execute(FormDataEvent.CALCULATE);
        // графа 4 - ИВЗЛ
        Assert.assertEquals(3L, row.getCell("group").getNumericValue().longValue());

        // проверка расчета только графы 4 - ВЗЛ СРН
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("name").setValue(3L, rowIndex);
        testHelper.execute(FormDataEvent.CALCULATE);
        // графа 4 - ВЗЛ СРН
        Assert.assertEquals(5L, row.getCell("group").getNumericValue().longValue());
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
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
        String [] nonEmptyColumns = { "name", "group", "sum51", "sum52", "sum53", "sum54", "sum55", "sum56", "sum61", "sum62", "sum63",
                "sum64", "sum65", "sum66", "sum7", "thresholdValue", "sign", "categoryRevised" };

        // Дополнительная проверка - ошибок быть не должно
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
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
        Assert.assertEquals("Строка 1: Для типа участника ТЦО «null» не задано пороговое значение в данном Налоговом периоде!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном Налоговом периоде!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 2. Наличие порогового значения
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("group").setValue(4L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 1: Для типа участника ТЦО «TEST» не задано пороговое значение в данном Налоговом периоде!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 3. Наличие правила назначения категории
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
        row.getCell("group").setValue(5L, rowIndex);
        row.getCell("sum7").setValue(100L, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов не задано правило назначения категории в данном Налоговом периоде!", entries.get(i++).getMessage());
        // лишняя проверка 4
        Assert.assertEquals("Строка 1: Для ожидаемого объема доходов и расходов указана неверная категория!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 4. Проверка соответствия категории пороговым значениям
        for (String column : nonEmptyColumns) {
            row.getCell(column).setValue(1L, rowIndex);
        }
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

        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        int expected = 1; // 1 стока из источников
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
        Long testRefbookId = 1L;
        DataRow<Cell> row;
        switch (sourceTemplateId) {
            case 807 : // Журнал взаиморасчетов
                row = sourceFormData.createDataRow();
                row.getCell("statReportId1").setValue(testRefbookId, null);
                row.getCell("statReportId2").setValue(testRefbookId, null);
                row.getCell("sbrfCode1").setValue(testLong, null);
                row.getCell("sum").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 854 : // Приложение 9
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId, null);
                row.getCell("sum44").setValue(testLong, null);
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
            case 816 : // 6.1
                row = sourceFormData.createDataRow();
                row.getCell("name").setValue(testRefbookId, null);
                row.getCell("sum").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 804 : // 6.2
            case 812 : // 6.3
            case 813 : // 6.4
            case 814 : // 6.5
            case 806 : // 6.6
            case 805 : // 6.7
            case 815 : // 6.8
            case 817 : // 6.9
            case 823 : // 6.10-1
            case 825 : // 6.10-2
            case 827 : // 6.11
            case 819 : // 6.12
            case 826 : // 6.13
            case 835 : // 6.14
            case 837 : // 6.15
            case 839 : // 6.16
            case 811 : // 6.17
            case 838 : // 6.18
            case 828 : // 6.19
            case 831 : // 6.20
            case 830 : // 6.21
            case 834 : // 6.22
            case 832 : // 6.23
            case 833 : // 6.24
            case 836 : // 6.25
                // TODO (Ramil Timerbaev) дополнить
                break;
        }
        return dataRows;
    }

    /** Проверить данные. */
    void checkData(List<DataRow<Cell>> dataRows) {
        DataRow<Cell> row = dataRows.get(0);
        // графа 1  (1) - нумерация
        // графа 2  (2)
        Assert.assertEquals(1L, row.getCell("name").getNumericValue().longValue());
        // графа 3  (3) - зависимая
        // графа 4  (4)
        Assert.assertEquals(1L, row.getCell("group").getNumericValue().longValue());

        // графа 5  (5.1)
        Assert.assertEquals(0L, row.getCell("sum51").getNumericValue().longValue());
        // графа 6  (5.2)
        Assert.assertEquals(0L, row.getCell("sum52").getNumericValue().longValue());
        // графа 7  (5.3)
        Assert.assertEquals(0L, row.getCell("sum53").getNumericValue().longValue());
        // графа 8  (5.4)
        Assert.assertEquals(100100L, row.getCell("sum54").getNumericValue().longValue());
        // графа 9  (5.5)
        Assert.assertEquals(100L, row.getCell("sum55").getNumericValue().longValue());
        // графа 10 (5.6)
        Assert.assertEquals(100L, row.getCell("sum56").getNumericValue().longValue());
        // графа 11 (6.1)
        Assert.assertEquals(0L, row.getCell("sum61").getNumericValue().longValue());
        // графа 12 (6.2)
        Assert.assertEquals(0L, row.getCell("sum62").getNumericValue().longValue());
        // графа 13 (6.3)
        Assert.assertEquals(0L, row.getCell("sum63").getNumericValue().longValue());
        // графа 14 (6.4)
        Assert.assertEquals(0L, row.getCell("sum64").getNumericValue().longValue());
        // графа 15 (6.5)
        Assert.assertEquals(0L, row.getCell("sum65").getNumericValue().longValue());
        // графа 16 (6.6)
        Assert.assertEquals(0L, row.getCell("sum66").getNumericValue().longValue());

        // графа 17 (7)
        String [] calc17Aliases = { "sum51", "sum52", "sum53", "sum54", "sum55", "sum56", "sum61", "sum62", "sum63", "sum64", "sum65", "sum66" };
        long expected17 = 0L;
        for (String alias : calc17Aliases) {
            expected17 += row.getCell(alias).getNumericValue().longValue();
        }
        Assert.assertEquals(expected17, row.getCell("sum7").getNumericValue().longValue());

        // графа 18 (8)
        Assert.assertEquals(1L, row.getCell("thresholdValue").getNumericValue().longValue());
        // графа 19 (9)
        Assert.assertEquals(1L, row.getCell("sign").getNumericValue().longValue());
        // графа 20 (10)
        Assert.assertEquals(3L, row.getCell("categoryRevised").getNumericValue().longValue());
    }

    private void mockProviders() {
        long [] refBookIds = { 505L, 511L, 513L, 514L, 515L, 520L, 525L };
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