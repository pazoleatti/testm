package com.aplana.sbrf.taxaccounting.form_template.transport.vehicles.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.WRONG_NON_EMPTY;
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.getColumnName;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог.
 */
public class VehiclesTest extends ScriptTestBase {
    private static final int TYPE_ID = 3201;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

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
        return getDefaultScriptTestMockHelper(VehiclesTest.class);
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

    // копирование предыдущих данных - предыдущая форма отсутствует
    // Логическая  проверка 12. Проверка наличия формы предыдущего периода в состоянии «Принята»
    @Test
    public void afterCreateNotPrevFormTest() {
        mockProvider(8L);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(null);
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        String msg = String.format("Данные по транспортным средствам из формы предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                "год", "2013", TestScriptHelper.DEPARTMENT_NAME);
        Assert.assertEquals(LogLevel.WARNING, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    // копирование предыдущих данных - предыдущая форма присутствует
    @Test
    public void afterCreateTest() throws ParseException {
        // предыдущий период
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2013);
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setOrder(4);
        prevReportPeriod.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        // данные за предыдущий период
        FormData prevFormData = new FormData();
        prevFormData.initFormTemplateParams(testHelper.getFormTemplate());
        prevFormData.setId(0L);
        prevFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getFormDataPrev(any(FormData.class))).thenReturn(prevFormData);
        // строка 1 - должна попасть в текущую форму при копировании
        DataRow<Cell> row1 = prevFormData.createDataRow();
        setDefaultValues(row1);
        // строка 2 - не должна попасть в текущую форму при копировании
        DataRow<Cell> row2 = prevFormData.createDataRow();
        setDefaultValues(row2);
        row2.getCell("regDate").setValue(format.parse("01.01.2016"), null);
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        dataRows.add(row1);
        dataRows.add(row2);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.setAllCached(dataRows);
        when(testHelper.getFormDataService().getDataRowHelper(eq(prevFormData))).thenReturn(sourceDataRowHelper);

        testHelper.execute(FormDataEvent.AFTER_CREATE);

        int expected = 1;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        List<Column> copyColumns = testHelper.getFormData().getFormColumns();
        for (Column column : copyColumns) {
            Object expectedValue = row1.getCell(column.getAlias()).getValue();
            Object currentValue = testHelper.getDataRowHelper().getAll().get(0).getCell(column.getAlias()).getValue();
            Assert.assertEquals(expectedValue, currentValue);
        }
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        mockProvider2(211L);
        mockProvider2(218L);
        int expected = 1; // в файле 1 строка
        String fileName = "importFile.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1 - счетчик
        // графа 2
        Assert.assertEquals(4L, row.getCell("codeOKATO").getNumericValue().longValue());
        // графа 3 - зависимая графа
        // графа 4
        Assert.assertEquals(8L, row.getCell("tsTypeCode").getNumericValue().longValue());
        // графа 5 - зависимая графа
        // графа 6
        String expectedStr = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        Assert.assertEquals(expectedStr, row.getCell("model").getValue());
        // графа 7
        Assert.assertEquals(1L, row.getCell("ecoClass").getNumericValue().longValue());
        // графа 8
        expectedStr = "1234567890123456789012345";
        Assert.assertEquals(expectedStr, row.getCell("identNumber").getValue());
        // графа 9
        expectedStr = "123456789012345678901234567890";
        Assert.assertEquals(expectedStr, row.getCell("regNumber").getValue());
        // графа 10
        Assert.assertNotNull(row.getCell("regDate").getValue());
        // графа 11
        Assert.assertNotNull(row.getCell("regDateEnd").getValue());
        // графа 12
        Assert.assertEquals(1L, row.getCell("month").getNumericValue().longValue());
        // графа 13
        Assert.assertEquals(123456789012.12, row.getCell("taxBase").getNumericValue().doubleValue(), 0);
        // графа 14
        Assert.assertEquals(5L, row.getCell("baseUnit").getNumericValue().longValue());
        // графа 15
        Assert.assertNotNull(row.getCell("year").getValue());
        // графа 16
        Assert.assertEquals(6L, row.getCell("pastYear").getNumericValue().longValue());
        // графа 17
        Assert.assertNotNull(row.getCell("stealDateStart").getValue());
        // графа 18
        Assert.assertNotNull(row.getCell("stealDateEnd").getValue());
        // графа 19
        expectedStr = "1234567890/1234567890";
        Assert.assertEquals(expectedStr, row.getCell("share").getValue());
        // графа 20
        Assert.assertNotNull(row.getCell("costOnPeriodBegin").getValue());
        // графа 21
        Assert.assertNotNull(row.getCell("costOnPeriodEnd").getValue());
        // графа 22
        Assert.assertEquals(5L, row.getCell("version").getNumericValue().longValue());
        // графа 23 - зависимая графа
        // графа 24
        Assert.assertEquals(4L, row.getCell("deductionCode").getNumericValue().longValue());
        // графа 25
        Assert.assertEquals(123456789012345L, row.getCell("deduction").getNumericValue().longValue());
    }

    @Test
    public void importExcelMsg1Test() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        int expected = 1; // в файле 1 строка
        String fileName = "importFileMsg1.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // checkLogger();
        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 22
        Assert.assertNull(row.getCell("version").getValue());

        // 1. Проверка заполнения средней стоимости
        int i = 0;
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. графа «%s» не заполнена",
                "9", ScriptUtils.getXLSColumnName(22), ScriptUtils.getColumnName(row, "version"),
                ScriptUtils.getColumnName(row, "averageCost"));
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    @Test
    public void importExcelMsg2Test() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        mockProvider2(211L);
        int expected = 1; // в файле 1 строка
        String fileName = "importFileMsg2.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // checkLogger();
        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 22
        Assert.assertNull(row.getCell("version").getValue());

        // 2. Проверка корректности заполнения средней стоимости
        int i = 0;
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», " +
                "т.к. в справочнике «Категории средней стоимости транспортных средств» " +
                "не найдена категория «%s»",
                "9", ScriptUtils.getXLSColumnName(22), ScriptUtils.getColumnName(row, "version"), "fakeValue");
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    @Test
    public void importExcelMsg3Test() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        mockProvider2(211L);
        int expected = 1; // в файле 1 строка
        String fileName = "importFileMsg3.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // checkLogger();
        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 22
        Assert.assertNull(row.getCell("version").getValue());

        // 3. Проверка наличия информации о модели в справочнике «Средняя стоимость транспортных средств (с 2015)»
        int i = 0;
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                "«Средняя стоимость транспортных средств (с 2015)» не найдена запись " +
                "со значением поля «Модель(версия)» равным «%s» и значением поля «Средняя стоимость» равным «%s»",
                "9", ScriptUtils.getXLSColumnName(22), ScriptUtils.getColumnName(row, "version"), "fakeValue", "nameA211");
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    @Test
    public void importExcelMsg4Test() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        mockProvider2(211L);
        mockProvider2(218L);
        int expected = 1; // в файле 1 строка
        String fileName = "importFileMsg4.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // checkLogger();
        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 22
        Assert.assertNull(row.getCell("version").getValue());

        // 4. Проверка возможности однозначного выбора информации о модели в справочнике «Средняя стоимость транспортных средств (с 2015)»
        int i = 0;
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                "«Средняя стоимость транспортных средств (с 2015)» найдено несколько записей " +
                "со значением поля «Модель(версия)» равным «%s» и значением поля «Средняя стоимость» равным «%s»",
                "9", ScriptUtils.getXLSColumnName(22), ScriptUtils.getColumnName(row, "version"), "manyValue", "nameA211");
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    @Test
    public void check1Test() throws ParseException {
        mockProvider2(209L);
        mockProvider2(211L);
        mockProvider2(218L);

        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = testHelper.getFormData().createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        String msg;
        int i;

        // все нормально
        setDefaultValues(row);
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
        testHelper.getLogger().clear();

        // 1. Проверка заполнения обязательных граф
        String[] nonEmptyColumns = { "codeOKATO", /* "regionName", */ "tsTypeCode", /* "tsType", */ "model", "identNumber",
                "regNumber", "regDate", "month", "taxBase", "baseUnit", "year", "pastYear", "share" };
        for (String alias : nonEmptyColumns) {
            row.getCell(alias).setValue(null, row.getIndex());
        }
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        for (String alias : nonEmptyColumns) {
            String columnName = getColumnName(row, alias);
            msg = String.format(WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 2.1 Проверка на наличие в форме строк с одинаковым значением граф 2, 4, 8, 9, 13, 14
        // 15. Проверка на наличие в форме строк с одинаковым значением граф 8, 9 и пересекающимися периодами владения
        setDefaultValues(row);
        DataRow<Cell> row2 = testHelper.getFormData().createDataRow();
        row2.setIndex(2);
        setDefaultValues(row2);
        dataRows.add(row2);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строки %s: Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», " +
                "Регистрационный знак «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, " +
                "регистрационным знаком ТС, налоговой базой, единицей измерения налоговой базы по ОКЕИ и пересекающимися периодами владения ТС",
                "1, 2", "7190000", "50000", "test", "test", "1.00", "A");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строки %s: На форме не должно быть строк с одинаковым значением графы «%s» («%s») и пересекающимися периодами владения ТС",
                "1, 2", "Идентификационный номер ТС", "test");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строки %s: На форме не должно быть строк с одинаковым значением графы «%s» («%s») и пересекающимися периодами владения ТС",
                "1, 2", "Регистрационный знак ТС", "test");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);

        // 2.2 Проверка на наличие в форме строк с одинаковым значением граф 2, 4, 8, 12, 13, 14
        setDefaultValues(row);
        row.getCell("regDateEnd").setValue(format.parse("31.03.2014"), row.getIndex());
        row.getCell("month").setValue(3, row.getIndex());
        setDefaultValues(row2);
        row2.getCell("regDate").setValue(format.parse("01.04.2014"), row.getIndex());
        row2.getCell("month").setValue(9, row.getIndex());
        dataRows.add(row2);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строки 1, 2: На форме присутствуют несколько строк по одному ТС. Проверьте периоды регистрации ТС");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);

        // 3. Проверка корректности заполнения даты регистрации ТС
        setDefaultValues(row);
        row.getCell("regDate").setValue(format.parse("01.01.2015"), row.getIndex());
        row.getCell("month").setValue(0, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                row.getIndex(), ScriptUtils.getColumnName(row, "regDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка корректности заполнения даты снятия с регистрации ТС
        setDefaultValues(row);
        row.getCell("regDateEnd").setValue(format.parse("01.01.2013"), row.getIndex());
        row.getCell("month").setValue(0, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "regDateEnd"), "01.01.2014", ScriptUtils.getColumnName(row, "regDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5. Проверка корректности заполнения года выпуска ТС
        setDefaultValues(row);
        row.getCell("year").setValue(format.parse("01.01.2017"), row.getIndex());
        row.getCell("pastYear").setValue(-2, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "year"), "2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 6. Проверка корректности заполнения расчетных граф 12, 16
        setDefaultValues(row);
        row.getCell("month").setValue(0, row.getIndex());
        row.getCell("pastYear").setValue(1, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        String [] calcColumns = { "month", "pastYear" };
        List<String> columnNames = new ArrayList<String>(calcColumns.length);
        for (String alias : calcColumns) {
            columnNames.add(row.getCell(alias).getColumn().getName());
        }
        String subMsg = StringUtils.join(columnNames.toArray(), "», «", null);
        msg = String.format("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", row.getIndex(), subMsg);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 7. Проверка заполнения даты начала розыска ТС при указании даты возврата ТС
        setDefaultValues(row);
        row.getCell("stealDateEnd").setValue(format.parse("01.01.2014"), row.getIndex());
        row.getCell("stealDateStart").setValue(null, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена, если заполнена графа «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "stealDateStart"), ScriptUtils.getColumnName(row, "stealDateEnd"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 8. Проверка корректности заполнения даты начала розыска ТС
        setDefaultValues(row);
        row.getCell("regDate").setValue(format.parse("01.02.2014"), row.getIndex());
        row.getCell("month").setValue(0, row.getIndex());
        row.getCell("stealDateStart").setValue(format.parse("01.01.2014"), row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Графа «%s» должна быть больше либо равна значению графы «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "stealDateStart"), ScriptUtils.getColumnName(row, "regDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();


        // 9. Проверка корректности заполнения даты возврата ТС
        setDefaultValues(row);
        row.getCell("stealDateStart").setValue(format.parse("01.02.2014"), row.getIndex());
        row.getCell("stealDateEnd").setValue(format.parse("01.01.2014"), row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s» и больше либо равно «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "stealDateEnd"),
                ScriptUtils.getColumnName(row, "stealDateStart"), "01.01.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 10. Проверка заполнения даты возврата ТС
        setDefaultValues(row);
        row.getCell("regDateEnd").setValue(format.parse("01.01.2014"), row.getIndex());
        row.getCell("month").setValue(0, row.getIndex());
        row.getCell("stealDateStart").setValue(format.parse("01.02.2014"), row.getIndex());
        row.getCell("stealDateEnd").setValue(format.parse("01.03.2014"), row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть заполнено и должно быть меньше либо равно значению графы «%s» и больше либо равно «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "stealDateEnd"),
                ScriptUtils.getColumnName(row, "regDateEnd"), "01.01.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11. Проверка корректности заполнения доли налогоплательщика в праве на ТС
        // 11.1 не цифры
        i = 0;
        setDefaultValues(row);
        row.getCell("share").setValue("a/b", null);
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                "«(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», " +
                "числитель должен быть меньше либо равен знаменателю, " +
                "числитель и знаменатель не должны быть равны нулю",
                row.getIndex(), ScriptUtils.getColumnName(row, "share"));
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11.2 начинаются с нулей
        i = 0;
        setDefaultValues(row);
        row.getCell("share").setValue("01/01", null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11.3 числитель больше знаменателя
        i = 0;
        setDefaultValues(row);
        row.getCell("share").setValue("2/1", null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 11.4 нет знака дроби
        i = 0;
        setDefaultValues(row);
        row.getCell("share").setValue("ab", null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 12. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется в методе copyFromPrevForm() после создания формы

        // 13. Проверка единицы измерения для наземных видов ТС
        setDefaultValues(row);
        row.getCell("tsTypeCode").setValue(6, row.getIndex());
        row.getCell("baseUnit").setValue(4, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s», указанное в киловаттах должно быть переведено в лошадиные силы и значение графы «%s» должно быть равно «251»",
                row.getIndex(), ScriptUtils.getColumnName(row, "taxBase"), ScriptUtils.getColumnName(row, "baseUnit"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 16. Проверка одновременного заполнения данных о налоговом вычете
        setDefaultValues(row);
        row.getCell("deductionCode").setValue(1L, row.getIndex());
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Данные о налоговом вычете указаны не полностью", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    @Test
    public void calc1Test() throws ParseException {
        mockProvider2(209L);
        mockProvider2(218L);

        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> row = testHelper.getFormData().createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        setDefaultValues(row);
        row.getCell("month").setValue(null, row.getIndex());
        row.getCell("pastYear").setValue(null, row.getIndex());
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertEquals(12, row.getCell("month").getNumericValue().longValue());
        Assert.assertEquals(5, row.getCell("pastYear").getNumericValue().longValue());
        checkLogger();
    }

    private void setDefaultValues(DataRow<Cell> row) throws ParseException {
        Date date = format.parse("01.01.2014");
        Long refbookRecordId = 1L;
        Long number = 1L;
        String str = "test";

        // графа 1
        row.getCell("rowNumber").setValue(row.getIndex(), null);
        // графа 2
        row.getCell("codeOKATO").setValue(refbookRecordId, null);
        // графа 3 - зависимая графа
        // графа 4
        row.getCell("tsTypeCode").setValue(refbookRecordId, null);
        // графа 5 - зависимая графа
        // графа 6
        row.getCell("model").setValue(str, null);
        // графа 7
        row.getCell("ecoClass").setValue(refbookRecordId, null);
        // графа 8
        row.getCell("identNumber").setValue(str, null);
        // графа 9
        row.getCell("regNumber").setValue(str, null);
        // графа 10
        row.getCell("regDate").setValue(date, null);
        // графа 11
        row.getCell("regDateEnd").setValue(null, null);
        // графа 12
        row.getCell("month").setValue(12, null);
        // графа 13
        row.getCell("taxBase").setValue(number, null);
        // графа 14
        row.getCell("baseUnit").setValue(refbookRecordId, null);
        // графа 15
        row.getCell("year").setValue(format.parse("01.01.2010"), null);
        // графа 16
        row.getCell("pastYear").setValue(5, null);
        // графа 17
        row.getCell("stealDateStart").setValue(null, null);
        // графа 18
        row.getCell("stealDateEnd").setValue(null, null);
        // графа 19
        row.getCell("share").setValue("1/1", null);
        // графа 20
        row.getCell("costOnPeriodBegin").setValue(null, null);
        // графа 21
        row.getCell("costOnPeriodEnd").setValue(null, null);
        // графа 22
        row.getCell("version").setValue(refbookRecordId, null);
        // графа 23
        row.getCell("averageCost").setValue(null, null);
        // графа 24
        row.getCell("deductionCode").setValue(null, null);
        // графа 25
        row.getCell("deduction").setValue(null, null);
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

    private void mockProvider2(Long refBookId) {
        // провайдер для справочника
        RefBookUniversal provider = mock(RefBookUniversal.class);
        provider.setRefBookId(refBookId);
        when(testHelper.getFormDataService().getRefBookProvider(any(RefBookFactory.class), eq(refBookId),
                anyMapOf(Long.class, RefBookDataProvider.class))).thenReturn(provider);

        // вернуть все записи справочника
        Map<Long, Map<String, RefBookValue>> refBookAllRecords = testHelper.getRefBookAllRecords(refBookId);
        ArrayList<Long> recordIds = new ArrayList<Long>(refBookAllRecords.keySet());
        when(provider.getUniqueRecordIds(any(Date.class), isNull(String.class))).thenReturn(recordIds);
        when(provider.getRecordData(eq(recordIds))).thenReturn(refBookAllRecords);
    }
}