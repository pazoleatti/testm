package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TARoleDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TARoleDaoTest {
	@Autowired
	TARoleDao taRoleDao;

	@Test
	public void testGetRole() {
		TARole role = taRoleDao.getRole(1);
		Assert.assertEquals(1, role.getId());
		Assert.assertEquals("Контролёр", role.getName());
		Assert.assertEquals("N_ROLE_CONTROL", role.getAlias());
	}

	@Test
	public void testGetAll() {
		Assert.assertEquals(3, taRoleDao.getAll().size());
	}
	@Test
	public void testGetRoleByAlias() {
        Assert.assertEquals(1, taRoleDao.getRoleByAlias("N_ROLE_CONTROL").getId());
        Assert.assertEquals("N_ROLE_OPER", taRoleDao.getRoleByAlias("N_ROLE_OPER").getAlias());
        Assert.assertEquals("Контролёр УНП", taRoleDao.getRoleByAlias("N_ROLE_CONTROL_UNP").getName());
	}
}
