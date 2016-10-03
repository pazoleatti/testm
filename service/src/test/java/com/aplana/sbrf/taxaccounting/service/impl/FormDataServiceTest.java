package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.ReferenceCheckResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookCreditRatingsClasses;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookCurrencyMetals;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookUniversal;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.script.impl.FormDataCompositionServiceImpl;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.io.IOUtils;
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

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("FormDataServiceTest.xml")
public class FormDataServiceTest extends Assert{

    private FormTemplate formTemplate;
    @Autowired
    private FormDataDao formDataDao;
    @Autowired
    private DataRowDao dataRowDao;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookFactory refBookFactory;
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
    @Autowired
    FormDataAccessService formDataAccessService;

    private static final int FORM_TEMPLATE_ID = 1;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    TAUserInfo userInfo;

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
        userInfo = new TAUserInfo();
        userInfo.setUser(user);
        when(userService.getUser(666)).thenReturn(user);
		ReflectionTestUtils.setField(formDataService, "userService", userService);

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
		ReflectionTestUtils.setField(formDataService, "tx", tx);
    }
    /**
     * Тест удаления приемника при распринятии последнего источника
     */

    /*

    Тут нереально разобраться, а покрывается только 1 какой то случай

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

        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setMonthly(false);
        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setMonthly(false);

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
        when(formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(), 1, null, null, false)).thenReturn(formData1);
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

        when(formDataDao.getLast(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), anyInt(), any(Integer.class), any(Boolean.class))).thenReturn(formData1);
        when(userService.getUser(user.getId())).thenReturn(user);

        final Map<String, LockData> map = new HashMap<String, LockData>();
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] arguments = invocation.getArguments();
                map.put((String) arguments[0], new LockData((String) arguments[0], (Integer) arguments[1]));
                return null;
            }
        }).when(lockDataService).lock(anyString(), anyInt(), anyString(), anyString());
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
        when(formDataDao.getLast(dft1.getFormTypeId(), dft1.getKind(), formData.getDepartmentId(), 2, null, null, false)).thenReturn(formDataDest);
        when(formDataDao.getLast(dft2.getFormTypeId(), dft2.getKind(), formData.getDepartmentId(), 2, null, null, false)).thenReturn(formDataDest);
        when(formTypeService.get(dft1.getFormTypeId())).thenReturn(formType1);
        when(formTypeService.get(dft2.getFormTypeId())).thenReturn(formType1);
        when(formDataDao.get(formData.getId(), false)).thenReturn(formData);

        when(formTemplateService.existFormTemplate(1, 2, true)).thenReturn(true);
        when(formTemplateService.existFormTemplate(2, 2, true)).thenReturn(true);
        when(formTemplateService.getActiveFormTemplateId(1, 2)).thenReturn(1);
        when(formTemplateService.getActiveFormTemplateId(2, 2)).thenReturn(2);
        when(formTemplateService.get(1)).thenReturn(formTemplate1);
        when(formTemplateService.get(2)).thenReturn(formTemplate2);

        formDataService.compose(formData, userInfo, logger, new LockStateLogger() {
            @Override
            public void updateState(String state) {

            }
        });
        // проверяем что источник удален
        assertTrue(list.size() == 1);
    }*/

    @Test
    public void existFormDataTest() throws ParseException {

        Logger logger = new Logger();

        FormType formType = new FormType();
        formType.setTaxType(TaxType.INCOME);
        formType.setId(1);
        formType.setName("Тестовый тип НФ");

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setType(formType);
        formTemplate.setId(1);
        formTemplate.setName("тестовый");

        FormData formData = new FormData(formTemplate);
        formData.setState(WorkflowState.CREATED);
        formData.setKind(FormDataKind.SUMMARY);
        formData.setDepartmentId(1);
        formData.setReportPeriodId(1);
        formData.setId(1l);
        formData.setDepartmentReportPeriodId(1);
        formData.setComparativePeriodId(1);

        FormData formData1 = new FormData(formTemplate);
        formData1.setState(WorkflowState.CREATED);
        formData1.setKind(FormDataKind.SUMMARY);
        formData1.setDepartmentId(1);
        formData1.setAccruing(true);
        formData1.setReportPeriodId(2);
        formData1.setId(2l);
        formData1.setDepartmentReportPeriodId(2);
        formData1.setManual(true);

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
        reportPeriod1.setAccName("второй квартал (полугодие)");

        Department department = new Department();
        department.setName("Тестовое подразделение");

        List<Long> list = new ArrayList<Long>() {{
            add(1l);
            add(2l);
        }};

        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        drp.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(formData.getDepartmentReportPeriodId())).thenReturn(drp);
        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setReportPeriod(reportPeriod1);
        drp1.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("01.01.2014"));
        when(departmentReportPeriodService.get(formData1.getDepartmentReportPeriodId())).thenReturn(drp1);

        when(formDataDao.getFormDataIds(1, FormDataKind.SUMMARY, 1)).thenReturn(list);
        when(formDataDao.getWithoutRows(1)).thenReturn(formData);
        when(formDataDao.getWithoutRows(2)).thenReturn(formData1);
        when(formTemplateService.get(formTemplate.getId())).thenReturn(formTemplate);

        when(periodService.getReportPeriod(1)).thenReturn(reportPeriod);
        when(periodService.getReportPeriod(2)).thenReturn(reportPeriod1);

        when(departmentService.getDepartment(1)).thenReturn(department);

        assertTrue(formDataService.existFormData(1, FormDataKind.SUMMARY, 1, logger));
        assertEquals(
                "Существует экземпляр налоговых форм: Тип: \"Сводная\", Вид: \"Тестовый тип НФ\", Подразделение: \"Тестовое подразделение\", Период: \"Тестовый период 2014\", Период сравнения: \"Тестовый период 2014\", Версия: \"Автоматическая\"",
                logger.getEntries().get(0).getMessage()
        );
        assertEquals(
                "Существует экземпляр налоговых форм: Тип: \"Сводная\", Вид: \"Тестовый тип НФ\", Подразделение: \"Тестовое подразделение\", Период: \"второй квартал (полугодие) 2014\", Дата сдачи корректировки: 01.01.2014, Версия: \"Абсолютные значения\"",
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

        assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 0",
				formDataService.getPreviousRowNumber(newFormData, null).equals(0));
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
        newFormData.setState(WorkflowState.APPROVED);

        List<FormData> formDataList = new ArrayList<FormData>();
        formDataList.add(formData);
        formDataList.add(formData1);

        when(formDataDao.getPrevFormDataList(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(formDataList);
        when(dataRowDao.getAutoNumerationRowCount(formData)).thenReturn(3);
        when(dataRowDao.getAutoNumerationRowCount(formData1)).thenReturn(5);
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);
        when(periodService.getReportPeriod(3)).thenReturn(new ReportPeriod());
        assertEquals(Integer.valueOf(8), formDataService.getPreviousRowNumber(newFormData, null));
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
        when(dataRowDao.getAutoNumerationRowCount(formData)).thenReturn(3);
        when(dataRowDao.getAutoNumerationRowCount(formData1)).thenReturn(5);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);
        when(periodService.getReportPeriod(3)).thenReturn(new ReportPeriod());
        assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 3",
				formDataService.getPreviousRowNumber(newFormData, null).equals(3));
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
        doReturn(false).when(dataRowDao).isDataRowsCountChanged((FormData) anyObject());

        dataService.updateAutoNumeration(formData, eq(any(Logger.class)), any(TAUserInfo.class));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class), any(Logger.class), any(TAUserInfo.class), eq(true), anyBoolean(), any(LockStateLogger.class));
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
        doReturn(false).when(dataRowDao).isDataRowsCountChanged((FormData) anyObject());
        doReturn(1L).when(formData).getId();

        dataService.updateAutoNumeration(formData, eq(any(Logger.class)), any(TAUserInfo.class));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class), any(Logger.class), any(TAUserInfo.class), eq(true), anyBoolean(), any(LockStateLogger.class));
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
        doReturn(true).when(dataRowDao).isDataRowsCountChanged((FormData) anyObject());
        doReturn(1L).when(formData).getId();
        doReturn(WorkflowState.CREATED).when(formData).getState();

        dataService.updateAutoNumeration(formData, eq(any(Logger.class)), any(TAUserInfo.class));
        verify(dataService, times(1)).updatePreviousRowNumber(any(FormData.class), any(Logger.class), any(TAUserInfo.class), eq(true), anyBoolean(), any(LockStateLogger.class));
    }

    /**
     * Проверка обновления атрибутов "Номер последней строки предыдущей НФ" при переходе по ЖЦ
     * 1. Переход не инициирует обновление атрибутов
     */
    @Test
    public void testUpdatePreviousRowNumberAttrWhenDoMove1() {
        FormDataServiceImpl dataService = spy(formDataService);

        doReturn(false).when(dataService).canUpdatePreviousRowNumberWhenDoMove(any(WorkflowMove.class));

        dataService.updatePreviousRowNumberAttr(any(FormData.class), any(WorkflowMove.class), any(Logger.class), any(TAUserInfo.class), any(LockStateLogger.class));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class), any(Logger.class), any(TAUserInfo.class), eq(false), anyBoolean(), any(LockStateLogger.class));
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

        dataService.updatePreviousRowNumberAttr(formData, WorkflowMove.ACCEPTED_TO_APPROVED, logger, userInfo, null);
        verify(dataService, times(1)).updatePreviousRowNumber(any(FormData.class), any(Logger.class), any(TAUserInfo.class), eq(false), anyBoolean(), any(LockStateLogger.class));
    }

    @Test
    public void testDoMove() {
        FormType type = new FormType();
        type.setName("form_type");
        FormData formData = mock(FormData.class);
        when(formData.getId()).thenReturn(1l);
        when(formData.getFormColumns()).thenReturn(new ArrayList<Column>(0));
        when(formData.getFormType()).thenReturn(type);
        when(formData.getKind()).thenReturn(FormDataKind.CONSOLIDATED);
        when(formData.getDepartmentId()).thenReturn(1);
        when(formData.getPeriodOrder()).thenReturn(1);
        when(formData.getDepartmentReportPeriodId()).thenReturn(1);
        when(formData.getReportPeriodId()).thenReturn(1);
        when(formData.isSorted()).thenReturn(false);

        Logger logger = new Logger();
        Department department = new Department();
        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        ReportPeriod rp = new ReportPeriod();
        rp.setName("report");
        Date startDate = new Date(0);
        Date endDate = new Date(1000);
        rp.setCalendarStartDate(startDate);
        rp.setEndDate(endDate);
        TaxPeriod tp = new TaxPeriod();
        tp.setYear(2015);
        rp.setTaxPeriod(tp);
        drp.setReportPeriod(rp);
        when(departmentReportPeriodService.get(formData.getDepartmentReportPeriodId())).thenReturn(drp);

        LockData lockData = new LockData();
        lockData.setUserId(userInfo.getUser().getId());

        when(formDataAccessService.getAvailableMoves(userInfo, formData.getId())).thenReturn(new ArrayList<WorkflowMove>(){{add(WorkflowMove.APPROVED_TO_ACCEPTED);}});
        when(formDataDao.get(formData.getId(), false)).thenReturn(formData);
        when(departmentService.getDepartment(formData.getDepartmentId())).thenReturn(department);
        when(periodService.getReportPeriod(formData.getReportPeriodId())).thenReturn(rp);
        when(lockDataService.lock(formDataService.generateTaskKey(formData.getId(), ReportType.EDIT_FD), userInfo.getUser().getId(), "FORM_DATA")).
                thenReturn(lockData);
        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setFixedRows(false);
        when(formTemplateService.get(any(Integer.class))).thenReturn(formTemplate1);

        FormDataScriptingService scriptingService = mock(FormDataScriptingService.class);
        // в скриптах реализована сортировка
        when(scriptingService.executeScript(any(TAUserInfo.class), any(FormData.class), any(FormDataEvent.class), any(Logger.class), any(Map.class))).thenReturn(true);
        ReflectionTestUtils.setField(formDataService, "formDataScriptingService", scriptingService);

        formDataService.doMove(formData.getId(), false, userInfo, WorkflowMove.APPROVED_TO_ACCEPTED, "", logger, true, new LockStateLogger() {
            @Override
            public void updateState(String state) {
            }
        });

        assertEquals(1, logger.getEntries().size());
        assertEquals("Выполнена сортировка строк налоговой формы.", logger.getEntries().get(0).getMessage());

        logger = new Logger();
        // в скриптах не реализована сортировка
        when(scriptingService.executeScript(any(TAUserInfo.class), any(FormData.class), any(FormDataEvent.class), any(Logger.class), any(Map.class))).thenReturn(false);
        formDataService.doMove(formData.getId(), false, userInfo, WorkflowMove.APPROVED_TO_ACCEPTED, "", logger, true, new LockStateLogger() {
            @Override
            public void updateState(String state) {
            }
        });

        assertEquals(0, logger.getEntries().size());
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

        when(lockDataService.getLock(formDataService.generateTaskKey(formData.getId(), ReportType.EDIT_FD))).
                thenReturn(lockData);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(new TaxPeriod());
        departmentReportPeriod.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);

        FormDataServiceImpl dataService = spy(formDataService);
        dataService.saveFormData(logger, userInfo, formData, false);

        InOrder inOrder = inOrder(dataService, formDataDao);

        inOrder.verify(dataService, times(1)).updateAutoNumeration(formData, logger, userInfo);
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
        assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.CREATED_TO_PREPARED));
        assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.CREATED_TO_ACCEPTED));
        assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.CREATED_TO_APPROVED));
        assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.PREPARED_TO_CREATED));
        assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.ACCEPTED_TO_CREATED));
        assertTrue(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.APPROVED_TO_CREATED));

        assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.PREPARED_TO_ACCEPTED));
        assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.PREPARED_TO_APPROVED));
        assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.ACCEPTED_TO_APPROVED));
        assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.ACCEPTED_TO_PREPARED));
        assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.APPROVED_TO_ACCEPTED));
        assertFalse(formDataService.canUpdatePreviousRowNumberWhenDoMove(WorkflowMove.APPROVED_TO_PREPARED));
    }

    /**
     * Экземпляры в статусе "Создана" не участвуют в сквозной нумерации
     */
    @Test
    public void testBeInOnAutoNumeration() {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();

        FormData formData = new FormData();
        formData.setState(WorkflowState.CREATED);
        assertFalse("Не должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        departmentReportPeriod.setCorrectionDate(new Date());
        formData.setState(WorkflowState.ACCEPTED);
        assertFalse("Не должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        departmentReportPeriod.setCorrectionDate(null);
        formData.setState(WorkflowState.ACCEPTED);
        assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        formData.setState(WorkflowState.APPROVED);
        assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));

        formData = new FormData();
        formData.setState(WorkflowState.PREPARED);
        assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData.getState(), departmentReportPeriod));
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

        when(formDataService.findFormData(1, FormDataKind.PRIMARY, 1, null, null, false)).thenReturn(formData1);
        when(formDataService.findFormData(1, FormDataKind.PRIMARY, 2, null, null, false)).thenReturn(formData2);
        when(formDataService.findFormData(1, FormDataKind.PRIMARY, 3, null, null, false)).thenReturn(formData3);

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
        assertNotNull(prevFormData);
        assertEquals(2, prevFormData.getId().intValue());
        // 2
        prevFormData = formDataService.getPreviousFormDataCorrection(formData1, periodList, departmentReportPeriod2);
        assertNotNull(prevFormData);
        assertEquals(3, prevFormData.getId().intValue());
        // 3
        prevFormData = formDataService.getPreviousFormDataCorrection(formData1, periodList, departmentReportPeriod3);
        assertNull(prevFormData);
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
        when(formDataDao.find(111, kind, 0, null, null, false)).thenReturn(formData1);

        FormData formDataOld = formDataService.getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, null, null, false);
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
        when(formDataDao.find(111, kind, 0, new Integer(12), null, false)).thenReturn(formData1);
        when(formDataDao.find(111, kind, 1, new Integer(1), null, false)).thenReturn(formData2);

        FormData formDataOld = formDataService.getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, periodOrder, null, false);
        FormData formDataOld2 = formDataService.getPrevPeriodFormData(formTemplate, departmentReportPeriod, kind, periodOrder2, null, false);

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

        FormType type = new FormType();
        type.setName("Тестовый");
        FormData fd = new FormData();
        fd.setReportPeriodId(17);
        fd.setDepartmentReportPeriodId(17);
        fd.setState(WorkflowState.ACCEPTED);
        fd.setFormTemplateId(1);
        fd.setDepartmentId(0);
        fd.setComparativePeriodId(1);
        fd.setFormType(type);
        fd.setKind(FormDataKind.ADDITIONAL);
        ReportPeriod rp = new ReportPeriod();
        rp.setName("Период");
        TaxPeriod tp = new TaxPeriod();
        tp.setYear(2015);
        rp.setTaxPeriod(tp);
        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        drp.setCorrectionDate(new Date());
        drp.setReportPeriod(rp);
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
		lockData.setDateLock(new Date());

		TAUser user = new TAUser();
		user.setId(31);
		user.setLogin("admin");
		try {
			formDataService.checkLockedMe(lockData, user);
		} catch (ServiceException e) {
			SimpleDateFormat SDF_HH_MM_DD_MM_YYYY = new SimpleDateFormat("HH:mm dd.MM.yyyy");
			String s = "Объект заблокирован другим пользователем";
			assertNotNull(e.getMessage());
			assertEquals(s, e.getMessage().substring(0, s.length()));
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
        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setMonthly(false);
        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setMonthly(false);

        FormData formData = new FormData();
        formData.setId(1L);
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

        ArrayList<Relation> sources = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setFullDepartmentName("Тестовое подразделение");
        r1.setFormTypeName("РНУ");
        r1.setFormDataKind(FormDataKind.PRIMARY);
        r1.setPeriodName("1 квартал");
        r1.setYear(2015);
        r1.setCorrectionDate(new Date(0));
        r1.setCreated(true);
        r1.setFormDataId(11L);
        r1.setState(WorkflowState.ACCEPTED);
        Relation r2 = new Relation();
        r2.setFullDepartmentName("Тестовое подразделение");
        r2.setFormTypeName("РНУ");
        r2.setFormDataKind(FormDataKind.ADDITIONAL);
        r2.setPeriodName("1 квартал");
        r2.setYear(2015);
        r2.setCorrectionDate(new Date(0));
        r2.setCreated(false);
        sources.add(r2);
        sources.add(r1);

        ArrayList<Relation> destinations = new ArrayList<Relation>();
        Relation r3 = new Relation();
        r3.setFullDepartmentName("Тестовое подразделение");
        r3.setFormTypeName("РНУ");
        r3.setFormDataKind(FormDataKind.PRIMARY);
        r3.setPeriodName("1 квартал");
        r3.setYear(2015);
        r3.setCreated(true);
        r3.setFormDataId(33L);
        destinations.add(r3);
        when(sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger)).thenReturn(sources);
        when(sourceService.getDestinationsInfo(formData, true, true, null, userInfo, logger)).thenReturn(destinations);
        when(sourceService.isFDSourceConsolidated(formData.getId(), r3.getFormDataId())).thenReturn(false);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setId(1);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(1);
        departmentReportPeriod.setBalance(false);
        departmentReportPeriod.setActive(true);
        formData.setDepartmentReportPeriodId(departmentReportPeriod.getId());

        when(formTemplateService.existFormTemplate(1, 2, true)).thenReturn(true);
        when(formTemplateService.existFormTemplate(2, 2, true)).thenReturn(true);
        when(formTemplateService.getActiveFormTemplateId(1, 2)).thenReturn(1);
        when(formTemplateService.getActiveFormTemplateId(2, 2)).thenReturn(2);
        when(formTemplateService.get(1)).thenReturn(formTemplate1);
        when(formTemplateService.get(2)).thenReturn(formTemplate2);

        when(departmentReportPeriodService.getLast(anyInt(), anyInt())).thenReturn(departmentReportPeriod);
        when(sourceService.isFDConsolidationTopical(1L)).thenReturn(false);
		formDataService.doCheck(logger, userInfo, formData, false);
		assertEquals(
				"Текущая форма содержит неактуальные консолидированные данные (расприняты формы-источники / удалены назначения по формам-источникам, на основе которых ранее выполнена консолидация). Для коррекции консолидированных данных необходимо нажать на кнопку \"Консолидировать\"",
				logger.getEntries().get(0).getMessage()
		);

        logger.clear();
        when(sourceService.isFDConsolidationTopical(1L)).thenReturn(true);
        formDataService.doCheck(logger, userInfo, formData, false);
        assertEquals(
                "Не выполнена консолидация данных в форму \"Тестовое подразделение\", \"РНУ\", \"Первичная\", \"1 квартал 2015\"",
                logger.getEntries().get(0).getMessage()
        );
        assertEquals(
                "Не выполнена консолидация данных из формы \"Тестовое подразделение\", \"РНУ\", \"Выходная\", \"1 квартал 2015 с датой сдачи корректировки 01.01.1970\" - экземпляр формы не создан",
                logger.getEntries().get(1).getMessage()
        );
        assertEquals(
                "Не выполнена консолидация данных из формы \"Тестовое подразделение\", \"РНУ\", \"Первичная\", \"1 квартал 2015 с датой сдачи корректировки 01.01.1970\" в статусе \"Принята\"",
                logger.getEntries().get(2).getMessage()
        );
    }

    @Test
    public void checkSourcesTest() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");
        Logger logger = new Logger();

        FormType formType = new FormType();
        formType.setId(1);
        formType.setName("Type1");
        FormType formType2 = new FormType();
        formType2.setId(2);
        formType2.setName("РНУ");

        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setMonthly(false);
        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setMonthly(false);

        when(formTemplateService.existFormTemplate(1, 2, true)).thenReturn(true);
        when(formTemplateService.existFormTemplate(2, 2, true)).thenReturn(true);
        when(formTemplateService.getActiveFormTemplateId(1, 2)).thenReturn(1);
        when(formTemplateService.getActiveFormTemplateId(2, 2)).thenReturn(2);
        when(formTemplateService.get(1)).thenReturn(formTemplate1);
        when(formTemplateService.get(2)).thenReturn(formTemplate2);

        Department department1 = new Department();
        department1.setName("Тестовое подразделение");
        Department department2 = new Department();
        department2.setName("Тестовое подразделение2");

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

        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        drp.setId(1);
        drp.setReportPeriod(reportPeriod);
        drp.setCorrectionDate(new Date(0));
        drp.setDepartmentId(1);
        drp.setBalance(false);
        drp.setActive(true);
        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setReportPeriod(reportPeriod);
        DepartmentReportPeriod drp2 = new DepartmentReportPeriod();
        drp1.setReportPeriod(reportPeriod);

        FormData formData = new FormData();
        formData.setId(1L);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);

        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setManual(false);
        formData.setState(WorkflowState.ACCEPTED);
        formData.setDepartmentReportPeriodId(1);
        formData.setPeriodOrder(null);
        formData.setAccruing(false);
        formData.setDepartmentReportPeriodId(drp.getId());
        when(formDataDao.get(1, false)).thenReturn(formData);
        when(departmentReportPeriodService.get(formData.getDepartmentReportPeriodId())).thenReturn(drp);

        ArrayList<DepartmentFormType> dftSources = new ArrayList<DepartmentFormType>();
        DepartmentFormType dft1 = new DepartmentFormType();
        dft1.setDepartmentId(1);
        dft1.setFormTypeId(1);
        dft1.setKind(FormDataKind.ADDITIONAL);
        DepartmentFormType dft2 = new DepartmentFormType();
        dft2.setDepartmentId(2);
        dft2.setFormTypeId(2);
        dft2.setKind(FormDataKind.CONSOLIDATED);
        DepartmentFormType dft3 = new DepartmentFormType();
        dft3.setDepartmentId(2);
        dft3.setFormTypeId(2);
        dft3.setKind(FormDataKind.PRIMARY);

        when(departmentService.getDepartment(dft1.getDepartmentId())).thenReturn(department1);
        when(departmentService.getDepartment(dft2.getDepartmentId())).thenReturn(department2);

        dftSources.add(dft1);
        dftSources.add(dft2);
        dftSources.add(dft3);
        when(departmentFormTypeDao.getFormSources(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                reportPeriod.getStartDate(),
                reportPeriod.getEndDate())).thenReturn(dftSources);

        List<Relation> sources = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setFullDepartmentName("Тестовое подразделение");
        r1.setFormTypeName("РНУ");
        r1.setFormDataKind(FormDataKind.CONSOLIDATED);
        r1.setPeriodName("1 квартал");
        r1.setYear(2015);
        r1.setCorrectionDate(new Date(0));
        r1.setCreated(true);
        r1.setFormDataId(11L);
        r1.setState(WorkflowState.CREATED);

        Relation r2 = new Relation();
        r2.setFullDepartmentName("Тестовое подразделение2");
        r2.setFormTypeName("РНУ");
        r2.setFormDataKind(FormDataKind.PRIMARY);
        r2.setPeriodName("1 квартал");
        r2.setYear(2015);
        r2.setCorrectionDate(new Date(0));
        r2.setCreated(false);

        Relation r3 = new Relation();
        r3.setFullDepartmentName("Тестовое подразделение");
        r3.setFormTypeName("РНУ");
        r3.setFormDataKind(FormDataKind.CONSOLIDATED);
        r3.setPeriodName("1 квартал");
        r3.setYear(2015);
        r3.setCorrectionDate(new Date(0));
        r3.setCreated(true);
        r3.setFormDataId(11L);
        r3.setState(WorkflowState.ACCEPTED);

        sources.add(r1);
        sources.add(r2);
        sources.add(r3);
        when(sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger)).thenReturn(sources);
        when(formDataDao.getWithoutRows(1L)).thenReturn(formData);

        formDataService.checkSources(1, false, userInfo, logger);
        assertEquals(
                "Для текущей формы следующие формы-источники имеют статус отличный от \"Принята\" (консолидация предусмотрена из форм-источников в статусе \"Принята\"):",
                logger.getEntries().get(0).getMessage()
        );
        assertEquals(
                "Тип: \"Консолидированная\", Вид: \"РНУ\", Подразделение: \"Тестовое подразделение\", Период: \"1 квартал 2015\", Дата сдачи корректировки: 01.01.1970, Версия: \"Абсолютные значения\"",
                logger.getEntries().get(1).getMessage()
        );

        assertEquals(
                "Для текущей формы следующие формы-источники не созданы:",
                logger.getEntries().get(2).getMessage()
        );
        assertEquals(
                "Тип: \"Первичная\", Вид: \"РНУ\", Подразделение: \"Тестовое подразделение2\", Период: \"1 квартал 2015\", Дата сдачи корректировки: 01.01.1970, Версия: \"Абсолютные значения\"",
                logger.getEntries().get(3).getMessage()
        );
    }

    @Test
    public void getSpecificReportTypesTest() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");
        Logger logger = new Logger();

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        FormType formType = new FormType();
        formType.setName("Тестовый");
        formType.setTaxType(TaxType.ETR);
        formTemplate.setType(formType);
        InputStream stream = FormTemplateServiceImplTest.class.getResourceAsStream("SpecificReport.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        formTemplate.setScript(script);

        FormData formData = new FormData();
        formData.setId(1L);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        formData.setFormTemplateId(formTemplate.getId());
        formData.setFormType(formType);

        FormDataScriptingServiceImpl scriptingService = new FormDataScriptingServiceImpl();

        FormTemplateDao formTemplateDao = mock(FormTemplateDao.class);
        when(formTemplateDao.get(anyInt())).thenReturn(formTemplate);
        ReflectionTestUtils.setField(scriptingService, "formTemplateDao", formTemplateDao);

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

        DepartmentService departmentService = mock(DepartmentService.class);
        ReflectionTestUtils.setField(scriptingService, "departmentService", departmentService);

        ReflectionTestUtils.setField(formDataService, "formDataScriptingService", scriptingService);

        List<String> specificReportTypes = formDataService.getSpecificReportTypes(formData, userInfo, logger);
        assertTrue(specificReportTypes.contains("Type1"));
        assertTrue(specificReportTypes.contains("Type2(CSV)"));
        assertTrue(specificReportTypes.contains("Тип3 список"));
        assertTrue(specificReportTypes.contains("XLSM")); // Стандартные типы теперь переопределяются
        assertEquals(4, specificReportTypes.size());
    }

    @Test
    public void createSpecificReport() throws IOException {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");
        Logger logger = new Logger();

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        FormType formType = new FormType();
        formType.setName("Тестовый");
        formType.setTaxType(TaxType.ETR);
        formTemplate.setType(formType);
        InputStream stream = FormTemplateServiceImplTest.class.getResourceAsStream("SpecificReport.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        formTemplate.setScript(script);

        FormData formData = new FormData();
        formData.setId(1L);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        formData.setFormTemplateId(formTemplate.getId());
        formData.setFormType(formType);

        FormDataScriptingServiceImpl scriptingService = new FormDataScriptingServiceImpl();

        FormTemplateDao formTemplateDao = mock(FormTemplateDao.class);
        when(formTemplateDao.get(anyInt())).thenReturn(formTemplate);
        ReflectionTestUtils.setField(scriptingService, "formTemplateDao", formTemplateDao);

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

        DepartmentService departmentService = mock(DepartmentService.class);
        ReflectionTestUtils.setField(scriptingService, "departmentService", departmentService);

        ReflectionTestUtils.setField(formDataService, "formDataScriptingService", scriptingService);

        ReportService reportService = mock(ReportService.class);
        ReflectionTestUtils.setField(formDataService, "reportService", reportService);
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
        ReflectionTestUtils.setField(formDataService, "blobDataService", blobDataService);

        String specificReportType = "Type1";
        formDataService.createSpecificReport(formData, false, false, specificReportType, userInfo, new LockStateLogger() {
            @Override
            public void updateState(String state) {
            }
        });
        assertEquals(strings.size(), 1);
        assertEquals(strings.get(0), specificReportType);
    }

    @Test
    public void getTaskName() {
        FormData formData101 = new FormData();
        FormType formTypeI= new FormType();
        formTypeI.setTaxType(TaxType.INCOME);
        formData101.setFormType(formTypeI);
        when(formDataDao.getWithoutRows(101)).thenReturn(formData101);
        assertEquals(formDataService.getTaskName(ReportType.DELETE_FD, 101, null), "Удаление налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.EDIT_FD, 101, null), "Редактирование налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.REFRESH_FD, 101, null), "Обновление налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.CALCULATE_FD, 101, null), "Расчет налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.CONSOLIDATE_FD, 101, null), "Консолидация налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.CHECK_FD, 101, null), "Проверка налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.MOVE_FD, 101, null), "Изменение состояния налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.EXCEL, 101, null), "Формирование отчета налоговой формы в XLSM-формате");
        assertEquals(formDataService.getTaskName(ReportType.CSV, 101, null), "Формирование отчета налоговой формы в CSV-формате");
        assertEquals(formDataService.getTaskName(ReportType.IMPORT_FD, 101, null), "Загрузка XLSM-файла с формы экземпляра налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.IMPORT_TF_FD, 101, null), "Импорт ТФ из каталога загрузки");
        assertEquals(formDataService.getTaskName(ReportType.EDIT_FILE_COMMENT, 101, null), "Добавление файлов и комментариев");
        assertEquals(formDataService.getTaskName(ReportType.SPECIFIC_REPORT, 101, null, "Report"), "Формирование специфического отчета \"Report\" налоговой формы");

        FormData formData102 = new FormData();
        FormType formTypeD= new FormType();
        formTypeD.setTaxType(TaxType.DEAL);
        formData102.setFormType(formTypeD);
        when(formDataDao.getWithoutRows(102)).thenReturn(formData102);
        assertEquals(formDataService.getTaskName(ReportType.DELETE_FD, 102, null), "Удаление формы");
        assertEquals(formDataService.getTaskName(ReportType.EDIT_FD, 102, null), "Редактирование формы");
        assertEquals(formDataService.getTaskName(ReportType.REFRESH_FD, 102, null), "Обновление формы");
        assertEquals(formDataService.getTaskName(ReportType.CALCULATE_FD, 102, null), "Расчет формы");
        assertEquals(formDataService.getTaskName(ReportType.CONSOLIDATE_FD, 102, null), "Консолидация формы");
        assertEquals(formDataService.getTaskName(ReportType.CHECK_FD, 102, null), "Проверка формы");
        assertEquals(formDataService.getTaskName(ReportType.MOVE_FD, 102, null), "Изменение состояния формы");
        assertEquals(formDataService.getTaskName(ReportType.EXCEL, 102, null), "Формирование отчета формы в XLSM-формате");
        assertEquals(formDataService.getTaskName(ReportType.CSV, 102, null), "Формирование отчета формы в CSV-формате");
        assertEquals(formDataService.getTaskName(ReportType.IMPORT_FD, 102, null), "Загрузка XLSM-файла с формы экземпляра налоговой формы");
        assertEquals(formDataService.getTaskName(ReportType.IMPORT_TF_FD, 102, null), "Импорт ТФ из каталога загрузки");
        assertEquals(formDataService.getTaskName(ReportType.EDIT_FILE_COMMENT, 102, null), "Добавление файлов и комментариев");
        assertEquals(formDataService.getTaskName(ReportType.SPECIFIC_REPORT, 102, null, "Report"), "Формирование специфического отчета \"Report\" формы");
    }

    @Test
    public void compose1() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");
        Logger logger = new Logger();

        FormType formType = new FormType();
        formType.setId(1);
        formType.setName("Type1");

        FormData formData = new FormData();
        formData.setId(1L);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setManual(false);
        formData.setState(WorkflowState.ACCEPTED);
        formData.setDepartmentReportPeriodId(1);
        formData.setPeriodOrder(null);
        formData.setAccruing(false);
        formData.setDepartmentReportPeriodId(1);
        when(formDataDao.get(1, false)).thenReturn(formData);

        List<Relation> sources = new ArrayList<Relation>();
        List<Relation> sourcesAccepted = new ArrayList<Relation>();

        Relation r1 = new Relation();
        r1.setFullDepartmentName("Тестовое подразделение");
        r1.setFormTypeName("РНУ");
        r1.setFormDataKind(FormDataKind.CONSOLIDATED);
        r1.setPeriodName("1 квартал");
        r1.setYear(2015);
        r1.setCorrectionDate(new Date(0));
        r1.setCreated(true);
        r1.setFormDataId(11L);
        r1.setState(WorkflowState.CREATED);

        Relation r2 = new Relation();
        r2.setFullDepartmentName("Тестовое подразделение2");
        r2.setFormTypeName("РНУ");
        r2.setFormDataKind(FormDataKind.PRIMARY);
        r2.setPeriodName("1 квартал");
        r2.setYear(2015);
        r2.setCorrectionDate(new Date(0));
        r2.setCreated(false);

        Relation r3 = new Relation();
        r3.setFullDepartmentName("Тестовое подразделение");
        r3.setFormTypeName("РНУ");
        r3.setFormDataKind(FormDataKind.CONSOLIDATED);
        r3.setPeriodName("1 квартал");
        r3.setYear(2015);
        r3.setCorrectionDate(null);
        r3.setCreated(true);
        r3.setFormDataId(11L);
        r3.setState(WorkflowState.ACCEPTED);

        sources.add(r1);
        sources.add(r2);
        sources.add(r3);

        sourcesAccepted.add(r3);
        when(sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger)).thenReturn(sources);
        when(sourceService.getSourcesInfo(formData, true, true, WorkflowState.ACCEPTED, userInfo, logger)).thenReturn(sourcesAccepted);

        when(formDataDao.getWithoutRows(1L)).thenReturn(formData);

        FormDataCompositionService formDataCompositionService = mock(FormDataCompositionServiceImpl.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(FormDataCompositionService.class)).thenReturn(formDataCompositionService);
        ReflectionTestUtils.setField(formDataService, "applicationContext", applicationContext);

        final List<Long> list = new CopyOnWriteArrayList<Long>();
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                list.addAll((Set<Long>)invocation.getArguments()[1]);
                return null;
            }
        }).when(sourceService).addFormDataConsolidationInfo(
                anyLong(),
                anyCollectionOf(Long.class));

        formDataService.compose(formData, userInfo, logger, new LockStateLogger() {
            @Override
            public void updateState(String state) {
            }
        });
        assertEquals(1, list.size());
        assertEquals(r3.getFormDataId(), list.get(0));
        assertEquals(
                "Выполнена консолидация данных из форм-источников:",
                logger.getEntries().get(0).getMessage()
        );
        assertEquals(
                "«Тестовое подразделение», «Консолидированная», «РНУ», «1 квартал»",
                logger.getEntries().get(1).getMessage()
        );
        assertEquals(
                "Для текущей формы следующие формы-источники имеют статус отличный от \"Принята\" (консолидация предусмотрена из форм-источников в статусе \"Принята\"):",
                logger.getEntries().get(2).getMessage()
        );
        assertEquals(
                "Тип: \"Консолидированная\", Вид: \"РНУ\", Подразделение: \"Тестовое подразделение\", Период: \"1 квартал 2015\", Дата сдачи корректировки: 01.01.1970, Версия: \"Абсолютные значения\"",
                logger.getEntries().get(3).getMessage()
        );
        assertEquals(
                "Для текущей формы следующие формы-источники не созданы:",
                logger.getEntries().get(4).getMessage()
        );
        assertEquals(
                "Тип: \"Первичная\", Вид: \"РНУ\", Подразделение: \"Тестовое подразделение2\", Период: \"1 квартал 2015\", Дата сдачи корректировки: 01.01.1970, Версия: \"Абсолютные значения\"",
                logger.getEntries().get(5).getMessage()
        );
    }

    @Test
    public void getValueForCheckLimit1() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        FormType formType = new FormType();
        formType.setId(1);
        formType.setName("Type1");

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        formTemplate.setType(formType);
        formTemplate.addColumn(new RefBookColumn());
        formTemplate.addColumn(new NumericColumn());
        formTemplate.addColumn(new NumericColumn());
        formTemplate.addColumn(new StringColumn());
        formTemplate.addColumn(new NumericColumn());

        FormData formData = new FormData();
        formData.setId(1L);
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setManual(false);
        formData.setState(WorkflowState.ACCEPTED);
        formData.setDepartmentReportPeriodId(1);
        formData.setPeriodOrder(null);
        formData.setAccruing(false);
        formData.setDepartmentReportPeriodId(1);
        when(formDataDao.get(1, false)).thenReturn(formData);

        when(dataRowDao.getRowCount(formData)).thenReturn(20);
        when(formTemplateService.get(formData.getFormTemplateId())).thenReturn(formTemplate);

        assertEquals(new Long(100L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.CHECK_FD, null, null, new Logger()));
        assertEquals(new Long(100L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.MOVE_FD, null, null, new Logger()));
        assertEquals(new Long(100L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.REFRESH_FD, null, null, new Logger()));
        assertEquals(new Long(100L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.CALCULATE_FD, null, null, new Logger()));
        assertEquals(new Long(100L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.EXCEL, null, null, new Logger()));
        assertEquals(new Long(100L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.CSV, null, null, new Logger()));
    }

    @Test
    public void getValueForCheckLimit2() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        Logger logger = new Logger();

        FormType formType = new FormType();
        formType.setId(1);
        formType.setName("Type1");

        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setId(10);
        formTemplate1.setType(formType);
        formTemplate1.addColumn(new RefBookColumn());
        formTemplate1.addColumn(new NumericColumn());
        formTemplate1.addColumn(new NumericColumn());
        formTemplate1.addColumn(new StringColumn());
        formTemplate1.addColumn(new NumericColumn());

        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setId(11);
        formTemplate2.setType(formType);
        formTemplate2.addColumn(new RefBookColumn());
        formTemplate2.addColumn(new NumericColumn());

        FormData formData = new FormData();
        formData.setId(1L);
        FormData formData1 = new FormData();
        formData1.setId(10L);
        formData1.setFormTemplateId(formTemplate1.getId());
        FormData formData2 = new FormData();
        formData2.setId(11L);
        formData2.setFormTemplateId(formTemplate2.getId());

        when(formDataDao.getWithoutRows(11)).thenReturn(formData1);
        when(formDataDao.getWithoutRows(12)).thenReturn(formData2);
        when(formTemplateService.get(formData1.getFormTemplateId())).thenReturn(formTemplate1);
        when(formTemplateService.get(formData2.getFormTemplateId())).thenReturn(formTemplate2);

        List<Relation> sourcesAccepted = new ArrayList<Relation>();
        Relation r1 = new Relation();
        r1.setFormDataId(11L);
        Relation r2 = new Relation();
        r2.setFormDataId(12L);
        sourcesAccepted.add(r1);
        sourcesAccepted.add(r2);
        when(sourceService.getSourcesInfo(formData, true, true, WorkflowState.ACCEPTED, userInfo, logger)).thenReturn(sourcesAccepted);

        when(dataRowDao.getRowCount(formData1)).thenReturn(10);
        when(dataRowDao.getRowCount(formData2)).thenReturn(1);

        assertEquals(new Long(52L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.CONSOLIDATE_FD, null, null, logger));
    }

    @Test
    public void getValueForCheckLimit3() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        Logger logger = new Logger();
        FormData formData = new FormData();

        BlobDataService blobDataService = mock(BlobDataService.class);
        when(blobDataService.getLength("uuid1")).thenReturn(1200L);
        ReflectionTestUtils.setField(formDataService, "blobDataService", blobDataService);

        assertEquals(new Long(2L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.IMPORT_FD, null, "uuid1", logger));
    }

    @Test
    public void getValueForCheckLimit4() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        Logger logger = new Logger();
        FormData formData = new FormData();

        FormDataScriptingService formDataScriptingService = mock(FormDataScriptingService.class);
        when(formDataScriptingService.executeScript(
                eq(userInfo), eq(formData), eq(FormDataEvent.CALCULATE_TASK_COMPLEXITY), eq(logger), any(Map.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, Object> exchangeParams = ((Map<String, Object>) invocation.getArguments()[4]);
                ((ScriptTaskComplexityHolder) exchangeParams.get("taskComplexityHolder")).setValue(10L);
                return null;
            }
        });
        ReflectionTestUtils.setField(formDataService, "formDataScriptingService", formDataScriptingService);

        assertEquals(new Long(10L), formDataService.getValueForCheckLimit(userInfo, formData, ReportType.SPECIFIC_REPORT, "report1", null, logger));
    }

    @Test(expected = ServiceException.class)
    public void getValueForCheckLimit5() {
        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");

        Logger logger = new Logger();
        FormData formData = new FormData();

        formDataService.getValueForCheckLimit(userInfo, formData, ReportType.CSV_REF_BOOK, null, null, logger);
    }

    private void mockPeriodInactiveRecords(FormData formData, final List<Long> existedIds) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date calendarStartDate = format.parse("01.01.2014");
        Date endDate = format.parse("31.12.2014");
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCalendarStartDate(calendarStartDate);
        reportPeriod.setEndDate(endDate);
        when(periodService.getReportPeriod(eq(formData.getReportPeriodId()))).thenReturn(reportPeriod);
        when(refBookDao.getInactiveRecordsInPeriod(eq(RefBook.REF_BOOK_RECORD_TABLE_NAME), anyListOf(Long.class), eq(calendarStartDate), eq(endDate), eq(false))).thenAnswer(new Answer<ArrayList<ReferenceCheckResult>>() {
            @Override
            public ArrayList<ReferenceCheckResult> answer(InvocationOnMock invocation) throws Throwable {
                List<Long> recordIds = (List<Long>) invocation.getArguments()[1];
                if (existedIds.equals(recordIds)) {
                    return new ArrayList<ReferenceCheckResult>();
                }
                ArrayList<ReferenceCheckResult> checkResults = new ArrayList<ReferenceCheckResult>();
                List<Long> newList = new ArrayList<Long>(recordIds);
                newList.removeAll(existedIds);
                for (Long id : newList) {
                    ReferenceCheckResult checkResult = new ReferenceCheckResult();
                    checkResult.setRecordId(id);
                    checkResult.setResult(CheckResult.NOT_EXISTS);
                    checkResults.add(checkResult);
                }
                return checkResults;
            }
        });
    }

    private FormData preCheckValues(final List<Long> existedIds) throws ParseException {
        // форма с двумя графами
        Long refBookUni1Id = 1L;
        Long refBookUni2Id = 2L;
        long refBookAttributeUni1Id = 1L;
        long refBookAttributeUni2Id = 2L;
        String alias1 = "column1";
        String alias2 = "column2";

        // графы формы
        RefBookColumn column = new RefBookColumn();
        column.setColumnType(ColumnType.REFBOOK);
        column.setAlias(alias1);
        column.setName("Колонка1");
        column.setRefBookAttributeId(refBookAttributeUni1Id);
        formTemplate.addColumn(column);
        column = new RefBookColumn();
        column.setColumnType(ColumnType.REFBOOK);
        column.setAlias(alias2);
        column.setName("Колонка2");
        column.setRefBookAttributeId(refBookAttributeUni2Id);
        formTemplate.addColumn(column);
        // универсальный справочник
        RefBook refBook = new RefBook();
        refBook.setId(refBookUni1Id);
        when(refBookDao.getByAttribute(eq(refBookAttributeUni1Id))).thenReturn(refBook);
        refBook = new RefBook();
        refBook.setId(refBookUni2Id);
        when(refBookDao.getByAttribute(eq(refBookAttributeUni2Id))).thenReturn(refBook);
        // провайдер
        RefBookUniversal provider = new RefBookUniversal();
        provider.setRefBookId(refBookUni1Id);
        ReflectionTestUtils.setField(provider, "refBookDao", refBookDao);
        when(refBookFactory.getDataProvider(eq(refBookUni1Id))).thenReturn(provider);
        // должен испоьзоваться первый провайдер, во втором нужен только класс
        provider = new RefBookUniversal();
//        provider.setRefBookId(refBookUni2Id);
//        ReflectionTestUtils.setField(provider, "refBookDao", refBookDao);
        when(refBookFactory.getDataProvider(eq(refBookUni2Id))).thenReturn(provider);
        // НФ
        FormData formData = new FormData(formTemplate);
        formData.setReportPeriodId(1);
        // строки
        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> dataRow = formData.createDataRow();
        dataRow.getCell(alias1).setValue(1L, 1);
        dataRow.getCell(alias2).setValue(2L, 1);
        dataRow.setIndex(1);
        rows.add(dataRow);
        dataRow = formData.createDataRow();
        dataRow.getCell(alias1).setValue(3L, 1);
        dataRow.getCell(alias2).setValue(4L, 1);
        dataRow.setIndex(2);
        rows.add(dataRow);
        when(dataRowDao.getRows(eq(formData), isNull(DataRowRange.class))).thenReturn(rows);
        mockPeriodInactiveRecords(formData, existedIds);
        return formData;
    }

    private FormData preCheckValues2(final List<Long> existedIds) throws ParseException {
        // форма с двумя графами
        Long refBookCurrencyId = RefBookCurrencyMetals.REF_BOOK_ID;
        Long refBookCreditId = RefBookCreditRatingsClasses.REF_BOOK_ID;
        long refBookAttributeCurrencyId = 1L;
        long refBookAttributeCreditId = 2L;
        String alias1 = "column1";
        String alias2 = "column2";

        // графы формы
        RefBookColumn column = new RefBookColumn();
        column.setColumnType(ColumnType.REFBOOK);
        column.setAlias(alias1);
        column.setName("Колонка1");
        column.setRefBookAttributeId(refBookAttributeCurrencyId);
        formTemplate.addColumn(column);
        column = new RefBookColumn();
        column.setColumnType(ColumnType.REFBOOK);
        column.setAlias(alias2);
        column.setName("Колонка2");
        column.setRefBookAttributeId(refBookAttributeCreditId);
        formTemplate.addColumn(column);
        // универсальный справочник
        RefBook refBook = new RefBook();
        refBook.setId(refBookCurrencyId);
        when(refBookDao.getByAttribute(eq(refBookAttributeCurrencyId))).thenReturn(refBook);
        refBook = new RefBook();
        refBook.setId(refBookCreditId);
        when(refBookDao.getByAttribute(eq(refBookAttributeCreditId))).thenReturn(refBook);
        // провайдер
        RefBookDataProvider provider = new RefBookCurrencyMetals();
        ReflectionTestUtils.setField(provider, "refBookFactory", refBookFactory);
        when(refBookFactory.getDataProvider(eq(refBookCurrencyId))).thenReturn(provider);
        provider = new RefBookUniversal();
        ReflectionTestUtils.setField(provider, "refBookDao", refBookDao);
        ((RefBookUniversal) provider).setRefBookId(RefBookCurrencyMetals.REF_BOOK_CURRENCY_ID);
        when(refBookFactory.getDataProvider(eq(RefBookCurrencyMetals.REF_BOOK_CURRENCY_ID))).thenReturn(provider);
        provider = new RefBookUniversal();
        //ReflectionTestUtils.setField(provider, "refBookDao", refBookDao);
        //((RefBookUniversal) provider).setRefBookId(RefBookCurrencyMetals.REF_BOOK_METALS_ID);
        when(refBookFactory.getDataProvider(eq(RefBookCurrencyMetals.REF_BOOK_METALS_ID))).thenReturn(provider);
        provider = new RefBookCreditRatingsClasses();
        ReflectionTestUtils.setField(provider, "refBookFactory", refBookFactory);
        when(refBookFactory.getDataProvider(eq(refBookCreditId))).thenReturn(provider);
        provider = new RefBookUniversal();
        ((RefBookUniversal) provider).setRefBookId(RefBookCreditRatingsClasses.REF_BOOK_CREDIT_CLASSES_ID);
        //ReflectionTestUtils.setField(provider, "refBookDao", refBookDao);
        //when(refBookFactory.getDataProvider(eq(RefBookCreditRatingsClasses.REF_BOOK_CREDIT_CLASSES_ID))).thenReturn(provider);
        provider = new RefBookUniversal();
        ReflectionTestUtils.setField(provider, "refBookDao", refBookDao);
        ((RefBookUniversal) provider).setRefBookId(RefBookCreditRatingsClasses.REF_BOOK_CREDIT_RATINGS_ID);
        when(refBookFactory.getDataProvider(eq(RefBookCreditRatingsClasses.REF_BOOK_CREDIT_RATINGS_ID))).thenReturn(provider);
        // НФ
        FormData formData = new FormData(formTemplate);
        formData.setReportPeriodId(1);
        // строки
        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> dataRow = formData.createDataRow();
        dataRow.getCell(alias1).setValue(10L, 1);
        dataRow.getCell(alias2).setValue(20L, 1);
        dataRow.setIndex(1);
        rows.add(dataRow);
        dataRow = formData.createDataRow();
        dataRow.getCell(alias1).setValue(31L, 1);
        dataRow.getCell(alias2).setValue(41L, 1);
        dataRow.setIndex(2);
        rows.add(dataRow);
        when(dataRowDao.getRows(eq(formData), isNull(DataRowRange.class))).thenReturn(rows);
        mockPeriodInactiveRecords(formData, existedIds);
        return formData;
    }

    @Test
    public void checkValuesTest() throws ParseException {
        Logger logger = new Logger();
        final List<Long> existedIds = new ArrayList<Long>() {{
            add(1L); // первая строка, первая графа
            add(2L); // первая строка, вторая графа
            add(3L); // вторая строка, первая графа
            add(4L); // вторая строка, вторая графа
        }};
        FormData formData = preCheckValues(existedIds);

        formDataService.checkValues(logger, formData);
        Assert.assertEquals(0, logger.getEntries().size());
    }

    @Test
    public void checkValues2Test() throws ParseException {
        Logger logger = new Logger();
        final List<Long> existedIds = new ArrayList<Long>() {{
            add(1L); // первая строка, первая графа
            add(2L); // первая строка, вторая графа
            add(3L); // вторая строка, первая графа
            //add(4L); // вторая строка, вторая графа
        }};
        FormData formData = preCheckValues(existedIds);

        try {
            formDataService.checkValues(logger, formData);
            Assert.fail("Should throw an exception");
        } catch (ServiceLoggerException e) {
            Assert.assertEquals(e.getMessage(), "Произошла ошибка при проверке справочных значений формы");
        }
        List<LogEntry> entries = logger.getEntries();
        Assert.assertEquals(1, entries.size());
        int i = 0;
        Assert.assertEquals("Строка 2: Значение графы \"Колонка2\" ссылается на несуществующую версию записи справочника!", entries.get(i++).getMessage());
    }

    @Test
    public void checkValues3Test() throws ParseException {
        Logger logger = new Logger();
        final List<Long> existedIds = new ArrayList<Long>() {{
            add(1L); // первая строка, первая графа
            add(2L); // первая строка, вторая графа
            add(3L); // вторая строка, первая графа
            add(4L); // вторая строка, вторая графа
        }};
        FormData formData = preCheckValues2(existedIds);

        formDataService.checkValues(logger, formData);
        Assert.assertEquals(0, logger.getEntries().size());
    }

    //@Test
    public void checkValues4Test() throws ParseException {
        Logger logger = new Logger();
        final List<Long> existedIds = new ArrayList<Long>() {{
            add(1L); // первая строка, первая графа
            add(2L); // первая строка, вторая графа
            add(3L); // вторая строка, первая графа
            //add(4L); // вторая строка, вторая графа
        }};
        FormData formData = preCheckValues2(existedIds);

        formDataService.checkValues(logger, formData);
        Assert.assertEquals(0, logger.getEntries().size());
    }
}
