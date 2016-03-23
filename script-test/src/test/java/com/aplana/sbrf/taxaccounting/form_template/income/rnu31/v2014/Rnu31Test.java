package com.aplana.sbrf.taxaccounting.form_template.income.rnu31.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * (РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям.
 */
public class Rnu31Test extends ScriptTestBase {
    private static final int TYPE_ID = 328;
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
        return getDefaultScriptTestMockHelper(Rnu31Test.class);
    }

    @Before
    public void mockFormDataService() {
        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");
        department.setType(DepartmentType.TERR_BANK);

        when(testHelper.getDepartmentService().get(anyInt())).thenReturn(department);

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

    // Проверка с данными
    @Test
    public void check1Test() {
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        List<LogEntry> entries = testHelper.getLogger().getEntries();
        String msg;
        DataRow<Cell> row = dataRows.get(0);

        // для попадания в ЛП:
        // Проверка обязательных полей
        int i = 0;
        testHelper.execute(FormDataEvent.CHECK);

        String [] nonEmptyColumns = {"ofz", "municipalBonds", "governmentBonds", "mortgageBonds", "municipalBondsBefore",
                "rtgageBondsBefore", "ovgvz", "eurobondsRF", "itherEurobonds", "corporateBonds"};
        for (String column : nonEmptyColumns) {
            msg = String.format(ScriptUtils.WRONG_NON_EMPTY, row.getIndex(), row.getCell(column).getColumn().getName());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(String.format("Нарушена уникальность номера по порядку!"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // Для проверки неотрицательности
        i = 0;
        String [] nonEmptyColumns1 = {"ofz", "municipalBonds", "mortgageBonds", "municipalBondsBefore", "rtgageBondsBefore", "corporateBonds",
                "governmentBonds", "ovgvz", "eurobondsRF", "itherEurobonds"};
        for (String column : nonEmptyColumns1) {
            row.getCell(column).setValue(-1L, null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(String.format("Нарушена уникальность номера по порядку!"), entries.get(i++).getMessage());
        for (String column : nonEmptyColumns1) {
            msg = String.format("Значение графы «%s» по строке %s отрицательное!", row.getCell(column).getColumn().getName(), row.getIndex());
            Assert.assertEquals(msg, entries.get(i++).getMessage());
        }
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();

        // Для прохождения ЛП
        i = 0;
        for (String column : nonEmptyColumns1) {
            row.getCell(column).setValue(1L, null);
        }
        testHelper.execute(FormDataEvent.CHECK);
        Assert.assertEquals(String.format("Нарушена уникальность номера по порядку!"), entries.get(i++).getMessage());
        Assert.assertEquals(i, testHelper.getLogger().getEntries().size());
        testHelper.getLogger().clear();
    }


    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        Assert.assertTrue(testHelper.getLogger().containsLevel(LogLevel.ERROR));
    }

    @Test
    public void importTransportFileTest() {
        int expected = 1; // в файле 1 строка
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = 1; // в файле 1 строка
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void composeTest() {
        // даты
        Calendar calendar = Calendar.getInstance();
        when(testHelper.getReportPeriodService().getMonthStartDate(anyInt(), anyInt())).thenReturn(calendar);
        when(testHelper.getReportPeriodService().getMonthEndDate(anyInt(), anyInt())).thenReturn(calendar);

        // Назначен один тип формы
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setKind(KIND);
        departmentFormType.setDepartmentId(DEPARTMENT_ID);
        departmentFormType.setFormTypeId(TYPE_ID);
        departmentFormType.setId(1);
        when(testHelper.getDepartmentFormTypeService().getFormSources(anyInt(), anyInt(), any(FormDataKind.class),
                any(Date.class), any(Date.class))).thenReturn(Arrays.asList(departmentFormType));

        FormType formType = new FormType();
        formType.setId(TYPE_ID);
        formType.setTaxType(TaxType.INCOME);

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        sourceFormData.setFormType(formType);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class), any(Integer.class), any(Boolean.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются импортом
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        sourceDataRowHelper.save(testHelper.getDataRowHelper().getAll());
        testHelper.initRowData();
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size()); //в импортируемом файле-источнике одна строка
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        Assert.assertEquals(1, dataRows.get(0).getCell("ofz").getNumericValue().doubleValue(), 0);
    }
}