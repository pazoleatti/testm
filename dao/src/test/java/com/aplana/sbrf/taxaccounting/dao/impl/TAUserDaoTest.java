package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
