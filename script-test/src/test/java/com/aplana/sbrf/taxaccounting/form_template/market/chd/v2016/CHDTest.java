package com.aplana.sbrf.taxaccounting.form_template.market.chd.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Отчет о кредитах (ЦХД)
 */
public class CHDTest extends ScriptTestBase {
    private static final int TYPE_ID = 903;
    private static final int DEPARTMENT_ID = 1;
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
        return getDefaultScriptTestMockHelper(CHDTest.class);
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

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // 1. Проверка заполнения граф
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Наименование заёмщика"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "ОПФ"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "ИНН / КИО заёмщика"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номер кредитного договора"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата кредитного договора (дд.мм.гг.)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата планируемого погашения с учетом последней пролонгации (дд.мм.гг.)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Частичное погашение основного долга (Да / Нет)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Срок кредита, лет"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Валюта суммы кредита"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма кредита (по договору), ед. валюты"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Процентная ставка, % годовых"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 2. Неотрицательность графы
        // 3. Проверка даты кредитного договора
        // 5. Проверка даты выдачи кредита
        // 6. Проверка даты погашения кредита
        // 7. Проверка даты погашения кредита 2
        row.getCell("name").setValue("string1", null);
        row.getCell("opf").setValue(1L, null);
        row.getCell("innKio").setValue("string2", null);
        row.getCell("creditRating").setValue(1L, null);
        row.getCell("docNum").setValue("string3", null);
        row.getCell("docDate").setValue(sdf.parse("04.01.2990"), null);
        row.getCell("docDate2").setValue(sdf.parse("03.01.2990"), null);
        row.getCell("docDate3").setValue(sdf.parse("02.01.2990"), null);
        row.getCell("partRepayment").setValue(1L, null);
        row.getCell("creditPeriod").setValue(-1L, null);
        row.getCell("currencyCode").setValue(1L, null);
        row.getCell("creditSum").setValue(-1L, null);
        row.getCell("creditRate").setValue(1, null);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Срок кредита, лет» должно быть больше либо равно 0!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма кредита (по договору), ед. валюты» должно быть больше либо равно 0!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Дата кредитного договора (дд.мм.гг.)","01.01.2014", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата выдачи (дд.мм.гг.)» должно быть больше либо равно значению графы «Дата кредитного договора (дд.мм.гг.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата планируемого погашения с учетом последней пролонгации (дд.мм.гг.)» должно быть больше либо равно значению графы «Дата кредитного договора (дд.мм.гг.)»!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата планируемого погашения с учетом последней пролонгации (дд.мм.гг.)» должно быть больше либо равно значению графы «Дата выдачи (дд.мм.гг.)»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 4. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
        row.getCell("creditPeriod").setValue(1L, null);
        row.getCell("creditSum").setValue(0L, null);
        row.getCell("docDate").setValue(sdf.parse("02.01.2014"), null);
        row.getCell("docDate2").setValue(sdf.parse("03.01.2014"), null);
        row.getCell("docDate3").setValue(sdf.parse("04.01.2014"), null);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: На форме уже существует строка со значениями граф «ИНН / КИО заёмщика» = «string2», «Номер кредитного договора» = «string3», «Дата кредитного договора (дд.мм.гг.)» = «02.01.2014»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        dataRows.remove(1);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertTrue(testHelper.getLogger().getEntries().isEmpty());
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
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
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        expected--;
        // Количество строк должно уменьшиться на 1
        Assert.assertEquals("Delete row", expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        // для справочника стран мира (id = 10)
        RefBookUniversal provider = mock(RefBookUniversal.class);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(10L), anyMap())).thenReturn(provider);
        List<Long> ids = Arrays.asList(1L, 2L);
        when(provider.getUniqueRecordIds(any(Date.class), (String) eq(null))).thenReturn(ids);
        when(provider.getRecordData(eq(ids))).thenReturn(testHelper.getRefBookAllRecords(10L));

        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        List<String> aliases = Arrays.asList("name", "innKio", "docNum", "docDate", "docDate2", "docDate3", "creditPeriod", "creditSum", "creditRate");
        defaultCheckLoadData(aliases, expected);
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("opf").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("opf").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("country").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("country").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("creditRating").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("creditRating").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("partRepayment").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("partRepayment").getNumericValue().longValue());

        Assert.assertEquals(1L, dataRows.get(0).getCell("currencyCode").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("currencyCode").getNumericValue().longValue());
    }
}