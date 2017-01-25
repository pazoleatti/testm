package com.aplana.sbrf.taxaccounting.form_template.ndfl.report_6ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeByDate;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeByRate;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeCommonValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Скрипт для тестирования Формирования xml-файла ЭД "1151099 (6 НДФЛ) Расчет сумм налога на доходы физических лиц, исчисленных и удержанных налоговым агентом"
 */
public class NdflReport6ScriptTest extends DeclarationScriptTestBase {

    private RefBookFactory refBookFactory;

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

    @Test
    public void buildXmlTest() throws IOException, SAXException {
        when(testHelper.getNdflPersonService().findNdflPersonIncomeByDate(any(Long.class))).thenAnswer(new Answer<List<NdflPersonIncomeByDate>>() {
            @Override
            public List<NdflPersonIncomeByDate> answer(InvocationOnMock invocationOnMock) throws Throwable {
                DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                List<NdflPersonIncomeByDate> ndflPersonIncomeByDateList = new ArrayList<NdflPersonIncomeByDate>();

                NdflPersonIncomeByDate ndflPersonIncomeByDate1 = new NdflPersonIncomeByDate();
                ndflPersonIncomeByDate1.setIncomeAccruedDate(format.parse("24.01.2017"));
                ndflPersonIncomeByDate1.setTaxDate(format.parse("24.01.2017"));
                ndflPersonIncomeByDate1.setTaxTransferDate(format.parse("24.01.2017"));
                ndflPersonIncomeByDate1.setIncomePayoutSumm(new BigDecimal(1111.11));
                ndflPersonIncomeByDate1.setWithholdingTax(2222L);
                ndflPersonIncomeByDateList.add(ndflPersonIncomeByDate1);

                return ndflPersonIncomeByDateList;
            }
        });

        when(testHelper.getNdflPersonService().findNdflPersonIncomeCommonValue(any(Long.class))).thenAnswer(new Answer<NdflPersonIncomeCommonValue>() {
            @Override
            public NdflPersonIncomeCommonValue answer(InvocationOnMock invocationOnMock) throws Throwable {
                NdflPersonIncomeCommonValue ndflPersonIncomeCommonValue = new NdflPersonIncomeCommonValue();
                ndflPersonIncomeCommonValue.setCountPerson(11);
                ndflPersonIncomeCommonValue.setWithholdingTax(22L);
                ndflPersonIncomeCommonValue.setNotHoldingTax(33L);
                ndflPersonIncomeCommonValue.setRefoundTax(44L);

                List<NdflPersonIncomeByRate> ndflPersonIncomeByRateList = new ArrayList<NdflPersonIncomeByRate>();
                NdflPersonIncomeByRate ndflPersonIncomeByRate1 = new NdflPersonIncomeByRate();
                ndflPersonIncomeByRate1.setTaxRate(13);
                ndflPersonIncomeByRate1.setIncomeAccruedSumm(new BigDecimal(5555.55));
                ndflPersonIncomeByRate1.setIncomeAccruedSummDiv(new BigDecimal(6666.66));
                ndflPersonIncomeByRate1.setTotalDeductionsSumm(new BigDecimal(7777.77));
                ndflPersonIncomeByRate1.setCalculatedTax(8888L);
                ndflPersonIncomeByRate1.setCalculatedTaxDiv(9999L);
                ndflPersonIncomeByRate1.setPrepaymentSum(new BigDecimal(1111.11));
                ndflPersonIncomeByRateList.add(ndflPersonIncomeByRate1);

                ndflPersonIncomeCommonValue.setNdflPersonIncomeByRateList(ndflPersonIncomeByRateList);

                return ndflPersonIncomeCommonValue;
            }
        });

        testHelper.execute(FormDataEvent.CALCULATE);
        checkLogger();

        String correctXml = IOUtils.toString(getClass().getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/ndfl/report_6ndfl/v2016/report_6ndfl.xml"), "UTF-8");
        XMLUnit.setIgnoreWhitespace(true);
        Diff xmlDiff = new Diff(correctXml, testHelper.getXmlStringWriter().toString());
        xmlDiff.overrideDifferenceListener(new DifferenceListener() {
            @Override
            public int differenceFound(Difference diff) {
                if (diff.getControlNodeDetail().getNode().getNodeName() == "ИдФайл") {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else {
                    System.err.println("called: " + diff);
                    return RETURN_ACCEPT_DIFFERENCE;
                }
            }
            @Override
            public void skippedComparison(Node arg0, Node arg1) { }
        });
        assertTrue(xmlDiff.identical());
    }
}