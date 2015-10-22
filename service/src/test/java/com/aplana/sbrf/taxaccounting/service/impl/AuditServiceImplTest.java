package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * User: lhaziev
 */
class TransactionHelperImpl implements TransactionHelper {

    @Override
    public <T> T executeInNewTransaction(TransactionLogic<T> logic) {
        return logic.execute();
    }

    @Override
    public <T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic) {
        return null;
    }
}


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("AuditServiceImplTest.xml")
public class AuditServiceImplTest {

    private static final Calendar CALENDAR = Calendar.getInstance();

    @Autowired
    private AuditService auditService;
    @Autowired
    private AuditDao auditDao;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PeriodService periodService;

    private TAUserInfo userInfo = new TAUserInfo();

    @Before
    public void init(){
        TAUser user = new TAUser();
        user.setDepartmentId(1);
        user.setLogin("user1");
        user.setRoles(new ArrayList<TARole>(){{
            add(new TARole(){{
                setName(TARole.ROLE_ADMIN);
            }});
            add(new TARole(){{
                setName(TARole.ROLE_CONTROL);
            }});
        }});
        userInfo.setIp("127.0.0.1");
        userInfo.setUser(user);
    }

    @Test
    public void testAdd(){
        Department department1 = new Department();
        department1.setName("Открытое акционерное общество \"Сбербанк России\"");
        department1.setId(0);

        Department department2 = new Department();
        department2.setName("Центральный аппарат");
        department2.setId(113);

        when(departmentService.getDepartment(0)).thenReturn(department1);
        when(departmentService.getParentTB(1)).thenReturn(department2);
        when(departmentService.getParentsHierarchy(1)).thenReturn("Центральный аппарат/Управление налогового планирования");

        auditService.add(FormDataEvent.MIGRATION, userInfo, 0, null, null, null, null, "MIGRATION", null);
        auditService.add(FormDataEvent.LOGIN, userInfo, 1, null, null, null, null, "LOGIN", null);

        ArgumentCaptor<LogSystem> argument = ArgumentCaptor.forClass(LogSystem.class);
        verify(auditDao, times(2)).add(argument.capture());

        assertEquals(argument.getAllValues().get(0).getFormDepartmentName(), "Открытое акционерное общество \"Сбербанк России\"");
        assertEquals(argument.getAllValues().get(0).getRoles(), TARole.ROLE_ADMIN + ", " + TARole.ROLE_CONTROL);

        assertEquals(argument.getAllValues().get(1).getFormDepartmentName(), "Центральный аппарат/Управление налогового планирования");
        assertEquals(argument.getAllValues().get(1).getUserDepartmentName(), "Центральный аппарат/Управление налогового планирования");
    }

    @Test
    public void testAddWithCorr(){

        Department department1 = new Department();
        department1.setName("Центральный аппарат");
        department1.setId(113);
        when(departmentService.getDepartment(department1.getId())).thenReturn(department1);
        when(departmentService.getParentsHierarchy(userInfo.getUser().getDepartmentId())).thenReturn("Центральный аппарат");
        when(departmentService.getParentsHierarchy(department1.getId())).thenReturn("Центральный аппарат/Управление налогового планирования");

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        CALENDAR.set(2015, Calendar.JANUARY, 1);
        departmentReportPeriod.setCorrectionDate(CALENDAR.getTime());
        CALENDAR.clear();
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2015);
        reportPeriod.setName("первый квартал");
        reportPeriod.setTaxPeriod(taxPeriod);
        when(periodService.getReportPeriod(reportPeriod.getId())).thenReturn(reportPeriod);

        FormData formData = new FormData();
        formData.setDepartmentId(department1.getId());
        formData.setDepartmentReportPeriodId(1);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setReportPeriodId(1);
        FormType type = new FormType();
        type.setName("217");
        formData.setFormType(type);

        auditService.add(FormDataEvent.MIGRATION, userInfo, null, formData, null, null);

        ArgumentCaptor<LogSystem> argument = ArgumentCaptor.forClass(LogSystem.class);
        verify(auditDao, atLeastOnce()).add(argument.capture());

        assertEquals(argument.getValue().getFormDepartmentName(), "Центральный аппарат/Управление налогового планирования");
        assertEquals(argument.getValue().getReportPeriodName(), "2015 первый квартал в корр.периоде 01.01.2015");
        assertEquals(argument.getValue().getRoles(), TARole.ROLE_ADMIN + ", " + TARole.ROLE_CONTROL);
    }

    @Test
    public void testAddForTemplate(){
        Department department1 = new Department();
        department1.setName("Центральный аппарат");
        department1.setId(113);
        when(departmentService.getDepartment(department1.getId())).thenReturn(department1);
        when(departmentService.getParentsHierarchy(userInfo.getUser().getDepartmentId())).thenReturn("Центральный аппарат");
        when(departmentService.getParentsHierarchy(department1.getId())).thenReturn("Центральный аппарат/Управление налогового планирования");

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        CALENDAR.set(2015, Calendar.JANUARY, 1);
        departmentReportPeriod.setCorrectionDate(CALENDAR.getTime());
        CALENDAR.clear();
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2015);
        reportPeriod.setName("первый квартал");
        reportPeriod.setTaxPeriod(taxPeriod);
        when(periodService.getReportPeriod(reportPeriod.getId())).thenReturn(reportPeriod);

        FormData formData = new FormData();
        formData.setDepartmentId(department1.getId());
        formData.setDepartmentReportPeriodId(1);
        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setReportPeriodId(1);
        FormType type = new FormType();
        type.setName("217");
        formData.setFormType(type);

        CALENDAR.set(2014, Calendar.JANUARY, 1);
        Date startDate = CALENDAR.getTime();
        CALENDAR.set(2015, Calendar.JANUARY, 1);
        Date endDate = CALENDAR.getTime();
        auditService.add(FormDataEvent.MIGRATION, userInfo, startDate, endDate, null, formData.getFormType().getName(), null, null);
        CALENDAR.clear();

        ArgumentCaptor<LogSystem> argument = ArgumentCaptor.forClass(LogSystem.class);
        verify(auditDao, atLeastOnce()).add(argument.capture());

        assertEquals(argument.getValue().getFormTypeName(), "217");
        assertEquals(argument.getValue().getReportPeriodName(), "С 2014 по 2015");
    }
}
