package com.aplana.sbrf.taxaccounting.formtemplate.transport.vehicles.v2012;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог
 *
 * @author Levykin
*/
public class VehiclesTest extends ScriptTestBase {
    private static final String PATH = "/form_template/transport/vehicles/v2012/";
    private static final int TYPE_ID = 201;
    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formType.setId(TYPE_ID);
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
    protected InputStream getImportInputStream() {
        return VehiclesTest.class.getResourceAsStream("importFile.xlsm");
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
       return getDefaultScriptTestMockHelper(VehiclesTest.class);
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
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        testHelper.setCurrentDataRow(testHelper.getDataRowHelper().getAll().get(0));
        testHelper.delRow();
        Assert.assertEquals(0, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        InputStream importInputStream = getImportInputStream();
        testHelper.setImportFileInputStream(importInputStream);
        testHelper.importExcel();
        Assert.assertEquals(5, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }
}
