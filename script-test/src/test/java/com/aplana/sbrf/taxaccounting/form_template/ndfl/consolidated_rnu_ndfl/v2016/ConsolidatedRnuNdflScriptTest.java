package com.aplana.sbrf.taxaccounting.form_template.ndfl.consolidated_rnu_ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Тесты скрипта для формы РНУ-НДФЛ Консолидированная
 *
 * @author Andrey Drunk
 */
public class ConsolidatedRnuNdflScriptTest extends DeclarationScriptTestBase {

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

    //@After
    //public void resetMock() {
    //reset(testHelper.getRefBookFactory());
    //}
    //@Before
    //public void mockService() {
    //mock сервисов для получения тестовых наборов данных
    //}

    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    @Test
    public void calculateTest() throws Exception {


        when(testHelper.getRefBookDataProvider().getRecordData(anyList())).thenReturn(createRefBook());

        //mock service
        when(testHelper.getDeclarationService().getDeclarationSourcesInfo(any(DeclarationData.class),
                anyBoolean(), anyBoolean(), Matchers.isNull(State.class), any(TAUserInfo.class), any(Logger.class))).thenAnswer(new Answer<List<Relation>>() {
            @Override
            public List<Relation> answer(InvocationOnMock invocationOnMock) throws Throwable {
                List<Relation> relation = new ArrayList<Relation>();
                //relation.add(createRelation());
                return relation;
            }
        });

        //declarationService.getDeclarationSourcesInfo(declarationData, false, false, null, userInfo, logger);

        testHelper.execute(FormDataEvent.CALCULATE);


        assertEquals("1", xpath("//*[local-name()='Файл']/@имя"));

        checkLogger();


    }


    @Test
    public void getSourcesTest() throws Exception {
        testHelper.execute(FormDataEvent.GET_SOURCES);
        checkLogger();
    }

    //@Test
    //public void calcCreateSpecificReport() throws Exception {
    //    testHelper.execute(FormDataEvent.CREATE_SPECIFIC_REPORT);
    //    checkLogger();
    //}

    private Relation createRelation() {
        Relation relation = new Relation();
        return relation;
    }


    private Map<Long, Map<String, RefBookValue>> createRefBook() {
        Map<Long, Map<String, RefBookValue>> map = new HashMap<Long, Map<String, RefBookValue>>();
        for (int i = 0; i < 5; i++) {
            map.put(Long.valueOf(i), createRefBookMock(i));
        }
        return map;
    }

    private Map<String, RefBookValue> createRefBookMock(long id) {
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, id));
        result.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "foo"));
        result.put("ADDRESS", new RefBookValue(RefBookAttributeType.REFERENCE, Long.valueOf(new Random().nextInt(1000))));

        return result;
    }


}
