package com.aplana.sbrf.taxaccounting.form_template.etr.etr_4_6.v2015;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Приложение 4-6. Отношение страховых взносов, уплачиваемых во внебюджетные фонды к расходам на оплату труда
 */
public class Etr46Test extends ScriptTestBase {
    private static final int TYPE_ID = 706;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final int COMP_DEP_PERIOD_ID = 1;
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
        formData.setComparativPeriodId(COMP_DEP_PERIOD_ID);
        formData.setReportPeriodId(REPORT_PERIOD_ID);
        formData.setPeriodOrder(1);
        formData.setComparativPeriodId(1);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Etr46Test.class);
    }

    @Before
    public void mockServices() {
        // макет нф
        when(testHelper.getFormDataService().getFormTemplate(anyInt(), anyInt())).thenReturn(testHelper.getFormTemplate());

        // подразделение-период
        when(testHelper.getDepartmentReportPeriodService().get(anyInt())).thenAnswer(
                new Answer<DepartmentReportPeriod>() {
                    @Override
                    public DepartmentReportPeriod answer(InvocationOnMock invocation) throws Throwable {
                        DepartmentReportPeriod result = new DepartmentReportPeriod();
                        ReportPeriod reportPeriod = new ReportPeriod();
                        reportPeriod.setId(REPORT_PERIOD_ID);
                        result.setReportPeriod(reportPeriod);
                        return result;
                    }
                });

        // периоды
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        ReportPeriod period = new ReportPeriod();
        period.setId(1);
        period.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(period);
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
        String WRONG_NON_EMPTY = "Строка %d: Графа «%s» не заполнена!";
        String ERROR_ZERO_DIVIDE = "Строка %d: Графа «%s» не может быть заполнена. Выполнение расчета невозможно, так как в результате проверки получен нулевой знаменатель (деление на ноль невозможно). Ячейка будет заполнена значением «0».";
        String WRONG_VALUE = "Строка %d: Неверное значение граф: «%s», «%s», «%s», «%s»!";
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> row = dataRows.get(0);

        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();

        int i = 0;
        for (int rowIndex = 1; rowIndex <= dataRows.size(); rowIndex++) {
            Assert.assertEquals(String.format(WRONG_NON_EMPTY, rowIndex, getColumnName(row, "comparePeriod")), entries.get(i++).getMessage());
            Assert.assertEquals(String.format(WRONG_NON_EMPTY, rowIndex, getColumnName(row, "currentPeriod")), entries.get(i++).getMessage());
            if (rowIndex < 4) {
                Assert.assertEquals(String.format(WRONG_NON_EMPTY, rowIndex, getColumnName(row, "deltaRub")), entries.get(i++).getMessage());
                Assert.assertEquals(String.format(WRONG_NON_EMPTY, rowIndex, getColumnName(row, "deltaPercent")), entries.get(i++).getMessage());
            }
        }
        for (int rowIndex = 1; rowIndex <= 3; rowIndex++) {
            Assert.assertEquals(String.format(ERROR_ZERO_DIVIDE, rowIndex, getColumnName(row, "deltaPercent")), entries.get(i++).getMessage());
        }
        for (int rowIndex = 1; rowIndex <= 3; rowIndex++) {
            Assert.assertEquals(String.format(WRONG_VALUE, rowIndex, getColumnName(row, "comparePeriod"), getColumnName(row, "currentPeriod"), getColumnName(row, "deltaRub"), getColumnName(row, "deltaPercent")), entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // Проверка после импорта
    @Test
    public void check1Test() {
        String WRONG_VALUE_IMPORT = "Строка файла %d: Строка %d: Неверное значение граф: «%s», «%s»!";
        int SHIFT = 12;
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        DataRow<Cell> row = dataRows.get(0);

        testHelper.setImportFileInputStream(getCustomInputStream("importFileCheck.xlsm"));
        testHelper.execute(FormDataEvent.IMPORT);

        testHelper.execute(FormDataEvent.CHECK);

        List<LogEntry> entries = testHelper.getLogger().getEntries();

        int i = 0;
        for (int rowIndex = 1; rowIndex <= dataRows.size(); rowIndex++) {
            if (rowIndex == 3) { // 3-я строка считается на основе 1-й и 2-й
                continue;
            }
            Assert.assertEquals(String.format(WRONG_VALUE_IMPORT, SHIFT + rowIndex, rowIndex, getColumnName(row, "comparePeriod"), getColumnName(row, "currentPeriod")), entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importExcelTest() {
        int expected = 6; // в файле 6 строк
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(2, dataRows.get(0).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(5, dataRows.get(1).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(40, dataRows.get(2).getCell("comparePeriod").getNumericValue().intValue());
        Assert.assertEquals(11.1, dataRows.get(3).getCell("comparePeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(11.2, dataRows.get(4).getCell("comparePeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(10.3, dataRows.get(5).getCell("comparePeriod").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(6, dataRows.get(0).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(5, dataRows.get(1).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(120, dataRows.get(2).getCell("currentPeriod").getNumericValue().intValue());
        Assert.assertEquals(10.1, dataRows.get(3).getCell("currentPeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(10.2, dataRows.get(4).getCell("currentPeriod").getNumericValue().doubleValue(), 0);
        Assert.assertEquals(11.3, dataRows.get(5).getCell("currentPeriod").getNumericValue().doubleValue(), 0);

        Assert.assertEquals(4, dataRows.get(0).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(1).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertEquals(80, dataRows.get(2).getCell("deltaRub").getNumericValue().intValue());
        Assert.assertNull(dataRows.get(3).getCell("deltaRub").getNumericValue());
        Assert.assertNull(dataRows.get(4).getCell("deltaRub").getNumericValue());
        Assert.assertNull(dataRows.get(5).getCell("deltaRub").getNumericValue());

        Assert.assertEquals(200, dataRows.get(0).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(0, dataRows.get(1).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertEquals(200, dataRows.get(2).getCell("deltaPercent").getNumericValue().intValue());
        Assert.assertNull(dataRows.get(3).getCell("deltaPercent").getNumericValue());
        Assert.assertNull(dataRows.get(4).getCell("deltaPercent").getNumericValue());
        Assert.assertNull(dataRows.get(5).getCell("deltaPercent").getNumericValue());
    }
}
