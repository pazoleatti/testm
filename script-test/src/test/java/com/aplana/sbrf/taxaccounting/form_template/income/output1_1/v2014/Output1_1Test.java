package com.aplana.sbrf.taxaccounting.form_template.income.output1_1.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (03/А).
 */
public class Output1_1Test extends ScriptTestBase {
    private static final int TYPE_ID = 411;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.SUMMARY;

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
        return getDefaultScriptTestMockHelper(Output1_1Test.class);
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

    // Консолидация
    @Test
    public void composeTest() {
        // предыдущий период
        ReportPeriod prevReportPeriod = new ReportPeriod();
        prevReportPeriod.setId(1);
        when(testHelper.getReportPeriodService().getPrevReportPeriod(anyInt())).thenReturn(prevReportPeriod);

        // даты
        Calendar calendar = Calendar.getInstance();
        when(testHelper.getReportPeriodService().getCalendarStartDate(anyInt())).thenReturn(calendar);
        when(testHelper.getReportPeriodService().getEndDate(anyInt())).thenReturn(calendar);

        // периоды
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        ReportPeriod period = new ReportPeriod();
        period.setId(1);
        period.setTaxPeriod(taxPeriod);
        when(testHelper.getReportPeriodService().get(anyInt())).thenReturn(period);
        when(testHelper.getReportPeriodService().listByTaxPeriod(anyInt())).thenReturn(Arrays.asList(period));

        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        // идентификатор шаблона источников
        int sourceTypeId = 10070;
        departmentFormType.setFormTypeId(sourceTypeId);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        // Макет источника
        FormTemplate sourceTemplate = testHelper.getTemplate("..//src/main//resources//form_template//income//incomeWithHoldingAgent//v2014//");

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(sourceTemplate);
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются в ручную
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        // формируем одну строку источника
        DataRow<Cell> row = sourceFormData.createDataRow();

        // графа 2, 3, 7, 13, 14, 15, 21, 26, 29, 30, 32..42
        String [] strColumns = { "emitentName", "emitentInn", "decisionNumber", "addresseeName", "inn", "kpp",
                "series", "number", "withheldNumber", "postcode", "district", "city", "locality", "street",
                "house", "housing", "apartment", "surname", "name", "patronymic", "phone" };
        String testStr = "test1";
        for (String alias : strColumns) {
            row.getCell(alias).setValue(testStr, null);
        }

        // графа 4, 5, 6, 10, 11, 12, 16, 17, 22, 23, 24, 27
        String [] numColumns = { "all", "rateZero", "distributionSum", "firstMonth", "lastMonth", "allSum", "type",
                "status", "rate", "dividends", "sum", "withheldSum" };
        Long testNum = 1L;
        for (String alias : numColumns) {
            row.getCell(alias).setValue(testNum, null);
        }

        // графа 19, 20, 31
        Long testRefbookId = 1L;
        String [] refbookColumns = { "citizenship", "kind", "region" };
        for (String alias : refbookColumns) {
            row.getCell(alias).setValue(testRefbookId, null);
        }

        // графа 8, 9, 18, 25, 28
        String [] dateColumns = { "decisionDate", "year", "birthday", "date", "withheldDate" };
        Date testDate = calendar.getTime();
        for (String alias : dateColumns) {
            row.getCell(alias).setValue(testDate, null);
        }

        dataRows.add(row);
        sourceDataRowHelper.save(dataRows);
        testHelper.initRowData();

        // Консолидация
        testHelper.execute(FormDataEvent.COMPOSE);
        int expected = 1; // в источнике 1 строка
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;
        int precision = 0;

        // графа 2..4
        String [] strColumns = { "taxPeriod", "emitent", "decreeNumber", "dividendType" };

        // графа 5..26
        String [] numColumns = {
                "dividendSumRaspredPeriod", "dividendSumNalogAgent", "dividendForgeinOrgAll",
                "dividendForgeinPersonalAll", "dividendStavka0", "dividendStavkaLess5",
                "dividendStavkaMore5", "dividendStavkaMore10", "dividendRussianMembersAll",
                "dividendRussianOrgStavka9", "dividendRussianOrgStavka0", "dividendPersonRussia",
                "dividendMembersNotRussianTax", "dividendAgentAll", "dividendAgentWithStavka0",
                "dividendSumForTaxAll", "dividendSumForTaxStavka9", "dividendSumForTaxStavka0", "taxSum",
                "taxSumFromPeriod", "taxSumFromPeriodAll"
        };

        String MSG = "row.%s[%d]";
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            // графа 1
            Assert.assertNotNull("row.financialYear[" + row.getIndex() + "]", row.getCell("financialYear").getDateValue());

            for (String alias : strColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                Assert.assertNotNull(msg, row.getCell(alias).getStringValue());
            }

            BigDecimal expectedNum = roundValue(index, precision);
            for (String alias : numColumns) {
                String msg = String.format(MSG, alias, row.getIndex());
                BigDecimal actualNum = row.getCell(alias).getNumericValue().setScale(precision, BigDecimal.ROUND_HALF_UP);
                Assert.assertEquals(msg, expectedNum, actualNum);
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