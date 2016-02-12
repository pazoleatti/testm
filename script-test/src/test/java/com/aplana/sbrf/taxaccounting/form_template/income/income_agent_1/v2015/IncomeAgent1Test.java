package com.aplana.sbrf.taxaccounting.form_template.income.income_agent_1.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов) (с 9 месяцев 2015).
 */
public class IncomeAgent1Test extends ScriptTestBase {
    private static final int TYPE_ID = 314;
    private static final int DEPARTMENT_ID = 2;
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
        return getDefaultScriptTestMockHelper(IncomeAgent1Test.class);
    }

    @Before
    public void mockFormDataService() {
        when(testHelper.getRefBookFactory().getDataProvider(any(Long.class))).thenAnswer(
                new Answer<RefBookDataProvider>() {
                    @Override
                    public RefBookDataProvider answer(InvocationOnMock invocation) throws Throwable {
                        return testHelper.getRefBookDataProvider();
                    }
                });

        when(testHelper.getRefBookFactory().getDataProvider(33L).getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenAnswer(
                new Answer<PagingResult<Map<String, RefBookValue>>>() {
                    @Override
                    public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();

                        Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                        map.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1L));
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, "INN"));
                        result.add(map);

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
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//income_agent_1//v2015//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка обязательных полей
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;

        // обязательные (графа 1..3, 7..13, 16, 17, 23..25, 27)
        String [] nonEmptyColumns = { "emitentName", "emitentInn", "decisionNumber", "decisionDate",
                "year", "firstMonth", "lastMonth", "allSum", "addresseeName", "type", "status",
                "dividends", "sum", "date", "withheldSum" };
        for (String column : nonEmptyColumns) {
            // TODO (Ramil Timerbaev) недоступен ScriptUtils.WRONG_NON_EMPTY потому что private
            // String msg = String.format(ScriptUtils.WRONG_NON_EMPTY, 1, row.getCell(column).getColumn().getName());
            String msg = String.format("Строка %d: Графа «%s» не заполнена!", 1, row.getCell(column).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // TODO (Ramil Timerbaev) дополнить
        // для попадания в ЛП:
        // 1. Проверка обязательных полей
        // 2. Проверка на заполнение зависимого поля ИНН и КПП (графа 14 и 15)
        // 3. Проверка паттернов (+ 5. Проверка контрольной суммы)
        // 4. Проверка диапазона дат
        // 5. Проверка контрольной суммы
        // 6. Проверка значения «Графы 17» (статус получателя)
        // 7. Проверка значения «Графы 16» (тип получателя)
        // 8. Проверка значения «Графы 9» (отчетный год)
        // 9. Проверка значения «Графы 10» и «Графы 11» (период распределения)
        // 10. Проверка уникальности значения графы 7 (номер решения)
        // 11. Проверка на заполнение зависимых полей Код региона, Фамилия, Имя (графы 31, 39, 40) - Начиная с периода формы «9 месяцев 2015»
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        int expected = 1; // в файле 1 строка
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 1; // в файле 1 строка
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        int testRecordId = 1;

        // графа 2
        Assert.assertEquals("test1", dataRows.get(0).getCell("emitentName").getStringValue());
        // графа 3
        Assert.assertEquals("test2", dataRows.get(0).getCell("emitentInn").getStringValue());
        // графа 4
        Assert.assertEquals(4, dataRows.get(0).getCell("all").getNumericValue().intValue());
        // графа 5
        Assert.assertEquals(5, dataRows.get(0).getCell("rateZero").getNumericValue().intValue());
        // графа 6
        Assert.assertEquals(6, dataRows.get(0).getCell("distributionSum").getNumericValue().intValue());
        // графа 7
        Assert.assertEquals("test7", dataRows.get(0).getCell("decisionNumber").getStringValue());
        // графа 8
        Assert.assertNotNull(dataRows.get(0).getCell("decisionDate").getDateValue());
        // графа 9
        Assert.assertNotNull(dataRows.get(0).getCell("year").getDateValue());
        // графа 10
        Assert.assertEquals(10, dataRows.get(0).getCell("firstMonth").getNumericValue().intValue());
        // графа 11
        Assert.assertEquals(11, dataRows.get(0).getCell("lastMonth").getNumericValue().intValue());
        // графа 12
        Assert.assertEquals(12, dataRows.get(0).getCell("allSum").getNumericValue().intValue());
        // графа 13
        Assert.assertEquals("test13", dataRows.get(0).getCell("addresseeName").getStringValue());
        // графа 14
        Assert.assertEquals("test14", dataRows.get(0).getCell("inn").getStringValue());
        // графа 15
        Assert.assertEquals("test15", dataRows.get(0).getCell("kpp").getStringValue());
        // графа 16
        Assert.assertEquals(16, dataRows.get(0).getCell("type").getNumericValue().intValue());
        // графа 17
        Assert.assertEquals(1, dataRows.get(0).getCell("status").getNumericValue().intValue());
        // графа 18
        Assert.assertNotNull(dataRows.get(0).getCell("birthday").getDateValue());
        // графа 19
        Assert.assertEquals(testRecordId, dataRows.get(0).getCell("citizenship").getNumericValue().intValue());
        // графа 20
        Assert.assertEquals(testRecordId, dataRows.get(0).getCell("kind").getNumericValue().intValue());
        // графа 21
        Assert.assertEquals("test21", dataRows.get(0).getCell("series").getStringValue());
        // графа 22
        Assert.assertEquals(22, dataRows.get(0).getCell("rate").getNumericValue().intValue());
        // графа 23
        Assert.assertEquals(23, dataRows.get(0).getCell("dividends").getNumericValue().intValue());
        // графа 24
        Assert.assertEquals(24, dataRows.get(0).getCell("sum").getNumericValue().intValue());
        // графа 25
        Assert.assertNotNull(dataRows.get(0).getCell("date").getDateValue());
        // графа 26
        Assert.assertEquals("test26", dataRows.get(0).getCell("number").getStringValue());
        // графа 27
        Assert.assertEquals(27, dataRows.get(0).getCell("withheldSum").getNumericValue().intValue());
        // графа 28
        Assert.assertNotNull(dataRows.get(0).getCell("withheldDate").getDateValue());
        // графа 29
        Assert.assertEquals("test29", dataRows.get(0).getCell("withheldNumber").getStringValue());
        // графа 30
        Assert.assertEquals("test30", dataRows.get(0).getCell("postcode").getStringValue());
        // графа 31
        Assert.assertEquals(testRecordId, dataRows.get(0).getCell("region").getNumericValue().intValue());
        // графа 32
        Assert.assertEquals("test32", dataRows.get(0).getCell("district").getStringValue());
        // графа 33
        Assert.assertEquals("test33", dataRows.get(0).getCell("city").getStringValue());
        // графа 34
        Assert.assertEquals("test34", dataRows.get(0).getCell("locality").getStringValue());
        // графа 35
        Assert.assertEquals("test35", dataRows.get(0).getCell("street").getStringValue());
        // графа 36
        Assert.assertEquals("test36", dataRows.get(0).getCell("house").getStringValue());
        // графа 37
        Assert.assertEquals("test37", dataRows.get(0).getCell("housing").getStringValue());
        // графа 38
        Assert.assertEquals("test38", dataRows.get(0).getCell("apartment").getStringValue());
        // графа 39
        Assert.assertEquals("test39", dataRows.get(0).getCell("surname").getStringValue());
        // графа 40
        Assert.assertEquals("test40", dataRows.get(0).getCell("name").getStringValue());
        // графа 41
        Assert.assertEquals("test41", dataRows.get(0).getCell("patronymic").getStringValue());
        // графа 42
        Assert.assertEquals("test42", dataRows.get(0).getCell("phone").getStringValue());
    }
}
