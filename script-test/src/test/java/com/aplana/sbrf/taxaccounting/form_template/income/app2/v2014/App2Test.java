package com.aplana.sbrf.taxaccounting.form_template.income.app2.v2014;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с
 * финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов
 * formTemplateId=1415
 *
 * @author SYasinskiy
 */
public class App2Test extends ScriptTestBase {
    private static final int TYPE_ID = 415;
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
        return getDefaultScriptTestMockHelper(App2Test.class);
    }

    @Before
    public void mockFormDataService() {
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

    //TODO
    /*
   @Test
    public void importTransportFileTest() {
        testHelper.setImportFileInputStream(getImportRnuInputStream());
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLoadData(testHelper.getDataRowHelper().getAll());
        checkLogger();
    }

    // Проверить загруженные данные
    void checkLoadData(List<DataRow<Cell>> dataRows) {
    }
    */
}
