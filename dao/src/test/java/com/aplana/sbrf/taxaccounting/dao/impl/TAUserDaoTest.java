package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TAUserDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
		Assert.assertEquals(1, user.getDepartmentId());
		Assert.assertTrue(user.hasRole("N_ROLE_CONTROL_NS"));
		Assert.assertEquals("controlBank@bank.ru", user.getEmail());
		Assert.assertTrue(user.isActive());
	}
	
	@Test(expected=DaoException.class)
	public void testGetByIncorrectId() {
		userDao.getUser(-1);
	}


	@Test
	public void testGetUserIdByLoginLower() {
		Assert.assertEquals(1, userDao.getUserIdByLogin("controlBank"));
	}

    public void testGetUserIdByLogin() {
        Assert.assertNull(userDao.getUserIdByLogin(LOGIN_CONTROL_BANK));
    }

	@Test
	public void testCreateUser() {
		TARole role = new TARole();
		role.setId(1);
		role.setAlias("N_ROLE_OPER");
		role.setName("Контролёр");
        role.setTaxType(TaxType.NDFL);
		List<TARole> roles = new ArrayList<TARole>();
		roles.add(role);
		TAUser user = new TAUser();
		user.setActive(true);
		user.setDepartmentId(1);
		user.setEmail("controlBank@bank.ru");
		user.setName("");
		user.setLogin(LOGIN_TEST_BANK.toLowerCase());
		user.setRoles(roles);
        user.setAsnuIds(new ArrayList<Long>());

        userDao.createUser(user);
		int userId = userDao.getUserIdByLogin(LOGIN_TEST_BANK.toLowerCase());
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
		
		userDao.setUserIsActive(user.getId(),user.isActive()?1:0);
		userDB = userDao.getUser(userId);
		Assert.assertFalse(userDB.isActive());
	}

    //@CacheEvict(value = "User", allEntries = true)
	@Test
	public void testUpdateUser(){
		TAUser user = new TAUser();
		user.setId(1);
		user.setActive(false);
		user.setDepartmentId(3);
		user.setEmail("@sard");
		user.setLogin(LOGIN_CONTROL_BANK);
        user.setAsnuIds(new ArrayList<Long>());

		TARole role = new TARole();
		role.setAlias("N_ROLE_OPER");
		role.setName("Оператор");
        role.setTaxType(TaxType.NDFL);
        List<TARole> roles = new ArrayList<TARole>();
		roles.add(role);
		TARole role1 = new TARole();
		role1.setAlias("N_ROLE_CONTROL_UNP");
		role1.setName("Контролёр УНП");
        role1.setTaxType(TaxType.NDFL);
		roles.add(role1);
		
		user.setRoles(roles);
		
		Assert.assertEquals(1,userDao.getUser(user.getId()).getDepartmentId());
		Assert.assertEquals("controlBank@bank.ru",userDao.getUser(user.getId()).getEmail());
		Assert.assertEquals("Контролёр Банка",userDao.getUser(user.getId()).getName());
		Assert.assertEquals("N_ROLE_CONTROL_NS",userDao.getUser(userDao.getUserIdByLogin(user.getLogin())).getRoles().get(0).getAlias());
		userDao.updateUser(user);
		Assert.assertEquals(3,userDao.getUser(user.getId()).getDepartmentId());
		Assert.assertEquals("@sard",userDao.getUser(user.getId()).getEmail());
		Assert.assertEquals("Контролёр Банка",userDao.getUser(user.getId()).getName());
        Assert.assertEquals("N_ROLE_OPER",userDao.getUser(userDao.getUserIdByLogin(user.getLogin())).getRoles().get(0).getAlias());
        Assert.assertEquals("N_ROLE_CONTROL_UNP",userDao.getUser(userDao.getUserIdByLogin(user.getLogin())).getRoles().get(1).getAlias());
		Assert.assertEquals(2,userDao.getUser(userDao.getUserIdByLogin(user.getLogin())).getRoles().size());
	}
	
	@Test
	public void testGetUserIds(){
		Assert.assertEquals(3, userDao.getUserIds().size());
	}
	
	@Test
	public void testCheckUserRole(){
		Assert.assertEquals(1, userDao.checkUserRole("N_ROLE_OPER"));
	}

	@Test
	public void testGetByFilter() {
		MembersFilterData filter = new MembersFilterData();
		filter.setUserName("Контролёр Банка");
		Assert.assertEquals(1, userDao.getByFilter(filter).size());
		Assert.assertEquals("Контролёр Банка", userDao.getUser(userDao.getByFilter(filter).get(0)).getName());

		filter.setActive(false);
		filter.setUserName(null);
		Assert.assertEquals(1, userDao.getByFilter(filter).size());
		Assert.assertEquals(3L, (long)userDao.getByFilter(filter).get(0));

		Set<Integer> depIds = new HashSet<Integer>();
		depIds.add(2);
		depIds.add(3);
		filter.setDepartmentIds(depIds);
		filter.setActive(null);
		Assert.assertEquals(2, userDao.getByFilter(filter).size());
		filter.setActive(false);
		Assert.assertEquals(1, userDao.getByFilter(filter).size());

		filter.setRoleIds(Arrays.asList((long) 1));
		filter.setDepartmentIds(null);
		filter.setActive(null);
		Assert.assertEquals(3, userDao.getByFilter(filter).size());
		filter.setActive(true);
		Assert.assertEquals(2, userDao.getByFilter(filter).size());
	}

    @Test
    public void testGetUsersByFilter(){
        MembersFilterData membersFilterData = new MembersFilterData();
        membersFilterData.setRoleIds(new ArrayList<Long>(Arrays.asList(2L,3L)));
        Assert.assertEquals(0, userDao.getByFilter(membersFilterData).size());
        membersFilterData.setRoleIds(new ArrayList<Long>(Arrays.asList(1L)));
        Assert.assertEquals(3, userDao.getByFilter(membersFilterData).size());
    }

}
