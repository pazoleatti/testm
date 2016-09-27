package com.aplana.sbrf.taxaccounting.form_template.land.land_registry.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Реестр земельных участков.
 */
public class LandRegistryTest extends ScriptTestBase {
    private static final int TYPE_ID = 912;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;
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
        return getDefaultScriptTestMockHelper(LandRegistryTest.class);
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
    public void check1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;
        int i;
        Date date;

        // строка
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // нет ошибок
        i = 0;
        setDefaultValue(row);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 1. Проверка обязательности заполнения граф
        i = 0;
        // Проверяемые на пустые значения атрибуты (графа 3, 4, 5, 7, 9, 14)
        String [] nonEmptyColumns = { "oktmo", "cadastralNumber", "landCategory", "cadastralCost", "ownershipDate", "startDate" };
        for (String alias : nonEmptyColumns) {
            row.getCell(alias).setValue(null, null);
        }
        row.getCell("benefitCode").setValue(null, null);
        testHelper.execute(FormDataEvent.CHECK);
        for (String alias : nonEmptyColumns) {
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), row.getCell(alias).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 2. Проверка одновременного заполнения данных о налоговой льготе
        i = 0;
        setDefaultValue(row);
        row.getCell("benefitCode").setValue(null, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Данные о налоговой льготе указаны не полностью", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка корректности заполнения даты возникновения права собственности
        i = 0;
        setDefaultValue(row);
        date = sdf.parse("01.01.2015");
        row.getCell("ownershipDate").setValue(date, null);
        row.getCell("terminationDate").setValue(date, null);
        row.getCell("startDate").setValue(date, null);
        row.getCell("endDate").setValue(null, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", row.getIndex(), ScriptUtils.getColumnName(row, "ownershipDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 4. Проверка корректности заполнения даты прекращения права собственности
        i = 0;
        setDefaultValue(row);
        date = sdf.parse("31.12.2013");
        row.getCell("ownershipDate").setValue(date, null);
        row.getCell("terminationDate").setValue(date, null);
        row.getCell("startDate").setValue(date, null);
        row.getCell("endDate").setValue(null, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "terminationDate"), "01.01.2014", ScriptUtils.getColumnName(row, "ownershipDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5 Проверка доли налогоплательщика в праве на земельный участок
        // 5.1 не цифры
        i = 0;
        setDefaultValue(row);
        row.getCell("taxPart").setValue("a/b", null);
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», " +
                "без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю", row.getIndex(), ScriptUtils.getColumnName(row, "taxPart"));
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5.2 начинаются с нулей
        i = 0;
        setDefaultValue(row);
        row.getCell("taxPart").setValue("01/01", null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5.3 числитель больше знаменателя
        i = 0;
        setDefaultValue(row);
        row.getCell("taxPart").setValue("2/1", null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 5.4 нет знака дроби
        i = 0;
        setDefaultValue(row);
        row.getCell("taxPart").setValue("ab", null);
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 6. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
        i = 0;
        setDefaultValue(row);
        row.getCell("taxPart").setValue("0/0", null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», " +
                "без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю", row.getIndex(), ScriptUtils.getColumnName(row, "taxPart"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        msg = String.format("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю", row.getIndex(), ScriptUtils.getColumnName(row, "taxPart"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 7. Проверка корректности заполнения даты начала действия льготы
        i = 0;
        setDefaultValue(row);
        row.getCell("startDate").setValue(sdf.parse("31.12.2013"), null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "startDate"), ScriptUtils.getColumnName(row, "ownershipDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 8. Проверка корректности заполнения даты окончания действия льготы
        i = 0;
        setDefaultValue(row);
        row.getCell("endDate").setValue(sdf.parse("31.12.2013"), null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s» и быть меньше либо равно значению графы «%s»",
                row.getIndex(), ScriptUtils.getColumnName(row, "endDate"), ScriptUtils.getColumnName(row, "startDate"),
                ScriptUtils.getColumnName(row, "terminationDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
    }

    // Расчет пустой
    @Test
    public void calc1Test() throws ParseException {
        FormData formData = getFormData();
        formData.initFormTemplateParams(testHelper.getFormTemplate());
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        long expected;

        // строка
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        dataRows.add(row);

        // нормальный расчет
        setDefaultValue(row);
        row.getCell("terminationDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("endDate").setValue(sdf.parse("01.01.2015"), null);
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
        expected = 12L;
        Assert.assertEquals(expected, row.getCell("benefitPeriod").getNumericValue().longValue());
        testHelper.getLogger().clear();

        // расчет не выполняется, результат 0
        setDefaultValue(row);
        row.getCell("terminationDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("startDate").setValue(sdf.parse("01.01.2015"), null);
        row.getCell("endDate").setValue(sdf.parse("01.01.2015"), null);
        testHelper.execute(FormDataEvent.CALCULATE);
        // ошибок быть не должно
        checkLogger();
        expected = 0L;
        Assert.assertEquals(expected, row.getCell("benefitPeriod").getNumericValue().longValue());
        testHelper.getLogger().clear();
    }

    @Test
    public void importXlsxTest() {
        int expected = 1; // в файле 1 строка
        String fileName = "importFile.xlsx";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // проверка значении
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals(null, row.getCell("name").getValue());
        // графа 3
        Assert.assertEquals(null, row.getCell("oktmo").getValue());
        // графа 4
        Assert.assertEquals("1", row.getCell("cadastralNumber").getStringValue());
        // графа 5
        Assert.assertEquals(null, row.getCell("landCategory").getValue());
        // графа 6
        Assert.assertEquals(null, row.getCell("constructionPhase").getValue());
        // графа 7
        Assert.assertEquals(2L, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 8
        Assert.assertEquals(null, row.getCell("taxPart").getValue());
        // графа 9
        Assert.assertEquals(null, row.getCell("ownershipDate").getValue());
        // графа 10
        Assert.assertEquals(null, row.getCell("terminationDate").getValue());
        // графа 11
        Assert.assertEquals(null, row.getCell("benefitCode").getValue());
        // графа 12
        Assert.assertEquals(null, row.getCell("benefitBase").getValue());
        // графа 13
        Assert.assertEquals(null, row.getCell("benefitParam").getValue());
        // графа 14
        Assert.assertEquals(null, row.getCell("startDate").getValue());
        // графа 15
        Assert.assertEquals(null, row.getCell("endDate").getValue());
        // графа 16
        Assert.assertEquals(null, row.getCell("benefitPeriod").getValue());
    }

    @Test
    public void importXlsmTest() {
        mockProvider(705L);
        int expected = 1; // в файле 1 строка
        String fileName = "importFile.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals("test1", row.getCell("name").getValue());
        // графа 3
        Assert.assertEquals(refbookRecordId, row.getCell("oktmo").getValue());
        // графа 4
        Assert.assertEquals("тест1", row.getCell("cadastralNumber").getStringValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 6
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 7
        Assert.assertEquals(123456789012345L, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 8
        Assert.assertEquals("1234567890/1234567890", row.getCell("taxPart").getValue());
        // графа 9
        Assert.assertNotNull(row.getCell("ownershipDate").getValue());
        // графа 10
        Assert.assertNotNull(row.getCell("terminationDate").getValue());
        // графа 11
        Assert.assertEquals(refbookRecordId, row.getCell("benefitCode").getValue());
        // графа 12 - зависимая графа
        // графа 13 - зависимая графа
        // графа 14
        Assert.assertNotNull(row.getCell("startDate").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("endDate").getValue());
        // графа 16
        Assert.assertEquals(3L, row.getCell("benefitPeriod").getNumericValue().longValue());
    }

    @Test
    public void importXlsmColumn3Test() {
        mockProvider(705L);
        int expected = 1; // в файле 1 строка
        String fileName = "importFile2.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals("test1", row.getCell("name").getValue());
        // графа 3
        Assert.assertNull(row.getCell("oktmo").getValue());
        // графа 4
        Assert.assertEquals("тест1", row.getCell("cadastralNumber").getStringValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 6
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 7
        Assert.assertEquals(123456789012345L, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 8
        Assert.assertEquals("1234567890/1234567890", row.getCell("taxPart").getValue());
        // графа 9
        Assert.assertNotNull(row.getCell("ownershipDate").getValue());
        // графа 10
        Assert.assertNotNull(row.getCell("terminationDate").getValue());
        // графа 11
        Assert.assertNull(row.getCell("benefitCode").getValue());
        // графа 12 - зависимая графа
        // графа 13 - зависимая графа
        // графа 14
        Assert.assertNotNull(row.getCell("startDate").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("endDate").getValue());
        // графа 16
        Assert.assertEquals(3L, row.getCell("benefitPeriod").getNumericValue().longValue());

        // проверка сообщении
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: На форме невозможно заполнить графу «%s», так как не заполнена графа «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(3), ScriptUtils.getColumnName(row, "benefitCode"), ScriptUtils.getColumnName(row, "oktmo"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    @Test
    public void importXlsmColumn11Test() {
        mockProvider(705L);
        int expected = 1; // в файле 1 строка
        String fileName = "importFile3.xlsm";
        testHelper.setImportFileName(fileName);
        testHelper.setImportFileInputStream(getCustomInputStream(fileName));
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();

        // проверка значении
        long refbookRecordId = 1L;
        DataRow<Cell> row = testHelper.getDataRowHelper().getAll().get(0);
        // графа 1
        Assert.assertEquals(null, row.getCell("rowNumber").getValue());
        // графа 2
        Assert.assertEquals("test1", row.getCell("name").getValue());
        // графа 3
        Assert.assertEquals(refbookRecordId, row.getCell("oktmo").getValue());
        // графа 4
        Assert.assertEquals("тест1", row.getCell("cadastralNumber").getStringValue());
        // графа 5
        Assert.assertEquals(refbookRecordId, row.getCell("landCategory").getValue());
        // графа 6
        Assert.assertEquals(refbookRecordId, row.getCell("constructionPhase").getValue());
        // графа 7
        Assert.assertEquals(123456789012345L, row.getCell("cadastralCost").getNumericValue().longValue());
        // графа 8
        Assert.assertEquals("1234567890/1234567890", row.getCell("taxPart").getValue());
        // графа 9
        Assert.assertNotNull(row.getCell("ownershipDate").getValue());
        // графа 10
        Assert.assertNotNull(row.getCell("terminationDate").getValue());
        // графа 11
        Assert.assertNull(row.getCell("benefitCode").getValue());
        // графа 12 - зависимая графа
        // графа 13 - зависимая графа
        // графа 14
        Assert.assertNotNull(row.getCell("startDate").getValue());
        // графа 15
        Assert.assertNotNull(row.getCell("endDate").getValue());
        // графа 16
        Assert.assertEquals(3L, row.getCell("benefitPeriod").getNumericValue().longValue());

        // проверка сообщении
        int i = 0;
        int fileRowIndex = 9;
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg = String.format("Строка %s, столбец %s: На форме невозможно заполнить графы: «%s», «%s», «%s», " +
                "так как в справочнике «Параметры налоговых льгот земельного налога» не найдена запись, " +
                "актуальная на дату «%s», в которой поле «Код субъекта РФ представителя декларации» = «%s», " +
                "поле «Код» = «%s», поле «Код ОКТМО» = «%s», поле «Параметры льготы» = «%s»",
                fileRowIndex, ScriptUtils.getXLSColumnName(11), ScriptUtils.getColumnName(row, "benefitCode"),
                ScriptUtils.getColumnName(row, "benefitBase"), ScriptUtils.getColumnName(row, "benefitParam"),
                "31.12.2014", "codeA4", "codeA704", "codeA96", "reductionA705test");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    // после создания - отсутствует предыдущий период, данные не копируются
    @Test
    public void afterCreateFailTest() {
        // провайдер для периодов
        mockProvider(8L);

        // текущий период
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2014);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setOrder(4);
        reportPeriod.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(reportPeriod);

        // предыдущий период
        TaxPeriod taxPeriodPrev = new TaxPeriod();
        taxPeriodPrev.setYear(2014);
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setOrder(2);
        prevReportPeriod.setTaxPeriod(taxPeriodPrev);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        int i = 0;
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        int expected = 0;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // проверка сообщении
        String msg = String.format("Данные по земельным участкам из предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма за период: %s %s для подразделения «%s»",
                "3 квартал", "2014", "null");
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
    }

    // после создания - данные копируются нормально
    @Test
    public void afterCreateOkTest() throws ParseException {
        // провайдер для периодов
        mockProvider(8L);

        // текущий период
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2014);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setOrder(1);
        reportPeriod.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(reportPeriod);

        // предыдущий период
        TaxPeriod taxPeriodPrev = new TaxPeriod();
        taxPeriodPrev.setYear(2013);
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setOrder(4);
        prevReportPeriod.setName("4 квартал");
        prevReportPeriod.setTaxPeriod(taxPeriodPrev);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        // данные формы предыдущего периода
        FormData prevFormData = new FormData();
        prevFormData.initFormTemplateParams(testHelper.getFormTemplate());
        prevFormData.setState(WorkflowState.ACCEPTED);
        DataRow<Cell> prevRow = prevFormData.createDataRow();
        prevRow.setIndex(1);
        setDefaultValue(prevRow);
        List<DataRow<Cell>> prevDataRows = new ArrayList<DataRow<Cell>>();
        prevDataRows.add(prevRow);
        DataRowHelper prevDataRowHelper = new DataRowHelperStub();
        prevDataRowHelper.save(prevDataRows);
        when(testHelper.getFormDataService().getFormDataPrev(any(FormData.class))).thenReturn(prevFormData);
        when(testHelper.getFormDataService().getDataRowHelper(prevFormData)).thenReturn(prevDataRowHelper);

        // все нормально
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        int expected = 1;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        String [] columns = { "name", "oktmo", "cadastralNumber", "landCategory", "constructionPhase", "cadastralCost",
                "taxPart", "ownershipDate", "terminationDate", "benefitCode", "benefitBase", "benefitParam", "startDate", "endDate" };
        for (String column : columns) {
            Object expectedValue = prevRow.getCell(column).getValue();
            Object value = testHelper.getDataRowHelper().getAll().get(0).getCell(column).getValue();
            Assert.assertEquals(expectedValue, value);
        }
        testHelper.getLogger().clear();

        // строки не скопируются потому что не подходятпо условиям
        setDefaultValue(prevRow);
        prevRow.getCell("ownershipDate").setValue(sdf.parse("01.01.2015"), prevRow.getIndex());
        prevRow.getCell("terminationDate").setValue(null, prevRow.getIndex());
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        expected = 0;
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    private void setDefaultValue(DataRow<Cell> dataRow) throws ParseException {
        int index = dataRow.getIndex();
        long refbookRecordId = 1L;
        long number = 1L;
        Date date = sdf.parse("01.01.2014");
        String str = "test";

        // графа 1
        dataRow.getCell("rowNumber").setValue(number, index);
        // графа 2
        dataRow.getCell("name").setValue(str, index);
        // графа 3
        dataRow.getCell("oktmo").setValue(refbookRecordId, index);
        // графа 4
        dataRow.getCell("cadastralNumber").setValue(str, index);
        // графа 5
        dataRow.getCell("landCategory").setValue(refbookRecordId, index);
        // графа 6
        dataRow.getCell("constructionPhase").setValue(refbookRecordId, index);
        // графа 7
        dataRow.getCell("cadastralCost").setValue(number, index);
        // графа 8
        dataRow.getCell("taxPart").setValue("1/2", index);
        // графа 9
        dataRow.getCell("ownershipDate").setValue(date, index);
        // графа 10
        dataRow.getCell("terminationDate").setValue(date, index);
        // графа 11
        dataRow.getCell("benefitCode").setValue(refbookRecordId, index);
        // графа 12
        dataRow.getCell("benefitBase").setValue(null, index);
        // графа 13
        dataRow.getCell("benefitParam").setValue(null, index);
        // графа 14
        dataRow.getCell("startDate").setValue(date, index);
        // графа 15
        dataRow.getCell("endDate").setValue(date, index);
        // графа 16
        dataRow.getCell("benefitPeriod").setValue(number, index);
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