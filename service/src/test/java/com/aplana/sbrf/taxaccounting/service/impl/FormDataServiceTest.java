package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
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
        when(formDataDao.get(formData1.getId())).thenReturn(formData);

        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                int formId = ((Long) invocation.getArguments()[0]).intValue();
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
        when(formDataAccessService.canDelete(userInfo, formData1.getId())).thenReturn(true);
        ReflectionTestUtils.setField(formDataService, "formDataAccessService", formDataAccessService);

        AuditService auditService = mock(AuditService.class);
        ReflectionTestUtils.setField(formDataService, "auditService", auditService);

        formDataService.compose(WorkflowMove.APPROVED_TO_ACCEPTED, formData, userInfo, logger);
        // проверяем что источник удален
        assertTrue(list.size() == 0);
    }
}
