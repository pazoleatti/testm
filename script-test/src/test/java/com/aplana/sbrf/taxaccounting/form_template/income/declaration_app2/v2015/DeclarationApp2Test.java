package com.aplana.sbrf.taxaccounting.form_template.income.declaration_app2.v2015;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Формирование XML для приложения 2 для декларации по прибыли
 * аналог com/aplana/sbrf/taxaccounting/form_template/transport/declaration/v2014/DeclarationTest.java
 * TODO:
 *      - добавить проверку xml'ки в calcTest
 *      - добавить проверку переформирования декларации с заполненными источниками
 */
public class DeclarationApp2Test extends DeclarationScriptTestBase {
    private static final int DEPARTMENT_ID = 1;
    private static final int DECLARATION_TEMPLATE_ID = 1022;
    private static final int REPORT_PERIOD_ID = 1;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final boolean ACCEPTED = false;
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
        declarationData.setAccepted(ACCEPTED);
        declarationData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        declarationData.setKpp(KPP);
        declarationData.setTaxOrganCode(CODE_ORG);
        return declarationData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(DeclarationApp2Test.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
    }

    @Before
    public void mockService() {
        when(testHelper.getRefBookFactory().getDataProvider(anyLong())).thenAnswer(new Answer<RefBookDataProvider>() {
            @Override
            public RefBookDataProvider answer(InvocationOnMock invocation) throws Throwable {
                final Long id = (Long) invocation.getArguments()[0];
                if (id == null) {
                    return null;
                }
                RefBookDataProvider refBookDataProvider = mock(RefBookDataProvider.class);
                // для остальных справочников используется метод getRecords
                when(refBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenAnswer(
                        new Answer<PagingResult<Map<String, RefBookValue>>>() {
                            @Override
                            public PagingResult<Map<String, RefBookValue>> answer(InvocationOnMock invocation) throws Throwable {
                                Map<String, RefBookValue> record = testHelper.getFormDataService().getRefBookValue(id, 1L, new HashMap<String, Map<String, RefBookValue>>());
                                return new PagingResult<Map<String, RefBookValue>>(Arrays.asList(record));
                            }
                        });
                // для справочника 30 используется метод getRecordData
                when(refBookDataProvider.getRecordData(anyLong())).thenAnswer(
                        new Answer<Map<String, RefBookValue>>() {
                            @Override
                            public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                                Long recordId = (Long) invocation.getArguments()[0];
                                return testHelper.getFormDataService().getRefBookValue(id, recordId, new HashMap<String, Map<String, RefBookValue>>());
                            }
                        }
                );
                return refBookDataProvider;
            }
        });
    }

    @Test
    public void createTest() {
        testHelper.execute(FormDataEvent.CREATE);
        checkLogger();
    }

    @Test
    public void checkTest() {
        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    // пустые источники
    @Test
    public void calcTest() {
        testHelper.execute(FormDataEvent.CALCULATE);
        // testHelper.getLogger().info("%s", testHelper.getXmlStringWriter() + "");
        Assert.assertNotNull(testHelper.getXmlStringWriter());
        // TODO (Bulat Kinzyabulatov) добавить проверку xml'ки
        checkLogger();
    }

    @Test
    public void acceptTest() {
        testHelper.execute(FormDataEvent.MOVE_CREATED_TO_ACCEPTED);
        checkLogger();
    }
}
