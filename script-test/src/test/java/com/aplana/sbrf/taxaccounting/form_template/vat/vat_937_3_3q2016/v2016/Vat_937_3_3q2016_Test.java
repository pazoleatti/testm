package com.aplana.sbrf.taxaccounting.form_template.vat.vat_937_3_3q2016.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Итоговые данные из журнала полученных и выставленных счетов-фактур по посреднической деятельности  (с 3 квартала 2016).
 */

public class Vat_937_3_3q2016_Test extends ScriptTestBase {
    private static final int TYPE_ID = 859;
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
        return getDefaultScriptTestMockHelper(Vat_937_3_3q2016_Test.class);
    }

    @Before
    public void mockFormDataService() {
        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");
        department.setType(DepartmentType.TERR_BANK);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setCorrectionDate(null);

        when(testHelper.getDepartmentReportPeriodService().get(anyInt())).thenReturn(departmentReportPeriod);
        when(testHelper.getDepartmentService().get(anyInt())).thenReturn(department);
        when(testHelper.getFormDataService().getFormTemplate(anyInt())).thenReturn(testHelper.getFormTemplate());
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
        Assert.assertFalse(testHelper.getLogger().containsLevel(LogLevel.ERROR));
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
    public void sortRowsTest() {
        testHelper.execute(FormDataEvent.SORT_ROWS);
        checkLogger();
    }

    @Test
    public void importTransportFileTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 2;
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        int expected = testHelper.getDataRowHelper().getAll().size() + 2;
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            BigDecimal expected;

            switch (row.getIndex()) {
                case 2:
                    // графа 2
                    Assert.assertEquals("row.opTypeCode[" + row.getIndex() + "]", 1L, row.getCell("opTypeCode").getValue());

                    // 14 графа
                    Assert.assertNull("row.cost[" + row.getIndex() + "]", row.getCell("cost").getNumericValue());

                    // 15 графа
                    Assert.assertNull("row.vatSum[" + row.getIndex() + "]", row.getCell("vatSum").getNumericValue());

                    // 16 графа
                    expected = roundValue(67L, 2);
                    Assert.assertEquals("row.diffDec[" + row.getIndex() + "]", expected, row.getCell("diffDec").getNumericValue());

                    // 17 графа
                    expected = roundValue(568L, 2);
                    Assert.assertEquals("row.diffInc[" + row.getIndex() + "]", expected, row.getCell("diffInc").getNumericValue());

                    // 18 графа
                    expected = roundValue(5L, 2);
                    Assert.assertEquals("row.diffVatDec[" + row.getIndex() + "]", expected, row.getCell("diffVatDec").getNumericValue());

                    // 19 графа
                    expected = roundValue(547L, 2);
                    Assert.assertEquals("row.diffVatInc[" + row.getIndex() + "]", expected, row.getCell("diffVatInc").getNumericValue());
                    break;
                case 5:
                    // 2 графа
                    Assert.assertEquals("row.opTypeCode[" + row.getIndex() + "]", 3L, row.getCell("opTypeCode").getValue());

                    // 14 графа
                    expected = roundValue(43536L, 2);
                    Assert.assertEquals("row.cost[" + row.getIndex() + "]", expected, row.getCell("cost").getNumericValue());

                    // 15 графа
                    expected = roundValue(658L, 2);
                    Assert.assertEquals("row.vatSum[" + row.getIndex() + "]", expected, row.getCell("vatSum").getNumericValue());

                    // 16 графа
                    expected = roundValue(756L, 2);
                    Assert.assertEquals("row.diffDec[" + row.getIndex() + "]", expected, row.getCell("diffDec").getNumericValue());

                    // 17 графа
                    expected = roundValue(76L, 2);
                    Assert.assertEquals("row.diffInc[" + row.getIndex() + "]", expected, row.getCell("diffInc").getNumericValue());

                    // 18 графа
                    expected = roundValue(5L, 2);
                    Assert.assertEquals("row.diffVatDec[" + row.getIndex() + "]", expected, row.getCell("diffVatDec").getNumericValue());

                    // 19 графа
                    expected = roundValue(-22L, 2);
                    Assert.assertEquals("row.diffVatInc[" + row.getIndex() + "]", expected, row.getCell("diffVatInc").getNumericValue());
            }
        }
    }

    // Консолидация
    @Test
    public void composeTest() {
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
        formType.setTaxType(TaxType.VAT);

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

        // Консолидация
        int expected = testHelper.getDataRowHelper().getAll().size() + 2 * 3; // 2 строк в 2 разделах и для каждой строки подзаголовок и подитог подразделения
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
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
