package com.aplana.sbrf.taxaccounting.dao.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

//TODO: переработать, чтобы не было необходимости поднимать полный файл dao.xml, а то получается integration-тест вместо unit-теста 
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class TAUserDaoTest {
	@Autowired
	private TAUserDao userDao;
	
	@Test
	public void testGetById() {
		TAUser user = userDao.getUser(Constants.BANK_CONTROL_USER_ID);
		Assert.assertEquals(Constants.BANK_CONTROL_USER_ID, user.getId());
		Assert.assertEquals(Constants.BANK_CONTROL_USER_LOGIN, user.getLogin());
		Assert.assertEquals(Department.ROOT_BANK_ID, user.getDepartmentId());
		Assert.assertTrue(user.hasRole(TARole.ROLE_CONTROL));
	}
	
	@Test(expected=DaoException.class)
	public void testGetByIncorrectId() {
		userDao.getUser(-1);
	}
	
	@Test
	public void testGetByLogin() {
		TAUser user = userDao.getUser(Constants.BANK_CONTROL_USER_LOGIN);
		Assert.assertEquals(Constants.BANK_CONTROL_USER_ID, user.getId());
		Assert.assertEquals(Constants.BANK_CONTROL_USER_LOGIN, user.getLogin());
		Assert.assertEquals(Department.ROOT_BANK_ID, user.getDepartmentId());
		Assert.assertTrue(user.hasRole(TARole.ROLE_CONTROL));
	}
	
	@Test(expected=DaoException.class)
	public void testGetByIncorrectLogin() {
		userDao.getUser("nonexistantUser");
	}
}
