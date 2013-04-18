package com.aplana.sbrf.taxaccounting.service.impl;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.TAUser;

public class TAUserServiceTest {
	
	private static TAUserServiceImpl service;
	
	private final static int USER_OPERATOR_ID = 14;
	
	@Test
	public void testService(){
		service = new TAUserServiceImpl();
		TAUserDao userDao = mock(TAUserDao.class);
		
		when(userDao.getUser(USER_OPERATOR_ID)).thenReturn(new TAUser());
		ReflectionTestUtils.setField(service, "userDao", userDao);
	}

}
