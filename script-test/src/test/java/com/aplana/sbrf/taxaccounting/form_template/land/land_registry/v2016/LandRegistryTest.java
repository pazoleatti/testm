package com.aplana.sbrf.taxaccounting.form_template.land.land_registry.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Реестр земельных участков
 * TODO импорт, расчет, проверка 12-й графы
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
        String [] nonEmptyColumns = {"oktmo", "cadastralNumber", "landCategory", "cadastralCost", "ownershipDate"};
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

        // 2. Проверка одновременного заполнения данных о налоговой льготе
        i = 0;
        setDefaultValue(row);
        row.getCell("benefitCode").setValue(1L, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Данные о налоговой льготе указаны не полностью", row.getIndex());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        testHelper.getLogger().clear();

        // 3. Проверка корректности заполнения даты возникновения права собственности
        i = 0;
        setDefaultValue(row);
        row.getCell("ownershipDate").setValue(sdf.parse("01.01.2015"), null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", row.getIndex(), ScriptUtils.getColumnName(row, "ownershipDate"), "31.12.2014");
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        testHelper.getLogger().clear();

        // 4. Проверка корректности заполнения даты прекращения права собственности
        i = 0;
        setDefaultValue(row);
        row.getCell("terminationDate").setValue(sdf.parse("31.12.2013"), null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно %s и больше либо равно значению графы «%s»", row.getIndex(), ScriptUtils.getColumnName(row, "terminationDate"), "01.01.2014", ScriptUtils.getColumnName(row, "ownershipDate"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        testHelper.getLogger().clear();

        // 5. Проверка формата значения доли налогоплательщика в праве на земельный участок
        i = 0;
        setDefaultValue(row);
        row.getCell("taxPart").setValue("0/0", null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», " +
                "без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю", row.getIndex(), ScriptUtils.getColumnName(row, "taxPart"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        testHelper.getLogger().clear();

        // 6. Проверка корректности заполнения срока использования льготы (графа 13)
        i = 0;
        setDefaultValue(row);
        row.getCell("benefitCode").setValue(1L, null);
        row.getCell("benefitParam").setValue("test", null);
        row.getCell("benefitPeriod").setValue(6L, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение в графе «%s» для форм в периодах 1 кв., 2 кв., 3 кв. должно быть больше либо равно 0 " +
                "и меньше либо равно 3, для форм за период «Год» должно быть больше либо равно 0 и меньше либо равно 12", row.getIndex(), ScriptUtils.getColumnName(row, "benefitPeriod"));
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        testHelper.getLogger().clear();
    }

    private void setDefaultValue(DataRow<Cell> dataRow) throws ParseException {
        String testValue = "test";
        dataRow.getCell("oktmo").setValue(1L, null);
        dataRow.getCell("cadastralNumber").setValue(testValue, null);
        dataRow.getCell("landCategory").setValue(1L, null);
        dataRow.getCell("constructionPhase").setValue(null, null);
        dataRow.getCell("cadastralCost").setValue(1.00, null);
        dataRow.getCell("taxPart").setValue(null, null);
        dataRow.getCell("ownershipDate").setValue(sdf.parse("01.01.2014"), null);
        dataRow.getCell("terminationDate").setValue(null, null);
        dataRow.getCell("benefitCode").setValue(null, null);
        dataRow.getCell("benefitParam").setValue(null, null);
        dataRow.getCell("benefitPeriod").setValue(null, null);
    }
}
