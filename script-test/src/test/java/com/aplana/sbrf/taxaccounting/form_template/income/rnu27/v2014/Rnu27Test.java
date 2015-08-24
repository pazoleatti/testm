package com.aplana.sbrf.taxaccounting.form_template.income.rnu27.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * РНУ-27.
 *
 * @author Ramil Timerbaev
 */
public class Rnu27Test extends ScriptTestBase {
    private static final int TYPE_ID = 326;
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
        return getDefaultScriptTestMockHelper(Rnu27Test.class);
    }

    @Before
    public void mockFormDataService() {
        when(testHelper.getDepartmentReportPeriodService().get(any(Integer.class))).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        result.setBalance(true);
                        return result;
                    }
                });
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
    public void importExcelTest() {
        int expected = 2 + 2 * 2 + 1; // в файле 2 строки + 2 подитога на каждую строку + 1 итоговая строка
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        // Проверка расчетных данных
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        int expected = 2 + 1; // в файле 2 строки + 1 итоговая
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;
        int precision = 6;

        // графа 2..4
        String [] strColumns = { "issuer", "regNumber", "tradeNumber" };

        // графа 6..9
        String [] numColumns = { "prev", "current", "reserveCalcValuePrev", "cost", "marketQuotation", "rubCourse",
                "marketQuotationInRub", "costOnMarketQuotation", "reserveCalcValue", "reserveCreation", "recovery" };

        // графа 5, 10
        String [] refbookColumns = { "currency", "signSecurity" };

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            String expectedString = "test" + index;
            for (String alias : strColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                if ("regNumber".equals(alias) && index > 1) {
                    Assert.assertEquals(msg, "", row.getCell(alias).getStringValue());
                } else {
                    Assert.assertEquals(msg, expectedString, row.getCell(alias).getStringValue());
                }
            }

            BigDecimal expectedNum = roundValue(index, precision);
            for (String alias : numColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal actualNum = row.getCell(alias).getNumericValue().setScale(precision, BigDecimal.ROUND_HALF_UP);
                Assert.assertEquals(msg, expectedNum, actualNum);
            }

            BigDecimal expectedRefbook = new BigDecimal(index);
            for (String alias : refbookColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertEquals(msg, expectedRefbook, row.getCell(alias).getNumericValue());
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