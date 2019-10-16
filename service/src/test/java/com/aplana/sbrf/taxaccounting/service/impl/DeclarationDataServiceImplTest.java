package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateEventScriptDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
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
    DepartmentReportPeriodFormatter departmentReportPeriodFormatter;
    @Autowired
    BlobDataService blobDataService;
    @Autowired
    ReportPeriodService reportPeriodService;
    @Autowired
    RefBookFactory refBookFactory;
    @Autowired
    TransactionHelper transactionHelper;
    @Autowired
    LockDataService lockDataService;
    @Autowired
    BasePermissionEvaluator basePermissionEvaluator;
    @Autowired
    DeclarationLocker declarationLocker;
    @Autowired
    ConfigurationService configurationService;
    @Autowired
    LogEntryService logEntryService;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");


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
        declarationData.setId(1L);
        declarationData.setDepartmentReportPeriodId(1);

        DeclarationData declarationData1 = new DeclarationData();
        declarationData1.setDeclarationTemplateId(1);
        declarationData1.setDepartmentId(1);
        declarationData1.setReportPeriodId(2);
        declarationData1.setId(2L);
        declarationData1.setDepartmentReportPeriodId(2);

        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        taxPeriod.setYear(2014);
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
            add(1L);
            add(2L);
        }};

        when(declarationDataDao.getDeclarationIds(1, 1)).thenReturn(list);
        when(declarationDataDao.get(1)).thenReturn(declarationData);
        when(declarationDataDao.get(2)).thenReturn(declarationData1);

        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        when(departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId())).thenReturn(drp);
        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setCorrectionDate(SDF.parse("01.01.2014"));
        when(departmentReportPeriodService.fetchOne(declarationData1.getDepartmentReportPeriodId())).thenReturn(drp1);

        when(declarationTemplateService.get(1)).thenReturn(declarationTemplate);

        when(periodService.fetchReportPeriod(declarationData.getReportPeriodId())).thenReturn(reportPeriod);
        when(periodService.fetchReportPeriod(declarationData1.getReportPeriodId())).thenReturn(reportPeriod1);

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
        String uuid1 = UUID.randomUUID().toString().toLowerCase();
        String uuid2 = UUID.randomUUID().toString().toLowerCase();

        BlobData blobData1 = new BlobData();
        blobData1.setInputStream(this.getClass().getClassLoader().getResourceAsStream(TEST_XML_FILE_NAME));
        Date expectedDate = new Date();
        blobData1.setCreationDate(expectedDate);
        BlobData blobData2 = new BlobData();
        String expectedName = "NO_PRIB_7750_7750_7707083893777777777_20141112_D63A8CB3-C93D-483C-BED5-81F4EC69B549";
        blobData2.setInputStream(this.getClass().getClassLoader().getResourceAsStream(TEST_XML_FILE_NAME));
        blobData2.setName(expectedName);

        when(blobDataService.get(uuid1)).thenReturn(blobData1);
        when(blobDataService.get(uuid2)).thenReturn(blobData2);

        long declarationDataId1 = 1, declarationDataId2 = 2;
        TAUserInfo userInfo = new TAUserInfo();

        when(reportService.getReportFileUuidSafe(declarationDataId1, DeclarationReportType.XML_DEC)).thenReturn(uuid1);
        when(reportService.getReportFileUuidSafe(declarationDataId2, DeclarationReportType.XML_DEC)).thenReturn(uuid2);
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

        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setId(1);
        drp1.setCorrectionDate(new Date(0));
        DepartmentReportPeriod drp2 = new DepartmentReportPeriod();
        drp2.setId(2);
        drp2.setCorrectionDate(new Date(0));


        ArrayList<Relation> sources = new ArrayList<>();
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

        sources.add(r1);
        when(sourceService.getDeclarationSourcesInfo(declarationData.getId())).thenReturn(sources);

        when(departmentService.getDepartment(declarationData.getDepartmentId())).thenReturn(department);
        when(departmentService.getDepartment(2)).thenReturn(department);
        when(declarationDataDao.get(declarationData.getId())).thenReturn(declarationData);
        when(reportService.getReportFileUuidSafe(anyLong(), Matchers.<DeclarationReportType>anyObject())).thenReturn(UUID.randomUUID().toString());
        when(declarationTemplateService.get(declarationData.getDeclarationTemplateId())).thenReturn(declarationTemplate);
        when(periodService.fetchReportPeriod(declarationData.getReportPeriodId())).thenReturn(reportPeriod);

        when(departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId())).thenReturn(drp1);

        when(departmentReportPeriodService.fetchLast(1, 1)).thenReturn(drp1);

        when(departmentReportPeriodService.fetchLast(2, 1)).thenReturn(drp2);

        try {
            when(sourceService.isDDConsolidationTopical(1L)).thenReturn(false);
            declarationDataService.check(logger, 1L, userInfo, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                }
            });
        } catch (ServiceException e) {
            //Nothing
        }

        assertEquals("Налоговая форма содержит неактуальные консолидированные данные  (расприняты формы-источники / удалены назначения по формам-источникам, на основе которых ранее выполнена консолидация). Для коррекции консолидированных данных необходимо нажать на кнопку \"Рассчитать\"",
                logger.getEntries().get(0).getMessage());

        try {
            logger.clear();
            when(sourceService.isDDConsolidationTopical(1L)).thenReturn(true);
            declarationDataService.check(logger, 1L, userInfo, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                }
            });
        } catch (ServiceLoggerException e) {
            //Nothing
        }

        assertEquals(
                "Не выполнена консолидация данных из формы \"Тестовое подразделение\", \"Тестовый макет\", \"Первичная\", \"1 квартал\", \"2015 с датой сдачи корректировки 01.01.1970\" в статусе \"Принята\"",
                logger.getEntries().get(0).getMessage()
        );
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
        declarationData.setId(1L);
        declarationData.setDepartmentReportPeriodId(1);

        DeclarationSubreport declarationSubreport = new DeclarationSubreport();
        declarationSubreport.setName("report name");
        declarationSubreport.setAlias("specific1");
        DeclarationReportType specificReport = DeclarationReportType.createSpecificReport();
        specificReport.setSubreport(declarationSubreport);

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

        DeclarationTemplateEventScriptDao declarationTemplateEventScriptDao = mock(DeclarationTemplateEventScriptDao.class);
        when(declarationTemplateEventScriptDao.findScript(eq(declarationTemplate.getId()), any(Integer.class))).thenReturn(declarationTemplate.getCreateScript());
        ReflectionTestUtils.setField(scriptingService, "declarationTemplateEventScriptDao", declarationTemplateEventScriptDao);

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
        final List<String> strings = new ArrayList<>();
        when(blobDataService.create(anyString(), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String path = (String) invocation.getArguments()[0];
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        strings.add(s);
                    }
                }
                return "uuid";
            }
        });
        ReflectionTestUtils.setField(declarationDataService, "blobDataService", blobDataService);

        SpecificReportContext specificReportContext = SpecificReportContext.builder()
                .logger(logger)
                .declarationData(declarationData)
                .ddReportType(specificReport)
                .subreportParamValues(Collections.<String, Object>emptyMap())
                .userInfo(userInfo)
                .build();
        declarationDataService.createSpecificReport(specificReportContext, new LockStateLogger() {
            @Override
            public void updateState(AsyncTaskState state) {
                //Nothing
            }
        });

        assertEquals(strings.size(), 1);
        assertEquals(strings.get(0), specificReport.getSubreport().getAlias());
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
        when(reportService.getReportFileUuidSafe(declarationData.getId(), DeclarationReportType.XML_DEC)).thenReturn("uuid1");

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

        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), AsyncTaskType.PDF_DEC, null));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), AsyncTaskType.EXCEL_DEC, null));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), AsyncTaskType.ACCEPT_DEC, null));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), AsyncTaskType.CHECK_DEC, null));
        assertEquals(new Long(2L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), AsyncTaskType.XML_DEC, null));
        Map<String, Object> params = new HashMap<>();
        params.put("alias", "alias1");
        assertEquals(new Long(10L), declarationDataService.getValueForCheckLimit(userInfo, declarationData.getId(), AsyncTaskType.SPECIFIC_REPORT_DEC, params));
    }

    @Test
    public void testUploadFile() {
        long declarationDataId = 0L;
        InputStream inputStream = mock(InputStream.class);
        String fileName = "fileName";

        declarationDataService.uploadFile(inputStream, fileName, declarationDataId);

        verify(blobDataService, times(1)).create(inputStream, fileName);
    }

    @Test
    public void testDownLoadFile() {
        String blobId = "blobId";
        DeclarationDataFile declarationDataFile = mock(DeclarationDataFile.class);
        when(declarationDataFile.getDeclarationDataId()).thenReturn(0L);
        when(declarationDataFile.getUuid()).thenReturn(blobId);
        declarationDataService.downloadFile(declarationDataFile);
        Mockito.verify(blobDataService, Mockito.times(1)).get(blobId);
    }

    @Test
    public void testCancel() {
        long declarationDataId = 1L;
        String note = "note";
        String lockKey = "TEST_LOCK";
        TAUserInfo userInfo = mock(TAUserInfo.class);

        TAUser user = mock(TAUser.class);
        int userId = 1;
        int departmentReportPeriodId = 0;
        int departmentId = 0;
        long reportPeriodTypeId = 0L;
        DeclarationData declarationData = mock(DeclarationData.class);
        DeclarationTemplate declarationTemplate = mock(DeclarationTemplate.class);
        DeclarationType type = mock(DeclarationType.class);
        DepartmentReportPeriod departmentReportPeriod = mock(DepartmentReportPeriod.class);
        ReportPeriod reportPeriod = mock(ReportPeriod.class);
        ReportPeriodType reportPeriodType = mock(ReportPeriodType.class);
        RefBookDataProvider provider = mock(RefBookDataProvider.class);
        TaxPeriod taxperiod = mock(TaxPeriod.class);
        Department department = mock(Department.class);
        Map<String, RefBookValue> asnu = mock(Map.class);
        RefBookValue name = mock(RefBookValue.class);

        when(userInfo.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);


        when(logEntryService.createLogger()).thenReturn(new Logger());
        when(declarationDataDao.get(declarationDataId)).thenReturn(declarationData);
        when(declarationDataDao.get(anyListOf(Long.class))).thenReturn(Collections.singletonList(declarationData));

        when(declarationData.getDeclarationTemplateId()).thenReturn(100);
        when(declarationTemplateService.get(100)).thenReturn(declarationTemplate);
        when(declarationData.getId()).thenReturn(declarationDataId);

        when(declarationTemplate.getType()).thenReturn(type);
        when(type.getName()).thenReturn("declarationTypeName");
        when(declarationData.getDepartmentReportPeriodId()).thenReturn(departmentReportPeriodId);
        when(departmentReportPeriodService.fetchOne(departmentReportPeriodId)).thenReturn(departmentReportPeriod);
        when(departmentReportPeriod.getReportPeriod()).thenReturn(reportPeriod);
        when(reportPeriod.getDictTaxPeriodId()).thenReturn(reportPeriodTypeId);
        when(reportPeriod.getTaxPeriod()).thenReturn(taxperiod);
        when(periodService.getPeriodTypeById(reportPeriodTypeId)).thenReturn(reportPeriodType);
        when(reportPeriodType.getName()).thenReturn("reportPeriodTypeName");
        when(taxperiod.getYear()).thenReturn(2018);
        when(refBookFactory.getDataProvider(RefBook.Id.ASNU.getId())).thenReturn(provider);
        when(departmentReportPeriod.getDepartmentId()).thenReturn(departmentId);
        when(departmentService.getDepartment(departmentId)).thenReturn(department);
        when(department.getName()).thenReturn("departmentName");
        when(provider.getRecordData(anyLong())).thenReturn(asnu);
        when(asnu.get("NAME")).thenReturn(name);
        when(basePermissionEvaluator.hasPermission(any(Authentication.class), any(Serializable.class), anyString(), any(Object.class))).thenReturn(true);

        LockData lock = new LockData(lockKey, userId);
        when(declarationLocker.establishLock(eq(declarationDataId), eq(OperationType.RETURN_DECLARATION), eq(userInfo), any(Logger.class))).thenReturn(lock);

        declarationDataService.cancelDeclarationList(Collections.singletonList(declarationDataId), note, userInfo);

        verify(declarationDataDao).setStatus(eq(declarationDataId), any(State.class));
    }

    @Test
    public void testNdflSortAndUpdateRowNum1() {
        Calendar instance = Calendar.getInstance();

        NdflPersonIncome income1 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 1);
        income1.setOperationId("1");
        income1.setPaymentDate(instance.getTime());
        income1.setRowNum(new BigDecimal("1"));
        income1.setOperationDate(income1.getPaymentDate());

        NdflPersonIncome income2 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 3);
        income2.setOperationId("2");
        income2.setIncomeAccruedDate(instance.getTime());
        income2.setRowNum(new BigDecimal("2"));
        income2.setOperationDate(income2.getIncomeAccruedDate());

        NdflPersonIncome income3 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income3.setOperationId("3");
        income3.setPaymentDate(instance.getTime());
        income3.setRowNum(new BigDecimal("3"));

        NdflPersonIncome income4 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income4.setOperationId("3");
        income4.setIncomeAccruedDate(instance.getTime());
        income4.setRowNum(new BigDecimal("4"));
        income3.setOperationDate(income4.getIncomeAccruedDate());
        income4.setOperationDate(income4.getIncomeAccruedDate());

        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setInp("qqq");
        ndflPerson.setIncomes(new ArrayList<>(Arrays.asList(income1, income2, income3, income4)));

        Collections.sort(ndflPerson.getIncomes(), NdflPersonIncome.getComparator());
        ndflPerson.setIncomes(declarationDataService.updateRowNum(ndflPerson.getIncomes()));

        assertEquals("1", ndflPerson.getIncomes().get(0).getOperationId());
        assertEquals("3", ndflPerson.getIncomes().get(1).getOperationId());
        assertEquals("3", ndflPerson.getIncomes().get(2).getOperationId());
        assertEquals("2", ndflPerson.getIncomes().get(3).getOperationId());

        assertEquals(new BigDecimal("1"), ndflPerson.getIncomes().get(0).getRowNum());
        assertEquals(new BigDecimal("2"), ndflPerson.getIncomes().get(1).getRowNum());
        assertEquals(new BigDecimal("3"), ndflPerson.getIncomes().get(2).getRowNum());
        assertEquals(new BigDecimal("4"), ndflPerson.getIncomes().get(3).getRowNum());
    }

    @Test
    public void testNdflSortAndUpdateRowNum2() {
        Calendar instance = Calendar.getInstance();

        NdflPersonIncome income1 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 1);
        income1.setOperationId("1");
        income1.setPaymentDate(instance.getTime());
        income1.setRowNum(new BigDecimal("5"));
        income1.setOperationDate(income1.getPaymentDate());

        NdflPersonIncome income2 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 3);
        income2.setOperationId("2");
        income2.setIncomeAccruedDate(instance.getTime());
        income2.setRowNum(new BigDecimal("6"));
        income2.setOperationDate(income2.getIncomeAccruedDate());

        NdflPersonIncome income3 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income3.setOperationId("3");
        income3.setPaymentDate(instance.getTime());
        income3.setRowNum(new BigDecimal("7"));

        NdflPersonIncome income4 = new NdflPersonIncome();
        instance.set(2010, Calendar.JANUARY, 2);
        income4.setOperationId("3");
        income4.setIncomeAccruedDate(instance.getTime());
        income4.setRowNum(new BigDecimal("8"));
        income3.setOperationDate(income4.getIncomeAccruedDate());
        income4.setOperationDate(income4.getIncomeAccruedDate());

        NdflPerson ndflPerson = new NdflPerson();
        ndflPerson.setInp("qqq");
        ndflPerson.setIncomes(new ArrayList<>(Arrays.asList(income1, income3, income2, income4)));

        Collections.sort(ndflPerson.getIncomes(), NdflPersonIncome.getComparator());
        ndflPerson.setIncomes(declarationDataService.updateRowNum(ndflPerson.getIncomes()));

        assertEquals("1", ndflPerson.getIncomes().get(0).getOperationId());
        assertEquals("3", ndflPerson.getIncomes().get(1).getOperationId());
        assertEquals("3", ndflPerson.getIncomes().get(2).getOperationId());
        assertEquals("2", ndflPerson.getIncomes().get(3).getOperationId());

        assertEquals(new BigDecimal("5"), ndflPerson.getIncomes().get(0).getRowNum());
        assertEquals(new BigDecimal("6"), ndflPerson.getIncomes().get(1).getRowNum());
        assertEquals(new BigDecimal("7"), ndflPerson.getIncomes().get(2).getRowNum());
        assertEquals(new BigDecimal("8"), ndflPerson.getIncomes().get(3).getRowNum());
    }


    @Test
    public void test_checkRowsEditCountParam_forValueLessThanParam_isSuccessful() {
        when(configurationService.getParamIntValue(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT)).thenReturn(10);
        ActionResult result = declarationDataService.checkRowsEditCountParam(9);
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void test_checkRowsEditCountParam_forTheSameValueAsParam_isSuccessful() {
        when(configurationService.getParamIntValue(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT)).thenReturn(10);
        ActionResult result = declarationDataService.checkRowsEditCountParam(10);
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void test_checkRowsEditCountParam_forValueMoreThanParam_isUnsuccessful() {
        when(configurationService.getParamIntValue(ConfigurationParam.DECLARATION_ROWS_BULK_EDIT_MAX_COUNT)).thenReturn(10);
        ActionResult result = declarationDataService.checkRowsEditCountParam(11);
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    public void test_checkCorrectWorkOfSearchExistingDeclarationDuringCreationOfKnfByKpp() {
        int declarationTypeId = 1;

        DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
        departmentDeclarationType.setDeclarationTypeId(declarationTypeId);

        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(declarationTypeId);
        declarationType.setName("Тестовый тип налоговой формы");

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setType(declarationType);
        declarationTemplate.setDeclarationFormKind(DeclarationFormKind.CONSOLIDATED);
        declarationTemplate.setId(1);

        Department department = new Department();
        department.setName("Тестовое подразделение");

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);

        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setId(1);
        drp1.setCorrectionDate(new Date(0));
        drp1.setReportPeriod(reportPeriod);
        drp1.setDepartmentId(1);
        drp1.setIsActive(true);

        TARole taRole = new TARole();
        taRole.setAlias(TARole.N_ROLE_CONTROL_UNP);

        TAUser user = new TAUser();
        user.setId(1);
        user.setRoles(Collections.singletonList(taRole));
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(user);

        int consolidateDeclarationTemplateId = DeclarationType.NDFL_CONSOLIDATE;

        DeclarationDataScriptingService declarationDataScriptingService = mock(DeclarationDataScriptingService.class);

        when(departmentReportPeriodService.fetchLast(1, 1)).thenReturn(drp1);
        when(departmentReportPeriodFormatter.getPeriodDescription(drp1)).thenReturn("period");
        when(departmentService.getDepartment(1)).thenReturn(department);
        when(declarationTemplateService.getActiveDeclarationTemplateId(consolidateDeclarationTemplateId, 1)).thenReturn(consolidateDeclarationTemplateId);
        when(declarationTemplateService.get(anyInt())).thenReturn(declarationTemplate);
        when(lockDataService.lock(anyString(), anyInt(), anyString())).thenReturn(null);
        when(sourceService.getDDTByDepartment(eq(1), any(TaxType.class), any(Date.class), any(Date.class)))
                .thenReturn(Collections.singletonList(departmentDeclarationType));

        when(declarationDataScriptingService.executeScript(
                eq(userInfo), any(DeclarationData.class), eq(FormDataEvent.CREATE), any(Logger.class), any(Map.class))
        ).thenReturn(true);
        when(declarationDataScriptingService.executeScript(
                eq(userInfo), any(DeclarationData.class), eq(FormDataEvent.AFTER_CREATE), any(Logger.class), any(Map.class))
        ).thenReturn(true);

        CreateDeclarationDataAction createDeclarationDataAction = new CreateDeclarationDataAction();
        createDeclarationDataAction.setKnfType(RefBookKnfType.BY_KPP);
        createDeclarationDataAction.setDepartmentId(1);
        createDeclarationDataAction.setPeriodId(1);
        createDeclarationDataAction.setDeclarationTypeId((long) consolidateDeclarationTemplateId);

        declarationDataService.create(userInfo, createDeclarationDataAction);
        Mockito.verify(declarationDataDao, Mockito.times(1))
                .findExistingDeclarationsForCreationCheck((DeclarationData) anyObject());
        reset(declarationDataDao);
    }

    @Test
    public void test_checkCorrectWorkOfSearchExistingDeclarationDuringCreationOfKnfByNonHoldingTax() {
        CreateKnfTestData createKnfTestData = new CreateKnfTestData().init();
        TAUserInfo userInfo = createKnfTestData.getUserInfo();
        int consolidateDeclarationTemplateId = createKnfTestData.getConsolidateDeclarationTemplateId();

        CreateDeclarationDataAction createDeclarationDataAction = new CreateDeclarationDataAction();
        createDeclarationDataAction.setKnfType(RefBookKnfType.BY_NONHOLDING_TAX);
        createDeclarationDataAction.setDepartmentId(1);
        createDeclarationDataAction.setPeriodId(1);
        createDeclarationDataAction.setDeclarationTypeId((long) consolidateDeclarationTemplateId);

        declarationDataService.create(userInfo, createDeclarationDataAction);
        Mockito.verify(declarationDataDao, Mockito.times(1))
                .findExistingDeclarationsForCreationCheck((DeclarationData) anyObject());
        reset(declarationDataDao);
    }

    @Test
    public void test_checkCorrectWorkOfSearchExistingDeclarationDuringCreationOfKnfByApp2() {
        CreateKnfTestData createKnfTestData = new CreateKnfTestData().init();
        TAUserInfo userInfo = createKnfTestData.getUserInfo();
        int consolidateDeclarationTemplateId = createKnfTestData.getConsolidateDeclarationTemplateId();

        CreateDeclarationDataAction createDeclarationDataAction = new CreateDeclarationDataAction();
        createDeclarationDataAction.setKnfType(RefBookKnfType.FOR_APP2);
        createDeclarationDataAction.setDepartmentId(1);
        createDeclarationDataAction.setPeriodId(1);
        createDeclarationDataAction.setDeclarationTypeId((long) consolidateDeclarationTemplateId);

        declarationDataService.create(userInfo, createDeclarationDataAction);
        Mockito.verify(declarationDataDao, Mockito.times(1))
                .findExistingDeclarationsForCreationCheck((DeclarationData) anyObject());
        reset(declarationDataDao);
    }

    @Test
    public void test_checkCorrectWorkOfSearchExistingDeclarationDuringCreationOfKnfByAllDataYearlyPeriod() {
        CreateKnfTestData createKnfTestData = new CreateKnfTestData().init();
        TAUserInfo userInfo = createKnfTestData.getUserInfo();
        int consolidateDeclarationTemplateId = createKnfTestData.getConsolidateDeclarationTemplateId();

        CreateDeclarationDataAction createDeclarationDataAction = new CreateDeclarationDataAction();
        createDeclarationDataAction.setKnfType(RefBookKnfType.ALL);
        createDeclarationDataAction.setDepartmentId(1);
        createDeclarationDataAction.setPeriodId(1);
        createDeclarationDataAction.setDeclarationTypeId((long) consolidateDeclarationTemplateId);

        declarationDataService.create(userInfo, createDeclarationDataAction);
        Mockito.verify(declarationDataDao, Mockito.times(1))
                .findExistingDeclarationsForCreationCheck((DeclarationData) anyObject(), anyInt(), anyString());
        reset(declarationDataDao);
    }

    @Test
    public void test_checkCorrectWorkOfSearchExistingDeclarationDuringCreationOfKnfByAllDataNotYearlyPeriod() {
        int declarationTypeId = 1;

        DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
        departmentDeclarationType.setDeclarationTypeId(declarationTypeId);

        ReportPeriodType reportPeriodType = new ReportPeriodType();
        reportPeriodType.setCode("21");

        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(declarationTypeId);
        declarationType.setName("Тестовый тип налоговой формы");

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setType(declarationType);
        declarationTemplate.setDeclarationFormKind(DeclarationFormKind.CONSOLIDATED);
        declarationTemplate.setId(1);

        Department department = new Department();
        department.setName("Тестовое подразделение");

        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setTaxType(TaxType.NDFL);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(taxPeriod);
        reportPeriod.setId(1);

        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setId(1);
        drp1.setCorrectionDate(new Date(0));
        drp1.setReportPeriod(reportPeriod);
        drp1.setDepartmentId(1);
        drp1.setIsActive(true);

        TARole taRole = new TARole();
        taRole.setAlias(TARole.N_ROLE_CONTROL_UNP);

        TAUser user = new TAUser();
        user.setId(1);
        user.setRoles(Collections.singletonList(taRole));
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(user);

        int consolidateDeclarationTemplateId = DeclarationType.NDFL_CONSOLIDATE;

        DeclarationDataScriptingService declarationDataScriptingService = mock(DeclarationDataScriptingService.class);

        PeriodServiceImpl periodServiceImpl = new PeriodServiceImpl();

        when(departmentReportPeriodService.fetchLast(1, 1)).thenReturn(drp1);
        when(departmentReportPeriodFormatter.getPeriodDescription(drp1)).thenReturn("period");
        when(departmentService.getDepartment(1)).thenReturn(department);
        when(declarationTemplateService.getActiveDeclarationTemplateId(consolidateDeclarationTemplateId, 1)).thenReturn(consolidateDeclarationTemplateId);
        when(declarationTemplateService.get(anyInt())).thenReturn(declarationTemplate);
        when(periodService.getPeriodTypeById(anyLong())).thenReturn(reportPeriodType);
        when(periodService.isYearPeriodType(eq(reportPeriodType))).thenReturn(periodServiceImpl.isYearPeriodType(reportPeriodType));
        when(lockDataService.lock(anyString(), anyInt(), anyString())).thenReturn(null);
        when(sourceService.getDDTByDepartment(eq(1), any(TaxType.class), any(Date.class), any(Date.class)))
                .thenReturn(Collections.singletonList(departmentDeclarationType));

        when(declarationDataScriptingService.executeScript(
                eq(userInfo), any(DeclarationData.class), eq(FormDataEvent.CREATE), any(Logger.class), any(Map.class))
        ).thenReturn(true);
        when(declarationDataScriptingService.executeScript(
                eq(userInfo), any(DeclarationData.class), eq(FormDataEvent.AFTER_CREATE), any(Logger.class), any(Map.class))
        ).thenReturn(true);

        CreateDeclarationDataAction createDeclarationDataAction = new CreateDeclarationDataAction();
        createDeclarationDataAction.setKnfType(RefBookKnfType.ALL);
        createDeclarationDataAction.setDepartmentId(1);
        createDeclarationDataAction.setPeriodId(1);
        createDeclarationDataAction.setDeclarationTypeId((long) consolidateDeclarationTemplateId);

        declarationDataService.create(userInfo, createDeclarationDataAction);
        Mockito.verify(declarationDataDao, Mockito.times(1))
                .findExistingDeclarationsForCreationCheck((DeclarationData) anyObject());
        reset(declarationDataDao);
    }

    private class CreateKnfTestData {
        private TAUserInfo userInfo;
        private int consolidateDeclarationTemplateId;

        TAUserInfo getUserInfo() { return userInfo; }
        int getConsolidateDeclarationTemplateId() { return consolidateDeclarationTemplateId; }

        CreateKnfTestData init() {
            int declarationTypeId = 1;

            DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
            departmentDeclarationType.setDeclarationTypeId(declarationTypeId);

            ReportPeriodType reportPeriodType = new ReportPeriodType();
            reportPeriodType.setCode("34");

            DeclarationType declarationType = new DeclarationType();
            declarationType.setId(declarationTypeId);
            declarationType.setName("Тестовый тип налоговой формы");

            DeclarationTemplate declarationTemplate = new DeclarationTemplate();
            declarationTemplate.setType(declarationType);
            declarationTemplate.setDeclarationFormKind(DeclarationFormKind.CONSOLIDATED);
            declarationTemplate.setId(1);

            Department department = new Department();
            department.setName("Тестовое подразделение");

            TaxPeriod taxPeriod = new TaxPeriod();
            taxPeriod.setTaxType(TaxType.NDFL);

            ReportPeriod reportPeriod = new ReportPeriod();
            reportPeriod.setTaxPeriod(taxPeriod);
            reportPeriod.setId(1);

            DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
            drp1.setId(1);
            drp1.setCorrectionDate(new Date(0));
            drp1.setReportPeriod(reportPeriod);
            drp1.setDepartmentId(1);
            drp1.setIsActive(true);

            TARole taRole = new TARole();
            taRole.setAlias(TARole.N_ROLE_CONTROL_UNP);

            TAUser user = new TAUser();
            user.setId(1);
            user.setRoles(Collections.singletonList(taRole));
            userInfo = new TAUserInfo();
            userInfo.setUser(user);

            consolidateDeclarationTemplateId = DeclarationType.NDFL_CONSOLIDATE;

            DeclarationDataScriptingService declarationDataScriptingService = mock(DeclarationDataScriptingService.class);

            PeriodServiceImpl periodServiceImpl = new PeriodServiceImpl();

            when(departmentReportPeriodService.fetchLast(1, 1)).thenReturn(drp1);
            when(departmentReportPeriodFormatter.getPeriodDescription(drp1)).thenReturn("period");
            when(departmentService.getDepartment(1)).thenReturn(department);
            when(declarationTemplateService.getActiveDeclarationTemplateId(consolidateDeclarationTemplateId, 1)).thenReturn(consolidateDeclarationTemplateId);
            when(declarationTemplateService.get(anyInt())).thenReturn(declarationTemplate);
            when(periodService.getPeriodTypeById(anyLong())).thenReturn(reportPeriodType);
            when(periodService.isYearPeriodType(eq(reportPeriodType))).thenReturn(periodServiceImpl.isYearPeriodType(reportPeriodType));
            when(lockDataService.lock(anyString(), anyInt(), anyString())).thenReturn(null);
            when(sourceService.getDDTByDepartment(eq(1), any(TaxType.class), any(Date.class), any(Date.class)))
                    .thenReturn(Collections.singletonList(departmentDeclarationType));

            when(declarationDataScriptingService.executeScript(
                    eq(userInfo), any(DeclarationData.class), eq(FormDataEvent.CREATE), any(Logger.class), any(Map.class))
            ).thenReturn(true);
            when(declarationDataScriptingService.executeScript(
                    eq(userInfo), any(DeclarationData.class), eq(FormDataEvent.AFTER_CREATE), any(Logger.class), any(Map.class))
            ).thenReturn(true);
            return this;
        }
    }
}
