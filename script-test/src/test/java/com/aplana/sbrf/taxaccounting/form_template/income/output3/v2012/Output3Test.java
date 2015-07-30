package com.aplana.sbrf.taxaccounting.form_template.income.output3.v2012;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.reset;

/**
 * Сумма налога, подлежащая уплате в бюджет, по данным налогоплательщика.
 */
public class Output3Test extends ScriptTestBase {
    private static final int TYPE_ID = 308;
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
        return getDefaultScriptTestMockHelper(Output3Test.class);
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
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;
        int precision = 0;
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }
            // графа 1
            BigDecimal expectedRefbook = new BigDecimal(index);
            Assert.assertEquals("row.paymentType[" + row.getIndex() + "]", expectedRefbook, row.getCell("paymentType").getNumericValue());
            // графа 2
            Assert.assertNotNull("row.okatoCode[" + row.getIndex() + "]", row.getCell("okatoCode").getStringValue());
            // графа 3
            Assert.assertNotNull("row.budgetClassificationCode[" + row.getIndex() + "]", row.getCell("budgetClassificationCode").getStringValue());
            // графа 4
            Assert.assertNotNull("row.dateOfPayment[" + row.getIndex() + "]", row.getCell("dateOfPayment").getDateValue());
            // графа 5
            BigDecimal expectedNum = roundValue(index, precision);
            Assert.assertEquals("row.sumTax[" + row.getIndex() + "]", expectedNum, row.getCell("sumTax").getNumericValue());

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