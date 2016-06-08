package com.aplana.sbrf.taxaccounting.form_template.income.output2_2.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Сведения о дивидендах (доходах от долевого участия в других организациях,
 * созданных на территории Российской Федерации), выплаченных в отчетном квартале.
 */
public class Output2_2Test extends ScriptTestBase {
    private static final int TYPE_ID = 416;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

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
        return getDefaultScriptTestMockHelper(Output2_2Test.class);
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

    // Проверка со входом во все ЛП
    @Test
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getTemplate("..//src/main//resources//form_template//income//output2_2//v2014//"));
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // для попадания в ЛП:
        // Проверка заполнения ячеек
        // Проверка допустимых значений «Графы 6» (диапазон 00...99)
        // Проверка одновременного заполнения/не заполнения «Графы 17» и «Графы 18»
        DataRow<Cell> row1 = formData.createDataRow();
        row1.setIndex(1);
        row1.getCell("name").setValue("name", null);
        dataRows.add(row1);

        // для попадания в ЛП:
        // Проверка допустимых значений «Графы 6» (диапазон 00...99)
        // Проверки паттернов
        // Проверка формата дат
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        row2.getCell("emitent").setValue("emitent", null);
        row2.getCell("decreeNumber").setValue("1", null);
        row2.getCell("inn").setValue("123", null);
        row2.getCell("kpp").setValue("123", null);
        row2.getCell("recType").setValue("1ы", null);
        row2.getCell("title").setValue("title", null);
        row2.getCell("subdivisionRF").setValue(1, null);
        row2.getCell("dividendDate").setValue(sdf.parse("01.01.2100"), null);
        row2.getCell("sumDividend").setValue(0, null);
        row2.getCell("sumTax").setValue(0, null);
        dataRows.add(row2);

        // успешное прохождение всех ЛП
        DataRow<Cell> row3 = formData.createDataRow();
        row3.setIndex(3);
        row3.getCell("emitent").setValue("emitent", null);
        row3.getCell("decreeNumber").setValue("1", null);
        row3.getCell("inn").setValue("7702232171", null);
        row3.getCell("kpp").setValue("770201001", null);
        row3.getCell("recType").setValue("11", null);
        row3.getCell("title").setValue("title", null);
        row3.getCell("subdivisionRF").setValue(1, null);
        row3.getCell("dividendDate").setValue(sdf.parse("31.12.2099"), null);
        row3.getCell("sumDividend").setValue(0, null);
        row3.getCell("sumTax").setValue(0, null);
        dataRows.add(row3);

        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;

        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Эмитент"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Номер решения о выплате дивидендов"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Получатель. ИНН"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Получатель. КПП"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Получатель. Тип."), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Получатель. Наименование"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Место нахождения (адрес). Код региона"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата перечисления дивидендов"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма дивидендов в рублях"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма налога в рублях"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графы «Фамилия» и «Имя» должны быть заполнены одновременно (либо обе графы не должны заполняться)!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Графа «Получатель. Тип» заполнена неверно!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Графа «Получатель. Тип» заполнена неверно!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Атрибут \"Получатель. ИНН\" заполнен неверно (123)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\"", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Расшифровка паттерна \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}\": Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 8 символов: (0-9)", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Атрибут \"Получатель. КПП\" заполнен неверно (123)! Ожидаемый паттерн: \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\"", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Расшифровка паттерна \"([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})\": Первые 2 символа: (0-9; 1-9 / 1-9; 0-9). Следующие 2 символа: (0-9). Следующие 2 символа: (0-9 / A-Z). Последние 3 символа: (0-9)", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 2: Значение даты атрибута «Дата перечисления дивидендов» должно принимать значение из следующего диапазона: 01.01.1991 - 31.12.2099", entries.get(i++).getMessage());

        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
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
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 2 + 1; // в файле 2 строки + строка итога
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        long refbookId = 4L;
        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refbookId);
        when(testHelper.getRefBookFactory().getDataProvider(refbookId)).thenReturn(provider);

        // записи для справочника
        Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(refbookId);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records.values());
        when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                any(RefBookAttribute.class))).thenReturn(result);

        // предыдущий период
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setId(1);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        // даты
        Calendar calendar = Calendar.getInstance();
        when(testHelper.getReportPeriodService().getCalendarStartDate(anyInt())).thenReturn(calendar);
        when(testHelper.getReportPeriodService().getEndDate(anyInt())).thenReturn(calendar);

        // периоды
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        ReportPeriod period = new ReportPeriod();
        period.setId(1);
        period.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(period);
        when(testHelper.getReportPeriodService().listByTaxPeriod(anyInt())).thenReturn(Arrays.asList(period));

        when(testHelper.getDepartmentReportPeriodService().get(anyInt())).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        ReportPeriod reportPeriod = new ReportPeriod();
                        reportPeriod.setId(REPORT_PERIOD_ID);
                        result.setReportPeriod(reportPeriod);
                        return result;
                    }
                });

        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        // идентификатор шаблона источников
        int sourceTypeId = 10070;
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//income//incomeWithHoldingAgent//v2014//");

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются в ручную
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        // формируем одну строку источника
        DataRow<Cell> row = sourceFormData.createDataRow();

        // графа 2, 3, 7, 13, 14, 15, 21, 26, 29, 30, 32..42
        String [] strColumns = { "emitentName", "emitentInn", "decisionNumber", "addresseeName", "inn", "kpp",
                "series", "number", "withheldNumber", "postcode", "district", "city", "locality", "street",
                "house", "housing", "apartment", "surname", "name", "patronymic", "phone" };
        String testStr = "test1";
        for (String alias : strColumns) {
            row.getCell(alias).setValue(testStr, null);
        }

        // графа 4, 5, 6, 10, 11, 12, 16, 17, 22, 23, 24, 27
        String [] numColumns = { "all", "rateZero", "distributionSum", "firstMonth", "lastMonth", "allSum", "type",
                "status", "rate", "dividends", "sum", "withheldSum" };
        Long testNum = 1L;
        for (String alias : numColumns) {
            // что б строка источника попала в приемник есть условия
            row.getCell(alias).setValue("rate".equals(alias) ? 0L : testNum, null);
        }

        // графа 19, 20, 31
        Long testRefbookId = 1L;
        String [] refbookColumns = { "citizenship", "kind", "region" };
        for (String alias : refbookColumns) {
            row.getCell(alias).setValue(testRefbookId, null);
        }

        // графа 8, 9, 18, 25, 28
        String [] dateColumns = { "decisionDate", "year", "birthday", "date", "withheldDate" };
        Date testDate = calendar.getTime();
        for (String alias : dateColumns) {
            row.getCell(alias).setValue(testDate, null);
        }

        dataRows.add(row);
        sourceDataRowHelper.save(dataRows);
        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        int expected = 1 + 1; // в источнике 1 строка + строка итога
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;
        int precision = 0;

        // графа 2..8, 10..20
        String [] strColumns = {
                "emitent", "decreeNumber", "inn", "kpp", "recType", "title", "zipCode", "area", "city", "region",
                "street", "homeNumber", "corpNumber", "apartment", "surname", "name", "patronymic", "phone"
        };

        // графа 4..6
        String [] strNotNullColumns = { "inn", "kpp", "recType" };
        List<String> strNotNullColumnsList = Arrays.asList(strNotNullColumns);

        // графа 22, 23
        String [] numColumns = { "sumDividend", "sumTax" };

        // графа 9
        String [] refbookColumns = { "subdivisionRF" };

        // графа 21
        String [] dateColumns = { "dividendDate" };

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            String expectedString = "test" + index;
            for (String alias : strColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                // некоторые графы имеют определенный формат значения, их проверять только на заполнение
                if (strNotNullColumnsList.contains(alias)) {
                    Assert.assertNotNull(msg, row.getCell(alias).getStringValue());
                } else {
                    Assert.assertEquals(msg, expectedString, row.getCell(alias).getStringValue());
                }
            }

            BigDecimal expectedNum = roundValue(index, precision);
            for (String alias : numColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal actualNum = row.getCell(alias).getNumericValue().setScale(precision, BigDecimal.ROUND_HALF_UP);
                Assert.assertEquals(msg, expectedNum, actualNum);
            }

            BigDecimal expectedRefbook = new BigDecimal(index);
            for (String alias : refbookColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertEquals(msg, expectedRefbook, row.getCell(alias).getNumericValue());
            }

            for (String alias : dateColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertNotNull(msg, row.getCell(alias).getDateValue());
            }

            index++;
        }
    }

    // Округляет число до требуемой точности
    BigDecimal roundValue(Long value, int precision) {
        if (value != null) {
            return (BigDecimal.valueOf(value)).setScale(precision, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }
}