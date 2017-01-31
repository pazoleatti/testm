package com.aplana.sbrf.taxaccounting.form_template.ndfl.primary_rnu_ndfl.v2016;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.NdflPersonDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.form_template.ndfl.consolidated_rnu_ndfl.v2016.ConsolidatedRnuNdflScriptTest;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams;
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataServiceImpl;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.script.NdflPersonService;
import com.aplana.sbrf.taxaccounting.service.script.impl.DeclarationServiceImpl;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import com.lowagie.text.pdf.codec.Base64;
import com.sun.org.apache.xpath.internal.SourceTree;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import sun.nio.cs.StandardCharsets;


import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.util.TestUtils.readFile;
import static com.aplana.sbrf.taxaccounting.util.TestUtils.toDate;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Drunk
 */
public class PrimaryRnuNdflScriptTest extends DeclarationScriptTestBase {

    private static final int DEPARTMENT_ID = 1;
    private static final int DECLARATION_TEMPLATE_ID = 1022;
    private static final int REPORT_PERIOD_ID = 3489;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final long ASNU_ID = 1000;
    private static final String KPP = "123456789";
    private static final String CODE_ORG = "0123456789";
    private static final String REPORT_PERSON_NAME = "report_person";

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
    protected DeclarationSubreport createDeclarationSubreport() {
        DeclarationSubreport result = super.createDeclarationSubreport();
        result.setName(REPORT_PERSON_NAME);
        return result;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(PrimaryRnuNdflScriptTest.class);
    }

    @Override
    protected InputStream getInputStream() {
        return PrimaryRnuNdflScriptTest.class.getResourceAsStream("_______18_0001_001000212016ecf863ca-6349-4105-b1e1-33c1good.xml");
    }

    @Test
    public void importDataTest() throws IOException {

        final List<NdflPerson> importedData = new ArrayList<NdflPerson>();
        when(testHelper.getNdflPersonService().save(any(NdflPerson.class))).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                NdflPerson ndflPerson = (NdflPerson) args[0];
                //System.out.println(ndflPerson + " [" + ndflPerson.getFirstName() + "," + ndflPerson.getLastName() + "]");
                importedData.add(ndflPerson);
                return (long) importedData.size();
            }
        });
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(importedData.size(), 2);
        checkLogger();
    }

    /**
     * Тестирование проверок НДФЛ
     * @throws IOException
     */
//    @Test
    public void checkDataTest() throws IOException {

        RefBookFactory refBookFactory = mock(RefBookFactory.class);

        // Провайдер стран
        RefBookDataProvider countryProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> pagingResultCountry = new PagingResult<Map<String, RefBookValue>>();
//        Map<String, RefBookValue> mapCountry = mock(Map.class);
        Map<String, RefBookValue> mapCountry = new HashMap<String, RefBookValue>();
//        when(mapCountry.get("CODE")).thenReturn(new RefBookValue(RefBookAttributeType.STRING, "512"));
        mapCountry.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "512"));
        pagingResultCountry.add(mapCountry);
        when(refBookFactory.getDataProvider(10L)).thenReturn(countryProvider);
        when(countryProvider.getRecords(any(Date.class), any(PagingParams.class), any(String.class), any(RefBookAttribute.class))).thenReturn(pagingResultCountry);

        // Данные о физическом лице - получателе дохода
        when(testHelper.getNdflPersonService().findNdflPerson(any(Long.class))).thenAnswer(new Answer<List<NdflPerson>>() {
            @Override
            public List<NdflPerson> answer(InvocationOnMock invocationOnMock) throws Throwable {
                NdflPerson ndflPerson1 = new NdflPerson();
                ndflPerson1.setCitizenship("512");

                List<NdflPerson> ndflPersonList = new ArrayList<NdflPerson>();
                ndflPersonList.add(ndflPerson1);
                return ndflPersonList;
            }
        });

        testHelper.execute(FormDataEvent.CHECK);
        checkLogger();
    }

    /**
     * Тест создания спецотчета "Спецотчет РНУ НДФЛ по физическому лицу"
     *
     * @throws IOException
     */
    @Test
    public void createSpecificReportTest() throws Exception {

        ScriptSpecificDeclarationDataReportHolder reportHolder = createReportHolder();
        reportHolder.getSubreportParamValues().put("lastName", "Иванов");
        testHelper.setScriptSpecificReportHolder(reportHolder);

        List<NdflPerson> result = new ArrayList<NdflPerson>();
        result.add(createGoodNdflPerson());
        PagingResult pagingResult = new PagingResult<NdflPerson>(result, 1);

        when(testHelper.getNdflPersonService().findNdflPersonByParameters(anyLong(), anyMap())).thenReturn(pagingResult);

        doAnswer(new Answer<JasperPrint>() {
            @Override
            public JasperPrint answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                InputStream jrxmlTemplate = (InputStream) args[0];
                Map param = (Map) args[1];
                Closure closure = (Closure) args[2];

                DeclarationService declarationService = new DeclarationServiceImpl();
                ByteArrayInputStream xmlData = declarationService.generateXmlData(closure);

                byte[] buf = new byte[xmlData.available()];
                xmlData.read(buf);

                //Проверяем сформированную xml
                checkXmlData(buf);

                xmlData = new ByteArrayInputStream(buf);

                DeclarationDataService declarationDataService = new DeclarationDataServiceImpl();
                Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
                return declarationDataService.createJasperReport(xmlData, jrxmlTemplate, param, connection);

            }
        }).when(testHelper.getDeclarationService()).createJasperReport(any(InputStream.class), anyMap(), any(Closure.class));

        Map<String, RefBookValue> periodCodeRefbookData = new HashMap<String, RefBookValue>();
        periodCodeRefbookData.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "21"));
        periodCodeRefbookData.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "первый квартал"));
        when(testHelper.getRefBookDataProvider().getRecordData(anyLong())).thenReturn(periodCodeRefbookData);

        Department department = new Department();
        department.setSbrfCode("18_0001_00");
        when(testHelper.getDepartmentService().get(anyInt())).thenReturn(department);


        testHelper.execute(FormDataEvent.CREATE_SPECIFIC_REPORT);


        ScriptSpecificDeclarationDataReportHolder resultReportHolder = testHelper.getScriptSpecificReportHolder();
        ByteArrayOutputStream outputStream = (ByteArrayOutputStream) resultReportHolder.getFileOutputStream();

        //Что-то сформировали
        Assert.assertTrue(outputStream.toByteArray().length > 0);

        //writeToFileAndOpen(outputStream, "person_report.pdf");

        checkLogger();
    }

    private void checkXmlData(byte[] buf) throws Exception {

        NdflPerson ndflPerson = createGoodNdflPerson();

        String xml = new String(buf, "UTF-8");

        //System.out.println(xml);

        String ndflPersonPath = "//*[local-name()='Файл']/*[local-name()='ИнфЧасть']/*[local-name()='ПолучДох']";

        Assert.assertEquals(ndflPerson.getInp(), xpath(xml, ndflPersonPath + "/@ИНП"));
        Assert.assertEquals(ndflPerson.getSnils(), xpath(xml, ndflPersonPath + "/@СНИЛС"));
        Assert.assertEquals(ndflPerson.getLastName(), xpath(xml, ndflPersonPath + "/@ФамФЛ"));
        Assert.assertEquals(ndflPerson.getFirstName(), xpath(xml, ndflPersonPath + "/@ИмяФЛ"));
        Assert.assertEquals(ndflPerson.getMiddleName(), xpath(xml, ndflPersonPath + "/@ОтчФЛ"));
        Assert.assertEquals(ndflPerson.getBirthDay(), parseDate(xpath(xml, ndflPersonPath + "/@ДатаРожд")));
        Assert.assertEquals(ndflPerson.getCitizenship(), xpath(xml, ndflPersonPath + "/@Гражд"));
        Assert.assertEquals(ndflPerson.getInnNp(), xpath(xml, ndflPersonPath + "/@ИННФЛ"));
        Assert.assertEquals(ndflPerson.getInnForeign(), xpath(xml, ndflPersonPath + "/@ИННИно"));
        Assert.assertEquals(ndflPerson.getIdDocType(), xpath(xml, ndflPersonPath + "/@УдЛичнФЛКод"));
        Assert.assertEquals(ndflPerson.getIdDocNumber(), xpath(xml, ndflPersonPath + "/@УдЛичнФЛНом"));
        Assert.assertEquals(ndflPerson.getStatus(), xpath(xml, ndflPersonPath + "/@СтатусФЛ"));
        Assert.assertEquals(ndflPerson.getPostIndex(), xpath(xml, ndflPersonPath + "/@Индекс"));
        Assert.assertEquals(ndflPerson.getRegionCode(), xpath(xml, ndflPersonPath + "/@КодРегион"));
        Assert.assertEquals(ndflPerson.getArea(), xpath(xml, ndflPersonPath + "/@Район"));
        Assert.assertEquals(ndflPerson.getCity(), xpath(xml, ndflPersonPath + "/@Город"));
        Assert.assertEquals(ndflPerson.getLocality(), xpath(xml, ndflPersonPath + "/@НаселПункт"));
        Assert.assertEquals(ndflPerson.getStreet(), xpath(xml, ndflPersonPath + "/@Улица"));
        Assert.assertEquals(ndflPerson.getHouse(), xpath(xml, ndflPersonPath + "/@Дом"));
        Assert.assertEquals(ndflPerson.getBuilding(), xpath(xml, ndflPersonPath + "/@Корпус"));
        Assert.assertEquals(ndflPerson.getFlat(), xpath(xml, ndflPersonPath + "/@Кварт"));
        Assert.assertEquals(ndflPerson.getCountryCode(), xpath(xml, ndflPersonPath + "/@КодСтрИно"));
        Assert.assertEquals(ndflPerson.getAddress(), xpath(xml, ndflPersonPath + "/@АдресИно"));
        Assert.assertEquals(ndflPerson.getAdditionalData(), xpath(xml, ndflPersonPath + "/@ДопИнф"));





    }

    public Date parseDate(String xmlDate) throws ParseException {
        return new java.text.SimpleDateFormat("dd.MM.yyyy").parse(xmlDate);
    }




    /**
     * Метод помогает при разработке. Сохраняет отчет в файл и открывает его для визуальной проверки.
     *
     * @param byteArrayOutputStream
     * @throws IOException
     */
    private void writeToFileAndOpen(ByteArrayOutputStream byteArrayOutputStream, String fileName) throws IOException {
        File file = new File("D:\\sbrf1\\" + fileName);
        OutputStream foutputStream = new FileOutputStream(file);
        try {
            byteArrayOutputStream.writeTo(foutputStream);
        } finally {
            foutputStream.close();
        }
        Desktop.getDesktop().open(file);
    }


    private NdflPerson createGoodNdflPerson() throws Exception {

        NdflPerson person = new NdflPerson();
        person.setId(null);
        person.setDeclarationDataId(1L);
        person.setInp("000-000-000-00");
        person.setSnils("123-321-111-11");
        person.setLastName("Иванов");
        person.setFirstName("Иван");
        person.setMiddleName("Иванович");
        person.setBirthDay(toDate("01.01.1980"));
        person.setCitizenship("643");

        person.setInnNp("123456789123");
        person.setInnForeign("");
        person.setIdDocType("11");
        person.setIdDocNumber("2002 123456");
        person.setStatus("1");
        person.setPostIndex("394000");
        person.setRegionCode("77");
        person.setArea("MSK");
        person.setCity("Москва");

        person.setLocality("Loc");
        person.setStreet("улица");
        person.setHouse("1A");
        person.setBuilding("123");
        person.setFlat("500");
        person.setCountryCode("643");
        person.setAddress("aaaaaaaa");
        person.setAdditionalData("eeeeee");


        List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<NdflPersonIncome>();
        ndflPersonIncomes.add(createNdflPersonIncomes(1, 11L));
        ndflPersonIncomes.add(createNdflPersonIncomes(2, 11L));
        ndflPersonIncomes.add(createNdflPersonIncomes(3, 11L));
        ndflPersonIncomes.add(createNdflPersonIncomes(6, 22L));
        ndflPersonIncomes.add(createNdflPersonIncomes(5, 22L));
        ndflPersonIncomes.add(createNdflPersonIncomes(4, 33L));
        ndflPersonIncomes.add(createNdflPersonIncomes(7, 22L));
        ndflPersonIncomes.add(createNdflPersonIncomes(9, 22L));
        ndflPersonIncomes.add(createNdflPersonIncomes(8, 22L));


        person.setIncomes(ndflPersonIncomes);

        List<NdflPersonDeduction> ndflPersonDeductions = new ArrayList<NdflPersonDeduction>();
        ndflPersonDeductions.add(createNdflPersonDeduction(1));
        ndflPersonDeductions.add(createNdflPersonDeduction(2));
        person.setDeductions(ndflPersonDeductions);

        List<NdflPersonPrepayment> ndflPersonPrepayments = new ArrayList<NdflPersonPrepayment>();
        ndflPersonPrepayments.add(createNdflPersonPrepayment(1));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(2));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(3));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(5));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(5));

        person.setPrepayments(ndflPersonPrepayments);

        return person;
    }

    private NdflPersonIncome createNdflPersonIncomes(int row, long operationId) throws Exception {
        NdflPersonIncome personIncome = new NdflPersonIncome();
        personIncome.setRowNum(row);
        personIncome.setOperationId(11111L);
        personIncome.setOktmo("oktmo111");
        personIncome.setKpp("kpp111");
        personIncome.setIncomeAccruedDate(parseDate("01.01.2017"));
        personIncome.setPaymentNumber("aaaaaaaa"+row);
        personIncome.setTaxSumm(122222);
        personIncome.setOperationId(operationId);
        return personIncome;
    }

    private NdflPersonDeduction createNdflPersonDeduction(int row) {
        NdflPersonDeduction personDeduction = new NdflPersonDeduction();
        personDeduction.setRowNum(row);
        personDeduction.setOperationId(11111L);
        personDeduction.setTypeCode("001");

        personDeduction.setNotifType("11");
        personDeduction.setNotifDate(toDate("01.01.1980"));
        personDeduction.setNotifNum("notif_num");
        personDeduction.setNotifSource("notif_source");
        personDeduction.setNotifSumm(new BigDecimal("999999.99"));

        personDeduction.setIncomeAccrued(toDate("01.01.2016"));
        personDeduction.setIncomeCode("1234");
        personDeduction.setIncomeSumm(new BigDecimal("999999.99")); //123456789123456789.12

        personDeduction.setPeriodPrevDate(toDate("01.01.2016"));
        personDeduction.setPeriodPrevSumm(new BigDecimal("999999.99")); //123456789123456789.12
        personDeduction.setPeriodCurrDate(toDate("01.01.2016"));
        personDeduction.setPeriodCurrSumm(new BigDecimal("999999.99"));


        return personDeduction;
    }

    private NdflPersonPrepayment createNdflPersonPrepayment(int row) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
        personPrepayment.setRowNum(row);
        personPrepayment.setOperationId(11111L);
        personPrepayment.setSumm(1999999L); //по xsd это поле xs:integer
        personPrepayment.setNotifNum("123-456-000");
        personPrepayment.setNotifDate(toDate("01.01.2016"));
        personPrepayment.setNotifSource("AAA");
        return personPrepayment;
    }


}
