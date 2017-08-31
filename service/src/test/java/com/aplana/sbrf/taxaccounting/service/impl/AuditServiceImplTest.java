package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Autowired
    private ServerInfo serverInfo;

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
                setName(TARole.N_ROLE_CONTROL_NS);
            }});
        }});
        userInfo.setIp("127.0.0.1");
        userInfo.setUser(user);
    }

    @Test
    public void testAdd() {
        Department department1 = new Department();
        department1.setName("Открытое акционерное общество \"Сбербанк России\"");
        department1.setId(0);

        Department department2 = new Department();
        department2.setName("Центральный аппарат");
        department2.setId(113);

        when(departmentService.getDepartment(0)).thenReturn(department1);
        when(departmentService.getParentTB(1)).thenReturn(department2);
        when(departmentService.getParentsHierarchy(1)).thenReturn("Центральный аппарат/Управление налогового планирования");
        when(serverInfo.getServerName()).thenReturn("server");

        auditService.add(FormDataEvent.CALCULATE, userInfo, 0, null, null, null, null, "MIGRATION", null);
        auditService.add(FormDataEvent.LOGIN, userInfo, 1, null, null, null, null, "LOGIN", null);

        ArgumentCaptor<LogSystem> argument = ArgumentCaptor.forClass(LogSystem.class);
        verify(auditDao, times(2)).add(argument.capture());

        assertEquals(argument.getAllValues().get(0).getFormDepartmentName(), "Открытое акционерное общество \"Сбербанк России\"");
        assertEquals(argument.getAllValues().get(0).getRoles(), TARole.ROLE_ADMIN + ", " + TARole.N_ROLE_CONTROL_NS);

        assertEquals(argument.getAllValues().get(1).getFormDepartmentName(), "Центральный аппарат/Управление налогового планирования");
        assertEquals(argument.getAllValues().get(1).getUserDepartmentName(), "Центральный аппарат/Управление налогового планирования");

        assertEquals(argument.getAllValues().get(0).getServer(), "server");
        assertEquals(argument.getAllValues().get(1).getServer(), "server");
    }
}