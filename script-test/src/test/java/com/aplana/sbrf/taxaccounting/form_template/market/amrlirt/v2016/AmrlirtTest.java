package com.aplana.sbrf.taxaccounting.form_template.market.amrlirt.v2016;

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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Данные АМРЛИРТ.
 */
public class AmrlirtTest extends ScriptTestBase {
    private static final int TYPE_ID = 902;
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
        return getDefaultScriptTestMockHelper(AmrlirtTest.class);
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
    public void chec1kTest() {
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

        // 1. Проверка заполнения обязательных полей
        i = 0;
        String [] nonEmptyColumns = { "crmId", "name", "inn", "code", "lgd" };
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

        // 2. Неотрицательность графы
        i = 0;
        setDefaultValue(row);
        row.getCell("lgd").setValue(-1L, null);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: Значение графы «%s» должно быть больше либо равно 0!",
                row.getIndex(), row.getCell("lgd").getColumn().getName());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();

        // 3. Проверка на отсутствие нескольких записей по одном и тому же кредитному договору
        i = 0;
        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        dataRows.add(row2);
        setDefaultValue(row);
        setDefaultValue(row2);
        testHelper.execute(FormDataEvent.CHECK);
        msg = String.format("Строка %s: На форме уже существует строка со значением графы «%s» = «%s»!",
                row2.getIndex(), row2.getCell("crmId").getColumn().getName(), row2.getCell("crmId").getValue());
        Assert.assertEquals(msg, entries.get(i++).getMessage());
        Assert.assertEquals(i, entries.size());
        testHelper.getLogger().clear();
        dataRows.remove(row2);
    }

    @Test
    public void importExcelTest() {
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        printLog();
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 1
        Assert.assertEquals("test11", dataRows.get(0).getCell("crmId").getStringValue());
        Assert.assertEquals("test21", dataRows.get(1).getCell("crmId").getStringValue());

        // графа 2
        Assert.assertEquals("test12", dataRows.get(0).getCell("name").getStringValue());
        Assert.assertEquals("test22", dataRows.get(1).getCell("name").getStringValue());

        // графа 3
        Assert.assertEquals("test13", dataRows.get(0).getCell("inn").getStringValue());
        Assert.assertEquals("test23", dataRows.get(1).getCell("inn").getStringValue());

        // графа 4
        Assert.assertEquals("test14", dataRows.get(0).getCell("code").getStringValue());
        Assert.assertEquals("test24", dataRows.get(1).getCell("code").getStringValue());

        // графа 5
        Assert.assertEquals(11L, dataRows.get(0).getCell("lgd").getNumericValue().longValue());
        Assert.assertEquals(22L, dataRows.get(1).getCell("lgd").getNumericValue().longValue());
    }

    private void setDefaultValue(DataRow<Cell> dataRow) {
        String testValue = "test";
        dataRow.getCell("crmId").setValue(testValue, null);
        dataRow.getCell("name").setValue(testValue, null);
        dataRow.getCell("inn").setValue(testValue, null);
        dataRow.getCell("code").setValue(testValue, null);
        dataRow.getCell("lgd").setValue(1L, null);
    }
}