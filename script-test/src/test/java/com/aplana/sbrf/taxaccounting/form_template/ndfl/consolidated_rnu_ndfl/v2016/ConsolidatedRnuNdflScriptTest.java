package com.aplana.sbrf.taxaccounting.form_template.ndfl.consolidated_rnu_ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Тесты скрипта для формы РНУ-НДФЛ Консолидированная
 * @author Andrey Drunk
 */
public class ConsolidatedRnuNdflScriptTest extends DeclarationScriptTestBase{

    private static final int DEPARTMENT_ID = 1;
    private static final int DECLARATION_TEMPLATE_ID = 1022;
    private static final int REPORT_PERIOD_ID = 3489;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final long ASNU_ID = 1000;
    private static final String KPP = "123456789";
    private static final String CODE_ORG = "0123456789";

    @Override
    protected DeclarationData getDeclarationData() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setDepartmentId(DEPARTMENT_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);
        declarationData.setReportPeriodId(REPORT_PERIOD_ID);
        declarationData.setState(State.ACCEPTED);
        declarationData.setAsnuId(ASNU_ID);
        declarationData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        declarationData.setKpp(KPP);
        declarationData.setTaxOrganCode(CODE_ORG);
        return declarationData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(ConsolidatedRnuNdflScriptTest.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
    }

    @Before
    public void mockService() {
        //mock сервисов для получения тестовых наборов данных
    }

    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    @Test
    public void calcTest() throws Exception {
        //TODO заготовка тестов формирования xml, и проверка с помощью xpath
        testHelper.execute(FormDataEvent.CALCULATE);
        // testHelper.getLogger().info("%s", testHelper.getXmlStringWriter() + "");
        assertNotNull(testHelper.getXmlStringWriter());

        //Проверяем XML
        assertEquals("100500", xpath("//*[local-name()='Файл']/@имя"));

        checkLogger();
    }

    @Test
    public void calcCompose() throws Exception {
        testHelper.execute(FormDataEvent.COMPOSE);
        checkLogger();
    }

    @Test
    public void calcGetSources() throws Exception {
        testHelper.execute(FormDataEvent.GET_SOURCES);
        checkLogger();
    }

    //@Test
    //public void calcCreateSpecificReport() throws Exception {
    //    testHelper.execute(FormDataEvent.CREATE_SPECIFIC_REPORT);
    //    checkLogger();
    //}

}
