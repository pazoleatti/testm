package com.aplana.sbrf.taxaccounting.form_template.transport.benefit_vehicles.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.WRONG_NON_EMPTY;
import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.getColumnName;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог
 *
 * @author SYasinskiy
 */
public class BenefitVehiclesTest extends ScriptTestBase {
    private static final int TYPE_ID = 2202;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    private static final int formTypeId201 = 201;
    private static final String formType201Path = "..//src/main//resources//form_template//transport//vehicles//v2016//";

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formType.setId(TYPE_ID);
        formType.setName("Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог");
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
        return getDefaultScriptTestMockHelper(BenefitVehiclesTest.class);
    }

    @Test
    public void create() {
        testHelper.execute(FormDataEvent.CREATE);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
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

    // TODO (Ramil Timerbaev) отключил из за костыля, потому что надо передавать dataSource в скрипты
    // копирование данных - предыдущая форма отсутствует и сведения о ТС тоже
    // Логическая  проверка 7. Проверка наличия формы предыдущего периода в состоянии «Принята»
    // Логическая  проверка 8. Проверка наличия формы «Сведения о транспортных средствах, по которым уплачивается налог»
    // @Test
    public void afterCreateNotCopyFormTest() {
        mockProvider(8L);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(null);
        // убрать получение данных сведении о ТС
        when(testHelper.getFormDataService().getLast(eq(formTypeId201), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(null);

        testHelper.execute(FormDataEvent.AFTER_CREATE);
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        String msg = String.format("Данные по транспортным средствам из формы «%s» предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                testHelper.getFormData().getFormType().getName(), "год", "2013", TestScriptHelper.DEPARTMENT_NAME);
        Assert.assertEquals(LogLevel.WARNING, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Данные по транспортным средствам из формы «Сведения о ТС, по которым уплачивается транспортный налог» не были скопированы. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                "test period name", "2014", TestScriptHelper.DEPARTMENT_NAME);
        Assert.assertEquals(LogLevel.WARNING, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    // TODO (Ramil Timerbaev) отключил из за костыля, потому что надо передавать dataSource в скрипты
    // копирование предыдущих данных - предыдущая форма присутствует
    // @Test
    public void afterCreateCopyPrevTest() throws ParseException {
        // убрать получение данных сведении о ТС
        when(testHelper.getFormDataService().getLast(eq(formTypeId201), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(null);

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
        row2.getCell("benefitStartDate").setValue(format.parse("01.01.2016"), null);
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

    // TODO (Ramil Timerbaev)
    // копирование данных 201 - форма "сведения о ТС" присутствует
    // @Test
    public void afterCreateCopy201Test() throws ParseException {
        mockProvider(8L);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(null);

        DataRow<Cell> row = testHelper.getFormData().createDataRow();
        row.setIndex(1);
        setDefaultValues(row);

        // сведения о ТС
        FormTemplate template201 = testHelper.getTemplate(formType201Path);
        FormData formData201 = new FormData();
        formData201.initFormTemplateParams(template201);
        formData201.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(formTypeId201), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(formData201);
        // строки и хелпер
        DataRow<Cell> row201 = formData201.createDataRow();
        List<DataRow<Cell>> dataRows201 = new ArrayList<DataRow<Cell>>();
        setDefaultValues201(row201, row);
        dataRows201.add(row201);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.save(dataRows201);
        when(testHelper.getFormDataService().getDataRowHelper(formData201)).thenReturn(sourceDataRowHelper);

        testHelper.execute(FormDataEvent.AFTER_CREATE);

        // Логическая  проверка 7. Проверка наличия формы предыдущего периода в состоянии «Принята»
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        int i = 0;
        String msg = String.format("Данные по транспортным средствам из формы «%s» предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                template201.getName(), "год", "2013", TestScriptHelper.DEPARTMENT_NAME);
        Assert.assertEquals(LogLevel.WARNING, entries.get(i).getLevel());
        Assert.assertEquals(msg, entries.get(i++).getMessage());

        // проверка значении
        int expected = 1;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // графа 2, 3, 5..8
        String [] copyColumns = { "codeOKATO", "tsTypeCode", "identNumber", "regNumber", "powerVal", "baseUnit" };
        for (String alias : copyColumns) {
            Object expectedValue = row.getCell(alias).getValue();
            Object currentValue = testHelper.getDataRowHelper().getAll().get(0).getCell(alias).getValue();
            Assert.assertEquals(expectedValue, currentValue);
        }
    }

    @Test
    public void importExcelTest() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        mockProvider2(4L);
        mockProvider2(6L);
        mockProvider2(7L);
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
        Assert.assertEquals(1L, row.getCell("codeOKATO").getNumericValue().longValue());
        // графа 3
        Assert.assertEquals(8L, row.getCell("tsTypeCode").getNumericValue().longValue());
        // графа 4  - зависимая графа
        // графа 5
        String expectedStr = "1234567890123456789012345";
        Assert.assertEquals(expectedStr, row.getCell("identNumber").getValue());
        // графа 6
        expectedStr = "123456789012345678901234567890";
        Assert.assertEquals(expectedStr, row.getCell("regNumber").getValue());
        // графа 7
        Assert.assertEquals(123456789012.12, row.getCell("powerVal").getNumericValue().doubleValue(), 0);
        // графа 8
        Assert.assertEquals(1L, row.getCell("baseUnit").getNumericValue().longValue());
        // графа 9
        Assert.assertEquals(4L, row.getCell("taxBenefitCode").getNumericValue().longValue());
        // графа 10 - зависимая графа
        // графа 11
        Assert.assertNotNull(row.getCell("benefitStartDate").getValue());
        // графа 12
        Assert.assertNotNull(row.getCell("benefitEndDate").getValue());
    }

    @Test
    public void importExcelMsg2Test() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        int expected = 1; // в файле 1 строка
        String fileName = "importFileMsg2.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 9
        Assert.assertNull(row.getCell("taxBenefitCode").getValue());

        // 2. Проверка заполнения кода ОКТМО
        int i = 0;
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнена графа «%s»",
                "9", ScriptUtils.getXLSColumnName(9), ScriptUtils.getColumnName(row, "taxBenefitCode"),
                ScriptUtils.getColumnName(row, "codeOKATO"));
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    @Test
    public void importExcelMsg3Test() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());

        int expected = 1; // в файле 1 строка
        String fileName = "importFileMsg3.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 9
        Assert.assertNull(row.getCell("taxBenefitCode").getValue());

        // 3. Проверка наличия информации о налоговой льготе в справочнике «Параметры налоговых льгот транспортного налога»
        int i = 0;
        String msg = String.format("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                "«Параметры налоговых льгот транспортного налога» не найдена соответствующая запись",
                "9", ScriptUtils.getXLSColumnName(9), ScriptUtils.getColumnName(row, "taxBenefitCode"));
        Assert.assertEquals(msg, testHelper.getLogger().getEntries().get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // TODO (Ramil Timerbaev)
    // @Test
    public void check1Test() throws ParseException {
        mockProvider2(4L);
        mockProvider2(6L);
        mockProvider2(7L);

        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();

        DataRow<Cell> row = testHelper.getFormData().createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        String msg;
        int i;

        // сведения о ТС
        FormTemplate template201 = testHelper.getTemplate(formType201Path);
        FormData formData201 = new FormData();
        formData201.initFormTemplateParams(template201);
        formData201.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(formTypeId201), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(formData201);
        // строки и хелпер
        DataRow<Cell> row201 = formData201.createDataRow();
        List<DataRow<Cell>> dataRows201 = new ArrayList<DataRow<Cell>>();
        dataRows201.add(row201);
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        sourceDataRowHelper.save(dataRows201);
        when(testHelper.getFormDataService().getDataRowHelper(formData201)).thenReturn(sourceDataRowHelper);

        // все нормально
        setDefaultValues(row);
        setDefaultValues201(row201, row);
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
        testHelper.getLogger().clear();

        // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
        // TODO (Ramil Timerbaev) пока невозможно изменить параметр formDataDepartment.regionId

        // 2. Проверка заполнения обязательных граф
        // графа 2, 3, 5..9, 11
        String [] nonEmptyColumns = { "codeOKATO", "tsTypeCode", "identNumber", "regNumber", "powerVal", "baseUnit", "taxBenefitCode", "benefitStartDate" };
        for (String alias : nonEmptyColumns) {
            row.getCell(alias).setValue(null, row.getIndex());
        }
        setDefaultValues201(row201, row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        for (String alias : nonEmptyColumns) {
            String columnName = getColumnName(row, alias);
            msg = String.format(WRONG_NON_EMPTY, row.getIndex(), columnName);
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка корректности заполнения даты начала использования льготы
        setDefaultValues(row);
        row.getCell("benefitStartDate").setValue(format.parse("01.01.2015"), row.getIndex());
        setDefaultValues201(row201, row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                row.getIndex(), ScriptUtils.getColumnName(row, "benefitStartDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка корректности заполнения даты окончания использования льготы
        setDefaultValues(row);
        row.getCell("benefitStartDate").setValue(format.parse("01.02.2014"), row.getIndex());
        row.getCell("benefitEndDate").setValue(format.parse("01.01.2014"), row.getIndex());
        setDefaultValues201(row201, row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "benefitEndDate"), ScriptUtils.getColumnName(row, "benefitStartDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5. Проверка на наличие в форме строк с одинаковым значением граф 2, 3, 5, 6, 7, 8 и пересекающимися периодами использования льготы
        setDefaultValues(row);
        setDefaultValues201(row201, row);
        DataRow<Cell> row2 = testHelper.getFormData().createDataRow();
        row2.setIndex(2);
        setDefaultValues(row2);
        dataRows.add(row2);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строки %s: Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», " +
                "Регистрационный знак «%s», Величина мощности «%s», Единица измерения мощности «%s»: " +
                "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, " +
                "идентификационным номером ТС, регистрационным знаком ТС, величиной мощности и " +
                "единицей измерения мощности и пересекающимися периодами использования льготы",
                "1, 2", "codeA96", "codeA42", "test", "test", "1.00", "codeA12");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);

        // 6. Проверка количества льгот для одного ТС на форме
        setDefaultValues(row);
        row.getCell("benefitStartDate").setValue(format.parse("01.01.2014"), row.getIndex());
        row.getCell("benefitEndDate").setValue(format.parse("31.01.2014"), row.getIndex());
        setDefaultValues201(row201, row);
        setDefaultValues(row2);
        row2.getCell("taxBenefitCode").setValue(5L, row.getIndex());
        row2.getCell("benefitStartDate").setValue(format.parse("01.02.2014"), row.getIndex());
        row2.getCell("benefitEndDate").setValue(format.parse("31.03.2014"), row.getIndex());
        dataRows.add(row2);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строки 1, 2: Для ТС не может быть указано более одного вида льготы");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);

        // 7. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется в методе copyFromPrevForm() при создании формы

        // 8. Проверка наличия формы «Сведения о транспортных средствах, по которым уплачивается налог»
        // Выполняется в методе copyFrom201() при создании формы

        // 9. Проверка корректности заполнения кода налоговой льготы
        setDefaultValues(row);
        row.getCell("taxBenefitCode").setValue(3L, row.getIndex());
        setDefaultValues201(row201, row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: Значение графы «%s» (%s) должно относиться к региону, " +
                        "в котором действует выбранная в графе «%s» льгота («%s»)",
                row.getIndex(), ScriptUtils.getColumnName(row, "codeOKATO"), "codeA96",
                ScriptUtils.getColumnName(row, "taxBenefitCode"), "03");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 10.a Проверка наличия сведений о ТС в форме «Сведения о транспортных средствах, по которым уплачивается транспортный налог»
        // убрать получение данных сведении о ТС
        when(testHelper.getFormDataService().getLast(eq(formTypeId201), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(null);
        setDefaultValues(row);
        setDefaultValues201(row201, row);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("В Системе отсутствует форма «Сведения о транспортных средствах, по которым уплачивается налог» " +
                "в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                "test period name", "2014", TestScriptHelper.DEPARTMENT_NAME);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        // вернуть получение данных сведении о ТС
        when(testHelper.getFormDataService().getLast(eq(formTypeId201), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(formData201);

        // 10.b Проверка наличия сведений о ТС в форме «Сведения о транспортных средствах, по которым уплачивается транспортный налог»
        setDefaultValues(row);
        setDefaultValues201(row201, row);
        row.getCell("powerVal").setValue(2L, null);
        testHelper.execute(FormDataEvent.CHECK);
        i = 0;
        msg = String.format("Строка %s: На форме «Сведения о транспортных средствах, по которым уплачивается налог» " +
                "отсутствуют сведения о ТС с кодом ОКТМО «%s», кодом вида ТС «%s», идентификационным номером «%s», " +
                "регистрационным знаком «%s», величиной мощности «%s» и единицей измерения мощности «%s»",
                row.getIndex(), "codeA96", "codeA42", "test", "test", "2.00", "codeA12");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    private void setDefaultValues(DataRow<Cell> row) throws ParseException {
        Date date = format.parse("01.01.2014");
        Long refbookRecordId = 1L;
        Long number = 1L;
        String str = "test";

        // графа 1 - счетчик
        // графа 2
        row.getCell("codeOKATO").setValue(refbookRecordId, null);
        // графа 3
        row.getCell("tsTypeCode").setValue(8L, null);
        // графа 4 - зависимая графа
        // графа 5
        row.getCell("identNumber").setValue(str, null);
        // графа 6
        row.getCell("regNumber").setValue(str, null);
        // графа 7
        row.getCell("powerVal").setValue(number, null);
        // графа 8
        row.getCell("baseUnit").setValue(refbookRecordId, null);
        // графа 9
        row.getCell("taxBenefitCode").setValue(4L, null);
        // графа 10 - зависимая графа
        // графа 11
        row.getCell("benefitStartDate").setValue(date, null);
        // графа 12
        row.getCell("benefitEndDate").setValue(null, null);
    }

    private void setDefaultValues201(DataRow<Cell> row201, DataRow<Cell> row) {
        // графа 2 = графа 2
        row201.getCell("codeOKATO").setValue(row.getCell("codeOKATO").getValue(), null);
        // графа 3 = графа 4
        row201.getCell("tsTypeCode").setValue(row.getCell("tsTypeCode").getValue(), null);
        // графа 5 = графа 8
        row201.getCell("identNumber").setValue(row.getCell("identNumber").getValue(), null);
        // графа 6 = графа 9
        row201.getCell("regNumber").setValue(row.getCell("regNumber").getValue(), null);
        // графа 7 = графа 13
        row201.getCell("taxBase").setValue(row.getCell("powerVal").getValue(), null);
        // графа 8 = графа 14
        row201.getCell("baseUnit").setValue(row.getCell("baseUnit").getValue(), null);
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
