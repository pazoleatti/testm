package com.aplana.sbrf.taxaccounting.form_template.income.reserve.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * (РСД) Расчет резерва по сомнительным долгам в целях налогообложения.
 */
public class ReserveTest extends ScriptTestBase {
    private static final int TYPE_ID = 614;
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
        return getDefaultScriptTestMockHelper(ReserveTest.class);
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
        // должны быть ошибки
        Assert.assertFalse(testHelper.getLogger().getEntries().isEmpty());
        testHelper.reset();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // должны быть ошибки
        Assert.assertFalse(testHelper.getLogger().getEntries().isEmpty());
        testHelper.reset();
    }

    @Test
    public void addDelRowTest() {
        // Добавлеие и удаление строк в данной форме не предусмотрено
        int expected = testHelper.getDataRowHelper().getAll().size();

        // Добавление
        testHelper.execute(FormDataEvent.ADD_ROW);
        // Кол-во строк не должно измениться
        Assert.assertEquals("Add new row", expected, testHelper.getDataRowHelper().getAll().size());

        // Удаление
        testHelper.setCurrentDataRow(testHelper.getDataRowHelper().getAll().get(44));
        testHelper.execute(FormDataEvent.DELETE_ROW);
        // Кол-во строк не должно измениться
        Assert.assertEquals("Delete row", expected, testHelper.getDataRowHelper().getAll().size());

        // ошибок быть не должно
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 45; // все строки фиксированные
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /**
     * Проверить загруженные данные.
     */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        List<String> aliases = Arrays.asList("sum45", "norm45", "reserve45", "sum90", "norm90", "reserve90",
                "totalReserve", "sumIncome", "normIncome", "valueReserve", "reservePrev", "reserveCurrent",
                "addChargeReserve", "restoreReserve", "usingReserve");
        int rowCount = 45;
        for (int i = 0; i < rowCount; i++) {
            if (i == 1 || i == 18 || i == 23) {
                continue;
            }
            int j = 0;
            for (String alias : aliases) {
                j++;
                BigDecimal numericValue = dataRows.get(i).getCell(alias).getNumericValue();
                Assert.assertNotNull("Строка файла " + (i + 13) + ", графа «" + alias + "» незаполнена", numericValue);
                Assert.assertEquals("Строка файла " + (i + 13) + ", графа «" + alias + "»", (i + 1) * j, numericValue.doubleValue(), 0);
            }
        }
        Assert.assertEquals(rowCount, testHelper.getDataRowHelper().getCount());
    }
}