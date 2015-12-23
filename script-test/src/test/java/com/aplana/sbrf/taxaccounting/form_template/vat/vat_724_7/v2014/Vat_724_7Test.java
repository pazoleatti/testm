package com.aplana.sbrf.taxaccounting.form_template.vat.vat_724_7.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
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
 * (724.7) Отчёт о суммах НДС начисленных налоговым агентом по договорам аренды имущества (балансовый счёт 60309.03)
 */
public class Vat_724_7Test extends ScriptTestBase {
    private static final int TYPE_ID = 605;
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
        return getDefaultScriptTestMockHelper(Vat_724_7Test.class);
    }

    @Before
    public void mockFormDataService() {
        Department department = new Department();
        department.setId(DEPARTMENT_ID);
        department.setName("Подразделение");
        department.setType(DepartmentType.TERR_BANK);

        when(testHelper.getDepartmentService().get(anyInt())).thenReturn(department);
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

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setBalance(false);
        when(testHelper.getDepartmentReportPeriodService().get(anyInt())).thenReturn(departmentReportPeriod);

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
        int expected = testHelper.getDataRowHelper().getAll().size() + 2 + 2; // 2 строки из одного источника и +подзаголовок и +подитог источника (подразделения)
        testHelper.execute(FormDataEvent.COMPOSE);
        Assert.assertEquals(expected, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    /** Проверить загруженные данные. */
    void checkLoadData(List<DataRow<Cell>> dataRows) {
        long index = 1;
        BigDecimal expected;
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                continue;
            }

            // графа 2
            Assert.assertNotNull("row.operDate[" + row.getIndex() + "]", row.getCell("operDate").getDateValue());

            // графа 3
            Assert.assertNotNull("row.name[" + row.getIndex() + "]", row.getCell("name").getStringValue());

            // графа 4
            Assert.assertNotNull("row.inn[" + row.getIndex() + "]", row.getCell("inn").getStringValue());

            // графа 5
            Assert.assertNotNull("row.balanceNumber[" + row.getIndex() + "]", row.getCell("balanceNumber").getStringValue());

            // графа 6
            expected = roundValue(index, 2);
            Assert.assertEquals("row.sum[" + row.getIndex() + "]", expected, row.getCell("sum").getNumericValue());

            // графа 7
            Assert.assertNotNull("row.orderNumber[" + row.getIndex() + "]", row.getCell("orderNumber").getStringValue());

            // графа 8
            expected = roundValue(index, 2);
            Assert.assertEquals("row.ndsSum[" + row.getIndex() + "]", expected, row.getCell("ndsSum").getNumericValue());

            // графа 9
            Assert.assertNotNull("row.sfDate[" + row.getIndex() + "]", row.getCell("sfDate").getDateValue());

            // графа 10
            Assert.assertNotNull("row.sfNumber[" + row.getIndex() + "]", row.getCell("sfNumber").getStringValue());

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