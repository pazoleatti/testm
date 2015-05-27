package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.script.impl.FormDataCompositionServiceImpl;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("FormDataServiceTest.xml")
public class FormDataServiceTest {

    private FormTemplate formTemplate;
    @Autowired
    private FormDataDao formDataDao;
    @Autowired
    private DataRowDao dataRowDao;
    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;
    @Autowired
    DepartmentService departmentService;

    @Autowired
    private FormDataServiceImpl formDataService;
    @Autowired
    PeriodService periodService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    SourceService sourceService;
    @Autowired
    FormTypeService formTypeService;
    @Autowired
    TAUserService userService;
    @Autowired
    ReportService reportService;

    private static final int FORM_TEMPLATE_ID = 1;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Before
    public void init() {
        // Макет
        formTemplate = new FormTemplate();
        formTemplate.setId(FORM_TEMPLATE_ID);

        // Тип формы
        FormType formType = new FormType();
        formType.setName(TaxType.INCOME.getName());
        formTemplate.setType(formType);

        // Налоговый период
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2014);
        taxPeriod.setTaxType(TaxType.INCOME);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(taxPeriod);

        FormData formData = mock(FormData.class);
        when(formData.getReportPeriodId()).thenReturn(1);

		TAUser user = new TAUser();
		user.setId(666);
		user.setLogin("MockUser");
		when(userService.getUser(666)).thenReturn(user);
		ReflectionTestUtils.setField(formDataService, "userService", userService);
    }
    /**
     * Тест удаления приемника при распринятии последнего источника
     */
    @Test
    public void compose() {
        FormDataCompositionService formDataCompositionService = mock(FormDataCompositionServiceImpl.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(FormDataCompositionService.class)).thenReturn(formDataCompositionService);
        ReflectionTestUtils.setField(formDataService, "applicationContext", applicationContext);
        // текущая форм дата будет вызывать compose на своих приемниках
        final FormData formData = new FormData();
        formData.setId(1l);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        FormType formType = new FormType();
        formType.setId(1);
        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setManual(false);
        formData.setState(WorkflowState.CREATED);

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        user.setName("user");
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");
        Logger logger = new Logger();

        // имеем 3 формы, 2 источника и 1 приемник
        // готовим тип формы для
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setDepartmentId(1);
        departmentFormType.setFormTypeId(1);
        departmentFormType.setId(1);
        departmentFormType.setKind(FormDataKind.PRIMARY);
        final List<DepartmentFormType> list = new CopyOnWriteArrayList<DepartmentFormType>();

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        reportPeriod.setCalendarStartDate(new Date());
        reportPeriod.setName("1 квартал");
        reportPeriod.setId(2);

        when(departmentFormTypeDao.getFormDestinations(2, 1, FormDataKind.PRIMARY, null, null)).thenReturn(new ArrayList<DepartmentFormType>());
        when(departmentFormTypeDao.getFormDestinations(1, 1, FormDataKind.PRIMARY, null, null)).thenReturn(list);
        when(periodService.getReportPeriod(any(Integer.class))).thenReturn(reportPeriod);

        Department department = new Department();
        department.setName("Тестовое подразделение");

        final FormData formData1 = new FormData();
        formData1.setId(2L);
        formData1.setFormType(formType);
        formData1.setKind(FormDataKind.CONSOLIDATED);
        formData1.setDepartmentId(1);
        when(formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(), 1, null)).thenReturn(formData1);
        when(formDataDao.get(formData1.getId(), false)).thenReturn(formData);
        when(departmentService.getDepartment(formData1.getDepartmentId())).thenReturn(department);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                list.add(new DepartmentFormType());
                return null;
            }
        }).when(sourceService).addFormDataConsolidationInfo(
                anyLong(),
                anyCollectionOf(Long.class));

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setId(1);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(1);
        departmentReportPeriod.setBalance(false);
        departmentReportPeriod.setActive(true);
        formData.setDepartmentReportPeriodId(departmentReportPeriod.getId());

        when(departmentReportPeriodService.getLast(anyInt(), anyInt())).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodService.get(anyInt())).thenReturn(departmentReportPeriod);

        when(formDataDao.getLast(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), anyInt())).thenReturn(formData1);
        when(userService.getUser(user.getId())).thenReturn(user);

        final Map<String, LockData> map = new HashMap<String, LockData>();
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] arguments = invocation.getArguments();
                map.put((String) arguments[0], new LockData((String) arguments[0], (Integer) arguments[1], new Date()));
                return null;
            }
        }).when(lockDataService).lock(anyString(), anyInt(), anyString(), anyString(), anyInt());
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                map.remove(invocation.getArguments()[0]);
                return null;
            }
        }).when(lockDataService).unlock(anyString(), anyInt());


        ArrayList<DepartmentFormType> dftSources = new ArrayList<DepartmentFormType>();
        DepartmentFormType dft1 = new DepartmentFormType();
        dft1.setDepartmentId(1);
        dft1.setFormTypeId(1);
        dft1.setKind(FormDataKind.ADDITIONAL);
        DepartmentFormType dft2 = new DepartmentFormType();
        dft2.setDepartmentId(2);
        dft2.setFormTypeId(2);
        dft2.setKind(FormDataKind.CONSOLIDATED);
        dftSources.add(dft1);
        dftSources.add(dft2);
        when(departmentService.getDepartment(dft1.getDepartmentId())).thenReturn(department);
        when(departmentService.getDepartment(dft2.getDepartmentId())).thenReturn(department);
        when(departmentFormTypeDao.getFormSources(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getCalendarStartDate(),
                reportPeriod.getEndDate())).thenReturn(dftSources);

        FormData formDataDest = new FormData();
        formDataDest.setId(3l);
        formDataDest.setReportPeriodId(2);
        formDataDest.setDepartmentId(1);
        FormType formType1 = new FormType();
        formType1.setId(2);
        formType1.setName("РНУ");
        formDataDest.setFormType(formType);
        formDataDest.setKind(FormDataKind.PRIMARY);
        formDataDest.setManual(false);
        formDataDest.setState(WorkflowState.ACCEPTED);
        formDataDest.setDepartmentReportPeriodId(3);
        formDataDest.setFormType(formType1);
        when(formDataDao.find(dft1.getFormTypeId(), dft1.getKind(), formData.getDepartmentReportPeriodId(), null)).thenReturn(formDataDest);
        when(formDataDao.find(dft2.getFormTypeId(), dft2.getKind(), formData.getDepartmentReportPeriodId(), null)).thenReturn(formDataDest);
        when(formTypeService.get(dft1.getFormTypeId())).thenReturn(formType1);
        when(formTypeService.get(dft2.getFormTypeId())).thenReturn(formType1);
        when(formDataDao.get(formData.getId(), false)).thenReturn(formData);

        formDataService.compose(formData, userInfo, logger);
        // проверяем что источник удален
        Assert.assertTrue(list.size() == 1);
    }

    @Test
    public void existFormDataTest() throws ParseException {

        Logger logger = new Logger();

        FormType formType = new FormType();
        formType.setId(1);
        formType.setName("Тестовый тип НФ");

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setType(formType);
        formTemplate.setId(1);

        FormData formData = new FormData(formTemplate);
        formData.setState(WorkflowState.CREATED);
        formData.setKind(FormDataKind.SUMMARY);
        formData.setDepartmentId(1);
        formData.setReportPeriodId(1);
        formData.setId(1l);
        formData.setDepartmentReportPeriodId(1);

        FormData formData1 = new FormData(formTemplate);
        formData1.setState(WorkflowState.CREATED);
        formData1.setKind(FormDataKind.SUMMARY);
        formData1.setDepartmentId(1);
        formData1.setReportPeriodId(2);
        formData1.setId(2l);
        formData1.setDepartmentReportPeriodId(2);

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

        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        when(departmentReportPeriodService.get(formData.getDepartmentReportPeriodId())).thenReturn(drp);
        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("01.01.2014"));
        when(departmentReportPeriodService.get(formData1.getDepartmentReportPeriodId())).thenReturn(drp1);

        when(formDataDao.getFormDataIds(1, FormDataKind.SUMMARY, 1)).thenReturn(list);
        when(formDataDao.getWithoutRows(1)).thenReturn(formData);
        when(formDataDao.getWithoutRows(2)).thenReturn(formData1);

        when(periodService.getReportPeriod(1)).thenReturn(reportPeriod);
        when(periodService.getReportPeriod(2)).thenReturn(reportPeriod1);

        when(departmentService.getDepartment(1)).thenReturn(department);

        Assert.assertTrue(formDataService.existFormData(1, FormDataKind.SUMMARY, 1, logger));
        assertEquals(
                "Существует экземпляр налоговой формы \"Тестовый тип НФ\" типа \"Сводная\" в подразделении \"Тестовое подразделение\" в периоде \"Тестовый период 2014\"",
                logger.getEntries().get(0).getMessage()
        );
        assertEquals(
                "Существует экземпляр налоговой формы \"Тестовый тип НФ\" типа \"Сводная\" в подразделении \"Тестовое подразделение\" в периоде \"Второй тестовый период 2014\"",
                logger.getEntries().get(1).getMessage()
        );
    }

    /**
     * Тестирование случая, когда экземпляр НФ является первым в сквозной нумерации
     */
    @Test
    public void getPreviousRowNumberFirstCase() {

        // Создаваемая форма
        FormData newFormData = new FormData(formTemplate);
        newFormData.setId((long) 1);
        newFormData.setReportPeriodId(1);
        newFormData.setDepartmentReportPeriodId(1);
        newFormData.setKind(FormDataKind.PRIMARY);

        when(formDataDao.getPrevFormDataList(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(new ArrayList<FormData>());
        when(periodService.getReportPeriod(1)).thenReturn(new ReportPeriod());

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);

        Assert.assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 0",
                formDataService.getPreviousRowNumber(newFormData).equals(0));
    }

    /**
     * Тестирование случая, когда экземпляр НФ не является первым и предыдущие экземпляры НФ в состоянии отличном от
     * "Создана"
     */
    @Test
    public void getPreviousRowNumberSecondCase() {
        // Существующие формы
        FormData formData = new FormData(formTemplate);
        formData.setId((long) 1);
        formData.setReportPeriodId(1);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setDepartmentReportPeriodId(1);
        formData.setState(WorkflowState.ACCEPTED);

        FormData formData1 = new FormData(formTemplate);
        formData1.setId((long) 2);
        formData1.setReportPeriodId(2);
        formData1.setKind(FormDataKind.PRIMARY);
        formData1.setDepartmentReportPeriodId(1);
        formData1.setState(WorkflowState.ACCEPTED);

        // Создаваемая форма
        FormData newFormData = new FormData(formTemplate);
        newFormData.setId((long) 3);
        newFormData.setReportPeriodId(3);
        newFormData.setKind(FormDataKind.PRIMARY);
        newFormData.setDepartmentReportPeriodId(1);
        newFormData.setState(WorkflowState.CREATED);

        List<FormData> formDataList = new ArrayList<FormData>();
        formDataList.add(formData);
        formDataList.add(formData1);

        when(formDataDao.getPrevFormDataList(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(formDataList);
        when(dataRowDao.getTempSizeWithoutTotal(formData)).thenReturn(3);
        when(dataRowDao.getTempSizeWithoutTotal(formData1)).thenReturn(5);
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);
        when(periodService.getReportPeriod(3)).thenReturn(new ReportPeriod());
        Assert.assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 8",
                formDataService.getPreviousRowNumber(newFormData).equals(8));
    }

    /**
     * Тестирование случая, когда экземпляр НФ не является первым и существуют предыдущие экземпляры НФ в состоянии
     * "Создана"
     */
    @Test
    public void getPreviousRowNumberThirdCase() {

        // Существующие формы
        FormData formData = new FormData(formTemplate);
        formData.setId((long) 1);
        formData.setReportPeriodId(1);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setDepartmentReportPeriodId(1);
        formData.setState(WorkflowState.ACCEPTED);

        FormData formData1 = new FormData(formTemplate);
        formData1.setId((long) 2);
        formData1.setReportPeriodId(2);
        formData1.setKind(FormDataKind.PRIMARY);
        formData1.setDepartmentReportPeriodId(1);
        formData1.setState(WorkflowState.CREATED);

        // Создаваемая форма
        FormData newFormData = new FormData(formTemplate);
        newFormData.setId((long) 3);
        newFormData.setReportPeriodId(3);
        newFormData.setKind(FormDataKind.PRIMARY);
        newFormData.setDepartmentReportPeriodId(1);
        newFormData.setState(WorkflowState.CREATED);

        List<FormData> formDataList = new ArrayList<FormData>();
        formDataList.add(formData);
        formDataList.add(formData1);

        when(formDataDao.getPrevFormDataList(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(formDataList);
        when(dataRowDao.getTempSizeWithoutTotal(formData)).thenReturn(3);
        when(dataRowDao.getTempSizeWithoutTotal(formData1)).thenReturn(5);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);
        when(periodService.getReportPeriod(3)).thenReturn(new ReportPeriod());
        Assert.assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 3",
                formDataService.getPreviousRowNumber(newFormData).equals(3));
    }

    /**
     * Проверка обновления атрибутов "Номер последней строки предыдущей НФ" при сохранении
     * 1. Не учитывается в сквозной нумерации
     * 2. Количество строк в табличной части до и после редактирования не изменилось
     */
    @Test
    public void testUpdatePreviousRowNumberAttrWhenSave1() {
        FormData formData = mock(FormData.class);
        FormDataServiceImpl dataService = spy(formDataService);

        doReturn(false).when(dataService).beInOnAutoNumeration(any(WorkflowState.class), any(DepartmentReportPeriod.class));
        doReturn(false).when(dataRowDao).isDataRowsCountChanged(anyLong());

        dataService.updatePreviousRowNumberAttr(formData, eq(any(Logger.class)));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class), any(Logger.class));
    }

    /**
     * Проверка обновления атрибутов "Номер последней строки предыдущей НФ" при сохранении
     * 1. Учитывается в сквозной нумерации
     * 2. Количество строк в табличной части до и после редактирования не изменилось
     */
    @Test
     public void testUpdatePreviousRowNumberAttrWhenSave2() {
        FormData formData = mock(FormData.class);
        FormDataServiceImpl dataService = spy(formDataService);

        doReturn(true).when(dataService).beInOnAutoNumeration(any(WorkflowState.class), any(DepartmentReportPeriod.class));
        doReturn(false).when(dataRowDao).isDataRowsCountChanged(anyLong());
        doReturn(1L).when(formData).getId();

        dataService.updatePreviousRowNumberAttr(formData, eq(any(Logger.class)));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class), any(Logger.class));
    }

    /**
     * Проверка обновления атрибутов "Номер последней строки предыдущей НФ" при сохранении
     * 1. Учитывается в сквозной нумерации (статус "Создана")
     * 2. Количество строк в табличной части до и после редактирования изменилось
     */
    @Test
    public void testUpdatePreviousRowNumberAttrWhenSave3() {
        FormData formData = mock(FormData.class);
        FormDataServiceImpl dataService = spy(formDataService);
        doReturn(true).when(dataService).beInOnAutoNumeration(any(WorkflowState.class), any(DepartmentReportPeriod.class));
        doReturn(true).when(dataRowDao).isDataRowsCountChanged(anyLong());
        doReturn(1L).when(formData).getId();
        doReturn(WorkflowState.CREATED).when(formData).getState();

        dataService.updatePreviousRowNumberAttr(formData, eq(any(Logger.class)));
        verify(dataService, times(1)).updatePreviousRowNumber(any(FormData.class), any(Logger.class));
    }

    /**
     * Проверка обновления атрибутов "Номер последней строки предыдущей НФ" при переходе по ЖЦ
     * 1. Переход не инициирует обновление атрибутов
     */
    @Test
    public void testUpdatePreviousRowNumberAttrWhenDoMove1() {
        FormDataServiceImpl dataService = spy(formDataService);

        doReturn(false).when(dataService).canUpdatePreviousRowNumberWhenDoMove(any(WorkflowMove.class));

        dataService.updatePreviousRowNumberAttr(any(FormData.class), any(WorkflowMove.class), any(Logger.class));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class), any(Logger.class));
    }

    /**
     * Проверка обновления атрибутов "Номер последней строки предыдущей НФ" при переходе по ЖЦ
     * 1. Переход инициирует обновление атрибутов
     */
    @Test
    public void testUpdatePreviousRowNumberAttrWhenDoMove2() {
        FormData formData = mock(FormData.class);
        Logger logger = mock(Logger.class);
        FormDataServiceImpl dataService = spy(formDataService);

        doReturn(true).when(dataService).canUpdatePreviousRowNumberWhenDoMove(any(WorkflowMove.class));

        dataService.updatePreviousRowNumberAttr(formData, WorkflowMove.ACCEPTED_TO_APPROVED, logger);
        verify(dataService, times(1)).updatePreviousRowNumber(any(FormData.class), any(Logger.class));
    }

    /**
     * При сохранении экземпляра НФ обновление атрибута "Номер последней строки предыдущей НФ" важен порядок вызова методов.
     */
    @Test
    public void testSaveFormDataMethodsInvokeOrder() {
        TAUser user = new TAUser();
        user.setId(1);
        Logger logger = mock(Logger.class);
        TAUserInfo userInfo = mock(TAUserInfo.class);
        when(userInfo.getUser()).thenReturn(user);
        FormData formData = getFormData();
        formData.setDepartmentReportPeriodId(1);
        LockData lockData = new LockData();
        lockData.setUserId(user.getId());

        when(lockDataService.getLock(LockData.LockObjects.FORM_DATA.name() + "_" + formData.getId())).
                thenReturn(lockData);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);

        FormDataServiceImpl dataService = spy(formDataService);
        dataService.saveFormData(logger, userInfo, formData);

        InOrder inOrder = inOrder(dataService, formDataDao);

        inOrder.verify(dataService, times(1)).updatePreviousRowNumberAttr(formData, logger);
        inOrder.verify(formDataDao, times(1)).save(formData);
    }

    /**
     * "Номер последней строки предыдущей НФ" обновляется для последующих экземпляров НФ текущей сквозной нумерации
     * только при переходах по ЖЦ:
     * 1. из состояния "Создана" в любое состояние
     * 2. из любого состояния в состояние "Создана"
     */
    @Test
    public void testCanUpdateAutoNumerationWhenDoMove() {
        Assert.assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.CREATED_TO_PREPARED));
        Assert.assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.CREATED_TO_ACCEPTED));
        Assert.assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.CREATED_TO_APPROVED));
        Assert.assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.PREPARED_TO_CREATED));
        Assert.assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.ACCEPTED_TO_CREATED));
        Assert.assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.APPROVED_TO_CREATED));

        Assert.assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.PREPARED_TO_ACCEPTED));
        Assert.assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.PREPARED_TO_APPROVED));
        Assert.assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.ACCEPTED_TO_APPROVED));
        Assert.assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.ACCEPTED_TO_PREPARED));
        Assert.assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.APPROVED_TO_ACCEPTED));
        Assert.assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.APPROVED_TO_PREPARED));
    }

    /**
     * Экземпляры в статусе "Создана" не участвуют в сквозной нумерации
     */
    @Test
    public void testBeInOnAutoNumeration() {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();

        FormData formData = new FormData();
        formData.setState(WorkflowState.CREATED);
        Assert.assertFalse("Не должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        departmentReportPeriod.setCorrectionDate(new Date());
        formData.setState(WorkflowState.ACCEPTED);
        Assert.assertFalse("Не должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        departmentReportPeriod.setCorrectionDate(null);
        formData.setState(WorkflowState.ACCEPTED);
        Assert.assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        formData.setState(WorkflowState.APPROVED);
        Assert.assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        formData.setState(WorkflowState.PREPARED);
        Assert.assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));
    }

    @Test
    public void getPreviousFormDataCorrectionTest() {
        // FormType
        FormType formType = new FormType();
        formType.setId(1);
        // FormTemplate
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        formTemplate.setType(formType);
        // FormData
        FormData formData1 = new FormData(formTemplate);
        formData1.setId(1L);
        formData1.setKind(FormDataKind.PRIMARY);
        // ReportPeriod
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);

        DepartmentReportPeriod departmentReportPeriod1 = new DepartmentReportPeriod();
        DepartmentReportPeriod departmentReportPeriod2 = new DepartmentReportPeriod();
        DepartmentReportPeriod departmentReportPeriod3 = new DepartmentReportPeriod();
        departmentReportPeriod1.setId(1);
        departmentReportPeriod2.setId(2);
        departmentReportPeriod3.setId(3);
        departmentReportPeriod1.setReportPeriod(reportPeriod);
        departmentReportPeriod2.setReportPeriod(reportPeriod);
        departmentReportPeriod3.setReportPeriod(reportPeriod);
        departmentReportPeriod1.setDepartmentId(1);
        departmentReportPeriod2.setDepartmentId(1);
        departmentReportPeriod3.setDepartmentId(1);
        departmentReportPeriod1.setCorrectionDate(new GregorianCalendar(2014, 3, 3).getTime());
        departmentReportPeriod2.setCorrectionDate(new GregorianCalendar(2014, 2, 2).getTime());
        departmentReportPeriod3.setCorrectionDate(new GregorianCalendar(2014, 1, 1).getTime());

        FormData formData2 = new FormData();
        formData2.setState(WorkflowState.ACCEPTED);
        formData2.setId(2L);

        FormData formData3 = new FormData();
        formData3.setState(WorkflowState.ACCEPTED);
        formData3.setId(3L);

        when(formDataService.findFormData(1, FormDataKind.PRIMARY, 1, null)).thenReturn(formData1);
        when(formDataService.findFormData(1, FormDataKind.PRIMARY, 2, null)).thenReturn(formData2);
        when(formDataService.findFormData(1, FormDataKind.PRIMARY, 3, null)).thenReturn(formData3);

        List<DepartmentReportPeriod> periodList = Arrays.asList(departmentReportPeriod1,
                departmentReportPeriod2, departmentReportPeriod3);

        Collections.sort(periodList, new Comparator<DepartmentReportPeriod>() {
            @Override
            public int compare(DepartmentReportPeriod o1, DepartmentReportPeriod o2) {
                if (o1.getCorrectionDate() == null) {
                    return -1;
                }
                if (o2.getCorrectionDate() == null) {
                    return 1;
                }
                return o1.getCorrectionDate().compareTo(o2.getCorrectionDate());
            }
        });
        // 1
        FormData prevFormData = formDataService.getPreviousFormDataCorrection(formData1, periodList, departmentReportPeriod1);
        Assert.assertNotNull(prevFormData);
        assertEquals(2, prevFormData.getId().intValue());
        // 2
        prevFormData = formDataService.getPreviousFormDataCorrection(formData1, periodList, departmentReportPeriod2);
        Assert.assertNotNull(prevFormData);
        assertEquals(3, prevFormData.getId().intValue());
        // 3
        prevFormData = formDataService.getPreviousFormDataCorrection(formData1, periodList, departmentReportPeriod3);
        Assert.assertNull(prevFormData);
    }

    @Test
    public void testGetPrevPeriodFormData() {
        FormTemplate formTemplate = new FormTemplate();
        FormType formType = new FormType();
        formType.setId(111);
        formTemplate.setType(formType);

        FormDataKind kind = FormDataKind.PRIMARY;

        DepartmentReportPeriod departmentReportPeriodPrev = new DepartmentReportPeriod();
        ReportPeriod reportPeriodPrev = new ReportPeriod();
        reportPeriodPrev.setId(0);
        reportPeriodPrev.setTaxPeriod(new TaxPeriod());
        departmentReportPeriodPrev.setId(0);
        departmentReportPeriodPrev.setReportPeriod(reportPeriodPrev);
        departmentReportPeriodPrev.setDepartmentId(1);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setId(1);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(1);

        FormData formData1 = new FormData();
        formData1.setId(1L);

        when(periodService.getReportPeriod(1)).thenReturn(reportPeriod);
        when(periodService.getPrevReportPeriod(1)).thenReturn(reportPeriodPrev);
        when(departmentReportPeriodService.getLast(1, 0)).thenReturn(departmentReportPeriodPrev);
        when(formDataDao.find(111, kind, 0, null)).thenReturn(formData1);

        FormData formDataOld = formDataService.getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, null);
        assertEquals(formDataOld.getId(), formData1.getId());
    }

    @Test
    public void testGetPrevPeriodFormData2() {
        FormTemplate formTemplate = new FormTemplate();
        FormType formType = new FormType();
        formType.setId(111);
        formTemplate.setType(formType);

        FormDataKind kind = FormDataKind.PRIMARY;
        Integer periodOrder = 1;
        Integer periodOrder2 = 2;

        List<Months> monthsList = new ArrayList<Months>();
        monthsList.add(null);
        for (int i = 0; i <= 11; ++i) {
            monthsList.add(Months.values()[i]);
        }

        DepartmentReportPeriod departmentReportPeriodPrev = new DepartmentReportPeriod();
        ReportPeriod reportPeriodPrev = new ReportPeriod();
        reportPeriodPrev.setId(0);
        reportPeriodPrev.setTaxPeriod(new TaxPeriod());
        departmentReportPeriodPrev.setId(0);
        departmentReportPeriodPrev.setReportPeriod(reportPeriodPrev);
        departmentReportPeriodPrev.setDepartmentId(1);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setId(1);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(1);

        FormData formData1 = new FormData();
        formData1.setId(1L);
        FormData formData2 = new FormData();
        formData1.setId(2L);

        when(periodService.getAvailableMonthList(0)).thenReturn(monthsList);
        when(periodService.getAvailableMonthList(1)).thenReturn(monthsList);
        when(periodService.getReportPeriod(1)).thenReturn(reportPeriod);
        when(periodService.getPrevReportPeriod(1)).thenReturn(reportPeriodPrev);
        when(departmentReportPeriodService.getLast(1, 0)).thenReturn(departmentReportPeriodPrev);
        when(formDataDao.find(111, kind, 0, new Integer(12))).thenReturn(formData1);
        when(formDataDao.find(111, kind, 1, new Integer(1))).thenReturn(formData2);

        FormData formDataOld = formDataService.getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, periodOrder);
        FormData formDataOld2 = formDataService.getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, periodOrder2);

        assertEquals(formDataOld.getId(), formData1.getId());
        assertEquals(formDataOld2.getId(), formData2.getId());
    }

    private FormData getFormData() {
        FormType formType = new FormType();
        formType.setId(1);

        FormData formData = new FormData();
        formData.setId(1L);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setManual(false);
        return formData;
    }

    @Test(expected = ServiceException.class)
    public void findFormDataIdsByRangeInReportPeriodTest() throws ParseException {
        ArrayList<Integer> a =new ArrayList<Integer>(1);
        a.add(7);

        FormData fd = new FormData();
        fd.setReportPeriodId(17);
        fd.setDepartmentReportPeriodId(17);
        fd.setState(WorkflowState.ACCEPTED);
        fd.setFormTemplateId(1);
        fd.setDepartmentId(0);
        ReportPeriod rp = new ReportPeriod();
        rp.setName("Период");
        TaxPeriod tp = new TaxPeriod();
        tp.setYear(2015);
        rp.setTaxPeriod(tp);
        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        drp.setCorrectionDate(new Date());
        FormTemplate ft = new FormTemplate();
        ft.setName("Вид НФ");

        Logger logger = new Logger();
        when(formDataDao.findFormDataIdsByRangeInReportPeriod(1,
                SIMPLE_DATE_FORMAT.parse("01.01.2012"), SIMPLE_DATE_FORMAT.parse("31.12.2012"))).thenReturn(a);
        when(formDataDao.findFormDataIdsByRangeInReportPeriod(1,
                SIMPLE_DATE_FORMAT.parse("01.01.2012"), SIMPLE_DATE_FORMAT.parse("31.12.9999"))).thenReturn(a);
        when(periodService.getReportPeriod(fd.getReportPeriodId())).thenReturn(rp);
        when(departmentReportPeriodService.get(fd.getDepartmentReportPeriodId())).thenReturn(drp);
        when(formTemplateService.get(fd.getFormTemplateId())).thenReturn(ft);
        Department department = new Department();
        department.setName("Филиал");
        when(departmentService.getDepartment(fd.getDepartmentId())).thenReturn(department);
        for (Integer id : a){
            when(formDataDao.getWithoutRows(id)).thenReturn(fd);
        }

        formDataService.findFormDataIdsByRangeInReportPeriod(1,
                SIMPLE_DATE_FORMAT.parse("01.01.2012"), SIMPLE_DATE_FORMAT.parse("31.12.2012"), logger
        );

        if (logger.containsLevel(LogLevel.ERROR)){
            throw new ServiceException(logger.getEntries().get(0).getMessage());
        }
    }

    @Test
    public void findFormDataIdsByRangeInReportPeriodTestNull() throws ParseException {
        Logger logger = new Logger();
        formDataService.findFormDataIdsByRangeInReportPeriod(1,
                SIMPLE_DATE_FORMAT.parse("01.01.2012"), null, logger
        );
    }

	@Test
	public void checkLockedMeTest() throws ParseException {
	    int exceptionCount = 0;

		LockData lockData = new LockData();
		lockData.setUserId(666);
		lockData.setDateBefore(SIMPLE_DATE_FORMAT.parse("30.10.1983"));

		TAUser user = new TAUser();
		user.setId(31);
		user.setLogin("admin");
		try {
			formDataService.checkLockedMe(lockData, user);
		} catch (ServiceException e) {
			assertEquals("Объект заблокирован другим пользователем (\"MockUser\", срок \"00:00 30.10.1983\")", e.getMessage());
			exceptionCount++;
		}
		try {
			formDataService.checkLockedMe(null, user);
		} catch (ServiceException e) {
			assertEquals("Блокировка не найдена. Объект должен быть заблокирован текущим пользователем", e.getMessage());
			exceptionCount++;
		}
		user.setId(666);
		formDataService.checkLockedMe(lockData, user);

		assertEquals(2, exceptionCount);
	}

    @Test
    public void doCheckTest() {
        FormData formData = new FormData();
        formData.setId(1l);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        FormType formType = new FormType();
        formType.setId(1);
        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setManual(false);
        formData.setState(WorkflowState.ACCEPTED);
        formData.setDepartmentReportPeriodId(1);
        formData.setPeriodOrder(null);
        FormDataPerformer performer = new FormDataPerformer();
        performer.setName("Name");
        performer.setPhone("8888888");
        performer.setPrintDepartmentId(8);
        performer.setReportDepartmentName("8/8");
        formData.setPerformer(performer);
        FormDataSigner signer = new FormDataSigner();
        signer.setName("signer1");
        signer.setPosition("Position1");
        signer.setOrd(1);
        formData.setSigners(Arrays.asList(signer));

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");
        Logger logger = new Logger();

        ReportPeriod reportPeriod = new ReportPeriod();
        TaxPeriod tp = new TaxPeriod();
        tp.setYear(2015);
        reportPeriod.setTaxPeriod(tp);
        reportPeriod.setCalendarStartDate(new Date());
        reportPeriod.setName("1 квартал");
        reportPeriod.setId(2);
        reportPeriod.setStartDate(new Date());
        reportPeriod.setEndDate(new Date());
        when(periodService.getReportPeriod(2)).thenReturn(reportPeriod);

        ArrayList<DepartmentFormType> dftTargets = new ArrayList<DepartmentFormType>(1);
        FormData formDataDest = new FormData();
        formDataDest.setId(3l);
        formDataDest.setReportPeriodId(2);
        formDataDest.setDepartmentId(1);
        FormType formType1 = new FormType();
        formType1.setId(2);
        formType1.setName("РНУ");
        formDataDest.setFormType(formType);
        formDataDest.setKind(FormDataKind.PRIMARY);
        formDataDest.setManual(false);
        formDataDest.setState(WorkflowState.ACCEPTED);
        formDataDest.setDepartmentReportPeriodId(3);
        formDataDest.setFormType(formType1);
        when(formDataDao.get(3l, null)).thenReturn(formDataDest);
        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        when(departmentReportPeriodService.get(formDataDest.getDepartmentReportPeriodId())).thenReturn(drp);
        Department department = new Department();
        department.setName("Тестовое подразделение");
        when(departmentService.getDepartment(formDataDest.getDepartmentId())).thenReturn(department);

        ArrayList<DepartmentFormType> dftSources = new ArrayList<DepartmentFormType>();
        DepartmentFormType dft1 = new DepartmentFormType();
        dft1.setDepartmentId(1);
        dft1.setFormTypeId(1);
        dft1.setKind(FormDataKind.ADDITIONAL);
        DepartmentFormType dft2 = new DepartmentFormType();
        dft2.setDepartmentId(2);
        dft2.setFormTypeId(2);
        dft2.setKind(FormDataKind.CONSOLIDATED);
        dftSources.add(dft1);
        dftSources.add(dft2);
        dftTargets.add(dft2);
        when(departmentFormTypeDao.getFormSources(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getStartDate(),
                reportPeriod.getEndDate())).thenReturn(dftSources);
        when(departmentFormTypeDao.getFormDestinations(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getStartDate(),
                reportPeriod.getEndDate())).thenReturn(dftTargets);
        when(formDataDao.find(dft2.getFormTypeId(), dft2.getKind(), formData.getDepartmentReportPeriodId(), null)).thenReturn(formDataDest);

        when(formTypeService.get(dft1.getFormTypeId())).thenReturn(formType1);

        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setCorrectionDate(new Date(0));
        when(departmentReportPeriodService.get(formData.getDepartmentReportPeriodId())).thenReturn(drp1);

        try{
            formDataService.doCheck(logger, userInfo, formData);
        }catch (ServiceLoggerException e){
            assertEquals(
                    "Не выполнена консолидация данных в форму Тестовое подразделение РНУ Первичная 1 квартал 2015 ",
                    logger.getEntries().get(0).getMessage()
            );
            assertEquals(
                    "Не выполнена консолидация данных из формы Тестовое подразделение РНУ Выходная 1 квартал 2015 с датой сдачи корректировки 01.01.1970 - экземпляр формы не создан",
                    logger.getEntries().get(1).getMessage()
            );
            assertEquals(
                    "Не выполнена консолидация данных из формы Тестовое подразделение РНУ Первичная 1 квартал 2015 с датой сдачи корректировки 01.01.1970 в статусе Принята",
                    logger.getEntries().get(2).getMessage()
            );
        }
    }
}
