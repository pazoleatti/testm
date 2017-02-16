package com.aplana.sbrf.taxaccounting.form_template.ndfl.consolidated_rnu_ndfl.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.lang3.builder.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.ParseException;
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
    private static final int DECLARATION_TEMPLATE_ID = 101;
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

    private  List<Department> createDepartmentList(){
        List<Department> departmentList = new ArrayList<Department>();
        Department department = new Department();
        department.setFullName("Test department");
        department.setId(123);
        departmentList.add(department);
        return departmentList;
    }


    private  List<ReportPeriod> createReportPeriodList(){
        List<ReportPeriod> reportPeriodList = new ArrayList<ReportPeriod>();
        reportPeriodList.add(createReportPeriod(1, "первый квартал"));
        reportPeriodList.add(createReportPeriod(2, "полугодие"));
        return reportPeriodList;
    }

    private ReportPeriod createReportPeriod(int id, String name){
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setName(name);
        reportPeriod.setId(id);
        reportPeriod.setStartDate(parseDate("01.01.2016"));
        reportPeriod.setEndDate(parseDate("31.12.2016"));

        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2016);
        reportPeriod.setTaxPeriod(taxPeriod);

        return reportPeriod;
    }

    private List<DeclarationData> createFirstQuarterDeclarationData(){
        List<DeclarationData> result = new ArrayList<DeclarationData>();
        result.add(createDeclarationData(11L, 100L, 1));
        result.add(createDeclarationData(111L, 100L, 1));
        result.add(createDeclarationData(22L, 100L, 2));
        result.add(createDeclarationData(222L, 100L, 2));
        result.add(createDeclarationData(2223L, 100L, 4));
        result.add(createDeclarationData(22234L, 100L, 4));
        result.add(createDeclarationData(22235L, 100L, 4));

        result.add(createDeclarationData(33L, 101L, 1));
        result.add(createDeclarationData(333L, 101L, 1));
        result.add(createDeclarationData(44L, 101L, 2));

        result.add(createDeclarationData(55L, 102L, 1));
        result.add(createDeclarationData(66L, 102L, 2));
        result.add(createDeclarationData(77L, 102L, 3));
        return result;
    }

    private List<DeclarationData> createHalfYearDeclarationData(){
        List<DeclarationData> result = new ArrayList<DeclarationData>();
        result.add(createDeclarationData(88L, 100L, 1));
        result.add(createDeclarationData(99L, 100L, 1));
        return result;
    }

    public DepartmentReportPeriod createDepartmentReportPeriod(Integer id, String date) {
        DepartmentReportPeriod d = new DepartmentReportPeriod();
        d.setId(id);
        if (date != null) {
            d.setCorrectionDate(parseDate(date));
        }

        ReportPeriod rp = new ReportPeriod();
        rp.setId(1111);
        rp.setName("testtest");
        d.setReportPeriod(createReportPeriod(1, "a"));
        return d;
    }

    public DeclarationData createDeclarationData(Long id, Long asnuId, int departmentReportPeriodId) {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(id);
        declarationData.setAsnuId(asnuId);
        declarationData.setDepartmentReportPeriodId(departmentReportPeriodId);
        declarationData.setDeclarationTemplateId(100);
        return declarationData;
    }

    @Test
    public void getSourcesTest() throws Exception {

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(100);
        declarationTemplate.setDeclarationFormKind(DeclarationFormKind.PRIMARY);

        when(testHelper.getDeclarationService().getTemplate(eq(100))).thenReturn(declarationTemplate);

        when(testHelper.getDepartmentService().getAllChildren(anyInt())).thenReturn(createDepartmentList());
        when(testHelper.getReportPeriodService().getReportPeriodsByDate(eq(TaxType.NDFL), any(Date.class), any(Date.class))).thenReturn(createReportPeriodList());
        when(testHelper.getDeclarationService().findAllDeclarationData(anyInt(), anyInt(), eq(1))).thenReturn(createFirstQuarterDeclarationData());
        when(testHelper.getDeclarationService().findAllDeclarationData(anyInt(), anyInt(), eq(2))).thenReturn(createHalfYearDeclarationData());

        when(testHelper.getDepartmentReportPeriodService().get(eq(1))).thenReturn(createDepartmentReportPeriod(1, "01.01.2016"));
        when(testHelper.getDepartmentReportPeriodService().get(eq(2))).thenReturn(createDepartmentReportPeriod(2, null));
        when(testHelper.getDepartmentReportPeriodService().get(eq(3))).thenReturn(createDepartmentReportPeriod(3, "02.01.2016"));
        when(testHelper.getDepartmentReportPeriodService().get(eq(4))).thenReturn(createDepartmentReportPeriod(4, "10.01.2016"));
        when(testHelper.getDepartmentReportPeriodService().get(eq(5))).thenReturn(createDepartmentReportPeriod(5, "03.01.2016"));

        testHelper.execute(FormDataEvent.GET_SOURCES);

        FormSources sources = testHelper.getSources();
        Assert.assertEquals(sources.getSourceList().size(), 8);
        Assert.assertTrue(sources.isSourcesProcessedByScript());

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


    @Test
    public void get() throws Exception {
        System.out.println("test");

        List<DepartmentReportPeriod> list = new ArrayList<DepartmentReportPeriod>();




    }



    public Date parseDate(String xmlDate) {
        try {
            return new java.text.SimpleDateFormat("dd.MM.yyyy").parse(xmlDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }




}
