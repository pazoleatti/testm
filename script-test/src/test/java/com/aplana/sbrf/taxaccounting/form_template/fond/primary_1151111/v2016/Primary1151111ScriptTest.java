package com.aplana.sbrf.taxaccounting.form_template.fond.primary_1151111.v2016;


import com.aplana.sbrf.taxaccounting.form_template.ndfl.report_6ndfl.v2016.NdflReport6ScriptTest;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Тестирование разбора xml-файла Расчета по страховым взносам
 */
public class Primary1151111ScriptTest extends DeclarationScriptTestBase {

    private static final int DEPARTMENT_ID = 1;
    private static final int REPORT_PERIOD_ID = 3489;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final String KPP = "123456789";

    protected DeclarationData getDeclarationData() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setDepartmentId(DEPARTMENT_ID);
        declarationData.setReportPeriodId(REPORT_PERIOD_ID);
        declarationData.setState(State.ACCEPTED);
        declarationData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        declarationData.setKpp(KPP);
        return declarationData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(NdflReport6ScriptTest.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
    }

    @Before
    public void mockService() {
        //mock сервисов для получения тестовых наборов данных
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
            testHelper = new DeclarationTestScriptHelper(path, getDeclarationData(), getMockHelper()) {};
        }
        testHelper.reset();
    }

    /**
     * Тестирование проверок загруженных данных
     */
    @Test
    public void checkTest() {
        when(testHelper.getRaschsvSvnpPodpisantService().findRaschsvSvnpPodpisant(any(Long.class)))
                .thenAnswer(new Answer<RaschsvSvnpPodpisant>() {
                    @Override
                    public RaschsvSvnpPodpisant answer(InvocationOnMock invocationOnMock) throws Throwable {
                        RaschsvSvnpPodpisant raschsvSvnpPodpisant = new RaschsvSvnpPodpisant();
                        raschsvSvnpPodpisant.setSvnpOkved("123");
                        return raschsvSvnpPodpisant;
                    }
                });
        testHelper.execute(FormDataEvent.CHECK);
    }

    /**
     * Тестирование разбора xml
     * @throws IOException
     */
    @Test
    public void parseTest() throws IOException {

        InputStream inputStream = Primary1151111ScriptTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/schema.xml");

//        Map<String, Object> params = new HashedMap<String, Object>();
//
//        // Проверка соответствия числа узлов по их имени
//        Map<String, Integer> countNodes = new HashedMap<String, Integer>();
//        countNodes.put("ПерсСвСтрахЛиц", 10);
//        countNodes.put("УплПерОПС", 1);
//        countNodes.put("УплПерОМС", 1);
//        countNodes.put("УплПерОПСДоп", 10);
//        countNodes.put("УплПерДСО", 10);
//        countNodes.put("УплПерОСС", 1);
//        countNodes.put("ПревРасхОСС", 1);
//        countNodes.put("РасчСВ_ОПС_ОМС", 10);
//        countNodes.put("РасчСВ_ОСС.ВНМ", 1);
//        countNodes.put("РасхОССЗак", 1);
//        countNodes.put("ВыплФинФБ", 1);
//
//        countNodes.put("ПравТариф3.1.427", 1);
//        countNodes.put("ПравТариф5.1.427", 1);
//        countNodes.put("ПравТариф7.1.427", 1);
//        countNodes.put("СвПримТариф9.1.427", 1);
//        countNodes.put("СвПримТариф2.2.425", 1);
//        countNodes.put("СвПримТариф1.3.422", 1);
//
//        countNodes.put("СведПатент", 10);
//        countNodes.put("СвИноГражд", 10);
//        countNodes.put("СведОбуч", 10);
//
//        DeclarationData declarationData = new DeclarationData();
//        declarationData.setId(1L);
//
//        params.put("declarationData", getDeclarationData());
//        params.put("countNodes", countNodes);

        testHelper.setImportFileInputStream(inputStream);
//        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        checkLogger();
    }
}
