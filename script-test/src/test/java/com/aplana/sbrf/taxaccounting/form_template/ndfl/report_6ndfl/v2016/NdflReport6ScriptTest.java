package com.aplana.sbrf.taxaccounting.form_template.ndfl.report_6ndfl.v2016;

import com.aplana.sbrf.taxaccounting.form_template.ndfl.report_2ndfl.v2016.SchemaSimpleTest;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.State;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeByDate;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeByRate;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeCommonValue;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.reset;
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
    private static final String CODE_ORG = "1234";

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
        // СумСтавка
        when(testHelper.getNdflPersonService().findNdflPersonIncomeCommonValue(any(Long.class), any(Date.class), any(Date.class))).thenAnswer(new Answer<NdflPersonIncomeCommonValue>() {
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

        // СумДата
        when(testHelper.getNdflPersonService().findNdflPersonIncomeByDate(any(Long.class), any(Date.class), any(Date.class))).thenAnswer(new Answer<List<NdflPersonIncomeByDate>>() {
            @Override
            public List<NdflPersonIncomeByDate> answer(InvocationOnMock invocationOnMock) throws Throwable {
                DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                List<NdflPersonIncomeByDate> ndflPersonIncomeByDateList = new ArrayList<NdflPersonIncomeByDate>();

                NdflPersonIncomeByDate ndflPersonIncomeByDate1 = new NdflPersonIncomeByDate();
                ndflPersonIncomeByDate1.setIncomeAccruedDate(format.parse("24.01.2017"));
                ndflPersonIncomeByDate1.setTaxDate(format.parse("24.01.2017"));
                ndflPersonIncomeByDate1.setTaxTransferDate(format.parse("24.01.2017"));
                ndflPersonIncomeByDate1.setIncomePayoutSumm(new BigDecimal(1111.5));
                ndflPersonIncomeByDate1.setWithholdingTax(2222L);
                ndflPersonIncomeByDateList.add(ndflPersonIncomeByDate1);

                return ndflPersonIncomeByDateList;
            }
        });

        // Период
        when(testHelper.getFormDataService().getRefBookValue(eq(8L), anyLong(), anyMap())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        result.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "21"));
                        return result;
                    }
                }
        );

        // ПоМесту
        when(testHelper.getFormDataService().getRefBookValue(eq(2L), anyLong(), anyMap())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        result.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "120"));
                        return result;
                    }
                }
        );

        // ПрПодп
        when(testHelper.getFormDataService().getRefBookValue(eq(35L), anyLong(), anyMap())).thenAnswer(
                new Answer<Map<String, RefBookValue>>() {
                    @Override
                    public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
                        result.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, 2));
                        return result;
                    }
                }
        );

        // НомКорр
        when(testHelper.getReportPeriodService().getCorrectionNumber(anyInt())).thenReturn(111);

        testHelper.execute(FormDataEvent.CALCULATE);

        // 1. валидация по xsd
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL xsdResource = getClass().getResource("/com/aplana/sbrf/taxaccounting/form_template/ndfl/report_6ndfl/v2016/schema.xsd");
            Schema schema = factory.newSchema(xsdResource);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(testHelper.getXmlStringWriter().toString())));
        } catch (SAXException e) {
            System.out.println("Exception: "+ e.getMessage());
            assertTrue(false);
        }

        // 2. сравнение со сгенерированным xml
        String correctXml = IOUtils.toString(getClass().getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/ndfl/report_6ndfl/v2016/report_6ndfl.xml"), "windows-1251");
        XMLUnit.setIgnoreWhitespace(true);
        Diff xmlDiff = new Diff(correctXml, testHelper.getXmlStringWriter().toString());
        xmlDiff.overrideDifferenceListener(new DifferenceListener() {

            // Атрибуты, значения которых будем игнорировать
            private final List<String> IGNORE_ATTRS =  new ArrayList<String>(){{
                add("ДатаДок");
                add("ИдФайл");
                add("ОтчетГод");
            }};

            @Override
            public int differenceFound(Difference diff) {
                if (IGNORE_ATTRS.contains(diff.getControlNodeDetail().getNode().getNodeName())) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else {
                    System.err.println(diff);
                    return RETURN_ACCEPT_DIFFERENCE;
                }
            }
            @Override
            public void skippedComparison(Node arg0, Node arg1) { }
        });
        assertTrue(xmlDiff.identical());

        checkLogger();
    }
}