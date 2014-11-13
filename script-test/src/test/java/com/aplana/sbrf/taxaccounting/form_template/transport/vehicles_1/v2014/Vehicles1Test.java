package com.aplana.sbrf.taxaccounting.form_template.transport.vehicles_1.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог (new)
 *
 * @author Levykin
 */
public class Vehicles1Test extends ScriptTestBase {
    private static final int TYPE_ID = 10704;
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
    protected InputStream getImportXlsInputStream() {
        return Vehicles1Test.class.getResourceAsStream("importFile.xlsm");
    }

    @Override
    protected InputStream getImportRnuInputStream() {
        return Vehicles1Test.class.getResourceAsStream("importFile.rnu");
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Vehicles1Test.class);
    }

    @Test
    public void create() {
        testHelper.create();
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    // Проверка пустой
    @Test
    public void checkTest() {
        testHelper.check();
        checkLogger();
    }

    // Расчет пустой
    @Test
    public void calcTest() {
        testHelper.calc();
        checkLogger();
    }

    @Test
    public void addDelRowTest() {
        // Добавление
        testHelper.addRow();
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
        testHelper.delRow();
        Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void afterCreateTest() {
        testHelper.afterCreate();
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

        // Один экземпляр-источник
        FormData sourceFormData = new FormData();
        sourceFormData.initFormTemplateParams(testHelper.getFormTemplate());
        sourceFormData.setId(2L);
        sourceFormData.setState(WorkflowState.ACCEPTED);
        when(testHelper.getFormDataService().getLast(eq(departmentFormType.getFormTypeId()), eq(KIND), eq(DEPARTMENT_ID),
                anyInt(), any(Integer.class))).thenReturn(sourceFormData);

        // DataRowHelper НФ-источника
        DataRowHelper sourceDataRowHelper = new DataRowHelperStub();
        when(testHelper.getFormDataService().getDataRowHelper(sourceFormData)).thenReturn(sourceDataRowHelper);

        // Данные НФ-источника, формируются импортом
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.importExcel();
        sourceDataRowHelper.save(testHelper.getDataRowHelper().getAll());
        testHelper.initRowData();

        // Консолидация
        testHelper.compose();
        Assert.assertEquals(7, testHelper.getDataRowHelper().getAll().size());

        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.sortRows();
        checkLogger();
    }

    //@Test TODO Добавить тест для импорта, пока .rnu файла нет
    public void importTransportFileTest() {
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.importTransportFile();
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.importExcel();
        Assert.assertEquals(7, testHelper.getDataRowHelper().getAll().size());
        // Проверка расчетных данных
        List<DataRow<Cell>> dataRows = testHelper.getDataRowHelper().getAll();
        Assert.assertEquals(6, dataRows.get(1).getCell("pastYear").getNumericValue().intValue());
        Assert.assertEquals(2, dataRows.get(2).getCell("pastYear").getNumericValue().intValue());
        Assert.assertEquals(13, dataRows.get(4).getCell("pastYear").getNumericValue().intValue());
        Assert.assertEquals(3, dataRows.get(6).getCell("pastYear").getNumericValue().intValue());
        checkLogger();
    }
}
