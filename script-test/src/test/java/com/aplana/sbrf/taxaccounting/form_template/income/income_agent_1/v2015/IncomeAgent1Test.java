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
                        map.put("INN", new RefBookValue(RefBookAttributeType.STRING, "1234567894"));
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
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;
        Date date = new Date();

        // для попадания в ЛП:
        // 1. Проверка обязательных полей
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // для попадания в ЛП:
        // 1. Проверка обязательных полей
        int i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        // обязательные (графа 1..3, 7..13, 16, 17, 23..25, 27)
        String [] nonEmptyColumns = { "emitentName", "emitentInn", "decisionNumber", "decisionDate",
                "year", "firstMonth", "lastMonth", "allSum", "addresseeName", "type", "status",
                "dividends", "sum", "date" };
        for (String column : nonEmptyColumns) {
            // TODO (Ramil Timerbaev) недоступен ScriptUtils.WRONG_NON_EMPTY потому что private
            // String msg = String.format(ScriptUtils.WRONG_NON_EMPTY, 1, row.getCell(column).getColumn().getName());
            msg = String.format("Строка %d: Графа «%s» не заполнена!", 1, row.getCell(column).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }

        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 2. Проверка на заполнение зависимого поля ИНН и КПП (графа 14 и 15)
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(1L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        row.getCell("region").setValue(1L, null);
        row.getCell("surname").setValue("test", null);
        row.getCell("name").setValue("test", null);
        row.getCell("withheldSum").setValue(0L, null);
        row.getCell("distributionSum").setValue(0L, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: В случае если графы «%s» равна значению «1» / «3» / «4» / «5» и графа и «%s» равны значению «1», должна быть заполнена графа «%s» и «%s»!",
                row.getCell("type").getColumn().getName(), row.getCell("status").getColumn().getName(), row.getCell("inn").getColumn().getName(), row.getCell("kpp").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5. Проверка контрольной суммы
        String inn = "0123456789";
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue(inn, null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(2L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        row.getCell("region").setValue(1L, null);
        row.getCell("surname").setValue("test", null);
        row.getCell("name").setValue("test", null);
        row.getCell("withheldDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("withheldNumber").setValue("test", null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Вычисленное контрольное число по полю \"%s\" некорректно (%s).",
                row.getCell("emitentInn").getColumn().getName(), inn);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка паттернов - графа 3 - паттерн
        inn = "012345678b";
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue(inn, null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(2L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        row.getCell("region").setValue(1L, null);
        row.getCell("surname").setValue("test", null);
        row.getCell("name").setValue("test", null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\"",
                row.getCell("emitentInn").getColumn().getName(), inn);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = "Строка 1: Расшифровка паттерна \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\": Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 8 символов: (0-9)";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка паттернов - графа 14, 15 - контрольная сумма
        inn = "0123456789";
        String kpp = "12345678";
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(1L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(0L, null);
        row.getCell("inn").setValue(inn, null);
        row.getCell("kpp").setValue(kpp, null);
        row.getCell("region").setValue(1L, null);
        row.getCell("surname").setValue("test", null);
        row.getCell("name").setValue("test", null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Вычисленное контрольное число по полю \"%s\" некорректно (%s).",
                row.getCell("inn").getColumn().getName(), inn);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка 1: Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\"",
                row.getCell("kpp").getColumn().getName(), kpp);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = "Строка 1: Расшифровка паттерна \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\": Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9)";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка паттернов - графа 14 - паттерн
        inn = "012345678b";
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(1L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(0L, null);
        row.getCell("inn").setValue(inn, null);
        row.getCell("kpp").setValue("776001002", null);
        row.getCell("region").setValue(1L, null);
        row.getCell("surname").setValue("test", null);
        row.getCell("name").setValue("test", null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\"",
                row.getCell("inn").getColumn().getName(), inn);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = "Строка 1: Расшифровка паттерна \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\": Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 8 символов: (0-9)";
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка диапазона дат
        Date tmpDate = sdf.parse("01.01.2100");
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(5L, null);
        row.getCell("status").setValue(3L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(tmpDate, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Значение даты графы «%s» должно принимать значение из следующего диапазона: 01.01.1900 - 31.12.2099!",
                row.getCell("date").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 6. Проверка значения «Графы 17» (статус получателя)
        Long status = 9L;
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(5L, null);
        row.getCell("status").setValue(status, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Графа «%s» заполнен неверно (%s)! Возможные значения: «1», «2», «3»",
                row.getCell("status").getColumn().getName(), status.toString());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 7. Проверка значения «Графы 16» (тип получателя)
        Long type = 9L;
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(type, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Графа «%s» заполнена неверно (%s)! Возможные значения: «1», «2», «3», «4», «5»",
                row.getCell("type").getColumn().getName(), type.toString());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 8. Проверка значения «Графы 9» (отчетный год)
        String emitentInn = "1234567894";
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue(emitentInn, null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(2L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        row.getCell("all").setValue(0L, null);
        row.getCell("rateZero").setValue(0L, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Графа «%s» заполнена неверно (%s)! Для Банка " +
                "(графа «%s» = ИНН %s формы настроек подразделения формы) по данной графе может быть указан " +
                "отчетный год формы или предыдущие отчетные года с периодом давности до четырех лет включительно.",
                row.getCell("year").getColumn().getName(), "2016", row.getCell("emitentInn").getColumn().getName(), emitentInn);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 9. Проверка значения «Графы 10» и «Графы 11» (период распределения)
        Long firstMonth = 13L;
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(firstMonth, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(2L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(1L, null);
        row.getCell("inn").setValue(null, null);
        row.getCell("kpp").setValue(null, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: Графа «%s» заполнена неверно (%s)! Возможные значения: «1», «2», «3», «4», «5», «6», «7», «8», «9», «10», «11», «12»",
                row.getCell("firstMonth").getColumn().getName(), firstMonth.toString());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 10. Проверка уникальности значения графы 7 (номер решения)
        // if (row.emitentInn && departmentInn && row.emitentInn == departmentInn && row.year && row.decisionNumber) {
        emitentInn = "1234567894";
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        dataRows.add(row2);
        tmpDate = sdf.parse("01.01.2014");
        for (DataRow<Cell> dataRow : dataRows) {
            dataRow.getCell("rowNum").setValue(dataRow.getIndex(), null);
            dataRow.getCell("emitentName").setValue("test", null);
            dataRow.getCell("emitentInn").setValue(emitentInn, null);
            dataRow.getCell("decisionNumber").setValue("test" + dataRow.getIndex(), null);
            dataRow.getCell("decisionDate").setValue(date, null);
            dataRow.getCell("year").setValue(tmpDate, null);
            dataRow.getCell("firstMonth").setValue(1L, null);
            dataRow.getCell("lastMonth").setValue(1L, null);
            dataRow.getCell("allSum").setValue(1L, null);
            dataRow.getCell("all").setValue(1L, null);
            dataRow.getCell("rateZero").setValue(1L, null);
            dataRow.getCell("distributionSum").setValue(1L, null);
            dataRow.getCell("addresseeName").setValue("test", null);
            dataRow.getCell("type").setValue(2L, null);
            dataRow.getCell("status").setValue(1L, null);
            dataRow.getCell("dividends").setValue(1L, null);
            dataRow.getCell("sum").setValue(1L, null);
            dataRow.getCell("date").setValue(tmpDate, null);
            dataRow.getCell("withheldSum").setValue(1L, null);
            dataRow.getCell("withheldDate").setValue(sdf.parse("01.01.2015"), null);
            dataRow.getCell("withheldNumber").setValue("test", null);
            dataRow.getCell("inn").setValue(null, null);
            dataRow.getCell("kpp").setValue(null, null);
        }
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строки 1, 2: Неуникальное значение графы «%s» (%s) в рамках «%s» = «%s» для строк Банка (графа «%s» = ИНН %s формы настроек подразделения формы)!",
                row.getCell("decisionNumber").getColumn().getName(), "test1, test2", row.getCell("year").getColumn().getName(), "2014", row.getCell("emitentInn").getColumn().getName(), emitentInn);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11. Проверка на заполнение зависимых полей Код региона, Фамилия, Имя (графы 31, 39, 40) - Начиная с периода формы «9 месяцев 2015»
        dataRows.clear();
        dataRows.add(row);
        row.getCell("emitentName").setValue("test", null);
        row.getCell("emitentInn").setValue("0123456788", null);
        row.getCell("decisionNumber").setValue("test", null);
        row.getCell("decisionDate").setValue(date, null);
        row.getCell("year").setValue(date, null);
        row.getCell("firstMonth").setValue(1L, null);
        row.getCell("lastMonth").setValue(1L, null);
        row.getCell("allSum").setValue(1L, null);
        row.getCell("addresseeName").setValue("test", null);
        row.getCell("type").setValue(1L, null);
        row.getCell("status").setValue(1L, null);
        row.getCell("dividends").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("date").setValue(date, null);
        row.getCell("withheldSum").setValue(0L, null);
        row.getCell("inn").setValue("0123456788", null);
        row.getCell("kpp").setValue("776001002", null);
        row.getCell("region").setValue(null, null);
        i = 0;
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка 1: В случае если графа «%s» не равна значению «2» и графа «%s» равна значению «1», должна быть заполнена графа «%s», «%s» и «%s»!",
                row.getCell("type").getColumn().getName(), row.getCell("status").getColumn().getName(),
                row.getCell("region").getColumn().getName(), row.getCell("surname").getColumn().getName(),
                row.getCell("name").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
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
