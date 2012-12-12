package com.aplana.sbrf.taxaccounting.dao.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TAUserDaoTest.xml"})
@DirtiesContext
public class TAUserDaoTest {
	@Autowired
	private TAUserDao userDao;
	
	@Test
	public void testGetById() {
		TAUser user = userDao.getUser(1);
		Assert.assertEquals(1, user.getId());
		Assert.assertEquals("controlBank", user.getLogin());
		Assert.assertEquals(Department.ROOT_BANK_ID, user.getDepartmentId());
		Assert.assertTrue(user.hasRole(TARole.ROLE_CONTROL));
	}
	
	@Test(expected=DaoException.class)
	public void testGetByIncorrectId() {
		userDao.getUser(-1);
	}
	
	@Test
	public void testGetByLogin() {
		TAUser user = userDao.getUser("controlBank");
		Assert.assertEquals(1, user.getId());
		Assert.assertEquals("controlBank", user.getLogin());
		Assert.assertEquals(Department.ROOT_BANK_ID, user.getDepartmentId());
		Assert.assertTrue(user.hasRole(TARole.ROLE_CONTROL));
	}
	
	@Test(expected=DaoException.class)
	public void testGetByIncorrectLogin() {
		userDao.getUser("nonexistantUser");
	}
}
