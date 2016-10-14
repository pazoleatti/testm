package com.aplana.sbrf.taxaccounting.form_template.land.calc_for_declaration.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Расчет земельного налога по земельным участкам, подлежащим включению в декларацию
 */
public class CalcForDeclarationTest extends ScriptTestBase {
    private static final int TYPE_ID = 918;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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
        return getDefaultScriptTestMockHelper(CalcForDeclarationTest.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
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
    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        // должна быть ошибка что нет итого
        int i = 0;
        Assert.assertEquals("Итоговые значения рассчитаны неверно!", testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        mockProvider(96L);

        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // простая строка
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        setDefaultValues(row);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total2Row = formData.createDataRow();
        total2Row.setIndex(2);
        total2Row.setAlias("total2#1");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total2Row.getCell("oktmo").setValue(row.getCell("oktmo").getValue(), null);
        total2Row.getCell("q1").setValue(1, null);
        total2Row.getCell("q2").setValue(1, null);
        total2Row.getCell("q3").setValue(1, null);
        total2Row.getCell("year").setValue(1, null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setIndex(3);
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total1Row.getCell("q1").setValue(1, null);
        total1Row.getCell("q2").setValue(1, null);
        total1Row.getCell("q3").setValue(1, null);
        total1Row.getCell("year").setValue(1, null);
        dataRows.add(total1Row);

        // строка всего
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setIndex(4);
        totalRow.setAlias("total");
        totalRow.getCell("q1").setValue(1, null);
        totalRow.getCell("q2").setValue(1, null);
        totalRow.getCell("q3").setValue(1, null);
        totalRow.getCell("year").setValue(1, null);
        dataRows.add(totalRow);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i;
        String msg;

        // 1 Проверка обязательности заполнения граф
        for (Column column : formData.getFormColumns()) {
            row.getCell(column.getAlias()).setValue(null, row.getIndex());
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        // графа 2..8, 10, 12, 14, 21, 22, 25..28
        String [] nonEmptyColumns = {"department", "kno", "kpp", "kbk", "oktmo", "cadastralNumber","landCategory",
                "cadastralCost", "ownershipDate", "period", "taxRate", "kv", "q1", "q2", "q3", "year"};
        for (String alias : nonEmptyColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        testHelper.getLogger().clear();
        setDefaultValues(row);

        // 2.1 Проверка корректности значений итоговых строк (нет строки ВСЕГО и подитговых строк)
        dataRows.remove(total2Row);
        dataRows.remove(total1Row);
        dataRows.remove(totalRow);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        Assert.assertEquals("Группа «КНО=kno, КПП=kpp, Код ОКТМО=codeA96» не имеет строки итога!", entries.get(i++).getMessage());
        Assert.assertEquals("Группа «КНО=kno, КПП=kpp» не имеет строки итога!", entries.get(i++).getMessage());
        Assert.assertEquals("Итоговые значения рассчитаны неверно!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        dataRows.add(total2Row);
        dataRows.add(total1Row);
        dataRows.add(totalRow);

        // 2.2 Проверка корректности значений итоговых строк (ошибка в суммах ВСЕГО и в подитогах)
        // графа 25..28
        String [] totalColumns = { "q1", "q2", "q3", "year" };
        for (String alias : totalColumns) {
            row.getCell(alias).setValue(0, null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        String subMsg = String.format("%s», «%s», «%s», «%s", row.getCell("q1").getColumn().getName(),
                row.getCell("q2").getColumn().getName(), row.getCell("q3").getColumn().getName(), row.getCell("year").getColumn().getName());
        int [] rowIndexes = { total2Row.getIndex(), total1Row.getIndex(), totalRow.getIndex() };
        for (int index : rowIndexes) {
            msg = String.format("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", index, subMsg);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        for (String alias : totalColumns) {
            row.getCell(alias).setValue(1, null);
        }

        // 2.3 Проверка корректности значений итоговых строк (лишний подитог)
        DataRow<Cell> tmpTotal2Row = formData.createDataRow();
        tmpTotal2Row.setIndex(6);
        tmpTotal2Row.setAlias("total2#tmp");
        dataRows.add(tmpTotal2Row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format(ScriptUtils.GROUP_WRONG_ITOG_ROW, tmpTotal2Row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        dataRows.remove(tmpTotal2Row);

        // успешное выполнение всех логических проверок
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
        testHelper.getLogger().clear();
    }

    private void setDefaultValues(DataRow<Cell> row) throws ParseException {
        row.getCell("department").setValue(1L, null);
        row.getCell("kno").setValue("kno", null);
        row.getCell("kpp").setValue("kpp", null);
        row.getCell("kbk").setValue(1L, null);
        row.getCell("oktmo").setValue(1L, null);
        row.getCell("cadastralNumber").setValue("cadastralNumber", null);
        row.getCell("landCategory").setValue(1L, null);
        row.getCell("cadastralCost").setValue(0, null);
        row.getCell("ownershipDate").setValue(sdf.parse("01.01.2014"), null);
        row.getCell("period").setValue(0, null);
        row.getCell("taxRate").setValue(0, null);
        row.getCell("kv").setValue(0, null);
        row.getCell("q1").setValue(1, null);
        row.getCell("q2").setValue(1, null);
        row.getCell("q3").setValue(1, null);
        row.getCell("year").setValue(1, null);
    }

    // консолидация без источников
    @Test
    public void composeNotSourcesTest() {
        int expected = 0;
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(1, testHelper.getLogger().getEntries().size());
        Assert.assertEquals("Не удалось консолидировать данные в форму. В Системе отсутствует форма вида «Земельные участки, подлежащие включению в декларацию» " +
                "в состоянии «Принята» за период: «test period name 2014» для подразделения «test department name»", testHelper.getLogger().getEntries().get(0).getMessage());
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        testHelper.getLogger().clear();
    }

    // консолидация - все источники сразу
    @Test
    public void composeTest() {
        try {
            // вспомогательные данные источника
            // задать источники
            List<Relation> sourcesInfo = new ArrayList<Relation>();
            FormType formType = new FormType();
            formType.setId(916);
            Relation relarion = new Relation();
            relarion.setFormDataId(916L);
            relarion.setFormType(formType);
            sourcesInfo.add(relarion);
            FormType formType2 = new FormType();
            formType2.setId(917);
            Relation relarion2 = new Relation();
            relarion2.setFormDataId(917L);
            relarion2.setFormType(formType2);
            sourcesInfo.add(relarion2);

            when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                    any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(sourcesInfo);

            Set<Integer> sourceTemplateIds = formTypeIdByTemplateIdMap.keySet();
            FormDataKind kind = FormDataKind.SUMMARY;
            for (int sourceTemplateId : sourceTemplateIds) {
                int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

                // форма источника
                FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
                when(testHelper.getFormDataService().getLast(eq(sourceTypeId), eq(kind), eq(DEPARTMENT_ID),
                        anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(sourceFormData);
                when(testHelper.getFormDataService().get(eq(sourceFormData.getId().longValue()), anyBoolean())).thenReturn(sourceFormData);

                // строки и хелпер источника
                List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData);
                DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
                sourceDataRowHelper.save(dataRows);
                sourceDataRowHelper.setAllCached(dataRows);
                when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);
            }

            // провайдеры и получения записей для справочников
            mockProvider(RefBook.WithTable.LAND.getTableRefBookId());

            testHelper.initRowData();

            // Консолидация
            testHelper.execute(FormDataEvent.COMPOSE);

            // ожидается:
            // 3 простые строки (1 строка из формы из 916 + 2 строки из формы 917)
            // 6 строк подитогов (по 2 строки подитога для каждой простой строк)
            // 1 строка всего
            int expected = 3 + 6 + 1;
            Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
            checkLogger();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sortRowsTest() throws ParseException {
        mockProvider(96L);

        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // простая строка 1
        DataRow<Cell> row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue("kno2", null);
        row.getCell("cadastralNumber").setValue("cadastralNumber2", null);
        dataRows.add(row);

        // простая строка 2
        row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue("kno2", null);
        row.getCell("cadastralNumber").setValue("cadastralNumber1", null);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО для строки 1 и 2
        DataRow<Cell> total2Row = formData.createDataRow();
        total2Row.setAlias("total2#1");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total2Row.getCell("oktmo").setValue(row.getCell("oktmo").getValue(), null);
        total2Row.getCell("q1").setValue(1, null);
        total2Row.getCell("q2").setValue(1, null);
        total2Row.getCell("q3").setValue(1, null);
        total2Row.getCell("year").setValue(1, null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО для строки 1 и 2
        DataRow<Cell> total1Row = formData.createDataRow();
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total1Row.getCell("q1").setValue(1, null);
        total1Row.getCell("q2").setValue(1, null);
        total1Row.getCell("q3").setValue(1, null);
        total1Row.getCell("year").setValue(1, null);
        dataRows.add(total1Row);

        // простая строка 3
        row = formData.createDataRow();
        setDefaultValues(row);
        row.getCell("kno").setValue("kno1", null);
        row.getCell("cadastralNumber").setValue("cadastralNumber3", null);
        dataRows.add(row);

        // подитог КНО/КПП/ОКТМО для строки 3
        total2Row = formData.createDataRow();
        total2Row.setAlias("total2#2");
        total2Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total2Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total2Row.getCell("oktmo").setValue(row.getCell("oktmo").getValue(), null);
        total2Row.getCell("q1").setValue(1, null);
        total2Row.getCell("q2").setValue(1, null);
        total2Row.getCell("q3").setValue(1, null);
        total2Row.getCell("year").setValue(1, null);
        dataRows.add(total2Row);

        // подитог КНО/КПП/ОКТМО для строки 3
        total1Row = formData.createDataRow();
        total1Row.setAlias("total1#1");
        total1Row.getCell("kno").setValue(row.getCell("kno").getValue(), null);
        total1Row.getCell("kpp").setValue(row.getCell("kpp").getValue(), null);
        total1Row.getCell("q1").setValue(1, null);
        total1Row.getCell("q2").setValue(1, null);
        total1Row.getCell("q3").setValue(1, null);
        total1Row.getCell("year").setValue(1, null);
        dataRows.add(total1Row);

        // строка всего
        DataRow<Cell> totalRow = formData.createDataRow();
        totalRow.setAlias("total");
        totalRow.getCell("q1").setValue(1, null);
        totalRow.getCell("q2").setValue(1, null);
        totalRow.getCell("q3").setValue(1, null);
        totalRow.getCell("year").setValue(1, null);
        dataRows.add(totalRow);

        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();

        int expected = 8;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        row = testHelper.getDataRowHelper().getAll().get(0);
        Assert.assertEquals("cadastralNumber3", row.getCell("cadastralNumber").getStringValue());
        row = testHelper.getDataRowHelper().getAll().get(3);
        Assert.assertEquals("cadastralNumber1", row.getCell("cadastralNumber").getStringValue());
        row = testHelper.getDataRowHelper().getAll().get(4);
        Assert.assertEquals("cadastralNumber2", row.getCell("cadastralNumber").getStringValue());
    }

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();

    static {
        formTypeIdByTemplateIdMap.put(916, 916); // Расчет земельного налога за отчетные периоды
        formTypeIdByTemplateIdMap.put(917, 917); // Земельные участки, подлежащие включению в декларацию
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();

    static {
        templatesPathMap.put(916, "..//src/main//resources//form_template//land//calc_for_declaration//v2016//");
        templatesPathMap.put(917, "..//src/main//resources//form_template//land//include_in_declaration//v2016//");
    }

    /**
     * Получить форму источника.
     *
     * @param id               идентификатор источника
     * @param sourceTemplateId идентификатор макета источника
     */
    private FormData getSourceFormData(int id, int sourceTemplateId) {
        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate(templatesPathMap.get(sourceTemplateId));

        FormType formType = new FormType();
        formType.setId(formTypeIdByTemplateIdMap.get(sourceTemplateId));
        formType.setTaxType(TaxType.LAND);
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
     * @param sourceFormData   форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData) {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "t" + sourceTemplateId;
        Date testDate = new Date();
        Long testLong = 1L;
        Long testRefbookId = 2L;
        DataRow<Cell> row = sourceFormData.createDataRow();
        row.getCell("department").setValue(testRefbookId, null);
        row.getCell("kno").setValue(testString, null);
        row.getCell("kpp").setValue("kppA710", null);
        row.getCell("kbk").setValue(testRefbookId, null);
        row.getCell("oktmo").setValue(testRefbookId, null);
        row.getCell("cadastralNumber").setValue(testString, null);
        row.getCell("landCategory").setValue(testRefbookId, null);
        row.getCell("cadastralCost").setValue(testLong, null);
        row.getCell("ownershipDate").setValue(testDate, null);
        row.getCell("period").setValue(testLong, null);
        row.getCell("taxRate").setValue(testLong, null);
        row.getCell("kv").setValue(testLong, null);
        row.getCell("q1").setValue(testLong, null);
        row.getCell("q2").setValue(testLong, null);
        row.getCell("q3").setValue(testLong, null);
        row.getCell("year").setValue(testLong, null);
        dataRows.add(row);
        DataRow<Cell> row2 = sourceFormData.createDataRow();
        row2.getCell("department").setValue(testRefbookId, null);
        row2.getCell("kno").setValue(testString, null);
        row2.getCell("kpp").setValue("skip", null);
        row2.getCell("kbk").setValue(testRefbookId, null);
        row2.getCell("oktmo").setValue(testRefbookId, null);
        row2.getCell("cadastralNumber").setValue(testString, null);
        row2.getCell("landCategory").setValue(testRefbookId, null);
        row2.getCell("cadastralCost").setValue(testLong, null);
        row2.getCell("ownershipDate").setValue(testDate, null);
        row2.getCell("period").setValue(testLong, null);
        row2.getCell("taxRate").setValue(testLong, null);
        row2.getCell("kv").setValue(testLong, null);
        row2.getCell("q1").setValue(testLong, null);
        row2.getCell("q2").setValue(testLong, null);
        row2.getCell("q3").setValue(testLong, null);
        row2.getCell("year").setValue(testLong, null);
        dataRows.add(row2);
        return dataRows;
    }

    private void mockProvider(final Long refBookId) {
        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
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