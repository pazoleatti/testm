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
        FormDataAccessService formDataAccessService = mock(FormDataAccessService.class);
        DepartmentDao departmentDao = mock(DepartmentDao.class);
        FormData formData = mock(FormData.class);
        FormDataScriptingService formDataScriptingService = mock(FormDataScriptingService.class);
        LogBusinessService logBusinessService = mock(LogBusinessService.class);
        AuditService auditService = mock(AuditService.class);

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

        when(formDataDao.getPrevFormDataListForCrossNumeration(any(FormData.class), any(TaxPeriod.class)))
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

        when(formDataDao.getPrevFormDataListForCrossNumeration(any(FormData.class), any(TaxPeriod.class)))
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

        when(formDataDao.getPrevFormDataListForCrossNumeration(any(FormData.class), any(TaxPeriod.class)))
                .thenReturn(formDataList);
        when(dataRowDao.getSizeWithoutTotal(formData, null)).thenReturn(3);
        when(dataRowDao.getSizeWithoutTotal(formData1, null)).thenReturn(5);

        assertTrue("\"Номер последней строки предыдущей НФ\" должен быть равен 3",
                formDataService.getPreviousRowNumber(newFormData).equals(3));
    }

    /**
     * Текущий экземпляр НФ находится в состоянии "Создана".
     *
     * 1. Не выполнять сравнение количества строк в табличной части до и после редактирования.
     * 2. Не выполнять проверку наличия автонумеруемых граф со сквозной нумерацией.
     * 3. Не обновлять сквозную нумерацию в налоговых формах следующих периодов текущей сквозной нумерации.
     * 4. Не выводить в панель уведомлений сообщение.
     */
    @Test
    public void testSaveFormDataStateCreated() {
        Logger logger = mock(Logger.class);
        TAUserInfo userInfo = mock(TAUserInfo.class);

        FormData formData = getFormData();
        formData.setState(WorkflowState.CREATED);

        FormDataService dataService = spy(formDataService);
        dataService.saveFormData(logger, userInfo, formData);

        verify(dataRowDao, never()).isDataRowsCountChanged(anyLong());
        verify(formTemplateService, never()).isAnyAutoNumerationColumn(any(FormTemplate.class), any(AutoNumerationColumnType.class));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class));
        verify(logger, never()).info(anyString());
    }

    /**
     * Текущий экземпляр НФ находится в состоянии отличном от "Создана".
     * Количество строк в табличной части до и после редактирования не изменилось.
     * Важен порядок вызова методов!!!
     *
     * 1. Выполнить сравнение количества строк в табличной части до и после редактирования.
     * 2. Не выполнять проверку наличия автонумеруемых граф со сквозной нумерацией.
     * 3. Не обновлять сквозную нумерацию в налоговых формах следующих периодов текущей сквозной нумерации.
     * 4. Не выводить в панель уведомлений сообщение.
     */
    @Test
    public void testSaveFormDataStateNotCreatedDataRowsNotChanged() {
        Logger logger = mock(Logger.class);
        TAUserInfo userInfo = mock(TAUserInfo.class);

        FormData formData = getFormData();

        FormDataService dataService = spy(formDataService);

        when(dataRowDao.isDataRowsCountChanged(formData.getId())).thenReturn(false);

        dataService.saveFormData(logger, userInfo, formData);

        InOrder inOrder = inOrder(dataRowDao, formDataDao);

        inOrder.verify(dataRowDao, times(1)).isDataRowsCountChanged(anyLong());
        inOrder.verify(formDataDao).save(any(FormData.class));

        verify(formTemplateService, never()).isAnyAutoNumerationColumn(any(FormTemplate.class), any(AutoNumerationColumnType.class));
        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class));
        verify(logger, never()).info(anyString());

    }

    /**
     * Текущий экземпляр НФ находится в состоянии отличном от "Создана".
     * Количество строк в табличной части до и после редактирования изменилось.
     * Важен порядок вызова методов!!!
     *
     * 1. Выполнить сравнение количества строк в табличной части до и после редактирования.
     * 2. Выполнить проверку наличия автонумеруемых граф со сквозной нумерацией.
     * 3. Не обновлять сквозную нумерацию в налоговых формах следующих периодов текущей сквозной нумерации.
     * 4. Не выводить в панель уведомлений сообщение.
     */
    @Test
    public void testSaveFormDataStateNotCreatedDataRowsChangedAnyAutoNumerationNotExist() {
        Logger logger = mock(Logger.class);
        TAUserInfo userInfo = mock(TAUserInfo.class);

        FormData formData = getFormData();

        FormDataService dataService = spy(formDataService);

        when(dataRowDao.isDataRowsCountChanged(formData.getId())).thenReturn(true);
        when(formTemplateService.isAnyAutoNumerationColumn(any(FormTemplate.class), any(AutoNumerationColumnType.class))).thenReturn(false);

        dataService.saveFormData(logger, userInfo, formData);

        InOrder inOrder = inOrder(dataRowDao, formTemplateService, formDataDao);

        inOrder.verify(dataRowDao, times(1)).isDataRowsCountChanged(anyLong());
        inOrder.verify(formTemplateService, times(1)).isAnyAutoNumerationColumn(any(FormTemplate.class), any(AutoNumerationColumnType.class));
        inOrder.verify(formDataDao).save(any(FormData.class));

        verify(dataService, never()).updatePreviousRowNumber(any(FormData.class));
        verify(logger, never()).info(anyString());
    }

    /**
     * Текущий экземпляр НФ находится в состоянии отличном от "Создана".
     * Количество строк в табличной части до и после редактирования изменилось.
     * Важен порядок вызова методов!!!
     *
     * 1. Выполнить сравнение количества строк в табличной части до и после редактирования.
     * 2. Выполнить проверку наличия автонумеруемых граф со сквозной нумерацией.
     * 3. Обновить сквозную нумерацию в налоговых формах следующих периодов текущей сквозной нумерации.
     * 4. Вывести в панель уведомлений сообщение.
     */
    @Test
    public void testSaveFormDataStateNotCreatedDataRowsChangedAnyAutoNumerationExist() {
        Logger logger = mock(Logger.class);
        TAUserInfo userInfo = mock(TAUserInfo.class);

        FormData formData = getFormData();

        final FormDataService dataService = spy(formDataService);

        when(dataRowDao.isDataRowsCountChanged(formData.getId())).thenReturn(true);
        when(formTemplateService.isAnyAutoNumerationColumn(any(FormTemplate.class), any(AutoNumerationColumnType.class))).thenReturn(true);
        doReturn("Message").when(dataService).updatePreviousRowNumber(any(FormData.class));

        dataService.saveFormData(logger, userInfo, formData);

        InOrder inOrder = inOrder(dataRowDao, formTemplateService, dataService, logger, formDataDao);

        inOrder.verify(dataRowDao, times(1)).isDataRowsCountChanged(anyLong());
        inOrder.verify(formTemplateService, times(1)).isAnyAutoNumerationColumn(any(FormTemplate.class), any(AutoNumerationColumnType.class));
        inOrder.verify(dataService, times(1)).updatePreviousRowNumber(any(FormData.class));
        inOrder.verify(logger, times(1)).info(anyString());
        inOrder.verify(formDataDao).save(any(FormData.class));
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
