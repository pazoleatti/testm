package com.aplana.sbrf.taxaccounting.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.api.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TAUserDaoTest.xml"})
@Transactional
public class TAUserDaoTest {
	@Autowired
	private TAUserDao userDao;
	
	private static String LOGIN_CONTROL_BANK = "controlBank";
	private static String LOGIN_TEST_BANK = "testBank";
	
	@Test
	public void testGetById() {
		TAUser user = userDao.getUser(1);
		Assert.assertEquals(1, user.getId());
		Assert.assertEquals("controlBank", user.getLogin());
		Assert.assertEquals(Department.ROOT_BANK_ID, user.getDepartmentId());
		Assert.assertTrue(user.hasRole(TARole.ROLE_CONTROL));
		Assert.assertEquals("controlBank@bank.ru", user.getEmail());
		Assert.assertTrue(user.isActive());
	}
	
	@Test(expected=DaoException.class)
	public void testGetByIncorrectId() {
		userDao.getUser(-1);
	}
	
	
	@Test
	public void testGetUserIdByLogin() {
		Assert.assertEquals(1, userDao.getUserIdByLogin(LOGIN_CONTROL_BANK));
	}

	@Test
	public void testCreateUser() {
		TARole role = new TARole();
		role.setId(1);
		role.setAlias("ROLE_OPER");
		role.setName("Контролёр");
		List<TARole> roles = new ArrayList<TARole>();
		roles.add(role);
		TAUser user = new TAUser();
		user.setActive(true);
		user.setDepartmentId(Department.ROOT_BANK_ID);
		user.setEmail("controlBank@bank.ru");
		user.setName("");
		user.setLogin(LOGIN_TEST_BANK);
		user.setRoles(roles);
		
		userDao.createUser(user);
		int userId = userDao.getUserIdByLogin(LOGIN_TEST_BANK);
		user.setId(userId);
		Assert.assertEquals(user.getDepartmentId(), userDao.getUser(userId).getDepartmentId());
		Assert.assertEquals(user.getLogin(), userDao.getUser(userId).getLogin());
		Assert.assertEquals(user.getRoles().get(0).getAlias(), userDao.getUser(userId).getRoles().get(0).getAlias());
	}
	
	@Test
	public void testSetUserIsActive() {
		TAUser user = new TAUser();
		user.setId(1);
		user.setActive(false);
		user.setLogin("controlBank");
		
		int userId = userDao.getUserIdByLogin(LOGIN_CONTROL_BANK);
		TAUser userDB = userDao.getUser(userId);
		Assert.assertEquals(1, userDB.getDepartmentId());
		
		userDao.setUserIsActive(user.getId(),user.isActive());
		userDB = userDao.getUser(userId);
		Assert.assertFalse(userDB.isActive());
	}
	
	@Test
	public void testUpdateUser(){
		TAUser user = new TAUser();
		user.setId(1);
		user.setActive(false);
		user.setDepartmentId(3);
		user.setEmail("@sard");
		user.setLogin(LOGIN_CONTROL_BANK);
		
		TARole role = new TARole();
		role.setAlias("ROLE_OPER");
		role.setName("Оператор");
		List<TARole> roles = new ArrayList<TARole>();
		roles.add(role);
		TARole role1 = new TARole();
		role1.setAlias("ROLE_CONTROL_UNP");
		role1.setName("Контролёр УНП");
		roles.add(role1);
		
		user.setRoles(roles);
		
		Assert.assertEquals(1,userDao.getUser(user.getId()).getDepartmentId());
		Assert.assertEquals("controlBank@bank.ru",userDao.getUser(user.getId()).getEmail());
		Assert.assertEquals("Контролёр Банка",userDao.getUser(user.getId()).getName());
		Assert.assertEquals("ROLE_CONTROL",userDao.getUser(userDao.getUserIdByLogin(user.getLogin())).getRoles().get(0).getAlias());
		userDao.updateUser(user);
		Assert.assertEquals(3,userDao.getUser(user.getId()).getDepartmentId());
		Assert.assertEquals("@sard",userDao.getUser(user.getId()).getEmail());
		Assert.assertEquals("Контролёр Банка",userDao.getUser(user.getId()).getName());
		Assert.assertEquals("ROLE_OPER",userDao.getUser(userDao.getUserIdByLogin(user.getLogin())).getRoles().get(0).getAlias());
		Assert.assertEquals(2,userDao.getUser(userDao.getUserIdByLogin(user.getLogin())).getRoles().size());
	}
	
	@Test
	public void testGetUserIds(){
		Assert.assertEquals(3, userDao.getUserIds().size());
	}
	
	@Test
	public void testCheckUserRole(){
		Assert.assertEquals(1, userDao.checkUserRole("ROLE_OPER"));
	}
}
