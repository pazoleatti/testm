package com.aplana.sbrf.taxaccounting.form_template.vat.vat_724_2_1.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Отчёт о суммах начисленного НДС по операциям Банка (v.2015).
 */
public class Vat_724_2_1Test extends ScriptTestBase {
    private static final int TYPE_ID = 10601;
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
        return getDefaultScriptTestMockHelper(Vat_724_2_1Test.class);
    }

    @Before
    public void mockFormDataService() {
        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");
        department.setType(DepartmentType.TERR_BANK);

        when(testHelper.getDepartmentService().get(anyInt())).thenReturn(department);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
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
        // должны быть ошибки из за пустых обязательных полей
        Assert.assertTrue("Must have empty required cells", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // должны быть ошибки из за пустых обязательных полей
        Assert.assertTrue("Must have empty required cells", testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void addDelRowTest() {
        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        int expected = testHelper.getDataRowHelper().getAll().size();
        // Не должно быть возможности добавления новой строки
        Assert.assertEquals("Addition new row should not be available!", expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        // Не должно быть возможности удаления новой строки
        Assert.assertNull("Form can't contain notfixed rows!", addDataRow);
        testHelper.setCurrentDataRow(null);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        // Количество строк не должно измениться, т.к. форма с фиксированными строками
        Assert.assertEquals("Remove row should not be available!", expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(26, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(26, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        // графа 4, 5
        String [] editColumns = { "realizeCost", "obtainCost" };
        Integer expected = 1;
        for (DataRow<Cell> row : dataRows) {
            for (String alias : editColumns) {
                Cell cell = row.getCell(alias);
                if (cell.isEditable()) {
                    Integer value = (cell.getNumericValue() != null ? cell.getNumericValue().intValue() : null);
                    Assert.assertEquals("row." + alias + "[" + row.getIndex() + "]", expected, value);
                }
            }
        }
    }
}