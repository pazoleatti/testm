package com.aplana.sbrf.taxaccounting.formtemplate.transport.vehicles_1.v2014;

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
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог (new)
 *
 * @author Levykin
 */
public class Vehicles1Test extends ScriptTestBase {
    private static final String PATH = "/form_template/transport/vehicles_1/v2014/";
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
    protected String getFolderPath() {
        return PATH;
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
        // TODO Проверить перенос данных в copyData()
        // Для этого нужен mock reportPeriodService#getPrevReportPeriod и др.
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

        // Данные НФ-источника TODO Заполнить импортом, иначе сложно обойти лог. проверки
        List<DataRow<Cell>> dataRowList = new LinkedList<DataRow<Cell>>();
        // Фиксированные строки
        dataRowList.addAll(testHelper.getFormTemplate().getRows());
        // Новые строки
        dataRowList.addAll(Arrays.asList(sourceFormData.createDataRow(), sourceFormData.createDataRow()));
        sourceDataRowHelper.save(dataRowList);
        // Консолидация
        // testHelper.compose();
        checkLogger();
    }

    @Test
    public void sortRowsTest() {
        testHelper.sortRows();
        checkLogger();
    }

    //@Test TODO Добавить тест для импорта
    public void importTransportFileTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.importTransportFile();
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    //@Test TODO Добавить тест для импорта
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.importExcel();
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }
}
