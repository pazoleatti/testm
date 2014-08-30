package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.eventhandler.EventLauncher;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mockito.Mockito.*;

public class FormDataServiceTest extends Assert {

    private final FormDataServiceImpl formDataService = new FormDataServiceImpl();

    private FormTemplate formTemplate;

    private FormDataDao formDataDao;

    private DataRowDao dataRowDao;

    private FormTemplateService formTemplateService;

    private FormDataAccessService formDataAccessService;

    private static final int FORM_TEMPLATE_ID = 1;

    @Before
    public void init() {
        // Макет
        formTemplate = new FormTemplate();
        formTemplate.setId(FORM_TEMPLATE_ID);

        // Тип формы
        FormType formType = new FormType();
        formType.setName(TaxType.INCOME.getName());
        formTemplate.setType(formType);

        // Налоговый периол
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2014);
        taxPeriod.setTaxType(TaxType.INCOME);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(taxPeriod);

        // Mock
        formDataDao = mock(FormDataDao.class);
        dataRowDao = mock(DataRowDao.class);
        PeriodService reportPeriodService = mock(PeriodService.class);
        formTemplateService = mock(FormTemplateService.class);
        LockCoreService lockCoreService = mock(LockCoreService.class);
        formDataAccessService = mock(FormDataAccessService.class);
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        FormData formData = mock(FormData.class);
        FormDataScriptingService formDataScriptingService = mock(FormDataScriptingService.class);
        LogBusinessService logBusinessService = mock(LogBusinessService.class);
        AuditService auditService = mock(AuditService.class);
        LogEntryService logEntryService = mock(LogEntryService.class);
        EventLauncher eventHandlerLauncher = mock(EventLauncher.class);

        ReflectionTestUtils.setField(formDataService, "formDataDao", formDataDao);
        ReflectionTestUtils.setField(formDataService, "dataRowDao", dataRowDao);
        ReflectionTestUtils.setField(formDataService, "reportPeriodService", reportPeriodService);
        ReflectionTestUtils.setField(formDataService, "formTemplateService", formTemplateService);
        ReflectionTestUtils.setField(formDataService, "departmentDao", departmentDao);
        ReflectionTestUtils.setField(formDataService, "lockCoreService", lockCoreService);
        ReflectionTestUtils.setField(formDataService, "formDataAccessService", formDataAccessService);
        ReflectionTestUtils.setField(formDataService, "formDataScriptingService", formDataScriptingService);
        ReflectionTestUtils.setField(formDataService, "logBusinessService", logBusinessService);
        ReflectionTestUtils.setField(formDataService, "auditService", auditService);
        ReflectionTestUtils.setField(formDataService, "logEntryService", logEntryService);
        ReflectionTestUtils.setField(formDataService, "eventHandlerLauncher", eventHandlerLauncher);

        when(reportPeriodService.getTaxPeriod(anyInt())).thenReturn(taxPeriod);
        when(formData.getReportPeriodId()).thenReturn(1);
        when(reportPeriodService.getReportPeriod(anyInt())).thenReturn(reportPeriod);
    }
    /**
     * Тест удаления приемника при распринятии последнего источника
     */
    @Test
    public void compose() {
        // текущая форм дата будет вызывать compose на своих приемниках
        final FormData formData = new FormData();
        formData.setReportPeriodId(2);
        formData.setDepartmentId(1);
        FormType formType = new FormType();
        formType.setId(1);
        formData.setFormType(formType);
        formData.setKind(FormDataKind.PRIMARY);
        formData.setManual(false);

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(1);
        userInfo.setUser(user);
        userInfo.setIp("127.0.0.1");
        Logger logger = new Logger();

        // мок для сервиса который возвращает баллансовый ли это период
        PeriodService reportPeriodService = mock(PeriodService.class);
        // период с id = 1 для подразделения с id=1 будет баллансовым
        when(reportPeriodService.isBalancePeriod(1, 1)).thenReturn(true);
        // период с id = 2 для подразделения с id=1 Не будет баллансовым
        when(reportPeriodService.isBalancePeriod(2, 1)).thenReturn(false);
        // подменяем reportPeriodService у тестируемого объекта
        ReflectionTestUtils.setField(formDataService, "reportPeriodService", reportPeriodService);

        // имеем 3 формы, 2 источника и 1 приемник
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        // готовим тип формы для
        DepartmentFormType departmentFormType = new DepartmentFormType();
        departmentFormType.setDepartmentId(1);
        departmentFormType.setFormTypeId(1);
        departmentFormType.setId(1);
        departmentFormType.setKind(FormDataKind.PRIMARY);
        final List<DepartmentFormType> list = new CopyOnWriteArrayList<DepartmentFormType>();
        list.add(departmentFormType);

        when(departmentFormTypeDao.getFormDestinations(2, 1, FormDataKind.PRIMARY, null, null)).thenReturn(new ArrayList<DepartmentFormType>());
        when(departmentFormTypeDao.getFormDestinations(1, 1, FormDataKind.PRIMARY, null, null)).thenReturn(list);
        ReflectionTestUtils.setField(formDataService, "departmentFormTypeDao", departmentFormTypeDao);
        ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
        when(reportPeriodDao.get(any(Integer.class))).thenReturn(mock(ReportPeriod.class));
        ReflectionTestUtils.setField(formDataService, "reportPeriodDao", reportPeriodDao);

        Department department = new Department();
        department.setName("Тестовое подразделение");

        DepartmentDao departmentDao = mock(DepartmentDao.class);
        ReflectionTestUtils.setField(formDataService, "departmentDao", departmentDao);


        FormDataDao formDataDao = mock(FormDataDao.class);
        FormData formData1 = new FormData();
        formData1.setId(2L);
        formData1.setFormType(formType);
        formData1.setKind(FormDataKind.CONSOLIDATED);
        formData1.setDepartmentId(1);
        when(formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(), departmentFormType.getDepartmentId(), formData.getReportPeriodId())).thenReturn(formData1);
        when(formDataDao.get(formData1.getId(), false)).thenReturn(formData);
        when(departmentDao.getDepartment(formData1.getDepartmentId())).thenReturn(department);

        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                for (DepartmentFormType departmentFormType1 : list) {
                    if (departmentFormType1.getDepartmentId() == formData.getDepartmentId() &&
                            departmentFormType1.getKind().equals(formData.getKind()) &&
                            departmentFormType1.getFormTypeId() == formData.getFormType().getId())
                        list.remove(departmentFormType1);
                }
                return null;
            }
        }).when(formDataDao).delete(formData1.getId());
        ReflectionTestUtils.setField(formDataService, "formDataDao", formDataDao);

        FormDataCompositionService formDataCompositionService = mock(FormDataCompositionService.class);

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(FormDataCompositionService.class)).thenReturn(formDataCompositionService);
        ReflectionTestUtils.setField(formDataService, "applicationContext", applicationContext);

        LockCoreService lockCoreService = mock(LockCoreService.class);
        ReflectionTestUtils.setField(formDataService, "lockCoreService", lockCoreService);

        FormDataAccessService formDataAccessService = mock(FormDataAccessService.class);
        ReflectionTestUtils.setField(formDataService, "formDataAccessService", formDataAccessService);

        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(formDataService, "auditService", auditService);

        LockDataService lockDataService = mock(LockDataService.class);
        final Map<String, LockData> map = new HashMap<String, LockData>();
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                Object[] arguments = invocation.getArguments();
                Object key = invocation.getArguments()[0];
                if (map.containsKey(key)) {
                    return map.get(key);
                }
                map.put((String) arguments[0], new LockData((String) arguments[0], (Integer) arguments[1], new Date()));
                return null;
            }
        }).when(lockDataService).lock(anyString(), anyInt(), anyInt());
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                map.remove(invocation.getArguments()[0]);
                return null;
            }
        }).when(lockDataService).unlock(anyString(),anyInt());
        ReflectionTestUtils.setField(formDataService, "lockDataService", lockDataService);

        formDataService.compose(WorkflowMove.APPROVED_TO_ACCEPTED, formData, userInfo, logger);
        // проверяем что источник удален
        assertTrue(list.size() == 0);
    }

    @Test
    public void existFormDataTest() {

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

        FormData formData1 = new FormData(formTemplate);
        formData1.setState(WorkflowState.CREATED);
        formData1.setKind(FormDataKind.SUMMARY);
        formData1.setDepartmentId(1);
        formData1.setReportPeriodId(2);
        formData1.setId(2l);

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

        FormDataDao formDataDao = mock(FormDataDao.class);
        ReflectionTestUtils.setField(formDataService, "formDataDao", formDataDao);

        when(formDataDao.getFormDataIds(1, FormDataKind.SUMMARY, 1)).thenReturn(list);
        when(formDataDao.getWithoutRows(1)).thenReturn(formData);
        when(formDataDao.getWithoutRows(2)).thenReturn(formData1);

        ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
        ReflectionTestUtils.setField(formDataService, "reportPeriodDao", reportPeriodDao);

        when(reportPeriodDao.get(1)).thenReturn(reportPeriod);
        when(reportPeriodDao.get(2)).thenReturn(reportPeriod1);

        DepartmentDao departmentDao = mock(DepartmentDao.class);
        ReflectionTestUtils.setField(formDataService, "departmentDao", departmentDao);

        when(departmentDao.getDepartment(1)).thenReturn(department);

        assertTrue(formDataService.existFormData(1, FormDataKind.SUMMARY, 1, logger));
        assertEquals("Существует экземпляр налоговой формы Тестовый тип НФ типа Сводная в подразделении Тестовое подразделение периоде Тестовый период 2014", logger.getEntries().get(0).getMessage());
        assertEquals("Существует экземпляр налоговой формы Тестовый тип НФ типа Сводная в подразделении Тестовое подразделение периоде Второй тестовый период 2014", logger.getEntries().get(1).getMessage());
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
        newFormData.setKind(FormDataKind.PRIMARY);

        when(formDataDao.getPrevFormDataList(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(new ArrayList<FormData>());
        assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 0",
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
        formData.setState(WorkflowState.ACCEPTED);

        FormData formData1 = new FormData(formTemplate);
        formData1.setId((long) 2);
        formData1.setReportPeriodId(2);
        formData1.setKind(FormDataKind.PRIMARY);
        formData1.setState(WorkflowState.ACCEPTED);

        // Создаваемая форма
        FormData newFormData = new FormData(formTemplate);
        newFormData.setId((long) 3);
        newFormData.setReportPeriodId(3);
        newFormData.setKind(FormDataKind.PRIMARY);
        newFormData.setState(WorkflowState.CREATED);

        List<FormData> formDataList = new ArrayList<FormData>();
        formDataList.add(formData);
        formDataList.add(formData1);

        when(formDataDao.getPrevFormDataList(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(formDataList);
        when(dataRowDao.getSizeWithoutTotal(formData, null)).thenReturn(3);
        when(dataRowDao.getSizeWithoutTotal(formData1, null)).thenReturn(5);

        assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 8",
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
        formData.setState(WorkflowState.ACCEPTED);

        FormData formData1 = new FormData(formTemplate);
        formData1.setId((long) 2);
        formData1.setReportPeriodId(2);
        formData1.setKind(FormDataKind.PRIMARY);
        formData1.setState(WorkflowState.CREATED);

        // Создаваемая форма
        FormData newFormData = new FormData(formTemplate);
        newFormData.setId((long) 3);
        newFormData.setReportPeriodId(3);
        newFormData.setKind(FormDataKind.PRIMARY);
        newFormData.setState(WorkflowState.CREATED);

        List<FormData> formDataList = new ArrayList<FormData>();
        formDataList.add(formData);
        formDataList.add(formData1);

        when(formDataDao.getPrevFormDataList(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(formDataList);
        when(dataRowDao.getSizeWithoutTotal(formData, null)).thenReturn(3);
        when(dataRowDao.getSizeWithoutTotal(formData1, null)).thenReturn(5);

        assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 3",
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

        doReturn(false).when(dataService).beInOnAutoNumeration(any(FormData.class));
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

        doReturn(true).when(dataService).beInOnAutoNumeration(any(FormData.class));
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

        doReturn(true).when(dataService).beInOnAutoNumeration(formData);
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
        Logger logger = mock(Logger.class);
        TAUserInfo userInfo = mock(TAUserInfo.class);
        FormData formData = getFormData();

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
        FormData formData = new FormData();
        formData.setState(WorkflowState.CREATED);
        assertFalse("Не должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData));

        FormData formData1 = new FormData();
        formData1.setState(WorkflowState.ACCEPTED);
        assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData1));

        FormData formData2 = new FormData();
        formData2.setState(WorkflowState.APPROVED);
        assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData2));

        FormData formData3 = new FormData();
        formData3.setState(WorkflowState.PREPARED);
        assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData3));
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
}
