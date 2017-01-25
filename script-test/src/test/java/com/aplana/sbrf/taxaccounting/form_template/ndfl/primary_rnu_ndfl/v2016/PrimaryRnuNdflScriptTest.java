package com.aplana.sbrf.taxaccounting.form_template.ndfl.primary_rnu_ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Andrey Drunk
 */


public class PrimaryRnuNdflScriptTest extends ScriptTestBase {

    /**
     * Тип формы НДФЛ
     */
    private static final int TYPE_ID = 814;
    /**
     * Подразделение
     */
    private static final int DEPARTMENT_ID = 1;

    /**
     * Идентификатор отчетного периода
     */
    private static final int REPORT_PERIOD_ID = 1;

    private static final int DEPARTMENT_PERIOD_ID = 1;

    /**
     * Тип формы первичная
     */
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
        return getDefaultScriptTestMockHelper(PrimaryRnuNdflScriptTest.class);
    }


    /**
     * Инициализация перед каждым отдельным тестом
     */
    @Before
    public void init() {
        // Хэлпер хранится статично для оптимизации, чтобы он был один для всех тестов отдельного скрипта
        if (testHelper == null) {
            String path = getFolderPath();
            if (path == null) {
                throw new ServiceException("Test folder path is null!");
            }
            testHelper = new TestScriptHelper(path, getFormData(), getMockHelper()) {
                @Override
                protected void initFormTemplate() {
                    //do nothing...
                }

                @Override
                public void initRowData() {
                    //do nothing...
                }
            };
        }
        testHelper.reset();
    }


    @Test
    public void create() throws IOException {

        InputStream inputStream = PrimaryRnuNdflScriptTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/ndfl/primary_rnu_ndfl/v2016/rnu_ndfl.xml");
        //String xml = IOUtils.toString(xmlInputStream, "windows-1251");

        Map<String, Object> param = new HashedMap<String, Object>();

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        param.put("declarationData", declarationData);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, param);

        //Assert.assertEquals(testHelper.getFormTemplate().getRows().size(), testHelper.getDataRowHelper().getAll().size());
        checkLogger();
    }

}
