package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.shared.FormDataCompositionService;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class FormDataServiceTest {

    private FormDataServiceImpl formDataService = new FormDataServiceImpl();

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

        when(departmentFormTypeDao.getFormDestinations(2, 1, FormDataKind.PRIMARY)).thenReturn(new ArrayList<DepartmentFormType>());
        when(departmentFormTypeDao.getFormDestinations(1, 1, FormDataKind.PRIMARY)).thenReturn(list);
        ReflectionTestUtils.setField(formDataService, "departmentFormTypeDao", departmentFormTypeDao);


        FormDataDao formDataDao = mock(FormDataDao.class);
        FormData formData1 = new FormData();
        formData1.setId(2L);
        when(formDataDao.find(departmentFormType.getFormTypeId(), departmentFormType.getKind(), departmentFormType.getDepartmentId(), formData.getReportPeriodId())).thenReturn(formData1);
        when(formDataDao.get(formData1.getId(), false)).thenReturn(formData);

        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                for (DepartmentFormType departmentFormType1 : list){
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
}
