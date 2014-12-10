package com.aplana.sbrf.taxaccounting.form_template.transport.benefit_vehicles.v2012;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог
 *
 * @author SYasinskiy
 */
public class BenefitVehiclesTest extends ScriptTestBase {
    private static final int TYPE_ID = 202;
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
    protected InputStream getImportRnuInputStream() {
        // Не требуется
        return null;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(BenefitVehiclesTest.class);
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
        Assert.assertEquals(1, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
        // Удаление
        testHelper.setCurrentDataRow(testHelper.getDataRowHelper().getAll().get(0));
        testHelper.execute(FormDataEvent.DELETE_ROW);
        Assert.assertEquals(0, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

    @Test
    public void importExcelTest() {
        testHelper.setImportFileInputStream(getImportXlsInputStream());
        testHelper.execute(FormDataEvent.IMPORT);
        Assert.assertEquals(3, testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }
}
