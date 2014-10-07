package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

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
    ReportPeriodDao reportPeriodDao;
    @Autowired
    DepartmentDao departmentDao;

    @Autowired
    private FormDataServiceImpl formDataService;
    @Autowired
    PeriodService periodService;
    @Autowired
    private LockDataService lockDataService;

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

        FormData formData = mock(FormData.class);

        when(formData.getReportPeriodId()).thenReturn(1);
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

        // имеем 3 формы, 2 источника и 1 приемник
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
        when(reportPeriodDao.get(any(Integer.class))).thenReturn(mock(ReportPeriod.class));

        Department department = new Department();
        department.setName("Тестовое подразделение");

        FormData formData1 = new FormData();
        formData1.setId(2L);
        formData1.setFormType(formType);
        formData1.setKind(FormDataKind.CONSOLIDATED);
        formData1.setDepartmentId(1);
        when(formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(), 1, null)).thenReturn(formData1);
        when(formDataDao.get(formData1.getId(), false)).thenReturn(formData);
        when(departmentDao.getDepartment(formData1.getDepartmentId())).thenReturn(department);

        doAnswer(new Answer<Object>() {
            @Override
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

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(2);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setId(1);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(1);
        departmentReportPeriod.setBalance(false);
        formData.setDepartmentReportPeriodId(departmentReportPeriod.getId());

        DepartmentReportPeriodDao departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        when(departmentReportPeriodDao.getLast(anyInt(), anyInt())).thenReturn(departmentReportPeriod);
        when(departmentReportPeriodDao.get(anyInt())).thenReturn(departmentReportPeriod);
        ReflectionTestUtils.setField(formDataService, "departmentReportPeriodDao", departmentReportPeriodDao);

        when(formDataDao.getLast(anyInt(), any(FormDataKind.class), anyInt(), anyInt(), anyInt())).thenReturn(formData1);

        FormDataCompositionService formDataCompositionService = mock(FormDataCompositionService.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(FormDataCompositionService.class)).thenReturn(formDataCompositionService);
        ReflectionTestUtils.setField(formDataService, "applicationContext", applicationContext);

        final Map<String, LockData> map = new HashMap<String, LockData>();
        doAnswer(new Answer<Object>() {
            @Override
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
            @Override
            public Object answer(InvocationOnMock invocation) {
                map.remove(invocation.getArguments()[0]);
                return null;
            }
        }).when(lockDataService).unlock(anyString(), anyInt());

        formDataService.compose(WorkflowMove.APPROVED_TO_ACCEPTED, formData, userInfo, logger);
        // проверяем что источник удален
        Assert.assertTrue(list.size() == 0);
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


        when(formDataDao.getFormDataIds(1, FormDataKind.SUMMARY, 1)).thenReturn(list);
        when(formDataDao.getWithoutRows(1)).thenReturn(formData);
        when(formDataDao.getWithoutRows(2)).thenReturn(formData1);

        when(reportPeriodDao.get(1)).thenReturn(reportPeriod);
        when(reportPeriodDao.get(2)).thenReturn(reportPeriod1);

        when(departmentDao.getDepartment(1)).thenReturn(department);

        Assert.assertTrue(formDataService.existFormData(1, FormDataKind.SUMMARY, 1, logger));
        Assert.assertEquals("Существует экземпляр налоговой формы \"Тестовый тип НФ\" типа \"Сводная\" в подразделении \"Тестовое подразделение\" периоде \"Тестовый период 2014\"", logger.getEntries().get(0).getMessage());
        Assert.assertEquals("Существует экземпляр налоговой формы \"Тестовый тип НФ\" типа \"Сводная\" в подразделении \"Тестовое подразделение\" периоде \"Второй тестовый период 2014\"", logger.getEntries().get(1).getMessage());
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
        when(periodService.getReportPeriod(1)).thenReturn(new ReportPeriod());
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
        when(dataRowDao.getSizeWithoutTotal(formData)).thenReturn(3);
        when(dataRowDao.getSizeWithoutTotal(formData1)).thenReturn(5);

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
        when(dataRowDao.getSizeWithoutTotal(formData)).thenReturn(3);
        when(dataRowDao.getSizeWithoutTotal(formData1)).thenReturn(5);

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
        TAUser user = new TAUser();
        user.setId(1);
        Logger logger = mock(Logger.class);
        TAUserInfo userInfo = mock(TAUserInfo.class);
        when(userInfo.getUser()).thenReturn(user);
        FormData formData = getFormData();
        LockData lockData = new LockData();
        lockData.setUserId(user.getId());

        when(lockDataService.lock(LockData.LOCK_OBJECTS.FORM_DATA.name() + "_" + formData.getId(), user.getId(), LockData.STANDARD_LIFE_TIME)).
                thenReturn(lockData);
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
        FormData formData = new FormData();
        formData.setState(WorkflowState.CREATED);
        Assert.assertFalse("Не должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData));

        FormData formData1 = new FormData();
        formData1.setState(WorkflowState.ACCEPTED);
        Assert.assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData1));

        FormData formData2 = new FormData();
        formData2.setState(WorkflowState.APPROVED);
        Assert.assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData2));

        FormData formData3 = new FormData();
        formData3.setState(WorkflowState.PREPARED);
        Assert.assertTrue("Должен участвовать в сквозной нумерации", formDataService.beInOnAutoNumeration(formData3));
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
