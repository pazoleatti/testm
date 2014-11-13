package com.aplana.sbrf.taxaccounting.form_template.deal;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * Базовый класс для тестов МУКС
 *
 * @author Levykin
 */
public abstract class DealBaseTest extends ScriptTestBase {
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formType.setId(getFormTypeId());
        formData.setId(TestScriptHelper.CURRENT_FORM_DATA_ID);
        formData.setFormType(formType);
        formData.setFormTemplateId(getFormTypeId());
        formData.setKind(KIND);
        formData.setState(WorkflowState.CREATED);
        formData.setDepartmentId(DEPARTMENT_ID);
        formData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        formData.setReportPeriodId(REPORT_PERIOD_ID);
        return formData;
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
    public void sortRowsTest() {
        testHelper.sortRows();
        checkLogger();
    }

    // Консолидация
    @Test
    public void composeTest() {
        // TODO
    }

    @Test
    public void importExcelTest() {
        // TODO
    }

    protected abstract int getFormTypeId();

    @Override
    protected InputStream getImportXlsInputStream() {
        return this.getClass().getResourceAsStream("importFile.xlsm");
    }

    @Override
    protected InputStream getImportRnuInputStream() {
        return null;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(this.getClass());
    }
}
