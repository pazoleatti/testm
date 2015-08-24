package com.aplana.sbrf.taxaccounting.form_template.income.incomeWithHoldingAgent.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов).
 *
 * @author Ramil Timerbaev
 */
public class IncomeWithHoldingAgentTest extends ScriptTestBase {
    private static final int TYPE_ID = 10070;
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
        return getDefaultScriptTestMockHelper(IncomeWithHoldingAgentTest.class);
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
    public void afterCreateTest() {
        testHelper.execute(FormDataEvent.AFTER_CREATE);
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // Кроме простого выполнения события других проверок нет, т.к. для формы консолидация выполняется сервисом
        testHelper.execute(FormDataEvent.COMPOSE);
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // Проверка расчетных данных
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;

        // графа 2, 3, 7, 13, 14, 15, 21, 26, 29, 30, 32..42
        String [] strColumns = { "emitentName", "emitentInn", "decisionNumber", "addresseeName", "inn", "kpp",
                "series", "number", "withheldNumber", "postcode", "district", "city", "locality", "street",
                "house", "housing", "apartment", "surname", "name", "patronymic", "phone" };

        // графа 4, 5, 6, 10, 11, 12, 16, 17, 22, 23, 24, 27
        String [] numColumns = { "all", "rateZero", "distributionSum", "firstMonth", "lastMonth", "allSum", "type",
                "status", "rate", "dividends", "sum", "withheldSum" };

        // графа 19, 20, 31
        String [] refbookColumns = { "citizenship", "kind", "region" };

        // графа 8, 9, 18, 25, 28
        String [] dateColumns = { "decisionDate", "year", "birthday", "date", "withheldDate" };

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            String expectedString = "test" + index;
            for (String alias : strColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertEquals(msg, expectedString, row.getCell(alias).getStringValue());
            }

            BigDecimal expectedNum = roundValue(index, 0);
            for (String alias : numColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal actualNum = row.getCell(alias).getNumericValue().setScale(0, BigDecimal.ROUND_HALF_UP);
                Assert.assertEquals(msg, expectedNum, actualNum);
            }

            BigDecimal expectedRefbook = new BigDecimal(index);
            for (String alias : refbookColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertEquals(msg, expectedRefbook, row.getCell(alias).getNumericValue());
            }

            for (String alias : dateColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertNotNull(msg, row.getCell(alias).getDateValue());
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