package com.aplana.sbrf.taxaccounting.form_template.ndfl.report_6ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataServiceImpl;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.script.impl.DeclarationServiceImpl;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.Diff;
import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Скрипт для тестирования Формирования xml-файла ЭД "1151099 (6 НДФЛ) Расчет сумм налога на доходы физических лиц, исчисленных и удержанных налоговым агентом"
 */
@Ignore
public class NdflReport6ScriptTest extends DeclarationScriptTestBase {

    private RefBookFactory refBookFactory;

    private static final int DEPARTMENT_ID = 1;
    private static final int DECLARATION_TEMPLATE_ID = 1022;
    private static final int REPORT_PERIOD_ID = 3489;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final long ASNU_ID = 1000;
    private static final String KPP = "123456789";
    private static final String OKTMO = "12345678901";
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
        declarationData.setOktmo(OKTMO);
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
        // Данные об авансах ФЛ по идентификатору декларации
        when(testHelper.getNdflPersonService().findPrepaymentsByDeclarationDataId(any(Long.class), any(String.class), any(String.class)))
                .thenAnswer(new Answer<List<NdflPersonPrepayment>>() {
                    @Override
                    public List<NdflPersonPrepayment> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        NdflPersonPrepayment ndflPersonPrepayment1 = new NdflPersonPrepayment();
                        ndflPersonPrepayment1.setOperationId(1L);
                        ndflPersonPrepayment1.setSumm(1L);

                        NdflPersonPrepayment ndflPersonPrepayment2 = new NdflPersonPrepayment();
                        ndflPersonPrepayment2.setOperationId(3L);
                        ndflPersonPrepayment2.setSumm(1L);

                        List<NdflPersonPrepayment> ndflPersonPrepaymentList = new ArrayList<NdflPersonPrepayment>();
                        ndflPersonPrepaymentList.add(ndflPersonPrepayment1);
                        ndflPersonPrepaymentList.add(ndflPersonPrepayment2);
                        return ndflPersonPrepaymentList;
                    }
                });

        // Данные о доходах ФЛ по идентификатору декларации
        when(testHelper.getNdflPersonService().findIncomesByPeriodAndDeclarationDataId(any(Long.class), any(Date.class), any(Date.class), any(String.class), any(String.class)))
                .thenAnswer(new Answer<List<NdflPersonIncome>>() {
                    @Override
                    public List<NdflPersonIncome> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

                        // Ставка 13 Идентификато операции 1
                        NdflPersonIncome ndflPersonIncome1 = new NdflPersonIncome();
                        ndflPersonIncome1.setNdflPersonId(1L);
                        ndflPersonIncome1.setOperationId(1L);
                        ndflPersonIncome1.setWithholdingTax(1L);
                        ndflPersonIncome1.setNotHoldingTax(1L);
                        ndflPersonIncome1.setRefoundTax(1L);
                        ndflPersonIncome1.setTaxRate(13);
                        ndflPersonIncome1.setIncomeAccruedSumm(new BigDecimal(1.1));
                        ndflPersonIncome1.setTotalDeductionsSumm(new BigDecimal(1.1));
                        ndflPersonIncome1.setCalculatedTax(1L);
                        ndflPersonIncome1.setIncomeCode("1010");
                        ndflPersonIncome1.setIncomeAccruedDate(format.parse("01.01.2001"));
                        ndflPersonIncome1.setTaxDate(format.parse("01.01.2001"));
                        ndflPersonIncome1.setTaxTransferDate(format.parse("01.01.2001"));
                        ndflPersonIncome1.setIncomePayoutSumm(new BigDecimal(1.1));

                        // Ставка 13 Идентификато операции 2
                        NdflPersonIncome ndflPersonIncome2 = new NdflPersonIncome();
                        ndflPersonIncome2.setNdflPersonId(2L);
                        ndflPersonIncome2.setOperationId(2L);
                        ndflPersonIncome2.setWithholdingTax(1L);
                        ndflPersonIncome2.setNotHoldingTax(1L);
                        ndflPersonIncome2.setRefoundTax(1L);
                        ndflPersonIncome2.setTaxRate(13);
                        ndflPersonIncome2.setIncomeAccruedSumm(new BigDecimal(1.1));
                        ndflPersonIncome2.setTotalDeductionsSumm(new BigDecimal(1.1));
                        ndflPersonIncome2.setCalculatedTax(1L);
                        ndflPersonIncome2.setIncomeCode("1010");
                        ndflPersonIncome2.setIncomeAccruedDate(format.parse("02.02.2002"));
                        ndflPersonIncome2.setTaxDate(format.parse("02.02.2002"));
                        ndflPersonIncome2.setTaxTransferDate(format.parse("02.02.2002"));
                        ndflPersonIncome2.setIncomePayoutSumm(new BigDecimal(1.1));

                        // Ставка 20 Идентификато операции 3
                        NdflPersonIncome ndflPersonIncome3 = new NdflPersonIncome();
                        ndflPersonIncome2.setNdflPersonId(2L);
                        ndflPersonIncome3.setOperationId(3L);
                        ndflPersonIncome3.setWithholdingTax(1L);
                        ndflPersonIncome3.setNotHoldingTax(1L);
                        ndflPersonIncome3.setRefoundTax(1L);
                        ndflPersonIncome3.setTaxRate(20);
                        ndflPersonIncome3.setIncomeAccruedSumm(new BigDecimal(1.1));
                        ndflPersonIncome3.setTotalDeductionsSumm(new BigDecimal(1.1));
                        ndflPersonIncome3.setCalculatedTax(1L);
                        ndflPersonIncome3.setIncomeCode("0000");
                        ndflPersonIncome3.setIncomeAccruedDate(format.parse("03.03.2003"));
                        ndflPersonIncome3.setTaxDate(format.parse("03.03.2003"));
                        ndflPersonIncome3.setTaxTransferDate(format.parse("03.03.2003"));
                        ndflPersonIncome3.setIncomePayoutSumm(new BigDecimal(1.1));

                        List<NdflPersonIncome> ndflPersonIncomeList = new ArrayList<NdflPersonIncome>();
                        ndflPersonIncomeList.add(ndflPersonIncome1);
                        ndflPersonIncomeList.add(ndflPersonIncome2);
                        ndflPersonIncomeList.add(ndflPersonIncome3);
                        return ndflPersonIncomeList;
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

    /**
     * Тест создания спецотчета
     * НАРУШАЕТ ВЫПОЛНЕНИЕ ТЕСТА ГЕНЕРАЦИИ XML, БУДУЧИ ВЫПОЛНЕННЫМ ПЕРВЫМ
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void createSpecificReportTest() throws Exception {

        ScriptSpecificDeclarationDataReportHolder reportHolder = createReportHolder();
        testHelper.setScriptSpecificReportHolder(reportHolder);

        doAnswer(new Answer<JasperPrint>() {
            @Override
            public JasperPrint answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                InputStream jrxmlTemplate = (InputStream) args[0];
                Map param = (Map) args[1];
                Closure closure = (Closure) args[2];

                DeclarationService declarationService = new DeclarationServiceImpl();
                ByteArrayInputStream xmlData = declarationService.generateXmlData(closure);

                DeclarationDataService declarationDataService = new DeclarationDataServiceImpl();
                Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
                return declarationDataService.createJasperReport(xmlData, jrxmlTemplate, param, connection);

            }
        }).when(testHelper.getDeclarationService()).createJasperReport(any(InputStream.class), anyMap(), any(Closure.class));

        testHelper.execute(FormDataEvent.CREATE_SPECIFIC_REPORT);

        ScriptSpecificDeclarationDataReportHolder resultReportHolder = testHelper.getScriptSpecificReportHolder();
        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) resultReportHolder.getFileOutputStream();

        //Что-то сформировали
        Assert.assertTrue(outputStream.toByteArray().length > 0);

//        writeToFileAndOpen(outputStream, "person_report.pdf");

        checkLogger();
    }

    /**
     * Метод помогает при разработке. Сохраняет отчет в файл и открывает его для визуальной проверки.
     *
     * @param byteArrayOutputStream
     * @throws IOException
     */
    private void writeToFileAndOpen(ByteArrayOutputStream byteArrayOutputStream, String fileName) throws IOException {
        File file = new File("C:\\temp\\" + fileName);
        OutputStream foutputStream = new FileOutputStream(file);
        try {
            byteArrayOutputStream.writeTo(foutputStream);
        } finally {
            foutputStream.close();
        }
        Desktop.getDesktop().open(file);
    }

    @Override
    protected DeclarationSubreport createDeclarationSubreport() {
        DeclarationSubreport result = new DeclarationSubreport();
        result.setId(1L);
        result.setAlias("report_6ndfl");
        result.setName("report_6ndfl");
        result.setBlobDataId("100500");
        result.setOrder(1);
        result.setDeclarationSubreportParams(Collections.EMPTY_LIST);
        return result;
    }
}