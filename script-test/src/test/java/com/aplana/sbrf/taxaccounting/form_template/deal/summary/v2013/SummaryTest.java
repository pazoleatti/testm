package com.aplana.sbrf.taxaccounting.form_template.deal.summary.v2013;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Сводный отчет.
 */
public class SummaryTest extends ScriptTestBase {
    private static final int TYPE_ID = 2409;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();
    static {
        formTypeIdByTemplateIdMap.put(2375, 375);
        formTypeIdByTemplateIdMap.put(2376, 376);
        formTypeIdByTemplateIdMap.put(2377, 377);
        formTypeIdByTemplateIdMap.put(2379, 379);
        formTypeIdByTemplateIdMap.put(2380, 380);
        formTypeIdByTemplateIdMap.put(2381, 381);
        formTypeIdByTemplateIdMap.put(2382, 382);
        formTypeIdByTemplateIdMap.put(2383, 383);
        formTypeIdByTemplateIdMap.put(2384, 384);
        formTypeIdByTemplateIdMap.put(2385, 385);
        formTypeIdByTemplateIdMap.put(2386, 386);
        formTypeIdByTemplateIdMap.put(2387, 387);
        formTypeIdByTemplateIdMap.put(3388, 388);
        formTypeIdByTemplateIdMap.put(2389, 389);
        formTypeIdByTemplateIdMap.put(2390, 390);
        formTypeIdByTemplateIdMap.put(2391, 391);
        formTypeIdByTemplateIdMap.put(3392, 392);
        formTypeIdByTemplateIdMap.put(2393, 393);
        formTypeIdByTemplateIdMap.put(2394, 394);
        formTypeIdByTemplateIdMap.put(2397, 397);
        formTypeIdByTemplateIdMap.put(2398, 398);
        formTypeIdByTemplateIdMap.put(2399, 399);
        formTypeIdByTemplateIdMap.put(2401, 401);
        formTypeIdByTemplateIdMap.put(2402, 402);
        formTypeIdByTemplateIdMap.put(2403, 403);
        formTypeIdByTemplateIdMap.put(2404, 404);
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();
    static {
        templatesPathMap.put(2375, "..//src/main//resources//form_template//deal//software_development//v2015//");
        templatesPathMap.put(2376, "..//src/main//resources//form_template//deal//rent_provision//v2015//");
        templatesPathMap.put(2377, "..//src/main//resources//form_template//deal//tech_service//v2015//");
        templatesPathMap.put(2379, "..//src/main//resources//form_template//deal//trademark//v2015//");
        templatesPathMap.put(2380, "..//src/main//resources//form_template//deal//auctions_property//v2015//");
        templatesPathMap.put(2381, "..//src/main//resources//form_template//deal//securities//v2015//");
        templatesPathMap.put(2382, "..//src/main//resources//form_template//deal//bank_service//v2015//");
        templatesPathMap.put(2383, "..//src/main//resources//form_template//deal//repo//v2015//");
        templatesPathMap.put(2384, "..//src/main//resources//form_template//deal//bonds_trade//v2015//");
        templatesPathMap.put(2385, "..//src/main//resources//form_template//deal//credit_contract//v2015//");
        templatesPathMap.put(2386, "..//src/main//resources//form_template//deal//letter_of_credit//v2015//");
        templatesPathMap.put(2387, "..//src/main//resources//form_template//deal//corporate_credit//v2015//");
        templatesPathMap.put(3388, "..//src/main//resources//form_template//deal//guarantees//v2015//");
        templatesPathMap.put(2389, "..//src/main//resources//form_template//deal//interbank_credits//v2015//");
        templatesPathMap.put(2390, "..//src/main//resources//form_template//deal//foreign_currency//v2015//");
        templatesPathMap.put(2391, "..//src/main//resources//form_template//deal//forward_contracts//v2015//");
        templatesPathMap.put(3392, "..//src/main//resources//form_template//deal//nondeliverable//v2015//");
        templatesPathMap.put(2393, "..//src/main//resources//form_template//deal//precious_metals_deliver//v2015//");
        templatesPathMap.put(2394, "..//src/main//resources//form_template//deal//precious_metals_trade//v2015//");
        templatesPathMap.put(2397, "..//src/main//resources//form_template//deal//take_corporate_credit//v2013//");
        templatesPathMap.put(2398, "..//src/main//resources//form_template//deal//bank_service_income//v2013//");
        templatesPathMap.put(2399, "..//src/main//resources//form_template//deal//bank_service_outcome//v2013//");
        templatesPathMap.put(2401, "..//src/main//resources//form_template//deal//guarantees_involvement//v2013//");
        templatesPathMap.put(2402, "..//src/main//resources//form_template//deal//take_interbank_credit//v2013//");
        templatesPathMap.put(2403, "..//src/main//resources//form_template//deal//take_itf//v2013//");
        templatesPathMap.put(2404, "..//src/main//resources//form_template//deal//rights_acquisition//v2013//");
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
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(SummaryTest.class);
    }

    @Test
    public void create() {
        when(testHelper.getFormDataService().find(anyInt(), any(FormDataKind.class), anyInt(), (Integer) anyInt(), any(Integer.class), any(Boolean.class))).thenReturn(null);

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

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//deal//summary//v2013//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // Проверка пустой строки
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 2. Проверка неотрицательности доходов и расходов
        // 4. Проверка одновременного заполнения полей «Код предмета сделки»
        // 5. Проверка корректности даты совершения сделки
        row.getCell("income").setValue(-1, null);
        row.getCell("outcome").setValue(-1, null);
        row.getCell("dealSubjectCode1").setValue(1L, null);
        row.getCell("dealSubjectCode2").setValue(1L, null);
        row.getCell("contractDate").setValue(sdf.parse("03.01.2050"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("02.01.2050"), null);
        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение атрибута «п. 300 \"Сумма доходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях\"» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение атрибута «п. 310 \"Сумма расходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях\"» должно быть больше или равно «0»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значения граф «п. 040 \"Код предмета сделки (код по ТН ВЭД)\"» и «п. 043 \"Код предмета сделки (код по ОКП)\"» не должны быть одновременно заполнены!", entries.get(i++).getMessage());
        String CHECK_DATE_PERIOD_EXT = "Строка %d: Дата по графе «%s» должна принимать значение из диапазона %s - %s и быть больше либо равна дате по графе «%s»!";
        Assert.assertEquals(String.format(CHECK_DATE_PERIOD_EXT, 1, "п. 150 \"Дата совершения сделки (цифрами день, месяц, год)\"", "01.01.2014", "31.12.2014", "п. 065 \"Дата договора\""), entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 3. Проверка заполнения доходов и расходов
        row.getCell("income").setValue(0, null);
        row.getCell("outcome").setValue(0, null);
        row.getCell("dealSubjectCode2").setValue(null, null);
        row.getCell("contractDate").setValue(sdf.parse("01.01.2050"), null);
        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значения атрибутов «п. 300 \"Сумма доходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях\"» и «п. 310 \"Сумма расходов налогоплательщика по контролируемой сделке (группе однородных сделок) в рублях\"» не должны быть одновременно равны «0»!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(CHECK_DATE_PERIOD_EXT, 1, "п. 150 \"Дата совершения сделки (цифрами день, месяц, год)\"", "01.01.2014", "31.12.2014", "п. 065 \"Дата договора\""), entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        row.getCell("income").setValue(1, null);
        row.getCell("contractDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("dealDoneDate").setValue(sdf.parse("02.01.2014"), null);

        i = 0;
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
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

    // Консолидация - нет источников
    @Test
    public void composeNotSourcesTest() {
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(null);

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        // должны получить 2 строки в приемнике: 1 строка из источника и 1 итоговая
        Assert.assertEquals(0, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    // Консолидация - все источники по отдельности
    @Test
    public void composeAllSourceTest() {
        HashMap<Integer, Integer> sourceExpectedMap = new LinkedHashMap<Integer, Integer>();
        sourceExpectedMap.put(2375, 2);
        sourceExpectedMap.put(2376, 2);
        sourceExpectedMap.put(2377, 2);
        sourceExpectedMap.put(2379, 2);
        sourceExpectedMap.put(2380, 2);
        sourceExpectedMap.put(2381, 2);
        sourceExpectedMap.put(2382, 2);
        sourceExpectedMap.put(2383, 2);
        sourceExpectedMap.put(2384, 2);
        sourceExpectedMap.put(2385, 2);
        sourceExpectedMap.put(2386, 2);
        sourceExpectedMap.put(2387, 2);
        sourceExpectedMap.put(3388, 2);
        sourceExpectedMap.put(2389, 2);
        sourceExpectedMap.put(2390, 2);
        sourceExpectedMap.put(2391, 2);
        sourceExpectedMap.put(3392, 2);
        sourceExpectedMap.put(2393, 2);
        sourceExpectedMap.put(2394, 2);
        sourceExpectedMap.put(2397, 2);
        sourceExpectedMap.put(2398, 2);
        sourceExpectedMap.put(2399, 2);
        sourceExpectedMap.put(2401, 2);
        sourceExpectedMap.put(2402, 2);
        sourceExpectedMap.put(2403, 2);
        sourceExpectedMap.put(2404, 2);

        for (Integer key : sourceExpectedMap.keySet()) {
            // идентификатор шаблона источников
            int sourceTemplateId = key;
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);
            FormDataKind kind = FormDataKind.CONSOLIDATED;

            // источник
            DepartmentFormType departmentFormType = getSource(sourceTypeId, sourceTypeId, kind);
            when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                    any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData, 1L);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

            testHelper.initRowData();

            // Консолидация
            testHelper.execute(FormDataEvent.COMPOSE);

            int expected = sourceExpectedMap.get(key);
            Assert.assertEquals(getMsg(sourceFormData), expected, testHelper.getDataRowHelper().getAll().size());

            checkLogger();
        }
    }

    // Консолидация - все источники сразу
    @Test
    public void composeTest() {
        int[] sourceTemplateIds = new int [] { 2375, 2376, 2377, 2379, 2380, 2381, 2382, 2383, 2384, 2385, 2386, 2387, 3388, 2389, 2390, 2391, 3392, 2393, 2394, 2397, 2398, 2399, 2401, 2402, 2403, 2404 };
        List<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>(sourceTemplateIds.length);
        for (int sourceTemplateId : sourceTemplateIds) {
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);
            FormDataKind kind = FormDataKind.CONSOLIDATED;

            // источник
            DepartmentFormType departmentFormType = getSource(sourceTypeId, sourceTypeId, kind);
            departmentFormTypes.add(departmentFormType);

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                    anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData, 1L);
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

        int expected = sourceTemplateIds.length + 1; // 26 сток из 26 источников + 1 строка для граппировки по организации
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 9; // 9 = 6 (строк из файла) + 3 (заголовки групп - названия организации)
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        //checkLogger();
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
     * Получить сообещния для проверки количества строк после консолидации.
     *
     * @param sourceFormData форма источника
     */
    private String getMsg(FormData sourceFormData) {
        return String.format("Неверное количество строк при консолидации в сводный отчет из иcточника %s - \"%s\" (type_id = %d, template_id = %d)",
                templatesPathMap.get(sourceFormData.getFormTemplateId()),
                sourceFormData.getFormType().getName(),
                sourceFormData.getFormType().getId(),
                sourceFormData.getFormTemplateId());
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData форма источника
     * @param refbook9Id идентификатор для графы организации (юр лица), влияющая на группировку
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData, long refbook9Id) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "test_" + sourceTemplateId;
        Date testDate = new Date();
        Long testLong = 100L;
        Long testRefbookId = 1L;
        DataRow<Cell> row;
        switch (sourceTemplateId) {
            case 2375 :
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullNamePerson").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("expensesSum").setValue(testLong, null);
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8 - справочник 11
                row.getCell("serviceType").setValue(testRefbookId, null);
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("cost").setValue(testLong, null);
                // графа 11
                row.getCell("dealDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2376:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("jurName").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("incomeBankSum").setValue(testLong, null);
                // графа 6
                row.getCell("outcomeBankSum").setValue(testLong, null);
                // графа 7
                row.getCell("contractNum").setValue(testString, null);
                // графа 8
                row.getCell("contractDate").setValue(testDate, null);
                // графа 9 - справочник 10
                row.getCell("country").setValue(testRefbookId, null);
                // графа 10 - справочник 4
                row.getCell("region").setValue(testRefbookId, null);
                // графа 11
                row.getCell("city").setValue(testString, null);
                // графа 12
                row.getCell("settlement").setValue(testString, null);
                // графа 13
                row.getCell("count").setValue(testLong, null);
                // графа 14
                row.getCell("price").setValue(testLong, null);
                // графа 15
                row.getCell("cost").setValue(testLong, null);
                // графа 16
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2377:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("jurName").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("bankSum").setValue(testLong, null);
                // графа 6
                row.getCell("contractNum").setValue(testString, null);
                // графа 7
                row.getCell("contractDate").setValue(testDate, null);
                // графа 8 - справочник 10
                row.getCell("country").setValue(testRefbookId, null);
                // графа 9 - справочник 4
                row.getCell("region").setValue(testRefbookId, null);
                // графа 10
                row.getCell("city").setValue(testString, null);
                // графа 11
                row.getCell("settlement").setValue(testString, null);
                // графа 12
                row.getCell("count").setValue(testLong, null);
                // графа 13
                row.getCell("price").setValue(testLong, null);
                // графа 14
                row.getCell("cost").setValue(testLong, null);
                // графа 15
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2379:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullNamePerson").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("sum").setValue(testLong, null);
                // графа 6
                  row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("price").setValue(testLong, null);
                // графа 9
                row.getCell("cost").setValue(testLong, null);
                // графа 10
                row.getCell("dealDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2380:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullNamePerson").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("sum").setValue(testLong, null);
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("count").setValue(testLong, null);
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("cost").setValue(testLong, null);
                // графа 11
                row.getCell("date").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2381:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullNamePerson").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5 - справочник 36
                row.getCell("dealSign").setValue(testRefbookId, null);
                // графа 6
                row.getCell("incomeSum").setValue(testLong, null);
                // графа 7
                row.getCell("outcomeSum").setValue(testLong, null);
                // графа 8
                row.getCell("docNumber").setValue(testString, null);
                // графа 9
                row.getCell("docDate").setValue(testDate, null);
                // графа 10 - справочник 9
                row.getCell("okeiCode").setValue(testRefbookId, null);
                // графа 11
                row.getCell("count").setValue(testLong, null);
                // графа 12
                row.getCell("price").setValue(testLong, null);
                // графа 13
                row.getCell("cost").setValue(testLong, null);
                // графа 14
                row.getCell("dealDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2382:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("jurName").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6 - справочник 13
                row.getCell("serviceName").setValue(testRefbookId, null);
                // графа 7
                row.getCell("bankIncomeSum").setValue(testLong, null);
                // графа 8
                row.getCell("contractNum").setValue(testString, null);
                // графа 9
                row.getCell("contractDate").setValue(testDate, null);
                // графа 10
                row.getCell("price").setValue(testLong, null);
                // графа 11
                row.getCell("cost").setValue(testLong, null);
                // графа 12
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2383:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("jurName").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6
                row.getCell("contractNum").setValue(testString, null);
                // графа 7
                row.getCell("contractDate").setValue(testDate, null);
                // графа 8
                row.getCell("transactionNum").setValue(testString, null);
                // графа 9
                row.getCell("transactionDeliveryDate").setValue(testDate, null);
                // графа 10 - справочник 14
                row.getCell("dealsMode").setValue(testRefbookId, null);
                // графа 11
                row.getCell("date1").setValue(testDate, null);
                // графа 12
                row.getCell("date2").setValue(testDate, null);
                // графа 13
                row.getCell("percentIncomeSum").setValue(testLong, null);
                // графа 14
                row.getCell("percentConsumptionSum").setValue(testLong, null);
                // графа 15
                row.getCell("priceFirstCurrency").setValue(testLong, null);
                // графа 16 - справочник 15
                row.getCell("currencyCode").setValue(testRefbookId, null);
                // графа 17
                row.getCell("courseCB").setValue(testLong, null);
                // графа 18
                row.getCell("priceFirstRub").setValue(testLong, null);
                // графа 19
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2384:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2
                row.getCell("transactionDeliveryDate").setValue(testDate, null);
                // графа 3 - справочник 9
                row.getCell("contraName").setValue(refbook9Id, null);
                // графа 4 - справочник 14
                row.getCell("transactionMode").setValue(testRefbookId, null);
                // графа 5 - зависимая графа
                // графа 6 - зависимая графа
                // графа 7 - зависимая графа
                // графа 8
                row.getCell("transactionSumCurrency").setValue(testLong, null);
                // графа 9 - справочник 15
                row.getCell("currency").setValue(testRefbookId, null);
                // графа 10
                row.getCell("courseCB").setValue(testLong, null);
                // графа 11
                row.getCell("transactionSumRub").setValue(testLong, null);
                // графа 12
                row.getCell("contractNum").setValue(testString, null);
                // графа 13
                row.getCell("contractDate").setValue(testDate, null);
                // графа 14
                row.getCell("transactionDate").setValue(testDate, null);
                // графа 15
                row.getCell("bondRegCode").setValue(testString, null);
                // графа 16
                row.getCell("bondCount").setValue(testLong, null);
                // графа 17
                row.getCell("priceOne").setValue(testLong, null);
                // графа 18 - справочник 16
                row.getCell("transactionType").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 2385:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("name").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("contractNum").setValue(testString, null);
                // графа 6
                row.getCell("contractDate").setValue(testDate, null);
                // графа 7 - справочник 12
                row.getCell("okeiCode").setValue(testRefbookId, null);
                // графа 8
                row.getCell("count").setValue(1L, null); // ограничение 1 символ
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("totalCost").setValue(testLong, null);
                // графа 11
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2386:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("dealNumber").setValue(testString, null);
                // графа 9
                row.getCell("dealDate").setValue(testDate, null);
                // графа 10
                row.getCell("sum").setValue(testLong, null);
                // графа 11
                row.getCell("price").setValue(testLong, null);
                // графа 12
                row.getCell("total").setValue(testLong, null);
                // графа 13 - скрытый столбец
                // графа 14
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2387:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullNamePerson").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("sum").setValue(testLong, null);
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("count").setValue(1L, null);
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("cost").setValue(testLong, null);
                // графа 11
                row.getCell("dealDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 3388:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("dealNumber").setValue(testString, null);
                // графа 9
                row.getCell("dealDate").setValue(testDate, null);
                // графа 10
                row.getCell("sum").setValue(testLong, null);
                // графа 11
                row.getCell("price").setValue(testLong, null);
                // графа 12
                row.getCell("total").setValue(testLong, null);
                // графа 13 - скрытый столбец
                // графа 14
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2389:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("dealNumber").setValue(testString, null);
                // графа 9
                row.getCell("dealDate").setValue(testDate, null);
                // графа 10
                row.getCell("count").setValue(1L, null); // ограничение 1 символ
                // графа 11
                row.getCell("sum").setValue(testLong, null);
                // графа 12
                row.getCell("price").setValue(testLong, null);
                // графа 13
                row.getCell("total").setValue(testLong, null);
                // графа 14
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2390:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6 - зависимая графа
                // графа 7
                row.getCell("docNum").setValue(testString, null);
                // графа 8
                row.getCell("docDate").setValue(testDate, null);
                // графа 9
                row.getCell("dealNumber").setValue(testString, null);
                // графа 10
                row.getCell("dealDate").setValue(testDate, null);
                // графа 11 - справочник 15
                row.getCell("currencyCode").setValue(testRefbookId, null);
                // графа 12 - справочник 10
                row.getCell("countryDealCode").setValue(testRefbookId, null);
                // графа 13
                row.getCell("incomeSum").setValue(testLong, null);
                // графа 14
                row.getCell("outcomeSum").setValue(testLong, null);
                // графа 15
                row.getCell("price").setValue(testLong, null);
                // графа 16
                row.getCell("total").setValue(testLong, null);
                // графа 17 - скрытый столбец
                // графа 18
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2391:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6 - зависимая графа
                // графа 7
                row.getCell("docNumber").setValue(testString, null);
                // графа 8
                row.getCell("docDate").setValue(testDate, null);
                // графа 9
                row.getCell("dealNumber").setValue(testString, null);
                // графа 10
                row.getCell("dealDate").setValue(testDate, null);
                // графа 11
                row.getCell("dealType").setValue(testString, null);
                // графа 12 - справочник 15
                row.getCell("currencyCode").setValue(testRefbookId, null);
                // графа 13 - справочник 10
                row.getCell("countryDealCode").setValue(testRefbookId, null);
                // графа 14
                row.getCell("incomeSum").setValue(testLong, null);
                // графа 15
                row.getCell("outcomeSum").setValue(testLong, null);
                // графа 16
                row.getCell("price").setValue(testLong, null);
                // графа 17
                row.getCell("total").setValue(testLong, null);
                // графа 18 - скрытый столбец
                // графа 19
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 3392:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("name").setValue(refbook9Id, null);
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6 - зависимая графа
                // графа 7
                row.getCell("contractNum").setValue(testString, null);
                // графа 8
                row.getCell("contractDate").setValue(testDate, null);
                // графа 9
                row.getCell("transactionNum").setValue(testString, null);
                // графа 10
                row.getCell("transactionDeliveryDate").setValue(testDate, null);
                // графа 11
                row.getCell("transactionType").setValue(testString, null);
                // графа 12
                row.getCell("incomeSum").setValue(testLong, null);
                // графа 13
                row.getCell("consumptionSum").setValue(testLong, null);
                // графа 14
                row.getCell("price").setValue(testLong, null);
                // графа 15
                row.getCell("cost").setValue(testLong, null);
                // графа 16 - скрытый столбец
                // графа 17
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2393:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("name").setValue(refbook9Id, null);
                // графа 4 - справочник 38
                row.getCell("dependence").setValue(testRefbookId, null);
                // графа 5 - зависимая графа
                // графа 6 - зависимая графа
                // графа 7 - зависимая графа
                // графа 8
                row.getCell("contractNum").setValue(testString, null);
                // графа 9
                row.getCell("contractDate").setValue(testDate, null);
                // графа 10
                row.getCell("transactionNum").setValue(testString, null);
                // графа 11 - справочник 85 (этот справочник не используется в сводном отчете)
                row.getCell("dealType").setValue(testRefbookId, null);
                // графа 12
                row.getCell("transactionDeliveryDate").setValue(testDate, null);
                // графа 13 - справочник 17
                row.getCell("innerCode").setValue(testRefbookId, null);
                // графа 14 - справочник 10
                row.getCell("unitCountryCode").setValue(testRefbookId, null);
                // графа 15 - справочник 18
                row.getCell("signPhis").setValue(testRefbookId, null);
                // графа 16 - справочник 38
                row.getCell("signTransaction").setValue(testRefbookId, null);
                // графа 17 - справочник 10
                row.getCell("countryCode2").setValue(testRefbookId, null);
                // графа 18 - справочник 4
                row.getCell("region1").setValue(testRefbookId, null);
                // графа 19
                row.getCell("city1").setValue(testString, null);
                // графа 20
                row.getCell("settlement1").setValue(testString, null);
                // графа 21 - справочник 10
                row.getCell("countryCode3").setValue(testRefbookId, null);
                // графа 22 - справочник 4
                row.getCell("region2").setValue(testRefbookId, null);
                // графа 23
                row.getCell("city2").setValue(testString, null);
                // графа 24
                row.getCell("settlement2").setValue(testString, null);
                // графа 25 - справочник 63
                row.getCell("conditionCode").setValue(testRefbookId, null);
                // графа 26
                row.getCell("count").setValue(1L, null); // ограничение 1 символ
                // графа 27
                row.getCell("incomeSum").setValue(testLong, null);
                // графа 28
                row.getCell("consumptionSum").setValue(testLong, null);
                // графа 29
                row.getCell("priceOne").setValue(testLong, null);
                // графа 30
                row.getCell("totalNds").setValue(testLong, null);
                // графа 31 - скрытый столбец
                // графа 32
                row.getCell("transactionDate").setValue(testDate, null);
                // графа 33
                row.getCell("okpCode").setValue(1L, null); // ограничение 1 символ
                dataRows.add(row);
                break;
            case 2394:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 4 - справочник 38
                row.getCell("interdependence").setValue(testRefbookId, null);
                // графа 5 - зависимая графа
                // графа 6 - зависимая графа
                // графа 7 - зависимая графа
                // графа 8
                row.getCell("docNumber").setValue(testString, null);
                // графа 9
                row.getCell("docDate").setValue(testDate, null);
                // графа 10
                row.getCell("dealNumber").setValue(testString, null);
                // графа 11
                row.getCell("dealDate").setValue(testDate, null);
                // графа 12 - справочник 20
                row.getCell("dealFocus").setValue(testRefbookId, null);
                // графа 13 - справочник 18
                row.getCell("deliverySign").setValue(testRefbookId, null);
                // графа 14 - справочник 17
                row.getCell("metalName").setValue(testRefbookId, null);
                // графа 15 - справочник 38
                row.getCell("foreignDeal").setValue(testRefbookId, null);
                // графа 16 - справочник 10
                row.getCell("countryCodeNumeric").setValue(testRefbookId, null);
                // графа 17 - справочник 4
                row.getCell("regionCode").setValue(testRefbookId, null);
                // графа 18
                row.getCell("city").setValue(testString, null);
                // графа 19
                row.getCell("locality").setValue(testString, null);
                // графа 20 - справочник 10
                row.getCell("countryCodeNumeric2").setValue(testRefbookId, null);
                // графа 21 - справочник 4
                row.getCell("region2").setValue(testRefbookId, null);
                // графа 22
                row.getCell("city2").setValue(testString, null);
                // графа 23
                row.getCell("locality2").setValue(testString, null);
                // графа 24 - справочник 63
                row.getCell("deliveryCode").setValue(testRefbookId, null);
                // графа 25
                row.getCell("count").setValue(1L, null); // ограничение 1 символ
                // графа 26
                row.getCell("incomeSum").setValue(testLong, null);
                // графа 27
                row.getCell("outcomeSum").setValue(testLong, null);
                // графа 28
                row.getCell("price").setValue(testLong, null);
                // графа 29
                row.getCell("total").setValue(testLong, null);
                // графа 30 - скрытый столбец
                // графа 31
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2397:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullNamePerson").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("sum").setValue(testLong, null);
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("count").setValue(1L, null);
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("cost").setValue(testLong, null);
                // графа 11
                row.getCell("dealDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2398:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("jurName").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("bankIncomeSum").setValue(testLong, null);
                // графа 6
                row.getCell("contractNum").setValue(testString, null);
                // графа 7
                row.getCell("contractDate").setValue(testDate, null);
                // графа 8
                row.getCell("serviceName").setValue(testString, null);
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("cost").setValue(testLong, null);
                // графа 11
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2399:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("jurName").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("bankIncomeSum").setValue(testLong, null);
                // графа 6
                row.getCell("contractNum").setValue(testString, null);
                // графа 7
                row.getCell("contractDate").setValue(testDate, null);
                // графа 8
                row.getCell("serviceName").setValue(testString, null);
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("cost").setValue(testLong, null);
                // графа 11
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2401:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("dealNumber").setValue(testString, null);
                // графа 9
                row.getCell("dealDate").setValue(testDate, null);
                // графа 10
                row.getCell("sum").setValue(testLong, null);
                // графа 11
                row.getCell("price").setValue(testLong, null);
                // графа 12
                row.getCell("total").setValue(testLong, null);
                // графа 13 - скрытый столбец
                // графа 14
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2402:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("fullNamePerson").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6
                row.getCell("docNum").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("dealNumber").setValue(testString, null);
                // графа 9
                row.getCell("dealDate").setValue(testDate, null);
                // графа 10
                row.getCell("count").setValue(testLong, null);
                // графа 11
                row.getCell("sum").setValue(testLong, null);
                // графа 12
                row.getCell("price").setValue(testLong, null);
                // графа 13
                row.getCell("total").setValue(testLong, null);
                // графа 14
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2403:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNumber").setValue(1L, null);
                // графа 2 - скрытый столбец
                // графа 3 - справочник 9
                row.getCell("fullName").setValue(refbook9Id, null);
                // графа 4 - зависимая графа
                // графа 5 - зависимая графа
                // графа 6
                row.getCell("docNumber").setValue(testString, null);
                // графа 7
                row.getCell("docDate").setValue(testDate, null);
                // графа 8
                row.getCell("dealNumber").setValue(testString, null);
                // графа 9
                row.getCell("dealDate").setValue(testDate, null);
                // графа 10
                row.getCell("outcomeSum").setValue(testLong, null);
                // графа 11
                row.getCell("price").setValue(testLong, null);
                // графа 12
                row.getCell("total").setValue(testLong, null);
                // графа 13
                row.getCell("dealDoneDate").setValue(testDate, null);
                dataRows.add(row);
                break;
            case 2404:
                row = sourceFormData.createDataRow();
                // графа 1
                row.getCell("rowNum").setValue(1L, null);
                // графа 2 - справочник 9
                row.getCell("name").setValue(refbook9Id, null);
                // графа 3 - зависимая графа
                // графа 4 - зависимая графа
                // графа 5
                row.getCell("contractNum").setValue(testString, null);
                // графа 6
                row.getCell("contractDate").setValue(testDate, null);
                // графа 7 - справочник 57
                row.getCell("okeiCode").setValue(testRefbookId, null);
                // графа 8
                row.getCell("count").setValue(1L, null); // ограничение 1 символ
                // графа 9
                row.getCell("price").setValue(testLong, null);
                // графа 10
                row.getCell("totalCost").setValue(testLong, null);
                // графа 11
                row.getCell("transactionDate").setValue(testDate, null);
                dataRows.add(row);
                break;
        }
        return dataRows;
    }
}