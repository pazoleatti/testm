package com.aplana.sbrf.taxaccounting.form_template.ndfl.report_2ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.custommonkey.xmlunit.Validator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.reset;

public class SchemaSimpleTest extends DeclarationScriptTestBase {
    private static final int DEPARTMENT_ID = 1;
    private static final int DECLARATION_TEMPLATE_ID = 103;
    private static final int REPORT_PERIOD_ID = 2;
    private static final int DEPARTMENT_PERIOD_ID = 3;

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(SchemaSimpleTest.class);
    }

    @Override
    protected DeclarationData getDeclarationData() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setReportPeriodId(REPORT_PERIOD_ID);
        declarationData.setDepartmentId(DEPARTMENT_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);
        declarationData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        return declarationData;
    }

    @Before
    public void init() {
        // Хэлпер хранится статично для оптимизации, чтобы он был один для всех тестов отдельного скрипта
        if (testHelper == null) {
            String path = getFolderPath();
            if (path == null) {
                throw new ServiceException("Test folder path is null!");
            }
            testHelper = new DeclarationTestScriptHelper(path, getDeclarationData(), getMockHelper());
        }
        testHelper.reset();
    }


    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
    }

    @Test
    public void buildXmlTest() throws SAXException, IOException {
        testHelper.execute(FormDataEvent.CALCULATE);
        assertNotNull(testHelper.getXmlStringWriter());
        System.out.println(testHelper.getXmlStringWriter().toString());
        InputSource is = new InputSource(new InputStreamReader(new ByteArrayInputStream(testHelper.getXmlStringWriter().toString().getBytes())));

        Validator v = new Validator(is);
        v.useXMLSchema(true);
        v.setJAXP12SchemaSource(SchemaSimpleTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/ndfl/report_2ndfl/v2016/schema.xsd)"));
        System.out.println(v.isValid());
        checkLogger();
    }
}
