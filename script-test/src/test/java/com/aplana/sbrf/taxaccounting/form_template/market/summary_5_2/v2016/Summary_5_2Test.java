package com.aplana.sbrf.taxaccounting.form_template.market.summary_5_2.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
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
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 5.2 Отчет о выданных Банком кредитах
 */
public class Summary_5_2Test extends ScriptTestBase {
    private static final int TYPE_ID = 907;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private static final HashMap<Integer, Integer> formTypeIdByTemplateIdMap = new LinkedHashMap<Integer, Integer>();
    static {
        formTypeIdByTemplateIdMap.put(901, 901); // Данные MIS
        formTypeIdByTemplateIdMap.put(902, 902); // Данные АМРЛИРТ
        formTypeIdByTemplateIdMap.put(903, 903); // Отчет о кредитах (ЦХД)
        formTypeIdByTemplateIdMap.put(904, 904); // 2.6 (Сводный) Отчет о состоянии кредитного портфеля
        formTypeIdByTemplateIdMap.put(905, 905); // 7.129 (Ежемесячный) Кредитные договоры в CRM
    }

    private static final HashMap<Integer, String> templatesPathMap = new LinkedHashMap<Integer, String>();
    static {
        templatesPathMap.put(901, "..//src/main//resources//form_template//market//mis//v2016//");
        templatesPathMap.put(902, "..//src/main//resources//form_template//market//amrlirt//v2016//");
        templatesPathMap.put(903, "..//src/main//resources//form_template//market//chd//v2016//");
        templatesPathMap.put(904, "..//src/main//resources//form_template//market//summary_2_6//v2016//");
        templatesPathMap.put(905, "..//src/main//resources//form_template//market//market_7_129//v2016//");
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
        return getDefaultScriptTestMockHelper(Summary_5_2Test.class);
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
        // ошибок быть не должно
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
    }

    @Test
    public void calc1Test() throws ParseException {
        mockProviders();
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();

        // строка
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // нормальный расчет
        setDefaultValues(row);
        row.getCell("closeDate").setValue(sdf.parse("01.01.2015"), null);
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
        // проверка значении
        double expected;

        // графа 9
        expected = 1L;
        Assert.assertEquals(expected, row.getCell("internationalRating").getNumericValue().longValue(), 0);

        // графа 15
        expected = 1.0;
        Assert.assertEquals(expected, row.getCell("avgPeriod").getNumericValue().longValue(), 0);

        // графа 22
        expected = row.getCell("rate").getNumericValue().longValue() + row.getCell("creditRate").getNumericValue().longValue();
        Assert.assertEquals(expected, row.getCell("totalRate").getNumericValue().longValue(), 0);

        // графа 27
        expected = row.getCell("purposeFond").getNumericValue().abs().longValue() - row.getCell("purposeSum").getNumericValue().abs().longValue();
        Assert.assertEquals(expected, row.getCell("economySum").getNumericValue().longValue(), 0);

        // графа 28
        expected = row.getCell("totalRate").getNumericValue().longValue() + row.getCell("economySum").getNumericValue().longValue();
        Assert.assertEquals(expected, row.getCell("economyRate").getNumericValue().doubleValue(), 0);
    }

    @Test
    public void chec1kTest() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;
        int i;

        // строка
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // нет ошибок
        i = 0;
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 1. Проверка заполнения обязательных полей
        i = 0;
        String [] nonEmptyColumns = { "dealNum", "debtorName", "relatedPerson", "offshore", "innKio",
                "creditRating", "docNum", "docDate", "closeDate", "partRepayment", "avgPeriod", "currencyCode",
                "creditSum", "rateType", "rate", "creditRate", "totalRate", "provideCategory", "specialPurpose",
                "purposeSum", "purposeFond", "economySum", "economyRate", "groupExclude" };
        for (String alias : nonEmptyColumns) {
            row.getCell(alias).setValue(null, null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        for (String alias : nonEmptyColumns) {
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), row.getCell(alias).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 2. Проверка валюты
        i = 0;
        setDefaultValues(row);
        row.getCell("currencyCode").setValue(5L, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Для российского рубля должно быть проставлено буквенное значение RUR!", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Неотрицательность графы
        i = 0;
        setDefaultValues(row);
        row.getCell("economyRate").setValue(-1L, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно 0!",
                row.getIndex(), row.getCell("economyRate").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4.1 Проверка ИНН на паттерн (меньше 10 цифр)
        i = 0;
        setDefaultValues(row);
        row.getCell("innKio").setValue("1", null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно состоять из 10 цифр. Первые две цифры принимают значения 0-9 и 1-9 ИЛИ 1-9 и 0-9!",
                row.getIndex(), row.getCell("innKio").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4.2 Проверка ИНН на паттерн (несоответствие паттерну)
        i = 0;
        setDefaultValues(row);
        row.getCell("innKio").setValue("9999999997", null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Вычисленное контрольное число по полю \"%s\" некорректно (%s).",
                row.getIndex(), row.getCell("innKio").getColumn().getName(), row.getCell("innKio").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4.3 Проверка ИНН на паттерн (все нормально)
        i = 0;
        setDefaultValues(row);
        row.getCell("innKio").setValue("9999999999", null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(i, entries.size());
        checkLogger();
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

    @Test
    public void compose1Test() throws ParseException {
        Set<Integer> sourceTemplateIds = formTypeIdByTemplateIdMap.keySet();
        List<Relation> relations = new ArrayList<Relation>(sourceTemplateIds.size());
        for (int sourceTemplateId : sourceTemplateIds) {
            int sourceTypeId = formTypeIdByTemplateIdMap.get(sourceTemplateId);

            // источник
            Relation relation = getSource(sourceTypeId, sourceTypeId);
            relations.add(relation);

            // форма источника
            FormData sourceFormData = getSourceFormData(sourceTemplateId, sourceTemplateId);
            when(testHelper.getFormDataService().get(eq(sourceFormData.getId().longValue()), anyBoolean())).thenReturn(sourceFormData);

            // строки и хелпер источника
            List<DataRow<Cell>> dataRows = getFillDataRows(sourceTemplateId, sourceFormData);
            DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
            sourceDataRowHelper.save(dataRows);
            when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

            // тип формы
            FormType formType = new FormType();
            formType.setName(String.valueOf(sourceTypeId));
            when(testHelper.getFormTypeService().get(eq(sourceTypeId))).thenReturn(formType);
        }
        // источник
        when(testHelper.getFormDataService().getSourcesInfo(any(FormData.class), anyBoolean(), anyBoolean(),
                any(WorkflowState.class), any(TAUserInfo.class), any(Logger.class))).thenReturn(relations);
        // подразделение
        Department department = new Department();
        department.setName("testDepartmentName");
        when(testHelper.getDepartmentService().get(anyInt())).thenReturn(department);
        // период
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName("testReportPeriodName");
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(reportPeriod);

        // провайдеры и получения записей для справочников
        mockProviders();

        testHelper.initRowData();

        // консолидация
        testHelper.execute(FormDataEvent.COMPOSE);

        int expected = 1; // 1 подходящая запись из 2.6
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        String msg = String.format("Форма-источник «%s», Подразделение: «%s», Период: «%s», строка %s: Сделка не была включена в отчет, т.к. не найдена связанная строка в формах-источниках: %s!",
                904, department.getName(), reportPeriod.getName(), 2, "«903», «901»");
        int i = 0;
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    /**
     * Получить источник.
     *
     * @param id идентификатор источника
     * @param sourceTypeId идентификатор типа формы источника
     */
    private Relation getSource(int id, int sourceTypeId) {
        Relation relation = new Relation();
        relation.setFormDataId((long) id);
        FormType formType = new FormType();
        formType.setId(sourceTypeId);
        relation.setFormType(formType);
        return relation;
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
        sourceFormData.setDepartmentId(DEPARTMENT_ID);
        sourceFormData.setReportPeriodId(REPORT_PERIOD_ID);

        return sourceFormData;
    }

    /**
     * Получить заполненные строки источника.
     *
     * @param sourceTemplateId идентификатор макета источника
     * @param sourceFormData форма источника
     */
    private List<DataRow<Cell>> getFillDataRows(int sourceTemplateId, FormData sourceFormData) throws ParseException {
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        String testString = "test_" + sourceTemplateId;
        Long testLong = 10L;
        Long testRefbookId = 2L;

        String inn = "6164248192";
        String docNum = "123";
        Date docDate = sdf.parse("01.01.2014");

        DataRow<Cell> row;
        switch (sourceTemplateId) {
            case 901 : // Данные MIS
                row = sourceFormData.createDataRow();
                row.getCell("innKio").setValue(inn, null);
                row.getCell("docNum").setValue(docNum, null);
                row.getCell("docDate").setValue(docDate, null);

                row.getCell("rateType").setValue(testRefbookId, null);
                row.getCell("creditRate").setValue(testLong, null);
                row.getCell("specialPurpose").setValue(testRefbookId, null);
                row.getCell("fondRate").setValue(testLong, null);
                row.getCell("etsRate").setValue(testLong, null);
                dataRows.add(row);
                break;
            case 902 : // Данные АМРЛИРТ
                break;
            case 903 : // Отчет о кредитах (ЦХД)
                row = sourceFormData.createDataRow();
                row.getCell("innKio").setValue(inn, null);
                row.getCell("docNum").setValue(docNum, null);
                row.getCell("docDate").setValue(docDate, null);

                row.getCell("partRepayment").setValue(testRefbookId, null);
                dataRows.add(row);
                break;
            case 904 : // 2.6 (Сводный) Отчет о состоянии кредитного портфеля
                // строка 1
                row = sourceFormData.createDataRow();
                // графа 2
                row.getCell("codeBank").setValue(testString, null);
                // графа 6
                row.getCell("opf").setValue(1L, null);
                // графа 7
                row.getCell("debtorName").setValue(testString, null);
                // графа 8
                row.getCell("inn").setValue(inn, null);
                // графа 9
                row.getCell("sign").setValue(0L, null);
                // графа 11
                row.getCell("law").setValue(testString, null);
                // графа 12
                row.getCell("creditType").setValue(testString, null);
                // графа 13
                row.getCell("docNum").setValue(docNum, null);
                // графа 14
                row.getCell("docDate").setValue(docDate, null);
                // графа 16
                row.getCell("closeDate").setValue(docDate, null);
                // графа 19
                row.getCell("currencySum").setValue(testRefbookId, null);
                // графа 20
                row.getCell("sumDoc").setValue(testLong, null);
                // графа 22
                row.getCell("rate").setValue(testLong, null);
                // графа 38
                row.getCell("creditRisk").setValue(testRefbookId, null);
                dataRows.add(row);

                // строка 2 - не должна попасть в приемник
                row = sourceFormData.createDataRow();
                // графа 6
                row.getCell("opf").setValue(1L, null);
                // графа 7
                row.getCell("debtorName").setValue(testString, null);
                // графа 8
                row.getCell("inn").setValue(inn, null);
                // графа 9
                row.getCell("sign").setValue(0L, null);
                // графа 11
                row.getCell("law").setValue(testString, null);
                // графа 12
                row.getCell("creditType").setValue(testString, null);
                // графа 13
                row.getCell("docNum").setValue("321", null);
                // графа 14
                row.getCell("docDate").setValue(docDate, null);
                dataRows.add(row);
                break;
            case 905 : // 7.129 (Ежемесячный) Кредитные договоры в CRM
                break;
        }
        return dataRows;
    }

    private void setDefaultValues(DataRow<Cell> row) throws ParseException {
        int index = row.getIndex();
        long refbookRecordId = 1L;
        long number = 1L;
        Date date = sdf.parse("01.01.2014");
        String str = "test";

        // графа 1
        row.getCell("rowNum").setValue(number, index);
        // графа 2
        row.getCell("dealNum").setValue(str, index);
        // графа 3
        row.getCell("debtorName").setValue(str, index);
        // графа 4
        row.getCell("country").setValue(refbookRecordId, index);
        // графа 5
        row.getCell("relatedPerson").setValue(refbookRecordId, index);
        // графа 6
        row.getCell("offshore").setValue(refbookRecordId, index);
        // графа 7
        row.getCell("innKio").setValue("6164248192", index);
        // графа 8
        row.getCell("creditRating").setValue(refbookRecordId, index);
        // графа 9
        row.getCell("internationalRating").setValue(refbookRecordId, index);
        // графа 10
        row.getCell("docNum").setValue(str, index);
        // графа 11
        row.getCell("docDate").setValue(date, index);
        // графа 12
        row.getCell("creditDate").setValue(date, index);
        // графа 13
        row.getCell("closeDate").setValue(date, index);
        // графа 14
        row.getCell("partRepayment").setValue(refbookRecordId, index);
        // графа 15
        row.getCell("avgPeriod").setValue(number, index);
        // графа 16
        row.getCell("currencyCode").setValue(refbookRecordId, index);
        // графа 17
        row.getCell("creditSum").setValue(number, index);
        // графа 18
        row.getCell("rateType").setValue(refbookRecordId, index);
        // графа 19
        row.getCell("rateBase").setValue(str, index);
        // графа 20
        row.getCell("rate").setValue(number, index);
        // графа 21
        row.getCell("creditRate").setValue(number, index);
        // графа 22
        row.getCell("totalRate").setValue(number, index);
        // графа 23
        row.getCell("provideCategory").setValue(refbookRecordId, index);
        // графа 24
        row.getCell("specialPurpose").setValue(refbookRecordId, index);
        // графа 25
        row.getCell("purposeSum").setValue(number, index);
        // графа 26
        row.getCell("purposeFond").setValue(number, index);
        // графа 27
        row.getCell("economySum").setValue(number, index);
        // графа 28
        row.getCell("economyRate").setValue(number, index);
        // графа 29
        row.getCell("groupExclude").setValue(refbookRecordId, index);
    }

    private void mockProviders() {
        // справочник 607 исключен, потому что надо что б в нем ничего не нашлось, иначе консолидация не пройдет
        long [] refBookIds = { 519L, 520L, 603L, 606L /*, 607L*/ };
        for (long id : refBookIds) {
            mockProvider(id);
        }
    }

    private void mockProvider(final Long refBookId) {
        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
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