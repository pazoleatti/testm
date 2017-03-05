package com.aplana.sbrf.taxaccounting.form_template.ndfl.report_2ndfl_1.v2016;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

import static junit.framework.TestCase.assertNotNull;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@Ignore
public class Ndfl2ScriptTest extends DeclarationScriptTestBase {

    private static final int DEPARTMENT_ID = 1254;
    private static final int DECLARATION_TEMPLATE_ID = 1;
    private static final int REPORT_PERIOD_ID = 2;
    private static final int DEPARTMENT_PERIOD_ID = 3;
    private static final long REF_BOOK_NDFL_DETAIL_ID = 951L;
    private static final long REB_BOOK_FORM_TYPE_ID = 931L;
    private  static final long REF_BOOK_DEDUCTION_TYPE_ID = 921L;
    private static final long REF_BOOK_DEDUCTION_MARK_ID = 927L;
    private static final long REF_BOOK_OKTMO_ID = 96L;
    private static final long REF_BOOK_SIGNATORY_MARK_ID = 35L;

    private DeclarationData declarationData = new DeclarationData();

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Ndfl2ScriptTest.class);
    }

    @Override
    protected DeclarationData getDeclarationData() {
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setReportPeriodId(REPORT_PERIOD_ID);
        declarationData.setDepartmentId(DEPARTMENT_ID);
        declarationData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);
        declarationData.setOktmo("11223344");
        return declarationData;
    }

    @Before
    public void init() {
        String path = getFolderPath();
        if (path == null) {
            throw new ServiceException("Test folder path is null!");
        }
        testHelper = new DeclarationTestScriptHelper(path, getDeclarationData(), getMockHelper());

        testHelper.reset();
    }


    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
    }

    @Test
    public void validation_test() throws IOException, SAXException, XpathException {
        Calendar calTax = Calendar.getInstance();
        calTax.set(2014, 5, 1);

        Calendar calPayment = Calendar.getInstance();
        calPayment.set(2014, 6, 1);
        List<NdflPerson> ndflPersons = createNdflPersonMocks(calTax.getTime(), calPayment.getTime());

        when(testHelper.getNdflPersonService().findNdflPersonByParameters(anyLong(), any(Map.class), anyInt(), anyInt())).thenReturn(new PagingResult<NdflPerson>(ndflPersons));

        initNdflRefBook(1, "Сбербанк", "+74955555555");
        initOktmoRefBook();
        initFormDataRefBook("2 НДФЛ (1)");
        initOther();

        testHelper.execute(FormDataEvent.CALCULATE);
        assertNotNull(testHelper.getXmlStringWriter());
        //assertTrue(validateResultBySchema("<?xml version='1.0' encoding='utf-8'?>\n" + testHelper.getXmlStringWriter().toString())); //TODO добавить новую xsd
        assertXpathExists("/Файл/СвРекв[@ОКТМО='11223344']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/СвРекв[@ПризнакФ='1']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/СвРекв/СвЮЛ[@КПП='110101001']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ[@КодНО='1684']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/СвНА[@Тлф='+74955555555']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох[@ИННФЛ='770111111111']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох[@ИННИно='111111111111']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох[@Статус='6']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох[@ДатаРожд='01.01.1985']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох[@Гражд='643']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/ФИО[@Фамилия='Петров']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/ФИО[@Имя='Петр']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/ФИО[@Отчество='Петрович']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/УдЛичнФЛ[@КодУдЛичн='21']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/УдЛичнФЛ[@СерНомДок='1234 5678']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@Индекс='111222']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@КодРегион='97']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@Район='Приволжский']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@Город='Москва']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@Улица='Ленина']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@Дом='1']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@Корпус='2']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрМЖРФ[@Кварт='3']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрИНО[@КодСтр='840']", testHelper.getXmlStringWriter().toString());
        assertXpathExists("/Файл/Документ/ПолучДох/АдрИНО[@АдрТекст='Мэдисон авеню д.10']", testHelper.getXmlStringWriter().toString());
        checkLogger();
    }

    @Test
    public void check_selection_when_priznakF_equals_1() throws IOException, SAXException, XpathException {
        Calendar calTax = Calendar.getInstance();
        calTax.set(2015, 5, 1);
        Calendar calPayment = Calendar.getInstance();
        calPayment.set(2015, 6, 1);
        List<NdflPerson> ndflPersons = createNdflPersonMocks(calTax.getTime(), calPayment.getTime());

        when(testHelper.getNdflPersonService().findNdflPersonByParameters(anyLong(), any(Map.class), anyInt(), anyInt())).thenReturn(new PagingResult<NdflPerson>(ndflPersons));

        initNdflRefBook(1, "Сбербанк", "+74955555555");
        initOktmoRefBook();
        initFormDataRefBook("2 НДФЛ (1)");
        initOther();

        testHelper.execute(FormDataEvent.CALCULATE);
        assertNotNull(testHelper.getXmlStringWriter());
        assertXpathNotExists("/Файл/Документ/Подписант/СведДох/ДохВыч", testHelper.getXmlStringWriter().toString());
    }

    @Test
    public void check_selection_when_priznakF_equals_2() throws IOException, SAXException, XpathException {
        Calendar calTax = Calendar.getInstance();
        calTax.set(2015, 5, 1);
        Calendar calPayment = Calendar.getInstance();
        calPayment.set(2015, 6, 1);
        List<NdflPerson> ndflPersons = createNdflPersonMocks(calTax.getTime(), calPayment.getTime());

        when(testHelper.getNdflPersonService().findNdflPersonByParameters(anyLong(), any(Map.class), anyInt(), anyInt())).thenReturn(new PagingResult<NdflPerson>(ndflPersons));

        initNdflRefBook(1, "Сбербанк", "+74955555555");
        initOktmoRefBook();
        initFormDataRefBook("2 НДФЛ (2)");
        initOther();

        testHelper.execute(FormDataEvent.CALCULATE);
        assertNotNull(testHelper.getXmlStringWriter());
        assertXpathNotExists("/Файл/Документ/Подписант/СведДох/ДохВыч", testHelper.getXmlStringWriter().toString());
    }

    private void initNdflRefBook(int signatoryId, String approveOrgName, String phone) {
        RefBookDataProvider ndflRefBookDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> ndflPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> ndflPagingResultItem = new HashMap<String, RefBookValue>();
        when(ndflRefBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(ndflPagingResult);
        ndflPagingResultItem.put("OKTMO", new RefBookValue(RefBookAttributeType.REFERENCE, 123L));
        ndflPagingResultItem.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "110101001"));
        ndflPagingResultItem.put("TAX_ORGAN_CODE", new RefBookValue(RefBookAttributeType.STRING, "1684"));
        ndflPagingResultItem.put("SIGNATORY_FIRSTNAME", new RefBookValue(RefBookAttributeType.STRING, "Иванов"));
        ndflPagingResultItem.put("SIGNATORY_SURNAME", new RefBookValue(RefBookAttributeType.STRING, "Иван"));
        ndflPagingResultItem.put("SIGNATORY_LASTNAME", new RefBookValue(RefBookAttributeType.STRING, "Иванович"));
        ndflPagingResultItem.put("APPROVE_DOC_NAME", new RefBookValue(RefBookAttributeType.STRING, "Доверенность"));
        ndflPagingResultItem.put("APPROVE_ORG_NAME", new RefBookValue(RefBookAttributeType.STRING, approveOrgName));
        ndflPagingResultItem.put("PHONE", new RefBookValue(RefBookAttributeType.STRING, phone));
        ndflPagingResultItem.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "Сбербанк"));
        ndflPagingResultItem.put("SIGNATORY_ID", new RefBookValue(RefBookAttributeType.NUMBER, signatoryId));
        ndflPagingResult.add(ndflPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_NDFL_DETAIL_ID)).thenReturn(ndflRefBookDataProvider);
    }

    private void initOktmoRefBook() {
        RefBookDataProvider oktmoRefBookDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> oktmoPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> oktmoPagingResultItem = new HashMap<String, RefBookValue>();
        when(oktmoRefBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(oktmoPagingResult);
        oktmoPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "11223344"));
        oktmoPagingResultItem.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "NoNameCity"));
        oktmoPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 123L));
        oktmoPagingResult.add(oktmoPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_OKTMO_ID)).thenReturn(oktmoRefBookDataProvider);
    }

    private void initFormDataRefBook(String code) {
        RefBookDataProvider formTypeRefBookDataProvider = mock(RefBookDataProvider.class);
        Map<String, RefBookValue> formTypeResult = new HashMap<String, RefBookValue>();
        when(formTypeRefBookDataProvider.getRecordData(anyLong())).thenReturn(formTypeResult);
        formTypeResult.put("CODE", new RefBookValue(RefBookAttributeType.STRING, code));
        when(testHelper.getRefBookFactory().getDataProvider(REB_BOOK_FORM_TYPE_ID)).thenReturn(formTypeRefBookDataProvider);

    }

    private void initOther() {

        when(testHelper.getDeclarationService().getTemplate(anyInt())).thenAnswer(new Answer<DeclarationTemplate>() {
            @Override
            public DeclarationTemplate answer(InvocationOnMock invocation) throws Throwable {
                DeclarationTemplate declarationTemplate = mock(DeclarationTemplate.class);
                when(declarationTemplate.getDeclarationFormTypeId()).thenReturn(1L);
                return declarationTemplate;
            }
        });

        RefBookDataProvider deductionTypeRefBookDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> deduction5TypePagingResult = new PagingResult<Map<String, RefBookValue>>();
        when(deductionTypeRefBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), eq("CODE = '501'"), any(RefBookAttribute.class))).thenReturn(deduction5TypePagingResult);
        Map<String, RefBookValue> deductionType5PagingResultItem = new HashMap<String, RefBookValue>();
        deductionType5PagingResultItem.put("DEDUCTION_MARK", new RefBookValue(RefBookAttributeType.NUMBER, 5));
        deduction5TypePagingResult.add(deductionType5PagingResultItem);
        PagingResult<Map<String, RefBookValue>> deduction1TypePagingResult = new PagingResult<Map<String, RefBookValue>>();
        when(deductionTypeRefBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), eq("CODE = '101'"), any(RefBookAttribute.class))).thenReturn(deduction1TypePagingResult);
        Map<String, RefBookValue> deductionType1PagingResultItem = new HashMap<String, RefBookValue>();
        deductionType1PagingResultItem.put("DEDUCTION_MARK", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        deduction1TypePagingResult.add(deductionType1PagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_DEDUCTION_TYPE_ID)).thenReturn(deductionTypeRefBookDataProvider);

        RefBookDataProvider deductionMarkRefBookDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> deduction5MarkPagingResult = new PagingResult<Map<String, RefBookValue>>();

        Map<String, RefBookValue> deductionMark5PagingResultItem = new HashMap<String, RefBookValue>();

        when(deductionMarkRefBookDataProvider.getRecordData(5L)).thenReturn(deductionMark5PagingResultItem);
        deductionMark5PagingResultItem.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "Остальные"));
        deduction5MarkPagingResult.add(deductionMark5PagingResultItem);
        PagingResult<Map<String, RefBookValue>> deduction1MarkPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> deductionMark1PagingResultItem = new HashMap<String, RefBookValue>();
        when(deductionMarkRefBookDataProvider.getRecordData(1L)).thenReturn(deductionMark1PagingResultItem);

        deductionMark1PagingResultItem.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "Имущественный"));
        deduction1MarkPagingResult.add(deductionMark1PagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_DEDUCTION_MARK_ID)).thenReturn(deductionMarkRefBookDataProvider);

        RefBookDataProvider signatoryMarkRefBookDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> signatoryMarkPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> signatoryMarkPagingResultItem = new HashMap<String, RefBookValue>();
        when(signatoryMarkRefBookDataProvider.getRecordData(anyLong())).thenReturn(signatoryMarkPagingResultItem);
        signatoryMarkPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "1"));
        signatoryMarkPagingResult.add(signatoryMarkPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_SIGNATORY_MARK_ID)).thenReturn(signatoryMarkRefBookDataProvider);
    }

    private boolean validateResultBySchema(String xml) throws SAXException, UnsupportedEncodingException {
        InputSource is = new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));

        Validator v = new Validator(is);
        v.useXMLSchema(true);
        v.setJAXP12SchemaSource(Ndfl2ScriptTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/ndfl/report_2ndfl_1/v2016/schema.xsd"));
        return v.isValid();
    }

    private List<NdflPerson> createNdflPersonMocks(Date taxDate, Date paymentDate) {
        List<NdflPerson> ndflPersonList = new ArrayList<NdflPerson>();
        NdflPerson ndflPerson = null;
        int taxrate = 13;
        for (int i = 0; i <2; i++) {
            ndflPerson = new NdflPerson();
            ndflPerson.setId(1L);
            ndflPerson.setInnNp("770111111111");
            ndflPerson.setInnForeign("111111111111");
            ndflPerson.setStatus("6");

            Calendar cal = Calendar.getInstance();
            cal.set(1985, 0, 1);
            ndflPerson.setBirthDay(cal.getTime());

            ndflPerson.setCitizenship("643");

            ndflPerson.setLastName("Петров");
            ndflPerson.setFirstName("Петр");
            ndflPerson.setMiddleName("Петрович");

            ndflPerson.setIdDocType("21");
            ndflPerson.setIdDocNumber("1234 5678");

            ndflPerson.setPostIndex("111222");
            ndflPerson.setRegionCode("97");
            ndflPerson.setArea("Приволжский");
            ndflPerson.setCity("Москва");
            ndflPerson.setLocality(null);
            ndflPerson.setStreet("Ленина");
            ndflPerson.setHouse("1");
            ndflPerson.setBuilding("2");
            ndflPerson.setFlat("3");

            ndflPerson.setCountryCode("840");
            ndflPerson.setAddress("Мэдисон авеню д.10");
            //
            NdflPersonIncome ndflPersonIncome1 = new NdflPersonIncome();
            ndflPersonIncome1.setTaxRate(13);
            ndflPersonIncome1.setIncomeAccruedSumm(BigDecimal.valueOf(100000L));
            ndflPersonIncome1.setNotHoldingTax(50000L);
            ndflPersonIncome1.setOperationId(1L);
            ndflPersonIncome1.setTaxBase(BigDecimal.valueOf(5000L));
            ndflPersonIncome1.setCalculatedTax(6000L);
            ndflPersonIncome1.setWithholdingTax(7000L);
            ndflPersonIncome1.setTaxSumm(8000);
            ndflPersonIncome1.setOverholdingTax(9000L);
            ndflPersonIncome1.setNotHoldingTax(4000L);

            Calendar calAccrued = Calendar.getInstance();
            calAccrued.set(2014, 4, 1);
            ndflPersonIncome1.setIncomeAccruedDate(calAccrued.getTime());

            ndflPersonIncome1.setTaxDate(taxDate);
            ndflPersonIncome1.setPaymentDate(paymentDate);

            ndflPersonIncome1.setIncomeCode("5011");

            NdflPersonIncome ndflPersonIncome2 = new NdflPersonIncome();
            ndflPersonIncome2.setTaxRate(taxrate);
            ndflPersonIncome2.setIncomeAccruedSumm(BigDecimal.valueOf(100000L));
            ndflPersonIncome2.setNotHoldingTax(50000L);
            ndflPersonIncome2.setOperationId(1L);
            ndflPersonIncome2.setTaxBase(BigDecimal.valueOf(5000L));
            ndflPersonIncome2.setCalculatedTax(6000L);
            ndflPersonIncome2.setWithholdingTax(7000L);
            ndflPersonIncome2.setTaxSumm(8000);
            ndflPersonIncome2.setOverholdingTax(9000L);
            ndflPersonIncome2.setNotHoldingTax(4000L);

            Calendar calAccrued2 = Calendar.getInstance();
            calAccrued2.set(2014, 5, 1);
            ndflPersonIncome2.setIncomeAccruedDate(calAccrued2.getTime());

            ndflPersonIncome2.setTaxDate(taxDate);
            ndflPersonIncome2.setPaymentDate(paymentDate);

            ndflPersonIncome2.setIncomeCode("5011");

            ndflPerson.getIncomes().add(ndflPersonIncome1);
            ndflPerson.getIncomes().add(ndflPersonIncome2);


            NdflPersonDeduction ndflPersonDeduction1 = new NdflPersonDeduction();
            ndflPersonDeduction1.setOperationId(1L);
            ndflPersonDeduction1.setIncomeAccrued(calAccrued.getTime());
            ndflPersonDeduction1.setTypeCode("501");
            ndflPersonDeduction1.setPeriodCurrDate(ndflPersonIncome1.getTaxDate());
            ndflPersonDeduction1.setPeriodCurrSumm(BigDecimal.valueOf(100000));
            ndflPersonDeduction1.setIncomeCode("5011");

            ndflPerson.getDeductions().add(ndflPersonDeduction1);

            NdflPersonDeduction ndflPersonDeduction2 = new NdflPersonDeduction();
            ndflPersonDeduction2.setOperationId(1L);
            ndflPersonDeduction2.setIncomeAccrued(calAccrued.getTime());
            ndflPersonDeduction2.setTypeCode("101");
            ndflPersonDeduction2.setPeriodCurrDate(ndflPersonIncome1.getTaxDate());
            ndflPersonDeduction2.setPeriodCurrSumm(BigDecimal.valueOf(200000));
            ndflPersonDeduction2.setNotifNum("123");
            ndflPersonDeduction2.setNotifDate(paymentDate);
            ndflPersonDeduction2.setNotifSource("1684");
            ndflPersonDeduction2.setIncomeCode("5011");
            ndflPerson.getDeductions().add(ndflPersonDeduction2);

            NdflPersonPrepayment ndflPersonPrepayment = new NdflPersonPrepayment();
            ndflPersonPrepayment.setNotifNum("123");
            ndflPersonPrepayment.setNotifDate(ndflPersonIncome1.getTaxDate());
            ndflPersonPrepayment.setNotifSource("1684");
            ndflPersonPrepayment.setSumm(1000L);
            ndflPersonPrepayment.setOperationId(1L);

            ndflPerson.getPrepayments().add(ndflPersonPrepayment);

            ndflPersonList.add(ndflPerson);
            taxrate = ++taxrate;
        }


        //when(testHelper.getNdflPersonService().findIncomesByPeriodAndNdflPersonId(anyLong(), any(Date.class), any(Date.class))).thenReturn(ndflPerson.getIncomes());
        //when(testHelper.getNdflPersonService().findDeductionsByPeriodAndNdflPersonId(anyLong(), any(Date.class), any(Date.class))).thenReturn(ndflPerson.getDeductions());
        when(testHelper.getNdflPersonService().findPrepaymentsByPeriodAndNdflPersonId(anyLong(), any(Date.class), any(Date.class))).thenReturn(ndflPerson.getPrepayments());
        return ndflPersonList;
    }

}
