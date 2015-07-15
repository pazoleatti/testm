package com.aplana.sbrf.taxaccounting.form_template.income.output1.v2012;

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
 * Сведения для расчёта налога с доходов в виде дивидендов (доходов от долевого участия в других организациях,
 * созданных на территории Российской Федерации).
 */
public class Output1Test extends ScriptTestBase {
    private static final int TYPE_ID = 306;
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
        return getDefaultScriptTestMockHelper(Output1Test.class);
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

        // графа 4..25
        String [] numColumns = {
                "dividendSumRaspredPeriod", "dividendForgeinOrgAll", "dividendForgeinPersonalAll",
                "dividendTotalRaspredPeriod", "dividendStavka0", "dividendStavkaLess5",
                "dividendStavkaMore5", "dividendStavkaMore10", "dividendRussianMembersAll",
                "dividendRussianMembersTotal", "dividendRussianOrgStavka9", "dividendRussianOrgStavka0",
                "dividendPersonRussia", "dividendMembersNotRussianTax", "dividendAgentAll",
                "dividendAgentWithStavka0", "dividendSumForTaxAll", "dividendSumForTaxStavka9",
                "dividendSumForTaxStavka0", "taxSum", "taxSumFromPeriod", "taxSumFromPeriodAll"
        };

        // графа 4, 7, 13, 20..4
        String [] calcColumns = { "dividendSumRaspredPeriod", "dividendTotalRaspredPeriod",
                "dividendRussianMembersTotal", "dividendSumForTaxAll", "dividendSumForTaxStavka9",
                "dividendSumForTaxStavka0", "taxSum", "taxSumFromPeriod"
        };
        List<String> calcColumnsList = Arrays.asList(calcColumns);

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            // графа 1
            Assert.assertNotNull("row.dividendType[" + row.getIndex() + "]", row.getCell("dividendType").getStringValue());

            // графа 2
            Assert.assertNotNull("row.taxPeriod[" + row.getIndex() + "]", row.getCell("taxPeriod").getStringValue());

            // графа 3
            Assert.assertNotNull("row.financialYear[" + row.getIndex() + "]", row.getCell("financialYear").getDateValue());

            // графа 4..25
            BigDecimal expectedNum = roundValue(index, precision);
            for (String alias : numColumns) {
                // пропустить рассчитываемые графы
                if (calcColumnsList.contains(alias)) {
                    continue;
                }
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal actualNum = row.getCell(alias).getNumericValue().setScale(precision, BigDecimal.ROUND_HALF_UP);
                Assert.assertEquals(msg, expectedNum, actualNum);
            }

            for (String alias : calcColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal value = row.getCell(alias).getNumericValue().setScale(precision, BigDecimal.ROUND_HALF_UP);
                Assert.assertNotNull(msg, value);
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