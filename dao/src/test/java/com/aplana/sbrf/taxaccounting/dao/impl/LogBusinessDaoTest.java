package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"LogBusinessDaoTest.xml"})
@Transactional
public class LogBusinessDaoTest {

	@Autowired
	private LogBusinessDao logBusinessDao;

	@Test
	public void testDeclarationGet() {
		LogBusiness d = logBusinessDao.getDeclarationLogsBusiness(1).get(0);
		assertEquals(1, d.getId());
		assertEquals(1, d.getEventId());
		assertEquals(1, d.getUserId());
		assertEquals("operator", d.getRoles());
		assertEquals(Integer.valueOf(1), d.getDeclarationId());
		assertEquals(null, d.getFormId());
		assertEquals("the best note", d.getNote());
	}

	@Test
	public void testDeclarationAdd() {
		LogBusiness d = new LogBusiness();
		d.setId(3);
		d.setLogDate(new Date());
		d.setDeclarationId(1);
		d.setEventId(3);
		d.setUserId(1);
		d.setRoles("operator");
		d.setNote("the best note");
		logBusinessDao.add(d);

		d = logBusinessDao.getDeclarationLogsBusiness(1).get(1);
		assertEquals(3, d.getId());
		assertEquals(3, d.getEventId());
		assertEquals(1, d.getUserId());
		assertEquals("operator", d.getRoles());
		assertEquals(Integer.valueOf(1), d.getDeclarationId());
		assertEquals("the best note", d.getNote());
	}
}
