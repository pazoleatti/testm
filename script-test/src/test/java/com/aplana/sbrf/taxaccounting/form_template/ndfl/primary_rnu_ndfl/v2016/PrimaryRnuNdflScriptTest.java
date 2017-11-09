package com.aplana.sbrf.taxaccounting.form_template.ndfl.primary_rnu_ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.identification.AttributeChangeEvent;
import com.aplana.sbrf.taxaccounting.model.identification.AttributeChangeEventType;
import com.aplana.sbrf.taxaccounting.model.identification.AttributeCountChangeListener;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
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
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.script.service.impl.DeclarationServiceImpl;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.util.*;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.util.TestUtils.toDate;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Drunk
 */
public class PrimaryRnuNdflScriptTest extends DeclarationScriptTestBase {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PrimaryRnuNdflScriptTest.class.getName());

    private static final int DEPARTMENT_ID = 1;
    private static final int DECLARATION_TEMPLATE_ID = 1022;
    private static final int REPORT_PERIOD_ID = 3489;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final long ASNU_ID = 1000;
    private static final String KPP = "123456789";
    private static final String CODE_ORG = "0123456789";
    private static final String REPORT_PERSON_NAME = "report_person";
    private static final String TEST_FILE_NAME = "_______18_0001_001000212016ecf863ca-6349-4105-b1e1-33c1good.xml";

    @Override
    protected DeclarationData getDeclarationData() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setDepartmentId(DEPARTMENT_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);
        declarationData.setReportPeriodId(REPORT_PERIOD_ID);
        declarationData.setState(State.ACCEPTED);
        declarationData.setAsnuId(ASNU_ID);
        declarationData.setDepartmentReportPeriodId((long) DEPARTMENT_PERIOD_ID);
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
        return PrimaryRnuNdflScriptTest.class.getResourceAsStream(TEST_FILE_NAME);
    }

    private String getTestFileName(){
        return PrimaryRnuNdflScriptTest.class.getResource(TEST_FILE_NAME).getFile();
    }


//    @Test
    public void importDataTest() throws IOException {
        testHelper.setDataFile(new File(getTestFileName()));
        RefBookDataProvider refBookDataProviderIncomeCode = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(RefBook.Id.INCOME_CODE.getId())).thenReturn(refBookDataProviderIncomeCode);
        RefBookDataProvider refBookDataProviderDeductionType = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(RefBook.Id.DEDUCTION_TYPE.getId())).thenReturn(refBookDataProviderDeductionType);

        PagingResult<Map<String, RefBookValue>> recordVersionIncomeCode  = new PagingResult<Map<String, RefBookValue>>();
        recordVersionIncomeCode.add(new HashMap<String, RefBookValue>(){{
            put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, 1));
            put("CODE", new RefBookValue(RefBookAttributeType.STRING, "1000"));
            put(RefBook.RECORD_VERSION_FROM_ALIAS, new RefBookValue(RefBookAttributeType.DATE, new Date()));
            put(RefBook.RECORD_VERSION_TO_ALIAS, new RefBookValue(RefBookAttributeType.DATE, new Date()));
        }});
        when(refBookDataProviderIncomeCode.getRecordsVersion(any(Date.class), any(Date.class), any(PagingParams.class), anyString())).thenReturn(recordVersionIncomeCode);

        PagingResult<Map<String, RefBookValue>> recordVersionDeductionType = new PagingResult<Map<String, RefBookValue>>();
        recordVersionDeductionType.add(new HashMap<String, RefBookValue>(){{
            put("CODE", new RefBookValue(RefBookAttributeType.STRING, "1000"));
        }});
        when(refBookDataProviderDeductionType.getRecordsVersion(any(Date.class), any(Date.class), any(PagingParams.class), anyString())).thenReturn(recordVersionDeductionType );

        final List<NdflPerson> importedData = new ArrayList<NdflPerson>();
        when(testHelper.getNdflPersonService().save(any(NdflPerson.class))).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                NdflPerson ndflPerson = (NdflPerson) args[0];
                LOGGER.info("save ndflPerson: " + ndflPerson + " [" + ndflPerson.getFirstName() + "," + ndflPerson.getLastName() + "]");
                importedData.add(ndflPerson);
                return (long) importedData.size();
            }
        });
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);
        Assert.assertEquals(2, importedData.size());
        checkLogger();
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
        result.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "foo" + id));
        result.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "bar" + id));
        result.put("ADDRESS", new RefBookValue(RefBookAttributeType.REFERENCE, Long.valueOf(new Random().nextInt(1000))));
        return result;
    }

    private PagingResult<Map<String, RefBookValue>> getCountryRefBook() {
        PagingResult<Map<String, RefBookValue>> pagingResult = new PagingResult<Map<String, RefBookValue>>();
        for (int i = 0; i < 8; i++) {
            Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
            values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.REFERENCE, Long.valueOf(i)));
            values.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "000" + i));
            pagingResult.add(values);
        }
        return pagingResult;
    }

    /**
     * Тест расчета данных декларации
     *
     * @throws IOException
     */

    @Test
	@Ignore
    public void calculateTest() throws IOException {
        final int ndflPersonSize = 5;

        final Map<Long, NdflPerson> ndflPersonMap = mockFindNdflPerson(ndflPersonSize);
        when(testHelper.getNdflPersonService().findNdflPerson(any(Long.class))).thenReturn(new ArrayList<NdflPerson>(ndflPersonMap.values()));

        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), any(String.class), any(RefBookAttribute.class))).thenReturn(getCountryRefBook());

        when(testHelper.getRefBookDataProvider().getRecordData(anyList())).thenReturn(createRefBook());

        when(testHelper.getRefBookPersonService().identificatePerson(any(PersonData.class), anyList(), anyInt(), any(Logger.class))).thenReturn(null).thenReturn(null);
        //.thenReturn(1L).thenReturn(2L).thenReturn(3L);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                //System.out.println("UPDATE REF BOOK " + args);
                return null;
            }
        }).when(testHelper.getRefBookDataProvider()).updateRecordVersionWithoutLock(any(Logger.class), anyLong(), any(Date.class), any(Date.class), anyMap());

        when(testHelper.getRefBookDataProvider().createRecordVersionWithoutLock(any(Logger.class), any(Date.class), any(Date.class), anyList())).thenAnswer(new Answer<List<Long>>() {
            @Override
            public List<Long> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                //System.out.println("SAVE REF BOOK " + args);
                return new ArrayList<Long>(ndflPersonMap.keySet());
            }
        });

        when(testHelper.getNdflPersonService().updateRefBookPersonReferences(anyList())).thenAnswer(new Answer<int[]>() {
            @Override
            public int[] answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                List<NaturalPerson> personData = (List<NaturalPerson>) args[0];
                for (NaturalPerson person : personData) {
                    Assert.assertNotNull(person.getId());
                    Assert.assertNotNull(person.getPrimaryPersonId());
                }
                return new int[]{};
            }
        });


        testHelper.execute(FormDataEvent.CALCULATE);

        Assert.assertTrue((Boolean) testHelper.getCalculateParams().get(DeclarationDataScriptParams.NOT_REPLACE_XML));

        checkLogger();
    }

    private Map<Long, NdflPerson> mockFindNdflPerson(int size) {
        Map<Long, NdflPerson> result = new HashMap<Long, NdflPerson>();
        for (int i = 0; i < size; i++) {
            result.put(Long.valueOf(i), createGoodNdflPerson(Long.valueOf(i)));
        }
        return result;
    }


    /**
     * Тестирование проверок НДФЛ
     *
     * @throws IOException
     */
//    @Test
    public void checkDataTest() throws IOException {

        RefBookFactory refBookFactory = mock(RefBookFactory.class);

        // Провайдер стран
//        RefBookDataProvider countryProvider = mock(RefBookDataProvider.class);
//        when(testHelper.getRefBookFactory().getDataProvider(any(Long.class))).thenReturn(countryProvider);
//        PagingResult<Map<String, RefBookValue>> pagingResultCountry = new PagingResult<Map<String, RefBookValue>>();
//        Map<String, RefBookValue> mapCountry = new HashMap<String, RefBookValue>();
//        mapCountry.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "512"));
////        Map<String, RefBookValue> mapCountry = mock(Map.class);
////        when(mapCountry.get("CODE")).thenReturn(new RefBookValue(RefBookAttributeType.STRING, "512"));
//        pagingResultCountry.add(mapCountry);
//        when(testHelper.getRefBookDataProvider().getRecords(any(Date.class), any(PagingParams.class), any(String.class), any(RefBookAttribute.class))).thenReturn(pagingResultCountry);

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


    //@Test
    public void createSpecificReportTest() throws Exception {
        ScriptSpecificDeclarationDataReportHolder reportHolder = createReportHolder();
        reportHolder.getSubreportParamValues().put(SubreportAliasConstants.LAST_NAME, "Иванов");
        testHelper.setScriptSpecificReportHolder(reportHolder);
        testHelper.execute(FormDataEvent.CREATE_SPECIFIC_REPORT);
    }


    /**
     * Тест создания спецотчета "Спецотчет РНУ НДФЛ по физическому лицу"
     *
     * @throws IOException
     */
    //@Test
    public void createSpecificReportTestFull() throws Exception {

        ScriptSpecificDeclarationDataReportHolder reportHolder = createReportHolder();
        reportHolder.getSubreportParamValues().put(SubreportAliasConstants.LAST_NAME, "Иванов");
        testHelper.setScriptSpecificReportHolder(reportHolder);

        List<NdflPerson> result = new ArrayList<NdflPerson>();
        result.add(createGoodNdflPerson());
        PagingResult pagingResult = new PagingResult<NdflPerson>(result, 1);

        when(testHelper.getNdflPersonService().findNdflPersonByParameters(anyLong(), anyMap())).thenReturn(pagingResult);

        when(testHelper.getNdflPersonService().get(anyLong())).thenReturn(result.get(0));

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

    @Test
    public void attrChangeListenerTest(){

        AttributeCountChangeListener attrChangeListener = new AttributeCountChangeListener();

        AttributeChangeEvent changeEvent = new AttributeChangeEvent("INC_REP", 1);
        changeEvent.setType(AttributeChangeEventType.REFRESHED);

        RefBookValue refBookValue = new RefBookValue(RefBookAttributeType.NUMBER, 0);
        changeEvent.setCurrentValue(refBookValue);

        attrChangeListener.processAttr(changeEvent);

        Assert.assertNotNull(attrChangeListener.getMessages().get("INC_REP"));
        Assert.assertTrue(attrChangeListener.isUpdate());
        Assert.assertEquals(attrChangeListener.getMessages().get("INC_REP"), "0->1");

        //System.out.println(attrChangeListener.getMessages().get("INC_REP"));

    }


    public Date parseDate(String xmlDate) {
        try {
            return new java.text.SimpleDateFormat("dd.MM.yyyy").parse(xmlDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
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

    private NdflPerson createGoodNdflPerson(Long id) {
        NdflPerson person = new NdflPerson();
        person.setId(id);
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
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(1), "11"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(2), "11"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(3), "11"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(6), "22"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(5), "22"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(4), "33"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(7), "22"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(9), "22"));
        ndflPersonIncomes.add(createNdflPersonIncomes(new BigDecimal(8), "22"));

        person.setIncomes(ndflPersonIncomes);

        List<NdflPersonDeduction> ndflPersonDeductions = new ArrayList<NdflPersonDeduction>();
        ndflPersonDeductions.add(createNdflPersonDeduction(new BigDecimal(1)));
        ndflPersonDeductions.add(createNdflPersonDeduction(new BigDecimal(2)));
        person.setDeductions(ndflPersonDeductions);

        List<NdflPersonPrepayment> ndflPersonPrepayments = new ArrayList<NdflPersonPrepayment>();
        ndflPersonPrepayments.add(createNdflPersonPrepayment(new BigDecimal(1)));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(new BigDecimal(2)));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(new BigDecimal(3)));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(new BigDecimal(5)));
        ndflPersonPrepayments.add(createNdflPersonPrepayment(new BigDecimal(5)));

        person.setPrepayments(ndflPersonPrepayments);

        return person;
    }

    private NdflPerson createGoodNdflPerson() throws Exception {
        return createGoodNdflPerson(null);
    }

    private NdflPersonIncome createNdflPersonIncomes(BigDecimal row, String operationId) {
        NdflPersonIncome personIncome = new NdflPersonIncome();
        personIncome.setRowNum(row);
        personIncome.setOperationId("11111");
        personIncome.setOktmo("oktmo111");
        personIncome.setKpp("kpp111");
        personIncome.setIncomeAccruedDate(toDate("01.01.2017"));
        personIncome.setPaymentNumber("aaaaaaaa" + row);
        personIncome.setTaxSumm(122222L);
        personIncome.setOperationId(operationId);
        return personIncome;
    }

    private NdflPersonDeduction createNdflPersonDeduction(BigDecimal row) {
        NdflPersonDeduction personDeduction = new NdflPersonDeduction();
        personDeduction.setRowNum(row);
        personDeduction.setOperationId("11111");
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

    private NdflPersonPrepayment createNdflPersonPrepayment(BigDecimal row) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
        personPrepayment.setRowNum(row);
        personPrepayment.setOperationId("11111");
        personPrepayment.setSumm(new BigDecimal(1999999)); //по xsd это поле xs:integer
        personPrepayment.setNotifNum("123-456-000");
        personPrepayment.setNotifDate(toDate("01.01.2016"));
        personPrepayment.setNotifSource("AAA");
        return personPrepayment;
    }


//    class NdflPersonDataFactory {
//        public NdflPerson getNdflPerson(){
//            return ...;
//        }
//    }

}
