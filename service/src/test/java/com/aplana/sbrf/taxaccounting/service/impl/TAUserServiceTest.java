package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.WSException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TAUserServiceTest {
	
	private static TAUserServiceImpl service;
	
	private final static int USER_OPERATOR_ID = 14;
	
	private final static int USER_DEPARTMENT_ID = 1;
	
	private final static String USER_ROLE = "ROLE_CONTROL_UNP";
	
	private final static String USER_LOGIN = "controlBank";
	
	private static TAUser user;
	
	@BeforeClass
	public static void init(){
		service = new TAUserServiceImpl();
		TAUserDao userDao = mock(TAUserDao.class);
		DepartmentDao depDao = mock(DepartmentDao.class);
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
		user.setLogin(USER_LOGIN);
		user.setEmail("controlBank@bank.ru");
		user.setDepartmentId(USER_DEPARTMENT_ID);
		user.setRoles(listUserRoles);
        user.setActive(true);
		
		when(userDao.getUser(USER_OPERATOR_ID)).thenReturn(user);
		when(userDao.getUser(1)).thenReturn(user);
		when(userDao.getUser(2)).thenReturn(user);
		when(userDao.getUser(3)).thenReturn(user);
		when(userDao.createUser(user)).thenReturn(user.getId());
		when(userDao.getUserIds()).thenReturn(listUserIds);
		when(userDao.checkUserRole(USER_ROLE)).thenReturn(1);
		when(userDao.checkUserLogin(USER_LOGIN)).thenReturn(0);
		
		when(depDao.getDepartment(USER_DEPARTMENT_ID)).thenReturn(new Department());
		ReflectionTestUtils.setField(service, "userDao", userDao);
		ReflectionTestUtils.setField(service, "departmentDao", depDao);
	}
	
	@Test
	public void testServiceGetUser(){
		assertEquals(user.getLogin(),service.getUser(USER_OPERATOR_ID).getLogin());
	}
	
	@Test
	public void testServiceSetUserIsActive(){
		service.setUserIsActive(user.getLogin(), false);
	}
	
	@Test(expected = WSException.class)
	public void testServiceUpdateUserInfo(){
		service.updateUser(user);
	}
	
	@Test
	public void testServiceCreateUser(){
		assertEquals(user.getId(),service.createUser(user));
	}
	
	@Test
	public void testListAllUsers(){
		assertEquals(3, service.listAllUsers().size());
	}

    @Test
    public void testListAllFullUsers(){
        assertEquals(3, service.listAllFullActiveUsers().size());
    }

}
