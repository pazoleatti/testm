package com.aplana.sbrf.taxaccounting.form_template.deal.rnu_111.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * РНУ 111. Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению
 * межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон процентных ставок, не соответствующих рыночному уровню
 */
public class Rnu_111Test extends ScriptTestBase {
    private static final int TYPE_ID = 808;
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
        return getDefaultScriptTestMockHelper(Rnu_111Test.class);
    }

    @Before
    public void mockServices() {
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

    // Проверка с данными
    @Test
    public void check1Test() throws ParseException {
        // TODO добавить тесты для ЛП
    }

    // Расчет пустой (в импорте - растчет заполненной)
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        List<String> aliases = Arrays.asList("reasonNumber", "reasonDate", "base", "sum", "time", "rate", "sum1",
                "rate1", "sum2", "rate2", "sum3");
        defaultCheckLoadData(aliases, 3);
        checkLogger();
        checkLoadData(testHelper.getDataRowHelper().getAll());

        // проверка расчетов
        testHelper.execute(FormDataEvent.CALCULATE);
        checkAfterCalc(testHelper.getDataRowHelper().getAll());
        Assert.assertEquals(5, testHelper.getDataRowHelper().getCount());
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1L, dataRows.get(0).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("name").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(0).getCell("code").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("code").getNumericValue().longValue());
        Assert.assertEquals(1L, dataRows.get(0).getCell("currency").getNumericValue().longValue());
        Assert.assertEquals(2L, dataRows.get(1).getCell("currency").getNumericValue().longValue());
    }

    // Проверить расчеты
    void checkAfterCalc(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(365, dataRows.get(0).getCell("base").getNumericValue().intValue());
        Assert.assertNull(dataRows.get(1).getCell("base").getNumericValue());
        Assert.assertEquals(365, dataRows.get(2).getCell("base").getNumericValue().intValue());
        Assert.assertNull(dataRows.get(3).getCell("base").getNumericValue());
        Assert.assertNull(dataRows.get(4).getCell("base").getNumericValue());

        Assert.assertEquals(2, dataRows.get(0).getCell("rate2").getNumericValue().doubleValue(), 0);
        Assert.assertNull(dataRows.get(1).getCell("rate2").getNumericValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("rate2").getNumericValue().doubleValue(), 0);
        Assert.assertNull(dataRows.get(3).getCell("rate2").getNumericValue());
        Assert.assertNull(dataRows.get(4).getCell("rate2").getNumericValue());

        Assert.assertEquals(2, dataRows.get(0).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(2, dataRows.get(1).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(2, dataRows.get(2).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(2, dataRows.get(3).getCell("sum3").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(4, dataRows.get(4).getCell("sum3").getNumericValue().doubleValue(), 0);
    }
}

