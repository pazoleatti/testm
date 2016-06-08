package com.aplana.sbrf.taxaccounting.form_template.income.output1_2.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by bkinzyabulatov on 12.12.2014.
 */
public class Output12Test extends ScriptTestBase {
    private static final int TYPE_ID = 1414;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.ADDITIONAL;

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
        return getDefaultScriptTestMockHelper(Output12Test.class);
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
        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size() + 1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        DataRow<Cell> addDataRow = null;
        for (DataRow<Cell> dataRow : testHelper.getDataRowHelper().getAll()) {
            if (dataRow.getAlias() == null) {
                addDataRow = dataRow;
                break;
            }
        }
        Assert.assertNotNull(addDataRow);
        testHelper.setCurrentDataRow(addDataRow);
        testHelper.execute(FormDataEvent.DELETE_ROW);
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        int expected = 1 +1; // в файле 1 строка и и строка итога
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        DataRow<Cell> dataRow = dataRows.get(0);
        Assert.assertEquals(1, dataRow.getCell("taCategory").getNumericValue().intValue());
        Assert.assertEquals("1", dataRow.getCell("emitent").getStringValue());
        Assert.assertEquals("", dataRow.getCell("inn").getStringValue());
        Assert.assertEquals("1", dataRow.getCell("decreeNumber").getStringValue());
        Assert.assertEquals("1", dataRow.getCell("dividendType").getStringValue());
        Assert.assertEquals("2015", new SimpleDateFormat("yyyy").format(dataRow.getCell("financialYear").getDateValue()));
        Assert.assertEquals("31", dataRow.getCell("taxPeriod").getStringValue());
        Assert.assertEquals(1, dataRow.getCell("totalDividend").getNumericValue().intValue());
        Assert.assertEquals(8, dataRow.getCell("dividendSumRaspredPeriod").getNumericValue().intValue());
        Assert.assertEquals(4, dataRow.getCell("dividendRussianTotal").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendRussianStavka0").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendRussianStavka6").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendRussianStavka9").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendRussianTaxFree").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendRussianPersonal").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendForgeinOrgAll").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendForgeinPersonalAll").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendStavka0").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendStavkaLess5").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendStavkaMore5").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendStavkaMore10").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendTaxUnknown").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendNonIncome").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendAgentAll").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendAgentWithStavka0").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendD1D2").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendSumForTaxStavka9").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("dividendSumForTaxStavka0").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("taxSum").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("taxSumFromPeriod").getNumericValue().intValue());
        Assert.assertEquals(1, dataRow.getCell("taxSumLast").getNumericValue().intValue());
    }
}
