package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TAUserServiceTest.xml")
public class TAUserServiceTest {

    @Autowired
    private TAUserService userService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    TAUserDao userDao;

    @Autowired
    TARoleDao roleDao;

    private final static int USER_OPERATOR_ID = 14;

    private final static int USER_DEPARTMENT_ID = 1;

    private final static String USER_ROLE = "ROLE_CONTROL_UNP";

    private final static String USER_LOGIN_CONTROL = "controlBank";
    private final static String USER_LOGIN_OPER = "operTB";
    private final static String USER_LOGIN_OPER_VIEW = "operTB_View";

    private static TAUser user;

    @Before
    public void init() {
        List<Integer> listUserIds = new ArrayList<Integer>();
        listUserIds.add(1);
        listUserIds.add(2);
        listUserIds.add(3);

        TARole role = new TARole();
        role.setAlias(USER_ROLE);
        List<TARole> listUserRoles = new ArrayList<TARole>();
        listUserRoles.add(role);

        user = new TAUser();
        user.setId(USER_OPERATOR_ID);
        user.setLogin(USER_LOGIN_CONTROL);
        user.setEmail("controlBank@bank.ru");
        user.setDepartmentId(USER_DEPARTMENT_ID);
        user.setRoles(listUserRoles);
        user.setActive(true);

        when(userDao.getUser(USER_OPERATOR_ID)).thenReturn(user);
        when(userDao.getUser(1)).thenReturn(user);
        when(userDao.getUser(2)).thenReturn(user);
        when(userDao.getUser(3)).thenReturn(user);
        when(userDao.getAllUserIds()).thenReturn(listUserIds);
        when(userDao.checkUserRole(USER_ROLE)).thenReturn(1);
        when(userService.existsUser(USER_LOGIN_CONTROL)).thenReturn(false);
        when(userService.existsUser(USER_LOGIN_OPER)).thenReturn(true);

        when(departmentService.getDepartment(USER_DEPARTMENT_ID)).thenReturn(new Department());
        when(departmentService.existDepartment(USER_DEPARTMENT_ID)).thenReturn(true);
    }

    @Test
    public void testServiceGetUser() {
        assertEquals(user.getLogin(), userService.getUser(USER_OPERATOR_ID).getLogin());
    }

    @Test
    public void testListAllUsers() {
        assertEquals(3, userService.listAllUsers().size());
    }

    @Test
    public void testListAllFullUsers() {
        assertEquals(3, userService.listAllFullActiveUsers().size());
    }

    @Test
    public void testGetUsersViewByFilter() {
        userService.getUsersViewWithFilter(any(MembersFilterData.class));
        verify(userDao).getUsersViewWithFilter(any(MembersFilterData.class));
    }
}
