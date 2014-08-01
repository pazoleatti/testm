package com.aplana.sbrf.taxaccounting.service.impl;


import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * User: lhaziev
 */
public class AuditServiceImplTest {

    private AuditService auditService = new AuditServiceImpl();
    private AuditDao auditDao;

    @Before
    public void init() {
        auditDao = mock(AuditDao.class);

        Department department1 = new Department();
        department1.setName("Открытое акционерное общество \"Сбербанк России\"");
        department1.setId(0);

        Department department2 = new Department();
        department2.setName("Центральный аппарат");
        department2.setId(113);

        DepartmentService departmentService = mock(DepartmentService.class);
        when(departmentService.getDepartment(0)).thenReturn(department1);
        when(departmentService.getParentTB(1)).thenReturn(department2);
        when(departmentService.getParentsHierarchy(1)).thenReturn("Центральный аппарат/Управление налогового планирования");
        ReflectionTestUtils.setField(auditService, "departmentService", departmentService);

        auditDao = mock(AuditDao.class);
        ReflectionTestUtils.setField(auditService, "auditDao", auditDao);

        TransactionHelper tx = new TransactionHelper() {
            @Override
            public void executeInNewTransaction(TransactionLogic logic) {
                logic.execute();
            }

            @Override
            public <T> T returnInNewTransaction(TransactionLogic<T> logic) {
                return null;
            }
        };
        ReflectionTestUtils.setField(auditService, "tx", tx);

    }

    @Test
    public void testAdd(){
        TAUser user = new TAUser();
        user.setLogin("user1");
        user.setRoles(new ArrayList<TARole>(){{
                add(new TARole(){{
                    setName(TARole.ROLE_ADMIN);
                }});
                add(new TARole(){{
                    setName(TARole.ROLE_CONTROL);
                }});
        }});
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setIp("127.0.0.1");
        userInfo.setUser(user);

        auditService.add(FormDataEvent.MIGRATION, userInfo, 0, null, null, null, null, "MIGRATION");
        auditService.add(FormDataEvent.LOGIN, userInfo, 1, null, null, null, null, "LOGIN");

        ArgumentCaptor<LogSystem> argument = ArgumentCaptor.forClass(LogSystem.class);
        verify(auditDao, times(2)).add(argument.capture());

        assertEquals(argument.getAllValues().get(0).getFormDepartmentName(), "Открытое акционерное общество \"Сбербанк России\"");
        assertEquals(argument.getAllValues().get(0).getRoles(), TARole.ROLE_ADMIN + ", " + TARole.ROLE_CONTROL);
        assertEquals(argument.getAllValues().get(0).getDepartmentTBId(), null);

        assertEquals(argument.getAllValues().get(1).getFormDepartmentName(), "Центральный аппарат/Управление налогового планирования");
        assertEquals(argument.getAllValues().get(1).getUserDepartmentName(), "Открытое акционерное общество \"Сбербанк России\"");
        assertEquals(argument.getAllValues().get(1).getDepartmentTBId(), new Integer(113));
    }
}
