package com.aplana.sbrf.taxaccounting.form_template.income.app2_src_1.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с
 * финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов (ЦФО НДФЛ).
 */
public class App2Src1Test extends ScriptTestBase {
    private static final int TYPE_ID = 418;
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
        return getDefaultScriptTestMockHelper(App2Src1Test.class);
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
    public void importTransportFileTest() {
        // настройка справочников
        Long [] refbookIds = new Long [] { 4L, 10L, 350L, 360L, 370L };
        for (Long refbookId : refbookIds) {
            // провайдер для каждого справочника
            RefBookUniversal provider = mock(RefBookUniversal.class);
            provider.setRefBookId(refbookId);
            when(testHelper.getRefBookFactory().getDataProvider(refbookId)).thenReturn(provider);

            // записи для каждого справочника
            Map<Long, Map<String, RefBookValue>> records = testHelper.getRefBookAllRecords(refbookId);
            PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records.values());
            when(provider.getRecords(any(Date.class), any(PagingParams.class), anyString(),
                    any(RefBookAttribute.class))).thenReturn(result);
        }

        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll(), false);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 2; // в файле 2 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll(), true);
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // Кроме простого выполнения события других проверок нет, т.к. консолидация выполняется сервисом
        testHelper.execute(FormDataEvent.COMPOSE);
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows, boolean isImportXLS) {
        long index = 1;

        // графа 1..6, 10, 11, 13..19, 21
        String [] strColumns = { "innRF", "inn", "surname", "name", "patronymic", "status", "series", "postcode",
                "district", "city", "locality", "street", "house", "housing", "apartment", "address" };

        // графа 22..30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70
        String [] numColumns = { "taxRate", /* "income", "deduction", "taxBase", */ "calculated", "withheld", "listed",
                "withheldAgent", "nonWithheldAgent", "col_041_1", "col_043_1_1", "col_043_1_2", "col_043_1_3",
                "col_043_1_4", "col_043_1_5", "col_041_2", "col_043_2_1", "col_043_2_2", "col_043_2_3", "col_043_2_4",
                "col_043_2_5", "col_041_3", "col_043_3_1", "col_043_3_2", "col_043_3_3", "col_043_3_4", "col_043_3_5",
                "col_052_3_1", "col_052_3_2" };

        // графа 8, 9, 12, 20, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 69
        String [] refbookColumns = { "citizenship", "code", "region", "country", "col_040_1", "col_042_1_1",
                "col_042_1_2", "col_042_1_3", "col_042_1_4", "col_042_1_5", "col_040_2", "col_042_2_1", "col_042_2_2",
                "col_042_2_3", "col_042_2_4", "col_042_2_5", "col_040_3", "col_042_3_1", "col_042_3_2", "col_042_3_3",
                "col_042_3_4", "col_042_3_5", "col_051_3_1", "col_051_3_2" };

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            String expectedString = "test" + index;
            for (String alias : strColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                if ("status".equals(alias) || "postcode".equals(alias)) {
                    Assert.assertEquals(msg, String.valueOf(index), row.getCell(alias).getStringValue());
                } else {
                    Assert.assertEquals(msg, expectedString, row.getCell(alias).getStringValue());
                }
            }

            BigDecimal expectedNum = roundValue(index, 2);
            for (String alias : numColumns) {
                if (isImportXLS && ("income".equals(alias) || "deduction".equals(alias) || "taxBase".equals(alias))) {
                    continue;
                }
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal actualNum = row.getCell(alias).getNumericValue().setScale(2, BigDecimal.ROUND_HALF_UP);
                Assert.assertEquals(msg, expectedNum, actualNum);
            }

            BigDecimal expectedRefbook = new BigDecimal(index);
            for (String alias : refbookColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertEquals(msg, expectedRefbook, row.getCell(alias).getNumericValue());
            }

            // графа 7
            Assert.assertNotNull("row.birthday[" + row.getIndex() + "]", row.getCell("birthday").getDateValue());

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
