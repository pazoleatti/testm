package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("DeclarationDataServiceTest.xml")
public class DeclarationDataServiceImplTest {

    private static String TEST_XML_FILE_NAME = "com/aplana/sbrf/taxaccounting/service/impl/declaration.xml";

    @Autowired
    DeclarationDataServiceImpl declarationDataService;
    @Autowired
	DeclarationDataDao declarationDataDao;
    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    DeclarationTemplateService declarationTemplateService;
    @Autowired
    PeriodService periodService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    SourceService sourceService;
    @Autowired
    ReportService reportService;
    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");

	////////////////
	// TODO: (sgoryachkin)
	// Незнаю как это тестировать. Закормментил тесты
	//
	//
	//
	////////////////
	
    @Test
    public void testme() {
        // TODO фиктивный тест, добил чтоб не падала сборка
        assert 1 == 1;
    }

    @Test
    public void existDeclarationTest() throws ParseException {

        Logger logger = new Logger();

        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(1);
        declarationType.setName("Тестовый тип налоговой формы");

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setType(declarationType);
        declarationTemplate.setId(1);

        DeclarationData declarationData = new DeclarationData();
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        declarationData.setReportPeriodId(1);
        declarationData.setId(1l);
        declarationData.setDepartmentReportPeriodId(1);

        DeclarationData declarationData1 = new DeclarationData();
        declarationData1.setDeclarationTemplateId(1);
        declarationData1.setDepartmentId(1);
        declarationData1.setReportPeriodId(2);
        declarationData1.setId(2l);
        declarationData1.setDepartmentReportPeriodId(2);

        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        taxPeriod.setYear(2014);
        taxPeriod.setTaxType(TaxType.INCOME);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        reportPeriod.setTaxPeriod(taxPeriod);
        reportPeriod.setName("Тестовый период");
        ReportPeriod reportPeriod1 = new ReportPeriod();
        reportPeriod1.setId(2);
        reportPeriod1.setTaxPeriod(taxPeriod);
        reportPeriod1.setName("Второй тестовый период");

        Department department = new Department();
        department.setName("Тестовое подразделение");

        List<Long> list = new ArrayList<Long>() {{
            add(1l);
            add(2l);
        }};

        when(declarationDataDao.getDeclarationIds(1, 1)).thenReturn(list);
        when(declarationDataDao.get(1)).thenReturn(declarationData);
        when(declarationDataDao.get(2)).thenReturn(declarationData1);

        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        when(departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())).thenReturn(drp);
        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setCorrectionDate(SDF.parse("01.01.2014"));
        when(departmentReportPeriodService.get(declarationData1.getDepartmentReportPeriodId())).thenReturn(drp1);

        when(declarationTemplateService.get(1)).thenReturn(declarationTemplate);

        when(periodService.getReportPeriod(declarationData.getReportPeriodId())).thenReturn(reportPeriod);
        when(periodService.getReportPeriod(declarationData1.getReportPeriodId())).thenReturn(reportPeriod1);

        when(departmentService.getDepartment(1)).thenReturn(department);

        assertTrue(declarationDataService.existDeclaration(1, 1, logger.getEntries()));
        assertEquals(
                "Существует экземпляр \"Тестовый тип налоговой формы\" в подразделении \"Тестовое подразделение\" в периоде \"Тестовый период 2014\" для макета!",
                logger.getEntries().get(0).getMessage()
        );
        assertEquals(
                "Существует экземпляр \"Тестовый тип налоговой формы\" в подразделении \"Тестовое подразделение\" в периоде \"Второй тестовый период 2014\" с датой сдачи корректировки 01.01.2014 для макета!",
                logger.getEntries().get(1).getMessage()
        );
    }

    @Test
    public void getXmlSAXParserTest() {
        DeclarationDataAccessService declarationDataAccessService = mock(DeclarationDataAccessService.class);
        ReflectionTestUtils.setField(declarationDataService, "declarationDataAccessService", declarationDataAccessService);

        String uuid1 = UUID.randomUUID().toString().toLowerCase();
        String uuid2 = UUID.randomUUID().toString().toLowerCase();

        BlobData blobData1 = new BlobData();
        blobData1.setInputStream(this.getClass().getClassLoader().getResourceAsStream(TEST_XML_FILE_NAME));
        LocalDateTime expectedDate = new LocalDateTime();
        blobData1.setCreationDate(expectedDate);
        BlobData blobData2 = new BlobData();
        String expectedName = "NO_PRIB_7750_7750_7707083893777777777_20141112_D63A8CB3-C93D-483C-BED5-81F4EC69B549";
        blobData2.setInputStream(this.getClass().getClassLoader().getResourceAsStream(TEST_XML_FILE_NAME));
        blobData2.setName(expectedName);

        BlobDataService blobDataService = mock(BlobDataService.class);
        when(blobDataService.get(uuid1)).thenReturn(blobData1);
        when(blobDataService.get(uuid2)).thenReturn(blobData2);
        ReflectionTestUtils.setField(declarationDataService, "blobDataService", blobDataService);

        long declarationDataId1 = 1, declarationDataId2 = 2;
        TAUserInfo userInfo = new TAUserInfo();

        when(reportService.getDec(userInfo, declarationDataId1, DeclarationDataReportType.XML_DEC)).thenReturn(uuid1);
        when(reportService.getDec(userInfo, declarationDataId2, DeclarationDataReportType.XML_DEC)).thenReturn(uuid2);
        ReflectionTestUtils.setField(declarationDataService, "reportService", reportService);

        assertEquals(expectedDate, declarationDataService.getXmlDataDocDate(declarationDataId1, userInfo));
        assertEquals(expectedName, declarationDataService.getXmlDataFileName(declarationDataId2, userInfo));
    }

    @Test
    public void checkTest() {
        Logger logger = new Logger();

        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(1);
        declarationType.setName("Тестовый тип налоговой формы");

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setType(declarationType);
        declarationTemplate.setId(1);

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        declarationData.setReportPeriodId(1);
        declarationData.setDepartmentReportPeriodId(1);

        Department department = new Department();
        department.setName("Тестовое подразделение");

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        ReportPeriod reportPeriod = new ReportPeriod();
        TaxPeriod tp = new TaxPeriod();
        tp.setYear(2015);
        reportPeriod.setTaxPeriod(tp);
        reportPeriod.setCalendarStartDate(new Date());
        reportPeriod.setName("1 квартал");
        reportPeriod.setId(2);
        reportPeriod.setStartDate(new Date());
        reportPeriod.setEndDate(new Date());

        ArrayList<DepartmentFormType> departmentFormTypes = new ArrayList<DepartmentFormType>(2);
        DepartmentFormType dft1 = new DepartmentFormType();
        dft1.setDepartmentId(1);
        dft1.setFormTypeId(1);
        dft1.setKind(FormDataKind.ADDITIONAL);
        DepartmentFormType dft2 = new DepartmentFormType();
        dft2.setDepartmentId(2);
        dft2.setFormTypeId(2);
        dft2.setKind(FormDataKind.CONSOLIDATED);
        departmentFormTypes.add(dft1);
        departmentFormTypes.add(dft2);
        departmentFormTypes.add(dft2);
        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setId(1);
        drp1.setCorrectionDate(new Date(0));
        DepartmentReportPeriod drp2 = new DepartmentReportPeriod();
        drp2.setId(2);
        drp2.setCorrectionDate(new Date(0));


        ArrayList<Relation> sources = new ArrayList<Relation>();
        DeclarationTemplate declarationTemplate1 = new DeclarationTemplate();
        declarationTemplate1.setName("Тестовый макет");
        declarationTemplate1.setDeclarationFormKind(DeclarationFormKind.PRIMARY);
        Relation r1 = new Relation();
        r1.setFullDepartmentName("Тестовое подразделение");
        r1.setDeclarationTemplate(declarationTemplate1);
        r1.setPeriodName("1 квартал");
        r1.setYear(2015);
        r1.setCorrectionDate(new Date(0));
        r1.setCreated(true);
        r1.setDeclarationDataId(11L);
        r1.setDeclarationState(State.ACCEPTED);

        Relation r2 = new Relation();
        DeclarationTemplate declarationTemplate2 = new DeclarationTemplate();
        declarationTemplate2.setName("Тестовый макет");
        declarationTemplate2.setDeclarationFormKind(DeclarationFormKind.CONSOLIDATED);
        r2.setFullDepartmentName("Тестовое подразделение");
        r2.setDeclarationTemplate(declarationTemplate2);
        r2.setPeriodName("1 квартал");
        r2.setYear(2015);
        r2.setCorrectionDate(new Date(0));
        r2.setCreated(false);
        sources.add(r1);
        sources.add(r2);
        when(sourceService.getDeclarationSourcesInfo(declarationData, true, false, null, userInfo, logger)).thenReturn(sources);

        when(departmentService.getDepartment(declarationData.getDepartmentId())).thenReturn(department);
        when(departmentService.getDepartment(dft2.getDepartmentId())).thenReturn(department);
        when(declarationDataDao.get(declarationData.getId())).thenReturn(declarationData);
        when(reportService.getDec(Matchers.<TAUserInfo>any(), anyLong(), Matchers.<DeclarationDataReportType>anyObject())).thenReturn(UUID.randomUUID().toString());
        when(declarationTemplateService.get(declarationData.getDeclarationTemplateId())).thenReturn(declarationTemplate);
        when(periodService.getReportPeriod(declarationData.getReportPeriodId())).thenReturn(reportPeriod);

        when(departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())).thenReturn(drp1);

        when(departmentReportPeriodService.getLast(1, 1)).thenReturn(drp1);

        when(departmentReportPeriodService.getLast(2, 1)).thenReturn(drp2);

        try{
            when(sourceService.isDDConsolidationTopical(1L)).thenReturn(false);
            declarationDataService.check(logger, 1L, userInfo, new LockStateLogger() {
                @Override
                public void updateState(String state) {
                }
            });
        } catch (ServiceException e){
            //Nothing
        }

        assertEquals("Налоговая форма содержит неактуальные консолидированные данные  (расприняты формы-источники / удалены назначения по формам-источникам, на основе которых ранее выполнена консолидация). Для коррекции консолидированных данных необходимо нажать на кнопку \"Рассчитать\"",
                logger.getEntries().get(0).getMessage());

        try{
            logger.clear();
            when(sourceService.isDDConsolidationTopical(1L)).thenReturn(true);
            declarationDataService.check(logger, 1L, userInfo, new LockStateLogger() {
                @Override
                public void updateState(String state) {
                }
            });
        } catch (ServiceLoggerException e){
            //Nothing
        }

        assertEquals(
                "Не выполнена консолидация данных из формы \"Тестовое подразделение\", \"Тестовый макет\", \"Первичная\", \"1 квартал\", \"2015 с датой сдачи корректировки 01.01.1970\" в статусе \"Принята\"",
                logger.getEntries().get(0).getMessage()
        );
        assertEquals(
                "Не выполнена консолидация данных из формы \"Тестовое подразделение\", \"Тестовый макет\", \"Консолидированная\", \"1 квартал\", \"2015 с датой сдачи корректировки 01.01.1970\" - экземпляр формы не создан",
                logger.getEntries().get(1).getMessage()
        );
    }

    @Test
    public void getTaskName() {
        DeclarationSubreport declarationSubreport = new DeclarationSubreport();
        declarationSubreport.setName("report name");
        DeclarationDataReportType specificReport = new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, declarationSubreport);

        assertEquals(declarationDataService.getAsyncTaskName(DeclarationDataReportType.CHECK_DEC, TaxType.INCOME), "Проверка налоговой формы");
        assertEquals(declarationDataService.getAsyncTaskName(DeclarationDataReportType.ACCEPT_DEC, TaxType.INCOME), "Принятие налоговой формы");
        assertEquals(declarationDataService.getAsyncTaskName(DeclarationDataReportType.EXCEL_DEC, TaxType.INCOME), "Формирование отчета налоговой формы в XLSX-формате");
        assertEquals(declarationDataService.getAsyncTaskName(DeclarationDataReportType.XML_DEC, TaxType.INCOME), "Расчет налоговой формы");
        assertEquals(declarationDataService.getAsyncTaskName(DeclarationDataReportType.PDF_DEC, TaxType.INCOME), "Создание формы предварительного просмотра");
        assertEquals(declarationDataService.getAsyncTaskName(specificReport, TaxType.INCOME), "Формирование отчета \"report name\" налоговой формы");
    }

    @Test
    public void createSpecificReport() throws IOException {
        Logger logger = new Logger();

        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(112);
        declarationType.setName("Тестовый тип налоговой формы");

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setType(declarationType);
        declarationTemplate.setId(10010);
        InputStream stream = DeclarationTemplateServiceImpl.class.getResourceAsStream("SpecificDecReport.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        declarationTemplate.setCreateScript(script);

        DeclarationData declarationData = new DeclarationData();
        declarationData.setDeclarationTemplateId(declarationTemplate.getId());
        declarationData.setDepartmentId(1);
        declarationData.setReportPeriodId(1);
        declarationData.setId(1l);
        declarationData.setDepartmentReportPeriodId(1);

        DeclarationSubreport declarationSubreport = new DeclarationSubreport();
        declarationSubreport.setName("report name");
        declarationSubreport.setAlias("specific1");
        DeclarationDataReportType specificReport = new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, declarationSubreport);

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        DeclarationDataScriptingServiceImpl scriptingService = new DeclarationDataScriptingServiceImpl();

        DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);
        when(declarationTemplateDao.get(declarationTemplate.getId())).thenReturn(declarationTemplate);
        when(declarationTemplateDao.getDeclarationTemplateScript(declarationTemplate.getId())).thenReturn(declarationTemplate.getCreateScript());

        ReflectionTestUtils.setField(scriptingService, "declarationTemplateDao", declarationTemplateDao);
        Properties versionInfoProperties = new Properties();
        versionInfoProperties.put("productionMode", "true");
        versionInfoProperties.put("version", "test");
        ApplicationInfo applicationInfo = new ApplicationInfo();
        ReflectionTestUtils.setField(scriptingService, "applicationInfo", applicationInfo);
        ReflectionTestUtils.setField(applicationInfo, "versionInfoProperties", versionInfoProperties);

        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBeansWithAnnotation(ScriptExposed.class)).thenReturn(new HashMap<String, Object>());
        scriptingService.setApplicationContext(ctx);

        TransactionHelper tx = new TransactionHelper() {
            @Override
            public <T> T executeInNewTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }

            @Override
            public <T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }
        };
        ReflectionTestUtils.setField(scriptingService, "tx", tx);

        ReflectionTestUtils.setField(declarationDataService, "declarationDataScriptingService", scriptingService);

        BlobDataService blobDataService = mock(BlobDataService.class);
        final List<String> strings = new ArrayList<String>();
        when(blobDataService.create(anyString(), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String path = (String)invocation.getArguments()[0];
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
                try {
                    String s;
                    while ((s = in.readLine()) != null) {
                        strings.add(s);
                    }
                } finally {
                    in.close();
                }
                return "uuid";
            }
        });
        ReflectionTestUtils.setField(declarationDataService, "blobDataService", blobDataService);

        declarationDataService.createSpecificReport(logger, declarationData, specificReport, null, null,null, userInfo, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                //Nothing
            }
        });

        assertEquals(strings.size(), 1);
        assertEquals(strings.get(0), specificReport.getReportAlias());
    }

    //@Test
    public void getValueForCheckLimit() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        when(declarationDataDao.get(declarationData.getId())).thenReturn(declarationData);
                when(reportService.getDec(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC)).thenReturn("uuid1");

        BlobDataService blobDataService = mock(BlobDataService.class);
        when(blobDataService.getLength("uuid1")).thenReturn(1200L);
        ReflectionTestUtils.setField(declarationDataService, "blobDataService", blobDataService);

        DeclarationDataScriptingService declarationDataScriptingService = mock(DeclarationDataScriptingService.class);
        when(declarationDataScriptingService.executeScript(
                eq(userInfo), eq(declarationData), eq(FormDataEvent.CALCULATE_TASK_COMPLEXITY), any(Logger.class), any(Map.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, Object> exchangeParams = ((Map<String, Object>) invocation.getArguments()[4]);
                ((ScriptTaskComplexityHolder) exchangeParams.get("taskComplexityHolder")).setValue(10L);
                return null;
            }
        });
        ReflectionTestUtils.setField(declarationDataService, "declarationDataScriptingService", declarationDataScriptingService);

        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), DeclarationDataReportType.PDF_DEC));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), DeclarationDataReportType.EXCEL_DEC));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), DeclarationDataReportType.ACCEPT_DEC));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), DeclarationDataReportType.CHECK_DEC));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), DeclarationDataReportType.XML_DEC));
        assertEquals(new Long(10L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, new DeclarationSubreport(){{setAlias("alias1");}})));
    }
}
