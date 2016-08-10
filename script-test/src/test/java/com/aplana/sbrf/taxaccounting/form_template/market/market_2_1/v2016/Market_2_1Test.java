package com.aplana.sbrf.taxaccounting.form_template.market.market_2_1.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * 2.1 (Ежемесячный) Реестр выданных Банком гарантий (контргарантий, поручительств).
 */
public class Market_2_1Test extends ScriptTestBase {
    private static final int TYPE_ID = 909;
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
        return getDefaultScriptTestMockHelper(Market_2_1Test.class);
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

    @Test
    public void check1Test() {
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);
        String msg;
        int i;
        int rowIndex = row.getIndex();

        // Дополнительная проверка - ошибок быть не должно
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        checkLogger();
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 1. Проверка заполнения обязательных полей
        // очистить значения
        for (Column column : formData.getFormColumns()) {
            row.getCell(column.getAlias()).setValue(null, rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);
        // должно быть много сообщении об незаполненности обязательных полей
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
        // графа 1..4, 7, 10, 11, 15, 16, 19, 20, 25..29, 31..37
        String [] nonEmptyColumns = { "code", "name", "rowNum", "guarantor", "procuct1", "taxpayerName", "taxpayerInn",
                "beneficiaryName", "beneficiaryInn", "number", "issuanceDate", "sumInCurrency",
                "sumInRub", "currency", "debtBalance", "isNonRecurring", "isCharged", "tariff", "remuneration",
                "remunerationStartYear", "remunerationIssuance", "provide", "numberGuarantee" };
        i = 0;
        for (String alias : nonEmptyColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 2. Неотрицательность графы
        setDefaultValues(row);
        // графа 28, 32..35, 43, 44, 46, 48
        String [] nonNegativeColumns = { "debtBalance", "tariff", "remuneration", "remunerationStartYear",
                "remunerationIssuance", "sumDiversion", "arrears", "arrearsGuarantee", "reserve" };
        for (String alias : nonNegativeColumns) {
            row.getCell(alias).setValue(-1, rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        for (String alias : nonNegativeColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно 0!", rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 3. Положительность графы
        setDefaultValues(row);
        String [] positiveColumns = { "sumInCurrency", "sumInRub" };
        for (String alias : positiveColumns) {
            row.getCell(alias).setValue(-1, rowIndex);
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        for (String alias : positiveColumns) {
            String columnName = row.getCell(alias).getColumn().getName();
            msg = String.format("Строка %s: Значение графы «%s» должно быть больше 0!", rowIndex, columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 4. Проверка даты выдачи кредита
        setDefaultValues(row);
        Date tmpDate = new Date(0L);
        row.getCell("endDate").setValue(tmpDate, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        String columnName1 = row.getCell("endDate").getColumn().getName();
        String columnName2 = row.getCell("issuanceDate").getColumn().getName();
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»!",
                rowIndex, columnName1, columnName2);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 5. Проверка валюты
        int rubRecordId = 4;
        setDefaultValues(row);
        row.getCell("currency").setValue(rubRecordId, rowIndex);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Для российского рубля должно быть проставлено буквенное значение RUR!", rowIndex);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // 6. Проверка на отсутствие нескольких записей по одном и тому же гарантийному обязательству
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        dataRows.add(row2);
        setDefaultValues(row);
        setDefaultValues(row2);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        msg = String.format("Строка %s: На форме уже существует строка со значениями граф «%s» = «%s», «%s» = «%s», «%s» = «%s»!",
                row2.getIndex(),
                row2.getCell("taxpayerInn").getColumn().getName(),  row2.getCell("taxpayerInn").getValue(),
                row2.getCell("number").getColumn().getName(),       row2.getCell("number").getValue(),
                row2.getCell("issuanceDate").getColumn().getName(), format.format(row2.getCell("issuanceDate").getValue()));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);
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
        FormTemplate formTemplate = testHelper.getFormTemplate();
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(formTemplate);
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        int expected = 1; // в файле 1 строка
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Double number = 1.00;
        compareRow(dataRows.get(0),
                "1",                    // графа 1  (1.1)
                "1",                    // графа 2  (1.2)
                null,                   // графа 3  (2)
                "1",                    // графа 4  (3)
                "1",                    // графа 5  (3.1)
                "1",                    // графа 6  (3.2)
                "1",                    // графа 7  (4.1)
                "1",                    // графа 8  (4.2)
                "1",                    // графа 9  (4.3)
                "1",                    // графа 10 (5)
                "1",                    // графа 11 (6)
                "1",                    // графа 12 (6.1)
                1L,                     // графа 13 (7.1)
                1L,                     // графа 14 (7.2)
                "1",                    // графа 15 (8)
                "1",                    // графа 16 (9)
                "1",                    // графа 17 (10)
                "1",                    // графа 18 (11)
                "1",                    // графа 19 (12)
                "01.01.2016",           // графа 20 (13)
                "01.01.2016",           // графа 21 (14)
                "01.01.2016",           // графа 22 (15)
                "1",                    // графа 23 (16)
                "01.01.2016",           // графа 24 (17)
                number,                 // графа 25 (18.1)
                number,                 // графа 26 (18.2)
                5L,                     // графа 27 (19)
                number,                 // графа 28 (20)
                2L,                     // графа 29 (21.1)
                "1",                    // графа 30 (21.2)
                2L,                     // графа 31 (22.1)
                number,                 // графа 32 (22.2)
                number,                 // графа 33 (22.3)
                number,                 // графа 34 (22.4)
                number,                 // графа 35 (22.5)
                "1",                    // графа 36 (23)
                "1",                    // графа 37 (24)
                "1",                    // графа 38 (25)
                2L,                     // графа 39 (26)
                "01.01.2016",           // графа 40 (26.1)
                number,                 // графа 41 (26.2)
                "1",                    // графа 42 (26.3)
                number,                 // графа 43 (26.4)
                number,                 // графа 44 (27.1)
                "01.01.2016",           // графа 45 (27.2)
                number,                 // графа 46 (28.1)
                "01.01.2016",           // графа 47 (28.2)
                number,                 // графа 48 (29)
                "1",                    // графа 49 (30)
                "1"                     // графа 50 (31)
        );
    }

    void compareRow(DataRow<Cell> row, Object... args) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        List<Column> columns = testHelper.getFormTemplate().getColumns();
        for (int i = 0; i < columns.size() - 1; i++) {
            Column column = columns.get(i);
            Object expected = null;
            String alias = column.getAlias();
            if (i < args.length) {
                expected = args[i];
            }
            if (expected != null) {
                if (column.getColumnType() == ColumnType.NUMBER) {
                    expected = BigDecimal.valueOf((Double) expected).setScale(((NumericColumn)column).getPrecision(), BigDecimal.ROUND_HALF_UP);
                }
                if (column.getColumnType() == ColumnType.DATE) {
                    try {
                        expected = format.parse((String)expected);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, row.getCell(alias).getValue());
            } else {
                Assert.assertNull("row." + alias + "[" + row.getIndex() + "]", row.getCell(alias).getValue());
            }
        }
    }

    private void setDefaultValues(DataRow<Cell> row) {
        int index = row.getIndex();
        long refbookRecordId = 1L;
        long number = 1L;
        Date date = new Date();
        String str = "test";

        // графа 1  (1.1)
        row.getCell("code").setValue(str, index);
        // графа 2  (1.2)
        row.getCell("name").setValue(str, index);
        // графа 3  (2)
        row.getCell("rowNum").setValue(number, index);
        // графа 4  (3)
        row.getCell("guarantor").setValue(str, index);
        // графа 5  (3.1)
        row.getCell("vnd").setValue(str, index);
        // графа 6  (3.2)
        row.getCell("level").setValue(str, index);
        // графа 7  (4.1)
        row.getCell("procuct1").setValue(str, index);
        // графа 8  (4.2)
        row.getCell("procuct2").setValue(str, index);
        // графа 9  (4.3)
        row.getCell("procuct3").setValue(str, index);
        // графа 10 (5)
        row.getCell("taxpayerName").setValue(str, index);
        // графа 11 (6)
        row.getCell("taxpayerInn").setValue(str, index);
        // графа 12 (6.1)
        row.getCell("okved").setValue(str, index);
        // графа 13 (7.1)
        row.getCell("creditRating").setValue(refbookRecordId, index);
        // графа 14 (7.2)
        row.getCell("creditClass").setValue(refbookRecordId, index);
        // графа 15 (8)
        row.getCell("beneficiaryName").setValue(str, index);
        // графа 16 (9)
        row.getCell("beneficiaryInn").setValue(str, index);
        // графа 17 (10)
        row.getCell("emitentName").setValue(str, index);
        // графа 18 (11)
        row.getCell("instructingName").setValue(str, index);
        // графа 19 (12)
        row.getCell("number").setValue(str, index);
        // графа 20 (13)
        row.getCell("issuanceDate").setValue(date, index);
        // графа 21 (14)
        row.getCell("additionDate").setValue(date, index);
        // графа 22 (15)
        row.getCell("startDate").setValue(date, index);
        // графа 23 (16)
        row.getCell("conditionEffective").setValue(str, index);
        // графа 24 (17)
        row.getCell("endDate").setValue(date, index);
        // графа 25 (18.1)
        row.getCell("sumInCurrency").setValue(number, index);
        // графа 26 (18.2)
        row.getCell("sumInRub").setValue(number, index);
        // графа 27 (19)
        row.getCell("currency").setValue(refbookRecordId, index);
        // графа 28 (20)
        row.getCell("debtBalance").setValue(number, index);
        // графа 29 (21.1)
        row.getCell("isNonRecurring").setValue(refbookRecordId, index);
        // графа 30 (21.2)
        row.getCell("paymentPeriodic").setValue(str, index);
        // графа 31 (22.1)
        row.getCell("isCharged").setValue(refbookRecordId, index);
        // графа 32 (22.2)
        row.getCell("tariff").setValue(number, index);
        // графа 33 (22.3)
        row.getCell("remuneration").setValue(number, index);
        // графа 34 (22.4)
        row.getCell("remunerationStartYear").setValue(number, index);
        // графа 35 (22.5)
        row.getCell("remunerationIssuance").setValue(number, index);
        // графа 36 (23)
        row.getCell("provide").setValue(str, index);
        // графа 37 (24)
        row.getCell("numberGuarantee").setValue(str, index);
        // графа 38 (25)
        row.getCell("numberAddition").setValue(str, index);
        // графа 39 (26)
        row.getCell("isGuaranetee").setValue(refbookRecordId, index);
        // графа 40 (26.1)
        row.getCell("dateGuaranetee").setValue(date, index);
        // графа 41 (26.2)
        row.getCell("sumGuaranetee").setValue(number, index);
        // графа 42 (26.3)
        row.getCell("term").setValue(str, index);
        // графа 43 (26.4)
        row.getCell("sumDiversion").setValue(number, index);
        // графа 44 (27.1)
        row.getCell("arrears").setValue(number, index);
        // графа 45 (27.2)
        row.getCell("arrearsDate").setValue(date, index);
        // графа 46 (28.1)
        row.getCell("arrearsGuarantee").setValue(number, index);
        // графа 47 (28.2)
        row.getCell("arrearsGuaranteeDate").setValue(date, index);
        // графа 48 (29)
        row.getCell("reserve").setValue(number, index);
        // графа 49 (30)
        row.getCell("comment").setValue(str, index);
        // графа 50 (31)
        row.getCell("segment").setValue(str, index);
    }
}