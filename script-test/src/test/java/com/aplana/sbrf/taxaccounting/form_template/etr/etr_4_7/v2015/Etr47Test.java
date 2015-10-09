package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_7.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-7. Отношение налога на имущество к остаточной стоимости
 */
public class Etr47Test extends ScriptTestBase {
    private static final int TYPE_ID = 702;
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
        formData.setPeriodOrder(1);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Etr47Test.class);
    }

    @Before
    public void mockServices() {
        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());
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
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = 4; // в файле 4 строки
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(new BigDecimal("1000.00"), dataRows.get(0).getCell("comparePeriod").getNumericValue());
        Assert.assertEquals(new BigDecimal("10.00"), dataRows.get(1).getCell("comparePeriod").getNumericValue());
        Assert.assertEquals(new BigDecimal("10000.00"), dataRows.get(2).getCell("comparePeriod").getNumericValue());
        Assert.assertEquals(new BigDecimal("2.20"), dataRows.get(3).getCell("comparePeriod").getNumericValue());

        Assert.assertEquals(new BigDecimal("-999.99"), dataRows.get(0).getCell("currentPeriod").getNumericValue());
        Assert.assertEquals(new BigDecimal("12.00"), dataRows.get(1).getCell("currentPeriod").getNumericValue());
        Assert.assertEquals(new BigDecimal("-8333.25"), dataRows.get(2).getCell("currentPeriod").getNumericValue());
        Assert.assertEquals(new BigDecimal("2.20"), dataRows.get(3).getCell("currentPeriod").getNumericValue());

        Assert.assertEquals(new BigDecimal("-1999.99"), dataRows.get(0).getCell("deltaRub").getNumericValue());
        Assert.assertEquals(new BigDecimal("2.00"), dataRows.get(1).getCell("deltaRub").getNumericValue());
        Assert.assertEquals(new BigDecimal("-18333.25"), dataRows.get(2).getCell("deltaRub").getNumericValue());
        Assert.assertNull(dataRows.get(3).getCell("deltaRub").getNumericValue());

        Assert.assertEquals(new BigDecimal("-200.00"), dataRows.get(0).getCell("deltaPercent").getNumericValue());
        Assert.assertEquals(new BigDecimal("20.00"), dataRows.get(1).getCell("deltaPercent").getNumericValue());
        Assert.assertEquals(new BigDecimal("-183.33"), dataRows.get(2).getCell("deltaPercent").getNumericValue());
        Assert.assertNull(dataRows.get(3).getCell("deltaPercent").getNumericValue());
    }
}
