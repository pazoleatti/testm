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
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TAUserDaoTest.xml"})
@Transactional
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
		Assert.assertEquals("controlBank@bank.ru", user.getEmail());
		Assert.assertEquals("F32C1F04-7860-43CA-884F-39CC1D740064", user.getUuid());
		Assert.assertTrue(user.isActive());
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
		Assert.assertEquals("controlBank@bank.ru", user.getEmail());
		Assert.assertEquals("F32C1F04-7860-43CA-884F-39CC1D740064", user.getUuid());
		Assert.assertTrue(user.isActive());
	}

	@Test
	public void testListRoles() {
		Assert.assertEquals(3, userDao.listRolesAll().size());
	}
	
	@Test(expected=DaoException.class)
	public void testGetByIncorrectLogin() {
		userDao.getUser("nonexistantUser");
	}
	
	@Test
	public void testAddUser() {
		TARole role = new TARole();
		role.setId(1);
		role.setAlias("ROLE_CONTROL");
		role.setName("Контролёр");
		List<TARole> roles = new ArrayList<TARole>();
		roles.add(role);
		TAUser user = new TAUser();
		user.setActive(true);
		user.setDepartmentId(Department.ROOT_BANK_ID);
		user.setEmail("controlBank@bank.ru");
		user.setName("");
		user.setLogin("testBank");
		user.setRoles(roles);
		user.setUuid("23435");
		
		userDao.addUser(user);
		Assert.assertEquals(user.getUuid(), userDao.getUser("testBank").getUuid());
	}
	
	@Test
	public void testUpdateUserInfo() {
		TAUser user = new TAUser();
		user.setActive(false);
		user.setLogin("controlBank");
		
		TAUser userDB = userDao.getUser("controlBank");
		Assert.assertEquals(1, userDB.getDepartmentId());
		
		userDao.setUserIsActive(user);
		userDB = userDao.getUser("controlBank");
		Assert.assertFalse(userDB.isActive());
	}
	
	@Test
	public void testUpdateUserRoles() {
		
		TARole role = new TARole();
		role.setId(2);
		role.setAlias("ROLE_OPER");
		role.setName("Оператор");
		List<TARole> roles = new ArrayList<TARole>();
		roles.add(role);
		TARole role1 = new TARole();
		role1.setId(3);
		role1.setAlias("ROLE_CONTROL_UNP");
		role1.setName("Контролёр УНП");
		roles.add(role1);
		
		TAUser user = new TAUser();
		user.setId(1);
		user.setActive(false);
		user.setDepartmentId(3);
		user.setLogin("controlBank");
		user.setRoles(roles);
		
		Assert.assertEquals("ROLE_CONTROL",userDao.getUser(user.getLogin()).getRoles().get(0).getAlias());
		
		userDao.updateUserRoles(user);
		Assert.assertEquals("ROLE_OPER",userDao.getUser(user.getLogin()).getRoles().get(0).getAlias());
		Assert.assertEquals(2,userDao.getUser(user.getLogin()).getRoles().size());
	}
}
