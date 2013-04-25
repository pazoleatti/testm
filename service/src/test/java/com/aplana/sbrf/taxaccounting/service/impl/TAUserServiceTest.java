package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.TAUser;

import static org.junit.Assert.assertEquals;

public class TAUserServiceTest {
	
	private static TAUserServiceImpl service;
	
	private final static int USER_OPERATOR_ID = 14;
	
	private static TAUser user;
	
	@BeforeClass
	public static void init(){
		service = new TAUserServiceImpl();
		TAUserDao userDao = mock(TAUserDao.class);
		List<Integer> listUserIds = new ArrayList<Integer>();
		listUserIds.add(1);
		listUserIds.add(2);
		listUserIds.add(3);
		
		user = new TAUser();
		user.setId(USER_OPERATOR_ID);
		user.setLogin("controlBank");
		user.setEmail("controlBank@bank.ru");
		
		when(userDao.getUser(USER_OPERATOR_ID)).thenReturn(user);
		when(userDao.getUser(1)).thenReturn(user);
		when(userDao.getUser(2)).thenReturn(user);
		when(userDao.getUser(3)).thenReturn(user);
		when(userDao.createUser(user)).thenReturn(user.getId());
		when(userDao.getUserIds()).thenReturn(listUserIds);
		ReflectionTestUtils.setField(service, "userDao", userDao);
	}
	
	@Test
	public void testServiceGetUser(){
		assertEquals(user.getLogin(),service.getUser(USER_OPERATOR_ID).getLogin());
	}
	
	@Test
	public void testServiceSetUserIsActive(){
		service.setUserIsActive(user.getLogin(), false);
	}
	
	@Test
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

}
