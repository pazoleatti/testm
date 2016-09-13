package com.aplana.sbrf.taxaccounting.form_template.market.market_5_2a.v2016;

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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * 5.2(а) Отчет о выданных Банком инструментах торгового финансирования
 */
public class Market_5_2aTest extends ScriptTestBase {
    private static final int TYPE_ID = 911;
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
        return getDefaultScriptTestMockHelper(Market_5_2aTest.class);
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
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Наименование банка-эмитента"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "SWIFT"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Кредитный рейтинг"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Референс инструмента"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата выдачи"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Дата окончания действия"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Срок обязательства (дней)"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Валюта обязательства"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Сумма обязательства, тыс. ед. валюты"), entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.WRONG_NON_EMPTY, 1, "Плата, % годовых"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 2. Неотрицательность графы
        // 3. Проверка даты кредитного договора
        // 5. Проверка даты выдачи кредита
        // 6. Проверка даты погашения кредита
        // 7. Проверка даты погашения кредита 2
        row.getCell("nameBank").setValue("string1", null);
        row.getCell("country").setValue(1L, null);
        row.getCell("swift").setValue("string2", null);
        row.getCell("creditRating").setValue(1L, null);
        row.getCell("tool").setValue("string3", null);
        row.getCell("issueDate").setValue(sdf.parse("04.01.2990"), null);
        row.getCell("expireDate").setValue(sdf.parse("03.01.2990"), null);
        row.getCell("period").setValue(-1L, null);
        row.getCell("currency").setValue(1L, null);
        row.getCell("sum").setValue(-1L, null);
        row.getCell("payRate").setValue(1, null);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: Значение графы «Срок обязательства (дней)» должно быть больше либо равно 0!", entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Сумма обязательства, тыс. ед. валюты» должно быть больше 0!", entries.get(i++).getMessage());
        Assert.assertEquals(String.format(ScriptUtils.CHECK_DATE_PERIOD, 1, "Дата выдачи","01.01.2014", "31.12.2014"), entries.get(i++).getMessage());
        Assert.assertEquals("Строка 1: Значение графы «Дата окончания действия» должно быть больше либо равно значению графы «Дата выдачи»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для попадания в ЛП:
        // 4. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
        row.getCell("period").setValue(1L, null);
        row.getCell("sum").setValue(1L, null);
        row.getCell("issueDate").setValue(sdf.parse("02.01.2014"), null);
        row.getCell("expireDate").setValue(sdf.parse("03.01.2014"), null);
        dataRows.add(row);

        testHelper.execute(FormDataEvent.CHECK);

        entries = testHelper.getLogger().getEntries();
        i = 0;
        Assert.assertEquals("Строка 1: На форме уже существует строка со значениями граф «SWIFT» = «string2», «Референс инструмента» = «string3», «Дата выдачи» = «02.01.2014»!", entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // для успешного прохождения всех ЛП:
        dataRows.remove(1);
        testHelper.execute(FormDataEvent.CHECK);
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

        int expected = 1; // в файле 1 строка
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        printLog();
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        compareRow(dataRows.get(0), "Просто Банк", 1L, "SWIFT", 1L, "1", "01.01.2016", "01.07.2016", 182.00, 1L, 1000.00, 1.00);
    }

    void compareRow(DataRow<Cell> row, Object... args) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        int skipColumnCount = 1;
        List<Column> columns = testHelper.getFormTemplate().getColumns();
        for (int i = 0; i < (columns.size() - skipColumnCount); i++) {
            int columnCount = i + skipColumnCount;
            Column column = columns.get(columnCount);
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
}