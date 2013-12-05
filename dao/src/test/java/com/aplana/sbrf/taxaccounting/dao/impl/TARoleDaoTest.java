package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TARoleDaoTest.xml"})
@Transactional
public class TARoleDaoTest {
	@Autowired
	TARoleDao taRoleDao;

	@Test
	public void testGetRole() {
		TARole role = taRoleDao.getRole(1);
		Assert.assertEquals(1, role.getId());
		Assert.assertEquals("Контролёр", role.getName());
		Assert.assertEquals("ROLE_CONTROL", role.getAlias());
	}

	@Test
	public void testGetAll() {
		Assert.assertEquals(3, taRoleDao.getAll().size());
	}
}
